package kyung.kung_android.data.auth

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kyung.kung_android.BuildConfig
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

    fun getAccessSync(): String? {
        if (!primed) runBlocking { prime() }
        return cachedAccess
    }

    fun getRefreshSync(): String? {
        if (!primed) runBlocking { prime() }
        return cachedRefresh
    }

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
    }

    suspend fun clear() {
        ds.edit { it.clear() }
        cacheMutex.withLock {
            cachedAccess = null
            cachedRefresh = null
            primed = true
        }
    }

    private suspend fun ensurePrimed() {
        if (!primed) prime()
    }

    private suspend fun prime() {
        cacheMutex.withLock {
            if (primed) return
            val prefs = ds.data.first()
            cachedAccess = prefs[Keys.ACCESS]?.let(::decryptOrNull)
            cachedRefresh = prefs[Keys.REFRESH]?.let(::decryptOrNull)
            primed = true
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "primed access=${cachedAccess != null} refresh=${cachedRefresh != null}")
            }
        }
    }

    private fun decryptOrNull(stored: String): String? {
        val blob = EncryptedBlob.fromBase64(stored)
        if (blob == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "stored blob format invalid")
            return null
        }
        return runCatching { String(cipher.decrypt(blob), Charsets.UTF_8) }
            .onFailure {
                if (BuildConfig.DEBUG) Log.w(TAG, "stored blob unreadable: ${it.javaClass.simpleName}")
            }
            .getOrNull()
    }

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
    }

    private companion object {
        private const val TAG = "TokenStore"
    }
}
