package kyung.kung_android.data.community.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostCreateRequest(
    val categoryId: Long? = null,
    val locationId: Long? = null,
    val imageFileIds: List<Long> = emptyList(),
    val boardType: String,
    val title: String,
    val content: String,
)
