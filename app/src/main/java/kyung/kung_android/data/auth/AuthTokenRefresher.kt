package kyung.kung_android.data.auth

import kotlinx.coroutines.CancellationException
import kyung.kung_android.data.auth.api.AuthApi
import kyung.kung_android.data.auth.dto.ReissueRequest
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.data.network.TokenRefresher
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenRefresher @Inject constructor(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
) : TokenRefresher {

    override suspend fun refresh(): String? {
        val current = tokenStore.getRefresh() ?: return null
        return try {
            val res = authApi.reissue(ReissueRequest(refreshToken = current))
            tokenStore.saveTokens(access = res.accessToken, refresh = res.refreshToken)
            res.accessToken
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            if (e.isAuthError) {
                tokenStore.clear()
            }
            null
        } catch (e: IOException) {
            null
        }
    }
}
