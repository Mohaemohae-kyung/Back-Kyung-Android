package kyung.kung_android.security

import android.util.Log
import kyung.kung_android.BuildConfig

/**
 * 디버그 빌드에서만 출력하는 진단용 로거.
 *
 * BuildConfig.DEBUG 가 false 인 빌드에서는 각 메서드가 조기 반환한다.
 *
 * logcat 필터: `adb logcat -s SecCheck`
 */
internal object SecurityDebugLog {

    private const val TAG = "SecCheck"

    val enabled: Boolean
        get() = BuildConfig.DEBUG

    /** 일반 진단 메시지. */
    fun d(stage: String, message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "[$stage] $message")
    }

    /** 개별 시그널 결과(HIT / pass)를 한 줄로 출력. */
    fun signal(stage: String, name: String, detected: Boolean) {
        if (!BuildConfig.DEBUG) return
        val mark = if (detected) "HIT ▶" else "pass"
        Log.d(TAG, "[$stage] $name -> $mark")
    }

    /** 예외 발생 지점 추적용. */
    fun e(stage: String, message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        Log.e(TAG, "[$stage] $message", throwable)
    }
}
