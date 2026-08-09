package com.eduprep.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.data.local.ChatDao
import com.eduprep.app.data.local.ChatEntity
import com.eduprep.app.data.remote.TutorStep
import com.eduprep.app.data.remote.TutorContent
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
    val messages: List<TutorMessage> = emptyList(),
    val currentInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AITutorViewModel @Inject constructor(
    private val backendRepository: BackendRepository,
    private val chatDao: ChatDao
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
        val textToSend = state.currentInput.trim()
        if (textToSend.isEmpty() || state.isLoading) return

        _uiState.update {
            it.copy(
                currentInput = "",
                isLoading = true,
                errorMessage = null
            )
        }

        // Build history payload from current memory history
        val historyPayload = state.messages.map { msg ->
            TutorStep(
                type = if (msg.isUser) "user_input" else "model_response",
                content = listOf(TutorContent(text = msg.text))
            )
        } + TutorStep(
            type = "user_input",
            content = listOf(TutorContent(text = textToSend))
        )

        viewModelScope.launch {
            // Save user message to database
            chatDao.insertMessage(
                ChatEntity(
                    isUser = true,
                    messageText = textToSend
                )
            )

            val result = backendRepository.sendTutorChat(historyPayload)
            result.onSuccess { response ->
                // Save AI message to database
                chatDao.insertMessage(
                    ChatEntity(
                        isUser = false,
                        messageText = response.reply
                    )
                )
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
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
