package kyung.kung_android.ui.quote_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import javax.inject.Inject

data class QuoteDetailUiState(
    val requestId: Long = 0L,
    val quote: ServiceRequestResponse? = null,
    val expert: ExpertDetailResponse? = null,
    val isLoading: Boolean = false,
    val isCancelling: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val requestId: Long = savedStateHandle.get<Long>("requestId") ?: 0L

    private val _state = MutableStateFlow(QuoteDetailUiState(requestId = requestId))
    val state: StateFlow<QuoteDetailUiState> = _state.asStateFlow()

    fun onCancel() {
        viewModelScope.launch {
            _state.update { it.copy(isCancelling = true) }
            try {
                val updated = serviceRequestRepository.cancel(requestId)
                _state.update { it.copy(quote = updated, isCancelling = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isCancelling = false, error = "취소에 실패했어요.") }
            }
        }
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val quote = serviceRequestRepository.getRequest(requestId)
                _state.update { it.copy(quote = quote, isLoading = false) }
                quote.expertProfileId?.let { id ->
                    runCatching { expertRepository.getExpertDetail(id) }
                        .onSuccess { expert -> _state.update { it.copy(expert = expert) } }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
            }
        }
    }
}
