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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.CardBackDeep
import com.example.ui.theme.CardBackEdge
import com.example.ui.theme.CardBackRoyal
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SkyAzure
import com.example.ui.theme.SkyFrost
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldBright
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
            colors = listOf(a, StudioGoldBright, b, SkyAzure, a),
            start = Offset(roll * 12f, pitch * 12f),
            end = Offset(420f + roll * 18f, 640f + pitch * 18f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                card.rarity.primaryColor,
                Color.White,
                card.rarity.primaryColor.copy(alpha = 0.75f),
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
            .shadow(
                elevation = if (compact) 6.dp else 14.dp,
                shape = cardShape,
                ambientColor = card.rarity.primaryColor.copy(alpha = 0.35f),
                spotColor = InkSlate.copy(alpha = 0.28f)
            )
            .clip(cardShape)
            .background(Color(0xFF101935))
            .border(
                width = if (card.isMisprint) 3.dp else if (card.rarity.tierRank >= 4) 2.5.dp else 1.8.dp,
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

        // Layer 2: Crisp studio vignette so typography over artwork always has high contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF0A1124).copy(alpha = 0.48f),
                            0.35f to Color.Transparent,
                            0.62f to Color(0xFF0A1124).copy(alpha = 0.32f),
                            1.0f to Color(0xFF091022).copy(alpha = 0.94f)
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
                        card.isMisprint -> 0.44f
                        card.rarity.tierRank >= 5 -> 0.36f
                        card.rarity.tierRank >= 3 -> 0.25f
                        else -> 0.14f
                    }

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

                    if (card.rarity.tierRank >= 3 || card.isMisprint) {
                        val sweepOffset = (shimmerShift + roll * 0.025f) * size.width
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    SkyAzure.copy(alpha = 0.18f),
                                    StudioGoldBright.copy(alpha = 0.22f),
                                    MisprintMagenta.copy(alpha = 0.18f),
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

        // Layer 4: 1-of-1 Misprint Glitch Scanlines
        if (card.isMisprint) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stripeCount = 14
                val step = size.height / stripeCount
                for (i in 0 until stripeCount) {
                    if (i % 2 == 0) {
                        val y = i * step + glitchJitter * 1.8f
                        drawLine(
                            color = MisprintNeon.copy(alpha = 0.16f),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RarityChip(rarity = card.rarity, compact = compact)
                if (card.isMisprint) {
                    Surface(
                        color = MisprintMagenta,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, StudioGoldBright)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Misprint",
                                tint = StudioGoldBright,
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

            Column(modifier = Modifier.fillMaxWidth()) {
                if (card.isMisprint && card.misprintAnomaly != null && !compact) {
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.9f),
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
                    color = StudioGoldBright,
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
                    color = Color.White,
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
                        color = if (card.isMisprint) StudioGoldBright else Color(0xFFCBD5E1),
                        maxLines = 1
                    )
                    if (!compact) {
                        Text(
                            text = card.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCBD5E1)
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
    modifier: Modifier = Modifier,
    showTeaserLabel: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_back_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isMisprint || rarityHint.tierRank >= 5) 600 else 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val frameColor = when {
        isMisprint -> MisprintNeon
        rarityHint.tierRank >= SkinRarity.EPIC.tierRank -> rarityHint.primaryColor
        else -> StudioGoldBright
    }
    val shape = RoundedCornerShape(22.dp)

    // Completely opaque base (CardBackDeep) so underlying UI never bleeds through
    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = InkSlate.copy(alpha = 0.3f),
                spotColor = CardBackDeep.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(CardBackDeep)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CardBackRoyal,
                        CardBackEdge,
                        CardBackDeep
                    )
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        PureWhiteCard,
                        StudioGoldBright,
                        frameColor.copy(alpha = pulse),
                        StudioGoldBright
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ornate TCG geometric card-back linework
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            drawRoundRect(
                color = StudioGoldBright.copy(alpha = 0.35f),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawRoundRect(
                color = frameColor.copy(alpha = 0.25f),
                topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            // Corner diamonds
            val pad = 20.dp.toPx()
            val corners = listOf(
                Offset(pad, pad),
                Offset(size.width - pad, pad),
                Offset(pad, size.height - pad),
                Offset(size.width - pad, size.height - pad)
            )
            corners.forEach { c ->
                drawCircle(
                    color = StudioGoldBright.copy(alpha = 0.45f),
                    radius = 4.dp.toPx(),
                    center = c
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(CardBackDeep)
                    .border(2.5.dp, frameColor.copy(alpha = pulse), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMisprint) Icons.Default.Bolt else Icons.Default.AutoAwesome,
                    contentDescription = "Hidden card back crest",
                    tint = if (isMisprint) MisprintNeon else StudioGoldBright,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MEGA COLLECTOR",
                style = MaterialTheme.typography.titleLarge,
                color = StudioGoldBright
            )
            if (showTeaserLabel) {
                Spacer(modifier = Modifier.height(6.dp))
                val teaserText = when {
                    isMisprint -> "1/1 ANOMALY PULSE"
                    rarityHint.tierRank >= SkinRarity.COLLECTOR.tierRank -> "MYTHIC AURA DETECTED"
                    rarityHint.tierRank >= SkinRarity.EPIC.tierRank -> "HIGH FOIL RESONANCE"
                    else -> "TAP, SWIPE, OR PINCH"
                }
                Surface(
                    color = CardBackDeep.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, frameColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = teaserText,
                        style = MaterialTheme.typography.labelSmall,
                        color = frameColor.copy(alpha = pulse),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkinFallbackArt(card: OwnedCard, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    card.rarity.secondaryColor,
                    CardBackRoyal,
                    CardBackDeep
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (card.rarity.tierRank >= 4) Icons.Default.Star else Icons.Default.Shield,
            contentDescription = null,
            tint = SkyFrost.copy(alpha = 0.35f),
            modifier = Modifier.size(72.dp)
        )
    }
}
