package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectorDao {
    @Query("SELECT * FROM skin_catalog ORDER BY heroName ASC, skinName ASC")
    fun observeCatalog(): Flow<List<SkinCatalogEntity>>

    @Query("SELECT * FROM skin_catalog")
    suspend fun getAllCatalogOnce(): List<SkinCatalogEntity>

    @Query("SELECT COUNT(*) FROM skin_catalog")
    suspend fun getCatalogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalog(items: List<SkinCatalogEntity>)

    @Query("SELECT * FROM owned_cards ORDER BY obtainedAtMs DESC, instanceId DESC")
    fun observeOwnedCards(): Flow<List<OwnedCardEntity>>

    @Query("SELECT * FROM owned_cards WHERE instanceId = :instanceId LIMIT 1")
    fun observeCardById(instanceId: Long): Flow<OwnedCardEntity?>

    @Query("SELECT * FROM owned_cards WHERE instanceId = :instanceId LIMIT 1")
    suspend fun getCardById(instanceId: Long): OwnedCardEntity?

    @Query("SELECT * FROM owned_cards")
    suspend fun getAllOwnedOnce(): List<OwnedCardEntity>

    @Insert
    suspend fun insertOwnedCards(cards: List<OwnedCardEntity>): List<Long>

    @Insert
    suspend fun insertOwnedCard(card: OwnedCardEntity): Long

    @Query("DELETE FROM owned_cards WHERE instanceId = :instanceId")
    suspend fun deleteOwnedCardById(instanceId: Long)

    @Query("DELETE FROM owned_cards WHERE instanceId IN (:instanceIds)")
    suspend fun deleteOwnedCardsByIds(instanceIds: List<Long>)

    @Query("UPDATE owned_cards SET isListedForTrade = :listed WHERE instanceId = :instanceId")
    suspend fun setCardListedForTrade(instanceId: Long, listed: Boolean)

    @Query("SELECT * FROM trade_listings ORDER BY createdAtMs DESC")
    fun observeTradeListings(): Flow<List<TradeListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradeListing(listing: TradeListingEntity): Long

    @Query("DELETE FROM trade_listings WHERE id = :listingId")
    suspend fun deleteTradeListingById(listingId: Long)

    @Query("DELETE FROM trade_listings WHERE cardInstanceId = :cardInstanceId")
    suspend fun deleteTradeListingsForCard(cardInstanceId: Long)

    @Transaction
    suspend fun replaceCatalog(items: List<SkinCatalogEntity>) {
        upsertCatalog(items)
    }
}
