package kyung.kung_android.ui.store_editor

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
import kyung.kung_android.domain.file.FileRepository
import kyung.kung_android.domain.store.StoreRepository
import java.math.BigDecimal
import javax.inject.Inject

data class StoreEditorUiState(
    val categoryId: Long? = null,
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val serviceType: String = "ONLINE",
    val locationId: Long? = null,
    val thumbnailUri: Uri? = null,
    val thumbnailFileId: Long? = null,
    val isUploadingImage: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val needsLocation: Boolean get() = serviceType != "ONLINE"

    val canSubmit: Boolean
        get() = categoryId != null &&
            title.isNotBlank() &&
            (price.toBigDecimalOrNull()?.let { it >= BigDecimal.ZERO } == true) &&
            (!needsLocation || locationId != null) &&
            thumbnailFileId != null &&
            !isUploadingImage &&
            !isSubmitting
}

sealed interface StoreEditorEffect {
    data object Success : StoreEditorEffect
}

@HiltViewModel
class StoreEditorViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StoreEditorUiState())
    val state: StateFlow<StoreEditorUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<StoreEditorEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<StoreEditorEffect> = _effects.asSharedFlow()

    fun onPickImage(uri: Uri) {
        _state.update { it.copy(thumbnailUri = uri, isUploadingImage = true, error = null) }
        viewModelScope.launch {
            try {
                val res = fileRepository.uploadImage(uri = uri, domain = "STORE_PRODUCT")
                _state.update { it.copy(thumbnailFileId = res.fileId, isUploadingImage = false) }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        thumbnailUri = null,
                        thumbnailFileId = null,
                        isUploadingImage = false,
                        error = "이미지 업로드에 실패했어요.",
                    )
                }
            }
        }
    }

    fun onCategorySelected(id: Long) = _state.update { it.copy(categoryId = id) }
    fun onTitleChange(v: String) = _state.update { it.copy(title = v) }
    fun onDescriptionChange(v: String) = _state.update { it.copy(description = v) }
    fun onPriceChange(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() }) }
    fun onServiceTypeChange(type: String) = _state.update {
        it.copy(serviceType = type, locationId = if (type == "ONLINE") null else it.locationId)
    }
    fun onLocationSelected(id: Long) = _state.update { it.copy(locationId = id) }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                storeRepository.create(
                    categoryId = requireNotNull(s.categoryId),
                    title = s.title,
                    description = s.description.ifBlank { null },
                    price = s.price.toBigDecimal(),
                    serviceType = s.serviceType,
                    locationId = if (s.needsLocation) s.locationId else null,
                    thumbnailImageFileId = s.thumbnailFileId,
                )
                _effects.emit(StoreEditorEffect.Success)
            } catch (t: Throwable) {
                _state.update { it.copy(isSubmitting = false, error = "상품 등록에 실패했어요. 다시 시도해주세요.") }
            }
        }
    }
}
