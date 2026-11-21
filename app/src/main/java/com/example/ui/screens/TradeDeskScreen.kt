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
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.MythicCrimson
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
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
    modifier: Modifier = Modifier
) {
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
            .background(VoidObsidian)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Honest Cloud Sync Notice
        item {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ArenaGold.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = ArenaGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "P2P Trade Slip Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArenaGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.trade_offline_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section 1: Mint a Shareable Trade Slip
        item {
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, PrismCyan.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = PrismCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MINT TRADE SLIP",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a card from your Vault (1-of-1 Misprints included) to generate a shareable trade slip for friends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (availableToList.isEmpty()) {
                        Text(
                            text = "No unlisted cards available in your Vault.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(availableToList, key = { it.instanceId }) { card ->
                                val isSelected = selectedCard?.instanceId == card.instanceId
                                Surface(
                                    color = if (isSelected) SurfaceElevated else VoidObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ArenaGold else OutlineSubtle
                                    ),
                                    modifier = Modifier
                                        .width(142.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedCard = card }
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
                                                    tint = MisprintNeon,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = card.skinName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = card.heroName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
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
                                focusedBorderColor = PrismCyan,
                                unfocusedBorderColor = OutlineSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
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
                                containerColor = PrismCyan,
                                contentColor = VoidObsidian
                            ),
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
                color = SurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REDEEM FRIEND'S TRADE SLIP",
                        style = MaterialTheme.typography.titleLarge,
                        color = ArenaGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paste a MEGA-TRADE-V1:... code sent by another collector to preview and swap.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pasteCodeInput,
                        onValueChange = { pasteCodeInput = it },
                        placeholder = { Text("MEGA-TRADE-V1:...", color = TextSecondary) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArenaGold,
                            unfocusedBorderColor = OutlineSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
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
                            containerColor = ArenaGold,
                            contentColor = VoidObsidian
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("decode_trade_slip_button")
                    ) {
                        Text("INSPECT TRADE OFFER", style = MaterialTheme.typography.labelLarge)
                    }

                    uiState.decodedSlipPreview?.let { slip ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = SurfaceCard,
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
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${slip.heroName} — ${slip.skinName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${slip.serialCode}${if (slip.isMisprint) " • 1/1 MISPRINT" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (slip.isMisprint) MisprintNeon else ArenaGold
                                )
                                if (slip.seekingNote.isNotBlank()) {
                                    Text(
                                        text = "Seeking: ${slip.seekingNote}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onClearDecodedSlip,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Dismiss", color = TextSecondary)
                                    }
                                    Button(
                                        onClick = { showPickOfferDialogForSlip = slip },
                                        enabled = uiState.ownedCards.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrismCyan,
                                            contentColor = VoidObsidian
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
                color = TextSecondary
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
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("MEGA COLLECTOR Trade Slip", listing.tradeCode))
                        onShowToast("Trade Slip copied to clipboard!")
                    },
                    onShareCode = {
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
            containerColor = SurfaceElevated,
            title = {
                Text("Select Card to Swap", color = ArenaGold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.ownedCards, key = { it.instanceId }) { offered ->
                        Surface(
                            color = SurfaceCard,
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
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${offered.rarity.displayName} • ${offered.serialCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (offered.isMisprint) MisprintNeon else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPickOfferDialogForSlip = null }) {
                    Text("Cancel", color = TextSecondary)
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
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
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
                    .background(SurfaceElevated)
            ) {
                if (listing.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = listing.imageUrl,
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
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Seeking: ${listing.seekingNote}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onCopyCode) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy trade slip", tint = PrismCyan)
            }
            IconButton(onClick = onShareCode) {
                Icon(Icons.Default.Share, contentDescription = "Share trade slip", tint = ArenaGold)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Cancel listing", tint = MythicCrimson)
            }
        }
    }
}
