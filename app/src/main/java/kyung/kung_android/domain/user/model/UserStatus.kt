package kyung.kung_android.domain.user.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserStatusSerializer::class)
enum class UserStatus(val raw: String) {
    ACTIVE("ACTIVE"),
    DELETED("DELETED"),
    SUSPENDED("SUSPENDED"),
    UNKNOWN("");

    companion object {
        fun from(raw: String?): UserStatus =
            entries.firstOrNull { it.raw == raw } ?: UNKNOWN
    }
}

object UserStatusSerializer : KSerializer<UserStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserStatus) {
        require(value != UserStatus.UNKNOWN) {
            "UserStatus.UNKNOWN must not be serialized; it is a deserialize-only fallback."
        }
        encoder.encodeString(value.raw)
    }

    override fun deserialize(decoder: Decoder): UserStatus =
        UserStatus.from(decoder.decodeString())
}
