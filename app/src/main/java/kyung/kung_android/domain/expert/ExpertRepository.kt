package kyung.kung_android.domain.expert

import kyung.kung_android.data.expert.api.ExpertApi
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.expert.dto.ExpertProfileCreateRequest
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.data.expert.dto.FavoriteToggleResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertRepository @Inject constructor(
    private val expertApi: ExpertApi,
) {

    suspend fun searchExperts(
        categoryId: Long? = null,
        locationId: Long? = null,
        keyword: String? = null,
    ): List<ExpertSearchResponse> =
        expertApi.searchExperts(
            categoryId = categoryId,
            locationId = locationId,
            keyword = keyword?.trim()?.takeIf { it.isNotEmpty() },
        )

    suspend fun getExpertDetail(expertId: Long): ExpertDetailResponse =
        expertApi.getExpertDetail(expertId)

    suspend fun toggleFavorite(expertProfileId: Long): FavoriteToggleResponse =
        expertApi.toggleFavorite(expertProfileId)

    suspend fun createProfile(
        displayName: String,
        introduction: String,
        careerYears: Long,
        mainCategoryId: Long,
        mainLocationId: Long,
    ) {
        expertApi.createProfile(
            ExpertProfileCreateRequest(
                displayName = displayName.trim(),
                introduction = introduction.trim(),
                careerYears = careerYears,
                mainCategoryId = mainCategoryId,
                mainLocationId = mainLocationId,
            )
        )
    }
}
