package kyung.kung_android.data.expert_service.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpertServiceCreateRequest(
    val categoryId: Long,
    val locationId: Long,
    val serviceTitle: String,
    val serviceDescription: String,
    val price: Int = 0,
)
