package kyung.kung_android.ui.booking

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
import kyung.kung_android.data.checkout.dto.BookingCheckoutResponse
import kyung.kung_android.domain.checkout.CheckoutRepository
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class BookingCheckoutUiState(
    val bookingId: Long = 0L,
    val info: BookingCheckoutResponse? = null,
    val paymentMethod: String = "CARD",
    val agreePrivacy: Boolean = false,
    val agreeThirdParty: Boolean = false,
    val isLoading: Boolean = true,
    val isPaying: Boolean = false,
    val showPinDialog: Boolean = false,
    val pin: String = "",
    val error: String? = null,
) {
    val canPay: Boolean
        get() = info != null && agreePrivacy && agreeThirdParty && !isPaying
}

sealed interface BookingCheckoutEffect {
    data class NavigateToTossPayment(
        val orderId: String,
        val amount: String,
        val paymentMethod: String,
        val orderName: String,
    ) : BookingCheckoutEffect

    data object NavigateToPaymentPasswordSetup : BookingCheckoutEffect
}

@HiltViewModel
class BookingCheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkoutRepository: CheckoutRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    private val _state = MutableStateFlow(BookingCheckoutUiState(bookingId = bookingId))
    val state: StateFlow<BookingCheckoutUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<BookingCheckoutEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<BookingCheckoutEffect> = _effects.asSharedFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val info = checkoutRepository.getBookingCheckout(bookingId)
                _state.update { it.copy(info = info, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "결제 정보를 불러오지 못했어요.") }
            }
        }
    }

    fun onAgreePrivacyChange(value: Boolean) = _state.update { it.copy(agreePrivacy = value) }

    fun onAgreeThirdPartyChange(value: Boolean) = _state.update { it.copy(agreeThirdParty = value) }

    fun startPayment() {
        val current = _state.value
        if (!current.canPay) return
        viewModelScope.launch {
            val hasPassword = userRepository.currentUser.value?.hasPaymentPassword
                ?: runCatching { userRepository.getMe().hasPaymentPassword }.getOrDefault(false)
            if (!hasPassword) {
                _effects.emit(BookingCheckoutEffect.NavigateToPaymentPasswordSetup)
            } else {
                _state.update { it.copy(showPinDialog = true, pin = "", error = null) }
            }
        }
    }

    fun onPinChange(value: String) = _state.update { it.copy(pin = value) }

    fun dismissPinDialog() = _state.update { it.copy(showPinDialog = false, pin = "") }

    fun confirmPin() {
        val current = _state.value
        if (current.pin.length != 6 || current.info == null || current.isPaying) return
        val pin = current.pin
        _state.update { it.copy(isPaying = true, showPinDialog = false, error = null) }
        viewModelScope.launch {
            try {
                val method = _state.value.paymentMethod
                val prepared = paymentRepository.prepareForBooking(
                    bookingId = bookingId,
                    paymentMethod = method,
                    paymentPin = pin,
                )
                _state.update { it.copy(isPaying = false, pin = "") }
                _effects.emit(
                    BookingCheckoutEffect.NavigateToTossPayment(
                        orderId = prepared.orderId,
                        amount = prepared.finalAmount.toPlainString(),
                        paymentMethod = method,
                        orderName = prepared.orderName
                            ?: _state.value.info?.productTitle
                            ?: "매칭온 결제",
                    )
                )
            } catch (t: Throwable) {
                _state.update {
                    it.copy(isPaying = false, pin = "", error = t.message ?: "결제 준비에 실패했어요. 다시 시도해주세요.")
                }
            }
        }
    }
}
