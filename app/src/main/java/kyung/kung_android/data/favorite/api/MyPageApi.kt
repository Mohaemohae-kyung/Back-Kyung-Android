package kyung.kung_android.data.favorite.api

import kyung.kung_android.data.favorite.dto.FavoriteExpertResponse
import retrofit2.http.GET

interface MyPageApi {

    @GET("/api/mypage/favorites")
    suspend fun getMyFavoriteExperts(): List<FavoriteExpertResponse>
}
