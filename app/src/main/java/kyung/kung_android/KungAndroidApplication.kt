package kyung.kung_android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt DI 부팅용 Application 클래스.
 * AndroidManifest.xml 의 <application android:name=".KungAndroidApplication"/> 에서 참조됨.
 */
@HiltAndroidApp
class KungAndroidApplication : Application()
