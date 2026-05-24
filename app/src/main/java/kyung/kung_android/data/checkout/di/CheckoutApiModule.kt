package kyung.kung_android.data.checkout.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.checkout.api.CheckoutApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CheckoutApiModule {

    @Provides
    @Singleton
    fun provideCheckoutApi(@AuthClient retrofit: Retrofit): CheckoutApi =
        retrofit.create(CheckoutApi::class.java)
}
