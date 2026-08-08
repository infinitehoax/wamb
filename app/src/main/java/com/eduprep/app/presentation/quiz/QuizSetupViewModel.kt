package com.eduprep.app.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.domain.model.Question
import com.eduprep.app.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizSetupUiState(
    val subjects: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val years: List<String> = emptyList(),
    val selectedSubject: String = "All",
    val selectedTopic: String = "All",
    val selectedYear: String = "All",
    val selectedMode: QuizMode = QuizMode.PRACTICE,
    val isLoading: Boolean = false
)

@HiltViewModel
class QuizSetupViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizSetupUiState())
    val uiState: StateFlow<QuizSetupUiState> = _uiState.asStateFlow()

    init {
        seedAndLoad()
    }

    private fun seedAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dbSubjects = quizRepository.getDistinctSubjects()
                if (dbSubjects.isEmpty()) {
                    seedSampleQuestions()
                }
                loadSubjectsInternal()
            } catch (e: Exception) {
                loadSubjectsInternal()
            }
        }
    }

    private suspend fun seedSampleQuestions() {
        val sampleQuestions = listOf(
            Question(
                id = 1,
                subject = "Mathematics",
                topic = "Algebra",
                year = "2022",
                text = "Solve for x in the equation: 3x - 5 = 10.",
                optA = "x = 3",
                optB = "x = 5",
                optC = "x = 7",
                optD = "x = 15",
                optE = "x = 20",
                answer = "B",
                explanation = "Adding 5 to both sides of the equation: 3x = 15. Dividing both sides by 3: x = 5. Therefore, option B is correct.",
                isTheory = false
            ),
            Question(
                id = 2,
                subject = "Mathematics",
                topic = "Trigonometry",
                year = "2021",
                text = "What is the value of sin(30) + cos(60)?",
                optA = "0.5",
                optB = "1.0",
                optC = "1.5",
                optD = "2.0",
                answer = "B",
                explanation = "Since sin(30) = 0.5 and cos(60) = 0.5, we have sin(30) + cos(60) = 0.5 + 0.5 = 1.0. Thus, option B is correct.",
                isTheory = false
            ),
            Question(
                id = 3,
                subject = "English Language",
                topic = "Lexis and Structure",
                year = "2022",
                text = "Choose the word that is most nearly opposite in meaning to the underlined word: 'The manager's decision was quite arbitrary.'",
                optA = "Reasonable",
                optB = "Dictatorial",
                optC = "Unplanned",
                optD = "Subjective",
                answer = "A",
                explanation = "'Arbitrary' means based on random choice or personal whim rather than reason. The opposite of arbitrary is 'Reasonable' or systematic. Thus, option A is correct.",
                isTheory = false
            ),
            Question(
                id = 4,
                subject = "English Language",
                topic = "Comprehension",
                year = "2020",
                text = "Identify the grammatical name given to the underlined phrase: 'The boy who stole the watch was caught.'",
                optA = "Adverbial Clause",
                optB = "Adjectival/Relative Clause",
                optC = "Noun Clause",
                optD = "Prepositional Phrase",
                answer = "B",
                explanation = "The phrase 'who stole the watch' qualifies the noun 'The boy', so it serves as an Adjectival or Relative Clause. Thus, option B is correct.",
                isTheory = false
            ),
            Question(
                id = 5,
                subject = "Physics",
                topic = "Mechanics",
                year = "2021",
                text = "A car accelerates uniformly from rest at 2 m/s² for 5 seconds. What is its final velocity?",
                optA = "5 m/s",
                optB = "10 m/s",
                optC = "15 m/s",
                optD = "25 m/s",
                answer = "B",
                explanation = "Using the equation of motion v = u + at, where initial velocity u = 0, acceleration a = 2, and time t = 5: v = 0 + (2 * 5) = 10 m/s. Option B is correct.",
                isTheory = false
            ),
            Question(
                id = 6,
                subject = "Physics",
                topic = "Optics",
                year = "2020",
                text = "What is the refractive index of a medium if the speed of light in that medium is 2.0 x 10⁸ m/s? (Speed of light in vacuum = 3.0 x 10⁸ m/s)",
                optA = "1.33",
                optB = "1.50",
                optC = "1.66",
                optD = "2.00",
                answer = "B",
                explanation = "Refractive index (n) is defined as the speed of light in vacuum (c) divided by the speed of light in the medium (v): n = c / v = (3.0 x 10⁸) / (2.0 x 10⁸) = 1.5. Thus, option B is correct.",
                isTheory = false
            ),
            Question(
                id = 7,
                subject = "Biology",
                topic = "Genetics",
                year = "2022",
                text = "Which of the following is considered the hereditary material in living organisms?",
                optA = "RNA",
                optB = "DNA",
                optC = "Ribosomes",
                optD = "Proteins",
                answer = "B",
                explanation = "DNA (Deoxyribonucleic acid) is the genetic and hereditary material present in all cellular organisms. Option B is correct.",
                isTheory = false
            ),
            Question(
                id = 8,
                subject = "Biology",
                topic = "Ecology",
                year = "2021",
                text = "What name is given to the association between a fungus and an alga in Lichen?",
                optA = "Parasitism",
                optB = "Mutualism",
                optC = "Commensalism",
                optD = "Saprophytism",
                answer = "B",
                explanation = "The relationship between a fungus and an alga in Lichen is symbiotic and mutually beneficial, known as Mutualism. Thus, option B is correct.",
                isTheory = false
            ),
            Question(
                id = 9,
                subject = "Mathematics",
                topic = "Probability",
                year = "2020",
                text = "A fair die is rolled once. What is the probability of obtaining a prime number?",
                optA = "1/6",
                optB = "1/3",
                optC = "1/2",
                optD = "2/3",
                answer = "C",
                explanation = "The prime numbers on a die are 2, 3, and 5 (3 outcomes). Total outcomes = 6. Probability = 3 / 6 = 1/2. Option C is correct.",
                isTheory = false
            ),
            Question(
                id = 10,
                subject = "Physics",
                topic = "Electricity",
                year = "2022",
                text = "Two resistors of resistances 4 ohms and 6 ohms are connected in parallel. What is their effective resistance?",
                optA = "2.4 ohms",
                optB = "5.0 ohms",
                optC = "10.0 ohms",
                optD = "24.0 ohms",
                answer = "A",
                explanation = "For resistors in parallel: 1/R = 1/R1 + 1/R2 = 1/4 + 1/6 = 5/12. Therefore, R = 12/5 = 2.4 ohms. Option A is correct.",
                isTheory = false
            )
        )
        quizRepository.insertQuestions(sampleQuestions)
    }

    private fun loadSubjectsInternal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dbSubjects = quizRepository.getDistinctSubjects()
                val finalSubjects = listOf("All") + dbSubjects
                _uiState.update { state ->
                    state.copy(
                        subjects = finalSubjects,
                        selectedSubject = if (state.selectedSubject in finalSubjects) state.selectedSubject else "All",
                        isLoading = false
                    )
                }
                loadTopicsAndYears()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadTopicsAndYears() {
        val subject = _uiState.value.selectedSubject
        viewModelScope.launch {
            try {
                val dbTopics = quizRepository.getDistinctTopics(subject)
                val finalTopics = listOf("All") + dbTopics

                val dbYears = quizRepository.getDistinctYears(subject)
                val finalYears = listOf("All") + dbYears

                _uiState.update { state ->
                    state.copy(
                        topics = finalTopics,
                        years = finalYears,
                        selectedTopic = if (state.selectedTopic in finalTopics) state.selectedTopic else "All",
                        selectedYear = if (state.selectedYear in finalYears) state.selectedYear else "All"
                    )
                }
            } catch (e: Exception) {
                // handle silently
            }
        }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
        loadTopicsAndYears()
    }

    fun selectTopic(topic: String) {
        _uiState.update { it.copy(selectedTopic = topic) }
    }

    fun selectYear(year: String) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun selectMode(mode: QuizMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }
}
