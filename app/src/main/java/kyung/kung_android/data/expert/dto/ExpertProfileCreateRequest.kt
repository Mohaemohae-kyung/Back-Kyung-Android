package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertProfileCreateRequest(
    val displayName: String,
    val introduction: String,
    val careerYears: Long,
    val mainCategoryId: Long,
    val mainLocationId: Long,
)
