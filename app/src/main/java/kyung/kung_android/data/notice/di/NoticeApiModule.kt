package kyung.kung_android.data.notice.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.notice.api.NoticeApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoticeApiModule {

    @Provides
    @Singleton
    fun provideNoticeApi(@AuthClient retrofit: Retrofit): NoticeApi =
        retrofit.create(NoticeApi::class.java)
}
