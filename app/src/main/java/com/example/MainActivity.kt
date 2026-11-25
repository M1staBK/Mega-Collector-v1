package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.remote.AuthRepository
import com.example.data.remote.AuthResult
import com.example.data.remote.AuthenticatedUser
import com.example.model.DecodedTradeSlip
import com.example.model.OwnedCard
import com.example.model.P2PTradeListing
import com.example.model.TradeListing
import com.example.ui.components.AppPreloadOverlay
import com.example.ui.components.CollectorSettingsController
import com.example.ui.components.ProvideCollectorSettings
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.SkyStudioBackdrop
import com.example.ui.screens.CardInspectScreen
import com.example.ui.screens.MetaGuidesScreen
import com.example.ui.screens.PackOpeningScreen
import com.example.ui.screens.PlayArcadeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.RecycleForgeScreen
import com.example.ui.screens.ShopPacksScreen
import com.example.ui.screens.TradeDeskScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.MegaCollectorTheme
import com.example.ui.theme.ThemeAccentOption
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MegaCollectorUiState
import kotlinx.coroutines.launch

private enum class CollectorTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    META("meta", R.string.nav_meta, Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    PLAY("play", R.string.nav_play, Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    QUESTS("quests", R.string.nav_quests, Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
    PACKS("packs", R.string.nav_packs, Icons.Filled.Style, Icons.Outlined.Style),
    VAULT("vault", R.string.nav_vault, Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

private enum class VaultSubTab(
    val label: String,
    val icon: ImageVector
) {
    COLLECTION("Cards", Icons.Filled.Style),
    RECYCLE("Recycle", Icons.Filled.Recycling),
    TRADE("Trade", Icons.Filled.SwapHoriz)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialBootTheme = com.example.data.local.PlayerPreferencesStore(applicationContext).readBootThemeSync()
        val initialBarStyle = if (initialBootTheme.darkModeEnabled) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        }
        enableEdgeToEdge(
            statusBarStyle = initialBarStyle,
            navigationBarStyle = initialBarStyle
        )
        // Paint the window in the player's theme colour from the very first frame (no colour flash on boot)
        val bootPalette = com.example.ui.theme.buildCollectorPalette(
            isDark = initialBootTheme.darkModeEnabled,
            accent = com.example.ui.theme.ThemeAccentOption.fromId(initialBootTheme.themeAccentId)
        )
        window.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(
                android.graphics.Color.argb(
                    255,
                    (bootPalette.canvas.red * 255f).toInt(),
                    (bootPalette.canvas.green * 255f).toInt(),
                    (bootPalette.canvas.blue * 255f).toInt()
                )
            )
        )
        setContent {
            MegaCollectorRoot(
                onThemeChanged = { darkMode ->
                    val style = if (darkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }
            )
        }
    }
}

@Composable
fun MegaCollectorRoot(
    onThemeChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember(context) { AuthRepository(context.applicationContext) }
    val currentUser by authRepo.authStateFlow.collectAsStateWithLifecycle(initialValue = authRepo.currentUser())

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (authRepo.currentUser() == null) {
            authRepo.attemptSilentSignIn()
        }
    }

    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkMode = uiState.wallet.darkModeEnabled
    val vibrationEnabled = uiState.wallet.vibrationEnabled
    val themeAccent = remember(uiState.wallet.themeAccentId) {
        ThemeAccentOption.fromId(uiState.wallet.themeAccentId)
    }

    LaunchedEffect(currentUser) {
        viewModel.bindAuthenticatedUser(currentUser)
    }

    LaunchedEffect(darkMode) {
        onThemeChanged(darkMode)
    }

    MegaCollectorTheme(
        darkTheme = darkMode,
        accentOption = themeAccent,
        vibrationEnabled = vibrationEnabled
    ) {
        MegaCollectorApp(
            viewModel = viewModel,
            currentUser = currentUser,
            isSigningIn = isSigningIn,
            authError = authError,
            onConsumeAuthError = { authError = null },
            onGoogleSignInClick = { activityCtx ->
                scope.launch {
                    isSigningIn = true
                    authError = null
                    when (val result = authRepo.signInWithGoogle(activityCtx)) {
                        is AuthResult.Success -> authError = null
                        is AuthResult.Cancelled -> authError = "Google Sign-In cancelled."
                        is AuthResult.Error -> authError = result.message
                    }
                    isSigningIn = false
                }
            },
            onSignOut = {
                scope.launch {
                    authRepo.signOut()
                }
            }
        )
    }
}

@Composable
fun MegaCollectorApp(
    viewModel: MainViewModel,
    currentUser: AuthenticatedUser?,
    isSigningIn: Boolean = false,
    authError: String? = null,
    onConsumeAuthError: () -> Unit = {},
    onGoogleSignInClick: (Context) -> Unit = {},
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = CollectorTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val mainPagerState = rememberPagerState(
        initialPage = CollectorTab.META.ordinal,
        pageCount = { CollectorTab.entries.size }
    )
    val vaultPagerState = rememberPagerState(
        initialPage = VaultSubTab.COLLECTION.ordinal,
        pageCount = { VaultSubTab.entries.size }
    )
    val activeTab = CollectorTab.entries[mainPagerState.currentPage]
    var inspectingCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var preselectedTradeCard by remember { mutableStateOf<OwnedCard?>(null) }
    val activeAccent = remember(uiState.wallet.themeAccentId) {
        ThemeAccentOption.fromId(uiState.wallet.themeAccentId)
    }

    fun navigateToTab(tab: CollectorTab, subTab: VaultSubTab? = null) {
        if (subTab != null) {
            scope.launch { vaultPagerState.animateScrollToPage(subTab.ordinal) }
        }
        scope.launch { mainPagerState.animateScrollToPage(tab.ordinal) }
    }

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

    LaunchedEffect(authError) {
        if (authError != null) {
            snackbarHostState.showSnackbar(authError)
            onConsumeAuthError()
        }
    }

    val packSession = uiState.packSession

    ProvideCollectorSettings(
        controller = CollectorSettingsController(
            soundEnabled = !uiState.wallet.soundMuted,
            darkModeEnabled = uiState.wallet.darkModeEnabled,
            vibrationEnabled = uiState.wallet.vibrationEnabled,
            themeAccent = activeAccent,
            unlockedThemeIds = uiState.wallet.unlockedThemeIds,
            onToggleSound = viewModel::setSoundEnabled,
            onToggleDarkMode = viewModel::setDarkModeEnabled,
            onToggleVibration = viewModel::setVibrationEnabled,
            onSelectThemeAccent = viewModel::setThemeAccent,
            onCogOpened = viewModel::playClick
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SkyStudioBackdrop(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useRail = maxWidth >= 600.dp

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            if (!useRail && packSession == null && inspectingCardId == null) {
                                Surface(
                                    color = palette.navBar,
                                    shadowElevation = 12.dp,
                                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                                    border = BorderStroke(
                                        1.2.dp,
                                        if (palette.isDark) palette.border else palette.glassHighlight
                                    ),
                                    modifier = Modifier.clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        tonalElevation = 0.dp
                                    ) {
                                        CollectorTab.entries.forEach { tab ->
                                            val selected = activeTab == tab
                                            val badgeCount = when (tab) {
                                                CollectorTab.PLAY -> uiState.scratcherStatus.charges +
                                                    uiState.prismStatus.charges +
                                                    uiState.wheelStatus.charges +
                                                    uiState.mysteryPickStatus.charges +
                                                    uiState.memoryMatchStatus.charges +
                                                    if (uiState.quizCooldownRemainingMs <= 0L) 1 else 0
                                                CollectorTab.QUESTS -> claimableQuestCount
                                                CollectorTab.VAULT -> uiState.openP2PListings.size
                                                else -> 0
                                            }
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = {
                                                    viewModel.playClick()
                                                    navigateToTab(tab)
                                                },
                                                icon = {
                                                    BadgedBox(
                                                        badge = {
                                                            if (badgeCount > 0) {
                                                                Badge(
                                                                    containerColor = palette.buttonPrimary,
                                                                    contentColor = Color.White
                                                                ) {
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
                                                label = {
                                                    Text(
                                                        text = stringResource(tab.labelRes),
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = palette.accentPrimary,
                                                    selectedTextColor = palette.accentPrimary,
                                                    indicatorColor = palette.frostSurface,
                                                    unselectedIconColor = palette.muted,
                                                    unselectedTextColor = palette.muted
                                                ),
                                                modifier = Modifier.testTag("nav_${tab.route}")
                                            )
                                        }
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
                                    containerColor = palette.navBar,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    CollectorTab.entries.forEach { tab ->
                                        val selected = activeTab == tab
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = {
                                                viewModel.playClick()
                                                navigateToTab(tab)
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = stringResource(tab.labelRes)
                                                )
                                            },
                                            label = { Text(stringResource(tab.labelRes)) },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = palette.accentPrimary,
                                                selectedTextColor = palette.accentPrimary,
                                                indicatorColor = palette.frostSurface,
                                                unselectedIconColor = palette.muted,
                                                unselectedTextColor = palette.muted
                                            ),
                                            modifier = Modifier.testTag("rail_${tab.route}")
                                        )
                                    }
                                }
                            }

                            HorizontalPager(
                                state = mainPagerState,
                                beyondViewportPageCount = 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .testTag("main_tab_pager")
                            ) { page ->
                                when (CollectorTab.entries[page]) {
                                    CollectorTab.PLAY -> PlayArcadeScreen(
                                        uiState = uiState,
                                        onClaimScratcherPrize = viewModel::claimScratcherPrize,
                                        onAwardPrismDiamonds = viewModel::awardPrismDiamonds,
                                        onSpinWheelPrize = viewModel::spinWheelPrize,
                                        onWheelTickSfx = viewModel::playWheelTick,
                                        onCompleteHourlyQuiz = viewModel::completeHourlyQuiz,
                                        onClaimMysteryPick = viewModel::claimMysteryPickPrize,
                                        onCompleteMemoryMatch = viewModel::completeMemoryMatch,
                                        onPlasticCrinkle = viewModel::playPlasticCrinkle,
                                        onOpenSettings = {},
                                        onButtonClickSfx = viewModel::playClick,
                                        includeStatusBarPadding = true
                                    )

                                    CollectorTab.QUESTS -> QuestsScreen(
                                        uiState = uiState,
                                        onClaimQuest = viewModel::claimQuest,
                                        onClaimAchievement = viewModel::claimAchievementReward,
                                        onGoToBinder = {
                                            navigateToTab(CollectorTab.VAULT, VaultSubTab.COLLECTION)
                                        },
                                        onOpenSettings = {},
                                        onButtonClickSfx = viewModel::playClick,
                                        includeStatusBarPadding = true
                                    )

                                    CollectorTab.PACKS -> ShopPacksScreen(
                                        uiState = uiState,
                                        currentUser = currentUser,
                                        dailyRewardTiers = viewModel.dailyRewardTiers,
                                        onClaimDailyReward = {
                                            viewModel.playClick()
                                            viewModel.claimDailyReward()
                                        },
                                        onOpenPackBundle = viewModel::openPackBundle,
                                        onExchangeDiamondsForCoins = viewModel::exchangeDiamondsForCoins,
                                        onPurchaseOrEquipFrame = viewModel::purchaseOrEquipFrame,
                                        onPurchaseOrEquipBinder = viewModel::purchaseOrEquipBinder,
                                        onGoToArcade = { navigateToTab(CollectorTab.PLAY) },
                                        onGoToMissions = { navigateToTab(CollectorTab.QUESTS) },
                                        onGoToBinder = {
                                            navigateToTab(CollectorTab.VAULT, VaultSubTab.COLLECTION)
                                        },
                                        onOpenSettings = {},
                                        onButtonClickSfx = viewModel::playClick
                                    )

                                    CollectorTab.VAULT -> VaultTabHost(
                                        uiState = uiState,
                                        currentUser = currentUser,
                                        isSigningIn = isSigningIn,
                                        vaultPagerState = vaultPagerState,
                                        preselectedTradeCard = preselectedTradeCard,
                                        onSelectSubTab = { sub ->
                                            viewModel.playClick()
                                            scope.launch { vaultPagerState.animateScrollToPage(sub.ordinal) }
                                        },
                                        onInspectCard = { id ->
                                            viewModel.playClick()
                                            inspectingCardId = id
                                        },
                                        onRecycleSingle = { card -> viewModel.recycleSingleCard(card) },
                                        onRecycleAllDuplicates = {
                                            viewModel.playClick()
                                            viewModel.recycleAllDuplicates()
                                        },
                                        onClearPreselectedCard = { preselectedTradeCard = null },
                                        onAcceptP2PListing = viewModel::acceptP2PListing,
                                        onCancelCloudP2PListing = viewModel::cancelCloudP2PListing,
                                        onViewTraderProfile = viewModel::viewTraderProfile,
                                        onDismissViewingProfile = viewModel::dismissViewingProfile,
                                        onCreateTradeListing = { card, seeking, gold ->
                                            viewModel.createTradeListing(card, seeking, gold)
                                        },
                                        onCancelListing = viewModel::cancelTradeListing,
                                        onDecodeSlip = viewModel::decodeTradeSlip,
                                        onClearDecodedSlip = viewModel::clearDecodedSlip,
                                        onCompleteTrade = viewModel::completeTradeWithSlip,
                                        onGoogleSignInClick = onGoogleSignInClick,
                                        onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                        onButtonClickSfx = viewModel::playClick
                                    )

                                    CollectorTab.META -> {
                                        val metaState by viewModel.metaState.collectAsStateWithLifecycle()
                                        LaunchedEffect(Unit) { viewModel.loadMetaIfNeeded() }
                                        MetaGuidesScreen(
                                            metaState = metaState,
                                            onSelectRankFilter = viewModel::selectMetaRankFilter,
                                            onSelectSortMode = viewModel::selectMetaSortMode,
                                            onRefreshMeta = { viewModel.refreshMeta(force = true) },
                                            onOpenHeroDetail = viewModel::openMetaHeroDetail,
                                            onDismissHeroDetail = viewModel::dismissMetaHeroDetail,
                                            onOpenSettings = {},
                                            onButtonClickSfx = viewModel::playClick
                                        )
                                    }

                                    CollectorTab.PROFILE -> ProfileScreen(
                                        uiState = uiState,
                                        currentUser = currentUser,
                                        onUpdateAlias = viewModel::updateAlias,
                                        onGoogleSignInClick = onGoogleSignInClick,
                                        isSigningIn = isSigningIn,
                                        onSignOut = onSignOut,
                                        onSyncCatalog = {
                                            viewModel.playClick()
                                            viewModel.syncCatalog()
                                        },
                                        onTogglePinnedBadge = viewModel::togglePinnedBadge,
                                        onClaimAchievementReward = viewModel::claimAchievementReward,
                                        onOpenSettings = {},
                                        onButtonClickSfx = viewModel::playClick
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen Pack Opening Overlay
            if (packSession != null && inspectingCardId == null) {
                BackHandler {
                    if (!packSession.bundleCommitted || packSession.allRevealed || packSession.showSummaryGrid) {
                        viewModel.closePackSession()
                    }
                }
                PackOpeningScreen(
                    session = packSession,
                    onOpenSettings = {},
                    onPlasticCrinkle = viewModel::playPlasticCrinkle,
                    onPackTear = viewModel::playPackTear,
                    onCardFlipSound = viewModel::playCardFlipSfx,
                    onPackRipped = viewModel::ripAndCommitPack,
                    onRevealCard = viewModel::revealCardAt,
                    onAdvanceFromReveal = viewModel::advanceToNextFaceDownCard,
                    onShowSummaryGrid = viewModel::showPulledSummaryGrid,
                    onRevealCurrentPack = viewModel::revealCurrentPackCards,
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
                        navigateToTab(CollectorTab.VAULT, VaultSubTab.TRADE)
                    },
                    onButtonClickSfx = viewModel::playClick
                )
            }

            // Asset Preload Launch Overlay (theme-accented bar only, zero status/stage copy)
            AnimatedVisibility(
                visible = uiState.isPreloading,
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                AppPreloadOverlay(progress = uiState.preloadProgress)
            }
        }
    }
}

@Composable
private fun VaultTabHost(
    uiState: MegaCollectorUiState,
    currentUser: AuthenticatedUser?,
    isSigningIn: Boolean,
    vaultPagerState: PagerState,
    preselectedTradeCard: OwnedCard?,
    onSelectSubTab: (VaultSubTab) -> Unit,
    onInspectCard: (Long) -> Unit,
    onRecycleSingle: (OwnedCard) -> Unit,
    onRecycleAllDuplicates: () -> Unit,
    onClearPreselectedCard: () -> Unit,
    onAcceptP2PListing: (P2PTradeListing, OwnedCard) -> Unit,
    onCancelCloudP2PListing: (P2PTradeListing) -> Unit,
    onViewTraderProfile: (String) -> Unit,
    onDismissViewingProfile: () -> Unit,
    onCreateTradeListing: (OwnedCard, String, Int) -> Unit,
    onCancelListing: (TradeListing) -> Unit,
    onDecodeSlip: (String) -> Unit,
    onClearDecodedSlip: () -> Unit,
    onCompleteTrade: (DecodedTradeSlip, OwnedCard) -> Unit,
    onGoogleSignInClick: (Context) -> Unit,
    onShowToast: (String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    val vaultSubTab = VaultSubTab.entries[vaultPagerState.currentPage]
    val misprintCount = remember(uiState.ownedCards) { uiState.ownedCards.count { it.isMisprint } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Screen Header: My Collection + Misprint/Badge Pill + Cog
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = palette.frostSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, palette.glassHighlight),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CollectionsBookmark,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "My Collection",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink
                    )
                    Text(
                        text = "Browse your cards and skins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = palette.cardSurface,
                    shape = RoundedCornerShape(999.dp),
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.1.dp, palette.glassHighlight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = palette.accentPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$misprintCount / 1",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.accentPrimary
                        )
                    }
                }
                SettingsGearButton()
            }
        }

        // Frosted Pill Segmented Tab Switcher (Cards | Recycle | Trade)
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(999.dp),
            shadowElevation = 3.dp,
            border = BorderStroke(1.2.dp, palette.glassHighlight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VaultSubTab.entries.forEach { sub ->
                    val selected = vaultSubTab == sub
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                brush = if (selected) {
                                    Brush.horizontalGradient(
                                        listOf(palette.buttonPrimary, palette.skyAccent.copy(alpha = 0.92f))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            )
                            .clickable { onSelectSubTab(sub) }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .testTag("vault_subtab_${sub.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = sub.icon,
                                contentDescription = null,
                                tint = if (selected) Color.White else palette.muted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = sub.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
                                ),
                                color = if (selected) Color.White else palette.muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        HorizontalPager(
            state = vaultPagerState,
            beyondViewportPageCount = 0,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("vault_subtab_pager")
        ) { subPage ->
            when (VaultSubTab.entries[subPage]) {
                VaultSubTab.COLLECTION -> VaultScreen(
                    uiState = uiState,
                    onInspectCard = onInspectCard,
                    onButtonClickSfx = onButtonClickSfx,
                    includeStatusBarPadding = false
                )
                VaultSubTab.RECYCLE -> RecycleForgeScreen(
                    uiState = uiState,
                    onRecycleSingle = onRecycleSingle,
                    onRecycleAllDuplicates = onRecycleAllDuplicates,
                    onInspectCard = onInspectCard,
                    onButtonClickSfx = onButtonClickSfx,
                    includeStatusBarPadding = false
                )
                VaultSubTab.TRADE -> TradeDeskScreen(
                    uiState = uiState,
                    currentUserUid = currentUser?.uid,
                    preselectedCardForTrade = preselectedTradeCard,
                    onClearPreselectedCard = onClearPreselectedCard,
                    onAcceptP2PListing = onAcceptP2PListing,
                    onCancelCloudP2PListing = onCancelCloudP2PListing,
                    onViewTraderProfile = onViewTraderProfile,
                    onDismissViewingProfile = onDismissViewingProfile,
                    onCreateTradeListing = onCreateTradeListing,
                    onCancelListing = onCancelListing,
                    onDecodeSlip = onDecodeSlip,
                    onClearDecodedSlip = onClearDecodedSlip,
                    onCompleteTrade = onCompleteTrade,
                    onGoogleSignInClick = onGoogleSignInClick,
                    isSigningIn = isSigningIn,
                    onShowToast = onShowToast,
                    onButtonClickSfx = onButtonClickSfx,
                    includeStatusBarPadding = false
                )
            }
        }
    }
}
