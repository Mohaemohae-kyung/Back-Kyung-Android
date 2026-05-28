package kyung.kung_android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kyung.kung_android.security.SecurityCheckManager

@HiltAndroidApp
class KungAndroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SecurityCheckManager.collectInitialFridaCheck()
    }
}
