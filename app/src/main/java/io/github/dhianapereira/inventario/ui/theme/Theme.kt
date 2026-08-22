package io.github.dhianapereira.inventario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = PaperRaised,
    primaryContainer = PaperMuted,
    onPrimaryContainer = Ink,
    secondary = InkMuted,
    onSecondary = PaperRaised,
    secondaryContainer = PaperMuted,
    onSecondaryContainer = Ink,
    tertiary = Line,
    onTertiary = PaperRaised,
    tertiaryContainer = PaperMuted,
    onTertiaryContainer = Ink,
    error = UtilityRed,
    onError = PaperRaised,
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD5),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFF3B0805),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperMuted,
    onSurfaceVariant = InkMuted,
    outline = Line,
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFC9C5BD),
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = Chalk,
    scrim = Ink,
)

private val DarkColorScheme = darkColorScheme(
    primary = Chalk,
    onPrimary = Night,
    primaryContainer = NightMuted,
    onPrimaryContainer = Chalk,
    secondary = ChalkMuted,
    onSecondary = Night,
    secondaryContainer = NightMuted,
    onSecondaryContainer = Chalk,
    tertiary = NightLine,
    onTertiary = Night,
    tertiaryContainer = NightMuted,
    onTertiaryContainer = Chalk,
    error = UtilityRedDark,
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    background = Night,
    onBackground = Chalk,
    surface = Night,
    onSurface = Chalk,
    surfaceVariant = NightMuted,
    onSurfaceVariant = ChalkMuted,
    outline = NightLine,
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF474640),
    inverseSurface = Chalk,
    inverseOnSurface = Night,
    inversePrimary = Ink,
    scrim = androidx.compose.ui.graphics.Color.Black,
)

@Composable
fun InventarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = InventarioTypography,
        shapes = InventarioShapes,
        content = content,
    )
}
