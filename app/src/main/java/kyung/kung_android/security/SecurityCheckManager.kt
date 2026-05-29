package kyung.kung_android.security

import android.content.Context
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

    @Volatile
    private var initialFridaResult: FridaCheckResult? = null

    fun collectInitialFridaCheck(): FridaCheckResult {
        val result = collectFridaCheckResult()
        initialFridaResult = result
        SecurityDebugLog.d(
            "Manager",
            "initialFridaCheck detected=${result.detected} signals=${result.signals}"
        )
        return result
    }

    fun getInitialFridaResult(): FridaCheckResult? {
        return initialFridaResult
    }

    fun collectFridaCheckResult(): FridaCheckResult {
        return runCatching {
            val signals = NativeSecurityCheck.collectFridaDebugSignals()
            val detected = signals.values.any { it }

            SecurityDebugLog.signal("Manager", "fridaCheck", detected)
            FridaCheckResult(
                detected = detected,
                signals = signals
            )
        }.getOrElse { throwable ->
            SecurityDebugLog.e("Manager", "collectFridaCheckResult threw -> default(false)", throwable)
            FridaCheckResult(
                detected = false,
                signals = emptyMap()
            )
        }
    }

    fun collectAppSecurityCheckResult(context: Context): AppSecurityCheckResult {
        val initialFridaDetected = initialFridaResult?.detected == true
        val currentFridaDetected = collectFridaCheckResult().detected

        val result = AppSecurityCheckResult(
            signatureSha256List = SignatureHashUtil.getSignatureSha256(context),
            classesDexSha256 = DexHashUtil.getClassesDexSha256(context),
            rootSignals = RootDetectionManager.collectRootSignals(context),
            fridaDetected = initialFridaDetected || currentFridaDetected,
            initialFridaDetected = initialFridaDetected,
            currentFridaDetected = currentFridaDetected
        )

        SecurityDebugLog.d(
            "Manager",
            buildString {
                append("appSecurityCheck summary | ")
                append("frida(initial=$initialFridaDetected, current=$currentFridaDetected) | ")
                append("root=${result.rootSignals} | ")
                append("sigCount=${result.signatureSha256List.size} | ")
                append("dex=${result.classesDexSha256}")
            }
        )
        return result
    }
}