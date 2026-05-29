package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class E2ePayloadResponse(
    val success: Boolean,
    val cipherText: String? = null,
    val message: String? = null,
)
