package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AppSettings
import com.example.domain.model.AppThemeMode
import com.example.domain.model.ColorPreset
import com.example.domain.model.CornerPreset
import com.example.domain.model.FontPreset
import com.example.domain.model.VisualizerStyle
import com.example.ui.theme.LocalAppCornerRadius

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    onNavigateBack: () -> Unit,
    onUpdateThemeMode: (AppThemeMode) -> Unit,
    onUpdateColorPreset: (ColorPreset) -> Unit,
    onUpdateCustomPrimaryColor: (Long) -> Unit,
    onUpdateCornerPreset: (CornerPreset) -> Unit,
    onUpdateVisualizerStyle: (VisualizerStyle) -> Unit,
    onUpdateNeonGlow: (Boolean) -> Unit,
    onUpdateBackgroundBlur: (Boolean) -> Unit,
    onResetCustomizationsToDefault: () -> Unit,
    onUpdateFontPreset: (FontPreset) -> Unit,
    onUpdateLanguageCode: (String) -> Unit,
    onUpdateFontScale: (Float) -> Unit,
    onImportCustomFont: (Uri, String?, (Boolean) -> Unit) -> Unit,
    onRemoveCustomFont: () -> Unit,
    onUpdateCrossfadeSeconds: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cornerRadius = LocalAppCornerRadius.current
    val primaryColor = MaterialTheme.colorScheme.primary
    var showResetDialog by remember { mutableStateOf(false) }

    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName: String? = null
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }

            onImportCustomFont(uri, fileName) { success ->
                val msg = if (success) {
                    context.getString(R.string.font_uploaded_success)
                } else {
                    context.getString(R.string.font_uploaded_error)
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. قسم وضع المظهر ونظام الألوان (Theme Mode & Color Palette)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(R.string.settings_theme_mode),
                    cornerRadius = cornerRadius
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Theme Mode Selection
                        Text(
                            text = stringResource(R.string.settings_theme_mode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val themeModes = listOf(
                            Triple(AppThemeMode.SYSTEM, stringResource(R.string.theme_system), "⚙️"),
                            Triple(AppThemeMode.DARK, stringResource(R.string.theme_dark), "🌙"),
                            Triple(AppThemeMode.LIGHT, stringResource(R.string.theme_light), "☀️"),
                            Triple(AppThemeMode.AMOLED, stringResource(R.string.theme_amoled), "🖤")
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            themeModes.forEach { (mode, title, emoji) ->
                                val isSelected = appSettings.themeMode == mode
                                val animatedBorderColor by animateColorAsState(
                                    targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    label = "themeBorder"
                                )
                                val animatedBgColor by animateColorAsState(
                                    targetValue = if (isSelected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    label = "themeBg"
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = animatedBgColor,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onUpdateThemeMode(mode) }
                                        .testTag("theme_mode_${mode.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // Accent Color Palette
                        Text(
                            text = stringResource(R.string.settings_color_palette),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val colorPresets = listOf(
                            ColorPreset.SKY_BLUE,
                            ColorPreset.CYAN,
                            ColorPreset.INDIGO,
                            ColorPreset.SUNSET,
                            ColorPreset.EMERALD,
                            ColorPreset.VIOLET,
                            ColorPreset.CRIMSON,
                            ColorPreset.ROSE_GOLD,
                            ColorPreset.DYNAMIC,
                            ColorPreset.CUSTOM
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorPresets.forEach { preset ->
                                val isSelected = appSettings.colorPreset == preset
                                val presetColor = when (preset) {
                                    ColorPreset.DYNAMIC -> MaterialTheme.colorScheme.primary
                                    ColorPreset.CUSTOM -> Color(appSettings.customPrimaryColor)
                                    else -> Color(preset.primaryDarkHex)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onUpdateColorPreset(preset) }
                                        .testTag("color_preset_${preset.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(presetColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = getColorPresetLabel(preset),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Custom color swatches if CUSTOM is selected
                        AnimatedVisibility(
                            visible = appSettings.colorPreset == ColorPreset.CUSTOM,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.pick_custom_color),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val swatches = listOf(
                                        0xFF0284C7, 0xFF0891B2, 0xFF06B6D4, 0xFF14B8A6,
                                        0xFF059669, 0xFF22C55E, 0xFFEAB308, 0xFFF59E0B,
                                        0xFFEA580C, 0xFFEF4444, 0xFFDC2626, 0xFFDB2777,
                                        0xFFD946EF, 0xFF7C3AED, 0xFF4F46E5, 0xFF6366F1
                                    )
                                    swatches.forEach { hex ->
                                        val isColorSelected = appSettings.customPrimaryColor == hex
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(hex))
                                                .border(
                                                    width = if (isColorSelected) 3.dp else 1.dp,
                                                    color = if (isColorSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.4f),
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    onUpdateColorPreset(ColorPreset.CUSTOM)
                                                    onUpdateCustomPrimaryColor(hex)
                                                }
                                                .testTag("custom_color_${hex.toString(16)}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isColorSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. قسم استدارة الحواف وشكل الواجهة (Corners & UI Shape)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.RoundedCorner,
                    title = stringResource(R.string.settings_ui_style),
                    subtitle = "تخصيص درجة استدارة البطاقات والأزرار",
                    cornerRadius = cornerRadius
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val cornerOptions = listOf(
                            CornerPreset.EXTRA_ROUNDED,
                            CornerPreset.STANDARD,
                            CornerPreset.SHARP
                        )
                        cornerOptions.forEach { preset ->
                            val isSelected = appSettings.cornerPreset == preset
                            Surface(
                                shape = RoundedCornerShape(preset.radiusDp.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(preset.radiusDp.dp))
                                    .clickable { onUpdateCornerPreset(preset) }
                                    .testTag("corner_preset_${preset.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = getCornerPresetLabel(preset),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${preset.radiusDp}dp radius",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. قسم المؤثر البصري والتوهج والضبابية (Visualizer & Effects)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.GraphicEq,
                    title = stringResource(R.string.settings_visualizer),
                    subtitle = "المؤثرات البصرية وتوهج المشغل",
                    cornerRadius = cornerRadius
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = stringResource(R.string.visualizer_type),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val styles = listOf(
                                VisualizerStyle.EQUALIZER_BARS,
                                VisualizerStyle.SMOOTH_WAVE,
                                VisualizerStyle.ENERGY_PULSE,
                                VisualizerStyle.NEON_GLOW
                            )
                            styles.forEach { style ->
                                val isSelected = appSettings.visualizerStyle == style
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onUpdateVisualizerStyle(style) }
                                        .testTag("visualizer_style_${style.name.lowercase()}")
                                ) {
                                    Text(
                                        text = getVisualizerStyleLabel(style),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // Neon Ambient Glow Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = stringResource(R.string.neon_ambient_glow),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.neon_glow_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = appSettings.enableNeonGlow,
                                onCheckedChange = { onUpdateNeonGlow(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = primaryColor
                                ),
                                modifier = Modifier.testTag("neon_glow_switch")
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // Frosted Background Blur Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = stringResource(R.string.player_blur_effect),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.player_blur_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = appSettings.enableBackgroundBlur,
                                onCheckedChange = { onUpdateBackgroundBlur(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = primaryColor
                                ),
                                modifier = Modifier.testTag("background_blur_switch")
                            )
                        }
                    }
                }
            }

            // 4. قسم لغة التطبيق (Language Selection)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = "اختر لغة الواجهة / Select interface language",
                    cornerRadius = cornerRadius
                ) {
                    val languages = listOf(
                        LanguageOption("system", stringResource(R.string.lang_system), "⚙️", "Auto"),
                        LanguageOption("ar", stringResource(R.string.lang_arabic), "🇸🇦", "العربية"),
                        LanguageOption("en", stringResource(R.string.lang_english), "🇺🇸", "English"),
                        LanguageOption("fr", stringResource(R.string.lang_french), "🇫🇷", "Français")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { lang ->
                            val isSelected = appSettings.languageCode == lang.code
                            val animatedBorderColor by animateColorAsState(
                                targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                label = "langBorder"
                            )
                            val animatedBgColor by animateColorAsState(
                                targetValue = if (isSelected) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                label = "langBg"
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = animatedBgColor,
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onUpdateLanguageCode(lang.code) }
                                    .testTag("lang_option_${lang.code}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) primaryColor.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = lang.flagEmoji, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lang.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = lang.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. قسم الخطوط والطباعة (Fonts & Typography)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.TextFields,
                    title = stringResource(R.string.settings_typography),
                    subtitle = "تخصيص نمط ونوع الخط وحجمه",
                    cornerRadius = cornerRadius
                ) {
                    Text(
                        text = stringResource(R.string.settings_font_family),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val fontPresets = listOf(
                        Pair(FontPreset.CAIRO, stringResource(R.string.font_cairo)),
                        Pair(FontPreset.POPPINS, stringResource(R.string.font_poppins)),
                        Pair(FontPreset.SERIF, stringResource(R.string.font_serif)),
                        Pair(FontPreset.MONOSPACE, stringResource(R.string.font_monospace)),
                        Pair(FontPreset.SYSTEM, stringResource(R.string.font_system))
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fontPresets.forEach { (preset, label) ->
                            val isSelected = appSettings.fontPreset == preset
                            val cleanLabel = label.substringBefore(" (")
                            val chipBorderColor by animateColorAsState(
                                targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                label = "fontChipBorder"
                            )
                            val chipBgColor by animateColorAsState(
                                targetValue = if (isSelected) primaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                label = "fontChipBg"
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = chipBgColor,
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, chipBorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onUpdateFontPreset(preset) }
                                    .testTag("font_chip_${preset.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = cleanLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // رفع خط مخصص (Custom Font Upload)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primaryColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.upload_custom_font),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (appSettings.customFontFileName != null) {
                                        stringResource(R.string.current_custom_font, appSettings.customFontFileName ?: "")
                                    } else {
                                        stringResource(R.string.upload_font_desc)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (appSettings.customFontFileName != null) {
                                IconButton(
                                    onClick = onRemoveCustomFont,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.remove_custom_font),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Button(
                                onClick = {
                                    fontPickerLauncher.launch(arrayOf("font/*", "application/x-font-ttf", "application/octet-stream", "*/*"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("pick_ttf_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.apply),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // مقياس حجم الخط (Font Scale)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.font_size_scale),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${(appSettings.fontScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Slider(
                        value = appSettings.fontScale,
                        onValueChange = onUpdateFontScale,
                        valueRange = 0.85f..1.25f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("font_scale_slider")
                    )

                    // صندوق معاينة الخط الحي
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "معاينة الخط / Font Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.font_preview_sample),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 6. قسم التلاشي التدريجي بين الأغاني (Crossfade)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.Audiotrack,
                    title = stringResource(R.string.settings_crossfade_title),
                    subtitle = stringResource(R.string.settings_crossfade_desc),
                    cornerRadius = cornerRadius
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (appSettings.crossfadeSeconds == 0) {
                                    stringResource(R.string.crossfade_disabled)
                                } else {
                                    stringResource(R.string.crossfade_seconds_format, appSettings.crossfadeSeconds)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (appSettings.crossfadeSeconds > 0) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (appSettings.crossfadeSeconds > 0) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (appSettings.crossfadeSeconds > 0) primaryColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (appSettings.crossfadeSeconds == 0) "0s" else "${appSettings.crossfadeSeconds}s",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (appSettings.crossfadeSeconds > 0) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Slider(
                            value = appSettings.crossfadeSeconds.toFloat(),
                            onValueChange = { onUpdateCrossfadeSeconds(it.toInt()) },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor,
                                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crossfade_slider")
                        )

                        // Quick presets row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(0, 2, 4, 6, 8)
                            presets.forEach { sec ->
                                val isSelected = appSettings.crossfadeSeconds == sec
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onUpdateCrossfadeSeconds(sec) }
                                        .testTag("crossfade_preset_$sec")
                                 ) {
                                    Text(
                                        text = if (sec == 0) "Off" else "${sec}s",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. قسم استعادة التخصيصات الافتراضية (Reset Customizations)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.RestartAlt,
                    title = stringResource(R.string.settings_reset),
                    subtitle = stringResource(R.string.reset_confirm_desc),
                    cornerRadius = cornerRadius
                ) {
                    Button(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_customizations_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_reset),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // 8. قسم حول التطبيق (About App)
            item {
                SettingsCardContainer(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about_app_title),
                    subtitle = "معلومات الإصدار والميزات",
                    cornerRadius = cornerRadius
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // شارة أيقونة التطبيق المتوهجة
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            primaryColor,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = stringResource(R.string.about_app_version_value),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.about_app_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // شبكة الميزات والقدرات (Feature Badges)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeatureBadge(icon = Icons.Default.CloudOff, text = stringResource(R.string.about_badge_offline))
                            FeatureBadge(icon = Icons.Default.Audiotrack, text = stringResource(R.string.about_badge_media3))
                            FeatureBadge(icon = Icons.Default.Security, text = stringResource(R.string.about_badge_privacy))
                            FeatureBadge(icon = Icons.Default.BatteryChargingFull, text = stringResource(R.string.about_badge_battery))
                            FeatureBadge(icon = Icons.Default.Lock, text = "Lock Screen")
                            FeatureBadge(icon = Icons.Default.Watch, text = "Wear OS / Watch")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.about_developer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // مساحة سفلية إضافية
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.reset_confirm_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(R.string.reset_confirm_desc))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetCustomizationsToDefault()
                            showResetDialog = false
                            Toast.makeText(context, context.getString(R.string.settings_reset), Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("confirm_reset_button")
                    ) {
                        Text(text = stringResource(R.string.reset))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetDialog = false },
                        modifier = Modifier.testTag("cancel_reset_button")
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

private data class LanguageOption(
    val code: String,
    val title: String,
    val flagEmoji: String,
    val subtitle: String
)

@Composable
private fun FeatureBadge(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsCardContainer(
    icon: ImageVector,
    title: String,
    subtitle: String,
    cornerRadius: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    primaryColor.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun getColorPresetLabel(preset: ColorPreset): String {
    return when (preset) {
        ColorPreset.SKY_BLUE -> stringResource(R.string.color_sky_blue)
        ColorPreset.CYAN -> stringResource(R.string.color_cyan)
        ColorPreset.INDIGO -> stringResource(R.string.color_indigo)
        ColorPreset.SUNSET -> stringResource(R.string.color_sunset)
        ColorPreset.EMERALD -> stringResource(R.string.color_emerald)
        ColorPreset.VIOLET -> stringResource(R.string.color_violet)
        ColorPreset.CRIMSON -> stringResource(R.string.color_crimson)
        ColorPreset.ROSE_GOLD -> stringResource(R.string.color_rose_gold)
        ColorPreset.DYNAMIC -> stringResource(R.string.color_dynamic)
        ColorPreset.CUSTOM -> stringResource(R.string.color_custom)
    }
}

@Composable
private fun getCornerPresetLabel(preset: CornerPreset): String {
    return when (preset) {
        CornerPreset.EXTRA_ROUNDED -> stringResource(R.string.corner_extra_rounded)
        CornerPreset.STANDARD -> stringResource(R.string.corner_standard)
        CornerPreset.SHARP -> stringResource(R.string.corner_sharp)
    }
}

@Composable
private fun getVisualizerStyleLabel(style: VisualizerStyle): String {
    return when (style) {
        VisualizerStyle.EQUALIZER_BARS -> stringResource(R.string.visualizer_bars)
        VisualizerStyle.SMOOTH_WAVE -> stringResource(R.string.visualizer_wave)
        VisualizerStyle.ENERGY_PULSE -> stringResource(R.string.visualizer_pulse)
        VisualizerStyle.NEON_GLOW -> stringResource(R.string.visualizer_glow)
    }
}
