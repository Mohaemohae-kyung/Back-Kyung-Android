package kyung.kung_android.data.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserWithdrawRequest(
    val password: String,
    val reason: String? = null,
)
