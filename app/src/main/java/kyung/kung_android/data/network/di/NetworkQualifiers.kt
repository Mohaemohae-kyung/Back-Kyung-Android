package kyung.kung_android.data.network.di

import javax.inject.Qualifier

/** 인증 헤더 부착 + 401 자동 갱신이 적용된 일반 API용 클라이언트 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient

/** login/signup/reissue 등 비인증 API용 클라이언트 (Interceptor/Authenticator 미적용) */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoAuthClient
