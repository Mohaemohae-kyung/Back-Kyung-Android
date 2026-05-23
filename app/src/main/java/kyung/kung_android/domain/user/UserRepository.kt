package kyung.kung_android.domain.user

import kyung.kung_android.data.user.api.UserApi
import kyung.kung_android.data.user.dto.UserProfileResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
) {

    suspend fun getMe(): UserProfileResponse = userApi.getMe()
}
