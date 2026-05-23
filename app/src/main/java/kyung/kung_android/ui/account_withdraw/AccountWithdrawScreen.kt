package kyung.kung_android.ui.account_withdraw

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.theme.KungColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.ui.common.SectionTitle

private val REASONS = listOf(
    "NO_LONGER_USE" to "더 이상 사용하지 않아요",
    "OTHER_SERVICE" to "다른 서비스를 사용해요",
    "PRIVACY" to "개인정보가 걱정돼요",
    "OTHER" to "기타",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountWithdrawScreen(
    onBack: () -> Unit,
    onWithdrawSuccess: () -> Unit,
    viewModel: AccountWithdrawViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                WithdrawEvent.Success -> onWithdrawSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "계정 탈퇴",
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
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                KungPrimaryButton(
                    text = "탈퇴하기",
                    onClick = { confirmDialog = true },
                    enabled = state.canSubmit,
                    containerColor = MaterialTheme.colorScheme.error,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WarningBox()

            SectionTitle("탈퇴 사유")
            REASONS.forEach { (key, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = state.reasonKey == key,
                        onClick = { viewModel.onReasonChange(key) },
                    )
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (state.reasonKey == "OTHER") {
                OutlinedTextField(
                    value = state.customReason,
                    onValueChange = viewModel::onCustomReasonChange,
                    placeholder = { Text("사유를 입력해주세요") },
                    shape = RoundedCornerShape(14.dp),
                    colors = withdrawFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionTitle("비밀번호 확인")
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("비밀번호") },
                shape = RoundedCornerShape(14.dp),
                colors = withdrawFieldColors(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.agreed,
                    onCheckedChange = viewModel::onAgreedChange,
                )
                Text(
                    text = "위 안내를 확인했고 탈퇴에 동의합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (confirmDialog) {
        AlertDialog(
            onDismissRequest = { confirmDialog = false },
            title = { Text("정말 탈퇴하시겠어요?") },
            text = { Text("탈퇴하면 모든 정보가 즉시 삭제되며 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDialog = false
                    viewModel.submit()
                }) { Text("탈퇴", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDialog = false }) { Text("취소") } },
        )
    }
}

@Composable
private fun withdrawFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KungColors.Purple,
    unfocusedBorderColor = KungColors.BorderSoft,
    focusedContainerColor = KungColors.BgRaised,
    unfocusedContainerColor = KungColors.BgSurface,
    cursorColor = KungColors.Purple,
)

@Composable
private fun WarningBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "탈퇴하면 다음 정보가 모두 사라집니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            listOf(
                "• 작성한 견적·게시글·댓글",
                "• 채팅 기록",
                "• 찜한 고수",
            ).forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

