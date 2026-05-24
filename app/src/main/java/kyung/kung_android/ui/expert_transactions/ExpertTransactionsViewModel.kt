package kyung.kung_android.ui.expert_transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.domain.request.ServiceRequestRepository
import javax.inject.Inject

data class ExpertTransactionsUiState(
    val items: List<ServiceRequestResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ExpertTransactionsViewModel @Inject constructor(
    private val serviceRequestRepository: ServiceRequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpertTransactionsUiState())
    val state: StateFlow<ExpertTransactionsUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val all = serviceRequestRepository.getReceivedRequests()
                val completed = all.filter { it.status == "COMPLETED" }
                _state.update { it.copy(items = completed, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "거래 내역을 불러오지 못했어요.") }
            }
        }
    }
}
