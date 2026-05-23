package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class PaymentResponse(
    val paymentId: Long,
    val transactionId: Long? = null,
    val bookingId: Long? = null,
    val serviceRequestId: Long? = null,
    val orderId: String? = null,
    val transactionType: String? = null,
    val paymentMethod: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val discountAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val finalAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val paymentAmount: BigDecimal? = null,
    val transactionStatus: String? = null,
    val paymentStatus: String? = null,
    val pgProvider: String? = null,
    val pgPaymentKey: String? = null,
    val paidAt: String? = null,
    val cancelledAt: String? = null,
    val failedReason: String? = null,
    val createdAt: String? = null,
)
