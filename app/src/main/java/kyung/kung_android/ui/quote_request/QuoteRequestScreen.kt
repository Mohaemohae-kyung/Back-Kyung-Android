package kyung.kung_android.ui.quote_request

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.theme.KungColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteRequestScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: QuoteRequestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scroll = rememberScrollState()
    var datePickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    QuoteRequestEffect.NavigateToReceivedQuote -> onSuccess()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "견적 요청",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.expert?.let { expert ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KungColors.PurpleBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, KungColors.PurpleSoft),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InitialAvatar(name = expert.displayName, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expert.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            expert.mainCategoryName?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KungColors.Slate,
                                )
                            }
                        }
                        Text(
                            text = "견적 요청",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = KungColors.Purple,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            FieldLabel("제목 *")
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = { Text("간단히 어떤 도움이 필요하신가요?") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = requestFieldColors(),
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            FieldLabel("내용 *")
            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                placeholder = { Text("자세한 요청 내용을 적어주세요") },
                shape = RoundedCornerShape(14.dp),
                colors = requestFieldColors(),
                isError = state.contentError != null,
                supportingText = state.contentError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )

            FieldLabel("희망 예산")
            OutlinedTextField(
                value = state.budgetText,
                onValueChange = viewModel::onBudgetChange,
                placeholder = { Text("예: 50000") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = requestFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("원", style = MaterialTheme.typography.labelLarge) },
            )

            FieldLabel("희망 일정")
            OutlinedTextField(
                value = state.preferredDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                onValueChange = { },
                placeholder = { Text("선택해주세요") },
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(14.dp),
                colors = requestFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { datePickerOpen = true }) {
                        Text(
                            text = if (state.preferredDate == null) "선택" else "변경",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = KungColors.Purple,
                        )
                    }
                },
            )

            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            KungPrimaryButton(
                text = "견적 요청하기",
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (datePickerOpen) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { datePickerOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.onDateChange(date)
                        }
                        datePickerOpen = false
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { datePickerOpen = false }) { Text("취소") }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = KungColors.Slate,
    )
}

@Composable
private fun requestFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KungColors.Purple,
    unfocusedBorderColor = KungColors.BorderSoft,
    focusedContainerColor = KungColors.BgRaised,
    unfocusedContainerColor = KungColors.BgSurface,
    cursorColor = KungColors.Purple,
)

