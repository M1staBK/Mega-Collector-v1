package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MegaCollectorDatabase
import com.example.data.local.PlayerPreferencesStore
import com.example.data.local.PlayerWalletState
import com.example.data.repository.CollectorRepository
import com.example.model.BoosterPack
import com.example.model.DailyRewardTier
import com.example.model.DecodedTradeSlip
import com.example.model.OwnedCard
import com.example.model.SkinCatalogItem
import com.example.model.TradeListing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CatalogSyncStatus {
    data object Idle : CatalogSyncStatus
    data object Syncing : CatalogSyncStatus
    data class Synced(val count: Int) : CatalogSyncStatus
    data class Error(val message: String) : CatalogSyncStatus
}

data class PackOpeningSession(
    val pack: BoosterPack,
    val pulledCards: List<OwnedCard>,
    val isRipped: Boolean = false,
    val currentCardIndex: Int = 0,
    val revealedIndices: Set<Int> = emptySet()
) {
    val allRevealed: Boolean get() = revealedIndices.size >= pulledCards.size
}

data class MegaCollectorUiState(
    val wallet: PlayerWalletState = PlayerWalletState(),
    val catalog: List<SkinCatalogItem> = emptyList(),
    val ownedCards: List<OwnedCard> = emptyList(),
    val tradeListings: List<TradeListing> = emptyList(),
    val syncStatus: CatalogSyncStatus = CatalogSyncStatus.Idle,
    val isDailyRewardClaimable: Boolean = true,
    val nextDailyDayIndex: Int = 1,
    val packSession: PackOpeningSession? = null,
    val isOpeningPack: Boolean = false,
    val decodedSlipPreview: DecodedTradeSlip? = null,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MegaCollectorDatabase.getInstance(application)
    private val prefs = PlayerPreferencesStore(application)
    val repository = CollectorRepository(db.collectorDao(), prefs)

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
        val today = repository.currentLocalEpochDay()
        val claimable = wallet.lastDailyClaimEpochDay < today
        val isConsecutive = wallet.lastDailyClaimEpochDay == today - 1L
        val nextDay = if (claimable) {
            if (isConsecutive) (wallet.dailyStreakCount % 7) + 1 else 1
        } else {
            wallet.dailyStreakCount.coerceIn(1, 7)
        }

        MegaCollectorUiState(
            wallet = wallet,
            catalog = catalog,
            ownedCards = owned,
            tradeListings = trades,
            syncStatus = if (catalog.isNotEmpty() && eph.syncStatus is CatalogSyncStatus.Idle) {
                CatalogSyncStatus.Synced(catalog.size)
            } else {
                eph.syncStatus
            },
            isDailyRewardClaimable = claimable,
            nextDailyDayIndex = nextDay,
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
            val count = db.collectorDao().getCatalogCount()
            if (count == 0) {
                syncCatalog()
            } else {
                _syncStatus.value = CatalogSyncStatus.Synced(count)
            }
        }
    }

    fun observeOwnedCard(instanceId: Long): Flow<OwnedCard?> =
        repository.observeCard(instanceId)

    fun syncCatalog() {
        if (_syncStatus.value is CatalogSyncStatus.Syncing) return
        viewModelScope.launch {
            _syncStatus.value = CatalogSyncStatus.Syncing
            repository.syncSkinsFromApi()
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

    fun openPack(pack: BoosterPack, useFreeTicket: Boolean) {
        if (_isOpeningPack.value) return
        viewModelScope.launch {
            _isOpeningPack.value = true
            val currentWallet = repository.walletFlow.first()
            repository.openPack(pack, currentWallet, useFreeTicket)
                .onSuccess { cards ->
                    _packSession.value = PackOpeningSession(
                        pack = pack,
                        pulledCards = cards,
                        isRipped = false,
                        currentCardIndex = 0,
                        revealedIndices = emptySet()
                    )
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Unable to open pack"
                }
            _isOpeningPack.value = false
        }
    }

    fun markPackRipped() {
        _packSession.update { it?.copy(isRipped = true) }
    }

    fun revealCardAt(index: Int) {
        _packSession.update { session ->
            if (session == null) return@update null
            val nextRevealed = session.revealedIndices + index
            val nextFocus = (index + 1).coerceAtMost(session.pulledCards.lastIndex)
            session.copy(
                revealedIndices = nextRevealed,
                currentCardIndex = if (index in session.revealedIndices) session.currentCardIndex else nextFocus
            )
        }
    }

    fun selectPackCardIndex(index: Int) {
        _packSession.update { session ->
            session?.copy(currentCardIndex = index.coerceIn(0, session.pulledCards.lastIndex))
        }
    }

    fun revealAllCards() {
        _packSession.update { session ->
            session?.copy(
                isRipped = true,
                revealedIndices = session.pulledCards.indices.toSet()
            )
        }
    }

    fun closePackSession() {
        _packSession.value = null
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val currentWallet = repository.walletFlow.first()
            repository.claimDailyReward(currentWallet)
                .onSuccess { tier ->
                    val packBonus = tier.bonusPack?.let { " + Free ${it.title}!" } ?: ""
                    _snackbarMessage.value = "Day ${tier.dayIndex} Reward: +${tier.goldAmount} Gold$packBonus"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Daily reward already claimed."
                }
        }
    }

    fun recycleSingleCard(card: OwnedCard, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.recycleSingleCard(card)
                .onSuccess { gold ->
                    _snackbarMessage.value = "Recycled ${card.skinName} for +$gold Arena Gold!"
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
                .onSuccess { (count, gold) ->
                    _snackbarMessage.value = "Forged $count duplicate cards into +$gold Arena Gold!"
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
        viewModelScope.launch {
            val alias = repository.walletFlow.first().collectorAlias
            repository.createTradeListing(card, seekingNote, alias, goldSweetener)
                .onSuccess { listing ->
                    _snackbarMessage.value = "Trade Slip minted for ${card.skinName}!"
                    onCreated(listing)
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not create Trade Slip."
                }
        }
    }

    fun cancelTradeListing(listing: TradeListing) {
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
        repository.decodeTradeSlip(code)
            .onSuccess { slip ->
                _decodedSlipPreview.value = slip
            }
            .onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Invalid Trade Slip code."
            }
    }

    fun clearDecodedSlip() {
        _decodedSlipPreview.value = null
    }

    fun completeTradeWithSlip(slip: DecodedTradeSlip, offeredCard: OwnedCard) {
        viewModelScope.launch {
            repository.completeTradeWithSlip(slip, offeredCard)
                .onSuccess { received ->
                    _decodedSlipPreview.value = null
                    val bonusMsg = if (slip.goldSweetener > 0) " (+${slip.goldSweetener} Gold)" else ""
                    _snackbarMessage.value = "Trade Complete! Received ${received.skinName}$bonusMsg"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Trade failed."
                }
        }
    }

    fun updateAlias(newAlias: String) {
        viewModelScope.launch {
            repository.updateAlias(newAlias)
            _snackbarMessage.value = "Collector tag updated!"
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}
