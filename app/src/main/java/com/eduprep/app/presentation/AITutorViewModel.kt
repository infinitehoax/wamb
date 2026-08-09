package com.eduprep.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.data.remote.TutorHistoryItem
import com.eduprep.app.domain.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TutorMessage(
    val isUser: Boolean,
    val text: String
)

data class AITutorUiState(
    val messages: List<TutorMessage> = listOf(
        TutorMessage(false, "Hello! I am your friendly expert WAEC and JAMB tutor. What topic or subject can I help you explain or understand today? Feel free to ask me to write equations or diagrams.")
    ),
    val currentInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AITutorViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AITutorUiState())
    val uiState: StateFlow<AITutorUiState> = _uiState.asStateFlow()

    fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input, errorMessage = null) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val textToSend = state.currentInput.trim()
        if (textToSend.isEmpty() || state.isLoading) return

        // Add user message to state
        val updatedMessages = state.messages + TutorMessage(true, textToSend)
        _uiState.update {
            it.copy(
                messages = updatedMessages,
                currentInput = "",
                isLoading = true,
                errorMessage = null
            )
        }

        // Build the list of history elements to match PythonAnywhere structure
        val historyPayload = updatedMessages.map { msg ->
            TutorHistoryItem(
                type = if (msg.isUser) "user_input" else "ai_reply",
                content = msg.text
            )
        }

        viewModelScope.launch {
            val result = backendRepository.sendTutorChat(historyPayload)
            result.onSuccess { response ->
                _uiState.update { s ->
                    s.copy(
                        messages = s.messages + TutorMessage(false, response.reply),
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { s ->
                    s.copy(
                        errorMessage = error.message ?: "Tutor Engine Failed. Please check your internet connection.",
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
