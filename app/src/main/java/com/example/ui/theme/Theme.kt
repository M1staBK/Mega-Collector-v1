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

private val MegaPearlLightColorScheme = lightColorScheme(
    primary = PearlLightCobalt,
    onPrimary = Color.White,
    primaryContainer = PearlLightFrost,
    onPrimaryContainer = PearlLightCobalt,
    secondary = Color(0xFFFBBF24),
    onSecondary = PearlLightInk,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = PearlLightGoldText,
    tertiary = SkyAzure,
    onTertiary = PearlLightInk,
    background = PearlLightCanvas,
    onBackground = PearlLightInk,
    surface = PearlLightCard,
    onSurface = PearlLightInk,
    surfaceVariant = PearlLightFrost,
    onSurfaceVariant = PearlLightMuted,
    outline = PearlLightBorder,
    error = PearlLightCrimson,
    onError = Color.White
)

private val MegaMoonlightDarkColorScheme = darkColorScheme(
    primary = MoonlightSilverBlue,
    onPrimary = MoonlightNight,
    primaryContainer = MoonlightFrost,
    onPrimaryContainer = MoonlightSilverBlue,
    secondary = MoonlightGold,
    onSecondary = MoonlightNight,
    secondaryContainer = Color(0xFF2C2312),
    onSecondaryContainer = MoonlightGoldSoft,
    tertiary = MoonlightGloomHalo,
    onTertiary = Color.White,
    background = MoonlightNight,
    onBackground = MoonlightInk,
    surface = MoonlightCard,
    onSurface = MoonlightInk,
    surfaceVariant = MoonlightFrost,
    onSurfaceVariant = MoonlightMuted,
    outline = MoonlightBorder,
    error = MoonlightCrimson,
    onError = MoonlightNight
)

private val MegaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MegaCollectorTheme(
    darkTheme: Boolean = false,
    vibrationEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkCollectorPalette else LightCollectorPalette
    val colorScheme = if (darkTheme) MegaMoonlightDarkColorScheme else MegaPearlLightColorScheme

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
