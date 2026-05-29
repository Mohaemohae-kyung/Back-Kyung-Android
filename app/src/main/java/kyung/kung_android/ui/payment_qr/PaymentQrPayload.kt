package kyung.kung_android.ui.payment_qr

import android.net.Uri

data class PaymentQrPayload(
    val v: Int = 1,
    val rid: Long,
    val amt: String,
    val exp: Long,
) {
    fun encode(): String =
        Uri.Builder()
            .scheme("matchingon")
            .authority("pay")
            .appendQueryParameter("v", v.toString())
            .appendQueryParameter("rid", rid.toString())
            .appendQueryParameter("amt", amt)
            .appendQueryParameter("exp", exp.toString())
            .build()
            .toString()

    companion object {
        fun decode(text: String): PaymentQrPayload {
            val uri = Uri.parse(text)
            require(uri.scheme == "matchingon" && uri.host == "pay") {
                "Not a matchingon pay deeplink"
            }
            val rid = uri.getQueryParameter("rid")?.toLongOrNull()
                ?: error("missing rid")
            val amt = uri.getQueryParameter("amt") ?: error("missing amt")
            val exp = uri.getQueryParameter("exp")?.toLongOrNull() ?: 0L
            val v = uri.getQueryParameter("v")?.toIntOrNull() ?: 1
            return PaymentQrPayload(v = v, rid = rid, amt = amt, exp = exp)
        }
    }
}
