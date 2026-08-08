package com.eduprep.app.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.domain.model.Question
import com.eduprep.app.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Long, String> = emptyMap(),
    val isCurrentBookmarked: Boolean = false,
    val remainingTimeSeconds: Long = 0,
    val isTimeUp: Boolean = false,
    val isSubmitted: Boolean = false,
    val mode: QuizMode = QuizMode.PRACTICE,
    val showExplanation: Boolean = false,
    val checkedAnswers: Set<Long> = emptySet(), // Locked answers in Study mode
    val isLoading: Boolean = false
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // Read navigation arguments
    private val subject: String = savedStateHandle["subject"] ?: "All"
    private val year: String = savedStateHandle["year"] ?: "All"
    private val topic: String = savedStateHandle["topic"] ?: "All"
    private val modeString: String = savedStateHandle["mode"] ?: "PRACTICE"

    init {
        val parsedMode = try {
            QuizMode.valueOf(modeString)
        } catch (e: Exception) {
            QuizMode.PRACTICE
        }
        _uiState.update { it.copy(mode = parsedMode) }
        loadQuestions(parsedMode)
    }

    private fun loadQuestions(quizMode: QuizMode) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch up to 10 questions for practice/mock
                val questions = quizRepository.getRandomQuestions(subject, year, topic, 10)
                _uiState.update { state ->
                    state.copy(
                        questions = questions,
                        isLoading = false
                    )
                }
                updateBookmarkStatus()
                startTimer(quizMode)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startTimer(quizMode: QuizMode) {
        timerJob?.cancel()
        if (quizMode == QuizMode.STUDY) {
            _uiState.update { it.copy(remainingTimeSeconds = 0) }
            return
        }

        val totalTime = when (quizMode) {
            QuizMode.MOCK -> 30 * 60L // 30 mins
            QuizMode.PRACTICE -> 15 * 60L // 15 mins
            else -> 0L
        }

        _uiState.update { it.copy(remainingTimeSeconds = totalTime) }

        timerJob = viewModelScope.launch {
            var time = totalTime
            while (time > 0) {
                delay(1000)
                time--
                _uiState.update { it.copy(remainingTimeSeconds = time) }
            }
            _uiState.update { it.copy(isTimeUp = true) }
            submitQuiz()
        }
    }

    fun selectOption(option: String) {
        val state = _uiState.value
        val questions = state.questions
        if (questions.isEmpty() || state.isSubmitted) return

        val currentQuestion = questions[state.currentQuestionIndex]
        if (state.mode == QuizMode.STUDY && state.checkedAnswers.contains(currentQuestion.id)) {
            // Already checked in Study mode, cannot re-select
            return
        }

        val updatedAnswers = state.selectedAnswers.toMutableMap()
        updatedAnswers[currentQuestion.id] = option

        _uiState.update { it.copy(selectedAnswers = updatedAnswers) }

        if (state.mode == QuizMode.STUDY) {
            // Instantly show answer explanation and lock it
            val updatedChecked = state.checkedAnswers.toMutableSet()
            updatedChecked.add(currentQuestion.id)
            _uiState.update {
                it.copy(
                    checkedAnswers = updatedChecked,
                    showExplanation = true
                )
            }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentQuestionIndex < state.questions.size - 1) {
            val nextIndex = state.currentQuestionIndex + 1
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    showExplanation = state.mode == QuizMode.STUDY && state.checkedAnswers.contains(state.questions[nextIndex].id)
                )
            }
            updateBookmarkStatus()
        }
    }

    fun previousQuestion() {
        val state = _uiState.value
        if (state.currentQuestionIndex > 0) {
            val prevIndex = state.currentQuestionIndex - 1
            _uiState.update {
                it.copy(
                    currentQuestionIndex = prevIndex,
                    showExplanation = state.mode == QuizMode.STUDY && state.checkedAnswers.contains(state.questions[prevIndex].id)
                )
            }
            updateBookmarkStatus()
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value
        if (state.questions.isEmpty()) return
        val currentQuestion = state.questions[state.currentQuestionIndex]

        viewModelScope.launch {
            if (state.isCurrentBookmarked) {
                quizRepository.deleteBookmark(currentQuestion.id)
            } else {
                quizRepository.insertBookmark(currentQuestion.id)
            }
            updateBookmarkStatus()
        }
    }

    private fun updateBookmarkStatus() {
        val state = _uiState.value
        if (state.questions.isEmpty()) return
        val currentQuestion = state.questions[state.currentQuestionIndex]

        viewModelScope.launch {
            val isBookmarked = quizRepository.isBookmarked(currentQuestion.id)
            _uiState.update { it.copy(isCurrentBookmarked = isBookmarked) }
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        _uiState.update { it.copy(isSubmitted = true) }
    }

    fun calculateScore(): Int {
        val state = _uiState.value
        var correctCount = 0
        state.questions.forEach { q ->
            val picked = state.selectedAnswers[q.id]
            if (picked != null && picked == q.answer) {
                correctCount++
            }
        }
        return correctCount
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
