package kyung.kung_android.domain.favorite

import kyung.kung_android.data.favorite.api.MyPageApi
import kyung.kung_android.data.favorite.dto.FavoriteExpertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val myPageApi: MyPageApi,
) {

    suspend fun getMyFavoriteExperts(): List<FavoriteExpertResponse> =
        myPageApi.getMyFavoriteExperts()
}
