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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.HourlyChargeStatus
import com.example.data.local.PlayerWalletState
import com.example.model.QuizQuestion
import com.example.model.QuizQuestionBank
import com.example.model.QuizQuestionType
import com.example.model.SkinCatalogItem
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.RarityChip
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.rememberMlbbImageRequest
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MoonlightInk
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.SkyAzure
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldBright
import com.example.ui.theme.StudioGoldText
import com.example.ui.theme.rememberSafeHapticPerformer
import com.example.ui.viewmodel.MegaCollectorUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun PlayArcadeScreen(
    uiState: MegaCollectorUiState,
    onClaimScratcherPrize: (Int) -> Unit,
    onAwardPrismDiamonds: (Int, String) -> Unit,
    onCompleteHourlyQuiz: (Int, Int) -> Unit,
    onPlasticCrinkle: () -> Unit,
    onOpenSettings: () -> Unit,
    onButtonClickSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.16f else 0.34f),
                        palette.canvas
                    )
                )
            )
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arcade Studio Header
            item {
                Surface(
                    color = PureWhiteCard,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.2.dp, SkyAzure.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (palette.isDark) "Moonlight Arcade" else "Collector Arcade",
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.accentPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            SettingsGearButton(onClick = onOpenSettings)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scratchers & Prism Strike refill +1 every 1 hour (stack up to 3). 10-Question Quiz resets every 1 hour!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CoinsPill(coins = uiState.wallet.coins)
                            DiamondsPill(diamonds = uiState.wallet.diamonds)
                        }
                    }
                }
            }

            // Hourly Coin Scratchers (Stack up to 3, +1 per hour)
            item {
                HourlyScratcherSection(
                    status = uiState.scratcherStatus,
                    onScratchComplete = onClaimScratcherPrize,
                    onScratchSound = onPlasticCrinkle
                )
            }

            // Minigame 1: 10-Question Hourly Legends & Map Quiz
            item {
                HourlyTenQuestionQuizCard(
                    catalog = uiState.catalog,
                    cooldownRemainingMs = uiState.quizCooldownRemainingMs,
                    onCompleteQuiz = onCompleteHourlyQuiz,
                    onButtonClickSfx = onButtonClickSfx
                )
            }

            // Minigame 2: Hourly Prism Timing Tap (Stack up to 3, +1 per hour)
            item {
                HourlyPrismTimingTapGame(
                    status = uiState.prismStatus,
                    onStrike = onAwardPrismDiamonds,
                    onButtonClickSfx = onButtonClickSfx
                )
            }
        }
    }
}

@Composable
private fun HourlyChargeBadge(
    status: HourlyChargeStatus,
    accent: Color
) {
    val label = if (status.charges >= PlayerWalletState.MAX_HOURLY_CHARGES) {
        "${status.charges}/3 Ready"
    } else {
        "${status.charges}/3 • +1 in ${formatCountdownMs(status.msUntilNextCharge)}"
    }
    Surface(
        color = SkyFrost,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
    }
}

@Composable
private fun HourlyScratcherSection(
    status: HourlyChargeStatus,
    onScratchComplete: (Int) -> Unit,
    onScratchSound: () -> Unit
) {
    val palette = CollectorTheme.colors
    val performHaptic = rememberSafeHapticPerformer()
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
    val hasCharges = status.charges > 0

    fun finishScratch() {
        if (!scratched && hasCharges) {
            scratched = true
            performHaptic(HapticFeedbackType.LongPress)
            onScratchComplete(prizeCoins)
        }
    }

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.5.dp, StudioGold.copy(alpha = 0.55f))
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
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coin Scratchers",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                }
                HourlyChargeBadge(status = status, accent = palette.goldText)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recharges +1 ticket every hour (stacks up to 3). Scratch to win 100–500 🪙!",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!hasCharges && !scratched) {
                Surface(
                    color = palette.abyss,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scratcher Recharging",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.accentPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next +1 Scratcher unlocks in ${formatCountdownMs(status.msUntilNextCharge)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }
            } else {
                val scratchBacking = if (palette.isDark) {
                    listOf(Color(0xFF28200D), Color(0xFF3A2D11))
                } else {
                    listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                }
                val revealedHeadlineColor = if (palette.isDark) StudioGoldBright else StudioGoldText
                val revealedSubColor = if (palette.isDark) MoonlightInk else InkSlate
                val scratchUncoveredColor = if (palette.isDark) Color(0xFF28200D) else Color(0xFFFEF3C7)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(colors = scratchBacking))
                        .border(2.dp, StudioGold.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                        .pointerInput(cardSeed, status.charges) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (!scratched && status.charges > 0) {
                                        onScratchSound()
                                        scratchPoints.add(offset)
                                    }
                                }
                            ) { change, _ ->
                                change.consume()
                                if (!scratched && status.charges > 0) {
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
                            color = revealedHeadlineColor
                        )
                        Text(
                            text = if (prizeCoins >= 350) "MEGA JACKPOT!" else "Coin Ticket",
                            style = MaterialTheme.typography.labelMedium,
                            color = revealedSubColor
                        )
                    }

                    if (!scratched) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF334155))
                                ),
                                cornerRadius = CornerRadius(18f, 18f)
                            )
                            scratchPoints.forEach { pt ->
                                drawCircle(
                                    color = scratchUncoveredColor,
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
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Drag finger here to scratch!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
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
                } else if (hasCharges) {
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
                        Text("Grab Next Scratcher (${status.charges}/3 Left)")
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyTenQuestionQuizCard(
    catalog: List<SkinCatalogItem>,
    cooldownRemainingMs: Long,
    onCompleteQuiz: (Int, Int) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    var quizSessionId by remember { mutableIntStateOf(0) }
    var inProgress by remember { mutableStateOf(false) }
    var questions by remember(quizSessionId) {
        mutableStateOf<List<QuizQuestion>>(emptyList())
    }
    var currentIndex by remember(quizSessionId) { mutableIntStateOf(0) }
    var correctCount by remember(quizSessionId) { mutableIntStateOf(0) }
    var selectedOption by remember(quizSessionId, currentIndex) { mutableStateOf<String?>(null) }
    var finishedScore by remember(quizSessionId) { mutableStateOf<Pair<Int, Int>?>(null) }

    val onCooldown = cooldownRemainingMs > 0L

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.5.dp, SkyAzure.copy(alpha = 0.55f))
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
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "10-Question Arena Quiz",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                }

                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        1.dp,
                        if (onCooldown) IceBorder else palette.accentPrimary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = if (inProgress) "Q ${currentIndex + 1}/10"
                        else if (onCooldown) formatCountdownMs(cooldownRemainingMs)
                        else "Hourly Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (onCooldown && !inProgress) SlateMuted else palette.accentPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "10 questions mixing MLBBDex Skin IDs & Map Knowledge once every 1 hour (+2 💎/hit + up to +10 💎 bonus)!",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                finishedScore != null && !inProgress -> {
                    val (score, reward) = finishedScore!!
                    Surface(
                        color = palette.abyss,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RarityUncommon.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RarityUncommon,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Quiz Scored $score / 10!",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkSlate
                            )
                            Text(
                                text = "Earned +$reward 💎 • Next 10-question quiz in ${formatCountdownMs(cooldownRemainingMs)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.accentPrimary
                            )
                        }
                    }
                }

                !inProgress && onCooldown -> {
                    Surface(
                        color = palette.abyss,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, IceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hourly Quiz Cooldown",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.accentPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Next 10-question set unlocks in ${formatCountdownMs(cooldownRemainingMs)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMuted
                            )
                        }
                    }
                }

                !inProgress -> {
                    Button(
                        onClick = {
                            onButtonClickSfx()
                            questions = QuizQuestionBank.buildHourlyQuiz(catalog)
                            currentIndex = 0
                            correctCount = 0
                            selectedOption = null
                            finishedScore = null
                            inProgress = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCobalt,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_hourly_quiz_button")
                    ) {
                        Text("Start 10-Question Quiz", style = MaterialTheme.typography.labelLarge)
                    }
                }

                else -> {
                    val q = questions.getOrNull(currentIndex)
                    if (q != null) {
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / 10f },
                            color = palette.accentPrimary,
                            trackColor = SkyFrost,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SkyFrost,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = q.type.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (q.type == QuizQuestionType.MAP_KNOWLEDGE) StudioGoldText else palette.accentPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (q.skinRarity != null) {
                                RarityChip(rarity = q.skinRarity, compact = true)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Score: $correctCount/${currentIndex + if (selectedOption != null) 1 else 0}",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.goldText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (q.imageUrl.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.abyss)
                                        .border(1.dp, IceBorder, RoundedCornerShape(14.dp))
                                ) {
                                    SubcomposeAsyncImage(
                                        model = rememberMlbbImageRequest(q.imageUrl),
                                        contentDescription = q.prompt,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = q.prompt,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkSlate,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Text(
                                text = q.prompt,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            q.options.forEach { option ->
                                val answered = selectedOption != null
                                val isCorrect = option == q.correctAnswer
                                val isChosen = option == selectedOption
                                val bgColor = when {
                                    !answered -> SkyFrost
                                    isCorrect -> RarityUncommon.copy(alpha = 0.24f)
                                    isChosen -> StudioCrimson.copy(alpha = 0.24f)
                                    else -> palette.abyss
                                }
                                val borderColor = when {
                                    !answered -> IceBorder
                                    isCorrect -> RarityUncommon
                                    isChosen -> StudioCrimson
                                    else -> IceBorder
                                }

                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, borderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quiz_option_${option.take(16)}")
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (selectedOption == null) {
                                                selectedOption = option
                                                if (option == q.correctAnswer) {
                                                    correctCount++
                                                }
                                                onButtonClickSfx()
                                            }
                                        },
                                        border = null,
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = InkSlate
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedOption != null) {
                            if (q.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = q.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.accentPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val isLast = currentIndex >= questions.lastIndex
                            Button(
                                onClick = {
                                    onButtonClickSfx()
                                    if (!isLast) {
                                        currentIndex++
                                        selectedOption = null
                                    } else {
                                        val finalScore = correctCount
                                        val bonus = when {
                                            finalScore >= 8 -> 10
                                            finalScore >= 5 -> 4
                                            else -> 0
                                        }
                                        val totalDiamonds = (finalScore * 2) + bonus
                                        finishedScore = finalScore to totalDiamonds
                                        inProgress = false
                                        onCompleteQuiz(finalScore, totalDiamonds)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLast) StudioGold else DeepCobalt,
                                    contentColor = if (isLast) palette.goldButtonContent else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("quiz_next_question_button")
                            ) {
                                Text(
                                    text = if (isLast) "Finish Quiz & Claim Diamonds" else "Next Question (${currentIndex + 2}/10)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyPrismTimingTapGame(
    status: HourlyChargeStatus,
    onStrike: (Int, String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
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
    val hasCharges = status.charges > 0

    Surface(
        color = PureWhiteCard,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.5.dp, palette.gloomHalo.copy(alpha = 0.65f))
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
                HourlyChargeBadge(status = status, accent = palette.accentPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recharges +1 strike every hour (stacks up to 3). Bullseye (+8 💎) • Inner (+4 💎) • Outer (+1 💎)",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            val prismGold = StudioGoldBright
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRect(
                    color = SkyAzure.copy(alpha = 0.45f),
                    topLeft = Offset(size.width * 0.32f, 0f),
                    size = Size(size.width * 0.36f, size.height)
                )
                drawRect(
                    color = prismGold,
                    topLeft = Offset(size.width * 0.45f, 0f),
                    size = Size(size.width * 0.10f, size.height)
                )
                val markerX = size.width * sweepPosition
                drawLine(
                    color = Color.White,
                    start = Offset(markerX, 2f),
                    end = Offset(markerX, size.height - 2f),
                    strokeWidth = 10f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasCharges) lastResultText
                else "Recharging... +1 strike in ${formatCountdownMs(status.msUntilNextCharge)}",
                style = MaterialTheme.typography.labelMedium,
                color = palette.accentPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (!hasCharges) return@Button
                    val distFromCenter = abs(sweepPosition - 0.5f)
                    when {
                        distFromCenter <= 0.06f -> {
                            lastResultText = "GOLD BULLSEYE! +8 💎 Diamonds"
                            onStrike(8, "Gold Bullseye!")
                        }
                        distFromCenter <= 0.18f -> {
                            lastResultText = "Prism Hit! +4 💎 Diamonds"
                            onStrike(4, "Prism Hit!")
                        }
                        else -> {
                            onButtonClickSfx()
                            lastResultText = "Outer Edge Hit! +1 💎 Diamond"
                            onStrike(1, "Timing Tap!")
                        }
                    }
                },
                enabled = hasCharges,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCobalt,
                    contentColor = Color.White,
                    disabledContainerColor = SkyFrost,
                    disabledContentColor = SlateMuted
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("prism_strike_button")
            ) {
                Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasCharges) "Strike for Diamonds! (${status.charges}/3)"
                    else "Recharging (${formatCountdownMs(status.msUntilNextCharge)})",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun formatCountdownMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
