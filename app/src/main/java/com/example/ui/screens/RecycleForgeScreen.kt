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
import com.example.ui.components.ArenaGoldPill
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
fun RecycleForgeScreen(
    uiState: MegaCollectorUiState,
    onRecycleSingle: (OwnedCard) -> Unit,
    onRecycleAllDuplicates: () -> Unit,
    onInspectCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
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
            .background(VoidObsidian)
            .statusBarsPadding()
    ) {
        // Forge Header
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, OutlineSubtle),
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
                            tint = PrismCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECYCLE FORGE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                    }
                    ArenaGoldPill(gold = uiState.wallet.arenaGold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Convert duplicate skins into Arena Gold to buy Mythic & Legend packs. All 1-of-1 Misprints and 1 copy of each skin are protected during batch forge.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRecycleAllDuplicates,
                    enabled = safeAutoExtras.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaGold,
                        contentColor = VoidObsidian
                    ),
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
                            "FORGE ${safeAutoExtras.size} DUPLICATES (+%,d GOLD)".format(autoReclaimGold)
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
                subtitle = "Open booster packs in the Shop tab. Any duplicate pulls will appear here ready to forge into Gold!",
                modifier = Modifier.weight(1f)
            )
        } else {
            val displayList = if (duplicateExtras.isNotEmpty()) duplicateExtras else uiState.ownedCards
            Text(
                text = if (duplicateExtras.isNotEmpty()) "DUPLICATE COPIES (${duplicateExtras.size})"
                else "ALL VAULT CARDS (INDIVIDUAL RECYCLE)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
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
                        onRecycleClick = { pendingConfirmCard = card }
                    )
                }
            }
        }
    }

    pendingConfirmCard?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingConfirmCard = null },
            containerColor = SurfaceElevated,
            title = {
                Text(
                    text = if (card.isMisprint) "Confirm Misprint Recycle" else "Recycle Skin Card?",
                    color = if (card.isMisprint) MythicCrimson else ArenaGold
                )
            },
            text = {
                Text(
                    text = if (card.isMisprint) {
                        "${card.skinName} (${card.serialCode}) is a 1-of-1 Misprint! Are you sure you want to melt it down for +${card.recycleValueGold} Gold?"
                    } else {
                        "Recycle ${card.heroName} — ${card.skinName} for +${card.recycleValueGold} Arena Gold?"
                    },
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = card
                        pendingConfirmCard = null
                        onRecycleSingle(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MythicCrimson)
                ) {
                    Text("Forge +${card.recycleValueGold} Gold", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirmCard = null }) {
                    Text("Cancel", color = TextSecondary)
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
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (card.isMisprint) MisprintNeon else card.rarity.primaryColor.copy(alpha = 0.45f)
        ),
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
                    .background(SurfaceElevated)
            ) {
                if (card.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = card.imageUrl,
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
                            tint = MisprintNeon,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.skinName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${card.heroName} • ${card.serialCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onRecycleClick,
                border = BorderStroke(1.dp, ArenaGold.copy(alpha = 0.7f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("recycle_single_${card.instanceId}")
            ) {
                Text(
                    text = "+${card.recycleValueGold}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArenaGold
                )
            }
        }
    }
}
