package kyung.kung_android.ui.chat_detail

sealed interface ChatPaymentQrEffect {
    data class NavigateToGenerate(
        val requestId: Long,
        val amount: String,
    ) : ChatPaymentQrEffect
}
