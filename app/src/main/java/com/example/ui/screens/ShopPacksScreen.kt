package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.remote.AuthenticatedUser
import com.example.model.DailyRewardTier
import com.example.model.DiamondExchangeOption
import com.example.model.MegaPackConfig
import com.example.model.PackBundleOption
import com.example.model.PackSatinTheme
import com.example.model.SkinRarity
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.FrostedGlassCard
import com.example.model.MlbbCollectionTier
import com.example.ui.components.MlbbCollectionBadge
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.components.SettingsGearButton
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MisprintMagenta
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
    currentUser: AuthenticatedUser? = null,
    dailyRewardTiers: List<DailyRewardTier>,
    onClaimDailyReward: () -> Unit,
    onOpenPackBundle: (PackBundleOption, Boolean, Boolean, PackSatinTheme?) -> Unit,
    onExchangeDiamondsForCoins: (DiamondExchangeOption) -> Unit,
    onGoToArcade: () -> Unit = {},
    onGoToMissions: () -> Unit = {},
    onGoToBinder: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var showOddsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Studio Header Band + Arena Banner + Currency Pills
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val collectionTier = remember(uiState.ownedCards) {
                                MlbbCollectionTier.fromPrestige(uiState.ownedCards.sumOf { it.prestigeScore })
                            }
                            Surface(
                                color = palette.frostSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, palette.accentPrimary.copy(alpha = 0.55f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MlbbCollectionBadge(
                                        tier = collectionTier,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MEGA Collector",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = palette.ink
                                )
                                Text(
                                    text = if (palette.isDark) {
                                        "Moonlight Sky Card Studio"
                                    } else {
                                        "MOBA Legends Sky Card Studio"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.muted
                                )
                            }
                        }

                        SettingsGearButton(onClick = onOpenSettings)
                    }

                    // Feathered Arena Artwork Strip with Currency Pills
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(114.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_arena_banner),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .alpha(if (palette.isDark) 0.56f else 0.78f)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            palette.canvas.copy(alpha = 0.25f),
                                            palette.canvas.copy(alpha = 0.86f)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinsPill(coins = uiState.wallet.coins)
                            DiamondsPill(diamonds = uiState.wallet.diamonds)
                        }
                    }
                }
            }

            // Daily Streak Rewards
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
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.daily_rewards_title),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.ink
                            )
                        }

                        if (uiState.isDailyRewardClaimable) {
                            Button(
                                onClick = onClaimDailyReward,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.buttonPrimary,
                                    contentColor = Color.White
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
                                color = palette.buttonPrimary,
                                shape = RoundedCornerShape(999.dp),
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "Streak Day ${uiState.wallet.dailyStreakCount}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

            // Booster Pack Showcase Card (MEGA Legends Pack)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Booster Pack Shop",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    MegaPackShowcaseCard(
                        bonusFreePacks = uiState.wallet.bonusFreePacks,
                        catalogSize = uiState.catalog.size,
                        isOpeningPack = uiState.isOpeningPack,
                        onShowOdds = {
                            onButtonClickSfx()
                            showOddsDialog = true
                        },
                        onUseFreeTicket = {
                            onOpenPackBundle(PackBundleOption.SINGLE, false, true, null)
                        }
                    )
                }
            }

            // Choose Pack Bundle
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = palette.accentPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Choose Pack Bundle",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink
                        )
                    }

                    PackBundleOption.entries.forEach { bundle ->
                        BundlePurchaseCard(
                            bundle = bundle,
                            userCoins = uiState.wallet.coins,
                            userDiamonds = uiState.wallet.diamonds,
                            enabled = uiState.catalog.isNotEmpty() && !uiState.isOpeningPack,
                            onBuyWithCoins = {
                                onOpenPackBundle(bundle, false, false, null)
                            },
                            onBuyWithDiamonds = {
                                onOpenPackBundle(bundle, true, false, null)
                            }
                        )
                    }
                }
            }

            // Redeem Diamonds for Coins
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiamondExchangeOption.ALL.forEach { option ->
                            val canAfford = uiState.wallet.diamonds >= option.diamondCost
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                FrostedGlassCard(
                                    cornerRadius = 20.dp,
                                    borderColor = if (canAfford) palette.glassHighlight else palette.border,
                                    containerAlpha = if (canAfford) null else 0.65f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable(enabled = canAfford) {
                                            onExchangeDiamondsForCoins(option)
                                        }
                                        .testTag("exchange_${option.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "+%,d".format(option.coinReward),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 21.sp
                                            ),
                                            color = palette.accentPrimary
                                        )
                                        Text(
                                            text = "Coins",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateMuted
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            color = if (canAfford) palette.buttonPrimary else palette.border,
                                            shape = RoundedCornerShape(999.dp),
                                            shadowElevation = if (canAfford) 2.dp else 0.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Diamond,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${option.diamondCost}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                if (option.bonusLabel != null) {
                                    Surface(
                                        color = palette.frostSurface,
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.65f)),
                                        shadowElevation = 2.dp,
                                        modifier = Modifier.zIndex(2f)
                                    ) {
                                        Text(
                                            text = option.bonusLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = palette.accentPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MLBBDex Credits
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
private fun MegaPackShowcaseCard(
    bonusFreePacks: Int,
    catalogSize: Int,
    isOpeningPack: Boolean,
    onShowOdds: () -> Unit,
    onUseFreeTicket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    FrostedGlassCard(
        cornerRadius = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            palette.frostSurface.copy(alpha = 0.45f),
                            palette.cardSurface.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Canonical Single Booster Pack Cover
                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .height(148.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = MegaPackConfig.COVER_DRAWABLE_RES),
                        contentDescription = MegaPackConfig.PACK_TITLE,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color(0xCC0D1426),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "5 CARDS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = MegaPackConfig.PACK_TITLE,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = InkSlate,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onShowOdds,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("pack_odds_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View pull odds",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = "5 Cards per Pack • Full ${catalogSize.coerceAtLeast(1100)}+ MLBBDex Pool • 0.005% 1/1 Misprint",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = palette.frostSurface,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1/1 Misprint: 0.005% (1/20,000)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.accentPrimary
                            )
                        }
                    }

                    if (bonusFreePacks > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onUseFreeTicket,
                            enabled = catalogSize > 0 && !isOpeningPack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.buttonPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_free_pack_button")
                        ) {
                            Text(
                                text = "Open Free Pack ($bonusFreePacks Left)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BundlePurchaseCard(
    bundle: PackBundleOption,
    userCoins: Int,
    userDiamonds: Int,
    enabled: Boolean,
    onBuyWithCoins: () -> Unit,
    onBuyWithDiamonds: () -> Unit
) {
    val palette = CollectorTheme.colors
    val canAffordCoins = enabled && userCoins >= bundle.coinCost
    val canAffordDiamonds = enabled && userDiamonds >= bundle.diamondCost

    Box(modifier = Modifier.fillMaxWidth()) {
        FrostedGlassCard(
            cornerRadius = 22.dp,
            borderColor = if (bundle.discountPercent > 0) {
                palette.accentPrimary.copy(alpha = 0.50f)
            } else {
                palette.glassHighlight
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (bundle.badgeText != null) 8.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column: Pack title + Coin Purchase Pill
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bundle.label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = InkSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = if (canAffordCoins) palette.frostSurface else palette.abyss.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(
                            1.1.dp,
                            if (canAffordCoins) palette.accentPrimary.copy(alpha = 0.55f) else palette.border
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(enabled = canAffordCoins, onClick = onBuyWithCoins)
                            .testTag("buy_coins_${bundle.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (canAffordCoins) palette.buttonPrimary else SlateMuted.copy(alpha = 0.4f),
                                shape = CircleShape,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${bundle.coinCost} Coins",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (canAffordCoins) InkSlate else SlateMuted
                            )
                            if (bundle.discountPercent > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${bundle.fullCoinCost}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    ),
                                    color = SlateMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right column: Diamond Purchase Pill Button
                Column(horizontalAlignment = Alignment.End) {
                    if (bundle.discountPercent > 0) {
                        Text(
                            text = "${bundle.packCount * 5} Gems",
                            style = MaterialTheme.typography.labelSmall.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = SlateMuted,
                            modifier = Modifier.padding(end = 8.dp, bottom = 2.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = onBuyWithDiamonds,
                        enabled = canAffordDiamonds,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (canAffordDiamonds) palette.cardSurface else palette.abyss.copy(alpha = 0.5f),
                            contentColor = palette.accentPrimary,
                            disabledContainerColor = palette.abyss.copy(alpha = 0.45f),
                            disabledContentColor = SlateMuted
                        ),
                        border = BorderStroke(
                            width = 1.4.dp,
                            color = if (canAffordDiamonds) palette.skyAccent.copy(alpha = 0.8f) else palette.border
                        ),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("buy_diamonds_${bundle.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = if (canAffordDiamonds) palette.skyAccent else SlateMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${bundle.diamondCost} Diamonds",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }

        if (bundle.badgeText != null) {
            Surface(
                color = palette.buttonPrimary,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp)
                    .zIndex(2f)
            ) {
                Text(
                    text = bundle.badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                )
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
    val palette = CollectorTheme.colors
    val borderColor = when {
        isCurrentClaimable -> palette.accentPrimary
        isClaimed -> palette.glassHighlight
        else -> palette.border
    }
    Surface(
        color = if (isCurrentClaimable) palette.frostSurface else palette.cardSurface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (isCurrentClaimable) 5.dp else 2.dp,
        border = BorderStroke(if (isCurrentClaimable) 1.8.dp else 1.2.dp, borderColor),
        modifier = Modifier
            .width(94.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = isCurrentClaimable, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Day ${tier.dayIndex}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrentClaimable) palette.accentPrimary else SlateMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+${tier.coinAmount}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isClaimed) palette.ink else palette.accentPrimary
            )
            Text(
                text = "Coins",
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted
            )
            if (tier.bonusDiamonds > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "+${tier.bonusDiamonds} 💎",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            if (tier.bonusFreePacks > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = palette.buttonPrimary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "+${tier.bonusFreePacks} Pack",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            if (isClaimed) {
                Spacer(modifier = Modifier.height(5.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Claimed",
                    tint = palette.accentPrimary,
                    modifier = Modifier.size(16.dp)
                )
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
