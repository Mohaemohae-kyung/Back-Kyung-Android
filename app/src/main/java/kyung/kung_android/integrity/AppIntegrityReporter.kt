package kyung.kung_android.integrity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kyung.kung_android.BuildConfig
import kyung.kung_android.dto.AppIntegrityReportRequest
import kyung.kung_android.dto.AppIntegrityReportResponse
import kyung.kung_android.network.ApiService
import kyung.kung_android.security.SecurityCheckManager
import kyung.kung_android.security.SecurityDebugLog

class AppIntegrityReporter(
    private val context: Context,
    private val apiService: ApiService
) {
    suspend fun report(): AppIntegrityReportResponse {
        val securityResult = SecurityCheckManager.collectAppSecurityCheckResult(context)

        val request = AppIntegrityReportRequest(
            packageName = context.packageName,
            versionCode = getVersionCode(),
            versionName = getVersionName(),
            buildType = BuildConfig.BUILD_TYPE,

            signatureSha256List = securityResult.signatureSha256List,
            classesDexSha256 = securityResult.classesDexSha256,

            rootSignals = securityResult.rootSignals,
            fridaDetected = securityResult.fridaDetected
        )

        SecurityDebugLog.d(
            "Reporter",
            buildString {
                append("report -> server | ")
                append("pkg=${request.packageName} ")
                append("v=${request.versionName}(${request.versionCode}) ")
                append("buildType=${request.buildType} | ")
                append("frida=${request.fridaDetected} ")
                append("root=${request.rootSignals} | ")
                append("sigCount=${request.signatureSha256List.size} ")
                append("dex=${request.classesDexSha256}")
            }
        )

        return try {
            apiService.reportAppIntegrity(request).also { response ->
                SecurityDebugLog.d("Reporter", "server response <- $response")
            }
        } catch (e: Exception) {
            SecurityDebugLog.e("Reporter", "reportAppIntegrity failed", e)
            throw e
        }
    }

    private fun getVersionCode(): Long {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        return packageInfo.longVersionCode
    }

    private fun getVersionName(): String {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        return packageInfo.versionName ?: "unknown"
    }
}