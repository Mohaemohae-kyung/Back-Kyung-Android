package kyung.kung_android.ui.expert_search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.favorite.FavoriteRepository
import kyung.kung_android.domain.location.model.Regions
import javax.inject.Inject

data class ExpertSearchUiState(
    val keyword: String = "",
    val selectedCategoryId: Long? = null,
    val selectedLocationId: Long? = null,
    val experts: List<ExpertSearchResponse> = emptyList(),
    val favoritedExpertIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

private data class LocalState(
    val keyword: String = "",
    val selectedCategoryId: Long? = null,
    val selectedLocationId: Long? = null,
    val allExperts: List<ExpertSearchResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ExpertSearchViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())

    val state: StateFlow<ExpertSearchUiState> = combine(
        _local,
        favoriteRepository.favoriteIds,
    ) { local, favoriteIds ->
        ExpertSearchUiState(
            keyword = local.keyword,
            selectedCategoryId = local.selectedCategoryId,
            selectedLocationId = local.selectedLocationId,
            experts = filterExperts(local),
            favoritedExpertIds = favoriteIds,
            isLoading = local.isLoading,
            error = local.error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ExpertSearchUiState())

    private var loadJob: Job? = null

    init {
        loadAll()
    }

    fun onKeywordChange(value: String) {
        _local.update { it.copy(keyword = value, error = null) }
    }

    fun onSubmit() = loadAll()

    fun onCategorySelected(categoryId: Long?) {
        _local.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onLocationSelected(locationId: Long?) {
        _local.update { it.copy(selectedLocationId = locationId) }
    }

    fun onFavoriteToggle(expertProfileId: Long) {
        viewModelScope.launch {
            try {
                favoriteRepository.toggleFavorite(expertProfileId)
            } catch (e: ApiException) {
                _local.update { it.copy(error = e.message) }
            } catch (t: Throwable) {
                _local.update { it.copy(error = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    fun applyHomeArgs(keyword: String?, categoryId: Long?, locationId: Long?) {
        _local.update {
            it.copy(
                keyword = keyword.orEmpty(),
                selectedCategoryId = categoryId,
                selectedLocationId = locationId,
            )
        }
        if (_local.value.allExperts.isEmpty()) loadAll()
    }

    private fun loadAll() {
        loadJob?.cancel()
        _local.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                val experts = expertRepository.searchExperts()
                _local.update { it.copy(allExperts = experts, isLoading = false) }
            } catch (e: ApiException) {
                if (e.isAuthError) {
                    _local.update { it.copy(isLoading = false, allExperts = emptyList(), error = null) }
                } else {
                    _local.update { it.copy(isLoading = false, error = e.message ?: "검색에 실패했습니다.") }
                }
            } catch (t: Throwable) {
                _local.update { it.copy(isLoading = false, error = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    private fun filterExperts(local: LocalState): List<ExpertSearchResponse> {
        val keyword = local.keyword.trim().lowercase()
        val matchingCategoryNames = resolveCategoryNames(local.selectedCategoryId)
        val matchingLocationName = local.selectedLocationId?.let { Regions.byId(it)?.name }

        return local.allExperts.filter { expert ->
            val searchText = listOfNotNull(
                expert.displayName,
                expert.introduction,
                expert.categoryNames.joinToString(" "),
                expert.mainLocationName,
            ).joinToString(" ").lowercase()

            val matchesKeyword = keyword.isEmpty() || searchText.contains(keyword)

            val matchesCategory = matchingCategoryNames == null ||
                expert.categoryNames.any { it in matchingCategoryNames }

            val matchesLocation = matchingLocationName == null ||
                (expert.mainLocationName?.contains(matchingLocationName) == true)

            matchesKeyword && matchesCategory && matchesLocation
        }
    }

    private fun resolveCategoryNames(categoryId: Long?): Set<String>? {
        if (categoryId == null) return null
        val parent = Categories.byId(categoryId)
        if (parent != null) {
            return (Categories.subcategoriesOf(categoryId).map { it.name } + parent.name).toSet()
        }
        val child = Categories.SUBCATEGORIES.values.flatten().firstOrNull { it.id == categoryId }
        return child?.let { setOf(it.name) }
    }
}
