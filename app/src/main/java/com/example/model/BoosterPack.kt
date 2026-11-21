package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.SkyAzure

object MegaPackConfig {
    const val PACK_TITLE = "MEGA Legends Pack"
    const val PACK_SUBTITLE = "5 Cards per Pack • Full 1,100+ MLBBDex Pool • 0.005% 1/1 Misprint"
    const val CARDS_PER_PACK = 5
    const val BASE_COIN_COST_PER_PACK = 200
    const val BASE_DIAMOND_COST_PER_PACK = 10
    const val MISPRINT_CHANCE_PERCENT = 0.005
    const val MISPRINT_ODDS_LABEL = "0.005% (1/20,000)"
    @DrawableRes val COVER_DRAWABLE_RES = R.drawable.img_pack_mythic
    val ACCENT_COLOR: Color = SkyAzure

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
        coinCost = 200,
        fullCoinCost = 200,
        diamondCost = 10
    ),
    FIVE_PACK(
        id = "bundle_5",
        packCount = 5,
        discountPercent = 5,
        label = "5 Packs (25 Cards)",
        badgeText = "5% OFF",
        coinCost = 950,      // 5 * 200 * 0.95
        fullCoinCost = 1000,
        diamondCost = 47     // 5 * 10 * 0.95 rounded down
    ),
    TEN_PACK(
        id = "bundle_10",
        packCount = 10,
        discountPercent = 10,
        label = "10 Packs (50 Cards)",
        badgeText = "10% OFF",
        coinCost = 1800,     // 10 * 200 * 0.90
        fullCoinCost = 2000,
        diamondCost = 90     // 10 * 10 * 0.90
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
            DiamondExchangeOption("ex_10", diamondCost = 10, coinReward = 200),
            DiamondExchangeOption("ex_45", diamondCost = 45, coinReward = 1000, bonusLabel = "+100 Bonus"),
            DiamondExchangeOption("ex_85", diamondCost = 85, coinReward = 2000, bonusLabel = "+300 Bonus")
        )
    }
}
