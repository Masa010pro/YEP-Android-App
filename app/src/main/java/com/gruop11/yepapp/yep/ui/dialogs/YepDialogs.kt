package com.gruop11.yepapp.yep.ui.dialogs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gruop11.yepapp.ui.theme.*
import com.gruop11.yepapp.yep.data.SeedData
import com.gruop11.yepapp.yep.i18n.YepStrings
import com.gruop11.yepapp.yep.model.*
import com.gruop11.yepapp.yep.ui.components.StatusBadge
import com.gruop11.yepapp.yep.ui.components.UserAvatar

@Composable
fun NotificationsDialog(
    notifications: List<YepNotification>,
    activeUserId: String,
    strings: YepStrings,
    onDismissDialog: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onSelectNotification: (YepNotification) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf<NotificationCategory?>(null) }
    val filtered = remember(notifications, selectedCategory) {
        if (selectedCategory == null) notifications
        else notifications.filter { it.category == selectedCategory }
    }

    Dialog(
        onDismissRequest = onDismissDialog,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 580.dp),
            shape = RoundedCornerShape(20.dp),
            color = YepWhite,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.notificationsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = YepSlateDark,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onMarkAllRead,
                            modifier = Modifier.testTag("mark_all_read_button"),
                        ) {
                            Text(strings.markAllRead, color = YepEmeraldDark)
                        }
                        IconButton(onClick = onDismissDialog) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text(strings.filterAll) },
                    )
                    FilterChip(
                        selected = selectedCategory == NotificationCategory.SUBMISSION,
                        onClick = { selectedCategory = NotificationCategory.SUBMISSION },
                        label = { Text(strings.filterCapstones) },
                    )
                    FilterChip(
                        selected = selectedCategory == NotificationCategory.MENTOR_FEEDBACK,
                        onClick = { selectedCategory = NotificationCategory.MENTOR_FEEDBACK },
                        label = { Text(strings.filterFeedback) },
                    )
                    FilterChip(
                        selected = selectedCategory == NotificationCategory.OPPORTUNITY,
                        onClick = { selectedCategory = NotificationCategory.OPPORTUNITY },
                        label = { Text(strings.filterOpportunities) },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = YepSlate400,
                                modifier = Modifier.size(40.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(strings.noNotifications, color = YepSlate500)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        items(filtered, key = { it.id }) { notif ->
                            val isRead = activeUserId in notif.readByUserIds
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isRead) YepWhite else YepEmeraldSoft,
                                border = BorderStroke(1.dp, if (isRead) YepSlate200 else YepEmeraldBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectNotification(notif) }
                                    .testTag("notification_item_${notif.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    if (!isRead) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 6.dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(YepEmerald),
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = YepSlateDark,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = YepSlate600,
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDismissNotification(notif.id) },
                                        modifier = Modifier.size(36.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss notification",
                                            tint = YepSlate500,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: UserProfile,
    strings: YepStrings? = null,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> String?,
) {
    val context = LocalContext.current
    var name by remember(user) { mutableStateOf(user.name) }
    var email by remember(user) { mutableStateOf(user.email) }
    var title by remember(user) { mutableStateOf(user.title) }
    var organization by remember(user) { mutableStateOf(user.organization) }
    var regionalHub by remember(user) { mutableStateOf(user.regionalHub) }
    var ageText by remember(user) { mutableStateOf(user.age.toString()) }
    var careerGoal by remember(user) { mutableStateOf(user.careerGoal) }
    var skillsRaw by remember(user) { mutableStateOf(user.skills.joinToString(", ")) }
    var interestsRaw by remember(user) { mutableStateOf(user.interests.joinToString(", ")) }
    var bio by remember(user) { mutableStateOf(user.bio) }
    var photoUri by remember(user) { mutableStateOf(user.photoUri ?: "") }
    var hubMenuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            photoUri = uri.toString()
        }
    }

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
                    Text(
                        text = "${strings?.editProfile ?: "Edit Profile"} · ${strings?.roleLabel(user.role) ?: user.role.label}",
                        style = MaterialTheme.typography.titleLarge,
                        color = YepSlateDark,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Avatar & Photo Picker Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UserAvatar(
                        user = user.copy(
                            name = name.ifBlank { user.name },
                            photoUri = photoUri.ifBlank { null },
                        ),
                        size = 64.dp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier.testTag("pick_photo_button"),
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings?.choosePhoto ?: "Choose Photo")
                            }
                            TextButton(
                                onClick = { photoUri = "" },
                                modifier = Modifier.testTag("use_initials_button"),
                            ) {
                                Text(strings?.useInitials ?: "Use Initials")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = photoUri,
                    onValueChange = { photoUri = it },
                    label = { Text(strings?.photoUrlOptional ?: "Photo URL (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings?.fullNameLabel ?: "Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings?.emailAddressLabel ?: "Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings?.titleRoleLabel ?: "Title / Role") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                        label = { Text(if (user.role == UserRole.PARTICIPANT) "Age (18–35)" else "Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(105.dp),
                    )
                }
                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text(strings?.organizationLabel ?: "Organization / Cohort Hub") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = hubMenuExpanded,
                    onExpandedChange = { hubMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = regionalHub,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings?.regionalHubLabel ?: "Regional Hub") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hubMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
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
                    value = careerGoal,
                    onValueChange = { careerGoal = it },
                    label = { Text(strings?.careerGoalLabel ?: "Career Goal") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = skillsRaw,
                    onValueChange = { skillsRaw = it },
                    label = { Text(strings?.skillsLabel ?: "Skills (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = interestsRaw,
                    onValueChange = { interestsRaw = it },
                    label = { Text(strings?.interestsLabel ?: "Interests (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(strings?.personalBioLabel ?: "Personal Bio") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                errorMessage?.let { err ->
                    Text(err, color = YepRose, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) { Text(strings?.cancelLabel ?: "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAge = ageText.toIntOrNull() ?: user.age
                            val err = onSave(
                                name,
                                email,
                                title,
                                organization,
                                regionalHub,
                                parsedAge,
                                careerGoal,
                                skillsRaw,
                                interestsRaw,
                                bio,
                                photoUri.ifBlank { null },
                            )
                            if (err != null) errorMessage = err else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YepEmerald),
                        modifier = Modifier.testTag("save_profile_button"),
                    ) {
                        Text(strings?.saveProfile ?: "Save Profile")
                    }
                }
            }
        }
    }
}

@Composable
fun CapstoneSubmitDialog(
    exchange: VirtualExchange,
    isOfflineDataSaver: Boolean,
    strings: YepStrings? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        projectTitle: String,
        architectureDescription: String,
        githubUrl: String,
        demoUrl: String,
        keyTakeaways: String,
    ) -> String?,
) {
    var projectTitle by remember { mutableStateOf("") }
    var architecture by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("https://github.com/") }
    var demoUrl by remember { mutableStateOf("https://") }
    var takeaways by remember { mutableStateOf("") }
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings?.submitCapstone ?: "Submit Capstone", style = MaterialTheme.typography.titleLarge, color = YepSlateDark)
                        Text(exchange.title, style = MaterialTheme.typography.bodySmall, color = YepSlate500)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                if (isOfflineDataSaver) {
                    StatusBadge(
                        text = "Offline Data Saver ON · Queues locally as PENDING SYNC",
                        textColor = YepAmber,
                        bgColor = YepAmberSoft,
                        borderColor = YepAmberBorder,
                    )
                }

                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = { projectTitle = it },
                    label = { Text(strings?.projectTitleLabel ?: "Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("capstone_title_input"),
                )
                OutlinedTextField(
                    value = architecture,
                    onValueChange = { architecture = it },
                    label = { Text(strings?.architectureDescriptionLabel ?: "Architecture Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("capstone_arch_input"),
                )
                OutlinedTextField(
                    value = githubUrl,
                    onValueChange = { githubUrl = it },
                    label = { Text(strings?.githubUrlLabel ?: "GitHub Repository URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = demoUrl,
                    onValueChange = { demoUrl = it },
                    label = { Text(strings?.liveDemoUrlLabel ?: "Live Demo / Video URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = takeaways,
                    onValueChange = { takeaways = it },
                    label = { Text(strings?.keyTakeawaysLabel ?: "Key Takeaways & Resilience Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                error?.let { Text(it, color = YepRose, style = MaterialTheme.typography.bodySmall) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text(strings?.cancelLabel ?: "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val err = onSubmit(projectTitle, architecture, githubUrl, demoUrl, takeaways)
                            if (err != null) error = err else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOfflineDataSaver) YepAmber else YepEmerald,
                        ),
                        modifier = Modifier.testTag("capstone_submit_confirm"),
                    ) {
                        Text(if (isOfflineDataSaver) (strings?.queueOffline ?: "Queue Offline") else (strings?.submitCapstone ?: "Submit Capstone"))
                    }
                }
            }
        }
    }
}

@Composable
fun StudyPackDialog(
    exchange: VirtualExchange,
    enrollment: Enrollment?,
    strings: YepStrings? = null,
    onDismiss: () -> Unit,
    onToggleChecklist: (enrollmentId: String, itemId: String) -> Unit,
) {
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatusBadge(strings?.offlineStudyPack ?: "Offline Study Pack · Cached", YepEmeraldDark, YepEmeraldSoft, YepEmeraldBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(exchange.title, style = MaterialTheme.typography.titleMedium, color = YepSlateDark)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                HorizontalDivider(color = YepSlate200)

                // Lightweight Markdown Reader
                exchange.studyPackMarkdown.lines().forEach { rawLine ->
                    val line = rawLine.trim()
                    when {
                        line.startsWith("# ") -> Text(
                            text = line.removePrefix("# "),
                            style = MaterialTheme.typography.titleMedium,
                            color = YepEmeraldDark,
                            fontWeight = FontWeight.Bold,
                        )
                        line.startsWith("## ") -> Text(
                            text = line.removePrefix("## "),
                            style = MaterialTheme.typography.titleSmall,
                            color = YepSlateDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        line.startsWith("- ") -> Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 4.dp),
                        ) {
                            Text("•", color = YepEmerald, fontWeight = FontWeight.Bold)
                            Text(
                                text = line.removePrefix("- "),
                                style = MaterialTheme.typography.bodySmall,
                                color = YepSlate600,
                            )
                        }
                        line.isNotBlank() -> Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = YepSlate600,
                        )
                    }
                }

                HorizontalDivider(color = YepSlate200)
                Text(
                    text = strings?.interactiveMilestoneChecklist ?: "Interactive Milestone Checklist",
                    style = MaterialTheme.typography.titleSmall,
                    color = YepSlateDark,
                )

                exchange.studyChecklist.forEach { item ->
                    val checked = enrollment?.completedChecklistIds?.contains(item.id) == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = enrollment != null) {
                                enrollment?.let { onToggleChecklist(it.id, item.id) }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            enabled = enrollment != null,
                            onCheckedChange = {
                                enrollment?.let { onToggleChecklist(it.id, item.id) }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = YepEmerald),
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = YepSlateDark,
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = YepSlateDark),
                ) {
                    Text(strings?.doneReading ?: "Done Reading")
                }
            }
        }
    }
}
