package kyung.kung_android.security

import android.content.Context
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipFile

object DexHashUtil {

    fun getClassesDexSha256(context: Context): String? {
        return runCatching {
            var state = dexState(0x12, 0x35)
            var guard = context.packageName.length xor 0x4A

            var apkPath = ""
            var result: String? = null

            while (true) {
                when (state) {
                    dexState(0x12, 0x35) -> {
                        apkPath = context.applicationInfo.sourceDir

                        guard = guard xor 0x11
                        state = nextDexState(state, 0x24, guard)
                    }

                    dexState(0x24, 0x48) -> {
                        ZipFile(apkPath).use { zipFile ->
                            val entry = zipFile.getEntry("classes.dex")

                            if (entry == null) {
                                state = dexState(0x7A, 0x0F)
                            } else {
                                zipFile.getInputStream(entry).use { input ->
                                    result = sha256HexLower(input)
                                }

                                guard = guard xor 0x23
                                state = nextDexState(state, 0x51, guard)
                            }
                        }
                    }

                    dexState(0x51, 0x6C) -> {
                        val noise = guard and 0x03

                        if ((noise xor noise) != 0) {
                            return@runCatching null
                        }

                        return@runCatching result
                    }

                    dexState(0x7A, 0x0F) -> {
                        return@runCatching null
                    }

                    else -> {
                        return@runCatching null
                    }
                }
            }

            null
        }.getOrNull()
    }

    private fun sha256HexLower(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)

        var state = hashState(0x14, 0x29)
        var finished = false

        while (!finished) {
            when (state) {
                hashState(0x14, 0x29) -> {
                    val read = input.read(buffer)

                    state = if (read <= 0) {
                        hashState(0x6E, 0x11)
                    } else {
                        digest.update(buffer, 0, read)
                        nextHashState(state, 0x33, read)
                    }
                }

                hashState(0x33, 0x57) -> {
                    state = hashState(0x14, 0x29)
                }

                hashState(0x6E, 0x11) -> {
                    finished = true
                }

                else -> {
                    finished = true
                }
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun dexState(a: Int, b: Int): Int {
        return (a xor b) + ((a and 0x0F) shl 2)
    }

    private fun nextDexState(current: Int, marker: Int, guard: Int): Int {
        val noise = (current xor guard) and 0x03

        return when (marker) {
            0x24 -> {
                val candidate = dexState(0x24, 0x48)
                if (noise >= 0) candidate else -1
            }

            0x51 -> {
                val candidate = dexState(0x51, 0x6C)
                if ((guard xor guard) == 0) candidate else -1
            }

            else -> -1
        }
    }

    private fun hashState(a: Int, b: Int): Int {
        return (a xor b) + ((b and 0x0F) shl 1)
    }

    private fun nextHashState(current: Int, marker: Int, read: Int): Int {
        val noise = (current xor read) and 0x07

        return when (marker) {
            0x33 -> {
                val candidate = hashState(0x33, 0x57)
                if (noise >= 0) candidate else -1
            }

            else -> -1
        }
    }
}