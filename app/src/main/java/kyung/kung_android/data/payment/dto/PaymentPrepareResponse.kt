package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class PaymentPrepareResponse(
    val paymentId: Long,
    val transactionId: Long? = null,
    val bookingId: Long? = null,
    val serviceRequestId: Long? = null,
    val orderId: String,
    val orderName: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val discountAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val finalAmount: BigDecimal,
    val paymentStatus: String? = null,
)
