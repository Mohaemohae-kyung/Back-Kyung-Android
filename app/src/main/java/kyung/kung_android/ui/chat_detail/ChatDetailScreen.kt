package kyung.kung_android.ui.chat_detail

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.KungPrimaryButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.data.chat.dto.ChatMessageResponse
import kyung.kung_android.ui.theme.KungColors
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateCheckout: (requestId: Long) -> Unit = {},
    onNavigateQrGenerate: (requestId: Long, amount: String) -> Unit = { _, _ -> },
    onNavigateQrScan: () -> Unit = {},
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showRequestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.qrEffect.collect { effect ->
            when (effect) {
                is ChatPaymentQrEffect.NavigateToGenerate ->
                    onNavigateQrGenerate(effect.requestId, effect.amount)
            }
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "채팅",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateQrScan) {
                        Text(
                            text = "QR 스캔",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            ChatInputBar(
                value = state.input,
                onValueChange = viewModel::updateInput,
                onSend = viewModel::send,
                enabled = state.isConnected,
            )
        },
        containerColor = KungColors.BgSurface,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!state.isConnected) {
                    ConnectionBanner()
                }
                state.linkedRequest?.let { req ->
                    val isOfflineMode = req.paymentMode == ChatDetailViewModel.MODE_OFFLINE
                    QuoteInfoCard(
                        request = req,
                        expert = state.linkedExpert,
                        showPayButton = state.isRequester && req.status == "CHATTING" && state.hasPaymentRequest,
                        onPayClick = { onNavigateCheckout(req.requestId) },
                        showRequestButton = state.isExpertSide && req.status == "CHATTING",
                        paymentRequested = state.hasPaymentRequest,
                        isRequestingPayment = state.isRequestingPayment,
                        onRequestPaymentClick = {
                            if (state.hasPaymentRequest && isOfflineMode) {
                                viewModel.regenerateQr()
                            } else {
                                showRequestDialog = true
                            }
                        },
                        isOfflineMode = isOfflineMode,
                    )
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(state.messages, key = { it.chatMessageId }) { msg ->
                        if (msg.messageType == "SYSTEM") {
                            SystemMessage(text = msg.content)
                        } else {
                            val isMine = state.currentUserId != null && msg.senderId == state.currentUserId
                            val otherName: String
                            val otherImageUrl: String?
                            when (msg.senderId) {
                                state.linkedExpert?.ownerUserId -> {
                                    otherName = state.linkedExpert?.displayName ?: "고수"
                                    otherImageUrl = state.linkedExpert?.profileImageUrl
                                }
                                state.linkedRequest?.userId -> {
                                    otherName = state.linkedRequest?.requesterName ?: "사용자"
                                    otherImageUrl = null
                                }
                                else -> {
                                    otherName = "상대"
                                    otherImageUrl = null
                                }
                            }
                            MessageBubble(
                                message = msg,
                                isMine = isMine,
                                otherName = otherName,
                                otherImageUrl = otherImageUrl,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRequestDialog) {
        PaymentRequestDialog(
            isRequesting = state.isRequestingPayment,
            onDismiss = { showRequestDialog = false },
            onSubmit = { serviceName, amount, paymentMode ->
                viewModel.requestPayment(serviceName, amount, paymentMode)
                showRequestDialog = false
            },
        )
    }
}

@Composable
private fun ConnectionBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KungColors.ErrorBg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "연결이 끊어졌어요. 다시 연결하는 중...",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = KungColors.ErrorDark,
        )
    }
}

@Composable
private fun SystemMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = KungColors.Charcoal.copy(alpha = 0.55f),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(KungColors.PurpleBg.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageResponse,
    isMine: Boolean,
    otherName: String,
    otherImageUrl: String?,
) {
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }
    val bubble: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(if (isMine) KungColors.Purple else KungColors.PurpleBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = message.content,
                color = if (isMine) KungColors.White else KungColors.Charcoal,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            )
        }
    }
    if (isMine) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) { bubble() }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            if (otherImageUrl != null) {
                AsyncImage(
                    model = otherImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                InitialAvatar(name = otherName, size = 32.dp)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = otherName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = KungColors.Slate,
                )
                Spacer(modifier = Modifier.size(4.dp))
                bubble()
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(KungColors.BgSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "메시지를 입력하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KungColors.Hint,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(
                    color = KungColors.Charcoal,
                    fontSize = 15.sp,
                ),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        val sendEnabled = enabled && value.isNotBlank()
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (sendEnabled) KungColors.Purple else KungColors.BgSubtle)
                .clickable(enabled = sendEnabled, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = "전송",
                tint = if (sendEnabled) KungColors.White else KungColors.Hint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun QuoteInfoCard(
    request: ServiceRequestResponse,
    expert: ExpertDetailResponse?,
    showPayButton: Boolean,
    onPayClick: () -> Unit,
    showRequestButton: Boolean = false,
    paymentRequested: Boolean = false,
    isRequestingPayment: Boolean = false,
    onRequestPaymentClick: () -> Unit = {},
    isOfflineMode: Boolean = false,
) {
    val numberFmt = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA) }
    val dateFmt = remember { java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        val otherName = expert?.displayName ?: request.requesterName ?: "상대방"
        Text(
            text = "상대방 · $otherName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = request.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row {
            Text(
                text = "예산",
                modifier = Modifier.widthIn(min = 48.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = request.budget?.let { "${numberFmt.format(it)}원" } ?: "협의",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.size(2.dp))
        Row {
            Text(
                text = "일정",
                modifier = Modifier.widthIn(min = 48.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = request.preferredDate?.toLocalDate()?.format(dateFmt) ?: "협의",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (showPayButton) {
            Spacer(modifier = Modifier.size(10.dp))
            KungPrimaryButton(
                text = "결제하기",
                onClick = onPayClick,
            )
        }
        if (showRequestButton) {
            Spacer(modifier = Modifier.size(10.dp))
            KungPrimaryButton(
                text = when {
                    isRequestingPayment -> "요청 중..."
                    paymentRequested && isOfflineMode -> "QR 다시 보기"
                    paymentRequested -> "결제 요청 완료"
                    else -> "결제 요청"
                },
                onClick = onRequestPaymentClick,
                enabled = !isRequestingPayment && (!paymentRequested || isOfflineMode),
            )
        }
    }
}

@Composable
private fun PaymentRequestDialog(
    isRequesting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (serviceName: String, amount: BigDecimal, paymentMode: String) -> Unit,
) {
    var serviceName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf(ChatDetailViewModel.MODE_ONLINE) }
    val amount = amountText.toBigDecimalOrNull()
    val canSubmit = serviceName.isNotBlank() &&
        amount != null && amount > BigDecimal.ZERO && !isRequesting

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("결제 요청") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "서비스명과 금액, 결제 방식을 입력해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("서비스명") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                    label = { Text("결제금액") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { paymentMode = ChatDetailViewModel.MODE_ONLINE },
                    ) {
                        Text(
                            text = if (paymentMode == ChatDetailViewModel.MODE_ONLINE) "● 비대면" else "○ 비대면",
                        )
                    }
                    TextButton(
                        onClick = { paymentMode = ChatDetailViewModel.MODE_OFFLINE },
                    ) {
                        Text(
                            text = if (paymentMode == ChatDetailViewModel.MODE_OFFLINE) "● 대면" else "○ 대면",
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (amount != null) onSubmit(serviceName.trim(), amount, paymentMode) },
                enabled = canSubmit,
            ) { Text(if (isRequesting) "요청 중..." else "요청하기") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}
