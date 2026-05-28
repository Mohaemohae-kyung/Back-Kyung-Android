package kyung.kung_android.domain.booking

import kyung.kung_android.data.booking.api.BookingApi
import kyung.kung_android.data.booking.dto.BookingAvailabilityResponse
import kyung.kung_android.data.booking.dto.BookingPrepareRequest
import kyung.kung_android.data.booking.dto.BookingResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val bookingApi: BookingApi,
) {

    suspend fun checkAvailability(
        storeProductId: Long,
        startAt: LocalDateTime,
        endAt: LocalDateTime,
        locationId: Long? = null,
    ): BookingAvailabilityResponse =
        bookingApi.checkAvailability(
            storeProductId = storeProductId,
            startAt = startAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endAt = endAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            locationId = locationId,
        )

    suspend fun prepareBooking(
        storeProductId: Long,
        startAt: LocalDateTime,
        endAt: LocalDateTime,
        locationId: Long? = null,
        locationText: String? = null,
    ): BookingResponse =
        bookingApi.prepare(
            BookingPrepareRequest(
                storeProductId = storeProductId,
                startAt = startAt,
                endAt = endAt,
                locationId = locationId,
                locationText = locationText,
            )
        )

    suspend fun getBooking(bookingId: Long): BookingResponse =
        bookingApi.getBooking(bookingId)
}
