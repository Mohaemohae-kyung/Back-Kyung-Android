package kyung.kung_android.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<MyPageUiState> = combine(
        authRepository.isLoggedIn,
        userRepository.currentUser,
        _loading,
        _error,
    ) { loggedIn, user, loading, error ->
        MyPageUiState(
            isLoggedIn = loggedIn,
            user = user,
            isLoading = loading,
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MyPageUiState())

    fun loadUser() {
        if (!authRepository.isLoggedIn.value) return
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                userRepository.getMe()
            } catch (t: Throwable) {
                _error.value = "사용자 정보를 불러오지 못했어요."
            } finally {
                _loading.value = false
            }
        }
    }

    fun onLoggedOut() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
