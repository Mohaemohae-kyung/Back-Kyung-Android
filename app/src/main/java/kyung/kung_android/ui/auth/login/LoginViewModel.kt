package kyung.kung_android.ui.auth.login

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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LoginEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<LoginEffect> = _effects.asSharedFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessage = null, emailError = null, passwordError = null) }

        viewModelScope.launch {
            try {
                authRepository.login(current.email, current.password)
                _effects.emit(LoginEffect.NavigateToHome)
            } catch (e: ApiException) {
                handleError(e)
            } catch (t: Throwable) {
                _state.update { it.copy(errorMessage = "네트워크 오류가 발생했습니다.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleError(e: ApiException) {
        when {
            e.isValidationError -> {
                _state.update {
                    it.copy(
                        emailError = e.fieldErrors?.get("email"),
                        passwordError = e.fieldErrors?.get("password"),
                    )
                }
            }
            e.errorCode == "AUTH_401_1" -> {
                _state.update { it.copy(errorMessage = "이메일 또는 비밀번호를 확인해주세요.") }
            }
            e.errorCode == "AUTH_401_2" -> {
                _state.update { it.copy(errorMessage = "탈퇴한 회원입니다.") }
            }
            else -> {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}
