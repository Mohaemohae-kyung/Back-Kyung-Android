package kyung.kung_android.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)
    private val cacheMutex = Mutex()

    @Volatile private var cachedAccess: String? = null
    @Volatile private var cachedRefresh: String? = null
    @Volatile private var primed: Boolean = false

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun getAccessSync(): String? = cachedAccess

    fun getRefreshSync(): String? = cachedRefresh

    suspend fun getAccess(): String? {
        ensurePrimed()
        return cachedAccess
    }

    suspend fun getRefresh(): String? {
        ensurePrimed()
        return cachedRefresh
    }

    suspend fun saveTokens(access: String, refresh: String) {
        prefs.edit()
            .putString(Keys.ACCESS, access)
            .putString(Keys.REFRESH, refresh)
            .apply()

        cacheMutex.withLock {
            cachedAccess = access
            cachedRefresh = refresh
            primed = true
        }

        _isLoggedIn.value = true
    }

    suspend fun clear() {
        prefs.edit().clear().apply()

        cacheMutex.withLock {
            cachedAccess = null
            cachedRefresh = null
            primed = true
        }

        _isLoggedIn.value = false
    }

    suspend fun prime() {
        cacheMutex.withLock {
            if (primed) return

            cachedAccess = prefs.getString(Keys.ACCESS, null)
            cachedRefresh = prefs.getString(Keys.REFRESH, null)
            primed = true
        }

        _isLoggedIn.value = cachedAccess != null
    }

    private suspend fun ensurePrimed() {
        if (!primed) prime()
    }

    private object Keys {
        const val ACCESS = "access_token"
        const val REFRESH = "refresh_token"
    }
}