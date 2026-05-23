package kyung.kung_android.data.user.api

import kyung.kung_android.data.user.dto.UserProfileResponse
import retrofit2.http.GET

interface UserApi {

    @GET("/api/users/me")
    suspend fun getMe(): UserProfileResponse
}
