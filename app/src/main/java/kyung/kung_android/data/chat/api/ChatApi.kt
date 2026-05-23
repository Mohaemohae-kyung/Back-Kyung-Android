package kyung.kung_android.data.chat.api

import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.data.chat.dto.ChatRoomResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatApi {

    @GET("/api/chat/rooms")
    suspend fun getRooms(): List<ChatRoomResponse>

    @GET("/api/chat/rooms/{chatRoomId}/messages")
    suspend fun getMessages(
        @Path("chatRoomId") chatRoomId: Long,
    ): List<ChatMessageResponse>
}
