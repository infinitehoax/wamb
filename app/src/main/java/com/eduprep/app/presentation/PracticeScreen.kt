package com.eduprep.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eduprep.app.presentation.quiz.QuizMode
import com.eduprep.app.presentation.quiz.QuizSetupViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    onStartQuiz: (subject: String, year: String, topic: String, mode: String, limit: Int, duration: Int, shuffleQ: Boolean, shuffleOpt: Boolean) -> Unit,
    onStartTheory: (questionId: Long) -> Unit,
    viewModel: QuizSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showSetupSheet by remember { mutableStateOf(false) }
    var setupIsTheory by remember { mutableStateOf(false) }

    var showSubjectSheet by remember { mutableStateOf(false) }
    var showYearSheet by remember { mutableStateOf(false) }
    var showTopicSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Prepare to Excel",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Practice custom WAEC & JAMB questions offline. Select your setup options below to begin.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                )
            }
        }

        Text(
            text = "Select Practice Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Configure Objective Test Card
        Card(
            onClick = {
                setupIsTheory = false
                showSetupSheet = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Configure Objective Test 🎯",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Practice custom WAEC & JAMB multiple choice questions offline. Custom question count, shuffle, and mode configurations available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Theory & Essay Practice Card
        Card(
            onClick = {
                setupIsTheory = true
                showSetupSheet = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Theory & Essay Practice 📝",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Detailed essay questions graded by AI or local exact numerical matching. Perfect for standard WAEC/JAMB theory practice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Single setup sheet for both objective & theory
    if (showSetupSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSetupSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (setupIsTheory) "Theory Setup" else "Objective Setup",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                HorizontalDivider()

                // Subject Selector
                SelectorRow(
                    label = "Subject",
                    selectedValue = uiState.selectedSubject,
                    onClick = { showSubjectSheet = true }
                )

                // Year Selector
                SelectorRow(
                    label = "Year",
                    selectedValue = uiState.selectedYear,
                    onClick = { showYearSheet = true }
                )

                // Topic Selector
                SelectorRow(
                    label = "Topic",
                    selectedValue = uiState.selectedTopic,
                    onClick = { showTopicSheet = true }
                )

                // Number of Questions (Both modes)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Number of Questions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.numberOfQuestions}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.numberOfQuestions.toFloat(),
                        onValueChange = { viewModel.selectNumberOfQuestions(it.toInt()) },
                        valueRange = 5f..50f,
                        steps = 8 // yields 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
                    )
                }

                // Time Limit (Both modes, but hidden if Study Mode is selected)
                if (setupIsTheory || uiState.selectedMode != QuizMode.STUDY) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Time Limit",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.durationMinutes} minutes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = uiState.durationMinutes.toFloat(),
                            onValueChange = { viewModel.selectDurationMinutes(it.toInt()) },
                            valueRange = 10f..120f,
                            steps = 10 // yields 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120
                        )
                    }
                }

                // If Objective is selected: Mode selector & Shuffles
                if (!setupIsTheory) {
                    Text(
                        text = "Select Exam Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModeCard(
                            title = "Study",
                            desc = "No timer. Explanations instantly.",
                            selected = uiState.selectedMode == QuizMode.STUDY,
                            onClick = { viewModel.selectMode(QuizMode.STUDY) },
                            modifier = Modifier.weight(1f)
                        )
                        ModeCard(
                            title = "Practice",
                            desc = "Timer active. Answers at end.",
                            selected = uiState.selectedMode == QuizMode.PRACTICE,
                            onClick = { viewModel.selectMode(QuizMode.PRACTICE) },
                            modifier = Modifier.weight(1f)
                        )
                        ModeCard(
                            title = "Mock",
                            desc = "Strict exam conditions.",
                            selected = uiState.selectedMode == QuizMode.MOCK,
                            onClick = { viewModel.selectMode(QuizMode.MOCK) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Switch Rows for Shuffle Questions & Options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { viewModel.toggleShuffleQuestions(!uiState.shuffleQuestions) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shuffle Questions", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = uiState.shuffleQuestions,
                            onCheckedChange = { viewModel.toggleShuffleQuestions(it) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { viewModel.toggleShuffleOptions(!uiState.shuffleOptions) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shuffle Options", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = uiState.shuffleOptions,
                            onCheckedChange = { viewModel.toggleShuffleOptions(it) }
                        )
                    }
                } else {
                    // If Theory is selected: Banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Theory answers will be graded by AI or local numerical matching.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary Start Button (Height 56dp for standard 48dp+ touch target rules)
                Button(
                    onClick = {
                        if (!setupIsTheory) {
                            showSetupSheet = false
                            onStartQuiz(
                                uiState.selectedSubject,
                                uiState.selectedYear,
                                uiState.selectedTopic,
                                uiState.selectedMode.name,
                                uiState.numberOfQuestions,
                                uiState.durationMinutes,
                                uiState.shuffleQuestions,
                                uiState.shuffleOptions
                            )
                        } else {
                            // Find matching theory questions
                            val matching = viewModel.getMatchingTheoryQuestions(
                                uiState.selectedSubject,
                                uiState.selectedYear,
                                uiState.selectedTopic,
                                uiState.numberOfQuestions
                            )
                            if (matching.isEmpty()) {
                                Toast.makeText(context, "No matching theory questions found. Try another topic or subject.", Toast.LENGTH_LONG).show()
                            } else {
                                showSetupSheet = false
                                onStartTheory(matching.first().id)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (setupIsTheory) "Start Theory Practice" else "Start Objective Exam",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }

    // Modal Bottom Sheets for Selections (displayed over the main setup bottom sheet beautifully)
    if (showSubjectSheet) {
        SelectionBottomSheet(
            title = "Select Subject",
            items = uiState.subjects,
            selectedItem = uiState.selectedSubject,
            onDismiss = { showSubjectSheet = false },
            onItemSelected = {
                viewModel.selectSubject(it)
                showSubjectSheet = false
            }
        )
    }

    if (showYearSheet) {
        SelectionBottomSheet(
            title = "Select Year",
            items = uiState.years,
            selectedItem = uiState.selectedYear,
            onDismiss = { showYearSheet = false },
            onItemSelected = {
                viewModel.selectYear(it)
                showYearSheet = false
            }
        )
    }

    if (showTopicSheet) {
        SelectionBottomSheet(
            title = "Select Topic",
            items = uiState.topics,
            selectedItem = uiState.selectedTopic,
            onDismiss = { showTopicSheet = false },
            onItemSelected = {
                viewModel.selectTopic(it)
                showTopicSheet = false
            }
        )
    }
}

@Composable
fun SelectorRow(
    label: String,
    selectedValue: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand Selection"
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBottomSheet(
    title: String,
    items: List<String>,
    selectedItem: String,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { onItemSelected(item) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (isSelected) {
                            RadioButton(
                                selected = true,
                                onClick = { onItemSelected(item) }
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}
