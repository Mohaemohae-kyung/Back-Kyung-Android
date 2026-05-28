package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertDetailResponse(
    val expertProfileId: Long,
    val ownerUserId: Long? = null,
    val displayName: String,
    val introduction: String? = null,
    val careerYears: Double? = null,
    val mainCategoryName: String? = null,
    val mainLocationName: String? = null,
    val categoryIds: List<Long> = emptyList(),
    val categoryNames: List<String> = emptyList(),
    val verifiedYn: String? = null,
    val status: String? = null,
    val profileImageUrl: String? = null,
    val portfolioWebViewUrl: String? = null,
)
