package com.example.data.repository

import android.util.Base64
import com.example.data.local.CollectorDao
import com.example.data.local.OwnedCardEntity
import com.example.data.local.PlayerPreferencesStore
import com.example.data.local.PlayerWalletState
import com.example.data.local.SkinCatalogEntity
import com.example.data.local.TradeListingEntity
import com.example.data.remote.MlbbDexClient
import com.example.data.remote.ParsedHeroRef
import com.example.data.remote.SkinJsonParser
import com.example.model.DailyRewardTier
import com.example.model.DecodedTradeSlip
import com.example.model.DiamondExchangeOption
import com.example.model.MegaPackConfig
import com.example.model.MisprintAnomaly
import com.example.model.OwnedCard
import com.example.model.P2PTradeListing
import com.example.model.PackBundleOption
import com.example.model.QuestCatalog
import com.example.model.QuestCategory
import com.example.model.QuestDefinition
import com.example.model.QuestUiItem
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.TradeListing
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
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
        DailyRewardTier(1, coinAmount = 50, bonusDiamonds = 2),
        DailyRewardTier(2, coinAmount = 75, bonusDiamonds = 2),
        DailyRewardTier(3, coinAmount = 100, bonusFreePacks = 1),
        DailyRewardTier(4, coinAmount = 125, bonusDiamonds = 4),
        DailyRewardTier(5, coinAmount = 150, bonusDiamonds = 4),
        DailyRewardTier(6, coinAmount = 200, bonusFreePacks = 1),
        DailyRewardTier(7, coinAmount = 350, bonusFreePacks = 2, bonusDiamonds = 8)
    )

    suspend fun syncQuestPeriodsOnLaunch() {
        prefs.syncQuestPeriodsIfNeeded(currentLocalEpochDay(), currentLocalWeekKey())
    }

    suspend fun syncSkinsFromApi(
        onProgress: (syncedCount: Int, stageLabel: String) -> Unit = { _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val collectedById = LinkedHashMap<String, SkinCatalogItem>(1300)

            onProgress(0, "Connecting to mlbbdex.com/api/v1/skins/...")
            // 1) Fetch newest 100 skins (high-res illustration URLs + series)
            val topRaw = runCatching {
                MlbbDexClient.api.getSkinsPage(limit = 100, page = 1).string()
            }.getOrNull()
            if (topRaw != null) {
                SkinJsonParser.parseSkinsPage(topRaw).items.forEach { item ->
                    collectedById[item.id] = item
                }
            }

            // 2) Fetch all hero slugs (mlbbdex returns all ~133 heroes cleanly in 1 request)
            onProgress(collectedById.size, "Loading full MLBB hero roster...")
            val allHeroes = LinkedHashMap<String, ParsedHeroRef>()
            for (heroPage in 1..3) {
                val rawHeroes = runCatching {
                    MlbbDexClient.api.getHeroesPage(limit = 200, page = heroPage).string()
                }.getOrNull() ?: break
                val refs = SkinJsonParser.parseHeroRefs(rawHeroes)
                if (refs.isEmpty()) break
                var added = 0
                for (ref in refs) {
                    if (allHeroes.put(ref.slug, ref) == null) added++
                }
                if (added == 0 || refs.size < 150) break
            }

            // 3) Fetch every hero's full skin list + illustrations/images.skins with retries so zero heroes drop
            if (allHeroes.isNotEmpty()) {
                val semaphore = Semaphore(4)
                val heroList = allHeroes.values.toList()
                val chunks = heroList.chunked(16)
                var processedHeroes = 0

                for (chunk in chunks) {
                    val batchSkins = coroutineScope {
                        chunk.map { hero ->
                            async {
                                semaphore.withPermit {
                                    var parsed: List<SkinCatalogItem> = emptyList()
                                    for (attempt in 0 until 3) {
                                        if (attempt > 0) delay(300L * attempt)
                                        val rawDetail = runCatching {
                                            MlbbDexClient.api.getHeroDetail(hero.slug).string()
                                        }.getOrNull()
                                        if (rawDetail != null) {
                                            val candidate = SkinJsonParser.parseHeroDetailSkins(
                                                rawJson = rawDetail,
                                                fallbackHeroName = hero.name,
                                                fallbackRole = hero.role
                                            )
                                            if (candidate.isNotEmpty()) {
                                                parsed = candidate
                                                break
                                            }
                                        }
                                    }
                                    parsed
                                }
                            }
                        }.awaitAll().flatten()
                    }
                    processedHeroes += chunk.size

                    for (skin in batchSkins) {
                        val existing = collectedById[skin.id]
                        if (existing == null) {
                            collectedById[skin.id] = skin
                        } else {
                            // Merge best available artwork URL & proper hero capitalization
                            val bestImage = skin.imageUrl.ifBlank { existing.imageUrl }
                            val bestRarity = if (skin.rawTier != "Common") skin.rarity else existing.rarity
                            val bestTier = if (skin.rawTier != "Common") skin.rawTier else existing.rawTier
                            collectedById[skin.id] = skin.copy(
                                imageUrl = bestImage,
                                rarity = bestRarity,
                                rawTier = bestTier
                            )
                        }
                    }

                    onProgress(
                        collectedById.size,
                        "Importing ${collectedById.size} skins ($processedHeroes/${heroList.size} heroes)..."
                    )
                }
            }

            if (collectedById.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("MLBBDex returned an empty skin catalog.")
                )
            }

            // Preserve any previously cached valid image URLs if a skin arrived without an imageUrl on retry
            val existingById = dao.getAllCatalogOnce().associateBy { it.id }
            val mergedEntities = collectedById.values.map { incoming ->
                val prevUrl = existingById[incoming.id]?.imageUrl.orEmpty()
                val finalItem = if (incoming.imageUrl.isBlank() && prevUrl.isNotBlank()) {
                    incoming.copy(imageUrl = prevUrl)
                } else {
                    incoming
                }
                SkinCatalogEntity.fromDomain(finalItem)
            }

            dao.upsertCatalog(mergedEntities)
            dao.backfillMissingOwnedImages()

            prefs.markCatalogSynced(
                timestampMs = System.currentTimeMillis(),
                epochDay = currentLocalEpochDay(),
                schemaVersion = PlayerPreferencesStore.CURRENT_CATALOG_SCHEMA_VERSION
            )
            Result.success(collectedById.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPackReadyToRip(
        bundle: PackBundleOption,
        wallet: PlayerWalletState,
        payWithDiamonds: Boolean,
        useFreeTicket: Boolean
    ): Result<PackBundleOption> = withContext(Dispatchers.IO) {
        val count = dao.getCatalogCount()
        if (count <= 0) {
            return@withContext Result.failure(
                IllegalStateException("Skin catalog is still syncing. Check progress in Profile.")
            )
        }
        val effectiveBundle = if (useFreeTicket) PackBundleOption.SINGLE else bundle
        when {
            useFreeTicket && wallet.bonusFreePacks <= 0 -> {
                Result.failure(IllegalStateException("No free pack ticket available."))
            }
            !useFreeTicket && payWithDiamonds && wallet.diamonds < effectiveBundle.diamondCost -> {
                Result.failure(
                    IllegalStateException("Not enough Diamonds. Need ${effectiveBundle.diamondCost} 💎.")
                )
            }
            !useFreeTicket && !payWithDiamonds && wallet.coins < effectiveBundle.coinCost -> {
                Result.failure(
                    IllegalStateException("Not enough Coins. Need ${effectiveBundle.coinCost} 🪙.")
                )
            }
            else -> Result.success(effectiveBundle)
        }
    }

    suspend fun openPackBundle(
        bundle: PackBundleOption,
        wallet: PlayerWalletState,
        payWithDiamonds: Boolean,
        useFreeTicket: Boolean
    ): Result<List<OwnedCard>> = withContext(Dispatchers.IO) {
        try {
            val catalog = dao.getAllCatalogOnce().map { it.toDomain() }
            if (catalog.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Skin catalog not synced yet. Refresh in Profile.")
                )
            }

            val effectiveBundle = if (useFreeTicket) PackBundleOption.SINGLE else bundle

            when {
                useFreeTicket -> {
                    if (wallet.bonusFreePacks <= 0) {
                        return@withContext Result.failure(IllegalStateException("No free pack ticket available."))
                    }
                }
                payWithDiamonds -> {
                    if (wallet.diamonds < effectiveBundle.diamondCost) {
                        return@withContext Result.failure(
                            IllegalStateException("Not enough Diamonds. Need ${effectiveBundle.diamondCost} 💎.")
                        )
                    }
                }
                else -> {
                    if (wallet.coins < effectiveBundle.coinCost) {
                        return@withContext Result.failure(
                            IllegalStateException("Not enough Coins. Need ${effectiveBundle.coinCost} 🪙.")
                        )
                    }
                }
            }

            val groupedByRarity = catalog.groupBy { it.rarity }
            val nowMs = System.currentTimeMillis()
            val pulledEntities = mutableListOf<OwnedCardEntity>()
            val totalCards = effectiveBundle.totalCards
            val weights = MegaPackConfig.RARITY_WEIGHTS
            val totalWeight = weights.values.sum().coerceAtLeast(1.0)

            var epicPlusCount = 0
            var luckyboxPlusCount = 0
            var mythicOrMisprintCount = 0

            for (slotIndex in 0 until totalCards) {
                val rolledRarity = rollRarity(weights)
                val chosenSkin = pickSkinForRarity(rolledRarity, groupedByRarity, catalog)
                val actualRarity = chosenSkin.rarity

                val actualWeight = weights[actualRarity] ?: actualRarity.basePullWeight
                val pullRatePct = (actualWeight / totalWeight) * 100.0
                val roundedRatePct = ((pullRatePct * 1000.0).roundToInt() / 1000.0).coerceAtLeast(0.001)

                val isMisprint = Random.nextDouble(100.0) < MegaPackConfig.MISPRINT_CHANCE_PERCENT
                val anomaly = if (isMisprint) MisprintAnomaly.entries.random() else null
                val serialCode = if (isMisprint) "1/1 • MP-${randomHex4()}" else "STD-${randomHex4()}"

                if (actualRarity.tierRank >= SkinRarity.EPIC.tierRank) epicPlusCount++
                if (actualRarity.tierRank >= SkinRarity.LUCKYBOX.tierRank) luckyboxPlusCount++
                if (isMisprint || actualRarity.tierRank >= SkinRarity.COLLECTOR.tierRank) mythicOrMisprintCount++

                pulledEntities += OwnedCardEntity(
                    catalogId = chosenSkin.id,
                    skinName = chosenSkin.skinName,
                    heroName = chosenSkin.heroName,
                    role = chosenSkin.role,
                    rawTier = chosenSkin.rawTier,
                    rarityName = actualRarity.name,
                    imageUrl = chosenSkin.imageUrl,
                    isMisprint = isMisprint,
                    misprintAnomalyName = anomaly?.name,
                    serialCode = serialCode,
                    pullRatePercent = roundedRatePct,
                    obtainedAtMs = nowMs + slotIndex,
                    isListedForTrade = false
                )
            }

            val sortedEntities = pulledEntities.sortedWith(
                compareBy<OwnedCardEntity> { if (it.isMisprint) 1 else 0 }
                    .thenBy { SkinRarity.fromNameSafe(it.rarityName).tierRank }
            )

            val insertedIds = dao.insertOwnedCards(sortedEntities)
            prefs.spendForPackBundleAndRecordProgress(
                packCount = effectiveBundle.packCount,
                coinCost = if (!useFreeTicket && !payWithDiamonds) effectiveBundle.coinCost else 0,
                diamondCost = if (!useFreeTicket && payWithDiamonds) effectiveBundle.diamondCost else 0,
                useFreeTicket = useFreeTicket,
                epicPlusPulled = epicPlusCount,
                luckyboxPlusPulled = luckyboxPlusCount,
                mythicOrMisprintPulled = mythicOrMisprintCount,
                todayEpochDay = currentLocalEpochDay(),
                currentWeekKey = currentLocalWeekKey()
            )

            val resultCards = sortedEntities.mapIndexed { idx, entity ->
                entity.copy(instanceId = insertedIds[idx]).toDomain()
            }
            Result.success(resultCards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun rollRarity(weights: Map<SkinRarity, Double>): SkinRarity {
        val eligible = weights.filter { it.value > 0.0 }
        val total = eligible.values.sum()
        if (total <= 0.0) return SkinRarity.COMMON

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

        val nearestAny = grouped.entries
            .filter { it.value.isNotEmpty() }
            .minByOrNull { kotlin.math.abs(it.key.tierRank - target.tierRank) }
        return nearestAny?.value?.random() ?: fallbackAll.random()
    }

    fun currentLocalEpochDay(): Long {
        val cal = Calendar.getInstance()
        return (cal.get(Calendar.YEAR).toLong() * 366L) + cal.get(Calendar.DAY_OF_YEAR).toLong()
    }

    fun currentLocalWeekKey(): Long {
        val cal = Calendar.getInstance()
        return (cal.get(Calendar.YEAR).toLong() * 54L) + cal.get(Calendar.WEEK_OF_YEAR).toLong()
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
                    coinReward = tier.coinAmount,
                    bonusFreePacks = tier.bonusFreePacks,
                    bonusDiamonds = tier.bonusDiamonds
                )
                Result.success(tier)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun exchangeDiamondsForCoins(option: DiamondExchangeOption): Result<DiamondExchangeOption> =
        withContext(Dispatchers.IO) {
            val ok = prefs.exchangeDiamondsForCoins(option.diamondCost, option.coinReward)
            if (ok) Result.success(option)
            else Result.failure(IllegalStateException("Need ${option.diamondCost} 💎 Diamonds."))
        }

    suspend fun consumeScratcherChargeAndAwardCoins(coinPrize: Int): Result<Int> =
        withContext(Dispatchers.IO) {
            val ok = prefs.consumeScratcherChargeAndAwardCoins(
                coinReward = coinPrize,
                nowMs = System.currentTimeMillis(),
                todayEpochDay = currentLocalEpochDay(),
                currentWeekKey = currentLocalWeekKey()
            )
            if (ok) Result.success(coinPrize)
            else Result.failure(IllegalStateException("Scratcher recharging! +1 ticket refills every hour (max 3)."))
        }

    suspend fun consumePrismChargeAndAwardDiamonds(diamondsEarned: Int): Result<Int> =
        withContext(Dispatchers.IO) {
            val ok = prefs.consumePrismChargeAndAwardDiamonds(
                diamondReward = diamondsEarned,
                nowMs = System.currentTimeMillis(),
                todayEpochDay = currentLocalEpochDay(),
                currentWeekKey = currentLocalWeekKey()
            )
            if (ok) Result.success(diamondsEarned)
            else Result.failure(IllegalStateException("Prism Strike recharging! +1 charge refills every hour (max 3)."))
        }

    suspend fun completeHourlyQuizAndAwardDiamonds(diamondsEarned: Int): Result<Int> =
        withContext(Dispatchers.IO) {
            val ok = prefs.completeHourlyQuizAndAwardDiamonds(
                diamondReward = diamondsEarned,
                nowMs = System.currentTimeMillis(),
                todayEpochDay = currentLocalEpochDay(),
                currentWeekKey = currentLocalWeekKey()
            )
            if (ok) Result.success(diamondsEarned)
            else Result.failure(IllegalStateException("Quiz is on 1-hour cooldown."))
        }

    suspend fun consumeWheelSpinAndAward(
        coinReward: Int,
        diamondReward: Int,
        freePackReward: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val ok = prefs.consumeWheelSpinAndAward(
            coinReward = coinReward,
            diamondReward = diamondReward,
            freePackReward = freePackReward,
            nowMs = System.currentTimeMillis(),
            todayEpochDay = currentLocalEpochDay(),
            currentWeekKey = currentLocalWeekKey()
        )
        if (ok) Result.success(Unit)
        else Result.failure(IllegalStateException("Prize Wheel recharging! +1 spin every hour (max 3)."))
    }

    suspend fun togglePinnedBadge(badgeId: String): Boolean =
        prefs.togglePinnedBadge(badgeId)

    fun buildQuestItems(wallet: PlayerWalletState): List<QuestUiItem> {
        val today = currentLocalEpochDay()
        val week = currentLocalWeekKey()
        val dailyStale = wallet.questDailyEpochDay < today
        val weeklyStale = wallet.questWeeklyKey < week

        val allDefs = QuestCatalog.dailyQuests + QuestCatalog.weeklyQuests + QuestCatalog.communityQuests
        return allDefs.map { def ->
            val progress = when (def.id) {
                "d_open_1", "d_open_5" -> if (dailyStale) 0 else wallet.dailyPacksOpened
                "d_rare_epic" -> if (dailyStale) 0 else wallet.dailyEpicPlusCollected
                "d_scratch_2" -> if (dailyStale) 0 else wallet.dailyScratchersDone
                "d_minigame_2" -> if (dailyStale) 0 else wallet.dailyMinigamesWon

                "w_open_15" -> if (weeklyStale) 0 else wallet.weeklyPacksOpened
                "w_luckybox_3" -> if (weeklyStale) 0 else wallet.weeklyLuckyboxPlusCollected
                "w_recycle_5" -> if (weeklyStale) 0 else wallet.weeklyCardsRecycled
                "w_minigame_6" -> if (weeklyStale) 0 else wallet.weeklyMinigamesWon

                "c_packs_25" -> wallet.communityPacksOpened
                "c_epic_10" -> wallet.communityEpicPlusCollected
                "c_mythic_1" -> wallet.communityMythicCollected
                "c_trade_1" -> wallet.communityTradesDone
                else -> 0
            }
            val claimed = when (def.category) {
                QuestCategory.DAILY -> !dailyStale && def.id in wallet.claimedDailyQuestIds
                QuestCategory.WEEKLY -> !weeklyStale && def.id in wallet.claimedWeeklyQuestIds
                QuestCategory.COMMUNITY -> def.id in wallet.claimedCommunityQuestIds
            }
            QuestUiItem(definition = def, currentProgress = progress, isClaimed = claimed)
        }
    }

    suspend fun claimQuest(quest: QuestUiItem): Result<QuestDefinition> = withContext(Dispatchers.IO) {
        if (!quest.canClaim) {
            return@withContext Result.failure(IllegalStateException("Quest not ready to claim."))
        }
        prefs.claimQuestReward(
            questId = quest.definition.id,
            categoryName = quest.definition.category.name,
            coinReward = quest.definition.coinReward,
            diamondReward = quest.definition.diamondReward,
            freePackReward = quest.definition.freePackReward,
            todayEpochDay = currentLocalEpochDay(),
            currentWeekKey = currentLocalWeekKey()
        )
        Result.success(quest.definition)
    }

    suspend fun recycleSingleCard(card: OwnedCard): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getCardById(card.instanceId)
                ?: return@withContext Result.failure(IllegalStateException("Card no longer in Vault."))
            if (existing.isListedForTrade) {
                dao.deleteTradeListingsForCard(existing.instanceId)
            }
            dao.deleteOwnedCardById(existing.instanceId)
            val coinEarned = card.recycleValueGold
            prefs.addCoins(
                amount = coinEarned,
                fromRecycleCount = 1,
                todayEpochDay = currentLocalEpochDay(),
                currentWeekKey = currentLocalWeekKey()
            )
            Result.success(coinEarned)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recycleAllDuplicates(): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val allOwned = dao.getAllOwnedOnce().map { it.toDomain() }
            val byCatalog = allOwned.groupBy { it.catalogId }
            val idsToDelete = mutableListOf<Long>()
            var totalCoins = 0

            for ((_, group) in byCatalog) {
                if (group.size <= 1) continue
                val sorted = group.sortedWith(
                    compareByDescending<OwnedCard> { it.isMisprint }
                        .thenByDescending { it.isListedForTrade }
                        .thenBy { it.obtainedAtMs }
                )
                val extras = sorted.drop(1).filter { !it.isMisprint && !it.isListedForTrade }
                for (extra in extras) {
                    idsToDelete += extra.instanceId
                    totalCoins += extra.recycleValueGold
                }
            }

            if (idsToDelete.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No un-listed non-misprint duplicate cards available to recycle.")
                )
            }

            dao.deleteOwnedCardsByIds(idsToDelete)
            prefs.addCoins(
                amount = totalCoins,
                fromRecycleCount = idsToDelete.size,
                todayEpochDay = currentLocalEpochDay(),
                currentWeekKey = currentLocalWeekKey()
            )
            Result.success(idsToDelete.size to totalCoins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTradeListing(
        card: OwnedCard,
        seekingNote: String,
        senderAlias: String,
        goldSweetener: Int = 0,
        senderPrestige: Int = 0,
        senderCardCount: Int = 0,
        senderBadgeIds: List<String> = emptyList()
    ): Result<TradeListing> = withContext(Dispatchers.IO) {
        try {
            val slipJson = JSONObject().apply {
                put("v", 1)
                put("slipId", "TS-${randomHex4()}-${randomHex4()}")
                put("sender", senderAlias.ifBlank { "Collector" })
                put("prestige", senderPrestige)
                put("cards", senderCardCount)
                put("badges", JSONArray(senderBadgeIds.take(3)))
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
            prefs.recordTradeQuestProgress()
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
            val badgeArray = obj.optJSONArray("badges")
            val parsedBadges = buildList {
                if (badgeArray != null) {
                    for (i in 0 until badgeArray.length()) {
                        val b = badgeArray.optString(i)
                        if (b.isNotBlank()) add(b)
                    }
                }
            }
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
                slipId = obj.optString("slipId", "TS-0000"),
                senderPrestige = obj.optInt("prestige", 1850),
                senderCardCount = obj.optInt("cards", 24),
                senderBadgeIds = parsedBadges
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
                prefs.addCoins(slip.goldSweetener)
            }
            prefs.recordTradeQuestProgress()
            Result.success(newEntity.copy(instanceId = insertedId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeDirectP2PSwap(
        listing: P2PTradeListing,
        offeredCard: OwnedCard
    ): Result<OwnedCard> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getCardById(offeredCard.instanceId)
                ?: return@withContext Result.failure(IllegalStateException("Offered card is no longer in Vault."))

            if (existing.isListedForTrade) {
                dao.deleteTradeListingsForCard(existing.instanceId)
            }
            dao.deleteOwnedCardById(existing.instanceId)

            val receivedEntity = OwnedCardEntity(
                catalogId = listing.catalogId,
                skinName = listing.skinName,
                heroName = listing.heroName,
                role = listing.role,
                rawTier = listing.rawTier,
                rarityName = listing.rarity.name,
                imageUrl = listing.imageUrl,
                isMisprint = listing.isMisprint,
                misprintAnomalyName = listing.misprintAnomaly?.name,
                serialCode = listing.serialCode,
                pullRatePercent = listing.rarity.basePullWeight,
                obtainedAtMs = System.currentTimeMillis(),
                isListedForTrade = false
            )
            val insertedId = dao.insertOwnedCard(receivedEntity)
            prefs.recordTradeQuestProgress()
            Result.success(receivedEntity.copy(instanceId = insertedId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun settleSoldP2PListing(listing: P2PTradeListing): Result<OwnedCard> = withContext(Dispatchers.IO) {
        try {
            val allOwned = dao.getAllOwnedOnce()
            val held = allOwned.firstOrNull {
                it.serialCode == listing.serialCode && it.catalogId == listing.catalogId
            }
            if (held != null) {
                if (held.isListedForTrade) {
                    dao.deleteTradeListingsForCard(held.instanceId)
                }
                dao.deleteOwnedCardById(held.instanceId)
            }

            val receivedEntity = OwnedCardEntity(
                catalogId = listing.acceptedCatalogId,
                skinName = listing.acceptedSkinName,
                heroName = listing.acceptedHeroName,
                role = listing.acceptedRole.ifBlank { "Hero" },
                rawTier = listing.acceptedRawTier.ifBlank { listing.acceptedRarity.displayName },
                rarityName = listing.acceptedRarity.name,
                imageUrl = listing.acceptedImageUrl,
                isMisprint = listing.acceptedIsMisprint,
                misprintAnomalyName = listing.acceptedMisprintAnomaly?.name,
                serialCode = listing.acceptedSerialCode.ifBlank { "P2P-${randomHex4()}" },
                pullRatePercent = listing.acceptedRarity.basePullWeight,
                obtainedAtMs = System.currentTimeMillis(),
                isListedForTrade = false
            )
            val insertedId = dao.insertOwnedCard(receivedEntity)
            prefs.recordTradeQuestProgress()
            Result.success(receivedEntity.copy(instanceId = insertedId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAlias(alias: String) {
        prefs.updateCollectorAlias(alias)
    }

    suspend fun seedAliasFromGoogleIfDefault(googleName: String) {
        prefs.seedAliasFromGoogleIfDefault(googleName)
    }

    suspend fun updateMlbbAccount(ign: String, serverId: String) {
        prefs.updateMlbbAccount(ign, serverId)
    }

    suspend fun setSoundMuted(muted: Boolean) {
        prefs.setSoundMuted(muted)
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        prefs.setDarkModeEnabled(enabled)
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        prefs.setVibrationEnabled(enabled)
    }

    suspend fun setThemeAccentId(accentId: String) {
        prefs.setThemeAccentId(accentId)
    }

    private fun randomHex4(): String =
        Integer.toHexString(Random.nextInt(0x1000, 0xFFFF)).uppercase(Locale.US)
}
