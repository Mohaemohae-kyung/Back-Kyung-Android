package kyung.kung_android.ui.chat_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.data.chat.stomp.ChatStompClient
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.domain.chat.ChatRepository
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import kyung.kung_android.domain.user.UserRepository
import kyung.kung_android.ui.navigation.AppRoute
import org.hildan.krossbow.stomp.StompSession
import java.math.BigDecimal
import javax.inject.Inject

data class ChatDetailUiState(
    val chatRoomId: Long = 0L,
    val currentUserId: Long? = null,
    val messages: List<ChatMessageResponse> = emptyList(),
    val linkedRequest: ServiceRequestResponse? = null,
    val linkedExpert: ExpertDetailResponse? = null,
    val input: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val isRequestingPayment: Boolean = false,
    val paymentRequested: Boolean = false,
    val error: String? = null,
) {
    val isRequester: Boolean
        get() = linkedRequest?.userId != null && currentUserId != null &&
            linkedRequest.userId == currentUserId

    /** 이 채팅의 견적을 받은 고수 입장 (의뢰인이 내가 아님) */
    val isExpertSide: Boolean
        get() = linkedRequest?.userId != null && currentUserId != null &&
            linkedRequest.userId != currentUserId

    /** 고수가 결제 요청을 보냈는지 (채팅에 PAYMENT_REQUEST 메시지 존재) */
    val hasPaymentRequest: Boolean
        get() = paymentRequested || messages.any { it.messageType == "PAYMENT_REQUEST" }
}

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val expertRepository: ExpertRepository,
    private val paymentRepository: PaymentRepository,
    private val stompClient: ChatStompClient,
) : ViewModel() {

    private val chatRoomId: Long = checkNotNull(savedStateHandle[AppRoute.ARG_CHAT_ROOM_ID])

    private val _state = MutableStateFlow(ChatDetailUiState(chatRoomId = chatRoomId))
    val state: StateFlow<ChatDetailUiState> = _state.asStateFlow()

    @Volatile private var session: StompSession? = null

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { userRepository.getMe() }
                .onSuccess { me -> _state.update { it.copy(currentUserId = me.userId) } }
            loadLinkedRequest()
            loadHistory()
            markRead()
            connectWithRetry()
        }
    }

    private suspend fun loadLinkedRequest() {
        val me = userRepository.currentUser.value ?: return
        val isExpert = me.role == "EXPERT" || me.role == "ADMIN"
        runCatching {
            if (isExpert) serviceRequestRepository.getReceivedRequests()
            else serviceRequestRepository.getMyRequests()
        }.onSuccess { list ->
            val match = list.firstOrNull { it.chatRoomId == chatRoomId } ?: return@onSuccess
            _state.update { it.copy(linkedRequest = match) }
            match.expertProfileId?.let { pid ->
                runCatching { expertRepository.getExpertDetail(pid) }
                    .onSuccess { d -> _state.update { it.copy(linkedExpert = d) } }
            }
        }
    }

    private suspend fun loadHistory() {
        runCatching { chatRepository.getMessages(chatRoomId) }
            .onSuccess { history ->
                _state.update { it.copy(messages = history, isLoading = false) }
            }
            .onFailure {
                _state.update { it.copy(isLoading = false, error = "이전 메시지를 불러오지 못했어요.") }
            }
    }

    private suspend fun markRead() {
        runCatching { chatRepository.markRead(chatRoomId) }
    }

    private suspend fun connectWithRetry() {
        var attempt = 0
        while (viewModelScope.isActive) {
            try {
                val s = stompClient.connect()
                session = s
                attempt = 0
                _state.update { it.copy(isConnected = true, error = null) }

                stompClient.subscribeRoom(s, chatRoomId)
                    .catch { /* stream error */ }
                    .onCompletion { _state.update { it.copy(isConnected = false) } }
                    .collect { msg ->
                        _state.update { it.copy(messages = it.messages + msg) }
                        runCatching { chatRepository.markRead(chatRoomId) }
                    }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                _state.update { it.copy(isConnected = false) }
            }

            session = null
            if (!viewModelScope.isActive) return

            attempt = (attempt + 1).coerceAtMost(MAX_BACKOFF_STEP)
            val backoff = BACKOFF_BASE_MS * (1L shl (attempt - 1).coerceAtLeast(0))
            delay(backoff.coerceAtMost(MAX_BACKOFF_MS))
        }
    }

    fun requestPayment(serviceName: String, amount: BigDecimal) {
        val req = _state.value.linkedRequest ?: return
        if (_state.value.isRequestingPayment) return
        _state.update { it.copy(isRequestingPayment = true, error = null) }
        viewModelScope.launch {
            try {
                serviceRequestRepository.update(
                    requestId = req.requestId,
                    title = serviceName,
                    content = "결제 요청",
                    budget = amount,
                )
                paymentRepository.prepareForServiceRequest(req.requestId)
                val updated = runCatching { serviceRequestRepository.getRequest(req.requestId) }.getOrNull()
                _state.update {
                    it.copy(
                        isRequestingPayment = false,
                        paymentRequested = true,
                        linkedRequest = updated ?: it.linkedRequest,
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isRequestingPayment = false, error = "결제 요청에 실패했어요.") }
            }
        }
    }

    fun updateInput(text: String) {
        _state.update { it.copy(input = text) }
    }

    fun send() {
        val text = _state.value.input.trim()
        val senderId = _state.value.currentUserId
        val s = session
        if (text.isEmpty() || s == null || senderId == null) return
        viewModelScope.launch {
            runCatching {
                stompClient.sendMessage(
                    session = s,
                    roomId = chatRoomId,
                    senderId = senderId,
                    message = text,
                )
            }.onSuccess {
                _state.update { it.copy(input = "") }
            }.onFailure {
                _state.update { it.copy(error = "메시지를 보내지 못했어요.") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val s = session ?: return
        runBlocking { runCatching { stompClient.disconnect(s) } }
    }

    private companion object {
        const val BACKOFF_BASE_MS = 1_000L
        const val MAX_BACKOFF_MS = 15_000L
        const val MAX_BACKOFF_STEP = 5
    }
}
