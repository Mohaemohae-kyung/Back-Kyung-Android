package kyung.kung_android.data.auth

import android.util.Base64

data class EncryptedBlob(
    val iv: ByteArray,
    val ciphertext: ByteArray,
) {
    fun toBase64(): String {
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ctB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$ivB64:$ctB64"
    }

    companion object {
        fun fromBase64(value: String): EncryptedBlob? {
            val parts = value.split(":", limit = 2)
            if (parts.size != 2) return null
            return runCatching {
                EncryptedBlob(
                    iv = Base64.decode(parts[0], Base64.NO_WRAP),
                    ciphertext = Base64.decode(parts[1], Base64.NO_WRAP),
                )
            }.getOrNull()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedBlob) return false
        return iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int = 31 * iv.contentHashCode() + ciphertext.contentHashCode()
}
