package kyung.kung_android.data.booking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.data.booking.api.BookingApi
import kyung.kung_android.data.network.di.AuthClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookingApiModule {

    @Provides
    @Singleton
    fun provideBookingApi(@AuthClient retrofit: Retrofit): BookingApi =
        retrofit.create(BookingApi::class.java)
}
