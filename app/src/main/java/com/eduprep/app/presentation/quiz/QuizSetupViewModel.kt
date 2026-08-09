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
    val isLoading: Boolean = false,
    val theoryQuestions: List<Question> = emptyList()
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
                text = """
                    Solve for **x** in the quadratic equation where the coefficient of the quadratic term is positive:

                    ${'$'}${'$'} 3x^2 - 5x - 2 = 0 ${'$'}${'$'}

                    You can use the general quadratic formula:

                    ${'$'}${'$'} x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a} ${'$'}${'$'}
                """.trimIndent(),
                optA = """\( x = -\frac{1}{3} \) or \( x = 2 \)""",
                optB = """\( x = \frac{1}{3} \) or \( x = -2 \)""",
                optC = """\( x = -\frac{1}{3} \) or \( x = -2 \)""",
                optD = """![Graph](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=) None of the above""",
                optE = null,
                answer = "A",
                explanation = """
                    Comparing ${'$'}${'$'}3x^2 - 5x - 2 = 0${'$'}${'$'} to ${'$'}${'$'}ax^2 + bx + c = 0${'$'}${'$'}, we have:
                    **_a_** = 3, **_b_** = -5, and **_c_** = -2.

                    Substitute these into the quadratic formula:

                    ${'$'}${'$'} x = \frac{-(-5) \pm \sqrt{(-5)^2 - 4(3)(-2)}}{2(3)} ${'$'}${'$'}

                    ${'$'}${'$'} x = \frac{5 \pm 7}{6} ${'$'}${'$'}

                    Thus:
                    ${'$'}${'$'} x = 2 \text{ or } x = -\frac{1}{3} ${'$'}${'$'}
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 2,
                subject = "Mathematics",
                topic = "Trigonometry",
                year = "2021",
                text = """
                    Evaluate the following trigonometric expression at standard angles:

                    ${'$'}${'$'} \sin^2(\theta) + \cos^2(\theta) + \tan(\phi) ${'$'}${'$'}

                    where \( \theta = 30^\circ \) and \( \phi = 45^\circ \).
                """.trimIndent(),
                optA = "0.5",
                optB = "1.0",
                optC = "2.0",
                optD = "1.5",
                answer = "C",
                explanation = """
                    Using the fundamental trigonometric identity, we know that for any angle \( \theta \):

                    ${'$'}${'$'} \sin^2(\theta) + \cos^2(\theta) = 1 ${'$'}${'$'}

                    Since \( \phi = 45^\circ \), we have:

                    ${'$'}${'$'} \tan(45^\circ) = 1 ${'$'}${'$'}

                    Therefore, the expression evaluates to:

                    ${'$'}${'$'} 1 + 1 = 2 ${'$'}${'$'}

                    Thus, option **C** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 3,
                subject = "English Language",
                topic = "Lexis and Structure",
                year = "2022",
                text = """
                    Choose the word that is **most nearly opposite** in meaning to the underlined word:

                    'The manager's decision was quite _arbitrary_.'
                """.trimIndent(),
                optA = "Reasonable",
                optB = "Dictatorial",
                optC = "Unplanned",
                optD = "Subjective",
                answer = "A",
                explanation = """
                    The word **arbitrary** means based on random choice or personal whim, rather than any reasonable system or logic.

                    The opposite is **Reasonable** (rational, logical). Thus, option **A** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 4,
                subject = "English Language",
                topic = "Comprehension",
                year = "2020",
                text = """
                    Identify the grammatical name given to the underlined phrase in the sentence below:

                    'The boy **_who stole the watch_** was caught by the vigilant security guard.'
                """.trimIndent(),
                optA = "Adverbial Clause of Time",
                optB = "Adjectival or Relative Clause",
                optC = "Noun Clause",
                optD = "Prepositional Phrase",
                answer = "B",
                explanation = """
                    The underlined phrase **_who stole the watch_** contains a subject and a verb, and it post-modifies/qualifies the noun **'The boy'**.

                    Therefore, it is an **Adjectival or Relative Clause**. Option **B** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 5,
                subject = "Physics",
                topic = "Mechanics",
                year = "2021",
                text = """
                    A vehicle accelerates uniformly from rest at a rate of \( 2\text{ m/s}^2 \) for a duration of \( 5\text{ seconds} \). Find its final velocity.

                    ![Velocity-Time Graph](velocity_time_graph.png)
                """.trimIndent(),
                optA = "5 m/s",
                optB = "10 m/s",
                optC = "15 m/s",
                optD = "25 m/s",
                answer = "B",
                explanation = """
                    From the equations of linear motion, the final velocity \( v \) is given by:

                    ${'$'}${'$'} v = u + at ${'$'}${'$'}

                    Given:
                    - Initial velocity \( u = 0\text{ m/s} \) (since it starts from rest)
                    - Acceleration \( a = 2\text{ m/s}^2 \)
                    - Time \( t = 5\text{ seconds} \)

                    Substitute the values:

                    ${'$'}${'$'} v = 0 + (2 \times 5) = 10\text{ m/s} ${'$'}${'$'}

                    Therefore, the final velocity is **10 m/s**. Option **B** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 6,
                subject = "Physics",
                topic = "Optics",
                year = "2020",
                text = """
                    What is the refractive index of a medium if the speed of light in that medium is \( 2.0 \times 10^8\text{ m/s} \)?

                    (Speed of light in vacuum \( c = 3.0 \times 10^8\text{ m/s} \))
                """.trimIndent(),
                optA = "1.33",
                optB = "1.50",
                optC = "1.66",
                optD = "2.00",
                answer = "B",
                explanation = """
                    The refractive index \( n \) of a medium is defined by the ratio:

                    ${'$'}${'$'} n = \frac{c}{v} ${'$'}${'$'}

                    Where:
                    - \( c \) is the speed of light in vacuum = \( 3.0 \times 10^8\text{ m/s} \)
                    - \( v \) is the speed of light in the medium = \( 2.0 \times 10^8\text{ m/s} \)

                    Substituting the values:

                    ${'$'}${'$'} n = \frac{3.0 \times 10^8}{2.0 \times 10^8} = 1.50 ${'$'}${'$'}

                    Hence, option **B** is the correct answer.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 7,
                subject = "Biology",
                topic = "Genetics",
                year = "2022",
                text = """
                    Which macromolecule serves as the primary genetic and hereditary material in all living organisms?

                    ![DNA Double Helix](dna_structure.png)
                """.trimIndent(),
                optA = "RNA",
                optB = "DNA",
                optC = "Ribosome",
                optD = "Protein",
                answer = "B",
                explanation = """
                    **DNA** (Deoxyribonucleic acid) is the double-stranded molecule that carries the genetic instructions for development, functioning, growth, and reproduction of all known organisms. Option **B** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 8,
                subject = "Biology",
                topic = "Ecology",
                year = "2021",
                text = """
                    What type of ecological association is demonstrated by the mutual relationship between a fungus and an alga in Lichen?

                    ![Lichen Symbiosis](lichen_diagram.png)
                """.trimIndent(),
                optA = "Parasitism",
                optB = "Mutualism",
                optC = "Commensalism",
                optD = "Saprophytism",
                answer = "B",
                explanation = """
                    In a **Lichen**, the alga provides photosynthetic nutrition while the fungus provides structural support, water, and minerals.

                    This mutually beneficial relationship is called **Mutualism** (symbiosis). Thus, option **B** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 9,
                subject = "Mathematics",
                topic = "Probability",
                year = "2020",
                text = """
                    A fair six-sided die is rolled once. What is the probability of obtaining a prime number?

                    The possible outcomes are represented by:

                    ${'$'}${'$'} S = \{1, 2, 3, 4, 5, 6\} ${'$'}${'$'}
                """.trimIndent(),
                optA = "1/6",
                optB = "1/3",
                optC = "1/2",
                optD = "2/3",
                answer = "C",
                explanation = """
                    The sample space \( S \) of a single die roll has \( n(S) = 6 \) elements.

                    The subset representing prime number outcomes is:

                    ${'$'}${'$'} P = \{2, 3, 5\} ${'$'}${'$'}

                    This gives \( n(P) = 3 \). The probability is:

                    ${'$'}${'$'} P(\text{Prime}) = \frac{n(P)}{n(S)} = \frac{3}{6} = \frac{1}{2} ${'$'}${'$'}

                    Therefore, option **C** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 10,
                subject = "Physics",
                topic = "Electricity",
                year = "2022",
                text = """
                    Determine the effective resistance of the circuit shown below, containing two resistors connected in parallel:

                    ![Parallel Resistors Circuit](parallel_resistors.png)
                """.trimIndent(),
                optA = "2.4 ohms",
                optB = "5.0 ohms",
                optC = "10.0 ohms",
                optD = "24.0 ohms",
                answer = "A",
                explanation = """
                    For parallel resistors, the reciprocal of the equivalent resistance \( R_p \) is the sum of the reciprocals of the individual resistances:

                    ${'$'}${'$'} \frac{1}{R_p} = \frac{1}{R_1} + \frac{1}{R_2} ${'$'}${'$'}

                    Given:
                    - \( R_1 = 4\ \Omega \)
                    - \( R_2 = 6\ \Omega \)

                    Calculating:

                    ${'$'}${'$'} \frac{1}{R_p} = \frac{1}{4} + \frac{1}{6} = \frac{3+2}{12} = \frac{5}{12} ${'$'}${'$'}

                    ${'$'}${'$'} R_p = \frac{12}{5} = 2.4\ \Omega ${'$'}${'$'}

                    Note how the subscript \( R_p \), \( R_1 \), and \( R_2 \) are preserved nicely without underline collision!

                    Also, consider chemical reactions such as the decomposition of sulfuric acid \( <math>H_2SO_4</math> \) which forms water \( <math>H_2O</math> \) and sulfur trioxide \( <math>SO_3</math> \):

                    ${'$'}${'$'} \text{H}_2\text{SO}_4 \rightarrow \text{H}_2\text{O} + \text{SO}_3 ${'$'}${'$'}

                    In both LaTeX math equations and inline \( <math>H_2SO_4</math> \) blocks, the subscripts/underscores are fully preserved and isolated from standard Markdown formatting.

                    Thus, option **A** is correct.
                """.trimIndent(),
                isTheory = false
            ),
            Question(
                id = 11,
                subject = "Physics",
                topic = "Mechanics",
                year = "2022",
                text = "A body of mass 5 kg is pulled with a constant force. If it accelerates at 3 m/s², what is the magnitude of the applied force in Newtons?",
                optA = "",
                optB = "",
                optC = "",
                optD = "",
                answer = "",
                explanation = "Stating and applying Newton's second law: F = m * a. Substitute m = 5 kg and a = 3 m/s² to get F = 15 N. Keywords: force, mass, acceleration, Newton.",
                isTheory = true,
                exactMathAnswer = "15"
            ),
            Question(
                id = 12,
                subject = "Chemistry",
                topic = "Electrolysis",
                year = "2021",
                text = "Describe the electrolysis of acidified water. State the reaction occurring at the anode and the cathode, and explain why dilute sulfuric acid is added.",
                optA = "",
                optB = "",
                optC = "",
                optD = "",
                answer = "",
                explanation = "Acidified water electrolysis: 1. Water is a weak electrolyte, dilute sulfuric acid is added to increase electrical conductivity. 2. At the cathode, hydrogen ions are reduced to produce Hydrogen gas (H2). 3. At the anode, hydroxide ions are oxidized to produce Oxygen gas (O2). Keywords: hydrogen, oxygen, conductivity, sulfuric acid.",
                isTheory = true,
                exactMathAnswer = null
            ),
            Question(
                id = 13,
                subject = "Biology",
                topic = "Photosynthesis",
                year = "2022",
                text = "Explain the light-dependent and light-independent stages of photosynthesis. Where in the chloroplast does each stage occur?",
                optA = "",
                optB = "",
                optC = "",
                optD = "",
                answer = "",
                explanation = "Light-dependent stage: Occurs in the thylakoid membrane (grana). Chlorophyll absorbs light energy to split water (photolysis) producing ATP and NADPH. Light-independent stage (Calvin Cycle): Occurs in the stroma. Carbon dioxide is fixed to produce glucose using ATP and NADPH. Keywords: thylakoid, stroma, photolysis, chlorophyll, glucose, calvin.",
                isTheory = true,
                exactMathAnswer = null
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
                val theoryQ = quizRepository.getTheoryQuestions()
                _uiState.update { state ->
                    state.copy(
                        subjects = finalSubjects,
                        selectedSubject = if (state.selectedSubject in finalSubjects) state.selectedSubject else "All",
                        theoryQuestions = theoryQ,
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
