package kyung.kung_android

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.integrity.AppIntegrityReporter
import kyung.kung_android.network.ApiService
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore
    @Inject lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            tokenStore.prime()
            if (BuildConfig.DEBUG) {
                moveToMain()
            } else {
                checkAppIntegrity()
            }
        }
    }

    private suspend fun checkAppIntegrity() {
        try {
            val response = withContext(Dispatchers.IO) {
                val reporter = AppIntegrityReporter(
                    context = this@SplashActivity,
                    apiService = apiService
                )
                reporter.report()
            }

            when (response.riskLevel) {
                "LOW" -> moveToMain()
                "WARN", "MEDIUM" -> moveToMain()
                "BLOCK", "HIGH" -> showBlockDialog(response.reason)
                else -> showBlockDialog("UNKNOWN_RISK_LEVEL")
            }
        } catch (e: Exception) {
            showBlockDialog("SECURITY_CHECK_FAILED")
        }
    }

    private fun moveToMain() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    private fun showBlockDialog(reason: String?) {
        AlertDialog.Builder(this)
            .setTitle("보안 위험 감지")
            .setMessage(
                "앱 무결성 검증에 실패했습니다.\n\n" +
                        "사유: ${reason ?: "알 수 없음"}\n\n" +
                        "앱을 종료합니다."
            )
            .setCancelable(false)
            .setPositiveButton("종료") { _, _ ->
                finishAffinity()
            }
            .show()
    }
}
