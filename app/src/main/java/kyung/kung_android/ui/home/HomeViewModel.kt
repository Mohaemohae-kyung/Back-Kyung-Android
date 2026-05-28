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
import kyung.kung_android.data.store.dto.StoreProductResponse
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.store.StoreRepository
import javax.inject.Inject

data class HomeUiState(
    val recommended: List<ExpertSearchResponse> = emptyList(),
    val isLoadingRecommended: Boolean = true,
    val recommendedStore: List<StoreProductResponse> = emptyList(),
    val isLoadingStore: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun loadRecommendations() {
        _state.update { it.copy(isLoadingRecommended = true, isLoadingStore = true) }
        viewModelScope.launch {
            try {
                val experts = expertRepository.searchExperts()
                _state.update { it.copy(recommended = experts, isLoadingRecommended = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoadingRecommended = false) }
            }
        }
        viewModelScope.launch {
            try {
                val products = storeRepository.getStoreProducts()
                _state.update { it.copy(recommendedStore = products, isLoadingStore = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoadingStore = false) }
            }
        }
    }
}
