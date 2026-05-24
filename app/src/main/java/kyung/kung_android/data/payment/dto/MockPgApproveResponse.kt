package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class MockPgApproveResponse(
    val mockPgPaymentId: Long? = null,
    val orderId: String,
    val paymentKey: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val paymentMethod: String? = null,
    val status: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val approvedAt: LocalDateTime? = null,
)
