package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.domain.model.AppThemeMode
import com.example.domain.model.ColorPreset

val DarkBackgroundDefault = Color(0xFF090D16)
val DarkSurfaceDefault = Color(0xFF0F172A)
val DarkSurfaceVariantDefault = Color(0xFF1E293B)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0A)
val AmoledSurfaceVariant = Color(0xFF141414)

val LightBackgroundDefault = Color(0xFFF8FAFC)
val LightSurfaceDefault = Color(0xFFFFFFFF)
val LightSurfaceVariantDefault = Color(0xFFF1F5F9)

fun getAppColorScheme(
    themeMode: AppThemeMode,
    preset: ColorPreset,
    customPrimaryColorHex: Long,
    systemDark: Boolean
): ColorScheme {
    val isAmoled = themeMode == AppThemeMode.AMOLED
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.LIGHT -> false
    }

    val primaryColor: Color
    val secondaryColor: Color
    val tertiaryColor: Color

    when (preset) {
        ColorPreset.SKY_BLUE -> {
            primaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            secondaryColor = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0EA5E9)
            tertiaryColor = if (isDark) Color(0xFF22D3EE) else Color(0xFF0891B2)
        }
        ColorPreset.INDIGO -> {
            primaryColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
            secondaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            tertiaryColor = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)
        }
        ColorPreset.SUNSET -> {
            primaryColor = if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
            secondaryColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
            tertiaryColor = if (isDark) Color(0xFFF43F5E) else Color(0xFFBE123C)
        }
        ColorPreset.EMERALD -> {
            primaryColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
            secondaryColor = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
            tertiaryColor = if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857)
        }
        ColorPreset.CYAN -> {
            primaryColor = if (isDark) Color(0xFF22D3EE) else Color(0xFF0891B2)
            secondaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            tertiaryColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
        }
        ColorPreset.VIOLET -> {
            primaryColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
            secondaryColor = if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA)
            tertiaryColor = if (isDark) Color(0xFFF472B6) else Color(0xFFDB2777)
        }
        ColorPreset.CRIMSON -> {
            primaryColor = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
            secondaryColor = if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
            tertiaryColor = if (isDark) Color(0xFFFDA4AF) else Color(0xFFBE123C)
        }
        ColorPreset.ROSE_GOLD -> {
            primaryColor = if (isDark) Color(0xFFF472B6) else Color(0xFFDB2777)
            secondaryColor = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)
            tertiaryColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
        }
        ColorPreset.CUSTOM -> {
            val custom = Color(customPrimaryColorHex)
            primaryColor = custom
            secondaryColor = custom.copy(alpha = 0.85f)
            tertiaryColor = custom.copy(alpha = 0.7f)
        }
        ColorPreset.DYNAMIC -> {
            primaryColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
            secondaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            tertiaryColor = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)
        }
    }

    return if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = if (isDark && primaryColor.red * 0.299 + primaryColor.green * 0.587 + primaryColor.blue * 0.114 > 0.6) Color.Black else Color.White,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            tertiary = tertiaryColor,
            onTertiary = Color.Black,
            background = if (isAmoled) AmoledBackground else DarkBackgroundDefault,
            onBackground = Color(0xFFF1F5F9),
            surface = if (isAmoled) AmoledSurface else DarkSurfaceDefault,
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = if (isAmoled) AmoledSurfaceVariant else DarkSurfaceVariantDefault,
            onSurfaceVariant = Color(0xFF94A3B8)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = secondaryColor,
            onSecondary = Color.White,
            tertiary = tertiaryColor,
            onTertiary = Color.White,
            background = LightBackgroundDefault,
            onBackground = Color(0xFF0F172A),
            surface = LightSurfaceDefault,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightSurfaceVariantDefault,
            onSurfaceVariant = Color(0xFF64748B)
        )
    }
}
