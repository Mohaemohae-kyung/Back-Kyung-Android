package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaymentPrepareRequest(
    val targetType: String,
    val targetId: Long,
    val paymentMethod: String,
    val userCouponId: Long? = null,
    val pgProvider: String? = null,
    val paymentPin: String? = null,
)
