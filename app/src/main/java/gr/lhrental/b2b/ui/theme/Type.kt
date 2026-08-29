package gr.lhrental.b2b.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System sans-serif stands in for the site's Josefin Sans until a licensed
// font asset is added under res/font.
private val LhFontFamily = FontFamily.SansSerif

val LhTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = LhFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = LhFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = LhFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = LhFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = LhFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = LhFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
)
