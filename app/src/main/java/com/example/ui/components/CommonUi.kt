package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import com.example.model.BadgeIconStyle
import com.example.model.SkinRarity
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.PearlLightInk
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.ThemeAccentOption
import com.example.ui.viewmodel.CatalogSyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CollectorSettingsController(
    val soundEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val themeAccent: ThemeAccentOption = ThemeAccentOption.SKY_COBALT,
    val onToggleSound: (Boolean) -> Unit = {},
    val onToggleDarkMode: (Boolean) -> Unit = {},
    val onToggleVibration: (Boolean) -> Unit = {},
    val onSelectThemeAccent: (ThemeAccentOption) -> Unit = {},
    val onCogOpened: () -> Unit = {}
)

val LocalCollectorSettings = compositionLocalOf { CollectorSettingsController() }

@Composable
fun ProvideCollectorSettings(
    controller: CollectorSettingsController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalCollectorSettings provides controller, content = content)
}

@Composable
fun rememberMlbbImageRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return remember(url, context) {
        ImageRequest.Builder(context)
            .data(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) MegaCollector/3.0")
            .addHeader("Accept", "image/webp,image/png,image/jpeg,*/*")
            .size(920, 1320)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .transformations(CardDetailUpscaleTransformation(url, targetMinPx = 920))
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(120)
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
            .size(920, 920)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .transformations(CharacterFocusSquareTransformation(url))
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(120)
            .build()
    }
}

@Composable
fun rememberUserAvatarRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return remember(url, context) {
        ImageRequest.Builder(context)
            .data(url)
            .size(256, 256)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(120)
            .build()
    }
}

@Composable
fun SkyStudioBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val palette = CollectorTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (palette.isDark) {
                        listOf(palette.abyss, palette.canvas, palette.abyss)
                    } else {
                        listOf(
                            Color(0xFFE7EEFF),
                            Color(0xFFF3F7FF),
                            Color(0xFFE9F0FE)
                        )
                    }
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft upper-right luminous sky bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.24f else 0.42f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.14f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.88f, h * 0.14f)
            )

            // Soft lower-left radial bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.skyAccent.copy(alpha = if (palette.isDark) 0.10f else 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.14f, h * 0.68f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.14f, h * 0.68f)
            )

            // Subtle geometric orb watermark (upper-left & lower-right) inspired by TCG studio cards
            val orbCenterTop = Offset(w * 0.12f, h * 0.12f)
            drawCircle(
                color = palette.orbWatermark,
                radius = w * 0.20f,
                center = orbCenterTop,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = palette.orbWatermark,
                radius = w * 0.06f,
                center = orbCenterTop,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )

            val orbCenterBottom = Offset(w * 0.88f, h * 0.86f)
            drawCircle(
                color = palette.orbWatermark,
                radius = w * 0.24f,
                center = orbCenterBottom,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
        content()
    }
}

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    borderColor: Color? = null,
    containerAlpha: Float? = null,
    content: @Composable () -> Unit
) {
    val palette = CollectorTheme.colors
    val alpha = containerAlpha ?: if (palette.isDark) 0.94f else 0.88f
    val stroke = borderColor ?: if (palette.isDark) palette.border else palette.glassHighlight
    Surface(
        color = palette.cardSurface.copy(alpha = alpha),
        shape = RoundedCornerShape(cornerRadius),
        shadowElevation = if (palette.isDark) 4.dp else 5.dp,
        border = BorderStroke(1.3.dp, stroke),
        modifier = modifier,
        content = content
    )
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
        color = if (palette.isDark) MoonlightNight.copy(alpha = 0.92f) else PearlLightInk.copy(alpha = 0.90f),
        shape = RoundedCornerShape(999.dp),
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
                horizontal = if (compact) 7.dp else 9.dp,
                vertical = if (compact) 2.5.dp else 3.5.dp
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
        color = palette.cardSurface.copy(alpha = if (palette.isDark) 0.96f else 0.92f),
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, if (palette.isDark) palette.gold.copy(alpha = 0.55f) else palette.glassHighlight)
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = palette.gold,
                shape = CircleShape,
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = palette.goldButtonContent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "%,d".format(coins),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = palette.ink,
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
        color = palette.cardSurface.copy(alpha = if (palette.isDark) 0.96f else 0.92f),
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, if (palette.isDark) palette.skyAccent.copy(alpha = 0.55f) else palette.glassHighlight)
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = palette.skyAccent.copy(alpha = 0.16f),
                shape = CircleShape,
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Diamonds",
                        tint = palette.skyAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "%,d".format(diamonds),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = palette.ink,
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
        color = palette.cardSurface.copy(alpha = if (palette.isDark) 0.94f else 0.90f),
        shape = CircleShape,
        shadowElevation = 3.dp,
        border = BorderStroke(
            1.2.dp,
            if (isMuted) palette.border else palette.glassHighlight
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

/**
 * Gear button that opens a vertical icon popup anchored right at the cog's location.
 * Tapping any icon directly toggles Speaker (Sound), Moon/Sun (Dark/Light Mode), Vibration, or Theme Accent.
 */
@Composable
fun SettingsGearButton(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val settings = LocalCollectorSettings.current
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val yOffsetPx = remember(density) { with(density) { 54.dp.roundToPx() } }

    Box(modifier = modifier) {
        Surface(
            color = palette.cardSurface.copy(alpha = if (palette.isDark) 0.95f else 0.92f),
            shape = CircleShape,
            shadowElevation = 4.dp,
            border = BorderStroke(
                1.3.dp,
                if (palette.isDark) palette.border else palette.glassHighlight
            )
        ) {
            IconButton(
                onClick = {
                    settings.onCogOpened()
                    onClick()
                    expanded = !expanded
                },
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

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = yOffsetPx),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.85f, 0f)),
                    exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(0.85f, 0f))
                ) {
                    Surface(
                        color = palette.cardSurface,
                        shape = RoundedCornerShape(999.dp),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.2.dp, palette.border),
                        modifier = Modifier.testTag("settings_vertical_popup")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Speaker / Crossed Speaker
                            Surface(
                                color = if (settings.soundEnabled) palette.frostSurface else palette.canvas,
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (settings.soundEnabled) palette.accentPrimary else palette.border
                                )
                            ) {
                                IconButton(
                                    onClick = { settings.onToggleSound(!settings.soundEnabled) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("settings_audio_switch")
                                ) {
                                    Icon(
                                        imageVector = if (settings.soundEnabled) {
                                            Icons.AutoMirrored.Filled.VolumeUp
                                        } else {
                                            Icons.AutoMirrored.Filled.VolumeOff
                                        },
                                        contentDescription = if (settings.soundEnabled) "Sound On" else "Sound Off",
                                        tint = if (settings.soundEnabled) palette.accentPrimary else palette.muted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // 2. Moon / Sun
                            Surface(
                                color = if (settings.darkModeEnabled) palette.frostSurface else palette.goldSurfaceTint,
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (settings.darkModeEnabled) palette.accentPrimary else palette.gold
                                )
                            ) {
                                IconButton(
                                    onClick = { settings.onToggleDarkMode(!settings.darkModeEnabled) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("settings_dark_mode_switch")
                                ) {
                                    Icon(
                                        imageVector = if (settings.darkModeEnabled) {
                                            Icons.Default.DarkMode
                                        } else {
                                            Icons.Default.LightMode
                                        },
                                        contentDescription = if (settings.darkModeEnabled) "Dark Mode" else "Light Mode",
                                        tint = if (settings.darkModeEnabled) palette.accentPrimary else palette.gold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // 3. Vibration / Crossed Vibration
                            Surface(
                                color = if (settings.vibrationEnabled) palette.frostSurface else palette.canvas,
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (settings.vibrationEnabled) palette.accentPrimary else palette.border
                                )
                            ) {
                                IconButton(
                                    onClick = { settings.onToggleVibration(!settings.vibrationEnabled) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("settings_vibration_switch")
                                ) {
                                    VibrationStateIcon(
                                        enabled = settings.vibrationEnabled,
                                        tint = if (settings.vibrationEnabled) palette.accentPrimary else palette.muted,
                                        surfaceBg = if (settings.vibrationEnabled) palette.frostSurface else palette.canvas
                                    )
                                }
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = palette.border,
                                modifier = Modifier.width(34.dp)
                            )

                            // 4. Theme Accent Swatch Pills
                            ThemeAccentOption.entries.forEach { accent ->
                                val isSelected = settings.themeAccent == accent
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            settings.onSelectThemeAccent(accent)
                                        }
                                        .testTag("settings_accent_${accent.name.lowercase()}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(accent.swatchColor)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.2.dp,
                                                color = if (isSelected) palette.ink else Color.White.copy(alpha = 0.7f),
                                                shape = CircleShape
                                            )
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Theme accent: ${accent.label} selected",
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
    }
}

@Composable
private fun VibrationStateIcon(
    enabled: Boolean,
    tint: Color,
    surfaceBg: Color
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Vibration,
            contentDescription = if (enabled) "Vibration On" else "Vibration Off",
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        if (!enabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 2.5.dp.toPx()
                drawLine(
                    color = surfaceBg,
                    start = Offset(pad - 1.5.dp.toPx(), pad - 1.5.dp.toPx()),
                    end = Offset(size.width - pad + 1.5.dp.toPx(), size.height - pad + 1.5.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(pad, pad),
                    end = Offset(size.width - pad, size.height - pad),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun CatalogSyncStatusCard(
    syncStatus: CatalogSyncStatus,
    lastSyncedMs: Long,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val syncedDateLabel = remember(lastSyncedMs) {
        if (lastSyncedMs <= 0L) "Not synced yet"
        else "Cached: " + SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(lastSyncedMs))
    }

    val accentTint = when (syncStatus) {
        is CatalogSyncStatus.Error -> palette.crimson
        is CatalogSyncStatus.Synced -> RarityUncommon
        else -> palette.accentPrimary
    }

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = accentTint.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (syncStatus) {
                is CatalogSyncStatus.Syncing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.4.dp,
                        color = palette.accentPrimary
                    )
                }
                is CatalogSyncStatus.Error -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = palette.crimson,
                        modifier = Modifier.size(22.dp)
                    )
                }
                is CatalogSyncStatus.Synced -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RarityUncommon,
                        modifier = Modifier.size(22.dp)
                    )
                }
                CatalogSyncStatus.Idle -> {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (syncStatus) {
                        is CatalogSyncStatus.Syncing -> syncStatus.stage
                        is CatalogSyncStatus.Synced -> "${syncStatus.count} Skins Active in Pool"
                        is CatalogSyncStatus.Error -> "API Sync Interrupted"
                        CatalogSyncStatus.Idle -> "MLBBDex Daily Skin Sync"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink
                )
                Text(
                    text = when (syncStatus) {
                        is CatalogSyncStatus.Error -> syncStatus.message
                        else -> "$syncedDateLabel • Auto-syncs daily"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = palette.muted
                )
            }

            Button(
                onClick = onRefresh,
                enabled = syncStatus !is CatalogSyncStatus.Syncing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.frostSurface,
                    contentColor = palette.accentPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("profile_sync_catalog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh MLBBDex API",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (syncStatus is CatalogSyncStatus.Syncing) "Syncing" else "Sync",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun BadgeIconVector(style: BadgeIconStyle): ImageVector = when (style) {
    BadgeIconStyle.STAR_PACK -> Icons.Default.Star
    BadgeIconStyle.DEX_CROWN -> Icons.Default.WorkspacePremium
    BadgeIconStyle.EPIC_GEM -> Icons.Default.Diamond
    BadgeIconStyle.MYTHIC_WINGS -> Icons.Default.AutoAwesome
    BadgeIconStyle.MISPRINT_SPARK -> Icons.Default.Bolt
    BadgeIconStyle.FORGE_ANVIL -> Icons.Default.Build
    BadgeIconStyle.TRADE_HANDS -> Icons.Default.SwapHoriz
    BadgeIconStyle.QUIZ_SCROLL -> Icons.Default.MenuBook
    BadgeIconStyle.WHEEL_FORTUNE -> Icons.Default.Casino
    BadgeIconStyle.STREAK_FLAME -> Icons.Default.LocalFireDepartment
    BadgeIconStyle.VAULT_CHEST -> Icons.Default.Inventory2
    BadgeIconStyle.COLLECTOR_TROPHY -> Icons.Default.EmojiEvents
}

@Composable
fun MlbbDexCreditCard(
    skinCount: Int,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp
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
                            listOf(palette.frostSurface, palette.gloomHalo.copy(alpha = 0.35f))
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
