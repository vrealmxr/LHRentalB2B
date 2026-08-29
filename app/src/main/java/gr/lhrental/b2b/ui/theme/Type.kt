package gr.lhrental.b2b.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import gr.lhrental.b2b.R

// The live site itself sets this for the whole b2b portal (see b2b/css/mainv2.css).
val LhFontFamily = FontFamily(
    Font(R.font.josefin_sans_regular, FontWeight.Normal),
    Font(R.font.josefin_sans_medium, FontWeight.Medium),
    Font(R.font.josefin_sans_semibold, FontWeight.SemiBold),
    Font(R.font.josefin_sans_bold, FontWeight.Bold),
)

/**
 * Josefin Sans, downloaded straight from Google Fonts as a raw TTF (not run
 * through Android's font-asset pipeline), carries oversized ascent/descent
 * metrics — with no explicit lineHeight, Compose used the font's own huge
 * natural line box, which then got clipped inside anything with a fixed
 * height (buttons, chips: see the v0.2.0 screenshot). Explicit lineHeight +
 * includeFontPadding=false forces Compose to use OUR line box instead of
 * the font's.
 */
private fun lhStyle(size: Int, weight: FontWeight, lineHeight: Int) = TextStyle(
    fontFamily = LhFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

val LhTypography = Typography(
    headlineSmall = lhStyle(22, FontWeight.SemiBold, 28),
    titleLarge = lhStyle(18, FontWeight.SemiBold, 24),
    titleMedium = lhStyle(16, FontWeight.Medium, 22),
    bodyLarge = lhStyle(16, FontWeight.Normal, 22),
    bodyMedium = lhStyle(14, FontWeight.Normal, 20),
    labelLarge = lhStyle(14, FontWeight.Medium, 20),
)
