package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyResponse(
    val publicKey: String,
)
