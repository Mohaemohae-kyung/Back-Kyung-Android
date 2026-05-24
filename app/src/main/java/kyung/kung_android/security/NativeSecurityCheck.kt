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

    fun isFridaDetected(): Boolean {
        return runCatching {
            var state = 0
            var result = false

            var mapsDetected = false
            var tracerDetected = false
            var portDetected = false
            var libraryDetected = false

            while (true) {
                when (state) {
                    0 -> {
                        mapsDetected = detectFridaInMaps()
                        state = 10
                    }

                    10 -> {
                        tracerDetected = detectTracerPid()
                        state = 20
                    }

                    20 -> {
                        portDetected = detectFridaPorts()
                        state = 30
                    }

                    30 -> {
                        libraryDetected = detectSuspiciousLibraries()
                        state = 40
                    }

                    40 -> {
                        result = mapsDetected ||
                                tracerDetected ||
                                portDetected ||
                                libraryDetected
                        state = 100
                    }

                    100 -> {
                        return@runCatching result
                    }

                    else -> {
                        return@runCatching true
                    }
                }
            }

            false
        }.getOrDefault(false)
    }

    fun collectFridaDebugSignals(): Map<String, Boolean> {
        return runCatching {
            var state = 0

            var mapsDetected = false
            var tracerDetected = false
            var portDetected = false
            var libraryDetected = false

            while (true) {
                when (state) {
                    0 -> {
                        mapsDetected = detectFridaInMaps()
                        state = nextState(state, 10)
                    }

                    10 -> {
                        tracerDetected = detectTracerPid()
                        state = nextState(state, 20)
                    }

                    20 -> {
                        portDetected = detectFridaPorts()
                        state = nextState(state, 30)
                    }

                    30 -> {
                        libraryDetected = detectSuspiciousLibraries()
                        state = 100
                    }

                    100 -> {
                        return@runCatching mapOf(
                            "mapsDetected" to mapsDetected,
                            "tracerPidDetected" to tracerDetected,
                            "fridaPortDetected" to portDetected,
                            "suspiciousLibraryDetected" to libraryDetected
                        )
                    }

                    else -> {
                        return@runCatching fallbackSignals()
                    }
                }
            }

            fallbackSignals()
        }.getOrDefault(fallbackSignals())
    }

    private fun nextState(current: Int, next: Int): Int {
        val mixed = current xor next

        return if ((mixed xor current xor next) == 0) {
            next
        } else {
            999
        }
    }

    private fun fallbackSignals(): Map<String, Boolean> {
        return mapOf(
            "mapsDetected" to false,
            "tracerPidDetected" to false,
            "fridaPortDetected" to false,
            "suspiciousLibraryDetected" to false
        )
    }
}