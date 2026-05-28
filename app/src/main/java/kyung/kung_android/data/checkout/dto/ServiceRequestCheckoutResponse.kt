package kyung.kung_android.data.checkout.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class ServiceRequestCheckoutResponse(
    val requestId: Long,
    val expertProfileId: Long? = null,
    val expertDisplayName: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val requestTitle: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val preferredDate: LocalDateTime? = null,
    val requestStatus: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val baseAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val discountAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val finalAmount: BigDecimal? = null,
)
