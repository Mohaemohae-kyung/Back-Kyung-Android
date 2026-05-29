package kyung.kung_android.fcm

import android.content.Context

object FcmTokenStore {
    private const val PREFS = "fcm_token_store"
    private const val KEY_TOKEN = "fcm_token"

    fun cacheToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun cachedToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
}
