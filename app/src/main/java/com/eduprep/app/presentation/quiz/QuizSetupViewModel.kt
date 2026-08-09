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
                text = "Solve for **x** in the quadratic equation where the coefficient of the quadratic term is positive:\n\n\$\$3x^2 - 5x - 2 = 0\$\$\n\nYou can use the general quadratic formula:\n\n\$\$x = \\\\frac{-b \\\\pm \\\\sqrt{b^2 - 4ac}}{2a}\$\$",
                optA = "\\( x = -\\frac{1}{3} \\) or \\( x = 2 \\)",
                optB = "\\( x = \\frac{1}{3} \\) or \\( x = -2 \\)",
                optC = "\\( x = -\\frac{1}{3} \\) or \\( x = -2 \\)",
                optD = "![graph](test_graph.png) None of the above",
                optE = null,
                answer = "A",
                explanation = "Comparing \$\$3x^2 - 5x - 2 = 0\$\$ to \$\$ax^2 + bx + c = 0\$\$, we have:\n**_a_** = 3, **_b_** = -5, and **_c_** = -2.\n\nSubstitute these into the quadratic formula:\n\n\$\$x = \\\\frac{-(-5) \\\\pm \\\\sqrt{(-5)^2 - 4(3)(-2)}}{2(3)}\$\$\n\n\$\$x = \\\\frac{5 \\\\pm \\\\sqrt{25 + 24}}{6}\$\$\n\n\$\$x = \\\\frac{5 \\\\pm \\\\sqrt{49}}{6}\$\$\n\n\$\$x = \\\\frac{5 \\\\pm 7}{6}\$\$\n\nThus:\n\$\$x = \\\\frac{12}{6} = 2 \\\\quad \\\\text{or} \\\\quad x = \\\\frac{-2}{6} = -\\\\frac{1}{3}\$\$\n\nTherefore, **x = -1/3 or x = 2**. Option **A** is correct.",
                isTheory = false
            ),
            Question(
                id = 2,
                subject = "Mathematics",
                topic = "Trigonometry",
                year = "2021",
                text = "Evaluate the following trigonometric expression at standard angles:\n\n\$\$\\sin^2(\\theta) + \\cos^2(\\theta) + \\tan(\\phi)\$\$\n\nwhere \\\\(\\theta = 30^\\circ\\\\) and \\\\(\\phi = 45^\\circ\\\\).",
                optA = "0.5",
                optB = "1.0",
                optC = "2.0",
                optD = "1.5",
                answer = "C",
                explanation = "Using the fundamental trigonometric identity, we know that for any angle \\\\(\\theta\\\\):\n\n\$\$\\sin^2(\\theta) + \\cos^2(\\theta) = 1\$\$\n\nSince \\\\(\\phi = 45^\\circ\\\\), we have:\n\n\$\$\\tan(45^\\circ) = 1\$\$\n\nTherefore, the expression evaluates to:\n\n\$\$1 + 1 = 2\$\$\n\nThus, option **C** is correct.",
                isTheory = false
            ),
            Question(
                id = 3,
                subject = "English Language",
                topic = "Lexis and Structure",
                year = "2022",
                text = "Choose the word that is **most nearly opposite** in meaning to the underlined word:\n\n'The manager's decision was quite _arbitrary_.'",
                optA = "Reasonable",
                optB = "Dictatorial",
                optC = "Unplanned",
                optD = "Subjective",
                answer = "A",
                explanation = "The word **arbitrary** means based on random choice or personal whim, rather than any reasonable system or logic.\n\nThe opposite is **Reasonable** (rational, logical). Thus, option **A** is correct.",
                isTheory = false
            ),
            Question(
                id = 4,
                subject = "English Language",
                topic = "Comprehension",
                year = "2020",
                text = "Identify the grammatical name given to the underlined phrase in the sentence below:\n\n'The boy **_who stole the watch_** was caught by the vigilant security guard.'",
                optA = "Adverbial Clause of Time",
                optB = "Adjectival or Relative Clause",
                optC = "Noun Clause",
                optD = "Prepositional Phrase",
                answer = "B",
                explanation = "The underlined phrase **_who stole the watch_** contains a subject and a verb, and it post-modifies/qualifies the noun **'The boy'**.\n\nTherefore, it is an **Adjectival or Relative Clause**. Option **B** is correct.",
                isTheory = false
            ),
            Question(
                id = 5,
                subject = "Physics",
                topic = "Mechanics",
                year = "2021",
                text = "A vehicle accelerates uniformly from rest at a rate of \\\\(2\\\\text{ m/s}^2\\\\) for a duration of \\\\(5\\text{ seconds}\\\\). Find its final velocity.\n\n![Velocity-Time Graph](velocity_time_graph.png)",
                optA = "5 m/s",
                optB = "10 m/s",
                optC = "15 m/s",
                optD = "25 m/s",
                answer = "B",
                explanation = "From the equations of linear motion, the final velocity \\\\(v\\\\) is given by:\n\n\$\$v = u + at\$\$\n\nGiven:\n- Initial velocity \\\\(u = 0\\\\text{ m/s}\\\\) (since it starts from rest)\n- Acceleration \\\\(a = 2\\\\text{ m/s}^2\\\\)\n- Time \\\\(t = 5\\\\text{ seconds}\\\\)\n\nSubstitute the values:\n\n\$\$v = 0 + (2 \\\\times 5) = 10\\\\text{ m/s}\$\$\n\nTherefore, the final velocity is **10 m/s**. Option **B** is correct.",
                isTheory = false
            ),
            Question(
                id = 6,
                subject = "Physics",
                topic = "Optics",
                year = "2020",
                text = "What is the refractive index of a medium if the speed of light in that medium is \\\\(2.0 \\\\times 10^8\\\\text{ m/s}\\\\)?\n\n(Speed of light in vacuum \\\\(c = 3.0 \\\\times 10^8\\\\text{ m/s}\\\\))",
                optA = "1.33",
                optB = "1.50",
                optC = "1.66",
                optD = "2.00",
                answer = "B",
                explanation = "The refractive index \\\\(n\\\\) of a medium is defined by the ratio:\n\n\$\$n = \\\\frac{c}{v}\$\$\n\nWhere:\n- \\\\(c\\\\) is the speed of light in vacuum = \\\\(3.0 \\\\times 10^8\\\\text{ m/s}\\\\)\n- \\\\(v\\\\) is the speed of light in the medium = \\\\(2.0 \\\\times 10^8\\\\text{ m/s}\\\\)\n\nSubstituting the values:\n\n\$\$n = \\\\frac{3.0 \\\\times 10^8}{2.0 \\\\times 10^8} = 1.50\$\$\n\nHence, option **B** is the correct answer.",
                isTheory = false
            ),
            Question(
                id = 7,
                subject = "Biology",
                topic = "Genetics",
                year = "2022",
                text = "Which macromolecule serves as the primary genetic and hereditary material in all living organisms?\n\n![DNA Double Helix](dna_structure.png)",
                optA = "RNA",
                optB = "DNA",
                optC = "Ribosome",
                optD = "Protein",
                answer = "B",
                explanation = "**DNA** (Deoxyribonucleic acid) is the double-stranded molecule that carries the genetic instructions for development, functioning, growth, and reproduction of all known organisms. Option **B** is correct.",
                isTheory = false
            ),
            Question(
                id = 8,
                subject = "Biology",
                topic = "Ecology",
                year = "2021",
                text = "What type of ecological association is demonstrated by the mutual relationship between a fungus and an alga in Lichen?\n\n![Lichen Symbiosis](lichen_diagram.png)",
                optA = "Parasitism",
                optB = "Mutualism",
                optC = "Commensalism",
                optD = "Saprophytism",
                answer = "B",
                explanation = "In a **Lichen**, the alga provides photosynthetic nutrition while the fungus provides structural support, water, and minerals.\n\nThis mutually beneficial relationship is called **Mutualism** (symbiosis). Thus, option **B** is correct.",
                isTheory = false
            ),
            Question(
                id = 9,
                subject = "Mathematics",
                topic = "Probability",
                year = "2020",
                text = "A fair six-sided die is rolled once. What is the probability of obtaining a prime number?\n\nThe possible outcomes are represented by:\n\n\$\$S = \\\\{1, 2, 3, 4, 5, 6\\\\}\$\$",
                optA = "1/6",
                optB = "1/3",
                optC = "1/2",
                optD = "2/3",
                answer = "C",
                explanation = "The sample space \\\\(S\\\\) of a single die roll has \\(n(S) = 6\\) elements.\n\nThe subset representing prime number outcomes is:\n\n\$\$P = \\\\{2, 3, 5\\\\}\$\$\n\nThis gives \\(n(P) = 3\\). The probability is:\n\n\$\$P(\\\\text{Prime}) = \\\\frac{n(P)}{n(S)} = \\\\frac{3}{6} = \\\\frac{1}{2}\$\$\n\nTherefore, option **C** is correct.",
                isTheory = false
            ),
            Question(
                id = 10,
                subject = "Physics",
                topic = "Electricity",
                year = "2022",
                text = "Determine the effective resistance of the circuit shown below, containing two resistors connected in parallel:\n\n![Parallel Resistors Circuit](parallel_resistors.png)",
                optA = "2.4 ohms",
                optB = "5.0 ohms",
                optC = "10.0 ohms",
                optD = "24.0 ohms",
                answer = "A",
                explanation = "For parallel resistors, the reciprocal of the equivalent resistance \\\\(R_p\\\\) is the sum of the reciprocals of the individual resistances:\n\n\$\$\\frac{1}{R_p} = \\\\frac{1}{R_1} + \\\\frac{1}{R_2}\$\$\n\nGiven:\n- \\\\(R_1 = 4\\\\ \\\\Omega\\\\)\n- \\\\(R_2 = 6\\\\ \\\\Omega\\\\)\n\nCalculating:\n\n\$\$\\frac{1}{R_p} = \\\\frac{1}{4} + \\\\frac{1}{6} = \\\\frac{3+2}{12} = \\\\frac{5}{12}\$\$\n\n\$\$R_p = \\\\frac{12}{5} = 2.4\\\\ \\\\Omega\$\$\n\nNote how the subscript \\\\(R_p\\\\), \\\\(R_1\\\\), and \\\\(R_2\\\\) are preserved nicely without underline collision!\n\nAlso, consider chemical reactions such as the decomposition of sulfuric acid \\\\(<math>H_2SO_4</math>\\\\) which forms water \\\\(<math>H_2O</math>\\\\) and sulfur trioxide \\\\(<math>SO_3</math>\\\\):\n\n\$\$\\text{H}_2\\text{SO}_4 \\\\rightarrow \\text{H}_2\\text{O} + \\text{SO}_3\$\$\n\nIn both LaTeX math equations and inline \\\\(<math>H_2SO_4</math>\\\\) blocks, the subscripts/underscores are fully preserved and isolated from standard Markdown formatting.\n\nThus, option **A** is correct.",
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
