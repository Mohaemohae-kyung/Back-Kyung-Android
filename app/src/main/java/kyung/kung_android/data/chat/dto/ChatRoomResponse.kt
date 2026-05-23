package kyung.kung_android.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomResponse(
    val chatRoomId: Long,
    val userId: Long? = null,
    val roomName: String? = null,
    val lastMessage: String? = null,
    val unreadCount: Long = 0,
)
