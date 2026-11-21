package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.OwnedCard
import com.example.ui.screens.CardInspectScreen
import com.example.ui.screens.PackOpeningScreen
import com.example.ui.screens.PlayArcadeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.RecycleForgeScreen
import com.example.ui.screens.ShopPacksScreen
import com.example.ui.screens.TradeDeskScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.MegaCollectorTheme
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private enum class CollectorTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PACKS("packs", R.string.nav_packs, Icons.Filled.Storefront, Icons.Outlined.Storefront),
    PLAY("play", R.string.nav_play, Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    QUESTS("quests", R.string.nav_quests, Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
    VAULT("vault", R.string.nav_vault, Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

private enum class VaultSubTab(val label: String) {
    COLLECTION("My Cards"),
    RECYCLE("Recycle"),
    TRADE("Trade")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MegaCollectorTheme {
                MegaCollectorApp()
            }
        }
    }
}

@Composable
fun MegaCollectorApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by rememberSaveable { mutableStateOf(CollectorTab.PACKS) }
    var vaultSubTab by rememberSaveable { mutableStateOf(VaultSubTab.COLLECTION) }
    var inspectingCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var preselectedTradeCard by remember { mutableStateOf<OwnedCard?>(null) }

    val claimableQuestCount = remember(uiState.quests) {
        uiState.quests.count { it.canClaim }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    val packSession = uiState.packSession

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = PearlCanvas,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail && packSession == null && inspectingCardId == null) {
                        NavigationBar(
                            containerColor = PureWhiteCard,
                            tonalElevation = 4.dp
                        ) {
                            CollectorTab.entries.forEach { tab ->
                                val selected = activeTab == tab
                                val badgeCount = when (tab) {
                                    CollectorTab.QUESTS -> claimableQuestCount
                                    CollectorTab.PLAY -> uiState.dailyScratchersRemaining
                                    else -> 0
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        viewModel.playClick()
                                        activeTab = tab
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (badgeCount > 0) {
                                                    Badge(containerColor = StudioGold) {
                                                        Text("$badgeCount")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = stringResource(tab.labelRes)
                                            )
                                        }
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DeepCobalt,
                                        selectedTextColor = DeepCobalt,
                                        indicatorColor = SkyFrost,
                                        unselectedIconColor = SlateMuted,
                                        unselectedTextColor = SlateMuted
                                    ),
                                    modifier = Modifier.testTag("nav_${tab.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail && packSession == null && inspectingCardId == null) {
                        NavigationRail(
                            containerColor = PureWhiteCard,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            CollectorTab.entries.forEach { tab ->
                                val selected = activeTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        viewModel.playClick()
                                        activeTab = tab
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = DeepCobalt,
                                        selectedTextColor = DeepCobalt,
                                        indicatorColor = SkyFrost,
                                        unselectedIconColor = SlateMuted,
                                        unselectedTextColor = SlateMuted
                                    ),
                                    modifier = Modifier.testTag("rail_${tab.route}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (activeTab) {
                            CollectorTab.PACKS -> ShopPacksScreen(
                                uiState = uiState,
                                dailyRewardTiers = viewModel.dailyRewardTiers,
                                onClaimDailyReward = {
                                    viewModel.playClick()
                                    viewModel.claimDailyReward()
                                },
                                onOpenPackBundle = viewModel::openPackBundle,
                                onExchangeDiamondsForCoins = viewModel::exchangeDiamondsForCoins,
                                onSyncCatalog = {
                                    viewModel.playClick()
                                    viewModel.syncCatalog()
                                },
                                onToggleMute = viewModel::toggleSoundMute,
                                onButtonClickSfx = viewModel::playClick
                            )

                            CollectorTab.PLAY -> PlayArcadeScreen(
                                uiState = uiState,
                                onClaimScratcherPrize = viewModel::claimScratcherPrize,
                                onAwardMinigameDiamonds = viewModel::awardMinigameDiamonds,
                                onPlasticCrinkle = viewModel::playPlasticCrinkle,
                                onButtonClickSfx = viewModel::playClick
                            )

                            CollectorTab.QUESTS -> QuestsScreen(
                                uiState = uiState,
                                onClaimQuest = viewModel::claimQuest,
                                onButtonClickSfx = viewModel::playClick
                            )

                            CollectorTab.VAULT -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PearlCanvas)
                                    .statusBarsPadding()
                            ) {
                                Surface(
                                    color = PureWhiteCard,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, IceBorder),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        VaultSubTab.entries.forEach { sub ->
                                            val selected = vaultSubTab == sub
                                            FilterChip(
                                                selected = selected,
                                                onClick = {
                                                    viewModel.playClick()
                                                    vaultSubTab = sub
                                                },
                                                label = { Text(sub.label) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = DeepCobalt,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = SkyFrost,
                                                    labelColor = SlateMuted
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("vault_subtab_${sub.name.lowercase()}")
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    when (vaultSubTab) {
                                        VaultSubTab.COLLECTION -> VaultScreen(
                                            uiState = uiState,
                                            onInspectCard = { id ->
                                                viewModel.playClick()
                                                inspectingCardId = id
                                            },
                                            onButtonClickSfx = viewModel::playClick,
                                            includeStatusBarPadding = false
                                        )
                                        VaultSubTab.RECYCLE -> RecycleForgeScreen(
                                            uiState = uiState,
                                            onRecycleSingle = { card -> viewModel.recycleSingleCard(card) },
                                            onRecycleAllDuplicates = {
                                                viewModel.playClick()
                                                viewModel.recycleAllDuplicates()
                                            },
                                            onInspectCard = { id ->
                                                viewModel.playClick()
                                                inspectingCardId = id
                                            },
                                            onButtonClickSfx = viewModel::playClick,
                                            includeStatusBarPadding = false
                                        )
                                        VaultSubTab.TRADE -> TradeDeskScreen(
                                            uiState = uiState,
                                            preselectedCardForTrade = preselectedTradeCard,
                                            onClearPreselectedCard = { preselectedTradeCard = null },
                                            onCreateTradeListing = { card, seeking, gold ->
                                                viewModel.createTradeListing(card, seeking, gold)
                                            },
                                            onCancelListing = viewModel::cancelTradeListing,
                                            onDecodeSlip = viewModel::decodeTradeSlip,
                                            onClearDecodedSlip = viewModel::clearDecodedSlip,
                                            onCompleteTrade = viewModel::completeTradeWithSlip,
                                            onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                            onButtonClickSfx = viewModel::playClick,
                                            includeStatusBarPadding = false
                                        )
                                    }
                                }
                            }

                            CollectorTab.PROFILE -> ProfileScreen(
                                uiState = uiState,
                                onUpdateAlias = viewModel::updateAlias,
                                onToggleMute = viewModel::toggleSoundMute,
                                onButtonClickSfx = viewModel::playClick
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Pack Opening Overlay
        if (packSession != null && inspectingCardId == null) {
            BackHandler {
                if (packSession.allRevealed) {
                    viewModel.closePackSession()
                }
            }
            PackOpeningScreen(
                session = packSession,
                soundMuted = uiState.wallet.soundMuted,
                onToggleMute = viewModel::toggleSoundMute,
                onPlasticCrinkle = viewModel::playPlasticCrinkle,
                onPackTear = viewModel::playPackTear,
                onCardFlipSound = viewModel::playCardFlipSfx,
                onPackRipped = viewModel::markPackRipped,
                onRevealCard = viewModel::revealCardAt,
                onAdvanceFromReveal = viewModel::advanceToNextFaceDownCard,
                onShowSummaryGrid = viewModel::showPulledSummaryGrid,
                onFocusRevealedCard = viewModel::focusRevealedCard,
                onInspectCard = { cardId ->
                    viewModel.playClick()
                    inspectingCardId = cardId
                },
                onClose = viewModel::closePackSession
            )
        }

        // Fullscreen 3D Gyroscope Card Inspector Overlay
        val currentInspectId = inspectingCardId
        if (currentInspectId != null) {
            BackHandler {
                viewModel.playClick()
                inspectingCardId = null
            }
            val inspectedCard by viewModel.observeOwnedCard(currentInspectId)
                .collectAsStateWithLifecycle(initialValue = null)
            val dupCount = remember(uiState.ownedCards, inspectedCard) {
                inspectedCard?.let { target ->
                    uiState.ownedCards.count { it.catalogId == target.catalogId }
                } ?: 1
            }

            CardInspectScreen(
                card = inspectedCard,
                duplicateCount = dupCount,
                onBack = {
                    viewModel.playClick()
                    inspectingCardId = null
                },
                onRecycle = { card ->
                    viewModel.recycleSingleCard(card) {
                        inspectingCardId = null
                    }
                },
                onMintTradeSlip = { card ->
                    viewModel.playClick()
                    preselectedTradeCard = card
                    inspectingCardId = null
                    viewModel.closePackSession()
                    activeTab = CollectorTab.VAULT
                    vaultSubTab = VaultSubTab.TRADE
                },
                onButtonClickSfx = viewModel::playClick
            )
        }
    }
}
