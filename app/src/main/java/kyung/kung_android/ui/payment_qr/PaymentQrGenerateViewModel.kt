package kyung.kung_android.ui.payment_qr

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PaymentQrGenerateUiState(
    val requestId: Long = 0L,
    val amount: String = "0",
    val expiresAt: Long = 0L,
    val payloadText: String = "",
)

@HiltViewModel
class PaymentQrGenerateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val requestId: Long =
        savedStateHandle.get<String>("requestId")?.toLongOrNull() ?: 0L
    private val amount: String =
        savedStateHandle.get<String>("amount").orEmpty().ifBlank { "0" }
    private val expiresAt: Long =
        System.currentTimeMillis() / 1000 + DEFAULT_EXPIRE_SECONDS

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<PaymentQrGenerateUiState> = _state.asStateFlow()

    private fun initialState(): PaymentQrGenerateUiState {
        val payload = PaymentQrPayload(rid = requestId, amt = amount, exp = expiresAt)
        return PaymentQrGenerateUiState(
            requestId = requestId,
            amount = amount,
            expiresAt = expiresAt,
            payloadText = payload.encode(),
        )
    }

    companion object {
        private const val DEFAULT_EXPIRE_SECONDS = 600L
    }
}
