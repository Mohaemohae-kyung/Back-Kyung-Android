package kyung.kung_android.data.auth.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.auth.api.AuthApi
import kyung.kung_android.data.auth.api.AuthenticatedAuthApi
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.network.di.NoAuthClient
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(@NoAuthClient retrofit: Retrofit): AuthApi =
        retrofit.create()

    @Provides
    @Singleton
    fun provideAuthenticatedAuthApi(@AuthClient retrofit: Retrofit): AuthenticatedAuthApi =
        retrofit.create()
}
