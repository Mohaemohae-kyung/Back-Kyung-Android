package kyung.kung_android.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kyung.kung_android.domain.auth.AuthRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class MainScaffoldViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isExpert: StateFlow<Boolean> = userRepository.currentUser
        .map { it?.role == "EXPERT" || it?.role == "ADMIN" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        authRepository.isLoggedIn
            .filter { it }
            .onEach {
                if (userRepository.currentUser.value == null) {
                    runCatching { userRepository.getMe() }
                }
            }
            .launchIn(viewModelScope)
    }
}
