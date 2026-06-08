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

private const val PIN_LENGTH = 6

enum class PinChangeStep { CURRENT, NEW, CONFIRM }

data class PaymentPasswordChangeUiState(
    val step: PinChangeStep = PinChangeStep.CURRENT,
    /** 입력 자릿수(표시용). */
    val pinLength: Int = 0,
    /** 키 배열 재구성 트리거. */
    val keypadNonce: Int = 0,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

sealed interface PaymentPasswordChangeEffect {
    data object Done : PaymentPasswordChangeEffect
}

@HiltViewModel
class PaymentPasswordChangeViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentPasswordChangeUiState())
    val state: StateFlow<PaymentPasswordChangeUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PaymentPasswordChangeEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<PaymentPasswordChangeEffect> = _effects.asSharedFlow()

    // 입력 중 버퍼와 확인된 현재 PIN/새 PIN을 가변 버퍼로 다룬다.
    private val pinBuffer = CharArray(PIN_LENGTH)
    private var pinCount = 0
    private val currentPin = CharArray(PIN_LENGTH)
    private var currentCount = 0
    private val newPin = CharArray(PIN_LENGTH)
    private var newCount = 0

    private fun clearBuffer() {
        pinBuffer.fill(' ')
        pinCount = 0
    }

    private fun clearCurrent() {
        currentPin.fill(' ')
        currentCount = 0
    }

    private fun clearNew() {
        newPin.fill(' ')
        newCount = 0
    }

    fun onPinDigit(digit: Char) {
        if (pinCount >= PIN_LENGTH || _state.value.isSubmitting) return
        pinBuffer[pinCount++] = digit
        _state.update { it.copy(pinLength = pinCount, error = null) }
        if (pinCount == PIN_LENGTH) onProceed()
    }

    fun onPinDelete() {
        if (pinCount == 0) return
        pinCount--
        pinBuffer[pinCount] = ' '
        _state.update { it.copy(pinLength = pinCount) }
    }

    private fun onProceed() {
        if (pinCount != PIN_LENGTH) return
        when (_state.value.step) {
            PinChangeStep.CURRENT -> verifyCurrent()
            PinChangeStep.NEW -> {
                pinBuffer.copyInto(newPin)
                newCount = pinCount
                clearBuffer()
                _state.update {
                    it.copy(step = PinChangeStep.CONFIRM, pinLength = 0, keypadNonce = it.keypadNonce + 1, error = null)
                }
            }
            PinChangeStep.CONFIRM -> {
                if (!newMatches()) {
                    clearBuffer()
                    clearNew()
                    _state.update {
                        it.copy(
                            step = PinChangeStep.NEW,
                            pinLength = 0,
                            keypadNonce = it.keypadNonce + 1,
                            error = "새 비밀번호가 일치하지 않아요. 다시 입력해주세요.",
                        )
                    }
                    return
                }
                submitChange()
            }
        }
    }

    private fun newMatches(): Boolean {
        if (newCount != pinCount) return false
        var diff = 0
        for (i in 0 until PIN_LENGTH) diff = diff or (newPin[i].code xor pinBuffer[i].code)
        return diff == 0
    }

    private fun verifyCurrent() {
        val pin = String(pinBuffer, 0, pinCount)
        clearBuffer()
        _state.update { it.copy(isSubmitting = true, pinLength = 0, error = null) }
        viewModelScope.launch {
            try {
                val userId = userRepository.currentUser.value?.userId
                    ?: userRepository.getMe().userId
                    ?: throw IllegalStateException("사용자 정보를 확인할 수 없어요.")
                paymentRepository.verifyPaymentPassword(userId = userId, currentPin = pin)
                // 확인된 현재 PIN을 보관(최종 변경 호출에 필요)
                pin.toCharArray().copyInto(currentPin)
                currentCount = pin.length
                _state.update {
                    it.copy(step = PinChangeStep.NEW, isSubmitting = false, pinLength = 0, keypadNonce = it.keypadNonce + 1, error = null)
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        pinLength = 0,
                        keypadNonce = it.keypadNonce + 1,
                        error = t.message ?: "현재 결제 비밀번호가 일치하지 않아요.",
                    )
                }
            }
        }
    }

    private fun submitChange() {
        val newValue = String(newPin, 0, newCount)
        val currentValue = String(currentPin, 0, currentCount)
        clearBuffer()
        clearNew()
        clearCurrent()
        _state.update { it.copy(isSubmitting = true, pinLength = 0, error = null) }
        viewModelScope.launch {
            try {
                val userId = userRepository.currentUser.value?.userId
                    ?: userRepository.getMe().userId
                    ?: throw IllegalStateException("사용자 정보를 확인할 수 없어요.")
                paymentRepository.changePaymentPassword(
                    userId = userId,
                    currentPin = currentValue,
                    newPin = newValue,
                )
                _effects.emit(PaymentPasswordChangeEffect.Done)
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        step = PinChangeStep.CURRENT,
                        isSubmitting = false,
                        pinLength = 0,
                        keypadNonce = it.keypadNonce + 1,
                        error = t.message ?: "변경에 실패했어요. 다시 시도해주세요.",
                    )
                }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    override fun onCleared() {
        clearBuffer()
        clearCurrent()
        clearNew()
        super.onCleared()
    }
}
