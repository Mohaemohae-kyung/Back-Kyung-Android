package kyung.kung_android.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomResponse(
    val chatRoomId: Long,
    val userId: Long? = null,
)
