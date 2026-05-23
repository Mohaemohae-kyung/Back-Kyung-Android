package kyung.kung_android.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppIntegrityReportRequest(
    val packageName: String,
    val versionCode: Long,
    val versionName: String,
    val buildType: String,
    val signatureSha256List: List<String>,
    val classesDexSha256: String?,
    val rootSignals: RootSignals,
    val fridaDetected: Boolean
)