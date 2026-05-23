package kyung.kung_android.data.expert.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.expert.api.ExpertApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpertApiModule {

    @Provides
    @Singleton
    fun provideExpertApi(@AuthClient retrofit: Retrofit): ExpertApi =
        retrofit.create(ExpertApi::class.java)
}
