package kyung.kung_android.data.notice.api

import kyung.kung_android.data.notice.dto.NoticePostPageResponse
import kyung.kung_android.data.notice.dto.NoticePostResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NoticeApi {

    @GET("/api/expert-center/posts")
    suspend fun getExpertCenterPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,DESC",
    ): NoticePostPageResponse

    @GET("/api/expert-center/posts/{postId}")
    suspend fun getExpertCenterPost(
        @Path("postId") postId: Long,
    ): NoticePostResponse
}
