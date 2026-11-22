package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.PrismCyan

enum class MisprintAnomaly(
    val title: String,
    val description: String,
    val accentA: Color,
    val accentB: Color,
    val valueMultiplier: Int
) {
    INVERTED_CHROMA(
        title = "Inverted Chroma",
        description = "Factory prism spectrum inverted during foil press.",
        accentA = MisprintNeon,
        accentB = MisprintMagenta,
        valueMultiplier = 5
    ),
    GOLD_FOIL_BLEED(
        title = "Gold Foil Bleed",
        description = "Molten 24K border stamp overflowed onto artwork.",
        accentA = ArenaGold,
        accentB = Color(0xFFEA580C),
        valueMultiplier = 6
    ),
    PRISMATIC_GHOST(
        title = "Prismatic Ghost",
        description = "Translucent spectral refraction layer locked in 1/1 plate.",
        accentA = PrismCyan,
        accentB = Color(0xFF9368E8),
        valueMultiplier = 7
    ),
    DOUBLE_STAMP_ERROR(
        title = "Double-Stamp Error",
        description = "Dual-offset serial strike across holographic laminate.",
        accentA = Color(0xFFE15B74),
        accentB = ArenaGold,
        valueMultiplier = 6
    ),
    VOID_GLITCH(
        title = "Void Glitch",
        description = "Ultra-rare chromatic scanline desync on master print.",
        accentA = Color(0xFF5FB49C),
        accentB = Color(0xFF64748B),
        valueMultiplier = 8
    ),
    CRIMSON_ECLIPSE(
        title = "Crimson Eclipse",
        description = "Blood-moon ink wash bled across darkened satin lacquer.",
        accentA = Color(0xFFDC4C64),
        accentB = Color(0xFF9F1239),
        valueMultiplier = 7
    ),
    HOLO_SHATTER(
        title = "Holo Shatter",
        description = "Crystalline diamond-shard foil press stamped diagonally.",
        accentA = Color(0xFF7DD3FC),
        accentB = Color(0xFFFDE68A),
        valueMultiplier = 7
    ),
    SEPIA_RELIC(
        title = "Sepia Relic",
        description = "Vintage archival bronze test print with sovereign seal.",
        accentA = Color(0xFFD97706),
        accentB = Color(0xFF92400E),
        valueMultiplier = 6
    ),
    EMERALD_OVERPRINT(
        title = "Emerald Overprint",
        description = "Verdant jade pigment over-inked onto character highlights.",
        accentA = Color(0xFF10B981),
        accentB = Color(0xFF34D399),
        valueMultiplier = 6
    ),
    STARLIGHT_NEGATIVE(
        title = "Starlight Negative",
        description = "Astral cyanotype blueprint exposed on a 1/1 test sheet.",
        accentA = Color(0xFF60A5FA),
        accentB = Color(0xFFA78BFA),
        valueMultiplier = 8
    );

    companion object {
        const val MAX_VARIANTS = 10

        fun fromNameSafe(name: String?): MisprintAnomaly? =
            if (name.isNullOrBlank()) null
            else entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
