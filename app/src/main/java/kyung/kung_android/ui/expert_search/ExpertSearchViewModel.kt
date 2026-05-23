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
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.favorite.FavoriteRepository
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

private data class LocalState(
    val keyword: String = "",
    val selectedCategoryId: Long? = null,
    val selectedLocationId: Long? = null,
    val experts: List<ExpertSearchResponse> = emptyList(),
    val isLoading: Boolean = false,
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
            experts = local.experts,
            favoritedExpertIds = favoriteIds,
            isLoading = local.isLoading,
            error = local.error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ExpertSearchUiState())

    private var searchJob: Job? = null

    init {
        search()
    }

    fun onKeywordChange(value: String) {
        _local.update { it.copy(keyword = value, error = null) }
    }

    fun onSubmit() = search()

    fun onCategorySelected(categoryId: Long?) {
        _local.update { it.copy(selectedCategoryId = categoryId) }
        search()
    }

    fun onLocationSelected(locationId: Long?) {
        _local.update { it.copy(selectedLocationId = locationId) }
        search()
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

    /**
     * 홈/외부 진입 args를 권위적으로 반영. null/빈 값은 해당 필터를 clear.
     * (이전 keyword에 카테고리만 추가되는 stale 잔존 문제 방지)
     */
    fun applyHomeArgs(keyword: String?, categoryId: Long?, locationId: Long?) {
        _local.update {
            it.copy(
                keyword = keyword.orEmpty(),
                selectedCategoryId = categoryId,
                selectedLocationId = locationId,
            )
        }
        search()
    }

    private fun search() {
        searchJob?.cancel()
        val current = _local.value
        _local.update { it.copy(isLoading = true, error = null) }
        searchJob = viewModelScope.launch {
            try {
                val experts = expertRepository.searchExperts(
                    categoryId = current.selectedCategoryId,
                    locationId = current.selectedLocationId,
                    keyword = current.keyword,
                )
                _local.update { it.copy(experts = experts, isLoading = false) }
            } catch (e: ApiException) {
                if (e.isAuthError) {
                    // 비로그인 진입은 자유 (백엔드 SecurityConfig 수정 전 임시 마스킹)
                    _local.update { it.copy(isLoading = false, experts = emptyList(), error = null) }
                } else {
                    _local.update { it.copy(isLoading = false, error = e.message ?: "검색에 실패했습니다.") }
                }
            } catch (t: Throwable) {
                _local.update { it.copy(isLoading = false, error = "네트워크 오류가 발생했습니다.") }
            }
        }
    }
}
