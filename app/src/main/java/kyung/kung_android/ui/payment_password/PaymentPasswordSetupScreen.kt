package kyung.kung_android.ui.payment_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.PaymentPinField
import kyung.kung_android.ui.common.SecureScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPasswordSetupScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: PaymentPasswordSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SecureScreen()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PaymentPasswordSetupEffect.Done -> onDone()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "결제 비밀번호 설정",
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (state.step) {
                    PinStep.FIRST -> "결제에 사용할 6자리 비밀번호를 입력해주세요."
                    PinStep.CONFIRM -> "확인을 위해 한 번 더 입력해주세요."
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            PaymentPinField(
                value = state.pin,
                onValueChange = viewModel::onPinChange,
                label = if (state.step == PinStep.FIRST) "결제 비밀번호" else "결제 비밀번호 확인",
                isError = state.error != null,
                imeAction = ImeAction.Done,
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            KungPrimaryButton(
                text = if (state.step == PinStep.FIRST) "다음" else "설정 완료",
                onClick = viewModel::onProceed,
                enabled = state.canProceed,
                loading = state.isSubmitting,
            )
        }
    }
}
