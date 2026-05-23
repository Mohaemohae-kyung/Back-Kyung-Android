package kyung.kung_android.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.chat.ChatRepository
import javax.inject.Inject

data class ChatRoomCard(
    val chatRoomId: Long,
    val peerName: String,
    val lastMessage: String?,
    val unreadCount: Long,
)

data class ChatListUiState(
    val rooms: List<ChatRoomCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val rooms = chatRepository.getRooms()
                val cards = rooms.map { room ->
                    ChatRoomCard(
                        chatRoomId = room.chatRoomId,
                        peerName = room.roomName ?: "상대",
                        lastMessage = room.lastMessage,
                        unreadCount = room.unreadCount,
                    )
                }
                _state.update { it.copy(rooms = cards, isLoading = false) }
            } catch (e: ApiException) {
                if (e.httpStatus == 500) {
                    _state.update { it.copy(rooms = emptyList(), isLoading = false, error = null) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "목록을 불러오지 못했어요.") }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "목록을 불러오지 못했어요.") }
            }
        }
    }
}
