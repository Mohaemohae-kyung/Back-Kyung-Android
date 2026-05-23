package kyung.kung_android.data.file.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.file.api.FileApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileApiModule {

    @Provides
    @Singleton
    fun provideFileApi(@AuthClient retrofit: Retrofit): FileApi =
        retrofit.create(FileApi::class.java)
}
