package kyung.kung_android.data.store.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.store.api.StoreApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StoreApiModule {

    @Provides
    @Singleton
    fun provideStoreApi(@AuthClient retrofit: Retrofit): StoreApi =
        retrofit.create(StoreApi::class.java)
}
