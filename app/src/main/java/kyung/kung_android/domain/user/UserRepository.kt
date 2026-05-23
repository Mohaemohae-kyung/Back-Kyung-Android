package kyung.kung_android.domain.user

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

    suspend fun getMe(): UserProfileResponse = userApi.getMe()

    suspend fun updateMyProfile(
        name: String? = null,
        phone: String? = null,
        nickname: String? = null,
        profileImageFileId: Long? = null,
    ): UserProfileResponse = userApi.updateMyProfile(
        UserProfileUpdateRequest(
            name = name?.trim()?.takeIf { it.isNotEmpty() },
            phone = phone?.trim()?.takeIf { it.isNotEmpty() },
            nickname = nickname?.trim()?.takeIf { it.isNotEmpty() },
            profileImageFileId = profileImageFileId,
        )
    )

    suspend fun withdraw(password: String, reason: String?) {
        userApi.withdraw(
            UserWithdrawRequest(
                password = password,
                reason = reason?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
    }
}
