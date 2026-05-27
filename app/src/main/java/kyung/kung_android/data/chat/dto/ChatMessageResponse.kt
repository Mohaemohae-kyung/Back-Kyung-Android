package kyung.kung_android.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageResponse(
    val chatMessageId: Long,
    val roomId: Long,
    val senderId: Long? = null,
    val messageType: String = "TEXT",
    val content: String,
    val readYn: String? = null,
)
