package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

/**
 * 고수/전문가가 고객에게 결제 요청 메시지를 생성할 때 보내는 요청.
 * 실제 결제 비밀번호 검증과 Toss 결제 준비는 고객이 결제할 때 수행한다.
 */
@Serializable
data class ServiceRequestPaymentRequestCreateRequest(
    val paymentMethod: String,
    val pgProvider: String,
)
