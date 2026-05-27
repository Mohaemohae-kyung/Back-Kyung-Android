package kyung.kung_android.data.store.api

import kyung.kung_android.data.store.dto.StoreProductCreateRequest
import kyung.kung_android.data.store.dto.StoreProductResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StoreApi {

    @GET("/api/store-products")
    suspend fun getStoreProducts(
        @Query("categoryId") categoryId: Long? = null,
    ): List<StoreProductResponse>

    @POST("/api/store-products")
    suspend fun createStoreProduct(
        @Body request: StoreProductCreateRequest,
    ): StoreProductResponse

    @GET("/api/store-products/{storeProductId}")
    suspend fun getStoreProduct(
        @Path("storeProductId") storeProductId: Long,
    ): StoreProductResponse
}
