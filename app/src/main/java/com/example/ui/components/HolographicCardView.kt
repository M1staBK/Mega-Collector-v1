package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.MisprintAnomaly
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.model.SkinVoiceLines
import com.example.ui.sensors.CardTiltState
import com.example.ui.theme.BrushQuoteFontFamily
import com.example.ui.theme.CardBackDeep
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.MoonlightSilverBlue
import com.example.ui.theme.SignatureHandwritingFontFamily
import com.example.ui.theme.SkyAzure
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
    val isFullArtLegend = card.rarity == SkinRarity.LEGEND

    val infiniteTransition = rememberInfiniteTransition(label = "card_holo")
    val shimmerShift by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (card.isMisprint) 2000 else 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_shift"
    )
    val glitchJitter by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "misprint_glitch"
    )

    val isHoloCard = card.isHolographicEdition
    val borderBrush = if (card.isMisprint) {
        val a = card.misprintAnomaly?.accentA ?: MisprintNeon
        val b = card.misprintAnomaly?.accentB ?: MisprintMagenta
        Brush.linearGradient(
            colors = listOf(a, StudioGoldBright, b, SkyAzure, a),
            start = Offset(roll * 12f, pitch * 12f),
            end = Offset(420f + roll * 18f, 640f + pitch * 18f)
        )
    } else if (isHoloCard) {
        Brush.linearGradient(
            colors = listOf(
                SkyAzure,
                Color(0xFFFDE047),
                MisprintMagenta,
                Color(0xFF34D399),
                card.rarity.primaryColor
            ),
            start = Offset(roll * 14f + shimmerShift * 180f, pitch * 14f),
            end = Offset(440f + roll * 14f, 620f + pitch * 14f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                card.rarity.primaryColor,
                Color(0xFFFFF3C4).copy(alpha = 0.90f),
                card.rarity.primaryColor.copy(alpha = 0.85f),
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

    val voiceQuote = remember(card.heroName, card.skinName, card.rarity, card.role) {
        SkinVoiceLines.quoteFor(
            heroName = card.heroName,
            skinName = card.skinName,
            rarity = card.rarity,
            role = card.role
        )
    }

    val imageColorFilter = remember(card.misprintAnomaly) {
        colorFilterForAnomaly(card.misprintAnomaly)
    }

    val cardShape = RoundedCornerShape(if (compact) 14.dp else 18.dp)
    val framePad = if (compact) 4.dp else 7.dp
    val artCorner = RoundedCornerShape(if (compact) 9.dp else 12.dp)

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(0.665f)
            .then(dragModifier)
            .graphicsLayer {
                rotationX = pitch
                rotationY = roll
                cameraDistance = 14f * density
            }
            .shadow(
                elevation = if (compact) 5.dp else 10.dp,
                shape = cardShape,
                ambientColor = card.rarity.primaryColor.copy(alpha = 0.24f),
                spotColor = Color.Black.copy(alpha = 0.42f)
            )
            .clip(cardShape)
            .background(Color(0xFF191924))
            .let { base ->
                if (isFullArtLegend) {
                    // Legend skins render Full Art edge-to-edge with NO frame edges
                    base
                } else {
                    base.border(
                        width = if (card.isMisprint) (if (compact) 2.dp else 2.6.dp) else if (compact) 1.5.dp else 2.2.dp,
                        brush = borderBrush,
                        shape = cardShape
                    )
                }
            }
    ) {
        val narrowMiniCard = compact && maxWidth < 135.dp

        if (isFullArtLegend) {
            // FULL-ART LEGEND RENDER: Edge-to-edge artwork filling entire card with smooth dark scrim at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = (-roll * 0.55f).roundToInt(),
                            y = (pitch * 0.55f).roundToInt()
                        )
                    }
            ) {
                if (card.imageUrl.isNotBlank()) {
                    val request = rememberMlbbImageRequest(card.imageUrl, compact = compact)
                    val fallbackPainter = painterResource(id = R.drawable.bg_mlbb_card_art)
                    AsyncImage(
                        model = request,
                        contentDescription = "${card.heroName} ${card.skinName} full art Legend skin",
                        contentScale = ContentScale.Crop,
                        placeholder = fallbackPainter,
                        error = fallbackPainter,
                        colorFilter = imageColorFilter,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    SkinFallbackArt(modifier = Modifier.fillMaxSize())
                }
            }

            // Smooth bottom vignette for readable quote, signature & standout nameplate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.40f to Color.Transparent,
                            0.66f to Color(0xB814141F),
                            1.0f to Color(0xF212131C)
                        )
                    )
            )

            // Subtle centered geometric chevron watermark behind footer
            CenterChevronWatermark(
                accentColor = card.rarity.primaryColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (compact) 8.dp else 16.dp)
                    .size(width = if (compact) 36.dp else 68.dp, height = if (compact) 22.dp else 40.dp)
            )

            if (isHoloCard) {
                HoloEditionTopTag(
                    compact = compact,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (compact) 6.dp else 10.dp)
                )
            }

            if (card.isMisprint) {
                MisprintTopTag(
                    compact = compact,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(if (compact) 6.dp else 10.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (compact) 6.dp else 11.dp,
                        vertical = if (compact) 5.dp else 10.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 7.dp)
            ) {
                VoiceQuotePlaque(
                    voiceQuote = voiceQuote,
                    heroName = card.heroName,
                    accentColor = card.rarity.primaryColor,
                    compact = compact,
                    narrowMiniCard = narrowMiniCard,
                    translucentFullArt = true
                )
                CardFooterRow(
                    card = card,
                    compact = compact,
                    narrowMiniCard = narrowMiniCard
                )
            }
        } else {
            // STANDARD TCG-INSPIRED CARD FRAME: Matte slate-charcoal body, framed top art, clean quote box, bold footer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0xFF2B2738),
                            0.55f to Color(0xFF232030),
                            0.82f to Color(0xFF1B1B27),
                            1.0f to Color(0xFF14151E)
                        )
                    )
            )
            // Subtle ambient rarity sheen across the dark slate body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                card.rarity.primaryColor.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            center = Offset(300f, 520f),
                            radius = 520f
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = framePad, top = framePad, end = framePad)
                        .aspectRatio(if (compact) 1.08f else 1.03f)
                        .clip(artCorner)
                        .background(Color(0xFF0E1019))
                        .border(
                            width = if (compact) 0.9.dp else 1.25.dp,
                            color = card.rarity.primaryColor.copy(alpha = 0.85f),
                            shape = artCorner
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(
                                    x = (-roll * 0.45f).roundToInt(),
                                    y = (pitch * 0.45f).roundToInt()
                                )
                            }
                    ) {
                        if (card.imageUrl.isNotBlank()) {
                            val request = rememberMlbbSquareImageRequest(card.imageUrl, compact = compact)
                            val fallbackPainter = painterResource(id = R.drawable.bg_mlbb_card_art)
                            AsyncImage(
                                model = request,
                                contentDescription = "${card.heroName} ${card.skinName} skin card",
                                contentScale = ContentScale.Crop,
                                placeholder = fallbackPainter,
                                error = fallbackPainter,
                                colorFilter = imageColorFilter,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            SkinFallbackArt(modifier = Modifier.fillMaxSize())
                        }
                    }

                    if (isHoloCard) {
                        HoloEditionTopTag(
                            compact = compact,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(if (compact) 5.dp else 7.dp)
                        )
                    }

                    if (card.isMisprint) {
                        MisprintTopTag(
                            compact = compact,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(if (compact) 5.dp else 7.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Subtle centered geometric chevron watermark behind lower footer gap
                    CenterChevronWatermark(
                        accentColor = card.rarity.primaryColor,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (compact) 6.dp else 14.dp)
                            .size(width = if (compact) 36.dp else 68.dp, height = if (compact) 22.dp else 40.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = framePad,
                                vertical = if (compact) (if (narrowMiniCard) 2.5.dp else 4.dp) else 8.dp
                            ),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        VoiceQuotePlaque(
                            voiceQuote = voiceQuote,
                            heroName = card.heroName,
                            accentColor = card.rarity.primaryColor,
                            compact = compact,
                            narrowMiniCard = narrowMiniCard,
                            translucentFullArt = false
                        )
                        CardFooterRow(
                            card = card,
                            compact = compact,
                            narrowMiniCard = narrowMiniCard
                        )
                    }
                }
            }
        }

        // Layer 3: Gyroscope-reactive specular sheen + Collectible Holographic Prism (strictly Collector & Legend only!)
        val foilGold = StudioGoldBright
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val glareCenterX = size.width * (0.5f + (roll / 36f)).coerceIn(-0.3f, 1.3f)
                    val glareCenterY = size.height * (0.5f - (pitch / 36f)).coerceIn(-0.3f, 1.3f)
                    val glareIntensity = when {
                        card.isMisprint -> 0.34f
                        isHoloCard -> 0.30f
                        card.rarity.tierRank >= 5 -> 0.22f
                        card.rarity.tierRank >= 3 -> 0.16f
                        else -> 0.10f
                    }

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glareIntensity),
                                card.rarity.primaryColor.copy(alpha = glareIntensity * 0.48f),
                                Color.Transparent
                            ),
                            center = Offset(glareCenterX, glareCenterY),
                            radius = size.maxDimension * 0.72f
                        ),
                        blendMode = BlendMode.Screen
                    )

                    if (isHoloCard) {
                        // True Prismatic Holographic Foil (strictly >= Collector): Multi-band rainbow sweep + diamond refraction grid + sparkles
                        val sweepOffset = (shimmerShift + roll * 0.032f) * size.width
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF38BDF8).copy(alpha = 0.20f),
                                    Color(0xFF34D399).copy(alpha = 0.16f),
                                    foilGold.copy(alpha = 0.22f),
                                    MisprintMagenta.copy(alpha = 0.20f),
                                    Color(0xFFFB7185).copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                start = Offset(sweepOffset - size.width * 0.55f, 0f),
                                end = Offset(sweepOffset + size.width * 0.55f, size.height)
                            ),
                            blendMode = BlendMode.Plus
                        )

                        // Etched diagonal diamond lattice refraction lines
                        val step = size.width / 8f
                        val tiltShift = roll * 1.6f
                        val latticeAlpha = if (compact) 0.07f else 0.09f
                        for (i in -4..12) {
                            val xStart = i * step + tiltShift
                            drawLine(
                                color = Color.White.copy(alpha = latticeAlpha),
                                start = Offset(xStart, 0f),
                                end = Offset(xStart + size.height * 0.55f, size.height),
                                strokeWidth = 1.2f
                            )
                            drawLine(
                                color = SkyAzure.copy(alpha = latticeAlpha * 0.9f),
                                start = Offset(xStart + size.height * 0.55f, 0f),
                                end = Offset(xStart, size.height),
                                strokeWidth = 1.2f
                            )
                        }

                        // Twinkling starlight sparkles
                        val sparklePositions = listOf(
                            Offset(size.width * 0.20f, size.height * 0.18f),
                            Offset(size.width * 0.78f, size.height * 0.28f),
                            Offset(size.width * 0.34f, size.height * 0.48f),
                            Offset(size.width * 0.66f, size.height * 0.62f),
                            Offset(size.width * 0.24f, size.height * 0.78f)
                        )
                        sparklePositions.forEachIndexed { idx, pt ->
                            val phase = ((shimmerShift + idx * 0.27f) % 1f).coerceIn(0f, 1f)
                            val pulse = kotlin.math.sin(phase * Math.PI).toFloat().coerceIn(0f, 1f)
                            if (pulse > 0.15f) {
                                val arm = (if (compact) 5.dp else 8.dp).toPx() * pulse
                                drawLine(
                                    color = Color.White.copy(alpha = 0.72f * pulse),
                                    start = Offset(pt.x - arm, pt.y),
                                    end = Offset(pt.x + arm, pt.y),
                                    strokeWidth = 1.8f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.72f * pulse),
                                    start = Offset(pt.x, pt.y - arm),
                                    end = Offset(pt.x, pt.y + arm),
                                    strokeWidth = 1.8f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    } else if (card.isMisprint) {
                        val sweepOffset = (shimmerShift + roll * 0.025f) * size.width
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    SkyAzure.copy(alpha = 0.16f),
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

        // Layer 4: 1-of-1 Misprint Glitch Scanlines (muted soft anomaly accent)
        if (card.isMisprint) {
            val scanColor = card.misprintAnomaly?.accentA ?: MisprintNeon
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stripeCount = 14
                val step = size.height / stripeCount
                for (i in 0 until stripeCount) {
                    if (i % 2 == 0) {
                        val y = i * step + glitchJitter * 1.4f
                        drawLine(
                            color = scanColor.copy(alpha = 0.11f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.6f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterChevronWatermark(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        val outerChevron = Path().apply {
            moveTo(0f, h * 0.12f)
            lineTo(w * 0.34f, h * 0.12f)
            lineTo(cx, h * 0.74f)
            lineTo(w * 0.66f, h * 0.12f)
            lineTo(w, h * 0.12f)
            lineTo(cx, h * 0.98f)
            close()
        }
        drawPath(
            path = outerChevron,
            color = Color.White.copy(alpha = 0.08f)
        )
        drawPath(
            path = outerChevron,
            color = accentColor.copy(alpha = 0.10f),
            style = Stroke(width = w * 0.025f, join = StrokeJoin.Miter)
        )

        val innerChevron = Path().apply {
            moveTo(w * 0.30f, h * 0.10f)
            lineTo(cx, h * 0.48f)
            lineTo(w * 0.70f, h * 0.10f)
        }
        drawPath(
            path = innerChevron,
            color = Color.White.copy(alpha = 0.10f),
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun HoloEditionTopTag(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF131829),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(SkyAzure, StudioGoldBright, MisprintMagenta)
            )
        ),
        modifier = modifier
    ) {
        Text(
            text = "HOLO",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                fontSize = if (compact) 7.5.sp else 9.5.sp
            ),
            color = StudioGoldBright,
            modifier = Modifier.padding(
                horizontal = if (compact) 5.dp else 7.dp,
                vertical = 2.dp
            )
        )
    }
}

@Composable
private fun MisprintTopTag(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MisprintMagenta.copy(alpha = 0.92f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, StudioGoldBright),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Misprint",
                tint = StudioGoldBright,
                modifier = Modifier.size(if (compact) 10.dp else 12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "1/1",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 8.sp else 10.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun VoiceQuotePlaque(
    voiceQuote: String,
    heroName: String,
    accentColor: Color,
    compact: Boolean,
    narrowMiniCard: Boolean,
    translucentFullArt: Boolean
) {
    Surface(
        color = Color(0xFF282538).copy(alpha = if (translucentFullArt) 0.78f else 0.90f),
        shape = RoundedCornerShape(if (compact) 7.dp else 11.dp),
        border = BorderStroke(
            width = if (compact) 0.75.dp else 1.dp,
            color = accentColor.copy(alpha = 0.58f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) (if (narrowMiniCard) 5.dp else 7.dp) else 12.dp,
                    vertical = if (compact) (if (narrowMiniCard) 2.5.dp else 3.5.dp) else 6.5.dp
                )
        ) {
            Text(
                text = "“$voiceQuote”",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = BrushQuoteFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = when {
                        narrowMiniCard -> 8.5.sp
                        compact -> 10.sp
                        else -> 14.sp
                    },
                    lineHeight = when {
                        narrowMiniCard -> 10.sp
                        compact -> 11.5.sp
                        else -> 16.5.sp
                    }
                ),
                color = Color(0xFFF8FAFC),
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!compact) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "— $heroName",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SignatureHandwritingFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 17.5.sp
                    ),
                    color = StudioGoldBright.copy(alpha = 0.96f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun CardFooterRow(
    card: OwnedCard,
    compact: Boolean,
    narrowMiniCard: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 2.dp else 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = if (compact) 4.dp else 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
            ) {
                Text(
                    text = card.heroName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = when {
                            narrowMiniCard -> 8.5.sp
                            compact -> 10.sp
                            else -> 13.5.sp
                        },
                        lineHeight = when {
                            narrowMiniCard -> 10.sp
                            compact -> 11.5.sp
                            else -> 15.sp
                        },
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (compact) 0.5.sp else 0.9.sp
                    ),
                    color = StudioGoldBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (card.isMisprint && card.misprintAnomaly != null && !compact) {
                    Text(
                        text = "• 1/1 ${card.misprintAnomaly.title.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = card.misprintAnomaly.accentA,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (compact) 0.5.dp else 2.dp))
            Text(
                text = card.skinName,
                style = when {
                    narrowMiniCard -> MaterialTheme.typography.titleSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    compact -> MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    else -> MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                color = Color.White,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(if (compact) 1.5.dp else 3.5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 4.5.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (card.isMisprint) (card.misprintAnomaly?.accentA ?: MisprintNeon)
                            else card.rarity.primaryColor
                        )
                )
                Text(
                    text = card.role.ifBlank { "Hero" },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = when {
                            narrowMiniCard -> 8.sp
                            compact -> 9.5.sp
                            else -> 13.sp
                        },
                        lineHeight = when {
                            narrowMiniCard -> 9.5.sp
                            compact -> 11.sp
                            else -> 15.sp
                        },
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    ),
                    color = if (card.isMisprint) {
                        (card.misprintAnomaly?.accentA ?: MisprintNeon)
                    } else {
                        Color(0xFFE2E8F0)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        RarityLogoBadge(
            rarity = card.rarity,
            compact = compact,
            narrowMiniCard = narrowMiniCard
        )
    }
}

private fun colorFilterForAnomaly(anomaly: MisprintAnomaly?): ColorFilter? {
    return when (anomaly) {
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
            ColorMatrix().apply { setToSaturation(1.38f) }
        )
        MisprintAnomaly.PRISMATIC_GHOST -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.75f, 0.15f, 0.25f, 0f, 16f,
                    0.15f, 0.85f, 0.2f, 0f, 18f,
                    0.2f, 0.2f, 1.05f, 0f, 26f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        MisprintAnomaly.DOUBLE_STAMP_ERROR -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    1.15f, 0f, 0f, 0f, 8f,
                    0f, 1.02f, 0.08f, 0f, 0f,
                    0.12f, 0f, 1.15f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        MisprintAnomaly.VOID_GLITCH -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.65f, 0.1f, 0.35f, 0f, 0f,
                    0.1f, 0.55f, 0.25f, 0f, 0f,
                    0.3f, 0.15f, 1.1f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        MisprintAnomaly.CRIMSON_ECLIPSE -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    1.28f, 0.1f, 0f, 0f, 14f,
                    0f, 0.72f, 0f, 0f, 0f,
                    0f, 0f, 0.78f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        MisprintAnomaly.HOLO_SHATTER -> ColorFilter.colorMatrix(
            ColorMatrix().apply { setToSaturation(1.5f) }
        )
        MisprintAnomaly.SEPIA_RELIC -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.85f, 0.35f, 0.12f, 0f, 12f,
                    0.25f, 0.72f, 0.10f, 0f, 6f,
                    0.10f, 0.22f, 0.55f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        MisprintAnomaly.EMERALD_OVERPRINT -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.75f, 0.15f, 0f, 0f, 0f,
                    0.15f, 1.24f, 0.15f, 0f, 12f,
                    0f, 0.2f, 0.88f, 0f, 6f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        MisprintAnomaly.STARLIGHT_NEGATIVE -> ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.78f, 0.12f, 0.32f, 0f, 18f,
                    0.18f, 0.82f, 0.28f, 0f, 16f,
                    0.35f, 0.20f, 1.20f, 0f, 30f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
        null -> null
    }
}

@Composable
fun RarityLogoBadge(
    rarity: SkinRarity,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    narrowMiniCard: Boolean = false
) {
    val iconSize = when {
        narrowMiniCard -> 18.dp
        compact -> 23.dp
        else -> 36.dp
    }
    Column(
        modifier = modifier.padding(bottom = if (compact) 1.dp else 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RarityLogoSymbol(
            rarity = rarity,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(if (compact) 1.5.dp else 3.dp))
        Text(
            text = rarity.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = when {
                    narrowMiniCard -> 5.8.sp
                    compact -> 7.sp
                    else -> 9.5.sp
                },
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = if (compact) 0.4.sp else 0.7.sp
            ),
            color = rarity.primaryColor,
            maxLines = 1
        )
    }
}

/**
 * Custom Canvas emblems styled after in-game MLBB skin tag crests for every rarity tier.
 */
@Composable
fun RarityLogoSymbol(
    rarity: SkinRarity,
    modifier: Modifier = Modifier
) {
    val crownGold = StudioGoldBright
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = size.minDimension * 0.43f
        val primary = rarity.primaryColor
        val secondary = rarity.secondaryColor
        val highlight = Color.White.copy(alpha = 0.90f)

        when (rarity) {
            SkinRarity.COMMON -> {
                // Classic brushed steel shield + center rivet
                val shield = Path().apply {
                    moveTo(cx, cy - r * 0.9f)
                    lineTo(cx + r * 0.78f, cy - r * 0.48f)
                    lineTo(cx + r * 0.65f, cy + r * 0.45f)
                    lineTo(cx, cy + r * 0.92f)
                    lineTo(cx - r * 0.65f, cy + r * 0.45f)
                    lineTo(cx - r * 0.78f, cy - r * 0.48f)
                    close()
                }
                drawPath(shield, color = primary.copy(alpha = 0.35f))
                drawPath(shield, color = primary, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
                drawCircle(color = highlight, radius = r * 0.24f)
            }

            SkinRarity.UNCOMMON -> {
                // MLBB Elite/Season-style winged emerald badge
                val wings = Path().apply {
                    moveTo(cx - r * 1.02f, cy - r * 0.25f)
                    lineTo(cx - r * 0.38f, cy - r * 0.1f)
                    lineTo(cx, cy - r * 0.85f)
                    lineTo(cx + r * 0.38f, cy - r * 0.1f)
                    lineTo(cx + r * 1.02f, cy - r * 0.25f)
                    lineTo(cx + r * 0.65f, cy + r * 0.45f)
                    lineTo(cx, cy + r * 0.9f)
                    lineTo(cx - r * 0.65f, cy + r * 0.45f)
                    close()
                }
                drawPath(wings, color = primary.copy(alpha = 0.38f))
                drawPath(wings, color = primary, style = Stroke(width = w * 0.075f, join = StrokeJoin.Round))
                // Inner emerald diamond crystal
                val gem = Path().apply {
                    moveTo(cx, cy - r * 0.4f)
                    lineTo(cx + r * 0.32f, cy + r * 0.08f)
                    lineTo(cx, cy + r * 0.56f)
                    lineTo(cx - r * 0.32f, cy + r * 0.08f)
                    close()
                }
                drawPath(gem, color = highlight)
            }

            SkinRarity.SPECIAL -> {
                // MLBB Special-style sapphire faceted shield + dual swept crystal wings
                val crest = Path().apply {
                    moveTo(cx - r * 1.05f, cy - r * 0.42f)
                    lineTo(cx - r * 0.48f, cy - r * 0.18f)
                    lineTo(cx, cy - r * 0.95f)
                    lineTo(cx + r * 0.48f, cy - r * 0.18f)
                    lineTo(cx + r * 1.05f, cy - r * 0.42f)
                    lineTo(cx + r * 0.72f, cy + r * 0.36f)
                    lineTo(cx, cy + r * 0.96f)
                    lineTo(cx - r * 0.72f, cy + r * 0.36f)
                    close()
                }
                drawPath(
                    crest,
                    brush = Brush.verticalGradient(listOf(primary, secondary))
                )
                drawPath(crest, color = crownGold, style = Stroke(width = w * 0.06f, join = StrokeJoin.Round))
                val core = Path().apply {
                    moveTo(cx, cy - r * 0.42f)
                    lineTo(cx + r * 0.35f, cy)
                    lineTo(cx, cy + r * 0.48f)
                    lineTo(cx - r * 0.35f, cy)
                    close()
                }
                drawPath(core, color = highlight)
            }

            SkinRarity.EPIC -> {
                // MLBB Epic-style violet-gold horned crest with swept pinions
                val leftWing = Path().apply {
                    moveTo(cx - r * 1.08f, cy - r * 0.62f)
                    lineTo(cx - r * 0.4f, cy - r * 0.25f)
                    lineTo(cx - r * 0.28f, cy + r * 0.45f)
                    lineTo(cx - r * 0.85f, cy + r * 0.15f)
                    close()
                }
                val rightWing = Path().apply {
                    moveTo(cx + r * 1.08f, cy - r * 0.62f)
                    lineTo(cx + r * 0.4f, cy - r * 0.25f)
                    lineTo(cx + r * 0.28f, cy + r * 0.45f)
                    lineTo(cx + r * 0.85f, cy + r * 0.15f)
                    close()
                }
                drawPath(leftWing, color = crownGold)
                drawPath(rightWing, color = crownGold)
                val coreGem = Path().apply {
                    moveTo(cx, cy - r * 0.98f)
                    lineTo(cx + r * 0.56f, cy)
                    lineTo(cx, cy + r * 0.98f)
                    lineTo(cx - r * 0.56f, cy)
                    close()
                }
                drawPath(coreGem, brush = Brush.linearGradient(listOf(primary, secondary)))
                drawPath(coreGem, color = highlight, style = Stroke(width = w * 0.055f, join = StrokeJoin.Round))
                drawCircle(color = crownGold, radius = r * 0.20f)
            }

            SkinRarity.LUCKYBOX -> {
                // MLBB Luckybox rose-gold treasure prism crest with ribbon wings
                val wings = Path().apply {
                    moveTo(cx - r * 1.08f, cy - r * 0.35f)
                    lineTo(cx - r * 0.55f, cy - r * 0.65f)
                    lineTo(cx, cy - r * 0.25f)
                    lineTo(cx + r * 0.55f, cy - r * 0.65f)
                    lineTo(cx + r * 1.08f, cy - r * 0.35f)
                    lineTo(cx + r * 0.8f, cy + r * 0.55f)
                    lineTo(cx, cy + r * 0.95f)
                    lineTo(cx - r * 0.8f, cy + r * 0.55f)
                    close()
                }
                drawPath(wings, brush = Brush.linearGradient(listOf(primary, crownGold)))
                drawPath(wings, color = highlight, style = Stroke(width = w * 0.055f, join = StrokeJoin.Round))
                drawCircle(color = Color(0xFF1A1025), radius = r * 0.38f)
                drawCircle(color = crownGold, radius = r * 0.24f)
            }

            SkinRarity.COLLECTOR -> {
                // MLBB Collector imperial golden pagoda & triple laurel wings
                val lowerWing = Path().apply {
                    moveTo(cx - r * 1.12f, cy - r * 0.15f)
                    lineTo(cx - r * 0.42f, cy - r * 0.45f)
                    lineTo(cx, cy - r * 1.02f)
                    lineTo(cx + r * 0.42f, cy - r * 0.45f)
                    lineTo(cx + r * 1.12f, cy - r * 0.15f)
                    lineTo(cx + r * 0.72f, cy + r * 0.62f)
                    lineTo(cx, cy + r * 0.98f)
                    lineTo(cx - r * 0.72f, cy + r * 0.62f)
                    close()
                }
                drawPath(lowerWing, brush = Brush.verticalGradient(listOf(crownGold, primary)))
                drawPath(lowerWing, color = highlight, style = Stroke(width = w * 0.06f, join = StrokeJoin.Round))
                val innerDiamond = Path().apply {
                    moveTo(cx, cy - r * 0.52f)
                    lineTo(cx + r * 0.36f, cy + r * 0.05f)
                    lineTo(cx, cy + r * 0.62f)
                    lineTo(cx - r * 0.36f, cy + r * 0.05f)
                    close()
                }
                drawPath(innerDiamond, color = Color(0xFF3A1F0A))
                drawCircle(color = highlight, radius = r * 0.18f, center = Offset(cx, cy + r * 0.05f))
            }

            SkinRarity.LEGEND -> {
                // MLBB Legend crimson-gold triple seraph wings + spiked crown + radiant star jewel
                val seraph = Path().apply {
                    moveTo(cx - r * 1.15f, cy - r * 0.45f)
                    lineTo(cx - r * 0.68f, cy - r * 0.18f)
                    lineTo(cx - r * 0.92f, cy - r * 0.78f)
                    lineTo(cx - r * 0.38f, cy - r * 0.38f)
                    lineTo(cx, cy - r * 1.05f)
                    lineTo(cx + r * 0.38f, cy - r * 0.38f)
                    lineTo(cx + r * 0.92f, cy - r * 0.78f)
                    lineTo(cx + r * 0.68f, cy - r * 0.18f)
                    lineTo(cx + r * 1.15f, cy - r * 0.45f)
                    lineTo(cx + r * 0.76f, cy + r * 0.46f)
                    lineTo(cx, cy + r * 1.0f)
                    lineTo(cx - r * 0.76f, cy + r * 0.46f)
                    close()
                }
                drawPath(seraph, brush = Brush.verticalGradient(listOf(crownGold, primary, secondary)))
                drawPath(seraph, color = highlight, style = Stroke(width = w * 0.055f, join = StrokeJoin.Round))
                drawCircle(color = highlight, radius = r * 0.25f, center = Offset(cx, cy + r * 0.08f))
                drawCircle(color = crownGold, radius = r * 0.15f, center = Offset(cx, cy - r * 1.05f))
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
        initialValue = 0.68f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isMisprint || rarityHint.tierRank >= 5) 650 else 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val frameColor = when {
        isMisprint -> MisprintNeon
        rarityHint.tierRank >= SkinRarity.EPIC.tierRank -> rarityHint.primaryColor
        else -> StudioGoldBright
    }
    val shape = RoundedCornerShape(18.dp)
    val accent = CollectorTheme.colors.accentPrimary

    Box(
        modifier = modifier
            .aspectRatio(0.665f)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(CardBackDeep)
            .border(
                width = 2.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MoonlightSilverBlue.copy(alpha = 0.85f),
                        StudioGoldBright,
                        frameColor.copy(alpha = pulse),
                        StudioGoldBright
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_card_back),
            contentDescription = "MEGA COLLECTOR card back artwork",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            MoonlightNight.copy(alpha = 0.28f)
                        )
                    )
                )
        )

        val trimGold = StudioGoldBright
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            drawRoundRect(
                color = frameColor.copy(alpha = if (isMisprint || rarityHint.tierRank >= SkinRarity.EPIC.tierRank) 0.45f * pulse else 0.22f),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 1.6.dp.toPx())
            )
            drawRoundRect(
                color = trimGold.copy(alpha = 0.20f),
                topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx()),
                cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx()),
                style = Stroke(width = 0.9.dp.toPx())
            )
        }

        if (showTeaserLabel) {
            val teaserText = when {
                isMisprint -> "1/1 ANOMALY PULSE"
                rarityHint.tierRank >= SkinRarity.COLLECTOR.tierRank -> "MYTHIC AURA DETECTED"
                rarityHint.tierRank >= SkinRarity.EPIC.tierRank -> "HIGH FOIL RESONANCE"
                else -> "TAP, SWIPE, OR PINCH"
            }
            Surface(
                color = MoonlightNight.copy(alpha = 0.9f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, frameColor.copy(alpha = 0.7f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
            ) {
                Text(
                    text = teaserText,
                    style = MaterialTheme.typography.labelSmall,
                    color = frameColor.copy(alpha = pulse),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun SkinFallbackArt(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.bg_mlbb_card_art),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MoonlightNight.copy(alpha = 0.22f))
        )
    }
}
