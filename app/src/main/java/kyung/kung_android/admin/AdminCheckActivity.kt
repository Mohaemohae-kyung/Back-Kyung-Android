package kyung.kung_android.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView

class AdminCheckActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAdmin = getIntent().getBooleanExtra("is_admin", false)

        if (isAdmin) {
            val intent = Intent(this, AdminWebViewActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val textView = TextView(this)
        textView.setText("관리자 권한이 없어 관리자 페이지 접근이 차단되었습니다.")
        textView.setTextSize(18f)
        textView.setPadding(40, 80, 40, 40)
        setContentView(textView)
    }
}