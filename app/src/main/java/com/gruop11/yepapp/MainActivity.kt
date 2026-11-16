package com.gruop11.yepapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gruop11.yepapp.ui.theme.YepEmerald
import com.gruop11.yepapp.ui.theme.YepPlatformTheme
import com.gruop11.yepapp.ui.theme.YepSlateBg
import com.gruop11.yepapp.yep.ai.configureAppCheck
import com.gruop11.yepapp.yep.model.SyncStatus
import com.gruop11.yepapp.yep.model.UserProfile
import com.gruop11.yepapp.yep.model.UserRole
import com.gruop11.yepapp.yep.ui.components.OfflineDataSaverBanner
import com.gruop11.yepapp.yep.ui.components.YepBottomNavigationBar
import com.gruop11.yepapp.yep.ui.components.YepTopAppBar
import com.gruop11.yepapp.yep.ui.dialogs.EditProfileDialog
import com.gruop11.yepapp.yep.ui.dialogs.NotificationsDialog
import com.gruop11.yepapp.yep.ui.screens.*
import com.gruop11.yepapp.yep.viewmodel.YepViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must configure App Check with launch intent before setContent or ViewModels.
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            YepPlatformTheme {
                YepPlatformRoot()
            }
        }
    }
}

private fun homeRouteFor(user: UserProfile?): String = when (user?.role) {
    null -> "overview"
    UserRole.PARTICIPANT -> "dashboard"
    UserRole.MENTOR -> "mentor"
    UserRole.PARTNER -> "partner-manage"
    UserRole.ADMIN -> "admin"
}

@Composable
fun YepPlatformRoot(
    viewModel: YepViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val activeUser = state.activeUser
    val homeRoute = remember(activeUser?.id, activeUser?.role) { homeRouteFor(activeUser) }
    var currentRoute by remember { mutableStateOf("overview") }

    // Synchronize route when authentication state changes
    LaunchedEffect(activeUser?.id) {
        currentRoute = homeRouteFor(activeUser)
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    var showNotifications by remember { mutableStateOf(false) }
    var editProfileTarget by remember { mutableStateOf<UserProfile?>(null) }

    // Android Back Navigation hierarchy: secondary screens return to role/public home
    BackHandler(enabled = currentRoute != homeRoute) {
        currentRoute = homeRoute
    }

    val pendingSyncCount = remember(state.snapshot.submissions) {
        state.snapshot.submissions.count { it.syncStatus == SyncStatus.PENDING_SYNC }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(YepSlateBg),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = YepEmerald)
        }
        return
    }

    Scaffold(
        containerColor = YepSlateBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                YepTopAppBar(
                    activeUser = activeUser,
                    language = state.language,
                    isOfflineDataSaver = state.isOfflineDataSaver,
                    unreadCount = state.unreadNotificationCount,
                    strings = state.strings,
                    onBrandClick = { currentRoute = homeRoute },
                    onSelectLanguage = viewModel::setLanguage,
                    onToggleOffline = viewModel::toggleOfflineDataSaver,
                    onNotificationsClick = { showNotifications = true },
                    onSignInClick = { currentRoute = "signin" },
                    onSignOutClick = {
                        viewModel.signOut()
                        currentRoute = "overview"
                    },
                    onAvatarClick = { activeUser?.let { editProfileTarget = it } },
                )
                OfflineDataSaverBanner(
                    visible = state.isOfflineDataSaver,
                    pendingCount = pendingSyncCount,
                    strings = state.strings,
                    onGoOnlineAndSync = { viewModel.toggleOfflineDataSaver(false) },
                )
            }
        },
        bottomBar = {
            YepBottomNavigationBar(
                activeUser = activeUser,
                currentRoute = currentRoute,
                strings = state.strings,
                onNavigate = { target -> currentRoute = target },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when (currentRoute) {
                "overview" -> PublicOverviewScreen(
                    activeCohortsCount = state.snapshot.exchanges.count { !it.isFlagged },
                    publishedOpeningsCount = state.snapshot.opportunities.count { !it.isFlagged },
                    strings = state.strings,
                    onExploreExchanges = { currentRoute = "exchanges" },
                    onSignInClick = { currentRoute = "signin" },
                )

                "signin" -> SignInScreen(
                    users = state.snapshot.users,
                    strings = state.strings,
                    onQuickSignIn = { uid -> viewModel.signInWithUserId(uid) },
                    onEmailSignIn = viewModel::signInWithEmail,
                    onRegister = viewModel::registerAccount,
                )

                "dashboard" -> {
                    val participant = activeUser ?: return@Box
                    val stageSnap = remember(state.snapshot, participant.id) {
                        viewModel.computeStageSnapshot(participant.id, state.snapshot)
                    }
                    val cohortBaseline = remember(state.snapshot) {
                        viewModel.computeCohortBaselineStages(state.snapshot)
                    }
                    val cohortRubrics = remember(state.snapshot) {
                        viewModel.computeCohortRubricAverages(state.snapshot)
                    }
                    ParticipantDashboardScreen(
                        user = participant,
                        snapshot = state.snapshot,
                        stageSnapshot = stageSnap,
                        cohortStageBaseline = cohortBaseline,
                        cohortRubricAverages = cohortRubrics,
                        isOfflineDataSaver = state.isOfflineDataSaver,
                        strings = state.strings,
                        aiChatHistory = state.aiChatHistory,
                        isAiThinking = state.isAiAdvisorThinking,
                        onEditProfile = { editProfileTarget = participant },
                        onToggleConsent = viewModel::togglePartnerConsent,
                        onUpdateProgress = viewModel::updateEnrollmentProgress,
                        onToggleChecklist = viewModel::toggleChecklistItem,
                        onSubmitCapstone = viewModel::submitCapstone,
                        onSyncQueued = { viewModel.syncPendingSubmissions() },
                        onAskAi = viewModel::askParticipantAi,
                        onExploreExchanges = { currentRoute = "exchanges" },
                    )
                }

                "exchanges" -> ExchangesCatalogScreen(
                    activeUser = activeUser,
                    snapshot = state.snapshot,
                    isOfflineDataSaver = state.isOfflineDataSaver,
                    strings = state.strings,
                    onEnroll = viewModel::enrollInExchange,
                    onToggleChecklist = viewModel::toggleChecklistItem,
                    onSubmitCapstone = viewModel::submitCapstone,
                )

                "opportunities" -> OpportunitiesBoardScreen(
                    activeUser = activeUser,
                    snapshot = state.snapshot,
                    strings = state.strings,
                    onSubmitEoi = viewModel::submitExpressionOfInterest,
                )

                "mentor" -> {
                    val mentor = activeUser ?: return@Box
                    val cohortBaseline = remember(state.snapshot) {
                        viewModel.computeCohortBaselineStages(state.snapshot)
                    }
                    val cohortRubrics = remember(state.snapshot) {
                        viewModel.computeCohortRubricAverages(state.snapshot)
                    }
                    MentorWorkspaceScreen(
                        mentor = mentor,
                        snapshot = state.snapshot,
                        strings = state.strings,
                        computeStageFor = { uid -> viewModel.computeStageSnapshot(uid, state.snapshot) },
                        cohortStageBaseline = cohortBaseline,
                        cohortRubricAverages = cohortRubrics,
                        facilitatorDiagnosticOutput = state.facilitatorDiagnosticOutput,
                        isFacilitatorAiRunning = state.isFacilitatorAiRunning,
                        onEditProfile = { editProfileTarget = mentor },
                        onEvaluateCapstone = viewModel::evaluateCapstoneRubric,
                        onScheduleSession = viewModel::scheduleLiveSession,
                        onRunFacilitatorAi = viewModel::runFacilitatorAi,
                    )
                }

                "partner-manage" -> {
                    val partner = activeUser ?: return@Box
                    PartnerPipelineScreen(
                        partner = partner,
                        snapshot = state.snapshot,
                        strings = state.strings,
                        onEditProfile = { editProfileTarget = partner },
                        onPublishOpportunity = viewModel::publishOpportunity,
                        onUpdateEoiStatus = viewModel::updateEoiStatus,
                        onSendMessage = viewModel::sendDirectMessage,
                    )
                }

                "admin" -> {
                    val admin = activeUser ?: return@Box
                    AdminControlScreen(
                        admin = admin,
                        snapshot = state.snapshot,
                        strings = state.strings,
                        syncedVersion = state.syncedVersion,
                        isSyncing = state.isSyncing,
                        lastSyncOk = state.lastSyncOk,
                        isOfflineDataSaver = state.isOfflineDataSaver,
                        exportedSummary = state.exportedImpactSummary,
                        onEditOwnProfile = { editProfileTarget = admin },
                        onEditAnyUser = { u -> editProfileTarget = u },
                        onToggleUserSuspension = viewModel::toggleUserSuspension,
                        onToggleExchangeFlag = viewModel::toggleExchangeFlag,
                        onToggleOpportunityFlag = viewModel::toggleOpportunityFlag,
                        onExportZeroPii = viewModel::exportZeroPiiImpactSummary,
                        onDismissExportSummary = viewModel::clearExportedSummary,
                        onSyncNow = viewModel::syncNow,
                        onResetSharedState = viewModel::resetSharedSyncState,
                    )
                }
            }
        }
    }

    if (showNotifications && activeUser != null) {
        NotificationsDialog(
            notifications = state.visibleNotifications,
            activeUserId = activeUser.id,
            strings = state.strings,
            onDismissDialog = { showNotifications = false },
            onMarkAllRead = viewModel::markAllNotificationsRead,
            onDismissNotification = viewModel::dismissNotification,
            onSelectNotification = { notif ->
                viewModel.markNotificationRead(notif.id)
                showNotifications = false
                currentRoute = notif.targetScreen
            },
        )
    }

    editProfileTarget?.let { target ->
        EditProfileDialog(
            user = target,
            strings = state.strings,
            onDismiss = { editProfileTarget = null },
            onSave = { name, email, title, org, hub, age, goal, skills, interests, bio, photoUri ->
                viewModel.updateUserProfile(
                    targetUserId = target.id,
                    name = name,
                    email = email,
                    title = title,
                    organization = org,
                    regionalHub = hub,
                    age = age,
                    careerGoal = goal,
                    skillsRaw = skills,
                    interestsRaw = interests,
                    bio = bio,
                    photoUri = photoUri,
                )
            },
        )
    }
}
