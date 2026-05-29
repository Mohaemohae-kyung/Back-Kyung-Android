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
            var state = mixState(0x13, 0x27)
            var mask = 0
            var guard = 0x5A

            while (true) {
                when (state) {
                    mixState(0x13, 0x27) -> {
                        val hit = detectFridaInMaps()
                        if (hit) {
                            mask = mask or 0x01
                        }
                        SecurityDebugLog.signal("Native/Frida", "detectFridaInMaps", hit)

                        guard = guard xor 0x11
                        state = nextState(state, 0x2A, guard)
                    }

                    mixState(0x2A, 0x4B) -> {
                        val hit = detectTracerPid()
                        if (hit) {
                            mask = mask or 0x02
                        }
                        SecurityDebugLog.signal("Native/Frida", "detectTracerPid", hit)

                        guard = guard xor 0x23
                        state = nextState(state, 0x31, guard)
                    }

                    mixState(0x31, 0x6D) -> {
                        val hit = detectFridaPorts()
                        if (hit) {
                            mask = mask or 0x04
                        }
                        SecurityDebugLog.signal("Native/Frida", "detectFridaPorts", hit)

                        guard = guard xor 0x35
                        state = nextState(state, 0x44, guard)
                    }

                    mixState(0x44, 0x19) -> {
                        val hit = detectSuspiciousLibraries()
                        if (hit) {
                            mask = mask or 0x08
                        }
                        SecurityDebugLog.signal("Native/Frida", "detectSuspiciousLibraries", hit)

                        guard = guard xor 0x47
                        state = nextState(state, 0x7F, guard)
                    }

                    mixState(0x7F, 0x08) -> {
                        val normalized = mask and 0x0F
                        val result = normalized != 0
                        SecurityDebugLog.d(
                            "Native/Frida",
                            "isFridaDetected mask=0x%02X -> %s".format(normalized, result)
                        )
                        return@runCatching result
                    }

                    else -> {
                        return@runCatching true
                    }
                }
            }

            true
        }.getOrDefault(false)
    }

    private fun mixState(a: Int, b: Int): Int {
        return (a xor b) + ((a and 0x0F) shl 2)
    }

    private fun nextState(current: Int, marker: Int, guard: Int): Int {
        val noise = (current xor guard) and 0x03

        return when (marker) {
            0x2A -> {
                val candidate = mixState(0x2A, 0x4B)
                if (noise >= 0) candidate else -1
            }

            0x31 -> {
                val candidate = mixState(0x31, 0x6D)
                if ((guard xor guard) == 0) candidate else -1
            }

            0x44 -> {
                val candidate = mixState(0x44, 0x19)
                if (((current or guard) and 0x00) == 0) candidate else -1
            }

            0x7F -> {
                val candidate = mixState(0x7F, 0x08)
                if ((marker and 0x7F) == 0x7F) candidate else -1
            }

            else -> -1
        }
    }

    fun collectFridaDebugSignals(): Map<String, Boolean> {
        return runCatching {
            var state = mixState2(0x21, 0x34)
            var mask = 0
            var guard = 0x6C

            while (true) {
                when (state) {
                    mixState2(0x21, 0x34) -> {
                        val hit = detectFridaInMaps()
                        if (hit) {
                            mask = mask or 0x01
                        }
                        SecurityDebugLog.signal("Native/Signals", "mapsDetected", hit)

                        guard = guard xor 0x12
                        state = nextSignalState(state, 0x41, guard)
                    }

                    mixState2(0x41, 0x15) -> {
                        val hit = detectTracerPid()
                        if (hit) {
                            mask = mask or 0x02
                        }
                        SecurityDebugLog.signal("Native/Signals", "tracerPidDetected", hit)

                        guard = guard xor 0x24
                        state = nextSignalState(state, 0x52, guard)
                    }

                    mixState2(0x52, 0x2B) -> {
                        val hit = detectFridaPorts()
                        if (hit) {
                            mask = mask or 0x04
                        }
                        SecurityDebugLog.signal("Native/Signals", "fridaPortDetected", hit)

                        guard = guard xor 0x36
                        state = nextSignalState(state, 0x63, guard)
                    }

                    mixState2(0x63, 0x7A) -> {
                        val hit = detectSuspiciousLibraries()
                        if (hit) {
                            mask = mask or 0x08
                        }
                        SecurityDebugLog.signal("Native/Signals", "suspiciousLibraryDetected", hit)

                        guard = guard xor 0x48
                        state = nextSignalState(state, 0x7E, guard)
                    }

                    mixState2(0x7E, 0x09) -> {
                        val normalized = mask and 0x0F

                        val signals = mapOf(
                            "mapsDetected" to ((normalized and 0x01) != 0),
                            "tracerPidDetected" to ((normalized and 0x02) != 0),
                            "fridaPortDetected" to ((normalized and 0x04) != 0),
                            "suspiciousLibraryDetected" to ((normalized and 0x08) != 0)
                        )
                        SecurityDebugLog.d(
                            "Native/Signals",
                            "collectFridaDebugSignals mask=0x%02X -> %s".format(normalized, signals)
                        )
                        return@runCatching signals
                    }

                    else -> {
                        return@runCatching fallbackSignals()
                    }
                }
            }

            fallbackSignals()
        }.getOrDefault(fallbackSignals())
    }

    private fun mixState2(a: Int, b: Int): Int {
        return (a xor b) + ((a and 0x0F) shl 2)
    }

    private fun nextSignalState(current: Int, marker: Int, guard: Int): Int {
        val noise = (current xor guard) and 0x03

        return when (marker) {
            0x41 -> {
                val candidate = mixState2(0x41, 0x15)
                if (noise >= 0) candidate else -1
            }

            0x52 -> {
                val candidate = mixState2(0x52, 0x2B)
                if ((guard xor guard) == 0) candidate else -1
            }

            0x63 -> {
                val candidate = mixState2(0x63, 0x7A)
                if (((current or guard) and 0x00) == 0) candidate else -1
            }

            0x7E -> {
                val candidate = mixState2(0x7E, 0x09)
                if ((marker and 0x7E) == 0x7E) candidate else -1
            }

            else -> -1
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