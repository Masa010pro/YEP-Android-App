package com.gruop11.yepapp.yep.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gruop11.yepapp.ui.theme.*
import com.gruop11.yepapp.yep.i18n.YepStrings
import com.gruop11.yepapp.yep.model.*

@Composable
fun YepTopAppBar(
    activeUser: UserProfile?,
    language: AppLanguage,
    isOfflineDataSaver: Boolean,
    unreadCount: Int,
    strings: YepStrings,
    onBrandClick: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleOffline: (Boolean) -> Unit,
    onNotificationsClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    var langExpanded by remember { mutableStateOf(false) }

    Surface(
        color = YepWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Brand Mark + Active Role
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onBrandClick)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .testTag("top_brand_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(listOf(YepEmerald, YepEmeraldDark)),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "YEP",
                            color = YepWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "YEP",
                            style = MaterialTheme.typography.titleSmall,
                            color = YepSlateDark,
                            maxLines = 1,
                        )
                        Text(
                            text = activeUser?.let { strings.roleLabel(it.role) } ?: strings.navOverview,
                            style = MaterialTheme.typography.labelSmall,
                            color = YepEmeraldDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // 6-Language Selector Dropdown Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = YepSlate100,
                        border = BorderStroke(1.dp, YepSlate200),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { langExpanded = true }
                            .testTag("language_selector_pill"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = language.code,
                                style = MaterialTheme.typography.labelMedium,
                                color = YepSlateDark,
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select language",
                                tint = YepSlate600,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false },
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${lang.code} · ${lang.nativeName}",
                                            fontWeight = if (lang == language) FontWeight.Bold else FontWeight.Medium,
                                            color = if (lang == language) YepEmeraldDark else YepSlateDark,
                                        )
                                        Text(
                                            text = lang.regionSubtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = YepSlate500,
                                        )
                                    }
                                },
                                leadingIcon = {
                                    if (lang == language) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = YepEmerald,
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectLanguage(lang)
                                    langExpanded = false
                                },
                            )
                        }
                    }
                }

                // Online / Offline Data Saver Toggle Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isOfflineDataSaver) YepAmberSoft else YepEmeraldSoft,
                    border = BorderStroke(
                        1.dp,
                        if (isOfflineDataSaver) YepAmberBorder else YepEmeraldBorder,
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onToggleOffline(!isOfflineDataSaver) }
                        .testTag("offline_toggle_chip"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = if (isOfflineDataSaver) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = if (isOfflineDataSaver) "Offline Data Saver" else "Online",
                            tint = if (isOfflineDataSaver) YepAmber else YepEmeraldDark,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = if (isOfflineDataSaver) strings.offlineLabel else strings.onlineLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOfflineDataSaver) YepAmber else YepEmeraldDark,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }

                // Notifications Bell
                if (activeUser != null) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("notifications_bell_button"),
                        ) {
                            Icon(
                                imageVector = if (unreadCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = strings.notificationsTitle,
                                tint = YepSlate800,
                                modifier = Modifier.size(21.dp),
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(YepRose),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = YepWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    UserAvatar(
                        user = activeUser,
                        size = 30.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onAvatarClick)
                            .testTag("top_avatar_button"),
                    )

                    IconButton(
                        onClick = onSignOutClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("sign_out_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = strings.signOut,
                            tint = YepSlate600,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = YepEmerald,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onSignInClick)
                            .testTag("top_sign_in_button"),
                    ) {
                        Text(
                            text = strings.navSignIn,
                            color = YepWhite,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = YepSlate200)
        }
    }
}

@Composable
fun OfflineDataSaverBanner(
    visible: Boolean,
    pendingCount: Int,
    strings: YepStrings,
    onGoOnlineAndSync: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        Surface(
            color = YepAmberSoft,
            border = BorderStroke(1.dp, YepAmberBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = YepAmber,
                    modifier = Modifier.size(18.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.offlineBannerMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = YepSlateDark,
                        fontWeight = FontWeight.Medium,
                    )
                    if (pendingCount > 0) {
                        Text(
                            text = "$pendingCount submission(s) waiting in offline queue",
                            style = MaterialTheme.typography.labelSmall,
                            color = YepAmber,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                TextButton(
                    onClick = onGoOnlineAndSync,
                    modifier = Modifier.testTag("go_online_sync_button"),
                ) {
                    Text(
                        text = strings.goOnlineSync,
                        style = MaterialTheme.typography.labelMedium,
                        color = YepEmeraldDark,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun YepBottomNavigationBar(
    activeUser: UserProfile?,
    currentRoute: String,
    strings: YepStrings,
    onNavigate: (String) -> Unit,
) {
    val items = remember(activeUser?.role, strings) {
        when (activeUser?.role) {
            null -> listOf(
                NavDestinationItem("overview", strings.navOverview, Icons.Filled.Home, Icons.Outlined.Home),
                NavDestinationItem("exchanges", strings.navExchanges, Icons.Filled.School, Icons.Outlined.School),
                NavDestinationItem("opportunities", strings.navOpportunities, Icons.Filled.Work, Icons.Outlined.WorkOutline),
                NavDestinationItem("signin", strings.navSignIn, Icons.Filled.Person, Icons.Outlined.Person),
            )
            UserRole.PARTICIPANT -> listOf(
                NavDestinationItem("dashboard", strings.navDashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                NavDestinationItem("exchanges", strings.navExchanges, Icons.Filled.School, Icons.Outlined.School),
                NavDestinationItem("opportunities", strings.navOpportunities, Icons.Filled.Work, Icons.Outlined.WorkOutline),
            )
            UserRole.MENTOR -> listOf(
                NavDestinationItem("mentor", strings.navMentor, Icons.Filled.Assessment, Icons.Outlined.Assessment),
                NavDestinationItem("exchanges", strings.navExchanges, Icons.Filled.School, Icons.Outlined.School),
                NavDestinationItem("opportunities", strings.navOpportunities, Icons.Filled.Work, Icons.Outlined.WorkOutline),
            )
            UserRole.PARTNER -> listOf(
                NavDestinationItem("partner-manage", strings.navPipeline, Icons.Filled.Handshake, Icons.Outlined.Handshake),
                NavDestinationItem("opportunities", strings.navOpportunities, Icons.Filled.Work, Icons.Outlined.WorkOutline),
                NavDestinationItem("exchanges", strings.navExchanges, Icons.Filled.School, Icons.Outlined.School),
            )
            UserRole.ADMIN -> listOf(
                NavDestinationItem("admin", strings.navControl, Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
                NavDestinationItem("exchanges", strings.navExchanges, Icons.Filled.School, Icons.Outlined.School),
                NavDestinationItem("opportunities", strings.navOpportunities, Icons.Filled.Work, Icons.Outlined.WorkOutline),
            )
        }
    }

    NavigationBar(
        containerColor = YepWhite,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YepEmeraldDark,
                    selectedTextColor = YepEmeraldDark,
                    indicatorColor = YepEmeraldSoft,
                    unselectedIconColor = YepSlate500,
                    unselectedTextColor = YepSlate500,
                ),
                modifier = Modifier.testTag("bottom_nav_${item.route}"),
            )
        }
    }
}

@Composable
fun UserAvatar(
    user: UserProfile,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (user.role) {
        UserRole.PARTICIPANT -> YepEmerald
        UserRole.MENTOR -> YepIndigo
        UserRole.PARTNER -> YepBlue
        UserRole.ADMIN -> YepSlateDark
    }
    val initials = remember(user.name) {
        user.name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "YP" }
    }
    val decodedDataUrlBitmap = remember(user.photoUri) {
        val raw = user.photoUri
        if (raw != null && raw.startsWith("data:image")) {
            val base64Part = raw.substringAfter("base64,", "")
            if (base64Part.isNotBlank()) {
                runCatching {
                    val bytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?.asImageBitmap()
                }.getOrNull()
            } else null
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.5.dp, YepEmeraldBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            decodedDataUrlBitmap != null -> {
                androidx.compose.foundation.Image(
                    bitmap = decodedDataUrlBitmap,
                    contentDescription = "${user.name} avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            !user.photoUri.isNullOrBlank() -> {
                AsyncImage(
                    model = user.photoUri,
                    contentDescription = "${user.name} avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                Text(
                    text = initials,
                    color = YepWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.36f).sp,
                )
            }
        }
    }
}

@Composable
fun YepCard(
    modifier: Modifier = Modifier,
    containerColor: Color = YepWhite,
    borderColor: Color = YepSlate200,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    softBg: Color,
    modifier: Modifier = Modifier,
) {
    YepCard(
        modifier = modifier,
        contentPadding = 12.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(softBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = YepSlateDark,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = YepSlate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color = Color.Transparent,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
fun StageMilestoneTrack(
    snapshot: ParticipantStageSnapshot,
    strings: YepStrings,
) {
    val labels = listOf(
        strings.stageOnboard,
        strings.stageEnrolled,
        strings.stageLearning,
        strings.stageCapstone,
        strings.stageRubric,
        strings.stagePartner,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(
                    text = "Stage ${snapshot.currentStage}/6",
                    textColor = YepEmeraldDark,
                    bgColor = YepEmeraldSoft,
                    borderColor = YepEmeraldBorder,
                )
                StatusBadge(
                    text = snapshot.skillTier.label,
                    textColor = YepIndigo,
                    bgColor = YepIndigoSoft,
                )
            }
            Text(
                text = "${snapshot.overallPercent}% Overall",
                style = MaterialTheme.typography.labelLarge,
                color = YepSlateDark,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            snapshot.stages.forEachIndexed { idx, st ->
                val active = st.stageNumber <= snapshot.currentStage
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    st.isComplete -> YepEmerald
                                    active -> YepBlue
                                    else -> YepSlate200
                                },
                            ),
                    )
                    Text(
                        text = labels.getOrElse(idx) { "S${st.stageNumber}" },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (active) YepSlateDark else YepSlate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
