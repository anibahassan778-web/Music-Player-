package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.CornerPreset

val LocalAppCornerRadius = compositionLocalOf { 16.dp }

fun createAppShapes(cornerPreset: CornerPreset): Shapes {
    val radius = cornerPreset.radiusDp.dp
    val smallRadius = (cornerPreset.radiusDp / 2).coerceAtLeast(4).dp
    val largeRadius = (cornerPreset.radiusDp * 1.5f).toInt().dp

    return Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(smallRadius),
        medium = RoundedCornerShape(radius),
        large = RoundedCornerShape(largeRadius),
        extraLarge = RoundedCornerShape((largeRadius.value + 6).dp)
    )
}
