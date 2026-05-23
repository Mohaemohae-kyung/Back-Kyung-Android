package kyung.kung_android.data.request.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.request.api.ServiceRequestApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceRequestApiModule {

    @Provides
    @Singleton
    fun provideServiceRequestApi(@AuthClient retrofit: Retrofit): ServiceRequestApi =
        retrofit.create(ServiceRequestApi::class.java)
}
