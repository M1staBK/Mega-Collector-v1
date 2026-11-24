package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

private val MegaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MegaCollectorTheme(
    darkTheme: Boolean = false,
    accentOption: ThemeAccentOption = ThemeAccentOption.SKY_COBALT,
    vibrationEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = buildCollectorPalette(isDark = darkTheme, accent = accentOption)
    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accentPrimary,
            onPrimary = palette.canvas,
            primaryContainer = palette.frostSurface,
            onPrimaryContainer = palette.accentPrimary,
            secondary = palette.gold,
            onSecondary = palette.canvas,
            secondaryContainer = Color(0xFF2C2312),
            onSecondaryContainer = palette.goldText,
            tertiary = palette.gloomHalo,
            onTertiary = Color.White,
            background = palette.canvas,
            onBackground = palette.ink,
            surface = palette.cardSurface,
            onSurface = palette.ink,
            surfaceVariant = palette.frostSurface,
            onSurfaceVariant = palette.muted,
            outline = palette.border,
            error = palette.crimson,
            onError = MoonlightNight
        )
    } else {
        lightColorScheme(
            primary = palette.accentPrimary,
            onPrimary = Color.White,
            primaryContainer = palette.frostSurface,
            onPrimaryContainer = palette.accentPrimary,
            secondary = Color(0xFFFBBF24),
            onSecondary = palette.ink,
            secondaryContainer = Color(0xFFFEF3C7),
            onSecondaryContainer = palette.goldText,
            tertiary = palette.skyAccent,
            onTertiary = palette.ink,
            background = palette.canvas,
            onBackground = palette.ink,
            surface = palette.cardSurface,
            onSurface = palette.ink,
            surfaceVariant = palette.frostSurface,
            onSurfaceVariant = palette.muted,
            outline = palette.border,
            error = palette.crimson,
            onError = Color.White
        )
    }

    CompositionLocalProvider(
        LocalCollectorPalette provides palette,
        LocalVibrationEnabled provides vibrationEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MegaCollectorTypography,
            shapes = MegaShapes,
            content = content
        )
    }
}

@Composable
fun rememberSafeHapticPerformer(): (HapticFeedbackType) -> Unit {
    val haptic: HapticFeedback = LocalHapticFeedback.current
    val enabled = LocalVibrationEnabled.current
    return { type ->
        if (enabled) {
            haptic.performHapticFeedback(type)
        }
    }
}
