package kyung.kung_android.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReissueRequest(
    val refreshToken: String,
)
