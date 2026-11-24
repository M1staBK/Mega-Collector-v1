package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BinderStyle
import com.example.model.CardBinderDesign
import com.example.model.CosmeticCatalog
import com.example.model.ProfileFrameItem
import com.example.model.ProfileFrameStyle
import com.example.ui.theme.CollectorTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the user's Google avatar (or collector monogram icon) wrapped inside an ornate,
 * distinct Canvas bezel corresponding to their equipped [ProfileFrameItem].
 */
@Composable
fun FramedAvatar(
    photoUrl: String,
    alias: String,
    frame: ProfileFrameItem,
    size: Dp = 64.dp,
    fallbackTint: Color? = null,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val effectivePrimary = if (frame.id == CosmeticCatalog.DEFAULT_FRAME_ID) {
        palette.buttonPrimary
    } else {
        frame.primaryColor
    }
    val effectiveSecondary = if (frame.id == CosmeticCatalog.DEFAULT_FRAME_ID) {
        palette.skyAccent
    } else {
        frame.secondaryColor
    }
    val effectiveGlow = if (frame.id == CosmeticCatalog.DEFAULT_FRAME_ID) {
        palette.accentPrimary
    } else {
        frame.glowColor
    }

    val avatarSize = size * 0.78f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer cosmetic bezel + ornaments
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.5f
            val r = (minOf(w, h) * 0.42f)

            // Ambient radial halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(effectiveGlow.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = minOf(w, h) * 0.52f
                ),
                radius = minOf(w, h) * 0.52f,
                center = Offset(cx, cy)
            )

            // Base metallic ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        effectivePrimary,
                        effectiveGlow,
                        effectiveSecondary,
                        effectivePrimary
                    ),
                    center = Offset(cx, cy)
                ),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = w * 0.08f)
            )

            when (frame.style) {
                ProfileFrameStyle.CLASSIC_RING -> {
                    drawCircle(
                        color = effectiveGlow.copy(alpha = 0.55f),
                        radius = r * 1.07f,
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.02f)
                    )
                }

                ProfileFrameStyle.ROYAL_CROWN -> {
                    // Gold top crown prongs & cardinal studs
                    val crown = Path().apply {
                        moveTo(cx - r * 0.40f, cy - r * 0.88f)
                        lineTo(cx - r * 0.28f, cy - r * 1.18f)
                        lineTo(cx - r * 0.10f, cy - r * 0.98f)
                        lineTo(cx, cy - r * 1.24f)
                        lineTo(cx + r * 0.10f, cy - r * 0.98f)
                        lineTo(cx + r * 0.28f, cy - r * 1.18f)
                        lineTo(cx + r * 0.40f, cy - r * 0.88f)
                        close()
                    }
                    drawPath(
                        path = crown,
                        brush = Brush.verticalGradient(listOf(effectiveGlow, effectivePrimary, effectiveSecondary))
                    )
                    drawCircle(color = effectiveGlow, radius = w * 0.036f, center = Offset(cx, cy + r * 1.02f))
                }

                ProfileFrameStyle.OBSIDIAN_LIGHTNING -> {
                    // Crackling purple & gold lightning arcs around obsidian ring
                    drawCircle(
                        color = Color(0xFF07050C),
                        radius = r * 1.04f,
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.035f)
                    )
                    val angles = listOf(-140.0, -40.0, 40.0, 140.0)
                    angles.forEachIndexed { idx, deg ->
                        val rad = Math.toRadians(deg)
                        val sx = cx + cos(rad).toFloat() * r * 0.90f
                        val sy = cy + sin(rad).toFloat() * r * 0.90f
                        val mx = cx + cos(rad + 0.18).toFloat() * r * 1.15f
                        val my = cy + sin(rad + 0.18).toFloat() * r * 1.15f
                        val ex = cx + cos(rad - 0.10).toFloat() * r * 1.24f
                        val ey = cy + sin(rad - 0.10).toFloat() * r * 1.24f
                        val bolt = Path().apply {
                            moveTo(sx, sy)
                            lineTo(mx, my)
                            lineTo(ex, ey)
                        }
                        drawPath(
                            path = bolt,
                            color = if (idx % 2 == 0) effectiveGlow else effectivePrimary,
                            style = Stroke(width = w * 0.038f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                ProfileFrameStyle.EXALTED_FLAME -> {
                    // Flared rose-magenta wing fins on left and right
                    for (side in listOf(-1f, 1f)) {
                        val wing = Path().apply {
                            moveTo(cx + side * r * 0.86f, cy - r * 0.42f)
                            lineTo(cx + side * r * 1.26f, cy - r * 0.56f)
                            lineTo(cx + side * r * 1.05f, cy)
                            lineTo(cx + side * r * 1.22f, cy + r * 0.34f)
                            lineTo(cx + side * r * 0.86f, cy + r * 0.42f)
                            close()
                        }
                        drawPath(
                            path = wing,
                            brush = Brush.verticalGradient(listOf(effectiveGlow, effectivePrimary, effectiveSecondary))
                        )
                    }
                }

                ProfileFrameStyle.MEGA_AURUM -> {
                    // Bright yellow/gold 8-point blazing sunburst
                    for (i in 0 until 8) {
                        val rad = (i * 45.0) * PI / 180.0
                        val innerR = r * 0.96f
                        val outerR = r * if (i % 2 == 0) 1.22f else 1.12f
                        drawLine(
                            color = if (i % 2 == 0) effectiveGlow else effectivePrimary,
                            start = Offset(
                                cx + cos(rad).toFloat() * innerR,
                                cy + sin(rad).toFloat() * innerR
                            ),
                            end = Offset(
                                cx + cos(rad).toFloat() * outerR,
                                cy + sin(rad).toFloat() * outerR
                            ),
                            strokeWidth = w * 0.048f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                ProfileFrameStyle.CYBER_PRISM -> {
                    // Octagonal prism bevel + neon cyan/violet corners
                    val poly = Path().apply {
                        for (i in 0 until 8) {
                            val a = Math.toRadians(i * 45.0 + 22.5)
                            val px = cx + cos(a).toFloat() * r * 1.10f
                            val py = cy + sin(a).toFloat() * r * 1.10f
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(
                        path = poly,
                        brush = Brush.linearGradient(listOf(effectivePrimary, effectiveGlow, effectiveSecondary)),
                        style = Stroke(width = w * 0.036f, join = StrokeJoin.Miter)
                    )
                }

                ProfileFrameStyle.WORLD_SOVEREIGN -> {
                    // Imperial winged crest + top ruby diamond
                    for (side in listOf(-1f, 1f)) {
                        val crest = Path().apply {
                            moveTo(cx + side * r * 0.82f, cy - r * 0.50f)
                            lineTo(cx + side * r * 1.28f, cy - r * 0.25f)
                            lineTo(cx + side * r * 1.12f, cy + r * 0.25f)
                            lineTo(cx + side * r * 0.80f, cy + r * 0.56f)
                            close()
                        }
                        drawPath(
                            path = crest,
                            brush = Brush.verticalGradient(listOf(effectiveGlow, effectivePrimary))
                        )
                    }
                    val diamond = Path().apply {
                        moveTo(cx, cy - r * 1.25f)
                        lineTo(cx + r * 0.20f, cy - r * 0.98f)
                        lineTo(cx, cy - r * 0.78f)
                        lineTo(cx - r * 0.20f, cy - r * 0.98f)
                        close()
                    }
                    drawPath(path = diamond, color = effectiveGlow)
                }

                ProfileFrameStyle.MYTHIC_LATTICE -> {
                    // 4 cardinal celestial diamond nodes
                    for (i in 0 until 4) {
                        val a = Math.toRadians(i * 90.0)
                        val nx = cx + cos(a).toFloat() * r * 1.06f
                        val ny = cy + sin(a).toFloat() * r * 1.06f
                        val d = w * 0.075f
                        val gem = Path().apply {
                            moveTo(nx, ny - d)
                            lineTo(nx + d, ny)
                            lineTo(nx, ny + d)
                            lineTo(nx - d, ny)
                            close()
                        }
                        drawPath(path = gem, color = effectiveGlow)
                        drawPath(path = gem, color = effectivePrimary, style = Stroke(width = w * 0.018f))
                    }
                }
            }
        }

        // Inner circular portrait container
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            fallbackTint ?: effectivePrimary,
                            effectiveSecondary
                        )
                    )
                )
                .border(1.5.dp, effectiveGlow.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = rememberUserAvatarRequest(photoUrl),
                    contentDescription = "$alias avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "$alias avatar",
                    tint = Color.White,
                    modifier = Modifier.size(avatarSize * 0.54f)
                )
            }
        }
    }
}

/**
 * Miniature 3D stitched folio preview of a [CardBinderDesign] for the Cosmetics Shop.
 */
@Composable
fun BinderCoverPreview(
    binder: CardBinderDesign,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    val primary = if (binder.id == CosmeticCatalog.DEFAULT_BINDER_ID) palette.buttonPrimary else binder.coverPrimary
    val secondary = if (binder.id == CosmeticCatalog.DEFAULT_BINDER_ID) palette.abyss else binder.coverSecondary
    val trim = if (binder.id == CosmeticCatalog.DEFAULT_BINDER_ID) palette.accentPrimary else binder.trimGold

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(primary, secondary)))
            .border(1.6.dp, trim.copy(alpha = 0.90f), RoundedCornerShape(14.dp))
    ) {
        // Left stitched binder spine + 3 metallic rings
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .fillMaxHeight()
                    .background(binder.spineColor)
                    .border(0.6.dp, trim.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(width = 11.dp, height = 5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(binder.ringColor)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Stitched inner border
                    drawRoundRect(
                        color = trim.copy(alpha = 0.45f),
                        topLeft = Offset(2f, 2f),
                        size = Size(w - 4f, h - 4f),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 1.6f)
                    )
                    // Corner gold brackets
                    val bLen = minOf(w, h) * 0.22f
                    drawLine(trim, Offset(0f, 0f), Offset(bLen, 0f), strokeWidth = 4f)
                    drawLine(trim, Offset(0f, 0f), Offset(0f, bLen), strokeWidth = 4f)
                    drawLine(trim, Offset(w, h), Offset(w - bLen, h), strokeWidth = 4f)
                    drawLine(trim, Offset(w, h), Offset(w, h - bLen), strokeWidth = 4f)
                }

                Surface(
                    color = secondary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, trim)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (binder.style == BinderStyle.OBSIDIAN_PACK_EDITION) {
                                Icons.Default.Bolt
                            } else {
                                Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = trim,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "VAULT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.9.sp
                            ),
                            color = trim
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wraps the Collector Vault banner in the active [CardBinderDesign] folio leather,
 * metallic spine clasps, and gold trim.
 */
@Composable
fun VaultBinderBannerFrame(
    binder: CardBinderDesign,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = CollectorTheme.colors
    val isDefault = binder.id == CosmeticCatalog.DEFAULT_BINDER_ID
    val primary = if (isDefault) palette.frostSurface else binder.coverPrimary
    val secondary = if (isDefault) palette.cardSurface else binder.coverSecondary
    val trim = if (isDefault) palette.glassHighlight else binder.trimGold
    val spine = if (isDefault) palette.buttonPrimary.copy(alpha = 0.18f) else binder.spineColor

    Surface(
        color = secondary,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(if (isDefault) 1.3.dp else 1.8.dp, trim.copy(alpha = 0.85f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(primary, secondary)))
        ) {
            // Binder left metallic spine strip with clasps
            Column(
                modifier = Modifier
                    .width(20.dp)
                    .background(spine)
                    .padding(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 13.dp, height = 6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isDefault) palette.accentPrimary.copy(alpha = 0.7f) else binder.ringColor)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}
