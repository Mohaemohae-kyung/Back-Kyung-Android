package kyung.kung_android.ui.post_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kyung.kung_android.data.community.dto.CommentResponse
import kyung.kung_android.data.community.dto.PostResponse
import kyung.kung_android.domain.auth.AuthRepository
import kyung.kung_android.domain.community.CommunityRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class PostDetailUiState(
    val postId: Long = 0L,
    val post: PostResponse? = null,
    val comments: List<CommentResponse> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository,
    authRepository: AuthRepository,
    userRepository: UserRepository,
) : ViewModel() {

    private val postId: Long = savedStateHandle.get<Long>("postId") ?: 0L

    private val _state = MutableStateFlow(PostDetailUiState(postId = postId))
    val state: StateFlow<PostDetailUiState> = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isOwnPost: StateFlow<Boolean> = combine(_state, userRepository.currentUser) { st, me ->
        val writer = st.post?.writerName?.trim().orEmpty()
        val myName = me?.name?.trim().orEmpty()
        writer.isNotEmpty() && myName.isNotEmpty() && writer == myName
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onInputChange(v: String) = _state.update { it.copy(input = v) }

    fun onSendComment() {
        val text = _state.value.input.trim()
        if (text.isEmpty() || _state.value.isPosting) return

        _state.update { it.copy(isPosting = true) }
        viewModelScope.launch {
            try {
                val created = communityRepository.createComment(postId, text)
                _state.update {
                    it.copy(
                        comments = it.comments + created,
                        input = "",
                        isPosting = false,
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isPosting = false, error = "댓글 전송에 실패했어요.") }
            }
        }
    }

    fun onDeletePost(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                communityRepository.deletePost(postId)
                onDone()
            } catch (t: Throwable) {
                _state.update { it.copy(error = "삭제에 실패했어요.") }
            }
        }
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val post = communityRepository.getPost(postId)
                val comments = communityRepository.getComments(postId)
                _state.update { it.copy(post = post, comments = comments, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "게시글을 불러오지 못했어요.") }
            }
        }
    }
}
