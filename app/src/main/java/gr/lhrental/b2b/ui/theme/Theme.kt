package gr.lhrental.b2b.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LhBlack,
    onPrimary = LhWhite,
    secondary = LhCharcoal,
    onSecondary = LhWhite,
    background = LhGrey100,
    onBackground = LhBlack,
    surface = LhWhite,
    onSurface = LhBlack,
    surfaceVariant = LhGrey200,
    onSurfaceVariant = LhGrey700,
    outline = LhGrey400,
    error = LhDanger,
)

private val DarkColors = darkColorScheme(
    primary = LhWhite,
    onPrimary = LhBlack,
    secondary = LhGrey200,
    onSecondary = LhBlack,
    background = LhBlack,
    onBackground = LhWhite,
    surface = LhCharcoal,
    onSurface = LhWhite,
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = LhGrey400,
    outline = LhGrey700,
    error = Color(0xFFE57373),
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
