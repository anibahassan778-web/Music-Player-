package com.example.domain.model

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
    AMOLED
}

enum class ColorPreset(
    val titleResKey: String,
    val primaryLightHex: Long,
    val primaryDarkHex: Long,
    val secondaryHex: Long
) {
    SKY_BLUE("color_sky_blue", 0xFF0284C7, 0xFF38BDF8, 0xFF0EA5E9),
    CYAN("color_cyan", 0xFF0891B2, 0xFF22D3EE, 0xFF38BDF8),
    INDIGO("color_indigo", 0xFF4F46E5, 0xFF818CF8, 0xFF0284C7),
    SUNSET("color_sunset", 0xFFEA580C, 0xFFFB923C, 0xFFF59E0B),
    EMERALD("color_emerald", 0xFF059669, 0xFF34D399, 0xFF10B981),
    VIOLET("color_violet", 0xFF7C3AED, 0xFFA78BFA, 0xFFC084FC),
    CRIMSON("color_crimson", 0xFFDC2626, 0xFFF87171, 0xFFFB7185),
    ROSE_GOLD("color_rose_gold", 0xFFDB2777, 0xFFF472B6, 0xFFFB7185),
    DYNAMIC("color_dynamic", 0, 0, 0),
    CUSTOM("color_custom", 0, 0, 0)
}

enum class FontPreset(val titleResKey: String) {
    SYSTEM("font_system"),
    CAIRO("font_cairo"),
    POPPINS("font_poppins"),
    SERIF("font_serif"),
    MONOSPACE("font_monospace"),
    CUSTOM_TTF("font_custom_ttf")
}

enum class CornerPreset(val titleResKey: String, val radiusDp: Int) {
    EXTRA_ROUNDED("corner_extra_rounded", 24),
    STANDARD("corner_standard", 16),
    SHARP("corner_sharp", 6)
}

enum class VisualizerStyle(val titleResKey: String) {
    EQUALIZER_BARS("visualizer_bars"),
    SMOOTH_WAVE("visualizer_wave"),
    ENERGY_PULSE("visualizer_pulse"),
    NEON_GLOW("visualizer_glow")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val colorPreset: ColorPreset = ColorPreset.SKY_BLUE,
    val customPrimaryColor: Long = 0xFF38BDF8,
    val fontPreset: FontPreset = FontPreset.CAIRO,
    val customFontFilePath: String? = null,
    val customFontFileName: String? = null,
    val languageCode: String = "system",
    val cornerPreset: CornerPreset = CornerPreset.STANDARD,
    val visualizerStyle: VisualizerStyle = VisualizerStyle.EQUALIZER_BARS,
    val enableNeonGlow: Boolean = true,
    val enableBackgroundBlur: Boolean = true,
    val fontScale: Float = 1.0f,
    val crossfadeSeconds: Int = 0
)
