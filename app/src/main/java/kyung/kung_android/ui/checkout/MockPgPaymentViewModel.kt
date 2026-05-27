package kyung.kung_android.ui.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.chat.stomp.ChatStompClient
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import kyung.kung_android.domain.user.UserRepository
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class MockPgUiState(
    val orderId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: String = "CARD",
    val isApproving: Boolean = false,
    val error: String? = null,
)

sealed interface MockPgEffect {
    data class Success(val paymentId: Long) : MockPgEffect
}

@HiltViewModel
class MockPgPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val userRepository: UserRepository,
    private val stompClient: ChatStompClient,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()
    private val amount: BigDecimal =
        runCatching { BigDecimal(savedStateHandle.get<String>("amount").orEmpty()) }
            .getOrDefault(BigDecimal.ZERO)
    private val paymentMethod: String = savedStateHandle.get<String>("method") ?: "CARD"

    // 견적 결제일 때만 전달됨. 결제 완료 후 채팅방에 완료 메시지를 보내기 위함.
    private val requestId: Long? = savedStateHandle.get<String>("requestId")?.toLongOrNull()

    private val _state = MutableStateFlow(
        MockPgUiState(orderId = orderId, amount = amount, paymentMethod = paymentMethod)
    )
    val state: StateFlow<MockPgUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MockPgEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<MockPgEffect> = _effects.asSharedFlow()

    fun approve() {
        if (_state.value.isApproving) return
        _state.update { it.copy(isApproving = true, error = null) }
        viewModelScope.launch {
            try {
                val pg = paymentRepository.approveMockPg(
                    orderId = orderId,
                    amount = amount,
                    paymentMethod = paymentMethod,
                )
                val confirmed = paymentRepository.confirm(
                    orderId = orderId,
                    paymentKey = pg.paymentKey,
                    amount = amount,
                )
                if (requestId != null) {
                    runCatching { sendPaymentCompletedChat(amount.toLong(), requestId) }
                }
                _effects.emit(MockPgEffect.Success(paymentId = confirmed.paymentId))
            } catch (t: Throwable) {
                _state.update { it.copy(isApproving = false, error = "결제 승인에 실패했어요. 다시 시도해주세요.") }
            }
        }
    }

    private suspend fun sendPaymentCompletedChat(amount: Long, requestId: Long) {
        val req = runCatching { serviceRequestRepository.getRequest(requestId) }.getOrNull() ?: return
        val roomId = req.chatRoomId ?: return
        val senderId = userRepository.currentUser.value?.userId ?: return
        val formatted = NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
        val text = "💸 결제가 완료되었습니다. ${formatted}원"
        val session = runCatching { stompClient.connect() }.getOrNull() ?: return
        try {
            stompClient.sendMessage(
                session = session,
                roomId = roomId,
                senderId = senderId,
                message = text,
            )
        } finally {
            runCatching { stompClient.disconnect(session) }
        }
    }
}
