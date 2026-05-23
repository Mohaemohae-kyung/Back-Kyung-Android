package kyung.kung_android.ui.expert_search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.expert.ExpertRepository
import javax.inject.Inject

data class ExpertSearchUiState(
    val keyword: String = "",
    val selectedCategoryId: Long? = null,
    val selectedLocationId: Long? = null,
    val experts: List<ExpertSearchResponse> = emptyList(),
    val favoritedExpertIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ExpertSearchViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpertSearchUiState())
    val state: StateFlow<ExpertSearchUiState> = _state.asStateFlow()

    init {
        search()
    }

    fun onKeywordChange(value: String) {
        _state.update { it.copy(keyword = value, error = null) }
    }

    fun onSubmit() {
        search()
    }

    fun onCategorySelected(categoryId: Long?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
        search()
    }

    fun onLocationSelected(locationId: Long?) {
        _state.update { it.copy(selectedLocationId = locationId) }
        search()
    }

    fun onFavoriteToggle(expertProfileId: Long) {
        viewModelScope.launch {
            try {
                val result = expertRepository.toggleFavorite(expertProfileId)
                _state.update {
                    val next = it.favoritedExpertIds.toMutableSet()
                    if (result.favorite) next.add(result.expertProfileId) else next.remove(result.expertProfileId)
                    it.copy(favoritedExpertIds = next)
                }
            } catch (e: ApiException) {
                _state.update { it.copy(error = e.message) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    fun applyKeywordFromHome(keyword: String) {
        _state.update { it.copy(keyword = keyword) }
        search()
    }

    fun applyCategoryFromHome(categoryId: Long) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
        search()
    }

    private fun search() {
        val current = _state.value
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val experts = expertRepository.searchExperts(
                    categoryId = current.selectedCategoryId,
                    locationId = current.selectedLocationId,
                    keyword = current.keyword,
                )
                _state.update { it.copy(experts = experts, isLoading = false) }
            } catch (e: ApiException) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "검색에 실패했습니다.") }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "네트워크 오류가 발생했습니다.") }
            }
        }
    }
}
