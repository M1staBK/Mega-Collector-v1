package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.HourlyChargeStatus
import com.example.data.local.PlayerWalletState
import com.example.model.QuizQuestion
import com.example.model.QuizQuestionBank
import com.example.model.QuizQuestionType
import com.example.model.SkinCatalogItem
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.CoinsPill
import com.example.ui.components.DiamondsPill
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.RarityChip
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.rememberMlbbImageRequest
import com.example.ui.components.rememberMlbbSquareImageRequest
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MoonlightInk
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.RarityUncommon
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldBright
import com.example.ui.theme.StudioGoldText
import com.example.ui.theme.rememberSafeHapticPerformer
import com.example.ui.viewmodel.MegaCollectorUiState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.SuccessResult
import com.example.model.MlbbCollectionTier
import com.example.ui.components.MlbbChestStyle
import com.example.ui.components.MlbbCollectionBadge
import com.example.ui.components.MlbbTreasureChest
import com.example.ui.components.buildMlbbSquareImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PlayArcadeScreen(
    uiState: MegaCollectorUiState,
    onClaimScratcherPrize: (Int) -> Unit,
    onAwardPrismDiamonds: (Int, String) -> Unit,
    onSpinWheelPrize: (label: String, coins: Int, diamonds: Int, freePacks: Int) -> Unit,
    onWheelTickSfx: (Float) -> Unit,
    onCompleteHourlyQuiz: (Int, Int) -> Unit,
    onClaimMysteryPick: (label: String, coins: Int, diamonds: Int, freePacks: Int) -> Unit = { _, _, _, _ -> },
    onCompleteMemoryMatch: (moves: Int, coins: Int, diamonds: Int) -> Unit = { _, _, _ -> },
    onPlasticCrinkle: () -> Unit,
    onOpenSettings: () -> Unit,
    onButtonClickSfx: () -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .let { if (includeStatusBarPadding) it.statusBarsPadding() else it }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("play_arcade_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arcade Top Hero Header (Star Emblem + Title + Decorative Pack Foil)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = palette.frostSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, palette.glassHighlight),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Arcade",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = palette.ink
                            )
                            Text(
                                text = "Challenge yourself",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.muted
                            )
                        }
                    }

                    // Subtle angled decorative pack silhouette fading gently into backdrop
                    Box(
                        modifier = Modifier
                            .size(width = 76.dp, height = 56.dp)
                            .graphicsLayer { rotationZ = 10f }
                            .clip(RoundedCornerShape(12.dp))
                            .alpha(if (palette.isDark) 0.42f else 0.72f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_pack_mythic),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Frosted Collector Arcade Info Card with Settings Cog + Currency Pills
            item {
                FrostedGlassCard(cornerRadius = 24.dp) {
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
                                Surface(
                                    color = palette.frostSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Casino,
                                            contentDescription = null,
                                            tint = palette.accentPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (palette.isDark) "Moonlight Arcade" else "Collector Arcade",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = palette.ink
                                )
                            }
                            SettingsGearButton(onClick = onOpenSettings)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wheel, Scratchers, Mystery Vault, Memory Blitz & Prism Strike refill +1/hr (stack 3). Quiz resets hourly!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CoinsPill(coins = uiState.wallet.coins)
                            DiamondsPill(diamonds = uiState.wallet.diamonds)
                        }
                    }
                }
            }

            // Minigame 1: Collector Prize Wheel (Hourly stack of 3)
            item {
                HourlyPrizeWheelSection(
                    status = uiState.wheelStatus,
                    onWheelTick = onWheelTickSfx,
                    onPrizeWon = onSpinWheelPrize
                )
            }

            // Minigame 2: Mystery Vault Pick (5 sealed chests)
            item {
                MysteryVaultPickSection(
                    status = uiState.mysteryPickStatus,
                    onPickPrize = onClaimMysteryPick,
                    onButtonClickSfx = onButtonClickSfx
                )
            }

            // Minigame 3: Memory Blitz (3x2 card pairs)
            item {
                MemoryBlitzSection(
                    status = uiState.memoryMatchStatus,
                    catalog = uiState.catalog,
                    onCompleteMatch = onCompleteMemoryMatch,
                    onButtonClickSfx = onButtonClickSfx
                )
            }

            // Minigame 4: Hourly Coin Scratchers (Stack up to 3, +1 per hour)
            item {
                HourlyScratcherSection(
                    status = uiState.scratcherStatus,
                    onScratchComplete = onClaimScratcherPrize,
                    onScratchSound = onPlasticCrinkle
                )
            }

            // Minigame 5: 5-Question Hourly Legends & Map Quiz
            item {
                HourlyFiveQuestionQuizCard(
                    catalog = uiState.catalog,
                    cooldownRemainingMs = uiState.quizCooldownRemainingMs,
                    onCompleteQuiz = onCompleteHourlyQuiz,
                    onButtonClickSfx = onButtonClickSfx
                )
            }

            // Minigame 6: Hourly Prism Timing Tap (Green center, Yellow inner, White outer; freezes on use)
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

internal data class WheelSlice(
    val shortLabel: String,
    val rewardTitle: String,
    val coins: Int,
    val diamonds: Int,
    val freePacks: Int,
    val sliceColor: Color,
    val weight: Int
)

internal val COLLECTOR_WHEEL_SLICES = listOf(
    WheelSlice("15 🪙", "+15 Coins", coins = 15, diamonds = 0, freePacks = 0, sliceColor = Color(0xFF334155), weight = 22),
    WheelSlice("1 💎", "+1 Diamond", coins = 0, diamonds = 1, freePacks = 0, sliceColor = Color(0xFF0EA5E9), weight = 18),
    WheelSlice("25 🪙", "+25 Coins", coins = 25, diamonds = 0, freePacks = 0, sliceColor = Color(0xFFD97706), weight = 18),
    WheelSlice("2 💎", "+2 Diamonds", coins = 0, diamonds = 2, freePacks = 0, sliceColor = Color(0xFF6366F1), weight = 14),
    WheelSlice("40 🪙", "+40 Coins", coins = 40, diamonds = 0, freePacks = 0, sliceColor = Color(0xFF10B981), weight = 15),
    WheelSlice("PACK!", "+1 Free Pack", coins = 0, diamonds = 0, freePacks = 1, sliceColor = Color(0xFFE54D67), weight = 5),
    WheelSlice("20 🪙", "+20 Coins", coins = 20, diamonds = 0, freePacks = 0, sliceColor = Color(0xFF475569), weight = 20),
    WheelSlice("3 💎", "+3 Diamonds", coins = 0, diamonds = 3, freePacks = 0, sliceColor = Color(0xFF8B5CF6), weight = 10),
    WheelSlice("60 🪙", "+60 Coins", coins = 60, diamonds = 0, freePacks = 0, sliceColor = Color(0xFF059669), weight = 10),
    WheelSlice("1 💎", "+1 Diamond", coins = 0, diamonds = 1, freePacks = 0, sliceColor = Color(0xFF0284C7), weight = 15),
    WheelSlice("90 🪙", "+90 Coins", coins = 90, diamonds = 0, freePacks = 0, sliceColor = Color(0xFFB45309), weight = 5),
    WheelSlice("5 💎", "+5 Diamonds", coins = 0, diamonds = 5, freePacks = 0, sliceColor = Color(0xFF7C3AED), weight = 4)
)

@Composable
private fun HourlyPrizeWheelSection(
    status: HourlyChargeStatus,
    onWheelTick: (Float) -> Unit,
    onPrizeWon: (String, Int, Int, Int) -> Unit
) {
    val palette = CollectorTheme.colors
    val scope = rememberCoroutineScope()
    val performHaptic = rememberSafeHapticPerformer()
    val textMeasurer = rememberTextMeasurer()

    val rotationAnim = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var lastPrizeText by remember { mutableStateOf("Spin to win Coins, Diamonds, or a Free Booster Pack!") }
    val hasCharges = status.charges > 0

    fun spinWheel() {
        if (isSpinning || !hasCharges) return
        val totalWeight = COLLECTOR_WHEEL_SLICES.sumOf { it.weight }
        var roll = Random.nextInt(totalWeight)
        var chosenIndex = 0
        for ((idx, slice) in COLLECTOR_WHEEL_SLICES.withIndex()) {
            roll -= slice.weight
            if (roll < 0) {
                chosenIndex = idx
                break
            }
        }
        val slice = COLLECTOR_WHEEL_SLICES[chosenIndex]
        val sliceSweep = 360f / COLLECTOR_WHEEL_SLICES.size.toFloat()
        // Pointer sits at -90 deg (top center). Slice i spans from (i * sweep + rot) to ((i+1) * sweep + rot).
        val targetCenterAngle = chosenIndex * sliceSweep + sliceSweep / 2f
        val jitter = (Random.nextFloat() - 0.5f) * (sliceSweep * 0.52f)
        val desiredFinalMod = (270f - targetCenterAngle + jitter + 360f) % 360f
        val currentMod = ((rotationAnim.value % 360f) + 360f) % 360f
        val deltaMod = (desiredFinalMod - currentMod + 360f) % 360f
        val fullTurns = 5f * 360f
        val targetRotation = rotationAnim.value + fullTurns + deltaMod

        isSpinning = true
        performHaptic(HapticFeedbackType.LongPress)
        scope.launch {
            var lastPegIdx = (rotationAnim.value / sliceSweep).toInt()
            rotationAnim.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = 3200,
                    easing = CubicBezierEasing(0.14f, 0.86f, 0.24f, 1.0f)
                )
            ) {
                val curPegIdx = (value / sliceSweep).toInt()
                if (curPegIdx != lastPegIdx) {
                    lastPegIdx = curPegIdx
                    val progress = ((value - (targetRotation - fullTurns)) / fullTurns).coerceIn(0f, 1f)
                    onWheelTick(1.18f - progress * 0.28f)
                }
            }
            isSpinning = false
            performHaptic(HapticFeedbackType.LongPress)
            lastPrizeText = "Landed on ${slice.rewardTitle}!"
            onPrizeWon(slice.rewardTitle, slice.coins, slice.diamonds, slice.freePacks)
        }
    }

    FrostedGlassCard(cornerRadius = 24.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collector Prize Wheel",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = InkSlate
                    )
                }
                HourlyChargeBadge(status = status, accent = palette.accentPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = lastPrizeText,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Wheel + Soft Golden Glow + Top Pointer
            Box(
                modifier = Modifier
                    .size(246.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                palette.gold.copy(alpha = if (palette.isDark) 0.24f else 0.32f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                PrizeWheelCanvas(
                    slices = COLLECTOR_WHEEL_SLICES,
                    rotationDegrees = rotationAnim.value,
                    textMeasurer = textMeasurer,
                    rimColor = StudioGold,
                    hubColor = palette.cardSurface,
                    modifier = Modifier
                        .size(214.dp)
                        .testTag("prize_wheel_canvas")
                )

                // Top stationary triangle pointer
                val pointerGold = StudioGoldBright
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(26.dp)
                ) {
                    val pointer = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.92f)
                        lineTo(size.width * 0.12f, 0f)
                        lineTo(size.width * 0.88f, 0f)
                        close()
                    }
                    drawPath(pointer, color = pointerGold)
                    drawPath(pointer, color = Color(0xFF0F172A), style = Stroke(width = 3f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = ::spinWheel,
                enabled = hasCharges && !isSpinning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.buttonPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = SkyFrost,
                    disabledContentColor = SlateMuted
                ),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("spin_wheel_button")
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isSpinning -> "Spinning Wheel..."
                        hasCharges -> "Spin Now (${status.charges}/3)"
                        else -> "Recharging (${formatCountdownMs(status.msUntilNextCharge)})"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PrizeWheelCanvas(
    slices: List<WheelSlice>,
    rotationDegrees: Float,
    textMeasurer: TextMeasurer,
    rimColor: Color,
    hubColor: Color,
    modifier: Modifier = Modifier
) {
    val pegGold = StudioGoldBright
    Canvas(modifier = modifier) {
        val count = slices.size
        val sweep = 360f / count.toFloat()
        val radius = size.minDimension / 2f - 6.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(degrees = rotationDegrees, pivot = center) {
            slices.forEachIndexed { idx, slice ->
                val startAngle = idx * sweep
                drawArc(
                    color = slice.sliceColor,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.28f),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 1.6.dp.toPx())
                )

                val midAngleDeg = startAngle + sweep / 2f
                val rad = Math.toRadians(midAngleDeg.toDouble())
                val textDist = radius * 0.67f
                val tx = center.x + (textDist * cos(rad)).toFloat()
                val ty = center.y + (textDist * sin(rad)).toFloat()

                val measured = textMeasurer.measure(
                    text = slice.shortLabel,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                rotate(degrees = midAngleDeg + 90f, pivot = Offset(tx, ty)) {
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            tx - measured.size.width / 2f,
                            ty - measured.size.height / 2f
                        )
                    )
                }
            }

            // Outer pegs
            for (i in 0 until count) {
                val pegRad = Math.toRadians((i * sweep).toDouble())
                val px = center.x + (radius * 0.96f * cos(pegRad)).toFloat()
                val py = center.y + (radius * 0.96f * sin(pegRad)).toFloat()
                drawCircle(color = pegGold, radius = 3.5.dp.toPx(), center = Offset(px, py))
            }
        }

        // Gold outer rim & center cap
        drawCircle(
            color = rimColor,
            radius = radius,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )
        drawCircle(color = hubColor, radius = 16.dp.toPx(), center = center)
        drawCircle(color = rimColor, radius = 16.dp.toPx(), center = center, style = Stroke(width = 2.5.dp.toPx()))
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
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
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
            roll < 0.08 -> 100
            roll < 0.22 -> 60
            roll < 0.48 -> 40
            roll < 0.78 -> 25
            else -> 15
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

    FrostedGlassCard(
        cornerRadius = 24.dp,
        borderColor = palette.accentPrimary.copy(alpha = 0.42f)
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
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coin Scratchers",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                }
                HourlyChargeBadge(status = status, accent = palette.accentPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recharges +1 ticket every hour (stacks up to 3). Scratch to win 15–100 🪙!",
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
                        .border(1.8.dp, StudioGold.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
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
                            text = if (prizeCoins >= 60) "MEGA JACKPOT!" else "Coin Ticket",
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
                            color = palette.accentPrimary,
                            trackColor = SkyFrost,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = ::finishScratch,
                            border = BorderStroke(1.dp, palette.accentPrimary),
                            modifier = Modifier.testTag("quick_scratch_button")
                        ) {
                            Text("Quick Scratch", color = palette.accentPrimary)
                        }
                    }
                } else if (hasCharges) {
                    Button(
                        onClick = { cardSeed++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.buttonPrimary,
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
private fun HourlyFiveQuestionQuizCard(
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

    val totalQuestions = QuizQuestionBank.QUIZ_QUESTION_COUNT
    val onCooldown = cooldownRemainingMs > 0L

    FrostedGlassCard(cornerRadius = 24.dp) {
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
                        text = "$totalQuestions-Question Arena Quiz",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkSlate
                    )
                }

                Surface(
                    color = SkyFrost,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        1.dp,
                        if (onCooldown) IceBorder else palette.accentPrimary.copy(alpha = 0.48f)
                    )
                ) {
                    Text(
                        text = if (inProgress) "Q ${currentIndex + 1}/$totalQuestions"
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
                text = "$totalQuestions questions mixing MLBBDex Skin IDs & Map Knowledge every 1 hour (+1 💎/hit + up to +2 💎 bonus)!",
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
                        border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Quiz Scored $score / $totalQuestions!",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkSlate
                            )
                            Text(
                                text = "Earned +$reward 💎 • Next $totalQuestions-question quiz in ${formatCountdownMs(cooldownRemainingMs)}",
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
                                text = "Next $totalQuestions-question set unlocks in ${formatCountdownMs(cooldownRemainingMs)}",
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
                        Text("Start $totalQuestions-Question Quiz", style = MaterialTheme.typography.labelLarge)
                    }
                }

                else -> {
                    val q = questions.getOrNull(currentIndex)
                    if (q != null) {
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / totalQuestions.toFloat() },
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
                                    color = palette.accentPrimary,
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
                                color = palette.accentPrimary
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
                                            finalScore >= totalQuestions -> 2
                                            finalScore >= 3 -> 1
                                            else -> 0
                                        }
                                        val totalDiamonds = finalScore + bonus
                                        finishedScore = finalScore to totalDiamonds
                                        inProgress = false
                                        onCompleteQuiz(finalScore, totalDiamonds)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.buttonPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("quiz_next_question_button")
                            ) {
                                Text(
                                    text = if (isLast) "Finish Quiz & Claim Diamonds" else "Next Question (${currentIndex + 2}/$totalQuestions)",
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

/**
 * Prism Timing Tap:
 * - Outer zone: White (#F8FAFC) -> +1 Diamond
 * - 2nd/Inner zone: Yellow (#FACC15) -> +2 Diamonds
 * - Center stripe: Green (#10B981) -> +5 Diamonds
 * - Stops the moving marker immediately when used, displaying where it landed until re-armed!
 */
@Composable
private fun HourlyPrismTimingTapGame(
    status: HourlyChargeStatus,
    onStrike: (Int, String) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    val hasCharges = status.charges > 0

    var isAiming by remember(hasCharges) { mutableStateOf(hasCharges) }
    val sweepAnim = remember { Animatable(0.05f) }
    var lastResultText by remember { mutableStateOf("Tap when the marker crosses the Green Center!") }

    LaunchedEffect(isAiming, hasCharges) {
        if (!hasCharges) {
            isAiming = false
            return@LaunchedEffect
        }
        while (isAiming && hasCharges) {
            sweepAnim.animateTo(
                targetValue = 0.95f,
                animationSpec = tween(durationMillis = 1150, easing = LinearEasing)
            )
            if (!isAiming) break
            sweepAnim.animateTo(
                targetValue = 0.05f,
                animationSpec = tween(durationMillis = 1150, easing = LinearEasing)
            )
        }
    }

    val centerGreen = Color(0xFF10B981)
    val innerYellow = Color(0xFFFACC15)
    val outerWhite = Color(0xFFF8FAFC)
    var currentPos by remember { mutableFloatStateOf(0.5f) }
    currentPos = sweepAnim.value

    FrostedGlassCard(cornerRadius = 24.dp) {
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
                        tint = palette.accentPrimary,
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
                text = "Recharges +1/hr (max 3). Green Center (+5 💎) • Yellow Inner (+2 💎) • White Edge (+1 💎)",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.2.dp, palette.border, RoundedCornerShape(12.dp))
            ) {
                // Outer Track: White
                drawRoundRect(
                    color = outerWhite,
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // 2nd Inner Zone: Yellow
                drawRect(
                    color = innerYellow,
                    topLeft = Offset(size.width * 0.30f, 0f),
                    size = Size(size.width * 0.40f, size.height)
                )
                // Center Stripe: Green
                drawRect(
                    color = centerGreen,
                    topLeft = Offset(size.width * 0.44f, 0f),
                    size = Size(size.width * 0.12f, size.height)
                )
                // High-contrast Needle Marker (dark outline + electric blue core)
                val markerX = size.width * currentPos
                drawLine(
                    color = Color(0xFF0F172A),
                    start = Offset(markerX, 1f),
                    end = Offset(markerX, size.height - 1f),
                    strokeWidth = 10f
                )
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(markerX, 3f),
                    end = Offset(markerX, size.height - 3f),
                    strokeWidth = 4f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (!hasCharges) "Recharging... +1 strike in ${formatCountdownMs(status.msUntilNextCharge)}"
                else lastResultText,
                style = MaterialTheme.typography.labelMedium,
                color = palette.accentPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (hasCharges && !isAiming) {
                Button(
                    onClick = {
                        onButtonClickSfx()
                        lastResultText = "Aiming! Tap Strike on the Green Center!"
                        isAiming = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("prism_arm_next_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Arm Next Strike (${status.charges}/3 Left)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (!hasCharges || !isAiming) return@Button
                        // Stop the animation immediately where the player struck
                        isAiming = false
                        val distFromCenter = abs(sweepAnim.value - 0.5f)
                        when {
                            distFromCenter <= 0.06f -> {
                                lastResultText = "GREEN BULLSEYE! +5 💎 Diamonds"
                                onStrike(5, "Green Bullseye!")
                            }
                            distFromCenter <= 0.20f -> {
                                lastResultText = "Yellow Zone Hit! +2 💎 Diamonds"
                                onStrike(2, "Yellow Inner Hit!")
                            }
                            else -> {
                                onButtonClickSfx()
                                lastResultText = "White Zone Hit! +1 💎 Diamond"
                                onStrike(1, "White Outer Hit!")
                            }
                        }
                    },
                    enabled = hasCharges && isAiming,
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
}

private data class MysteryChestPrize(
    val shortLabel: String,
    val fullLabel: String,
    val coins: Int,
    val diamonds: Int,
    val freePacks: Int
)

private val MYSTERY_CHEST_POOL = listOf(
    MysteryChestPrize("+80 🪙", "+80 Coins", coins = 80, diamonds = 0, freePacks = 0),
    MysteryChestPrize("+45 🪙", "+45 Coins", coins = 45, diamonds = 0, freePacks = 0),
    MysteryChestPrize("+30 🪙", "+30 Coins", coins = 30, diamonds = 0, freePacks = 0),
    MysteryChestPrize("+3 💎", "+3 Diamonds", coins = 0, diamonds = 3, freePacks = 0),
    MysteryChestPrize("+1 Pack", "+1 Free Pack", coins = 0, diamonds = 0, freePacks = 1)
)

@Composable
private fun MysteryVaultPickSection(
    status: HourlyChargeStatus,
    onPickPrize: (String, Int, Int, Int) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    val performHaptic = rememberSafeHapticPerformer()
    var roundNonce by remember { mutableIntStateOf(Random.nextInt(100_000)) }
    var roundSeed by remember { mutableIntStateOf(0) }
    val chests = remember(roundNonce, roundSeed) {
        MYSTERY_CHEST_POOL.shuffled(Random(roundNonce + roundSeed * 43 + 17))
    }
    val chestStyles = remember(roundNonce, roundSeed) {
        MlbbChestStyle.entries.shuffled(Random(roundNonce + roundSeed * 131 + 29)).take(5)
    }
    var pickedIndex by remember(roundNonce, roundSeed) { mutableIntStateOf(-1) }
    val hasCharges = status.charges > 0
    val revealed = pickedIndex >= 0

    FrostedGlassCard(cornerRadius = 24.dp) {
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
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mystery Vault Pick",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink
                    )
                }
                HourlyChargeBadge(status = status, accent = palette.accentPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (revealed) {
                    "Opened chest #${pickedIndex + 1} for ${chests[pickedIndex].fullLabel}! The other 4 stay sealed."
                } else {
                    "Pick 1 of 5 sealed MLBB chests — unpicked rewards stay hidden in suspense!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chests.forEachIndexed { idx, prize ->
                    val isPicked = pickedIndex == idx
                    val chestStyle = chestStyles[idx]
                    val labelText = when {
                        isPicked -> prize.shortLabel
                        revealed -> "???"
                        else -> "#${idx + 1}"
                    }
                    Surface(
                        color = when {
                            isPicked -> palette.buttonPrimary
                            revealed -> palette.frostSurface
                            hasCharges -> palette.cardSurface
                            else -> palette.abyss
                        },
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = if (hasCharges && !revealed) 2.dp else 0.dp,
                        border = BorderStroke(
                            width = if (isPicked) 2.dp else 1.2.dp,
                            color = when {
                                isPicked -> palette.accentPrimary
                                hasCharges && !revealed -> palette.accentPrimary.copy(alpha = 0.55f)
                                else -> palette.border
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(104.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = hasCharges && !revealed) {
                                performHaptic(HapticFeedbackType.LongPress)
                                pickedIndex = idx
                                onPickPrize(prize.fullLabel, prize.coins, prize.diamonds, prize.freePacks)
                            }
                            .testTag("mystery_chest_$idx")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            MlbbTreasureChest(
                                style = chestStyle,
                                opened = isPicked,
                                highlighted = isPicked,
                                dimmed = revealed && !isPicked,
                                accentColor = if (isPicked) Color(0xFFFFDE59) else palette.skyAccent,
                                contentDescription = if (isPicked) prize.fullLabel else "Sealed MLBB Chest ${idx + 1}",
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = when {
                                    isPicked -> Color.White
                                    revealed -> palette.muted
                                    hasCharges -> palette.ink
                                    else -> palette.muted
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (revealed && hasCharges) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onButtonClickSfx()
                        roundSeed++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.buttonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mystery_play_again_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Another Chest (${status.charges}/3 Left)")
                }
            } else if (!hasCharges && !revealed) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Recharging... +1 chest pick in ${formatCountdownMs(status.msUntilNextCharge)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.accentPrimary
                )
            }
        }
    }
}

private data class MemoryTileItem(
    val id: Int,
    val pairKey: String,
    val title: String,
    val imageUrl: String,
    val fallbackIconIdx: Int
)

@Composable
private fun MemoryBlitzSection(
    status: HourlyChargeStatus,
    catalog: List<SkinCatalogItem>,
    onCompleteMatch: (Int, Int, Int) -> Unit,
    onButtonClickSfx: () -> Unit
) {
    val palette = CollectorTheme.colors
    val context = LocalContext.current
    val performHaptic = rememberSafeHapticPerformer()
    var gameSeed by remember { mutableIntStateOf(0) }
    val hasCharges = status.charges > 0

    val deck = remember(gameSeed, catalog.size) {
        val imageSkins = catalog.filter { it.imageUrl.isNotBlank() }
        val chosenSkins = when {
            gameSeed == 0 && imageSkins.size >= 3 ->
                imageSkins.take(18).shuffled(Random(101)).take(3)
            imageSkins.size >= 3 ->
                imageSkins.shuffled(Random(gameSeed + 101)).take(3)
            catalog.size >= 3 ->
                catalog.shuffled(Random(gameSeed + 101)).take(3)
            else -> emptyList()
        }
        val pairs = if (chosenSkins.size == 3) {
            chosenSkins.mapIndexed { idx, skin ->
                Triple("skin_${skin.id}", skin.heroName, skin.imageUrl) to idx
            }
        } else {
            listOf(
                Triple("world", "World", "") to 0,
                Triple("mega", "Mega", "") to 1,
                Triple("expert", "Expert", "") to 2
            )
        }
        pairs.flatMapIndexed { pIdx, (info, iconIdx) ->
            listOf(
                MemoryTileItem(pIdx * 2, info.first, info.second, info.third, iconIdx),
                MemoryTileItem(pIdx * 2 + 1, info.first, info.second, info.third, iconIdx)
            )
        }.shuffled(Random(gameSeed + 404))
    }

    val decodedBitmaps = remember(gameSeed, deck) { mutableStateMapOf<String, ImageBitmap>() }
    LaunchedEffect(deck) {
        val urls = deck.map { it.imageUrl }.filter { it.isNotBlank() }.distinct()
        if (urls.isNotEmpty()) {
            val loader = Coil.imageLoader(context)
            withContext(Dispatchers.IO) {
                coroutineScope {
                    urls.map { url ->
                        async {
                            if (decodedBitmaps.containsKey(url)) return@async
                            val req = buildMlbbSquareImageRequest(context, url, crossfadeMs = 0)
                            val result = runCatching { loader.execute(req) }.getOrNull()
                            if (result is SuccessResult) {
                                val bmp = runCatching { result.drawable.toBitmap().asImageBitmap() }.getOrNull()
                                if (bmp != null) {
                                    decodedBitmaps[url] = bmp
                                }
                            }
                        }
                    }.awaitAll()
                }
            }
        }
    }

    var matchedKeys by remember(gameSeed) { mutableStateOf<Set<String>>(emptySet()) }
    var flippedIndices by remember(gameSeed) { mutableStateOf<List<Int>>(emptyList()) }
    var moves by remember(gameSeed) { mutableIntStateOf(0) }
    var boardCompleted by remember(gameSeed) { mutableStateOf(false) }

    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            val a = deck[flippedIndices[0]]
            val b = deck[flippedIndices[1]]
            if (a.pairKey == b.pairKey) {
                val updatedMatched = matchedKeys + a.pairKey
                matchedKeys = updatedMatched
                flippedIndices = emptyList()
                if (updatedMatched.size >= 3 && !boardCompleted) {
                    boardCompleted = true
                    performHaptic(HapticFeedbackType.LongPress)
                    val (coins, diamonds) = if (moves <= 4) 50 to 3 else 35 to 2
                    onCompleteMatch(moves, coins, diamonds)
                }
            } else {
                // Ensure both flipped hero cards are rendered cleanly before starting the peek window
                var waited = 0L
                while (waited < 450L && (
                    (a.imageUrl.isNotBlank() && !decodedBitmaps.containsKey(a.imageUrl)) ||
                        (b.imageUrl.isNotBlank() && !decodedBitmaps.containsKey(b.imageUrl))
                    )
                ) {
                    delay(45L)
                    waited += 45L
                }
                delay(700L)
                flippedIndices = emptyList()
            }
        }
    }

    FrostedGlassCard(cornerRadius = 24.dp) {
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
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Memory Blitz",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink
                    )
                }
                HourlyChargeBadge(status = status, accent = palette.accentPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (boardCompleted) {
                    "Board cleared in $moves moves! Reward claimed."
                } else {
                    "Match all 3 hero pairs in <=4 moves for +50 Coins & +3 Diamonds! (Moves: $moves)"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )

            Spacer(modifier = Modifier.height(12.dp))

            val fallbackTiers = listOf(
                MlbbCollectionTier.WORLD,
                MlbbCollectionTier.MEGA,
                MlbbCollectionTier.EXPERT
            )

            for (row in 0 until 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (row == 0) 8.dp else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 3) {
                        val idx = row * 3 + col
                        val tile = deck[idx]
                        val isMatched = tile.pairKey in matchedKeys
                        val isFaceUp = isMatched || idx in flippedIndices
                        val preloadedBmp = decodedBitmaps[tile.imageUrl]

                        Surface(
                            color = when {
                                isMatched -> palette.frostSurface
                                isFaceUp -> palette.frostSurface
                                hasCharges -> palette.buttonPrimary
                                else -> palette.abyss
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (isMatched || isFaceUp) 1.6.dp else 1.dp,
                                color = when {
                                    isMatched -> palette.accentPrimary
                                    isFaceUp -> palette.skyAccent
                                    else -> palette.glassHighlight
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.12f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    enabled = hasCharges && !boardCompleted && !isFaceUp && flippedIndices.size < 2
                                ) {
                                    onButtonClickSfx()
                                    val next = flippedIndices + idx
                                    flippedIndices = next
                                    if (next.size == 2) {
                                        moves++
                                    }
                                }
                                .testTag("memory_card_$idx")
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFaceUp) {
                                    when {
                                        preloadedBmp != null -> {
                                            Image(
                                                bitmap = preloadedBmp,
                                                contentDescription = tile.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        tile.imageUrl.isNotBlank() -> {
                                            SubcomposeAsyncImage(
                                                model = rememberMlbbSquareImageRequest(tile.imageUrl, crossfadeMs = 0),
                                                contentDescription = tile.title,
                                                contentScale = ContentScale.Crop,
                                                loading = {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(palette.frostSurface)
                                                            .padding(6.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        MlbbCollectionBadge(
                                                            tier = fallbackTiers[tile.fallbackIconIdx % fallbackTiers.size],
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = tile.title,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = palette.ink,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                },
                                                error = {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(palette.frostSurface)
                                                            .padding(6.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        MlbbCollectionBadge(
                                                            tier = fallbackTiers[tile.fallbackIconIdx % fallbackTiers.size],
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = tile.title,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = palette.ink,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        else -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(6.dp)
                                            ) {
                                                MlbbCollectionBadge(
                                                    tier = fallbackTiers[tile.fallbackIconIdx % fallbackTiers.size],
                                                    modifier = Modifier.size(34.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tile.title,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = palette.ink,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    MlbbCollectionBadge(
                                        tier = MlbbCollectionTier.EXPERT,
                                        dimmed = !hasCharges,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (boardCompleted && hasCharges) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onButtonClickSfx()
                        gameSeed++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.buttonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_play_again_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Next Board (${status.charges}/3 Left)")
                }
            } else if (!hasCharges && !boardCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Recharging... +1 board in ${formatCountdownMs(status.msUntilNextCharge)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.accentPrimary
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
