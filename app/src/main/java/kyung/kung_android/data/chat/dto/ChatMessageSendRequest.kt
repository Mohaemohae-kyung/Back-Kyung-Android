package kyung.kung_android.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageSendRequest(
    val roomId: String,
    val senderId: String,
    val message: String,
    val type: String,
)
