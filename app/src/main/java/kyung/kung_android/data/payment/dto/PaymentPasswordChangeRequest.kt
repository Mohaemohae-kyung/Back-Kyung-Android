package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

/**
 * 결제 비밀번호 변경용 평문 페이로드. E2E 암호화되어 전달된다.
 * 현재 비밀번호 확인 후 새 비밀번호로 교체한다.
 */
@Serializable
data class PaymentPasswordChangeRequest(
    val userId: Long,
    val currentPin: String,
    val newPin: String,
)
