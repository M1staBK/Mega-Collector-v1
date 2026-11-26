package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.CounterBuildPlan
import com.example.model.HeroBuildGuide
import com.example.model.HeroCounterMatchup
import com.example.model.HeroMetaDetail
import com.example.model.MetaHeroStats
import com.example.model.MetaRankFilter
import com.example.model.MetaSortMode
import com.example.model.MetaTierGrade
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.rememberMlbbSquareImageRequest
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioGold
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.heightIn

data class MetaGuidesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rankFilter: MetaRankFilter = MetaRankFilter.MYTHIC,
    val sortMode: MetaSortMode = MetaSortMode.TIER_LIST,
    val heroes: List<MetaHeroStats> = emptyList(),
    val isLoadingDetail: Boolean = false,
    val selectedHeroDetail: HeroMetaDetail? = null,
    val detailError: String? = null
)

@Composable
fun MetaGuidesScreen(
    metaState: MetaGuidesUiState,
    onSelectRankFilter: (MetaRankFilter) -> Unit,
    onSelectSortMode: (MetaSortMode) -> Unit,
    onRefreshMeta: () -> Unit,
    onOpenHeroDetail: (MetaHeroStats) -> Unit,
    onDismissHeroDetail: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("All") }

    val roles = remember { listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support") }

    val filteredSortedHeroes = remember(
        metaState.heroes,
        metaState.sortMode,
        searchQuery,
        selectedRole
    ) {
        metaState.heroes
            .filter { hero ->
                val matchesQuery = searchQuery.isBlank() ||
                    hero.heroName.contains(searchQuery, ignoreCase = true) ||
                    hero.lane.contains(searchQuery, ignoreCase = true)
                val matchesRole = selectedRole == "All" ||
                    hero.roles.any { it.equals(selectedRole, ignoreCase = true) }
                matchesQuery && matchesRole
            }
            .let { list ->
                when (metaState.sortMode) {
                    MetaSortMode.TIER_LIST -> list.sortedByDescending { it.tierScore }
                    MetaSortMode.TOP_PICKS -> list.sortedByDescending { it.pickRatePct }
                    MetaSortMode.TOP_BANS -> list.sortedByDescending { it.banRatePct }
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.12f else 0.28f),
                        palette.canvas
                    )
                )
            )
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card
            item {
                Surface(
                    color = palette.cardSurface,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, palette.border)
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
                                    imageVector = Icons.Default.Insights,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "MOBA META",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.5.sp
                                        ),
                                        color = palette.accentPrimary
                                    )
                                    Text(
                                        text = "Last 7 days of ranked games  •  tier = win rate + ¼ ban rate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.muted
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        onButtonClickSfx()
                                        onRefreshMeta()
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("refresh_meta_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh meta analytics",
                                        tint = palette.accentPrimary
                                    )
                                }
                                SettingsGearButton(onClick = onOpenSettings)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sort / Mode Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetaSortMode.entries.forEach { mode ->
                                val selected = metaState.sortMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onButtonClickSfx()
                                        onSelectSortMode(mode)
                                    },
                                    label = {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepCobalt,
                                        selectedLabelColor = Color.White,
                                        containerColor = palette.frostSurface,
                                        labelColor = palette.ink
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("meta_mode_${mode.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rank Filter Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MetaRankFilter.entries) { filter ->
                                val selected = metaState.rankFilter == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onButtonClickSfx()
                                        onSelectRankFilter(filter)
                                    },
                                    label = { Text(filter.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.accentPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = palette.frostSurface,
                                        labelColor = palette.muted
                                    ),
                                    modifier = Modifier.testTag("meta_rank_${filter.apiParam}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search hero or lane...", color = palette.muted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = palette.muted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accentPrimary,
                                unfocusedBorderColor = palette.border,
                                focusedTextColor = palette.ink,
                                unfocusedTextColor = palette.ink
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meta_hero_search")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(roles) { role ->
                                val selected = selectedRole == role
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onButtonClickSfx()
                                        selectedRole = role
                                    },
                                    label = { Text(role) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioGold,
                                        selectedLabelColor = palette.goldButtonContent,
                                        containerColor = palette.frostSurface,
                                        labelColor = palette.muted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Loading / Error / Empty / Data states
            when {
                metaState.isLoading && metaState.heroes.isEmpty() -> {
                    item {
                        Surface(
                            color = palette.cardSurface,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, palette.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = palette.accentPrimary)
                                Text(
                                    text = "Loading the live MLBB meta...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.muted
                                )
                            }
                        }
                    }
                }

                metaState.errorMessage != null && metaState.heroes.isEmpty() -> {
                    item {
                        Surface(
                            color = palette.cardSurface,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = StudioCrimson,
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = metaState.errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.ink
                                )
                                Button(
                                    onClick = {
                                        onButtonClickSfx()
                                        onRefreshMeta()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DeepCobalt,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Retry Live Meta")
                                }
                            }
                        }
                    }
                }

                filteredSortedHeroes.isEmpty() -> {
                    item {
                        Surface(
                            color = palette.cardSurface,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, palette.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No heroes match \"$searchQuery\" in $selectedRole.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.muted,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }

                else -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredSortedHeroes.size} HEROES • ${metaState.rankFilter.label.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.muted
                            )
                            Text(
                                text = "Tap hero for Counters & Builds",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accentPrimary
                            )
                        }
                    }

                    val showPodium = metaState.sortMode == MetaSortMode.TIER_LIST &&
                        searchQuery.isBlank() && selectedRole == "All" && filteredSortedHeroes.size >= 3
                    if (showPodium) {
                        item(key = "meta_podium") {
                            MetaPodium(
                                heroes = filteredSortedHeroes.take(3),
                                onClick = { hero ->
                                    onButtonClickSfx()
                                    onOpenHeroDetail(hero)
                                }
                            )
                        }
                    }

                    val gradeCounts = filteredSortedHeroes.groupingBy { it.tierGrade }.eachCount()
                    filteredSortedHeroes.forEachIndexed { index, hero ->
                        if (metaState.sortMode == MetaSortMode.TIER_LIST &&
                            (index == 0 || filteredSortedHeroes[index - 1].tierGrade != hero.tierGrade)
                        ) {
                            item(key = "tier_header_${hero.tierGrade.name}") {
                                TierSectionHeader(grade = hero.tierGrade, count = gradeCounts[hero.tierGrade] ?: 0)
                            }
                        }
                        item(key = "${hero.heroId}_${hero.heroName}") {
                            MetaHeroRowCard(
                                hero = hero,
                                sortMode = metaState.sortMode,
                                onClick = {
                                    onButtonClickSfx()
                                    onOpenHeroDetail(hero)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val selectedDetail = metaState.selectedHeroDetail
    if (selectedDetail != null || metaState.isLoadingDetail || metaState.detailError != null) {
        HeroDetailGuideDialog(
            detail = selectedDetail,
            isLoading = metaState.isLoadingDetail,
            errorMessage = metaState.detailError,
            onDismiss = {
                onButtonClickSfx()
                onDismissHeroDetail()
            }
        )
    }
}

@Composable
private fun MetaHeroRowCard(
    hero: MetaHeroStats,
    sortMode: MetaSortMode,
    onClick: () -> Unit
) {
    val palette = CollectorTheme.colors
    val tierColor = tierBadgeColor(hero.tierGrade)

    Surface(
        color = palette.cardSurface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("meta_hero_card_${hero.slug}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.frostSurface),
                contentAlignment = Alignment.Center
            ) {
                if (hero.iconUrl.isNotBlank()) {
                    AsyncImage(
                        model = rememberMlbbSquareImageRequest(hero.iconUrl),
                        contentDescription = hero.heroName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = hero.heroName.take(2).uppercase(Locale.US),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.accentPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = hero.heroName,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = tierColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "${hero.tierGrade.label} Tier",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${hero.roles.joinToString("/")} • ${hero.lane}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip(
                        label = "Win",
                        value = "%.1f%%".format(hero.winRatePct),
                        highlight = hero.winRatePct >= 52.0,
                        accent = RarityUncommon
                    )
                    MetricChip(
                        label = "Pick",
                        value = "%.1f%%".format(hero.pickRatePct),
                        highlight = sortMode == MetaSortMode.TOP_PICKS,
                        accent = palette.accentPrimary
                    )
                    MetricChip(
                        label = "Ban",
                        value = "%.1f%%".format(hero.banRatePct),
                        highlight = sortMode == MetaSortMode.TOP_BANS,
                        accent = StudioCrimson
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    highlight: Boolean,
    accent: Color
) {
    val palette = CollectorTheme.colors
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = palette.muted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = if (highlight) accent else palette.ink
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroDetailGuideDialog(
    detail: HeroMetaDetail?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    val palette = CollectorTheme.colors
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 10.dp,
            border = BorderStroke(1.2.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.88f).dp)
                .testTag("hero_meta_detail_dialog")
        ) {
            if (isLoading && detail == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = palette.accentPrimary)
                    Text(
                        text = "Loading counters & build guides...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted
                    )
                }
            } else if (detail != null) {
                val hero = detail.heroStats
                val tierColor = tierBadgeColor(hero.tierGrade)
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.frostSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hero.iconUrl.isNotBlank()) {
                                AsyncImage(
                                    model = rememberMlbbSquareImageRequest(hero.iconUrl),
                                    contentDescription = hero.heroName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(hero.heroName.take(2), color = palette.accentPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = hero.heroName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.ink
                                )
                                Surface(
                                    color = tierColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(7.dp)
                                ) {
                                    Text(
                                        text = hero.tierGrade.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = tierColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${hero.roles.joinToString(" / ")} • ${hero.lane}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.muted
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.muted)
                        }
                    }

                    // Live Win / Pick / Ban progress breakdown
                    Surface(
                        color = palette.frostSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatRateBar("Win Rate", hero.winRatePct, RarityUncommon)
                            StatRateBar("Pick Rate", hero.pickRatePct, palette.accentPrimary)
                            StatRateBar("Ban Rate", hero.banRatePct, StudioCrimson)
                        }
                    }

                    // Counters & Matchups
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = StudioCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HERO COUNTERS & MATCHUPS",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.ink
                            )
                        }

                        if (detail.counteredBy.isEmpty() && detail.strongAgainst.isEmpty() && detail.synergies.isEmpty()) {
                            Text(
                                text = errorMessage
                                    ?: "No matchup records returned yet for ${hero.heroName} from arena.rone.dev.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.muted
                            )
                        } else {
                            if (detail.counteredBy.isNotEmpty()) {
                                MatchupGroupCard(
                                    title = "Countered By (Hard Matchups)",
                                    accent = StudioCrimson,
                                    items = detail.counteredBy
                                )
                            }
                            if (detail.strongAgainst.isNotEmpty()) {
                                MatchupGroupCard(
                                    title = "Strong Against",
                                    accent = RarityUncommon,
                                    items = detail.strongAgainst
                                )
                            }
                            if (detail.synergies.isNotEmpty()) {
                                MatchupGroupCard(
                                    title = "Best Teammate Synergies",
                                    accent = palette.accentPrimary,
                                    items = detail.synergies
                                )
                            }
                        }
                    }

                    // Counter build (rule based) and Moonton's own matchup notes
                    detail.counterBuild?.let { plan -> CounterBuildSection(plan = plan) }
                    if (detail.officialNotes.isNotEmpty()) {
                        OfficialNotesSection(notes = detail.officialNotes)
                    }

                    // Build Guides
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECOMMENDED BUILD GUIDES",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.ink
                            )
                        }

                        if (detail.buildGuides.isEmpty()) {
                            Text(
                                text = "No item build guide returned by arena.rone.dev for ${hero.heroName}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.muted
                            )
                        } else {
                            detail.buildGuides.forEach { guide ->
                                BuildGuideCard(guide = guide)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRateBar(
    label: String,
    percent: Double,
    color: Color
) {
    val palette = CollectorTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.muted)
            Text(
                text = "%.2f%%".format(percent),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (percent / 100.0).toFloat().coerceIn(0f, 1f) },
            color = color,
            trackColor = palette.cardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupGroupCard(
    title: String,
    accent: Color,
    items: List<HeroCounterMatchup>
) {
    val palette = CollectorTheme.colors
    Surface(
        color = palette.frostSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = accent
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { m ->
                    Surface(
                        color = palette.cardSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (m.iconUrl.isNotBlank()) {
                                AsyncImage(
                                    model = rememberMlbbSquareImageRequest(m.iconUrl),
                                    contentDescription = m.heroName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = m.heroName,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.ink
                            )
                            if (m.winRatePct > 0.0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "%.1f%%".format(m.winRatePct) +
                                        if (m.deltaWinRatePct != 0.0) " (%+.1f)".format(m.deltaWinRatePct) else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BuildGuideCard(guide: HeroBuildGuide) {
    val palette = CollectorTheme.colors
    Surface(
        color = palette.frostSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${guide.title} • ${guide.laneRole}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink
                )
                Text(
                    text = "%.1f%% WR".format(guide.proWinRatePct),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RarityUncommon
                )
            }
            Text(
                text = "Emblem: ${guide.emblemSet}  •  Spell: ${guide.battleSpell}",
                style = MaterialTheme.typography.labelSmall,
                color = palette.accentPrimary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                guide.items.forEach { item ->
                    Surface(
                        color = palette.cardSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.iconUrl.isNotBlank()) {
                                AsyncImage(
                                    model = rememberMlbbSquareImageRequest(item.iconUrl),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.ink
                            )
                        }
                    }
                }
            }
            if (guide.comboTip.isNotBlank()) {
                Text(
                    text = guide.comboTip,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = palette.ink.copy(alpha = 0.88f)
                )
            }
        }
    }
}

private fun tierBadgeColor(grade: MetaTierGrade): Color = when (grade) {
    MetaTierGrade.S_PLUS -> Color(0xFFE11D48)
    MetaTierGrade.S -> Color(0xFFD97706)
    MetaTierGrade.A -> Color(0xFF0284C7)
    MetaTierGrade.B -> Color(0xFF10B981)
    MetaTierGrade.C -> Color(0xFF64748B)
}


@Composable
private fun MetaPodium(
    heroes: List<MetaHeroStats>,
    onClick: (MetaHeroStats) -> Unit
) {
    val palette = CollectorTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(1, 0, 2).forEach { place ->
            val hero = heroes[place]
            val first = place == 0
            val tierColor = tierBadgeColor(hero.tierGrade)
            Surface(
                color = palette.cardSurface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = if (first) 6.dp else 2.dp,
                border = BorderStroke(if (first) 1.6.dp else 1.dp, if (first) StudioGold else palette.border),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onClick(hero) }
                    .testTag("meta_podium_${place + 1}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (first) 16.dp else 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "#${place + 1}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = if (first) StudioGold else palette.muted
                    )
                    Box(
                        modifier = Modifier
                            .size(if (first) 72.dp else 56.dp)
                            .clip(CircleShape)
                            .background(palette.frostSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hero.iconUrl.isNotBlank()) {
                            AsyncImage(
                                model = rememberMlbbSquareImageRequest(hero.iconUrl),
                                contentDescription = hero.heroName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Text(
                        text = hero.heroName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "%.1f%% win".format(hero.winRatePct),
                        style = MaterialTheme.typography.labelSmall,
                        color = RarityUncommon
                    )
                    Surface(
                        color = tierColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "${hero.tierGrade.label} Tier",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TierSectionHeader(grade: MetaTierGrade, count: Int) {
    val palette = CollectorTheme.colors
    val color = tierBadgeColor(grade)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(color = color, shape = RoundedCornerShape(10.dp)) {
            Text(
                text = "${grade.label} TIER",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
        Text(
            text = "$count heroes",
            style = MaterialTheme.typography.labelMedium,
            color = palette.muted
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(color.copy(alpha = 0.35f))
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CounterBuildSection(plan: CounterBuildPlan) {
    val palette = CollectorTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = StudioGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "COUNTER BUILD",
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink
            )
        }
        Surface(
            color = palette.frostSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, StudioGold.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = plan.headline,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    plan.traits.forEach { trait ->
                        Surface(
                            color = palette.cardSurface,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text(
                                text = trait,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accentPrimary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                plan.items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(palette.cardSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.iconUrl.isNotBlank()) {
                                AsyncImage(
                                    model = rememberMlbbSquareImageRequest(item.iconUrl),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = item.name.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StudioGold
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.ink
                            )
                            Text(
                                text = item.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.muted
                            )
                        }
                    }
                }
                Text(
                    text = plan.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
            }
        }
    }
}

@Composable
private fun OfficialNotesSection(notes: List<Pair<String, String>>) {
    val palette = CollectorTheme.colors
    Surface(
        color = palette.frostSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "FROM THE GAME'S OWN HERO NOTES",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.accentPrimary
            )
            notes.forEach { (label, text) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted
                )
            }
        }
    }
}
