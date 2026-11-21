package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RarityCollector
import com.example.ui.theme.RarityCommon
import com.example.ui.theme.RarityElite
import com.example.ui.theme.RarityEpic
import com.example.ui.theme.RarityLegend
import com.example.ui.theme.RaritySpecial

enum class SkinRarity(
    val displayName: String,
    val tierRank: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val basePullWeight: Double,
    val recycleGold: Int,
    val prestigePower: Int,
    val suspenseDelayMs: Long
) {
    COMMON(
        displayName = "Common",
        tierRank = 1,
        primaryColor = RarityCommon,
        secondaryColor = Color(0xFF475569),
        basePullWeight = 55.0,
        recycleGold = 35,
        prestigePower = 10,
        suspenseDelayMs = 250L
    ),
    ELITE(
        displayName = "Elite",
        tierRank = 2,
        primaryColor = RarityElite,
        secondaryColor = Color(0xFF0E7490),
        basePullWeight = 28.0,
        recycleGold = 80,
        prestigePower = 30,
        suspenseDelayMs = 500L
    ),
    SPECIAL(
        displayName = "Special",
        tierRank = 3,
        primaryColor = RaritySpecial,
        secondaryColor = Color(0xFF047857),
        basePullWeight = 12.0,
        recycleGold = 160,
        prestigePower = 75,
        suspenseDelayMs = 850L
    ),
    EPIC(
        displayName = "Epic",
        tierRank = 4,
        primaryColor = RarityEpic,
        secondaryColor = Color(0xFF6D28D9),
        basePullWeight = 4.2,
        recycleGold = 350,
        prestigePower = 180,
        suspenseDelayMs = 1350L
    ),
    COLLECTOR(
        displayName = "Collector",
        tierRank = 5,
        primaryColor = RarityCollector,
        secondaryColor = Color(0xFFB45309),
        basePullWeight = 0.7,
        recycleGold = 750,
        prestigePower = 450,
        suspenseDelayMs = 1900L
    ),
    LEGEND(
        displayName = "Legend",
        tierRank = 6,
        primaryColor = RarityLegend,
        secondaryColor = Color(0xFF9F1239),
        basePullWeight = 0.1,
        recycleGold = 1500,
        prestigePower = 1000,
        suspenseDelayMs = 2500L
    );

    companion object {
        fun fromNameSafe(name: String?): SkinRarity =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: COMMON

        fun fromApiTier(rawTier: String?, skinName: String = ""): SkinRarity {
            val normalized = (rawTier.orEmpty() + " " + skinName).lowercase()
            return when {
                normalized.contains("legend") ||
                    normalized.contains("prime") ||
                    normalized.contains("grand") ||
                    normalized.contains("exorcist") ||
                    normalized.contains("aspirant") ||
                    normalized.contains("zenith") -> LEGEND
                normalized.contains("collector") ||
                    normalized.contains("kof") ||
                    normalized.contains("transformers") ||
                    normalized.contains("sanrio") ||
                    normalized.contains("jujutsu") ||
                    normalized.contains("mistbender") ||
                    normalized.contains("abyss") -> COLLECTOR
                normalized.contains("epic") ||
                    normalized.contains("luckybox") ||
                    normalized.contains("zodiac") ||
                    normalized.contains("venom") ||
                    normalized.contains("saber") ||
                    normalized.contains("lightborn") ||
                    normalized.contains("stun") ||
                    normalized.contains("valentine") -> EPIC
                normalized.contains("special") ||
                    normalized.contains("starlight") ||
                    normalized.contains("annual") ||
                    normalized.contains("summer") ||
                    normalized.contains("halloween") ||
                    normalized.contains("christmas") ||
                    normalized.contains("mpl") ||
                    normalized.contains("create") -> SPECIAL
                normalized.contains("elite") ||
                    normalized.contains("season") ||
                    normalized.contains("painted") -> ELITE
                else -> COMMON
            }
        }
    }
}
