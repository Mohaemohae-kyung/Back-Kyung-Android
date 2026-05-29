package kyung.kung_android.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class AdminEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val next = getIntent().getStringExtra("next")

        val intent: Intent?

        if ("admin" == next) {
            // 취약 지점:
            // 관리자 검증 Activity를 거치지 않고 관리자 WebView로 직접 이동
            intent = Intent(this, AdminWebViewActivity::class.java)
        } else {
            // 정상 의도:
            // 관리자 페이지 접근 전 검증 Activity를 거침
            intent = Intent(this, AdminCheckActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}