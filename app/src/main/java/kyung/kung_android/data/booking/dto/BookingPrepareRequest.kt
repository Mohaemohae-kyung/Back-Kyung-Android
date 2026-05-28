package kyung.kung_android.data.booking.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class BookingPrepareRequest(
    val storeProductId: Long,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endAt: LocalDateTime,
    val locationId: Long? = null,
    val locationText: String? = null,
)
