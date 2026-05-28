package kyung.kung_android.data.store.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.request.dto.BigDecimalAsStringSerializer
import java.math.BigDecimal

@Serializable
data class StoreProductCreateRequest(
    val categoryId: Long,
    val title: String,
    val description: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val price: BigDecimal,
    val serviceType: String,
    val locationId: Long? = null,
    val thumbnailImageFileId: Long? = null,
)
