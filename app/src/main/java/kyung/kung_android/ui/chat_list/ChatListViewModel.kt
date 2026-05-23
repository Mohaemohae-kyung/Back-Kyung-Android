package kyung.kung_android.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.domain.chat.ChatRepository
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import javax.inject.Inject

data class ChatRoomCard(
    val chatRoomId: Long,
    val peerName: String,
    val categoryName: String?,
    val lastMessage: String?,
)

data class ChatListUiState(
    val rooms: List<ChatRoomCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val rooms = chatRepository.getRooms()
                val requestsByRoom = runCatching { serviceRequestRepository.getMyRequests() }
                    .getOrDefault(emptyList())
                    .filter { it.chatRoomId != null }
                    .associateBy { it.chatRoomId!! }

                val cards = coroutineScope {
                    rooms.map { room ->
                        async {
                            val request = requestsByRoom[room.chatRoomId]
                            val expertDeferred = request?.expertProfileId?.let { id ->
                                async { runCatching { expertRepository.getExpertDetail(id) }.getOrNull() }
                            }
                            val lastMessageDeferred = async {
                                runCatching { chatRepository.getLatestMessage(room.chatRoomId) }.getOrNull()
                            }
                            val expert = expertDeferred?.await()
                            val lastMessage = lastMessageDeferred.await()
                            ChatRoomCard(
                                chatRoomId = room.chatRoomId,
                                peerName = expert?.displayName ?: "상대",
                                categoryName = expert?.mainCategoryName,
                                lastMessage = lastMessage?.content,
                            )
                        }
                    }.awaitAll()
                }
                _state.update { it.copy(rooms = cards, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "목록을 불러오지 못했어요.") }
            }
        }
    }
}
