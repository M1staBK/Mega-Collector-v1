package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.collectorDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mega_collector_prefs"
)

data class PlayerWalletState(
    val coins: Int = 1200,
    val diamonds: Int = 25,
    val lastDailyClaimEpochDay: Long = -1L,
    val dailyStreakCount: Int = 0,
    val totalPacksOpened: Int = 0,
    val totalCoinsRecycled: Int = 0,
    val bonusFreePacks: Int = 1,
    val collectorAlias: String = "Mythic Ace",
    val catalogLastSyncedMs: Long = 0L,
    val catalogSchemaVersion: Int = 0,
    val soundMuted: Boolean = false,
    // Daily Scratchers
    val lastScratcherEpochDay: Long = -1L,
    val dailyScratchersUsedToday: Int = 0,
    // Quest Counters (Daily)
    val questDailyEpochDay: Long = -1L,
    val dailyPacksOpened: Int = 0,
    val dailyEpicPlusCollected: Int = 0,
    val dailyScratchersDone: Int = 0,
    val dailyMinigamesWon: Int = 0,
    val claimedDailyQuestIds: Set<String> = emptySet(),
    // Quest Counters (Weekly)
    val questWeeklyKey: Long = -1L,
    val weeklyPacksOpened: Int = 0,
    val weeklyLuckyboxPlusCollected: Int = 0,
    val weeklyCardsRecycled: Int = 0,
    val weeklyMinigamesWon: Int = 0,
    val claimedWeeklyQuestIds: Set<String> = emptySet(),
    // Quest Counters (Community / Season)
    val communityPacksOpened: Int = 0,
    val communityEpicPlusCollected: Int = 0,
    val communityMythicCollected: Int = 0,
    val communityTradesDone: Int = 0,
    val claimedCommunityQuestIds: Set<String> = emptySet()
) {
    val dailyScratchersRemaining: Int
        get() = (MAX_DAILY_SCRATCHERS - dailyScratchersUsedToday).coerceAtLeast(0)

    companion object {
        const val MAX_DAILY_SCRATCHERS = 3
    }
}

class PlayerPreferencesStore(private val context: Context) {
    private object Keys {
        val COINS = intPreferencesKey("arena_gold")
        val DIAMONDS = intPreferencesKey("player_diamonds")
        val LAST_DAILY_EPOCH_DAY = longPreferencesKey("last_daily_epoch_day")
        val DAILY_STREAK_COUNT = intPreferencesKey("daily_streak_count")
        val TOTAL_PACKS_OPENED = intPreferencesKey("total_packs_opened")
        val TOTAL_COINS_RECYCLED = intPreferencesKey("total_gold_recycled")
        val BONUS_FREE_PACKS = intPreferencesKey("bonus_starter_packs")
        val COLLECTOR_ALIAS = stringPreferencesKey("collector_alias")
        val CATALOG_LAST_SYNCED_MS = longPreferencesKey("catalog_last_synced_ms")
        val CATALOG_SCHEMA_VERSION = intPreferencesKey("catalog_schema_version")
        val SOUND_MUTED = booleanPreferencesKey("sound_muted")

        val LAST_SCRATCHER_EPOCH_DAY = longPreferencesKey("last_scratcher_epoch_day")
        val DAILY_SCRATCHERS_USED = intPreferencesKey("daily_scratchers_used")

        val QUEST_DAILY_EPOCH_DAY = longPreferencesKey("quest_daily_epoch_day")
        val DAILY_PACKS_OPENED = intPreferencesKey("daily_packs_opened")
        val DAILY_EPIC_PLUS = intPreferencesKey("daily_epic_plus")
        val DAILY_SCRATCHERS_DONE = intPreferencesKey("daily_scratchers_done")
        val DAILY_MINIGAMES_WON = intPreferencesKey("daily_minigames_won")
        val CLAIMED_DAILY_QUESTS = stringSetPreferencesKey("claimed_daily_quests")

        val QUEST_WEEKLY_KEY = longPreferencesKey("quest_weekly_key")
        val WEEKLY_PACKS_OPENED = intPreferencesKey("weekly_packs_opened")
        val WEEKLY_LUCKYBOX_PLUS = intPreferencesKey("weekly_luckybox_plus")
        val WEEKLY_CARDS_RECYCLED = intPreferencesKey("weekly_cards_recycled")
        val WEEKLY_MINIGAMES_WON = intPreferencesKey("weekly_minigames_won")
        val CLAIMED_WEEKLY_QUESTS = stringSetPreferencesKey("claimed_weekly_quests")

        val COMMUNITY_PACKS_OPENED = intPreferencesKey("community_packs_opened")
        val COMMUNITY_EPIC_PLUS = intPreferencesKey("community_epic_plus")
        val COMMUNITY_MYTHIC = intPreferencesKey("community_mythic")
        val COMMUNITY_TRADES = intPreferencesKey("community_trades")
        val CLAIMED_COMMUNITY_QUESTS = stringSetPreferencesKey("claimed_community_quests")
    }

    val walletFlow: Flow<PlayerWalletState> = context.collectorDataStore.data.map { prefs ->
        PlayerWalletState(
            coins = prefs[Keys.COINS] ?: 1200,
            diamonds = prefs[Keys.DIAMONDS] ?: 25,
            lastDailyClaimEpochDay = prefs[Keys.LAST_DAILY_EPOCH_DAY] ?: -1L,
            dailyStreakCount = prefs[Keys.DAILY_STREAK_COUNT] ?: 0,
            totalPacksOpened = prefs[Keys.TOTAL_PACKS_OPENED] ?: 0,
            totalCoinsRecycled = prefs[Keys.TOTAL_COINS_RECYCLED] ?: 0,
            bonusFreePacks = prefs[Keys.BONUS_FREE_PACKS] ?: 1,
            collectorAlias = prefs[Keys.COLLECTOR_ALIAS] ?: "Mythic Ace",
            catalogLastSyncedMs = prefs[Keys.CATALOG_LAST_SYNCED_MS] ?: 0L,
            catalogSchemaVersion = prefs[Keys.CATALOG_SCHEMA_VERSION] ?: 0,
            soundMuted = prefs[Keys.SOUND_MUTED] ?: false,
            lastScratcherEpochDay = prefs[Keys.LAST_SCRATCHER_EPOCH_DAY] ?: -1L,
            dailyScratchersUsedToday = prefs[Keys.DAILY_SCRATCHERS_USED] ?: 0,
            questDailyEpochDay = prefs[Keys.QUEST_DAILY_EPOCH_DAY] ?: -1L,
            dailyPacksOpened = prefs[Keys.DAILY_PACKS_OPENED] ?: 0,
            dailyEpicPlusCollected = prefs[Keys.DAILY_EPIC_PLUS] ?: 0,
            dailyScratchersDone = prefs[Keys.DAILY_SCRATCHERS_DONE] ?: 0,
            dailyMinigamesWon = prefs[Keys.DAILY_MINIGAMES_WON] ?: 0,
            claimedDailyQuestIds = prefs[Keys.CLAIMED_DAILY_QUESTS] ?: emptySet(),
            questWeeklyKey = prefs[Keys.QUEST_WEEKLY_KEY] ?: -1L,
            weeklyPacksOpened = prefs[Keys.WEEKLY_PACKS_OPENED] ?: 0,
            weeklyLuckyboxPlusCollected = prefs[Keys.WEEKLY_LUCKYBOX_PLUS] ?: 0,
            weeklyCardsRecycled = prefs[Keys.WEEKLY_CARDS_RECYCLED] ?: 0,
            weeklyMinigamesWon = prefs[Keys.WEEKLY_MINIGAMES_WON] ?: 0,
            claimedWeeklyQuestIds = prefs[Keys.CLAIMED_WEEKLY_QUESTS] ?: emptySet(),
            communityPacksOpened = prefs[Keys.COMMUNITY_PACKS_OPENED] ?: 0,
            communityEpicPlusCollected = prefs[Keys.COMMUNITY_EPIC_PLUS] ?: 0,
            communityMythicCollected = prefs[Keys.COMMUNITY_MYTHIC] ?: 0,
            communityTradesDone = prefs[Keys.COMMUNITY_TRADES] ?: 0,
            claimedCommunityQuestIds = prefs[Keys.CLAIMED_COMMUNITY_QUESTS] ?: emptySet()
        )
    }

    suspend fun spendForPackBundleAndRecordProgress(
        packCount: Int,
        coinCost: Int,
        diamondCost: Int,
        useFreeTicket: Boolean,
        epicPlusPulled: Int,
        luckyboxPlusPulled: Int,
        mythicOrMisprintPulled: Int,
        todayEpochDay: Long,
        currentWeekKey: Long
    ) {
        context.collectorDataStore.edit { prefs ->
            when {
                useFreeTicket -> {
                    val cur = prefs[Keys.BONUS_FREE_PACKS] ?: 1
                    prefs[Keys.BONUS_FREE_PACKS] = (cur - 1).coerceAtLeast(0)
                }
                diamondCost > 0 -> {
                    val curD = prefs[Keys.DIAMONDS] ?: 25
                    prefs[Keys.DIAMONDS] = (curD - diamondCost).coerceAtLeast(0)
                }
                else -> {
                    val curC = prefs[Keys.COINS] ?: 1200
                    prefs[Keys.COINS] = (curC - coinCost).coerceAtLeast(0)
                }
            }
            prefs[Keys.TOTAL_PACKS_OPENED] = (prefs[Keys.TOTAL_PACKS_OPENED] ?: 0) + packCount

            resetDailyIfNeeded(prefs, todayEpochDay)
            resetWeeklyIfNeeded(prefs, currentWeekKey)

            prefs[Keys.DAILY_PACKS_OPENED] = (prefs[Keys.DAILY_PACKS_OPENED] ?: 0) + packCount
            prefs[Keys.DAILY_EPIC_PLUS] = (prefs[Keys.DAILY_EPIC_PLUS] ?: 0) + epicPlusPulled

            prefs[Keys.WEEKLY_PACKS_OPENED] = (prefs[Keys.WEEKLY_PACKS_OPENED] ?: 0) + packCount
            prefs[Keys.WEEKLY_LUCKYBOX_PLUS] = (prefs[Keys.WEEKLY_LUCKYBOX_PLUS] ?: 0) + luckyboxPlusPulled

            prefs[Keys.COMMUNITY_PACKS_OPENED] = (prefs[Keys.COMMUNITY_PACKS_OPENED] ?: 0) + packCount
            prefs[Keys.COMMUNITY_EPIC_PLUS] = (prefs[Keys.COMMUNITY_EPIC_PLUS] ?: 0) + epicPlusPulled
            prefs[Keys.COMMUNITY_MYTHIC] = (prefs[Keys.COMMUNITY_MYTHIC] ?: 0) + mythicOrMisprintPulled
        }
    }

    suspend fun addCoins(amount: Int, fromRecycleCount: Int = 0, todayEpochDay: Long? = null, currentWeekKey: Long? = null) {
        context.collectorDataStore.edit { prefs ->
            val cur = prefs[Keys.COINS] ?: 1200
            prefs[Keys.COINS] = cur + amount
            if (fromRecycleCount > 0) {
                prefs[Keys.TOTAL_COINS_RECYCLED] = (prefs[Keys.TOTAL_COINS_RECYCLED] ?: 0) + amount
                if (todayEpochDay != null && currentWeekKey != null) {
                    resetDailyIfNeeded(prefs, todayEpochDay)
                    resetWeeklyIfNeeded(prefs, currentWeekKey)
                    prefs[Keys.WEEKLY_CARDS_RECYCLED] = (prefs[Keys.WEEKLY_CARDS_RECYCLED] ?: 0) + fromRecycleCount
                }
            }
        }
    }

    suspend fun addDiamonds(amount: Int, fromMinigameWin: Boolean = false, todayEpochDay: Long? = null, currentWeekKey: Long? = null) {
        context.collectorDataStore.edit { prefs ->
            val cur = prefs[Keys.DIAMONDS] ?: 25
            prefs[Keys.DIAMONDS] = cur + amount
            if (fromMinigameWin && todayEpochDay != null && currentWeekKey != null) {
                resetDailyIfNeeded(prefs, todayEpochDay)
                resetWeeklyIfNeeded(prefs, currentWeekKey)
                prefs[Keys.DAILY_MINIGAMES_WON] = (prefs[Keys.DAILY_MINIGAMES_WON] ?: 0) + 1
                prefs[Keys.WEEKLY_MINIGAMES_WON] = (prefs[Keys.WEEKLY_MINIGAMES_WON] ?: 0) + 1
            }
        }
    }

    suspend fun exchangeDiamondsForCoins(diamondCost: Int, coinGain: Int): Boolean {
        var success = false
        context.collectorDataStore.edit { prefs ->
            val curD = prefs[Keys.DIAMONDS] ?: 25
            if (curD >= diamondCost) {
                prefs[Keys.DIAMONDS] = curD - diamondCost
                prefs[Keys.COINS] = (prefs[Keys.COINS] ?: 1200) + coinGain
                success = true
            }
        }
        return success
    }

    suspend fun recordScratcherReward(
        coinReward: Int,
        todayEpochDay: Long,
        currentWeekKey: Long
    ): Boolean {
        var recorded = false
        context.collectorDataStore.edit { prefs ->
            val storedDay = prefs[Keys.LAST_SCRATCHER_EPOCH_DAY] ?: -1L
            val used = if (storedDay < todayEpochDay) 0 else (prefs[Keys.DAILY_SCRATCHERS_USED] ?: 0)
            if (used < PlayerWalletState.MAX_DAILY_SCRATCHERS) {
                prefs[Keys.LAST_SCRATCHER_EPOCH_DAY] = todayEpochDay
                prefs[Keys.DAILY_SCRATCHERS_USED] = used + 1
                prefs[Keys.COINS] = (prefs[Keys.COINS] ?: 1200) + coinReward

                resetDailyIfNeeded(prefs, todayEpochDay)
                resetWeeklyIfNeeded(prefs, currentWeekKey)
                prefs[Keys.DAILY_SCRATCHERS_DONE] = (prefs[Keys.DAILY_SCRATCHERS_DONE] ?: 0) + 1
                recorded = true
            }
        }
        return recorded
    }

    suspend fun recordTradeQuestProgress() {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.COMMUNITY_TRADES] = (prefs[Keys.COMMUNITY_TRADES] ?: 0) + 1
        }
    }

    suspend fun syncQuestPeriodsIfNeeded(todayEpochDay: Long, currentWeekKey: Long) {
        context.collectorDataStore.edit { prefs ->
            if ((prefs[Keys.LAST_SCRATCHER_EPOCH_DAY] ?: -1L) < todayEpochDay) {
                prefs[Keys.LAST_SCRATCHER_EPOCH_DAY] = todayEpochDay
                prefs[Keys.DAILY_SCRATCHERS_USED] = 0
            }
            resetDailyIfNeeded(prefs, todayEpochDay)
            resetWeeklyIfNeeded(prefs, currentWeekKey)
        }
    }

    suspend fun claimQuestReward(
        questId: String,
        categoryName: String,
        coinReward: Int,
        diamondReward: Int,
        freePackReward: Int,
        todayEpochDay: Long,
        currentWeekKey: Long
    ) {
        context.collectorDataStore.edit { prefs ->
            resetDailyIfNeeded(prefs, todayEpochDay)
            resetWeeklyIfNeeded(prefs, currentWeekKey)
            val key = when (categoryName) {
                "DAILY" -> Keys.CLAIMED_DAILY_QUESTS
                "WEEKLY" -> Keys.CLAIMED_WEEKLY_QUESTS
                else -> Keys.CLAIMED_COMMUNITY_QUESTS
            }
            val currentSet = prefs[key] ?: emptySet()
            if (questId !in currentSet) {
                prefs[key] = currentSet + questId
                if (coinReward > 0) {
                    prefs[Keys.COINS] = (prefs[Keys.COINS] ?: 1200) + coinReward
                }
                if (diamondReward > 0) {
                    prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: 25) + diamondReward
                }
                if (freePackReward > 0) {
                    prefs[Keys.BONUS_FREE_PACKS] = (prefs[Keys.BONUS_FREE_PACKS] ?: 1) + freePackReward
                }
            }
        }
    }

    suspend fun recordDailyRewardClaim(
        epochDay: Long,
        newStreak: Int,
        coinReward: Int,
        bonusFreePacks: Int,
        bonusDiamonds: Int
    ) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.COINS] = (prefs[Keys.COINS] ?: 1200) + coinReward
            if (bonusDiamonds > 0) {
                prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: 25) + bonusDiamonds
            }
            if (bonusFreePacks > 0) {
                prefs[Keys.BONUS_FREE_PACKS] = (prefs[Keys.BONUS_FREE_PACKS] ?: 1) + bonusFreePacks
            }
            prefs[Keys.LAST_DAILY_EPOCH_DAY] = epochDay
            prefs[Keys.DAILY_STREAK_COUNT] = newStreak
        }
    }

    suspend fun updateCollectorAlias(alias: String) {
        val cleaned = alias.trim().take(24)
        if (cleaned.isNotEmpty()) {
            context.collectorDataStore.edit { prefs ->
                prefs[Keys.COLLECTOR_ALIAS] = cleaned
            }
        }
    }

    suspend fun markCatalogSynced(timestampMs: Long, schemaVersion: Int) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.CATALOG_LAST_SYNCED_MS] = timestampMs
            prefs[Keys.CATALOG_SCHEMA_VERSION] = schemaVersion
        }
    }

    suspend fun setSoundMuted(muted: Boolean) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.SOUND_MUTED] = muted
        }
    }

    private fun resetDailyIfNeeded(prefs: androidx.datastore.preferences.core.MutablePreferences, todayEpochDay: Long) {
        val stored = prefs[Keys.QUEST_DAILY_EPOCH_DAY] ?: -1L
        if (stored < todayEpochDay) {
            prefs[Keys.QUEST_DAILY_EPOCH_DAY] = todayEpochDay
            prefs[Keys.DAILY_PACKS_OPENED] = 0
            prefs[Keys.DAILY_EPIC_PLUS] = 0
            prefs[Keys.DAILY_SCRATCHERS_DONE] = 0
            prefs[Keys.DAILY_MINIGAMES_WON] = 0
            prefs[Keys.CLAIMED_DAILY_QUESTS] = emptySet()
        }
    }

    private fun resetWeeklyIfNeeded(prefs: androidx.datastore.preferences.core.MutablePreferences, currentWeekKey: Long) {
        val stored = prefs[Keys.QUEST_WEEKLY_KEY] ?: -1L
        if (stored < currentWeekKey) {
            prefs[Keys.QUEST_WEEKLY_KEY] = currentWeekKey
            prefs[Keys.WEEKLY_PACKS_OPENED] = 0
            prefs[Keys.WEEKLY_LUCKYBOX_PLUS] = 0
            prefs[Keys.WEEKLY_CARDS_RECYCLED] = 0
            prefs[Keys.WEEKLY_MINIGAMES_WON] = 0
            prefs[Keys.CLAIMED_WEEKLY_QUESTS] = emptySet()
        }
    }

    companion object {
        const val CURRENT_CATALOG_SCHEMA_VERSION = 2
    }
}
