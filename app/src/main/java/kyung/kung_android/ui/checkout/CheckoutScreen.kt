package kyung.kung_android.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.PinKeypad
import kyung.kung_android.ui.common.SecureScreen
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.theme.KungColors
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val NUMBER_FMT = NumberFormat.getNumberInstance(Locale.KOREA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onNavigateTossPayment: (orderId: String, amount: String, method: String, requestId: Long, orderName: String) -> Unit,
    onNavigatePaymentPasswordSetup: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SecureScreen()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CheckoutEffect.NavigateToTossPayment ->
                    onNavigateTossPayment(effect.orderId, effect.amount, effect.paymentMethod, effect.requestId, effect.orderName)
                CheckoutEffect.NavigateToPaymentPasswordSetup ->
                    onNavigatePaymentPasswordSetup()
            }
        }
    }

    if (state.showPinDialog) {
        PaymentPinDialog(
            pin = state.pin,
            isError = state.error != null,
            onPinChange = viewModel::onPinChange,
            onConfirm = viewModel::confirmPin,
            onDismiss = viewModel::dismissPinDialog,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "결제",
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
            val amount = state.info?.let { state.displayFinalAmount }
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

            else -> CheckoutContent(
                state = state,
                onMethodSelected = viewModel::onMethodSelected,
                onCouponSelected = viewModel::onCouponSelected,
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
private fun CheckoutContent(
    state: CheckoutUiState,
    onMethodSelected: (String) -> Unit,
    onCouponSelected: (Long?) -> Unit,
    onAgreePrivacyChange: (Boolean) -> Unit,
    onAgreeThirdPartyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val info = state.info ?: return
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column {
            SectionTitle("주문 내역")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = info.requestTitle.orEmpty(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            info.expertDisplayName?.let {
                Text(
                    text = "고수 · $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column {
            SectionTitle("결제 수단")
            Spacer(modifier = Modifier.height(8.dp))
            MethodRow(
                label = "신용/체크카드",
                selected = true,
                onClick = {},
            )
        }

        HorizontalDivider()

        if (state.coupons.isNotEmpty()) {
            Column {
                SectionTitle("쿠폰")
                Spacer(modifier = Modifier.height(8.dp))
                CouponRow(
                    label = "쿠폰 미적용",
                    selected = state.selectedCouponId == null,
                    onClick = { onCouponSelected(null) },
                )
                state.coupons.forEach { coupon ->
                    val discount = coupon.discountAmount ?: BigDecimal.ZERO
                    CouponRow(
                        label = "${coupon.name ?: "쿠폰"} (-${NUMBER_FMT.format(discount)}원)",
                        selected = state.selectedCouponId == coupon.userCouponId,
                        onClick = { onCouponSelected(coupon.userCouponId) },
                    )
                }
            }
            HorizontalDivider()
        }

        Column {
            SectionTitle("결제 금액")
            Spacer(modifier = Modifier.height(8.dp))
            AmountRow("기본 금액", info.baseAmount ?: BigDecimal.ZERO)
            if ((info.discountAmount ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                AmountRow("할인", info.discountAmount ?: BigDecimal.ZERO, negative = true)
            }
            state.selectedCoupon?.discountAmount?.takeIf { it > BigDecimal.ZERO }?.let {
                AmountRow("쿠폰 할인", it, negative = true)
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            AmountRow(
                label = "총 결제 금액",
                amount = state.displayFinalAmount,
                emphasize = true,
            )
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
private fun PaymentPinDialog(
    pin: String,
    isError: Boolean,
    onPinChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("결제 비밀번호 입력") },
        text = {
            PinKeypad(
                value = pin,
                onValueChange = { v ->
                    onPinChange(v)
                    if (v.length == 6) onConfirm()
                },
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm,
                enabled = pin.length == 6,
            ) { Text("결제") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
private fun AgreeRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MethodRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) KungColors.PurpleBg else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) KungColors.Purple else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = KungColors.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun CouponRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) KungColors.PurpleBg else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) KungColors.Purple else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = KungColors.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
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
