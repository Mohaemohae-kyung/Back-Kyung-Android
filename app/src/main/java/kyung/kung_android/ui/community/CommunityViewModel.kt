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
import kyung.kung_android.domain.community.CommunityRepository
import javax.inject.Inject

data class CommunityUiState(
    val posts: List<PostResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    private var nextPage = 0

    fun refresh() {
        nextPage = 0
        _state.update { CommunityUiState() }
        loadFirstPage()
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore) return
        loadPage(nextPage)
    }

    private fun loadFirstPage() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadPage(0)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            _state.update {
                if (page == 0) it.copy(isLoading = true) else it.copy(isLoadingMore = true)
            }
            try {
                val response = communityRepository.getPosts(page = page, size = 20)
                _state.update {
                    val combined = if (page == 0) response.content else it.posts + response.content
                    it.copy(
                        posts = combined,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = !response.isLast,
                    )
                }
                nextPage = page + 1
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "목록을 불러오지 못했어요.",
                    )
                }
            }
        }
    }
}
