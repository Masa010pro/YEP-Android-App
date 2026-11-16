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
import com.gruop11.yepapp.ui.theme.*
import com.gruop11.yepapp.yep.i18n.YepStrings
import com.gruop11.yepapp.yep.model.*
import com.gruop11.yepapp.yep.ui.charts.CohortTierStackedBarChart
import com.gruop11.yepapp.yep.ui.charts.CompetencyTrajectoryMultiLineChart
import com.gruop11.yepapp.yep.ui.charts.ImpactEngagementAreaChart
import com.gruop11.yepapp.yep.ui.components.StatusBadge
import com.gruop11.yepapp.yep.ui.components.UserAvatar
import com.gruop11.yepapp.yep.ui.components.YepCard
import com.gruop11.yepapp.platform.sync.YepSyncClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminControlScreen(
    admin: UserProfile,
    snapshot: YepStateSnapshot,
    strings: YepStrings,
    syncedVersion: Int,
    isSyncing: Boolean,
    lastSyncOk: Boolean,
    isOfflineDataSaver: Boolean,
    exportedSummary: String?,
    onEditOwnProfile: () -> Unit,
    onEditAnyUser: (UserProfile) -> Unit,
    onToggleUserSuspension: (String, String) -> Unit,
    onToggleExchangeFlag: (String) -> Unit,
    onToggleOpportunityFlag: (String) -> Unit,
    onExportZeroPii: (timeframe: String, themeFilter: String) -> Unit,
    onDismissExportSummary: () -> Unit,
    onSyncNow: () -> Unit,
    onResetSharedState: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var timeframe by remember { mutableStateOf("6M") }
    var themeFilter by remember { mutableStateOf("All") }
    var suspendTargetUser by remember { mutableStateOf<UserProfile?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val themes = remember(snapshot.exchanges) {
        listOf("All") + snapshot.exchanges.map { it.theme }.distinct()
    }
    val themeMultiplier = if (themeFilter == "All") 1f else 0.42f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Admin Header
        YepCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UserAvatar(admin, size = 48.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(admin.name, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    Text("${admin.title} · ${admin.organization}", style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                }
                OutlinedButton(
                    onClick = onEditOwnProfile,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) {
                    Text(strings.editProfile)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOfflineDataSaver) YepAmberSoft else if (lastSyncOk) YepEmeraldSoft else YepRoseSoft,
                border = BorderStroke(1.dp, if (isOfflineDataSaver) YepAmberBorder else YepEmeraldBorder),
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                isOfflineDataSaver -> "Web Sync Paused (Data Saver)"
                                isSyncing -> "Syncing with Web App..."
                                lastSyncOk -> "2-Way Web Sync Active (v$syncedVersion)"
                                else -> "Sync Reconnecting (Local Saved)"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = YepSlateDark,
                        )
                        Text(
                            text = YepSyncClient.SYNC_BASE_URL.removePrefix("https://").take(26) + "…",
                            style = MaterialTheme.typography.labelSmall,
                            color = YepSlate500,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onSyncNow,
                            enabled = !isOfflineDataSaver && !isSyncing,
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                        ) {
                            Text("Sync Now", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            enabled = !isOfflineDataSaver && !isSyncing,
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                        ) {
                            Text("Reset Shared", maxLines = 1)
                        }
                    }
                }
            }
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset Shared Web & Mobile State?") },
                text = { Text("This calls /api/sync/reset on the YEP backend and restores seeded baseline data across both apps.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirm = false
                            onResetSharedState()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YepRose),
                    ) { Text("Reset") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
                },
            )
        }

        // Segmented Tabs
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                0 to strings.impactAnalyticsTab,
                1 to strings.usersTab,
                2 to strings.moderationTab,
                3 to strings.auditTrailTab,
            ).forEach { (idx, label) ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { Text(label) },
                    modifier = Modifier.testTag("admin_tab_$idx"),
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Impact Analytics with 3 Compose Canvas Charts
                YepCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Timeframe & Theme", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("6M", "12M").forEach { tf ->
                                FilterChip(
                                    selected = timeframe == tf,
                                    onClick = { timeframe = tf },
                                    label = { Text(tf) },
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        themes.forEach { t ->
                            FilterChip(
                                selected = themeFilter == t,
                                onClick = { themeFilter = t },
                                label = { Text(t) },
                            )
                        }
                    }
                }

                YepCard {
                    Text("Youth Engagement, Enrollments & Completions", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    ImpactEngagementAreaChart(timeframe = timeframe, themeMultiplier = themeMultiplier)
                }

                YepCard {
                    Text("4-Pillar Skill Competency Trajectory (2.6–5.0)", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    CompetencyTrajectoryMultiLineChart(timeframe = timeframe)
                }

                YepCard {
                    Text("Cohort Tier Distribution Shifts", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    CohortTierStackedBarChart()
                }

                Button(
                    onClick = { onExportZeroPii(timeframe, themeFilter) },
                    colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("export_zero_pii_button"),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.exportImpactSummary)
                }
            }

            1 -> {
                // User Governance
                snapshot.users.forEach { u ->
                    YepCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            UserAvatar(u, size = 42.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                                Text("${u.role.label} · ${u.regionalHub} · Age ${u.age}", style = MaterialTheme.typography.bodySmall, color = YepSlate500)
                                if (u.isSuspended) {
                                    Text("Suspended: ${u.suspensionReason.orEmpty()}", style = MaterialTheme.typography.labelSmall, color = YepRose)
                                }
                            }
                            StatusBadge(
                                text = if (u.isSuspended) "Suspended" else "Active",
                                textColor = if (u.isSuspended) YepRose else YepEmeraldDark,
                                bgColor = if (u.isSuspended) YepRoseSoft else YepEmeraldSoft,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onEditAnyUser(u) },
                                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                            ) {
                                Text(strings.editProfile)
                            }
                            if (u.id != admin.id) {
                                Button(
                                    onClick = {
                                        if (u.isSuspended) onToggleUserSuspension(u.id, "")
                                        else suspendTargetUser = u
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (u.isSuspended) YepEmerald else YepRose,
                                    ),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                                ) {
                                    Text(if (u.isSuspended) "Unlock" else "Suspend")
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Content Moderation
                Text("Virtual Exchanges", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                snapshot.exchanges.forEach { ex ->
                    YepCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ex.title, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                                Text("${ex.theme} · ${ex.enrolledCount}/${ex.maxCapacity}", style = MaterialTheme.typography.bodySmall, color = YepSlate500)
                            }
                            Button(
                                onClick = { onToggleExchangeFlag(ex.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ex.isFlagged) YepEmerald else YepRose,
                                ),
                            ) {
                                Text(if (ex.isFlagged) "Unflag" else "Flag")
                            }
                        }
                    }
                }

                Text("Partner Opportunities", style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                snapshot.opportunities.forEach { opp ->
                    YepCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(opp.title, style = MaterialTheme.typography.titleSmall, color = YepSlateDark)
                                Text("${opp.organization} · ${opp.compensation}", style = MaterialTheme.typography.bodySmall, color = YepSlate500)
                            }
                            Button(
                                onClick = { onToggleOpportunityFlag(opp.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (opp.isFlagged) YepEmerald else YepRose,
                                ),
                            ) {
                                Text(if (opp.isFlagged) "Unflag" else "Flag")
                            }
                        }
                    }
                }
            }

            3 -> {
                // Immutable Audit Trail
                val fmt = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.US) }
                snapshot.auditLogs.forEach { entry ->
                    YepCard(contentPadding = 12.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            StatusBadge(entry.actionType.name, YepIndigo, YepIndigoSoft)
                            Text(fmt.format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall, color = YepSlate500)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(entry.summary, style = MaterialTheme.typography.bodySmall, color = YepSlateDark, fontWeight = FontWeight.Medium)
                        Text("Actor: ${entry.actorName}", style = MaterialTheme.typography.labelSmall, color = YepSlate500)
                    }
                }
            }
        }
    }

    suspendTargetUser?.let { target ->
        var reason by remember { mutableStateOf("Safeguarding / verification review") }
        Dialog(onDismissRequest = { suspendTargetUser = null }) {
            Surface(shape = RoundedCornerShape(20.dp), color = YepWhite) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Suspend ${target.name}", style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Suspension Reason") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { suspendTargetUser = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onToggleUserSuspension(target.id, reason)
                                suspendTargetUser = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YepRose),
                        ) {
                            Text("Confirm Suspend")
                        }
                    }
                }
            }
        }
    }

    exportedSummary?.let { text ->
        Dialog(onDismissRequest = onDismissExportSummary) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = YepWhite,
                border = BorderStroke(1.dp, YepEmeraldBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = YepEmerald)
                        Text("Zero-PII Export Ready", style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    }
                    Text(text, style = MaterialTheme.typography.bodySmall, color = YepSlate600)
                    Button(
                        onClick = onDismissExportSummary,
                        colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
