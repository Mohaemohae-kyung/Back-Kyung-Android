package kyung.kung_android.data.expert.dto

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteToggleResponse(
    val expertProfileId: Long,
    val favorite: Boolean,
)
