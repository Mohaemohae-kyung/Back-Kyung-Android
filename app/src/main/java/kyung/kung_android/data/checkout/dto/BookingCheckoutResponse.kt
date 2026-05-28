package kyung.kung_android.data.checkout.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class BookingCheckoutResponse(
    val bookingId: Long,
    val storeProductId: Long? = null,
    val productTitle: String? = null,
    val expertDisplayName: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endAt: LocalDateTime? = null,
    val locationId: Long? = null,
    val locationName: String? = null,
    val locationText: String? = null,
    val bookingStatus: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val paymentExpiresAt: LocalDateTime? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val baseAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val discountAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val finalAmount: BigDecimal? = null,
)
