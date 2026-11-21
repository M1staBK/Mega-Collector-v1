package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SkinRarity
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.MlbbDexCreditCard
import com.example.ui.components.SettingsGearButton
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyAzure
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldText
import com.example.ui.viewmodel.MegaCollectorUiState

@Composable
fun ProfileScreen(
    uiState: MegaCollectorUiState,
    onUpdateAlias: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var showEditAlias by remember { mutableStateOf(false) }

    val uniqueSkinCount = remember(uiState.ownedCards) {
        uiState.ownedCards.map { it.catalogId }.distinct().size
    }
    val misprintCount = remember(uiState.ownedCards) {
        uiState.ownedCards.count { it.isMisprint }
    }
    val totalPrestige = remember(uiState.ownedCards) {
        uiState.ownedCards.sumOf { it.prestigeScore }
    }

    val collectorRank = remember(totalPrestige) {
        when {
            totalPrestige >= 15000 -> "Immortal Collector"
            totalPrestige >= 7500 -> "Mythical Glory"
            totalPrestige >= 3500 -> "Mythic"
            totalPrestige >= 1500 -> "Legend"
            totalPrestige >= 500 -> "Epic"
            else -> "Warrior"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PearlCanvas)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 3.dp,
                border = BorderStroke(1.5.dp, SkyAzure.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(SkyAzure, StudioGold))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Collector avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.wallet.collectorAlias,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = InkSlate
                                    )
                                    IconButton(
                                        onClick = {
                                            onButtonClickSfx()
                                            showEditAlias = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit collector name",
                                            tint = SlateMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = StudioGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$collectorRank • $totalPrestige Power",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = StudioGoldText
                                    )
                                }
                            }
                        }

                        SettingsGearButton(onClick = onOpenSettings)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoinsPill(coins = uiState.wallet.coins)
                        DiamondsPill(diamonds = uiState.wallet.diamonds)
                    }
                }
            }
        }

        // KPI Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "Total Cards",
                    value = "${uiState.ownedCards.size}",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Unique Skins",
                    value = "$uniqueSkinCount",
                    accent = StudioGoldText,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "1/1 Misprints",
                    value = "$misprintCount",
                    accent = palette.misprintLabel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "Packs Opened",
                    value = "${uiState.wallet.totalPacksOpened}",
                    accent = InkSlate,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Coins Recycled",
                    value = "%,d".format(uiState.wallet.totalCoinsRecycled),
                    accent = StudioGoldText,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Daily Streak",
                    value = "${uiState.wallet.dailyStreakCount}d",
                    accent = palette.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7-Tier Rarity Distribution Breakdown
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, IceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "COLLECTION BY RARITY",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                    val total = uiState.ownedCards.size.coerceAtLeast(1)
                    SkinRarity.entries.reversed().forEach { rarity ->
                        val count = uiState.ownedCards.count { it.rarity == rarity }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = rarity.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = rarity.primaryColor
                                )
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkSlate
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / total.toFloat() },
                                color = rarity.primaryColor,
                                trackColor = SkyFrost,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = palette.misprintLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1-of-1 Misprint Anomalies",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.misprintLabel
                            )
                        }
                        Text(
                            text = "$misprintCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.misprintLabel
                        )
                    }
                }
            }
        }

        // Credits & MLBBDex.com Attribution
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MlbbDexCreditCard(skinCount = uiState.catalog.size)
                Surface(
                    color = PureWhiteCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.api_credit_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accentPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.api_credit_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }
            }
        }
    }

    if (showEditAlias) {
        var draftAlias by remember { mutableStateOf(uiState.wallet.collectorAlias) }
        AlertDialog(
            onDismissRequest = { showEditAlias = false },
            containerColor = PureWhiteCard,
            title = { Text("Collector Tag", color = InkSlate) },
            text = {
                OutlinedTextField(
                    value = draftAlias,
                    onValueChange = { draftAlias = it },
                    singleLine = true,
                    label = { Text("Display Alias") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAlias(draftAlias)
                        showEditAlias = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCobalt,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onButtonClickSfx()
                        showEditAlias = false
                    }
                ) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = PureWhiteCard,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, IceBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted
            )
        }
    }
}
