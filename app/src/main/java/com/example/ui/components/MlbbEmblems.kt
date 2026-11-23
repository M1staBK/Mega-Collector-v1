package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.model.MlbbCollectionTier

/**
 * Custom Canvas emblem modeling the official MLBB Collection Level badges:
 * - Tiers 1-4 (right side): sleek faceted metallic V-shield crests (Novice -> Expert)
 * - Tiers 5-8 (left side): ornate crowned multi-wing diamond crests (Renowned -> World)
 */
@Composable
fun MlbbCollectionBadge(
    tier: MlbbCollectionTier,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false
) {
    val alphaMul = if (dimmed) 0.42f else 1f
    Box(
        modifier = modifier.semantics {
            contentDescription = tier.title
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f

            val primary = tier.primaryColor.copy(alpha = alphaMul)
            val secondary = tier.secondaryColor.copy(alpha = alphaMul)
            val trim = tier.trimColor.copy(alpha = alphaMul)
            val core = tier.coreColor.copy(alpha = alphaMul)

            if (tier.isWingedCrest) {
                // Radiant glow halo behind high-tier winged crests
                if (!dimmed) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(trim.copy(alpha = 0.28f), Color.Transparent),
                            center = Offset(cx, h * 0.46f),
                            radius = w * 0.52f
                        ),
                        radius = w * 0.52f,
                        center = Offset(cx, h * 0.46f)
                    )
                }

                // Upper flared angel/dragon wings (3 tiered pinions per side)
                val upperWings = Path().apply {
                    moveTo(cx, h * 0.14f)
                    lineTo(w * 0.18f, h * 0.10f)
                    lineTo(w * 0.06f, h * 0.28f)
                    lineTo(w * 0.22f, h * 0.34f)
                    lineTo(w * 0.12f, h * 0.48f)
                    lineTo(w * 0.28f, h * 0.56f)
                    lineTo(cx, h * 0.92f)
                    lineTo(w * 0.72f, h * 0.56f)
                    lineTo(w * 0.88f, h * 0.48f)
                    lineTo(w * 0.78f, h * 0.34f)
                    lineTo(w * 0.94f, h * 0.28f)
                    lineTo(w * 0.82f, h * 0.10f)
                    close()
                }
                drawPath(
                    path = upperWings,
                    brush = Brush.verticalGradient(listOf(trim, primary, secondary))
                )
                drawPath(
                    path = upperWings,
                    color = trim.copy(alpha = alphaMul * 0.9f),
                    style = Stroke(width = w * 0.032f, join = StrokeJoin.Round)
                )

                // Top crown spire
                val crown = Path().apply {
                    moveTo(cx, h * 0.03f)
                    lineTo(w * 0.60f, h * 0.22f)
                    lineTo(cx, h * 0.30f)
                    lineTo(w * 0.40f, h * 0.22f)
                    close()
                }
                drawPath(
                    path = crown,
                    brush = Brush.verticalGradient(listOf(core, trim))
                )

                // Inner diamond shield core
                val innerShield = Path().apply {
                    moveTo(cx, h * 0.20f)
                    lineTo(w * 0.72f, h * 0.38f)
                    lineTo(cx, h * 0.86f)
                    lineTo(w * 0.28f, h * 0.38f)
                    close()
                }
                drawPath(
                    path = innerShield,
                    brush = Brush.linearGradient(
                        colors = listOf(primary, secondary, primary),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                drawPath(
                    path = innerShield,
                    color = trim,
                    style = Stroke(width = w * 0.026f, join = StrokeJoin.Round)
                )

                // Central radiant gem
                val gem = Path().apply {
                    moveTo(cx, h * 0.31f)
                    lineTo(w * 0.59f, h * 0.47f)
                    lineTo(cx, h * 0.69f)
                    lineTo(w * 0.41f, h * 0.47f)
                    close()
                }
                drawPath(
                    path = gem,
                    brush = Brush.verticalGradient(listOf(core, trim, primary))
                )
                drawLine(
                    color = core,
                    start = Offset(cx, h * 0.22f),
                    end = Offset(cx, h * 0.84f),
                    strokeWidth = w * 0.024f,
                    cap = StrokeCap.Round
                )
            } else {
                // Tiers 1-4: Faceted V-Shield Chevron Crest (right side of screenshot)
                val outerShield = Path().apply {
                    moveTo(w * 0.14f, h * 0.14f)
                    lineTo(w * 0.42f, h * 0.14f)
                    lineTo(cx, h * 0.24f)
                    lineTo(w * 0.58f, h * 0.14f)
                    lineTo(w * 0.86f, h * 0.14f)
                    lineTo(w * 0.93f, h * 0.25f)
                    lineTo(w * 0.72f, h * 0.54f)
                    lineTo(cx, h * 0.93f)
                    lineTo(w * 0.28f, h * 0.54f)
                    lineTo(w * 0.07f, h * 0.25f)
                    close()
                }
                drawPath(
                    path = outerShield,
                    brush = Brush.linearGradient(
                        colors = listOf(trim, primary, secondary),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                drawPath(
                    path = outerShield,
                    color = trim,
                    style = Stroke(width = w * 0.035f, join = StrokeJoin.Round)
                )

                // Left metallic highlight facet
                val leftFacet = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    lineTo(cx, h * 0.30f)
                    lineTo(cx, h * 0.82f)
                    lineTo(w * 0.32f, h * 0.50f)
                    close()
                }
                drawPath(
                    path = leftFacet,
                    brush = Brush.verticalGradient(listOf(core.copy(alpha = alphaMul * 0.85f), primary))
                )

                // Right shaded facet
                val rightFacet = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    lineTo(cx, h * 0.30f)
                    lineTo(cx, h * 0.82f)
                    lineTo(w * 0.68f, h * 0.50f)
                    close()
                }
                drawPath(
                    path = rightFacet,
                    brush = Brush.verticalGradient(listOf(primary, secondary))
                )

                // Center crest spine + diamond keystone
                val keystone = Path().apply {
                    moveTo(cx, h * 0.27f)
                    lineTo(w * 0.57f, h * 0.43f)
                    lineTo(cx, h * 0.64f)
                    lineTo(w * 0.43f, h * 0.43f)
                    close()
                }
                drawPath(path = keystone, color = core)
                drawLine(
                    color = trim,
                    start = Offset(cx, h * 0.26f),
                    end = Offset(cx, h * 0.85f),
                    strokeWidth = w * 0.025f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

enum class MlbbChestStyle {
    SILVER_MAGENTA_ARCH,
    EMERALD_RUNIC_CUBE,
    GOLD_HELMET_VAULT,
    GOLDEN_LOTUS_CORE,
    ROYAL_GOLD_AMETHYST,
    CRIMSON_RIBBON_GIFT,
    EMERALD_RUBY_RIBBON,
    SAPPHIRE_RUBY_RIBBON,
    FOREST_BRONZE_CREST,
    MINT_VISOR_RIBBON,
    AMETHYST_EMERALD_HEX
}

private enum class ChestSilhouette {
    ARCHED_VAULT,
    RUNIC_CUBE,
    RIBBON_GIFT
}

/**
 * Custom Canvas renderer modeling all 11 MLBB chests from the reference screenshot.
 * Keeps the chest shut unless [opened] is true.
 */
@Composable
fun MlbbTreasureChest(
    opened: Boolean,
    highlighted: Boolean,
    dimmed: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    style: MlbbChestStyle = MlbbChestStyle.SILVER_MAGENTA_ARCH,
    contentDescription: String = "MLBB Mystery Chest"
) {
    val alpha = if (dimmed) 0.55f else 1f
    val silhouette = when (style) {
        MlbbChestStyle.SILVER_MAGENTA_ARCH,
        MlbbChestStyle.GOLD_HELMET_VAULT,
        MlbbChestStyle.GOLDEN_LOTUS_CORE -> ChestSilhouette.ARCHED_VAULT
        MlbbChestStyle.EMERALD_RUNIC_CUBE,
        MlbbChestStyle.ROYAL_GOLD_AMETHYST,
        MlbbChestStyle.AMETHYST_EMERALD_HEX -> ChestSilhouette.RUNIC_CUBE
        else -> ChestSilhouette.RIBBON_GIFT
    }

    Box(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f

            val auraColor = if (highlighted) Color(0xFFFFDE59) else accentColor
            if (highlighted || !opened) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = if (highlighted) 0.44f else 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(cx, h * 0.54f),
                        radius = w * 0.54f
                    ),
                    radius = w * 0.54f,
                    center = Offset(cx, h * 0.54f)
                )
            }

            if (opened) {
                val beam = Path().apply {
                    moveTo(w * 0.24f, h * 0.54f)
                    lineTo(w * 0.10f, h * 0.08f)
                    lineTo(w * 0.90f, h * 0.08f)
                    lineTo(w * 0.76f, h * 0.54f)
                    close()
                }
                drawPath(
                    path = beam,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFF3B0).copy(alpha = 0.62f * alpha),
                            Color(0xFFF59E0B).copy(alpha = 0.80f * alpha)
                        )
                    )
                )
            }

            when (silhouette) {
                ChestSilhouette.ARCHED_VAULT -> drawArchedVaultChest(style, opened, alpha)
                ChestSilhouette.RUNIC_CUBE -> drawRunicCubeChest(style, opened, alpha)
                ChestSilhouette.RIBBON_GIFT -> drawRibbonGiftChest(style, opened, alpha)
            }
        }
    }
}

private fun DrawScope.drawArchedVaultChest(
    style: MlbbChestStyle,
    opened: Boolean,
    alpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f

    val trimLight: Color
    val trimDark: Color
    val bodyTop: Color
    val bodyBottom: Color
    val lidTop: Color
    val lidBottom: Color
    val coreColor: Color

    when (style) {
        MlbbChestStyle.SILVER_MAGENTA_ARCH -> {
            trimLight = Color(0xFFE0ECFF).copy(alpha = alpha)
            trimDark = Color(0xFF6B85B8).copy(alpha = alpha)
            bodyTop = Color(0xFFE5A43B).copy(alpha = alpha)
            bodyBottom = Color(0xFF8C5319).copy(alpha = alpha)
            lidTop = Color(0xFFEC4899).copy(alpha = alpha)
            lidBottom = Color(0xFF7E22CE).copy(alpha = alpha)
            coreColor = Color(0xFFD8E6FF).copy(alpha = alpha)
        }
        MlbbChestStyle.GOLD_HELMET_VAULT -> {
            trimLight = Color(0xFFFDE68A).copy(alpha = alpha)
            trimDark = Color(0xFF92400E).copy(alpha = alpha)
            bodyTop = Color(0xFF7E22CE).copy(alpha = alpha)
            bodyBottom = Color(0xFF3B0764).copy(alpha = alpha)
            lidTop = Color(0xFFA855F7).copy(alpha = alpha)
            lidBottom = Color(0xFF581C87).copy(alpha = alpha)
            coreColor = Color(0xFF38BDF8).copy(alpha = alpha)
        }
        else -> { // GOLDEN_LOTUS_CORE
            trimLight = Color(0xFFFFF7D6).copy(alpha = alpha)
            trimDark = Color(0xFFB45309).copy(alpha = alpha)
            bodyTop = Color(0xFFF59E0B).copy(alpha = alpha)
            bodyBottom = Color(0xFF78350F).copy(alpha = alpha)
            lidTop = Color(0xFFFCD34D).copy(alpha = alpha)
            lidBottom = Color(0xFFB45309).copy(alpha = alpha)
            coreColor = Color(0xFFF472B6).copy(alpha = alpha)
        }
    }

    val bodyPath = Path().apply {
        moveTo(w * 0.14f, h * 0.48f)
        lineTo(w * 0.86f, h * 0.48f)
        lineTo(w * 0.81f, h * 0.88f)
        lineTo(w * 0.19f, h * 0.88f)
        close()
    }
    drawPath(bodyPath, brush = Brush.verticalGradient(listOf(bodyTop, bodyBottom)))
    drawPath(bodyPath, color = trimDark, style = Stroke(width = w * 0.04f, join = StrokeJoin.Round))

    drawGoldStrap(w * 0.25f, h * 0.48f, h * 0.88f, w * 0.08f, trimLight, trimDark)
    drawGoldStrap(w * 0.75f, h * 0.48f, h * 0.88f, w * 0.08f, trimLight, trimDark)

    rotate(degrees = if (opened) -34f else 0f, pivot = Offset(w * 0.15f, h * 0.48f)) {
        val lidPath = Path().apply {
            moveTo(w * 0.12f, h * 0.50f)
            cubicTo(w * 0.14f, h * 0.20f, w * 0.86f, h * 0.20f, w * 0.88f, h * 0.50f)
            close()
        }
        drawPath(lidPath, brush = Brush.verticalGradient(listOf(lidTop, lidBottom)))
        drawPath(lidPath, color = trimLight, style = Stroke(width = w * 0.045f, join = StrokeJoin.Round))

        drawLine(
            brush = Brush.verticalGradient(listOf(trimLight, trimDark)),
            start = Offset(w * 0.28f, h * 0.28f),
            end = Offset(w * 0.26f, h * 0.49f),
            strokeWidth = w * 0.08f,
            cap = StrokeCap.Round
        )
        drawLine(
            brush = Brush.verticalGradient(listOf(trimLight, trimDark)),
            start = Offset(w * 0.72f, h * 0.28f),
            end = Offset(w * 0.74f, h * 0.49f),
            strokeWidth = w * 0.08f,
            cap = StrokeCap.Round
        )
    }

    when (style) {
        MlbbChestStyle.SILVER_MAGENTA_ARCH -> {
            drawCircle(color = trimDark, radius = w * 0.125f, center = Offset(cx, h * 0.58f))
            drawCircle(color = coreColor, radius = w * 0.095f, center = Offset(cx, h * 0.58f))
            drawCircle(color = Color(0xFF1E293B).copy(alpha = alpha), radius = w * 0.032f, center = Offset(cx, h * 0.565f))
        }
        MlbbChestStyle.GOLD_HELMET_VAULT -> {
            // Helmet visor mask crest with glowing cyan eyes
            val visor = Path().apply {
                moveTo(cx, h * 0.40f)
                lineTo(w * 0.63f, h * 0.52f)
                lineTo(w * 0.58f, h * 0.78f)
                lineTo(cx, h * 0.83f)
                lineTo(w * 0.42f, h * 0.78f)
                lineTo(w * 0.37f, h * 0.52f)
                close()
            }
            drawPath(visor, brush = Brush.verticalGradient(listOf(trimLight, trimDark)))
            drawCircle(color = coreColor, radius = w * 0.035f, center = Offset(w * 0.44f, h * 0.60f))
            drawCircle(color = coreColor, radius = w * 0.035f, center = Offset(w * 0.56f, h * 0.60f))
        }
        else -> {
            // White lotus petals flanking glowing pink seam
            drawCircle(color = coreColor, radius = w * 0.14f, center = Offset(cx, h * 0.56f))
            drawArc(
                color = trimLight,
                startAngle = 100f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.16f, h * 0.38f),
                size = Size(w * 0.18f, h * 0.34f),
                style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
            )
            drawArc(
                color = trimLight,
                startAngle = -80f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.02f, h * 0.38f),
                size = Size(w * 0.18f, h * 0.34f),
                style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawRunicCubeChest(
    style: MlbbChestStyle,
    opened: Boolean,
    alpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f

    val bodyColor: Color
    val bodyDark: Color
    val trimLight: Color
    val trimDark: Color
    val topGlow: Color
    val gemColor: Color

    when (style) {
        MlbbChestStyle.EMERALD_RUNIC_CUBE -> {
            bodyColor = Color(0xFF334E58).copy(alpha = alpha)
            bodyDark = Color(0xFF17272E).copy(alpha = alpha)
            trimLight = Color(0xFFE2E8F0).copy(alpha = alpha)
            trimDark = Color(0xFF475569).copy(alpha = alpha)
            topGlow = Color(0xFF94A3B8).copy(alpha = alpha)
            gemColor = Color(0xFF10B981).copy(alpha = alpha)
        }
        MlbbChestStyle.ROYAL_GOLD_AMETHYST -> {
            bodyColor = Color(0xFFF59E0B).copy(alpha = alpha)
            bodyDark = Color(0xFF92400E).copy(alpha = alpha)
            trimLight = Color(0xFFFEF08A).copy(alpha = alpha)
            trimDark = Color(0xFFB45309).copy(alpha = alpha)
            topGlow = Color(0xFFE879F9).copy(alpha = alpha)
            gemColor = Color(0xFF9333EA).copy(alpha = alpha)
        }
        else -> { // AMETHYST_EMERALD_HEX
            bodyColor = Color(0xFF7E22CE).copy(alpha = alpha)
            bodyDark = Color(0xFF3B0764).copy(alpha = alpha)
            trimLight = Color(0xFFA7F3D0).copy(alpha = alpha)
            trimDark = Color(0xFF4C1D95).copy(alpha = alpha)
            topGlow = Color(0xFF6EE7B7).copy(alpha = alpha)
            gemColor = Color(0xFF10B981).copy(alpha = alpha)
        }
    }

    drawRoundRect(
        brush = Brush.verticalGradient(listOf(bodyColor, bodyDark)),
        topLeft = Offset(w * 0.16f, h * 0.42f),
        size = Size(w * 0.68f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRoundRect(
        color = trimDark,
        topLeft = Offset(w * 0.16f, h * 0.42f),
        size = Size(w * 0.68f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
        style = Stroke(width = w * 0.04f)
    )

    rotate(degrees = if (opened) -34f else 0f, pivot = Offset(w * 0.16f, h * 0.42f)) {
        val topPlate = Path().apply {
            moveTo(w * 0.22f, h * 0.24f)
            lineTo(w * 0.78f, h * 0.24f)
            lineTo(w * 0.87f, h * 0.44f)
            lineTo(w * 0.13f, h * 0.44f)
            close()
        }
        drawPath(topPlate, brush = Brush.verticalGradient(listOf(topGlow, bodyColor)))
        drawPath(topPlate, color = trimLight, style = Stroke(width = w * 0.038f, join = StrokeJoin.Round))
    }

    // Center crystal/orb lock emblem
    val gemR = w * 0.14f
    val gy = h * 0.64f
    drawCircle(color = trimLight, radius = gemR * 1.15f, center = Offset(cx, gy))
    val hex = Path().apply {
        moveTo(cx, gy - gemR)
        lineTo(cx + gemR * 0.86f, gy - gemR * 0.45f)
        lineTo(cx + gemR * 0.86f, gy + gemR * 0.45f)
        lineTo(cx, gy + gemR)
        lineTo(cx - gemR * 0.86f, gy + gemR * 0.45f)
        lineTo(cx - gemR * 0.86f, gy - gemR * 0.45f)
        close()
    }
    drawPath(hex, brush = Brush.radialGradient(listOf(Color.White.copy(alpha = alpha), gemColor)))
    drawPath(hex, color = trimDark, style = Stroke(width = w * 0.025f))
}

private fun DrawScope.drawRibbonGiftChest(
    style: MlbbChestStyle,
    opened: Boolean,
    alpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f

    val boxTop: Color
    val boxBottom: Color
    val trimColor: Color
    val ribbonColor: Color
    val crestColor: Color
    val gemColor: Color

    when (style) {
        MlbbChestStyle.CRIMSON_RIBBON_GIFT -> {
            boxTop = Color(0xFFEF4444).copy(alpha = alpha)
            boxBottom = Color(0xFF991B1B).copy(alpha = alpha)
            trimColor = Color(0xFFFBBF24).copy(alpha = alpha)
            ribbonColor = Color(0xFFC084FC).copy(alpha = alpha)
            crestColor = Color(0xFFFDE68A).copy(alpha = alpha)
            gemColor = Color(0xFFFB7185).copy(alpha = alpha)
        }
        MlbbChestStyle.EMERALD_RUBY_RIBBON -> {
            boxTop = Color(0xFF22C55E).copy(alpha = alpha)
            boxBottom = Color(0xFF14532D).copy(alpha = alpha)
            trimColor = Color(0xFFE2E8F0).copy(alpha = alpha)
            ribbonColor = Color(0xFFBBF7D0).copy(alpha = alpha)
            crestColor = Color(0xFFF8FAFC).copy(alpha = alpha)
            gemColor = Color(0xFFEF4444).copy(alpha = alpha)
        }
        MlbbChestStyle.SAPPHIRE_RUBY_RIBBON -> {
            boxTop = Color(0xFF2563EB).copy(alpha = alpha)
            boxBottom = Color(0xFF1E3A8A).copy(alpha = alpha)
            trimColor = Color(0xFFDCE7FF).copy(alpha = alpha)
            ribbonColor = Color(0xFF93C5FD).copy(alpha = alpha)
            crestColor = Color(0xFFF1F5F9).copy(alpha = alpha)
            gemColor = Color(0xFFEF4444).copy(alpha = alpha)
        }
        MlbbChestStyle.FOREST_BRONZE_CREST -> {
            boxTop = Color(0xFF16A34A).copy(alpha = alpha)
            boxBottom = Color(0xFF7F1D1D).copy(alpha = alpha)
            trimColor = Color(0xFFD97706).copy(alpha = alpha)
            ribbonColor = Color(0xFFFBBF24).copy(alpha = alpha)
            crestColor = Color(0xFFFDE68A).copy(alpha = alpha)
            gemColor = Color(0xFF991B1B).copy(alpha = alpha)
        }
        else -> { // MINT_VISOR_RIBBON
            boxTop = Color(0xFF10B981).copy(alpha = alpha)
            boxBottom = Color(0xFF064E3B).copy(alpha = alpha)
            trimColor = Color(0xFFA7F3D0).copy(alpha = alpha)
            ribbonColor = Color(0xFFE0F2FE).copy(alpha = alpha)
            crestColor = Color(0xFFF8FAFC).copy(alpha = alpha)
            gemColor = Color(0xFF06B6D4).copy(alpha = alpha)
        }
    }

    // Box base
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(boxTop, boxBottom)),
        topLeft = Offset(w * 0.17f, h * 0.42f),
        size = Size(w * 0.66f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
    )
    drawRoundRect(
        color = trimColor,
        topLeft = Offset(w * 0.17f, h * 0.42f),
        size = Size(w * 0.66f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
        style = Stroke(width = w * 0.035f)
    )

    // Lid + Ribbon Bow
    rotate(degrees = if (opened) -34f else 0f, pivot = Offset(w * 0.16f, h * 0.42f)) {
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(boxTop, boxBottom)),
            topLeft = Offset(w * 0.14f, h * 0.31f),
            size = Size(w * 0.72f, h * 0.13f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )
        drawRoundRect(
            color = trimColor,
            topLeft = Offset(w * 0.14f, h * 0.31f),
            size = Size(w * 0.72f, h * 0.13f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
            style = Stroke(width = w * 0.03f)
        )
        // Satin Bow Loops
        drawOval(
            color = ribbonColor,
            topLeft = Offset(w * 0.25f, h * 0.16f),
            size = Size(w * 0.24f, h * 0.16f)
        )
        drawOval(
            color = ribbonColor,
            topLeft = Offset(w * 0.51f, h * 0.16f),
            size = Size(w * 0.24f, h * 0.16f)
        )
        drawCircle(color = trimColor, radius = w * 0.06f, center = Offset(cx, h * 0.26f))
    }

    // Center shield crest + ruby/visor gem
    val shield = Path().apply {
        moveTo(w * 0.34f, h * 0.49f)
        lineTo(w * 0.66f, h * 0.49f)
        lineTo(w * 0.62f, h * 0.73f)
        lineTo(cx, h * 0.84f)
        lineTo(w * 0.38f, h * 0.73f)
        close()
    }
    drawPath(shield, color = crestColor)
    drawPath(shield, color = boxBottom, style = Stroke(width = w * 0.024f, join = StrokeJoin.Round))
    drawCircle(color = gemColor, radius = w * 0.068f, center = Offset(cx, h * 0.63f))
}

private fun DrawScope.drawGoldStrap(
    x: Float,
    topY: Float,
    bottomY: Float,
    strapWidth: Float,
    goldLight: Color,
    goldDark: Color
) {
    drawRect(
        brush = Brush.verticalGradient(listOf(goldLight, goldDark)),
        topLeft = Offset(x - strapWidth * 0.5f, topY),
        size = Size(strapWidth, bottomY - topY)
    )
}

