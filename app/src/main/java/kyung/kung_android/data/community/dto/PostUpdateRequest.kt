package kyung.kung_android.data.community.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostUpdateRequest(
    val title: String,
    val content: String,
    val categoryId: Long? = null,
    val locationId: Long? = null,
    val imageFileIds: List<Long> = emptyList(),
)
