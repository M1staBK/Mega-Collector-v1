package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.model.SkinRarity
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.PearlLightInk

@Composable
fun rememberMlbbImageRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return remember(url, context) {
        ImageRequest.Builder(context)
            .data(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) MegaCollector/3.0")
            .addHeader("Accept", "image/webp,image/png,image/jpeg,*/*")
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}

@Composable
fun rememberMlbbSquareImageRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return remember(url, context) {
        ImageRequest.Builder(context)
            .data(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) MegaCollector/3.0")
            .addHeader("Accept", "image/webp,image/png,image/jpeg,*/*")
            .transformations(CharacterFocusSquareTransformation(url))
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}

@Composable
fun RarityChip(
    rarity: SkinRarity,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier,
        color = if (palette.isDark) MoonlightNight.copy(alpha = 0.90f) else PearlLightInk.copy(alpha = 0.88f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, rarity.primaryColor)
    ) {
        Text(
            text = rarity.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 9.sp else 11.sp
            ),
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 2.dp else 3.dp
            )
        )
    }
}

@Composable
fun CoinsPill(
    coins: Int,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier,
        color = palette.cardSurface,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, palette.gold.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "Coins",
                tint = palette.gold,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%,d".format(coins),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = palette.goldText,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
fun ArenaGoldPill(
    gold: Int,
    modifier: Modifier = Modifier
) = CoinsPill(coins = gold, modifier = modifier)

@Composable
fun DiamondsPill(
    diamonds: Int,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier,
        color = palette.cardSurface,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, palette.skyAccent.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = "Diamonds",
                tint = palette.skyAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%,d".format(diamonds),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = palette.accentPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
fun SoundMuteButton(
    isMuted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier,
        color = palette.cardSurface,
        shape = CircleShape,
        shadowElevation = 3.dp,
        border = BorderStroke(
            1.dp,
            if (isMuted) palette.border else palette.accentPrimary.copy(alpha = 0.55f)
        )
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier
                .size(48.dp)
                .testTag("sound_mute_toggle_button")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isMuted) "Unmute sound effects" else "Mute sound effects",
                tint = if (isMuted) palette.muted else palette.accentPrimary,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
fun SettingsGearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier,
        color = palette.cardSurface,
        shape = CircleShape,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.45f))
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .testTag("settings_cog_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Open settings",
                tint = palette.accentPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Minimalist icon-only settings strip: no labels, no toggle switches — just tap each icon directly.
 */
@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    darkModeEnabled: Boolean,
    vibrationEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = CollectorTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(999.dp),
            shadowElevation = 10.dp,
            border = BorderStroke(1.5.dp, palette.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Speaker / Crossed Speaker Icon
                Surface(
                    color = if (soundEnabled) palette.frostSurface else palette.canvas,
                    shape = CircleShape,
                    border = BorderStroke(
                        1.5.dp,
                        if (soundEnabled) palette.accentPrimary else palette.border
                    )
                ) {
                    IconButton(
                        onClick = { onToggleSound(!soundEnabled) },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("settings_audio_switch")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                            contentDescription = if (soundEnabled) "Sound On" else "Sound Off",
                            tint = if (soundEnabled) palette.accentPrimary else palette.muted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // 2. Moon / Sun Icon
                Surface(
                    color = if (darkModeEnabled) palette.frostSurface else palette.goldSurfaceTint,
                    shape = CircleShape,
                    border = BorderStroke(
                        1.5.dp,
                        if (darkModeEnabled) palette.accentPrimary else palette.gold
                    )
                ) {
                    IconButton(
                        onClick = { onToggleDarkMode(!darkModeEnabled) },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("settings_dark_mode_switch")
                    ) {
                        Icon(
                            imageVector = if (darkModeEnabled) {
                                Icons.Default.DarkMode
                            } else {
                                Icons.Default.LightMode
                            },
                            contentDescription = if (darkModeEnabled) "Dark Mode" else "Light Mode",
                            tint = if (darkModeEnabled) palette.accentPrimary else palette.gold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // 3. Vibration / Crossed Vibration Icon
                Surface(
                    color = if (vibrationEnabled) palette.frostSurface else palette.canvas,
                    shape = CircleShape,
                    border = BorderStroke(
                        1.5.dp,
                        if (vibrationEnabled) palette.accentPrimary else palette.border
                    )
                ) {
                    IconButton(
                        onClick = { onToggleVibration(!vibrationEnabled) },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("settings_vibration_switch")
                    ) {
                        VibrationStateIcon(
                            enabled = vibrationEnabled,
                            tint = if (vibrationEnabled) palette.accentPrimary else palette.muted,
                            surfaceBg = if (vibrationEnabled) palette.frostSurface else palette.canvas
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VibrationStateIcon(
    enabled: Boolean,
    tint: Color,
    surfaceBg: Color
) {
    Box(
        modifier = Modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Vibration,
            contentDescription = if (enabled) "Vibration On" else "Vibration Off",
            tint = tint,
            modifier = Modifier.size(25.dp)
        )
        if (!enabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 3.dp.toPx()
                // Crisp cutout gap + diagonal slash matching Material VolumeOff style
                drawLine(
                    color = surfaceBg,
                    start = Offset(pad - 1.5.dp.toPx(), pad - 1.5.dp.toPx()),
                    end = Offset(size.width - pad + 1.5.dp.toPx(), size.height - pad + 1.5.dp.toPx()),
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(pad, pad),
                    end = Offset(size.width - pad, size.height - pad),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun MlbbDexCreditCard(
    skinCount: Int,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.cardSurface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(palette.frostSurface, palette.gloomHalo.copy(alpha = 0.45f))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "MLBBDex API Credit",
                    tint = palette.accentPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Data & Skin Catalog by MLBBDex.com",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Live API: mlbbdex.com/api/v1/skins/ • $skinCount skins indexed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted
                )
            }
        }
    }
}

@Composable
fun EmptyVaultState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = palette.frostSurface,
            shape = CircleShape,
            border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.35f)),
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = palette.accentPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.ink
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
    }
}
