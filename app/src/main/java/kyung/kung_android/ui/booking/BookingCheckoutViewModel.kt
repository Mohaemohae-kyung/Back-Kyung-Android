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
import kyung.kung_android.data.coupon.dto.AvailableCouponDto
import kyung.kung_android.domain.checkout.CheckoutRepository
import kyung.kung_android.domain.coupon.CouponRepository
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.user.UserRepository
import java.math.BigDecimal
import javax.inject.Inject

data class BookingCheckoutUiState(
    val bookingId: Long = 0L,
    val info: BookingCheckoutResponse? = null,
    val paymentMethod: String = "CARD",
    val coupons: List<AvailableCouponDto> = emptyList(),
    val selectedCouponId: Long? = null,
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

    val selectedCoupon: AvailableCouponDto?
        get() = coupons.firstOrNull { it.userCouponId == selectedCouponId }

    /** 쿠폰 선택을 반영한 표시용 최종 금액(실제 청구액은 서버 prepare 응답 기준). */
    val displayFinalAmount: BigDecimal
        get() {
            val base = info?.finalAmount ?: BigDecimal.ZERO
            val discount = selectedCoupon?.discountAmount ?: BigDecimal.ZERO
            return (base - discount).coerceAtLeast(BigDecimal.ZERO)
        }
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
    private val couponRepository: CouponRepository,
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
            // 사용 가능한 쿠폰 조회(실패해도 결제 흐름은 진행)
            runCatching {
                couponRepository.getUsableCoupons(targetType = "BOOKING", targetId = bookingId)
            }.onSuccess { coupons ->
                _state.update { it.copy(coupons = coupons) }
            }
        }
    }

    fun onCouponSelected(userCouponId: Long?) = _state.update { it.copy(selectedCouponId = userCouponId) }

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
                    userCouponId = _state.value.selectedCouponId,
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
