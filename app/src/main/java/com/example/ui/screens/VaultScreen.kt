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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.HolographicCardView
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import com.example.ui.viewmodel.MegaCollectorUiState

private enum class VaultFilter(val label: String) {
    ALL("All"),
    MISPRINTS("1/1 Misprints"),
    DUPLICATES("Duplicates"),
    LEGEND("Legend"),
    COLLECTOR("Collector"),
    EPIC("Epic"),
    SPECIAL("Special"),
    ELITE("Elite"),
    COMMON("Common")
}

@Composable
fun VaultScreen(
    uiState: MegaCollectorUiState,
    onInspectCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(VaultFilter.ALL) }

    val countByCatalog = remember(uiState.ownedCards) {
        uiState.ownedCards.groupingBy { it.catalogId }.eachCount()
    }
    val uniqueCount = countByCatalog.size
    val misprintCount = remember(uiState.ownedCards) {
        uiState.ownedCards.count { it.isMisprint }
    }

    val filteredCards = remember(uiState.ownedCards, searchQuery, selectedFilter, countByCatalog) {
        uiState.ownedCards.filter { card ->
            val matchesQuery = searchQuery.isBlank() ||
                card.skinName.contains(searchQuery, ignoreCase = true) ||
                card.heroName.contains(searchQuery, ignoreCase = true) ||
                card.serialCode.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                VaultFilter.ALL -> true
                VaultFilter.MISPRINTS -> card.isMisprint
                VaultFilter.DUPLICATES -> (countByCatalog[card.catalogId] ?: 1) > 1
                VaultFilter.LEGEND -> card.rarity == SkinRarity.LEGEND
                VaultFilter.COLLECTOR -> card.rarity == SkinRarity.COLLECTOR
                VaultFilter.EPIC -> card.rarity == SkinRarity.EPIC
                VaultFilter.SPECIAL -> card.rarity == SkinRarity.SPECIAL
                VaultFilter.ELITE -> card.rarity == SkinRarity.ELITE
                VaultFilter.COMMON -> card.rarity == SkinRarity.COMMON
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidObsidian)
            .statusBarsPadding()
    ) {
        // Header Summary
        Surface(
            color = SurfaceElevated,
            border = BorderStroke(1.dp, OutlineSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COLLECTOR VAULT",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArenaGold
                        )
                        Text(
                            text = "${uiState.ownedCards.size} total cards • $uniqueCount unique skins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        color = MisprintNeon.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MisprintNeon.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MisprintNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$misprintCount 1/1",
                                style = MaterialTheme.typography.labelMedium,
                                color = MisprintNeon
                            )
                        }
                    }
                }

                if (uiState.catalog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = (uniqueCount.toFloat() / uiState.catalog.size.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        color = PrismCyan,
                        trackColor = SurfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dex Completion: $uniqueCount / ${uiState.catalog.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search hero, skin, or serial...", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrismCyan,
                unfocusedBorderColor = OutlineSubtle,
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("vault_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VaultFilter.entries) { filter ->
                val selected = selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArenaGold,
                        selectedLabelColor = VoidObsidian,
                        containerColor = SurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCards.isEmpty()) {
            EmptyVaultState(
                title = if (uiState.ownedCards.isEmpty()) "Your Vault is Empty" else "No Matching Skins",
                subtitle = if (uiState.ownedCards.isEmpty()) {
                    "Head to the Packs tab and open your free Starter Arena Pack to pull MOBA Legends skins!"
                } else {
                    "Try clearing your search or selecting another rarity filter."
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 152.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCards, key = { it.instanceId }) { card ->
                    val dupCount = countByCatalog[card.catalogId] ?: 1
                    VaultGridCardItem(
                        card = card,
                        duplicateCount = dupCount,
                        onClick = { onInspectCard(card.instanceId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultGridCardItem(
    card: OwnedCard,
    duplicateCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("vault_card_${card.instanceId}")
    ) {
        HolographicCardView(
            card = card,
            compact = true
        )

        if (duplicateCount > 1) {
            Surface(
                color = VoidObsidian.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ArenaGold),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 34.dp, end = 8.dp)
            ) {
                Text(
                    text = "x$duplicateCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaGold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (card.isListedForTrade) {
            Surface(
                color = PrismCyan.copy(alpha = 0.92f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "TRADE LISTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = VoidObsidian,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
