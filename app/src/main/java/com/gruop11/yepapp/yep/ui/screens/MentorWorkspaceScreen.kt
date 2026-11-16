package com.gruop11.yepapp.yep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorWorkspaceScreen(
    mentor: UserProfile,
    snapshot: YepStateSnapshot,
    strings: YepStrings,
    computeStageFor: (String) -> ParticipantStageSnapshot,
    cohortStageBaseline: List<Float>,
    cohortRubricAverages: List<Float>,
    facilitatorDiagnosticOutput: String?,
    isFacilitatorAiRunning: Boolean,
    onEditProfile: () -> Unit,
    onEvaluateCapstone: (String, Int, Int, Int, Int, String) -> Unit,
    onScheduleSession: (String, String, String, String, String, String) -> String?,
    onRunFacilitatorAi: (mode: String, squadSize: Int, customQuery: String) -> Unit,
) {
    var selectedParticipantId by remember { mutableStateOf<String?>(null) } // null == All Cohort Average
    var tierFilter by remember { mutableStateOf<SkillTier?>(null) }
    var gradingTarget by remember { mutableStateOf<CapstoneSubmission?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    var aiTab by remember { mutableStateOf("GROUP") } // GROUP, COMPETENCY, QUERY
    var squadSize by remember { mutableIntStateOf(3) }
    var customQuery by remember { mutableStateOf("Which participants are interview-ready?") }

    val participants = remember(snapshot.users) {
        snapshot.users.filter { it.role == UserRole.PARTICIPANT }
    }
    val participantSnapshots = remember(participants, snapshot) {
        participants.map { u -> u to computeStageFor(u.id) }
    }
    val filteredParticipants = remember(participantSnapshots, tierFilter) {
        if (tierFilter == null) participantSnapshots
        else participantSnapshots.filter { it.second.skillTier == tierFilter }
    }

    val pendingQueue = remember(snapshot.submissions) {
        snapshot.submissions.filter { it.rubricScores == null }.sortedByDescending { it.submittedAt }
    }
    val gradedSubmissions = remember(snapshot.submissions) {
        snapshot.submissions.filter { it.rubricScores != null }
    }
    val cohortMeanScore = remember(gradedSubmissions) {
        if (gradedSubmissions.isEmpty()) "—"
        else String.format(Locale.US, "%.2f", gradedSubmissions.mapNotNull { it.rubricScores?.mean }.average())
    }

    val selectedPair = filteredParticipants.firstOrNull { it.first.id == selectedParticipantId }
    val chartStageData = remember(selectedPair, filteredParticipants, cohortStageBaseline) {
        if (selectedPair != null) {
            selectedPair.second.stages.map { it.completionPercent.toFloat() }
        } else if (filteredParticipants.isNotEmpty()) {
            (0 until 6).map { idx ->
                filteredParticipants.map { it.second.stages[idx].completionPercent.toFloat() }.average().toFloat()
            }
        } else {
            cohortStageBaseline
        }
    }
    val chartRubricData = remember(selectedPair, filteredParticipants, cohortRubricAverages) {
        if (selectedPair != null) {
            val r = selectedPair.second.rubricAverages
            listOf(
                r.technicalExecution.toFloat(),
                r.problemSolving.toFloat(),
                r.practicalImpact.toFloat(),
                r.presentationClarity.toFloat(),
            )
        } else {
            cohortRubricAverages
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Mentor Header
        YepCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UserAvatar(mentor, size = 50.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(mentor.name, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    Text("${mentor.title} · ${mentor.organization}", style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                }
                OutlinedButton(
                    onClick = onEditProfile,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) {
                    Text(strings.editProfile)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { showScheduleDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = YepIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("schedule_session_button"),
            ) {
                Icon(Icons.Default.VideoCall, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.scheduleLiveSession)
            }
        }

        // KPIs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiMetricCard(
                title = "Pending Queue",
                value = pendingQueue.size.toString(),
                icon = Icons.Default.PendingActions,
                accentColor = YepAmber,
                softBg = YepAmberSoft,
                modifier = Modifier.weight(1f),
            )
            KpiMetricCard(
                title = "Graded",
                value = gradedSubmissions.size.toString(),
                icon = Icons.Default.CheckCircle,
                accentColor = YepEmerald,
                softBg = YepEmeraldSoft,
                modifier = Modifier.weight(1f),
            )
            KpiMetricCard(
                title = "Cohort Mean",
                value = cohortMeanScore,
                icon = Icons.Default.Star,
                accentColor = YepIndigo,
                softBg = YepIndigoSoft,
                modifier = Modifier.weight(1f),
            )
        }

        // Participant Stage & Competency Tracker
        YepCard {
            Text(strings.mentorTrackerTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
            Spacer(modifier = Modifier.height(8.dp))

            // Participant Selector Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = selectedParticipantId == null,
                    onClick = { selectedParticipantId = null },
                    label = { Text("All Cohort Avg") },
                )
                filteredParticipants.forEach { (u, st) ->
                    FilterChip(
                        selected = selectedParticipantId == u.id,
                        onClick = { selectedParticipantId = u.id },
                        label = { Text("${u.name.substringBefore(" ")} (${st.skillTier.label.take(3)})") },
                    )
                }
            }

            // Skill Tier Filter
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = tierFilter == null,
                    onClick = { tierFilter = null },
                    label = { Text("Tier: All") },
                )
                SkillTier.entries.forEach { t ->
                    FilterChip(
                        selected = tierFilter == t,
                        onClick = {
                            tierFilter = t
                            selectedParticipantId = null
                        },
                        label = { Text(t.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            StageCompletionComboChart(
                participantStages = chartStageData,
                cohortBaseline = cohortStageBaseline,
                primaryLabel = selectedPair?.first?.name?.substringBefore(" ") ?: "Selected Cohort",
                baselineLabel = "Cohort Baseline",
                strings = strings,
            )

            Spacer(modifier = Modifier.height(14.dp))
            RubricComparisonGroupedBarChart(
                participantScores = chartRubricData,
                cohortScores = cohortRubricAverages,
                primaryLabel = selectedPair?.first?.name?.substringBefore(" ") ?: "Selected Avg",
                cohortLabel = "Cohort Avg",
            )
        }

        // Pending Capstone Review Queue
        YepCard {
            Text(strings.pendingReviewTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
            Spacer(modifier = Modifier.height(8.dp))

            if (pendingQueue.isEmpty()) {
                Text("All capstones have been graded!", style = MaterialTheme.typography.bodySmall, color = YepEmeraldDark)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingQueue.forEach { sub ->
                        val author = snapshot.users.firstOrNull { it.id == sub.userId }
                        val exchange = snapshot.exchanges.firstOrNull { it.id == sub.exchangeId }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = YepSlateBg,
                            border = BorderStroke(1.dp, YepSlate200),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(sub.projectTitle, style = MaterialTheme.typography.titleSmall, color = YepSlateDark, modifier = Modifier.weight(1f))
                                    StatusBadge(sub.syncStatus.name, YepBlue, YepBlueSoft)
                                }
                                Text(
                                    "By ${author?.name ?: "Learner"} · ${exchange?.title ?: "Exchange"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = YepIndigo,
                                )
                                Text(sub.architectureDescription, style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                                Text("Repo: ${sub.githubUrl}", style = MaterialTheme.typography.labelSmall, color = YepSlate500)
                                Button(
                                    onClick = { gradingTarget = sub },
                                    colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("grade_capstone_${sub.id}"),
                                ) {
                                    Text("Open 1–5 Rubric Grader")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Facilitator AI Diagnostic & Squad Grouping Assistant
        YepCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = YepIndigo)
                Text(strings.aiDiagnosticTitle, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = aiTab == "GROUP",
                    onClick = { aiTab = "GROUP" },
                    label = { Text(strings.groupFormationTab) },
                )
                FilterChip(
                    selected = aiTab == "COMPETENCY",
                    onClick = { aiTab = "COMPETENCY" },
                    label = { Text(strings.competencyEvalTab) },
                )
                FilterChip(
                    selected = aiTab == "QUERY",
                    onClick = { aiTab = "QUERY" },
                    label = { Text(strings.readinessQueryTab) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (aiTab) {
                "GROUP" -> {
                    Text("Target Squad Size:", style = MaterialTheme.typography.labelMedium, color = YepSlate600)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 4).forEach { sz ->
                            FilterChip(
                                selected = squadSize == sz,
                                onClick = { squadSize = sz },
                                label = { Text("$sz Learners") },
                            )
                        }
                    }
                    Button(
                        enabled = !isFacilitatorAiRunning,
                        onClick = { onRunFacilitatorAi("GROUP", squadSize, "") },
                        colors = ButtonDefaults.buttonColors(containerColor = YepIndigo),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).testTag("run_squad_builder"),
                    ) {
                        Text("Generate Mixed-Tier Squads")
                    }
                }
                "COMPETENCY" -> {
                    Button(
                        enabled = !isFacilitatorAiRunning,
                        onClick = { onRunFacilitatorAi("COMPETENCY", squadSize, "") },
                        colors = ButtonDefaults.buttonColors(containerColor = YepIndigo),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).testTag("run_competency_eval"),
                    ) {
                        Text("Run Cohort Competency Diagnostic")
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        AssistChip(
                            onClick = { customQuery = "Which participants are interview-ready?" },
                            label = { Text("Interview-ready?") },
                        )
                        AssistChip(
                            onClick = { customQuery = "Who needs mentorship intervention?" },
                            label = { Text("Needs intervention?") },
                        )
                    }
                    OutlinedTextField(
                        value = customQuery,
                        onValueChange = { customQuery = it },
                        label = { Text("Readiness Query") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        enabled = !isFacilitatorAiRunning && customQuery.isNotBlank(),
                        onClick = { onRunFacilitatorAi("QUERY", squadSize, customQuery) },
                        colors = ButtonDefaults.buttonColors(containerColor = YepIndigo),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text("Run Readiness Query")
                    }
                }
            }

            if (isFacilitatorAiRunning) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = YepIndigo)
            }

            facilitatorDiagnosticOutput?.let { out ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YepIndigoSoft,
                    border = BorderStroke(1.dp, YepSlate200),
                ) {
                    Text(
                        text = out,
                        style = MaterialTheme.typography.bodySmall,
                        color = YepSlateDark,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }

    gradingTarget?.let { sub ->
        RubricGraderDialog(
            submission = sub,
            onDismiss = { gradingTarget = null },
            onSave = { tech, solve, impact, clarity, feedback ->
                onEvaluateCapstone(sub.id, tech, solve, impact, clarity, feedback)
                gradingTarget = null
            },
        )
    }

    if (showScheduleDialog) {
        ScheduleSessionDialog(
            exchanges = snapshot.exchanges,
            onDismiss = { showScheduleDialog = false },
            onSave = onScheduleSession,
        )
    }
}

@Composable
private fun RubricGraderDialog(
    submission: CapstoneSubmission,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int, String) -> Unit,
) {
    var tech by remember { mutableFloatStateOf(4f) }
    var solve by remember { mutableFloatStateOf(4f) }
    var impact by remember { mutableFloatStateOf(4f) }
    var clarity by remember { mutableFloatStateOf(4f) }
    var feedback by remember { mutableStateOf("") }

    val mean = (tech.toInt() + solve.toInt() + impact.toInt() + clarity.toInt()) / 4.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(20.dp),
            color = YepWhite,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("1–5 Rubric Grader", style = MaterialTheme.typography.titleLarge, color = YepSlateDark)
                    StatusBadge(
                        text = "Mean: ${String.format(Locale.US, "%.2f", mean)} / 5.00",
                        textColor = YepEmeraldDark,
                        bgColor = YepEmeraldSoft,
                    )
                }
                Text(submission.projectTitle, style = MaterialTheme.typography.titleSmall, color = YepIndigo)

                RubricSlider("Technical Execution", tech) { tech = it }
                RubricSlider("Problem Solving", solve) { solve = it }
                RubricSlider("Practical Impact", impact) { impact = it }
                RubricSlider("Presentation Clarity", clarity) { clarity = it }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Qualitative Mentor Feedback") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("rubric_feedback_input"),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(tech.toInt(), solve.toInt(), impact.toInt(), clarity.toInt(), feedback) },
                        colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                        modifier = Modifier.testTag("rubric_submit_button"),
                    ) {
                        Text("Publish Evaluation")
                    }
                }
            }
        }
    }
}

@Composable
private fun RubricSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = YepSlateDark)
            Text("${value.toInt()} / 5", style = MaterialTheme.typography.labelLarge, color = YepEmeraldDark, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(thumbColor = YepEmerald, activeTrackColor = YepEmerald),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSessionDialog(
    exchanges: List<VirtualExchange>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> String?,
) {
    var selectedExchange by remember { mutableStateOf(exchanges.first()) }
    var expanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var scheduledAt by remember { mutableStateOf("Fri, Nov 27 · 16:00 EAT") }
    var duration by remember { mutableStateOf("60 min") }
    var agenda by remember { mutableStateOf("") }
    var meetingUrl by remember { mutableStateOf("https://meet.yep-initiative.org/live-clinic") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(20.dp),
            color = YepWhite,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Schedule Live Virtual Session", style = MaterialTheme.typography.titleLarge, color = YepSlateDark)

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedExchange.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Virtual Exchange") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        exchanges.forEach { ex ->
                            DropdownMenuItem(
                                text = { Text(ex.title) },
                                onClick = {
                                    selectedExchange = ex
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Session Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = scheduledAt, onValueChange = { scheduledAt = it }, label = { Text("Date & Time (EAT)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = agenda, onValueChange = { agenda = it }, label = { Text("Agenda") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = meetingUrl, onValueChange = { meetingUrl = it }, label = { Text("Meeting Link") }, modifier = Modifier.fillMaxWidth())

                error?.let { Text(it, color = YepRose, style = MaterialTheme.typography.bodySmall) }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val err = onSave(selectedExchange.id, title, scheduledAt, duration, agenda, meetingUrl)
                            if (err != null) error = err else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YepIndigo),
                    ) {
                        Text("Schedule Session")
                    }
                }
            }
        }
    }
}
