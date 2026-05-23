package kyung.kung_android.domain.payment

import kyung.kung_android.data.payment.api.PaymentApi
import kyung.kung_android.data.payment.dto.PaymentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentApi: PaymentApi,
) {

    suspend fun getMyPayments(): List<PaymentResponse> = paymentApi.getMyPayments()
}
