package com.example.ui.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.audio.PackSoundEngine
import com.example.data.local.HourlyChargeStatus
import com.example.data.local.MegaCollectorDatabase
import com.example.data.local.PlayerPreferencesStore
import com.example.data.local.PlayerWalletState
import com.example.data.remote.AuthenticatedUser
import com.example.data.remote.FirestoreCollectorService
import com.example.data.repository.CollectorRepository
import com.example.model.AchievementBadgeUi
import com.example.model.AchievementCatalog
import com.example.model.CollectorHistoryEvent
import com.example.model.CollectorHistoryType
import com.example.model.CollectorPublicProfile
import com.example.model.DailyRewardTier
import com.example.model.DecodedTradeSlip
import com.example.model.DiamondExchangeOption
import com.example.model.MlbbCollectionTier
import com.example.model.OwnedCard
import com.example.model.P2PTradeListing
import com.example.model.PackBundleOption
import com.example.model.PackSatinTheme
import com.example.model.QuestUiItem
import com.example.model.QuizQuestionBank
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.TradeListing
import com.example.ui.components.buildMlbbCardImageRequest
import com.example.ui.components.buildMlbbSquareImageRequest
import com.example.ui.theme.ThemeAccentOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicInteger

sealed interface CatalogSyncStatus {
    data object Idle : CatalogSyncStatus
    data class Syncing(val count: Int = 0, val stage: String = "Syncing skins from mlbbdex.com...") : CatalogSyncStatus
    data class Synced(val count: Int) : CatalogSyncStatus
    data class Error(val message: String) : CatalogSyncStatus
}

data class PendingPackPurchase(
    val bundle: PackBundleOption,
    val payWithDiamonds: Boolean,
    val useFreeTicket: Boolean
)

data class PackOpeningSession(
    val bundle: PackBundleOption,
    val wrapperTheme: PackSatinTheme = PackSatinTheme.ALL.first(),
    val pendingPurchase: PendingPackPurchase? = null,
    val pulledCards: List<OwnedCard> = emptyList(),
    val isRipped: Boolean = false,
    val isCommittingRip: Boolean = false,
    val revealedIndices: Set<Int> = emptySet(),
    val focusedRevealedIndex: Int? = null,
    val showSummaryGrid: Boolean = false
) {
    val allRevealed: Boolean get() = pulledCards.isNotEmpty() && revealedIndices.size >= pulledCards.size
    val nextUnrevealedIndex: Int? get() = pulledCards.indices.firstOrNull { it !in revealedIndices }
    val remainingUnrevealedCount: Int get() = (pulledCards.size - revealedIndices.size).coerceAtLeast(0)
}

data class MegaCollectorUiState(
    val wallet: PlayerWalletState = PlayerWalletState(),
    val catalog: List<SkinCatalogItem> = emptyList(),
    val ownedCards: List<OwnedCard> = emptyList(),
    val tradeListings: List<TradeListing> = emptyList(),
    val openP2PListings: List<P2PTradeListing> = emptyList(),
    val historyEvents: List<CollectorHistoryEvent> = emptyList(),
    val viewingTraderProfile: CollectorPublicProfile? = null,
    val quests: List<QuestUiItem> = emptyList(),
    val achievements: List<AchievementBadgeUi> = emptyList(),
    val syncStatus: CatalogSyncStatus = CatalogSyncStatus.Idle,
    val isDailyRewardClaimable: Boolean = true,
    val nextDailyDayIndex: Int = 1,
    val scratcherStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val prismStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val wheelStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val mysteryPickStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val memoryMatchStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val quizCooldownRemainingMs: Long = 0L,
    val isPreloading: Boolean = true,
    val preloadProgress: Float = 0f,
    val packSession: PackOpeningSession? = null,
    val isOpeningPack: Boolean = false,
    val decodedSlipPreview: DecodedTradeSlip? = null,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MegaCollectorDatabase.getInstance(application)
    private val prefs = PlayerPreferencesStore(application)
    private val initialBootTheme = prefs.readBootThemeSync()
    val repository = CollectorRepository(db.collectorDao(), prefs)
    val soundEngine = PackSoundEngine(application)
    private val firestoreService = FirestoreCollectorService(application)

    private val _activeUser = MutableStateFlow<AuthenticatedUser?>(null)
    private val _syncStatus = MutableStateFlow<CatalogSyncStatus>(CatalogSyncStatus.Idle)
    private val _packSession = MutableStateFlow<PackOpeningSession?>(null)
    private val _isOpeningPack = MutableStateFlow(false)
    private val _decodedSlipPreview = MutableStateFlow<DecodedTradeSlip?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _openP2PListings = MutableStateFlow<List<P2PTradeListing>>(emptyList())
    private val _historyEvents = MutableStateFlow<List<CollectorHistoryEvent>>(emptyList())
    private val _viewingTraderProfile = MutableStateFlow<CollectorPublicProfile?>(null)
    private val _isPreloading = MutableStateFlow(!isRobolectricEnv())
    private val _preloadProgress = MutableStateFlow(if (isRobolectricEnv()) 1f else 0.08f)

    private var lastSatinThemeId: String? = null
    private var historyListenerJob: Job? = null
    private var openP2PJob: Job? = null
    private var sellerP2PJob: Job? = null
    private val settlingListingIds = HashSet<String>()

    private val clockTickFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    val dailyRewardTiers: List<DailyRewardTier> = repository.dailyRewardTiers

    private data class EphemeralState(
        val syncStatus: CatalogSyncStatus,
        val packSession: PackOpeningSession?,
        val isOpeningPack: Boolean,
        val decodedSlip: DecodedTradeSlip?,
        val snackbar: String?,
        val openP2PListings: List<P2PTradeListing>,
        val historyEvents: List<CollectorHistoryEvent>,
        val viewingTraderProfile: CollectorPublicProfile?,
        val isPreloading: Boolean,
        val preloadProgress: Float,
        val nowMs: Long
    )

    private val preloadFlow = combine(_isPreloading, _preloadProgress) { loading, prog ->
        loading to prog
    }

    private val ephemeralFlow = combine(
        _syncStatus,
        _packSession,
        _isOpeningPack,
        _decodedSlipPreview,
        _snackbarMessage,
        _openP2PListings,
        _historyEvents,
        _viewingTraderProfile,
        preloadFlow,
        clockTickFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val preloadPair = values[8] as Pair<Boolean, Float>
        EphemeralState(
            syncStatus = values[0] as CatalogSyncStatus,
            packSession = values[1] as PackOpeningSession?,
            isOpeningPack = values[2] as Boolean,
            decodedSlip = values[3] as DecodedTradeSlip?,
            snackbar = values[4] as String?,
            openP2PListings = values[5] as List<P2PTradeListing>,
            historyEvents = values[6] as List<CollectorHistoryEvent>,
            viewingTraderProfile = values[7] as CollectorPublicProfile?,
            isPreloading = preloadPair.first,
            preloadProgress = preloadPair.second,
            nowMs = values[9] as Long
        )
    }

    val uiState: StateFlow<MegaCollectorUiState> = combine(
        repository.walletFlow,
        repository.catalogFlow,
        repository.ownedCardsFlow,
        repository.tradeListingsFlow,
        ephemeralFlow
    ) { wallet, catalog, owned, trades, eph ->
        soundEngine.setMuted(wallet.soundMuted)
        val today = repository.currentLocalEpochDay()
        val claimable = wallet.lastDailyClaimEpochDay < today
        val isConsecutive = wallet.lastDailyClaimEpochDay == today - 1L
        val nextDay = if (claimable) {
            if (isConsecutive) (wallet.dailyStreakCount % 7) + 1 else 1
        } else {
            wallet.dailyStreakCount.coerceIn(1, 7)
        }

        val scratcherStatus = wallet.currentScratcherStatus(eph.nowMs)
        val prismStatus = wallet.currentPrismStatus(eph.nowMs)
        val wheelStatus = wallet.currentWheelStatus(eph.nowMs)
        val mysteryPickStatus = wallet.currentMysteryPickStatus(eph.nowMs)
        val memoryMatchStatus = wallet.currentMemoryMatchStatus(eph.nowMs)
        val quizCooldown = wallet.currentQuizCooldownRemainingMs(eph.nowMs)

        MegaCollectorUiState(
            wallet = wallet,
            catalog = catalog,
            ownedCards = owned,
            tradeListings = trades,
            openP2PListings = eph.openP2PListings,
            historyEvents = eph.historyEvents,
            viewingTraderProfile = eph.viewingTraderProfile,
            quests = repository.buildQuestItems(wallet),
            achievements = AchievementCatalog.evaluate(wallet, owned),
            syncStatus = if (catalog.isNotEmpty() && eph.syncStatus is CatalogSyncStatus.Idle) {
                CatalogSyncStatus.Synced(catalog.size)
            } else {
                eph.syncStatus
            },
            isDailyRewardClaimable = claimable,
            nextDailyDayIndex = nextDay,
            scratcherStatus = scratcherStatus,
            prismStatus = prismStatus,
            wheelStatus = wheelStatus,
            mysteryPickStatus = mysteryPickStatus,
            memoryMatchStatus = memoryMatchStatus,
            quizCooldownRemainingMs = quizCooldown,
            isPreloading = eph.isPreloading,
            preloadProgress = eph.preloadProgress,
            packSession = eph.packSession,
            isOpeningPack = eph.isOpeningPack,
            decodedSlipPreview = eph.decodedSlip,
            snackbarMessage = eph.snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MegaCollectorUiState(
            wallet = PlayerWalletState(
                themeAccentId = initialBootTheme.themeAccentId,
                darkModeEnabled = initialBootTheme.darkModeEnabled,
                vibrationEnabled = initialBootTheme.vibrationEnabled,
                soundMuted = initialBootTheme.soundMuted
            ),
            isPreloading = !isRobolectricEnv(),
            preloadProgress = if (isRobolectricEnv()) 1f else 0.08f
        )
    )

    init {
        viewModelScope.launch {
            runStartupPreload()
        }
    }

    private suspend fun runStartupPreload() {
        val ctx = getApplication<Application>()
        _preloadProgress.value = 0.12f
        val didReset = repository.applyFreshStartIfNeeded()
        if (didReset) {
            syncCloudProfileNow()
        }
        repository.syncQuestPeriodsOnLaunch()
        val count = db.collectorDao().getCatalogCount()
        val wallet = repository.walletFlow.first()
        soundEngine.setMuted(wallet.soundMuted)
        _preloadProgress.value = 0.24f

        // Decode bundled artwork drawables into memory caches off the main thread
        withContext(Dispatchers.IO) {
            val drawables = listOf(
                R.drawable.img_pack_mythic,
                R.drawable.img_arena_banner,
                R.drawable.bg_mlbb_card_art
            )
            val opts = BitmapFactory.Options().apply { inPreferredConfig = android.graphics.Bitmap.Config.RGB_565 }
            drawables.forEachIndexed { idx, resId ->
                runCatching {
                    BitmapFactory.decodeResource(ctx.resources, resId, opts)?.recycle()
                }
                _preloadProgress.value = 0.24f + 0.14f * ((idx + 1).toFloat() / drawables.size)
            }
        }

        // 24-hour catalog sync window check
        val elapsedSinceSync = System.currentTimeMillis() - wallet.catalogLastSyncedMs
        val within24h = wallet.catalogLastSyncedMs > 0L &&
            elapsedSinceSync in 0L until 86_400_000L &&
            wallet.catalogSchemaVersion >= PlayerPreferencesStore.CURRENT_CATALOG_SCHEMA_VERSION &&
            count > 0

        if (within24h) {
            _syncStatus.value = CatalogSyncStatus.Synced(count)
        } else if (!isRobolectricEnv()) {
            _syncStatus.value = CatalogSyncStatus.Syncing(0, "Connecting to mlbbdex.com...")
            repository.syncSkinsFromApi { synced, stage ->
                _syncStatus.value = CatalogSyncStatus.Syncing(synced, stage)
            }
                .onSuccess { syncedCount ->
                    _syncStatus.value = CatalogSyncStatus.Synced(syncedCount)
                }
                .onFailure { err ->
                    _syncStatus.value = CatalogSyncStatus.Error(
                        err.localizedMessage ?: "Could not reach mlbbdex.com/api/v1/skins/"
                    )
                }
        } else {
            syncCatalog()
        }
        _preloadProgress.value = 0.45f

        // Concrete parallel Coil cache pre-warming with exact card & square transform keys
        val ownedUrls = db.collectorDao().getAllOwnedOnce().map { it.imageUrl }
        val catalogUrls = db.collectorDao().getAllCatalogOnce()
            .asSequence()
            .map { it.imageUrl }
            .filter { it.isNotBlank() }
            .take(18)
            .toList()
        val preloadUrls = (ownedUrls + catalogUrls)
            .asSequence()
            .filter { it.isNotBlank() }
            .distinct()
            .take(18)
            .toList()

        if (!isRobolectricEnv() && preloadUrls.isNotEmpty()) {
            val loader = Coil.imageLoader(ctx)
            val completed = AtomicInteger(0)
            val total = preloadUrls.size
            val semaphore = Semaphore(6)
            withTimeoutOrNull(4200L) {
                coroutineScope {
                    preloadUrls.map { url ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                runCatching {
                                    loader.execute(buildMlbbSquareImageRequest(ctx, url, crossfadeMs = 0))
                                }
                                runCatching {
                                    loader.execute(buildMlbbCardImageRequest(ctx, url, crossfadeMs = 0))
                                }
                                val done = completed.incrementAndGet()
                                _preloadProgress.value =
                                    (0.45f + 0.48f * (done.toFloat() / total.coerceAtLeast(1)))
                                        .coerceAtMost(0.95f)
                            }
                        }
                    }.awaitAll()
                }
            }
        }

        // Smooth finish of the loading progress bar before revealing studio
        if (!isRobolectricEnv()) {
            val steps = listOf(0.88f, 0.95f, 1.0f)
            for (target in steps) {
                if (_preloadProgress.value < target) {
                    _preloadProgress.value = target
                    delay(65L)
                }
            }
            delay(90L)
        } else {
            _preloadProgress.value = 1f
        }
        _isPreloading.value = false

        // Continue silent background warming for additional owned/catalog cards
        warmImageUrls(ownedUrls.drop(18))
    }

    private fun isRobolectricEnv(): Boolean =
        "robolectric".equals(Build.FINGERPRINT, ignoreCase = true)

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }

    fun bindAuthenticatedUser(user: AuthenticatedUser?) {
        val prevUid = _activeUser.value?.uid
        _activeUser.value = user
        if (prevUid == user?.uid && historyListenerJob != null) return

        historyListenerJob?.cancel()
        openP2PJob?.cancel()
        sellerP2PJob?.cancel()
        _historyEvents.value = emptyList()
        _openP2PListings.value = emptyList()
        _viewingTraderProfile.value = null

        if (user == null) return

        viewModelScope.launch {
            repository.seedAliasFromGoogleIfDefault(user.displayName)
            syncCloudProfileNow()
        }

        historyListenerJob = viewModelScope.launch {
            firestoreService.observeHistoryEvents(user.uid).collect { events ->
                _historyEvents.value = events
            }
        }

        openP2PJob = viewModelScope.launch {
            firestoreService.observeOpenP2PListings().collect { list ->
                _openP2PListings.value = list
            }
        }

        sellerP2PJob = viewModelScope.launch {
            firestoreService.observeSellerP2PListings(user.uid).collect { myListings ->
                for (listing in myListings) {
                    if (listing.status == "COMPLETED" && !listing.sellerSettled && settlingListingIds.add(listing.id)) {
                        repository.settleSoldP2PListing(listing)
                            .onSuccess { received ->
                                firestoreService.markListingSellerSettled(listing.id)
                                recordCloudHistory(
                                    type = CollectorHistoryType.TRADE,
                                    title = "P2P Listing Accepted!",
                                    detail = "Traded ${listing.skinName} with ${listing.acceptedByName} for ${received.skinName}",
                                    rarityName = received.rarity.name
                                )
                                syncCloudProfileNow()
                                _snackbarMessage.value = "${listing.acceptedByName} swapped for your ${listing.skinName}! Received ${received.skinName}."
                            }
                            .onFailure {
                                settlingListingIds.remove(listing.id)
                            }
                    }
                }
            }
        }
    }

    private fun warmImageUrls(urls: List<String>) {
        val ctx = getApplication<Application>()
        val loader = Coil.imageLoader(ctx)
        urls.asSequence()
            .filter { it.isNotBlank() }
            .distinct()
            .take(24)
            .forEach { url ->
                loader.enqueue(buildMlbbSquareImageRequest(ctx, url, crossfadeMs = 0))
                loader.enqueue(buildMlbbCardImageRequest(ctx, url, crossfadeMs = 0))
            }
    }

    private fun syncCloudProfileNow() {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val wallet = repository.walletFlow.first()
            val owned = db.collectorDao().getAllOwnedOnce().map { it.toDomain() }
            val prestige = owned.sumOf { it.prestigeScore }
            val rank = MlbbCollectionTier.fromPrestige(prestige).title
            val pinned = AchievementCatalog.evaluate(wallet, owned)
                .filter { it.isPinned }
                .map { it.definition.id }
            firestoreService.syncUserAndPublicProfile(
                user = user,
                wallet = wallet,
                ownedCards = owned,
                collectorRank = rank,
                pinnedBadgeIds = pinned
            )
        }
    }

    private fun recordCloudHistory(
        type: CollectorHistoryType,
        title: String,
        detail: String,
        rarityName: String = "",
        coinDelta: Int = 0,
        diamondDelta: Int = 0
    ) {
        val uid = _activeUser.value?.uid ?: return
        viewModelScope.launch {
            firestoreService.recordHistoryEvent(
                uid = uid,
                eventType = type,
                title = title,
                detail = detail,
                rarityName = rarityName,
                coinDelta = coinDelta,
                diamondDelta = diamondDelta
            )
        }
    }

    fun playClick() = soundEngine.playClick()

    fun playWheelTick(pitch: Float = 1.0f) = soundEngine.playWheelTick(pitch)

    fun playPlasticCrinkle() = soundEngine.playPlasticCrinkle()

    fun playPackTear() = soundEngine.playPackTear()

    fun playCardFlipSfx(rarity: SkinRarity, isMisprint: Boolean) =
        soundEngine.playCardFlip(rarity, isMisprint)

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val muted = !enabled
            repository.setSoundMuted(muted)
            soundEngine.setMuted(muted)
            if (enabled) soundEngine.playClick()
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        soundEngine.playClick()
        viewModelScope.launch { repository.setDarkModeEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        soundEngine.playClick()
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }

    fun setThemeAccent(accent: ThemeAccentOption) {
        soundEngine.playClick()
        viewModelScope.launch { repository.setThemeAccentId(accent.id) }
    }

    fun observeOwnedCard(instanceId: Long): Flow<OwnedCard?> =
        repository.observeCard(instanceId)

    fun syncCatalog() {
        if (_syncStatus.value is CatalogSyncStatus.Syncing) return
        viewModelScope.launch {
            _syncStatus.value = CatalogSyncStatus.Syncing(0, "Connecting to mlbbdex.com...")
            repository.syncSkinsFromApi { count, stage ->
                _syncStatus.value = CatalogSyncStatus.Syncing(count, stage)
            }
                .onSuccess { count ->
                    _syncStatus.value = CatalogSyncStatus.Synced(count)
                    _snackbarMessage.value = "Synced $count skins (cached for 24h)!"
                    val sampleUrls = db.collectorDao().getAllCatalogOnce().take(20).map { it.imageUrl }
                    warmImageUrls(sampleUrls)
                }
                .onFailure { err ->
                    _syncStatus.value = CatalogSyncStatus.Error(
                        err.localizedMessage ?: "Could not reach mlbbdex.com/api/v1/skins/"
                    )
                }
        }
    }

    fun openPackBundle(
        bundle: PackBundleOption,
        payWithDiamonds: Boolean = false,
        useFreeTicket: Boolean = false,
        preferredTheme: PackSatinTheme? = null
    ) {
        if (_isOpeningPack.value || _packSession.value != null) return
        soundEngine.playClick()
        viewModelScope.launch {
            val currentWallet = repository.walletFlow.first()
            repository.verifyPackReadyToRip(bundle, currentWallet, payWithDiamonds, useFreeTicket)
                .onSuccess { effectiveBundle ->
                    val theme = preferredTheme ?: PackSatinTheme.random(lastSatinThemeId)
                    lastSatinThemeId = theme.id
                    _packSession.value = PackOpeningSession(
                        bundle = effectiveBundle,
                        wrapperTheme = theme,
                        pendingPurchase = PendingPackPurchase(
                            bundle = effectiveBundle,
                            payWithDiamonds = payWithDiamonds,
                            useFreeTicket = useFreeTicket
                        ),
                        pulledCards = emptyList(),
                        isRipped = false,
                        isCommittingRip = false
                    )
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Unable to prepare pack"
                }
        }
    }

    fun ripAndCommitPack() {
        val session = _packSession.value ?: return
        if (session.isRipped || session.isCommittingRip || _isOpeningPack.value) return
        val purchase = session.pendingPurchase ?: return

        viewModelScope.launch {
            _isOpeningPack.value = true
            _packSession.update { it?.copy(isCommittingRip = true) }

            val currentWallet = repository.walletFlow.first()
            repository.openPackBundle(
                bundle = purchase.bundle,
                wallet = currentWallet,
                payWithDiamonds = purchase.payWithDiamonds,
                useFreeTicket = purchase.useFreeTicket
            )
                .onSuccess { cards ->
                    soundEngine.playPackTear()
                    warmImageUrls(cards.map { it.imageUrl })
                    _packSession.update { current ->
                        current?.copy(
                            pulledCards = cards,
                            isRipped = true,
                            isCommittingRip = false,
                            pendingPurchase = null,
                            revealedIndices = emptySet(),
                            focusedRevealedIndex = null,
                            showSummaryGrid = false
                        )
                    }
                    val best = cards.maxByOrNull { it.rarity.tierRank + if (it.isMisprint) 10 else 0 }
                    if (best != null) {
                        recordCloudHistory(
                            type = CollectorHistoryType.PULL,
                            title = "Ripped ${purchase.bundle.packCount}x Booster (${cards.size} cards)",
                            detail = "Best hit: ${best.heroName} — ${best.skinName} (${best.rarity.displayName}${if (best.isMisprint) " 1/1" else ""})",
                            rarityName = best.rarity.name
                        )
                    }
                    syncCloudProfileNow()
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Unable to rip pack"
                    _packSession.value = null
                }
            _isOpeningPack.value = false
        }
    }

    fun exchangeDiamondsForCoins(option: DiamondExchangeOption) {
        soundEngine.playClick()
        viewModelScope.launch {
            repository.exchangeDiamondsForCoins(option)
                .onSuccess {
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Diamond Exchange",
                        detail = "Converted ${option.diamondCost} Diamonds",
                        coinDelta = option.coinReward,
                        diamondDelta = -option.diamondCost
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Redeemed ${option.diamondCost} 💎 for +${option.coinReward} 🪙 Coins!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Not enough Diamonds."
                }
        }
    }

    fun claimScratcherPrize(coinPrize: Int) {
        viewModelScope.launch {
            repository.consumeScratcherChargeAndAwardCoins(coinPrize)
                .onSuccess { coins ->
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Dawn Scratcher",
                        detail = "Scratch ticket reward",
                        coinDelta = coins
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Scratcher Won +$coins 🪙 Coins!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Scratcher recharging!"
                }
        }
    }

    fun awardPrismDiamonds(diamondsEarned: Int, winLabel: String) {
        viewModelScope.launch {
            repository.consumePrismChargeAndAwardDiamonds(diamondsEarned)
                .onSuccess { earned ->
                    if (earned >= 4) soundEngine.playRewardChime() else soundEngine.playClick()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Prism Strike",
                        detail = winLabel,
                        diamondDelta = earned
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "$winLabel +$earned 💎 Diamonds!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Prism Strike recharging!"
                }
        }
    }

    fun spinWheelPrize(
        label: String,
        coinReward: Int,
        diamondReward: Int,
        freePackReward: Int
    ) {
        viewModelScope.launch {
            repository.consumeWheelSpinAndAward(coinReward, diamondReward, freePackReward)
                .onSuccess {
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Prize Wheel",
                        detail = "Won $label",
                        coinDelta = coinReward,
                        diamondDelta = diamondReward
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Wheel Prize: $label!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Prize Wheel recharging!"
                }
        }
    }

    fun completeHourlyQuiz(score: Int, diamondsEarned: Int) {
        viewModelScope.launch {
            repository.completeHourlyQuizAndAwardDiamonds(diamondsEarned)
                .onSuccess { earned ->
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Hourly MLBB Quiz",
                        detail = "Scored $score/${QuizQuestionBank.QUIZ_QUESTION_COUNT}",
                        diamondDelta = earned
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value =
                        "Quiz Complete ($score/${QuizQuestionBank.QUIZ_QUESTION_COUNT}): +$earned 💎 Diamonds!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Quiz on hourly cooldown."
                }
        }
    }

    fun claimMysteryPickPrize(
        label: String,
        coinReward: Int,
        diamondReward: Int,
        freePackReward: Int
    ) {
        viewModelScope.launch {
            repository.consumeMysteryPickChargeAndAward(coinReward, diamondReward, freePackReward)
                .onSuccess {
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Mystery Vault Pick",
                        detail = "Opened chest: $label",
                        coinDelta = coinReward,
                        diamondDelta = diamondReward
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Mystery Chest: $label!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Mystery Vault recharging!"
                }
        }
    }

    fun completeMemoryMatch(moves: Int, coinReward: Int, diamondReward: Int) {
        viewModelScope.launch {
            repository.consumeMemoryMatchChargeAndAward(coinReward, diamondReward)
                .onSuccess {
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Memory Blitz",
                        detail = "Matched all pairs in $moves moves",
                        coinDelta = coinReward,
                        diamondDelta = diamondReward
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Memory Blitz ($moves moves): +$coinReward 🪙 +$diamondReward 💎!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Memory Blitz recharging!"
                }
        }
    }

    fun claimQuest(quest: QuestUiItem) {
        soundEngine.playClick()
        viewModelScope.launch {
            repository.claimQuest(quest)
                .onSuccess { def ->
                    soundEngine.playRewardChime()
                    val parts = buildList {
                        if (def.coinReward > 0) add("+${def.coinReward} 🪙")
                        if (def.diamondReward > 0) add("+${def.diamondReward} 💎")
                        if (def.freePackReward > 0) add("+${def.freePackReward} Free Pack")
                    }
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Quest Claimed: ${parts.joinToString(" • ")}"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Quest not ready."
                }
        }
    }

    fun togglePinnedBadge(badge: AchievementBadgeUi) {
        if (!badge.isUnlocked) return
        soundEngine.playClick()
        viewModelScope.launch {
            val pinned = repository.togglePinnedBadge(badge.definition.id)
            syncCloudProfileNow()
            _snackbarMessage.value = if (pinned) {
                "Pinned \"${badge.definition.title}\" to your Profile showcase!"
            } else {
                "Unpinned \"${badge.definition.title}\"."
            }
        }
    }

    fun revealCardAt(index: Int) {
        _packSession.update { session ->
            if (session == null || session.pulledCards.isEmpty()) return@update session
            val validIdx = index.coerceIn(0, session.pulledCards.lastIndex)
            session.copy(
                revealedIndices = session.revealedIndices + validIdx,
                focusedRevealedIndex = validIdx
            )
        }
    }

    fun advanceToNextFaceDownCard() {
        soundEngine.playClick()
        _packSession.update { session ->
            if (session == null) return@update null
            if (session.allRevealed) {
                session.copy(focusedRevealedIndex = null, showSummaryGrid = true)
            } else {
                session.copy(focusedRevealedIndex = null)
            }
        }
    }

    fun showPulledSummaryGrid() {
        soundEngine.playClick()
        _packSession.update { session ->
            if (session == null || !session.isRipped) return@update session
            session.copy(
                revealedIndices = session.pulledCards.indices.toSet(),
                focusedRevealedIndex = null,
                showSummaryGrid = true
            )
        }
    }

    fun focusRevealedCard(index: Int) {
        soundEngine.playClick()
        _packSession.update { session ->
            if (session == null || index !in session.revealedIndices) return@update session
            session.copy(focusedRevealedIndex = index, showSummaryGrid = false)
        }
    }

    fun closePackSession() {
        soundEngine.playClick()
        _packSession.value = null
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val currentWallet = repository.walletFlow.first()
            repository.claimDailyReward(currentWallet)
                .onSuccess { tier ->
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.MINIGAME,
                        title = "Daily Streak Day ${tier.dayIndex}",
                        detail = "Daily login reward",
                        coinDelta = tier.coinAmount,
                        diamondDelta = tier.bonusDiamonds
                    )
                    syncCloudProfileNow()
                    val extras = buildList {
                        add("+${tier.coinAmount} 🪙")
                        if (tier.bonusDiamonds > 0) add("+${tier.bonusDiamonds} 💎")
                        if (tier.bonusFreePacks > 0) add("+${tier.bonusFreePacks} Free Pack")
                    }
                    _snackbarMessage.value = "Day ${tier.dayIndex} Reward: ${extras.joinToString(" ")}"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Daily reward already claimed."
                }
        }
    }

    fun recycleSingleCard(card: OwnedCard, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.recycleSingleCard(card)
                .onSuccess { coins ->
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.RECYCLE,
                        title = "Forged ${card.skinName}",
                        detail = "${card.heroName} (${card.rarity.displayName})",
                        rarityName = card.rarity.name,
                        coinDelta = coins
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Recycled ${card.skinName} for +$coins 🪙 Coins!"
                    onSuccess()
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to recycle card"
                }
        }
    }

    fun recycleAllDuplicates() {
        viewModelScope.launch {
            repository.recycleAllDuplicates()
                .onSuccess { (count, coins) ->
                    soundEngine.playRewardChime()
                    recordCloudHistory(
                        type = CollectorHistoryType.RECYCLE,
                        title = "Bulk Recycled $count Duplicates",
                        detail = "Vault duplicate forge",
                        coinDelta = coins
                    )
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Forged $count duplicate cards into +$coins 🪙 Coins!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "No duplicates to recycle."
                }
        }
    }

    fun createTradeListing(
        card: OwnedCard,
        seekingNote: String,
        goldSweetener: Int,
        onCreated: (TradeListing) -> Unit = {}
    ) {
        soundEngine.playClick()
        viewModelScope.launch {
            val currentState = uiState.value
            val alias = currentState.wallet.collectorAlias
            val prestige = currentState.ownedCards.sumOf { it.prestigeScore }
            val uniqueCount = currentState.ownedCards.map { it.catalogId }.distinct().size
            val pinnedIds = currentState.achievements.filter { it.isPinned }.map { it.definition.id }
            repository.createTradeListing(
                card = card,
                seekingNote = seekingNote,
                senderAlias = alias,
                goldSweetener = goldSweetener,
                senderPrestige = prestige,
                senderCardCount = uniqueCount,
                senderBadgeIds = pinnedIds
            )
                .onSuccess { listing ->
                    soundEngine.playRewardChime()
                    val user = _activeUser.value
                    if (user != null) {
                        firestoreService.publishP2PListing(
                            user = user,
                            wallet = currentState.wallet,
                            card = card,
                            seekingNote = seekingNote,
                            tradeCode = listing.tradeCode
                        )
                    }
                    syncCloudProfileNow()
                    _snackbarMessage.value = "Published live P2P listing & Trade Slip for ${card.skinName}!"
                    onCreated(listing)
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not create Trade Slip."
                }
        }
    }

    fun acceptP2PListing(listing: P2PTradeListing, offeredCard: OwnedCard) {
        val user = _activeUser.value ?: run {
            _snackbarMessage.value = "Please sign in with Google to accept P2P trades."
            return
        }
        viewModelScope.launch {
            val wallet = repository.walletFlow.first()
            val buyerName = wallet.collectorAlias.ifBlank { user.displayName }
            firestoreService.acceptP2PListingTransaction(
                listingId = listing.id,
                buyerUid = user.uid,
                buyerName = buyerName,
                offeredCard = offeredCard
            )
                .onSuccess {
                    repository.completeDirectP2PSwap(listing, offeredCard)
                        .onSuccess { received ->
                            soundEngine.playRewardChime()
                            recordCloudHistory(
                                type = CollectorHistoryType.TRADE,
                                title = "P2P Swap with ${listing.sellerName}",
                                detail = "Gave ${offeredCard.skinName}, received ${received.skinName}",
                                rarityName = received.rarity.name
                            )
                            syncCloudProfileNow()
                            _snackbarMessage.value = "P2P Swap Complete! Received ${received.skinName}!"
                        }
                        .onFailure { err ->
                            _snackbarMessage.value = err.localizedMessage ?: "Local swap failed."
                        }
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Trade listing is no longer available."
                }
        }
    }

    fun cancelCloudP2PListing(listing: P2PTradeListing) {
        viewModelScope.launch {
            firestoreService.cancelP2PListing(listing.id)
                .onSuccess {
                    _snackbarMessage.value = "Removed ${listing.skinName} from live P2P marketplace."
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to cancel cloud listing."
                }
        }
    }

    fun viewTraderProfile(targetUid: String) {
        viewModelScope.launch {
            firestoreService.fetchPublicProfile(targetUid)
                .onSuccess { profile ->
                    if (profile != null) {
                        _viewingTraderProfile.value = profile
                    } else {
                        _snackbarMessage.value = "Collector profile is not published yet."
                    }
                }
                .onFailure {
                    _snackbarMessage.value = "Could not load collector profile."
                }
        }
    }

    fun dismissViewingProfile() {
        _viewingTraderProfile.value = null
    }

    fun cancelTradeListing(listing: TradeListing) {
        soundEngine.playClick()
        viewModelScope.launch {
            repository.cancelTradeListing(listing)
                .onSuccess {
                    _snackbarMessage.value = "Removed ${listing.skinName} from Trade Desk."
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to cancel listing."
                }
        }
    }

    fun decodeTradeSlip(code: String) {
        soundEngine.playClick()
        repository.decodeTradeSlip(code)
            .onSuccess { slip -> _decodedSlipPreview.value = slip }
            .onFailure { err -> _snackbarMessage.value = err.localizedMessage ?: "Invalid Trade Slip code." }
    }

    fun clearDecodedSlip() {
        soundEngine.playClick()
        _decodedSlipPreview.value = null
    }

    fun completeTradeWithSlip(slip: DecodedTradeSlip, offeredCard: OwnedCard) {
        viewModelScope.launch {
            repository.completeTradeWithSlip(slip, offeredCard)
                .onSuccess { received ->
                    soundEngine.playRewardChime()
                    _decodedSlipPreview.value = null
                    recordCloudHistory(
                        type = CollectorHistoryType.TRADE,
                        title = "Trade Slip with ${slip.senderAlias}",
                        detail = "Gave ${offeredCard.skinName}, received ${received.skinName}",
                        rarityName = received.rarity.name,
                        coinDelta = slip.goldSweetener
                    )
                    syncCloudProfileNow()
                    val bonusMsg = if (slip.goldSweetener > 0) " (+${slip.goldSweetener} 🪙)" else ""
                    _snackbarMessage.value = "Trade Complete! Received ${received.skinName}$bonusMsg"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Trade failed."
                }
        }
    }

    fun updateAlias(newAlias: String) {
        soundEngine.playClick()
        viewModelScope.launch {
            repository.updateAlias(newAlias)
            syncCloudProfileNow()
            _snackbarMessage.value = "Collector tag updated!"
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}
