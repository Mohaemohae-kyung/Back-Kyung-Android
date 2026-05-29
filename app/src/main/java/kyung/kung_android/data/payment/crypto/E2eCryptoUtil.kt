package kyung.kung_android.data.payment.crypto

import kyung.kung_android.data.payment.dto.E2ePayloadRequest
import java.security.PublicKey

interface E2eCryptoUtil {

    fun newSession(): E2eCryptoSession

    fun parsePublicKeyPem(pem: String): PublicKey

    fun encryptPayload(
        plainJson: String,
        publicKey: PublicKey,
        session: E2eCryptoSession,
    ): E2ePayloadRequest

    fun decryptResponse(cipherText: String, session: E2eCryptoSession): String
}
