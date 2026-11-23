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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.remote.AuthenticatedUser
import com.example.model.AchievementBadgeUi
import com.example.model.AchievementCatalog
import com.example.model.CollectorHistoryEvent
import com.example.model.CollectorHistoryType
import com.example.model.CollectorPublicProfile
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.ui.components.BadgeIconVector
import com.example.ui.components.CatalogSyncStatusCard
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.components.ProfileViewerDialog
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.rememberUserAvatarRequest
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldText
import com.example.ui.viewmodel.MegaCollectorUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onOpenSettings: () -> Unit = {},
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val context = LocalContext.current
    var showEditAlias by remember { mutableStateOf(false) }
    var showSelfProfilePreview by remember { mutableStateOf(false) }
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

    val collectorRank = remember(totalPrestige) {
        when {
            totalPrestige >= 15000 -> "Immortal Collector"
            totalPrestige >= 7500 -> "Mythical Glory"
            totalPrestige >= 3500 -> "Mythic"
            totalPrestige >= 1500 -> "Legend"
            totalPrestige >= 500 -> "Epic"
            else -> "Warrior"
        }
    }

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
            .statusBarsPadding(),
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
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(palette.accentPrimary, StudioGold)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val photo = currentUser?.photoUrl.orEmpty()
                                if (photo.isNotBlank()) {
                                    AsyncImage(
                                        model = rememberUserAvatarRequest(photo),
                                        contentDescription = "Google account photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Collector avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.wallet.collectorAlias.ifBlank {
                                            currentUser?.displayName ?: "Dawn Collector"
                                        },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = InkSlate,
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
                                            tint = SlateMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (!currentUser?.email.isNullOrBlank()) {
                                    Text(
                                        text = currentUser?.email.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = StudioGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$collectorRank • $totalPrestige Power",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = StudioGoldText
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
                                        tint = SlateMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (currentUser == null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = SkyFrost,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, palette.mintAccent.copy(alpha = 0.55f)),
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
                                            color = InkSlate
                                        )
                                        Text(
                                            text = "Optional: Sign in with Google for cloud backup & live P2P",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateMuted
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
                                        containerColor = DeepCobalt,
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
                                    color = def.accentColor.copy(alpha = if (selected) 0.24f else 0.14f),
                                    shape = CircleShape,
                                    border = BorderStroke(
                                        width = if (selected) 2.dp else 1.2.dp,
                                        color = def.accentColor.copy(alpha = if (selected) 1f else 0.65f)
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
                                            tint = def.accentColor,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tap / hover badge",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateMuted
                            )
                        }
                        AnimatedVisibility(visible = inspectedHeaderBadge != null) {
                            inspectedHeaderBadge?.let { b ->
                                Surface(
                                    color = b.definition.accentColor.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, b.definition.accentColor.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = "${b.definition.title}: ${b.definition.description}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkSlate,
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
                    accent = StudioGoldText,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "1/1 Misprints",
                    value = "$misprintCount",
                    accent = palette.misprintLabel,
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
                    accent = InkSlate,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Trades Done",
                    value = "${uiState.wallet.totalTradesCompleted}",
                    accent = RarityUncommon,
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
                                color = InkSlate
                            )
                            Text(
                                text = "Tap or hover any medallion to view progress & pin.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = SlateMuted
                            )
                        }
                        Surface(
                            color = SkyFrost,
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
                                color = SkyFrost,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, def.accentColor.copy(alpha = 0.6f)),
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
                                                tint = def.accentColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = def.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = InkSlate
                                                )
                                                Text(
                                                    text = def.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SlateMuted
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${badge.currentProgress.coerceAtMost(def.targetCount)}/${def.targetCount}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (badge.isUnlocked) def.accentColor else SlateMuted
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { badge.progressFraction },
                                        color = if (badge.isUnlocked) def.accentColor else palette.accentPrimary,
                                        trackColor = PureWhiteCard,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                    )

                                    if (badge.isUnlocked) {
                                        Button(
                                            onClick = { onTogglePinnedBadge(badge) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (badge.isPinned) SlateMuted else StudioGold,
                                                contentColor = if (badge.isPinned) Color.White else palette.goldButtonContent
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
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
                            color = InkSlate
                        )
                    }

                    if (uiState.historyEvents.isEmpty()) {
                        Text(
                            text = "Open packs, trade skins, or spin the wheel to log your cloud history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
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
                        color = InkSlate
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
                                    color = InkSlate
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / total.toFloat() },
                                color = rarity.primaryColor,
                                trackColor = SkyFrost,
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
                    color = PureWhiteCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IceBorder)
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
                            color = SlateMuted
                        )
                    }
                }
            }
        }
    }

    if (showEditAlias) {
        var draftAlias by remember { mutableStateOf(uiState.wallet.collectorAlias) }
        AlertDialog(
            onDismissRequest = { showEditAlias = false },
            containerColor = PureWhiteCard,
            title = { Text("Collector Tag", color = InkSlate) },
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
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }

    if (showSelfProfilePreview) {
        ProfileViewerDialog(
            profile = selfPublicProfile,
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
        isInspected -> DeepCobalt
        badge.isPinned -> StudioGold
        badge.isUnlocked -> def.accentColor.copy(alpha = 0.65f)
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
        color = if (badge.isUnlocked) def.accentColor.copy(alpha = 0.14f) else palette.canvas.copy(alpha = 0.72f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isInspected || badge.isPinned) 2.dp else 1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = BadgeIconVector(def.iconStyle),
                contentDescription = def.title,
                tint = if (badge.isUnlocked) def.accentColor else palette.muted.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(28.dp)
                    .alpha(if (badge.isUnlocked) 1f else 0.55f)
            )
            if (badge.isPinned) {
                Surface(
                    color = StudioGold,
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
                            tint = palette.goldButtonContent,
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
        CollectorHistoryType.PULL -> event.rarity?.primaryColor ?: palette.accentPrimary
        CollectorHistoryType.TRADE -> RarityUncommon
        CollectorHistoryType.RECYCLE -> StudioGoldText
        CollectorHistoryType.MINIGAME -> palette.accentPrimary
        CollectorHistoryType.PROFILE -> SlateMuted
    }
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.US) }

    Surface(
        color = SkyFrost,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, IceBorder),
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
                    color = InkSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (event.detail.isNotBlank()) {
                    Text(
                        text = event.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted,
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
                        color = StudioGoldText
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
                    color = SlateMuted
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
                color = SlateMuted
            )
        }
    }
}
