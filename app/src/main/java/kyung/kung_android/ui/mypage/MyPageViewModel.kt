package kyung.kung_android.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.user.dto.UserProfileResponse
import kyung.kung_android.domain.auth.AuthRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class MyPageUiState(
    val isLoggedIn: Boolean = false,
    val user: UserProfileResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val isExpert: Boolean
        get() = user?.role == "EXPERT" || user?.role == "ADMIN"
}

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MyPageUiState())
    val state: StateFlow<MyPageUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isLoggedIn.collectLatest { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn, user = if (!loggedIn) null else it.user) }
                if (loggedIn) loadUser()
            }
        }
    }

    private fun loadUser() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = userRepository.getMe()
                _state.update { it.copy(user = user, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "사용자 정보를 불러오지 못했어요.") }
            }
        }
    }

    fun onLoggedOut() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { MyPageUiState(isLoggedIn = false) }
        }
    }
}
