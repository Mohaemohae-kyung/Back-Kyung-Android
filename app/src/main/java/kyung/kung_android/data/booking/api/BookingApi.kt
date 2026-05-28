package kyung.kung_android.data.booking.api

import kyung.kung_android.data.booking.dto.BookingAvailabilityResponse
import kyung.kung_android.data.booking.dto.BookingPrepareRequest
import kyung.kung_android.data.booking.dto.BookingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApi {

    @POST("/api/bookings/prepare")
    suspend fun prepare(@Body request: BookingPrepareRequest): BookingResponse

    @GET("/api/bookings/availability")
    suspend fun checkAvailability(
        @Query("storeProductId") storeProductId: Long,
        @Query("startAt") startAt: String,
        @Query("endAt") endAt: String,
        @Query("locationId") locationId: Long? = null,
    ): BookingAvailabilityResponse

    @GET("/api/bookings/{bookingId}")
    suspend fun getBooking(@Path("bookingId") bookingId: Long): BookingResponse
}
