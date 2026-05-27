package kyung.kung_android.data.booking.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class BookingResponse(
    val bookingId: Long,
    val userId: Long? = null,
    val storeProductId: Long? = null,
    val expertServiceId: Long? = null,
    val expertProfileId: Long? = null,
    val productTitle: String? = null,
    val serviceTitle: String? = null,
    val expertDisplayName: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endAt: LocalDateTime? = null,
    val locationId: Long? = null,
    val locationName: String? = null,
    val locationText: String? = null,
    val status: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val paymentExpiresAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null,
)
