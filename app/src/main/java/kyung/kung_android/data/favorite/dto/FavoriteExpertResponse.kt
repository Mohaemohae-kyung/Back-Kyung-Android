package kyung.kung_android.data.favorite.dto

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteExpertResponse(
    val expertProfileId: Long,
    val displayName: String,
    val careerYears: Double? = null,
    val mainCategoryName: String? = null,
    val favorite: Boolean = true,
)
