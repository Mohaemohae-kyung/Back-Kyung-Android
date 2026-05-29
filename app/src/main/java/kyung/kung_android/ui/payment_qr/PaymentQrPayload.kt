package kyung.kung_android.ui.payment_qr

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PaymentQrPayload(
    val v: Int = 1,
    val rid: Long,
    val amt: String,
    val exp: Long,
) {
    fun encode(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun decode(text: String): PaymentQrPayload =
            Json.decodeFromString(serializer(), text)
    }
}
