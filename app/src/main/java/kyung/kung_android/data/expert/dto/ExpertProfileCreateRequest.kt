package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertProfileCreateRequest(
    val displayName: String,
    val introduction: String,
    val careerYears: Double,
    val mainCategoryId: Long,
    val categoryIds: List<Long> = emptyList(),
    val mainLocationId: Long,
    val externalPortfolioUrl: String? = null,
)
