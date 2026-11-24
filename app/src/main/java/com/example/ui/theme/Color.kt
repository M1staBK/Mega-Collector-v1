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

// Obsidian Pack Noir Surface Tokens (black + purple + gold shine)
val ObsidianPackCanvas = Color(0xFF07050C)
val ObsidianPackAbyss = Color(0xFF0E0918)
val ObsidianPackCard = Color(0xFF140F22)
val ObsidianPackFrost = Color(0xFF221638)
val ObsidianPackBorder = Color(0xFF5B3496)
val ObsidianPackInk = Color(0xFFF5F0FF)
val ObsidianPackMuted = Color(0xFFBBA9D9)
val ObsidianPackGold = Color(0xFFFACC15)
val ObsidianPackGoldBright = Color(0xFFFEF08A)

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
    val hasPackNoirShine: Boolean = false,
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
    val secondarySwatchColor: Color? = null,
    val forcesDarkCanvas: Boolean = false,
    val hasPackNoirShine: Boolean = false,
    val isUnlockedByDefault: Boolean = true,
    val unlockHint: String? = null,
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
    OBSIDIAN_PACK(
        id = "OBSIDIAN_PACK",
        label = "Pack Noir",
        swatchColor = Color(0xFF120B1F),
        secondarySwatchColor = Color(0xFFFACC15),
        forcesDarkCanvas = false,
        hasPackNoirShine = true,
        isUnlockedByDefault = true,
        lightPrimary = Color(0xFF6B21A8),
        lightButton = Color(0xFF6B21A8),
        lightSky = Color(0xFF9333EA),
        lightMint = Color(0xFFD97706),
        lightFrost = Color(0xFFF1E8FF),
        lightHalo = Color(0xFFE9D5FF),
        darkPrimary = Color(0xFFFACC15),
        darkButton = Color(0xFF7E22CE),
        darkSky = Color(0xFFC084FC),
        darkMint = Color(0xFFFDE047),
        darkFrost = ObsidianPackFrost,
        darkHalo = Color(0xFF6B21A8)
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
    ),
    CELESTIAL_GOLD(
        id = "CELESTIAL_GOLD",
        label = "Celestial Aurum",
        swatchColor = Color(0xFFFACC15),
        secondarySwatchColor = Color(0xFF92400E),
        forcesDarkCanvas = true,
        hasPackNoirShine = true,
        isUnlockedByDefault = false,
        unlockHint = "Unlock via Vault Architect (25 Unique Skins) achievement",
        lightPrimary = Color(0xFFD97706),
        lightButton = Color(0xFFB45309),
        lightSky = Color(0xFFFACC15),
        lightMint = Color(0xFFFDE047),
        lightFrost = Color(0xFF2D2209),
        lightHalo = Color(0xFF78350F),
        darkPrimary = Color(0xFFFDE047),
        darkButton = Color(0xFFB45309),
        darkSky = Color(0xFFFACC15),
        darkMint = Color(0xFFFEF08A),
        darkFrost = Color(0xFF281F0B),
        darkHalo = Color(0xFF713F12)
    ),
    MYTHIC_PRISM(
        id = "MYTHIC_PRISM",
        label = "Mythic Prism",
        swatchColor = Color(0xFF06B6D4),
        secondarySwatchColor = Color(0xFFEC4899),
        forcesDarkCanvas = true,
        hasPackNoirShine = false,
        isUnlockedByDefault = false,
        unlockHint = "Unlock via Mythic Royalty (Collector/Legend) achievement",
        lightPrimary = Color(0xFF06B6D4),
        lightButton = Color(0xFF7C3AED),
        lightSky = Color(0xFFEC4899),
        lightMint = Color(0xFF38BDF8),
        lightFrost = Color(0xFF1A223B),
        lightHalo = Color(0xFF5B21B6),
        darkPrimary = Color(0xFF67E8F9),
        darkButton = Color(0xFF0891B2),
        darkSky = Color(0xFFF472B6),
        darkMint = Color(0xFF38BDF8),
        darkFrost = Color(0xFF1B233D),
        darkHalo = Color(0xFF581C87)
    );

    fun isUnlocked(unlockedThemeIds: Set<String>): Boolean =
        isUnlockedByDefault || id in unlockedThemeIds

    companion object {
        val DEFAULT_UNLOCKED_IDS: Set<String> =
            entries.filter { it.isUnlockedByDefault }.map { it.id }.toSet()

        fun fromId(id: String?): ThemeAccentOption =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SKY_COBALT
    }
}

fun buildCollectorPalette(
    isDark: Boolean,
    accent: ThemeAccentOption = ThemeAccentOption.SKY_COBALT
): CollectorPalette {
    val effectiveDark = isDark || accent.forcesDarkCanvas
    if (accent == ThemeAccentOption.OBSIDIAN_PACK) {
        return if (effectiveDark) {
            CollectorPalette(
                isDark = true,
                hasPackNoirShine = true,
                canvas = ObsidianPackCanvas,
                abyss = ObsidianPackAbyss,
                cardSurface = ObsidianPackCard,
                frostSurface = ObsidianPackFrost,
                border = ObsidianPackBorder,
                glassHighlight = ObsidianPackGold.copy(alpha = 0.55f),
                orbWatermark = Color(0xFF9333EA).copy(alpha = 0.14f),
                ink = ObsidianPackInk,
                muted = ObsidianPackMuted,
                accentPrimary = ObsidianPackGold,
                buttonPrimary = Color(0xFF7E22CE),
                skyAccent = Color(0xFFA855F7),
                mintAccent = ObsidianPackGoldBright,
                gloomHalo = Color(0xFF6B21A8),
                gold = ObsidianPackGold,
                goldBright = ObsidianPackGoldBright,
                goldText = ObsidianPackGoldBright,
                goldSurfaceTint = Color(0xFF271B09),
                crimson = MoonlightCrimson,
                navBar = Color(0xFF0B0714),
                goldButtonContent = ObsidianPackCanvas,
                heroTopScrim = Color(0xFF22133B).copy(alpha = 0.88f),
                heroMidScrim = ObsidianPackCanvas.copy(alpha = 0.94f),
                misprintLabel = ObsidianPackGoldBright
            )
        } else {
            val noirLightCanvas = Color(0xFFF6F2FC)
            val noirLightAbyss = Color(0xFFEDE4FA)
            CollectorPalette(
                isDark = false,
                hasPackNoirShine = true,
                canvas = noirLightCanvas,
                abyss = noirLightAbyss,
                cardSurface = Color(0xFFFCFAFF),
                frostSurface = accent.lightFrost,
                border = Color(0xFFD8C5F4),
                glassHighlight = Color(0xFFE9D8FF),
                orbWatermark = Color(0xFF7E22CE).copy(alpha = 0.08f),
                ink = Color(0xFF160E26),
                muted = Color(0xFF5D4C78),
                accentPrimary = accent.lightPrimary,
                buttonPrimary = accent.lightButton,
                skyAccent = accent.lightSky,
                mintAccent = accent.lightMint,
                gloomHalo = accent.lightHalo,
                gold = ObsidianPackGold,
                goldBright = ObsidianPackGoldBright,
                goldText = PearlLightGoldText,
                goldSurfaceTint = Color(0xFFFFF7DC),
                crimson = PearlLightCrimson,
                navBar = Color(0xFFFAF7FF),
                goldButtonContent = Color(0xFF332000),
                heroTopScrim = accent.lightFrost.copy(alpha = 0.80f),
                heroMidScrim = noirLightCanvas.copy(alpha = 0.92f),
                misprintLabel = accent.lightPrimary
            )
        }
    }
    return if (effectiveDark) {
        val isCelestial = accent == ThemeAccentOption.CELESTIAL_GOLD
        val darkCanvas = if (isCelestial) Color(0xFF0A0805) else MoonlightNight
        val darkAbyss = if (isCelestial) Color(0xFF141008) else MoonlightAbyss
        val darkCard = if (isCelestial) Color(0xFF1C160C) else MoonlightCard
        val darkBorder = if (isCelestial) Color(0xFF6B4F14) else MoonlightBorder
        CollectorPalette(
            isDark = true,
            hasPackNoirShine = accent.hasPackNoirShine,
            canvas = darkCanvas,
            abyss = darkAbyss,
            cardSurface = darkCard,
            frostSurface = accent.darkFrost,
            border = darkBorder,
            glassHighlight = if (isCelestial) ObsidianPackGold.copy(alpha = 0.58f) else Color(0xFF3E4F73),
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
            navBar = darkAbyss,
            goldButtonContent = darkCanvas,
            heroTopScrim = accent.darkFrost.copy(alpha = 0.86f),
            heroMidScrim = darkCanvas.copy(alpha = 0.92f),
            misprintLabel = accent.darkPrimary
        )
    } else {
        CollectorPalette(
            isDark = false,
            hasPackNoirShine = false,
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
