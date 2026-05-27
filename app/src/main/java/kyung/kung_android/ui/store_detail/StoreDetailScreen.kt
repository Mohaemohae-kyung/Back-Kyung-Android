package kyung.kung_android.ui.store_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import kyung.kung_android.data.store.dto.StoreProductResponse
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.store.StoreThumbnail
import kyung.kung_android.ui.store.formatPrice
import kyung.kung_android.ui.theme.KungColors
import java.time.LocalDate

private val WEEKDAY_KO = listOf("월", "화", "수", "목", "금", "토", "일")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    onBack: () -> Unit,
    onNavigateCheckout: (bookingId: Long) -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.loadProduct()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StoreDetailEffect.NavigateToCheckout -> onNavigateCheckout(effect.bookingId)
            }
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = "마켓 상세",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            state.product?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    KungPrimaryButton(
                        text = if (state.isPreparingBooking) "예약 준비 중..." else "예약하기",
                        onClick = { viewModel.reserve() },
                        enabled = state.canReserve,
                    )
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading && state.product == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.product == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.error ?: "정보를 불러오지 못했어요.")
                }
            }
            else -> {
                StoreDetailContent(
                    product = requireNotNull(state.product),
                    state = state,
                    onDateSelected = viewModel::onDateSelected,
                    onTimeSelected = viewModel::onTimeSelected,
                    onMoveWindow = viewModel::moveDateWindow,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun StoreDetailContent(
    product: StoreProductResponse,
    state: StoreDetailUiState,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (String) -> Unit,
    onMoveWindow: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                StoreThumbnail(url = product.thumbnailImageUrl, size = 220.dp)
            }
        }
        item {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                ),
            )
            val meta = listOfNotNull(product.categoryName, product.locationName).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatPrice(product),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = KungColors.Purple,
            )
        }
        item { HorizontalDivider() }
        item {
            SectionTitle("서비스 소개")
            Text(
                text = product.description.orEmpty().ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item { HorizontalDivider() }
        item {
            SectionTitle("예약 날짜")
            Spacer(modifier = Modifier.height(8.dp))
            DateCarousel(
                dateWindowStart = state.dateWindowStart,
                selectedDate = state.selectedDate,
                onMoveWindow = onMoveWindow,
                onDateSelected = onDateSelected,
            )
        }
        item {
            SectionTitle("예약 시간")
            Spacer(modifier = Modifier.height(8.dp))
            TimeGrid(
                slotStates = state.slotStates,
                selectedTime = state.selectedTime,
                onTimeSelected = onTimeSelected,
            )
            state.reserveError?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DateCarousel(
    dateWindowStart: LocalDate,
    selectedDate: LocalDate,
    onMoveWindow: (Long) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val dates = (0L..6L).map { dateWindowStart.plusDays(it) }
    val canMovePrev = dateWindowStart.isAfter(today)
    val startMonth = dates.first().monthValue
    val endMonth = dates.last().monthValue
    val title = if (startMonth == endMonth) "${startMonth}월 예약 가능 날짜"
    else "${startMonth}월 - ${endMonth}월 예약 가능 날짜"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = { onMoveWindow(-7) }, enabled = canMovePrev) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "이전",
                tint = if (canMovePrev) MaterialTheme.colorScheme.onSurface else KungColors.Hint,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        )
        IconButton(onClick = { onMoveWindow(7) }) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "다음")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        dates.forEach { date ->
            val selected = date == selectedDate
            val isToday = date == today
            val dayName = if (isToday) "오늘" else WEEKDAY_KO[date.dayOfWeek.value - 1]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) KungColors.Purple else MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = if (selected) KungColors.Purple else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) KungColors.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) KungColors.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun TimeGrid(
    slotStates: Map<String, SlotState>,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StoreDetailViewModel.TIME_OPTIONS.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowItems.forEach { time ->
                    TimeSlot(
                        time = time,
                        slotState = slotStates[time] ?: SlotState.CHECKING,
                        selected = selectedTime == time,
                        onClick = { onTimeSelected(time) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TimeSlot(
    time: String,
    slotState: SlotState,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = slotState == SlotState.AVAILABLE
    val reserved = slotState == SlotState.RESERVED
    val bg = when {
        selected -> KungColors.Purple
        available -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        selected -> KungColors.White
        available -> MaterialTheme.colorScheme.onSurface
        else -> KungColors.Hint
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (selected) KungColors.Purple else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(enabled = available, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (reserved) "예약됨" else time,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (reserved) TextDecoration.LineThrough else TextDecoration.None,
        )
    }
}
