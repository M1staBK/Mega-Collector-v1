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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.OwnedCard
import com.example.ui.components.CoinsPill
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.RarityChip
import com.example.ui.components.rememberMlbbImageRequest
import com.example.ui.theme.CollectorTheme
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
fun RecycleForgeScreen(
    uiState: MegaCollectorUiState,
    onRecycleSingle: (OwnedCard) -> Unit,
    onRecycleAllDuplicates: () -> Unit,
    onInspectCard: (Long) -> Unit,
    onButtonClickSfx: () -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var pendingConfirmCard by remember { mutableStateOf<OwnedCard?>(null) }

    val duplicateExtras = remember(uiState.ownedCards) {
        uiState.ownedCards
            .groupBy { it.catalogId }
            .values
            .filter { it.size > 1 }
            .flatMap { group ->
                group.sortedWith(
                    compareByDescending<OwnedCard> { it.isMisprint }
                        .thenByDescending { it.isListedForTrade }
                        .thenBy { it.obtainedAtMs }
                ).drop(1)
            }
    }

    val safeAutoExtras = remember(duplicateExtras) {
        duplicateExtras.filter { !it.isMisprint && !it.isListedForTrade }
    }
    val autoReclaimGold = remember(safeAutoExtras) {
        safeAutoExtras.sumOf { it.recycleValueGold }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it }
    ) {
        FrostedGlassCard(
            cornerRadius = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = null,
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECYCLE FORGE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = InkSlate
                        )
                    }
                    CoinsPill(coins = uiState.wallet.coins)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Convert duplicate skins into Coins to buy MEGA Legends Pack bundles. All 1-of-1 Misprints and 1 copy of each skin stay protected during batch forge.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRecycleAllDuplicates,
                    enabled = safeAutoExtras.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.buttonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("recycle_all_duplicates_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (safeAutoExtras.isNotEmpty()) {
                            "FORGE ${safeAutoExtras.size} DUPLICATES (+%,d COINS)".format(autoReclaimGold)
                        } else {
                            "NO SAFE DUPLICATES TO FORGE"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        if (uiState.ownedCards.isEmpty()) {
            EmptyVaultState(
                title = "Nothing to Recycle Yet",
                subtitle = "Open booster packs in the Packs tab. Any duplicate pulls will appear here ready to forge into Coins!",
                modifier = Modifier.weight(1f)
            )
        } else {
            val displayList = if (duplicateExtras.isNotEmpty()) duplicateExtras else uiState.ownedCards
            Text(
                text = if (duplicateExtras.isNotEmpty()) "DUPLICATE COPIES (${duplicateExtras.size})"
                else "ALL VAULT CARDS (INDIVIDUAL RECYCLE)",
                style = MaterialTheme.typography.labelMedium,
                color = SlateMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayList, key = { it.instanceId }) { card ->
                    RecycleCardRow(
                        card = card,
                        onInspect = { onInspectCard(card.instanceId) },
                        onRecycleClick = {
                            onButtonClickSfx()
                            pendingConfirmCard = card
                        }
                    )
                }
            }
        }
    }

    pendingConfirmCard?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingConfirmCard = null },
            containerColor = PureWhiteCard,
            title = {
                Text(
                    text = if (card.isMisprint) "Confirm Misprint Recycle" else "Recycle Skin Card?",
                    color = if (card.isMisprint) StudioCrimson else InkSlate
                )
            },
            text = {
                Text(
                    text = if (card.isMisprint) {
                        "${card.skinName} (${card.serialCode}) is a 1-of-1 Misprint! Are you sure you want to melt it down for +${card.recycleValueGold} Coins?"
                    } else {
                        "Recycle ${card.heroName} — ${card.skinName} for +${card.recycleValueGold} Coins?"
                    },
                    color = SlateMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = card
                        pendingConfirmCard = null
                        onRecycleSingle(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson)
                ) {
                    Text("Forge +${card.recycleValueGold} Coins", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        pendingConfirmCard = null
                    }
                ) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
private fun RecycleCardRow(
    card: OwnedCard,
    onInspect: () -> Unit,
    onRecycleClick: () -> Unit
) {
    val palette = CollectorTheme.colors
    FrostedGlassCard(
        cornerRadius = 18.dp,
        borderColor = if (card.isMisprint) palette.misprintLabel.copy(alpha = 0.65f) else card.rarity.primaryColor.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onInspect)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SkyFrost)
            ) {
                if (card.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = rememberMlbbImageRequest(card.imageUrl),
                        contentDescription = card.skinName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.skinName,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSlate,
                    maxLines = 1
                )
                Text(
                    text = "${card.heroName} • ${card.serialCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onRecycleClick,
                border = BorderStroke(1.2.dp, palette.accentPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("recycle_single_${card.instanceId}")
            ) {
                Text(
                    text = "+${card.recycleValueGold}",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.accentPrimary
                )
            }
        }
    }
}
