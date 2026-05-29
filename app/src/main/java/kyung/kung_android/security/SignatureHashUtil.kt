package kyung.kung_android.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object SignatureHashUtil {

    fun getSignatureSha256(context: Context): List<String> {
        return runCatching {
            var state = sigState(0x11, 0x23)
            var guard = context.packageName.length xor 0x5D

            var packageManager: PackageManager? = null
            var packageName = ""
            var signingInfo: android.content.pm.SigningInfo? = null
            var result: List<String> = emptyList()

            while (true) {
                when (state) {
                    sigState(0x11, 0x23) -> {
                        packageManager = context.packageManager
                        packageName = context.packageName

                        guard = guard xor 0x13
                        state = nextSigState(state, 0x31, guard)
                    }

                    sigState(0x31, 0x42) -> {
                        val pm = packageManager ?: return@runCatching emptyList()

                        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getPackageInfo(
                                packageName,
                                PackageManager.PackageInfoFlags.of(
                                    PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                                )
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getPackageInfo(
                                packageName,
                                PackageManager.GET_SIGNING_CERTIFICATES
                            )
                        }

                        signingInfo = packageInfo.signingInfo

                        guard = guard xor 0x27
                        state = if (signingInfo == null) {
                            sigState(0x7A, 0x0F)
                        } else {
                            nextSigState(state, 0x52, guard)
                        }
                    }

                    sigState(0x52, 0x6A) -> {
                        val info = signingInfo ?: return@runCatching emptyList()

                        val signatures = if (info.hasMultipleSigners()) {
                            info.apkContentsSigners
                        } else {
                            info.signingCertificateHistory
                        }

                        var noise = guard and 0x03
                        if ((noise xor noise) != 0) {
                            return@runCatching emptyList()
                        }

                        result = signatures.map { signature ->
                            sha256ColonUpper(signature.toByteArray())
                        }
                        SecurityDebugLog.d(
                            "Signature",
                            "multipleSigners=${info.hasMultipleSigners()} hashes=$result"
                        )

                        guard = guard xor 0x39
                        state = nextSigState(state, 0x64, guard)
                    }

                    sigState(0x64, 0x18) -> {
                        return@runCatching result
                    }

                    sigState(0x7A, 0x0F) -> {
                        SecurityDebugLog.d("Signature", "signingInfo == null -> empty")
                        return@runCatching emptyList()
                    }

                    else -> {
                        SecurityDebugLog.d("Signature", "state machine fell through (state=$state) -> empty")
                        return@runCatching emptyList()
                    }
                }
            }

            emptyList()
        }.getOrElse { throwable ->
            SecurityDebugLog.e("Signature", "getSignatureSha256 threw -> empty", throwable)
            emptyList()
        }
    }

    private fun sha256ColonUpper(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.joinToString(":") { "%02X".format(it) }
    }

    private fun sigState(a: Int, b: Int): Int {
        return (a xor b) + ((a and 0x0F) shl 1)
    }

    private fun nextSigState(current: Int, marker: Int, guard: Int): Int {
        val noise = (current xor guard) and 0x03

        return when (marker) {
            0x31 -> {
                val candidate = sigState(0x31, 0x42)
                if (noise >= 0) candidate else -1
            }

            0x52 -> {
                val candidate = sigState(0x52, 0x6A)
                if ((guard xor guard) == 0) candidate else -1
            }

            0x64 -> {
                val candidate = sigState(0x64, 0x18)
                if (((current or guard) and 0x00) == 0) candidate else -1
            }

            else -> -1
        }
    }
}