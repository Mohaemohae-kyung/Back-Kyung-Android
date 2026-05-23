package kyung.kung_android.dto

import kotlinx.serialization.Serializable

@Serializable
data class RootSignals(
    val suBinaryDetected: Boolean,
    val magiskDetected: Boolean,
    val systemPartitionWritable: Boolean,
    val rootManagementAppDetected: Boolean,
    val suspiciousSystemPathDetected: Boolean,
    val rootShellExecutable: Boolean
)