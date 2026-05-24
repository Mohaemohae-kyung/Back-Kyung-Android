package kyung.kung_android.data.chat.api

import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.data.chat.dto.ChatRoomResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    @GET("/api/chat/rooms")
    suspend fun getRooms(): List<ChatRoomResponse>

    @GET("/api/chat/rooms/{chatRoomId}/messages")
    suspend fun getMessages(
        @Path("chatRoomId") chatRoomId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sort") sort: String = "chatMessageId,desc",
    ): List<ChatMessageResponse>

    @PATCH("/api/chat/rooms/{chatRoomId}/read")
    suspend fun markRead(
        @Path("chatRoomId") chatRoomId: Long,
    ): Unit?
}
