package kyung.kung_android.data.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileUpdateRequest(
    val name: String? = null,
    val phone: String? = null,
    val nickname: String? = null,
    val profileImageFileId: Long? = null,
)
