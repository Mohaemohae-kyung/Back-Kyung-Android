package kyung.kung_android.data.network

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kyung.kung_android.data.auth.TokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val refresher: TokenRefresher,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 무한 재시도 방지: 이미 한 번 재시도한 요청이면 포기
        if (responseRetryCount(response) >= MAX_RETRIES) return null

        // 토큰 만료가 아닌 도메인 401(결제 비밀번호 불일치 등)은 갱신/재시도 대상이 아니다.
        // 재시도하면 서버 측 실패 카운트가 한 번에 2씩 오르므로 여기서 차단한다.
        if (isDomainAuthError(response)) return null

        return runBlocking {
            mutex.withLock {
                val attached = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")?.trim()
                val currentAccess = tokenStore.getAccessSync()

                // 이미 다른 요청이 갱신을 끝낸 상태면 새 토큰으로 즉시 재시도
                if (!currentAccess.isNullOrBlank() && currentAccess != attached) {
                    return@withLock response.request.attachBearer(currentAccess)
                }

                // 갱신 시도
                val newAccess = refresher.refresh()
                if (newAccess.isNullOrBlank()) {
                    // 갱신 실패 → 더 이상 재시도 안 함, 원본 401이 그대로 전파됨
                    return@withLock null
                }
                response.request.attachBearer(newAccess)
            }
        }
    }

    /**
     * 401 응답 본문을 소비하지 않고(peek) 들여다봐, 토큰 만료가 아닌
     * 결제 도메인 오류 코드가 담겨 있으면 갱신 흐름에서 제외한다.
     */
    private fun isDomainAuthError(response: Response): Boolean = try {
        val body = response.peekBody(PEEK_LIMIT).string()
        DOMAIN_AUTH_CODES.any { body.contains("\"$it\"") }
    } catch (t: Throwable) {
        false
    }

    private fun responseRetryCount(response: Response): Int {
        var count = 0
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private fun Request.attachBearer(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    companion object {
        private const val MAX_RETRIES = 1
        private const val PEEK_LIMIT = 2048L
        private val DOMAIN_AUTH_CODES = listOf("INVALID_PASSWORD")
    }
}
