package kyung.kung_android.ui.account_settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kyung.kung_android.data.user.dto.UserProfileResponse
import kyung.kung_android.domain.auth.AuthRepository
import kyung.kung_android.domain.file.FileRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class AccountSettingsUiState(
    val user: UserProfileResponse? = null,
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isUpdating: Boolean = false,
    val isLoggingOut: Boolean = false,
    val error: String? = null,
)

sealed interface AccountSettingsEvent {
    data object LoggedOut : AccountSettingsEvent
}

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountSettingsUiState())
    val state: StateFlow<AccountSettingsUiState> = _state.asStateFlow()

    private val _events = Channel<AccountSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    fun onPickProfileImage(uri: Uri) {
        _state.update { it.copy(isUploadingImage = true, error = null) }
        viewModelScope.launch {
            try {
                val uploaded = withContext(Dispatchers.IO) {
                    fileRepository.uploadImage(uri = uri, domain = "PROFILE")
                }
                val updated = userRepository.updateMyProfile(profileImageFileId = uploaded.fileId)
                _state.update { it.copy(user = updated, isUploadingImage = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isUploadingImage = false, error = "사진 업로드에 실패했어요.") }
            }
        }
    }

    fun updateName(name: String) {
        if (name.isBlank() || _state.value.isUpdating) return
        _state.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                val updated = userRepository.updateMyProfile(name = name)
                _state.update { it.copy(user = updated, isUpdating = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isUpdating = false, error = "활동명 변경에 실패했어요.") }
            }
        }
    }

    fun logout() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(isLoggingOut = false) }
            _events.send(AccountSettingsEvent.LoggedOut)
        }
    }
}
