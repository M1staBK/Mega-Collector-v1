package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MegaStudioLightColorScheme = lightColorScheme(
    primary = DeepCobalt,
    onPrimary = Color.White,
    primaryContainer = SkyFrost,
    onPrimaryContainer = DeepCobalt,
    secondary = StudioGold,
    onSecondary = InkSlate,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = StudioGoldText,
    tertiary = StudioCrimson,
    onTertiary = Color.White,
    background = PearlCanvas,
    onBackground = InkSlate,
    surface = PureWhiteCard,
    onSurface = InkSlate,
    surfaceVariant = SkyFrost,
    onSurfaceVariant = SlateMuted,
    outline = IceBorder,
    error = StudioCrimson,
    onError = Color.White
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
        colorScheme = MegaStudioLightColorScheme,
        typography = MegaCollectorTypography,
        shapes = MegaShapes,
        content = content
    )
}
