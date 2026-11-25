package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * 22-tier MLBB Collection Level ladder ordered from entry (Novice 3, Lv.1) to rarest (World, Lv.22).
 * Every rank below World features 3 sub-rank variants (3 -> 2 -> 1), with Mega Collector colored
 * bright yellow/gold above Exalted Collector.
 */
enum class MlbbCollectionTier(
    val levelNumber: Int,
    val title: String,
    val shortLabel: String,
    val minPower: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val trimColor: Color,
    val coreColor: Color,
    val isWingedCrest: Boolean,
    val subRankNumber: Int = 0
) {
    NOVICE_3(
        levelNumber = 1,
        title = "Novice Collector 3",
        shortLabel = "Novice 3",
        minPower = 0,
        primaryColor = Color(0xFF475569),
        secondaryColor = Color(0xFF1E293B),
        trimColor = Color(0xFF94A3B8),
        coreColor = Color(0xFFCBD5E1),
        isWingedCrest = false,
        subRankNumber = 3
    ),
    NOVICE_2(
        levelNumber = 2,
        title = "Novice Collector 2",
        shortLabel = "Novice 2",
        minPower = 60,
        primaryColor = Color(0xFF526278),
        secondaryColor = Color(0xFF233044),
        trimColor = Color(0xFFA3B1C6),
        coreColor = Color(0xFFD6DFEB),
        isWingedCrest = false,
        subRankNumber = 2
    ),
    NOVICE_1(
        levelNumber = 3,
        title = "Novice Collector 1",
        shortLabel = "Novice 1",
        minPower = 120,
        primaryColor = Color(0xFF64748B),
        secondaryColor = Color(0xFF2A384E),
        trimColor = Color(0xFFB8C4D6),
        coreColor = Color(0xFFE2E8F0),
        isWingedCrest = false,
        subRankNumber = 1
    ),
    JUNIOR_3(
        levelNumber = 4,
        title = "Junior Collector 3",
        shortLabel = "Junior 3",
        minPower = 220,
        primaryColor = Color(0xFF8392A6),
        secondaryColor = Color(0xFF3E4C61),
        trimColor = Color(0xFFD1DBE8),
        coreColor = Color(0xFFF1F5F9),
        isWingedCrest = false,
        subRankNumber = 3
    ),
    JUNIOR_2(
        levelNumber = 5,
        title = "Junior Collector 2",
        shortLabel = "Junior 2",
        minPower = 340,
        primaryColor = Color(0xFF94A3B8),
        secondaryColor = Color(0xFF475569),
        trimColor = Color(0xFFE2E8F0),
        coreColor = Color(0xFFF8FAFC),
        isWingedCrest = false,
        subRankNumber = 2
    ),
    JUNIOR_1(
        levelNumber = 6,
        title = "Junior Collector 1",
        shortLabel = "Junior 1",
        minPower = 480,
        primaryColor = Color(0xFFA6B4C8),
        secondaryColor = Color(0xFF526278),
        trimColor = Color(0xFFF1F5F9),
        coreColor = Color(0xFFFFFFFF),
        isWingedCrest = false,
        subRankNumber = 1
    ),
    SEASONED_3(
        levelNumber = 7,
        title = "Seasoned Collector 3",
        shortLabel = "Seasoned 3",
        minPower = 650,
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF065F46),
        trimColor = Color(0xFF34D399),
        coreColor = Color(0xFFA7F3D0),
        isWingedCrest = false,
        subRankNumber = 3
    ),
    SEASONED_2(
        levelNumber = 8,
        title = "Seasoned Collector 2",
        shortLabel = "Seasoned 2",
        minPower = 850,
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        trimColor = Color(0xFF6EE7B7),
        coreColor = Color(0xFFD1FAE5),
        isWingedCrest = false,
        subRankNumber = 2
    ),
    SEASONED_1(
        levelNumber = 9,
        title = "Seasoned Collector 1",
        shortLabel = "Seasoned 1",
        minPower = 1100,
        primaryColor = Color(0xFF14B8A6),
        secondaryColor = Color(0xFF0F766E),
        trimColor = Color(0xFF99F6E4),
        coreColor = Color(0xFFECFDF5),
        isWingedCrest = false,
        subRankNumber = 1
    ),
    EXPERT_3(
        levelNumber = 10,
        title = "Expert Collector 3",
        shortLabel = "Expert 3",
        minPower = 1400,
        primaryColor = Color(0xFF0891B2),
        secondaryColor = Color(0xFF164E63),
        trimColor = Color(0xFF22D3EE),
        coreColor = Color(0xFFCFFAFE),
        isWingedCrest = false,
        subRankNumber = 3
    ),
    EXPERT_2(
        levelNumber = 11,
        title = "Expert Collector 2",
        shortLabel = "Expert 2",
        minPower = 1750,
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF0E7490),
        trimColor = Color(0xFF67E8F9),
        coreColor = Color(0xFFE0F2FE),
        isWingedCrest = false,
        subRankNumber = 2
    ),
    EXPERT_1(
        levelNumber = 12,
        title = "Expert Collector 1",
        shortLabel = "Expert 1",
        minPower = 2150,
        primaryColor = Color(0xFF0EA5E9),
        secondaryColor = Color(0xFF0369A1),
        trimColor = Color(0xFF7DD3FC),
        coreColor = Color(0xFFF0F9FF),
        isWingedCrest = false,
        subRankNumber = 1
    ),
    RENOWNED_3(
        levelNumber = 13,
        title = "Renowned Collector 3",
        shortLabel = "Renowned 3",
        minPower = 2650,
        primaryColor = Color(0xFF6D28D9),
        secondaryColor = Color(0xFF3B0764),
        trimColor = Color(0xFFA78BFA),
        coreColor = Color(0xFFEDE9FE),
        isWingedCrest = true,
        subRankNumber = 3
    ),
    RENOWNED_2(
        levelNumber = 14,
        title = "Renowned Collector 2",
        shortLabel = "Renowned 2",
        minPower = 3200,
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFF4C1D95),
        trimColor = Color(0xFFC4B5FD),
        coreColor = Color(0xFFF3E8FF),
        isWingedCrest = true,
        subRankNumber = 2
    ),
    RENOWNED_1(
        levelNumber = 15,
        title = "Renowned Collector 1",
        shortLabel = "Renowned 1",
        minPower = 3850,
        primaryColor = Color(0xFF9333EA),
        secondaryColor = Color(0xFF581C87),
        trimColor = Color(0xFFD8B4FE),
        coreColor = Color(0xFFFAF5FF),
        isWingedCrest = true,
        subRankNumber = 1
    ),
    EXALTED_3(
        levelNumber = 16,
        title = "Exalted Collector 3",
        shortLabel = "Exalted 3",
        minPower = 4700,
        primaryColor = Color(0xFFBE185D),
        secondaryColor = Color(0xFF831843),
        trimColor = Color(0xFFF9A8D4),
        coreColor = Color(0xFFFCE7F3),
        isWingedCrest = true,
        subRankNumber = 3
    ),
    EXALTED_2(
        levelNumber = 17,
        title = "Exalted Collector 2",
        shortLabel = "Exalted 2",
        minPower = 6200,
        primaryColor = Color(0xFFDB2777),
        secondaryColor = Color(0xFF9D174D),
        trimColor = Color(0xFFFBCFE8),
        coreColor = Color(0xFFFDF2F8),
        isWingedCrest = true,
        subRankNumber = 2
    ),
    EXALTED_1(
        levelNumber = 18,
        title = "Exalted Collector 1",
        shortLabel = "Exalted 1",
        minPower = 8000,
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFF9D174D),
        trimColor = Color(0xFFFDE68A),
        coreColor = Color(0xFFFFF1F2),
        isWingedCrest = true,
        subRankNumber = 1
    ),
    MEGA_3(
        levelNumber = 19,
        title = "Mega Collector 3",
        shortLabel = "Mega 3",
        minPower = 16000,
        primaryColor = Color(0xFFCA8A04),
        secondaryColor = Color(0xFF713F12),
        trimColor = Color(0xFFFDE047),
        coreColor = Color(0xFFFEF9C3),
        isWingedCrest = true,
        subRankNumber = 3
    ),
    MEGA_2(
        levelNumber = 20,
        title = "Mega Collector 2",
        shortLabel = "Mega 2",
        minPower = 21000,
        primaryColor = Color(0xFFEAB308),
        secondaryColor = Color(0xFF854D0E),
        trimColor = Color(0xFFFEF08A),
        coreColor = Color(0xFFFEFCE8),
        isWingedCrest = true,
        subRankNumber = 2
    ),
    MEGA_1(
        levelNumber = 21,
        title = "Mega Collector 1",
        shortLabel = "Mega 1",
        minPower = 27000,
        primaryColor = Color(0xFFFACC15),
        secondaryColor = Color(0xFFA16207),
        trimColor = Color(0xFFFFFBEB),
        coreColor = Color(0xFFFFFFFF),
        isWingedCrest = true,
        subRankNumber = 1
    ),
    WORLD(
        levelNumber = 22,
        title = "World Collector",
        shortLabel = "World",
        minPower = 36000,
        primaryColor = Color(0xFFDC2626),
        secondaryColor = Color(0xFF7F1D1D),
        trimColor = Color(0xFFF59E0B),
        coreColor = Color(0xFFFFFBEB),
        isWingedCrest = true,
        subRankNumber = 0
    );

    /** Distinct Legend skins a collector must own before this tier unlocks (power alone is not enough). */
    val legendsRequired: Int
        get() = when (this) {
            MEGA_3 -> 1
            MEGA_2 -> 2
            MEGA_1 -> 3
            WORLD -> 5
            else -> 0
        }

    fun nextTier(): MlbbCollectionTier? =
        entries.firstOrNull { it.levelNumber == levelNumber + 1 }

    companion object {
        /** Reference order: Left/Top is rarest (WORLD) down to entry (NOVICE_3). */
        val RAREST_LEFT_TO_RIGHT: List<MlbbCollectionTier> = entries.sortedByDescending { it.levelNumber }

        fun uniqueLegendCount(cards: List<OwnedCard>): Int =
            cards.filter { it.rarity == SkinRarity.LEGEND }.map { it.catalogId }.distinct().size

        /** Tier from power AND Legend ownership: Mega and World need Legend skins, not just duplicates. */
        fun fromCollection(totalPrestige: Int, uniqueLegends: Int): MlbbCollectionTier =
            RAREST_LEFT_TO_RIGHT.firstOrNull {
                totalPrestige >= it.minPower && uniqueLegends >= it.legendsRequired
            } ?: NOVICE_3

        fun fromOwnedCards(cards: List<OwnedCard>): MlbbCollectionTier =
            fromCollection(cards.sumOf { it.prestigeScore }, uniqueLegendCount(cards))

        fun fromPrestige(totalPrestige: Int): MlbbCollectionTier =
            RAREST_LEFT_TO_RIGHT.firstOrNull { totalPrestige >= it.minPower } ?: NOVICE_3

        fun fromLevel(level: Int): MlbbCollectionTier =
            entries.firstOrNull { it.levelNumber == level } ?: NOVICE_3

        fun fromRankTitle(title: String?): MlbbCollectionTier? {
            if (title.isNullOrBlank()) return null
            entries.firstOrNull { it.title.equals(title, ignoreCase = true) }?.let { return it }
            return when {
                title.contains("World", ignoreCase = true) -> WORLD
                title.contains("Mega", ignoreCase = true) -> MEGA_1
                title.contains("Exalted", ignoreCase = true) -> EXALTED_1
                title.contains("Renowned", ignoreCase = true) -> RENOWNED_1
                title.contains("Expert", ignoreCase = true) -> EXPERT_1
                title.contains("Seasoned", ignoreCase = true) -> SEASONED_1
                title.contains("Junior", ignoreCase = true) -> JUNIOR_1
                title.contains("Novice", ignoreCase = true) -> NOVICE_1
                else -> null
            }
        }
    }
}
