package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.model.CosmeticCatalog
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.ui.components.BinderBookmark
import com.example.ui.components.PremiumBinder
import com.example.ui.viewmodel.MegaCollectorUiState

/**
 * The collection is a binder. There is no card grid any more: the cover opens to four large cards
 * at a time, and the bookmarks on the edge jump to one kind of card (rarity, holo, 1/1 misprints).
 */
@Composable
fun VaultScreen(
    uiState: MegaCollectorUiState,
    onInspectCard: (Long) -> Unit,
    onButtonClickSfx: () -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val binder = remember(uiState.wallet.equippedBinderId) {
        CosmeticCatalog.binderById(uiState.wallet.equippedBinderId)
    }
    var selected by remember { mutableStateOf("ALL") }

    // One copy of each card (the best one), rarest first
    val unique = remember(uiState.ownedCards) {
        uiState.ownedCards
            .groupBy { it.catalogId }
            .map { (_, copies) ->
                copies.maxWith(compareBy<OwnedCard>({ it.isMisprint }, { it.rarity.ordinal }, { it.obtainedAtMs }))
            }
            .sortedWith(
                compareByDescending<OwnedCard> { it.isMisprint }
                    .thenByDescending { it.rarity.ordinal }
                    .thenBy { it.heroName }
            )
    }

    val bookmarks = remember(unique) {
        buildList {
            add(BinderBookmark("ALL", "ALL", Color(0xFFE2E8F0), unique.size))
            add(BinderBookmark("MISPRINT", "1/1", Color(0xFFF43F5E), unique.count { it.isMisprint }))
            add(BinderBookmark("HOLO", "HOLO", Color(0xFF38BDF8), unique.count { it.isHolographicEdition }))
            listOf(
                SkinRarity.LEGEND to "LEGEND",
                SkinRarity.COLLECTOR to "COLLECT",
                SkinRarity.LUCKYBOX to "LUCKY",
                SkinRarity.EPIC to "EPIC",
                SkinRarity.SPECIAL to "SPECIAL",
                SkinRarity.UNCOMMON to "UNCOM",
                SkinRarity.COMMON to "COMMON"
            ).forEach { (rarity, label) ->
                add(BinderBookmark(rarity.name, label, rarity.primaryColor, unique.count { it.rarity == rarity }))
            }
        }
    }

    val shown = remember(unique, selected) {
        when (selected) {
            "ALL" -> unique
            "MISPRINT" -> unique.filter { it.isMisprint }
            "HOLO" -> unique.filter { it.isHolographicEdition }
            else -> unique.filter { it.rarity.name == selected }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it }
    ) {
        PremiumBinder(
            binder = binder,
            cards = shown,
            totalCollected = unique.size,
            bookmarks = bookmarks,
            selectedBookmark = selected,
            onSelectBookmark = { selected = it },
            onInspectCard = onInspectCard,
            onButtonClickSfx = onButtonClickSfx
        )
    }
}
