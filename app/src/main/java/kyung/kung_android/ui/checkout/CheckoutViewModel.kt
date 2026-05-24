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
import kyung.kung_android.data.checkout.dto.ServiceRequestCheckoutResponse
import kyung.kung_android.data.chat.stomp.ChatStompClient
import kyung.kung_android.domain.checkout.CheckoutRepository
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import kyung.kung_android.domain.user.UserRepository
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class CheckoutUiState(
    val requestId: Long = 0L,
    val info: ServiceRequestCheckoutResponse? = null,
    val paymentMethod: String = "MATCHING_ON_PAY",
    val isLoading: Boolean = true,
    val isPaying: Boolean = false,
    val error: String? = null,
)

sealed interface CheckoutEffect {
    data class Success(val paymentId: Long) : CheckoutEffect
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkoutRepository: CheckoutRepository,
    private val paymentRepository: PaymentRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val userRepository: UserRepository,
    private val stompClient: ChatStompClient,
) : ViewModel() {

    private val requestId: Long = savedStateHandle.get<Long>("requestId") ?: 0L

    private val _state = MutableStateFlow(CheckoutUiState(requestId = requestId))
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CheckoutEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CheckoutEffect> = _effects.asSharedFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val info = checkoutRepository.getServiceRequestCheckout(requestId)
                _state.update { it.copy(info = info, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "결제 정보를 불러오지 못했어요.") }
            }
        }
    }

    fun onMethodSelected(method: String) = _state.update { it.copy(paymentMethod = method) }

    private suspend fun sendPaymentCompletedChat(amount: Long) {
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

    fun startPayment() {
        val info = _state.value.info ?: return
        val amount = info.finalAmount ?: return
        if (_state.value.isPaying) return
        _state.update { it.copy(isPaying = true, error = null) }
        viewModelScope.launch {
            try {
                val method = _state.value.paymentMethod
                val prepared = paymentRepository.prepareForServiceRequest(
                    requestId = requestId,
                    paymentMethod = method,
                )
                val pg = paymentRepository.approveMockPg(
                    orderId = prepared.orderId,
                    amount = prepared.finalAmount,
                    paymentMethod = method,
                )
                val confirmed = paymentRepository.confirm(
                    orderId = prepared.orderId,
                    paymentKey = pg.paymentKey,
                    amount = prepared.finalAmount,
                )
                runCatching { sendPaymentCompletedChat(prepared.finalAmount.toLong()) }
                _effects.emit(CheckoutEffect.Success(paymentId = confirmed.paymentId))
            } catch (t: Throwable) {
                _state.update { it.copy(isPaying = false, error = "결제에 실패했어요.") }
            }
        }
    }
}
