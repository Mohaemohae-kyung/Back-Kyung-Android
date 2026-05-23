package kyung.kung_android.ui.expert_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.favorite.FavoriteRepository
import javax.inject.Inject

data class ExpertDetailUiState(
    val expertId: Long = 0L,
    val expert: ExpertDetailResponse? = null,
    val isFavorited: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private data class LocalState(
    val expert: ExpertDetailResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ExpertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expertRepository: ExpertRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val expertId: Long = savedStateHandle.get<Long>("expertId") ?: 0L

    private val _local = MutableStateFlow(LocalState())

    val state: StateFlow<ExpertDetailUiState> = combine(
        _local,
        favoriteRepository.favoriteIds,
    ) { local, favoriteIds ->
        ExpertDetailUiState(
            expertId = expertId,
            expert = local.expert,
            isFavorited = expertId in favoriteIds,
            isLoading = local.isLoading,
            error = local.error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ExpertDetailUiState(expertId = expertId))

    fun onFavoriteToggle() {
        viewModelScope.launch {
            try {
                favoriteRepository.toggleFavorite(expertId)
            } catch (t: Throwable) {
                _local.update { it.copy(error = "찜 처리에 실패했어요.") }
            }
        }
    }

    fun loadExpert() {
        _local.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val expert = expertRepository.getExpertDetail(expertId)
                _local.update { it.copy(expert = expert, isLoading = false) }
            } catch (t: Throwable) {
                _local.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
            }
        }
    }
}
