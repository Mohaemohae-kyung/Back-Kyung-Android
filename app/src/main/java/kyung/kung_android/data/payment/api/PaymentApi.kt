package kyung.kung_android.data.payment.api

import kyung.kung_android.data.network.RawResponse
import kyung.kung_android.data.payment.dto.E2ePayloadRequest
import kyung.kung_android.data.payment.dto.E2ePayloadResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import kyung.kung_android.data.payment.dto.PublicKeyResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {

    @GET("/api/payments/me")
    suspend fun getMyPayments(): List<PaymentResponse>

    @GET("/api/payments/{paymentId}")
    suspend fun getPayment(@Path("paymentId") paymentId: Long): PaymentResponse

    @RawResponse
    @GET("/api/payments/public-key")
    suspend fun getPublicKey(): PublicKeyResponse

    @RawResponse
    @POST("/api/payments/prepare")
    suspend fun prepare(@Body request: E2ePayloadRequest): E2ePayloadResponse

    @RawResponse
    @POST("/api/payments/confirm")
    suspend fun confirm(@Body request: E2ePayloadRequest): E2ePayloadResponse
}
