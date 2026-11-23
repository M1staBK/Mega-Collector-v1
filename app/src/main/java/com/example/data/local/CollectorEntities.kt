package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.MisprintAnomaly
import com.example.model.OwnedCard
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.TradeListing

@Entity(tableName = "skin_catalog")
data class SkinCatalogEntity(
    @PrimaryKey val id: String,
    val skinName: String,
    val heroName: String,
    val role: String,
    val rawTier: String,
    val rarityName: String,
    val imageUrl: String
) {
    fun toDomain() = SkinCatalogItem(
        id = id,
        skinName = skinName,
        heroName = heroName,
        role = role,
        rawTier = rawTier,
        rarity = SkinRarity.fromNameSafe(rarityName),
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(item: SkinCatalogItem) = SkinCatalogEntity(
            id = item.id,
            skinName = item.skinName,
            heroName = item.heroName,
            role = item.role,
            rawTier = item.rawTier,
            rarityName = item.rarity.name,
            imageUrl = item.imageUrl
        )
    }
}

@Entity(tableName = "owned_cards")
data class OwnedCardEntity(
    @PrimaryKey(autoGenerate = true) val instanceId: Long = 0L,
    val catalogId: String,
    val skinName: String,
    val heroName: String,
    val role: String,
    val rawTier: String,
    val rarityName: String,
    val imageUrl: String,
    val isMisprint: Boolean,
    val misprintAnomalyName: String?,
    val serialCode: String,
    val pullRatePercent: Double,
    val obtainedAtMs: Long,
    val isListedForTrade: Boolean = false,
    val isHolographic: Boolean = false
) {
    fun toDomain(): OwnedCard {
        val mappedRarity = SkinRarity.fromNameSafe(rarityName)
        return OwnedCard(
            instanceId = instanceId,
            catalogId = catalogId,
            skinName = skinName,
            heroName = heroName,
            role = role,
            rawTier = rawTier,
            rarity = mappedRarity,
            imageUrl = imageUrl,
            isMisprint = isMisprint,
            misprintAnomaly = MisprintAnomaly.fromNameSafe(misprintAnomalyName),
            serialCode = serialCode,
            pullRatePercent = pullRatePercent,
            obtainedAtMs = obtainedAtMs,
            isListedForTrade = isListedForTrade,
            isHolographic = isHolographic && mappedRarity.tierRank >= SkinRarity.COLLECTOR.tierRank
        )
    }
}

@Entity(tableName = "trade_listings")
data class TradeListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardInstanceId: Long,
    val skinName: String,
    val heroName: String,
    val rarityName: String,
    val imageUrl: String,
    val isMisprint: Boolean,
    val misprintAnomalyName: String?,
    val serialCode: String,
    val seekingNote: String,
    val tradeCode: String,
    val createdAtMs: Long
) {
    fun toDomain() = TradeListing(
        id = id,
        cardInstanceId = cardInstanceId,
        skinName = skinName,
        heroName = heroName,
        rarity = SkinRarity.fromNameSafe(rarityName),
        imageUrl = imageUrl,
        isMisprint = isMisprint,
        misprintAnomaly = MisprintAnomaly.fromNameSafe(misprintAnomalyName),
        serialCode = serialCode,
        seekingNote = seekingNote,
        tradeCode = tradeCode,
        createdAtMs = createdAtMs
    )
}
