package kyung.kung_android.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.chatbot.api.ChatBotApi
import kyung.kung_android.data.chatbot.dto.LlmChatRequest
import java.util.UUID
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
class ChatBotViewModel @Inject constructor(
    private val chatBotApi: ChatBotApi,
) : ViewModel() {

    private var sessionId: String = UUID.randomUUID().toString()

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
            val reply = runCatching {
                chatBotApi.sendMessage(
                    LlmChatRequest(message = text, session_id = sessionId)
                ).reply
            }.getOrElse {
                "⚠️ 응답을 받지 못했습니다. 잠시 후 다시 시도해주세요."
            }

            _state.update {
                it.copy(
                    messages = it.messages + ChatBotMessage.Bot(reply),
                    isThinking = false,
                )
            }
        }
    }

    fun onNewQuestion() {
        sessionId = UUID.randomUUID().toString()
        _state.update { ChatBotUiState() }
    }
}
