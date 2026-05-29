package kyung.kung_android.data.payment.crypto

import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class E2eCryptoSession(
    val aesKey: SecretKeySpec,
    val iv: IvParameterSpec,
    val aesKeyBytes: ByteArray,
    val ivBytes: ByteArray,
)
