package kyung.kung_android.ui.checkout

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentInfo
import kyung.kung_android.BuildConfig
import kyung.kung_android.ui.common.SecureScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TossPaymentScreen(
    onBack: () -> Unit,
    onPaymentSuccess: (paymentId: Long) -> Unit,
    viewModel: TossPaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SecureScreen()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            TossPayments.RESULT_PAYMENT_SUCCESS -> {
                val success = result.data
                    ?.getParcelableExtra<TossPaymentResult.Success>(TossPayments.EXTRA_PAYMENT_RESULT_SUCCESS)
                if (success != null) viewModel.onPaymentSuccess(success.paymentKey)
                else viewModel.onPaymentFailed("결제 정보를 확인할 수 없어요.")
            }

            TossPayments.RESULT_PAYMENT_FAILED -> {
                val fail = result.data
                    ?.getParcelableExtra<TossPaymentResult.Fail>(TossPayments.EXTRA_PAYMENT_RESULT_FAILED)
                viewModel.onPaymentFailed(fail?.errorMessage)
            }

            else -> viewModel.onPaymentFailed(null)
        }
    }

    LaunchedEffect(state.launched, state.orderId) {
        if (state.launched || state.orderId.isBlank() || state.amount.signum() <= 0) return@LaunchedEffect
        val activity = context as? Activity ?: return@LaunchedEffect
        viewModel.markLaunched()
        val info = TossCardPaymentInfo(
            orderId = state.orderId,
            orderName = state.orderName,
            amount = state.amount.toLong(),
        )
        TossPayments(BuildConfig.TOSS_CLIENT_KEY).requestCardPayment(activity, info, launcher)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TossPaymentEffect.Success -> onPaymentSuccess(effect.paymentId)
            }
        }
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
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
    }

    val error = state.error
    if (error != null) {
        val canRetryConfirm = state.pendingPaymentKey != null && !state.isConfirming
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text(text = "결제를 완료하지 못했어요") },
            text = { Text(text = error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissError()
                        if (canRetryConfirm) viewModel.retryConfirm()
                        else viewModel.retryPayment()
                    },
                ) { Text(text = "다시 시도") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    onBack()
                }) { Text(text = "닫기") }
            },
        )
    }
}
