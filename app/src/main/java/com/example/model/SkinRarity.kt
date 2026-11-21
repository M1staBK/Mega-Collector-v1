package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RarityCollector
import com.example.ui.theme.RarityCommon
import com.example.ui.theme.RarityEpic
import com.example.ui.theme.RarityLegend
import com.example.ui.theme.RarityLuckybox
import com.example.ui.theme.RaritySpecial
import com.example.ui.theme.RarityUncommon

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
        basePullWeight = 32.99,
        recycleGold = 30,
        prestigePower = 10,
        suspenseDelayMs = 200L
    ),
    UNCOMMON(
        displayName = "Uncommon",
        tierRank = 2,
        primaryColor = RarityUncommon,
        secondaryColor = Color(0xFF047857),
        basePullWeight = 36.0,
        recycleGold = 65,
        prestigePower = 25,
        suspenseDelayMs = 350L
    ),
    SPECIAL(
        displayName = "Special",
        tierRank = 3,
        primaryColor = RaritySpecial,
        secondaryColor = Color(0xFF0369A1),
        basePullWeight = 19.0,
        recycleGold = 140,
        prestigePower = 60,
        suspenseDelayMs = 600L
    ),
    EPIC(
        displayName = "Epic",
        tierRank = 4,
        primaryColor = RarityEpic,
        secondaryColor = Color(0xFF5B21B6),
        basePullWeight = 8.0,
        recycleGold = 300,
        prestigePower = 150,
        suspenseDelayMs = 950L
    ),
    LUCKYBOX(
        displayName = "Luckybox",
        tierRank = 5,
        primaryColor = RarityLuckybox,
        secondaryColor = Color(0xFF9D174D),
        basePullWeight = 3.0,
        recycleGold = 550,
        prestigePower = 320,
        suspenseDelayMs = 1350L
    ),
    COLLECTOR(
        displayName = "Collector",
        tierRank = 6,
        primaryColor = RarityCollector,
        secondaryColor = Color(0xFFB45309),
        basePullWeight = 1.0,
        recycleGold = 950,
        prestigePower = 600,
        suspenseDelayMs = 1800L
    ),
    LEGEND(
        displayName = "Legend",
        tierRank = 7,
        primaryColor = RarityLegend,
        secondaryColor = Color(0xFF881337),
        basePullWeight = 0.01,
        recycleGold = 2500,
        prestigePower = 1500,
        suspenseDelayMs = 2400L
    );

    companion object {
        fun fromNameSafe(name: String?): SkinRarity = when {
            name.equals("ELITE", ignoreCase = true) -> UNCOMMON
            else -> entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: COMMON
        }

        fun fromApiTier(rawTier: String?, skinName: String = ""): SkinRarity {
            val tierClean = rawTier.orEmpty().trim().lowercase()
            if (tierClean.isNotEmpty() && tierClean != "unknown" && tierClean != "other" && tierClean != "none") {
                classifyText(tierClean)?.let { return it }
            }
            val nameClean = skinName.trim().lowercase()
            if (nameClean.isNotEmpty()) {
                classifyText(nameClean)?.let { return it }
            }
            return COMMON
        }

        private fun classifyText(text: String): SkinRarity? = when {
            text.contains("legend") ||
                text.contains("prime") ||
                text.contains("zenith") ||
                text.contains("aspirant") ||
                text.contains("exorcist") ||
                text.contains("11.11") -> LEGEND
            text.contains("collector") ||
                text.contains("grand") ||
                text.contains("kof") ||
                text.contains("transformers") ||
                text.contains("sanrio") ||
                text.contains("jujutsu") ||
                text.contains("mistbender") ||
                text.contains("abyss") ||
                text.contains("ducati") ||
                text.contains("neobeast") ||
                text.contains("kishin") ||
                text.contains("soul vessel") -> COLLECTOR
            text.contains("luckybox") ||
                text.contains("lucky box") ||
                text.contains("limited epic") ||
                text.contains("vault") -> LUCKYBOX
            text.contains("epic") ||
                text.contains("zodiac") ||
                text.contains("venom") ||
                text.contains("saber") ||
                text.contains("s.a.b.e.r") ||
                text.contains("lightborn") ||
                text.contains("stun") ||
                text.contains("m-world") ||
                text.contains("sparkle") ||
                text.contains("dragon tamer") ||
                text.contains("blazing") ||
                text.contains("valentine") -> EPIC
            text.contains("special") ||
                text.contains("starlight") ||
                text.contains("annual") ||
                text.contains("summer") ||
                text.contains("halloween") ||
                text.contains("christmas") ||
                text.contains("mpl") ||
                text.contains("msc") ||
                text.contains("lunar") ||
                text.contains("create") -> SPECIAL
            text.contains("uncommon") ||
                text.contains("elite") ||
                text.contains("season") ||
                text.contains("painted") -> UNCOMMON
            text.contains("common") ||
                text.contains("basic") ||
                text.contains("normal") ||
                text.contains("classic") ||
                text.contains("default") -> COMMON
            else -> null
        }
    }
}
