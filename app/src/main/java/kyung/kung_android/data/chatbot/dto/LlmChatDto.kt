package kyung.kung_android.data.chatbot.dto

import kotlinx.serialization.Serializable

@Serializable
data class LlmChatRequest(
    val message: String,
    val session_id: String,
)

@Serializable
data class LlmChatResponse(
    val reply: String,
)
