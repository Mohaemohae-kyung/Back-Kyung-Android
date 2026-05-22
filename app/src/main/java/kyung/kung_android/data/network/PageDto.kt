package kyung.kung_android.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    @SerialName("content") val content: List<T>,
    @SerialName("number") val page: Int,
    @SerialName("size") val size: Int,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("first") val isFirst: Boolean = false,
    @SerialName("last") val isLast: Boolean = false,
)
