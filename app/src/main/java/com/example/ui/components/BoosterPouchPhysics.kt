package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.example.model.PackSatinTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure math & geometry model for physical foil booster pack girth, pinched-waist heat-crimped
 * top/bottom seals, volumetric 3D pillow-prism side gussets, anisotropic satin/foil specular
 * shading, and rotational settle physics.
 */
data class BoosterPackLightingState(
    val specularCenterU: Float,
    val specularIntensity: Float,
    val fresnelRimAlpha: Float,
    val girthSideRatio: Float,
    val visibleSideIsRight: Boolean,
    val crimpGlintShift: Float
)

data class BoosterPack3DPoint(
    val x: Float,
    val y: Float,
    val z: Float
)

data class BoosterPackFacePlacement(
    val translationX: Float,
    val translationY: Float,
    val zOrder: Float,
    val scaleFactor: Float
)

object BoosterPouchPhysics {
    const val PACK_ASPECT_RATIO = 1.88f
    const val TOP_CRIMP_FRAC = 0.095f
    const val BOTTOM_CRIMP_FRAC = 0.905f
    const val BELLY_START_FRAC = 0.165f
    const val BELLY_END_FRAC = 0.835f
    const val BELLY_HALF_DEPTH_RATIO = 0.044f
    const val CRIMP_HALF_DEPTH_RATIO = 0.0045f

    /**
     * Returns normalized half-width inset fraction from outer edge at normalized height v in [0, 1].
     * Flat top & bottom crimp fins flare slightly wider (small inset), pinch inward cleanly at the
     * waist (~13% & ~87%), and swell gently across the elongated card cavity.
     */
    fun pouchSideInsetFraction(vNorm: Float): Float {
        val v = vNorm.coerceIn(0f, 1f)
        return when {
            v <= TOP_CRIMP_FRAC -> 0.014f
            v >= BOTTOM_CRIMP_FRAC -> 0.014f
            v < BELLY_START_FRAC -> {
                val t = (v - TOP_CRIMP_FRAC) / (BELLY_START_FRAC - TOP_CRIMP_FRAC)
                val pinch = sin(t * PI).toFloat() * 0.036f
                0.014f + pinch * (1f - t * 0.65f)
            }
            v > BELLY_END_FRAC -> {
                val t = (1f - v - (1f - BOTTOM_CRIMP_FRAC)) / ((1f - BELLY_END_FRAC) - (1f - BOTTOM_CRIMP_FRAC))
                val pinch = sin(t * PI).toFloat() * 0.036f
                0.014f + pinch * (1f - t * 0.65f)
            }
            else -> {
                val bellyT = (v - BELLY_START_FRAC) / (BELLY_END_FRAC - BELLY_START_FRAC)
                val swell = sin(bellyT * PI).toFloat()
                0.014f * (1f - swell)
            }
        }
    }

    /**
     * Returns the sleek 3D half-depth (Z thickness / 2 as a fraction of pack width) at normalized
     * vertical coordinate [vNorm] in [0, 1]. Thin heat-crimped seals at top/bottom (~0.0045w) swell
     * cleanly into a slim, realistic 5-card pillow pouch (~0.044w half-depth).
     */
    fun bellyHalfDepthFraction(vNorm: Float): Float {
        val v = vNorm.coerceIn(0f, 1f)
        return when {
            v <= TOP_CRIMP_FRAC -> CRIMP_HALF_DEPTH_RATIO
            v >= BOTTOM_CRIMP_FRAC -> CRIMP_HALF_DEPTH_RATIO
            v < BELLY_START_FRAC -> {
                val t = ((v - TOP_CRIMP_FRAC) / (BELLY_START_FRAC - TOP_CRIMP_FRAC)).coerceIn(0f, 1f)
                val smooth = t * t * (3f - 2f * t)
                CRIMP_HALF_DEPTH_RATIO + (BELLY_HALF_DEPTH_RATIO * 0.68f - CRIMP_HALF_DEPTH_RATIO) * smooth
            }
            v > BELLY_END_FRAC -> {
                val t = ((BOTTOM_CRIMP_FRAC - v) / (BOTTOM_CRIMP_FRAC - BELLY_END_FRAC)).coerceIn(0f, 1f)
                val smooth = t * t * (3f - 2f * t)
                CRIMP_HALF_DEPTH_RATIO + (BELLY_HALF_DEPTH_RATIO * 0.68f - CRIMP_HALF_DEPTH_RATIO) * smooth
            }
            else -> {
                val bellyT = ((v - BELLY_START_FRAC) / (BELLY_END_FRAC - BELLY_START_FRAC)).coerceIn(0f, 1f)
                val arc = sin(bellyT * PI).toFloat()
                BELLY_HALF_DEPTH_RATIO * (0.68f + 0.32f * arc)
            }
        }
    }

    /**
     * Returns 3D half-depth (as a fraction of pack width) at surface `(uNorm, vNorm)` for a real
     * 3D pillow-sculpted foil wrapper: flat over the internal card stack, smoothly curved shoulders,
     * and micro-corrugated vertical ridges along the top/bottom heat-crimped seals.
     */
    fun pouchMeshHalfDepthFraction(uNorm: Float, vNorm: Float): Float {
        val u = uNorm.coerceIn(0f, 1f)
        val v = vNorm.coerceIn(0f, 1f)
        val vDepth = bellyHalfDepthFraction(v)
        val s = abs(2f * u - 1f).coerceIn(0f, 1f)
        // Super-elliptical pillow cross-section wrapping the card deck
        val pillow = (1f - s.pow(2.5f)).coerceAtLeast(0f).pow(0.55f)
        val edgeLip = 0.14f
        val baseHalfDepth = vDepth * (edgeLip + (1f - edgeLip) * pillow)

        // Subtle physical vertical foil crimp corrugation on top/bottom seals
        val crimpFactor = when {
            v <= TOP_CRIMP_FRAC -> 1f
            v >= BOTTOM_CRIMP_FRAC -> 1f
            else -> 0f
        }
        val corrugation = crimpFactor * 0.0022f * cos(u * 26.0 * PI).toFloat()
        return (baseHalfDepth + corrugation).coerceAtLeast(0.002f)
    }

    /**
     * Extra Z offset (fraction of pack width) for the folded vertical heat-seal fin on the back face.
     */
    fun pouchBackCenterFinFraction(uNorm: Float, vNorm: Float): Float {
        val v = vNorm.coerceIn(0f, 1f)
        if (v <= TOP_CRIMP_FRAC || v >= BOTTOM_CRIMP_FRAC) return 0f
        val distFromSpine = abs(uNorm - 0.5f)
        return if (distFromSpine < 0.08f) {
            val spineBell = (1f - distFromSpine / 0.08f)
            0.009f * spineBell * spineBell
        } else 0f
    }

    /**
     * Projects a pack-local 3D point `(xLocal, yLocal, zLocal)` into view space using the same
     * right-handed rotation convention as Compose `graphicsLayer(rotationY = yawDeg, rotationX = pitchDeg)`.
     */
    fun projectPackPoint3D(
        xLocal: Float,
        yLocal: Float,
        zLocal: Float,
        yawDeg: Float,
        pitchDeg: Float,
        widthPx: Float,
        heightPx: Float,
        cameraDistancePx: Float
    ): BoosterPack3DPoint {
        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())
        val cosY = cos(yawRad).toFloat()
        val sinY = sin(yawRad).toFloat()
        val cosP = cos(pitchRad).toFloat()
        val sinP = sin(pitchRad).toFloat()

        // Rotate around Y (yaw)
        val x1 = xLocal * cosY + zLocal * sinY
        val z1 = -xLocal * sinY + zLocal * cosY

        // Rotate around X (pitch)
        val y2 = yLocal * cosP - z1 * sinP
        val z2 = yLocal * sinP + z1 * cosP

        val safeCam = cameraDistancePx.coerceAtLeast(100f)
        val persp = (safeCam / (safeCam - z2).coerceAtLeast(40f)).coerceIn(0.65f, 1.45f)
        return BoosterPack3DPoint(
            x = widthPx * 0.5f + x1 * persp,
            y = heightPx * 0.5f + y2 * persp,
            z = z2
        )
    }

    /**
     * Computes center placement & depth order for front (`isFront = true`) or back face plane
     * separated by `±halfDepthPx` along the pack normal so rotating 90 deg exposes full 3D depth.
     */
    fun computeFacePlacement(
        isFront: Boolean,
        yawDeg: Float,
        pitchDeg: Float,
        halfDepthPx: Float,
        cameraDistancePx: Float
    ): BoosterPackFacePlacement {
        val signedZ = if (isFront) halfDepthPx else -halfDepthPx
        val proj = projectPackPoint3D(
            xLocal = 0f,
            yLocal = 0f,
            zLocal = signedZ,
            yawDeg = yawDeg,
            pitchDeg = pitchDeg,
            widthPx = 0f,
            heightPx = 0f,
            cameraDistancePx = cameraDistancePx
        )
        val safeCam = cameraDistancePx.coerceAtLeast(100f)
        val persp = (safeCam / (safeCam - proj.z).coerceAtLeast(40f)).coerceIn(0.84f, 1.18f)
        return BoosterPackFacePlacement(
            translationX = proj.x,
            translationY = proj.y,
            zOrder = proj.z,
            scaleFactor = persp
        )
    }

    /**
     * Computes anisotropic satin/foil surface lighting & 3D girth extrusion ratio from pack angles.
     */
    fun computeLightingState(
        totalYawDeg: Float,
        totalPitchDeg: Float,
        tiltRollDeg: Float,
        tiltPitchDeg: Float
    ): BoosterPackLightingState {
        val yawRad = Math.toRadians(totalYawDeg.toDouble())
        val pitchRad = Math.toRadians(totalPitchDeg.toDouble())
        val rollInput = Math.toRadians((tiltRollDeg * 1.45f).toDouble())
        val pitchInput = Math.toRadians((tiltPitchDeg * 1.35f).toDouble())

        val lx = (sin(yawRad * 0.72 + rollInput * 0.85)).toFloat().coerceIn(-0.95f, 0.95f)
        val ly = (sin(pitchRad * 0.8 - pitchInput * 0.65) - 0.24).toFloat().coerceIn(-0.95f, 0.95f)
        val lz = sqrt((1f - (lx * lx * 0.55f + ly * ly * 0.35f)).coerceIn(0.15f, 1f))

        val specU = (0.5f + lx * 0.42f).coerceIn(0.06f, 0.94f)
        val specIntensity = (0.34f + 0.44f * abs(lx) + 0.20f * (1f - lz)).coerceIn(0.28f, 0.88f)
        val fresnel = (0.24f + 0.42f * abs(sin(yawRad)).toFloat().pow(0.75f)).coerceIn(0.20f, 0.68f)

        val sinYaw = sin(yawRad).toFloat()
        return BoosterPackLightingState(
            specularCenterU = specU,
            specularIntensity = specIntensity,
            fresnelRimAlpha = fresnel,
            girthSideRatio = abs(sinYaw).coerceIn(0f, 1f),
            visibleSideIsRight = sinYaw >= 0f,
            crimpGlintShift = ((lx * 0.5f + 0.5f)).coerceIn(0f, 1f)
        )
    }

    /**
     * Given current yaw and angular drag velocity (deg/s), computes the inertial snap target
     * (multiples of 180 deg so quick flicks cleanly complete a full 180 flip instead of snapping back).
     */
    fun nearestSettledFaceYaw(currentYawDeg: Float, yawVelocityDegPerSec: Float): Float {
        val projectedYaw = currentYawDeg + (yawVelocityDegPerSec * 0.20f).coerceIn(-210f, 210f)
        val halfTurns = (projectedYaw / 180f).roundToInt()
        return halfTurns * 180f
    }

    /**
     * Builds the physical booster pouch silhouette path with flared flat crimp fins at top/bottom,
     * pinched waist creases, tiny V tear notches on upper-left & upper-right shoulders, and convex belly.
     */
    fun buildPouchOutlinePath(size: Size, includeTearNotches: Boolean = true): Path {
        val w = size.width
        val h = size.height
        val cornerR = (w * 0.045f).coerceIn(5f, 14f)
        val topH = h * TOP_CRIMP_FRAC
        val botY = h * BOTTOM_CRIMP_FRAC
        val bellyTop = h * BELLY_START_FRAC
        val bellyBot = h * BELLY_END_FRAC
        val edgeInset = w * 0.014f
        val pinchInset = w * 0.044f

        return Path().apply {
            moveTo(edgeInset + cornerR, 0f)
            lineTo(w - edgeInset - cornerR, 0f)
            quadraticTo(w - edgeInset, 0f, w - edgeInset, cornerR)

            lineTo(w - edgeInset, topH * 0.72f)
            if (includeTearNotches) {
                lineTo(w - edgeInset - w * 0.022f, topH * 0.86f)
                lineTo(w - edgeInset, topH)
            } else {
                lineTo(w - edgeInset, topH)
            }
            cubicTo(
                w - pinchInset * 1.05f, topH + (bellyTop - topH) * 0.45f,
                w - pinchInset * 0.30f, topH + (bellyTop - topH) * 0.85f,
                w - edgeInset * 0.4f, bellyTop
            )
            cubicTo(
                w + w * 0.008f, h * 0.36f,
                w + w * 0.008f, h * 0.64f,
                w - edgeInset * 0.4f, bellyBot
            )
            cubicTo(
                w - pinchInset * 0.30f, bellyBot + (botY - bellyBot) * 0.15f,
                w - pinchInset * 1.05f, bellyBot + (botY - bellyBot) * 0.55f,
                w - edgeInset, botY
            )
            lineTo(w - edgeInset, h - cornerR)
            quadraticTo(w - edgeInset, h, w - edgeInset - cornerR, h)

            lineTo(edgeInset + cornerR, h)
            quadraticTo(edgeInset, h, edgeInset, h - cornerR)

            lineTo(edgeInset, botY)
            cubicTo(
                pinchInset * 1.05f, bellyBot + (botY - bellyBot) * 0.55f,
                pinchInset * 0.30f, bellyBot + (botY - bellyBot) * 0.15f,
                edgeInset * 0.4f, bellyBot
            )
            cubicTo(
                -w * 0.008f, h * 0.64f,
                -w * 0.008f, h * 0.36f,
                edgeInset * 0.4f, bellyTop
            )
            cubicTo(
                pinchInset * 0.30f, topH + (bellyTop - topH) * 0.85f,
                pinchInset * 1.05f, topH + (bellyTop - topH) * 0.45f,
                edgeInset, topH
            )
            if (includeTearNotches) {
                lineTo(edgeInset + w * 0.022f, topH * 0.86f)
                lineTo(edgeInset, topH * 0.72f)
            }
            lineTo(edgeInset, cornerR)
            quadraticTo(edgeInset, 0f, edgeInset + cornerR, 0f)
            close()
        }
    }
}

val BoosterPouchShape = GenericShape { size, _ ->
    addPath(BoosterPouchPhysics.buildPouchOutlinePath(size, includeTearNotches = true))
}

/**
 * Renders true volumetric 3D pillow-prism side gussets, pinched crimp caps, and laminated
 * inner card-stack thickness between the separated front (`+Z`) and back (`-Z`) pack faces.
 * Drawn OUTSIDE Compose's flat `rotationY` transform so it remains solid and plump even at 90°/270°.
 */
@Composable
fun BoosterPack3DPrismShell(
    satinTheme: PackSatinTheme,
    yawDeg: Float,
    pitchDeg: Float,
    cameraDistancePx: Float,
    lighting: BoosterPackLightingState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 4f || h <= 4f) return@Canvas

        val slices = 18
        fun drawSideWallRibbon(isRightEdge: Boolean) {
            val frontPts = ArrayList<BoosterPack3DPoint>(slices + 1)
            val backPts = ArrayList<BoosterPack3DPoint>(slices + 1)
            val midPts = ArrayList<BoosterPack3DPoint>(slices + 1)

            for (i in 0..slices) {
                val v = i.toFloat() / slices.toFloat()
                val inset = BoosterPouchPhysics.pouchSideInsetFraction(v)
                val xNorm = if (isRightEdge) (0.5f - inset) else (-0.5f + inset)
                val xLocal = xNorm * w
                val yLocal = (v - 0.5f) * h
                val zHalf = BoosterPouchPhysics.bellyHalfDepthFraction(v) * w

                frontPts += BoosterPouchPhysics.projectPackPoint3D(
                    xLocal, yLocal, +zHalf, yawDeg, pitchDeg, w, h, cameraDistancePx
                )
                backPts += BoosterPouchPhysics.projectPackPoint3D(
                    xLocal, yLocal, -zHalf, yawDeg, pitchDeg, w, h, cameraDistancePx
                )
                midPts += BoosterPouchPhysics.projectPackPoint3D(
                    xLocal, yLocal, 0f, yawDeg, pitchDeg, w, h, cameraDistancePx
                )
            }

            val ribbon = Path().apply {
                moveTo(frontPts.first().x, frontPts.first().y)
                for (i in 1..slices) lineTo(frontPts[i].x, frontPts[i].y)
                for (i in slices downTo 0) lineTo(backPts[i].x, backPts[i].y)
                close()
            }

            drawPath(
                path = ribbon,
                brush = Brush.verticalGradient(
                    0.0f to satinTheme.baseColor,
                    0.16f to satinTheme.logoAccent.copy(alpha = 0.85f),
                    0.50f to satinTheme.sheenHighlight.copy(alpha = 0.72f),
                    0.84f to satinTheme.logoAccent.copy(alpha = 0.85f),
                    1.0f to satinTheme.baseColor
                )
            )
            drawPath(
                path = ribbon,
                color = satinTheme.crestTint.copy(alpha = 0.60f),
                style = Stroke(width = 1.6f, join = StrokeJoin.Round)
            )

            // Visible inner laminated foil seam / stacked card-edge highlight along belly center
            val seam = Path().apply {
                moveTo(midPts[2].x, midPts[2].y)
                for (i in 3..(slices - 2)) {
                    lineTo(midPts[i].x, midPts[i].y)
                }
            }
            drawPath(
                path = seam,
                color = satinTheme.crestTint.copy(alpha = 0.88f * lighting.fresnelRimAlpha.coerceAtLeast(0.45f)),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Top & bottom thin heat-crimped foil edge caps connecting front & back flanges
        fun drawEndCap(vNorm: Float) {
            val inset = BoosterPouchPhysics.pouchSideInsetFraction(vNorm)
            val zHalf = BoosterPouchPhysics.bellyHalfDepthFraction(vNorm) * w
            val yLocal = (vNorm - 0.5f) * h
            val xL = (-0.5f + inset) * w
            val xR = (0.5f - inset) * w

            val fl = BoosterPouchPhysics.projectPackPoint3D(xL, yLocal, +zHalf, yawDeg, pitchDeg, w, h, cameraDistancePx)
            val fr = BoosterPouchPhysics.projectPackPoint3D(xR, yLocal, +zHalf, yawDeg, pitchDeg, w, h, cameraDistancePx)
            val br = BoosterPouchPhysics.projectPackPoint3D(xR, yLocal, -zHalf, yawDeg, pitchDeg, w, h, cameraDistancePx)
            val bl = BoosterPouchPhysics.projectPackPoint3D(xL, yLocal, -zHalf, yawDeg, pitchDeg, w, h, cameraDistancePx)

            val cap = Path().apply {
                moveTo(fl.x, fl.y)
                lineTo(fr.x, fr.y)
                lineTo(br.x, br.y)
                lineTo(bl.x, bl.y)
                close()
            }
            drawPath(
                path = cap,
                brush = Brush.horizontalGradient(
                    listOf(satinTheme.logoAccent, satinTheme.crestTint, satinTheme.logoAccent)
                )
            )
        }

        val sinY = sin(Math.toRadians(yawDeg.toDouble())).toFloat()
        // Draw far wall first, then end caps, then near-facing side gusset wall
        drawSideWallRibbon(isRightEdge = sinY < 0f)
        drawEndCap(0f)
        drawEndCap(1f)
        drawSideWallRibbon(isRightEdge = sinY >= 0f)
    }
}

/**
 * Renders the physical satin/foil material lighting, plump convex girth shadow, pinched waist creases,
 * and vertical heat-crimped seal ribs over any booster pack face.
 */
@Composable
fun BoosterPouchMaterialOverlay(
    satinTheme: PackSatinTheme,
    lighting: BoosterPackLightingState,
    modifier: Modifier = Modifier,
    showTopCrimp: Boolean = true,
    showBottomCrimp: Boolean = true,
    crimpRibDensity: Int = 28
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 2f || h <= 2f) return@Canvas

        val topSealY = h * BoosterPouchPhysics.TOP_CRIMP_FRAC
        val botSealY = h * BoosterPouchPhysics.BOTTOM_CRIMP_FRAC
        val outline = BoosterPouchPhysics.buildPouchOutlinePath(size, includeTearNotches = true)

        clipPath(outline) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0.0f to Color.Black.copy(alpha = 0.36f),
                    0.10f to Color.Black.copy(alpha = 0.14f),
                    0.26f to Color.Transparent,
                    0.50f to satinTheme.sheenHighlight.copy(alpha = 0.12f),
                    0.74f to Color.Transparent,
                    0.90f to Color.Black.copy(alpha = 0.14f),
                    1.0f to Color.Black.copy(alpha = 0.36f)
                )
            )

            val specCenter = w * lighting.specularCenterU
            val bandHalfW = w * 0.34f
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        satinTheme.sheenHighlight.copy(alpha = 0.16f * lighting.specularIntensity),
                        Color.White.copy(alpha = 0.36f * lighting.specularIntensity),
                        satinTheme.sheenHighlight.copy(alpha = 0.22f * lighting.specularIntensity),
                        Color.Transparent
                    ),
                    startX = specCenter - bandHalfW,
                    endX = specCenter + bandHalfW
                ),
                blendMode = BlendMode.Screen
            )

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        satinTheme.logoAccent.copy(alpha = 0.14f * lighting.fresnelRimAlpha),
                        Color.White.copy(alpha = 0.16f * lighting.specularIntensity),
                        Color.Transparent
                    ),
                    start = Offset(w * (lighting.specularCenterU - 0.5f), 0f),
                    end = Offset(w * (lighting.specularCenterU + 0.5f), h)
                ),
                blendMode = BlendMode.Plus
            )

            if (showTopCrimp) {
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.22f),
                        0.55f to satinTheme.sheenHighlight.copy(alpha = 0.22f),
                        1.0f to Color.Transparent
                    ),
                    topLeft = Offset(0f, topSealY - h * 0.01f),
                    size = Size(w, h * 0.068f)
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            satinTheme.sheenHighlight.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.72f),
                            satinTheme.sheenHighlight.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(w * 0.04f, topSealY),
                    end = Offset(w * 0.96f, topSealY),
                    strokeWidth = (h * 0.006f).coerceIn(1.2f, 2.8f),
                    cap = StrokeCap.Round
                )

                drawRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, topSealY)
                )
                val step = w / crimpRibDensity.toFloat()
                for (i in 1 until crimpRibDensity) {
                    val rx = i * step
                    val normX = i.toFloat() / crimpRibDensity.toFloat()
                    val glintDist = 1f - abs(normX - lighting.crimpGlintShift).coerceIn(0f, 1f)
                    val ribAlpha = (0.16f + 0.34f * glintDist.pow(2f)).coerceIn(0.12f, 0.55f)
                    drawLine(
                        color = satinTheme.sheenHighlight.copy(alpha = ribAlpha),
                        start = Offset(rx, 0f),
                        end = Offset(rx, topSealY - 1f),
                        strokeWidth = (step * 0.34f).coerceIn(1f, 2.6f)
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.34f),
                        start = Offset(rx + step * 0.42f, 0f),
                        end = Offset(rx + step * 0.42f, topSealY - 1f),
                        strokeWidth = (step * 0.26f).coerceIn(0.8f, 2.0f)
                    )
                }
            }

            if (showBottomCrimp) {
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.45f to satinTheme.sheenHighlight.copy(alpha = 0.20f),
                        1.0f to Color.Black.copy(alpha = 0.26f)
                    ),
                    topLeft = Offset(0f, botSealY - h * 0.058f),
                    size = Size(w, h * 0.068f)
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            satinTheme.sheenHighlight.copy(alpha = 0.62f),
                            Color.White.copy(alpha = 0.68f),
                            satinTheme.sheenHighlight.copy(alpha = 0.62f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(w * 0.04f, botSealY),
                    end = Offset(w * 0.96f, botSealY),
                    strokeWidth = (h * 0.006f).coerceIn(1.2f, 2.8f),
                    cap = StrokeCap.Round
                )

                drawRect(
                    color = Color.Black.copy(alpha = 0.24f),
                    topLeft = Offset(0f, botSealY),
                    size = Size(w, h - botSealY)
                )
                val step = w / crimpRibDensity.toFloat()
                for (i in 1 until crimpRibDensity) {
                    val rx = i * step
                    val normX = i.toFloat() / crimpRibDensity.toFloat()
                    val glintDist = 1f - abs(normX - lighting.crimpGlintShift).coerceIn(0f, 1f)
                    val ribAlpha = (0.16f + 0.34f * glintDist.pow(2f)).coerceIn(0.12f, 0.55f)
                    drawLine(
                        color = satinTheme.sheenHighlight.copy(alpha = ribAlpha),
                        start = Offset(rx, botSealY + 1f),
                        end = Offset(rx, h),
                        strokeWidth = (step * 0.34f).coerceIn(1f, 2.6f)
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.34f),
                        start = Offset(rx + step * 0.42f, botSealY + 1f),
                        end = Offset(rx + step * 0.42f, h),
                        strokeWidth = (step * 0.26f).coerceIn(0.8f, 2.0f)
                    )
                }
            }

            drawPath(
                path = outline,
                brush = Brush.linearGradient(
                    colors = listOf(
                        satinTheme.sheenHighlight.copy(alpha = 0.85f),
                        satinTheme.logoAccent.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.72f * lighting.fresnelRimAlpha),
                        satinTheme.sheenHighlight.copy(alpha = 0.85f)
                    )
                ),
                style = Stroke(width = (w * 0.012f).coerceIn(1.4f, 3.2f))
            )
        }
    }
}
