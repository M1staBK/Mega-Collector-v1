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
import androidx.compose.material.icons.filled.Autorenew
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.GenericShape
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
import com.example.ui.sensors.CardTiltState
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

internal data class AngledTearGeometry(
    val leftYRatio: Float,
    val rightYRatio: Float,
    val angleDeg: Float,
    val leftToRight: Boolean,
    val peelDirX: Float,
    val peelDirY: Float
)

internal fun computeAngledTearGeometry(
    startNormY: Float,
    dragDeltaX: Float,
    dragDeltaY: Float,
    baseSplitYRatio: Float = 0.24f
): AngledTearGeometry {
    val totalDist = hypot(dragDeltaX, dragDeltaY)
    val leftToRight = dragDeltaX >= 0f
    if (totalDist < 6f) {
        return AngledTearGeometry(
            leftYRatio = baseSplitYRatio,
            rightYRatio = baseSplitYRatio,
            angleDeg = 0f,
            leftToRight = true,
            peelDirX = 1f,
            peelDirY = -0.65f
        )
    }

    val safeDx = if (abs(dragDeltaX) < 18f) {
        if (dragDeltaX >= 0f) 18f else -18f
    } else {
        dragDeltaX
    }
    val rawSlope = (dragDeltaY / safeDx).coerceIn(-0.34f, 0.34f)
    val centerY = (baseSplitYRatio + (startNormY - baseSplitYRatio) * 0.35f).coerceIn(0.16f, 0.34f)
    val halfSpan = 0.5f * rawSlope
    val leftY = (centerY - halfSpan).coerceIn(0.10f, 0.44f)
    val rightY = (centerY + halfSpan).coerceIn(0.10f, 0.44f)
    val angleDeg = Math.toDegrees(atan2((rightY - leftY).toDouble(), 1.0)).toFloat().coerceIn(-38f, 38f)

    val normVy = dragDeltaY / totalDist
    val peelDirX = if (leftToRight) 1f else -1f
    val peelDirY = (-0.68f + normVy * 0.45f).coerceIn(-1.25f, 0.15f)

    return AngledTearGeometry(
        leftYRatio = leftY,
        rightYRatio = rightY,
        angleDeg = angleDeg,
        leftToRight = leftToRight,
        peelDirX = peelDirX,
        peelDirY = peelDirY
    )
}

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
                        auraColor.copy(alpha = (if (palette.isDark) 0.11f else 0.12f) + suspenseProgress * 0.10f),
                        palette.gloomHalo.copy(alpha = if (palette.isDark) 0.08f else 0.16f),
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
                            tiltState = tiltState,
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
                                    color = MoonlightNight,
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.2.dp, palette.accentPrimary),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = palette.accentPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "1 OF 1 MISPRINT DISCOVERED!",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.accentPrimary
                                        )
                                    }
                                }
                            }

                            this@Column.AnimatedVisibility(
                                visible = focusedCard.isHolographicEdition && !focusedCard.isMisprint,
                                enter = fadeIn() + scaleIn(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 6.dp)
                            ) {
                                Surface(
                                    color = palette.cardSurface,
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.2.dp, palette.accentPrimary),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = palette.accentPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "HOLO PRISM COLLECTOR+",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.accentPrimary
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
                        modifier = Modifier
                            .size(340.dp)
                            .alpha(0.36f)
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
                                containerColor = palette.buttonPrimary,
                                contentColor = Color.White
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
    tiltState: CardTiltState,
    isCommitting: Boolean,
    onCrinkle: () -> Unit,
    onRipComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val tearAnim = remember { Animatable(0f) }
    val yawAnim = remember { Animatable(0f) }
    val pitchAnim = remember { Animatable(0f) }
    var tornCommitted by remember { mutableStateOf(false) }

    // 2D Finger-Swipe Angle Tracking State
    var startNormY by remember { mutableFloatStateOf(0.24f) }
    var cumulativeDragX by remember { mutableFloatStateOf(0f) }
    var cumulativeDragY by remember { mutableFloatStateOf(0f) }
    var tearStripSize by remember { mutableStateOf(IntSize.Zero) }

    val tearGeometry = remember(startNormY, cumulativeDragX, cumulativeDragY) {
        computeAngledTearGeometry(
            startNormY = startNormY,
            dragDeltaX = cumulativeDragX,
            dragDeltaY = cumulativeDragY
        )
    }

    val pulseTransition = rememberInfiniteTransition(label = "pack_float")
    val floatOffset by pulseTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    val entryScale = remember { Animatable(0.92f) }
    LaunchedEffect(Unit) {
        entryScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    val totalYaw = yawAnim.value + tiltState.rollDegrees * 0.45f
    val normalizedYaw = ((totalYaw % 360f) + 360f) % 360f
    val showingBack = normalizedYaw in 90f..270f
    val tearProgress = tearAnim.value.coerceIn(0f, 1f)

    // Shapes splitting the upper torn foil cap from the lower pouch along the angled serrated seam
    val serratedTeeth = 14
    val topFoilCapShape = remember(tearGeometry.leftYRatio, tearGeometry.rightYRatio, tearProgress) {
        GenericShape { size, _ ->
            val yL = size.height * tearGeometry.leftYRatio
            val yR = size.height * tearGeometry.rightYRatio
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, yR)
            for (i in serratedTeeth downTo 0) {
                val t = i.toFloat() / serratedTeeth.toFloat()
                val x = size.width * t
                val baseY = yL + (yR - yL) * t
                val toothOffset = if (i in 1 until serratedTeeth && tearProgress > 0.02f) {
                    (if (i % 2 == 0) -1f else 1f) * 5.5f * (0.4f + tearProgress * 0.6f)
                } else 0f
                lineTo(x, baseY + toothOffset)
            }
            close()
        }
    }

    val lowerPouchShape = remember(tearGeometry.leftYRatio, tearGeometry.rightYRatio, tearProgress) {
        GenericShape { size, _ ->
            val yL = size.height * tearGeometry.leftYRatio
            val yR = size.height * tearGeometry.rightYRatio
            moveTo(0f, yL)
            for (i in 0..serratedTeeth) {
                val t = i.toFloat() / serratedTeeth.toFloat()
                val x = size.width * t
                val baseY = yL + (yR - yL) * t
                val toothOffset = if (i in 1 until serratedTeeth && tearProgress > 0.02f) {
                    (if (i % 2 == 0) -1f else 1f) * 5.5f * (0.4f + tearProgress * 0.6f)
                } else 0f
                lineTo(x, baseY + toothOffset)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    }

    fun commitTear() {
        if (tornCommitted || isCommitting) return
        tornCommitted = true
        scope.launch {
            tearAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            onRipComplete()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(352.dp)
                .offset { IntOffset(0, floatOffset.roundToInt()) }
                .graphicsLayer {
                    scaleX = entryScale.value
                    scaleY = entryScale.value
                    rotationY = totalYaw
                    rotationX = (pitchAnim.value - tiltState.pitchDegrees * 0.35f).coerceIn(-18f, 18f)
                    rotationZ = tearProgress * (tearGeometry.angleDeg * 0.10f + tearGeometry.peelDirX * 2.2f)
                    cameraDistance = 18f * density
                }
        ) {
            // Pack Frame (Rotates 3D on lower-body drag; shows Front Art or Back Foil Seal)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(satinTheme.baseColor)
                    .border(
                        width = 2.dp,
                        color = satinTheme.logoAccent.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .pointerInput(isCommitting) {
                        if (!isCommitting) {
                            detectDragGestures(
                                onDragStart = { onCrinkle() },
                                onDragEnd = {
                                    scope.launch {
                                        val mod = ((yawAnim.value % 360f) + 360f) % 360f
                                        val snapTarget = if (mod in 90f..270f) 180f else 0f
                                        launch {
                                            yawAnim.animateTo(
                                                targetValue = snapTarget,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                        }
                                        launch {
                                            pitchAnim.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        yawAnim.animateTo(if (((yawAnim.value % 360f) + 360f) % 360f in 90f..270f) 180f else 0f)
                                        pitchAnim.animateTo(0f)
                                    }
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    yawAnim.snapTo(yawAnim.value + dragAmount.x * 0.45f)
                                    pitchAnim.snapTo((pitchAnim.value - dragAmount.y * 0.14f).coerceIn(-16f, 16f))
                                }
                            }
                        }
                    }
                    .pointerInput(isCommitting) {
                        if (!isCommitting) {
                            detectTapGestures(
                                onDoubleTap = {
                                    onCrinkle()
                                    scope.launch {
                                        val target = if (showingBack) 0f else 180f
                                        yawAnim.animateTo(
                                            target,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                        )
                                    }
                                }
                            )
                        }
                    }
            ) {
                if (showingBack) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        PackBackSealFace(satinTheme = satinTheme, packCount = packCount)
                    }
                } else {
                    // 1. Revealed Interior Cards Edge & Chrome Pouch Cavity (visible behind the angled tear slit)
                    if (tearProgress > 0.02f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFCBD5E1),
                                            Color(0xFF475569),
                                            satinTheme.baseColor
                                        )
                                    )
                                )
                        ) {
                            // Top peeking rim of the card stack inside the pack
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, StudioGoldBright),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 62.dp)
                                    .height(74.dp)
                                    .graphicsLayer {
                                        translationY = -tearProgress * 10f
                                        rotationZ = tearGeometry.angleDeg * 0.18f
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(0xFFE2E8F0),
                                                    StudioGoldBright.copy(alpha = 0.55f),
                                                    Color(0xFF93C5FD)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }

                    // 2. Lower Pouch Artwork (clipped cleanly below the angled serrated tear path)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(if (tearProgress > 0.01f) lowerPouchShape else RoundedCornerShape(22.dp))
                    ) {
                        PackFrontArtFace(
                            coverRes = coverRes,
                            satinTheme = satinTheme,
                            tearProgress = tearProgress
                        )
                    }

                    // 3. Torn Top Foil Cap (clipped cleanly ABOVE the angled tear path; peels off along finger swipe vector!)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(if (tearProgress > 0.01f) topFoilCapShape else RoundedCornerShape(22.dp))
                            .graphicsLayer {
                                translationX = tearGeometry.peelDirX * tearProgress * 124f
                                translationY = tearGeometry.peelDirY * tearProgress * 92f
                                rotationZ = tearProgress * (tearGeometry.angleDeg * 0.42f + tearGeometry.peelDirX * -18f)
                                rotationX = tearProgress * 34f
                                alpha = (1f - tearProgress * 0.72f).coerceIn(0.18f, 1f)
                            }
                    ) {
                        PackFrontArtFace(
                            coverRes = coverRes,
                            satinTheme = satinTheme,
                            tearProgress = tearProgress
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(106.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            satinTheme.sheenHighlight.copy(alpha = 0.28f),
                                            satinTheme.baseColor.copy(alpha = 0.75f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // 4. Interactive Top 38% Angled Tear Touch Zone + Prompt Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(134.dp)
                            .align(Alignment.TopCenter)
                            .onSizeChanged { tearStripSize = it }
                            .pointerInput(isCommitting, tornCommitted) {
                                if (!isCommitting && !tornCommitted) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            onCrinkle()
                                            cumulativeDragX = 0f
                                            cumulativeDragY = 0f
                                            val h = tearStripSize.height.coerceAtLeast(1).toFloat()
                                            startNormY = ((startOffset.y / h) * 0.38f).coerceIn(0.12f, 0.36f)
                                        },
                                        onDragEnd = {
                                            if (tearAnim.value >= 0.56f) {
                                                commitTear()
                                            } else {
                                                scope.launch {
                                                    tearAnim.animateTo(
                                                        0f,
                                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                    )
                                                    cumulativeDragX = 0f
                                                    cumulativeDragY = 0f
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            scope.launch {
                                                tearAnim.animateTo(0f)
                                                cumulativeDragX = 0f
                                                cumulativeDragY = 0f
                                            }
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        cumulativeDragX += dragAmount.x
                                        cumulativeDragY += dragAmount.y
                                        onCrinkle()
                                        val stepDist = hypot(dragAmount.x, dragAmount.y)
                                        val next = (tearAnim.value + stepDist / 340f).coerceIn(0f, 1f)
                                        scope.launch { tearAnim.snapTo(next) }
                                        if (next >= 0.65f) {
                                            commitTear()
                                        }
                                    }
                                }
                            }
                    ) {
                        if (tearProgress < 0.78f) {
                            Surface(
                                color = MoonlightNight.copy(alpha = (0.86f * (1f - tearProgress)).coerceAtLeast(0f)),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, satinTheme.crestTint.copy(alpha = 0.78f)),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 18.dp)
                                    .graphicsLayer {
                                        translationX = tearGeometry.peelDirX * tearProgress * 78f
                                        translationY = tearGeometry.peelDirY * tearProgress * 56f
                                        rotationZ = tearGeometry.angleDeg * 0.35f
                                        alpha = (1f - tearProgress * 1.25f).coerceIn(0f, 1f)
                                    }
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
                                        text = if (packCount > 1) "SWIPE ANY ANGLE TO RIP ${packCount}x" else "SWIPE ANY ANGLE TO TEAR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = satinTheme.crestTint
                                    )
                                }
                            }
                        }
                    }

                    // 5. Angled Perforated Cut Line, Exposed Metallic Foil Opening & Directional Foil Shred Particles
                    val tearGoldColor = StudioGoldBright
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val yL = size.height * tearGeometry.leftYRatio
                        val yR = size.height * tearGeometry.rightYRatio

                        fun pointAt(t: Float): Offset = Offset(
                            x = size.width * t,
                            y = yL + (yR - yL) * t
                        )

                        // Perforated dashed guide along remaining uncut portion
                        val dashCount = 18
                        for (i in 0 until dashCount) {
                            val t0 = i.toFloat() / dashCount.toFloat()
                            val t1 = (i + 0.62f) / dashCount.toFloat()
                            val isCut = if (tearGeometry.leftToRight) t1 <= tearProgress else (1f - t0) <= tearProgress
                            if (!isCut) {
                                drawLine(
                                    color = satinTheme.sheenHighlight.copy(alpha = 0.70f),
                                    start = pointAt(t0),
                                    end = pointAt(t1),
                                    strokeWidth = 2.6f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Metallic silver-gold inner foil seam & directional foil sparks along the active slice
                        if (tearProgress > 0.01f) {
                            val cutStartT = if (tearGeometry.leftToRight) 0f else (1f - tearProgress)
                            val cutEndT = if (tearGeometry.leftToRight) tearProgress else 1f
                            val pStart = pointAt(cutStartT)
                            val pEnd = pointAt(cutEndT)

                            // Exposed metallic silver/gold seam
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE2E8F0),
                                        tearGoldColor,
                                        Color(0xFFF8FAFC)
                                    ),
                                    start = pStart,
                                    end = pEnd
                                ),
                                start = pStart,
                                end = pEnd,
                                strokeWidth = 8f + tearProgress * 7f,
                                cap = StrokeCap.Round
                            )

                            // Crisp bright blade glint at active tear tip
                            val tip = if (tearGeometry.leftToRight) pEnd else pStart
                            drawCircle(
                                color = Color.White.copy(alpha = 0.92f),
                                radius = 7f + tearProgress * 5f,
                                center = tip
                            )
                            drawCircle(
                                color = tearGoldColor.copy(alpha = 0.52f),
                                radius = 15f + tearProgress * 9f,
                                center = tip
                            )

                            // Directional metallic foil shred particles flying off the active cut point
                            val angleRad = Math.toRadians(tearGeometry.angleDeg.toDouble())
                            for (p in 0 until 7) {
                                val spread = (p - 3) * 0.32
                                val dist = (14f + p * 8f) * tearProgress
                                val px = tip.x + tearGeometry.peelDirX * (cos(angleRad + spread) * dist).toFloat()
                                val py = tip.y + tearGeometry.peelDirY * (abs(sin(angleRad + spread)) * dist + 10f).toFloat()
                                drawCircle(
                                    color = if (p % 2 == 0) tearGoldColor else satinTheme.sheenHighlight,
                                    radius = (4.2f - p * 0.35f).coerceAtLeast(1.6f),
                                    center = Offset(px, py),
                                    alpha = (1f - tearProgress * 0.45f).coerceIn(0.25f, 1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Turn Pack / Hint Row
        Surface(
            color = PureWhiteCard.copy(alpha = 0.90f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, IceBorder),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(enabled = !isCommitting) {
                    onCrinkle()
                    scope.launch {
                        val target = if (showingBack) 0f else 180f
                        yawAnim.animateTo(
                            target,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    }
                }
                .testTag("turn_pack_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "Turn pack",
                    tint = DeepCobalt,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showingBack) "Flip to Front  •  Drag to Rotate" else "Turn to Back Seal  •  Drag to Rotate",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = InkSlate
                )
            }
        }
    }
}

@Composable
private fun PackFrontArtFace(
    coverRes: Int,
    satinTheme: PackSatinTheme,
    tearProgress: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = coverRes),
            contentDescription = "Booster pack front artwork",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.32f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        0.0f to satinTheme.baseColor.copy(alpha = 0.92f),
                        0.32f to satinTheme.sheenHighlight.copy(alpha = 0.34f + tearProgress * 0.12f),
                        0.50f to Color.White.copy(alpha = 0.22f),
                        0.68f to satinTheme.logoAccent.copy(alpha = 0.36f),
                        1.0f to satinTheme.baseColor.copy(alpha = 0.95f)
                    )
                )
        )

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
    }
}

@Composable
private fun PackBackSealFace(
    satinTheme: PackSatinTheme,
    packCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        satinTheme.baseColor.copy(alpha = 0.96f),
                        satinTheme.logoAccent.copy(alpha = 0.30f),
                        satinTheme.baseColor.copy(alpha = 0.98f)
                    )
                )
            )
    ) {
        // Center Crimped Foil Spine
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(352.dp)
                .align(Alignment.Center)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            satinTheme.sheenHighlight.copy(alpha = 0.40f),
                            Color.White.copy(alpha = 0.28f),
                            satinTheme.sheenHighlight.copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SEALED BOOSTER SPECS • ${packCount * MegaPackConfig.CARDS_PER_PACK} CARDS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    fontSize = 9.sp
                ),
                color = satinTheme.crestTint
            )

            Surface(
                color = MoonlightNight.copy(alpha = 0.84f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, satinTheme.sheenHighlight.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CERTIFIED PULL ODDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = StudioGoldBright
                    )
                    val oddsRows = listOf(
                        "1-of-1 Misprint" to "0.005%",
                        "Legend Tier" to "0.01%",
                        "Collector Tier" to "1.0%",
                        "Luckybox Tier" to "3.0%",
                        "Epic Tier" to "8.0%",
                        "Special Tier" to "19.0%",
                        "Uncommon Tier" to "36.0%",
                        "Common Tier" to "32.99%"
                    )
                    oddsRows.forEach { (label, pct) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Text(
                                text = pct,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = satinTheme.crestTint
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MLBBDEX ARCHIVE EDITION • ${satinTheme.label.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color.White.copy(alpha = 0.72f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf(2, 4, 1, 3, 2, 5, 1, 2, 4, 1, 3, 2, 1, 4, 2).forEach { w ->
                        Box(
                            modifier = Modifier
                                .width(w.dp)
                                .height(16.dp)
                                .background(Color.White.copy(alpha = 0.65f))
                        )
                    }
                }
            }
        }
    }
}
