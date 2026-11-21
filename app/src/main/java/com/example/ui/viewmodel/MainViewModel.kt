package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PackSoundEngine
import com.example.data.local.MegaCollectorDatabase
import com.example.data.local.PlayerPreferencesStore
import com.example.data.local.PlayerWalletState
import com.example.data.repository.CollectorRepository
import com.example.model.DailyRewardTier
import com.example.model.DecodedTradeSlip
import com.example.model.DiamondExchangeOption
import com.example.model.OwnedCard
import com.example.model.PackBundleOption
import com.example.model.QuestUiItem
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.TradeListing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CatalogSyncStatus {
    data object Idle : CatalogSyncStatus
    data class Syncing(val count: Int = 0, val stage: String = "Syncing skins from mlbbdex.com...") : CatalogSyncStatus
    data class Synced(val count: Int) : CatalogSyncStatus
    data class Error(val message: String) : CatalogSyncStatus
}

data class PackOpeningSession(
    val bundle: PackBundleOption,
    val pulledCards: List<OwnedCard>,
    val isRipped: Boolean = false,
    val revealedIndices: Set<Int> = emptySet(),
    val focusedRevealedIndex: Int? = null,
    val showSummaryGrid: Boolean = false
) {
    val allRevealed: Boolean get() = revealedIndices.size >= pulledCards.size
    val nextUnrevealedIndex: Int? get() = pulledCards.indices.firstOrNull { it !in revealedIndices }
    val remainingUnrevealedCount: Int get() = (pulledCards.size - revealedIndices.size).coerceAtLeast(0)
}

data class MegaCollectorUiState(
    val wallet: PlayerWalletState = PlayerWalletState(),
    val catalog: List<SkinCatalogItem> = emptyList(),
    val ownedCards: List<OwnedCard> = emptyList(),
    val tradeListings: List<TradeListing> = emptyList(),
    val quests: List<QuestUiItem> = emptyList(),
    val syncStatus: CatalogSyncStatus = CatalogSyncStatus.Idle,
    val isDailyRewardClaimable: Boolean = true,
    val nextDailyDayIndex: Int = 1,
    val dailyScratchersRemaining: Int = PlayerWalletState.MAX_DAILY_SCRATCHERS,
    val packSession: PackOpeningSession? = null,
    val isOpeningPack: Boolean = false,
    val decodedSlipPreview: DecodedTradeSlip? = null,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MegaCollectorDatabase.getInstance(application)
    private val prefs = PlayerPreferencesStore(application)
    val repository = CollectorRepository(db.collectorDao(), prefs)
    val soundEngine = PackSoundEngine(application)

    private val _syncStatus = MutableStateFlow<CatalogSyncStatus>(CatalogSyncStatus.Idle)
    private val _packSession = MutableStateFlow<PackOpeningSession?>(null)
    private val _isOpeningPack = MutableStateFlow(false)
    private val _decodedSlipPreview = MutableStateFlow<DecodedTradeSlip?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val dailyRewardTiers: List<DailyRewardTier> = repository.dailyRewardTiers

    private data class EphemeralState(
        val syncStatus: CatalogSyncStatus,
        val packSession: PackOpeningSession?,
        val isOpeningPack: Boolean,
        val decodedSlip: DecodedTradeSlip?,
        val snackbar: String?
    )

    private val ephemeralFlow = combine(
        _syncStatus,
        _packSession,
        _isOpeningPack,
        _decodedSlipPreview,
        _snackbarMessage
    ) { sync, session, opening, slip, snack ->
        EphemeralState(sync, session, opening, slip, snack)
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

        val scratchersLeft = if (wallet.lastScratcherEpochDay < today) {
            PlayerWalletState.MAX_DAILY_SCRATCHERS
        } else {
            wallet.dailyScratchersRemaining
        }

        MegaCollectorUiState(
            wallet = wallet,
            catalog = catalog,
            ownedCards = owned,
            tradeListings = trades,
            quests = repository.buildQuestItems(wallet),
            syncStatus = if (catalog.isNotEmpty() && eph.syncStatus is CatalogSyncStatus.Idle) {
                CatalogSyncStatus.Synced(catalog.size)
            } else {
                eph.syncStatus
            },
            isDailyRewardClaimable = claimable,
            nextDailyDayIndex = nextDay,
            dailyScratchersRemaining = scratchersLeft,
            packSession = eph.packSession,
            isOpeningPack = eph.isOpeningPack,
            decodedSlipPreview = eph.decodedSlip,
            snackbarMessage = eph.snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MegaCollectorUiState()
    )

    init {
        viewModelScope.launch {
            repository.syncQuestPeriodsOnLaunch()
            val count = db.collectorDao().getCatalogCount()
            val wallet = repository.walletFlow.first()
            soundEngine.setMuted(wallet.soundMuted)
            if (count < 300 || wallet.catalogSchemaVersion < PlayerPreferencesStore.CURRENT_CATALOG_SCHEMA_VERSION) {
                syncCatalog()
            } else {
                _syncStatus.value = CatalogSyncStatus.Synced(count)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }

    fun playClick() = soundEngine.playClick()

    fun playPlasticCrinkle() = soundEngine.playPlasticCrinkle()

    fun playPackTear() = soundEngine.playPackTear()

    fun playCardFlipSfx(rarity: SkinRarity, isMisprint: Boolean) =
        soundEngine.playCardFlip(rarity, isMisprint)

    fun toggleSoundMute() {
        viewModelScope.launch {
            val current = repository.walletFlow.first().soundMuted
            val next = !current
            repository.setSoundMuted(next)
            soundEngine.setMuted(next)
            if (!next) {
                soundEngine.playClick()
            }
        }
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
                    _snackbarMessage.value = "Synced $count MOBA Legends skins from MLBBDex!"
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
        useFreeTicket: Boolean = false
    ) {
        if (_isOpeningPack.value) return
        soundEngine.playClick()
        viewModelScope.launch {
            _isOpeningPack.value = true
            val currentWallet = repository.walletFlow.first()
            repository.openPackBundle(bundle, currentWallet, payWithDiamonds, useFreeTicket)
                .onSuccess { cards ->
                    val actualBundle = if (useFreeTicket) PackBundleOption.SINGLE else bundle
                    _packSession.value = PackOpeningSession(
                        bundle = actualBundle,
                        pulledCards = cards,
                        isRipped = false,
                        revealedIndices = emptySet(),
                        focusedRevealedIndex = null,
                        showSummaryGrid = false
                    )
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Unable to open pack"
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
                    _snackbarMessage.value = "Redeemed ${option.diamondCost} 💎 for +${option.coinReward} 🪙 Coins!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Not enough Diamonds."
                }
        }
    }

    fun claimScratcherPrize(coinPrize: Int) {
        viewModelScope.launch {
            repository.recordDailyScratcherCoinPrize(coinPrize)
                .onSuccess { coins ->
                    soundEngine.playRewardChime()
                    _snackbarMessage.value = "Scratcher Won +$coins 🪙 Coins!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "No daily scratchers left today."
                }
        }
    }

    fun awardMinigameDiamonds(diamondsEarned: Int, winLabel: String) {
        viewModelScope.launch {
            repository.awardMinigameDiamonds(diamondsEarned)
            if (diamondsEarned >= 4) {
                soundEngine.playRewardChime()
            } else {
                soundEngine.playClick()
            }
            _snackbarMessage.value = "$winLabel +$diamondsEarned 💎 Diamonds!"
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
                    _snackbarMessage.value = "Quest Claimed: ${parts.joinToString(" • ")}"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Quest not ready."
                }
        }
    }

    fun markPackRipped() {
        _packSession.update { it?.copy(isRipped = true, focusedRevealedIndex = null) }
    }

    fun revealCardAt(index: Int) {
        _packSession.update { session ->
            if (session == null) return@update null
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
            session?.copy(
                isRipped = true,
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
            val alias = repository.walletFlow.first().collectorAlias
            repository.createTradeListing(card, seekingNote, alias, goldSweetener)
                .onSuccess { listing ->
                    soundEngine.playRewardChime()
                    _snackbarMessage.value = "Trade Slip minted for ${card.skinName}!"
                    onCreated(listing)
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not create Trade Slip."
                }
        }
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
            .onSuccess { slip ->
                _decodedSlipPreview.value = slip
            }
            .onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Invalid Trade Slip code."
            }
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
            _snackbarMessage.value = "Collector tag updated!"
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}
