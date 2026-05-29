package kyung.kung_android.data.payment.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.network.di.AuthClient
import kyung.kung_android.data.payment.api.PaymentApi
import kyung.kung_android.data.payment.crypto.E2eCryptoUtil
import kyung.kung_android.data.payment.crypto.E2eCryptoUtilImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentApiModule {

    @Provides
    @Singleton
    fun providePaymentApi(@AuthClient retrofit: Retrofit): PaymentApi =
        retrofit.create(PaymentApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentCryptoModule {

    @Binds
    @Singleton
    abstract fun bindE2eCryptoUtil(impl: E2eCryptoUtilImpl): E2eCryptoUtil
}
