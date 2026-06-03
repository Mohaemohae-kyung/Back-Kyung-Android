package kyung.kung_android.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val nickname: String? = null,
    val phone: String? = null,
    val residentRegistrationNumber: String? = null,
    val detailAddress: String? = null,
)
