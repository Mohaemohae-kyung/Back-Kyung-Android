package kyung.kung_android.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import kyung.kung_android.data.checkout.dto.BookingCheckoutResponse
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.theme.KungColors
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NUMBER_FMT = NumberFormat.getNumberInstance(Locale.KOREA)
private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREA)
private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCheckoutScreen(
    onBack: () -> Unit,
    onNavigateTossPayment: (orderId: String, amount: String, method: String, orderName: String) -> Unit,
    viewModel: BookingCheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BookingCheckoutEffect.NavigateToTossPayment ->
                    onNavigateTossPayment(effect.orderId, effect.amount, effect.paymentMethod, effect.orderName)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "예약 결제",
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
        bottomBar = {
            val amount = state.info?.finalAmount
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                KungPrimaryButton(
                    text = when {
                        state.isPaying -> "결제 중..."
                        amount != null -> "${NUMBER_FMT.format(amount)}원 결제하기"
                        else -> "결제하기"
                    },
                    onClick = { viewModel.startPayment() },
                    enabled = state.canPay,
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.info == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(text = state.error ?: "결제 정보를 불러오지 못했어요.") }

            else -> BookingCheckoutContent(
                info = requireNotNull(state.info),
                state = state,
                onAgreePrivacyChange = viewModel::onAgreePrivacyChange,
                onAgreeThirdPartyChange = viewModel::onAgreeThirdPartyChange,
                modifier = Modifier.padding(padding),
            )
        }
        if (state.isPaying) {
            Box(
                modifier = Modifier.fillMaxSize().background(KungColors.Charcoal.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun BookingCheckoutContent(
    info: BookingCheckoutResponse,
    state: BookingCheckoutUiState,
    onAgreePrivacyChange: (Boolean) -> Unit,
    onAgreeThirdPartyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column {
            SectionTitle("예약 상품")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("상품명", info.productTitle ?: "-")
            InfoRow("고수명", info.expertDisplayName ?: "-")
        }

        HorizontalDivider()

        Column {
            SectionTitle("일정 정보")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("예약 날짜", info.startAt?.format(DATE_FMT) ?: "-")
            InfoRow("예약 시간", formatTimeRange(info.startAt, info.endAt))
            InfoRow("지역", info.locationName ?: info.locationText ?: "-")
        }

        HorizontalDivider()

        Column {
            SectionTitle("결제 수단")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "신용/체크카드",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        HorizontalDivider()

        Column {
            SectionTitle("결제 금액")
            Spacer(modifier = Modifier.height(8.dp))
            AmountRow("서비스 금액", info.baseAmount ?: BigDecimal.ZERO)
            if ((info.discountAmount ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                AmountRow("할인 금액", info.discountAmount ?: BigDecimal.ZERO, negative = true)
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            AmountRow("최종 결제 금액", info.finalAmount ?: BigDecimal.ZERO, emphasize = true)
        }

        HorizontalDivider()

        Column {
            SectionTitle("이용 동의")
            Spacer(modifier = Modifier.height(4.dp))
            AgreeRow(
                checked = state.agreePrivacy,
                label = "개인정보 수집 및 이용 동의 (필수)",
                onCheckedChange = onAgreePrivacyChange,
            )
            AgreeRow(
                checked = state.agreeThirdParty,
                label = "제3자 정보 공유 동의 (필수)",
                onCheckedChange = onAgreeThirdPartyChange,
            )
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: BigDecimal,
    emphasize: Boolean = false,
    negative: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val prefix = if (negative) "-" else ""
        Text(
            text = "$prefix${NUMBER_FMT.format(amount)}원",
            style = if (emphasize) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = if (negative) KungColors.Coral else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AgreeRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatTimeRange(start: LocalDateTime?, end: LocalDateTime?): String {
    if (start == null) return "-"
    val startStr = start.format(TIME_FMT)
    val endStr = end?.format(TIME_FMT)
    return if (endStr != null) "$startStr - $endStr" else startStr
}
