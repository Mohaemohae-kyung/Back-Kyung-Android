package kyung.kung_android.ui.auth.signup

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

data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val nickname: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val nicknameError: String? = null,
    val phoneError: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.length >= 8 && name.isNotBlank() && !isLoading
}

sealed interface SignupEffect {
    data object NavigateBackToLogin : SignupEffect
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignupUiState())
    val state: StateFlow<SignupUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SignupEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SignupEffect> = _effects.asSharedFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, emailError = null, errorMessage = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null, errorMessage = null) }
    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null, errorMessage = null) }
    fun onNicknameChange(v: String) = _state.update { it.copy(nickname = v, nicknameError = null, errorMessage = null) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v, phoneError = null, errorMessage = null) }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null, passwordError = null, nameError = null,
                nicknameError = null, phoneError = null,
            )
        }

        viewModelScope.launch {
            try {
                authRepository.signup(
                    email = current.email,
                    password = current.password,
                    name = current.name,
                    nickname = current.nickname,
                    phone = current.phone,
                )
                _effects.emit(SignupEffect.NavigateBackToLogin)
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
        if (e.isValidationError) {
            val f = e.fieldErrors.orEmpty()
            _state.update {
                it.copy(
                    emailError = f["email"],
                    passwordError = f["password"],
                    nameError = f["name"],
                    nicknameError = f["nickname"],
                    phoneError = f["phone"],
                )
            }
            return
        }
        _state.update { it.copy(errorMessage = e.message) }
    }
}
