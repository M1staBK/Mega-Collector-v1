package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Static Brand / Card / Rarity Constants (safe for non-Composable model files)
val MoonlightNight = Color(0xFF111622)
val MoonlightAbyss = Color(0xFF171E2E)
val MoonlightCard = Color(0xFF1D2538)
val MoonlightFrost = Color(0xFF253047)
val MoonlightBorder = Color(0xFF32405E)

val MoonlightInk = Color(0xFFE4E9F2)
val MoonlightMuted = Color(0xFF9AA7BD)
val MoonlightSilverBlue = Color(0xFF8AB4F8)
val MoonlightIndigoBtn = Color(0xFF4B6ED6)
val MoonlightGloomHalo = Color(0xFF3A4D7A)
val MoonlightGold = Color(0xFFE5B84B)
val MoonlightGoldSoft = Color(0xFFF3D58D)
val MoonlightCrimson = Color(0xFFE57388)

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

// Card Back & Rarity Accents (refined satin hues, less jarring neon)
val CardBackRoyal = Color(0xFF1A2440)
val CardBackDeep = Color(0xFF0E1424)
val CardBackEdge = Color(0xFF28375E)

val SkyAzure = Color(0xFF5FA8E8)
val ArenaGold = Color(0xFFEAB308)
val ArenaGoldDeep = Color(0xFFCA8A04)
val PrismCyan = Color(0xFF3898B8)
val MythicCrimson = Color(0xFFE15B74)

val RarityCommon = Color(0xFF6E7F96)
val RarityUncommon = Color(0xFF1FB57E)
val RaritySpecial = Color(0xFF2A9DF4)
val RarityEpic = Color(0xFF9368E8)
val RarityLuckybox = Color(0xFFD95B96)
val RarityCollector = Color(0xFFE5A024)
val RarityLegend = Color(0xFFE54D67)

val MisprintNeon = Color(0xFF68BBE3)
val MisprintMagenta = Color(0xFFC084FC)

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
    goldSurfaceTint = Color(0xFF282218),
    crimson = MoonlightCrimson,
    navBar = MoonlightAbyss,
    goldButtonContent = MoonlightNight,
    heroTopScrim = MoonlightAbyss.copy(alpha = 0.84f),
    heroMidScrim = MoonlightNight.copy(alpha = 0.92f),
    misprintLabel = Color(0xFFA5B4FC)
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
