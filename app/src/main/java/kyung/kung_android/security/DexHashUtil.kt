package kyung.kung_android.security

import android.content.Context
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipFile

object DexHashUtil {

    fun getClassesDexSha256(context: Context): String? {
        val apkPath = context.applicationInfo.sourceDir

        return runCatching {
            ZipFile(apkPath).use { zipFile ->
                val entry = zipFile.getEntry("classes.dex") ?: return null

                zipFile.getInputStream(entry).use { input ->
                    sha256HexLower(input)
                }
            }
        }.getOrNull()
    }

    private fun sha256HexLower(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)

        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}