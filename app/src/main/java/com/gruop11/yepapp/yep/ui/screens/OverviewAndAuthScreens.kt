package com.gruop11.yepapp.yep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gruop11.yepapp.R
import com.gruop11.yepapp.ui.theme.*
import com.gruop11.yepapp.yep.data.SeedData
import com.gruop11.yepapp.yep.i18n.YepStrings
import com.gruop11.yepapp.yep.model.UserProfile
import com.gruop11.yepapp.yep.model.UserRole
import com.gruop11.yepapp.yep.ui.components.KpiMetricCard
import com.gruop11.yepapp.yep.ui.components.StatusBadge
import com.gruop11.yepapp.yep.ui.components.YepCard

@Composable
fun PublicOverviewScreen(
    activeCohortsCount: Int,
    publishedOpeningsCount: Int,
    strings: YepStrings,
    onExploreExchanges: () -> Unit,
    onSignInClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 16:9 Hero Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = YepSlateDark,
            border = BorderStroke(1.dp, YepSlate800),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_yep_hero),
                        contentDescription = "Youth Empowerment Project cohort collaborating",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, YepSlateDark.copy(alpha = 0.88f)),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                    ) {
                        StatusBadge(
                            text = "YOUTH EMPOWERMENT PROJECT · EAST & WEST AFRICA",
                            textColor = YepEmeraldSoft,
                            bgColor = YepEmeraldDark.copy(alpha = 0.9f),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.heroTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = YepWhite,
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = strings.heroSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = YepSlate200,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onSignInClick,
                            colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("overview_signin_cta"),
                        ) {
                            Text(strings.signInRegister, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onExploreExchanges,
                            border = BorderStroke(1.dp, YepEmeraldBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = YepEmeraldSoft),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("overview_explore_cta"),
                        ) {
                            Text(strings.exploreExchanges, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Program KPIs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KpiMetricCard(
                title = strings.activeCohortsLabel,
                value = activeCohortsCount.toString(),
                icon = Icons.Default.School,
                accentColor = YepEmerald,
                softBg = YepEmeraldSoft,
                modifier = Modifier.weight(1f),
            )
            KpiMetricCard(
                title = strings.publishedOpeningsLabel,
                value = publishedOpeningsCount.toString(),
                icon = Icons.Default.Work,
                accentColor = YepBlue,
                softBg = YepBlueSoft,
                modifier = Modifier.weight(1f),
            )
        }

        // 3 Core Pillars
        PillarCard(
            icon = Icons.Default.CloudSync,
            title = strings.pillarOfflineTitle,
            description = strings.pillarOfflineDesc,
            accent = YepAmber,
            softBg = YepAmberSoft,
        )
        PillarCard(
            icon = Icons.Default.Grading,
            title = strings.pillarRubricTitle,
            description = strings.pillarRubricDesc,
            accent = YepIndigo,
            softBg = YepIndigoSoft,
        )
        PillarCard(
            icon = Icons.Default.VerifiedUser,
            title = strings.pillarConsentTitle,
            description = strings.pillarConsentDesc,
            accent = YepEmeraldDark,
            softBg = YepEmeraldSoft,
        )
    }
}

@Composable
private fun PillarCard(
    icon: ImageVector,
    title: String,
    description: String,
    accent: Color,
    softBg: Color,
) {
    YepCard(contentPadding = 14.dp) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(softBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = accent)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                Text(description, style = MaterialTheme.typography.bodySmall, color = YepSlate600)
            }
        }
    }
}

@Composable
fun SignInScreen(
    users: List<UserProfile>,
    strings: YepStrings,
    onQuickSignIn: (String) -> Unit,
    onEmailSignIn: (String, String) -> String?,
    onRegister: (
        role: UserRole,
        name: String,
        email: String,
        password: String,
        regionalHub: String,
        age: Int,
        skillsRaw: String,
        careerGoal: String,
        bio: String,
    ) -> String?,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signInError by remember { mutableStateOf<String?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    val quickAccounts = remember(users) {
        listOfNotNull(
            users.firstOrNull { it.email.equals("amara.okafor@outlook.com", ignoreCase = true) }
                ?: users.firstOrNull { it.role == UserRole.PARTICIPANT },
            users.firstOrNull { it.email.equals("marcus.vance@opendev.foundation", ignoreCase = true) }
                ?: users.firstOrNull { it.role == UserRole.MENTOR },
            users.firstOrNull { it.email.equals("elena.rostova@afritechhorizons.io", ignoreCase = true) }
                ?: users.firstOrNull { it.role == UserRole.PARTNER },
            users.firstOrNull { it.email.equals("sarah.chen@yep-initiative.org", ignoreCase = true) }
                ?: users.firstOrNull { it.role == UserRole.ADMIN },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YepSlateBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = strings.quickTestAccountsTitle,
            style = MaterialTheme.typography.titleLarge,
            color = YepSlateDark,
        )

        // 2x2 Mobile Grid of 1-Click Test Accounts
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quickAccounts.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    pair.forEach { account ->
                        val (accent, soft) = when (account.role) {
                            UserRole.PARTICIPANT -> YepEmerald to YepEmeraldSoft
                            UserRole.MENTOR -> YepIndigo to YepIndigoSoft
                            UserRole.PARTNER -> YepBlue to YepBlueSoft
                            UserRole.ADMIN -> YepSlateDark to YepSlate100
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = YepWhite,
                            border = BorderStroke(1.dp, YepSlate200),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 96.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onQuickSignIn(account.id) }
                                .testTag("quick_login_${account.role.name.lowercase()}"),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                StatusBadge(account.role.label, accent, soft)
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = YepSlateDark,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = account.regionalHub,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = YepSlate500,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Email Sign-In Card
        YepCard {
            Text("Email Sign-In", style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("signin_email_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("signin_password_input"),
            )
            signInError?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, color = YepRose, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { signInError = onEmailSignIn(email, password) },
                colors = ButtonDefaults.buttonColors(containerColor = YepSlateDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("signin_submit_button"),
            ) {
                Text(strings.navSignIn)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showRegisterDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("open_register_button"),
            ) {
                Text(strings.createAccountTitle)
            }
        }
    }

    if (showRegisterDialog) {
        RegisterAccountDialog(
            strings = strings,
            onDismiss = { showRegisterDialog = false },
            onRegister = onRegister,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAccountDialog(
    strings: YepStrings,
    onDismiss: () -> Unit,
    onRegister: (
        role: UserRole,
        name: String,
        email: String,
        password: String,
        regionalHub: String,
        age: Int,
        skillsRaw: String,
        careerGoal: String,
        bio: String,
    ) -> String?,
) {
    var role by remember { mutableStateOf(UserRole.PARTICIPANT) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var regionalHub by remember { mutableStateOf(SeedData.regionalHubs.first()) }
    var ageFloat by remember { mutableFloatStateOf(22f) }
    var skillsRaw by remember { mutableStateOf("Python, Git Workflow") }
    var careerGoal by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var hubMenuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val age = ageFloat.toInt()
    val ageEligible = role != UserRole.PARTICIPANT || age in 18..35
    val has8Chars = password.length >= 8
    val hasDigit = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp),
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
                    Text(strings.createAccountTitle, style = MaterialTheme.typography.titleLarge, color = YepSlateDark)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                // Role Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(UserRole.PARTICIPANT, UserRole.MENTOR, UserRole.PARTNER).forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = {
                                Text(
                                    text = when (r) {
                                        UserRole.PARTICIPANT -> "Participant"
                                        UserRole.MENTOR -> "Mentor"
                                        UserRole.PARTNER -> "Partner"
                                        else -> r.label
                                    },
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Age Verification Slider (16-40) when Participant
                if (role == UserRole.PARTICIPANT) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (ageEligible) YepEmeraldSoft else YepRoseSoft,
                        border = BorderStroke(1.dp, if (ageEligible) YepEmeraldBorder else YepRose),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Age Verification (18–35 Policy)", style = MaterialTheme.typography.labelLarge, color = YepSlateDark)
                                Text("$age yrs", style = MaterialTheme.typography.titleSmall, color = if (ageEligible) YepEmeraldDark else YepRose)
                            }
                            Slider(
                                value = ageFloat,
                                onValueChange = { ageFloat = it },
                                valueRange = 16f..40f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (ageEligible) YepEmerald else YepRose,
                                    activeTrackColor = if (ageEligible) YepEmerald else YepRose,
                                ),
                                modifier = Modifier.testTag("register_age_slider"),
                            )
                            Text(
                                text = if (ageEligible) strings.ageEligibilityValid else strings.ageEligibilityBlocked,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (ageEligible) YepEmeraldDark else YepRose,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("register_name_input"),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                )

                // Password Strength Checklist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StrengthPill("8+ chars", has8Chars)
                    StrengthPill("Number", hasDigit)
                    StrengthPill("Symbol (opt)", hasSymbol)
                }

                ExposedDropdownMenuBox(
                    expanded = hubMenuExpanded,
                    onExpandedChange = { hubMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = regionalHub,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Regional Hub") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hubMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = hubMenuExpanded,
                        onDismissRequest = { hubMenuExpanded = false },
                    ) {
                        SeedData.regionalHubs.forEach { hub ->
                            DropdownMenuItem(
                                text = { Text(hub) },
                                onClick = {
                                    regionalHub = hub
                                    hubMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = skillsRaw,
                    onValueChange = { skillsRaw = it },
                    label = { Text("Skills (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = careerGoal,
                    onValueChange = { careerGoal = it },
                    label = { Text("Career Goal") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                errorMsg?.let { Text(it, color = YepRose, style = MaterialTheme.typography.bodySmall) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = ageEligible,
                        onClick = {
                            val err = onRegister(role, name, email, password, regionalHub, age, skillsRaw, careerGoal, bio)
                            if (err != null) errorMsg = err else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                        modifier = Modifier.testTag("register_submit_button"),
                    ) {
                        Text("Create Account")
                    }
                }
            }
        }
    }
}

@Composable
private fun StrengthPill(label: String, met: Boolean) {
    StatusBadge(
        text = (if (met) "✓ " else "○ ") + label,
        textColor = if (met) YepEmeraldDark else YepSlate500,
        bgColor = if (met) YepEmeraldSoft else YepSlate100,
    )
}
