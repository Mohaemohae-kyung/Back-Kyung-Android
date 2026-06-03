package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

/**
 * 결제 전담 서버로 E2E 암호화되어 전달되는 평문 페이로드.
 * userId 는 암호문 내부에 포함되어야 한다(서버가 복호화 후 plainData.userId 사용).
 */
@Serializable
data class PaymentPasswordSetupRequest(
    val userId: Long,
    val paymentPin: String,
)
