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
        valueMultiplier = 6
    ),
    GOLD_FOIL_BLEED(
        title = "Gold Foil Bleed",
        description = "Molten 24K border stamp overflowed onto artwork.",
        accentA = ArenaGold,
        accentB = Color(0xFFFF6D00),
        valueMultiplier = 7
    ),
    PRISMATIC_GHOST(
        title = "Prismatic Ghost",
        description = "Translucent spectral refraction layer locked in 1/1 plate.",
        accentA = PrismCyan,
        accentB = Color(0xFFA855F7),
        valueMultiplier = 8
    ),
    DOUBLE_STAMP_ERROR(
        title = "Double-Stamp Error",
        description = "Dual-offset serial strike across holographic laminate.",
        accentA = Color(0xFFFF3864),
        accentB = ArenaGold,
        valueMultiplier = 7
    ),
    VOID_GLITCH(
        title = "Void Glitch",
        description = "Ultra-rare chromatic scanline desync on master print.",
        accentA = Color(0xFF00F5D4),
        accentB = Color(0xFF6366F1),
        valueMultiplier = 9
    );

    companion object {
        fun fromNameSafe(name: String?): MisprintAnomaly? =
            if (name.isNullOrBlank()) null
            else entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
