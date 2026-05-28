package kyung.kung_android.ui.transaction_detail

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
import kyung.kung_android.data.payment.dto.PaymentResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.payment.PaymentRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import javax.inject.Inject

data class TransactionDetailUiState(
    val paymentId: Long = 0L,
    val payment: PaymentResponse? = null,
    val request: ServiceRequestResponse? = null,
    val expert: ExpertDetailResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val paymentId: Long = savedStateHandle.get<Long>("paymentId") ?: 0L

    private val _state = MutableStateFlow(TransactionDetailUiState(paymentId = paymentId))
    val state: StateFlow<TransactionDetailUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val payment = paymentRepository.getPayment(paymentId)
                _state.update { it.copy(payment = payment) }
                payment.serviceRequestId?.let { rid ->
                    runCatching { serviceRequestRepository.getRequest(rid) }
                        .onSuccess { req ->
                            _state.update { it.copy(request = req) }
                            req.expertProfileId?.let { pid ->
                                runCatching { expertRepository.getExpertDetail(pid) }
                                    .onSuccess { e -> _state.update { it.copy(expert = e) } }
                            }
                        }
                }
                _state.update { it.copy(isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "거래 정보를 불러오지 못했어요.") }
            }
        }
    }
}
