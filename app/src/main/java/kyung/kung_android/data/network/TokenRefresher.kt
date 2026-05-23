package kyung.kung_android.data.network

/**
 * 만료된 access 토큰 갱신 책임.
 * 구현: data.auth.AuthTokenRefresher
 */
interface TokenRefresher {
    /**
     * 갱신 시도.
     * @return 새 access 토큰 (성공) 또는 null (실패 — 호출자가 강제 로그아웃 처리)
     */
    suspend fun refresh(): String?
}
