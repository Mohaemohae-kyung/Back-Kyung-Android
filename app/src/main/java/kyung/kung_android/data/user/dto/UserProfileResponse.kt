package kyung.kung_android.data.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val userId: Long? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val nickname: String? = null,
    val role: String,
    val profileImageUrl: String? = null,
    val expertProfileId: Long? = null,
)
