package kyung.kung_android.ui.account_settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

private data class LocalState(
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isUpdating: Boolean = false,
    val isLoggingOut: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())

    val state: StateFlow<AccountSettingsUiState> = combine(
        userRepository.currentUser,
        _local,
    ) { user, local ->
        AccountSettingsUiState(
            user = user,
            isLoading = local.isLoading,
            isUploadingImage = local.isUploadingImage,
            isUpdating = local.isUpdating,
            isLoggingOut = local.isLoggingOut,
            error = local.error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AccountSettingsUiState())

    private val _events = Channel<AccountSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    fun onPickProfileImage(uri: Uri) {
        _local.update { it.copy(isUploadingImage = true, error = null) }
        viewModelScope.launch {
            try {
                val uploaded = withContext(Dispatchers.IO) {
                    fileRepository.uploadImage(uri = uri, domain = "PROFILE")
                }
                userRepository.updateMyProfile(profileImageFileId = uploaded.fileId)
            } catch (t: Throwable) {
                _local.update { it.copy(error = "사진 업로드에 실패했어요.") }
            } finally {
                _local.update { it.copy(isUploadingImage = false) }
            }
        }
    }

    fun updateName(name: String) {
        if (name.isBlank() || _local.value.isUpdating) return
        _local.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                userRepository.updateMyProfile(name = name)
            } catch (t: Throwable) {
                _local.update { it.copy(error = "활동명 변경에 실패했어요.") }
            } finally {
                _local.update { it.copy(isUpdating = false) }
            }
        }
    }

    fun logout() {
        if (_local.value.isLoggingOut) return
        _local.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            _local.update { it.copy(isLoggingOut = false) }
            _events.send(AccountSettingsEvent.LoggedOut)
        }
    }
}
