package kyung.kung_android.ui.post_editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.domain.community.CommunityRepository
import kyung.kung_android.domain.file.FileRepository
import javax.inject.Inject

data class PostEditorUiState(
    val title: String = "",
    val content: String = "",
    val imageFileIds: List<Long> = emptyList(),
    val uploadingCount: Int = 0,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = title.isNotBlank() && content.isNotBlank() && uploadingCount == 0 && !isSubmitting

    val imageCount: Int
        get() = imageFileIds.size + uploadingCount
}

sealed interface PostEditorEffect {
    data object NavigateBack : PostEditorEffect
}

@HiltViewModel
class PostEditorViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PostEditorUiState())
    val state: StateFlow<PostEditorUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PostEditorEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<PostEditorEffect> = _effects.asSharedFlow()

    fun onTitleChange(v: String) = _state.update { it.copy(title = v) }
    fun onContentChange(v: String) = _state.update { it.copy(content = v) }

    fun onAddImage(uri: Uri) {
        if (_state.value.imageCount >= MAX_IMAGES) return
        _state.update { it.copy(uploadingCount = it.uploadingCount + 1) }
        viewModelScope.launch {
            try {
                val response = fileRepository.uploadImage(uri = uri, domain = "COMMUNITY_POST")
                _state.update {
                    it.copy(
                        imageFileIds = it.imageFileIds + response.fileId,
                        uploadingCount = (it.uploadingCount - 1).coerceAtLeast(0),
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        uploadingCount = (it.uploadingCount - 1).coerceAtLeast(0),
                        errorMessage = "이미지 업로드에 실패했어요.",
                    )
                }
            }
        }
    }

    fun onRemoveImage(fileId: Long) {
        _state.update { it.copy(imageFileIds = it.imageFileIds - fileId) }
    }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                communityRepository.createPost(
                    boardType = "LIFE",
                    title = current.title,
                    content = current.content,
                    imageFileIds = current.imageFileIds,
                )
                _effects.emit(PostEditorEffect.NavigateBack)
            } catch (t: Throwable) {
                _state.update { it.copy(errorMessage = "글 등록에 실패했어요.") }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private companion object {
        private const val MAX_IMAGES = 5
    }
}
