package kyung.kung_android.ui.payment_history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import kyung.kung_android.ui.common.KungPullToRefresh
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import kyung.kung_android.ui.theme.KungColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.data.payment.dto.PaymentResponse
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onBack: () -> Unit,
    onNavigateTransactionDetail: (paymentId: Long) -> Unit = {},
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "매칭온페이 거래내역",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        KungPullToRefresh(
            isLoading = state.isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        when {
            state.isLoading && state.payments.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.payments.isEmpty() -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = state.error ?: "거래내역이 없어요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.payments, key = { it.paymentId }) { item ->
                    PaymentCard(
                        item = item,
                        onClick = { onNavigateTransactionDetail(item.paymentId) },
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun PaymentCard(item: PaymentResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(KungColors.PurpleBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = transactionTypeLabel(item.transactionType),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = KungColors.Purple,
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = 8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KungColors.BgSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = paymentStatusLabel(item.paymentStatus),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = KungColors.Slate,
                    )
                }
            }
            Text(
                text = formatWon(item.paymentAmount ?: item.finalAmount),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            )
            item.orderId?.let {
                Text(
                    text = "주문번호 $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.paidAt?.let {
                Text(
                    text = it.replace("T", " ").take(16),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatWon(amount: java.math.BigDecimal?): String {
    val v = amount ?: return "—"
    return NumberFormat.getNumberInstance(Locale.KOREA).format(v) + "원"
}

private fun transactionTypeLabel(type: String?): String = when (type) {
    "BOOKING" -> "예약 결제"
    "SERVICE_REQUEST" -> "견적 결제"
    else -> type ?: "결제"
}

private fun paymentStatusLabel(status: String?): String = when (status) {
    "READY" -> "결제 대기"
    "PAID" -> "결제 완료"
    "CANCELLED" -> "결제 취소"
    "REFUNDED" -> "환불 완료"
    "FAILED" -> "결제 실패"
    else -> status ?: "—"
}
