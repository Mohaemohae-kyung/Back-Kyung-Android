package kyung.kung_android.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.data.store.dto.StoreProductResponse
import kyung.kung_android.domain.store.StoreRepository
import javax.inject.Inject

data class StoreUiState(
    val selectedCategoryId: Long? = null,
    val products: List<StoreProductResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun onCategorySelected(categoryId: Long?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
        load()
    }

    fun refresh() = load()

    fun load() {
        loadJob?.cancel()
        val current = _state.value
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                val products = storeRepository.getStoreProducts(categoryId = current.selectedCategoryId)
                _state.update { it.copy(products = products, isLoading = false) }
            } catch (e: ApiException) {
                if (e.isAuthError) {
                    _state.update { it.copy(isLoading = false, products = emptyList(), error = null) }
                } else {
                    _state.update { it.copy(isLoading = false, error = e.message ?: "마켓을 불러오지 못했어요.") }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "네트워크 오류가 발생했습니다.") }
            }
        }
    }
}
