package kyung.kung_android.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.community.dto.PostResponse
import kyung.kung_android.data.notice.dto.NoticePostResponse
import kyung.kung_android.domain.community.CommunityRepository
import kyung.kung_android.domain.notice.NoticeRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class BoardState<T>(
    val posts: List<T> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val nextPage: Int = 0,
)

data class CommunityUiState(
    val selectedBoard: BoardType = BoardType.LIFE,
    val role: String? = null,
    val life: BoardState<PostResponse> = BoardState(),
    val center: BoardState<NoticePostResponse> = BoardState(),
) {
    val canSeeCenter: Boolean
        get() = role == "EXPERT" || role == "ADMIN"
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _state.update { it.copy(role = user?.role) }
            }
        }
        loadLifePage(0, isFirst = true)
    }

    fun selectBoard(board: BoardType) {
        if (_state.value.selectedBoard == board) return
        _state.update { it.copy(selectedBoard = board) }
        when (board) {
            BoardType.LIFE -> if (_state.value.life.posts.isEmpty()) loadLifePage(0, isFirst = true)
            BoardType.CENTER -> {
                if (_state.value.canSeeCenter && _state.value.center.posts.isEmpty()) {
                    loadCenterPage(0, isFirst = true)
                }
            }
        }
    }

    fun refresh() {
        when (_state.value.selectedBoard) {
            BoardType.LIFE -> loadLifePage(0, isFirst = true)
            BoardType.CENTER -> if (_state.value.canSeeCenter) loadCenterPage(0, isFirst = true)
        }
    }

    fun loadMore() {
        when (_state.value.selectedBoard) {
            BoardType.LIFE -> {
                val s = _state.value.life
                if (s.isLoadingMore || !s.hasMore) return
                loadLifePage(s.nextPage, isFirst = false)
            }
            BoardType.CENTER -> {
                if (!_state.value.canSeeCenter) return
                val s = _state.value.center
                if (s.isLoadingMore || !s.hasMore) return
                loadCenterPage(s.nextPage, isFirst = false)
            }
        }
    }

    private fun loadLifePage(page: Int, isFirst: Boolean) {
        viewModelScope.launch {
            _state.update {
                val s = it.life
                it.copy(
                    life = s.copy(
                        isLoading = isFirst,
                        isLoadingMore = !isFirst,
                        error = if (isFirst) null else s.error,
                    )
                )
            }
            try {
                val response = communityRepository.getPosts(page = page, size = 20)
                _state.update {
                    val current = it.life
                    val combined = if (page == 0) response.content else current.posts + response.content
                    it.copy(
                        life = current.copy(
                            posts = combined,
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = !response.isLast,
                            nextPage = page + 1,
                        )
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        life = it.life.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = "목록을 불러오지 못했어요.",
                        )
                    )
                }
            }
        }
    }

    private fun loadCenterPage(page: Int, isFirst: Boolean) {
        viewModelScope.launch {
            _state.update {
                val s = it.center
                it.copy(
                    center = s.copy(
                        isLoading = isFirst,
                        isLoadingMore = !isFirst,
                        error = if (isFirst) null else s.error,
                    )
                )
            }
            try {
                val response = noticeRepository.getExpertCenterPosts(page = page, size = 20)
                _state.update {
                    val current = it.center
                    val combined = if (page == 0) response.content else current.posts + response.content
                    it.copy(
                        center = current.copy(
                            posts = combined,
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = !response.isLast,
                            nextPage = page + 1,
                        )
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        center = it.center.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = "목록을 불러오지 못했어요.",
                        )
                    )
                }
            }
        }
    }
}
