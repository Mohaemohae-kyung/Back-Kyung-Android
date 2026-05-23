package kyung.kung_android.domain.auth

import kotlinx.coroutines.flow.StateFlow
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.data.auth.api.AuthApi
import kyung.kung_android.data.auth.api.AuthenticatedAuthApi
import kyung.kung_android.data.auth.dto.LoginRequest
import kyung.kung_android.data.auth.dto.LoginResponse
import kyung.kung_android.data.auth.dto.SignupRequest
import kyung.kung_android.data.auth.dto.SignupResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authenticatedAuthApi: AuthenticatedAuthApi,
    private val tokenStore: TokenStore,
) {

    suspend fun signup(
        email: String,
        password: String,
        name: String,
        nickname: String?,
        phone: String?,
    ): SignupResponse {
        return authApi.signup(
            SignupRequest(
                email = email.trim().lowercase(),
                password = password,
                name = name.trim(),
                nickname = nickname?.trim()?.takeIf { it.isNotEmpty() },
                phone = phone?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val res = authApi.login(
            LoginRequest(
                email = email.trim().lowercase(),
                password = password,
            )
        )
        tokenStore.saveTokens(access = res.accessToken, refresh = res.refreshToken)
        return res
    }

    suspend fun logout(): Result<Unit> {
        val serverResult = runCatching { authenticatedAuthApi.logout() }.map { }
        tokenStore.clear()
        return serverResult
    }

    val isLoggedIn: StateFlow<Boolean> = tokenStore.isLoggedIn
}
