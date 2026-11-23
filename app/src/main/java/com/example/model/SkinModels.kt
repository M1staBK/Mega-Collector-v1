package com.example.model

data class SkinCatalogItem(
    val id: String,
    val skinName: String,
    val heroName: String,
    val role: String,
    val rawTier: String,
    val rarity: SkinRarity,
    val imageUrl: String
)

data class OwnedCard(
    val instanceId: Long,
    val catalogId: String,
    val skinName: String,
    val heroName: String,
    val role: String,
    val rawTier: String,
    val rarity: SkinRarity,
    val imageUrl: String,
    val isMisprint: Boolean,
    val misprintAnomaly: MisprintAnomaly?,
    val serialCode: String,
    val pullRatePercent: Double,
    val obtainedAtMs: Long,
    val isListedForTrade: Boolean,
    val isHolographic: Boolean = false
) {
    /** Only Collector and Legend skins can ever carry Holographic editions. */
    val isHolographicEdition: Boolean
        get() = isHolographic && rarity.tierRank >= SkinRarity.COLLECTOR.tierRank

    val recycleValueGold: Int
        get() = if (isMisprint && misprintAnomaly != null) {
            maxOf(rarity.recycleGold * misprintAnomaly.valueMultiplier, 25)
        } else {
            rarity.recycleGold
        }

    val prestigeScore: Int
        get() {
            val base = if (isMisprint && misprintAnomaly != null) {
                rarity.prestigePower * misprintAnomaly.valueMultiplier
            } else {
                rarity.prestigePower
            }
            return if (isHolographicEdition) (base * 3) / 2 else base
        }
}

data class DailyRewardTier(
    val dayIndex: Int, // 1..7
    val coinAmount: Int,
    val bonusFreePacks: Int = 0,
    val bonusDiamonds: Int = 0
)

data class TradeListing(
    val id: Long,
    val cardInstanceId: Long,
    val skinName: String,
    val heroName: String,
    val rarity: SkinRarity,
    val imageUrl: String,
    val isMisprint: Boolean,
    val misprintAnomaly: MisprintAnomaly?,
    val serialCode: String,
    val seekingNote: String,
    val tradeCode: String,
    val createdAtMs: Long
)

data class DecodedTradeSlip(
    val senderAlias: String,
    val catalogId: String,
    val skinName: String,
    val heroName: String,
    val role: String,
    val rawTier: String,
    val rarity: SkinRarity,
    val imageUrl: String,
    val isMisprint: Boolean,
    val misprintAnomaly: MisprintAnomaly?,
    val serialCode: String,
    val goldSweetener: Int,
    val seekingNote: String,
    val slipId: String,
    val senderPrestige: Int = 1850,
    val senderCardCount: Int = 24,
    val senderBadgeIds: List<String> = emptyList()
)
