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
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DailyRewardTier
import com.example.model.DiamondExchangeOption
import com.example.model.MegaPackConfig
import com.example.model.PackBundleOption
import com.example.model.SkinRarity
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.components.SettingsGearButton
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldText
import com.example.ui.viewmodel.MegaCollectorUiState
import java.util.Locale

@Composable
fun ShopPacksScreen(
    uiState: MegaCollectorUiState,
    dailyRewardTiers: List<DailyRewardTier>,
    onClaimDailyReward: () -> Unit,
    onOpenPackBundle: (PackBundleOption, Boolean, Boolean) -> Unit,
    onExchangeDiamondsForCoins: (DiamondExchangeOption) -> Unit,
    onOpenSettings: () -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var showOddsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.16f else 0.35f),
                        palette.canvas
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Studio Hero Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(198.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_arena_banner),
                        contentDescription = "MEGA COLLECTOR Studio Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        palette.heroTopScrim,
                                        palette.heroMidScrim,
                                        palette.canvas
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MEGA COLLECTOR",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = palette.accentPrimary
                                )
                                Text(
                                    text = if (palette.isDark) {
                                        "MOBA Legends Moonlight Card Studio"
                                    } else {
                                        "MOBA Legends Sky Card Studio"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSlate
                                )
                            }
                            SettingsGearButton(onClick = onOpenSettings)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinsPill(coins = uiState.wallet.coins)
                            DiamondsPill(diamonds = uiState.wallet.diamonds)
                        }
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
                                tint = StudioGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Streak Rewards",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkSlate
                            )
                        }

                        if (uiState.isDailyRewardClaimable) {
                            Button(
                                onClick = onClaimDailyReward,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioGold,
                                    contentColor = palette.goldButtonContent
                                ),
                                shape = RoundedCornerShape(999.dp),
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
                                color = SkyFrost,
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.40f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Streak Day ${uiState.wallet.dailyStreakCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.accentPrimary
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

            // Single Canonical Booster Pack Showcase
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Booster Pack Shop",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkSlate
                            )
                        }
                        Surface(
                            color = SkyFrost,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, palette.misprintLabel.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "0.005% 1/1 MISPRINTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.misprintLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    MegaPackShowcaseCard(
                        freeTickets = uiState.wallet.bonusFreePacks,
                        catalogReady = uiState.catalog.isNotEmpty(),
                        isOpening = uiState.isOpeningPack,
                        onOpenFreeTicket = { onOpenPackBundle(PackBundleOption.SINGLE, false, true) },
                        onShowOdds = {
                            onButtonClickSfx()
                            showOddsDialog = true
                        }
                    )
                }
            }

            // Pack Bundles with Discounts (1 Pack, 5 Packs 5% OFF, 10 Packs 10% OFF)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Choose Pack Bundle",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkSlate
                    )

                    PackBundleOption.entries.forEach { bundle ->
                        BundlePurchaseCard(
                            bundle = bundle,
                            coins = uiState.wallet.coins,
                            diamonds = uiState.wallet.diamonds,
                            catalogReady = uiState.catalog.isNotEmpty(),
                            isOpening = uiState.isOpeningPack,
                            onBuyWithCoins = { onOpenPackBundle(bundle, false, false) },
                            onBuyWithDiamonds = { onOpenPackBundle(bundle, true, false) }
                        )
                    }
                }
            }

            // Diamond-to-Coin Exchange Bar
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Redeem Diamonds for Coins",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkSlate
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiamondExchangeOption.ALL.forEach { option ->
                            val canAfford = uiState.wallet.diamonds >= option.diamondCost
                            Surface(
                                color = if (canAfford) PureWhiteCard else PearlCanvas,
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = if (canAfford) 2.dp else 0.dp,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (canAfford) palette.accentPrimary.copy(alpha = 0.48f) else IceBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = canAfford) {
                                        onExchangeDiamondsForCoins(option)
                                    }
                                    .testTag("exchange_${option.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (option.bonusLabel != null) {
                                        Surface(
                                            color = StudioGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = option.bonusLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StudioGoldText,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = "+%,d".format(option.coinReward),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StudioGoldText
                                    )
                                    Text(
                                        text = "Coins",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateMuted
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = if (canAfford) DeepCobalt else IceBorder,
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Diamond,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${option.diamondCost}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MLBBDex API Credits Card
            item {
                MlbbDexCreditCard(
                    skinCount = uiState.catalog.size,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showOddsDialog) {
        PackOddsDialog(
            onDismiss = {
                onButtonClickSfx()
                showOddsDialog = false
            }
        )
    }
}

@Composable
private fun DailyTierTile(
    tier: DailyRewardTier,
    isClaimed: Boolean,
    isCurrentClaimable: Boolean,
    onClick: () -> Unit
) {
    val palette = CollectorTheme.colors
    val borderColor = when {
        isCurrentClaimable -> StudioGold
        isClaimed -> palette.accentPrimary.copy(alpha = 0.42f)
        else -> IceBorder
    }
    Surface(
        color = if (isCurrentClaimable) palette.goldSurfaceTint else PureWhiteCard,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isCurrentClaimable) 4.dp else 1.dp,
        border = BorderStroke(if (isCurrentClaimable) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isCurrentClaimable, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Day ${tier.dayIndex}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrentClaimable) StudioGoldText else SlateMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+${tier.coinAmount}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isClaimed) SlateMuted else StudioGoldText
            )
            Text(
                text = "Coins",
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted
            )
            if (tier.bonusDiamonds > 0) {
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+${tier.bonusDiamonds} 💎",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            if (tier.bonusFreePacks > 0) {
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    color = MisprintMagenta.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+${tier.bonusFreePacks} Pack",
                        style = MaterialTheme.typography.labelSmall,
                        color = MisprintMagenta,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            if (isClaimed) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Claimed",
                    tint = palette.accentPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun MegaPackShowcaseCard(
    freeTickets: Int,
    catalogReady: Boolean,
    isOpening: Boolean,
    onOpenFreeTicket: () -> Unit,
    onShowOdds: () -> Unit
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, palette.accentPrimary.copy(alpha = 0.38f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(132.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.8.dp, palette.accentPrimary.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(id = MegaPackConfig.COVER_DRAWABLE_RES),
                        contentDescription = MegaPackConfig.PACK_TITLE,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = MoonlightNight.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "${MegaPackConfig.CARDS_PER_PACK} CARDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
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
                            text = MegaPackConfig.PACK_TITLE,
                            style = MaterialTheme.typography.titleLarge,
                            color = InkSlate,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onShowOdds,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("pack_odds_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View drop rates",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = MegaPackConfig.PACK_SUBTITLE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = SkyFrost,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1/1 Misprint: ${MegaPackConfig.MISPRINT_ODDS_LABEL}",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accentPrimary
                            )
                        }
                    }
                }
            }

            if (freeTickets > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenFreeTicket,
                    enabled = catalogReady && !isOpening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCobalt,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_free_pack_button")
                ) {
                    Text(
                        text = "Open Free Pack ($freeTickets Left)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun BundlePurchaseCard(
    bundle: PackBundleOption,
    coins: Int,
    diamonds: Int,
    catalogReady: Boolean,
    isOpening: Boolean,
    onBuyWithCoins: () -> Unit,
    onBuyWithDiamonds: () -> Unit
) {
    val palette = CollectorTheme.colors
    val canBuyCoins = coins >= bundle.coinCost
    val canBuyDiamonds = diamonds >= bundle.diamondCost

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = if (bundle == PackBundleOption.TEN_PACK) 1.5.dp else 1.dp,
            color = if (bundle == PackBundleOption.TEN_PACK) StudioGold else IceBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bundle.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkSlate
                    )
                    if (bundle.badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = StudioGold,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = palette.goldButtonContent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = bundle.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.goldButtonContent
                                )
                            }
                        }
                    }
                }

                if (bundle.discountPercent > 0) {
                    Text(
                        text = "%,d".format(bundle.fullCoinCost),
                        style = MaterialTheme.typography.labelMedium.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = SlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onBuyWithCoins,
                    enabled = catalogReady && canBuyCoins && !isOpening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioGold,
                        contentColor = palette.goldButtonContent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("buy_coins_${bundle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "%,d Coins".format(bundle.coinCost),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onBuyWithDiamonds,
                    enabled = catalogReady && canBuyDiamonds && !isOpening,
                    border = BorderStroke(
                        1.4.dp,
                        if (canBuyDiamonds) palette.accentPrimary else IceBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("buy_diamonds_${bundle.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = if (canBuyDiamonds) palette.accentPrimary else SlateMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${bundle.diamondCost} Diamonds",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (canBuyDiamonds) palette.accentPrimary else SlateMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun PackOddsDialog(
    onDismiss: () -> Unit
) {
    val palette = CollectorTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteCard,
        title = {
            Text(
                text = "${MegaPackConfig.PACK_TITLE} Drop Rates",
                style = MaterialTheme.typography.headlineMedium,
                color = InkSlate
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Every pack draws from the complete MLBBDex skin pool with rarity-locked odds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkinRarity.entries.reversed().forEach { rarity ->
                    val weight = MegaPackConfig.RARITY_WEIGHTS[rarity] ?: 0.0
                    val formatted = if (weight < 1.0 && weight > 0.0) {
                        String.format(Locale.US, "%.2f%%", weight)
                    } else if (weight % 1.0 == 0.0) {
                        "${weight.toInt()}%"
                    } else {
                        String.format(Locale.US, "%.2f%%", weight)
                    }
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
                            text = formatted,
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSlate
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Text(
                        text = "1-of-1 Misprint Bonus Roll: ${MegaPackConfig.MISPRINT_ODDS_LABEL}",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = palette.accentPrimary)
            }
        }
    )
}
