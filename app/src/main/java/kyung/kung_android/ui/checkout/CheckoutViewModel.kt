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
import kyung.kung_android.domain.checkout.CheckoutRepository
import kyung.kung_android.domain.payment.PaymentRepository
import javax.inject.Inject

data class CheckoutUiState(
    val requestId: Long = 0L,
    val info: ServiceRequestCheckoutResponse? = null,
    val paymentMethod: String = "CARD",
    val agreePrivacy: Boolean = false,
    val agreeThirdParty: Boolean = false,
    val isLoading: Boolean = true,
    val isPaying: Boolean = false,
    val error: String? = null,
) {
    val canPay: Boolean
        get() = info != null && info.finalAmount != null && !isPaying &&
            agreePrivacy && agreeThirdParty
}

sealed interface CheckoutEffect {
    data class NavigateToTossPayment(
        val orderId: String,
        val amount: String,
        val paymentMethod: String,
        val requestId: Long,
        val orderName: String,
    ) : CheckoutEffect
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkoutRepository: CheckoutRepository,
    private val paymentRepository: PaymentRepository,
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

    fun onAgreePrivacyChange(value: Boolean) = _state.update { it.copy(agreePrivacy = value) }

    fun onAgreeThirdPartyChange(value: Boolean) = _state.update { it.copy(agreeThirdParty = value) }

    fun startPayment() {
        val current = _state.value
        if (!current.canPay) return
        val info = _state.value.info ?: return
        if (info.finalAmount == null || _state.value.isPaying) return
        _state.update { it.copy(isPaying = true, error = null) }
        viewModelScope.launch {
            try {
                val method = _state.value.paymentMethod
                val prepared = paymentRepository.prepareForServiceRequest(
                    requestId = requestId,
                    paymentMethod = method,
                )
                _state.update { it.copy(isPaying = false) }
                _effects.emit(
                    CheckoutEffect.NavigateToTossPayment(
                        orderId = prepared.orderId,
                        amount = prepared.finalAmount.toPlainString(),
                        paymentMethod = method,
                        requestId = requestId,
                        orderName = prepared.orderName
                            ?: info.requestTitle
                            ?: "매칭온 결제",
                    )
                )
            } catch (t: Throwable) {
                _state.update { it.copy(isPaying = false, error = "결제 준비에 실패했어요. 다시 시도해주세요.") }
            }
        }
    }
}
