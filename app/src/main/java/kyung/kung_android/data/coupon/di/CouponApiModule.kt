package kyung.kung_android.data.coupon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.coupon.api.CouponApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CouponApiModule {

    @Provides
    @Singleton
    fun provideCouponApi(@AuthClient retrofit: Retrofit): CouponApi =
        retrofit.create(CouponApi::class.java)
}
