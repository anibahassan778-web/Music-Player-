package com.example.ui.theme

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppSettings
import com.example.domain.model.AppThemeMode
import com.example.domain.model.ColorPreset
import java.util.Locale

val LocalAppSettings = compositionLocalOf { AppSettings() }

@Composable
fun MyApplicationTheme(
    appSettings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = remember(appSettings.themeMode, appSettings.colorPreset, appSettings.customPrimaryColor, isSystemDark) {
        if (appSettings.colorPreset == ColorPreset.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val isDark = when (appSettings.themeMode) {
                AppThemeMode.SYSTEM -> isSystemDark
                AppThemeMode.DARK, AppThemeMode.AMOLED -> true
                AppThemeMode.LIGHT -> false
            }
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            getAppColorScheme(
                themeMode = appSettings.themeMode,
                preset = appSettings.colorPreset,
                customPrimaryColorHex = appSettings.customPrimaryColor,
                systemDark = isSystemDark
            )
        }
    }

    val typography = remember(appSettings.fontPreset, appSettings.customFontFilePath, appSettings.fontScale) {
        createAppTypography(
            preset = appSettings.fontPreset,
            customFilePath = appSettings.customFontFilePath,
            scale = appSettings.fontScale
        )
    }

    val shapes = remember(appSettings.cornerPreset) {
        createAppShapes(appSettings.cornerPreset)
    }

    // Determine Layout Direction & Locale
    val layoutDirection = remember(appSettings.languageCode) {
        when (appSettings.languageCode) {
            "ar" -> LayoutDirection.Rtl
            "en", "fr" -> LayoutDirection.Ltr
            else -> {
                val currentLocale = Locale.getDefault()
                if (currentLocale.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            }
        }
    }

    // Create localized configuration and context
    val currentConfig = LocalConfiguration.current
    val localizedConfig = remember(appSettings.languageCode, currentConfig) {
        val newConfig = Configuration(currentConfig)
        if (appSettings.languageCode != "system" && appSettings.languageCode.isNotEmpty()) {
            val targetLocale = Locale(appSettings.languageCode)
            Locale.setDefault(targetLocale)
            newConfig.setLocale(targetLocale)
            newConfig.setLayoutDirection(targetLocale)
        }
        newConfig
    }

    val localizedContext = remember(context, appSettings.languageCode) {
        if (appSettings.languageCode != "system" && appSettings.languageCode.isNotEmpty()) {
            val targetLocale = Locale(appSettings.languageCode)
            Locale.setDefault(targetLocale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(targetLocale)
            config.setLayoutDirection(targetLocale)
            context.createConfigurationContext(config)
        } else {
            context
        }
    }

    CompositionLocalProvider(
        LocalAppSettings provides appSettings,
        LocalAppCornerRadius provides appSettings.cornerPreset.radiusDp.dp,
        LocalLayoutDirection provides layoutDirection,
        LocalConfiguration provides localizedConfig,
        LocalContext provides localizedContext
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
