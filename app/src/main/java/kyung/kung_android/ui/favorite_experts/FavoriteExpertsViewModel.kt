package kyung.kung_android.ui.favorite_experts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.favorite.dto.FavoriteExpertResponse
import kyung.kung_android.domain.favorite.FavoriteRepository
import javax.inject.Inject

data class FavoriteExpertsUiState(
    val favorites: List<FavoriteExpertResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FavoriteExpertsViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoriteExpertsUiState())
    val state: StateFlow<FavoriteExpertsUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = favoriteRepository.getMyFavoriteExperts()
                _state.update { it.copy(favorites = list, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "찜한 고수를 불러오지 못했어요.") }
            }
        }
    }

    fun onToggleFavorite(expertProfileId: Long) {
        val prev = _state.value.favorites
        _state.update { it.copy(favorites = prev.filterNot { e -> e.expertProfileId == expertProfileId }) }
        viewModelScope.launch {
            runCatching { favoriteRepository.toggleFavorite(expertProfileId) }
                .onFailure {
                    _state.update { it.copy(favorites = prev, error = "해제에 실패했어요.") }
                }
        }
    }
}
