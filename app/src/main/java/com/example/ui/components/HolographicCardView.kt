package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.model.MisprintAnomaly
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.ui.sensors.CardTiltState
import com.example.ui.theme.ArenaGold
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidObsidian
import kotlin.math.roundToInt

@Composable
fun HolographicCardView(
    card: OwnedCard,
    modifier: Modifier = Modifier,
    tiltState: CardTiltState? = null,
    interactiveTilt: Boolean = false,
    compact: Boolean = false
) {
    val pitch = tiltState?.pitchDegrees ?: 0f
    val roll = tiltState?.rollDegrees ?: 0f
    val density = LocalDensity.current.density

    val infiniteTransition = rememberInfiniteTransition(label = "card_holo")
    val shimmerShift by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (card.isMisprint) 1800 else 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_shift"
    )
    val glitchJitter by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "misprint_glitch"
    )

    val borderBrush = if (card.isMisprint) {
        val a = card.misprintAnomaly?.accentA ?: MisprintNeon
        val b = card.misprintAnomaly?.accentB ?: MisprintMagenta
        Brush.linearGradient(
            colors = listOf(a, ArenaGold, b, PrismCyan, a),
            start = Offset(roll * 12f, pitch * 12f),
            end = Offset(420f + roll * 18f, 640f + pitch * 18f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                card.rarity.primaryColor,
                card.rarity.primaryColor.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.7f),
                card.rarity.secondaryColor
            ),
            start = Offset(roll * 10f, pitch * 10f),
            end = Offset(420f + roll * 10f, 600f + pitch * 10f)
        )
    }

    val dragModifier = if (interactiveTilt && tiltState != null) {
        Modifier.pointerInput(tiltState) {
            detectDragGestures(
                onDragEnd = { tiltState.releaseDrag() },
                onDragCancel = { tiltState.releaseDrag() }
            ) { change, dragAmount ->
                change.consume()
                tiltState.updateFromDrag(dragAmount.x, dragAmount.y)
            }
        }
    } else {
        Modifier
    }

    val cardShape = RoundedCornerShape(if (compact) 16.dp else 22.dp)

    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .then(dragModifier)
            .graphicsLayer {
                rotationX = pitch
                rotationY = roll
                cameraDistance = 14f * density
            }
            .clip(cardShape)
            .background(SurfaceCard)
            .border(
                width = if (card.isMisprint) 2.5.dp else if (card.rarity.tierRank >= 4) 2.dp else 1.2.dp,
                brush = borderBrush,
                shape = cardShape
            )
    ) {
        // Layer 1: Hero Skin Artwork with subtle parallax shift from gyroscope
        val imageColorFilter = when (card.misprintAnomaly) {
            MisprintAnomaly.INVERTED_CHROMA -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -0.8f, 0f, 0.4f, 0f, 180f,
                        0.2f, -0.8f, 0f, 0f, 190f,
                        0f, 0.3f, -0.8f, 0f, 220f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            MisprintAnomaly.GOLD_FOIL_BLEED -> ColorFilter.colorMatrix(
                ColorMatrix().apply { setToSaturation(1.35f) }
            )
            else -> null
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = (-roll * 0.65f).roundToInt(),
                        y = (pitch * 0.65f).roundToInt()
                    )
                }
        ) {
            if (card.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = card.imageUrl,
                    contentDescription = "${card.heroName} ${card.skinName} skin card",
                    contentScale = ContentScale.Crop,
                    colorFilter = imageColorFilter,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        SkinFallbackArt(card = card, modifier = Modifier.fillMaxSize())
                    },
                    error = {
                        SkinFallbackArt(card = card, modifier = Modifier.fillMaxSize())
                    }
                )
            } else {
                SkinFallbackArt(card = card, modifier = Modifier.fillMaxSize())
            }
        }

        // Layer 2: Atmospheric dark gradient vignette so card typography pops
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to VoidObsidian.copy(alpha = 0.55f),
                            0.35f to Color.Transparent,
                            0.65f to VoidObsidian.copy(alpha = 0.35f),
                            1.0f to VoidObsidian.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        // Layer 3: Gyroscope-reactive specular metallic foil glare & rainbow prism sheen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val glareCenterX = size.width * (0.5f + (roll / 36f)).coerceIn(-0.3f, 1.3f)
                    val glareCenterY = size.height * (0.5f - (pitch / 36f)).coerceIn(-0.3f, 1.3f)
                    val glareIntensity = when {
                        card.isMisprint -> 0.42f
                        card.rarity.tierRank >= 5 -> 0.34f
                        card.rarity.tierRank >= 3 -> 0.25f
                        else -> 0.14f
                    }

                    // Specular gyroscope spotlight reflection
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glareIntensity),
                                card.rarity.primaryColor.copy(alpha = glareIntensity * 0.65f),
                                Color.Transparent
                            ),
                            center = Offset(glareCenterX, glareCenterY),
                            radius = size.maxDimension * 0.72f
                        ),
                        blendMode = BlendMode.Screen
                    )

                    // Diagonal holographic rainbow sweep reacting to tilt + subtle idle motion
                    if (card.rarity.tierRank >= 3 || card.isMisprint) {
                        val sweepOffset = (shimmerShift + roll * 0.025f) * size.width
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrismCyan.copy(alpha = 0.16f),
                                    ArenaGold.copy(alpha = 0.20f),
                                    MisprintMagenta.copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                start = Offset(sweepOffset - size.width * 0.45f, 0f),
                                end = Offset(sweepOffset + size.width * 0.45f, size.height)
                            ),
                            blendMode = BlendMode.Plus
                        )
                    }
                }
        )

        // Layer 4: 1-of-1 Misprint Glitch Scanlines & Diagonal Watermark
        if (card.isMisprint) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stripeCount = 14
                val step = size.height / stripeCount
                for (i in 0 until stripeCount) {
                    if (i % 2 == 0) {
                        val y = i * step + glitchJitter * 1.8f
                        drawLine(
                            color = MisprintNeon.copy(alpha = 0.14f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }

        // Layer 5: Foreground Card Frame Metadata (Parallax forward)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = (roll * 0.4f).roundToInt(),
                        y = (-pitch * 0.4f).roundToInt()
                    )
                }
                .padding(if (compact) 8.dp else 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Rarity Pill + 1/1 Misprint Stamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RarityChip(rarity = card.rarity, compact = compact)
                if (card.isMisprint) {
                    Surface(
                        color = MisprintMagenta.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ArenaGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Misprint",
                                tint = ArenaGold,
                                modifier = Modifier.size(if (compact) 11.dp else 13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "1/1",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (compact) 9.sp else 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Info Plate
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (card.isMisprint && card.misprintAnomaly != null && !compact) {
                    Surface(
                        color = VoidObsidian.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, card.misprintAnomaly.accentA)
                    ) {
                        Text(
                            text = "MISPRINT: ${card.misprintAnomaly.title.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = card.misprintAnomaly.accentA,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = card.heroName.uppercase(),
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    color = card.rarity.primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.skinName,
                    style = if (compact) {
                        MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, lineHeight = 16.sp)
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    color = TextPrimary,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (compact) 3.dp else 6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.serialCode,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (compact) 9.sp else 11.sp
                        ),
                        color = if (card.isMisprint) ArenaGold else TextSecondary,
                        maxLines = 1
                    )
                    if (!compact) {
                        Text(
                            text = card.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardBackView(
    rarityHint: SkinRarity,
    isMisprint: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_back_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isMisprint || rarityHint.tierRank >= 5) 550 else 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val frameColor = if (isMisprint) MisprintNeon else rarityHint.primaryColor
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        frameColor.copy(alpha = 0.34f * pulse),
                        SurfaceElevated,
                        VoidObsidian
                    )
                )
            )
            .border(
                width = 2.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        frameColor.copy(alpha = pulse),
                        ArenaGold.copy(alpha = 0.7f * pulse),
                        frameColor.copy(alpha = pulse)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(frameColor.copy(alpha = 0.18f))
                    .border(2.dp, frameColor.copy(alpha = pulse), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMisprint) Icons.Default.Bolt else Icons.Default.AutoAwesome,
                    contentDescription = "Hidden card mystery aura",
                    tint = if (isMisprint) MisprintNeon else frameColor,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MEGA COLLECTOR",
                style = MaterialTheme.typography.titleLarge,
                color = ArenaGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            val teaserText = when {
                isMisprint -> "ANOMALY SIGNAL DETECTED"
                rarityHint == SkinRarity.LEGEND -> "MYTHIC RESONANCE SURGING"
                rarityHint == SkinRarity.COLLECTOR -> "GOLD VAULT SIGNATURE"
                rarityHint == SkinRarity.EPIC -> "HIGH ENERGY AURA"
                else -> "TAP TO REVEAL"
            }
            Text(
                text = teaserText,
                style = MaterialTheme.typography.labelMedium,
                color = frameColor.copy(alpha = pulse)
            )
        }
    }
}

@Composable
private fun SkinFallbackArt(card: OwnedCard, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    card.rarity.secondaryColor.copy(alpha = 0.85f),
                    SurfaceElevated,
                    VoidObsidian
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (card.rarity.tierRank >= 4) Icons.Default.Star else Icons.Default.Shield,
            contentDescription = null,
            tint = card.rarity.primaryColor.copy(alpha = 0.35f),
            modifier = Modifier.size(72.dp)
        )
    }
}
