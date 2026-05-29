package kyung.kung_android

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.domain.user.UserRepository
import kyung.kung_android.fcm.FcmTokenStore
import kyung.kung_android.integrity.AppIntegrityReporter
import kyung.kung_android.network.ApiService
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore
    @Inject lateinit var apiService: ApiService
    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            tokenStore.prime()
            registerFcmTokenIfLoggedIn()
            if (BuildConfig.DEBUG) {
                moveToMain()
            } else {
                checkAppIntegrity()
            }
        }
    }

    private fun registerFcmTokenIfLoggedIn() {
        if (tokenStore.getAccessSync() == null) return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            FcmTokenStore.cacheToken(applicationContext, token)
            lifecycleScope.launch {
                runCatching { userRepository.registerFcmToken(token) }
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
