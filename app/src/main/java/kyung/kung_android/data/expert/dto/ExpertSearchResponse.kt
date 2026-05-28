package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertSearchResponse(
    val expertProfileId: Long,
    val displayName: String,
    val introduction: String? = null,
    val careerYears: Double? = null,
    val mainLocationName: String? = null,
    val categoryNames: List<String> = emptyList(),
    val verifiedYn: String? = null,
    val status: String? = null,
    val profileImageUrl: String? = null,
    val nickname: String? = null,
)
