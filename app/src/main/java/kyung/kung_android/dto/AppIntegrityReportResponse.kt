package kyung.kung_android.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppIntegrityReportResponse(
    val validSignature: Boolean,
    val validDex: Boolean,
    val riskLevel: String,
    val reason: String
)