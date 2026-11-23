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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.QuestCategory
import com.example.model.QuestUiItem
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.SettingsGearButton
import com.example.ui.theme.CollectorTheme
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
    onGoToBinder: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onButtonClickSfx: () -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var selectedCategory by remember { mutableStateOf(QuestCategory.DAILY) }

    val filteredQuests = remember(uiState.quests, selectedCategory) {
        uiState.quests.filter { it.definition.category == selectedCategory }
    }
    val uniqueSkinCount = remember(uiState.ownedCards) {
        uiState.ownedCards.map { it.catalogId }.distinct().size
    }
    val catalogGoal = uiState.catalog.size.coerceAtLeast(226)
    val claimableTotal = remember(uiState.quests) { uiState.quests.count { it.canClaim } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Royal-to-Sky Dex Missions Header Banner (inspired by TCG Pocket Dex Missions)
            Surface(
                shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, palette.glassHighlight.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                if (palette.isDark) {
                                    listOf(palette.gloomHalo, palette.frostSurface)
                                } else {
                                    listOf(palette.buttonPrimary, palette.skyAccent)
                                }
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.size(48.dp))
                        Text(
                            text = "Quests & Missions",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        SettingsGearButton(onClick = onOpenSettings)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Binder Book Emblem + White Count Pill Capsule
                    Box(contentAlignment = Alignment.TopCenter) {
                        Surface(
                            color = PureWhiteCard,
                            shape = RoundedCornerShape(999.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 28.dp, bottom = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Style,
                                        contentDescription = null,
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$uniqueSkinCount / $catalogGoal",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = InkSlate
                                    )
                                }
                                Text(
                                    text = if (claimableTotal > 0) "$claimableTotal mission reward${if (claimableTotal > 1) "s" else ""} ready!" else "Collect skins & rip packs to unlock rewards",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (claimableTotal > 0) palette.accentPrimary else SlateMuted
                                )
                            }
                        }

                        Surface(
                            color = palette.buttonPrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, Color.White),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Capsule Actions + Category Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PureWhiteCard,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    onButtonClickSfx()
                                    onGoToBinder()
                                }
                                .testTag("missions_to_binder_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "To Card Vault",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkSlate
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "${uiState.wallet.coins} 🪙  •  ${uiState.wallet.diamonds} 💎",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Frosted Pill Category Filter Switcher
            Surface(
                color = palette.cardSurface,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.2.dp, palette.glassHighlight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuestCategory.entries.forEach { cat ->
                        val selected = selectedCategory == cat
                        val readyInCat = uiState.quests.count { it.definition.category == cat && it.canClaim }
                        val label = if (readyInCat > 0) {
                            "${cat.title.substringBefore(" ")} ($readyInCat)"
                        } else {
                            cat.title.substringBefore(" ")
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    brush = if (selected) {
                                        Brush.horizontalGradient(
                                            listOf(palette.buttonPrimary, palette.skyAccent.copy(alpha = 0.92f))
                                        )
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    }
                                )
                                .clickable {
                                    onButtonClickSfx()
                                    selectedCategory = cat
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                .testTag("quest_tab_${cat.name.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
                                ),
                                color = if (selected) Color.White else palette.muted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 18.dp),
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

    val (rewardIcon, rewardCountLabel, rewardTint) = when {
        def.freePackReward > 0 -> Triple(Icons.Default.CardGiftcard, "${def.freePackReward}", palette.accentPrimary)
        def.diamondReward > 0 -> Triple(Icons.Default.Diamond, "${def.diamondReward}", palette.accentPrimary)
        else -> Triple(Icons.Default.MonetizationOn, "${def.coinReward}", palette.accentPrimary)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = if (quest.canClaim) palette.buttonPrimary else palette.cardSurface,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = if (quest.canClaim) 5.dp else 2.dp,
            border = BorderStroke(
                width = if (quest.canClaim) 1.6.dp else 1.2.dp,
                color = when {
                    quest.canClaim -> palette.accentPrimary
                    quest.isClaimed -> palette.accentPrimary.copy(alpha = 0.45f)
                    else -> palette.glassHighlight
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            quest.canClaim -> Brush.horizontalGradient(
                                listOf(palette.buttonPrimary, palette.skyAccent)
                            )
                            else -> Brush.horizontalGradient(
                                listOf(
                                    palette.cardSurface,
                                    palette.cardSurface,
                                    palette.frostSurface
                                )
                            )
                        }
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Reward Emblem Square Badge
                    Surface(
                        color = if (quest.canClaim) Color.White else palette.frostSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, rewardTint.copy(alpha = 0.45f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = rewardIcon,
                                contentDescription = null,
                                tint = rewardTint,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = rewardCountLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = if (quest.canClaim) Color(0xFF0F172A) else palette.ink
                            )
                        }
                    }

                    // Mission Title + Progress Bar / Reward Summary
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = def.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (quest.canClaim) Color.White else palette.ink
                        )
                        Text(
                            text = def.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (quest.canClaim) Color.White.copy(alpha = 0.90f) else palette.muted
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { quest.progressFraction },
                                color = when {
                                    quest.canClaim -> Color.White
                                    quest.isClaimed -> palette.accentPrimary
                                    else -> palette.accentPrimary
                                },
                                trackColor = if (quest.canClaim) Color.White.copy(alpha = 0.35f) else palette.frostSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )

                            when {
                                quest.isClaimed -> {
                                    Surface(
                                        color = palette.frostSurface,
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Claimed",
                                                tint = palette.accentPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Claimed",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.accentPrimary
                                            )
                                        }
                                    }
                                }

                                quest.canClaim -> {
                                    Button(
                                        onClick = onClaim,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = palette.buttonPrimary
                                        ),
                                        shape = RoundedCornerShape(999.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("claim_quest_${def.id}")
                                    ) {
                                        Text(
                                            text = "Claim",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                    }
                                }

                                else -> {
                                    Text(
                                        text = "$shownProgress/${def.targetCount}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = palette.ink
                                    )
                                }
                            }
                        }

                        if (!quest.canClaim && (def.coinReward > 0 && def.diamondReward > 0)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+%,d 🪙  •  +${def.diamondReward} 💎".format(def.coinReward),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accentPrimary
                            )
                        }
                    }
                }
            }
        }

        // Red '!' Alert Pip when Ready to Claim
        if (quest.canClaim) {
            Surface(
                color = palette.crimson,
                shape = CircleShape,
                border = BorderStroke(1.5.dp, Color.White),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
