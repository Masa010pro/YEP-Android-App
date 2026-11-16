package com.gruop11.yepapp.yep.model

import com.squareup.moshi.JsonClass

enum class UserRole(val label: String) {
    PARTICIPANT("Participant"),
    MENTOR("Mentor / Facilitator"),
    PARTNER("Employer / Partner"),
    ADMIN("Administrator"),
}

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val regionSubtitle: String,
) {
    EN("EN", "English", "Global · East Africa"),
    SW("SW", "Kiswahili", "Kenya · Tanzania · Uganda · Rwanda"),
    LG("LG", "Oluganda", "Central Uganda"),
    NYN("NYN", "Runyankole", "Western Uganda"),
    RW("RW", "Ikinyarwanda", "Rwanda"),
    ACH("ACH", "Leb Acoli", "Northern Uganda · Western Kenya"),
}

enum class SkillTier(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
}

enum class EoiStatus(val key: String, val label: String) {
    SUBMITTED("submitted", "Submitted"),
    REVIEWING("reviewing", "Reviewing"),
    INTERVIEW_INVITED("interview_invited", "Interview Invited"),
    DECLINED("declined", "Declined"),
}

enum class NotificationCategory(val key: String) {
    SUBMISSION("submission"),
    MENTOR_FEEDBACK("mentor_feedback"),
    OPPORTUNITY("opportunity"),
}

enum class AuditActionType {
    USER_REGISTRATION,
    PROFILE_UPDATED,
    EXCHANGE_ENROLLED,
    SESSION_SCHEDULED,
    PROJECT_SUBMITTED,
    RUBRIC_EVALUATED,
    OPPORTUNITY_PUBLISHED,
    CONSENT_GRANTED_EOI,
    CONTENT_MODERATED,
    ACCOUNT_SUSPENDED,
    ACCOUNT_UNLOCKED,
    IMPACT_REPORT_EXPORTED,
}

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val title: String,
    val organization: String,
    val regionalHub: String,
    val age: Int,
    val skills: List<String>,
    val interests: List<String>,
    val bio: String,
    val careerGoal: String,
    val photoUri: String? = null,
    val consentDataSharing: Boolean = false,
    val isSuspended: Boolean = false,
    val suspensionReason: String? = null,
    val badges: List<String> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class LiveSession(
    val id: String,
    val exchangeId: String,
    val title: String,
    val scheduledAt: String,
    val durationLabel: String,
    val agenda: String,
    val meetingUrl: String,
)

@JsonClass(generateAdapter = true)
data class StudyChecklistItem(
    val id: String,
    val title: String,
)

@JsonClass(generateAdapter = true)
data class VirtualExchange(
    val id: String,
    val title: String,
    val theme: String,
    val skillLevel: SkillTier,
    val formatLabel: String,
    val durationLabel: String,
    val enrolledCount: Int,
    val maxCapacity: Int,
    val description: String,
    val outcomes: List<String>,
    val prerequisites: List<String>,
    val assignmentPrompt: String,
    val studyPackMarkdown: String,
    val studyChecklist: List<StudyChecklistItem>,
    val mentorId: String,
    val isFlagged: Boolean = false,
) {
    val isFull: Boolean get() = enrolledCount >= maxCapacity
}

@JsonClass(generateAdapter = true)
data class Enrollment(
    val id: String,
    val userId: String,
    val exchangeId: String,
    val progressPercent: Int,
    val completedChecklistIds: List<String> = emptyList(),
    val enrolledAt: Long,
)

@JsonClass(generateAdapter = true)
data class RubricScores(
    val technicalExecution: Int,
    val problemSolving: Int,
    val practicalImpact: Int,
    val presentationClarity: Int,
) {
    val mean: Double
        get() = (technicalExecution + problemSolving + practicalImpact + presentationClarity) / 4.0
}

@JsonClass(generateAdapter = true)
data class CapstoneSubmission(
    val id: String,
    val userId: String,
    val exchangeId: String,
    val projectTitle: String,
    val architectureDescription: String,
    val githubUrl: String,
    val demoUrl: String,
    val keyTakeaways: String,
    val syncStatus: SyncStatus,
    val submittedAt: Long,
    val rubricScores: RubricScores? = null,
    val qualitativeFeedback: String? = null,
    val evaluatedByMentorId: String? = null,
    val evaluatedAt: Long? = null,
)

@JsonClass(generateAdapter = true)
data class Opportunity(
    val id: String,
    val partnerId: String,
    val organization: String,
    val title: String,
    val compensation: String,
    val workType: String,
    val location: String,
    val description: String,
    val requiredSkills: List<String>,
    val isFlagged: Boolean = false,
    val createdAt: Long,
)

@JsonClass(generateAdapter = true)
data class ExpressionOfInterest(
    val id: String,
    val opportunityId: String,
    val participantId: String,
    val status: EoiStatus,
    val consentConfirmed: Boolean,
    val coverNote: String,
    val createdAt: Long,
)

@JsonClass(generateAdapter = true)
data class DirectMessage(
    val id: String,
    val eoiId: String,
    val senderId: String,
    val senderName: String,
    val body: String,
    val timestamp: Long,
)

@JsonClass(generateAdapter = true)
data class YepNotification(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val message: String,
    val recipientId: String? = null,
    val recipientRole: UserRole? = null,
    val targetScreen: String,
    val targetEntityId: String? = null,
    val createdAt: Long,
    val readByUserIds: List<String> = emptyList(),
    val dismissedByUserIds: List<String> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class AuditLogEntry(
    val id: String,
    val actionType: AuditActionType,
    val actorId: String,
    val actorName: String,
    val summary: String,
    val timestamp: Long,
)

@JsonClass(generateAdapter = true)
data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long,
)

@JsonClass(generateAdapter = true)
data class YepStateSnapshot(
    val users: List<UserProfile>,
    val exchanges: List<VirtualExchange>,
    val liveSessions: List<LiveSession>,
    val enrollments: List<Enrollment>,
    val submissions: List<CapstoneSubmission>,
    val opportunities: List<Opportunity>,
    val eois: List<ExpressionOfInterest>,
    val messages: List<DirectMessage>,
    val notifications: List<YepNotification>,
    val auditLogs: List<AuditLogEntry>,
)

data class StageMilestone(
    val stageNumber: Int,
    val titleKey: String,
    val completionPercent: Int,
    val isComplete: Boolean,
)

data class ParticipantStageSnapshot(
    val userId: String,
    val currentStage: Int,
    val skillTier: SkillTier,
    val overallPercent: Int,
    val stages: List<StageMilestone>,
    val rubricAverages: RubricScores,
)
