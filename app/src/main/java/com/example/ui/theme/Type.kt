package com.example.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.FontPreset
import java.io.File

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal)
)

val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins, FontWeight.Normal)
)

fun resolveFontFamily(preset: FontPreset, customFilePath: String?): FontFamily {
    return when (preset) {
        FontPreset.CAIRO -> CairoFontFamily
        FontPreset.POPPINS -> PoppinsFontFamily
        FontPreset.SERIF -> FontFamily.Serif
        FontPreset.MONOSPACE -> FontFamily.Monospace
        FontPreset.SYSTEM -> FontFamily.Default
        FontPreset.CUSTOM_TTF -> {
            if (customFilePath != null) {
                try {
                    val fontFile = File(customFilePath)
                    if (fontFile.exists() && fontFile.length() > 0) {
                        val typeface = Typeface.createFromFile(fontFile)
                        FontFamily(typeface)
                    } else {
                        CairoFontFamily
                    }
                } catch (e: Exception) {
                    CairoFontFamily
                }
            } else {
                CairoFontFamily
            }
        }
    }
}

fun createAppTypography(
    preset: FontPreset = FontPreset.CAIRO,
    customFilePath: String? = null,
    scale: Float = 1.0f
): Typography {
    val fontFamily = resolveFontFamily(preset, customFilePath)
    val s = scale.coerceIn(0.8f, 1.3f)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (57 * s).sp,
            lineHeight = (64 * s).sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (45 * s).sp,
            lineHeight = (52 * s).sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * s).sp,
            lineHeight = (44 * s).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * s).sp,
            lineHeight = (40 * s).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28 * s).sp,
            lineHeight = (36 * s).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * s).sp,
            lineHeight = (32 * s).sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * s).sp,
            lineHeight = (28 * s).sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.5.sp
        )
    )
}

val DefaultTypography = createAppTypography()
