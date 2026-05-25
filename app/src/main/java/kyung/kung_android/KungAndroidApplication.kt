package kyung.kung_android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.util.Log
import kyung.kung_android.security.SecurityCheckManager

/**
 * Hilt DI 부팅용 Application 클래스.
 * AndroidManifest.xml 의 <application android:name=".KungAndroidApplication"/> 에서 참조됨.
 */
@HiltAndroidApp
class KungAndroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val initialFridaResult = SecurityCheckManager.collectInitialFridaCheck()

        if (BuildConfig.DEBUG) {
            Log.d("AppSecurityCheck", "initialFridaDetected=${initialFridaResult.detected}")
            Log.d("AppSecurityCheck", "initialFridaSignals=${initialFridaResult.signals}")
        }
    }
}
