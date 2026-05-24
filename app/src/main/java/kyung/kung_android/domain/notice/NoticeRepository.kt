package kyung.kung_android.domain.notice

import kyung.kung_android.data.notice.api.NoticeApi
import kyung.kung_android.data.notice.dto.NoticePostPageResponse
import kyung.kung_android.data.notice.dto.NoticePostResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor(
    private val noticeApi: NoticeApi,
) {

    suspend fun getExpertCenterPosts(page: Int = 0, size: Int = 20): NoticePostPageResponse =
        noticeApi.getExpertCenterPosts(page = page, size = size)

    suspend fun getExpertCenterPost(postId: Long): NoticePostResponse =
        noticeApi.getExpertCenterPost(postId)
}
