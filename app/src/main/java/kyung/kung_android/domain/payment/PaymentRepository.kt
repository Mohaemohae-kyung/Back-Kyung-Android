package kyung.kung_android.domain.payment

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kyung.kung_android.data.payment.api.PaymentApi
import kyung.kung_android.data.payment.crypto.E2eCryptoUtil
import kyung.kung_android.data.payment.dto.E2ePayloadRequest
import kyung.kung_android.data.payment.dto.E2ePayloadResponse
import kyung.kung_android.data.payment.dto.PaymentConfirmRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import java.math.BigDecimal
import java.security.PublicKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentApi: PaymentApi,
    private val cryptoUtil: E2eCryptoUtil,
    private val json: Json,
) {

    @Volatile
    private var cachedPublicKeyPem: String? = null

    suspend fun getMyPayments(): List<PaymentResponse> = paymentApi.getMyPayments()

    suspend fun getPayment(paymentId: Long): PaymentResponse = paymentApi.getPayment(paymentId)

    suspend fun prepareForServiceRequest(
        requestId: Long,
        paymentMethod: String = "CARD",
    ): PaymentPrepareResponse =
        e2eCall(
            plain = PaymentPrepareRequest(
                targetType = "SERVICE_REQUEST",
                targetId = requestId,
                paymentMethod = paymentMethod,
                pgProvider = PG_PROVIDER_TOSS,
            ),
            requestSerializer = PaymentPrepareRequest.serializer(),
            responseSerializer = PaymentPrepareResponse.serializer(),
            apiCall = { paymentApi.prepare(it) },
        )

    suspend fun prepareForBooking(
        bookingId: Long,
        paymentMethod: String = "CARD",
    ): PaymentPrepareResponse =
        e2eCall(
            plain = PaymentPrepareRequest(
                targetType = "BOOKING",
                targetId = bookingId,
                paymentMethod = paymentMethod,
                pgProvider = PG_PROVIDER_TOSS,
            ),
            requestSerializer = PaymentPrepareRequest.serializer(),
            responseSerializer = PaymentPrepareResponse.serializer(),
            apiCall = { paymentApi.prepare(it) },
        )

    suspend fun confirm(
        orderId: String,
        paymentKey: String,
        amount: BigDecimal,
    ): PaymentResponse =
        e2eCall(
            plain = PaymentConfirmRequest(orderId, paymentKey, amount),
            requestSerializer = PaymentConfirmRequest.serializer(),
            responseSerializer = PaymentResponse.serializer(),
            apiCall = { paymentApi.confirm(it) },
        )

    private suspend fun publicKey(): PublicKey {
        val pem = cachedPublicKeyPem
            ?: paymentApi.getPublicKey().publicKey.also { cachedPublicKeyPem = it }
        return cryptoUtil.parsePublicKeyPem(pem)
    }

    private suspend fun <REQ, RES> e2eCall(
        plain: REQ,
        requestSerializer: KSerializer<REQ>,
        responseSerializer: KSerializer<RES>,
        apiCall: suspend (E2ePayloadRequest) -> E2ePayloadResponse,
    ): RES {
        val session = cryptoUtil.newSession()
        val plainJson = json.encodeToString(requestSerializer, plain)
        val encrypted = cryptoUtil.encryptPayload(plainJson, publicKey(), session)

        val response: E2ePayloadResponse = try {
            apiCall(encrypted)
        } catch (e: Exception) {
            cachedPublicKeyPem = null
            throw e
        }

        val cipherText = response.cipherText
        if (!response.success || cipherText.isNullOrEmpty()) {
            throw E2eDecryptException(response.message ?: "E2E response missing cipherText")
        }

        val plainResponseJson = try {
            cryptoUtil.decryptResponse(cipherText, session)
        } catch (e: Exception) {
            throw E2eDecryptException("E2E response decrypt failed: ${e.message}")
        }

        return json.decodeFromString(responseSerializer, plainResponseJson)
    }

    companion object {
        private const val PG_PROVIDER_TOSS = "TOSS_PAYMENTS"
    }
}
