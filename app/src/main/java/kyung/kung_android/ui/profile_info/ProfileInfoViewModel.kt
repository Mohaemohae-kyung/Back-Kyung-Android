package kyung.kung_android.ui.profile_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.user.dto.UserProfileResponse
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class ProfileInfoUiState(
    val user: UserProfileResponse? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileInfoUiState())
    val state: StateFlow<ProfileInfoUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
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

    fun updatePhone(phone: String) {
        if (phone.isBlank() || _state.value.isUpdating) return
        _state.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                val updated = userRepository.updateMyProfile(phone = phone)
                _state.update { it.copy(user = updated, isUpdating = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isUpdating = false, error = "변경에 실패했어요.") }
            }
        }
    }
}
