package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.remote.AuthenticatedUser
import com.example.model.AchievementBadgeUi
import com.example.model.AchievementCatalog
import com.example.model.CollectorHistoryEvent
import com.example.model.CollectorHistoryType
import com.example.model.CollectorPublicProfile
import com.example.model.CosmeticCatalog
import com.example.model.MlbbCollectionTier
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.ui.components.BadgeIconVector
import com.example.ui.components.CatalogSyncStatusCard
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.FramedAvatar
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.MlbbCollectionBadge
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.components.ProfileViewerDialog
import com.example.ui.components.SettingsGearButton
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.viewmodel.MegaCollectorUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import com.example.model.ProfileFrameItem
import com.example.model.CardBinderDesign

@Composable
fun ProfileScreen(
    uiState: MegaCollectorUiState,
    currentUser: AuthenticatedUser?,
    onUpdateAlias: (String) -> Unit,
    onGoogleSignInClick: (Context) -> Unit = {},
    isSigningIn: Boolean = false,
    onSignOut: () -> Unit,
    onSyncCatalog: () -> Unit,
    onTogglePinnedBadge: (AchievementBadgeUi) -> Unit,
    onClaimAchievementReward: (AchievementBadgeUi) -> Unit = {},
    onEquipFrame: (ProfileFrameItem) -> Unit = {},
    onEquipBinder: (CardBinderDesign) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val context = LocalContext.current
    var showEditAlias by remember { mutableStateOf(false) }
    var showEditCosmetics by remember { mutableStateOf(false) }
    var showSelfProfilePreview by remember { mutableStateOf(false) }
    var showCollectionLadderDialog by remember { mutableStateOf(false) }
    var inspectedBadgeId by remember { mutableStateOf<String?>(null) }
    var inspectedHeaderBadgeId by remember { mutableStateOf<String?>(null) }

    val uniqueSkinCount = remember(uiState.ownedCards) {
        uiState.ownedCards.map { it.catalogId }.distinct().size
    }
    val misprintCount = remember(uiState.ownedCards) {
        uiState.ownedCards.count { it.isMisprint }
    }
    val totalPrestige = remember(uiState.ownedCards) {
        uiState.ownedCards.sumOf { it.prestigeScore }
    }

    val uniqueLegends = remember(uiState.ownedCards) {
        MlbbCollectionTier.uniqueLegendCount(uiState.ownedCards)
    }
    val collectionTier = remember(totalPrestige, uniqueLegends) {
        MlbbCollectionTier.fromCollection(totalPrestige, uniqueLegends)
    }
    val collectorRank = collectionTier.title

    val pinnedBadges = remember(uiState.achievements) {
        uiState.achievements.filter { it.isPinned }.take(AchievementCatalog.MAX_PINNED_BADGES)
    }
    val unlockedCount = remember(uiState.achievements) {
        uiState.achievements.count { it.isUnlocked }
    }
    val inspectedBadge = remember(uiState.achievements, inspectedBadgeId) {
        uiState.achievements.firstOrNull { it.definition.id == inspectedBadgeId }
    }
    val inspectedHeaderBadge = remember(pinnedBadges, inspectedHeaderBadgeId) {
        pinnedBadges.firstOrNull { it.definition.id == inspectedHeaderBadgeId }
    }

    val selfPublicProfile = remember(
        uiState.wallet,
        uiState.ownedCards,
        currentUser,
        collectorRank,
        totalPrestige,
        uniqueSkinCount,
        misprintCount,
        pinnedBadges
    ) {
        val topSkins = uiState.ownedCards
            .sortedByDescending { it.prestigeScore }
            .distinctBy { it.catalogId }
            .take(3)
            .map { card ->
                SkinCatalogItem(
                    id = card.catalogId,
                    skinName = card.skinName,
                    heroName = card.heroName,
                    role = card.role,
                    rawTier = card.rawTier,
                    rarity = card.rarity,
                    imageUrl = card.imageUrl
                )
            }

        CollectorPublicProfile(
            traderId = currentUser?.uid ?: "self",
            alias = uiState.wallet.collectorAlias.ifBlank { currentUser?.displayName ?: "Collector" },
            mlbbIgn = "",
            mlbbServerId = "",
            photoUrl = currentUser?.photoUrl.orEmpty(),
            rankTitle = collectorRank,
            avatarColor = palette.accentPrimary,
            prestigeScore = totalPrestige,
            uniqueSkinsCount = uniqueSkinCount,
            packsOpened = uiState.wallet.totalPacksOpened,
            tradesDone = uiState.wallet.totalTradesCompleted,
            misprintCount = misprintCount,
            motto = "Collector of $uniqueSkinCount unique skins and $misprintCount 1/1 Misprints.",
            badgeIds = pinnedBadges.map { it.definition.id },
            showcaseSkins = topSkins
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("profile_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card (Google Account + Icon-only Pinned Badges)
        item {
            FrostedGlassCard(cornerRadius = 24.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val equippedFrame = remember(uiState.wallet.equippedFrameId) {
                                CosmeticCatalog.frameById(uiState.wallet.equippedFrameId)
                            }
                            val displayAlias = uiState.wallet.collectorAlias.ifBlank {
                                currentUser?.displayName ?: "Dawn Collector"
                            }
                            FramedAvatar(
                                photoUrl = currentUser?.photoUrl.orEmpty(),
                                alias = displayAlias,
                                frame = equippedFrame,
                                size = 66.dp,
                                fallbackTint = palette.buttonPrimary,
                                modifier = Modifier.testTag("profile_equipped_avatar")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = displayAlias,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = palette.ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    IconButton(
                                        onClick = {
                                            onButtonClickSfx()
                                            showEditAlias = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit collector name",
                                            tint = palette.muted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (!currentUser?.email.isNullOrBlank()) {
                                    Text(
                                        text = currentUser?.email.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.muted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MlbbCollectionBadge(
                                        tier = collectionTier,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$collectorRank • $totalPrestige Power",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.accentPrimary
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsGearButton(onClick = onOpenSettings)
                            if (currentUser != null) {
                                IconButton(
                                    onClick = {
                                        onButtonClickSfx()
                                        onSignOut()
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Sign out of Google account",
                                        tint = palette.muted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (currentUser == null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = palette.frostSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("optional_google_signin_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Preview / Guest Collector Mode",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.ink
                                        )
                                        Text(
                                            text = "Optional: Sign in with Google for cloud backup & live P2P",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.muted
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        onButtonClickSfx()
                                        onGoogleSignInClick(context)
                                    },
                                    enabled = !isSigningIn,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.buttonPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(999.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("profile_google_signin_button")
                                ) {
                                    Text(
                                        text = if (isSigningIn) "..." else "Sign In",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CoinsPill(coins = uiState.wallet.coins)
                            DiamondsPill(diamonds = uiState.wallet.diamonds)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onButtonClickSfx()
                                    showEditCosmetics = true
                                },
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.55f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("profile_edit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.accentPrimary
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    onButtonClickSfx()
                                    showSelfProfilePreview = true
                                },
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.55f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("profile_preview_public_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Public Card",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.accentPrimary
                                )
                            }
                        }
                    }

                    // Pinned Showcase Badges: Icon-Only Medallions (text shows on tap/hover!)
                    if (pinnedBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            pinnedBadges.forEach { badge ->
                                val def = badge.definition
                                val selected = inspectedHeaderBadgeId == def.id
                                Surface(
                                    color = if (selected) palette.buttonPrimary.copy(alpha = 0.16f) else palette.frostSurface,
                                    shape = CircleShape,
                                    border = BorderStroke(
                                        width = if (selected) 2.dp else 1.2.dp,
                                        color = palette.accentPrimary.copy(alpha = if (selected) 1f else 0.55f)
                                    ),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .pointerInput(def.id) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    when (event.type) {
                                                        PointerEventType.Enter -> inspectedHeaderBadgeId = def.id
                                                        PointerEventType.Exit -> if (inspectedHeaderBadgeId == def.id) inspectedHeaderBadgeId = null
                                                    }
                                                }
                                            }
                                        }
                                        .clickable {
                                            onButtonClickSfx()
                                            inspectedHeaderBadgeId = if (selected) null else def.id
                                        }
                                        .testTag("pinned_header_badge_${def.id}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = BadgeIconVector(def.iconStyle),
                                            contentDescription = def.title,
                                            tint = palette.accentPrimary,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tap / hover badge",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.muted
                            )
                        }
                        AnimatedVisibility(visible = inspectedHeaderBadge != null) {
                            inspectedHeaderBadge?.let { b ->
                                Surface(
                                    color = palette.frostSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = "${b.definition.title}: ${b.definition.description}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.ink,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // KPI Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "Total Cards",
                    value = "${uiState.ownedCards.size}",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Unique Skins",
                    value = "$uniqueSkinCount",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "1/1 Misprints",
                    value = "$misprintCount",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "Packs Opened",
                    value = "${uiState.wallet.totalPacksOpened}",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Trades Done",
                    value = "${uiState.wallet.totalTradesCompleted}",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Daily Streak",
                    value = "${uiState.wallet.dailyStreakCount}d",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Official MLBB Collection Level Card (shows active badge only; tap to view all 8 obtainable badges)
        item {
            val nextTier = collectionTier.nextTier()
            val tierSpan = ((nextTier?.minPower ?: totalPrestige) - collectionTier.minPower).coerceAtLeast(1)
            val tierProgress = if (nextTier == null) {
                1f
            } else {
                ((totalPrestige - collectionTier.minPower).toFloat() / tierSpan.toFloat()).coerceIn(0f, 1f)
            }
            FrostedGlassCard(
                cornerRadius = 22.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        onButtonClickSfx()
                        showCollectionLadderDialog = true
                    }
                    .testTag("collection_level_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLLECTION LEVEL",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.ink
                        )
                        Surface(
                            color = palette.frostSurface,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text(
                                text = "Lv.${collectionTier.levelNumber}/${MlbbCollectionTier.entries.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.accentPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = palette.frostSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.8.dp, palette.accentPrimary),
                            modifier = Modifier
                                .size(70.dp)
                                .testTag("collection_current_badge")
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MlbbCollectionBadge(
                                    tier = collectionTier,
                                    dimmed = false,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = collectionTier.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = palette.ink
                            )
                            Text(
                                text = if (nextTier != null) {
                                    if (totalPrestige >= nextTier.minPower && uniqueLegends < nextTier.legendsRequired) {
                                        "Own ${nextTier.legendsRequired} Legend skin(s) to reach ${nextTier.title}"
                                    } else {
                                        "${(nextTier.minPower - totalPrestige).coerceAtLeast(0)} Power to ${nextTier.title}"
                                    }
                                } else {
                                    "Max Tier Unlocked: World Collector!"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = palette.muted
                            )
                            LinearProgressIndicator(
                                progress = { tierProgress },
                                color = palette.accentPrimary,
                                trackColor = palette.frostSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap to view all ${MlbbCollectionTier.entries.size} obtainable badges",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.accentPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View all Collection Level badges",
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (showCollectionLadderDialog) {
                Dialog(onDismissRequest = { showCollectionLadderDialog = false }) {
                    FrostedGlassCard(
                        cornerRadius = 24.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("collection_level_dialog")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "All Collection Badges",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = palette.ink
                                    )
                                    Text(
                                        text = "World, then Mega down to Novice, each with 3 levels (III, II, I)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                        color = palette.muted
                                    )
                                }
                                IconButton(
                                    onClick = { showCollectionLadderDialog = false },
                                    modifier = Modifier.testTag("collection_level_dialog_close")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close collection badges dialog",
                                        tint = palette.ink
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("collection_level_ladder"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MlbbCollectionTier.RAREST_LEFT_TO_RIGHT.chunked(4).forEach { rowTiers ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowTiers.forEach { tier ->
                                            val isCurrent = tier == collectionTier
                                            val isUnlocked = totalPrestige >= tier.minPower && uniqueLegends >= tier.legendsRequired
                                            Surface(
                                                color = if (isCurrent) palette.frostSurface else palette.canvas,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(
                                                    width = if (isCurrent) 2.dp else 1.dp,
                                                    color = if (isCurrent) palette.accentPrimary else palette.border
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("collection_tier_${tier.levelNumber}")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.size(46.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        MlbbCollectionBadge(
                                                            tier = tier,
                                                            dimmed = !isUnlocked,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    Text(
                                                        text = tier.shortLabel,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        ),
                                                        color = if (isCurrent) palette.accentPrimary else palette.ink,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = if (isCurrent) "ACTIVE" else "${tier.minPower} PWR",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                        color = if (isCurrent) palette.accentPrimary else palette.muted,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MLBBDex API Sync Banner
        item {
            CatalogSyncStatusCard(
                syncStatus = uiState.syncStatus,
                lastSyncedMs = uiState.wallet.catalogLastSyncedMs,
                onRefresh = onSyncCatalog
            )
        }

        // Achievements & Showcase Badges Section (Icon-Only Grid; text revealed on tap/hover!)
        item {
            FrostedGlassCard(cornerRadius = 22.dp) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACHIEVEMENT BADGES",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.ink
                            )
                            Text(
                                text = "Tap or hover any medallion to view progress, rewards & pin.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = palette.muted
                            )
                        }
                        Surface(
                            color = palette.frostSurface,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text(
                                text = "$unlockedCount/${uiState.achievements.size} • ${pinnedBadges.size}/${AchievementCatalog.MAX_PINNED_BADGES}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.accentPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // 4-Column Icon-Only Medallion Grid
                    uiState.achievements.chunked(4).forEach { rowBadges ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowBadges.forEach { badge ->
                                val isInspected = inspectedBadgeId == badge.definition.id
                                AchievementIconMedallion(
                                    badge = badge,
                                    isInspected = isInspected,
                                    onHoverEnter = { inspectedBadgeId = badge.definition.id },
                                    onHoverExit = { /* keep last inspected open or let tap toggle */ },
                                    onClick = {
                                        onButtonClickSfx()
                                        inspectedBadgeId = if (isInspected) null else badge.definition.id
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - rowBadges.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Tooltip / Inspector Card revealed on Tap or Hover
                    AnimatedVisibility(visible = inspectedBadge != null) {
                        inspectedBadge?.let { badge ->
                            val def = badge.definition
                            Surface(
                                color = palette.frostSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, palette.accentPrimary.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("achievement_inspector_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = BadgeIconVector(def.iconStyle),
                                                contentDescription = def.title,
                                                tint = palette.accentPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = def.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = palette.ink
                                                )
                                                Text(
                                                    text = def.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = palette.muted
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${badge.currentProgress.coerceAtMost(def.targetCount)}/${def.targetCount}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (badge.isUnlocked) palette.accentPrimary else palette.muted
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { badge.progressFraction },
                                        color = palette.accentPrimary,
                                        trackColor = palette.cardSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                    )

                                    if (def.hasReward && def.rewardSummaryText.isNotBlank()) {
                                        Surface(
                                            color = palette.cardSurface,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (badge.canClaim) palette.gold else palette.border
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = if (badge.isClaimed) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                                                        contentDescription = null,
                                                        tint = if (badge.canClaim) palette.gold else palette.accentPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Reward: ${def.rewardSummaryText}",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = palette.ink,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                if (badge.isClaimed) {
                                                    Text(
                                                        text = "CLAIMED",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = palette.accentPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (badge.canClaim) {
                                        Button(
                                            onClick = { onClaimAchievementReward(badge) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = palette.gold,
                                                contentColor = palette.goldButtonContent
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("claim_achievement_reward_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CardGiftcard,
                                                contentDescription = null,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Claim Reward",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                            )
                                        }
                                    }

                                    if (badge.isUnlocked) {
                                        Button(
                                            onClick = { onTogglePinnedBadge(badge) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (badge.isPinned) palette.muted else palette.buttonPrimary,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("toggle_pin_badge_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (badge.isPinned) "Unpin from Profile" else "Pin to Profile",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloud Activity History
        item {
            FrostedGlassCard(cornerRadius = 22.dp) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COLLECTOR HISTORY",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.ink
                        )
                    }

                    if (uiState.historyEvents.isEmpty()) {
                        Text(
                            text = "Open packs, trade skins, or spin the wheel to log your cloud history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted
                        )
                    } else {
                        uiState.historyEvents.take(12).forEach { event ->
                            HistoryEventRow(event = event)
                        }
                    }
                }
            }
        }

        // 7-Tier Rarity Distribution Breakdown
        item {
            FrostedGlassCard(cornerRadius = 22.dp) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "COLLECTION BY RARITY",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.ink
                    )
                    val total = uiState.ownedCards.size.coerceAtLeast(1)
                    SkinRarity.entries.reversed().forEach { rarity ->
                        val count = uiState.ownedCards.count { it.rarity == rarity }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = rarity.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = rarity.primaryColor
                                )
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.ink
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / total.toFloat() },
                                color = rarity.primaryColor,
                                trackColor = palette.frostSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = palette.misprintLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1-of-1 Misprint Anomalies",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.misprintLabel
                            )
                        }
                        Text(
                            text = "$misprintCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.misprintLabel
                        )
                    }
                }
            }
        }

        // Credits & MLBBDex.com Attribution
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MlbbDexCreditCard(skinCount = uiState.catalog.size)
                Surface(
                    color = palette.cardSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.api_credit_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accentPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.api_credit_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                    }
                }
            }
        }
    }

    if (showEditCosmetics) {
        EditProfileCosmeticsDialog(
            equippedFrameId = uiState.wallet.equippedFrameId,
            unlockedFrameIds = uiState.wallet.unlockedFrameIds,
            equippedBinderId = uiState.wallet.equippedBinderId,
            unlockedBinderIds = uiState.wallet.unlockedBinderIds,
            onEquipFrame = onEquipFrame,
            onEquipBinder = onEquipBinder,
            onEditAlias = {
                showEditCosmetics = false
                showEditAlias = true
            },
            onDismiss = {
                onButtonClickSfx()
                showEditCosmetics = false
            }
        )
    }

    if (showEditAlias) {
        var draftAlias by remember { mutableStateOf(uiState.wallet.collectorAlias) }
        AlertDialog(
            onDismissRequest = { showEditAlias = false },
            containerColor = palette.cardSurface,
            title = { Text("Collector Tag", color = palette.ink) },
            text = {
                OutlinedTextField(
                    value = draftAlias,
                    onValueChange = { draftAlias = it },
                    singleLine = true,
                    label = { Text("Display Alias") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAlias(draftAlias)
                        showEditAlias = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCobalt,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        showEditAlias = false
                    }
                ) {
                    Text("Cancel", color = palette.muted)
                }
            }
        )
    }

    if (showSelfProfilePreview) {
        ProfileViewerDialog(
            profile = selfPublicProfile,
            equippedFrameId = uiState.wallet.equippedFrameId,
            onDismiss = {
                onButtonClickSfx()
                showSelfProfilePreview = false
            }
        )
    }
}

@Composable
private fun AchievementIconMedallion(
    badge: AchievementBadgeUi,
    isInspected: Boolean,
    onHoverEnter: () -> Unit,
    onHoverExit: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val def = badge.definition
    val borderColor = when {
        isInspected -> palette.buttonPrimary
        badge.canClaim -> palette.gold
        badge.isPinned -> palette.accentPrimary
        badge.isUnlocked -> palette.accentPrimary.copy(alpha = 0.55f)
        else -> palette.border
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(def.id) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> onHoverEnter()
                            PointerEventType.Exit -> onHoverExit()
                        }
                    }
                }
            }
            .clickable(onClick = onClick)
            .testTag("achievement_badge_${def.id}"),
        color = if (badge.isUnlocked) palette.frostSurface else palette.canvas,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isInspected || badge.isPinned || badge.canClaim) 2.dp else 1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = BadgeIconVector(def.iconStyle),
                contentDescription = def.title,
                tint = if (badge.isUnlocked) palette.accentPrimary else palette.muted.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(28.dp)
                    .alpha(if (badge.isUnlocked) 1f else 0.55f)
            )
            if (badge.canClaim) {
                Surface(
                    color = palette.gold,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Reward ready to claim",
                            tint = palette.goldButtonContent,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            if (badge.isPinned) {
                Surface(
                    color = palette.buttonPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            } else if (!badge.isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = palette.muted.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                        .size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryEventRow(event: CollectorHistoryEvent) {
    val palette = CollectorTheme.colors
    val icon = when (event.eventType) {
        CollectorHistoryType.PULL -> Icons.Default.Style
        CollectorHistoryType.TRADE -> Icons.Default.SwapHoriz
        CollectorHistoryType.RECYCLE -> Icons.Default.Recycling
        CollectorHistoryType.MINIGAME -> Icons.Default.Casino
        CollectorHistoryType.PROFILE -> Icons.Default.Person
    }
    val tint = when (event.eventType) {
        CollectorHistoryType.PULL -> palette.accentPrimary
        CollectorHistoryType.TRADE -> palette.accentPrimary
        CollectorHistoryType.RECYCLE -> palette.accentPrimary
        CollectorHistoryType.MINIGAME -> palette.accentPrimary
        CollectorHistoryType.PROFILE -> palette.muted
    }
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.US) }

    Surface(
        color = palette.frostSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = tint.copy(alpha = 0.14f),
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = event.eventType.label,
                        tint = tint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (event.detail.isNotBlank()) {
                    Text(
                        text = event.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (event.coinDelta != 0) {
                    Text(
                        text = "${if (event.coinDelta > 0) "+" else ""}${event.coinDelta} 🪙",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.accentPrimary
                    )
                }
                if (event.diamondDelta != 0) {
                    Text(
                        text = "${if (event.diamondDelta > 0) "+" else ""}${event.diamondDelta} 💎",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.accentPrimary
                    )
                }
                Text(
                    text = formatter.format(Date(event.createdAtMs)),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = palette.muted
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    FrostedGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted
            )
        }
    }
}


/** Pick your profile frame and your binder. Locked items are bought in Packs > Cosmetics. */
@Composable
private fun EditProfileCosmeticsDialog(
    equippedFrameId: String,
    unlockedFrameIds: Set<String>,
    equippedBinderId: String,
    unlockedBinderIds: Set<String>,
    onEquipFrame: (ProfileFrameItem) -> Unit,
    onEquipBinder: (CardBinderDesign) -> Unit,
    onEditAlias: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = CollectorTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardSurface,
        title = { Text("Edit profile", color = palette.ink) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = onEditAlias,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.55f)),
                    modifier = Modifier.testTag("profile_edit_alias_button")
                ) {
                    Text("Change collector tag", color = palette.accentPrimary)
                }

                Text(
                    text = "PROFILE FRAME",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.muted
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CosmeticCatalog.FRAMES.forEach { frame ->
                        val owned = frame.id in unlockedFrameIds
                        CosmeticPickChip(
                            name = frame.name,
                            swatch = listOf(frame.primaryColor, frame.secondaryColor),
                            owned = owned,
                            equipped = frame.id == equippedFrameId,
                            testTag = "profile_pick_frame_${frame.id}",
                            onClick = { if (owned) onEquipFrame(frame) }
                        )
                    }
                }

                Text(
                    text = "BINDER",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.muted
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CosmeticCatalog.BINDERS.forEach { binder ->
                        val owned = binder.id in unlockedBinderIds
                        CosmeticPickChip(
                            name = binder.name,
                            swatch = listOf(binder.coverPrimary, binder.coverSecondary),
                            owned = owned,
                            equipped = binder.id == equippedBinderId,
                            testTag = "profile_pick_binder_${binder.id}",
                            onClick = { if (owned) onEquipBinder(binder) }
                        )
                    }
                }

                Text(
                    text = "Greyed items are not unlocked yet. Buy them in Packs > Cosmetics.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = palette.accentPrimary)
            }
        }
    )
}

@Composable
private fun CosmeticPickChip(
    name: String,
    swatch: List<Color>,
    owned: Boolean,
    equipped: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val palette = CollectorTheme.colors
    Surface(
        color = if (equipped) palette.frostSurface else palette.cardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            if (equipped) 2.dp else 1.dp,
            if (equipped) palette.accentPrimary else palette.glassHighlight
        ),
        modifier = Modifier
            .width(92.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = owned, onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            swatch.map { it.copy(alpha = if (owned) 1f else 0.35f) }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!owned) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                } else if (equipped) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Equipped",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = if (owned) palette.ink else palette.muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
