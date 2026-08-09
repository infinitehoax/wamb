package com.eduprep.app.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.data.local.TheoryFeedbackDao
import com.eduprep.app.data.local.TheoryFeedbackEntity
import com.eduprep.app.domain.model.Question
import com.eduprep.app.domain.repository.BackendRepository
import com.eduprep.app.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TheoryUiState(
    val question: Question? = null,
    val studentAnswer: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val score: Int? = null,
    val feedback: String? = null,
    val missingKeywords: List<String> = emptyList(),
    val isEvaluatedOffline: Boolean = false,
    val isSubmitted: Boolean = false
)

@HiltViewModel
class TheoryViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val backendRepository: BackendRepository,
    private val theoryFeedbackDao: TheoryFeedbackDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TheoryUiState())
    val uiState: StateFlow<TheoryUiState> = _uiState.asStateFlow()

    private val questionIdString: String = savedStateHandle["questionId"] ?: "0"
    val questionId: Long = questionIdString.toLongOrNull() ?: 0L

    init {
        loadQuestionAndCache()
    }

    private fun loadQuestionAndCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val q = quizRepository.getQuestionById(questionId)
                if (q == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Question not found.") }
                    return@launch
                }

                // Check Room first for cached feedback
                val cached = theoryFeedbackDao.getFeedbackForQuestion(questionId)
                if (cached != null) {
                    val keywordsList = cached.missingKeywords.split(",").filter { it.isNotEmpty() }
                    _uiState.update { state ->
                        state.copy(
                            question = q,
                            score = cached.score,
                            feedback = cached.feedback,
                            missingKeywords = keywordsList,
                            isSubmitted = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            question = q,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load question.") }
            }
        }
    }

    fun updateStudentAnswer(answer: String) {
        if (_uiState.value.isSubmitted) return
        _uiState.update { it.copy(studentAnswer = answer, errorMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submitEssay() {
        val state = _uiState.value
        val q = state.question ?: return
        val answerText = state.studentAnswer.trim()

        if (answerText.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your answer before submitting.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Check if exactMathAnswer is set
            if (q.exactMathAnswer != null) {
                val isCorrect = MathGraderUtil.compareExactMath(answerText, q.exactMathAnswer)
                val finalScore = if (isCorrect) 10 else 0
                val finalFeedback = if (isCorrect) {
                    "Excellent! Your answer of **${answerText}** matches the expected mathematical answer **${q.exactMathAnswer}** correctly."
                } else {
                    "Incorrect. Your answer of **${answerText}** does not match the expected answer. The correct answer is **${q.exactMathAnswer}**."
                }

                _uiState.update { state ->
                    state.copy(
                        score = finalScore,
                        feedback = finalFeedback,
                        missingKeywords = emptyList(),
                        isEvaluatedOffline = true,
                        isSubmitted = true,
                        isLoading = false
                    )
                }
            } else {
                // 2. Hybrid grading using PythonAnywhere Flask Backend API
                val result = backendRepository.submitEssayForGrading(
                    question = q.text,
                    markingGuide = q.explanation,
                    studentAnswer = answerText
                )

                result.onSuccess { response ->
                    // Save to local cache
                    val keywordsString = response.missingKeywords.joinToString(",")
                    theoryFeedbackDao.insertFeedback(
                        TheoryFeedbackEntity(
                            questionId = questionId,
                            score = response.score,
                            feedback = response.feedback,
                            missingKeywords = keywordsString
                        )
                    )

                    _uiState.update { state ->
                        state.copy(
                            score = response.score,
                            feedback = response.feedback,
                            missingKeywords = response.missingKeywords,
                            isEvaluatedOffline = false,
                            isSubmitted = true,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = error.message ?: "AI Engine Failed. Please check your internet connection.",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun reviseEssay() {
        viewModelScope.launch {
            // Delete cached feedback from room so user can submit again
            theoryFeedbackDao.deleteFeedbackForQuestion(questionId)
            _uiState.update { state ->
                state.copy(
                    isSubmitted = false,
                    score = null,
                    feedback = null,
                    missingKeywords = emptyList(),
                    isEvaluatedOffline = false,
                    errorMessage = null
                )
            }
        }
    }
}
