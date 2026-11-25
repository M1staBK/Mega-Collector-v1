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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
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
import coil.Coil
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
import com.example.ui.components.BoosterPack3DMesh
import com.example.ui.components.PackMeshTextureCache
import com.example.ui.components.BoosterPouchPhysics
import com.example.ui.components.BoosterPouchShape
import com.example.ui.components.CardBackView
import com.example.ui.components.HolographicCardView
import com.example.ui.components.RarityChip
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.buildMlbbCardImageRequest
import com.example.ui.components.buildMlbbSquareImageRequest
import com.example.ui.sensors.CardTiltState
import com.example.ui.sensors.rememberCardTiltState
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.StudioGoldBright
import com.example.ui.theme.rememberSafeHapticPerformer
import com.example.ui.viewmodel.PackOpeningSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
    onRevealCurrentPack: () -> Unit = {},
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
        if (!session.showingPackStack || session.isCommittingRip) return
        performHaptic(HapticFeedbackType.LongPress)
        onPackTear()
        playBurstOnce = true
        scope.launch {
            delay(45L)
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
        session.showingPackStack -> session.wrapperTheme.logoAccent
        else -> palette.accentPrimary
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
                    val headerText = MegaPackConfig.PACK_TITLE
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.accentPrimary
                    )
                    val subtitle = when {
                        !session.bundleCommitted && session.showingPackStack ->
                            "Swipe across the top to open"
                        session.showingPackStack ->
                            "Pack ${session.currentPackIndex + 1} of ${session.bundle.packCount}"
                        session.showSummaryGrid -> "Tap any card to 3D gyro inspect"
                        focusedCard != null ->
                            if (session.bundle.packCount > 1) {
                                "Pack ${session.currentPackIndex + 1}/${session.bundle.packCount} • Revealed ${session.revealedIndices.size}/${session.pulledCards.size}"
                            } else {
                                "Revealed ${session.revealedIndices.size} of ${session.pulledCards.size}"
                            }
                        else -> "Tap the card to reveal"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsGearButton(onClick = onOpenSettings)
                    Surface(
                        color = palette.cardSurface,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, palette.border),
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
                                tint = palette.ink
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
                    session.showingPackStack -> {
                        PackTearStage(
                            keySeed = session.currentPackIndex,
                            coverRes = MegaPackConfig.COVER_DRAWABLE_RES,
                            baseTheme = session.wrapperTheme,
                            packCount = session.bundle.packCount,
                            remainingPacksInStack = session.remainingPacksInStack,
                            currentPackNumber = session.currentPackIndex + 1,
                            tiltState = tiltState,
                            isCommitting = session.isCommittingRip,
                            onCrinkle = onPlasticCrinkle,
                            onRipComplete = ::triggerRipSequence
                        )
                    }

                    session.showSummaryGrid -> {
                        PulledSummaryGrid(
                            cards = if (session.summaryPackOnly) session.currentPackCards else session.pulledCards,
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
                            val currentUnrevealedInPack = session.currentPackCardIndices
                                .filter { it !in session.revealedIndices }
                                .mapNotNull { session.pulledCards.getOrNull(it) }
                            FaceDownCardDeckStage(
                                remainingCards = currentUnrevealedInPack.ifEmpty {
                                    session.pulledCards.filterIndexed { idx, _ -> idx !in session.revealedIndices }
                                },
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
                val trayIndices = session.currentPackCardIndices
                    .takeIf { !it.isEmpty() }
                    ?: session.pulledCards.indices
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
                            color = palette.muted
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
                        itemsIndexed(trayIndices.toList()) { localSlot, globalIdx ->
                            val card = session.pulledCards[globalIdx]
                            val isRevealed = globalIdx in session.revealedIndices
                            val isFocused = globalIdx == session.focusedRevealedIndex
                            Surface(
                                color = if (isRevealed) palette.cardSurface else palette.frostSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (isFocused) 1.8.dp else 1.dp,
                                    color = when {
                                        isFocused -> palette.accentPrimary
                                        isRevealed -> card.rarity.primaryColor
                                        else -> palette.border
                                    }
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = isRevealed) { onFocusRevealedCard(globalIdx) }
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
                                            color = palette.ink,
                                            maxLines = 1
                                        )
                                    } else {
                                        Text(
                                            text = "Card #${localSlot + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.muted
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
            if (session.showingPackStack) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = ::triggerRipSequence,
                        enabled = !session.isCommittingRip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.buttonPrimary,
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
                            text = when {
                                session.isCommittingRip -> "Opening..."
                                session.bundle.packCount > 1 ->
                                    "Rip Pack ${session.currentPackIndex + 1} of ${session.bundle.packCount}"
                                else -> "Rip Pack Open"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (session.bundle.packCount > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = onShowSummaryGrid,
                                enabled = !session.isCommittingRip,
                                modifier = Modifier.testTag("quick_open_all_packs_button")
                            ) {
                                Text(
                                    text = if (session.bundleCommitted) {
                                        "Open All Remaining Packs (${session.remainingPacksInStack})"
                                    } else {
                                        "Open All Packs (${session.bundle.totalCards} cards)"
                                    },
                                    color = palette.accentPrimary
                                )
                            }
                        }
                    }
                }
            } else if (session.showSummaryGrid && session.summaryPackOnly && session.hasNextPackInStack) {
                Button(
                    onClick = onAdvanceFromReveal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.buttonPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("next_pack_after_reveal_button")
                ) {
                    Text(
                        "Next Pack (${session.bundle.packCount - session.currentPackIndex - 1} Left)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else if (session.showSummaryGrid) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.buttonPrimary,
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
                    if (!session.currentPackAllRevealed) {
                        Button(
                            onClick = onAdvanceFromReveal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.buttonPrimary,
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
                    } else if (session.hasNextPackInStack) {
                        val packsLeft = session.bundle.packCount - session.currentPackIndex - 1
                        Button(
                            onClick = onAdvanceFromReveal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.buttonPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("next_pack_in_stack_button")
                        ) {
                            Text(
                                text = "Next Pack in Stack ($packsLeft Left)",
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
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (!session.currentPackAllRevealed) {
                                    TextButton(
                                        onClick = onRevealCurrentPack,
                                        modifier = Modifier.testTag("reveal_all_cards_button")
                                    ) {
                                        Text("Reveal All Cards", color = palette.accentPrimary)
                                    }
                                }
                                if (session.bundle.packCount > 1) {
                                    TextButton(
                                        onClick = onShowSummaryGrid,
                                        modifier = Modifier.testTag("open_all_packs_button")
                                    ) {
                                        Text("Open All Packs", color = palette.accentPrimary)
                                    }
                                }
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
                            color = palette.muted
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
                            containerColor = palette.buttonPrimary,
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
                        TextButton(
                            onClick = onRevealCurrentPack,
                            modifier = Modifier.testTag("reveal_all_cards_button")
                        ) {
                            Text("Reveal All Cards", color = palette.accentPrimary)
                        }
                        if (session.bundle.packCount > 1) {
                            TextButton(
                                onClick = onShowSummaryGrid,
                                modifier = Modifier.testTag("open_all_packs_button")
                            ) {
                                Text("Open All Packs", color = palette.accentPrimary)
                            }
                        } else {
                            Text(
                                text = "${session.remainingUnrevealedCount} face-down",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.muted
                            )
                        }
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
    val context = LocalContext.current
    val isMultiPackCurated = cards.size > 5
    var showAllCards by remember(cards) { mutableStateOf(false) }

    val sortedCards = remember(cards) {
        cards.sortedWith(
            compareByDescending<OwnedCard> { it.isMisprint }
                .thenByDescending { it.rarity.tierRank }
                .thenByDescending { it.instanceId }
        )
    }

    // Immediate priority MemoryCache hydration so "Reveal All Cards" shows crisp art with zero delay
    LaunchedEffect(sortedCards) {
        val loader = Coil.imageLoader(context.applicationContext)
        val semaphore = Semaphore(12)
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            coroutineScope {
                sortedCards
                    .distinctBy { it.imageUrl to (it.rarity == SkinRarity.LEGEND) }
                    .map { pulled ->
                        async {
                            if (pulled.imageUrl.isBlank()) return@async
                            semaphore.withPermit {
                                val req = if (pulled.rarity == SkinRarity.LEGEND) {
                                    buildMlbbCardImageRequest(context, pulled.imageUrl, crossfadeMs = 0, compact = true)
                                } else {
                                    buildMlbbSquareImageRequest(context, pulled.imageUrl, crossfadeMs = 0, compact = true)
                                }
                                runCatching { loader.execute(req) }
                            }
                        }
                    }
                    .awaitAll()
            }
        }
    }

    Column(modifier = modifier) {
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, palette.border),
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
                    color = palette.ink
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

/** Each pack of a bundle uses the next colour theme in the list, so a stack of 10 is not one colour. */
private fun themeForPack(base: PackSatinTheme, packIndex: Int): PackSatinTheme {
    val all = PackSatinTheme.ALL
    val start = all.indexOfFirst { it.id == base.id }.coerceAtLeast(0)
    return all[(start + packIndex.coerceAtLeast(0)) % all.size]
}

@Composable
private fun PackTearStage(
    keySeed: Int,
    coverRes: Int,
    baseTheme: PackSatinTheme,
    packCount: Int,
    remainingPacksInStack: Int,
    currentPackNumber: Int,
    tiltState: CardTiltState,
    isCommitting: Boolean,
    onCrinkle: () -> Unit,
    onRipComplete: () -> Unit
) {
    val palette = CollectorTheme.colors
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    // Every pack in a bundle gets its own colour theme (pack 1 = base theme, pack 2 = next one, ...)
    val satinTheme = themeForPack(baseTheme, currentPackNumber - 1)
    val appContext = LocalContext.current
    LaunchedEffect(currentPackNumber, baseTheme) {
        // Build the next pack's textures ahead of time so the next pack never pops in blank
        runCatching {
            PackMeshTextureCache.getOrBuild(
                appContext,
                coverRes,
                themeForPack(baseTheme, currentPackNumber),
                packCount
            )
        }
    }
    val tearAnim = remember(keySeed) { Animatable(0f) }
    val yawAnim = remember(keySeed) { Animatable(0f) }
    val pitchAnim = remember(keySeed) { Animatable(0f) }
    val squashAnim = remember(keySeed) { Animatable(0f) }
    val cardRiseAnim = remember(keySeed) { Animatable(0f) }
    var tornCommitted by remember(keySeed) { mutableStateOf(false) }
    var isDraggingRotate by remember(keySeed) { mutableStateOf(false) }

    // 2D Finger-Swipe Angle Tracking State
    var startNormY by remember(keySeed) { mutableFloatStateOf(0.24f) }
    var cumulativeDragX by remember(keySeed) { mutableFloatStateOf(0f) }
    var cumulativeDragY by remember(keySeed) { mutableFloatStateOf(0f) }
    var tearStripSize by remember(keySeed) { mutableStateOf(IntSize.Zero) }

    val tearGeometry = remember(startNormY, cumulativeDragX, cumulativeDragY) {
        computeAngledTearGeometry(
            startNormY = startNormY,
            dragDeltaX = cumulativeDragX,
            dragDeltaY = cumulativeDragY
        )
    }

    val pulseTransition = rememberInfiniteTransition(label = "pack_float")
    val floatOffset by pulseTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    val entryScale = remember(keySeed) { Animatable(0.92f) }
    LaunchedEffect(keySeed) {
        entryScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.56f,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val totalYaw = yawAnim.value + tiltState.rollDegrees * 0.42f
    val totalPitch = (pitchAnim.value - tiltState.pitchDegrees * 0.34f).coerceIn(-20f, 20f)
    val normalizedYaw = ((totalYaw % 360f) + 360f) % 360f
    val showingBack = normalizedYaw in 90f..270f
    val tearProgress = tearAnim.value.coerceIn(0f, 1f)

    // Pick-up physics: ONLY the top pack lifts off the stack when rotated/inspected!
    val yawOffFlat = ((yawAnim.value % 360f) + 360f) % 360f
    val isTopPackPickedUp = isDraggingRotate ||
        yawOffFlat in 4f..356f ||
        abs(pitchAnim.value) > 2.5f
    val pickUpProgress by animateFloatAsState(
        targetValue = if (isTopPackPickedUp) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "top_pack_pickup"
    )

    val lighting = remember(totalYaw, totalPitch, tiltState.rollDegrees, tiltState.pitchDegrees) {
        BoosterPouchPhysics.computeLightingState(
            totalYawDeg = totalYaw,
            totalPitchDeg = totalPitch,
            tiltRollDeg = tiltState.rollDegrees,
            tiltPitchDeg = tiltState.pitchDegrees
        )
    }

    fun commitTear() {
        if (tornCommitted || isCommitting) return
        tornCommitted = true
        scope.launch {
            launch {
                squashAnim.animateTo(1f, tween(90))
                squashAnim.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
            }
            tearAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (460f * (1f - tearAnim.value)).toInt().coerceIn(140, 460),
                    easing = FastOutSlowInEasing
                )
            )
            // Let the cards slide up out of the opened pouch before the deck takes over
            cardRiseAnim.animateTo(1f, tween(durationMillis = 480, easing = FastOutSlowInEasing))
            delay(120L)
            onRipComplete()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tall, sleek TCG booster pack proportions (196 x 368 dp => 1.88:1 ratio)
        val packWidthDp = 288.dp
        val packHeightDp = 368.dp
        val prismCameraDistancePx = remember(density) { maxOf(820f * density, 1600f) }

        if (packCount > 1) {
            Surface(
                color = palette.frostSurface,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.42f)),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("pack_stack_counter_pill")
            ) {
                Text(
                    text = "Pack $currentPackNumber of $packCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    ),
                    color = palette.accentPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        val visibleStackLayers = remainingPacksInStack.coerceIn(1, 10)
        val topPickUpLiftPx = with(LocalDensity.current) { (pickUpProgress * 18.dp.toPx()) }
        val topPickUpScale = 1f + pickUpProgress * 0.068f
        val dynamicScaleX = entryScale.value * topPickUpScale * (1f + squashAnim.value * 0.03f - tearProgress * 0.014f)
        val dynamicScaleY = entryScale.value * topPickUpScale * (1f - squashAnim.value * 0.025f + tearProgress * 0.016f)

        Box(
            modifier = Modifier
                .width(packWidthDp + 44.dp)
                .height(packHeightDp + 38.dp),
            contentAlignment = Alignment.Center
        ) {
            // Soft ground shadow under the whole stack: tight and dark while the pack rests,
            // wider, lighter and lower while the top pack is lifted off the stack
            Canvas(
                modifier = Modifier
                    .width(packWidthDp + 44.dp)
                    .height(packHeightDp + 38.dp)
                    .zIndex(-1f)
            ) {
                val cx = size.width / 2f
                val lift = pickUpProgress.coerceIn(0f, 1f)
                val baseY = size.height - 9.dp.toPx() + lift * 6.dp.toPx()
                val rx = size.width * (0.34f + lift * 0.07f)
                val ry = 12.dp.toPx() * (1f + lift * 0.7f)
                scale(scaleX = 1f, scaleY = ry / rx, pivot = Offset(cx, baseY)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color.Black.copy(alpha = 0.50f - lift * 0.24f),
                                0.55f to Color.Black.copy(alpha = 0.22f - lift * 0.10f),
                                1f to Color.Transparent
                            ),
                            center = Offset(cx, baseY),
                            radius = rx
                        ),
                        radius = rx,
                        center = Offset(cx, baseY)
                    )
                }
            }

            // Stationary underlying stacked packs (layers 1..9 underneath — NEVER rotate when top pack rotates)
            for (layer in (visibleStackLayers - 1) downTo 1) {
                val offX = (sin(layer * 1.45f) * 6.5f - layer * 1.3f).dp
                val offY = (layer * 3.8f + 4f).dp
                val rotZ = ((layer * 1.15f) * if (layer % 2 == 0) -1f else 1f).coerceIn(-5.5f, 5.5f)
                val shadeAlpha = (0.18f + layer * 0.045f).coerceIn(0.18f, 0.56f)
                val layerTheme = themeForPack(baseTheme, currentPackNumber - 1 + layer)

                Box(
                    modifier = Modifier
                        .width(packWidthDp)
                        .height(packHeightDp)
                        .offset(x = offX, y = offY)
                        .zIndex((visibleStackLayers - layer).toFloat())
                        .graphicsLayer {
                            rotationZ = rotZ
                            val layerScale = 1f - layer * 0.012f
                            scaleX = layerScale
                            scaleY = layerScale
                        }
                        .shadow(
                            elevation = 1.5.dp,
                            shape = BoosterPouchShape,
                            spotColor = Color.Black.copy(alpha = 0.30f)
                        )
                        .clip(BoosterPouchShape)
                        .testTag("stacked_pack_layer_$layer")
                ) {
                    Image(
                        painter = painterResource(id = coverRes),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Ambient occlusion darken + crisp gold rim edge on stacked pack
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(layerTheme.logoAccent.copy(alpha = 0.34f))
                            .background(Color.Black.copy(alpha = shadeAlpha))
                            .border(1.2.dp, layerTheme.crestTint.copy(alpha = 0.42f), BoosterPouchShape)
                    )
                }
            }

            // TOP ACTIVE PACK ONLY: lifts off the stack when picked up, rotates as a full 3D mesh, and tears open
            Box(
                modifier = Modifier
                    .width(packWidthDp)
                    .height(packHeightDp)
                    .zIndex(20f)
                    .offset {
                        IntOffset(
                            0,
                            ((floatOffset * (0.6f + pickUpProgress * 0.6f)) - topPickUpLiftPx).roundToInt()
                        )
                    }
                    .graphicsLayer {
                        scaleX = dynamicScaleX
                        scaleY = dynamicScaleY
                        rotationZ = tearProgress * (tearGeometry.angleDeg * 0.12f + tearGeometry.peelDirX * 2.4f)
                    }
                    .pointerInput(keySeed, isCommitting) {
                        if (!isCommitting) {
                            val tracker = VelocityTracker()
                            detectDragGestures(
                                onDragStart = {
                                    tracker.resetTracking()
                                    isDraggingRotate = true
                                    onCrinkle()
                                    scope.launch { squashAnim.animateTo(0.35f, tween(80)) }
                                },
                                onDragEnd = {
                                    isDraggingRotate = false
                                    val vel = tracker.calculateVelocity()
                                    val yawVelDeg = vel.x * 0.45f
                                    val targetYaw = BoosterPouchPhysics.nearestSettledFaceYaw(
                                        currentYawDeg = yawAnim.value,
                                        yawVelocityDegPerSec = yawVelDeg
                                    )
                                    scope.launch {
                                        launch {
                                            yawAnim.animateTo(
                                                targetValue = targetYaw,
                                                initialVelocity = yawVelDeg,
                                                animationSpec = spring(
                                                    dampingRatio = 0.56f,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                        }
                                        launch {
                                            pitchAnim.animateTo(
                                                targetValue = 0f,
                                                initialVelocity = -vel.y * 0.14f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.52f,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                        launch {
                                            squashAnim.animateTo(
                                                0f,
                                                spring(dampingRatio = 0.48f, stiffness = Spring.StiffnessMedium)
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    isDraggingRotate = false
                                    scope.launch {
                                        val targetYaw = BoosterPouchPhysics.nearestSettledFaceYaw(yawAnim.value, 0f)
                                        yawAnim.animateTo(targetYaw, spring(dampingRatio = 0.6f))
                                        pitchAnim.animateTo(0f, spring(dampingRatio = 0.6f))
                                        squashAnim.animateTo(0f)
                                    }
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                tracker.addPosition(change.uptimeMillis, change.position)
                                scope.launch {
                                    yawAnim.snapTo(yawAnim.value + dragAmount.x * 0.48f)
                                    pitchAnim.snapTo((pitchAnim.value - dragAmount.y * 0.16f).coerceIn(-18f, 18f))
                                }
                            }
                        }
                    }
                    .pointerInput(keySeed, isCommitting) {
                        if (!isCommitting) {
                            detectTapGestures(
                                onDoubleTap = {
                                    onCrinkle()
                                    scope.launch {
                                        val nextBase = BoosterPouchPhysics.nearestSettledFaceYaw(yawAnim.value, 0f)
                                        val target = nextBase + 180f
                                        yawAnim.animateTo(
                                            target,
                                            spring(dampingRatio = 0.56f, stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    .testTag("top_active_3d_pack")
            ) {
                // Cards sit INSIDE the pouch: drawn under the pack mesh, so the front of the pouch
                // hides their lower part and they slide up out of the torn opening.
                if (tearProgress > 0.04f) {
                    val rise = (((tearProgress - 0.18f) / 0.82f).coerceIn(0f, 1f) * 0.40f +
                        cardRiseAnim.value * 0.60f)
                    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                        for (i in 2 downTo 0) {
                            val cardLift = rise * (74f - i * 14f)
                            CardBackView(
                                rarityHint = SkinRarity.COMMON,
                                isMisprint = false,
                                showTeaserLabel = false,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .width(232.dp)
                                    .offset(x = ((i - 1) * 4f).dp, y = (112f - cardLift).dp)
                                    .graphicsLayer { rotationZ = (i - 1) * 2.0f * rise }
                            )
                        }
                    }
                }

                // True 3D Tessellated Mesh for the Active Top Booster Pack
                BoosterPack3DMesh(
                    coverRes = coverRes,
                    satinTheme = satinTheme,
                    packCount = packCount,
                    yawDeg = totalYaw,
                    pitchDeg = totalPitch,
                    cameraDistancePx = prismCameraDistancePx,
                    lighting = lighting,
                    tearProgress = tearProgress,
                    tearLeftYRatio = tearGeometry.leftYRatio,
                    tearRightYRatio = tearGeometry.rightYRatio,
                    tearPeelDirX = tearGeometry.peelDirX,
                    tearPeelDirY = tearGeometry.peelDirY,
                    tearAngleDeg = tearGeometry.angleDeg,
                    pickUpProgress = pickUpProgress,
                    modifier = Modifier.fillMaxSize()
                )

                // Interactive Top 36% Angled Tear Touch Zone + Prompt Pill (when front-facing)
                if (!showingBack) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .align(Alignment.TopCenter)
                            .onSizeChanged { tearStripSize = it }
                            .pointerInput(keySeed, isCommitting, tornCommitted) {
                                if (!isCommitting && !tornCommitted) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            onCrinkle()
                                            cumulativeDragX = 0f
                                            cumulativeDragY = 0f
                                            val h = tearStripSize.height.coerceAtLeast(1).toFloat()
                                            startNormY = ((startOffset.y / h) * 0.36f).coerceIn(0.12f, 0.34f)
                                            scope.launch { squashAnim.animateTo(0.28f, tween(70)) }
                                        },
                                        onDragEnd = {
                                            if (tearAnim.value >= 0.62f) {
                                                commitTear()
                                            } else {
                                                scope.launch {
                                                    launch {
                                                        tearAnim.animateTo(
                                                            0f,
                                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                        )
                                                    }
                                                    launch {
                                                        squashAnim.animateTo(0f, spring(dampingRatio = 0.5f))
                                                    }
                                                    cumulativeDragX = 0f
                                                    cumulativeDragY = 0f
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            scope.launch {
                                                tearAnim.animateTo(0f)
                                                squashAnim.animateTo(0f)
                                                cumulativeDragX = 0f
                                                cumulativeDragY = 0f
                                            }
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        cumulativeDragX += dragAmount.x
                                        cumulativeDragY += dragAmount.y
                                        onCrinkle()
                                        if (!tornCommitted) {
                                            val travelPx = tearStripSize.width.coerceAtLeast(1) * 0.85f
                                            val next = (
                                                tearAnim.value +
                                                    (abs(dragAmount.x) + abs(dragAmount.y) * 0.25f) / travelPx
                                                ).coerceIn(0f, 1f)
                                            scope.launch { tearAnim.snapTo(next) }
                                            if (next >= 0.97f) {
                                                commitTear()
                                            }
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
                                    .padding(top = 44.dp)
                                    .graphicsLayer {
                                        translationX = tearGeometry.peelDirX * tearProgress * 78f
                                        translationY = tearGeometry.peelDirY * tearProgress * 56f
                                        rotationZ = tearGeometry.angleDeg * 0.35f
                                        alpha = (1f - tearProgress * 1.25f).coerceIn(0f, 1f)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwipeRight,
                                        contentDescription = null,
                                        tint = satinTheme.crestTint,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "SWIPE TO TEAR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = satinTheme.crestTint
                                    )
                                }
                            }
                        }
                    }

                    // Angled Perforated Cut Line, Metallic Foil Cut & Directional Spark Particles
                    val tearGoldColor = StudioGoldBright
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val yL = size.height * tearGeometry.leftYRatio
                        val yR = size.height * tearGeometry.rightYRatio

                        fun pointAt(t: Float): Offset = Offset(
                            x = size.width * t,
                            y = yL + (yR - yL) * t
                        )

                        val dashCount = 16
                        for (i in 0 until dashCount) {
                            val t0 = i.toFloat() / dashCount.toFloat()
                            val t1 = (i + 0.60f) / dashCount.toFloat()
                            val isCut = if (tearGeometry.leftToRight) t1 <= tearProgress else (1f - t0) <= tearProgress
                            if (!isCut) {
                                drawLine(
                                    color = satinTheme.sheenHighlight.copy(alpha = 0.70f),
                                    start = pointAt(t0),
                                    end = pointAt(t1),
                                    strokeWidth = 2.4f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        if (tearProgress > 0.01f) {
                            val cutStartT = if (tearGeometry.leftToRight) 0f else (1f - tearProgress)
                            val cutEndT = if (tearGeometry.leftToRight) tearProgress else 1f
                            val pStart = pointAt(cutStartT)
                            val pEnd = pointAt(cutEndT)

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

        // Turn Top Pack / Hint Row
        Surface(
            color = palette.cardSurface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(enabled = !isCommitting) {
                    onCrinkle()
                    scope.launch {
                        val base = BoosterPouchPhysics.nearestSettledFaceYaw(yawAnim.value, 0f)
                        val target = base + 180f
                        yawAnim.animateTo(
                            target,
                            spring(dampingRatio = 0.56f, stiffness = Spring.StiffnessMediumLow)
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
                    contentDescription = "Turn top pack",
                    tint = palette.accentPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showingBack) "Show Front" else "Turn Pack",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink
                )
            }
        }
    }
}
