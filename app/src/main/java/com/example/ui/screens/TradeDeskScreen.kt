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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.DecodedTradeSlip
import com.example.model.OwnedCard
import com.example.model.TradeListing
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.RarityChip
import com.example.ui.components.rememberMlbbImageRequest
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MisprintNeon
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
        // Honest Offline P2P Notice
        item {
            Surface(
                color = palette.goldSurfaceTint,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, StudioGold.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = StudioGoldText,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "P2P Trade Slip Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioGoldText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.trade_offline_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }
            }
        }

        // Section 1: Mint a Shareable Trade Slip
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 3.dp,
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
                            text = "MINT TRADE SLIP",
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

        // Section 2: Redeem Partner's Trade Slip
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 3.dp,
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
                        text = "Paste a MEGA-TRADE-V1:... code sent by another collector to preview and swap.",
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
                                1.5.dp,
                                if (slip.isMisprint) MisprintNeon else slip.rarity.primaryColor
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RarityChip(rarity = slip.rarity, compact = true)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "From ${slip.senderAlias}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SlateMuted
                                    )
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

        // Section 3: Active Minted Trade Listings
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
                    subtitle = "Mint a Trade Slip above to share any card or 1-of-1 Misprint with friends."
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
                                        color = if (offered.isMisprint) DeepCobalt else SlateMuted
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
            if (listing.isMisprint) MisprintNeon else listing.rarity.primaryColor.copy(alpha = 0.55f)
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
