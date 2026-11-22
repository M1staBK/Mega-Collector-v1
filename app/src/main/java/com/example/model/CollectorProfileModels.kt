package com.example.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp

data class CollectorPublicProfile(
    val traderId: String,
    val alias: String,
    val mlbbIgn: String = "",
    val mlbbServerId: String = "",
    val photoUrl: String = "",
    val rankTitle: String,
    val avatarColor: Color,
    val prestigeScore: Int,
    val uniqueSkinsCount: Int,
    val packsOpened: Int,
    val tradesDone: Int,
    val misprintCount: Int = 0,
    val motto: String,
    val badgeIds: List<String>,
    val showcaseSkins: List<SkinCatalogItem> = emptyList()
)

enum class CollectorHistoryType(val wireValue: String, val label: String) {
    PULL("PULL", "Pack Pull"),
    TRADE("TRADE", "P2P Trade"),
    RECYCLE("RECYCLE", "Recycle"),
    MINIGAME("MINIGAME", "Arcade"),
    PROFILE("PROFILE", "Profile");

    companion object {
        fun fromWire(raw: String): CollectorHistoryType =
            entries.firstOrNull { it.wireValue == raw } ?: PROFILE
    }
}

data class CollectorHistoryEvent(
    val id: String,
    val eventType: CollectorHistoryType,
    val title: String,
    val detail: String,
    val rarity: SkinRarity?,
    val coinDelta: Int,
    val diamondDelta: Int,
    val createdAtMs: Long
)

data class P2PTradeListing(
    val id: String,
    val sellerUid: String,
    val sellerName: String,
    val sellerPhotoUrl: String,
    val sellerMlbbIgn: String,
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
    val seekingNote: String,
    val tradeCode: String,
    val status: String, // OPEN, COMPLETED, CANCELLED
    val acceptedByUid: String = "",
    val acceptedByName: String = "",
    val acceptedCatalogId: String = "",
    val acceptedSkinName: String = "",
    val acceptedHeroName: String = "",
    val acceptedRole: String = "",
    val acceptedRawTier: String = "",
    val acceptedRarity: SkinRarity = SkinRarity.COMMON,
    val acceptedImageUrl: String = "",
    val acceptedIsMisprint: Boolean = false,
    val acceptedMisprintAnomaly: MisprintAnomaly? = null,
    val acceptedSerialCode: String = "",
    val sellerSettled: Boolean = false,
    val createdAtMs: Long = Timestamp.now().seconds * 1000L
)
