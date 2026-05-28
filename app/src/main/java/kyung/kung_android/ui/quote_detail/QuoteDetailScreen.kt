package kyung.kung_android.ui.quote_detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.ui.received_quote.StatusChip
import kyung.kung_android.ui.theme.KungColors
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    onBack: () -> Unit,
    onNavigateExpertDetail: (Long) -> Unit,
    onNavigateChat: (chatRoomId: Long) -> Unit,
    onNavigateCheckout: (requestId: Long) -> Unit = {},
    topBarTitle: String = "견적 상세",
    viewModel: QuoteDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCancelDialog by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topBarTitle,
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
        when {
            state.isLoading && state.quote == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.quote == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.error ?: "정보를 불러오지 못했어요.")
                }
            }
            else -> {
                QuoteDetailContent(
                    quote = requireNotNull(state.quote),
                    expert = state.expert,
                    isRequester = state.isRequester,
                    isReceivingExpert = state.isReceivingExpert,
                    isActing = state.isActing,
                    hasPaymentRequest = state.hasPaymentRequest,
                    onNavigateExpertDetail = onNavigateExpertDetail,
                    onNavigateChat = onNavigateChat,
                    onNavigateCheckout = onNavigateCheckout,
                    onRequestCancel = { showCancelDialog = true },
                    onApprove = { viewModel.onApprove() },
                    onReject = { viewModel.onReject() },
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("요청을 취소할까요?") },
            text = { Text("취소된 요청은 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.onCancel()
                }) { Text("취소하기") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("닫기") }
            },
        )
    }
}

@Composable
private fun QuoteDetailContent(
    quote: ServiceRequestResponse,
    expert: ExpertDetailResponse?,
    isRequester: Boolean,
    isReceivingExpert: Boolean,
    isActing: Boolean,
    hasPaymentRequest: Boolean,
    onNavigateExpertDetail: (Long) -> Unit,
    onNavigateChat: (Long) -> Unit,
    onNavigateCheckout: (Long) -> Unit,
    onRequestCancel: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                StatusChip(status = quote.status)
                Spacer(modifier = Modifier.height(8.dp))
                quote.createdAt?.let {
                    Text(
                        text = "${it.format(DATE_FMT)} 요청",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (!isReceivingExpert) {
            expert?.let {
                item {
                    ExpertCardRow(expert = it, onClick = { onNavigateExpertDetail(it.expertProfileId) })
                }
            }
        }

        if (isReceivingExpert) {
            item {
                RequesterCard(name = quote.requesterName)
            }
        }

        item {
            SectionTitle("요청 내용")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = quote.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = quote.content, style = MaterialTheme.typography.bodyMedium)
        }

        item {
            SectionTitle("희망 사항")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(
                label = "예산",
                value = quote.budget?.let { "${NUMBER_FMT.format(it)}원" } ?: "협의",
            )
            InfoRow(
                label = "일정",
                value = quote.preferredDate?.toLocalDate()?.format(DATE_FMT) ?: "협의",
            )
        }

        item {
            ActionArea(
                quote = quote,
                isRequester = isRequester,
                isReceivingExpert = isReceivingExpert,
                isActing = isActing,
                hasPaymentRequest = hasPaymentRequest,
                onNavigateChat = onNavigateChat,
                onNavigateCheckout = onNavigateCheckout,
                onRequestCancel = onRequestCancel,
                onApprove = onApprove,
                onReject = onReject,
            )
        }
    }
}

@Composable
private fun ExpertCardRow(expert: ExpertDetailResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun RequesterCard(name: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InitialAvatar(name = name ?: "?", size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "의뢰인",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = name ?: "—",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
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

@Composable
private fun ActionArea(
    quote: ServiceRequestResponse,
    isRequester: Boolean,
    isReceivingExpert: Boolean,
    isActing: Boolean,
    hasPaymentRequest: Boolean,
    onNavigateChat: (Long) -> Unit,
    onNavigateCheckout: (Long) -> Unit,
    onRequestCancel: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    when {
        isReceivingExpert && quote.status == "PENDING" -> {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isActing,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(54.dp),
                ) { Text(if (isActing) "처리 중..." else "거절") }
                Box(modifier = Modifier.weight(1f)) {
                    KungPrimaryButton(
                        text = if (isActing) "처리 중..." else "수락",
                        onClick = onApprove,
                        enabled = !isActing,
                    )
                }
            }
        }
        isReceivingExpert && quote.status == "CHATTING" -> {
            KungPrimaryButton(
                text = "채팅방 가기",
                onClick = { quote.chatRoomId?.let(onNavigateChat) },
                enabled = quote.chatRoomId != null,
            )
        }
        isRequester && quote.status == "PENDING" -> {
            OutlinedButton(
                onClick = onRequestCancel,
                enabled = !isActing,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) { Text(if (isActing) "취소 중..." else "요청 취소") }
        }
        isRequester && quote.status == "CHATTING" -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (hasPaymentRequest) {
                    KungPrimaryButton(
                        text = "결제하기",
                        onClick = { onNavigateCheckout(quote.requestId) },
                    )
                }
                OutlinedButton(
                    onClick = { quote.chatRoomId?.let(onNavigateChat) },
                    enabled = quote.chatRoomId != null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) { Text("채팅방 가기") }
            }
        }
        quote.status == "REJECTED" -> StatusBanner("고수가 견적을 거절했어요")
        quote.status == "CANCELLED" -> StatusBanner("취소된 요청이에요")
        quote.status == "COMPLETED" -> StatusBanner("거래가 완료됐어요")
        else -> {}
    }
}

@Composable
private fun StatusBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(KungColors.BgSurface)
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KungColors.Slate,
        )
    }
}

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val NUMBER_FMT: NumberFormat = NumberFormat.getNumberInstance()
