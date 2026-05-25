package kyung.kung_android.security

object NativeSecurityCheck {

    init {
        runCatching {
            System.loadLibrary("native_security")
        }
    }

    external fun detectFridaInMaps(): Boolean
    external fun detectTracerPid(): Boolean
    external fun detectFridaPorts(): Boolean
    external fun detectSuspiciousLibraries(): Boolean

    external fun detectSuBinary(): Boolean
    external fun detectMagiskFiles(): Boolean
    external fun detectWritableMount(): Boolean
    external fun detectSuspiciousRootPaths(): Boolean
    external fun detectRootShell(): Boolean

    fun isFridaDetected(): Boolean {
        return runCatching {
            val mapsDetected = detectFridaInMaps()
            val tracerDetected = detectTracerPid()
            val portDetected = detectFridaPorts()
            val libraryDetected = detectSuspiciousLibraries()

            mapsDetected ||
                    tracerDetected ||
                    portDetected ||
                    libraryDetected
        }.getOrDefault(false)
    }

    fun collectFridaDebugSignals(): Map<String, Boolean> {
        return runCatching {
            mapOf(
                "mapsDetected" to detectFridaInMaps(),
                "tracerPidDetected" to detectTracerPid(),
                "fridaPortDetected" to detectFridaPorts(),
                "suspiciousLibraryDetected" to detectSuspiciousLibraries()
            )
        }.getOrDefault(
            mapOf(
                "mapsDetected" to false,
                "tracerPidDetected" to false,
                "fridaPortDetected" to false,
                "suspiciousLibraryDetected" to false
            )
        )
    }
}