package kyung.kung_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.ui.navigation.AppRoute
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val startDest = try {
                if (tokenStore.getAccess() != null) AppRoute.HOME else AppRoute.LOGIN
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                runCatching { tokenStore.clear() }
                AppRoute.LOGIN
            }
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_START_DEST, startDest)
            startActivity(intent)
            finish()
        }
    }
}
