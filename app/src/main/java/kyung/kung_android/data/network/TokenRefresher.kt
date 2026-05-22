package kyung.kung_android.data.network

/**
 * 만료된 access 토큰을 갱신하는 역할.
 * 실제 구현은 인증 도메인 모듈에서 NoAuthClient 기반 Retrofit으로 reissue API를 호출.
 * 인증 도메인이 아직 없는 단계에선 [NoOpTokenRefresher]를 바인딩해 빌드만 통과시킴.
 */
interface TokenRefresher {
    /**
     * 갱신을 시도한다.
     * @return 새 access 토큰 (성공) 또는 null (실패)
     */
    suspend fun refresh(): String?
}

class NoOpTokenRefresher @javax.inject.Inject constructor() : TokenRefresher {
    override suspend fun refresh(): String? = null
}
