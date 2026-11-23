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

val PearlLightCanvas = Color(0xFFEDF2FE)
val PearlLightTint = Color(0xFFE3EBFC)
val PearlLightCard = Color(0xFFF9FBFF)
val PearlLightFrost = Color(0xFFE6EEFF)
val PearlLightBorder = Color(0xFFD6E2FA)
val PearlLightInk = Color(0xFF17264D)
val PearlLightMuted = Color(0xFF5A6B8C)
val PearlLightCobalt = Color(0xFF2557EB)
val PearlLightSkyAccent = Color(0xFF38BDF8)
val PearlLightGoldText = Color(0xFF9A5B00)
val PearlLightCrimson = Color(0xFFD92652)

// Card Back & Rarity Accents (refined satin hues, less jarring neon)
val CardBackRoyal = Color(0xFF1A2440)
val CardBackDeep = Color(0xFF0E1424)
val CardBackEdge = Color(0xFF28375E)

val SkyAzure = Color(0xFF38BDF8)
val ArenaGold = Color(0xFFFFBE1A)
val ArenaGoldDeep = Color(0xFFF59E0B)
val PrismCyan = Color(0xFF00C6AE)
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
    val glassHighlight: Color,
    val orbWatermark: Color,
    val ink: Color,
    val muted: Color,
    val accentPrimary: Color,
    val buttonPrimary: Color,
    val skyAccent: Color,
    val mintAccent: Color,
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

enum class ThemeAccentOption(
    val id: String,
    val label: String,
    val swatchColor: Color,
    val lightPrimary: Color,
    val lightButton: Color,
    val lightSky: Color,
    val lightMint: Color,
    val lightFrost: Color,
    val lightHalo: Color,
    val darkPrimary: Color,
    val darkButton: Color,
    val darkSky: Color,
    val darkMint: Color,
    val darkFrost: Color,
    val darkHalo: Color
) {
    SKY_COBALT(
        id = "SKY_COBALT",
        label = "Sky Cobalt",
        swatchColor = Color(0xFF2557EB),
        lightPrimary = Color(0xFF1F4CE0),
        lightButton = Color(0xFF2557EB),
        lightSky = Color(0xFF38BDF8),
        lightMint = Color(0xFF10B981),
        lightFrost = Color(0xFFE6EEFF),
        lightHalo = Color(0xFFC7D9FF),
        darkPrimary = Color(0xFF8AB4F8),
        darkButton = Color(0xFF4B6ED6),
        darkSky = Color(0xFF8AB4F8),
        darkMint = Color(0xFF3CDDC4),
        darkFrost = Color(0xFF253047),
        darkHalo = Color(0xFF3A4D7A)
    ),
    ARENA_GOLD(
        id = "ARENA_GOLD",
        label = "Royal Gold",
        swatchColor = Color(0xFFF59E0B),
        lightPrimary = Color(0xFF9A5B00),
        lightButton = Color(0xFFD97706),
        lightSky = Color(0xFFF59E0B),
        lightMint = Color(0xFFEAB308),
        lightFrost = Color(0xFFFEF3C7),
        lightHalo = Color(0xFFFDE68A),
        darkPrimary = Color(0xFFFCD34D),
        darkButton = Color(0xFFD97706),
        darkSky = Color(0xFFFBBF24),
        darkMint = Color(0xFFF59E0B),
        darkFrost = Color(0xFF2D2518),
        darkHalo = Color(0xFF5A451C)
    ),
    EMERALD_MINT(
        id = "EMERALD_MINT",
        label = "Emerald Jade",
        swatchColor = Color(0xFF10B981),
        lightPrimary = Color(0xFF047857),
        lightButton = Color(0xFF059669),
        lightSky = Color(0xFF10B981),
        lightMint = Color(0xFF14B8A6),
        lightFrost = Color(0xFFD1FAE5),
        lightHalo = Color(0xFFA7F3D0),
        darkPrimary = Color(0xFF6EE7B7),
        darkButton = Color(0xFF059669),
        darkSky = Color(0xFF34D399),
        darkMint = Color(0xFF2DD4BF),
        darkFrost = Color(0xFF172D29),
        darkHalo = Color(0xFF1F4C42)
    ),
    ROYAL_AMETHYST(
        id = "ROYAL_AMETHYST",
        label = "Mystic Violet",
        swatchColor = Color(0xFF7C3AED),
        lightPrimary = Color(0xFF6D28D9),
        lightButton = Color(0xFF7C3AED),
        lightSky = Color(0xFF8B5CF6),
        lightMint = Color(0xFFA855F7),
        lightFrost = Color(0xFFEDE9FE),
        lightHalo = Color(0xFFDDD6FE),
        darkPrimary = Color(0xFFC4B5FD),
        darkButton = Color(0xFF7C3AED),
        darkSky = Color(0xFFA78BFA),
        darkMint = Color(0xFFC084FC),
        darkFrost = Color(0xFF261F3D),
        darkHalo = Color(0xFF453474)
    ),
    CRIMSON_FLAME(
        id = "CRIMSON_FLAME",
        label = "Crimson Rose",
        swatchColor = Color(0xFFE11D48),
        lightPrimary = Color(0xFFBE123C),
        lightButton = Color(0xFFE11D48),
        lightSky = Color(0xFFF43F5E),
        lightMint = Color(0xFFFB7185),
        lightFrost = Color(0xFFFFE4E6),
        lightHalo = Color(0xFFFECDD3),
        darkPrimary = Color(0xFFFDA4AF),
        darkButton = Color(0xFFE11D48),
        darkSky = Color(0xFFFB7185),
        darkMint = Color(0xFFF472B6),
        darkFrost = Color(0xFF331B26),
        darkHalo = Color(0xFF5C263B)
    );

    companion object {
        fun fromId(id: String?): ThemeAccentOption =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SKY_COBALT
    }
}

fun buildCollectorPalette(
    isDark: Boolean,
    accent: ThemeAccentOption = ThemeAccentOption.SKY_COBALT
): CollectorPalette {
    return if (isDark) {
        CollectorPalette(
            isDark = true,
            canvas = MoonlightNight,
            abyss = MoonlightAbyss,
            cardSurface = MoonlightCard,
            frostSurface = accent.darkFrost,
            border = MoonlightBorder,
            glassHighlight = Color(0xFF3E4F73),
            orbWatermark = Color.White.copy(alpha = 0.04f),
            ink = MoonlightInk,
            muted = MoonlightMuted,
            accentPrimary = accent.darkPrimary,
            buttonPrimary = accent.darkButton,
            skyAccent = accent.darkSky,
            mintAccent = accent.darkMint,
            gloomHalo = accent.darkHalo,
            gold = MoonlightGold,
            goldBright = MoonlightGoldSoft,
            goldText = MoonlightGoldSoft,
            goldSurfaceTint = Color(0xFF282218),
            crimson = MoonlightCrimson,
            navBar = MoonlightAbyss,
            goldButtonContent = MoonlightNight,
            heroTopScrim = accent.darkFrost.copy(alpha = 0.86f),
            heroMidScrim = MoonlightNight.copy(alpha = 0.92f),
            misprintLabel = accent.darkPrimary
        )
    } else {
        CollectorPalette(
            isDark = false,
            canvas = PearlLightCanvas,
            abyss = PearlLightTint,
            cardSurface = PearlLightCard,
            frostSurface = accent.lightFrost,
            border = PearlLightBorder,
            glassHighlight = Color.White.copy(alpha = 0.94f),
            orbWatermark = accent.lightPrimary.copy(alpha = 0.06f),
            ink = PearlLightInk,
            muted = PearlLightMuted,
            accentPrimary = accent.lightPrimary,
            buttonPrimary = accent.lightButton,
            skyAccent = accent.lightSky,
            mintAccent = accent.lightMint,
            gloomHalo = accent.lightHalo,
            gold = Color(0xFFFFBE1A),
            goldBright = Color(0xFFFFDE59),
            goldText = PearlLightGoldText,
            goldSurfaceTint = Color(0xFFFFF6DC),
            crimson = PearlLightCrimson,
            navBar = Color(0xFFF8FAFF),
            goldButtonContent = Color(0xFF3D2700),
            heroTopScrim = accent.lightFrost.copy(alpha = 0.72f),
            heroMidScrim = PearlLightCanvas.copy(alpha = 0.90f),
            misprintLabel = accent.lightPrimary
        )
    }
}

private object DefaultCollectorPalettes {
    val light: CollectorPalette by lazy {
        buildCollectorPalette(isDark = false, accent = ThemeAccentOption.SKY_COBALT)
    }
    val dark: CollectorPalette by lazy {
        buildCollectorPalette(isDark = true, accent = ThemeAccentOption.SKY_COBALT)
    }
}

val LightCollectorPalette: CollectorPalette
    get() = DefaultCollectorPalettes.light

val DarkCollectorPalette: CollectorPalette
    get() = DefaultCollectorPalettes.dark

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
