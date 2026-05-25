package kyung.kung_android.security

import android.content.Context
import android.util.Log
import kyung.kung_android.dto.RootSignals

data class FridaCheckResult(
    val detected: Boolean,
    val signals: Map<String, Boolean>
)

data class AppSecurityCheckResult(
    val signatureSha256List: List<String>,
    val classesDexSha256: String?,
    val rootSignals: RootSignals,
    val fridaDetected: Boolean,
    val initialFridaDetected: Boolean,
    val currentFridaDetected: Boolean
)

object SecurityCheckManager {

    private const val TAG = "SecurityCheckManager"

    @Volatile
    private var initialFridaResult: FridaCheckResult? = null

    fun collectInitialFridaCheck(): FridaCheckResult {
        val result = collectFridaCheckResult()
        initialFridaResult = result
        return result
    }

    fun getInitialFridaResult(): FridaCheckResult? {
        return initialFridaResult
    }

    fun collectFridaCheckResult(): FridaCheckResult {
        return runCatching {
            val signals = NativeSecurityCheck.collectFridaDebugSignals()
            val detected = signals.values.any { it }

            FridaCheckResult(
                detected = detected,
                signals = signals
            )
        }.onFailure { e ->
            Log.w(TAG, "Frida check failed", e)
        }.getOrDefault(
            FridaCheckResult(
                detected = false,
                signals = emptyMap()
            )
        )
    }

    fun collectAppSecurityCheckResult(context: Context): AppSecurityCheckResult {
        val initialFridaDetected = initialFridaResult?.detected == true
        val currentFridaDetected = collectFridaCheckResult().detected

        return AppSecurityCheckResult(
            signatureSha256List = SignatureHashUtil.getSignatureSha256(context),
            classesDexSha256 = DexHashUtil.getClassesDexSha256(context),
            rootSignals = RootDetectionManager.collectRootSignals(context),
            fridaDetected = initialFridaDetected || currentFridaDetected,
            initialFridaDetected = initialFridaDetected,
            currentFridaDetected = currentFridaDetected
        )
    }
}