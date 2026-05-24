package kyung.kung_android.data.payment.api

import kyung.kung_android.data.payment.dto.PaymentConfirmRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {

    @GET("/api/payments/me")
    suspend fun getMyPayments(): List<PaymentResponse>

    @GET("/api/payments/{paymentId}")
    suspend fun getPayment(@Path("paymentId") paymentId: Long): PaymentResponse

    @POST("/api/payments/prepare")
    suspend fun prepare(@Body request: PaymentPrepareRequest): PaymentPrepareResponse

    @POST("/api/payments/confirm")
    suspend fun confirm(@Body request: PaymentConfirmRequest): PaymentResponse
}
