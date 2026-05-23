package kyung.kung_android.data.user.api

import kyung.kung_android.data.user.dto.UserProfileResponse
import kyung.kung_android.data.user.dto.UserProfileUpdateRequest
import kyung.kung_android.data.user.dto.UserWithdrawRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH

interface UserApi {

    @GET("/api/users/me")
    suspend fun getMe(): UserProfileResponse

    @PATCH("/api/users/me")
    suspend fun updateMyProfile(
        @Body body: UserProfileUpdateRequest,
    ): UserProfileResponse

    @HTTP(method = "DELETE", path = "/api/users/me", hasBody = true)
    suspend fun withdraw(
        @Body body: UserWithdrawRequest,
    )
}
