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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.HolographicCardView
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGoldBright
import com.example.ui.viewmodel.MegaCollectorUiState

private enum class VaultFilter(val label: String) {
    ALL("All"),
    MISPRINTS("1/1 Misprints"),
    DUPLICATES("Duplicates"),
    LEGEND("Legend"),
    COLLECTOR("Collector"),
    LUCKYBOX("Luckybox"),
    EPIC("Epic"),
    SPECIAL("Special"),
    UNCOMMON("Uncommon"),
    COMMON("Common")
}

@Composable
fun VaultScreen(
    uiState: MegaCollectorUiState,
    onInspectCard: (Long) -> Unit,
    onButtonClickSfx: () -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
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
                VaultFilter.LUCKYBOX -> card.rarity == SkinRarity.LUCKYBOX
                VaultFilter.EPIC -> card.rarity == SkinRarity.EPIC
                VaultFilter.SPECIAL -> card.rarity == SkinRarity.SPECIAL
                VaultFilter.UNCOMMON -> card.rarity == SkinRarity.UNCOMMON
                VaultFilter.COMMON -> card.rarity == SkinRarity.COMMON
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PearlCanvas)
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it }
    ) {
        // Header Summary
        Surface(
            color = PureWhiteCard,
            border = BorderStroke(1.dp, IceBorder),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp)
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
                            color = palette.accentPrimary
                        )
                        Text(
                            text = "${uiState.ownedCards.size} total cards • $uniqueCount unique skins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                    Surface(
                        color = SkyFrost,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.misprintLabel.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = palette.misprintLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$misprintCount 1/1",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.misprintLabel
                            )
                        }
                    }
                }

                if (uiState.catalog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = (uniqueCount.toFloat() / uiState.catalog.size.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        color = palette.accentPrimary,
                        trackColor = SkyFrost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dex Completion: $uniqueCount / ${uiState.catalog.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }
            }
        }

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search hero, skin, or serial...", color = SlateMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = SlateMuted)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepCobalt,
                unfocusedBorderColor = IceBorder,
                focusedContainerColor = PureWhiteCard,
                unfocusedContainerColor = PureWhiteCard,
                focusedTextColor = InkSlate,
                unfocusedTextColor = InkSlate
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("vault_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 7-Tier Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VaultFilter.entries) { filter ->
                val selected = selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = {
                        onButtonClickSfx()
                        selectedFilter = filter
                    },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepCobalt,
                        selectedLabelColor = Color.White,
                        containerColor = PureWhiteCard,
                        labelColor = SlateMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCards.isEmpty()) {
            EmptyVaultState(
                title = if (uiState.ownedCards.isEmpty()) "Your Vault is Empty" else "No Matching Skins",
                subtitle = if (uiState.ownedCards.isEmpty()) {
                    "Head to the Packs tab and open your free Starter Studio Pack to pull MOBA Legends skins!"
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
    val palette = CollectorTheme.colors
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
                color = MoonlightNight.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StudioGoldBright),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "x$duplicateCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioGoldBright,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (card.isListedForTrade) {
            Surface(
                color = palette.accentPrimary.copy(alpha = 0.90f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "TRADE LISTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
