package com.eduprep.app.presentation.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eduprep.app.domain.model.Question
import com.eduprep.app.presentation.components.ProEmptyState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    onBack: () -> Unit,
    onSubmitSuccess: (score: Int, total: Int, mode: String) -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle timer expiration or manual submission -> navigate to results
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            val score = viewModel.calculateScore()
            val total = uiState.questions.size
            onSubmitSuccess(score, total, uiState.mode.name)
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (uiState.questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProEmptyState(
                icon = "📚",
                title = "No Questions Found",
                body = "We couldn't find any questions matching your selection. Please adjust your topic, year, or subject filters and try again.",
                ctaText = "Go Back",
                onCtaClick = onBack
            )
        }
        return
    }

    val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
    val selectedOption = uiState.selectedAnswers[currentQuestion.id]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Q. ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Setup"
                        )
                    }
                },
                actions = {
                    // Countdown Timer (if not Study mode)
                    if (uiState.mode != QuizMode.STUDY) {
                        val minutes = uiState.remainingTimeSeconds / 60
                        val seconds = uiState.remainingTimeSeconds % 60
                        val timerColor = if (uiState.remainingTimeSeconds < 60) {
                            Color.Red
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = timerColor
                            ),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }

                    // Bookmark Star
                    IconButton(
                        onClick = { viewModel.toggleBookmark() },
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isCurrentBookmarked) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.Star
                            },
                            contentDescription = "Bookmark",
                            tint = if (uiState.isCurrentBookmarked) {
                                Color(0xFFFFD700) // Gold
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    OutlinedButton(
                        onClick = { viewModel.previousQuestion() },
                        enabled = uiState.currentQuestionIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Previous",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Next or Submit Button
                    val isLastQuestion = uiState.currentQuestionIndex == uiState.questions.size - 1
                    Button(
                        onClick = {
                            if (isLastQuestion) {
                                viewModel.submitQuiz()
                            } else {
                                viewModel.nextQuestion()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastQuestion) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isLastQuestion) "Submit" else "Next",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exam Mode Indicator Tag
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${uiState.mode.name} MODE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Question text
            Text(
                text = currentQuestion.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Options Area
            val options = remember(currentQuestion) {
                buildList {
                    add("A" to currentQuestion.optA)
                    add("B" to currentQuestion.optB)
                    add("C" to currentQuestion.optC)
                    add("D" to currentQuestion.optD)
                    currentQuestion.optE?.let { add("E" to it) }
                }
            }

            options.forEach { (letter, optText) ->
                val isSelected = selectedOption == letter
                val isCorrectAnswer = letter == currentQuestion.answer

                val backgroundColor = when {
                    uiState.mode == QuizMode.STUDY && uiState.showExplanation -> {
                        when {
                            isCorrectAnswer -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            isSelected -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                    }
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }

                val strokeColor = when {
                    uiState.mode == QuizMode.STUDY && uiState.showExplanation -> {
                        when {
                            isCorrectAnswer -> MaterialTheme.colorScheme.primary
                            isSelected -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        }
                    }
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }

                Surface(
                    onClick = { viewModel.selectOption(letter) },
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp),
                    border = if (strokeColor != Color.Transparent) BorderStroke(2.dp, strokeColor) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 56.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Styled option letter circle
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Study mode explanation card
            if (uiState.mode == QuizMode.STUDY && uiState.showExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
                val isUserCorrect = selectedOption == currentQuestion.answer
                Surface(
                    color = if (isUserCorrect) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isUserCorrect) "🎉 Correct Answer!" else "❌ Incorrect Answer",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUserCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        )

                        Text(
                            text = "Correct Option: ${currentQuestion.answer}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        HorizontalDivider()

                        Text(
                            text = currentQuestion.explanation.ifBlank { "No explanation provided." },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
