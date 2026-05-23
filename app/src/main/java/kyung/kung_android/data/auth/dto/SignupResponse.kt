package kyung.kung_android.data.auth.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.domain.user.model.UserRole
import kyung.kung_android.domain.user.model.UserStatus

@Serializable
data class SignupResponse(
    val userId: Long,
    val email: String,
    val name: String,
    val nickname: String? = null,
    val phone: String? = null,
    val role: UserRole = UserRole.UNKNOWN,
    val status: UserStatus = UserStatus.UNKNOWN,
)
