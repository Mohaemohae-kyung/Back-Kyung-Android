package kyung.kung_android.data.community.api

import kyung.kung_android.data.community.dto.CommentRequest
import kyung.kung_android.data.community.dto.CommentResponse
import kyung.kung_android.data.community.dto.PostCreateRequest
import kyung.kung_android.data.community.dto.PostPageResponse
import kyung.kung_android.data.community.dto.PostResponse
import kyung.kung_android.data.community.dto.PostUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {

    @GET("/api/community/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortColumn") sortColumn: String? = null,
        @Query("sortDirection") sortDirection: String = "DESC",
    ): PostPageResponse

    @GET("/api/community/posts/{postId}")
    suspend fun getPost(@Path("postId") postId: Long): PostResponse

    @POST("/api/community/posts")
    suspend fun createPost(@Body request: PostCreateRequest): PostResponse

    @PATCH("/api/community/posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Body request: PostUpdateRequest,
    ): PostResponse

    @DELETE("/api/community/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Long): Unit?

    @GET("/api/community/posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Long): List<CommentResponse>

    @POST("/api/community/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body request: CommentRequest,
    ): CommentResponse

    @PATCH("/api/community/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Long,
        @Body request: CommentRequest,
    ): CommentResponse

    @DELETE("/api/community/comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: Long): Unit?
}
