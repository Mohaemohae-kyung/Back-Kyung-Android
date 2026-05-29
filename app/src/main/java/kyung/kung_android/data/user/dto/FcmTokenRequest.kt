package kyung.kung_android.data.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    val token: String,
)
