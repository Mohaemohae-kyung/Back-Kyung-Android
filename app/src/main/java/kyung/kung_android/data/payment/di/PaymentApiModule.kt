package kyung.kung_android.data.payment.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.payment.api.MockPgApi
import kyung.kung_android.data.payment.api.PaymentApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentApiModule {

    @Provides
    @Singleton
    fun providePaymentApi(@AuthClient retrofit: Retrofit): PaymentApi =
        retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideMockPgApi(@AuthClient retrofit: Retrofit): MockPgApi =
        retrofit.create(MockPgApi::class.java)
}
