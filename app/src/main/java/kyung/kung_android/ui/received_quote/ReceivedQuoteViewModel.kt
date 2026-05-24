package kyung.kung_android.ui.received_quote

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

data class ReceivedQuoteUiState(
    val inProgress: List<ServiceRequestResponse> = emptyList(),
    val pastRequests: List<ServiceRequestResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ReceivedQuoteViewModel @Inject constructor(
    private val serviceRequestRepository: ServiceRequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReceivedQuoteUiState())
    val state: StateFlow<ReceivedQuoteUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val requests = serviceRequestRepository.getMyRequests()
                val inProgress = requests.filter { it.status == "PENDING" || it.status == "CHATTING" }
                val past = requests.filter { it.status != "PENDING" && it.status != "CHATTING" }
                _state.update { it.copy(inProgress = inProgress, pastRequests = past, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "목록을 불러오지 못했어요.") }
            }
        }
    }
}
