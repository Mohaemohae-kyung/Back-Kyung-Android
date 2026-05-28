package kyung.kung_android.security

import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import kyung.kung_android.dto.RootSignals

object RootDetectionManager {

    fun collectRootSignals(context: Context): RootSignals {
        return runCatching {
            var state = rootState(0x21, 0x43)
            var guard = context.packageName.length xor 0x5A
            var mask = 0

            while (true) {
                when (state) {
                    rootState(0x21, 0x43) -> {
                        val detected = checkSuBinary() || NativeSecurityCheck.detectSuBinary()
                        if (detected) {
                            mask = mask or 0x01
                        }

                        guard = guard xor 0x11
                        state = nextRootState(state, 0x31, guard)
                    }

                    rootState(0x31, 0x55) -> {
                        val detected = checkMagisk() || NativeSecurityCheck.detectMagiskFiles()
                        if (detected) {
                            mask = mask or 0x02
                        }

                        guard = guard xor 0x23
                        state = nextRootState(state, 0x42, guard)
                    }

                    rootState(0x42, 0x6A) -> {
                        val detected = checkSystemRwMounted() || NativeSecurityCheck.detectWritableMount()
                        if (detected) {
                            mask = mask or 0x04
                        }

                        guard = guard xor 0x35
                        state = nextRootState(state, 0x53, guard)
                    }

                    rootState(0x53, 0x12) -> {
                        val detected = checkRootApps(context)
                        if (detected) {
                            mask = mask or 0x08
                        }

                        guard = guard xor 0x47
                        state = nextRootState(state, 0x64, guard)
                    }

                    rootState(0x64, 0x7B) -> {
                        val detected = checkSuspiciousPaths() ||
                                NativeSecurityCheck.detectSuspiciousRootPaths()

                        if (detected) {
                            mask = mask or 0x10
                        }

                        guard = guard xor 0x59
                        state = nextRootState(state, 0x75, guard)
                    }

                    rootState(0x75, 0x2E) -> {
                        val detected = checkRootShellExecutable() ||
                                NativeSecurityCheck.detectRootShell()

                        if (detected) {
                            mask = mask or 0x20
                        }

                        guard = guard xor 0x6B
                        state = nextRootState(state, 0x7F, guard)
                    }

                    rootState(0x7F, 0x09) -> {
                        val normalized = mask and 0x3F

                        val rootSignals = RootSignals(
                            suBinaryDetected = (normalized and 0x01) != 0,
                            magiskDetected = (normalized and 0x02) != 0,
                            systemPartitionWritable = (normalized and 0x04) != 0,
                            rootManagementAppDetected = (normalized and 0x08) != 0,
                            suspiciousSystemPathDetected = (normalized and 0x10) != 0,
                            rootShellExecutable = (normalized and 0x20) != 0
                        )

                        return@runCatching rootSignals
                    }

                    else -> {
                        return@runCatching fallbackRootSignals()
                    }
                }
            }

            fallbackRootSignals()
        }.getOrDefault(fallbackRootSignals())
    }

    private fun rootState(a: Int, b: Int): Int {
        return (a xor b) + ((a and 0x0F) shl 2)
    }

    private fun nextRootState(current: Int, marker: Int, guard: Int): Int {
        val noise = (current xor guard) and 0x03

        return when (marker) {
            0x31 -> {
                val candidate = rootState(0x31, 0x55)
                if (noise >= 0) candidate else -1
            }

            0x42 -> {
                val candidate = rootState(0x42, 0x6A)
                if ((guard xor guard) == 0) candidate else -1
            }

            0x53 -> {
                val candidate = rootState(0x53, 0x12)
                if (((current or guard) and 0x00) == 0) candidate else -1
            }

            0x64 -> {
                val candidate = rootState(0x64, 0x7B)
                if ((marker and 0x64) == 0x64) candidate else -1
            }

            0x75 -> {
                val candidate = rootState(0x75, 0x2E)
                if ((noise xor noise) == 0) candidate else -1
            }

            0x7F -> {
                val candidate = rootState(0x7F, 0x09)
                if ((marker and 0x7F) == 0x7F) candidate else -1
            }

            else -> -1
        }
    }

    private fun fallbackRootSignals(): RootSignals {
        return RootSignals(
            suBinaryDetected = false,
            magiskDetected = false,
            systemPartitionWritable = false,
            rootManagementAppDetected = false,
            suspiciousSystemPathDetected = false,
            rootShellExecutable = false
        )
    }

    private fun checkSuBinary(): Boolean {
        val paths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/vendor/bin/su"
        )

        return paths.any { path ->
            File(path).exists()
        }
    }

    private fun checkMagisk(): Boolean {
        val paths = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules"
        )

        return paths.any { path ->
            File(path).exists()
        }
    }

    private fun checkSystemRwMounted(): Boolean {
        return try {
            val mounts = File("/proc/mounts").readLines()

            mounts.any { line ->
                val parts = line.split(" ")
                if (parts.size < 4) return@any false

                val mountPoint = parts[1]
                val options = parts[3].split(",")

                mountPoint in listOf("/system", "/vendor", "/product") &&
                        options.contains("rw")
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkRootApps(context: Context): Boolean {
        val packages = listOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.koushikdutta.superuser",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.devadvance.rootcloak"
        )

        return packages.any { packageName ->
            try {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_ACTIVITIES
                )
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkSuspiciousPaths(): Boolean {
        val paths = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules",
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server"
        )

        return paths.any { path ->
            File(path).exists()
        }
    }

    private fun checkRootShellExecutable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val exitCode = process.waitFor()

            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
}