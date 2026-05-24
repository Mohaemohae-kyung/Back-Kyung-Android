package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class MockPgApproveRequest(
    val orderId: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val paymentMethod: String,
)
