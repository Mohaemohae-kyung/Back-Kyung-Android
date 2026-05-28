package kyung.kung_android.ui.transaction_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.payment.dto.PaymentResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.theme.KungColors
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NUMBER_FMT = NumberFormat.getNumberInstance(Locale.KOREA)
private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
private val DATE_ONLY_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
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
                        text = "거래 상세",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
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
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.payment == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(text = state.error ?: "거래 정보를 불러오지 못했어요.") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { AmountHeader(requireNotNull(state.payment)) }
                state.expert?.let { item { ExpertSection(it) } }
                state.request?.let { item { RequestSection(it) } }
                item { PaymentSection(requireNotNull(state.payment)) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun AmountHeader(payment: PaymentResponse) {
    val amount = payment.finalAmount ?: payment.totalAmount ?: BigDecimal.ZERO
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KungColors.PurpleBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = statusLabel(payment.paymentStatus),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = KungColors.Purple,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${NUMBER_FMT.format(amount)}원",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = KungColors.Charcoal,
            )
        }
    }
}

private fun statusLabel(status: String?): String = when (status) {
    "PAID" -> "결제 완료"
    "PENDING" -> "결제 대기 중"
    "CANCELLED" -> "결제 취소됨"
    "FAILED" -> "결제 실패"
    null -> "결제"
    else -> status
}

@Composable
private fun ExpertSection(expert: ExpertDetailResponse) {
    Column {
        SectionTitle("고수 정보")
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, KungColors.BorderSoft),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (expert.profileImageUrl != null) {
                    AsyncImage(
                        model = expert.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    InitialAvatar(name = expert.displayName, size = 48.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expert.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = listOfNotNull(expert.mainCategoryName, expert.mainLocationName).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestSection(request: ServiceRequestResponse) {
    Column {
        SectionTitle("요청 내용")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = request.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = request.content, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(10.dp))
        InfoRow(
            label = "예산",
            value = request.budget?.let { "${NUMBER_FMT.format(it)}원" } ?: "협의",
        )
        InfoRow(
            label = "희망일",
            value = request.preferredDate?.toLocalDate()?.format(DATE_ONLY_FMT) ?: "협의",
        )
    }
}

@Composable
private fun PaymentSection(payment: PaymentResponse) {
    Column {
        SectionTitle("결제 정보")
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(
            label = "결제 수단",
            value = methodLabel(payment.paymentMethod),
        )
        InfoRow(
            label = "결제 일시",
            value = payment.paidAt?.let { runCatching { java.time.LocalDateTime.parse(it).format(DATE_FMT) }.getOrNull() }
                ?: payment.createdAt?.let { runCatching { java.time.LocalDateTime.parse(it).format(DATE_FMT) }.getOrNull() }
                ?: "—",
        )
        payment.orderId?.let {
            InfoRow(label = "주문번호", value = it)
        }
        payment.cancelledAt?.let { ts ->
            InfoRow(
                label = "취소 일시",
                value = runCatching { java.time.LocalDateTime.parse(ts).format(DATE_FMT) }.getOrNull() ?: ts,
            )
        }
    }
}

private fun methodLabel(method: String?): String = when (method) {
    "MATCHING_ON_PAY" -> "매칭온페이 결제"
    "CARD" -> "신용/체크카드"
    "EASY_PAY" -> "간편결제"
    "BANK_TRANSFER" -> "계좌이체"
    null -> "—"
    else -> method
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
