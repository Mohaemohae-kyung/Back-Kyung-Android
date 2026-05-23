package kyung.kung_android.data.favorite.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.favorite.api.MyPageApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyPageApiModule {

    @Provides
    @Singleton
    fun provideMyPageApi(@AuthClient retrofit: Retrofit): MyPageApi =
        retrofit.create(MyPageApi::class.java)
}
