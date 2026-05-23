package kyung.kung_android.data.chat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.chat.api.ChatApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatApiModule {

    @Provides
    @Singleton
    fun provideChatApi(@AuthClient retrofit: Retrofit): ChatApi =
        retrofit.create(ChatApi::class.java)
}
