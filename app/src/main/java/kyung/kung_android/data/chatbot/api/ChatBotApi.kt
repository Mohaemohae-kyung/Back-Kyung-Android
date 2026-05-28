package kyung.kung_android.data.chatbot.api

import kyung.kung_android.data.chatbot.dto.LlmChatRequest
import kyung.kung_android.data.chatbot.dto.LlmChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatBotApi {

    @POST("/api/chat/llm")
    suspend fun sendMessage(@Body request: LlmChatRequest): LlmChatResponse
}
