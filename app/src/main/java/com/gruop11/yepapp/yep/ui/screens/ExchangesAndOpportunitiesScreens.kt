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
import com.gruop11.yepapp.yep.ui.components.StatusBadge
import com.gruop11.yepapp.yep.ui.components.YepCard
import com.gruop11.yepapp.yep.ui.dialogs.CapstoneSubmitDialog
import com.gruop11.yepapp.yep.ui.dialogs.StudyPackDialog

@Composable
fun ExchangesCatalogScreen(
    activeUser: UserProfile?,
    snapshot: YepStateSnapshot,
    isOfflineDataSaver: Boolean,
    strings: YepStrings,
    onEnroll: (String) -> Unit,
    onToggleChecklist: (String, String) -> Unit,
    onSubmitCapstone: (String, String, String, String, String, String) -> String?,
) {
    var selectedTheme by remember { mutableStateOf("All") }
    var selectedLevel by remember { mutableStateOf<SkillTier?>(null) }
    var detailExchange by remember { mutableStateOf<VirtualExchange?>(null) }
    var capstoneExchange by remember { mutableStateOf<VirtualExchange?>(null) }
    var studyPackPair by remember { mutableStateOf<Pair<VirtualExchange, Enrollment?>?>(null) }

    val themes = remember(snapshot.exchanges) {
        listOf("All") + snapshot.exchanges.map { it.theme }.distinct()
    }
    val filteredExchanges = remember(snapshot.exchanges, selectedTheme, selectedLevel) {
        snapshot.exchanges.filter { ex ->
            (selectedTheme == "All" || ex.theme == selectedTheme) &&
                (selectedLevel == null || ex.skillLevel == selectedLevel)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(strings.navExchanges, style = MaterialTheme.typography.headlineSmall, color = YepSlateDark)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                themes.forEach { t ->
                    FilterChip(
                        selected = selectedTheme == t,
                        onClick = { selectedTheme = t },
                        label = { Text(t) },
                    )
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = selectedLevel == null,
                    onClick = { selectedLevel = null },
                    label = { Text(strings.filterAll) },
                )
                SkillTier.entries.forEach { lvl ->
                    FilterChip(
                        selected = selectedLevel == lvl,
                        onClick = { selectedLevel = lvl },
                        label = { Text(strings.skillTierLabel(lvl)) },
                    )
                }
            }
        }

        items(filteredExchanges, key = { it.id }) { exchange ->
            val enrollment = activeUser?.let { u ->
                snapshot.enrollments.firstOrNull { it.userId == u.id && it.exchangeId == exchange.id }
            }
            val isEnrolled = enrollment != null

            YepCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(exchange.theme, YepIndigo, YepIndigoSoft)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (exchange.isFlagged) {
                            StatusBadge("Moderated", YepRose, YepRoseSoft)
                        }
                        StatusBadge(
                            text = if (exchange.isFull) "${exchange.enrolledCount}/${exchange.maxCapacity} · Full"
                            else "${exchange.enrolledCount}/${exchange.maxCapacity} seats",
                            textColor = if (exchange.isFull) YepRose else YepEmeraldDark,
                            bgColor = if (exchange.isFull) YepRoseSoft else YepEmeraldSoft,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(exchange.title, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${strings.skillTierLabel(exchange.skillLevel)} · ${exchange.formatLabel} · ${exchange.durationLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = YepSlate500,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(exchange.description, style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { detailExchange = exchange },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("exchange_detail_${exchange.id}"),
                    ) {
                        Text(strings.detailsButton, maxLines = 1)
                    }
                    if (isEnrolled) {
                        Button(
                            onClick = { capstoneExchange = exchange },
                            colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("exchange_submit_${exchange.id}"),
                        ) {
                            Text(strings.submitCapstone, maxLines = 1)
                        }
                    } else {
                        Button(
                            enabled = !exchange.isFull && !exchange.isFlagged,
                            onClick = { onEnroll(exchange.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = YepSlateDark),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("exchange_enroll_${exchange.id}"),
                        ) {
                            Text(if (exchange.isFull) strings.cohortFull else strings.enrollNow, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    detailExchange?.let { ex ->
        val enrollment = activeUser?.let { u ->
            snapshot.enrollments.firstOrNull { it.userId == u.id && it.exchangeId == ex.id }
        }
        val sessions = snapshot.liveSessions.filter { it.exchangeId == ex.id }

        Dialog(
            onDismissRequest = { detailExchange = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 650.dp),
                shape = RoundedCornerShape(20.dp),
                color = YepWhite,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(ex.title, style = MaterialTheme.typography.titleMedium, color = YepSlateDark, modifier = Modifier.weight(1f))
                        IconButton(onClick = { detailExchange = null }) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                    Text(ex.description, style = MaterialTheme.typography.bodySmall, color = YepSlate600)

                    Text(strings.learningOutcomes, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                    ex.outcomes.forEach { out ->
                        Text("• $out", style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                    }

                    Text(strings.prerequisitesLabel, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                    ex.prerequisites.forEach { pre ->
                        Text("• $pre", style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                    }

                    Text(strings.assignmentPrompt, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                    Surface(shape = RoundedCornerShape(10.dp), color = YepEmeraldSoft) {
                        Text(ex.assignmentPrompt, style = MaterialTheme.typography.bodySmall, color = YepEmeraldDark, modifier = Modifier.padding(10.dp))
                    }

                    if (sessions.isNotEmpty()) {
                        Text(strings.sessionSchedule, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                        sessions.forEach { s ->
                            Text("• ${s.title} — ${s.scheduledAt} (${s.durationLabel})", style = MaterialTheme.typography.bodySmall, color = YepBlue)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                detailExchange = null
                                studyPackPair = ex to enrollment
                            },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                        ) {
                            Text(strings.offlineStudyPack, maxLines = 1)
                        }
                        if (enrollment == null) {
                            Button(
                                enabled = !ex.isFull && !ex.isFlagged,
                                onClick = {
                                    onEnroll(ex.id)
                                    detailExchange = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                            ) {
                                Text(if (ex.isFull) strings.cohortFull else strings.enrollNow)
                            }
                        }
                    }
                }
            }
        }
    }

    capstoneExchange?.let { ex ->
        CapstoneSubmitDialog(
            exchange = ex,
            isOfflineDataSaver = isOfflineDataSaver,
            onDismiss = { capstoneExchange = null },
            onSubmit = { title, arch, gh, demo, takeaways ->
                onSubmitCapstone(ex.id, title, arch, gh, demo, takeaways)
            },
        )
    }

    studyPackPair?.let { (ex, enr) ->
        StudyPackDialog(
            exchange = ex,
            enrollment = enr,
            onDismiss = { studyPackPair = null },
            onToggleChecklist = onToggleChecklist,
        )
    }
}

@Composable
fun OpportunitiesBoardScreen(
    activeUser: UserProfile?,
    snapshot: YepStateSnapshot,
    strings: YepStrings,
    onSubmitEoi: (opportunityId: String, coverNote: String, consentConfirmed: Boolean) -> String?,
) {
    var workTypeFilter by remember { mutableStateOf("All") }
    var locationFilter by remember { mutableStateOf("All") }
    var targetOpportunity by remember { mutableStateOf<Opportunity?>(null) }

    val workTypes = remember(snapshot.opportunities) {
        listOf("All") + snapshot.opportunities.map { it.workType }.distinct()
    }
    val locations = remember(snapshot.opportunities) {
        listOf("All") + snapshot.opportunities.map { it.location }.distinct()
    }
    val filtered = remember(snapshot.opportunities, workTypeFilter, locationFilter) {
        snapshot.opportunities.filter { opp ->
            (workTypeFilter == "All" || opp.workType == workTypeFilter) &&
                (locationFilter == "All" || opp.location == locationFilter)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(strings.navOpportunities, style = MaterialTheme.typography.headlineSmall, color = YepSlateDark)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                workTypes.forEach { wt ->
                    FilterChip(selected = workTypeFilter == wt, onClick = { workTypeFilter = wt }, label = { Text(wt) })
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                locations.forEach { loc ->
                    FilterChip(selected = locationFilter == loc, onClick = { locationFilter = loc }, label = { Text(loc) })
                }
            }
        }

        items(filtered, key = { it.id }) { opp ->
            val existingEoi = activeUser?.let { u ->
                snapshot.eois.firstOrNull { it.opportunityId == opp.id && it.participantId == u.id }
            }

            YepCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(opp.workType, YepBlue, YepBlueSoft)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (opp.isFlagged) {
                            StatusBadge("Moderated", YepRose, YepRoseSoft)
                        }
                        StatusBadge(opp.compensation, YepEmeraldDark, YepEmeraldSoft, YepEmeraldBorder)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(opp.title, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                Text("${opp.organization} · ${opp.location}", style = MaterialTheme.typography.labelMedium, color = YepSlate500)
                Spacer(modifier = Modifier.height(6.dp))
                Text(opp.description, style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    opp.requiredSkills.forEach { sk ->
                        StatusBadge(sk, YepSlate800, YepSlate100)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (existingEoi != null) {
                    StatusBadge(
                        text = "${strings.appliedStatusPrefix} ${strings.eoiStatusLabel(existingEoi.status)}",
                        textColor = YepEmeraldDark,
                        bgColor = YepEmeraldSoft,
                        borderColor = YepEmeraldBorder,
                    )
                } else {
                    Button(
                        enabled = !opp.isFlagged,
                        onClick = { targetOpportunity = opp },
                        colors = ButtonDefaults.buttonColors(containerColor = YepSlateDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("express_interest_${opp.id}"),
                    ) {
                        Text(strings.expressInterest)
                    }
                }
            }
        }
    }

    targetOpportunity?.let { opp ->
        var coverNote by remember { mutableStateOf("") }
        var consentChecked by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        Dialog(
            onDismissRequest = { targetOpportunity = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.94f),
                shape = RoundedCornerShape(20.dp),
                color = YepWhite,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(strings.expressInterest, style = MaterialTheme.typography.titleLarge, color = YepSlateDark)
                        IconButton(onClick = { targetOpportunity = null }) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                    Text("${opp.title} — ${opp.organization}", style = MaterialTheme.typography.titleSmall, color = YepEmeraldDark)

                    OutlinedTextField(
                        value = coverNote,
                        onValueChange = { coverNote = it },
                        label = { Text(strings.noteToHiringPartner) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("eoi_cover_note"),
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YepEmeraldSoft,
                        border = BorderStroke(1.dp, YepEmeraldBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = consentChecked,
                                onCheckedChange = { consentChecked = it },
                                colors = CheckboxDefaults.colors(checkedColor = YepEmerald),
                                modifier = Modifier.testTag("eoi_consent_checkbox"),
                            )
                            Text(
                                text = "${strings.explicitConsentText} ${opp.organization}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = YepSlateDark,
                            )
                        }
                    }

                    error?.let { Text(it, color = YepRose, style = MaterialTheme.typography.bodySmall) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { targetOpportunity = null }) { Text(strings.cancelLabel) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = consentChecked,
                            onClick = {
                                val err = onSubmitEoi(opp.id, coverNote, consentChecked)
                                if (err != null) error = err else targetOpportunity = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                            modifier = Modifier.testTag("eoi_submit_button"),
                        ) {
                            Text(strings.sendConsentedEoi)
                        }
                    }
                }
            }
        }
    }
}
