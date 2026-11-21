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
import androidx.compose.ui.text.font.FontStyle
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
import com.example.ui.theme.CardBackDeep
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.MisprintMagenta
import com.example.ui.theme.MisprintNeon
import com.example.ui.theme.MoonlightNight
import com.example.ui.theme.MoonlightSilverBlue
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
                Color.White.copy(alpha = 0.88f),
                card.rarity.primaryColor.copy(alpha = 0.82f),
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

    val cardShape = RoundedCornerShape(if (compact) 14.dp else 18.dp)
    val thinPad = if (compact) 4.dp else 6.dp
    val artCorner = RoundedCornerShape(if (compact) 10.dp else 13.dp)

    // Pokemon TCG-inspired proportion (~63:95): 1:1 square artwork flush at top, rich quote & bottom nameplate below
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
                elevation = if (compact) 6.dp else 14.dp,
                shape = cardShape,
                ambientColor = card.rarity.primaryColor.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(cardShape)
            .background(Color(0xFF0C1220))
            .border(
                width = if (card.isMisprint) (if (compact) 2.4.dp else 3.2.dp) else if (compact) 1.8.dp else 2.6.dp,
                brush = borderBrush,
                shape = cardShape
            )
    ) {
        // Subtle uploaded MLBB emblem card texture on backplate
        Image(
            painter = painterResource(id = R.drawable.bg_mlbb_card_art),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.28f)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. 1:1 SQUARE CHARACTER-FOCUSED ARTWORK (no extra edge/bar on top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = thinPad, top = thinPad, end = thinPad)
                    .aspectRatio(1f)
                    .clip(artCorner)
                    .background(Color(0xFF090D18))
                    .border(
                        width = if (compact) 1.dp else 1.5.dp,
                        color = card.rarity.primaryColor.copy(alpha = 0.72f),
                        shape = artCorner
                    )
            ) {
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
                            loading = {
                                SkinFallbackArt(modifier = Modifier.fillMaxSize())
                            },
                            error = {
                                SkinFallbackArt(modifier = Modifier.fillMaxSize())
                            }
                        )
                    } else {
                        SkinFallbackArt(modifier = Modifier.fillMaxSize())
                    }
                }

                // Minimalist top-right 1/1 tag only on Misprints
                if (card.isMisprint) {
                    Surface(
                        color = MisprintMagenta,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, StudioGoldBright),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(if (compact) 5.dp else 7.dp)
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
            }

            // 2. POKEMON TCG LOWER BODY: Skin Quote Voice Line + Bottom-Left Name + Bottom-Right Rarity Logo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = thinPad, vertical = if (compact) 4.dp else 7.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Quote Voice-Line Plaque (inspired by Pokemon TCG Pokédex/Flavor Text box)
                Surface(
                    color = Color(0xFF141D32).copy(alpha = 0.90f),
                    shape = RoundedCornerShape(if (compact) 6.dp else 9.dp),
                    border = BorderStroke(0.8.dp, card.rarity.primaryColor.copy(alpha = 0.42f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (compact) 6.dp else 9.dp,
                            vertical = if (compact) 3.dp else 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(if (compact) 11.dp else 24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(card.rarity.primaryColor)
                        )
                        Spacer(modifier = Modifier.width(if (compact) 5.dp else 7.dp))
                        Text(
                            text = "“$voiceQuote”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontSize = if (compact) 8.5.sp else 11.sp,
                                lineHeight = if (compact) 10.5.sp else 14.sp
                            ),
                            color = Color(0xFFE2E8F0),
                            maxLines = if (compact) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Thick Bottom Footer Row: Name at Bottom-Left, Rarity Emblem at Bottom-Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (compact) 3.dp else 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (compact) 6.dp else 10.dp)
                    ) {
                        if (card.isMisprint && card.misprintAnomaly != null && !compact) {
                            Text(
                                text = "1/1 ${card.misprintAnomaly.title.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
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
                                fontSize = if (compact) 8.5.sp else 11.sp,
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
                                    fontSize = 11.5.sp,
                                    lineHeight = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 17.sp,
                                    lineHeight = 20.sp,
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
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = if (card.isMisprint) MisprintNeon else Color(0xFF94A3B8),
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
        }

        // Layer 3: Gyroscope-reactive specular metallic foil glare & rainbow prism sheen
        val foilGold = StudioGoldBright
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
                        card.rarity.tierRank >= 3 -> 0.24f
                        else -> 0.14f
                    }

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glareIntensity),
                                card.rarity.primaryColor.copy(alpha = glareIntensity * 0.6f),
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
                                    SkyAzure.copy(alpha = 0.16f),
                                    foilGold.copy(alpha = 0.20f),
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

        // Layer 4: 1-of-1 Misprint Glitch Scanlines
        if (card.isMisprint) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stripeCount = 14
                val step = size.height / stripeCount
                for (i in 0 until stripeCount) {
                    if (i % 2 == 0) {
                        val y = i * step + glitchJitter * 1.6f
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
    }
}

@Composable
fun RarityLogoBadge(
    rarity: SkinRarity,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val iconSize = if (compact) 20.dp else 28.dp
    Surface(
        modifier = modifier,
        color = Color(0xFF151E34),
        shape = RoundedCornerShape(if (compact) 8.dp else 10.dp),
        border = BorderStroke(1.2.dp, rarity.primaryColor)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 5.dp else 8.dp,
                vertical = if (compact) 3.dp else 5.dp
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
                    fontSize = if (compact) 7.sp else 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.35.sp
                ),
                color = rarity.primaryColor,
                maxLines = 1
            )
        }
    }
}

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
        val r = size.minDimension * 0.42f
        val primary = rarity.primaryColor
        val highlight = Color.White.copy(alpha = 0.85f)

        when (rarity) {
            SkinRarity.COMMON -> {
                drawCircle(color = primary.copy(alpha = 0.35f), radius = r)
                drawCircle(color = primary, radius = r, style = Stroke(width = w * 0.10f))
                drawCircle(color = highlight, radius = r * 0.32f)
            }

            SkinRarity.UNCOMMON -> {
                val chevron = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r * 0.9f, cy + r * 0.1f)
                    lineTo(cx, cy + r * 0.85f)
                    lineTo(cx - r * 0.9f, cy + r * 0.1f)
                    close()
                }
                drawPath(chevron, color = primary.copy(alpha = 0.30f))
                drawPath(chevron, color = primary, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))
                drawLine(
                    color = highlight,
                    start = Offset(cx - r * 0.4f, cy - r * 0.05f),
                    end = Offset(cx, cy - r * 0.45f),
                    strokeWidth = w * 0.08f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = highlight,
                    start = Offset(cx, cy - r * 0.45f),
                    end = Offset(cx + r * 0.4f, cy - r * 0.05f),
                    strokeWidth = w * 0.08f,
                    cap = StrokeCap.Round
                )
            }

            SkinRarity.SPECIAL -> {
                val diamond = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r * 0.85f, cy)
                    lineTo(cx, cy + r)
                    lineTo(cx - r * 0.85f, cy)
                    close()
                }
                drawPath(diamond, color = primary.copy(alpha = 0.35f))
                drawPath(diamond, color = primary, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))
                drawCircle(color = highlight, radius = r * 0.25f)
            }

            SkinRarity.EPIC -> {
                val star = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r * 0.28f, cy - r * 0.28f)
                    lineTo(cx + r, cy)
                    lineTo(cx + r * 0.28f, cy + r * 0.28f)
                    lineTo(cx, cy + r)
                    lineTo(cx - r * 0.28f, cy + r * 0.28f)
                    lineTo(cx - r, cy)
                    lineTo(cx - r * 0.28f, cy - r * 0.28f)
                    close()
                }
                drawPath(star, color = primary)
                drawPath(star, color = highlight, style = Stroke(width = w * 0.06f))
            }

            SkinRarity.LUCKYBOX -> {
                val hex = Path().apply {
                    for (i in 0 until 6) {
                        val angle = Math.toRadians((60 * i - 90).toDouble())
                        val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                        val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(hex, color = primary.copy(alpha = 0.38f))
                drawPath(hex, color = primary, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))
                drawCircle(color = crownGold, radius = r * 0.28f)
            }

            SkinRarity.COLLECTOR -> {
                val wing = Path().apply {
                    moveTo(cx - r, cy - r * 0.45f)
                    lineTo(cx - r * 0.38f, cy + r * 0.1f)
                    lineTo(cx, cy - r * 0.9f)
                    lineTo(cx + r * 0.38f, cy + r * 0.1f)
                    lineTo(cx + r, cy - r * 0.45f)
                    lineTo(cx + r * 0.65f, cy + r * 0.72f)
                    lineTo(cx - r * 0.65f, cy + r * 0.72f)
                    close()
                }
                drawPath(wing, color = primary.copy(alpha = 0.42f))
                drawPath(wing, color = crownGold, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
                drawCircle(color = highlight, radius = r * 0.24f, center = Offset(cx, cy + r * 0.2f))
            }

            SkinRarity.LEGEND -> {
                val crown = Path().apply {
                    moveTo(cx - r * 0.95f, cy + r * 0.65f)
                    lineTo(cx - r * 0.95f, cy - r * 0.35f)
                    lineTo(cx - r * 0.45f, cy + r * 0.05f)
                    lineTo(cx, cy - r * 0.88f)
                    lineTo(cx + r * 0.45f, cy + r * 0.05f)
                    lineTo(cx + r * 0.95f, cy - r * 0.35f)
                    lineTo(cx + r * 0.95f, cy + r * 0.65f)
                    close()
                }
                drawPath(crown, color = primary)
                drawPath(crown, color = crownGold, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
                drawCircle(color = crownGold, radius = r * 0.18f, center = Offset(cx, cy - r * 0.88f))
                drawCircle(color = highlight, radius = r * 0.16f, center = Offset(cx, cy + r * 0.25f))
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
    val shape = RoundedCornerShape(18.dp)
    val accent = CollectorTheme.colors.accentPrimary

    Box(
        modifier = modifier
            .aspectRatio(0.665f)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.55f)
            )
            .clip(shape)
            .background(CardBackDeep)
            .border(
                width = 3.dp,
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
