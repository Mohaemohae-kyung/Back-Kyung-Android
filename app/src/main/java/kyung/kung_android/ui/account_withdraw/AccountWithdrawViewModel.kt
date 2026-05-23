package kyung.kung_android.ui.account_withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class AccountWithdrawUiState(
    val reasonKey: String? = null,
    val customReason: String = "",
    val password: String = "",
    val agreed: Boolean = false,
    val isWithdrawing: Boolean = false,
    val passwordError: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isWithdrawing && password.isNotBlank() && agreed && (
            (reasonKey != null && reasonKey != "OTHER") ||
                (reasonKey == "OTHER" && customReason.isNotBlank())
            )

    val effectiveReason: String?
        get() = when (reasonKey) {
            null -> null
            "OTHER" -> customReason.ifBlank { null }
            else -> reasonKey
        }
}

sealed interface WithdrawEvent {
    data object Success : WithdrawEvent
}

@HiltViewModel
class AccountWithdrawViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountWithdrawUiState())
    val state: StateFlow<AccountWithdrawUiState> = _state.asStateFlow()

    private val _events = Channel<WithdrawEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onReasonChange(key: String) = _state.update { it.copy(reasonKey = key, customReason = if (key != "OTHER") "" else it.customReason) }
    fun onCustomReasonChange(v: String) = _state.update { it.copy(customReason = v) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null) }
    fun onAgreedChange(v: Boolean) = _state.update { it.copy(agreed = v) }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isWithdrawing = true, errorMessage = null, passwordError = null) }
        viewModelScope.launch {
            try {
                userRepository.withdraw(password = s.password, reason = s.effectiveReason)
                tokenStore.clear()
                _events.send(WithdrawEvent.Success)
            } catch (t: Throwable) {
                val msg = t.message.orEmpty()
                if (msg.contains("비밀번호", ignoreCase = true) || msg.contains("password", ignoreCase = true)) {
                    _state.update { it.copy(isWithdrawing = false, passwordError = "비밀번호가 일치하지 않습니다.") }
                } else {
                    _state.update { it.copy(isWithdrawing = false, errorMessage = "탈퇴 처리에 실패했어요.") }
                }
            }
        }
    }
}
