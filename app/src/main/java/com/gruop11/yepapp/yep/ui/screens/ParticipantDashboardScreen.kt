package com.gruop11.yepapp.yep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gruop11.yepapp.ui.theme.*
import com.gruop11.yepapp.yep.i18n.YepStrings
import com.gruop11.yepapp.yep.model.*
import com.gruop11.yepapp.yep.ui.charts.RubricComparisonGroupedBarChart
import com.gruop11.yepapp.yep.ui.charts.StageCompletionComboChart
import com.gruop11.yepapp.yep.ui.components.*
import com.gruop11.yepapp.yep.ui.dialogs.CapstoneSubmitDialog
import com.gruop11.yepapp.yep.ui.dialogs.StudyPackDialog
import java.util.Locale

@Composable
fun ParticipantDashboardScreen(
    user: UserProfile,
    snapshot: YepStateSnapshot,
    stageSnapshot: ParticipantStageSnapshot,
    cohortStageBaseline: List<Float>,
    cohortRubricAverages: List<Float>,
    isOfflineDataSaver: Boolean,
    strings: YepStrings,
    aiChatHistory: List<AiChatMessage>,
    isAiThinking: Boolean,
    onEditProfile: () -> Unit,
    onToggleConsent: (Boolean) -> Unit,
    onUpdateProgress: (String, Int) -> Unit,
    onToggleChecklist: (String, String) -> Unit,
    onSubmitCapstone: (String, String, String, String, String, String) -> String?,
    onSyncQueued: () -> Unit,
    onAskAi: (String) -> Unit,
    onExploreExchanges: () -> Unit,
) {
    var capstoneTargetExchange by remember { mutableStateOf<VirtualExchange?>(null) }
    var studyPackTarget by remember { mutableStateOf<Pair<VirtualExchange, Enrollment>?>(null) }
    var showAiAdvisorDialog by remember { mutableStateOf(false) }

    val myEnrollments = remember(snapshot.enrollments, user.id) {
        snapshot.enrollments.filter { it.userId == user.id }
    }
    val mySubmissions = remember(snapshot.submissions, user.id) {
        snapshot.submissions.filter { it.userId == user.id }.sortedByDescending { it.submittedAt }
    }
    val gradedSubmissions = remember(mySubmissions) {
        mySubmissions.mapNotNull { it.rubricScores }
    }
    val pendingSyncCount = remember(mySubmissions) {
        mySubmissions.count { it.syncStatus == SyncStatus.PENDING_SYNC }
    }
    val meanRubricText = remember(gradedSubmissions) {
        if (gradedSubmissions.isEmpty()) "—"
        else String.format(Locale.US, "%.2f", gradedSubmissions.map { it.mean }.average())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Profile Banner + Partner Data Consent Switch
            YepCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UserAvatar(user = user, size = 54.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                        Text(
                            "${user.title} · ${user.regionalHub}",
                            style = MaterialTheme.typography.bodySmall,
                            color = YepSlate600,
                        )
                        Text(
                            "Age ${user.age} · ${user.skills.take(3).joinToString(" · ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = YepEmeraldDark,
                        )
                    }
                    OutlinedButton(
                        onClick = onEditProfile,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("dashboard_edit_profile_button"),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = strings.editProfile, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.editProfile, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (user.consentDataSharing) YepEmeraldSoft else YepSlate100,
                    border = BorderStroke(1.dp, if (user.consentDataSharing) YepEmeraldBorder else YepSlate200),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.partnerConsentTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = YepSlateDark,
                            )
                            Text(
                                text = strings.partnerConsentDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = YepSlate600,
                            )
                        }
                        Switch(
                            checked = user.consentDataSharing,
                            onCheckedChange = onToggleConsent,
                            colors = SwitchDefaults.colors(checkedTrackColor = YepEmerald),
                            modifier = Modifier.testTag("partner_consent_switch"),
                        )
                    }
                }
            }

            // 2x2 KPI Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KpiMetricCard(
                        title = strings.kpiActiveCohorts,
                        value = myEnrollments.size.toString(),
                        icon = Icons.Default.School,
                        accentColor = YepEmerald,
                        softBg = YepEmeraldSoft,
                        modifier = Modifier.weight(1f),
                    )
                    KpiMetricCard(
                        title = strings.kpiGradedCapstones,
                        value = gradedSubmissions.size.toString(),
                        icon = Icons.Default.TaskAlt,
                        accentColor = YepBlue,
                        softBg = YepBlueSoft,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KpiMetricCard(
                        title = strings.kpiMeanRubric,
                        value = "$meanRubricText / 5",
                        icon = Icons.Default.Star,
                        accentColor = YepIndigo,
                        softBg = YepIndigoSoft,
                        modifier = Modifier.weight(1f),
                    )
                    KpiMetricCard(
                        title = strings.kpiEarnedMilestones,
                        value = user.badges.size.toString(),
                        icon = Icons.Default.EmojiEvents,
                        accentColor = YepAmber,
                        softBg = YepAmberSoft,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // My Current Stage & Learning Track + 2 Canvas Charts
            YepCard {
                Text(strings.myStageTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                Spacer(modifier = Modifier.height(10.dp))
                StageMilestoneTrack(snapshot = stageSnapshot, strings = strings)

                Spacer(modifier = Modifier.height(16.dp))
                Text(strings.stageChartTitle, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                Spacer(modifier = Modifier.height(8.dp))
                StageCompletionComboChart(
                    participantStages = stageSnapshot.stages.map { it.completionPercent.toFloat() },
                    cohortBaseline = cohortStageBaseline,
                    primaryLabel = user.name.substringBefore(" "),
                    baselineLabel = "Cohort Baseline",
                    strings = strings,
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = YepSlate200)
                Spacer(modifier = Modifier.height(14.dp))

                Text(strings.rubricChartTitle, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                Spacer(modifier = Modifier.height(8.dp))
                val pScores = listOf(
                    stageSnapshot.rubricAverages.technicalExecution.toFloat(),
                    stageSnapshot.rubricAverages.problemSolving.toFloat(),
                    stageSnapshot.rubricAverages.practicalImpact.toFloat(),
                    stageSnapshot.rubricAverages.presentationClarity.toFloat(),
                )
                RubricComparisonGroupedBarChart(
                    participantScores = pScores,
                    cohortScores = cohortRubricAverages,
                    primaryLabel = user.name.substringBefore(" "),
                    cohortLabel = "Cohort Avg",
                )
            }

            // Enrolled Virtual Exchanges
            YepCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.enrolledExchangesTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    TextButton(onClick = onExploreExchanges) {
                        Text(strings.exploreExchanges, color = YepEmeraldDark)
                    }
                }

                if (myEnrollments.isEmpty()) {
                    Text(
                        text = "You haven't enrolled in any virtual exchanges yet. Explore cohorts to start!",
                        style = MaterialTheme.typography.bodySmall,
                        color = YepSlate500,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        myEnrollments.forEach { enrollment ->
                            val exchange = snapshot.exchanges.firstOrNull { it.id == enrollment.exchangeId }
                                ?: return@forEach
                            val sessions = snapshot.liveSessions.filter { it.exchangeId == exchange.id }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = YepSlateBg,
                                border = BorderStroke(1.dp, YepSlate200),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = exchange.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = YepSlateDark,
                                            modifier = Modifier.weight(1f),
                                        )
                                        StatusBadge(
                                            text = "${enrollment.progressPercent}%",
                                            textColor = YepEmeraldDark,
                                            bgColor = YepEmeraldSoft,
                                        )
                                    }

                                    Slider(
                                        value = enrollment.progressPercent.toFloat(),
                                        onValueChange = { onUpdateProgress(enrollment.id, it.toInt()) },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = YepEmerald,
                                            activeTrackColor = YepEmerald,
                                        ),
                                    )

                                    if (sessions.isNotEmpty()) {
                                        sessions.take(2).forEach { ses ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = YepBlueSoft,
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(
                                                        text = "Live: ${ses.title} (${ses.scheduledAt})",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = YepBlue,
                                                    )
                                                    Text(
                                                        text = "${ses.agenda} · ${ses.meetingUrl}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = YepSlate600,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Button(
                                            onClick = { capstoneTargetExchange = exchange },
                                            colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("submit_capstone_${exchange.id}"),
                                        ) {
                                            Text(strings.submitCapstone, maxLines = 1)
                                        }
                                        OutlinedButton(
                                            onClick = { studyPackTarget = exchange to enrollment },
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("study_pack_${exchange.id}"),
                                        ) {
                                            Text(strings.offlineStudyPack, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Capstone Submissions Status & Offline Queue Sync
            YepCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.capstoneSubmissionsTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    if (pendingSyncCount > 0) {
                        Button(
                            onClick = onSyncQueued,
                            colors = ButtonDefaults.buttonColors(containerColor = YepAmber),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("sync_queued_button"),
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${strings.syncQueued} ($pendingSyncCount)")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (mySubmissions.isEmpty()) {
                    Text("No capstones submitted yet.", style = MaterialTheme.typography.bodySmall, color = YepSlate500)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        mySubmissions.forEach { sub ->
                            val isPending = sub.syncStatus == SyncStatus.PENDING_SYNC
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = YepSlateBg,
                                border = BorderStroke(1.dp, if (isPending) YepAmberBorder else YepSlate200),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = sub.projectTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = YepSlateDark,
                                            modifier = Modifier.weight(1f),
                                        )
                                        StatusBadge(
                                            text = if (isPending) "PENDING SYNC" else "SYNCED",
                                            textColor = if (isPending) YepAmber else YepEmeraldDark,
                                            bgColor = if (isPending) YepAmberSoft else YepEmeraldSoft,
                                            borderColor = if (isPending) YepAmberBorder else YepEmeraldBorder,
                                        )
                                    }
                                    Text(
                                        text = sub.architectureDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = YepSlate600,
                                    )
                                    sub.rubricScores?.let { scores ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = YepEmeraldSoft,
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "Mentor Mean Rubric: ${String.format(Locale.US, "%.2f", scores.mean)} / 5.00",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = YepEmeraldDark,
                                                )
                                                Text(
                                                    text = "Exec ${scores.technicalExecution} · Solve ${scores.problemSolving} · Impact ${scores.practicalImpact} · Clarity ${scores.presentationClarity}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = YepSlate600,
                                                )
                                                if (!sub.qualitativeFeedback.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Feedback: ${sub.qualitativeFeedback}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = YepSlateDark,
                                                    )
                                                }
                                            }
                                        }
                                    } ?: Text(
                                        text = "Awaiting mentor 1–5 rubric review",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = YepIndigo,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Earned Achievement Badges
            YepCard {
                Text(strings.earnedBadgesTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    user.badges.forEach { badge ->
                        StatusBadge(
                            text = "★ $badge",
                            textColor = YepEmeraldDark,
                            bgColor = YepEmeraldSoft,
                            borderColor = YepEmeraldBorder,
                        )
                    }
                }
            }
        }

        // Floating Participant AI Advisor FAB
        ExtendedFloatingActionButton(
            onClick = { showAiAdvisorDialog = true },
            containerColor = YepSlateDark,
            contentColor = YepWhite,
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = strings.aiAdvisorTitle) },
            text = { Text("AI Advisor") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("participant_ai_advisor_fab"),
        )
    }

    capstoneTargetExchange?.let { ex ->
        CapstoneSubmitDialog(
            exchange = ex,
            isOfflineDataSaver = isOfflineDataSaver,
            onDismiss = { capstoneTargetExchange = null },
            onSubmit = { title, arch, gh, demo, takeaways ->
                onSubmitCapstone(ex.id, title, arch, gh, demo, takeaways)
            },
        )
    }

    studyPackTarget?.let { (ex, enr) ->
        StudyPackDialog(
            exchange = ex,
            enrollment = enr,
            onDismiss = { studyPackTarget = null },
            onToggleChecklist = onToggleChecklist,
        )
    }

    if (showAiAdvisorDialog) {
        ParticipantAiAdvisorDialog(
            strings = strings,
            messages = aiChatHistory,
            isThinking = isAiThinking,
            onDismiss = { showAiAdvisorDialog = false },
            onSendQuery = onAskAi,
        )
    }
}

@Composable
private fun ParticipantAiAdvisorDialog(
    strings: YepStrings,
    messages: List<AiChatMessage>,
    isThinking: Boolean,
    onDismiss: () -> Unit,
    onSendQuery: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val quickPrompts = listOf(
        strings.aiPromptMatchJobs,
        strings.aiPromptNextExchange,
        strings.aiPromptRubricRules,
        strings.aiPromptConsentRules,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(min = 420.dp, max = 640.dp),
            shape = RoundedCornerShape(20.dp),
            color = YepWhite,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = YepEmerald)
                        Text(strings.aiAdvisorTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                // Quick Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    quickPrompts.forEach { prompt ->
                        AssistChip(
                            onClick = { onSendQuery(prompt) },
                            label = { Text(prompt, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(messages, key = { it.id }) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (msg.isUser) YepEmerald else YepSlate100,
                                modifier = Modifier.widthIn(max = 290.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) YepWhite else YepSlateDark,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }
                    }
                    if (isThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = YepEmerald)
                                Text("Thinking...", style = MaterialTheme.typography.labelSmall, color = YepSlate500)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Ask about jobs, courses, rubrics...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("ai_advisor_input"),
                    )
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                onSendQuery(input)
                                input = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_advisor_send"),
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = YepEmerald)
                    }
                }
            }
        }
    }
}
