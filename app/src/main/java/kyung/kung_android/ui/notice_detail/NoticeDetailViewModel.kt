package kyung.kung_android.ui.notice_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.notice.dto.NoticePostResponse
import kyung.kung_android.domain.notice.NoticeRepository
import javax.inject.Inject

data class NoticeDetailUiState(
    val postId: Long = 0L,
    val post: NoticePostResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noticeRepository: NoticeRepository,
) : ViewModel() {

    private val postId: Long = savedStateHandle.get<Long>("postId") ?: 0L

    private val _state = MutableStateFlow(NoticeDetailUiState(postId = postId))
    val state: StateFlow<NoticeDetailUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val p = noticeRepository.getExpertCenterPost(postId)
                _state.update { it.copy(post = p, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "게시글을 불러오지 못했어요.") }
            }
        }
    }
}
