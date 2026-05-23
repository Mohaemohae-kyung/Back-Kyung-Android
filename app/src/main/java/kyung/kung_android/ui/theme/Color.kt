package kyung.kung_android.ui.theme

import androidx.compose.ui.graphics.Color

object KungColors {

    val Purple = Color(0xFF693BF2)
    val PurpleLight = Color(0xFF865FFF)
    val PurpleDark = Color(0xFF592CE0)
    val PurpleBg = Color(0xFFF1EEFF)
    val PurpleSoft = Color(0xFFE7E1FF)
    val Indigo = Color(0xFF4F3BE5)
    val Sky = Color(0xFF3BAFFF)
    val Mint = Color(0xFF22C7A0)
    val Pink = Color(0xFFFF5C9C)
    val Amber = Color(0xFFFFB23B)
    val Coral = Color(0xFFFF7867)

    val Charcoal = Color(0xFF1C242F)
    val Ink = Color(0xFF293341)
    val Slate = Color(0xFF465162)
    val Gray = Color(0xFF6A7685)
    val Hint = Color(0xFF8F9AAB)
    val Disabled = Color(0xFFAAB4BF)
    val Border = Color(0xFFC7CED6)
    val BorderSoft = Color(0xFFE0E5EB)
    val BgSubtle = Color(0xFFEFF1F5)
    val BgSurface = Color(0xFFF6F7F9)
    val BgRaised = Color(0xFFFBFBFD)
    val White = Color(0xFFFFFFFF)

    val ShadowSoft = Color(0x14000000)
    val ShadowMedium = Color(0x1F000000)

    val Success = Color(0xFF00A163)
    val SuccessBg = Color(0xFFE7FCEF)
    val Info = Color(0xFF0087FF)
    val InfoBg = Color(0xFFE7F4FF)
    val Warning = Color(0xFFFF7C11)
    val WarningYellow = Color(0xFFFFC300)
    val Error = Color(0xFFFF3541)
    val ErrorBg = Color(0xFFFDEBEC)
    val ErrorDark = Color(0xFFC11C26)

    val HeroGradient = listOf(Color(0xFF7B5BFF), Color(0xFF4F3BE5))
    val ExpertGradient = listOf(Color(0xFF1F2937), Color(0xFF3B3F66))

    private val AvatarPalette = listOf(
        listOf(Color(0xFF7B5BFF), Color(0xFF4F3BE5)),
        listOf(Color(0xFF22C7A0), Color(0xFF0E9F86)),
        listOf(Color(0xFFFF7867), Color(0xFFE4475C)),
        listOf(Color(0xFF3BAFFF), Color(0xFF2C7DD9)),
        listOf(Color(0xFFFFB23B), Color(0xFFEE7E1F)),
        listOf(Color(0xFFFF5C9C), Color(0xFFD3387C)),
    )

    fun avatarGradient(seed: String): List<Color> {
        if (seed.isEmpty()) return AvatarPalette[0]
        val idx = (seed.hashCode().let { if (it < 0) -it else it }) % AvatarPalette.size
        return AvatarPalette[idx]
    }

    private val CategoryAccents = mapOf(
        1L to listOf(Color(0xFF7B5BFF), Color(0xFF4F3BE5)),      // 취업/직무
        6L to listOf(Color(0xFFFFB23B), Color(0xFFEE7E1F)),      // 취미/자기계발
        11L to listOf(Color(0xFF22C7A0), Color(0xFF0E9F86)),     // 과외
        16L to listOf(Color(0xFFFF5C9C), Color(0xFFD3387C)),     // 외주
    )

    fun categoryGradient(id: Long): List<Color> =
        CategoryAccents[id] ?: listOf(Color(0xFF3BAFFF), Color(0xFF2C7DD9))
}
