package kyung.kung_android.data.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.NoOpTokenRefresher
import kyung.kung_android.data.network.TokenRefresher
import javax.inject.Singleton

/**
 * 인증 도메인이 구현되기 전까지 NoOp 구현을 바인딩한다.
 * 실제 reissue 호출 구현이 생기면 이 모듈을 제거하고 도메인 모듈에서 바인딩.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTokenRefresher(impl: NoOpTokenRefresher): TokenRefresher
}
