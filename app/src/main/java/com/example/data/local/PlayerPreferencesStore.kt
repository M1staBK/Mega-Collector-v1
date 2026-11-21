package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.collectorDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mega_collector_prefs"
)

data class PlayerWalletState(
    val arenaGold: Int = 1200,
    val lastDailyClaimEpochDay: Long = -1L,
    val dailyStreakCount: Int = 0,
    val totalPacksOpened: Int = 0,
    val totalGoldRecycled: Int = 0,
    val bonusFreePacksStarter: Int = 1,
    val bonusFreePacksMythic: Int = 0,
    val collectorAlias: String = "Mythic Ace",
    val catalogLastSyncedMs: Long = 0L
)

class PlayerPreferencesStore(private val context: Context) {
    private object Keys {
        val ARENA_GOLD = intPreferencesKey("arena_gold")
        val LAST_DAILY_EPOCH_DAY = longPreferencesKey("last_daily_epoch_day")
        val DAILY_STREAK_COUNT = intPreferencesKey("daily_streak_count")
        val TOTAL_PACKS_OPENED = intPreferencesKey("total_packs_opened")
        val TOTAL_GOLD_RECYCLED = intPreferencesKey("total_gold_recycled")
        val BONUS_STARTER_PACKS = intPreferencesKey("bonus_starter_packs")
        val BONUS_MYTHIC_PACKS = intPreferencesKey("bonus_mythic_packs")
        val COLLECTOR_ALIAS = stringPreferencesKey("collector_alias")
        val CATALOG_LAST_SYNCED_MS = longPreferencesKey("catalog_last_synced_ms")
    }

    val walletFlow: Flow<PlayerWalletState> = context.collectorDataStore.data.map { prefs ->
        PlayerWalletState(
            arenaGold = prefs[Keys.ARENA_GOLD] ?: 1200,
            lastDailyClaimEpochDay = prefs[Keys.LAST_DAILY_EPOCH_DAY] ?: -1L,
            dailyStreakCount = prefs[Keys.DAILY_STREAK_COUNT] ?: 0,
            totalPacksOpened = prefs[Keys.TOTAL_PACKS_OPENED] ?: 0,
            totalGoldRecycled = prefs[Keys.TOTAL_GOLD_RECYCLED] ?: 0,
            bonusFreePacksStarter = prefs[Keys.BONUS_STARTER_PACKS] ?: 1,
            bonusFreePacksMythic = prefs[Keys.BONUS_MYTHIC_PACKS] ?: 0,
            collectorAlias = prefs[Keys.COLLECTOR_ALIAS] ?: "Mythic Ace",
            catalogLastSyncedMs = prefs[Keys.CATALOG_LAST_SYNCED_MS] ?: 0L
        )
    }

    suspend fun spendGoldOrTicketAndRecordPack(
        useTicket: Boolean,
        isMythicTicket: Boolean,
        goldCost: Int
    ) {
        context.collectorDataStore.edit { prefs ->
            if (useTicket) {
                if (isMythicTicket) {
                    val cur = prefs[Keys.BONUS_MYTHIC_PACKS] ?: 0
                    prefs[Keys.BONUS_MYTHIC_PACKS] = (cur - 1).coerceAtLeast(0)
                } else {
                    val cur = prefs[Keys.BONUS_STARTER_PACKS] ?: 1
                    prefs[Keys.BONUS_STARTER_PACKS] = (cur - 1).coerceAtLeast(0)
                }
            } else {
                val curGold = prefs[Keys.ARENA_GOLD] ?: 1200
                prefs[Keys.ARENA_GOLD] = (curGold - goldCost).coerceAtLeast(0)
            }
            val opened = prefs[Keys.TOTAL_PACKS_OPENED] ?: 0
            prefs[Keys.TOTAL_PACKS_OPENED] = opened + 1
        }
    }

    suspend fun addGold(amount: Int, fromRecycle: Boolean = false) {
        context.collectorDataStore.edit { prefs ->
            val curGold = prefs[Keys.ARENA_GOLD] ?: 1200
            prefs[Keys.ARENA_GOLD] = curGold + amount
            if (fromRecycle) {
                val recycled = prefs[Keys.TOTAL_GOLD_RECYCLED] ?: 0
                prefs[Keys.TOTAL_GOLD_RECYCLED] = recycled + amount
            }
        }
    }

    suspend fun adjustGoldDelta(delta: Int) {
        context.collectorDataStore.edit { prefs ->
            val curGold = prefs[Keys.ARENA_GOLD] ?: 1200
            prefs[Keys.ARENA_GOLD] = (curGold + delta).coerceAtLeast(0)
        }
    }

    suspend fun recordDailyRewardClaim(
        epochDay: Long,
        newStreak: Int,
        goldReward: Int,
        grantStarterPack: Boolean,
        grantMythicPack: Boolean
    ) {
        context.collectorDataStore.edit { prefs ->
            val curGold = prefs[Keys.ARENA_GOLD] ?: 1200
            prefs[Keys.ARENA_GOLD] = curGold + goldReward
            prefs[Keys.LAST_DAILY_EPOCH_DAY] = epochDay
            prefs[Keys.DAILY_STREAK_COUNT] = newStreak
            if (grantStarterPack) {
                prefs[Keys.BONUS_STARTER_PACKS] = (prefs[Keys.BONUS_STARTER_PACKS] ?: 1) + 1
            }
            if (grantMythicPack) {
                prefs[Keys.BONUS_MYTHIC_PACKS] = (prefs[Keys.BONUS_MYTHIC_PACKS] ?: 0) + 1
            }
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

    suspend fun markCatalogSynced(timestampMs: Long) {
        context.collectorDataStore.edit { prefs ->
            prefs[Keys.CATALOG_LAST_SYNCED_MS] = timestampMs
        }
    }
}
