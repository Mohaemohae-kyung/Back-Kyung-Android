package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertDetailResponse(
    val expertServiceId: Long,
    val expertProfileId: Long,
    val ownerUserId: Long? = null,
    val displayName: String,
    val introduction: String? = null,
    val careerYears: Double? = null,
    val mainCategoryName: String? = null,
    val mainLocationName: String? = null,
    val verifiedYn: String? = null,
    val status: String? = null,
    val expertServiceIds: List<Long> = emptyList(),
    val serviceTitle: String? = null,
    val serviceDescription: String? = null,
    val price: Int? = null,
)
