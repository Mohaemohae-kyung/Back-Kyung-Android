package kyung.kung_android.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReissueResponse(
    val accessToken: String,
    val refreshToken: String,
)
