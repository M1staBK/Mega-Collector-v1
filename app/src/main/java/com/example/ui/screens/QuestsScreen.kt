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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.QuestCategory
import com.example.model.QuestUiItem
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.SettingsGearButton
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldText
import com.example.ui.viewmodel.MegaCollectorUiState

@Composable
fun QuestsScreen(
    uiState: MegaCollectorUiState,
    onClaimQuest: (QuestUiItem) -> Unit,
    onOpenSettings: () -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var selectedCategory by remember { mutableStateOf(QuestCategory.DAILY) }

    val filteredQuests = remember(uiState.quests, selectedCategory) {
        uiState.quests.filter { it.definition.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.08f else 0.30f),
                        palette.canvas
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Summary Card
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, IceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Collector Quests",
                                style = MaterialTheme.typography.headlineMedium,
                                color = InkSlate
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val claimableTotal = uiState.quests.count { it.canClaim }
                            if (claimableTotal > 0) {
                                Surface(
                                    color = StudioGold,
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = "$claimableTotal Ready",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.goldButtonContent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            SettingsGearButton(onClick = onOpenSettings)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoinsPill(coins = uiState.wallet.coins)
                        DiamondsPill(diamonds = uiState.wallet.diamonds)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuestCategory.entries.forEach { cat ->
                            val readyInCat = uiState.quests.count { it.definition.category == cat && it.canClaim }
                            val label = if (readyInCat > 0) {
                                "${cat.title.substringBefore(" ")} ($readyInCat)"
                            } else {
                                cat.title.substringBefore(" ")
                            }
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    onButtonClickSfx()
                                    selectedCategory = cat
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepCobalt,
                                    selectedLabelColor = Color.White,
                                    containerColor = SkyFrost,
                                    labelColor = InkSlate
                                ),
                                modifier = Modifier.testTag("quest_tab_${cat.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (selectedCategory == QuestCategory.COMMUNITY) {
                    item {
                        Surface(
                            color = SkyFrost,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.community_quest_notice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSlate
                                )
                            }
                        }
                    }
                }

                items(filteredQuests, key = { it.definition.id }) { item ->
                    QuestCardRow(
                        quest = item,
                        onClaim = { onClaimQuest(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestCardRow(
    quest: QuestUiItem,
    onClaim: () -> Unit
) {
    val palette = CollectorTheme.colors
    val def = quest.definition
    val shownProgress = quest.currentProgress.coerceAtMost(def.targetCount)

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (quest.canClaim) 4.dp else 1.dp,
        border = BorderStroke(
            width = if (quest.canClaim) 1.8.dp else 1.dp,
            color = when {
                quest.canClaim -> StudioGold
                quest.isClaimed -> RarityUncommon.copy(alpha = 0.55f)
                else -> IceBorder
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = def.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkSlate
                    )
                    Text(
                        text = def.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                when {
                    quest.isClaimed -> {
                        Surface(
                            color = RarityUncommon.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Claimed",
                                    tint = RarityUncommon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Claimed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RarityUncommon
                                )
                            }
                        }
                    }

                    quest.canClaim -> {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioGold,
                                contentColor = palette.goldButtonContent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("claim_quest_${def.id}")
                        ) {
                            Text("Claim", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    else -> {
                        Text(
                            text = "$shownProgress/${def.targetCount}",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accentPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { quest.progressFraction },
                color = if (quest.isCompleted) RarityUncommon else palette.accentPrimary,
                trackColor = SkyFrost,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (def.coinReward > 0) {
                    Surface(
                        color = palette.goldSurfaceTint,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+%,d 🪙".format(def.coinReward),
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioGoldText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (def.diamondReward > 0) {
                    Surface(
                        color = SkyFrost,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${def.diamondReward} 💎",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accentPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (def.freePackReward > 0) {
                    Surface(
                        color = MisprintMagenta.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${def.freePackReward} Free Pack",
                            style = MaterialTheme.typography.labelSmall,
                            color = MisprintMagenta,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
