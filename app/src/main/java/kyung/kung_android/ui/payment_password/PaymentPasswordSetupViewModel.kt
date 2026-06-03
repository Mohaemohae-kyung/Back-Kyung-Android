package kyung.kung_android.ui.payment_password

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
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

enum class PinStep { FIRST, CONFIRM }

data class PaymentPasswordSetupUiState(
    val step: PinStep = PinStep.FIRST,
    val pin: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val canProceed: Boolean get() = pin.length == 6 && !isSubmitting
}

sealed interface PaymentPasswordSetupEffect {
    data object Done : PaymentPasswordSetupEffect
}

@HiltViewModel
class PaymentPasswordSetupViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentPasswordSetupUiState())
    val state: StateFlow<PaymentPasswordSetupUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PaymentPasswordSetupEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<PaymentPasswordSetupEffect> = _effects.asSharedFlow()

    private var firstPin: String = ""

    fun onPinChange(value: String) = _state.update { it.copy(pin = value, error = null) }

    fun onProceed() {
        val current = _state.value
        if (!current.canProceed) return
        when (current.step) {
            PinStep.FIRST -> {
                firstPin = current.pin
                _state.update { it.copy(step = PinStep.CONFIRM, pin = "", error = null) }
            }
            PinStep.CONFIRM -> {
                if (current.pin != firstPin) {
                    firstPin = ""
                    _state.update {
                        it.copy(step = PinStep.FIRST, pin = "", error = "비밀번호가 일치하지 않아요. 다시 입력해주세요.")
                    }
                    return
                }
                submit(current.pin)
            }
        }
    }

    private fun submit(pin: String) {
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                val userId = userRepository.currentUser.value?.userId
                    ?: userRepository.getMe().userId
                    ?: throw IllegalStateException("사용자 정보를 확인할 수 없어요.")
                paymentRepository.setupPaymentPassword(userId = userId, paymentPin = pin)
                // 프로필 갱신(hasPaymentPassword 반영)
                runCatching { userRepository.getMe() }
                _effects.emit(PaymentPasswordSetupEffect.Done)
            } catch (t: Throwable) {
                firstPin = ""
                _state.update {
                    it.copy(step = PinStep.FIRST, pin = "", error = t.message ?: "설정에 실패했어요. 다시 시도해주세요.")
                }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
