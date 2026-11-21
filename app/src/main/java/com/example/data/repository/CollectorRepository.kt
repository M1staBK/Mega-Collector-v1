package com.example.data.repository

import android.util.Base64
import com.example.data.local.CollectorDao
import com.example.data.local.OwnedCardEntity
import com.example.data.local.PlayerPreferencesStore
import com.example.data.local.PlayerWalletState
import com.example.data.local.SkinCatalogEntity
import com.example.data.local.TradeListingEntity
import com.example.data.remote.MlbbDexClient
import com.example.data.remote.SkinJsonParser
import com.example.model.BoosterPack
import com.example.model.DailyRewardTier
import com.example.model.DecodedTradeSlip
import com.example.model.MisprintAnomaly
import com.example.model.OwnedCard
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.TradeListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class CollectorRepository(
    private val dao: CollectorDao,
    private val prefs: PlayerPreferencesStore
) {
    val walletFlow: Flow<PlayerWalletState> = prefs.walletFlow

    val catalogFlow: Flow<List<SkinCatalogItem>> =
        dao.observeCatalog().map { list -> list.map { it.toDomain() } }

    val ownedCardsFlow: Flow<List<OwnedCard>> =
        dao.observeOwnedCards().map { list -> list.map { it.toDomain() } }

    val tradeListingsFlow: Flow<List<TradeListing>> =
        dao.observeTradeListings().map { list -> list.map { it.toDomain() } }

    fun observeCard(instanceId: Long): Flow<OwnedCard?> =
        dao.observeCardById(instanceId).map { it?.toDomain() }

    val dailyRewardTiers: List<DailyRewardTier> = listOf(
        DailyRewardTier(1, 300),
        DailyRewardTier(2, 450),
        DailyRewardTier(3, 600, BoosterPack.STARTER_PACK),
        DailyRewardTier(4, 800),
        DailyRewardTier(5, 1000),
        DailyRewardTier(6, 1250),
        DailyRewardTier(7, 2000, BoosterPack.MYTHIC_PRISM_PACK)
    )

    suspend fun syncSkinsFromApi(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val rawJson = MlbbDexClient.api.getSkinsRaw().string()
            val parsed = SkinJsonParser.parseSkins(rawJson)
            if (parsed.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("MLBBDex returned an empty skin catalog.")
                )
            }
            dao.replaceCatalog(parsed.map { SkinCatalogEntity.fromDomain(it) })
            prefs.markCatalogSynced(System.currentTimeMillis())
            Result.success(parsed.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun openPack(
        pack: BoosterPack,
        wallet: PlayerWalletState,
        useFreeTicket: Boolean
    ): Result<List<OwnedCard>> = withContext(Dispatchers.IO) {
        try {
            val catalog = dao.getAllCatalogOnce().map { it.toDomain() }
            if (catalog.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Skin catalog not synced yet. Tap Sync MLBBDex first.")
                )
            }

            if (useFreeTicket) {
                val hasTicket = when (pack) {
                    BoosterPack.STARTER_PACK -> wallet.bonusFreePacksStarter > 0
                    BoosterPack.MYTHIC_PRISM_PACK -> wallet.bonusFreePacksMythic > 0
                    BoosterPack.IMPERIAL_LEGEND_VAULT -> false
                }
                if (!hasTicket) {
                    return@withContext Result.failure(IllegalStateException("No free pack ticket available."))
                }
            } else if (wallet.arenaGold < pack.costGold) {
                return@withContext Result.failure(
                    IllegalStateException("Not enough Arena Gold. Need ${pack.costGold} Gold.")
                )
            }

            val groupedByRarity = catalog.groupBy { it.rarity }
            val nowMs = System.currentTimeMillis()
            val pulledEntities = mutableListOf<OwnedCardEntity>()
            val totalWeight = pack.rarityWeights.values.sum().coerceAtLeast(1.0)

            for (slotIndex in 0 until pack.cardsPerPack) {
                val isFinalSlot = slotIndex == pack.cardsPerPack - 1
                val minRarity = if (isFinalSlot) pack.guaranteedMinRarity else null

                val rolledRarity = rollRarity(pack.rarityWeights, minRarity)
                val pullRatePct = ((pack.rarityWeights[rolledRarity] ?: 1.0) / totalWeight) * 100.0
                val chosenSkin = pickSkinForRarity(rolledRarity, groupedByRarity, catalog)

                val isMisprint = Random.nextInt(pack.misprintOneInDenominator) == 0
                val anomaly = if (isMisprint) MisprintAnomaly.entries.random() else null
                val serialCode = if (isMisprint) {
                    "1/1 • MP-${randomHex4()}"
                } else {
                    "STD-${randomHex4()}"
                }

                pulledEntities += OwnedCardEntity(
                    catalogId = chosenSkin.id,
                    skinName = chosenSkin.skinName,
                    heroName = chosenSkin.heroName,
                    role = chosenSkin.role,
                    rawTier = chosenSkin.rawTier,
                    rarityName = rolledRarity.name,
                    imageUrl = chosenSkin.imageUrl,
                    isMisprint = isMisprint,
                    misprintAnomalyName = anomaly?.name,
                    serialCode = serialCode,
                    pullRatePercent = String.format(Locale.US, "%.2f", pullRatePct).toDouble(),
                    obtainedAtMs = nowMs + slotIndex,
                    isListedForTrade = false
                )
            }

            // Sort ascending by hype so the best pull is revealed last!
            val sortedEntities = pulledEntities.sortedWith(
                compareBy<OwnedCardEntity> { if (it.isMisprint) 1 else 0 }
                    .thenBy { SkinRarity.fromNameSafe(it.rarityName).tierRank }
            )

            val insertedIds = dao.insertOwnedCards(sortedEntities)
            prefs.spendGoldOrTicketAndRecordPack(
                useTicket = useFreeTicket,
                isMythicTicket = pack == BoosterPack.MYTHIC_PRISM_PACK,
                goldCost = pack.costGold
            )

            val resultCards = sortedEntities.mapIndexed { idx, entity ->
                entity.copy(instanceId = insertedIds[idx]).toDomain()
            }
            Result.success(resultCards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun rollRarity(
        weights: Map<SkinRarity, Double>,
        minRarity: SkinRarity?
    ): SkinRarity {
        val eligible = weights.filter { (rarity, weight) ->
            weight > 0.0 && (minRarity == null || rarity.tierRank >= minRarity.tierRank)
        }
        val total = eligible.values.sum()
        if (total <= 0.0) return minRarity ?: SkinRarity.COMMON

        var roll = Random.nextDouble(total)
        for ((rarity, weight) in eligible) {
            roll -= weight
            if (roll <= 0.0) return rarity
        }
        return eligible.keys.last()
    }

    private fun pickSkinForRarity(
        target: SkinRarity,
        grouped: Map<SkinRarity, List<SkinCatalogItem>>,
        fallbackAll: List<SkinCatalogItem>
    ): SkinCatalogItem {
        val exactPool = grouped[target]
        if (!exactPool.isNullOrEmpty()) return exactPool.random()

        // Find nearest tier in catalog if API lacks a particular exact tier label
        val nearest = grouped.entries
            .filter { it.value.isNotEmpty() }
            .minByOrNull { kotlin.math.abs(it.key.tierRank - target.tierRank) }
        return nearest?.value?.random() ?: fallbackAll.random()
    }

    fun currentLocalEpochDay(): Long {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        return (year.toLong() * 366L) + dayOfYear.toLong()
    }

    suspend fun claimDailyReward(wallet: PlayerWalletState): Result<DailyRewardTier> =
        withContext(Dispatchers.IO) {
            try {
                val today = currentLocalEpochDay()
                if (wallet.lastDailyClaimEpochDay >= today) {
                    return@withContext Result.failure(
                        IllegalStateException("Daily reward already claimed today.")
                    )
                }

                val isConsecutive = wallet.lastDailyClaimEpochDay == today - 1L
                val nextStreakIndex = if (isConsecutive) {
                    (wallet.dailyStreakCount % 7) + 1
                } else {
                    1
                }
                val tier = dailyRewardTiers[nextStreakIndex - 1]
                prefs.recordDailyRewardClaim(
                    epochDay = today,
                    newStreak = nextStreakIndex,
                    goldReward = tier.goldAmount,
                    grantStarterPack = tier.bonusPack == BoosterPack.STARTER_PACK,
                    grantMythicPack = tier.bonusPack == BoosterPack.MYTHIC_PRISM_PACK
                )
                Result.success(tier)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun recycleSingleCard(card: OwnedCard): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getCardById(card.instanceId)
                ?: return@withContext Result.failure(IllegalStateException("Card no longer in Vault."))
            if (existing.isListedForTrade) {
                dao.deleteTradeListingsForCard(existing.instanceId)
            }
            dao.deleteOwnedCardById(existing.instanceId)
            val goldEarned = card.recycleValueGold
            prefs.addGold(goldEarned, fromRecycle = true)
            Result.success(goldEarned)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recycleAllDuplicates(): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val allOwned = dao.getAllOwnedOnce().map { it.toDomain() }
            val byCatalog = allOwned.groupBy { it.catalogId }
            val idsToDelete = mutableListOf<Long>()
            var totalGold = 0

            for ((_, group) in byCatalog) {
                if (group.size <= 1) continue
                // Always keep at least 1 card (preferring highest rarity / oldest),
                // never auto-recycle 1-of-1 Misprints or cards currently listed for trade!
                val sorted = group.sortedWith(
                    compareByDescending<OwnedCard> { it.isMisprint }
                        .thenByDescending { it.isListedForTrade }
                        .thenBy { it.obtainedAtMs }
                )
                val extras = sorted.drop(1).filter { !it.isMisprint && !it.isListedForTrade }
                for (extra in extras) {
                    idsToDelete += extra.instanceId
                    totalGold += extra.recycleValueGold
                }
            }

            if (idsToDelete.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No un-listed non-misprint duplicate cards available to recycle.")
                )
            }

            dao.deleteOwnedCardsByIds(idsToDelete)
            prefs.addGold(totalGold, fromRecycle = true)
            Result.success(idsToDelete.size to totalGold)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTradeListing(
        card: OwnedCard,
        seekingNote: String,
        senderAlias: String,
        goldSweetener: Int = 0
    ): Result<TradeListing> = withContext(Dispatchers.IO) {
        try {
            val slipJson = JSONObject().apply {
                put("v", 1)
                put("slipId", "TS-${randomHex4()}-${randomHex4()}")
                put("sender", senderAlias.ifBlank { "Collector" })
                put("catalogId", card.catalogId)
                put("skinName", card.skinName)
                put("heroName", card.heroName)
                put("role", card.role)
                put("rawTier", card.rawTier)
                put("rarity", card.rarity.name)
                put("imageUrl", card.imageUrl)
                put("isMisprint", card.isMisprint)
                put("anomaly", card.misprintAnomaly?.name ?: "")
                put("serial", card.serialCode)
                put("gold", goldSweetener.coerceAtLeast(0))
                put("seeking", seekingNote.trim().ifBlank { "Open to Epic+ or Misprint offers" })
            }
            val encoded = "MEGA-TRADE-V1:" + Base64.encodeToString(
                slipJson.toString().toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            val now = System.currentTimeMillis()
            val entity = TradeListingEntity(
                cardInstanceId = card.instanceId,
                skinName = card.skinName,
                heroName = card.heroName,
                rarityName = card.rarity.name,
                imageUrl = card.imageUrl,
                isMisprint = card.isMisprint,
                misprintAnomalyName = card.misprintAnomaly?.name,
                serialCode = card.serialCode,
                seekingNote = seekingNote.trim().ifBlank { "Open to offers" },
                tradeCode = encoded,
                createdAtMs = now
            )
            val id = dao.insertTradeListing(entity)
            dao.setCardListedForTrade(card.instanceId, true)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelTradeListing(listing: TradeListing): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteTradeListingById(listing.id)
            dao.setCardListedForTrade(listing.cardInstanceId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun decodeTradeSlip(rawCode: String): Result<DecodedTradeSlip> {
        return try {
            val cleaned = rawCode.trim()
            if (!cleaned.startsWith("MEGA-TRADE-V1:")) {
                return Result.failure(IllegalArgumentException("Invalid Trade Slip format. Expected MEGA-TRADE-V1:..."))
            }
            val payload = cleaned.removePrefix("MEGA-TRADE-V1:")
            val jsonStr = String(Base64.decode(payload, Base64.DEFAULT), Charsets.UTF_8)
            val obj = JSONObject(jsonStr)
            val slip = DecodedTradeSlip(
                senderAlias = obj.optString("sender", "Partner"),
                catalogId = obj.optString("catalogId", "unknown"),
                skinName = obj.getString("skinName"),
                heroName = obj.getString("heroName"),
                role = obj.optString("role", "Hero"),
                rawTier = obj.optString("rawTier", "Epic"),
                rarity = SkinRarity.fromNameSafe(obj.optString("rarity")),
                imageUrl = obj.optString("imageUrl", ""),
                isMisprint = obj.optBoolean("isMisprint", false),
                misprintAnomaly = MisprintAnomaly.fromNameSafe(obj.optString("anomaly")),
                serialCode = obj.optString("serial", "STD-0000"),
                goldSweetener = obj.optInt("gold", 0).coerceAtLeast(0),
                seekingNote = obj.optString("seeking", ""),
                slipId = obj.optString("slipId", "TS-0000")
            )
            Result.success(slip)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Could not decode Trade Slip. Check the pasted code."))
        }
    }

    suspend fun completeTradeWithSlip(
        slip: DecodedTradeSlip,
        offeredCard: OwnedCard
    ): Result<OwnedCard> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getCardById(offeredCard.instanceId)
                ?: return@withContext Result.failure(IllegalStateException("Offered card is no longer in Vault."))

            if (existing.isListedForTrade) {
                dao.deleteTradeListingsForCard(existing.instanceId)
            }
            dao.deleteOwnedCardById(existing.instanceId)

            val newEntity = OwnedCardEntity(
                catalogId = slip.catalogId,
                skinName = slip.skinName,
                heroName = slip.heroName,
                role = slip.role,
                rawTier = slip.rawTier,
                rarityName = slip.rarity.name,
                imageUrl = slip.imageUrl,
                isMisprint = slip.isMisprint,
                misprintAnomalyName = slip.misprintAnomaly?.name,
                serialCode = slip.serialCode,
                pullRatePercent = slip.rarity.basePullWeight,
                obtainedAtMs = System.currentTimeMillis(),
                isListedForTrade = false
            )
            val insertedId = dao.insertOwnedCard(newEntity)
            if (slip.goldSweetener > 0) {
                prefs.addGold(slip.goldSweetener, fromRecycle = false)
            }
            Result.success(newEntity.copy(instanceId = insertedId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAlias(alias: String) {
        prefs.updateCollectorAlias(alias)
    }

    private fun randomHex4(): String =
        Integer.toHexString(Random.nextInt(0x1000, 0xFFFF)).uppercase(Locale.US)
}
