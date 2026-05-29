package kyung.kung_android.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class E2ePayloadRequest(
    val encryptedAesKey: String,
    val iv: String,
    val cipherText: String,
)
