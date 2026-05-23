package kyung.kung_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KungLightColorScheme = lightColorScheme(
    primary = KungColors.Purple,
    onPrimary = KungColors.White,
    primaryContainer = KungColors.PurpleBg,
    onPrimaryContainer = KungColors.PurpleDark,

    secondary = KungColors.Ink,
    onSecondary = KungColors.White,
    secondaryContainer = KungColors.BgSubtle,
    onSecondaryContainer = KungColors.Charcoal,

    tertiary = KungColors.Success,
    onTertiary = KungColors.White,
    tertiaryContainer = KungColors.SuccessBg,
    onTertiaryContainer = KungColors.Success,

    background = KungColors.White,
    onBackground = KungColors.Charcoal,

    surface = KungColors.White,
    onSurface = KungColors.Charcoal,
    surfaceVariant = KungColors.BgSurface,
    onSurfaceVariant = KungColors.Gray,

    outline = KungColors.Border,
    outlineVariant = KungColors.BorderSoft,

    error = KungColors.Error,
    onError = KungColors.White,
    errorContainer = KungColors.ErrorBg,
    onErrorContainer = KungColors.ErrorDark,
)

private val KungDarkColorScheme = darkColorScheme(
    primary = KungColors.PurpleLight,
    onPrimary = KungColors.White,
    primaryContainer = KungColors.PurpleDark,
    onPrimaryContainer = KungColors.PurpleBg,

    secondary = KungColors.BgSubtle,
    onSecondary = KungColors.Charcoal,
    secondaryContainer = KungColors.Slate,
    onSecondaryContainer = KungColors.White,

    tertiary = KungColors.Success,
    onTertiary = KungColors.White,
    tertiaryContainer = KungColors.Slate,
    onTertiaryContainer = KungColors.SuccessBg,

    background = KungColors.Charcoal,
    onBackground = KungColors.White,

    surface = KungColors.Ink,
    onSurface = KungColors.White,
    surfaceVariant = KungColors.Slate,
    onSurfaceVariant = KungColors.BgSubtle,

    outline = KungColors.Slate,
    outlineVariant = KungColors.Slate,

    error = KungColors.Error,
    onError = KungColors.White,
    errorContainer = KungColors.ErrorDark,
    onErrorContainer = KungColors.ErrorBg,
)

@Composable
fun KungTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) KungDarkColorScheme else KungLightColorScheme
    MaterialTheme(
        colorScheme = colors,
        shapes = KungShapes,
        content = content,
    )
}
