package kyung.kung_android.ui.profile_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

private data class LocalState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())

    val state: StateFlow<ProfileInfoUiState> = combine(
        userRepository.currentUser,
        _local,
    ) { user, local ->
        ProfileInfoUiState(
            user = user,
            isLoading = local.isLoading,
            isUpdating = local.isUpdating,
            error = local.error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileInfoUiState())

    fun load() {
        _local.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                userRepository.getMe()
            } catch (t: Throwable) {
                _local.update { it.copy(error = "사용자 정보를 불러오지 못했어요.") }
            } finally {
                _local.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updatePhone(phone: String) {
        if (phone.isBlank() || _local.value.isUpdating) return
        _local.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                userRepository.updateMyProfile(phone = phone)
            } catch (t: Throwable) {
                _local.update { it.copy(error = "변경에 실패했어요.") }
            } finally {
                _local.update { it.copy(isUpdating = false) }
            }
        }
    }
}
