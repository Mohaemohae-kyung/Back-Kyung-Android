package kyung.kung_android.domain.payment

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kyung.kung_android.data.payment.api.PaymentApi
import kyung.kung_android.data.payment.crypto.E2eCryptoUtil
import kyung.kung_android.data.payment.dto.E2ePayloadRequest
import kyung.kung_android.data.payment.dto.E2ePayloadResponse
import kyung.kung_android.data.payment.dto.PaymentConfirmRequest
import kyung.kung_android.data.payment.dto.PaymentPasswordSetupRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import kyung.kung_android.data.payment.dto.ServiceRequestPaymentRequestCreateRequest
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

    /**
     * 고수/전문가가 고객에게 결제 요청 메시지를 생성한다.
     * 실제 결제 비밀번호 검증·Toss 준비는 고객이 결제할 때(prepare) 수행한다.
     */
    suspend fun requestServiceRequestPayment(
        requestId: Long,
        paymentMethod: String = "CARD",
    ): PaymentResponse =
        paymentApi.createServiceRequestPaymentRequest(
            requestId = requestId,
            request = ServiceRequestPaymentRequestCreateRequest(
                paymentMethod = paymentMethod,
                pgProvider = PG_PROVIDER_TOSS,
            ),
        )

    suspend fun prepareForServiceRequest(
        requestId: Long,
        paymentMethod: String = "CARD",
        paymentPin: String? = null,
        userCouponId: Long? = null,
    ): PaymentPrepareResponse =
        e2eCall(
            plain = PaymentPrepareRequest(
                targetType = "SERVICE_REQUEST",
                targetId = requestId,
                paymentMethod = paymentMethod,
                pgProvider = PG_PROVIDER_TOSS,
                paymentPin = paymentPin,
                userCouponId = userCouponId,
            ),
            requestSerializer = PaymentPrepareRequest.serializer(),
            responseSerializer = PaymentPrepareResponse.serializer(),
            apiCall = { paymentApi.prepare(it) },
        )

    suspend fun prepareForBooking(
        bookingId: Long,
        paymentMethod: String = "CARD",
        paymentPin: String? = null,
        userCouponId: Long? = null,
    ): PaymentPrepareResponse =
        e2eCall(
            plain = PaymentPrepareRequest(
                targetType = "BOOKING",
                targetId = bookingId,
                paymentMethod = paymentMethod,
                pgProvider = PG_PROVIDER_TOSS,
                paymentPin = paymentPin,
                userCouponId = userCouponId,
            ),
            requestSerializer = PaymentPrepareRequest.serializer(),
            responseSerializer = PaymentPrepareResponse.serializer(),
            apiCall = { paymentApi.prepare(it) },
        )

    /**
     * 결제 비밀번호 설정. 평문 {userId, paymentPin} 을 E2E 암호화해 전담 서버로 전달한다.
     * 전담 서버 응답은 평문 {success, message} 이므로 복호화 과정이 없다.
     */
    suspend fun setupPaymentPassword(userId: Long, paymentPin: String) {
        val session = cryptoUtil.newSession()
        val plainJson = json.encodeToString(
            PaymentPasswordSetupRequest.serializer(),
            PaymentPasswordSetupRequest(userId = userId, paymentPin = paymentPin),
        )
        val encrypted = cryptoUtil.encryptPayload(plainJson, publicKey(), session)
        val response = try {
            paymentApi.setupPaymentPassword(encrypted)
        } catch (e: Exception) {
            cachedPublicKeyPem = null
            throw e
        }
        if (!response.success) {
            throw E2eDecryptException(response.message ?: "결제 비밀번호 설정에 실패했어요.")
        }
    }

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
