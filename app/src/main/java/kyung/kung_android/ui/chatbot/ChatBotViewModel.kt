package kyung.kung_android.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatBotMessage {
    abstract val text: String

    data class Bot(override val text: String) : ChatBotMessage()
    data class User(override val text: String) : ChatBotMessage()
}

data class ChatBotUiState(
    val messages: List<ChatBotMessage> = listOf(GREETING),
    val input: String = "",
    val isThinking: Boolean = false,
) {
    companion object {
        val GREETING = ChatBotMessage.Bot(
            "안녕하세요. 매칭온 AI 상담 챗봇입니다. 무엇이 궁금하신가요?"
        )
    }
}

@HiltViewModel
class ChatBotViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ChatBotUiState())
    val state: StateFlow<ChatBotUiState> = _state.asStateFlow()

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun onSend() {
        val text = _state.value.input.trim()
        if (text.isEmpty() || _state.value.isThinking) return

        _state.update {
            it.copy(
                messages = it.messages + ChatBotMessage.User(text),
                input = "",
                isThinking = true,
            )
        }

        viewModelScope.launch {
            delay(MOCK_DELAY_MS)
            _state.update {
                it.copy(
                    messages = it.messages + ChatBotMessage.Bot(
                        "(mock 응답) v2에서 실제 LLM 연동 예정입니다."
                    ),
                    isThinking = false,
                )
            }
        }
    }

    fun onNewQuestion() {
        _state.update { ChatBotUiState() }
    }

    private companion object {
        private const val MOCK_DELAY_MS = 1000L
    }
}
