package kyung.kung_android.ui.received_quote

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.ui.theme.KungColors
import java.time.format.DateTimeFormatter

@Composable
fun ReceivedQuoteScreen(
    onNavigateExpertSearch: () -> Unit,
    onNavigateQuoteDetail: (Long) -> Unit,
    viewModel: ReceivedQuoteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    if (state.inProgress.isEmpty() && state.pastRequests.isEmpty() && !state.isLoading) {
        EmptyState(onNavigateExpertSearch = onNavigateExpertSearch)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.inProgress.isNotEmpty()) {
            item { SectionLabel("진행 중") }
            items(state.inProgress, key = { it.requestId }) {
                QuoteCard(request = it, onClick = { onNavigateQuoteDetail(it.requestId) })
            }
        }
        if (state.pastRequests.isNotEmpty()) {
            item { SectionLabel("지난 요청") }
            items(state.pastRequests, key = { it.requestId }) {
                QuoteCard(request = it, onClick = { onNavigateQuoteDetail(it.requestId) })
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun QuoteCard(
    request: ServiceRequestResponse,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(status = request.status)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = request.createdAt?.format(DATE_FMT) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
internal fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "PENDING" -> "검토 중" to KungColors.Gray
        "CHATTING" -> "상담 진행" to KungColors.Purple
        "COMPLETED" -> "완료" to KungColors.Success
        "REJECTED" -> "거절됨" to KungColors.Error
        "CANCELLED" -> "취소됨" to KungColors.Disabled
        else -> status to KungColors.Gray
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun EmptyState(onNavigateExpertSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "아직 견적 요청이 없어요",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "원하는 고수에게 견적을 보내보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateExpertSearch) {
            Text("고수 찾으러 가기")
        }
    }
}

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
