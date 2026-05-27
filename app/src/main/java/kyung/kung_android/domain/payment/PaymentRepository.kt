package kyung.kung_android.domain.payment

import kyung.kung_android.data.payment.api.MockPgApi
import kyung.kung_android.data.payment.api.PaymentApi
import kyung.kung_android.data.payment.dto.MockPgApproveRequest
import kyung.kung_android.data.payment.dto.MockPgApproveResponse
import kyung.kung_android.data.payment.dto.PaymentConfirmRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentApi: PaymentApi,
    private val mockPgApi: MockPgApi,
) {

    suspend fun getMyPayments(): List<PaymentResponse> = paymentApi.getMyPayments()

    suspend fun getPayment(paymentId: Long): PaymentResponse = paymentApi.getPayment(paymentId)

    suspend fun prepareForServiceRequest(
        requestId: Long,
        paymentMethod: String = "CARD",
    ): PaymentPrepareResponse =
        paymentApi.prepare(
            PaymentPrepareRequest(
                targetType = "SERVICE_REQUEST",
                targetId = requestId,
                paymentMethod = paymentMethod,
            )
        )

    suspend fun prepareForBooking(
        bookingId: Long,
        paymentMethod: String = "CARD",
    ): PaymentPrepareResponse =
        paymentApi.prepare(
            PaymentPrepareRequest(
                targetType = "BOOKING",
                targetId = bookingId,
                paymentMethod = paymentMethod,
            )
        )

    suspend fun approveMockPg(
        orderId: String,
        amount: BigDecimal,
        paymentMethod: String = "CARD",
    ): MockPgApproveResponse =
        mockPgApi.approve(MockPgApproveRequest(orderId, amount, paymentMethod))

    suspend fun confirm(
        orderId: String,
        paymentKey: String,
        amount: BigDecimal,
    ): PaymentResponse =
        paymentApi.confirm(PaymentConfirmRequest(orderId, paymentKey, amount))
}
