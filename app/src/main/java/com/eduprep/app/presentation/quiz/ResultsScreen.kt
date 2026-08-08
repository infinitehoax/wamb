package com.eduprep.app.presentation.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: QuizViewModel,
    onRetake: () -> Unit,
    onHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val score = viewModel.calculateScore()
    val total = uiState.questions.size
    val percentage = if (total > 0) (score * 100) / total else 0

    val (feedbackText, feedbackColor) = when {
        percentage >= 75 -> "Outstanding! You are fully prepared." to MaterialTheme.colorScheme.primary
        percentage >= 50 -> "Good effort! Keep practicing to secure your score." to MaterialTheme.colorScheme.secondary
        else -> "Don't give up! Review the explanations below and try again." to MaterialTheme.colorScheme.error
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Exam Results", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(
                        onClick = onHome,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Summary Header Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Progress Gauge / Circular score
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(feedbackColor.copy(alpha = 0.1f))
                        ) {
                            CircularProgressIndicator(
                                progress = { percentage / 100f },
                                strokeWidth = 8.dp,
                                color = feedbackColor,
                                trackColor = feedbackColor.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = feedbackColor
                                    )
                                )
                                Text(
                                    text = "$score / $total",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Text(
                            text = feedbackText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = feedbackColor
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Retake Button
                            Button(
                                onClick = onRetake,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retake"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Retake",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Go Home Button
                            OutlinedButton(
                                onClick = onHome,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Practice Hub",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Explanations Header
            item {
                Text(
                    text = "Detailed Explanations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // List of questions and user answers
            itemsIndexed(uiState.questions) { index, question ->
                val pickedAnswer = uiState.selectedAnswers[question.id]
                val isCorrect = pickedAnswer == question.answer

                val itemColor = if (isCorrect) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp,
                    border = BorderStroke(1.dp, itemColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "QUESTION ${index + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor
                                )
                            )

                            Surface(
                                color = itemColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isCorrect) "CORRECT" else "INCORRECT",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = itemColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Question Text
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                lineHeight = 24.sp
                            )
                        )

                        HorizontalDivider()

                        // Answers row
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val pickedText = when (pickedAnswer) {
                                "A" -> question.optA
                                "B" -> question.optB
                                "C" -> question.optC
                                "D" -> question.optD
                                "E" -> question.optE ?: ""
                                else -> "Not attempted"
                            }
                            val correctText = when (question.answer) {
                                "A" -> question.optA
                                "B" -> question.optB
                                "C" -> question.optC
                                "D" -> question.optD
                                "E" -> question.optE ?: ""
                                else -> ""
                            }

                            Text(
                                text = "Your Pick: " + (pickedAnswer ?: "None") + " - " + pickedText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor
                                )
                            )

                            if (!isCorrect) {
                                Text(
                                    text = "Correct: " + question.answer + " - " + correctText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Explanation Card
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Explanation:",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = question.explanation.ifBlank { "No explanation available for this question." },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
