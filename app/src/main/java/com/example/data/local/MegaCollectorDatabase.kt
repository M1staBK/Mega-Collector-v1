package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SkinCatalogEntity::class,
        OwnedCardEntity::class,
        TradeListingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MegaCollectorDatabase : RoomDatabase() {
    abstract fun collectorDao(): CollectorDao

    companion object {
        @Volatile
        private var INSTANCE: MegaCollectorDatabase? = null

        fun getInstance(context: Context): MegaCollectorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MegaCollectorDatabase::class.java,
                    "mega_collector_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
