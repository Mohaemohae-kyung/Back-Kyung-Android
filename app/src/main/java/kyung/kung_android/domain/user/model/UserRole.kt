package kyung.kung_android.domain.user.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserRoleSerializer::class)
enum class UserRole(val raw: String) {
    USER("USER"),
    EXPERT("EXPERT"),
    UNKNOWN("");

    companion object {
        fun from(raw: String?): UserRole =
            entries.firstOrNull { it.raw == raw } ?: UNKNOWN
    }
}

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) {
        require(value != UserRole.UNKNOWN) {
            "UserRole.UNKNOWN must not be serialized; it is a deserialize-only fallback."
        }
        encoder.encodeString(value.raw)
    }

    override fun deserialize(decoder: Decoder): UserRole =
        UserRole.from(decoder.decodeString())
}
