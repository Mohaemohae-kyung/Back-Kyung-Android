package kyung.kung_android.domain.favorite

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kyung.kung_android.data.expert.api.ExpertApi
import kyung.kung_android.data.expert.dto.FavoriteToggleResponse
import kyung.kung_android.data.favorite.api.MyPageApi
import kyung.kung_android.data.favorite.dto.FavoriteExpertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val myPageApi: MyPageApi,
    private val expertApi: ExpertApi,
) {

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    suspend fun getMyFavoriteExperts(): List<FavoriteExpertResponse> {
        val list = myPageApi.getMyFavoriteExperts()
        _favoriteIds.value = list.map { it.expertProfileId }.toSet()
        return list
    }

    suspend fun toggleFavorite(expertProfileId: Long): FavoriteToggleResponse {
        val result = expertApi.toggleFavorite(expertProfileId)
        _favoriteIds.value = if (result.favorite) {
            _favoriteIds.value + expertProfileId
        } else {
            _favoriteIds.value - expertProfileId
        }
        return result
    }

    fun clearCache() {
        _favoriteIds.value = emptySet()
    }
}
