package kyung.kung_android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.domain.expert.ExpertRepository
import javax.inject.Inject

data class HomeUiState(
    val recommended: List<ExpertSearchResponse> = emptyList(),
    val isLoadingRecommended: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        _state.update { it.copy(isLoadingRecommended = true) }
        viewModelScope.launch {
            try {
                val experts = expertRepository.searchExperts()
                _state.update { it.copy(recommended = experts.take(3), isLoadingRecommended = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoadingRecommended = false) }
            }
        }
    }
}
