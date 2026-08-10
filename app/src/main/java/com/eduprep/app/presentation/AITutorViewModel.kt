package com.eduprep.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.eduprep.app.data.local.ChatDao
import com.eduprep.app.data.local.ChatEntity
import com.eduprep.app.data.local.ThemePreferences
import com.eduprep.app.data.remote.TutorRequest
import com.eduprep.app.domain.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TutorMessage(
    val isUser: Boolean,
    val text: String
)

data class AITutorUiState(
    val messages: List<TutorMessage> = emptyList(),
    val currentInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AITutorViewModel @Inject constructor(
    private val backendRepository: BackendRepository,
    private val chatDao: ChatDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AITutorUiState())
    val uiState: StateFlow<AITutorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatDao.getAllMessages().collect { entities ->
                val tutorMessages = if (entities.isEmpty()) {
                    listOf(
                        TutorMessage(
                            isUser = false,
                            text = "Hello! I am your friendly expert WAEC and JAMB tutor. What topic or subject can I help you explain or understand today? Feel free to ask me to write equations or diagrams."
                        )
                    )
                } else {
                    entities.map { TutorMessage(it.isUser, it.messageText) }
                }
                _uiState.update { it.copy(messages = tutorMessages) }
            }
        }
    }

    fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input, errorMessage = null) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val newUserInput = state.currentInput.trim()
        if (newUserInput.isEmpty() || state.isLoading) return

        _uiState.update {
            it.copy(
                currentInput = "",
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val historyLimit = ThemePreferences.maxChatHistoryFlow(context).first() // Read limit (e.g., 10)
                val fullHistory = chatDao.getAllMessagesSync()

                // Save user message to database
                chatDao.insert(
                    ChatEntity(
                        isUser = true,
                        messageText = newUserInput
                    )
                )

                // CRITICAL: Use takeLast() to keep only the newest messages!
                val recentHistory = fullHistory.takeLast(historyLimit)

                // 1. Build the massive text block
                val historyBlock = recentHistory.joinToString("\n\n") { chat ->
                    if (chat.isUser) "Student: ${chat.messageText}" else "Teacher: ${chat.messageText}"
                }

                // 2. Append the new message
                val finalPrompt = if (historyBlock.isBlank()) {
                    "Student: $newUserInput"
                } else {
                    "$historyBlock\n\nStudent: $newUserInput"
                }

                // 3. Send via Retrofit
                val request = TutorRequest(prompt = finalPrompt)
                val result = backendRepository.sendTutorMessage(request)

                result.onSuccess { response ->
                    // 4. Save response to Room
                    chatDao.insert(ChatEntity(isUser = false, messageText = response.reply))
                    _uiState.update { s ->
                        s.copy(isLoading = false)
                    }
                }.onFailure { error ->
                    _uiState.update { s ->
                        s.copy(
                            errorMessage = error.message ?: "Tutor Engine Failed. Please check your internet connection.",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { s ->
                    s.copy(
                        errorMessage = e.message ?: "Tutor Engine Failed. Please try again.",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
