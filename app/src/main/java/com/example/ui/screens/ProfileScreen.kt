package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AchievementBadgeUi
import com.example.model.AchievementCatalog
import com.example.model.BazaarCollectorProfile
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.ui.components.BadgeIconVector
import com.example.ui.components.CatalogSyncStatusCard
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.components.ProfileViewerDialog
import com.example.ui.components.SettingsGearButton
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

@Composable
fun ProfileScreen(
    uiState: MegaCollectorUiState,
    onUpdateAlias: (String) -> Unit,
    onSyncCatalog: () -> Unit,
    onTogglePinnedBadge: (AchievementBadgeUi) -> Unit,
    onOpenSettings: () -> Unit = {},
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var showEditAlias by remember { mutableStateOf(false) }
    var showSelfProfilePreview by remember { mutableStateOf(false) }

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

    val selfPublicProfile = remember(
        uiState.wallet,
        uiState.ownedCards,
        collectorRank,
        totalPrestige,
        uniqueSkinCount,
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

        BazaarCollectorProfile(
            traderId = "self",
            alias = uiState.wallet.collectorAlias,
            rankTitle = collectorRank,
            avatarColor = palette.accentPrimary,
            prestigeScore = totalPrestige,
            uniqueSkinsCount = uniqueSkinCount,
            packsOpened = uiState.wallet.totalPacksOpened,
            tradesDone = uiState.wallet.totalTradesCompleted,
            motto = "Collector of $uniqueSkinCount unique skins and $misprintCount 1/1 Misprints.",
            badgeIds = pinnedBadges.map { it.definition.id },
            showcaseSkins = topSkins
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PearlCanvas)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 3.dp,
                border = BorderStroke(1.2.dp, palette.border)
            ) {
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
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(palette.accentPrimary, StudioGold)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Collector avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.wallet.collectorAlias,
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

                        SettingsGearButton(onClick = onOpenSettings)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
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

                    if (pinnedBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pinnedBadges.forEach { badge ->
                                val def = badge.definition
                                Surface(
                                    color = def.accentColor.copy(alpha = 0.13f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, def.accentColor.copy(alpha = 0.55f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = BadgeIconVector(def.iconStyle),
                                            contentDescription = def.title,
                                            tint = def.accentColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = def.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = InkSlate,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
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

        // MLBBDex API Sync Banner (moved from Main Shop to Profile)
        item {
            CatalogSyncStatusCard(
                syncStatus = uiState.syncStatus,
                lastSyncedMs = uiState.wallet.catalogLastSyncedMs,
                onRefresh = onSyncCatalog
            )
        }

        // Achievements & Showcase Badges Section
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder)
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
                                text = "ACHIEVEMENT BADGES",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkSlate
                            )
                            Text(
                                text = "Tap any unlocked badge to pin up to ${AchievementCatalog.MAX_PINNED_BADGES} onto your profile.",
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

                    uiState.achievements.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { badge ->
                                AchievementBadgeTile(
                                    badge = badge,
                                    onClick = { onTogglePinnedBadge(badge) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 7-Tier Rarity Distribution Breakdown
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder)
            ) {
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
private fun AchievementBadgeTile(
    badge: AchievementBadgeUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val def = badge.definition
    val borderColor = when {
        badge.isPinned -> StudioGold
        badge.isUnlocked -> def.accentColor.copy(alpha = 0.55f)
        else -> palette.border
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = badge.isUnlocked, onClick = onClick)
            .testTag("achievement_badge_${def.id}"),
        color = if (badge.isUnlocked) palette.frostSurface else palette.canvas.copy(alpha = 0.7f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (badge.isPinned) 1.6.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (badge.isUnlocked) def.accentColor.copy(alpha = 0.16f) else palette.cardSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = BadgeIconVector(def.iconStyle),
                            contentDescription = def.title,
                            tint = if (badge.isUnlocked) def.accentColor else palette.muted.copy(alpha = 0.5f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                when {
                    badge.isPinned -> Surface(
                        color = StudioGold,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = palette.goldButtonContent,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Pinned",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = palette.goldButtonContent
                            )
                        }
                    }

                    !badge.isUnlocked -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = palette.muted.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = def.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (badge.isUnlocked) InkSlate else SlateMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = def.description,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = SlateMuted,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            LinearProgressIndicator(
                progress = { badge.progressFraction },
                color = if (badge.isUnlocked) def.accentColor else palette.accentPrimary,
                trackColor = palette.cardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
            )

            Text(
                text = "${badge.currentProgress.coerceAtMost(def.targetCount)}/${def.targetCount}" +
                    if (badge.isUnlocked && !badge.isPinned) " • Tap to Pin" else "",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (badge.isUnlocked) def.accentColor else SlateMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    Surface(
        modifier = modifier,
        color = PureWhiteCard,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, IceBorder)
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
