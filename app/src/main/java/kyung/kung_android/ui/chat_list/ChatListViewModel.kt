package kyung.kung_android.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val rooms = chatRepository.getRooms()
                val requests = runCatching { serviceRequestRepository.getMyRequests() }
                    .getOrDefault(emptyList())
                    .filter { it.chatRoomId != null }
                    .associateBy { it.chatRoomId!! }

                val expertCache = mutableMapOf<Long, Pair<String, String?>>() // id -> (name, category)

                val cards = rooms.map { room ->
                    val request = requests[room.chatRoomId]
                    val expertProfileId = request?.expertProfileId
                    val expertInfo = expertProfileId?.let { id ->
                        expertCache[id] ?: runCatching {
                            val expert = expertRepository.getExpertDetail(id)
                            expert.displayName to expert.mainCategoryName
                        }.getOrNull()?.also { expertCache[id] = it }
                    }
                    val lastMessage = runCatching {
                        chatRepository.getMessages(room.chatRoomId).lastOrNull()?.content
                    }.getOrNull()

                    ChatRoomCard(
                        chatRoomId = room.chatRoomId,
                        peerName = expertInfo?.first ?: "상대",
                        categoryName = expertInfo?.second,
                        lastMessage = lastMessage,
                    )
                }
                _state.update { it.copy(rooms = cards, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "목록을 불러오지 못했어요.") }
            }
        }
    }
}
