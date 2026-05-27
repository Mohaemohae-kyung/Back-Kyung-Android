package kyung.kung_android.ui.payment_history

import androidx.lifecycle.SavedStateHandle
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
    val title: String = "거래내역",
    val payments: List<PaymentResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val type: String? = savedStateHandle.get<String>("type")

    private val _state = MutableStateFlow(PaymentHistoryUiState(title = titleFor(type)))
    val state: StateFlow<PaymentHistoryUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = paymentRepository.getMyPayments()
                val filtered = if (type != null) {
                    list.filter { it.transactionType == type }
                } else {
                    list
                }
                _state.update { it.copy(payments = filtered, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "거래내역을 불러오지 못했어요.") }
            }
        }
    }

    private fun titleFor(type: String?): String = when (type) {
        "BOOKING" -> "마켓 거래내역"
        "SERVICE_REQUEST" -> "고수찾기 거래내역"
        else -> "거래내역"
    }
}
