package kyung.kung_android.data.auth.api

import kyung.kung_android.data.auth.dto.LoginRequest
import kyung.kung_android.data.auth.dto.LoginResponse
import kyung.kung_android.data.auth.dto.PasswordChangeRequest
import kyung.kung_android.data.auth.dto.ReissueRequest
import kyung.kung_android.data.auth.dto.ReissueResponse
import kyung.kung_android.data.auth.dto.SignupRequest
import kyung.kung_android.data.auth.dto.SignupResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 비인증 호출 (NoAuthClient): signup, login, reissue
 */
interface AuthApi {

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/reissue")
    suspend fun reissue(@Body request: ReissueRequest): ReissueResponse
}

/**
 * 인증 호출 (AuthClient): logout
 */
interface AuthenticatedAuthApi {

    @POST("api/auth/logout")
    suspend fun logout(): Unit?

    @POST("api/auth/password/change")
    suspend fun changePassword(@Body request: PasswordChangeRequest): Unit?
}
