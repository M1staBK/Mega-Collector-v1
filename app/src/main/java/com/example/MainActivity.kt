package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.OwnedCard
import com.example.ui.screens.CardInspectScreen
import com.example.ui.screens.PackOpeningScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecycleForgeScreen
import com.example.ui.screens.ShopPacksScreen
import com.example.ui.screens.TradeDeskScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MegaCollectorTheme
import com.example.ui.theme.SurfaceAbyss
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import com.example.ui.viewmodel.MainViewModel

import kotlinx.coroutines.launch

private enum class CollectorTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PACKS("packs", R.string.nav_packs, Icons.Filled.Storefront, Icons.Outlined.Storefront),
    VAULT("vault", R.string.nav_vault, Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    RECYCLE("recycle", R.string.nav_forge, Icons.Filled.Recycling, Icons.Outlined.Recycling),
    TRADE("trade", R.string.nav_trade, Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    var inspectingCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var preselectedTradeCard by remember { mutableStateOf<OwnedCard?>(null) }

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
                containerColor = VoidObsidian,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail && packSession == null && inspectingCardId == null) {
                        NavigationBar(containerColor = SurfaceAbyss) {
                            CollectorTab.entries.forEach { tab ->
                                val selected = activeTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { activeTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = VoidObsidian,
                                        selectedTextColor = ArenaGold,
                                        indicatorColor = ArenaGold,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
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
                            containerColor = SurfaceAbyss,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            CollectorTab.entries.forEach { tab ->
                                val selected = activeTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { activeTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = VoidObsidian,
                                        selectedTextColor = ArenaGold,
                                        indicatorColor = ArenaGold,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
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
                                onClaimDailyReward = viewModel::claimDailyReward,
                                onOpenPack = viewModel::openPack,
                                onSyncCatalog = viewModel::syncCatalog
                            )
                            CollectorTab.VAULT -> VaultScreen(
                                uiState = uiState,
                                onInspectCard = { id -> inspectingCardId = id }
                            )
                            CollectorTab.RECYCLE -> RecycleForgeScreen(
                                uiState = uiState,
                                onRecycleSingle = { card -> viewModel.recycleSingleCard(card) },
                                onRecycleAllDuplicates = viewModel::recycleAllDuplicates,
                                onInspectCard = { id -> inspectingCardId = id }
                            )
                            CollectorTab.TRADE -> TradeDeskScreen(
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
                                onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                            CollectorTab.PROFILE -> ProfileScreen(
                                uiState = uiState,
                                onUpdateAlias = viewModel::updateAlias
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
                onPackRipped = viewModel::markPackRipped,
                onRevealCard = viewModel::revealCardAt,
                onSelectCardIndex = viewModel::selectPackCardIndex,
                onRevealAll = viewModel::revealAllCards,
                onInspectCard = { cardId -> inspectingCardId = cardId },
                onClose = viewModel::closePackSession
            )
        }

        // Fullscreen 3D Gyroscope Card Inspector Overlay
        val currentInspectId = inspectingCardId
        if (currentInspectId != null) {
            BackHandler {
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
                onBack = { inspectingCardId = null },
                onRecycle = { card ->
                    viewModel.recycleSingleCard(card) {
                        inspectingCardId = null
                    }
                },
                onMintTradeSlip = { card ->
                    preselectedTradeCard = card
                    inspectingCardId = null
                    viewModel.closePackSession()
                    activeTab = CollectorTab.TRADE
                }
            )
        }
    }
}
