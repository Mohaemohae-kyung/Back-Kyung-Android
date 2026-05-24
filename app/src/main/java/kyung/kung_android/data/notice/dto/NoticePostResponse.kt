package kyung.kung_android.data.notice.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoticePostResponse(
    val postId: Long,
    val noticeType: String? = null,
    val title: String,
    val content: String,
    val viewCount: Long = 0,
    val createdAt: String? = null,
)
