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
    val coins: Int = DEFAULT_STARTER_COINS,
    val diamonds: Int = DEFAULT_STARTER_DIAMONDS,
    val lastDailyClaimEpochDay: Long = -1L,
    val dailyStreakCount: Int = 0,
    val totalPacksOpened: Int = 0,
    val totalCoinsRecycled: Int = 0,
    val bonusFreePacks: Int = 1,
    val collectorAlias: String = "Mythic Ace",
    val catalogLastSyncedMs: Long = 0L,
    val catalogSyncedEpochDay: Long = -1L,
    val catalogSchemaVersion: Int = 0,
    val soundMuted: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    // Hourly-recharging Charges (1 charge per 1 hour, stacks up to 3)
    val scratcherStoredCharges: Int = MAX_HOURLY_CHARGES,
    val scratcherLastRefillMs: Long = 0L,
    val prismStoredCharges: Int = MAX_HOURLY_CHARGES,
    val prismLastRefillMs: Long = 0L,
    val wheelStoredCharges: Int = MAX_HOURLY_CHARGES,
    val wheelLastRefillMs: Long = 0L,
    // 5-Question Hourly Quiz Cooldown (every 1 hour)
    val quizLastCompletedMs: Long = 0L,
    // Lifetime Stats & Pinned Achievement Badges
    val pinnedBadgeIds: Set<String> = emptySet(),
    val totalWheelSpins: Int = 0,
    val totalQuizzesCompleted: Int = 0,
    val totalCardsRecycledLifetime: Int = 0,
    val totalTradesCompleted: Int = 0,
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
    fun currentScratcherStatus(nowMs: Long): HourlyChargeStatus =
        computeHourlyChargeStatus(scratcherStoredCharges, scratcherLastRefillMs, nowMs)

    fun currentPrismStatus(nowMs: Long): HourlyChargeStatus =
        computeHourlyChargeStatus(prismStoredCharges, prismLastRefillMs, nowMs)

    fun currentWheelStatus(nowMs: Long): HourlyChargeStatus =
        computeHourlyChargeStatus(wheelStoredCharges, wheelLastRefillMs, nowMs)

    fun currentQuizCooldownRemainingMs(nowMs: Long): Long {
        if (quizLastCompletedMs <= 0L) return 0L
        val elapsed = (nowMs - quizLastCompletedMs).coerceAtLeast(0L)
        return (ONE_HOUR_MS - elapsed).coerceAtLeast(0L)
    }

    companion object {
        const val DEFAULT_STARTER_COINS = 250
        const val DEFAULT_STARTER_DIAMONDS = 15
        const val MAX_HOURLY_CHARGES = 3
        const val ONE_HOUR_MS = 3_600_000L

        fun computeHourlyChargeStatus(
            storedCharges: Int,
            lastRefillMs: Long,
            nowMs: Long
        ): HourlyChargeStatus {
            val clampedStored = storedCharges.coerceIn(0, MAX_HOURLY_CHARGES)
            if (clampedStored >= MAX_HOURLY_CHARGES || lastRefillMs <= 0L) {
                return HourlyChargeStatus(charges = MAX_HOURLY_CHARGES, msUntilNextCharge = 0L)
            }
            val elapsed = (nowMs - lastRefillMs).coerceAtLeast(0L)
            val gained = (elapsed / ONE_HOUR_MS).toInt()
            val effective = (clampedStored + gained).coerceAtMost(MAX_HOURLY_CHARGES)
            val remaining = if (effective >= MAX_HOURLY_CHARGES) {
                0L
            } else {
                ONE_HOUR_MS - (elapsed % ONE_HOUR_MS)
            }
            return HourlyChargeStatus(charges = effective, msUntilNextCharge = remaining)
        }
    }
}

data class HourlyChargeStatus(
    val charges: Int,
    val msUntilNextCharge: Long
)

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
        val CATALOG_SYNCED_EPOCH_DAY = longPreferencesKey("catalog_synced_epoch_day")
        val CATALOG_SCHEMA_VERSION = intPreferencesKey("catalog_schema_version")
        val SOUND_MUTED = booleanPreferencesKey("sound_muted")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

        val SCRATCHER_CHARGES = intPreferencesKey("scratcher_charges_v2")
        val SCRATCHER_LAST_REFILL_MS = longPreferencesKey("scratcher_last_refill_ms")
        val PRISM_CHARGES = intPreferencesKey("prism_charges_v2")
        val PRISM_LAST_REFILL_MS = longPreferencesKey("prism_last_refill_ms")
        val WHEEL_CHARGES = intPreferencesKey("wheel_charges_v1")
        val WHEEL_LAST_REFILL_MS = longPreferencesKey("wheel_last_refill_ms")
        val QUIZ_LAST_COMPLETED_MS = longPreferencesKey("quiz_last_completed_ms")

        val PINNED_BADGE_IDS = stringSetPreferencesKey("pinned_badge_ids")
        val TOTAL_WHEEL_SPINS = intPreferencesKey("total_wheel_spins")
        val TOTAL_QUIZZES_COMPLETED = intPreferencesKey("total_quizzes_completed")
        val TOTAL_CARDS_RECYCLED_LIFETIME = intPreferencesKey("total_cards_recycled_lifetime")
        val TOTAL_TRADES_COMPLETED = intPreferencesKey("total_trades_completed")

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
            coins = prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS,
            diamonds = prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS,
            lastDailyClaimEpochDay = prefs[Keys.LAST_DAILY_EPOCH_DAY] ?: -1L,
            dailyStreakCount = prefs[Keys.DAILY_STREAK_COUNT] ?: 0,
            totalPacksOpened = prefs[Keys.TOTAL_PACKS_OPENED] ?: 0,
            totalCoinsRecycled = prefs[Keys.TOTAL_COINS_RECYCLED] ?: 0,
            bonusFreePacks = prefs[Keys.BONUS_FREE_PACKS] ?: 1,
            collectorAlias = prefs[Keys.COLLECTOR_ALIAS] ?: "Mythic Ace",
            catalogLastSyncedMs = prefs[Keys.CATALOG_LAST_SYNCED_MS] ?: 0L,
            catalogSyncedEpochDay = prefs[Keys.CATALOG_SYNCED_EPOCH_DAY] ?: -1L,
            catalogSchemaVersion = prefs[Keys.CATALOG_SCHEMA_VERSION] ?: 0,
            soundMuted = prefs[Keys.SOUND_MUTED] ?: false,
            darkModeEnabled = prefs[Keys.DARK_MODE_ENABLED] ?: false,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            scratcherStoredCharges = prefs[Keys.SCRATCHER_CHARGES] ?: PlayerWalletState.MAX_HOURLY_CHARGES,
            scratcherLastRefillMs = prefs[Keys.SCRATCHER_LAST_REFILL_MS] ?: 0L,
            prismStoredCharges = prefs[Keys.PRISM_CHARGES] ?: PlayerWalletState.MAX_HOURLY_CHARGES,
            prismLastRefillMs = prefs[Keys.PRISM_LAST_REFILL_MS] ?: 0L,
            wheelStoredCharges = prefs[Keys.WHEEL_CHARGES] ?: PlayerWalletState.MAX_HOURLY_CHARGES,
            wheelLastRefillMs = prefs[Keys.WHEEL_LAST_REFILL_MS] ?: 0L,
            quizLastCompletedMs = prefs[Keys.QUIZ_LAST_COMPLETED_MS] ?: 0L,
            pinnedBadgeIds = prefs[Keys.PINNED_BADGE_IDS] ?: emptySet(),
            totalWheelSpins = prefs[Keys.TOTAL_WHEEL_SPINS] ?: 0,
            totalQuizzesCompleted = prefs[Keys.TOTAL_QUIZZES_COMPLETED] ?: 0,
            totalCardsRecycledLifetime = prefs[Keys.TOTAL_CARDS_RECYCLED_LIFETIME] ?: 0,
            totalTradesCompleted = prefs[Keys.TOTAL_TRADES_COMPLETED] ?: 0,
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
                    val curD = prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS
                    prefs[Keys.DIAMONDS] = (curD - diamondCost).coerceAtLeast(0)
                }
                else -> {
                    val curC = prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS
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

    suspend fun addCoins(
        amount: Int,
        fromRecycleCount: Int = 0,
        todayEpochDay: Long? = null,
        currentWeekKey: Long? = null
    ) {
        context.collectorDataStore.edit { prefs ->
            val cur = prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS
            prefs[Keys.COINS] = (cur + amount).coerceAtLeast(0)
            if (fromRecycleCount > 0) {
                prefs[Keys.TOTAL_COINS_RECYCLED] = (prefs[Keys.TOTAL_COINS_RECYCLED] ?: 0) + amount
                prefs[Keys.TOTAL_CARDS_RECYCLED_LIFETIME] =
                    (prefs[Keys.TOTAL_CARDS_RECYCLED_LIFETIME] ?: 0) + fromRecycleCount
                if (todayEpochDay != null && currentWeekKey != null) {
                    resetDailyIfNeeded(prefs, todayEpochDay)
                    resetWeeklyIfNeeded(prefs, currentWeekKey)
                    prefs[Keys.WEEKLY_CARDS_RECYCLED] = (prefs[Keys.WEEKLY_CARDS_RECYCLED] ?: 0) + fromRecycleCount
                }
            }
        }
    }

    suspend fun tryDeductCoins(amount: Int): Boolean {
        if (amount <= 0) return true
        var ok = false
        context.collectorDataStore.edit { prefs ->
            val cur = prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS
            if (cur >= amount) {
                prefs[Keys.COINS] = cur - amount
                ok = true
            }
        }
        return ok
    }

    suspend fun exchangeDiamondsForCoins(diamondCost: Int, coinGain: Int): Boolean {
        var success = false
        context.collectorDataStore.edit { prefs ->
            val curD = prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS
            if (curD >= diamondCost) {
                prefs[Keys.DIAMONDS] = curD - diamondCost
                prefs[Keys.COINS] = (prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS) + coinGain
                success = true
            }
        }
        return success
    }

    suspend fun consumeScratcherChargeAndAwardCoins(
        coinReward: Int,
        nowMs: Long,
        todayEpochDay: Long,
        currentWeekKey: Long
    ): Boolean {
        var recorded = false
        context.collectorDataStore.edit { prefs ->
            val stored = prefs[Keys.SCRATCHER_CHARGES] ?: PlayerWalletState.MAX_HOURLY_CHARGES
            val lastRefill = prefs[Keys.SCRATCHER_LAST_REFILL_MS] ?: 0L
            val status = PlayerWalletState.computeHourlyChargeStatus(stored, lastRefill, nowMs)
            if (status.charges > 0) {
                val afterSpend = status.charges - 1
                val newRefillAnchor = when {
                    status.charges >= PlayerWalletState.MAX_HOURLY_CHARGES -> nowMs
                    else -> {
                        val elapsed = (nowMs - lastRefill).coerceAtLeast(0L)
                        val gained = (elapsed / PlayerWalletState.ONE_HOUR_MS).toInt()
                        lastRefill + gained * PlayerWalletState.ONE_HOUR_MS
                    }
                }
                prefs[Keys.SCRATCHER_CHARGES] = afterSpend
                prefs[Keys.SCRATCHER_LAST_REFILL_MS] = newRefillAnchor
                prefs[Keys.COINS] = (prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS) + coinReward

                resetDailyIfNeeded(prefs, todayEpochDay)
                resetWeeklyIfNeeded(prefs, currentWeekKey)
                prefs[Keys.DAILY_SCRATCHERS_DONE] = (prefs[Keys.DAILY_SCRATCHERS_DONE] ?: 0) + 1
                recorded = true
            }
        }
        return recorded
    }

    suspend fun consumePrismChargeAndAwardDiamonds(
        diamondReward: Int,
        nowMs: Long,
        todayEpochDay: Long,
        currentWeekKey: Long
    ): Boolean {
        var recorded = false
        context.collectorDataStore.edit { prefs ->
            val stored = prefs[Keys.PRISM_CHARGES] ?: PlayerWalletState.MAX_HOURLY_CHARGES
            val lastRefill = prefs[Keys.PRISM_LAST_REFILL_MS] ?: 0L
            val status = PlayerWalletState.computeHourlyChargeStatus(stored, lastRefill, nowMs)
            if (status.charges > 0) {
                val afterSpend = status.charges - 1
                val newRefillAnchor = when {
                    status.charges >= PlayerWalletState.MAX_HOURLY_CHARGES -> nowMs
                    else -> {
                        val elapsed = (nowMs - lastRefill).coerceAtLeast(0L)
                        val gained = (elapsed / PlayerWalletState.ONE_HOUR_MS).toInt()
                        lastRefill + gained * PlayerWalletState.ONE_HOUR_MS
                    }
                }
                prefs[Keys.PRISM_CHARGES] = afterSpend
                prefs[Keys.PRISM_LAST_REFILL_MS] = newRefillAnchor
                prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS) + diamondReward

                if (diamondReward > 0) {
                    resetDailyIfNeeded(prefs, todayEpochDay)
                    resetWeeklyIfNeeded(prefs, currentWeekKey)
                    prefs[Keys.DAILY_MINIGAMES_WON] = (prefs[Keys.DAILY_MINIGAMES_WON] ?: 0) + 1
                    prefs[Keys.WEEKLY_MINIGAMES_WON] = (prefs[Keys.WEEKLY_MINIGAMES_WON] ?: 0) + 1
                }
                recorded = true
            }
        }
        return recorded
    }

    suspend fun consumeWheelSpinAndAward(
        coinReward: Int,
        diamondReward: Int,
        freePackReward: Int,
        nowMs: Long,
        todayEpochDay: Long,
        currentWeekKey: Long
    ): Boolean {
        var recorded = false
        context.collectorDataStore.edit { prefs ->
            val stored = prefs[Keys.WHEEL_CHARGES] ?: PlayerWalletState.MAX_HOURLY_CHARGES
            val lastRefill = prefs[Keys.WHEEL_LAST_REFILL_MS] ?: 0L
            val status = PlayerWalletState.computeHourlyChargeStatus(stored, lastRefill, nowMs)
            if (status.charges > 0) {
                val afterSpend = status.charges - 1
                val newRefillAnchor = when {
                    status.charges >= PlayerWalletState.MAX_HOURLY_CHARGES -> nowMs
                    else -> {
                        val elapsed = (nowMs - lastRefill).coerceAtLeast(0L)
                        val gained = (elapsed / PlayerWalletState.ONE_HOUR_MS).toInt()
                        lastRefill + gained * PlayerWalletState.ONE_HOUR_MS
                    }
                }
                prefs[Keys.WHEEL_CHARGES] = afterSpend
                prefs[Keys.WHEEL_LAST_REFILL_MS] = newRefillAnchor
                if (coinReward > 0) {
                    prefs[Keys.COINS] = (prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS) + coinReward
                }
                if (diamondReward > 0) {
                    prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS) + diamondReward
                }
                if (freePackReward > 0) {
                    prefs[Keys.BONUS_FREE_PACKS] = (prefs[Keys.BONUS_FREE_PACKS] ?: 1) + freePackReward
                }
                prefs[Keys.TOTAL_WHEEL_SPINS] = (prefs[Keys.TOTAL_WHEEL_SPINS] ?: 0) + 1

                resetDailyIfNeeded(prefs, todayEpochDay)
                resetWeeklyIfNeeded(prefs, currentWeekKey)
                prefs[Keys.DAILY_MINIGAMES_WON] = (prefs[Keys.DAILY_MINIGAMES_WON] ?: 0) + 1
                prefs[Keys.WEEKLY_MINIGAMES_WON] = (prefs[Keys.WEEKLY_MINIGAMES_WON] ?: 0) + 1
                recorded = true
            }
        }
        return recorded
    }

    suspend fun completeHourlyQuizAndAwardDiamonds(
        diamondReward: Int,
        nowMs: Long,
        todayEpochDay: Long,
        currentWeekKey: Long
    ): Boolean {
        var recorded = false
        context.collectorDataStore.edit { prefs ->
            val last = prefs[Keys.QUIZ_LAST_COMPLETED_MS] ?: 0L
            if (last <= 0L || (nowMs - last) >= PlayerWalletState.ONE_HOUR_MS) {
                prefs[Keys.QUIZ_LAST_COMPLETED_MS] = nowMs
                prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS) + diamondReward
                prefs[Keys.TOTAL_QUIZZES_COMPLETED] = (prefs[Keys.TOTAL_QUIZZES_COMPLETED] ?: 0) + 1

                if (diamondReward > 0) {
                    resetDailyIfNeeded(prefs, todayEpochDay)
                    resetWeeklyIfNeeded(prefs, currentWeekKey)
                    prefs[Keys.DAILY_MINIGAMES_WON] = (prefs[Keys.DAILY_MINIGAMES_WON] ?: 0) + 1
                    prefs[Keys.WEEKLY_MINIGAMES_WON] = (prefs[Keys.WEEKLY_MINIGAMES_WON] ?: 0) + 1
                }
                recorded = true
            }
        }
        return recorded
    }

    suspend fun recordTradeQuestProgress() {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.COMMUNITY_TRADES] = (prefs[Keys.COMMUNITY_TRADES] ?: 0) + 1
            prefs[Keys.TOTAL_TRADES_COMPLETED] = (prefs[Keys.TOTAL_TRADES_COMPLETED] ?: 0) + 1
        }
    }

    suspend fun togglePinnedBadge(badgeId: String, maxPinned: Int = 3): Boolean {
        var added = false
        context.collectorDataStore.edit { prefs ->
            val current = (prefs[Keys.PINNED_BADGE_IDS] ?: emptySet()).toMutableSet()
            if (badgeId in current) {
                current.remove(badgeId)
                added = false
            } else {
                if (current.size >= maxPinned) {
                    val oldest = current.first()
                    current.remove(oldest)
                }
                current.add(badgeId)
                added = true
            }
            prefs[Keys.PINNED_BADGE_IDS] = current
        }
        return added
    }

    suspend fun syncQuestPeriodsIfNeeded(todayEpochDay: Long, currentWeekKey: Long) {
        context.collectorDataStore.edit { prefs ->
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
                    prefs[Keys.COINS] = (prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS) + coinReward
                }
                if (diamondReward > 0) {
                    prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS) + diamondReward
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
            prefs[Keys.COINS] = (prefs[Keys.COINS] ?: PlayerWalletState.DEFAULT_STARTER_COINS) + coinReward
            if (bonusDiamonds > 0) {
                prefs[Keys.DIAMONDS] = (prefs[Keys.DIAMONDS] ?: PlayerWalletState.DEFAULT_STARTER_DIAMONDS) + bonusDiamonds
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

    suspend fun markCatalogSynced(timestampMs: Long, epochDay: Long, schemaVersion: Int) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.CATALOG_LAST_SYNCED_MS] = timestampMs
            prefs[Keys.CATALOG_SYNCED_EPOCH_DAY] = epochDay
            prefs[Keys.CATALOG_SCHEMA_VERSION] = schemaVersion
        }
    }

    suspend fun setSoundMuted(muted: Boolean) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.SOUND_MUTED] = muted
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
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
        const val CURRENT_CATALOG_SCHEMA_VERSION = 3
    }
}
