package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MythicCrimson
import com.example.ui.theme.PrismCyan

enum class BoosterPack(
    val id: String,
    val title: String,
    val subtitle: String,
    val costGold: Int,
    val cardsPerPack: Int,
    val guaranteedMinRarity: SkinRarity?,
    val misprintOneInDenominator: Int,
    val rarityWeights: Map<SkinRarity, Double>,
    val accentColor: Color,
    @DrawableRes val coverDrawableRes: Int
) {
    STARTER_PACK(
        id = "starter_pack",
        title = "Starter Arena Pack",
        subtitle = "3 Cards • Authentic Odds • Rare 1/250 Misprint",
        costGold = 250,
        cardsPerPack = 3,
        guaranteedMinRarity = null,
        misprintOneInDenominator = 250,
        rarityWeights = mapOf(
            SkinRarity.COMMON to 55.0,
            SkinRarity.ELITE to 28.0,
            SkinRarity.SPECIAL to 12.0,
            SkinRarity.EPIC to 4.2,
            SkinRarity.COLLECTOR to 0.7,
            SkinRarity.LEGEND to 0.1
        ),
        accentColor = PrismCyan,
        coverDrawableRes = R.drawable.img_pack_mythic
    ),
    MYTHIC_PRISM_PACK(
        id = "mythic_prism_pack",
        title = "Mythic Prism Pack",
        subtitle = "5 Cards • 1 Guaranteed Epic+ • 1/120 Misprint",
        costGold = 600,
        cardsPerPack = 5,
        guaranteedMinRarity = SkinRarity.EPIC,
        misprintOneInDenominator = 120,
        rarityWeights = mapOf(
            SkinRarity.COMMON to 18.0,
            SkinRarity.ELITE to 24.0,
            SkinRarity.SPECIAL to 25.0,
            SkinRarity.EPIC to 21.0,
            SkinRarity.COLLECTOR to 9.0,
            SkinRarity.LEGEND to 3.0
        ),
        accentColor = ArenaGold,
        coverDrawableRes = R.drawable.img_pack_mythic
    ),
    IMPERIAL_LEGEND_VAULT(
        id = "imperial_legend_vault",
        title = "Imperial Legend Vault",
        subtitle = "5 Cards • Guaranteed Collector/Legend • 1/60 Misprint",
        costGold = 1400,
        cardsPerPack = 5,
        guaranteedMinRarity = SkinRarity.COLLECTOR,
        misprintOneInDenominator = 60,
        rarityWeights = mapOf(
            SkinRarity.COMMON to 0.0,
            SkinRarity.ELITE to 10.0,
            SkinRarity.SPECIAL to 20.0,
            SkinRarity.EPIC to 32.0,
            SkinRarity.COLLECTOR to 24.0,
            SkinRarity.LEGEND to 14.0
        ),
        accentColor = MythicCrimson,
        coverDrawableRes = R.drawable.img_pack_legend
    );

    companion object {
        fun fromId(id: String): BoosterPack =
            entries.firstOrNull { it.id == id } ?: STARTER_PACK
    }
}
