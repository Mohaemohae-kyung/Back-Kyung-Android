package kyung.kung_android.ui.expert_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.domain.expert.ExpertRepository
import javax.inject.Inject

data class ExpertDetailUiState(
    val expertId: Long = 0L,
    val expert: ExpertDetailResponse? = null,
    val isFavorited: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ExpertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val expertId: Long = savedStateHandle.get<Long>("expertId") ?: 0L

    private val _state = MutableStateFlow(ExpertDetailUiState(expertId = expertId))
    val state: StateFlow<ExpertDetailUiState> = _state.asStateFlow()

    fun onFavoriteToggle() {
        viewModelScope.launch {
            try {
                val result = expertRepository.toggleFavorite(expertId)
                _state.update { it.copy(isFavorited = result.favorite) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = "찜 처리에 실패했어요.") }
            }
        }
    }

    fun loadExpert() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val expert = expertRepository.getExpertDetail(expertId)
                _state.update { it.copy(expert = expert, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
            }
        }
    }
}
