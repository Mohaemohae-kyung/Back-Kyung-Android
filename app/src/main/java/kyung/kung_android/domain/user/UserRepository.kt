package kyung.kung_android.domain.user

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kyung.kung_android.data.user.api.UserApi
import kyung.kung_android.data.user.dto.UserProfileResponse
import kyung.kung_android.data.user.dto.UserProfileUpdateRequest
import kyung.kung_android.data.user.dto.UserWithdrawRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
) {

    private val _currentUser = MutableStateFlow<UserProfileResponse?>(null)
    val currentUser: StateFlow<UserProfileResponse?> = _currentUser.asStateFlow()

    suspend fun getMe(): UserProfileResponse {
        val user = userApi.getMe()
        _currentUser.value = user
        return user
    }

    suspend fun updateMyProfile(
        name: String? = null,
        phone: String? = null,
        nickname: String? = null,
        profileImageFileId: Long? = null,
    ): UserProfileResponse {
        val updated = userApi.updateMyProfile(
            UserProfileUpdateRequest(
                name = name?.trim()?.takeIf { it.isNotEmpty() },
                phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                nickname = nickname?.trim()?.takeIf { it.isNotEmpty() },
                profileImageFileId = profileImageFileId,
            )
        )
        _currentUser.value = updated
        return updated
    }

    suspend fun withdraw(password: String, reason: String?) {
        userApi.withdraw(
            UserWithdrawRequest(
                password = password,
                reason = reason?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
        _currentUser.value = null
    }

    fun clearCache() {
        _currentUser.value = null
    }

    suspend fun registerFcmToken(token: String) {
        userApi.registerFcmToken(kyung.kung_android.data.user.dto.FcmTokenRequest(token))
    }
}
