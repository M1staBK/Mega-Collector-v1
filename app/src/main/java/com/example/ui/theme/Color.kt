package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Static Brand / Card / Rarity Constants (safe for non-Composable model files)
val MoonlightNight = Color(0xFF080C18)
val MoonlightAbyss = Color(0xFF0F162A)
val MoonlightCard = Color(0xFF151E36)
val MoonlightFrost = Color(0xFF1E2B4A)
val MoonlightBorder = Color(0xFF2B3D6B)

val MoonlightInk = Color(0xFFEAF0FA)
val MoonlightMuted = Color(0xFF94A3B8)
val MoonlightSilverBlue = Color(0xFF7DD3FC)
val MoonlightIndigoBtn = Color(0xFF3B6EF6)
val MoonlightGloomHalo = Color(0xFF6366F1)
val MoonlightGold = Color(0xFFFBBF24)
val MoonlightGoldSoft = Color(0xFFFDE68A)
val MoonlightCrimson = Color(0xFFFB7185)

val PearlLightCanvas = Color(0xFFF4F7FB)
val PearlLightTint = Color(0xFFEAF2FC)
val PearlLightCard = Color(0xFFFFFFFF)
val PearlLightFrost = Color(0xFFE0F2FE)
val PearlLightBorder = Color(0xFFD6E4F5)
val PearlLightInk = Color(0xFF0F172A)
val PearlLightMuted = Color(0xFF475569)
val PearlLightCobalt = Color(0xFF1D4ED8)
val PearlLightSkyAccent = Color(0xFF0284C7)
val PearlLightGoldText = Color(0xFFB45309)
val PearlLightCrimson = Color(0xFFDC2626)

// Card Back & Rarity Accents (high contrast on both dark card footers & light UI)
val CardBackRoyal = Color(0xFF16203E)
val CardBackDeep = Color(0xFF090D1A)
val CardBackEdge = Color(0xFF23325C)

val SkyAzure = Color(0xFF38BDF8)
val ArenaGold = Color(0xFFFBBF24)
val ArenaGoldDeep = Color(0xFFD97706)
val PrismCyan = Color(0xFF06B6D4)
val MythicCrimson = Color(0xFFF43F5E)

val RarityCommon = Color(0xFF64748B)
val RarityUncommon = Color(0xFF10B981)
val RaritySpecial = Color(0xFF0EA5E9)
val RarityEpic = Color(0xFF8B5CF6)
val RarityLuckybox = Color(0xFFEC4899)
val RarityCollector = Color(0xFFF59E0B)
val RarityLegend = Color(0xFFF43F5E)

val MisprintNeon = Color(0xFF22D3EE)
val MisprintMagenta = Color(0xFFE879F9)

@Immutable
data class CollectorPalette(
    val isDark: Boolean,
    val canvas: Color,
    val abyss: Color,
    val cardSurface: Color,
    val frostSurface: Color,
    val border: Color,
    val ink: Color,
    val muted: Color,
    val accentPrimary: Color,
    val buttonPrimary: Color,
    val skyAccent: Color,
    val gloomHalo: Color,
    val gold: Color,
    val goldBright: Color,
    val goldText: Color,
    val goldSurfaceTint: Color,
    val crimson: Color,
    val navBar: Color,
    val goldButtonContent: Color,
    val heroTopScrim: Color,
    val heroMidScrim: Color,
    val misprintLabel: Color
)

val LightCollectorPalette = CollectorPalette(
    isDark = false,
    canvas = PearlLightCanvas,
    abyss = PearlLightTint,
    cardSurface = PearlLightCard,
    frostSurface = PearlLightFrost,
    border = PearlLightBorder,
    ink = PearlLightInk,
    muted = PearlLightMuted,
    accentPrimary = PearlLightCobalt,
    buttonPrimary = PearlLightCobalt,
    skyAccent = PearlLightSkyAccent,
    gloomHalo = Color(0xFFBAE6FD),
    gold = Color(0xFFFBBF24),
    goldBright = Color(0xFFFDE047),
    goldText = PearlLightGoldText,
    goldSurfaceTint = Color(0xFFFFFBEB),
    crimson = PearlLightCrimson,
    navBar = PearlLightCard,
    goldButtonContent = PearlLightInk,
    heroTopScrim = Color(0xFFE0F2FE).copy(alpha = 0.86f),
    heroMidScrim = Color(0xFFF4F7FB).copy(alpha = 0.90f),
    misprintLabel = PearlLightCobalt
)

val DarkCollectorPalette = CollectorPalette(
    isDark = true,
    canvas = MoonlightNight,
    abyss = MoonlightAbyss,
    cardSurface = MoonlightCard,
    frostSurface = MoonlightFrost,
    border = MoonlightBorder,
    ink = MoonlightInk,
    muted = MoonlightMuted,
    accentPrimary = MoonlightSilverBlue,
    buttonPrimary = MoonlightIndigoBtn,
    skyAccent = MoonlightSilverBlue,
    gloomHalo = MoonlightGloomHalo,
    gold = MoonlightGold,
    goldBright = MoonlightGoldSoft,
    goldText = MoonlightGoldSoft,
    goldSurfaceTint = Color(0xFF28200D),
    crimson = MoonlightCrimson,
    navBar = MoonlightAbyss,
    goldButtonContent = MoonlightNight,
    heroTopScrim = MoonlightAbyss.copy(alpha = 0.78f),
    heroMidScrim = MoonlightNight.copy(alpha = 0.84f),
    misprintLabel = MisprintNeon
)

val LocalCollectorPalette = staticCompositionLocalOf { LightCollectorPalette }
val LocalVibrationEnabled = staticCompositionLocalOf { true }

object CollectorTheme {
    val colors: CollectorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalCollectorPalette.current
}

// Reactive Composable Theme Aliases (automatically switch between Pearl Light and Moonlight Dark)
val PearlCanvas: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.canvas

val SkyFrost: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.frostSurface

val PureWhiteCard: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.cardSurface

val StudioTint: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.abyss

val IceBorder: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.border

val InkSlate: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.ink

val SlateMuted: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.muted

val DeepCobalt: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.buttonPrimary

val StudioGold: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.gold

val StudioGoldBright: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.goldBright

val StudioGoldText: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.goldText

val StudioCrimson: Color
    @Composable @ReadOnlyComposable get() = LocalCollectorPalette.current.crimson
