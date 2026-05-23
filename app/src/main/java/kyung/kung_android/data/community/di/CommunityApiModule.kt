package kyung.kung_android.data.community.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.community.api.CommunityApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityApiModule {

    @Provides
    @Singleton
    fun provideCommunityApi(@AuthClient retrofit: Retrofit): CommunityApi =
        retrofit.create(CommunityApi::class.java)
}
