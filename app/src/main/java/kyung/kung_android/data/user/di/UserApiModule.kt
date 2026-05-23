package kyung.kung_android.data.user.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.user.api.UserApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserApiModule {

    @Provides
    @Singleton
    fun provideUserApi(@AuthClient retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)
}
