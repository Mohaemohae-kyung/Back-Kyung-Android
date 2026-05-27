package kyung.kung_android.data.store.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class StoreProductResponse(
    val storeProductId: Long,
    val expertProfileId: Long,
    val categoryId: Long,
    val categoryName: String,
    val title: String,
    val thumbnailImageUrl: String? = null,
    val description: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val price: BigDecimal? = null,
    val serviceType: String? = null,
    val locationId: Long? = null,
    val locationName: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
