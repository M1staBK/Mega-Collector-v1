package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BoosterPack
import com.example.model.DailyRewardTier
import com.example.model.SkinRarity
import com.example.ui.components.ArenaGoldPill
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.MythicCrimson
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.SurfaceAbyss
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import com.example.ui.viewmodel.CatalogSyncStatus
import com.example.ui.viewmodel.MegaCollectorUiState

@Composable
fun ShopPacksScreen(
    uiState: MegaCollectorUiState,
    dailyRewardTiers: List<DailyRewardTier>,
    onClaimDailyReward: () -> Unit,
    onOpenPack: (BoosterPack, Boolean) -> Unit,
    onSyncCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var oddsPackDialog by remember { mutableStateOf<BoosterPack?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoidObsidian),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(234.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_arena_banner),
                    contentDescription = "MEGA COLLECTOR Arena Vault Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    VoidObsidian.copy(alpha = 0.45f),
                                    VoidObsidian.copy(alpha = 0.25f),
                                    VoidObsidian
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MEGA COLLECTOR",
                                style = MaterialTheme.typography.headlineLarge,
                                color = ArenaGold
                            )
                            Text(
                                text = "MOBA Legends Card Vault",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary.copy(alpha = 0.85f)
                            )
                        }
                        ArenaGoldPill(gold = uiState.wallet.arenaGold)
                    }

                    // MLBBDex live sync status pill
                    SyncStatusBanner(
                        syncStatus = uiState.syncStatus,
                        onRetry = onSyncCatalog
                    )
                }
            }
        }

        // Daily Streak Rewards Strip
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ArenaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAILY STREAK REWARDS",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    if (uiState.isDailyRewardClaimable) {
                        Button(
                            onClick = onClaimDailyReward,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ArenaGold,
                                contentColor = VoidObsidian
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("claim_daily_reward_button")
                        ) {
                            Text(
                                text = "Claim Day ${uiState.nextDailyDayIndex}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, OutlineSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrismCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Streak Day ${uiState.wallet.dailyStreakCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrismCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dailyRewardTiers) { tier ->
                        val claimed = if (uiState.isDailyRewardClaimable) {
                            tier.dayIndex < uiState.nextDailyDayIndex
                        } else {
                            tier.dayIndex <= uiState.wallet.dailyStreakCount
                        }
                        val isToday = uiState.isDailyRewardClaimable && tier.dayIndex == uiState.nextDailyDayIndex

                        DailyTierTile(
                            tier = tier,
                            isClaimed = claimed,
                            isCurrentClaimable = isToday,
                            onClick = { if (isToday) onClaimDailyReward() }
                        )
                    }
                }
            }
        }

        // Booster Packs Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        tint = PrismCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BOOSTER PACK SHOP",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "1/1 MISPRINTS LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MisprintNeon
                )
            }
        }

        // Booster Pack Cards
        items(BoosterPack.entries) { pack ->
            val freeTicketCount = when (pack) {
                BoosterPack.STARTER_PACK -> uiState.wallet.bonusFreePacksStarter
                BoosterPack.MYTHIC_PRISM_PACK -> uiState.wallet.bonusFreePacksMythic
                BoosterPack.IMPERIAL_LEGEND_VAULT -> 0
            }
            val catalogReady = uiState.catalog.isNotEmpty()
            val canAfford = uiState.wallet.arenaGold >= pack.costGold || freeTicketCount > 0

            BoosterPackShopCard(
                pack = pack,
                freeTickets = freeTicketCount,
                catalogReady = catalogReady,
                canAfford = canAfford,
                isOpening = uiState.isOpeningPack,
                onOpenWithGold = { onOpenPack(pack, false) },
                onOpenWithTicket = { onOpenPack(pack, true) },
                onShowOdds = { oddsPackDialog = pack },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // MLBBDex API Credits Card
        item {
            MlbbDexCreditCard(
                skinCount = uiState.catalog.size,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    oddsPackDialog?.let { pack ->
        PackOddsDialog(pack = pack, onDismiss = { oddsPackDialog = null })
    }
}

@Composable
private fun SyncStatusBanner(
    syncStatus: CatalogSyncStatus,
    onRetry: () -> Unit
) {
    Surface(
        color = SurfaceAbyss.copy(alpha = 0.88f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            when (syncStatus) {
                is CatalogSyncStatus.Error -> MythicCrimson
                is CatalogSyncStatus.Synced -> PrismCyan.copy(alpha = 0.45f)
                else -> OutlineSubtle
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                when (syncStatus) {
                    CatalogSyncStatus.Syncing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PrismCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Syncing skins from mlbbdex.com...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    is CatalogSyncStatus.Synced -> {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrismCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MLBBDex Live: ${syncStatus.count} skins ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    is CatalogSyncStatus.Error -> {
                        Text(
                            text = "API Sync failed: ${syncStatus.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MythicCrimson,
                            maxLines = 2
                        )
                    }
                    CatalogSyncStatus.Idle -> {
                        Text(
                            text = "Initializing MLBBDex catalog...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (syncStatus is CatalogSyncStatus.Error || syncStatus is CatalogSyncStatus.Synced) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh MLBBDex catalog",
                        tint = if (syncStatus is CatalogSyncStatus.Error) ArenaGold else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyTierTile(
    tier: DailyRewardTier,
    isClaimed: Boolean,
    isCurrentClaimable: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCurrentClaimable -> ArenaGold
        isClaimed -> PrismCyan.copy(alpha = 0.5f)
        else -> OutlineSubtle
    }
    Surface(
        color = if (isCurrentClaimable) SurfaceCard else SurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isCurrentClaimable) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = isCurrentClaimable, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAY ${tier.dayIndex}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrentClaimable) ArenaGold else TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "+${tier.goldAmount}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isClaimed) TextSecondary else ArenaGold
            )
            Text(
                text = "GOLD",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            if (tier.bonusPack != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MisprintMagenta.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+PACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MisprintMagenta,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            if (isClaimed) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Claimed",
                    tint = PrismCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun BoosterPackShopCard(
    pack: BoosterPack,
    freeTickets: Int,
    catalogReady: Boolean,
    canAfford: Boolean,
    isOpening: Boolean,
    onOpenWithGold: () -> Unit,
    onOpenWithTicket: () -> Unit,
    onShowOdds: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, pack.accentColor.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(132.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, pack.accentColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = pack.coverDrawableRes),
                    contentDescription = "${pack.title} cover art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = VoidObsidian.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "${pack.cardsPerPack} CARDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = pack.accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pack.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onShowOdds,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "View drop rates for ${pack.title}",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = pack.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MisprintNeon.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MisprintNeon.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MisprintNeon,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "1/${pack.misprintOneInDenominator} MISPRINT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MisprintNeon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (freeTickets > 0) {
                    Button(
                        onClick = onOpenWithTicket,
                        enabled = catalogReady && !isOpening,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrismCyan,
                            contentColor = VoidObsidian
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_free_${pack.id}_button")
                    ) {
                        Text(
                            text = "OPEN FREE TICKET ($freeTickets LEFT)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Button(
                        onClick = onOpenWithGold,
                        enabled = catalogReady && canAfford && !isOpening,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pack.accentColor,
                            contentColor = if (pack == BoosterPack.IMPERIAL_LEGEND_VAULT) Color.White else VoidObsidian
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_pack_${pack.id}_button")
                    ) {
                        Text(
                            text = if (canAfford) "BUY & OPEN • %,d GOLD".format(pack.costGold)
                            else "NEED %,d GOLD".format(pack.costGold),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackOddsDialog(
    pack: BoosterPack,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        title = {
            Text(
                text = "${pack.title} Pull Rates",
                style = MaterialTheme.typography.headlineMedium,
                color = ArenaGold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = pack.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkinRarity.entries.reversed().forEach { rarity ->
                    val weight = pack.rarityWeights[rarity] ?: 0.0
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
                            text = "${weight}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1-of-1 Misprint Chance: 1 in ${pack.misprintOneInDenominator} cards rolled!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MisprintNeon
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = ArenaGold)
            }
        }
    )
}
