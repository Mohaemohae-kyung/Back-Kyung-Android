package kyung.kung_android.security

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.io.File
import kyung.kung_android.dto.RootSignals

object RootDetectionManager {

    private const val TAG = "RootDetection"

    fun collectRootSignals(context: Context): RootSignals {
        val rootSignals = RootSignals(
            suBinaryDetected =
                checkSuBinary() || NativeSecurityCheck.detectSuBinary(),

            magiskDetected =
                checkMagisk() || NativeSecurityCheck.detectMagiskFiles(),

            systemPartitionWritable =
                checkSystemRwMounted() || NativeSecurityCheck.detectWritableMount(),

            rootManagementAppDetected =
                checkRootApps(context),

            suspiciousSystemPathDetected =
                checkSuspiciousPaths() || NativeSecurityCheck.detectSuspiciousRootPaths(),

            rootShellExecutable =
                checkRootShellExecutable() || NativeSecurityCheck.detectRootShell()
        )

        Log.d(TAG, "rootSignals = $rootSignals")

        return rootSignals
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

        paths.forEach { path ->
            val exists = File(path).exists()
            Log.d(TAG, "suspiciousPath check: $path = $exists")
        }

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