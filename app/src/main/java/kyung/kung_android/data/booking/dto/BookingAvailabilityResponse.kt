package kyung.kung_android.data.booking.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookingAvailabilityResponse(
    val available: Boolean = false,
    val reason: String? = null,
    val message: String? = null,
)
