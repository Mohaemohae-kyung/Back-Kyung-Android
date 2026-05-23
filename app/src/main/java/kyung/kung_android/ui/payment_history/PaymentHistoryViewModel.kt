package kyung.kung_android.ui.payment_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.payment.dto.PaymentResponse
import kyung.kung_android.domain.payment.PaymentRepository
import javax.inject.Inject

data class PaymentHistoryUiState(
    val payments: List<PaymentResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentHistoryUiState())
    val state: StateFlow<PaymentHistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = paymentRepository.getMyPayments()
                _state.update { it.copy(payments = list, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "거래내역을 불러오지 못했어요.") }
            }
        }
    }
}
