package kyung.kung_android.ui.password_change

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
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.auth.AuthRepository
import javax.inject.Inject

data class PasswordChangeUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val newPasswordValid: Boolean get() = newPassword.length >= 8
    val confirmMatches: Boolean get() = newPassword == confirmPassword

    val canSubmit: Boolean
        get() = currentPassword.isNotBlank() &&
            newPasswordValid &&
            confirmMatches &&
            !isSubmitting
}

sealed interface PasswordChangeEffect {
    data object Success : PasswordChangeEffect
}

@HiltViewModel
class PasswordChangeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordChangeUiState())
    val state: StateFlow<PasswordChangeUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PasswordChangeEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<PasswordChangeEffect> = _effects.asSharedFlow()

    fun onCurrentChange(v: String) = _state.update { it.copy(currentPassword = v, error = null) }
    fun onNewChange(v: String) = _state.update { it.copy(newPassword = v, error = null) }
    fun onConfirmChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.changePassword(
                    currentPassword = s.currentPassword,
                    newPassword = s.newPassword,
                )
                _effects.emit(PasswordChangeEffect.Success)
            } catch (e: ApiException) {
                _state.update { it.copy(isSubmitting = false, error = e.message ?: "비밀번호 변경에 실패했어요.") }
            } catch (t: Throwable) {
                _state.update { it.copy(isSubmitting = false, error = "네트워크 오류가 발생했어요.") }
            }
        }
    }
}
