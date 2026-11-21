package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.PlayerWalletState
import com.example.model.SkinCatalogItem
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.RarityChip
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.PearlCanvas
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.SkyAzure
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldBright
import com.example.ui.theme.StudioGoldText
import com.example.ui.viewmodel.MegaCollectorUiState
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun PlayArcadeScreen(
    uiState: MegaCollectorUiState,
    onClaimScratcherPrize: (Int) -> Unit,
    onAwardMinigameDiamonds: (Int, String) -> Unit,
    onPlasticCrinkle: () -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PearlCanvas)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Arcade Studio Header
        item {
            Surface(
                color = PureWhiteCard,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 3.dp,
                border = BorderStroke(1.5.dp, SkyAzure.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Arcade & Scratchers",
                                style = MaterialTheme.typography.headlineMedium,
                                color = DeepCobalt
                            )
                            Text(
                                text = "Win 💎 Diamonds from games & 🪙 Coins from daily scratchers!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoinsPill(coins = uiState.wallet.coins)
                        DiamondsPill(diamonds = uiState.wallet.diamonds)
                    }
                }
            }
        }

        // Daily Coin Scratchers Card
        item {
            DailyScratcherSection(
                remainingScratchers = uiState.dailyScratchersRemaining,
                onScratchComplete = onClaimScratcherPrize,
                onScratchSound = onPlasticCrinkle
            )
        }

        // Minigame 1: Hero Skin Guess (Earns Diamonds)
        item {
            HeroSkinGuessGame(
                catalog = uiState.catalog,
                onWinDiamonds = { amount ->
                    onAwardMinigameDiamonds(amount, "Skin Guess Win!")
                },
                onButtonClickSfx = onButtonClickSfx
            )
        }

        // Minigame 2: Prism Timing Tap (Earns Diamonds)
        item {
            PrismTimingTapGame(
                onWinDiamonds = { amount, label ->
                    onAwardMinigameDiamonds(amount, label)
                },
                onButtonClickSfx = onButtonClickSfx
            )
        }
    }
}

@Composable
private fun DailyScratcherSection(
    remainingScratchers: Int,
    onScratchComplete: (Int) -> Unit,
    onScratchSound: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var cardSeed by remember { mutableIntStateOf(0) }
    val prizeCoins = remember(cardSeed) {
        val roll = Random.nextDouble()
        when {
            roll < 0.08 -> 500
            roll < 0.22 -> 350
            roll < 0.48 -> 220
            roll < 0.78 -> 150
            else -> 100
        }
    }
    val scratchPoints = remember(cardSeed) { mutableStateListOf<Offset>() }
    var scratched by remember(cardSeed) { mutableStateOf(false) }

    val scratchProgress = (scratchPoints.size / 26f).coerceIn(0f, 1f)

    fun finishScratch() {
        if (!scratched && remainingScratchers > 0) {
            scratched = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onScratchComplete(prizeCoins)
        }
    }

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.5.dp, StudioGold.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Coin Scratchers",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                }
                Surface(
                    color = if (remainingScratchers > 0) Color(0xFFFFFBEB) else SkyFrost,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, if (remainingScratchers > 0) StudioGold else IceBorder)
                ) {
                    Text(
                        text = "$remainingScratchers/${PlayerWalletState.MAX_DAILY_SCRATCHERS} Left",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (remainingScratchers > 0) StudioGoldText else SlateMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rub the silver foil ticket to uncover 100 to 500 🪙 Coins!",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (remainingScratchers <= 0 && !scratched) {
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "All 3 Daily Scratchers Scratched!",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepCobalt
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Come back tomorrow for 3 fresh Coin Scratcher tickets.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                            )
                        )
                        .border(2.dp, StudioGold, RoundedCornerShape(18.dp))
                        .pointerInput(cardSeed, remainingScratchers) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (!scratched && remainingScratchers > 0) {
                                        onScratchSound()
                                        scratchPoints.add(offset)
                                    }
                                }
                            ) { change, _ ->
                                change.consume()
                                if (!scratched && remainingScratchers > 0) {
                                    val pos = change.position
                                    val last = scratchPoints.lastOrNull()
                                    if (last == null || (pos - last).getDistance() > 14f) {
                                        scratchPoints.add(pos)
                                        if (scratchPoints.size % 5 == 0) {
                                            onScratchSound()
                                        }
                                    }
                                    if (scratchPoints.size >= 24) {
                                        finishScratch()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Underlying Coin Prize
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = StudioGold,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "+$prizeCoins Coins!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = StudioGoldText
                        )
                        Text(
                            text = if (prizeCoins >= 350) "LUCKY JACKPOT!" else "Daily Coin Ticket",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSlate
                        )
                    }

                    // Silver Foil Scratch Overlay
                    if (!scratched) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8), Color(0xFFE2E8F0))
                                ),
                                cornerRadius = CornerRadius(18f, 18f)
                            )
                            scratchPoints.forEach { pt ->
                                drawCircle(
                                    color = Color(0xFFFFFBEB),
                                    radius = 34f,
                                    center = pt
                                )
                            }
                        }
                        if (scratchPoints.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = InkSlate.copy(alpha = 0.78f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Drag finger here to scratch!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkSlate
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!scratched) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { scratchProgress },
                            color = StudioGold,
                            trackColor = SkyFrost,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = ::finishScratch,
                            border = BorderStroke(1.dp, StudioGold),
                            modifier = Modifier.testTag("quick_scratch_button")
                        ) {
                            Text("Quick Scratch", color = StudioGoldText)
                        }
                    }
                } else if (remainingScratchers > 0) {
                    Button(
                        onClick = { cardSeed++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCobalt,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_scratcher_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grab Next Scratcher ($remainingScratchers Left)")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSkinGuessGame(
    catalog: List<SkinCatalogItem>,
    onWinDiamonds: (Int) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    var roundKey by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var answeredHero by remember(roundKey) { mutableStateOf<String?>(null) }

    val uniqueHeroNames = remember(catalog) {
        catalog.map { it.heroName }.distinct().filter { it.isNotBlank() && it != "Unknown Hero" }
    }

    val targetSkin = remember(catalog, roundKey) {
        val withArt = catalog.filter { it.heroName.isNotBlank() && it.heroName != "Unknown Hero" }
        withArt.randomOrNull()
    }

    val choices = remember(targetSkin, uniqueHeroNames, roundKey) {
        if (targetSkin == null) emptyList()
        else {
            val distractors = uniqueHeroNames
                .filter { !it.equals(targetSkin.heroName, ignoreCase = true) }
                .shuffled()
                .take(3)
            (distractors + targetSkin.heroName).shuffled()
        }
    }

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.5.dp, SkyAzure.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = DeepCobalt,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Who Wears This Skin?",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                }
                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, SkyAzure.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "Streak: $streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepCobalt,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Guess the hero for +5 💎 Diamonds (+7 💎 on 3+ win streak)!",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (targetSkin == null || choices.size < 2) {
                Text(
                    text = "Syncing hero catalog from MLBBDex...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SkyFrost)
                            .border(1.dp, IceBorder, RoundedCornerShape(14.dp))
                    ) {
                        if (targetSkin.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = targetSkin.imageUrl,
                                contentDescription = targetSkin.skinName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        RarityChip(rarity = targetSkin.rarity, compact = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = targetSkin.skinName,
                            style = MaterialTheme.typography.titleMedium,
                            color = InkSlate
                        )
                        Text(
                            text = "Role: ${targetSkin.role}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    choices.chunked(2).forEach { rowChoices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowChoices.forEach { hero ->
                                val isAnswered = answeredHero != null
                                val isCorrect = hero == targetSkin.heroName
                                val isChosen = hero == answeredHero
                                val bgColor = when {
                                    !isAnswered -> SkyFrost
                                    isCorrect -> RarityUncommon.copy(alpha = 0.22f)
                                    isChosen -> StudioCrimson.copy(alpha = 0.18f)
                                    else -> PearlCanvas
                                }
                                val borderColor = when {
                                    !isAnswered -> IceBorder
                                    isCorrect -> RarityUncommon
                                    isChosen -> StudioCrimson
                                    else -> IceBorder
                                }

                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, borderColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .testTag("quiz_choice_$hero")
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (answeredHero == null) {
                                                answeredHero = hero
                                                if (hero == targetSkin.heroName) {
                                                    streak++
                                                    val reward = if (streak >= 3) 7 else 5
                                                    onWinDiamonds(reward)
                                                } else {
                                                    onButtonClickSfx()
                                                    streak = 0
                                                }
                                            }
                                        },
                                        border = null,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = hero,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = InkSlate,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (answeredHero != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onButtonClickSfx()
                            roundKey++
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCobalt,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next Skin Round")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismTimingTapGame(
    onWinDiamonds: (Int, String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "prism_bar")
    val sweepPosition by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep_x"
    )

    var lastResultText by remember { mutableStateOf("Tap when the marker crosses the Gold Center!") }

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.5.dp, DeepCobalt.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = StudioGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prism Strike Timing",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkSlate
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bullseye (+8 💎) • Inner Blue (+4 💎) • Outer (+1 💎)",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Base track
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Inner Sky Blue Zone (0.32..0.68)
                drawRect(
                    color = SkyAzure.copy(alpha = 0.45f),
                    topLeft = Offset(size.width * 0.32f, 0f),
                    size = Size(size.width * 0.36f, size.height)
                )
                // Gold Bullseye Center (0.45..0.55)
                drawRect(
                    color = StudioGoldBright,
                    topLeft = Offset(size.width * 0.45f, 0f),
                    size = Size(size.width * 0.10f, size.height)
                )
                // Sweeping Marker
                val markerX = size.width * sweepPosition
                drawLine(
                    color = InkSlate,
                    start = Offset(markerX, 2f),
                    end = Offset(markerX, size.height - 2f),
                    strokeWidth = 10f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastResultText,
                style = MaterialTheme.typography.labelMedium,
                color = DeepCobalt
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val distFromCenter = abs(sweepPosition - 0.5f)
                    when {
                        distFromCenter <= 0.06f -> {
                            lastResultText = "GOLD BULLSEYE! +8 💎 Diamonds"
                            onWinDiamonds(8, "Gold Bullseye!")
                        }
                        distFromCenter <= 0.18f -> {
                            lastResultText = "Prism Hit! +4 💎 Diamonds"
                            onWinDiamonds(4, "Prism Hit!")
                        }
                        else -> {
                            onButtonClickSfx()
                            lastResultText = "Outer Edge Hit! +1 💎 Diamond"
                            onWinDiamonds(1, "Timing Tap!")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCobalt,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("prism_strike_button")
            ) {
                Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Strike for Diamonds!", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
