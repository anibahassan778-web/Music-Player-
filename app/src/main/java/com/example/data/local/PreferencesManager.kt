package com.example.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppSettings
import com.example.domain.model.AppThemeMode
import com.example.domain.model.ColorPreset
import com.example.domain.model.CornerPreset
import com.example.domain.model.FontPreset
import com.example.domain.model.VisualizerStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_LAST_SONG_ID = longPreferencesKey("last_played_song_id")
        val KEY_LAST_POSITION = longPreferencesKey("last_played_position")

        val KEY_THEME_MODE = stringPreferencesKey("pref_theme_mode")
        val KEY_COLOR_PRESET = stringPreferencesKey("pref_color_preset")
        val KEY_CUSTOM_PRIMARY_COLOR = longPreferencesKey("pref_custom_primary_color")
        val KEY_FONT_PRESET = stringPreferencesKey("pref_font_preset")
        val KEY_CUSTOM_FONT_FILE_PATH = stringPreferencesKey("pref_custom_font_file_path")
        val KEY_CUSTOM_FONT_FILE_NAME = stringPreferencesKey("pref_custom_font_file_name")
        val KEY_LANGUAGE_CODE = stringPreferencesKey("pref_language_code")
        val KEY_CORNER_PRESET = stringPreferencesKey("pref_corner_preset")
        val KEY_VISUALIZER_STYLE = stringPreferencesKey("pref_visualizer_style")
        val KEY_ENABLE_NEON_GLOW = booleanPreferencesKey("pref_enable_neon_glow")
        val KEY_ENABLE_BACKGROUND_BLUR = booleanPreferencesKey("pref_enable_background_blur")
        val KEY_FONT_SCALE = floatPreferencesKey("pref_font_scale")
        val KEY_CROSSFADE_SECONDS = androidx.datastore.preferences.core.intPreferencesKey("pref_crossfade_seconds")
    }

    val lastPlayedSongId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_SONG_ID]
    }

    val lastPlayedPosition: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_POSITION] ?: 0L
    }

    val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[KEY_THEME_MODE] ?: AppThemeMode.DARK.name
        val colorPresetStr = preferences[KEY_COLOR_PRESET] ?: ColorPreset.SKY_BLUE.name
        val fontPresetStr = preferences[KEY_FONT_PRESET] ?: FontPreset.CAIRO.name
        val cornerPresetStr = preferences[KEY_CORNER_PRESET] ?: CornerPreset.STANDARD.name
        val visualizerStr = preferences[KEY_VISUALIZER_STYLE] ?: VisualizerStyle.EQUALIZER_BARS.name

        AppSettings(
            themeMode = try { AppThemeMode.valueOf(themeModeStr) } catch (e: Exception) { AppThemeMode.DARK },
            colorPreset = try { ColorPreset.valueOf(colorPresetStr) } catch (e: Exception) { ColorPreset.SKY_BLUE },
            customPrimaryColor = preferences[KEY_CUSTOM_PRIMARY_COLOR] ?: 0xFF38BDF8,
            fontPreset = try { FontPreset.valueOf(fontPresetStr) } catch (e: Exception) { FontPreset.CAIRO },
            customFontFilePath = preferences[KEY_CUSTOM_FONT_FILE_PATH],
            customFontFileName = preferences[KEY_CUSTOM_FONT_FILE_NAME],
            languageCode = preferences[KEY_LANGUAGE_CODE] ?: "system",
            cornerPreset = try { CornerPreset.valueOf(cornerPresetStr) } catch (e: Exception) { CornerPreset.STANDARD },
            visualizerStyle = try { VisualizerStyle.valueOf(visualizerStr) } catch (e: Exception) { VisualizerStyle.EQUALIZER_BARS },
            enableNeonGlow = preferences[KEY_ENABLE_NEON_GLOW] ?: true,
            enableBackgroundBlur = preferences[KEY_ENABLE_BACKGROUND_BLUR] ?: true,
            fontScale = preferences[KEY_FONT_SCALE] ?: 1.0f,
            crossfadeSeconds = preferences[KEY_CROSSFADE_SECONDS] ?: 0
        )
    }

    suspend fun saveLastPlayed(songId: Long, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_SONG_ID] = songId
            preferences[KEY_LAST_POSITION] = position
        }
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun updateColorPreset(preset: ColorPreset) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COLOR_PRESET] = preset.name
        }
    }

    suspend fun updateCustomPrimaryColor(colorLong: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COLOR_PRESET] = ColorPreset.CUSTOM.name
            preferences[KEY_CUSTOM_PRIMARY_COLOR] = colorLong
        }
    }

    suspend fun updateFontPreset(preset: FontPreset) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_PRESET] = preset.name
        }
    }

    suspend fun updateLanguageCode(langCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE_CODE] = langCode
        }
    }

    suspend fun updateCornerPreset(preset: CornerPreset) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CORNER_PRESET] = preset.name
        }
    }

    suspend fun updateVisualizerStyle(style: VisualizerStyle) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VISUALIZER_STYLE] = style.name
        }
    }

    suspend fun updateNeonGlow(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ENABLE_NEON_GLOW] = enabled
        }
    }

    suspend fun updateBackgroundBlur(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ENABLE_BACKGROUND_BLUR] = enabled
        }
    }

    suspend fun updateFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_SCALE] = scale
        }
    }

    suspend fun updateCrossfadeSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CROSSFADE_SECONDS] = seconds
        }
    }

    suspend fun importCustomTtfFont(uri: Uri, fileName: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fontsDir = File(context.filesDir, "custom_fonts")
            if (!fontsDir.exists()) {
                fontsDir.mkdirs()
            }
            val targetFile = File(fontsDir, "user_font.ttf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Unable to read font file"))

            val displayName = fileName ?: "custom.ttf"

            context.dataStore.edit { preferences ->
                preferences[KEY_CUSTOM_FONT_FILE_PATH] = targetFile.absolutePath
                preferences[KEY_CUSTOM_FONT_FILE_NAME] = displayName
                preferences[KEY_FONT_PRESET] = FontPreset.CUSTOM_TTF.name
            }

            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCustomTtfFont() = withContext(Dispatchers.IO) {
        try {
            val fontsDir = File(context.filesDir, "custom_fonts")
            val targetFile = File(fontsDir, "user_font.ttf")
            if (targetFile.exists()) {
                targetFile.delete()
            }
            context.dataStore.edit { preferences ->
                preferences.remove(KEY_CUSTOM_FONT_FILE_PATH)
                preferences.remove(KEY_CUSTOM_FONT_FILE_NAME)
                preferences[KEY_FONT_PRESET] = FontPreset.CAIRO.name
            }
        } catch (e: Exception) {
            // Ignore error
        }
    }

    suspend fun resetCustomizationsToDefault() {
        removeCustomTtfFont()
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = AppThemeMode.DARK.name
            preferences[KEY_COLOR_PRESET] = ColorPreset.SKY_BLUE.name
            preferences[KEY_CUSTOM_PRIMARY_COLOR] = 0xFF38BDF8
            preferences[KEY_FONT_PRESET] = FontPreset.CAIRO.name
            preferences[KEY_CORNER_PRESET] = CornerPreset.STANDARD.name
            preferences[KEY_VISUALIZER_STYLE] = VisualizerStyle.EQUALIZER_BARS.name
            preferences[KEY_ENABLE_NEON_GLOW] = true
            preferences[KEY_ENABLE_BACKGROUND_BLUR] = true
            preferences[KEY_FONT_SCALE] = 1.0f
            preferences[KEY_LANGUAGE_CODE] = "system"
        }
    }
}
