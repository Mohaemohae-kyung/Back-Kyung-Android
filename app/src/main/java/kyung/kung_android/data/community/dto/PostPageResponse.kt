package kyung.kung_android.data.community.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostPageResponse(
    val content: List<PostResponse> = emptyList(),
    @SerialName("number") val page: Int = 0,
    val size: Int = 10,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    @SerialName("first") val isFirst: Boolean = true,
    @SerialName("last") val isLast: Boolean = true,
)
