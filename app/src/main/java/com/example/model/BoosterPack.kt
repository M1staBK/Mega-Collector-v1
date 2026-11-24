package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.SkyAzure

object MegaPackConfig {
    const val PACK_TITLE = "MEGA Legends Pack"
    const val PACK_SUBTITLE = "5 Cards per Pack • Full 1,100+ MLBBDex Pool • 0.005% 1/1 Misprint"
    const val CARDS_PER_PACK = 5
    const val BASE_COIN_COST_PER_PACK = 50
    const val BASE_DIAMOND_COST_PER_PACK = 5
    const val MISPRINT_CHANCE_PERCENT = 0.005
    const val MISPRINT_ODDS_LABEL = "0.005% (1/20,000)"
    @DrawableRes val COVER_DRAWABLE_RES = R.drawable.img_pack_mega_obsidian
    val ACCENT_COLOR: Color = Color(0xFFA855F7)

    val RARITY_WEIGHTS: Map<SkinRarity, Double> = mapOf(
        SkinRarity.COMMON to 32.99,
        SkinRarity.UNCOMMON to 36.0,
        SkinRarity.SPECIAL to 19.0,
        SkinRarity.EPIC to 8.0,
        SkinRarity.LUCKYBOX to 3.0,
        SkinRarity.COLLECTOR to 1.0,
        SkinRarity.LEGEND to 0.01
    )
}

data class PackSatinTheme(
    val id: String,
    val label: String,
    val baseColor: Color,
    val sheenHighlight: Color,
    val logoAccent: Color,
    val crestTint: Color
) {
    companion object {
        val ALL = listOf(
            PackSatinTheme(
                id = "obsidian_royale",
                label = "Obsidian Foil",
                baseColor = Color(0xFF0B0812),
                sheenHighlight = Color(0xFFD8B4FE),
                logoAccent = Color(0xFF9333EA),
                crestTint = Color(0xFFFACC15)
            ),
            PackSatinTheme(
                id = "azure_silk",
                label = "Azure Silk",
                baseColor = Color(0xFF1E3A5F),
                sheenHighlight = Color(0xFF93C5FD),
                logoAccent = Color(0xFF38BDF8),
                crestTint = Color(0xFFFDE68A)
            ),
            PackSatinTheme(
                id = "rose_champagne",
                label = "Rose Satin",
                baseColor = Color(0xFF5B2741),
                sheenHighlight = Color(0xFFFBCFE8),
                logoAccent = Color(0xFFF472B6),
                crestTint = Color(0xFFFEF3C7)
            ),
            PackSatinTheme(
                id = "emerald_foil",
                label = "Emerald Satin",
                baseColor = Color(0xFF14483A),
                sheenHighlight = Color(0xFFA7F3D0),
                logoAccent = Color(0xFF34D399),
                crestTint = Color(0xFFFDE68A)
            ),
            PackSatinTheme(
                id = "amethyst_velvet",
                label = "Violet Satin",
                baseColor = Color(0xFF3B265E),
                sheenHighlight = Color(0xFFDDD6FE),
                logoAccent = Color(0xFFA78BFA),
                crestTint = Color(0xFFFCD34D)
            ),
            PackSatinTheme(
                id = "ruby_crimson",
                label = "Crimson Silk",
                baseColor = Color(0xFF5E1B2B),
                sheenHighlight = Color(0xFFFECDD3),
                logoAccent = Color(0xFFFB7185),
                crestTint = Color(0xFFFEF08A)
            ),
            PackSatinTheme(
                id = "amber_sunburst",
                label = "Champagne Gold",
                baseColor = Color(0xFF563914),
                sheenHighlight = Color(0xFFFEF3C7),
                logoAccent = Color(0xFFFBBF24),
                crestTint = Color(0xFFFFFFFF)
            ),
            PackSatinTheme(
                id = "moonlight_platinum",
                label = "Platinum Satin",
                baseColor = Color(0xFF283244),
                sheenHighlight = Color(0xFFE2E8F0),
                logoAccent = Color(0xFF94A3B8),
                crestTint = Color(0xFF93C5FD)
            )
        )

        fun random(previousId: String? = null): PackSatinTheme {
            val pool = ALL.filter { it.id != previousId }.ifEmpty { ALL }
            return pool.random()
        }
    }
}

enum class PackBundleOption(
    val id: String,
    val packCount: Int,
    val discountPercent: Int,
    val label: String,
    val badgeText: String?,
    val coinCost: Int,
    val fullCoinCost: Int,
    val diamondCost: Int
) {
    SINGLE(
        id = "bundle_1",
        packCount = 1,
        discountPercent = 0,
        label = "1 Pack (5 Cards)",
        badgeText = null,
        coinCost = 50,
        fullCoinCost = 50,
        diamondCost = 5
    ),
    FIVE_PACK(
        id = "bundle_5",
        packCount = 5,
        discountPercent = 5,
        label = "5 Packs (25 Cards)",
        badgeText = "5% OFF",
        coinCost = 237,      // 5 * 50 * 0.95 = 237.5 -> 237
        fullCoinCost = 250,
        diamondCost = 23     // 5 * 5 * 0.95 = 23.75 -> 23
    ),
    TEN_PACK(
        id = "bundle_10",
        packCount = 10,
        discountPercent = 10,
        label = "10 Packs (50 Cards)",
        badgeText = "10% OFF",
        coinCost = 450,      // 10 * 50 * 0.90
        fullCoinCost = 500,
        diamondCost = 45     // 10 * 5 * 0.90
    );

    val totalCards: Int get() = packCount * MegaPackConfig.CARDS_PER_PACK
}

data class DiamondExchangeOption(
    val id: String,
    val diamondCost: Int,
    val coinReward: Int,
    val bonusLabel: String? = null
) {
    companion object {
        val ALL = listOf(
            DiamondExchangeOption("ex_5", diamondCost = 5, coinReward = 50),
            DiamondExchangeOption("ex_22", diamondCost = 22, coinReward = 250, bonusLabel = "+30 Bonus"),
            DiamondExchangeOption("ex_42", diamondCost = 42, coinReward = 500, bonusLabel = "+80 Bonus")
        )
    }
}
