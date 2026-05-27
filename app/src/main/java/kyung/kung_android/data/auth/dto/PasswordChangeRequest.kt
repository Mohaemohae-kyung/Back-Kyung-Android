package kyung.kung_android.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
)
