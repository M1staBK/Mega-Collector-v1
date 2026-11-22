package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.model.MegaPackConfig
import com.example.model.OwnedCard
import com.example.model.PackSatinTheme
import com.example.model.SkinRarity
import com.example.ui.components.CardBackView
import com.example.ui.components.HolographicCardView
import com.example.ui.components.RarityChip
import com.example.ui.components.SettingsGearButton
import com.example.ui.sensors.rememberCardTiltState
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.DeepCobalt
import com.example.ui.theme.IceBorder
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyAzure
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldBright
import com.example.ui.theme.rememberSafeHapticPerformer
import com.example.ui.viewmodel.PackOpeningSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PackOpeningScreen(
    session: PackOpeningSession,
    onOpenSettings: () -> Unit,
    onPlasticCrinkle: () -> Unit,
    onPackTear: () -> Unit,
    onCardFlipSound: (SkinRarity, Boolean) -> Unit,
    onPackRipped: () -> Unit,
    onRevealCard: (Int) -> Unit,
    onAdvanceFromReveal: () -> Unit,
    onShowSummaryGrid: () -> Unit,
    onFocusRevealedCard: (Int) -> Unit,
    onInspectCard: (Long) -> Unit,
    onClose: () -> Unit
) {
    val palette = CollectorTheme.colors
    val performHaptic = rememberSafeHapticPerformer()
    val scope = rememberCoroutineScope()
    val tiltState = rememberCardTiltState()

    val burstComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_pack_burst))
    val rareFlareComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_rare_flare))

    var playBurstOnce by remember { mutableStateOf(false) }
    val burstProgress by animateLottieCompositionAsState(
        composition = burstComposition,
        isPlaying = playBurstOnce,
        iterations = 1,
        restartOnPlay = true
    )

    var suspenseProgress by remember { mutableFloatStateOf(0f) }
    var isChargingFlip by remember { mutableStateOf(false) }

    val focusedIndex = session.focusedRevealedIndex
    val focusedCard = focusedIndex?.let { session.pulledCards.getOrNull(it) }
    val nextUnrevealedIndex = session.nextUnrevealedIndex
    val topFaceDownCard = nextUnrevealedIndex?.let { session.pulledCards.getOrNull(it) }

    fun triggerRipSequence() {
        if (session.isRipped || session.isCommittingRip) return
        performHaptic(HapticFeedbackType.LongPress)
        onPackTear()
        playBurstOnce = true
        scope.launch {
            delay(200L)
            onPackRipped()
        }
    }

    fun triggerDeckFlip(index: Int) {
        if (isChargingFlip || index in session.revealedIndices) return
        val targetCard = session.pulledCards.getOrNull(index) ?: return
        scope.launch {
            isChargingFlip = true
            performHaptic(HapticFeedbackType.LongPress)

            val totalMs = if (targetCard.isMisprint) 820L
            else if (targetCard.rarity.tierRank >= SkinRarity.EPIC.tierRank) (targetCard.rarity.suspenseDelayMs * 0.45f).toLong()
            else 90L

            val steps = (totalMs / 45L).toInt().coerceAtLeast(2)
            for (s in 1..steps) {
                suspenseProgress = s.toFloat() / steps.toFloat()
                if (s % 4 == 0 && (targetCard.rarity.tierRank >= 4 || targetCard.isMisprint)) {
                    performHaptic(HapticFeedbackType.TextHandleMove)
                }
                delay(45L)
            }
            suspenseProgress = 0f
            isChargingFlip = false

            onCardFlipSound(targetCard.rarity, targetCard.isMisprint)
            onRevealCard(index)

            if (targetCard.isMisprint || targetCard.rarity.tierRank >= SkinRarity.EPIC.tierRank) {
                playBurstOnce = false
                delay(16L)
                playBurstOnce = true
                performHaptic(HapticFeedbackType.LongPress)
            }
        }
    }

    val auraColor = when {
        focusedCard != null && focusedCard.isMisprint -> palette.misprintLabel
        focusedCard != null -> focusedCard.rarity.primaryColor
        topFaceDownCard != null && isChargingFlip -> topFaceDownCard.rarity.primaryColor
        !session.isRipped -> session.wrapperTheme.logoAccent
        else -> SkyAzure
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.canvas)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = (if (palette.isDark) 0.16f else 0.15f) + suspenseProgress * 0.22f),
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.10f else 0.22f),
                        palette.abyss.copy(alpha = 0.85f),
                        palette.canvas
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Studio Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val headerText = if (session.bundle.packCount > 1) {
                        "${MegaPackConfig.PACK_TITLE} (${session.bundle.packCount}x Bundle)"
                    } else {
                        MegaPackConfig.PACK_TITLE
                    }
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.accentPrimary
                    )
                    val subtitle = when {
                        !session.isRipped -> "Close now to cancel safely (${session.bundle.totalCards} cards)"
                        session.showSummaryGrid -> "Tap any card to 3D gyro inspect"
                        focusedCard != null -> "Revealed ${session.revealedIndices.size} of ${session.pulledCards.size}"
                        else -> "${session.remainingUnrevealedCount} cards stacked • Tap, swipe, or pinch"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsGearButton(onClick = onOpenSettings)
                    Surface(
                        color = PureWhiteCard,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, IceBorder),
                        shadowElevation = 2.dp
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_pack_opening_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close pack opening",
                                tint = InkSlate
                            )
                        }
                    }
                }
            }

            // Main Stage
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !session.isRipped -> {
                        PackTearStage(
                            coverRes = MegaPackConfig.COVER_DRAWABLE_RES,
                            satinTheme = session.wrapperTheme,
                            packCount = session.bundle.packCount,
                            isCommitting = session.isCommittingRip,
                            onCrinkle = onPlasticCrinkle,
                            onRipComplete = ::triggerRipSequence
                        )
                    }

                    session.showSummaryGrid -> {
                        PulledSummaryGrid(
                            cards = session.pulledCards,
                            onInspectCard = onInspectCard,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    focusedCard != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (focusedCard.rarity.tierRank >= SkinRarity.EPIC.tierRank || focusedCard.isMisprint) {
                                LottieAnimation(
                                    composition = rareFlareComposition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier.size(370.dp)
                                )
                            }

                            val flipAngle by animateFloatAsState(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "reveal_spotlight"
                            )

                            Box(
                                modifier = Modifier
                                    .width(268.dp)
                                    .graphicsLayer { rotationY = flipAngle }
                                    .clickable { onInspectCard(focusedCard.instanceId) }
                                    .testTag("pack_reveal_card")
                            ) {
                                HolographicCardView(
                                    card = focusedCard,
                                    tiltState = tiltState,
                                    interactiveTilt = true
                                )
                            }

                            this@Column.AnimatedVisibility(
                                visible = focusedCard.isMisprint,
                                enter = fadeIn() + scaleIn(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 6.dp)
                            ) {
                                Surface(
                                    color = MoonlightNight.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.2.dp, palette.misprintLabel.copy(alpha = 0.72f)),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = StudioGoldBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "1 OF 1 MISPRINT DISCOVERED!",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.misprintLabel
                                        )
                                    }
                                }
                            }
                        }
                    }

                    topFaceDownCard != null -> {
                        val targetIdx = nextUnrevealedIndex
                        if (targetIdx != null) {
                            FaceDownCardDeckStage(
                                remainingCards = session.pulledCards.filterIndexed { idx, _ -> idx !in session.revealedIndices },
                                topCard = topFaceDownCard,
                                isChargingFlip = isChargingFlip,
                                suspenseProgress = suspenseProgress,
                                onFlipTopCard = { triggerDeckFlip(targetIdx) }
                            )
                        }
                    }
                }

                if (playBurstOnce && burstProgress < 0.98f) {
                    LottieAnimation(
                        composition = burstComposition,
                        progress = { burstProgress },
                        modifier = Modifier.size(390.dp)
                    )
                }
            }

            // Bottom Mini-Tray of Revealed Cards
            if (session.isRipped && !session.showSummaryGrid && session.revealedIndices.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PULLED SO FAR (${session.revealedIndices.size}/${session.pulledCards.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                        TextButton(
                            onClick = onShowSummaryGrid,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Summary", color = palette.accentPrimary)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(session.pulledCards) { idx, card ->
                            val isRevealed = idx in session.revealedIndices
                            val isFocused = idx == session.focusedRevealedIndex
                            Surface(
                                color = if (isRevealed) PureWhiteCard else SkyFrost,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (isFocused) 1.8.dp else 1.dp,
                                    color = when {
                                        isFocused -> palette.accentPrimary
                                        isRevealed -> card.rarity.primaryColor
                                        else -> IceBorder
                                    }
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = isRevealed) { onFocusRevealedCard(idx) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isRevealed) {
                                        RarityChip(rarity = card.rarity, compact = true)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = card.skinName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = InkSlate,
                                            maxLines = 1
                                        )
                                    } else {
                                        Text(
                                            text = "Card #${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom Action Bar
            if (!session.isRipped) {
                Button(
                    onClick = ::triggerRipSequence,
                    enabled = !session.isCommittingRip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCobalt,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("rip_pack_button")
                ) {
                    if (session.isCommittingRip) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = if (session.isCommittingRip) "Opening..."
                        else if (session.bundle.packCount > 1) "Rip ${session.bundle.packCount}x Bundle Open"
                        else "Rip Pack Open",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else if (session.showSummaryGrid) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCobalt,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("collect_all_button")
                ) {
                    Text("Collect All to Vault", style = MaterialTheme.typography.titleMedium)
                }
            } else if (focusedCard != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!session.allRevealed) {
                        Button(
                            onClick = onAdvanceFromReveal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepCobalt,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("next_stack_card_button")
                        ) {
                            Text(
                                text = "Next Stacked Card (${session.remainingUnrevealedCount} Left)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Button(
                            onClick = onShowSummaryGrid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioGold,
                                contentColor = palette.goldButtonContent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("view_all_pulled_cards_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (session.pulledCards.size > 5) "View Top 5 Rarest Pulls" else "View All ${session.pulledCards.size} Pulled Cards",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!session.allRevealed) {
                            TextButton(onClick = onShowSummaryGrid) {
                                Text("Reveal All Cards", color = palette.accentPrimary)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onInspectCard(focusedCard.instanceId) },
                                border = BorderStroke(1.dp, palette.accentPrimary)
                            ) {
                                Text("3D Gyro Inspect", color = palette.accentPrimary)
                            }
                        }
                        Text(
                            text = "Tilt phone to shine foil!",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                }
            } else if (nextUnrevealedIndex != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { triggerDeckFlip(nextUnrevealedIndex) },
                        enabled = !isChargingFlip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCobalt,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("flip_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isChargingFlip) "Building Resonance..." else "Flip Top Card (Tap / Swipe / Pinch)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onShowSummaryGrid) {
                            Text("Reveal All Cards", color = SlateMuted)
                        }
                        Text(
                            text = "${session.remainingUnrevealedCount} face-down",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaceDownCardDeckStage(
    remainingCards: List<OwnedCard>,
    topCard: OwnedCard,
    isChargingFlip: Boolean,
    suspenseProgress: Float,
    onFlipTopCard: () -> Unit
) {
    var dragOffsetX by remember(topCard.instanceId) { mutableFloatStateOf(0f) }
    var dragOffsetY by remember(topCard.instanceId) { mutableFloatStateOf(0f) }
    var pinchScale by remember(topCard.instanceId) { mutableFloatStateOf(1f) }
    var flipTriggered by remember(topCard.instanceId) { mutableStateOf(false) }

    val density = LocalDensity.current.density
    val shakeOffsetX = if (isChargingFlip) {
        sin(suspenseProgress * 45f) * (8f + suspenseProgress * 14f)
    } else 0f

    fun fireOnce() {
        if (!flipTriggered && !isChargingFlip) {
            flipTriggered = true
            onFlipTopCard()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val stackDepth = remainingCards.size.coerceAtMost(5)
        for (layer in (stackDepth - 1) downTo 1) {
            val layerCard = remainingCards[layer]
            val offsetDpY = (layer * 8).dp
            val offsetDpX = (if (layer % 2 == 0) -layer * 4 else layer * 4).dp
            val rotZ = if (layer % 2 == 0) -layer * 2.0f else layer * 2.0f
            val scale = 1f - layer * 0.03f

            Box(
                modifier = Modifier
                    .width(256.dp)
                    .offset(x = offsetDpX, y = offsetDpY)
                    .graphicsLayer {
                        rotationZ = rotZ
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                CardBackView(
                    rarityHint = layerCard.rarity,
                    isMisprint = layerCard.isMisprint,
                    showTeaserLabel = false
                )
            }
        }

        Box(
            modifier = Modifier
                .width(262.dp)
                .zIndex(5f)
                .offset {
                    IntOffset(
                        (dragOffsetX * 0.45f + shakeOffsetX).roundToInt(),
                        (dragOffsetY * 0.35f).roundToInt()
                    )
                }
                .graphicsLayer {
                    rotationY = (dragOffsetX * 0.14f).coerceIn(-35f, 35f)
                    rotationZ = (dragOffsetX * 0.03f).coerceIn(-8f, 8f)
                    val boost = if (isChargingFlip) 1f + suspenseProgress * 0.08f else pinchScale
                    scaleX = boost
                    scaleY = boost
                    cameraDistance = 14f * density
                }
                .pointerInput(topCard.instanceId) {
                    detectTapGestures(onTap = { fireOnce() })
                }
                .pointerInput(topCard.instanceId) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(dragOffsetX) > 75f || abs(dragOffsetY) > 75f) {
                                fireOnce()
                            } else {
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            }
                        },
                        onDragCancel = {
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                        if (abs(dragOffsetX) > 120f || abs(dragOffsetY) > 120f) {
                            fireOnce()
                        }
                    }
                }
                .pointerInput(topCard.instanceId) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            pinchScale = (pinchScale * zoom).coerceIn(0.85f, 1.25f)
                            if (pinchScale >= 1.12f || pinchScale <= 0.89f) {
                                fireOnce()
                            }
                        }
                    }
                }
                .testTag("pack_reveal_card")
        ) {
            CardBackView(
                rarityHint = topCard.rarity,
                isMisprint = topCard.isMisprint,
                showTeaserLabel = true
            )
        }
    }
}

/**
 * When multi-pack yields > 5 cards (e.g. 5-pack or 10-pack = 25..50 cards), defaults to showing
 * ONLY the Top 5 Rarest cards in an iconic 3-on-top / 2-centered-on-bottom showcase layout,
 * with a "Show All X Cards" button to view all remaining pulls.
 */
@Composable
private fun PulledSummaryGrid(
    cards: List<OwnedCard>,
    onInspectCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val isMultiPackCurated = cards.size > 5
    var showAllCards by remember(cards) { mutableStateOf(false) }

    val sortedCards = remember(cards) {
        cards.sortedWith(
            compareByDescending<OwnedCard> { it.isMisprint }
                .thenByDescending { it.rarity.tierRank }
                .thenByDescending { it.instanceId }
        )
    }

    Column(modifier = modifier) {
        Surface(
            color = PureWhiteCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, IceBorder),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMultiPackCurated && !showAllCards) {
                        "Top 5 Rarest Pulls (of ${cards.size})"
                    } else {
                        "Pack Pulls (${cards.size} Cards)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSlate
                )
                if (isMultiPackCurated && showAllCards) {
                    TextButton(
                        onClick = { showAllCards = false },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Top 5 Only", color = palette.accentPrimary)
                    }
                } else {
                    val bestCard = sortedCards.firstOrNull()
                    if (bestCard != null) {
                        RarityChip(rarity = bestCard.rarity, compact = true)
                    }
                }
            }
        }

        if (isMultiPackCurated && !showAllCards) {
            val topFive = remember(sortedCards) { sortedCards.take(5) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
                    .testTag("top_five_curated_stage"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: 3 Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    topFive.take(3).forEach { card ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onInspectCard(card.instanceId) }
                                .testTag("summary_card_${card.instanceId}")
                        ) {
                            HolographicCardView(card = card, compact = true)
                        }
                    }
                }

                // Bottom Row: 2 Cards Centered (matching 1/3 width each)
                val bottomTwo = topFive.drop(3)
                if (bottomTwo.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(0.5f))
                        bottomTwo.forEach { card ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onInspectCard(card.instanceId) }
                                    .testTag("summary_card_${card.instanceId}")
                            ) {
                                HolographicCardView(card = card, compact = true)
                            }
                        }
                        if (bottomTwo.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { showAllCards = true },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, palette.accentPrimary.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("show_all_pulled_cards_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show All ${cards.size} Cards",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.accentPrimary
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 146.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pulled_cards_summary_grid")
            ) {
                items(sortedCards, key = { it.instanceId }) { card ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInspectCard(card.instanceId) }
                            .testTag("summary_card_${card.instanceId}")
                    ) {
                        HolographicCardView(
                            card = card,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackTearStage(
    coverRes: Int,
    satinTheme: PackSatinTheme,
    packCount: Int,
    isCommitting: Boolean,
    onCrinkle: () -> Unit,
    onRipComplete: () -> Unit
) {
    var tearProgress by remember { mutableFloatStateOf(0f) }
    val pulseTransition = rememberInfiniteTransition(label = "pack_float")
    val floatOffset by pulseTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    val scaleAnim = remember { Animatable(0.92f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    Box(
        modifier = Modifier
            .width(246.dp)
            .height(348.dp)
            .offset { IntOffset(0, floatOffset.roundToInt()) }
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                rotationZ = tearProgress * 3.5f
            }
            .shadow(14.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(satinTheme.baseColor)
            .border(2.5.dp, satinTheme.logoAccent.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
            .pointerInput(isCommitting) {
                if (!isCommitting) {
                    detectHorizontalDragGestures(
                        onDragStart = { onCrinkle() },
                        onDragEnd = {
                            if (tearProgress > 0.45f) {
                                onRipComplete()
                            } else {
                                tearProgress = 0f
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        onCrinkle()
                        tearProgress = (tearProgress + abs(dragAmount) / 340f).coerceIn(0f, 1f)
                        if (tearProgress >= 0.85f) {
                            onRipComplete()
                        }
                    }
                }
            }
            .clickable(enabled = !isCommitting) {
                onCrinkle()
                onRipComplete()
            }
    ) {
        // Subtle base cover texture blended into the satin wrapper colorway
        Image(
            painter = painterResource(id = coverRes),
            contentDescription = "Booster pack ready to rip",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.32f)
        )

        // Smooth satin finish diagonal silk sheen overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        0.0f to satinTheme.baseColor.copy(alpha = 0.92f),
                        0.32f to satinTheme.sheenHighlight.copy(alpha = 0.34f),
                        0.50f to Color.White.copy(alpha = 0.24f),
                        0.68f to satinTheme.logoAccent.copy(alpha = 0.36f),
                        1.0f to satinTheme.baseColor.copy(alpha = 0.95f)
                    )
                )
        )

        // Satin-Finish Custom Color Pack Crest & Logo in Center
        Surface(
            color = satinTheme.baseColor.copy(alpha = 0.78f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.4.dp, satinTheme.sheenHighlight.copy(alpha = 0.65f)),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .padding(top = 34.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = satinTheme.crestTint,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "MEGA\nCOLLECTOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 19.sp,
                        letterSpacing = 1.sp
                    ),
                    color = satinTheme.sheenHighlight,
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = satinTheme.logoAccent.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, satinTheme.crestTint.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = satinTheme.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = satinTheme.crestTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Top Tear Seam
        val tearGold = StudioGoldBright
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * 0.22f
            drawLine(
                color = satinTheme.sheenHighlight.copy(alpha = 0.68f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.5f
            )
            if (tearProgress > 0f) {
                drawLine(
                    color = tearGold,
                    start = Offset(0f, y),
                    end = Offset(size.width * tearProgress, y),
                    strokeWidth = 9f
                )
            }
        }

        Surface(
            color = MoonlightNight.copy(alpha = 0.88f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, satinTheme.crestTint.copy(alpha = 0.78f)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwipeRight,
                    contentDescription = null,
                    tint = satinTheme.crestTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (packCount > 1) "SWIPE TO RIP ${packCount}x" else "SWIPE TO TEAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = satinTheme.crestTint
                )
            }
        }
    }
}
