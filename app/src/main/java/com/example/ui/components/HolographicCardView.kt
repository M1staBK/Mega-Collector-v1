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
import androidx.compose.ui.draw.alpha
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
import coil.compose.SubcomposeAsyncImage
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
                Color.White.copy(alpha = 0.85f),
                card.rarity.primaryColor.copy(alpha = 0.80f),
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
    val thinPad = if (compact) 4.dp else 6.dp
    val artCorner = RoundedCornerShape(if (compact) 10.dp else 13.dp)

    Box(
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
                spotColor = Color.Black.copy(alpha = 0.40f)
            )
            .clip(cardShape)
            .background(Color(0xFF0C1220))
            .let { base ->
                if (isFullArtLegend) {
                    // Legend skins render Full Art edge-to-edge with NO frame edges
                    base
                } else {
                    base.border(
                        width = if (card.isMisprint) (if (compact) 2.2.dp else 2.8.dp) else if (compact) 1.6.dp else 2.4.dp,
                        brush = borderBrush,
                        shape = cardShape
                    )
                }
            }
    ) {
        if (isFullArtLegend) {
            // FULL-ART LEGEND RENDER: Edge-to-edge artwork filling entire card with smooth translucent scrim at bottom
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
                    val request = rememberMlbbImageRequest(card.imageUrl)
                    SubcomposeAsyncImage(
                        model = request,
                        contentDescription = "${card.heroName} ${card.skinName} full art Legend skin",
                        contentScale = ContentScale.Crop,
                        colorFilter = imageColorFilter,
                        modifier = Modifier.fillMaxSize(),
                        loading = { SkinFallbackArt(modifier = Modifier.fillMaxSize()) },
                        error = { SkinFallbackArt(modifier = Modifier.fillMaxSize()) }
                    )
                } else {
                    SkinFallbackArt(modifier = Modifier.fillMaxSize())
                }
            }

            // Smooth bottom vignette for readable calligraphy quote, signature & nameplate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.42f to Color.Transparent,
                            0.68f to Color(0xAA090D18),
                            1.0f to Color(0xEC070B14)
                        )
                    )
            )

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
                    .padding(horizontal = if (compact) 7.dp else 11.dp, vertical = if (compact) 6.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
            ) {
                VoiceQuotePlaque(
                    voiceQuote = voiceQuote,
                    heroName = card.heroName,
                    accentColor = card.rarity.primaryColor,
                    compact = compact,
                    translucentFullArt = true
                )
                CardFooterRow(
                    card = card,
                    compact = compact
                )
            }
        } else {
            // STANDARD POKEMON TCG-INSPIRED CARD FRAME: 1:1 square artwork on top, quote + signature + nameplate below
            Image(
                painter = painterResource(id = R.drawable.bg_mlbb_card_art),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.28f)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = thinPad, top = thinPad, end = thinPad)
                        .aspectRatio(1f)
                        .clip(artCorner)
                        .background(Color(0xFF090D18))
                        .border(
                            width = if (compact) 1.dp else 1.4.dp,
                            color = card.rarity.primaryColor.copy(alpha = 0.65f),
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
                            val request = rememberMlbbSquareImageRequest(card.imageUrl)
                            SubcomposeAsyncImage(
                                model = request,
                                contentDescription = "${card.heroName} ${card.skinName} skin card",
                                contentScale = ContentScale.Crop,
                                colorFilter = imageColorFilter,
                                modifier = Modifier.fillMaxSize(),
                                loading = { SkinFallbackArt(modifier = Modifier.fillMaxSize()) },
                                error = { SkinFallbackArt(modifier = Modifier.fillMaxSize()) }
                            )
                        } else {
                            SkinFallbackArt(modifier = Modifier.fillMaxSize())
                        }
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = thinPad, vertical = if (compact) 3.dp else 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    VoiceQuotePlaque(
                        voiceQuote = voiceQuote,
                        heroName = card.heroName,
                        accentColor = card.rarity.primaryColor,
                        compact = compact,
                        translucentFullArt = false
                    )
                    CardFooterRow(
                        card = card,
                        compact = compact
                    )
                }
            }
        }

        // Layer 3: Gyroscope-reactive specular metallic foil glare & soft rainbow prism sheen
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
                        card.rarity.tierRank >= 5 -> 0.28f
                        card.rarity.tierRank >= 3 -> 0.20f
                        else -> 0.12f
                    }

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glareIntensity),
                                card.rarity.primaryColor.copy(alpha = glareIntensity * 0.55f),
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
                                    SkyAzure.copy(alpha = 0.14f),
                                    foilGold.copy(alpha = 0.17f),
                                    MisprintMagenta.copy(alpha = 0.14f),
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
    translucentFullArt: Boolean
) {
    Surface(
        color = Color(0xFF121A2C).copy(alpha = if (translucentFullArt) 0.72f else 0.88f),
        shape = RoundedCornerShape(if (compact) 6.dp else 9.dp),
        border = BorderStroke(0.75.dp, accentColor.copy(alpha = 0.36f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 5.dp else 9.dp,
                vertical = if (compact) 2.5.dp else 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (compact) 14.dp else 28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor.copy(alpha = 0.85f))
            )
            Spacer(modifier = Modifier.width(if (compact) 4.dp else 7.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "“$voiceQuote”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = BrushQuoteFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (compact) 10.sp else 13.5.sp,
                        lineHeight = if (compact) 11.5.sp else 15.5.sp
                    ),
                    color = Color(0xFFF1F5F9),
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!compact) {
                    Text(
                        text = "— $heroName",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = SignatureHandwritingFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.5.sp,
                            lineHeight = 17.sp
                        ),
                        color = StudioGoldBright.copy(alpha = 0.94f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFooterRow(
    card: OwnedCard,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 2.dp else 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = if (compact) 5.dp else 8.dp)
        ) {
            if (card.isMisprint && card.misprintAnomaly != null && !compact) {
                Text(
                    text = "1/1 ${card.misprintAnomaly.title.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = card.misprintAnomaly.accentA,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = card.heroName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 8.sp else 10.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = StudioGoldBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.skinName,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = 16.5.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                color = Color.White,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!compact) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${card.serialCode} • ${card.role}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (card.isMisprint) (card.misprintAnomaly?.accentA ?: MisprintNeon) else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        RarityLogoBadge(
            rarity = card.rarity,
            compact = compact
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
    modifier: Modifier = Modifier
) {
    val iconSize = if (compact) 21.dp else 29.dp
    Surface(
        modifier = modifier,
        color = Color(0xFF121A2E).copy(alpha = 0.92f),
        shape = RoundedCornerShape(if (compact) 8.dp else 10.dp),
        border = BorderStroke(1.1.dp, rarity.primaryColor.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 5.dp else 8.dp,
                vertical = if (compact) 3.dp else 4.5.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RarityLogoSymbol(
                rarity = rarity,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = rarity.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 6.5.sp else 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                ),
                color = rarity.primaryColor,
                maxLines = 1
            )
        }
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
            painter = painterResource(id = R.drawable.bg_mlbb_card_art),
            contentDescription = "Card background artwork",
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
                            MoonlightNight.copy(alpha = 0.35f),
                            MoonlightNight.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        val trimGold = StudioGoldBright
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            drawRoundRect(
                color = frameColor.copy(alpha = 0.42f * pulse),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawRoundRect(
                color = trimGold.copy(alpha = 0.24f),
                topLeft = Offset(7.dp.toPx(), 7.dp.toPx()),
                size = Size(size.width - 14.dp.toPx(), size.height - 14.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
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
