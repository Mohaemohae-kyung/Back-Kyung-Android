package kyung.kung_android.data.chat.stomp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kyung.kung_android.BuildConfig
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.data.chat.dto.ChatMessageSendRequest
import kyung.kung_android.data.network.di.AuthClient
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatStompClient @Inject constructor(
    private val tokenStore: TokenStore,
    @AuthClient okHttpClient: OkHttpClient,
    private val json: Json,
) {

    private val wsClient = OkHttpWebSocketClient(okHttpClient)
    private val stompClient = StompClient(wsClient)

    suspend fun connect(): StompSession {
        val token = tokenStore.getAccess()
        val customHeaders = if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
        return stompClient.connect(
            url = wsUrl(),
            customStompConnectHeaders = customHeaders,
        )
    }

    suspend fun subscribeRoom(
        session: StompSession,
        chatRoomId: Long,
    ): Flow<ChatMessageResponse> {
        return session.subscribeText("/sub/chat/room/$chatRoomId").map { body ->
            json.decodeFromString(ChatMessageResponse.serializer(), body)
        }
    }

    suspend fun sendMessage(
        session: StompSession,
        roomId: Long,
        senderId: Long,
        message: String,
    ) {
        val payload = json.encodeToString(
            ChatMessageSendRequest.serializer(),
            ChatMessageSendRequest(
                roomId = roomId.toString(),
                senderId = senderId.toString(),
                message = message,
                type = "TEXT",
            ),
        )
        session.sendText("/pub/chat/message", payload)
    }

    suspend fun disconnect(session: StompSession) {
        runCatching { session.disconnect() }
    }

    private fun wsUrl(): String {
        val base = BuildConfig.BASE_URL.trimEnd('/')
        val scheme = if (base.startsWith("https")) "wss" else "ws"
        val host = base.removePrefix("https://").removePrefix("http://")
        return "$scheme://$host/ws-stomp"
    }
}
