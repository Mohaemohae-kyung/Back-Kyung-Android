package kyung.kung_android.data.payment.api

import kyung.kung_android.data.payment.dto.PaymentResponse
import retrofit2.http.GET

interface PaymentApi {

    @GET("/api/payments/me")
    suspend fun getMyPayments(): List<PaymentResponse>
}
