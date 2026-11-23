package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.data.local.PlayerWalletState
import com.example.model.CollectorHistoryEvent
import com.example.model.CollectorHistoryType
import com.example.model.CollectorPublicProfile
import com.example.model.MisprintAnomaly
import com.example.model.OwnedCard
import com.example.model.P2PTradeListing
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.math.abs

sealed interface CloudUiState<out T> {
    data object Loading : CloudUiState<Nothing>
    data class Success<T>(val data: T) : CloudUiState<T>
    data class Error(val message: String) : CloudUiState<Nothing>
}

class FirestoreCollectorService(context: Context) {

    private val appContext = context.applicationContext

    private fun isFirebaseConfigured(): Boolean = FirebaseApp.getApps(appContext).isNotEmpty()

    private fun getDbOrNull(): FirebaseFirestore? {
        if (!isFirebaseConfigured()) return null
        return runCatching {
            val resId = appContext.resources.getIdentifier("firestore_database_id", "string", appContext.packageName)
            val dbId = if (resId != 0) appContext.getString(resId) else "(default)"
            FirebaseFirestore.getInstance(dbId)
        }.getOrNull()
    }

    private fun getAuthOrNull(): FirebaseAuth? {
        if (!isFirebaseConfigured()) return null
        return runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }

    private fun requireDb(): FirebaseFirestore {
        return getDbOrNull()
            ?: throw IllegalStateException("Cloud database is offline until Firebase google-services.json is configured.")
    }

    private fun requireUid(): String {
        return getAuthOrNull()?.currentUser?.uid
            ?: throw IllegalStateException("Please sign in with Google to use cloud features.")
    }

    suspend fun syncUserAndPublicProfile(
        user: AuthenticatedUser,
        wallet: PlayerWalletState,
        ownedCards: List<OwnedCard>,
        collectorRank: String,
        pinnedBadgeIds: List<String>
    ): Result<Unit> {
        return try {
            val db = requireDb()
            val uid = requireUid()
            if (uid != user.uid) {
                return Result.failure(IllegalStateException("Authenticated user mismatch."))
            }

            val uniqueSkins = ownedCards.map { it.catalogId }.distinct().size
            val misprintCount = ownedCards.count { it.isMisprint }
            val prestigeScore = ownedCards.sumOf { it.prestigeScore }
            val displayName = wallet.collectorAlias.ifBlank { user.displayName }.take(60)
            val safeIgn = wallet.mlbbIgn.trim().take(40)
            val safeServer = wallet.mlbbServerId.trim().take(16)
            val safePhoto = user.photoUrl.take(512)

            val userRef = db.collection("users").document(uid)
            val existingUserSnap = userRef.get().await()
            val preservedCreatedAt: Any = existingUserSnap.getTimestamp("createdAt")
                ?: FieldValue.serverTimestamp()

            val userMap = mapOf(
                "userId" to uid,
                "email" to user.email.take(200),
                "displayName" to displayName,
                "photoUrl" to safePhoto,
                "mlbbIgn" to safeIgn,
                "mlbbServerId" to safeServer,
                "collectorRank" to collectorRank.take(40),
                "prestigeScore" to prestigeScore.coerceAtLeast(0),
                "coins" to wallet.coins.coerceAtLeast(0),
                "diamonds" to wallet.diamonds.coerceAtLeast(0),
                "uniqueSkinsCount" to uniqueSkins.coerceAtLeast(0),
                "totalSkinsOwned" to ownedCards.size.coerceAtLeast(0),
                "misprintCount" to misprintCount.coerceAtLeast(0),
                "totalPacksOpened" to wallet.totalPacksOpened.coerceAtLeast(0),
                "totalTradesCompleted" to wallet.totalTradesCompleted.coerceAtLeast(0),
                "dailyStreakCount" to wallet.dailyStreakCount.coerceAtLeast(0),
                "createdAt" to preservedCreatedAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(userMap, SetOptions.merge()).await()

            val top3 = ownedCards
                .sortedByDescending { it.prestigeScore }
                .distinctBy { it.catalogId }
                .take(3)

            val publicMap = mapOf(
                "userId" to uid,
                "displayName" to displayName,
                "photoUrl" to safePhoto,
                "mlbbIgn" to safeIgn,
                "mlbbServerId" to safeServer,
                "collectorRank" to collectorRank.take(40),
                "motto" to "Collecting $uniqueSkins unique skins & $misprintCount 1/1 Misprints.".take(180),
                "prestigeScore" to prestigeScore.coerceAtLeast(0),
                "uniqueSkinsCount" to uniqueSkins.coerceAtLeast(0),
                "totalPacksOpened" to wallet.totalPacksOpened.coerceAtLeast(0),
                "totalTradesCompleted" to wallet.totalTradesCompleted.coerceAtLeast(0),
                "misprintCount" to misprintCount.coerceAtLeast(0),
                "pinnedBadgeIds" to pinnedBadgeIds.take(3),
                "showcaseSkinNames" to top3.map { it.skinName.take(80) },
                "showcaseHeroNames" to top3.map { it.heroName.take(60) },
                "showcaseRarityNames" to top3.map { it.rarity.name },
                "showcaseImageUrls" to top3.map { it.imageUrl.take(512) },
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("public_profiles").document(uid).set(publicMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreCollector", "Profile sync failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun recordHistoryEvent(
        uid: String,
        eventType: CollectorHistoryType,
        title: String,
        detail: String,
        rarityName: String = "",
        coinDelta: Int = 0,
        diamondDelta: Int = 0
    ): Result<Unit> {
        return try {
            val db = requireDb()
            val activeUid = requireUid()
            if (activeUid != uid) return Result.failure(IllegalStateException("User mismatch"))
            val payload = mapOf(
                "userId" to uid,
                "eventType" to eventType.wireValue,
                "title" to title.trim().ifBlank { eventType.label }.take(100),
                "detail" to detail.trim().take(240),
                "rarityName" to rarityName.trim().take(32),
                "coinDelta" to coinDelta,
                "diamondDelta" to diamondDelta,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users")
                .document(uid)
                .collection("history")
                .add(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreCollector", "History write failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeHistoryEvents(uid: String): Flow<List<CollectorHistoryEvent>> = callbackFlow {
        val db = getDbOrNull()
        if (db == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("users")
            .document(uid)
            .collection("history")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(40)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toHistoryEvent()
                }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        val fsEx = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        Log.w("FirestoreCollector", "History stream error: ${fsEx?.message ?: err.message}")
        emit(emptyList())
    }

    fun observeOpenP2PListings(): Flow<List<P2PTradeListing>> = callbackFlow {
        val db = getDbOrNull()
        if (db == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("trade_listings")
            .whereEqualTo("status", "OPEN")
            .limit(60)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toP2PTradeListing() }
                    ?.sortedByDescending { it.createdAtMs }
                    .orEmpty()
                trySend(items)
            }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        Log.w("FirestoreCollector", "Open trade listings error: ${err.message}")
        emit(emptyList())
    }

    fun observeSellerP2PListings(uid: String): Flow<List<P2PTradeListing>> = callbackFlow {
        val db = getDbOrNull()
        if (db == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("trade_listings")
            .whereEqualTo("sellerUid", uid)
            .limit(40)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toP2PTradeListing() }
                    ?.sortedByDescending { it.createdAtMs }
                    .orEmpty()
                trySend(items)
            }
        awaitClose { reg.remove() }
    }.catch { err ->
        if (err is CancellationException) throw err
        Log.w("FirestoreCollector", "My trade listings error: ${err.message}")
        emit(emptyList())
    }

    suspend fun publishP2PListing(
        user: AuthenticatedUser,
        wallet: PlayerWalletState,
        card: OwnedCard,
        seekingNote: String,
        tradeCode: String
    ): Result<String> {
        return try {
            val db = requireDb()
            val uid = requireUid()
            val payload = mapOf(
                "sellerUid" to uid,
                "sellerName" to wallet.collectorAlias.ifBlank { user.displayName }.take(60),
                "sellerPhotoUrl" to user.photoUrl.take(512),
                "sellerMlbbIgn" to wallet.mlbbIgn.trim().take(40),
                "catalogId" to card.catalogId.take(80),
                "skinName" to card.skinName.take(100),
                "heroName" to card.heroName.take(60),
                "role" to card.role.ifBlank { "Hero" }.take(40),
                "rawTier" to card.rawTier.ifBlank { card.rarity.displayName }.take(40),
                "rarityName" to card.rarity.name,
                "imageUrl" to card.imageUrl.take(512),
                "isMisprint" to card.isMisprint,
                "misprintAnomalyName" to (card.misprintAnomaly?.name ?: "").take(60),
                "serialCode" to card.serialCode.take(40),
                "seekingNote" to seekingNote.trim().ifBlank { "Open to fair P2P offers" }.take(160),
                "tradeCode" to tradeCode.take(1500),
                "status" to "OPEN",
                "acceptedByUid" to "",
                "acceptedByName" to "",
                "acceptedCatalogId" to "",
                "acceptedSkinName" to "",
                "acceptedHeroName" to "",
                "acceptedRole" to "",
                "acceptedRawTier" to "",
                "acceptedRarityName" to "",
                "acceptedImageUrl" to "",
                "acceptedIsMisprint" to false,
                "acceptedMisprintAnomalyName" to "",
                "acceptedSerialCode" to "",
                "sellerSettled" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val docRef = db.collection("trade_listings").add(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreCollector", "Publish listing failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun cancelP2PListing(listingId: String): Result<Unit> {
        return try {
            val db = requireDb()
            requireUid()
            db.collection("trade_listings").document(listingId).update(
                mapOf(
                    "status" to "CANCELLED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptP2PListingTransaction(
        listingId: String,
        buyerUid: String,
        buyerName: String,
        offeredCard: OwnedCard
    ): Result<Unit> {
        return try {
            val db = requireDb()
            val activeUid = requireUid()
            if (activeUid != buyerUid) return Result.failure(IllegalStateException("Auth mismatch"))
            val docRef = db.collection("trade_listings").document(listingId)
            db.runTransaction { tx ->
                val snap = tx.get(docRef)
                val status = snap.getString("status") ?: ""
                val sellerUid = snap.getString("sellerUid") ?: ""
                if (status != "OPEN") {
                    throw IllegalStateException("This listing is no longer available.")
                }
                if (sellerUid == buyerUid) {
                    throw IllegalStateException("Cannot accept your own listing.")
                }
                tx.update(
                    docRef,
                    mapOf(
                        "status" to "COMPLETED",
                        "acceptedByUid" to buyerUid,
                        "acceptedByName" to buyerName.ifBlank { "Collector" }.take(60),
                        "acceptedCatalogId" to offeredCard.catalogId.take(80),
                        "acceptedSkinName" to offeredCard.skinName.take(100),
                        "acceptedHeroName" to offeredCard.heroName.take(60),
                        "acceptedRole" to offeredCard.role.ifBlank { "Hero" }.take(40),
                        "acceptedRawTier" to offeredCard.rawTier.ifBlank { offeredCard.rarity.displayName }.take(40),
                        "acceptedRarityName" to offeredCard.rarity.name,
                        "acceptedImageUrl" to offeredCard.imageUrl.take(512),
                        "acceptedIsMisprint" to offeredCard.isMisprint,
                        "acceptedMisprintAnomalyName" to (offeredCard.misprintAnomaly?.name ?: "").take(60),
                        "acceptedSerialCode" to offeredCard.serialCode.take(40),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markListingSellerSettled(listingId: String): Result<Unit> {
        return try {
            val db = requireDb()
            requireUid()
            db.collection("trade_listings").document(listingId).update(
                mapOf(
                    "sellerSettled" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPublicProfile(targetUid: String): Result<CollectorPublicProfile?> {
        return try {
            val db = requireDb()
            requireUid()
            val doc = db.collection("public_profiles").document(targetUid).get().await()
            if (!doc.exists()) return Result.success(null)
            Result.success(doc.toCollectorPublicProfile())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toHistoryEvent(): CollectorHistoryEvent? {
        val data = data ?: return null
        val created = (getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()
            ?: Date()).time
        val rarityRaw = data["rarityName"] as? String ?: ""
        return CollectorHistoryEvent(
            id = id,
            eventType = CollectorHistoryType.fromWire(data["eventType"] as? String ?: ""),
            title = data["title"] as? String ?: "Collector Activity",
            detail = data["detail"] as? String ?: "",
            rarity = if (rarityRaw.isNotBlank()) SkinRarity.fromNameSafe(rarityRaw) else null,
            coinDelta = (data["coinDelta"] as? Number)?.toInt() ?: 0,
            diamondDelta = (data["diamondDelta"] as? Number)?.toInt() ?: 0,
            createdAtMs = created
        )
    }

    private fun DocumentSnapshot.toP2PTradeListing(): P2PTradeListing? {
        val data = data ?: return null
        val created = (getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()
            ?: Date()).time
        val anomalyRaw = data["misprintAnomalyName"] as? String
        val acceptedAnomalyRaw = data["acceptedMisprintAnomalyName"] as? String
        val acceptedRarityRaw = data["acceptedRarityName"] as? String ?: ""
        return P2PTradeListing(
            id = id,
            sellerUid = data["sellerUid"] as? String ?: return null,
            sellerName = data["sellerName"] as? String ?: "Collector",
            sellerPhotoUrl = data["sellerPhotoUrl"] as? String ?: "",
            sellerMlbbIgn = data["sellerMlbbIgn"] as? String ?: "",
            catalogId = data["catalogId"] as? String ?: "",
            skinName = data["skinName"] as? String ?: return null,
            heroName = data["heroName"] as? String ?: "",
            role = data["role"] as? String ?: "Hero",
            rawTier = data["rawTier"] as? String ?: "Epic",
            rarity = SkinRarity.fromNameSafe(data["rarityName"] as? String),
            imageUrl = data["imageUrl"] as? String ?: "",
            isMisprint = data["isMisprint"] as? Boolean ?: false,
            misprintAnomaly = if (anomalyRaw.isNullOrBlank()) null else MisprintAnomaly.fromNameSafe(anomalyRaw),
            serialCode = data["serialCode"] as? String ?: "STD-0000",
            seekingNote = data["seekingNote"] as? String ?: "",
            tradeCode = data["tradeCode"] as? String ?: "",
            status = data["status"] as? String ?: "OPEN",
            acceptedByUid = data["acceptedByUid"] as? String ?: "",
            acceptedByName = data["acceptedByName"] as? String ?: "",
            acceptedCatalogId = data["acceptedCatalogId"] as? String ?: "",
            acceptedSkinName = data["acceptedSkinName"] as? String ?: "",
            acceptedHeroName = data["acceptedHeroName"] as? String ?: "",
            acceptedRole = data["acceptedRole"] as? String ?: "",
            acceptedRawTier = data["acceptedRawTier"] as? String ?: "",
            acceptedRarity = if (acceptedRarityRaw.isNotBlank()) SkinRarity.fromNameSafe(acceptedRarityRaw) else SkinRarity.COMMON,
            acceptedImageUrl = data["acceptedImageUrl"] as? String ?: "",
            acceptedIsMisprint = data["acceptedIsMisprint"] as? Boolean ?: false,
            acceptedMisprintAnomaly = if (acceptedAnomalyRaw.isNullOrBlank()) null else MisprintAnomaly.fromNameSafe(acceptedAnomalyRaw),
            acceptedSerialCode = data["acceptedSerialCode"] as? String ?: "",
            sellerSettled = data["sellerSettled"] as? Boolean ?: false,
            createdAtMs = created
        )
    }

    private fun DocumentSnapshot.toCollectorPublicProfile(): CollectorPublicProfile? {
        val data = data ?: return null
        val skinNames = (data["showcaseSkinNames"] as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        val heroNames = (data["showcaseHeroNames"] as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        val rarityNames = (data["showcaseRarityNames"] as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        val imageUrls = (data["showcaseImageUrls"] as? List<*>)?.mapNotNull { it as? String }.orEmpty()

        val showcaseSkins = skinNames.indices.map { i ->
            val rName = rarityNames.getOrNull(i)
            val rarity = SkinRarity.fromNameSafe(rName)
            SkinCatalogItem(
                id = "showcase_${id}_$i",
                skinName = skinNames[i],
                heroName = heroNames.getOrElse(i) { "Hero" },
                role = "Showcase",
                rawTier = rarity.displayName,
                rarity = rarity,
                imageUrl = imageUrls.getOrElse(i) { "" }
            )
        }
        val uid = data["userId"] as? String ?: id
        val accentColors = listOf(
            Color(0xFF0284C7),
            Color(0xFF6366F1),
            Color(0xFF10B981),
            Color(0xFFE11D48),
            Color(0xFFF59E0B)
        )
        val badgeList = (data["pinnedBadgeIds"] as? List<*>)?.mapNotNull { it as? String }.orEmpty()

        return CollectorPublicProfile(
            traderId = uid,
            alias = data["displayName"] as? String ?: "Collector",
            mlbbIgn = data["mlbbIgn"] as? String ?: "",
            mlbbServerId = data["mlbbServerId"] as? String ?: "",
            photoUrl = data["photoUrl"] as? String ?: "",
            rankTitle = data["collectorRank"] as? String ?: "Warrior",
            avatarColor = accentColors[abs(uid.hashCode()) % accentColors.size],
            prestigeScore = (data["prestigeScore"] as? Number)?.toInt() ?: 0,
            uniqueSkinsCount = (data["uniqueSkinsCount"] as? Number)?.toInt() ?: 0,
            packsOpened = (data["totalPacksOpened"] as? Number)?.toInt() ?: 0,
            tradesDone = (data["totalTradesCompleted"] as? Number)?.toInt() ?: 0,
            misprintCount = (data["misprintCount"] as? Number)?.toInt() ?: 0,
            motto = data["motto"] as? String ?: "",
            badgeIds = badgeList,
            showcaseSkins = showcaseSkins
        )
    }
}
