package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BazaarCollectorProfile
import com.example.model.BazaarTraderListing
import com.example.model.DecodedTradeSlip
import com.example.model.OwnedCard
import com.example.model.TradeListing
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.ProfileViewerDialog
import com.example.ui.components.RarityChip
import com.example.ui.components.rememberMlbbImageRequest
import com.example.ui.components.rememberMlbbSquareImageRequest
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldText
import com.example.ui.viewmodel.MegaCollectorUiState

@Composable
fun TradeDeskScreen(
    uiState: MegaCollectorUiState,
    preselectedCardForTrade: OwnedCard?,
    onClearPreselectedCard: () -> Unit,
    onExecuteBazaarTrade: (BazaarTraderListing, OwnedCard) -> Unit,
    onRefreshBazaar: () -> Unit,
    onCreateTradeListing: (OwnedCard, String, Int) -> Unit,
    onCancelListing: (TradeListing) -> Unit,
    onDecodeSlip: (String) -> Unit,
    onClearDecodedSlip: () -> Unit,
    onCompleteTrade: (DecodedTradeSlip, OwnedCard) -> Unit,
    onShowToast: (String) -> Unit,
    onButtonClickSfx: () -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val context = LocalContext.current
    var selectedCard by remember(preselectedCardForTrade) { mutableStateOf(preselectedCardForTrade) }
    var seekingNote by remember { mutableStateOf("") }
    var pasteCodeInput by remember { mutableStateOf("") }
    var showPickOfferDialogForSlip by remember { mutableStateOf<DecodedTradeSlip?>(null) }
    var showPickOfferDialogForBazaar by remember { mutableStateOf<BazaarTraderListing?>(null) }
    var viewingProfile by remember { mutableStateOf<BazaarCollectorProfile?>(null) }

    val availableToList = remember(uiState.ownedCards) {
        uiState.ownedCards.filter { !it.isListedForTrade }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PearlCanvas)
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Collector Bazaar Banner
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = palette.frostSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Collector Bazaar Active",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkSlate
                        )
                        Text(
                            text = "Swap skins directly with Dawn Collectors, inspect their Profiles & Badges, or trade via P2P slips.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRefreshBazaar,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("refresh_bazaar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Bazaar listings",
                            tint = palette.accentPrimary
                        )
                    }
                }
            }
        }

        // Section 1: Collector Bazaar Listings
        if (uiState.bazaarListings.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE BAZAAR OFFERS (${uiState.bazaarListings.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SlateMuted
                    )
                    Text(
                        text = "Tap trader to view profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accentPrimary
                    )
                }
            }

            items(uiState.bazaarListings, key = { it.listingId }) { bz ->
                BazaarOfferCard(
                    listing = bz,
                    ownedCards = uiState.ownedCards,
                    playerCoins = uiState.wallet.coins,
                    onViewProfile = {
                        onButtonClickSfx()
                        viewingProfile = bz.trader
                    },
                    onSwapClick = {
                        onButtonClickSfx()
                        showPickOfferDialogForBazaar = bz
                    }
                )
            }
        }

        // Section 2: Mint a Shareable Trade Slip
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "P2P TRADE SLIP EXCHANGE",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a card from your Vault (1-of-1 Misprints included) to generate a shareable trade slip for friends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (availableToList.isEmpty()) {
                        Text(
                            text = "No unlisted cards available in your Vault.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(availableToList, key = { it.instanceId }) { card ->
                                val isSelected = selectedCard?.instanceId == card.instanceId
                                Surface(
                                    color = if (isSelected) SkyFrost else PearlCanvas,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) DeepCobalt else IceBorder
                                    ),
                                    modifier = Modifier
                                        .width(146.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onButtonClickSfx()
                                            selectedCard = card
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RarityChip(rarity = card.rarity, compact = true)
                                            if (card.isMisprint) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = "Misprint",
                                                    tint = palette.misprintLabel,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = card.skinName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = InkSlate,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = card.heroName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SlateMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = seekingNote,
                            onValueChange = { seekingNote = it },
                            label = { Text("Seeking (e.g. Legend Gusion or 1/1 Misprint)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepCobalt,
                                unfocusedBorderColor = IceBorder,
                                focusedTextColor = InkSlate,
                                unfocusedTextColor = InkSlate
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val target = selectedCard ?: return@Button
                                onCreateTradeListing(target, seekingNote, 0)
                                selectedCard = null
                                seekingNote = ""
                                onClearPreselectedCard()
                            },
                            enabled = selectedCard != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepCobalt,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mint_trade_slip_button")
                        ) {
                            Text("MINT & LIST TRADE SLIP", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Section 3: Redeem Partner's Trade Slip
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REDEEM FRIEND'S TRADE SLIP",
                        style = MaterialTheme.typography.titleLarge,
                        color = StudioGoldText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paste a MEGA-TRADE-V1:... code sent by another collector to preview their profile & swap.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pasteCodeInput,
                        onValueChange = { pasteCodeInput = it },
                        placeholder = { Text("MEGA-TRADE-V1:...", color = SlateMuted) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioGold,
                            unfocusedBorderColor = IceBorder,
                            focusedTextColor = InkSlate,
                            unfocusedTextColor = InkSlate
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paste_trade_slip_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onDecodeSlip(pasteCodeInput) },
                        enabled = pasteCodeInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioGold,
                            contentColor = palette.goldButtonContent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("decode_trade_slip_button")
                    ) {
                        Text("INSPECT TRADE OFFER", style = MaterialTheme.typography.labelLarge)
                    }

                    uiState.decodedSlipPreview?.let { slip ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = SkyFrost,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.2.dp,
                                if (slip.isMisprint) palette.misprintLabel.copy(alpha = 0.7f) else slip.rarity.primaryColor.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RarityChip(rarity = slip.rarity, compact = true)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "From ${slip.senderAlias}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = InkSlate
                                        )
                                    }
                                    Surface(
                                        color = PureWhiteCard,
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable {
                                                onButtonClickSfx()
                                                viewingProfile = BazaarCollectorProfile(
                                                    traderId = "p2p_${slip.senderAlias}",
                                                    alias = slip.senderAlias,
                                                    rankTitle = when {
                                                        slip.senderPrestige >= 10000 -> "Mythical Glory"
                                                        slip.senderPrestige >= 4000 -> "Mythic"
                                                        else -> "Legend"
                                                    },
                                                    avatarColor = palette.accentPrimary,
                                                    prestigeScore = slip.senderPrestige,
                                                    uniqueSkinsCount = slip.senderCardCount,
                                                    packsOpened = (slip.senderCardCount / 4).coerceAtLeast(3),
                                                    tradesDone = 5,
                                                    motto = slip.seekingNote.ifBlank { "Trading via P2P Slip!" },
                                                    badgeIds = slip.senderBadgeIds.ifEmpty { listOf("badge_bazaar_broker") }
                                                )
                                            }
                                            .testTag("view_slip_sender_profile")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "View sender profile",
                                                tint = palette.accentPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Profile",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.accentPrimary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${slip.heroName} — ${slip.skinName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkSlate
                                )
                                Text(
                                    text = "${slip.serialCode}${if (slip.isMisprint) " • 1/1 MISPRINT" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (slip.isMisprint) palette.misprintLabel else StudioGoldText
                                )
                                if (slip.seekingNote.isNotBlank()) {
                                    Text(
                                        text = "Seeking: ${slip.seekingNote}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SlateMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onClearDecodedSlip,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Dismiss", color = SlateMuted)
                                    }
                                    Button(
                                        onClick = {
                                            onButtonClickSfx()
                                            showPickOfferDialogForSlip = slip
                                        },
                                        enabled = uiState.ownedCards.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DeepCobalt,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Pick Card to Swap")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Active Minted Trade Listings
        item {
            Text(
                text = "YOUR ACTIVE TRADE LISTINGS (${uiState.tradeListings.size})",
                style = MaterialTheme.typography.labelMedium,
                color = SlateMuted
            )
        }

        if (uiState.tradeListings.isEmpty()) {
            item {
                EmptyVaultState(
                    title = "No Active Trade Slips",
                    subtitle = "Swap on the Live Bazaar above or mint a Trade Slip for friends."
                )
            }
        } else {
            items(uiState.tradeListings, key = { it.id }) { listing ->
                TradeListingRow(
                    listing = listing,
                    onCopyCode = {
                        onButtonClickSfx()
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("MEGA COLLECTOR Trade Slip", listing.tradeCode))
                        onShowToast("Trade Slip copied to clipboard!")
                    },
                    onShareCode = {
                        onButtonClickSfx()
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "MEGA COLLECTOR Trade Offer: ${listing.heroName} - ${listing.skinName} (${listing.serialCode})\nSeeking: ${listing.seekingNote}\n\n${listing.tradeCode}"
                            )
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Trade Slip"))
                    },
                    onCancel = { onCancelListing(listing) }
                )
            }
        }
    }

    // Bazaar Card Picker Dialog
    showPickOfferDialogForBazaar?.let { bz ->
        val eligibleCards = remember(uiState.ownedCards, bz.listingId) {
            uiState.ownedCards.filter { it.rarity.tierRank >= bz.minRequiredRarity.tierRank }
        }
        val canAffordFee = uiState.wallet.coins >= bz.coinFee

        AlertDialog(
            onDismissRequest = { showPickOfferDialogForBazaar = null },
            containerColor = PureWhiteCard,
            title = {
                Text("Swap for ${bz.offeredSkin.skinName}", color = InkSlate)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Requires: ${bz.seekingLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (canAffordFee) palette.accentPrimary else StudioCrimson
                    )
                    if (eligibleCards.isEmpty()) {
                        Text(
                            text = "You don't have a ${bz.minRequiredRarity.displayName}+ card in your Vault yet. Open packs to pull one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    } else if (!canAffordFee) {
                        Text(
                            text = "You need ${bz.coinFee} Coins for this trade fee.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioCrimson
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(eligibleCards, key = { it.instanceId }) { offered ->
                                Surface(
                                    color = SkyFrost,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, offered.rarity.primaryColor.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showPickOfferDialogForBazaar = null
                                            onExecuteBazaarTrade(bz, offered)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${offered.heroName} — ${offered.skinName}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = InkSlate
                                            )
                                            Text(
                                                text = "${offered.rarity.displayName} • ${offered.serialCode}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (offered.isMisprint) palette.misprintLabel else SlateMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        showPickOfferDialogForBazaar = null
                    }
                ) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }

    // P2P Slip Card Picker Dialog
    showPickOfferDialogForSlip?.let { slip ->
        AlertDialog(
            onDismissRequest = { showPickOfferDialogForSlip = null },
            containerColor = PureWhiteCard,
            title = {
                Text("Select Card to Swap", color = InkSlate)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.ownedCards, key = { it.instanceId }) { offered ->
                        Surface(
                            color = SkyFrost,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, offered.rarity.primaryColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPickOfferDialogForSlip = null
                                    pasteCodeInput = ""
                                    onCompleteTrade(slip, offered)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${offered.heroName} — ${offered.skinName}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = InkSlate
                                    )
                                    Text(
                                        text = "${offered.rarity.displayName} • ${offered.serialCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (offered.isMisprint) palette.misprintLabel else SlateMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        showPickOfferDialogForSlip = null
                    }
                ) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }

    // View Collector Profile Dialog
    viewingProfile?.let { profile ->
        ProfileViewerDialog(
            profile = profile,
            onDismiss = {
                onButtonClickSfx()
                viewingProfile = null
            }
        )
    }
}

@Composable
private fun BazaarOfferCard(
    listing: BazaarTraderListing,
    ownedCards: List<OwnedCard>,
    playerCoins: Int,
    onViewProfile: () -> Unit,
    onSwapClick: () -> Unit
) {
    val palette = CollectorTheme.colors
    val hasEligibleCard = remember(ownedCards, listing.minRequiredRarity) {
        ownedCards.any { it.rarity.tierRank >= listing.minRequiredRarity.tierRank }
    }
    val canTrade = hasEligibleCard && playerCoins >= listing.coinFee

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            if (listing.isMisprint) palette.misprintLabel.copy(alpha = 0.6f)
            else listing.offeredSkin.rarity.primaryColor.copy(alpha = 0.48f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bazaar_card_${listing.listingId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Trader Header (Clickable to open ProfileViewerDialog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onViewProfile)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("bazaar_profile_${listing.trader.traderId}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = listing.trader.avatarColor.copy(alpha = 0.16f),
                    shape = CircleShape,
                    border = BorderStroke(1.2.dp, listing.trader.avatarColor),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = listing.trader.alias,
                            tint = listing.trader.avatarColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listing.trader.alias,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = InkSlate,
                        maxLines = 1
                    )
                    Text(
                        text = "${listing.trader.rankTitle} • ${listing.trader.uniqueSkinsCount} skins",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Text(
                        text = "View Profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skin Offer Body
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SkyFrost)
                ) {
                    if (listing.offeredSkin.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = rememberMlbbSquareImageRequest(listing.offeredSkin.imageUrl),
                            contentDescription = listing.offeredSkin.skinName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RarityChip(rarity = listing.offeredSkin.rarity, compact = true)
                        if (listing.isMisprint) {
                            Surface(
                                color = palette.misprintLabel.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "1/1 MISPRINT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = palette.misprintLabel,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${listing.offeredSkin.heroName} — ${listing.offeredSkin.skinName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Seeking: ${listing.seekingLabel}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = StudioGoldText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSwapClick,
                    enabled = ownedCards.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canTrade) DeepCobalt else palette.frostSurface,
                        contentColor = if (canTrade) Color.White else SlateMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("bazaar_swap_${listing.listingId}")
                ) {
                    Text(
                        text = if (canTrade) "Swap" else "Details",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeListingRow(
    listing: TradeListing,
    onCopyCode: () -> Unit,
    onShareCode: () -> Unit,
    onCancel: () -> Unit
) {
    val palette = CollectorTheme.colors
    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            if (listing.isMisprint) palette.misprintLabel.copy(alpha = 0.65f) else listing.rarity.primaryColor.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SkyFrost)
            ) {
                if (listing.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = rememberMlbbImageRequest(listing.imageUrl),
                        contentDescription = listing.skinName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                RarityChip(rarity = listing.rarity, compact = true)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${listing.heroName} • ${listing.skinName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Seeking: ${listing.seekingNote}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onCopyCode) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy trade slip", tint = palette.accentPrimary)
            }
            IconButton(onClick = onShareCode) {
                Icon(Icons.Default.Share, contentDescription = "Share trade slip", tint = StudioGoldText)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Cancel listing", tint = StudioCrimson)
            }
        }
    }
}
