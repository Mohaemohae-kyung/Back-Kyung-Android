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

data class TossPaymentUiState(
    val orderId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val orderName: String = "매칭온 결제",
    val launched: Boolean = false,
    val pendingPaymentKey: String? = null,
    val isConfirming: Boolean = false,
    val error: String? = null,
)

sealed interface TossPaymentEffect {
    data class Success(val paymentId: Long) : TossPaymentEffect
}

@HiltViewModel
class TossPaymentViewModel @Inject constructor(
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
    private val orderName: String =
        savedStateHandle.get<String>("orderName")?.takeIf { it.isNotBlank() } ?: "매칭온 결제"

    private val requestId: Long? = savedStateHandle.get<String>("requestId")?.toLongOrNull()

    private val _state = MutableStateFlow(
        TossPaymentUiState(orderId = orderId, amount = amount, orderName = orderName)
    )
    val state: StateFlow<TossPaymentUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TossPaymentEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<TossPaymentEffect> = _effects.asSharedFlow()

    fun markLaunched() = _state.update { it.copy(launched = true) }

    fun onPaymentSuccess(paymentKey: String) {
        if (_state.value.isConfirming) return
        _state.update { it.copy(pendingPaymentKey = paymentKey, isConfirming = true, error = null) }
        confirmInternal(paymentKey)
    }

    fun retryConfirm() {
        val key = _state.value.pendingPaymentKey ?: return
        if (_state.value.isConfirming) return
        _state.update { it.copy(isConfirming = true, error = null) }
        confirmInternal(key)
    }

    fun retryPayment() {
        _state.update { it.copy(launched = false, pendingPaymentKey = null, error = null) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun onPaymentFailed(message: String?) {
        val text = message?.takeIf { it.isNotBlank() } ?: "결제가 취소되었어요."
        _state.update { it.copy(isConfirming = false, error = text) }
    }

    private fun confirmInternal(paymentKey: String) {
        viewModelScope.launch {
            try {
                val confirmed = paymentRepository.confirm(
                    orderId = orderId,
                    paymentKey = paymentKey,
                    amount = amount,
                )
                if (requestId != null) {
                    runCatching { sendPaymentCompletedChat(amount.toLong(), requestId) }
                }
                _effects.emit(TossPaymentEffect.Success(paymentId = confirmed.paymentId))
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isConfirming = false,
                        error = "결제 승인에 실패했어요. 잠시 후 다시 시도해주세요.",
                    )
                }
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
