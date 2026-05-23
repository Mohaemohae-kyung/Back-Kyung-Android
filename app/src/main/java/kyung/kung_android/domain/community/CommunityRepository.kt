package kyung.kung_android.domain.community

import kyung.kung_android.data.community.api.CommunityApi
import kyung.kung_android.data.community.dto.CommentRequest
import kyung.kung_android.data.community.dto.CommentResponse
import kyung.kung_android.data.community.dto.PostCreateRequest
import kyung.kung_android.data.community.dto.PostPageResponse
import kyung.kung_android.data.community.dto.PostResponse
import kyung.kung_android.data.community.dto.PostUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val communityApi: CommunityApi,
) {

    suspend fun getPosts(page: Int = 0, size: Int = 20): PostPageResponse =
        communityApi.getPosts(page = page, size = size)

    suspend fun getPost(postId: Long): PostResponse = communityApi.getPost(postId)

    suspend fun createPost(
        boardType: String,
        title: String,
        content: String,
        categoryId: Long? = null,
        locationId: Long? = null,
        imageFileIds: List<Long> = emptyList(),
    ): PostResponse = communityApi.createPost(
        PostCreateRequest(
            categoryId = categoryId,
            locationId = locationId,
            imageFileIds = imageFileIds,
            boardType = boardType,
            title = title.trim(),
            content = content.trim(),
        )
    )

    suspend fun updatePost(
        postId: Long,
        title: String,
        content: String,
        categoryId: Long? = null,
        locationId: Long? = null,
        imageFileIds: List<Long> = emptyList(),
    ): PostResponse = communityApi.updatePost(
        postId,
        PostUpdateRequest(
            title = title.trim(),
            content = content.trim(),
            categoryId = categoryId,
            locationId = locationId,
            imageFileIds = imageFileIds,
        )
    )

    suspend fun deletePost(postId: Long) {
        communityApi.deletePost(postId)
    }

    suspend fun getComments(postId: Long): List<CommentResponse> =
        communityApi.getComments(postId)

    suspend fun createComment(postId: Long, content: String): CommentResponse =
        communityApi.createComment(postId, CommentRequest(content.trim()))

    suspend fun updateComment(commentId: Long, content: String): CommentResponse =
        communityApi.updateComment(commentId, CommentRequest(content.trim()))

    suspend fun deleteComment(commentId: Long) {
        communityApi.deleteComment(commentId)
    }
}
