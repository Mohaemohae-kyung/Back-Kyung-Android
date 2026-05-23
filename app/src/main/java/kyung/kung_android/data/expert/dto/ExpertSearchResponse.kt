package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertSearchResponse(
    val expertProfileId: Long,
    val displayName: String,
    val introduction: String? = null,
    val careerYears: Long? = null,
    val mainCategoryName: String? = null,
    val mainLocationName: String? = null,
    val verifiedYn: String? = null,
    val status: String? = null,
)
