package kyung.kung_android.data.expert.api

import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.data.expert.dto.FavoriteToggleResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpertApi {

    @GET("/api/experts/search")
    suspend fun searchExperts(
        @Query("categoryId") categoryId: Long? = null,
        @Query("locationId") locationId: Long? = null,
        @Query("keyword") keyword: String? = null,
    ): List<ExpertSearchResponse>

    @GET("/api/experts/{expertId}")
    suspend fun getExpertDetail(
        @Path("expertId") expertId: Long,
    ): ExpertDetailResponse

    @POST("/api/experts/{expertProfileId}/favorite")
    suspend fun toggleFavorite(
        @Path("expertProfileId") expertProfileId: Long,
    ): FavoriteToggleResponse
}
