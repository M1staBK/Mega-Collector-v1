package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PackSoundEngine
import com.example.data.local.HourlyChargeStatus
import com.example.data.local.MegaCollectorDatabase
import com.example.data.local.PlayerPreferencesStore
import com.example.data.local.PlayerWalletState
import com.example.data.repository.CollectorRepository
import com.example.model.AchievementBadgeUi
import com.example.model.AchievementCatalog
import com.example.model.BazaarMarketCatalog
import com.example.model.BazaarTraderListing
import com.example.model.DailyRewardTier
import com.example.model.DecodedTradeSlip
import com.example.model.DiamondExchangeOption
import com.example.model.OwnedCard
import com.example.model.PackBundleOption
import com.example.model.PackSatinTheme
import com.example.model.QuestUiItem
import com.example.model.QuizQuestionBank
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.TradeListing
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
    val bazaarListings: List<BazaarTraderListing> = emptyList(),
    val quests: List<QuestUiItem> = emptyList(),
    val achievements: List<AchievementBadgeUi> = emptyList(),
    val syncStatus: CatalogSyncStatus = CatalogSyncStatus.Idle,
    val isDailyRewardClaimable: Boolean = true,
    val nextDailyDayIndex: Int = 1,
    val scratcherStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val prismStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val wheelStatus: HourlyChargeStatus = HourlyChargeStatus(3, 0L),
    val quizCooldownRemainingMs: Long = 0L,
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
    private val _bazaarSeed = MutableStateFlow(0)

    private var lastSatinThemeId: String? = null

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
        val bazaarSeed: Int,
        val nowMs: Long
    )

    private val ephemeralFlow = combine(
        _syncStatus,
        _packSession,
        _isOpeningPack,
        _decodedSlipPreview,
        _snackbarMessage,
        _bazaarSeed
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        EphemeralState(
            syncStatus = values[0] as CatalogSyncStatus,
            packSession = values[1] as PackOpeningSession?,
            isOpeningPack = values[2] as Boolean,
            decodedSlip = values[3] as DecodedTradeSlip?,
            snackbar = values[4] as String?,
            bazaarSeed = values[5] as Int,
            nowMs = System.currentTimeMillis()
        )
    }.combine(clockTickFlow) { eph, nowMs ->
        eph.copy(nowMs = nowMs)
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
        val quizCooldown = wallet.currentQuizCooldownRemainingMs(eph.nowMs)

        MegaCollectorUiState(
            wallet = wallet,
            catalog = catalog,
            ownedCards = owned,
            tradeListings = trades,
            bazaarListings = BazaarMarketCatalog.buildDailyBazaarListings(
                catalog = catalog,
                epochDay = today,
                rotationSeed = eph.bazaarSeed
            ),
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
            quizCooldownRemainingMs = quizCooldown,
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

            val today = repository.currentLocalEpochDay()
            val alreadySyncedToday = wallet.catalogSyncedEpochDay >= today &&
                wallet.catalogSchemaVersion >= PlayerPreferencesStore.CURRENT_CATALOG_SCHEMA_VERSION &&
                count >= 950

            if (alreadySyncedToday) {
                _syncStatus.value = CatalogSyncStatus.Synced(count)
            } else {
                syncCatalog()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
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
            if (enabled) {
                soundEngine.playClick()
            }
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        soundEngine.playClick()
        viewModelScope.launch {
            repository.setDarkModeEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        soundEngine.playClick()
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
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
                    _snackbarMessage.value = "Synced $count skins (saved for today)!"
                }
                .onFailure { err ->
                    _syncStatus.value = CatalogSyncStatus.Error(
                        err.localizedMessage ?: "Could not reach mlbbdex.com/api/v1/skins/"
                    )
                }
        }
    }

    /**
     * Stages an un-ripped pack with a randomized satin wrapper theme.
     * IMPORTANT: No coins/diamonds/tickets are consumed until the user actually rips the pack!
     * Pressing X (close) before ripping cancels cleanly with 0 currency spent.
     */
    fun openPackBundle(
        bundle: PackBundleOption,
        payWithDiamonds: Boolean = false,
        useFreeTicket: Boolean = false
    ) {
        if (_isOpeningPack.value || _packSession.value != null) return
        soundEngine.playClick()
        viewModelScope.launch {
            val currentWallet = repository.walletFlow.first()
            repository.verifyPackReadyToRip(bundle, currentWallet, payWithDiamonds, useFreeTicket)
                .onSuccess { effectiveBundle ->
                    val theme = PackSatinTheme.random(lastSatinThemeId)
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
                    _snackbarMessage.value =
                        "Quiz Complete ($score/${QuizQuestionBank.QUIZ_QUESTION_COUNT}): +$earned 💎 Diamonds!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Quiz on hourly cooldown."
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

    fun refreshBazaar() {
        soundEngine.playClick()
        _bazaarSeed.update { it + 1 }
        _snackbarMessage.value = "Refreshed Collector Bazaar listings!"
    }

    fun executeBazaarTrade(listing: BazaarTraderListing, offeredCard: OwnedCard) {
        viewModelScope.launch {
            repository.executeBazaarTrade(listing, offeredCard)
                .onSuccess { received ->
                    soundEngine.playRewardChime()
                    _snackbarMessage.value = "Swapped with ${listing.trader.alias} for ${received.skinName}!"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Bazaar trade failed."
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
