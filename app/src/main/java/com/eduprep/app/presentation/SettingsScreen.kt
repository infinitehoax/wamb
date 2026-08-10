package com.eduprep.app.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eduprep.app.BuildConfig
import com.eduprep.app.data.local.ThemePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var showThemeSheet by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        val maxChatHistory by viewModel.maxChatHistoryFlow.collectAsState(initial = 10)
        var dropdownExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Theme Setting Row (At least 56dp tall for touch targets)
            ListItem(
                headlineContent = { Text("App Theme", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(currentTheme) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeSheet = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // AI Tutor Memory Limit Row
            ListItem(
                headlineContent = { Text("AI Tutor Memory Limit", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Limits how many past messages the AI remembers to save data.") },
                trailingContent = {
                    Box {
                        TextButton(onClick = { dropdownExpanded = true }) {
                            Text("$maxChatHistory messages")
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            listOf(5, 10, 20, 50).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("$option messages") },
                                    onClick = {
                                        viewModel.setMaxChatHistory(option)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Clear Chat History Row
            ListItem(
                headlineContent = { Text("Clear Chat History", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Delete all offline conversation records with the AI Tutor") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearChatDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Clear Saved Data Row
            ListItem(
                headlineContent = { Text("Clear Saved Offline Data", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Remove saved bookmarks and cached theory evaluation feedbacks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearDataDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // About Row
            ListItem(
                headlineContent = { Text("EduPrep Offline", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Version ${BuildConfig.VERSION_NAME}\nBuilt for African Students") }
            )
        }
    }

    // App Theme Sheet
    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Theme",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                HorizontalDivider()

                listOf("System", "Light", "Dark").forEach { theme ->
                    val isSelected = theme == currentTheme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {
                                ThemePreferences.setTheme(context, theme)
                                onThemeChanged(theme)
                                showThemeSheet = false
                            }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (theme == "System") "System Default" else theme,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (isSelected) {
                            RadioButton(
                                selected = true,
                                onClick = {
                                    ThemePreferences.setTheme(context, theme)
                                    onThemeChanged(theme)
                                    showThemeSheet = false
                                }
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }

    // Clear Chat Confirmation Dialog
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = { Text("Clear Chat History?") },
            text = { Text("Are you sure you want to delete all offline conversations? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearChatDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Saved Offline Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Saved Offline Data?") },
            text = { Text("Are you sure you want to clear your saved bookmarks and evaluation feedbacks? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSavedData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
