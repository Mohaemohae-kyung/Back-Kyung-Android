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

private const val PIN_LENGTH = 6

data class PaymentPasswordSetupUiState(
    val step: PinStep = PinStep.FIRST,
    /** 입력 자릿수(표시용). */
    val pinLength: Int = 0,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val canProceed: Boolean get() = pinLength == PIN_LENGTH && !isSubmitting
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

    private val pinBuffer = CharArray(PIN_LENGTH)
    private var pinCount = 0
    private val firstPin = CharArray(PIN_LENGTH)
    private var firstCount = 0

    private fun clearCurrent() {
        pinBuffer.fill(' ')
        pinCount = 0
    }

    private fun clearFirst() {
        firstPin.fill(' ')
        firstCount = 0
    }

    fun onPinDigit(digit: Char) {
        if (pinCount >= PIN_LENGTH) return
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
        val current = _state.value
        if (pinCount != PIN_LENGTH || current.isSubmitting) return
        when (current.step) {
            PinStep.FIRST -> {
                pinBuffer.copyInto(firstPin)
                firstCount = pinCount
                clearCurrent()
                _state.update { it.copy(step = PinStep.CONFIRM, pinLength = 0, error = null) }
            }
            PinStep.CONFIRM -> {
                if (!buffersMatch()) {
                    clearCurrent()
                    clearFirst()
                    _state.update {
                        it.copy(step = PinStep.FIRST, pinLength = 0, error = "비밀번호가 일치하지 않아요. 다시 입력해주세요.")
                    }
                    return
                }
                submit()
            }
        }
    }

    /** 1차/2차 입력 버퍼를 자리별로 비교한다. */
    private fun buffersMatch(): Boolean {
        if (firstCount != pinCount) return false
        var diff = 0
        for (i in 0 until PIN_LENGTH) diff = diff or (firstPin[i].code xor pinBuffer[i].code)
        return diff == 0
    }

    private fun submit() {
        val pin = String(pinBuffer, 0, pinCount)
        clearCurrent()
        clearFirst()
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
                _state.update {
                    it.copy(step = PinStep.FIRST, pinLength = 0, error = t.message ?: "설정에 실패했어요. 다시 시도해주세요.")
                }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    override fun onCleared() {
        clearCurrent()
        clearFirst()
        super.onCleared()
    }
}
