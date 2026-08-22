package io.github.dhianapereira.inventario.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CondensedFontFamily = FontFamily(
    Typeface.create("sans-serif-condensed", Typeface.NORMAL),
)

private val TechnicalFontFamily = FontFamily.Monospace

internal val InventarioTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = (-1).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 52.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 46.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.25.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.25.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.4.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.6.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.7.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = CondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.8.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
)
