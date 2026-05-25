package kyung.kung_android.integrity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kyung.kung_android.BuildConfig
import kyung.kung_android.dto.AppIntegrityReportRequest
import kyung.kung_android.dto.AppIntegrityReportResponse
import kyung.kung_android.dto.RootSignals
import kyung.kung_android.network.ApiService
import kyung.kung_android.security.DexHashUtil
import kyung.kung_android.security.RootDetectionManager
import kyung.kung_android.security.SignatureHashUtil

class AppIntegrityReporter(
    private val context: Context,
    private val apiService: ApiService
) {
    suspend fun report(): AppIntegrityReportResponse {
        val request = AppIntegrityReportRequest(
            packageName = context.packageName,
            versionCode = getVersionCode(),
            versionName = getVersionName(),
            buildType = BuildConfig.BUILD_TYPE,

            signatureSha256List = SignatureHashUtil.getSignatureSha256(context),
            classesDexSha256 = DexHashUtil.getClassesDexSha256(context),

            rootSignals = RootDetectionManager.collectRootSignals(context),
            fridaDetected = false
        )

        return apiService.reportAppIntegrity(request)
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