package kyung.kung_android.data.coupon.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class AvailableCouponDto(
    val userCouponId: Long,
    val name: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val discountAmount: BigDecimal? = null,
    val expiredAt: String? = null,
)
