package kyung.kung_android.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_tokens")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cipher: KeystoreCipher,
) {
    private val ds = context.authDataStore
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
        val accessBlob = cipher.encrypt(access.toByteArray(Charsets.UTF_8)).toBase64()
        val refreshBlob = cipher.encrypt(refresh.toByteArray(Charsets.UTF_8)).toBase64()
        ds.edit { prefs ->
            prefs[Keys.ACCESS] = accessBlob
            prefs[Keys.REFRESH] = refreshBlob
        }
        cacheMutex.withLock {
            cachedAccess = access
            cachedRefresh = refresh
            primed = true
        }
        _isLoggedIn.value = true
    }

    suspend fun clear() {
        ds.edit { it.clear() }
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
            val prefs = ds.data.first()
            cachedAccess = prefs[Keys.ACCESS]?.let(::decryptOrNull)
            cachedRefresh = prefs[Keys.REFRESH]?.let(::decryptOrNull)
            primed = true
        }
        _isLoggedIn.value = cachedAccess != null
    }

    private suspend fun ensurePrimed() {
        if (!primed) prime()
    }

    private fun decryptOrNull(stored: String): String? {
        val blob = EncryptedBlob.fromBase64(stored) ?: return null
        return runCatching { String(cipher.decrypt(blob), Charsets.UTF_8) }.getOrNull()
    }

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
    }
}
