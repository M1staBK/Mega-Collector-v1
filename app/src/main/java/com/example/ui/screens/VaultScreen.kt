package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.ui.components.EmptyVaultState
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.HolographicCardView
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldBright
import com.example.ui.viewmodel.MegaCollectorUiState

private enum class VaultFilter(val label: String) {
    ALL("All"),
    MISPRINTS("1/1 Misprints"),
    HOLO("Holo"),
    DUPLICATES("Duplicates"),
    LEGEND("Legend"),
    COLLECTOR("Collector"),
    LUCKYBOX("Luckybox"),
    EPIC("Epic"),
    SPECIAL("Special"),
    UNCOMMON("Uncommon"),
    COMMON("Common")
}

private enum class VaultSortOrder(val label: String) {
    RARITY("Rarity"),
    NEWEST("Newest"),
    NAME("Hero A-Z")
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
    var sortOrder by remember { mutableStateOf(VaultSortOrder.RARITY) }

    val countByCatalog = remember(uiState.ownedCards) {
        uiState.ownedCards.groupingBy { it.catalogId }.eachCount()
    }
    val uniqueCount = countByCatalog.size

    val filteredCards = remember(uiState.ownedCards, searchQuery, selectedFilter, sortOrder, countByCatalog) {
        val base = uiState.ownedCards.filter { card ->
            val matchesQuery = searchQuery.isBlank() ||
                card.skinName.contains(searchQuery, ignoreCase = true) ||
                card.heroName.contains(searchQuery, ignoreCase = true) ||
                card.serialCode.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                VaultFilter.ALL -> true
                VaultFilter.MISPRINTS -> card.isMisprint
                VaultFilter.HOLO -> card.isHolographicEdition
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
        when (sortOrder) {
            VaultSortOrder.RARITY -> base.sortedWith(
                compareByDescending<OwnedCard> { it.isMisprint }
                    .thenByDescending { it.rarity.ordinal }
                    .thenByDescending { it.obtainedAtMs }
            )
            VaultSortOrder.NEWEST -> base.sortedByDescending { it.obtainedAtMs }
            VaultSortOrder.NAME -> base.sortedWith(compareBy({ it.heroName }, { it.skinName }))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it }
    ) {
        // Frosted Collector Vault Hero Banner Card (matching screenshot)
        FrostedGlassCard(
            cornerRadius = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                palette.frostSurface.copy(alpha = if (palette.isDark) 0.55f else 0.72f),
                                palette.cardSurface.copy(alpha = 0.85f),
                                palette.gloomHalo.copy(alpha = if (palette.isDark) 0.30f else 0.38f)
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = palette.accentPrimary.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(18.dp)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Collector Vault",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.ownedCards.size} total cards • $uniqueCount unique skins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted
                    )

                    if (uiState.catalog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val progress = (uniqueCount.toFloat() / uiState.catalog.size.toFloat()).coerceIn(0f, 1f)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = palette.frostSurface,
                                shape = CircleShape,
                                border = BorderStroke(1.5.dp, palette.accentPrimary),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(palette.frostSurface)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = progress.coerceAtLeast(0.03f))
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(palette.buttonPrimary, palette.skyAccent)
                                                )
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = "Dex Completion: $uniqueCount / ${uiState.catalog.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.ink
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Pill + Circular Sort Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search cards, skins, or heroes...",
                        color = SlateMuted,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = palette.muted)
                },
                singleLine = true,
                shape = RoundedCornerShape(999.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.accentPrimary,
                    unfocusedBorderColor = palette.glassHighlight,
                    focusedContainerColor = palette.cardSurface.copy(alpha = 0.92f),
                    unfocusedContainerColor = palette.cardSurface.copy(alpha = 0.86f),
                    focusedTextColor = InkSlate,
                    unfocusedTextColor = InkSlate
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("vault_search_input")
            )

            Surface(
                color = palette.cardSurface,
                shape = CircleShape,
                shadowElevation = 2.dp,
                border = BorderStroke(1.2.dp, palette.glassHighlight)
            ) {
                IconButton(
                    onClick = {
                        onButtonClickSfx()
                        val orders = VaultSortOrder.entries
                        sortOrder = orders[(sortOrder.ordinal + 1) % orders.size]
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("vault_sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort by ${sortOrder.label}",
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pill Filter Chips Row
        LazyRow(
            modifier = Modifier.testTag("vault_filter_row"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VaultFilter.entries) { filter ->
                val selected = selectedFilter == filter
                Surface(
                    color = if (selected) {
                        palette.buttonPrimary
                    } else {
                        palette.cardSurface
                    },
                    shape = RoundedCornerShape(999.dp),
                    shadowElevation = if (selected) 3.dp else 1.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) palette.buttonPrimary else palette.glassHighlight
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(999.dp))
                        .clickable {
                            onButtonClickSfx()
                            selectedFilter = filter
                        }
                        .testTag("vault_filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        ),
                        color = if (selected) Color.White else palette.muted,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCards.isEmpty()) {
            EmptyVaultState(
                title = if (uiState.ownedCards.isEmpty()) "Your Vault is Empty" else "No Matching Skins",
                subtitle = if (uiState.ownedCards.isEmpty()) {
                    "Head to the Packs tab and open a booster pack to pull MOBA Legends skins!"
                } else {
                    "Try clearing your search or selecting another rarity filter."
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 152.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
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
                color = palette.cardSurface,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, palette.accentPrimary),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "x$duplicateCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accentPrimary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }

        if (card.isListedForTrade) {
            Surface(
                color = palette.accentPrimary.copy(alpha = 0.92f),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "TRADE LISTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }
    }
}
