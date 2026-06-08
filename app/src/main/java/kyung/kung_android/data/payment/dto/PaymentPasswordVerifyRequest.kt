package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

/**
 * 현재 결제 비밀번호 확인용 평문 페이로드. E2E 암호화되어 전달된다.
 * userId 는 암호문 내부에 포함한다(서버가 복호화 후 사용).
 */
@Serializable
data class PaymentPasswordVerifyRequest(
    val userId: Long,
    val currentPin: String,
)
