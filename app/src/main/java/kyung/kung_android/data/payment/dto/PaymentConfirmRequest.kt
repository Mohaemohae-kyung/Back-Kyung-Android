package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class PaymentConfirmRequest(
    val orderId: String,
    val paymentKey: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
)
