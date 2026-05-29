package kyung.kung_android.ui.payment_qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQrScanScreen(
    onBack: () -> Unit,
    onScanned: (requestId: Long, amount: String) -> Unit,
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val text = result.contents
        if (text.isNullOrBlank()) {
            error = "스캔이 취소됐어요."
            return@rememberLauncherForActivityResult
        }
        runCatching { PaymentQrPayload.decode(text) }
            .onSuccess { payload ->
                val nowSec = System.currentTimeMillis() / 1000
                if (payload.exp in 1..nowSec) {
                    error = "코드가 만료됐어요. 고수에게 코드를 다시 생성해 달라고 요청해주세요."
                    return@rememberLauncherForActivityResult
                }
                onScanned(payload.rid, payload.amt)
            }
            .onFailure {
                error = "결제 코드 형식이 아니에요."
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scanLauncher.launch(scanOptions())
        } else {
            error = "카메라 권한이 필요해요. 설정에서 권한을 허용해주세요."
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            scanLauncher.launch(scanOptions())
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "결제 코드 스캔",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = error ?: "카메라로 결제 코드를 비춰주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (error != null) {
                    Button(onClick = {
                        error = null
                        scanLauncher.launch(scanOptions())
                    }) {
                        Text("다시 스캔하기")
                    }
                }
            }
        }
    }
}

private fun scanOptions(): ScanOptions = ScanOptions().apply {
    setBeepEnabled(false)
    setOrientationLocked(true)
    setPrompt("화면 가운데 네모 안에 결제 코드를 맞춰주세요")
    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    setBarcodeImageEnabled(false)
    setCameraId(0)
}
