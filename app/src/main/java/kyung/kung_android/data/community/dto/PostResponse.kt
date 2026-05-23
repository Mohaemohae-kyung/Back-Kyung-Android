package kyung.kung_android.data.community.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    val postId: Long,
    val title: String,
    val content: String,
    val viewCount: Long = 0,
    val writerName: String? = null,
    val imageUrls: List<String> = emptyList(),
)
