package kyung.kung_android.ui.chat_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.data.chat.stomp.ChatStompClient
import kyung.kung_android.domain.chat.ChatRepository
import kyung.kung_android.domain.user.UserRepository
import kyung.kung_android.ui.navigation.AppRoute
import org.hildan.krossbow.stomp.StompSession
import javax.inject.Inject

data class ChatDetailUiState(
    val chatRoomId: Long = 0L,
    val currentUserId: Long? = null,
    val messages: List<ChatMessageResponse> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val stompClient: ChatStompClient,
) : ViewModel() {

    private val chatRoomId: Long = checkNotNull(savedStateHandle[AppRoute.ARG_CHAT_ROOM_ID])

    private val _state = MutableStateFlow(ChatDetailUiState(chatRoomId = chatRoomId))
    val state: StateFlow<ChatDetailUiState> = _state.asStateFlow()

    private var session: StompSession? = null

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { userRepository.getMe() }
                .onSuccess { me -> _state.update { it.copy(currentUserId = me.userId) } }
            loadHistory()
            connectStomp()
        }
    }

    private suspend fun loadHistory() {
        runCatching { chatRepository.getMessages(chatRoomId) }
            .onSuccess { history ->
                _state.update { it.copy(messages = history, isLoading = false) }
            }
            .onFailure {
                _state.update { it.copy(isLoading = false, error = "이전 메시지를 불러오지 못했어요.") }
            }
    }

    private fun connectStomp() {
        viewModelScope.launch {
            try {
                val s = stompClient.connect()
                session = s
                _state.update { it.copy(isConnected = true) }
                stompClient.subscribeRoom(s, chatRoomId).collect { msg ->
                    _state.update { it.copy(messages = it.messages + msg) }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isConnected = false, error = "채팅 연결에 실패했어요.") }
            }
        }
    }

    fun updateInput(text: String) {
        _state.update { it.copy(input = text) }
    }

    fun send() {
        val text = _state.value.input.trim()
        val senderId = _state.value.currentUserId
        val s = session
        if (text.isEmpty() || s == null || senderId == null) return
        viewModelScope.launch {
            runCatching {
                stompClient.sendMessage(
                    session = s,
                    roomId = chatRoomId,
                    senderId = senderId,
                    message = text,
                )
            }.onSuccess {
                _state.update { it.copy(input = "") }
            }.onFailure {
                _state.update { it.copy(error = "메시지를 보내지 못했어요.") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val s = session ?: return
        viewModelScope.launch { stompClient.disconnect(s) }
    }
}
