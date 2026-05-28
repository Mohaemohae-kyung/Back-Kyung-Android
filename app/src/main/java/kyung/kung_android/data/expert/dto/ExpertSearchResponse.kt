package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertSearchResponse(
    val expertServiceId: Long,
    val expertProfileId: Long,
    val displayName: String,
    val introduction: String? = null,
    val careerYears: Double? = null,
    val mainCategoryName: String? = null,
    val mainLocationName: String? = null,
    val categoryNames: List<String> = emptyList(),
    val verifiedYn: String? = null,
    val status: String? = null,
    val serviceTitle: String? = null,
    val serviceDescription: String? = null,
    val price: Int? = null,
    val profileImageUrl: String? = null,
)
