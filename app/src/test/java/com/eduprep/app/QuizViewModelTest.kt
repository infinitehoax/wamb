package com.eduprep.app

import androidx.lifecycle.SavedStateHandle
import com.eduprep.app.domain.model.Question
import com.eduprep.app.domain.repository.QuizRepository
import com.eduprep.app.presentation.quiz.QuizMode
import com.eduprep.app.presentation.quiz.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadQuestions updates state with retrieved questions`() = runTest(testDispatcher) {
        val sampleQuestions = listOf(
            Question(1, "Math", "Algebra", "2022", "Q1", "A", "B", "C", "D", "A", "Exp1", false),
            Question(2, "Math", "Algebra", "2022", "Q2", "A", "B", "C", "D", "B", "Exp2", false)
        )
        fakeRepository.questionsList = sampleQuestions

        val savedStateHandle = SavedStateHandle(mapOf(
            "subject" to "Math",
            "year" to "2022",
            "topic" to "Algebra",
            "mode" to "PRACTICE"
        ))

        val viewModel = QuizViewModel(fakeRepository, savedStateHandle)

        // Run pending tasks at current time to load questions without letting timer expire
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.questions.size)
        assertEquals("Q1", uiState.questions[0].text)
        assertEquals(QuizMode.PRACTICE, uiState.mode)
    }

    @Test
    fun `selectOption in Practice mode does not lock selection instantly`() = runTest(testDispatcher) {
        val sampleQuestions = listOf(
            Question(1, "Math", "Algebra", "2022", "Q1", "A", "B", "C", "D", "A", "Exp1", false)
        )
        fakeRepository.questionsList = sampleQuestions

        val savedStateHandle = SavedStateHandle(mapOf(
            "mode" to "PRACTICE"
        ))

        val viewModel = QuizViewModel(fakeRepository, savedStateHandle)
        runCurrent()

        viewModel.selectOption("A")
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals("A", uiState.selectedAnswers[1])
        assertFalse(uiState.showExplanation)
        assertFalse(uiState.checkedAnswers.contains(1))
    }

    @Test
    fun `selectOption in Study mode locks selection and shows explanation`() = runTest(testDispatcher) {
        val sampleQuestions = listOf(
            Question(1, "Math", "Algebra", "2022", "Q1", "A", "B", "C", "D", "A", "Exp1", false)
        )
        fakeRepository.questionsList = sampleQuestions

        val savedStateHandle = SavedStateHandle(mapOf(
            "mode" to "STUDY"
        ))

        val viewModel = QuizViewModel(fakeRepository, savedStateHandle)
        runCurrent()

        viewModel.selectOption("A")
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals("A", uiState.selectedAnswers[1])
        assertTrue(uiState.showExplanation)
        assertTrue(uiState.checkedAnswers.contains(1))

        // Subsequent option selection should be ignored in study mode once locked
        viewModel.selectOption("B")
        runCurrent()
        assertEquals("A", viewModel.uiState.value.selectedAnswers[1])
    }

    @Test
    fun `calculateScore returns correct number of correctly answered questions`() = runTest(testDispatcher) {
        val sampleQuestions = listOf(
            Question(1, "Math", "Algebra", "2022", "Q1", "A", "B", "C", "D", "A", "Exp1", false),
            Question(2, "Math", "Algebra", "2022", "Q2", "A", "B", "C", "D", "B", "Exp2", false),
            Question(3, "Math", "Algebra", "2022", "Q3", "A", "B", "C", "D", "C", "Exp3", false)
        )
        fakeRepository.questionsList = sampleQuestions

        val savedStateHandle = SavedStateHandle(mapOf(
            "mode" to "PRACTICE"
        ))

        val viewModel = QuizViewModel(fakeRepository, savedStateHandle)
        runCurrent()

        viewModel.selectOption("A") // Q1: Correct
        viewModel.nextQuestion()
        runCurrent()

        viewModel.selectOption("C") // Q2: Wrong (Correct is B)
        viewModel.nextQuestion()
        runCurrent()

        viewModel.selectOption("C") // Q3: Correct
        runCurrent()

        val score = viewModel.calculateScore()
        assertEquals(2, score)
    }

    @Test
    fun `toggleBookmark saves and deletes bookmark properly`() = runTest(testDispatcher) {
        val sampleQuestions = listOf(
            Question(1, "Math", "Algebra", "2022", "Q1", "A", "B", "C", "D", "A", "Exp1", false)
        )
        fakeRepository.questionsList = sampleQuestions

        val savedStateHandle = SavedStateHandle()
        val viewModel = QuizViewModel(fakeRepository, savedStateHandle)
        runCurrent()

        assertFalse(viewModel.uiState.value.isCurrentBookmarked)

        viewModel.toggleBookmark()
        runCurrent()
        assertTrue(viewModel.uiState.value.isCurrentBookmarked)
        assertTrue(fakeRepository.isBookmarked(1))

        viewModel.toggleBookmark()
        runCurrent()
        assertFalse(viewModel.uiState.value.isCurrentBookmarked)
        assertFalse(fakeRepository.isBookmarked(1))
    }
}

class FakeQuizRepository : QuizRepository {
    var questionsList = emptyList<Question>()
    private val bookmarkedIds = mutableSetOf<Long>()

    override suspend fun getDistinctSubjects(): List<String> = emptyList()
    override suspend fun getDistinctTopics(subject: String): List<String> = emptyList()
    override suspend fun getDistinctYears(subject: String): List<String> = emptyList()

    override suspend fun getRandomQuestions(
        subject: String,
        year: String,
        topic: String,
        limit: Int
    ): List<Question> {
        return questionsList.take(limit)
    }

    override suspend fun insertQuestions(questions: List<Question>) {
        questionsList = questionsList + questions
    }

    override suspend fun insertBookmark(questionId: Long) {
        bookmarkedIds.add(questionId)
    }

    override suspend fun deleteBookmark(questionId: Long) {
        bookmarkedIds.remove(questionId)
    }

    override suspend fun isBookmarked(questionId: Long): Boolean {
        return bookmarkedIds.contains(questionId)
    }

    override suspend fun getBookmarkedQuestions(): List<Question> {
        return questionsList.filter { bookmarkedIds.contains(it.id) }
    }
}
