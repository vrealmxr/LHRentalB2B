package gr.lhrental.b2b.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LhOrange,
    onPrimary = Color.White,
    secondary = LhInk,
    onSecondary = Color.White,
    background = LhBackground,
    onBackground = LhInk,
    surface = LhSurface,
    onSurface = LhInk,
    surfaceVariant = LhBorder,
    onSurfaceVariant = LhInkSoft,
    error = LhDanger,
)

private val DarkColors = darkColorScheme(
    primary = LhOrangeLight,
    onPrimary = LhInk,
    secondary = LhOrangeLight,
    onSecondary = LhInk,
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFF87373),
)

@Composable
fun LhRentalB2bTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = LhTypography,
        content = content,
    )
}
