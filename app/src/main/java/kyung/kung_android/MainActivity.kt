package kyung.kung_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import kyung.kung_android.data.auth.TokenStore
import kyung.kung_android.ui.navigation.AppNavHost
import kyung.kung_android.ui.navigation.AppRoute
import kyung.kung_android.ui.theme.KungTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDest = resolveStartDestination(
            rawExtra = intent.getStringExtra(EXTRA_START_DEST),
            hasToken = tokenStore.getAccessSync() != null,
        )

        setContent {
            KungTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(startDestination = startDest)
                }
            }
        }
    }

    private fun resolveStartDestination(rawExtra: String?, hasToken: Boolean): String {
        val isValid = rawExtra in VALID_ROUTES
        return when {
            !isValid -> if (hasToken) AppRoute.HOME else AppRoute.LOGIN
            rawExtra == AppRoute.HOME && !hasToken -> AppRoute.LOGIN
            else -> rawExtra!!
        }
    }

    companion object {
        const val EXTRA_START_DEST = "start_destination"

        private val VALID_ROUTES = setOf(AppRoute.LOGIN, AppRoute.SIGNUP, AppRoute.HOME)
    }
}
