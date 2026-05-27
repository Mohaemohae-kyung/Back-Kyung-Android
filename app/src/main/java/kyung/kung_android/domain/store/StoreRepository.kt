package kyung.kung_android.domain.store

import kyung.kung_android.data.store.api.StoreApi
import kyung.kung_android.data.store.dto.StoreProductCreateRequest
import kyung.kung_android.data.store.dto.StoreProductResponse
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val storeApi: StoreApi,
) {

    suspend fun getStoreProducts(categoryId: Long? = null): List<StoreProductResponse> =
        storeApi.getStoreProducts(categoryId = categoryId)

    suspend fun getStoreProduct(storeProductId: Long): StoreProductResponse =
        storeApi.getStoreProduct(storeProductId)

    suspend fun create(
        categoryId: Long,
        title: String,
        description: String?,
        price: BigDecimal,
        serviceType: String,
        locationId: Long? = null,
        thumbnailImageFileId: Long? = null,
    ): StoreProductResponse =
        storeApi.createStoreProduct(
            StoreProductCreateRequest(
                categoryId = categoryId,
                title = title.trim(),
                description = description?.trim(),
                price = price,
                serviceType = serviceType,
                locationId = locationId,
                thumbnailImageFileId = thumbnailImageFileId,
            )
        )
}
