package kyung.kung_android.data.expert_service.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.expert_service.api.ExpertServiceApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpertServiceApiModule {

    @Provides
    @Singleton
    fun provideExpertServiceApi(@AuthClient retrofit: Retrofit): ExpertServiceApi =
        retrofit.create(ExpertServiceApi::class.java)
}
