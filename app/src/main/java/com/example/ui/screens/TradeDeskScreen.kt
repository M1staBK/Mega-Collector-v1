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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CollectorPublicProfile
import com.example.model.DecodedTradeSlip
import com.example.model.OwnedCard
import com.example.model.P2PTradeListing
import com.example.model.TradeListing
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ProfileViewerDialog
import com.example.ui.components.RarityChip
import com.example.ui.components.rememberMlbbSquareImageRequest
import com.example.ui.components.rememberUserAvatarRequest
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
    currentUserUid: String?,
    preselectedCardForTrade: OwnedCard?,
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
    onGoogleSignInClick: (Context) -> Unit = {},
    isSigningIn: Boolean = false,
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
    var showPickOfferDialogForP2P by remember { mutableStateOf<P2PTradeListing?>(null) }
    var inlineSlipProfile by remember { mutableStateOf<CollectorPublicProfile?>(null) }

    val availableToList = remember(uiState.ownedCards) {
        uiState.ownedCards.filter { !it.isListedForTrade }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TCG Pocket Mint-Emerald Sash Trade Header
        item {
            FrostedGlassCard(
                cornerRadius = 24.dp,
                borderColor = palette.accentPrimary.copy(alpha = 0.45f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Angled Theme-Accented Banner Sash
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(palette.buttonPrimary, palette.skyAccent)
                                )
                            )
                    ) {
                        Surface(
                            color = palette.cardSurface,
                            shape = RoundedCornerShape(999.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 14.dp)
                                .graphicsLayer { rotationZ = -2f }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trade Desk",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = InkSlate
                                )
                            }
                        }

                        Surface(
                            color = palette.cardSurface,
                            shape = RoundedCornerShape(999.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 14.dp)
                        ) {
                            Text(
                                text = "${uiState.ownedCards.size} Binder • ${uiState.tradeListings.size} Slips",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.accentPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = palette.frostSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.45f)),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Player-to-Player Skin Exchange",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkSlate
                            )
                            Text(
                                text = "100% real player offers only — swap skins directly in Cloud P2P or share instant Trade Slips.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }
            }
        }

        if (currentUserUid == null) {
            item {
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Optional: Live Cloud P2P",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkSlate
                            )
                            Text(
                                text = "Preview mode supports Trade Slips locally. Sign in with Google to browse & post live cloud P2P listings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
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
                            modifier = Modifier.testTag("trade_google_signin_button")
                        ) {
                            Text(if (isSigningIn) "..." else "Sign In")
                        }
                    }
                }
            }
        }

        // Section 1: Live P2P Player Listings (from Firestore /trade_listings)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE P2P PLAYER LISTINGS (${uiState.openP2PListings.size})",
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

        if (uiState.openP2PListings.isEmpty()) {
            item {
                EmptyVaultState(
                    title = "No Live P2P Listings Yet",
                    subtitle = "Mint a Trade Listing below to post your skin to the live player marketplace, or redeem a friend's Trade Slip."
                )
            }
        } else {
            items(uiState.openP2PListings, key = { it.id }) { p2p ->
                val isOwnListing = currentUserUid != null && p2p.sellerUid == currentUserUid
                P2PMarketListingCard(
                    listing = p2p,
                    isOwnListing = isOwnListing,
                    canSwap = uiState.ownedCards.isNotEmpty() && !isOwnListing,
                    onViewProfile = {
                        onButtonClickSfx()
                        onViewTraderProfile(p2p.sellerUid)
                    },
                    onSwapClick = {
                        onButtonClickSfx()
                        showPickOfferDialogForP2P = p2p
                    },
                    onCancelOwn = {
                        onButtonClickSfx()
                        onCancelCloudP2PListing(p2p)
                    }
                )
            }
        }

        // Section 2: Mint & Publish Trade Listing + Shareable Slip
        item {
            FrostedGlassCard(cornerRadius = 24.dp) {
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
                            text = "POST P2P LISTING & TRADE SLIP",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a card from your Vault to publish on the live P2P Marketplace and generate a shareable trade slip for friends.",
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
                            label = { Text("Seeking (e.g. Legend Gusion or Collector Fanny)") },
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
                            Text("POST P2P TRADE & MINT SLIP", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Section 3: Redeem Friend's Trade Slip
        item {
            FrostedGlassCard(cornerRadius = 24.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REDEEM FRIEND'S TRADE SLIP",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.accentPrimary
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
                            focusedBorderColor = palette.accentPrimary,
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
                            containerColor = palette.buttonPrimary,
                            contentColor = Color.White
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
                                                inlineSlipProfile = CollectorPublicProfile(
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
                                                    badgeIds = slip.senderBadgeIds
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
                                    color = palette.accentPrimary
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

        // Section 4: Local Minted Slips
        item {
            Text(
                text = "YOUR MINTED TRADE SLIPS (${uiState.tradeListings.size})",
                style = MaterialTheme.typography.labelMedium,
                color = SlateMuted
            )
        }

        if (uiState.tradeListings.isEmpty()) {
            item {
                EmptyVaultState(
                    title = "No Active Local Trade Slips",
                    subtitle = "Swap on the Live P2P Listings above or mint a Trade Slip for friends."
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

    // Cloud P2P Swap Card Picker Dialog
    showPickOfferDialogForP2P?.let { p2p ->
        AlertDialog(
            onDismissRequest = { showPickOfferDialogForP2P = null },
            containerColor = PureWhiteCard,
            title = {
                Text("Swap with ${p2p.sellerName}", color = InkSlate)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Receiving: ${p2p.heroName} — ${p2p.skinName}\nSeeking: ${p2p.seekingNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                    LazyColumn(
                        modifier = Modifier.height(250.dp),
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
                                        showPickOfferDialogForP2P = null
                                        onAcceptP2PListing(p2p, offered)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
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
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        showPickOfferDialogForP2P = null
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
                                verticalAlignment = Alignment.CenterVertically
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

    // View Collector Profile Dialog (Cloud OR Slip Sender)
    val activeProfile = uiState.viewingTraderProfile ?: inlineSlipProfile
    activeProfile?.let { profile ->
        ProfileViewerDialog(
            profile = profile,
            onDismiss = {
                onButtonClickSfx()
                inlineSlipProfile = null
                onDismissViewingProfile()
            }
        )
    }
}

@Composable
private fun P2PMarketListingCard(
    listing: P2PTradeListing,
    isOwnListing: Boolean,
    canSwap: Boolean,
    onViewProfile: () -> Unit,
    onSwapClick: () -> Unit,
    onCancelOwn: () -> Unit
) {
    val palette = CollectorTheme.colors
    FrostedGlassCard(
        cornerRadius = 20.dp,
        borderColor = if (listing.isMisprint) palette.misprintLabel.copy(alpha = 0.6f)
        else listing.rarity.primaryColor.copy(alpha = 0.48f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("p2p_listing_card_${listing.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onViewProfile)
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = palette.accentPrimary.copy(alpha = 0.14f),
                    shape = CircleShape,
                    border = BorderStroke(1.2.dp, palette.accentPrimary),
                    modifier = Modifier.size(34.dp)
                ) {
                    if (listing.sellerPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = rememberUserAvatarRequest(listing.sellerPhotoUrl),
                            contentDescription = listing.sellerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = listing.sellerName,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOwnListing) "${listing.sellerName} (You)" else listing.sellerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = InkSlate,
                        maxLines = 1
                    )
                    Text(
                        text = "Verified P2P Collector",
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SkyFrost)
                ) {
                    if (listing.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = rememberMlbbSquareImageRequest(listing.imageUrl),
                            contentDescription = listing.skinName,
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
                        RarityChip(rarity = listing.rarity, compact = true)
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
                        text = "${listing.heroName} — ${listing.skinName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Seeking: ${listing.seekingNote}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = palette.accentPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (isOwnListing) {
                    OutlinedButton(
                        onClick = onCancelOwn,
                        border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge, color = StudioCrimson)
                    }
                } else {
                    Button(
                        onClick = onSwapClick,
                        enabled = canSwap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCobalt,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("p2p_swap_${listing.id}")
                    ) {
                        Text("Swap", style = MaterialTheme.typography.labelLarge)
                    }
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
    FrostedGlassCard(
        cornerRadius = 18.dp,
        borderColor = if (listing.isMisprint) palette.misprintLabel.copy(alpha = 0.65f) else listing.rarity.primaryColor.copy(alpha = 0.55f)
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
                        model = rememberMlbbSquareImageRequest(listing.imageUrl),
                        contentDescription = listing.skinName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.skinName,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${listing.heroName} • ${listing.serialCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (listing.isMisprint) palette.misprintLabel else SlateMuted
                )
                Text(
                    text = "Seeking: ${listing.seekingNote}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCopyCode, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", tint = palette.accentPrimary)
            }
            IconButton(onClick = onShareCode, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Share code", tint = palette.accentPrimary)
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Cancel listing", tint = StudioCrimson)
            }
        }
    }
}
