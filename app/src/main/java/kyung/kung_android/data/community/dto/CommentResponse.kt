package kyung.kung_android.data.community.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    val commentId: Long,
    val postId: Long,
    val content: String,
    val writerName: String? = null,
    val status: String? = null,
)
