package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MegaDarkColorScheme = darkColorScheme(
    primary = ArenaGold,
    onPrimary = VoidObsidian,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = ArenaGold,
    secondary = PrismCyan,
    onSecondary = VoidObsidian,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = PrismCyan,
    tertiary = MythicCrimson,
    onTertiary = TextPrimary,
    background = VoidObsidian,
    onBackground = TextPrimary,
    surface = SurfaceAbyss,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle,
    error = MythicCrimson,
    onError = TextPrimary
)

private val MegaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MegaCollectorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MegaDarkColorScheme,
        typography = MegaCollectorTypography,
        shapes = MegaShapes,
        content = content
    )
}
