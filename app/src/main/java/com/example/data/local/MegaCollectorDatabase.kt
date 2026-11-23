package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SkinCatalogEntity::class,
        OwnedCardEntity::class,
        TradeListingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MegaCollectorDatabase : RoomDatabase() {
    abstract fun collectorDao(): CollectorDao

    companion object {
        @Volatile
        private var INSTANCE: MegaCollectorDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE owned_cards ADD COLUMN isHolographic INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE owned_cards SET isHolographic = 1 WHERE rarityName = 'LEGEND' OR (rarityName = 'COLLECTOR' AND (instanceId % 2) = 0)"
                )
            }
        }

        fun getInstance(context: Context): MegaCollectorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MegaCollectorDatabase::class.java,
                    "mega_collector_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
