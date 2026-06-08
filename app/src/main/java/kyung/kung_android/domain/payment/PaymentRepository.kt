package kyung.kung_android.domain.payment

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kyung.kung_android.data.payment.api.PaymentApi
import kyung.kung_android.data.payment.crypto.E2eCryptoUtil
import kyung.kung_android.data.payment.dto.E2ePayloadRequest
import kyung.kung_android.data.payment.dto.E2ePayloadResponse
import kyung.kung_android.data.payment.dto.PaymentConfirmRequest
import kyung.kung_android.data.payment.dto.PaymentPasswordChangeRequest
import kyung.kung_android.data.payment.dto.PaymentPasswordSetupRequest
import kyung.kung_android.data.payment.dto.PaymentPasswordVerifyRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareRequest
import kyung.kung_android.data.payment.dto.PaymentPrepareResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import kyung.kung_android.data.payment.dto.ServiceRequestPaymentRequestCreateRequest
import retrofit2.HttpException
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
            throw e.asPaymentException()
        }
        if (!response.success) {
            throw E2eDecryptException(response.message ?: "결제 비밀번호 설정에 실패했어요.")
        }
    }

    /** 현재 결제 비밀번호 확인. 불일치 시 [PaymentException] 발생. */
    suspend fun verifyPaymentPassword(userId: Long, currentPin: String) {
        val session = cryptoUtil.newSession()
        val plainJson = json.encodeToString(
            PaymentPasswordVerifyRequest.serializer(),
            PaymentPasswordVerifyRequest(userId = userId, currentPin = currentPin),
        )
        val encrypted = cryptoUtil.encryptPayload(plainJson, publicKey(), session)
        val response = try {
            paymentApi.verifyPaymentPassword(encrypted)
        } catch (e: Exception) {
            cachedPublicKeyPem = null
            throw e.asPaymentException()
        }
        if (!response.success) {
            throw PaymentException(0, response.message ?: "결제 비밀번호가 일치하지 않아요.")
        }
    }

    /** 현재 비밀번호 확인 후 새 비밀번호로 변경. 실패 시 [PaymentException] 발생. */
    suspend fun changePaymentPassword(userId: Long, currentPin: String, newPin: String) {
        val session = cryptoUtil.newSession()
        val plainJson = json.encodeToString(
            PaymentPasswordChangeRequest.serializer(),
            PaymentPasswordChangeRequest(userId = userId, currentPin = currentPin, newPin = newPin),
        )
        val encrypted = cryptoUtil.encryptPayload(plainJson, publicKey(), session)
        val response = try {
            paymentApi.changePaymentPassword(encrypted)
        } catch (e: Exception) {
            cachedPublicKeyPem = null
            throw e.asPaymentException()
        }
        if (!response.success) {
            throw PaymentException(0, response.message ?: "결제 비밀번호 변경에 실패했어요.")
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

    /**
     * 전담 서버의 오류 응답(non-2xx)은 Retrofit이 [HttpException] 으로 던지면서 본문을 버린다.
     * 본문의 {code, message} 를 살려 [PaymentException] 으로 변환한다. 그 외 예외는 그대로 둔다.
     */
    private fun Throwable.asPaymentException(): Throwable {
        if (this !is HttpException) return this
        val status = code()
        val raw = runCatching { response()?.errorBody()?.string() }.getOrNull()

        // 본문에서 code/message 추출. 루트 또는 result 안에 중첩된 경우 모두 대응.
        var code: String? = null
        var message: String? = null
        if (!raw.isNullOrBlank()) {
            runCatching {
                val obj = json.parseToJsonElement(raw).jsonObject
                val nested = obj["result"] as? JsonObject
                code = obj["code"]?.jsonPrimitive?.contentOrNull
                    ?: nested?.get("code")?.jsonPrimitive?.contentOrNull
                message = obj["message"]?.jsonPrimitive?.contentOrNull
                    ?: nested?.get("message")?.jsonPrimitive?.contentOrNull
            }
        }

        // 결제 흐름에서 HTTP 401은 비밀번호 불일치뿐이다. 본문이 비어/파싱 실패해도 그렇게 처리.
        if (code == null && status == 401) code = PaymentException.CODE_INVALID_PASSWORD

        val finalMessage = message ?: when (code) {
            PaymentException.CODE_INVALID_PASSWORD -> "결제 비밀번호가 올바르지 않아요. 다시 입력해주세요."
            PaymentException.CODE_ACCOUNT_LOCKED -> "결제 비밀번호 오류가 누적되어 계정이 정지되었어요."
            PaymentException.CODE_PASSWORD_NOT_SET -> "결제 비밀번호 설정이 필요해요."
            else -> "결제 처리 중 오류가 발생했어요. ($status)"
        }
        return PaymentException(status, finalMessage, code)
    }

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
            throw e.asPaymentException()
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
