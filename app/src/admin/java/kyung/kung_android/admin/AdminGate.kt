package kyung.kung_android.admin

import android.content.Context
import android.content.Intent

object AdminGate {
    const val IS_ADMIN_BUILD: Boolean = true

    fun open(context: Context) {
        context.startActivity(Intent(context, AdminWebViewActivity::class.java))
    }
}
