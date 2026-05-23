package kyung.kung_android.data.community.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    val content: String,
)
