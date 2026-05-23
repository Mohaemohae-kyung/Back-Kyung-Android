package kyung.kung_android.data.auth.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.domain.user.model.UserRole

@Serializable
data class LoginResponse(
    val userId: Long,
    val email: String,
    val name: String,
    val nickname: String? = null,
    val role: UserRole = UserRole.UNKNOWN,
    val accessToken: String,
    val refreshToken: String,
)
