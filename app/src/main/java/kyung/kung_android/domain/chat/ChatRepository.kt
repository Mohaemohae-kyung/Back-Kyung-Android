package kyung.kung_android.domain.chat

import kyung.kung_android.data.chat.api.ChatApi
import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.data.chat.dto.ChatRoomResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApi: ChatApi,
) {

    suspend fun getRooms(): List<ChatRoomResponse> = chatApi.getRooms()

    suspend fun getMessages(
        chatRoomId: Long,
        page: Int = 0,
        size: Int = 50,
    ): List<ChatMessageResponse> = chatApi.getMessages(chatRoomId, page = page, size = size)

    suspend fun getLatestMessage(chatRoomId: Long): ChatMessageResponse? =
        chatApi.getMessages(chatRoomId, page = 0, size = 1).firstOrNull()

    suspend fun markRead(chatRoomId: Long) {
        chatApi.markRead(chatRoomId)
    }
}
