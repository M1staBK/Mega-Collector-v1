package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Official 8 MLBB Collection Level tiers ordered from least rare (Tier 1, rightmost in reference)
 * to rarest (Tier 8, leftmost in reference).
 */
enum class MlbbCollectionTier(
    val levelNumber: Int,
    val title: String,
    val minPower: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val trimColor: Color,
    val coreColor: Color,
    val isWingedCrest: Boolean
) {
    NOVICE(
        levelNumber = 1,
        title = "Novice Collector",
        minPower = 0,
        primaryColor = Color(0xFF475569),
        secondaryColor = Color(0xFF1E293B),
        trimColor = Color(0xFF94A3B8),
        coreColor = Color(0xFFCBD5E1),
        isWingedCrest = false
    ),
    JUNIOR(
        levelNumber = 2,
        title = "Junior Collector",
        minPower = 150,
        primaryColor = Color(0xFF94A3B8),
        secondaryColor = Color(0xFF475569),
        trimColor = Color(0xFFE2E8F0),
        coreColor = Color(0xFFF8FAFC),
        isWingedCrest = false
    ),
    SEASONED(
        levelNumber = 3,
        title = "Seasoned Collector",
        minPower = 450,
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        trimColor = Color(0xFF6EE7B7),
        coreColor = Color(0xFFD1FAE5),
        isWingedCrest = false
    ),
    EXPERT(
        levelNumber = 4,
        title = "Expert Collector",
        minPower = 1000,
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF0E7490),
        trimColor = Color(0xFF67E8F9),
        coreColor = Color(0xFFE0F2FE),
        isWingedCrest = false
    ),
    RENOWNED(
        levelNumber = 5,
        title = "Renowned Collector",
        minPower = 2200,
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFF4C1D95),
        trimColor = Color(0xFFC4B5FD),
        coreColor = Color(0xFFF3E8FF),
        isWingedCrest = true
    ),
    MEGA(
        levelNumber = 6,
        title = "Mega Collector",
        minPower = 4500,
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFF9D174D),
        trimColor = Color(0xFFFDE68A),
        coreColor = Color(0xFFFCE7F3),
        isWingedCrest = true
    ),
    EXALTED(
        levelNumber = 7,
        title = "Exalted Collector",
        minPower = 9000,
        primaryColor = Color(0xFFEA580C),
        secondaryColor = Color(0xFF9A3412),
        trimColor = Color(0xFFFBBF24),
        coreColor = Color(0xFFFEF3C7),
        isWingedCrest = true
    ),
    WORLD(
        levelNumber = 8,
        title = "World Collector",
        minPower = 16000,
        primaryColor = Color(0xFFDC2626),
        secondaryColor = Color(0xFF7F1D1D),
        trimColor = Color(0xFFF59E0B),
        coreColor = Color(0xFFFFFBEB),
        isWingedCrest = true
    );

    fun nextTier(): MlbbCollectionTier? =
        entries.firstOrNull { it.levelNumber == levelNumber + 1 }

    companion object {
        /** Reference order: Left side is rarest (WORLD) down to Right side (NOVICE). */
        val RAREST_LEFT_TO_RIGHT: List<MlbbCollectionTier> = entries.sortedByDescending { it.levelNumber }

        fun fromPrestige(totalPrestige: Int): MlbbCollectionTier =
            RAREST_LEFT_TO_RIGHT.firstOrNull { totalPrestige >= it.minPower } ?: NOVICE

        fun fromLevel(level: Int): MlbbCollectionTier =
            entries.firstOrNull { it.levelNumber == level } ?: NOVICE
    }
}
