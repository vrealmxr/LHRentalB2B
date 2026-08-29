package gr.lhrental.b2b.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material3's color scheme has far more slots than the ones we used to set —
// anything left unset falls back to Material's own baseline tonal palette,
// which is a series of faint purple-greys, not our monochrome one. Recent
// Material3 versions also route common containers (Card, NavigationBar,
// ModalBottomSheet, DatePickerDialog...) through the newer "surface
// container" roles rather than plain `surface`. Leaving any of these unset
// is exactly how stray grey backgrounds sneak in app-wide, so every slot
// below is pinned explicitly to black/white/our named greys — nothing is
// left to Material's defaults. Grey is used ONLY for disabled/muted content
// (surfaceVariant, outline) — every "active" role is pure black or white.
private val LightColors = lightColorScheme(
    primary = LhBlack,
    onPrimary = LhWhite,
    primaryContainer = LhBlack,
    onPrimaryContainer = LhWhite,
    secondary = LhBlack,
    onSecondary = LhWhite,
    secondaryContainer = LhBlack,
    onSecondaryContainer = LhWhite,
    tertiary = LhBlack,
    onTertiary = LhWhite,
    tertiaryContainer = LhBlack,
    onTertiaryContainer = LhWhite,
    error = LhDanger,
    onError = LhWhite,
    errorContainer = LhDanger,
    onErrorContainer = LhWhite,
    background = LhWhite,
    onBackground = LhBlack,
    surface = LhWhite,
    onSurface = LhBlack,
    surfaceVariant = LhGrey200,
    onSurfaceVariant = LhGrey700,
    outline = LhGrey400,
    outlineVariant = LhGrey200,
    scrim = LhBlack,
    inverseSurface = LhBlack,
    inverseOnSurface = LhWhite,
    inversePrimary = LhWhite,
    surfaceTint = LhWhite, // matches `surface` exactly -> tonal elevation blend is invisible
    surfaceDim = LhWhite,
    surfaceBright = LhWhite,
    surfaceContainerLowest = LhWhite,
    surfaceContainerLow = LhWhite,
    surfaceContainer = LhWhite,
    surfaceContainerHigh = LhWhite,
    surfaceContainerHighest = LhWhite,
)

private val DarkColors = darkColorScheme(
    primary = LhWhite,
    onPrimary = LhBlack,
    primaryContainer = LhWhite,
    onPrimaryContainer = LhBlack,
    secondary = LhWhite,
    onSecondary = LhBlack,
    secondaryContainer = LhWhite,
    onSecondaryContainer = LhBlack,
    tertiary = LhWhite,
    onTertiary = LhBlack,
    tertiaryContainer = LhWhite,
    onTertiaryContainer = LhBlack,
    error = Color(0xFFE57373),
    onError = LhBlack,
    errorContainer = Color(0xFFE57373),
    onErrorContainer = LhBlack,
    background = LhBlack,
    onBackground = LhWhite,
    surface = LhBlack,
    onSurface = LhWhite,
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = LhGrey400,
    outline = LhGrey700,
    outlineVariant = Color(0xFF3A3A3A),
    scrim = LhBlack,
    inverseSurface = LhWhite,
    inverseOnSurface = LhBlack,
    inversePrimary = LhBlack,
    surfaceTint = LhBlack, // matches `surface` exactly -> tonal elevation blend is invisible
    surfaceDim = LhBlack,
    surfaceBright = LhBlack,
    surfaceContainerLowest = LhBlack,
    surfaceContainerLow = LhBlack,
    surfaceContainer = LhBlack,
    surfaceContainerHigh = LhBlack,
    surfaceContainerHighest = LhBlack,
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

/**
 * Material3's OutlinedTextField sources its UNFOCUSED border/label/icon color
 * from `colorScheme.outline`/`onSurfaceVariant` by default — i.e. every field
 * on the screen looks grey except the one currently focused. That reads as
 * "grey backgrounds everywhere" once a form has more than one field. This
 * keeps every non-disabled state pure black/white; only a genuinely disabled
 * field still falls back to the (grey) disabled* colors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun lhTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.onSurface,
)
