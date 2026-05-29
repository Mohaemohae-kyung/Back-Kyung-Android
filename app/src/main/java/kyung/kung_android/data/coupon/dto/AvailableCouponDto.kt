package kyung.kung_android.data.coupon.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class AvailableCouponDto(
    val userCouponId: Long,
    val name: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val discountAmount: BigDecimal,
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiredAt: LocalDateTime? = null,
)
