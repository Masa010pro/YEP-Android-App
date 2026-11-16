package com.gruop11.yepapp.yep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gruop11.yepapp.yep.ai.YepAiEngine
import com.gruop11.yepapp.yep.data.SeedData
import com.gruop11.yepapp.yep.data.YepLocalRepository
import com.gruop11.yepapp.yep.i18n.YepStrings
import com.gruop11.yepapp.yep.i18n.YepTranslations
import com.gruop11.yepapp.yep.model.*
import com.gruop11.yepapp.yep.sync.YepSyncMapper
import com.gruop11.yepapp.platform.sync.YepSyncClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class YepUiState(
    val isLoading: Boolean = true,
    val snapshot: YepStateSnapshot = SeedData.createInitialSnapshot(),
    val activeUserId: String? = null,
    val language: AppLanguage = AppLanguage.EN,
    val isOfflineDataSaver: Boolean = false,
    val syncedVersion: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncOk: Boolean = true,
    val snackbarMessage: String? = null,
    val aiChatHistory: List<AiChatMessage> = emptyList(),
    val isAiAdvisorThinking: Boolean = false,
    val facilitatorDiagnosticOutput: String? = null,
    val isFacilitatorAiRunning: Boolean = false,
    val exportedImpactSummary: String? = null,
) {
    val strings: YepStrings get() = YepTranslations.forLanguage(language)
    val activeUser: UserProfile? get() = snapshot.users.firstOrNull { it.id == activeUserId }

    val visibleNotifications: List<YepNotification>
        get() {
            val user = activeUser ?: return emptyList()
            return snapshot.notifications
                .filter { notif ->
                    val dismissed = user.id in notif.dismissedByUserIds
                    val targeted = notif.recipientId == user.id || notif.recipientRole == user.role
                    !dismissed && targeted
                }
                .sortedByDescending { it.createdAt }
        }

    val unreadNotificationCount: Int
        get() {
            val uid = activeUserId ?: return 0
            return visibleNotifications.count { uid !in it.readByUserIds }
        }
}

class YepViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YepLocalRepository(application.applicationContext)
    private val aiEngine = YepAiEngine()
    private var syncClientId: String = "android_default"
    private var lastPushedVersion: Int = 0

    private val _uiState = MutableStateFlow(YepUiState())
    val uiState: StateFlow<YepUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            syncClientId = repository.getOrCreateClientId()
            val snapshot = repository.loadOrSeedSnapshot()
            val (uid, lang, offline) = repository.readInitialPrefs()
            val validUid = uid?.takeIf { id -> snapshot.users.any { it.id == id && !it.isSuspended } }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snapshot = snapshot,
                    activeUserId = validUid,
                    language = lang,
                    isOfflineDataSaver = offline,
                    aiChatHistory = listOf(
                        AiChatMessage(
                            id = "ai_welcome",
                            isUser = false,
                            text = YepTranslations.forLanguage(lang).aiAdvisorGreeting,
                            timestamp = System.currentTimeMillis(),
                        ),
                    ),
                )
            }

            if (!offline) {
                initialSyncHandshake()
            }

            // Real-time two-way sync polling loop
            while (isActive) {
                delay(3000L)
                if (!_uiState.value.isOfflineDataSaver) {
                    pullRemoteUpdates()
                }
            }
        }
    }

    private suspend fun initialSyncHandshake() {
        _uiState.update { it.copy(isSyncing = true) }
        val pull = YepSyncClient.pullState(sinceVersion = 0)
        if (pull != null && pull.state != null && (pull.state.optJSONArray("users")?.length() ?: 0) > 0) {
            applyRemoteState(pull)
        } else {
            val ver = YepSyncClient.pushState(syncClientId, YepSyncMapper.toJson(_uiState.value.snapshot))
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncedVersion = ver ?: it.syncedVersion,
                    lastSyncOk = ver != null,
                )
            }
        }
    }

    private suspend fun pullRemoteUpdates() {
        val since = _uiState.value.syncedVersion
        val pull = YepSyncClient.pullState(sinceVersion = since)
        if (pull == null) {
            _uiState.update { it.copy(lastSyncOk = false, isSyncing = false) }
            return
        }
        if (pull.unchanged || pull.state == null) {
            _uiState.update {
                it.copy(
                    syncedVersion = maxOf(it.syncedVersion, pull.version),
                    lastSyncOk = true,
                    isSyncing = false,
                )
            }
            return
        }
        if (pull.lastClientId == syncClientId && pull.version <= lastPushedVersion) {
            _uiState.update {
                it.copy(
                    syncedVersion = maxOf(it.syncedVersion, pull.version),
                    lastSyncOk = true,
                    isSyncing = false,
                )
            }
            return
        }
        applyRemoteState(pull)
    }

    private suspend fun applyRemoteState(pull: YepSyncClient.PullResult) {
        val remoteJson = pull.state ?: return
        val current = _uiState.value
        val previousEmail = current.activeUser?.email
        val merged = YepSyncMapper.mergeFromRemoteJson(remoteJson, current.snapshot)
        repository.saveSnapshot(merged)

        val resolvedActiveId = current.activeUserId?.let { oldId ->
            merged.users.firstOrNull { it.id == oldId && !it.isSuspended }?.id
                ?: previousEmail?.let { em ->
                    merged.users.firstOrNull {
                        it.email.equals(em, ignoreCase = true) && !it.isSuspended
                    }?.id
                }
        }
        if (resolvedActiveId != current.activeUserId) {
            repository.setActiveUserId(resolvedActiveId)
        }

        _uiState.update {
            it.copy(
                snapshot = merged,
                activeUserId = resolvedActiveId,
                syncedVersion = maxOf(it.syncedVersion, pull.version),
                lastSyncOk = true,
                isSyncing = false,
            )
        }
    }

    fun syncNow() {
        if (_uiState.value.isOfflineDataSaver) {
            showMessage("Turn off Data Saver to sync with the web app.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val hadPending = _uiState.value.snapshot.submissions.any { it.syncStatus == SyncStatus.PENDING_SYNC }
            if (hadPending) {
                syncPendingSubmissions(silentIfEmpty = true)
            } else {
                pullRemoteUpdates()
            }
            showMessage(
                if (_uiState.value.lastSyncOk) "Synced with YEP Web App (v${_uiState.value.syncedVersion})"
                else "Unable to reach sync server right now. Changes saved locally.",
            )
        }
    }

    fun resetSharedSyncState() {
        if (_uiState.value.isOfflineDataSaver) {
            showMessage("Go Online before resetting shared sync state.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val res = YepSyncClient.resetState(syncClientId)
            if (res?.state != null) {
                applyRemoteState(res)
                showMessage("Shared web & mobile state reset (v${res.version}).")
            } else {
                val seeded = SeedData.createInitialSnapshot()
                repository.saveSnapshot(seeded)
                val ver = YepSyncClient.pushState(syncClientId, YepSyncMapper.toJson(seeded))
                _uiState.update {
                    it.copy(
                        snapshot = seeded,
                        isSyncing = false,
                        syncedVersion = ver ?: it.syncedVersion,
                        lastSyncOk = ver != null,
                    )
                }
                showMessage("Reset local & synced state.")
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.setLanguage(language)
            _uiState.update { state ->
                val updatedHistory = if (state.aiChatHistory.size == 1 && state.aiChatHistory.first().id == "ai_welcome") {
                    listOf(
                        state.aiChatHistory.first().copy(
                            text = YepTranslations.forLanguage(language).aiAdvisorGreeting,
                        ),
                    )
                } else {
                    state.aiChatHistory
                }
                state.copy(language = language, aiChatHistory = updatedHistory)
            }
        }
    }

    fun toggleOfflineDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            repository.setOfflineDataSaver(enabled)
            _uiState.update { it.copy(isOfflineDataSaver = enabled) }
            if (!enabled) {
                syncPendingSubmissions(silentIfEmpty = true)
                pullRemoteUpdates()
            }
        }
    }

    fun syncPendingSubmissions(silentIfEmpty: Boolean = false) {
        val current = _uiState.value
        if (current.isOfflineDataSaver) {
            showMessage("Turn off Data Saver (Go Online) to sync queued submissions.")
            return
        }
        val pendingCount = current.snapshot.submissions.count { it.syncStatus == SyncStatus.PENDING_SYNC }
        if (pendingCount == 0) {
            if (!silentIfEmpty) showMessage("All capstone submissions are already synced.")
            return
        }
        mutateSnapshot(
            snackbar = "Synced $pendingCount queued capstone submission(s)!",
        ) { snap ->
            val updatedSubs = snap.submissions.map { sub ->
                if (sub.syncStatus == SyncStatus.PENDING_SYNC) {
                    sub.copy(syncStatus = SyncStatus.SYNCED)
                } else {
                    sub
                }
            }
            snap.copy(submissions = updatedSubs)
        }
    }

    fun signInWithUserId(userId: String): Boolean {
        val user = _uiState.value.snapshot.users.firstOrNull { it.id == userId } ?: return false
        if (user.isSuspended) {
            showMessage("Account suspended: ${user.suspensionReason ?: "Contact Programme Secretariat."}")
            return false
        }
        viewModelScope.launch {
            repository.setActiveUserId(user.id)
            _uiState.update { state ->
                state.copy(
                    activeUserId = user.id,
                    facilitatorDiagnosticOutput = null,
                    aiChatHistory = listOf(
                        AiChatMessage(
                            id = "ai_welcome",
                            isUser = false,
                            text = state.strings.aiAdvisorGreeting,
                            timestamp = System.currentTimeMillis(),
                        ),
                    ),
                )
            }
        }
        return true
    }

    fun signInWithEmail(email: String, password: String): String? {
        val cleanEmail = email.trim().lowercase(Locale.US)
        val user = _uiState.value.snapshot.users.firstOrNull {
            it.email.trim().lowercase(Locale.US) == cleanEmail
        } ?: return "No account found for $email."
        if (user.password != password && password.isBlank()) {
            return "Please enter your password."
        }
        if (user.password != password) {
            return "Incorrect password. Tip: Use 1-Click Test Accounts or register a new account."
        }
        if (user.isSuspended) {
            return "This account is suspended: ${user.suspensionReason ?: "Contact Admin."}"
        }
        signInWithUserId(user.id)
        return null
    }

    fun registerAccount(
        role: UserRole,
        name: String,
        email: String,
        password: String,
        regionalHub: String,
        age: Int,
        skillsRaw: String,
        careerGoal: String,
        bio: String,
    ): String? {
        if (name.isBlank() || email.isBlank()) return "Full name and email are required."
        if (password.length < 8 || !password.any { it.isDigit() }) {
            return "Password must be at least 8 characters and include a digit."
        }
        if (role == UserRole.PARTICIPANT && age !in 18..35) {
            return _uiState.value.strings.ageEligibilityBlocked
        }
        val cleanEmail = email.trim().lowercase(Locale.US)
        if (_uiState.value.snapshot.users.any { it.email.trim().lowercase(Locale.US) == cleanEmail }) {
            return "An account with this email already exists."
        }

        val newId = "u_${UUID.randomUUID().toString().take(8)}"
        val skills = skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf("Digital Literacy") }
        val newUser = UserProfile(
            id = newId,
            name = name.trim(),
            email = email.trim(),
            password = password,
            role = role,
            title = when (role) {
                UserRole.PARTICIPANT -> "YEP Cohort Learner"
                UserRole.MENTOR -> "Cohort Mentor & Facilitator"
                UserRole.PARTNER -> "Hiring Partner Lead"
                UserRole.ADMIN -> "Operations Admin"
            },
            organization = "YEP $regionalHub",
            regionalHub = regionalHub,
            age = age,
            skills = skills,
            interests = listOf("Youth Empowerment"),
            bio = bio.trim().ifBlank { "Excited to collaborate across the YEP Platform." },
            careerGoal = careerGoal.trim().ifBlank { "Build impactful, low-bandwidth digital solutions." },
            consentDataSharing = false,
            badges = listOf("Verified Member"),
        )

        mutateSnapshot(snackbar = "Welcome, ${newUser.name}!") { snap ->
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.USER_REGISTRATION,
                actorId = newUser.id,
                actorName = newUser.name,
                summary = "Registered as ${newUser.role.label} (Age ${newUser.age}, ${newUser.regionalHub}).",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(
                users = snap.users + newUser,
                auditLogs = listOf(log) + snap.auditLogs,
            )
        }
        signInWithUserId(newId)
        return null
    }

    fun signOut() {
        viewModelScope.launch {
            repository.setActiveUserId(null)
            _uiState.update { it.copy(activeUserId = null) }
        }
    }

    fun updateUserProfile(
        targetUserId: String,
        name: String,
        email: String,
        title: String,
        organization: String,
        regionalHub: String,
        age: Int,
        careerGoal: String,
        skillsRaw: String,
        interestsRaw: String,
        bio: String,
        photoUri: String?,
    ): String? {
        val existing = _uiState.value.snapshot.users.firstOrNull { it.id == targetUserId }
            ?: return "User not found."
        if (name.isBlank() || email.isBlank()) return "Name and email cannot be blank."
        if (existing.role == UserRole.PARTICIPANT && age !in 18..35) {
            return "Participant age must remain within the 18–35 eligibility window."
        }
        val actor = _uiState.value.activeUser ?: existing
        val parsedSkills = skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val parsedInterests = interestsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val encodedAvatar = YepSyncClient.encodeAvatarToDataUrl(getApplication(), photoUri)

        mutateSnapshot(snackbar = "Profile updated for ${name.trim()}") { snap ->
            val updatedUsers = snap.users.map { u ->
                if (u.id == targetUserId) {
                    u.copy(
                        name = name.trim(),
                        email = email.trim(),
                        title = title.trim(),
                        organization = organization.trim(),
                        regionalHub = regionalHub,
                        age = age,
                        careerGoal = careerGoal.trim(),
                        skills = parsedSkills.ifEmpty { u.skills },
                        interests = parsedInterests.ifEmpty { u.interests },
                        bio = bio.trim(),
                        photoUri = encodedAvatar,
                    )
                } else {
                    u
                }
            }
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.PROFILE_UPDATED,
                actorId = actor.id,
                actorName = actor.name,
                summary = "Updated profile metadata for ${name.trim()} (${regionalHub}).",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(users = updatedUsers, auditLogs = listOf(log) + snap.auditLogs)
        }
        return null
    }

    fun togglePartnerConsent(enabled: Boolean) {
        val user = _uiState.value.activeUser ?: return
        mutateSnapshot(
            snackbar = if (enabled) {
                "Portfolio discovery consent enabled."
            } else {
                "Portfolio & rubric scores set to private."
            },
        ) { snap ->
            val updatedUsers = snap.users.map {
                if (it.id == user.id) it.copy(consentDataSharing = enabled) else it
            }
            snap.copy(users = updatedUsers)
        }
    }

    fun enrollInExchange(exchangeId: String) {
        val user = _uiState.value.activeUser
        if (user == null) {
            showMessage("Please sign in to enroll in a virtual exchange.")
            return
        }
        if (user.role == UserRole.PARTICIPANT && user.age !in 18..35) {
            showMessage(_uiState.value.strings.ageEligibilityBlocked)
            return
        }
        val snap = _uiState.value.snapshot
        val exchange = snap.exchanges.firstOrNull { it.id == exchangeId } ?: return
        if (exchange.isFlagged) {
            showMessage("This exchange is temporarily under moderation review.")
            return
        }
        if (snap.enrollments.any { it.userId == user.id && it.exchangeId == exchangeId }) {
            showMessage("You are already enrolled in ${exchange.title}.")
            return
        }
        if (exchange.isFull) {
            showMessage("Cohort is at full capacity (${exchange.enrolledCount}/${exchange.maxCapacity}).")
            return
        }

        mutateSnapshot(snackbar = "Enrolled in ${exchange.title}!") { currentSnap ->
            val newEnrollment = Enrollment(
                id = "enr_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                exchangeId = exchangeId,
                progressPercent = 15,
                completedChecklistIds = emptyList(),
                enrolledAt = System.currentTimeMillis(),
            )
            val updatedExchanges = currentSnap.exchanges.map {
                if (it.id == exchangeId) it.copy(enrolledCount = it.enrolledCount + 1) else it
            }
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.EXCHANGE_ENROLLED,
                actorId = user.id,
                actorName = user.name,
                summary = "Enrolled in '${exchange.title}' (${exchange.enrolledCount + 1}/${exchange.maxCapacity}).",
                timestamp = System.currentTimeMillis(),
            )
            currentSnap.copy(
                enrollments = currentSnap.enrollments + newEnrollment,
                exchanges = updatedExchanges,
                auditLogs = listOf(log) + currentSnap.auditLogs,
            )
        }
    }

    fun updateEnrollmentProgress(enrollmentId: String, progressPercent: Int) {
        mutateSnapshot { snap ->
            val updated = snap.enrollments.map {
                if (it.id == enrollmentId) it.copy(progressPercent = progressPercent.coerceIn(0, 100)) else it
            }
            snap.copy(enrollments = updated)
        }
    }

    fun toggleChecklistItem(enrollmentId: String, checklistItemId: String) {
        mutateSnapshot { snap ->
            val updated = snap.enrollments.map { enr ->
                if (enr.id == enrollmentId) {
                    val nextSet = enr.completedChecklistIds.toMutableSet()
                    if (!nextSet.add(checklistItemId)) nextSet.remove(checklistItemId)
                    val total = snap.exchanges.firstOrNull { it.id == enr.exchangeId }?.studyChecklist?.size ?: 1
                    val derivedPct = ((nextSet.size.toFloat() / total.coerceAtLeast(1)) * 100).toInt()
                    enr.copy(
                        completedChecklistIds = nextSet.toList(),
                        progressPercent = maxOf(enr.progressPercent, derivedPct),
                    )
                } else {
                    enr
                }
            }
            snap.copy(enrollments = updated)
        }
    }

    fun submitCapstone(
        exchangeId: String,
        projectTitle: String,
        architectureDescription: String,
        githubUrl: String,
        demoUrl: String,
        keyTakeaways: String,
    ): String? {
        val user = _uiState.value.activeUser ?: return "Please sign in first."
        if (projectTitle.isBlank() || architectureDescription.isBlank()) {
            return "Project title and architecture description are required."
        }
        val exchange = _uiState.value.snapshot.exchanges.firstOrNull { it.id == exchangeId }
            ?: return "Exchange not found."
        val isOffline = _uiState.value.isOfflineDataSaver
        val syncStatus = if (isOffline) SyncStatus.PENDING_SYNC else SyncStatus.SYNCED
        val subId = "sub_${UUID.randomUUID().toString().take(8)}"

        mutateSnapshot(
            snackbar = if (isOffline) {
                "Saved locally as PENDING SYNC (Data Saver active)."
            } else {
                "Capstone submitted and synced to mentor queue!"
            },
        ) { snap ->
            val sub = CapstoneSubmission(
                id = subId,
                userId = user.id,
                exchangeId = exchangeId,
                projectTitle = projectTitle.trim(),
                architectureDescription = architectureDescription.trim(),
                githubUrl = githubUrl.trim().ifBlank { "https://github.com/yep-cohort/capstone" },
                demoUrl = demoUrl.trim().ifBlank { "https://demo.yep.dev" },
                keyTakeaways = keyTakeaways.trim(),
                syncStatus = syncStatus,
                submittedAt = System.currentTimeMillis(),
            )
            val notif = YepNotification(
                id = newNotifId(),
                category = NotificationCategory.SUBMISSION,
                title = "New Capstone Submitted",
                message = "${user.name} submitted '${sub.projectTitle}' for ${exchange.title}.",
                recipientId = exchange.mentorId,
                recipientRole = UserRole.MENTOR,
                targetScreen = "mentor",
                targetEntityId = subId,
                createdAt = System.currentTimeMillis(),
            )
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.PROJECT_SUBMITTED,
                actorId = user.id,
                actorName = user.name,
                summary = "Submitted capstone '${sub.projectTitle}' (${syncStatus.name}).",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(
                submissions = listOf(sub) + snap.submissions,
                notifications = listOf(notif) + snap.notifications,
                auditLogs = listOf(log) + snap.auditLogs,
            )
        }
        return null
    }

    fun evaluateCapstoneRubric(
        submissionId: String,
        technicalExecution: Int,
        problemSolving: Int,
        practicalImpact: Int,
        presentationClarity: Int,
        qualitativeFeedback: String,
    ) {
        val mentor = _uiState.value.activeUser ?: return
        val scores = RubricScores(
            technicalExecution = technicalExecution.coerceIn(1, 5),
            problemSolving = problemSolving.coerceIn(1, 5),
            practicalImpact = practicalImpact.coerceIn(1, 5),
            presentationClarity = presentationClarity.coerceIn(1, 5),
        )
        val meanFormatted = String.format(Locale.US, "%.2f", scores.mean)

        mutateSnapshot(snackbar = "Rubric evaluation saved ($meanFormatted / 5.00)") { snap ->
            val targetSub = snap.submissions.firstOrNull { it.id == submissionId } ?: return@mutateSnapshot snap
            val updatedSubs = snap.submissions.map { s ->
                if (s.id == submissionId) {
                    s.copy(
                        rubricScores = scores,
                        qualitativeFeedback = qualitativeFeedback.trim().ifBlank {
                            "Solid engineering execution and clear problem framing."
                        },
                        evaluatedByMentorId = mentor.id,
                        evaluatedAt = System.currentTimeMillis(),
                    )
                } else {
                    s
                }
            }
            val updatedUsers = snap.users.map { u ->
                if (u.id == targetSub.userId && "Verified Capstone" !in u.badges) {
                    u.copy(badges = u.badges + "Verified Capstone")
                } else {
                    u
                }
            }
            val notif = YepNotification(
                id = newNotifId(),
                category = NotificationCategory.MENTOR_FEEDBACK,
                title = "Mentor Feedback: $meanFormatted / 5.00",
                message = "${mentor.name} evaluated '${targetSub.projectTitle}'. Tap to view feedback.",
                recipientId = targetSub.userId,
                targetScreen = "dashboard",
                targetEntityId = submissionId,
                createdAt = System.currentTimeMillis(),
            )
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.RUBRIC_EVALUATED,
                actorId = mentor.id,
                actorName = mentor.name,
                summary = "Evaluated '${targetSub.projectTitle}' with mean rubric $meanFormatted / 5.00.",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(
                submissions = updatedSubs,
                users = updatedUsers,
                notifications = listOf(notif) + snap.notifications,
                auditLogs = listOf(log) + snap.auditLogs,
            )
        }
    }

    fun scheduleLiveSession(
        exchangeId: String,
        title: String,
        scheduledAt: String,
        durationLabel: String,
        agenda: String,
        meetingUrl: String,
    ): String? {
        val mentor = _uiState.value.activeUser ?: return "Sign in required."
        if (title.isBlank() || scheduledAt.isBlank()) return "Title and schedule time are required."
        val exchange = _uiState.value.snapshot.exchanges.firstOrNull { it.id == exchangeId }
            ?: return "Exchange not found."

        mutateSnapshot(snackbar = "Live session scheduled for ${exchange.title}") { snap ->
            val session = LiveSession(
                id = "ses_${UUID.randomUUID().toString().take(8)}",
                exchangeId = exchangeId,
                title = title.trim(),
                scheduledAt = scheduledAt.trim(),
                durationLabel = durationLabel.trim().ifBlank { "60 min" },
                agenda = agenda.trim().ifBlank { "Cohort Q&A and capstone architecture walkthrough." },
                meetingUrl = meetingUrl.trim().ifBlank { "https://meet.yep-initiative.org/cohort-live" },
            )
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.SESSION_SCHEDULED,
                actorId = mentor.id,
                actorName = mentor.name,
                summary = "Scheduled live session '${session.title}' (${session.scheduledAt}).",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(
                liveSessions = listOf(session) + snap.liveSessions,
                auditLogs = listOf(log) + snap.auditLogs,
            )
        }
        return null
    }

    fun publishOpportunity(
        organization: String,
        title: String,
        compensation: String,
        workType: String,
        location: String,
        description: String,
        requiredSkillsRaw: String,
    ): String? {
        val partner = _uiState.value.activeUser ?: return "Sign in required."
        if (title.isBlank() || organization.isBlank() || compensation.isBlank()) {
            return "Organization, title, and compensation are required."
        }
        val skills = requiredSkillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf("Problem Solving", "Git Workflow") }
        val oppId = "opp_${UUID.randomUUID().toString().take(8)}"

        mutateSnapshot(snackbar = "Published '$title' to Opportunity Board!") { snap ->
            val opp = Opportunity(
                id = oppId,
                partnerId = partner.id,
                organization = organization.trim(),
                title = title.trim(),
                compensation = compensation.trim(),
                workType = workType.trim().ifBlank { "Remote" },
                location = location.trim().ifBlank { "Remote" },
                description = description.trim(),
                requiredSkills = skills,
                createdAt = System.currentTimeMillis(),
            )
            val notif = YepNotification(
                id = newNotifId(),
                category = NotificationCategory.OPPORTUNITY,
                title = "New Paid Opening: ${opp.organization}",
                message = "${opp.title} (${opp.compensation} · ${opp.workType}) is now open for consented EOIs.",
                recipientRole = UserRole.PARTICIPANT,
                targetScreen = "opportunities",
                targetEntityId = oppId,
                createdAt = System.currentTimeMillis(),
            )
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.OPPORTUNITY_PUBLISHED,
                actorId = partner.id,
                actorName = partner.name,
                summary = "Published '${opp.title}' at ${opp.organization} (${opp.compensation}).",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(
                opportunities = listOf(opp) + snap.opportunities,
                notifications = listOf(notif) + snap.notifications,
                auditLogs = listOf(log) + snap.auditLogs,
            )
        }
        return null
    }

    fun submitExpressionOfInterest(
        opportunityId: String,
        coverNote: String,
        consentConfirmed: Boolean,
    ): String? {
        val user = _uiState.value.activeUser ?: return "Please sign in as a participant."
        if (!consentConfirmed) {
            return "Explicit portfolio consent is required before sharing your capstone & rubric scores."
        }
        val snap = _uiState.value.snapshot
        val opp = snap.opportunities.firstOrNull { it.id == opportunityId } ?: return "Opportunity not found."
        if (snap.eois.any { it.opportunityId == opportunityId && it.participantId == user.id }) {
            return "You already expressed interest in ${opp.title}."
        }
        val eoiId = "eoi_${UUID.randomUUID().toString().take(8)}"

        mutateSnapshot(snackbar = "Consented Expression of Interest sent to ${opp.organization}!") { current ->
            val eoi = ExpressionOfInterest(
                id = eoiId,
                opportunityId = opportunityId,
                participantId = user.id,
                status = EoiStatus.SUBMITTED,
                consentConfirmed = true,
                coverNote = coverNote.trim().ifBlank {
                    "Sharing my verified YEP capstone snapshot and rubric portfolio for ${opp.title}."
                },
                createdAt = System.currentTimeMillis(),
            )
            val updatedUsers = current.users.map { u ->
                if (u.id == user.id) u.copy(consentDataSharing = true) else u
            }
            val notif = YepNotification(
                id = newNotifId(),
                category = NotificationCategory.OPPORTUNITY,
                title = "Consented Candidate EOI",
                message = "${user.name} shared verified portfolio & rubric scores for ${opp.title}.",
                recipientId = opp.partnerId,
                recipientRole = UserRole.PARTNER,
                targetScreen = "partner-manage",
                targetEntityId = eoiId,
                createdAt = System.currentTimeMillis(),
            )
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.CONSENT_GRANTED_EOI,
                actorId = user.id,
                actorName = user.name,
                summary = "Granted explicit portfolio consent & sent EOI for '${opp.title}' (${opp.organization}).",
                timestamp = System.currentTimeMillis(),
            )
            current.copy(
                users = updatedUsers,
                eois = listOf(eoi) + current.eois,
                notifications = listOf(notif) + current.notifications,
                auditLogs = listOf(log) + current.auditLogs,
            )
        }
        return null
    }

    fun updateEoiStatus(eoiId: String, status: EoiStatus) {
        val partner = _uiState.value.activeUser ?: return
        mutateSnapshot(snackbar = "Candidate status updated to ${status.label}") { snap ->
            val targetEoi = snap.eois.firstOrNull { it.id == eoiId } ?: return@mutateSnapshot snap
            val opp = snap.opportunities.firstOrNull { it.id == targetEoi.opportunityId }
            val updatedEois = snap.eois.map {
                if (it.id == eoiId) it.copy(status = status) else it
            }
            val notif = YepNotification(
                id = newNotifId(),
                category = NotificationCategory.OPPORTUNITY,
                title = "Application Status: ${status.label}",
                message = "${opp?.organization ?: "Partner"} updated your application for ${opp?.title ?: "role"} to ${status.label}.",
                recipientId = targetEoi.participantId,
                targetScreen = "opportunities",
                targetEntityId = eoiId,
                createdAt = System.currentTimeMillis(),
            )
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.OPPORTUNITY_PUBLISHED,
                actorId = partner.id,
                actorName = partner.name,
                summary = "Updated EOI $eoiId status to ${status.label}.",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(
                eois = updatedEois,
                notifications = listOf(notif) + snap.notifications,
                auditLogs = listOf(log) + snap.auditLogs,
            )
        }
    }

    fun sendDirectMessage(eoiId: String, body: String) {
        val sender = _uiState.value.activeUser ?: return
        if (body.isBlank()) return
        mutateSnapshot { snap ->
            val msg = DirectMessage(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                eoiId = eoiId,
                senderId = sender.id,
                senderName = sender.name,
                body = body.trim(),
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(messages = snap.messages + msg)
        }
    }

    fun toggleUserSuspension(userId: String, reason: String) {
        val admin = _uiState.value.activeUser ?: return
        if (userId == admin.id) {
            showMessage("You cannot suspend your own active admin session.")
            return
        }
        mutateSnapshot { snap ->
            val target = snap.users.firstOrNull { it.id == userId } ?: return@mutateSnapshot snap
            val nextSuspended = !target.isSuspended
            val updatedUsers = snap.users.map { u ->
                if (u.id == userId) {
                    u.copy(
                        isSuspended = nextSuspended,
                        suspensionReason = if (nextSuspended) reason.trim().ifBlank { "Policy review" } else null,
                    )
                } else {
                    u
                }
            }
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = if (nextSuspended) AuditActionType.ACCOUNT_SUSPENDED else AuditActionType.ACCOUNT_UNLOCKED,
                actorId = admin.id,
                actorName = admin.name,
                summary = if (nextSuspended) {
                    "Suspended account ${target.name} (${reason.ifBlank { "Policy review" }})."
                } else {
                    "Unlocked account ${target.name}."
                },
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(users = updatedUsers, auditLogs = listOf(log) + snap.auditLogs)
        }
    }

    fun toggleExchangeFlag(exchangeId: String) {
        val admin = _uiState.value.activeUser ?: return
        mutateSnapshot { snap ->
            val ex = snap.exchanges.firstOrNull { it.id == exchangeId } ?: return@mutateSnapshot snap
            val nextFlag = !ex.isFlagged
            val updated = snap.exchanges.map {
                if (it.id == exchangeId) it.copy(isFlagged = nextFlag) else it
            }
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.CONTENT_MODERATED,
                actorId = admin.id,
                actorName = admin.name,
                summary = "${if (nextFlag) "Flagged" else "Unflagged"} exchange '${ex.title}'.",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(exchanges = updated, auditLogs = listOf(log) + snap.auditLogs)
        }
    }

    fun toggleOpportunityFlag(opportunityId: String) {
        val admin = _uiState.value.activeUser ?: return
        mutateSnapshot { snap ->
            val opp = snap.opportunities.firstOrNull { it.id == opportunityId } ?: return@mutateSnapshot snap
            val nextFlag = !opp.isFlagged
            val updated = snap.opportunities.map {
                if (it.id == opportunityId) it.copy(isFlagged = nextFlag) else it
            }
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.CONTENT_MODERATED,
                actorId = admin.id,
                actorName = admin.name,
                summary = "${if (nextFlag) "Flagged" else "Unflagged"} opportunity '${opp.title}' (${opp.organization}).",
                timestamp = System.currentTimeMillis(),
            )
            snap.copy(opportunities = updated, auditLogs = listOf(log) + snap.auditLogs)
        }
    }

    fun exportZeroPiiImpactSummary(timeframe: String, themeFilter: String) {
        val admin = _uiState.value.activeUser ?: return
        val snap = _uiState.value.snapshot
        val participants = snap.users.filter { it.role == UserRole.PARTICIPANT }
        val filteredExchanges = snap.exchanges.filter { themeFilter == "All" || it.theme == themeFilter }
        val exchangeIds = filteredExchanges.map { it.id }.toSet()
        val enrollmentsCount = snap.enrollments.count { it.exchangeId in exchangeIds }
        val subs = snap.submissions.filter { it.exchangeId in exchangeIds }
        val graded = subs.mapNotNull { it.rubricScores }
        val meanScore = if (graded.isEmpty()) 0.0 else graded.map { it.mean }.average()
        val consentedEois = snap.eois.count { it.consentConfirmed }

        val summary = buildString {
            appendLine("YEP Zero-PII M&E Impact Summary ($timeframe · Theme: $themeFilter)")
            appendLine("• Total Active Youth Learners (18–35): ${participants.size}")
            appendLine("• Filtered Cohort Enrollments: $enrollmentsCount across ${filteredExchanges.size} exchanges")
            appendLine("• Capstone Submissions: ${subs.size} (${graded.size} mentor-evaluated)")
            appendLine("• Cohort Mean Rubric: ${String.format(Locale.US, "%.2f", meanScore)} / 5.00")
            appendLine("• Consented Partner EOIs: $consentedEois (0 PII fields exported)")
        }

        mutateSnapshot(snackbar = "Zero-PII M&E Summary exported & logged.") { current ->
            val log = AuditLogEntry(
                id = newAuditId(),
                actionType = AuditActionType.IMPACT_REPORT_EXPORTED,
                actorId = admin.id,
                actorName = admin.name,
                summary = "Exported Zero-PII M&E report ($timeframe, $themeFilter): ${participants.size} learners, $enrollmentsCount enrollments, ${graded.size} graded capstones.",
                timestamp = System.currentTimeMillis(),
            )
            current.copy(auditLogs = listOf(log) + current.auditLogs)
        }
        _uiState.update { it.copy(exportedImpactSummary = summary) }
    }

    fun clearExportedSummary() {
        _uiState.update { it.copy(exportedImpactSummary = null) }
    }

    fun markNotificationRead(notificationId: String) {
        val uid = _uiState.value.activeUserId ?: return
        mutateSnapshot { snap ->
            val updated = snap.notifications.map { n ->
                if (n.id == notificationId && uid !in n.readByUserIds) {
                    n.copy(readByUserIds = n.readByUserIds + uid)
                } else {
                    n
                }
            }
            snap.copy(notifications = updated)
        }
    }

    fun markAllNotificationsRead() {
        val uid = _uiState.value.activeUserId ?: return
        val visibleIds = _uiState.value.visibleNotifications.map { it.id }.toSet()
        mutateSnapshot { snap ->
            val updated = snap.notifications.map { n ->
                if (n.id in visibleIds && uid !in n.readByUserIds) {
                    n.copy(readByUserIds = n.readByUserIds + uid)
                } else {
                    n
                }
            }
            snap.copy(notifications = updated)
        }
    }

    fun dismissNotification(notificationId: String) {
        val uid = _uiState.value.activeUserId ?: return
        mutateSnapshot { snap ->
            val updated = snap.notifications.map { n ->
                if (n.id == notificationId && uid !in n.dismissedByUserIds) {
                    n.copy(dismissedByUserIds = n.dismissedByUserIds + uid)
                } else {
                    n
                }
            }
            snap.copy(notifications = updated)
        }
    }

    fun askParticipantAi(query: String) {
        val participant = _uiState.value.activeUser ?: return
        if (query.isBlank()) return
        val userMsg = AiChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            isUser = true,
            text = query.trim(),
            timestamp = System.currentTimeMillis(),
        )
        _uiState.update {
            it.copy(
                aiChatHistory = it.aiChatHistory + userMsg,
                isAiAdvisorThinking = true,
            )
        }
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val stageSnap = computeStageSnapshot(participant.id, state.snapshot)
                val (reply, usedCloud) = aiEngine.askParticipantAdvisor(
                    query = query.trim(),
                    participant = participant,
                    language = state.language,
                    isOfflineDataSaver = state.isOfflineDataSaver,
                    snapshot = state.snapshot,
                    stageSnapshot = stageSnap,
                )
                val modeTag = if (usedCloud) "Gemini Live" else "Offline Grounded"
                val aiMsg = AiChatMessage(
                    id = "msg_${UUID.randomUUID().toString().take(8)}",
                    isUser = false,
                    text = "[$modeTag]\n$reply",
                    timestamp = System.currentTimeMillis(),
                )
                _uiState.update {
                    it.copy(
                        aiChatHistory = it.aiChatHistory + aiMsg,
                        isAiAdvisorThinking = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiAdvisorThinking = false) }
                showMessage("Advisor error: ${e.localizedMessage ?: "Try again"}")
            }
        }
    }

    fun runFacilitatorAi(mode: String, squadSize: Int = 3, customQuery: String = "") {
        _uiState.update { it.copy(isFacilitatorAiRunning = true) }
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val participants = state.snapshot.users.filter { it.role == UserRole.PARTICIPANT }
                val pairs = participants.map { u -> u to computeStageSnapshot(u.id, state.snapshot) }
                val (output, usedCloud) = aiEngine.runFacilitatorDiagnostic(
                    mode = mode,
                    squadSize = squadSize,
                    customQuery = customQuery,
                    language = state.language,
                    isOfflineDataSaver = state.isOfflineDataSaver,
                    snapshot = state.snapshot,
                    participantsWithStages = pairs,
                )
                val tag = if (usedCloud) "Gemini 3.8 Flash · Live Cohort" else "Offline Deterministic Diagnostic"
                _uiState.update {
                    it.copy(
                        facilitatorDiagnosticOutput = "[$tag]\n$output",
                        isFacilitatorAiRunning = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isFacilitatorAiRunning = false) }
                showMessage("Diagnostic failed: ${e.localizedMessage ?: "Try again"}")
            }
        }
    }

    fun computeStageSnapshot(userId: String, snap: YepStateSnapshot = _uiState.value.snapshot): ParticipantStageSnapshot {
        val user = snap.users.firstOrNull { it.id == userId }
        val enrollments = snap.enrollments.filter { it.userId == userId }
        val submissions = snap.submissions.filter { it.userId == userId }
        val graded = submissions.mapNotNull { it.rubricScores }
        val eois = snap.eois.filter { it.participantId == userId }

        val onboardPct = if (user != null && user.age in 18..35 && user.skills.isNotEmpty() && user.bio.isNotBlank()) 100 else 50
        val enrolledPct = if (enrollments.isNotEmpty()) 100 else 0
        val learningPct = if (enrollments.isEmpty()) 0 else enrollments.map { it.progressPercent }.average().toInt().coerceIn(0, 100)
        val capstonePct = when {
            submissions.any { it.syncStatus == SyncStatus.SYNCED } -> 100
            submissions.isNotEmpty() -> 75
            else -> 0
        }
        val rubricPct = when {
            graded.isNotEmpty() -> 100
            submissions.isNotEmpty() -> 35
            else -> 0
        }
        val partnerPct = when {
            user?.consentDataSharing == true && (graded.any { it.mean >= 3.5 } || eois.isNotEmpty()) -> 100
            user?.consentDataSharing == true -> 55
            else -> 20
        }

        val stages = listOf(
            StageMilestone(1, "onboard", onboardPct, onboardPct >= 100),
            StageMilestone(2, "enrolled", enrolledPct, enrolledPct >= 100),
            StageMilestone(3, "learning", learningPct, learningPct >= 75),
            StageMilestone(4, "capstone", capstonePct, capstonePct >= 100),
            StageMilestone(5, "rubric", rubricPct, rubricPct >= 100),
            StageMilestone(6, "partner", partnerPct, partnerPct >= 100),
        )

        val currentStage = stages.lastOrNull { it.completionPercent >= 75 }?.stageNumber ?: 1
        val overallPct = stages.map { it.completionPercent }.average().toInt()

        val meanRubric = if (graded.isEmpty()) {
            RubricScores(0, 0, 0, 0)
        } else {
            RubricScores(
                technicalExecution = graded.map { it.technicalExecution }.average().toInt().coerceAtLeast(1),
                problemSolving = graded.map { it.problemSolving }.average().toInt().coerceAtLeast(1),
                practicalImpact = graded.map { it.practicalImpact }.average().toInt().coerceAtLeast(1),
                presentationClarity = graded.map { it.presentationClarity }.average().toInt().coerceAtLeast(1),
            )
        }

        val tier = when {
            overallPct >= 82 && graded.isNotEmpty() && (user?.skills?.size ?: 0) >= 4 -> SkillTier.ADVANCED
            overallPct >= 62 || graded.isNotEmpty() -> SkillTier.INTERMEDIATE
            else -> SkillTier.BEGINNER
        }

        return ParticipantStageSnapshot(
            userId = userId,
            currentStage = currentStage,
            skillTier = tier,
            overallPercent = overallPct,
            stages = stages,
            rubricAverages = meanRubric,
        )
    }

    fun computeCohortBaselineStages(snap: YepStateSnapshot = _uiState.value.snapshot): List<Float> {
        val participants = snap.users.filter { it.role == UserRole.PARTICIPANT }
        if (participants.isEmpty()) return listOf(95f, 82f, 68f, 60f, 52f, 45f)
        val allStages = participants.map { computeStageSnapshot(it.id, snap).stages }
        return (0 until 6).map { stageIdx ->
            allStages.map { it[stageIdx].completionPercent.toFloat() }.average().toFloat()
        }
    }

    fun computeCohortRubricAverages(snap: YepStateSnapshot = _uiState.value.snapshot): List<Float> {
        val graded = snap.submissions.mapNotNull { it.rubricScores }
        if (graded.isEmpty()) return listOf(3.8f, 3.7f, 4.1f, 3.6f)
        return listOf(
            graded.map { it.technicalExecution.toFloat() }.average().toFloat(),
            graded.map { it.problemSolving.toFloat() }.average().toFloat(),
            graded.map { it.practicalImpact.toFloat() }.average().toFloat(),
            graded.map { it.presentationClarity.toFloat() }.average().toFloat(),
        )
    }

    private fun mutateSnapshot(
        snackbar: String? = null,
        transform: (YepStateSnapshot) -> YepStateSnapshot,
    ) {
        viewModelScope.launch {
            val next = transform(_uiState.value.snapshot)
            repository.saveSnapshot(next)
            _uiState.update {
                it.copy(
                    snapshot = next,
                    snackbarMessage = snackbar ?: it.snackbarMessage,
                )
            }
            if (!_uiState.value.isOfflineDataSaver) {
                _uiState.update { it.copy(isSyncing = true) }
                val pushedVer = YepSyncClient.pushState(syncClientId, YepSyncMapper.toJson(next))
                if (pushedVer != null) {
                    lastPushedVersion = maxOf(lastPushedVersion, pushedVer)
                }
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncedVersion = pushedVer?.let { v -> maxOf(it.syncedVersion, v) } ?: it.syncedVersion,
                        lastSyncOk = pushedVer != null,
                    )
                }
            }
        }
    }

    private fun newAuditId() = "aud_${UUID.randomUUID().toString().take(8)}"
    private fun newNotifId() = "not_${UUID.randomUUID().toString().take(8)}"
}
