package kyung.kung_android.data.payment.crypto

import kyung.kung_android.data.payment.dto.E2ePayloadRequest
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class E2eCryptoUtilImpl @Inject constructor() : E2eCryptoUtil {

    private val secureRandom = SecureRandom()
    private val rsaKeyFactory = KeyFactory.getInstance("RSA")
    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()

    override fun newSession(): E2eCryptoSession {
        val aesKeyBytes = ByteArray(AES_KEY_SIZE).also { secureRandom.nextBytes(it) }
        val ivBytes = ByteArray(IV_SIZE).also { secureRandom.nextBytes(it) }
        return E2eCryptoSession(
            aesKey = SecretKeySpec(aesKeyBytes, "AES"),
            iv = IvParameterSpec(ivBytes),
            aesKeyBytes = aesKeyBytes,
            ivBytes = ivBytes,
        )
    }

    override fun parsePublicKeyPem(pem: String): PublicKey {
        val cleaned = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val derBytes = base64Decoder.decode(cleaned)
        return rsaKeyFactory.generatePublic(X509EncodedKeySpec(derBytes))
    }

    override fun encryptPayload(
        plainJson: String,
        publicKey: PublicKey,
        session: E2eCryptoSession,
    ): E2ePayloadRequest {
        val aesKeyBase64 = base64Encoder.encodeToString(session.aesKeyBytes)
        val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        rsa.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedAesKey = rsa.doFinal(aesKeyBase64.toByteArray(Charsets.UTF_8))

        val aes = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aes.init(Cipher.ENCRYPT_MODE, session.aesKey, session.iv)
        val cipherText = aes.doFinal(plainJson.toByteArray(Charsets.UTF_8))

        return E2ePayloadRequest(
            encryptedAesKey = base64Encoder.encodeToString(encryptedAesKey),
            iv = base64Encoder.encodeToString(session.ivBytes),
            cipherText = base64Encoder.encodeToString(cipherText),
        )
    }

    override fun decryptResponse(cipherText: String, session: E2eCryptoSession): String {
        val cipherBytes = base64Decoder.decode(cipherText)
        val aes = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aes.init(Cipher.DECRYPT_MODE, session.aesKey, session.iv)
        val plainBytes = aes.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    private companion object {
        const val AES_KEY_SIZE = 32
        const val IV_SIZE = 16
    }
}
