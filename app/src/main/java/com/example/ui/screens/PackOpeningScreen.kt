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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.model.SkinRarity
import com.example.ui.components.CardBackView
import com.example.ui.components.HolographicCardView
import com.example.ui.sensors.rememberCardTiltState
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import com.example.ui.viewmodel.PackOpeningSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PackOpeningScreen(
    session: PackOpeningSession,
    onPackRipped: () -> Unit,
    onRevealCard: (Int) -> Unit,
    onSelectCardIndex: (Int) -> Unit,
    onRevealAll: () -> Unit,
    onInspectCard: (Long) -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
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

    // Suspense charge-up state when flipping high rarity cards
    var suspenseProgress by remember { mutableFloatStateOf(0f) }
    var isChargingFlip by remember { mutableStateOf(false) }

    val currentCard = session.pulledCards.getOrNull(session.currentCardIndex)
    val isCurrentRevealed = session.currentCardIndex in session.revealedIndices

    // Trigger burst animation when pack rips or when high-rarity card reveals
    fun triggerRipSequence() {
        if (session.isRipped) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        playBurstOnce = true
        scope.launch {
            delay(220L)
            onPackRipped()
        }
    }

    fun triggerSuspenseFlip(index: Int) {
        if (isChargingFlip || index in session.revealedIndices) return
        val targetCard = session.pulledCards.getOrNull(index) ?: return
        scope.launch {
            isChargingFlip = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val totalMs = if (targetCard.isMisprint) 2600L else targetCard.rarity.suspenseDelayMs
            val steps = (totalMs / 50L).toInt().coerceAtLeast(3)
            for (s in 1..steps) {
                suspenseProgress = s.toFloat() / steps.toFloat()
                if (s % 6 == 0 && (targetCard.rarity.tierRank >= 4 || targetCard.isMisprint)) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                delay(50L)
            }
            suspenseProgress = 0f
            isChargingFlip = false
            onRevealCard(index)
            if (targetCard.isMisprint || targetCard.rarity.tierRank >= 4) {
                playBurstOnce = false
                delay(16L)
                playBurstOnce = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val auraColor = when {
        currentCard == null -> PrismCyan
        currentCard.isMisprint -> MisprintNeon
        isCurrentRevealed || isChargingFlip -> currentCard.rarity.primaryColor
        else -> session.pack.accentColor
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.24f + suspenseProgress * 0.35f),
                        VoidObsidian,
                        Color(0xFF04030A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.pack.title.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = ArenaGold
                    )
                    Text(
                        text = if (!session.isRipped) "Swipe tear seam or tap to rip open"
                        else "Card ${session.currentCardIndex + 1} of ${session.pulledCards.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_pack_opening_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close pack opening",
                        tint = TextPrimary
                    )
                }
            }

            // Center Stage: Phase 1 (Rip Pack) vs Phase 2 (Card Reveal)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (!session.isRipped) {
                    PackTearStage(
                        coverRes = session.pack.coverDrawableRes,
                        accentColor = session.pack.accentColor,
                        onRipComplete = ::triggerRipSequence
                    )
                } else if (currentCard != null) {
                    // Rare flare Lottie aura behind Epic+ / Misprints
                    if ((isCurrentRevealed && (currentCard.rarity.tierRank >= 4 || currentCard.isMisprint)) ||
                        (isChargingFlip && (currentCard.rarity.tierRank >= 4 || currentCard.isMisprint))
                    ) {
                        LottieAnimation(
                            composition = rareFlareComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(370.dp)
                        )
                    }

                    val flipAngle by animateFloatAsState(
                        targetValue = if (isCurrentRevealed) 0f else 180f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "card_flip"
                    )

                    val shakeOffsetX = if (isChargingFlip) {
                        sin(suspenseProgress * 45f) * (8f + suspenseProgress * 14f)
                    } else 0f
                    val density = LocalDensity.current.density

                    Box(
                        modifier = Modifier
                            .width(268.dp)
                            .offset { IntOffset(shakeOffsetX.roundToInt(), 0) }
                            .graphicsLayer {
                                rotationY = flipAngle
                                cameraDistance = 14f * density
                                val scaleBoost = if (isChargingFlip) 1f + suspenseProgress * 0.08f else 1f
                                scaleX = scaleBoost
                                scaleY = scaleBoost
                            }
                            .clickable {
                                if (!isCurrentRevealed) {
                                    triggerSuspenseFlip(session.currentCardIndex)
                                } else {
                                    onInspectCard(currentCard.instanceId)
                                }
                            }
                            .testTag("pack_reveal_card")
                    ) {
                        if (flipAngle > 90f) {
                            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                                CardBackView(
                                    rarityHint = currentCard.rarity,
                                    isMisprint = currentCard.isMisprint
                                )
                            }
                        } else {
                            HolographicCardView(
                                card = currentCard,
                                tiltState = tiltState,
                                interactiveTilt = true
                            )
                        }
                    }

                    // 1-of-1 Misprint Banner Overlay
                    this@Column.AnimatedVisibility(
                        visible = isCurrentRevealed && currentCard.isMisprint,
                        enter = fadeIn() + scaleIn(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Surface(
                            color = VoidObsidian.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.5.dp, MisprintNeon)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ArenaGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1 OF 1 MISPRINT DISCOVERED!",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MisprintNeon
                                )
                            }
                        }
                    }
                }

                // Foreground Lottie burst explosion
                if (playBurstOnce && burstProgress < 0.98f) {
                    LottieAnimation(
                        composition = burstComposition,
                        progress = { burstProgress },
                        modifier = Modifier.size(390.dp)
                    )
                }
            }

            // Card Index Pips (after rip)
            if (session.isRipped) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    session.pulledCards.forEachIndexed { idx, card ->
                        val revealed = idx in session.revealedIndices
                        val selected = idx == session.currentCardIndex
                        val dotColor = when {
                            revealed && card.isMisprint -> MisprintNeon
                            revealed -> card.rarity.primaryColor
                            else -> TextSecondary.copy(alpha = 0.35f)
                        }
                        Box(
                            modifier = Modifier
                                .size(if (selected) 16.dp else 11.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = if (selected) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { onSelectCardIndex(idx) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottom Action Controls
            if (!session.isRipped) {
                Button(
                    onClick = ::triggerRipSequence,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaGold,
                        contentColor = VoidObsidian
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("rip_pack_button")
                ) {
                    Text(
                        text = "RIP PACK OPEN",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isCurrentRevealed) {
                        Button(
                            onClick = { triggerSuspenseFlip(session.currentCardIndex) },
                            enabled = !isChargingFlip,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = auraColor,
                                contentColor = VoidObsidian
                            ),
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
                                text = if (isChargingFlip) "BUILDING RESONANCE..." else "TAP TO FLIP CARD",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else if (!session.allRevealed) {
                        val nextUnrevealed = session.pulledCards.indices.firstOrNull { it !in session.revealedIndices }
                        Button(
                            onClick = {
                                if (nextUnrevealed != null) {
                                    onSelectCardIndex(nextUnrevealed)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrismCyan,
                                contentColor = VoidObsidian
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("NEXT CARD", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ArenaGold,
                                contentColor = VoidObsidian
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("collect_all_button")
                        ) {
                            Text("COLLECT ALL TO VAULT", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!session.allRevealed) {
                            TextButton(onClick = onRevealAll) {
                                Text("Reveal All", color = TextSecondary)
                            }
                        } else if (currentCard != null) {
                            OutlinedButton(
                                onClick = { onInspectCard(currentCard.instanceId) },
                                border = BorderStroke(1.dp, PrismCyan)
                            ) {
                                Text("3D Gyro Inspect", color = PrismCyan)
                            }
                        }
                        if (isCurrentRevealed && currentCard != null) {
                            Text(
                                text = "Tilt phone to shine foil!",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PackTearStage(
    coverRes: Int,
    accentColor: Color,
    onRipComplete: () -> Unit
) {
    var tearProgress by remember { mutableFloatStateOf(0f) }
    val pulseTransition = rememberInfiniteTransition(label = "pack_float")
    val floatOffset by pulseTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
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
            .width(240.dp)
            .height(340.dp)
            .offset { IntOffset(0, floatOffset.roundToInt()) }
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                rotationZ = tearProgress * 4f
            }
            .clip(RoundedCornerShape(22.dp))
            .border(2.5.dp, accentColor, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (tearProgress > 0.48f) {
                            onRipComplete()
                        } else {
                            tearProgress = 0f
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    tearProgress = (tearProgress + kotlin.math.abs(dragAmount) / 360f).coerceIn(0f, 1f)
                    if (tearProgress >= 0.85f) {
                        onRipComplete()
                    }
                }
            }
            .clickable { onRipComplete() }
    ) {
        Image(
            painter = painterResource(id = coverRes),
            contentDescription = "Booster pack ready to rip",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Glowing Tear Seam across top third
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * 0.24f
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 4f
            )
            if (tearProgress > 0f) {
                drawLine(
                    color = ArenaGold,
                    start = Offset(0f, y),
                    end = Offset(size.width * tearProgress, y),
                    strokeWidth = 10f
                )
            }
        }

        Surface(
            color = VoidObsidian.copy(alpha = 0.85f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, ArenaGold),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwipeRight,
                    contentDescription = null,
                    tint = ArenaGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SWIPE TO TEAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaGold
                )
            }
        }
    }
}
