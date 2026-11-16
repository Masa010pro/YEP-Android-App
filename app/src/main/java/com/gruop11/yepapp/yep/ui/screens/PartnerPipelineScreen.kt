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
import com.gruop11.yepapp.yep.ui.components.*
import java.util.Locale

@Composable
fun PartnerPipelineScreen(
    partner: UserProfile,
    snapshot: YepStateSnapshot,
    strings: YepStrings,
    onEditProfile: () -> Unit,
    onPublishOpportunity: (String, String, String, String, String, String, String) -> String?,
    onUpdateEoiStatus: (String, EoiStatus) -> Unit,
    onSendMessage: (String, String) -> Unit,
) {
    var showPublishDialog by remember { mutableStateOf(false) }
    var messageTargetEoi by remember { mutableStateOf<ExpressionOfInterest?>(null) }

    val consentedEois = remember(snapshot.eois) {
        snapshot.eois.filter { it.consentConfirmed }.sortedByDescending { it.createdAt }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            YepCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UserAvatar(partner, size = 50.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(partner.name, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                        Text("${partner.title} · ${partner.organization}", style = MaterialTheme.typography.bodySmall, color = YepSlate600)
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
                    onClick = { showPublishDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = YepBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("publish_opportunity_button"),
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.publishOpportunity)
                }
            }
        }

        item {
            Text(
                text = "${strings.partnerPipelineTitle} (${consentedEois.size} Consented EOIs)",
                style = MaterialTheme.typography.titleMedium,
                color = YepSlateDark,
            )
        }

        items(consentedEois, key = { it.id }) { eoi ->
            val candidate = snapshot.users.firstOrNull { it.id == eoi.participantId }
            val opportunity = snapshot.opportunities.firstOrNull { it.id == eoi.opportunityId }
            val candidateSubs = snapshot.submissions.filter { it.userId == eoi.participantId }
            val bestSub = candidateSubs.maxByOrNull { it.rubricScores?.mean ?: 0.0 }
            val msgCount = snapshot.messages.count { it.eoiId == eoi.id }

            YepCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        if (candidate != null) UserAvatar(candidate, size = 40.dp)
                        Column {
                            Text(candidate?.name ?: "Candidate", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                            Text(
                                text = "${opportunity?.title ?: "Role"} · ${candidate?.regionalHub.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = YepSlate500,
                            )
                        }
                    }
                    StatusBadge(
                        text = eoi.status.label,
                        textColor = when (eoi.status) {
                            EoiStatus.INTERVIEW_INVITED -> YepEmeraldDark
                            EoiStatus.REVIEWING -> YepBlue
                            EoiStatus.DECLINED -> YepRose
                            EoiStatus.SUBMITTED -> YepIndigo
                        },
                        bgColor = when (eoi.status) {
                            EoiStatus.INTERVIEW_INVITED -> YepEmeraldSoft
                            EoiStatus.REVIEWING -> YepBlueSoft
                            EoiStatus.DECLINED -> YepRoseSoft
                            EoiStatus.SUBMITTED -> YepIndigoSoft
                        },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(eoi.coverNote, style = MaterialTheme.typography.bodySmall, color = YepSlate600)

                bestSub?.let { sub ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = YepEmeraldSoft,
                        border = BorderStroke(1.dp, YepEmeraldBorder),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            val scoreStr = sub.rubricScores?.let { String.format(Locale.US, "%.2f / 5.00", it.mean) } ?: "Pending"
                            Text(
                                text = "Verified Capstone: ${sub.projectTitle} ($scoreStr)",
                                style = MaterialTheme.typography.labelLarge,
                                color = YepEmeraldDark,
                            )
                            Text("GitHub: ${sub.githubUrl}", style = MaterialTheme.typography.labelSmall, color = YepSlate600)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Update Pipeline Stage:", style = MaterialTheme.typography.labelSmall, color = YepSlate500)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    EoiStatus.entries.forEach { st ->
                        FilterChip(
                            selected = eoi.status == st,
                            onClick = { onUpdateEoiStatus(eoi.id, st) },
                            label = { Text(st.label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { messageTargetEoi = eoi },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("message_candidate_${eoi.id}"),
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Candidate Thread ($msgCount)")
                }
            }
        }
    }

    if (showPublishDialog) {
        PublishOpportunityDialog(
            defaultOrg = partner.organization,
            onDismiss = { showPublishDialog = false },
            onPublish = onPublishOpportunity,
        )
    }

    messageTargetEoi?.let { eoi ->
        val candidate = snapshot.users.firstOrNull { it.id == eoi.participantId }
        val thread = snapshot.messages.filter { it.eoiId == eoi.id }
        CandidateMessageDialog(
            candidateName = candidate?.name ?: "Candidate",
            messages = thread,
            activeUserId = partner.id,
            onDismiss = { messageTargetEoi = null },
            onSend = { body -> onSendMessage(eoi.id, body) },
        )
    }
}

@Composable
private fun PublishOpportunityDialog(
    defaultOrg: String,
    onDismiss: () -> Unit,
    onPublish: (String, String, String, String, String, String, String) -> String?,
) {
    var org by remember { mutableStateOf(defaultOrg) }
    var title by remember { mutableStateOf("") }
    var compensation by remember { mutableStateOf("$1,100/mo") }
    var workType by remember { mutableStateOf("Remote") }
    var location by remember { mutableStateOf("Remote (East Africa)") }
    var description by remember { mutableStateOf("") }
    var skillsRaw by remember { mutableStateOf("TypeScript, PostgreSQL, Git Workflow") }
    var error by remember { mutableStateOf<String?>(null) }

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
                Text("Publish New Paid Opportunity", style = MaterialTheme.typography.titleLarge, color = YepSlateDark)
                OutlinedTextField(value = org, onValueChange = { org = it }, label = { Text("Organization") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Role Title") }, modifier = Modifier.fillMaxWidth().testTag("opp_title_input"))
                OutlinedTextField(value = compensation, onValueChange = { compensation = it }, label = { Text("Compensation") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Remote", "Hybrid", "On-site").forEach { wt ->
                        FilterChip(selected = workType == wt, onClick = { workType = wt }, label = { Text(wt) })
                    }
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = skillsRaw, onValueChange = { skillsRaw = it }, label = { Text("Required Skills (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Role Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

                error?.let { Text(it, color = YepRose, style = MaterialTheme.typography.bodySmall) }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val err = onPublish(org, title, compensation, workType, location, description, skillsRaw)
                            if (err != null) error = err else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YepBlue),
                        modifier = Modifier.testTag("opp_publish_confirm"),
                    ) {
                        Text("Publish Role")
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateMessageDialog(
    candidateName: String,
    messages: List<DirectMessage>,
    activeUserId: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(min = 380.dp, max = 560.dp),
            shape = RoundedCornerShape(20.dp),
            color = YepWhite,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Direct Thread · $candidateName", style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Text("No messages yet. Start the interview conversation!", style = MaterialTheme.typography.bodySmall, color = YepSlate500)
                        }
                    } else {
                        items(messages, key = { it.id }) { msg ->
                            val isMine = msg.senderId == activeUserId
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isMine) YepBlue else YepSlate100,
                                    modifier = Modifier.widthIn(max = 280.dp),
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = msg.senderName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isMine) YepWhite.copy(alpha = 0.8f) else YepSlate500,
                                        )
                                        Text(
                                            text = msg.body,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isMine) YepWhite else YepSlateDark,
                                        )
                                    }
                                }
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
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("Write a message...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            if (draft.isNotBlank()) {
                                onSend(draft)
                                draft = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = YepBlue)
                    }
                }
            }
        }
    }
}
