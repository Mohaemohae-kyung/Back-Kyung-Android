package kyung.kung_android.data.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.auth.AuthTokenRefresher
import kyung.kung_android.data.network.TokenRefresher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTokenRefresher(impl: AuthTokenRefresher): TokenRefresher
}
