package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas as NativeCanvas
import android.graphics.LinearGradient
import android.graphics.Paint as NativePaint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.model.MegaPackConfig
import com.example.model.PackSatinTheme
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pure helper computing projected shadow half-width as a function of pack yaw.
 * Scales smoothly from full pack width at 0/180 deg to a thin edge-on sliver at 90/270 deg.
 */
internal fun computePackShadowHalfWidth(packWidthPx: Float, yawDeg: Float): Float {
    val yawRad = Math.toRadians(yawDeg.toDouble())
    val cosAbs = abs(cos(yawRad)).toFloat()
    val sinAbs = abs(sin(yawRad)).toFloat()
    // Thin sliver only: the shadow is the pack's edge-on thickness, never its full width.
    return packWidthPx * (0.010f + 0.026f * sinAbs)
}

/** Shadow strength: invisible when the pack faces the camera, only a faint line once it turns side-on. */
internal fun computePackShadowStrength(yawDeg: Float): Float {
    val sinAbs = abs(sin(Math.toRadians(yawDeg.toDouble()))).toFloat()
    val t = ((sinAbs - 0.30f) / 0.70f).coerceIn(0f, 1f)
    return t * t
}

/**
 * Cached texture set & contoured silhouette bounds for a true 3D tessellated booster pack mesh.
 */
data class PackMeshTextures(
    val frontBitmap: Bitmap,
    val backBitmap: Bitmap,
    val alphaMaskBitmap: Bitmap,
    val backAlphaMaskBitmap: Bitmap,
    val sideFoilBitmap: Bitmap,
    val innerFoilBitmap: Bitmap,
    val opaqueWhiteBitmap: Bitmap,
    val frontShader: BitmapShader,
    val backShader: BitmapShader,
    val alphaShader: BitmapShader,
    val backAlphaShader: BitmapShader,
    val sideShader: BitmapShader,
    val innerFoilShader: BitmapShader,
    val opaqueWhiteShader: BitmapShader,
    val rowLeftEdgeU: FloatArray,
    val rowRightEdgeU: FloatArray
)

object PackMeshTextureCache {
    private val cache = ConcurrentHashMap<String, PackMeshTextures>()
    private const val TARGET_W = 360
    private const val TARGET_H = 676
    const val MESH_COLS = 14
    const val MESH_ROWS = 24

    fun getOrNull(coverRes: Int, theme: PackSatinTheme, packCount: Int): PackMeshTextures? =
        cache["${coverRes}_${theme.id}_$packCount"]

    suspend fun getOrBuild(
        context: Context,
        @DrawableRes coverRes: Int,
        theme: PackSatinTheme,
        packCount: Int
    ): PackMeshTextures {
        val key = "${coverRes}_${theme.id}_$packCount"
        cache[key]?.let { return it }

        val built = withContext(Dispatchers.Default) {
            cache[key]?.let { return@withContext it }
            buildTexturesSync(context.applicationContext, coverRes, theme, packCount)
        }
        cache.putIfAbsent(key, built)
        return cache[key] ?: built
    }

    private fun buildTexturesSync(
        context: Context,
        @DrawableRes coverRes: Int,
        theme: PackSatinTheme,
        packCount: Int
    ): PackMeshTextures {
        val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }
        val raw = BitmapFactory.decodeResource(context.resources, coverRes, opts)
        val trimmed = trimTransparentBounds(raw)
        if (trimmed !== raw) raw.recycle()

        val frontBmp = Bitmap.createScaledBitmap(trimmed, TARGET_W, TARGET_H, true)
        if (trimmed !== frontBmp) trimmed.recycle()

        if (theme.id != "obsidian_royale") {
            val c = NativeCanvas(frontBmp)
            val overlayPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, TARGET_W.toFloat(), TARGET_H.toFloat(),
                    intArrayOf(
                        theme.baseColor.copy(alpha = 0.24f).toArgb(),
                        theme.sheenHighlight.copy(alpha = 0.14f).toArgb(),
                        theme.logoAccent.copy(alpha = 0.22f).toArgb()
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            }
            c.drawRect(0f, 0f, TARGET_W.toFloat(), TARGET_H.toFloat(), overlayPaint)
        }

        // Scan opaque edge bounds (>= 210 alpha, nudged 1px inward) so front/side edges join cleanly
        val pixels = IntArray(TARGET_W * TARGET_H)
        frontBmp.getPixels(pixels, 0, TARGET_W, 0, 0, TARGET_W, TARGET_H)

        val maskPixels = IntArray(TARGET_W * TARGET_H)
        for (i in pixels.indices) {
            val a = (pixels[i] ushr 24) and 0xFF
            maskPixels[i] = (a shl 24) or 0x00FFFFFF
        }
        val alphaMaskBmp = Bitmap.createBitmap(TARGET_W, TARGET_H, Bitmap.Config.ARGB_8888).apply {
            setPixels(maskPixels, 0, TARGET_W, 0, 0, TARGET_W, TARGET_H)
        }

        val leftU = FloatArray(MESH_ROWS + 1)
        val rightU = FloatArray(MESH_ROWS + 1)
        for (row in 0..MESH_ROWS) {
            val py = ((row.toFloat() / MESH_ROWS) * (TARGET_H - 1)).roundToInt().coerceIn(0, TARGET_H - 1)
            val rowOffset = py * TARGET_W
            var minX = TARGET_W - 1
            var maxX = 0
            for (x in 0 until TARGET_W) {
                val a = (pixels[rowOffset + x] ushr 24) and 0xFF
                if (a >= 210) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                }
            }
            if (maxX <= minX) {
                val inset = BoosterPouchPhysics.pouchSideInsetFraction(row.toFloat() / MESH_ROWS)
                leftU[row] = inset
                rightU[row] = 1f - inset
            } else {
                val safeMin = if (maxX - minX >= 6) minX + 1 else minX
                val safeMax = if (maxX - minX >= 6) maxX else (maxX + 1)
                leftU[row] = (safeMin.toFloat() / TARGET_W.toFloat()).coerceIn(0f, 0.20f)
                rightU[row] = (safeMax.toFloat() / TARGET_W.toFloat()).coerceIn(0.80f, 1f)
            }
        }

        // Mirrored copy of the front silhouette: the back is cut to exactly the same outline as the front
        val mirrorMatrix = android.graphics.Matrix().apply { preScale(-1f, 1f, TARGET_W / 2f, 0f) }
        val backAlphaMaskBmp = Bitmap.createBitmap(TARGET_W, TARGET_H, Bitmap.Config.ARGB_8888)
        NativeCanvas(backAlphaMaskBmp).drawBitmap(alphaMaskBmp, mirrorMatrix, null)

        val backBmp = renderBackSealBitmap(theme, packCount)
        NativeCanvas(backBmp).drawBitmap(
            backAlphaMaskBmp, 0f, 0f,
            NativePaint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }
        )
        val sideBmp = renderSideFoilBitmap(theme)
        val innerBmp = renderInnerFoilBitmap()
        val whiteBmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.WHITE)
        }

        return PackMeshTextures(
            frontBitmap = frontBmp,
            backBitmap = backBmp,
            alphaMaskBitmap = alphaMaskBmp,
            backAlphaMaskBitmap = backAlphaMaskBmp,
            sideFoilBitmap = sideBmp,
            innerFoilBitmap = innerBmp,
            opaqueWhiteBitmap = whiteBmp,
            frontShader = BitmapShader(frontBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            backShader = BitmapShader(backBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            alphaShader = BitmapShader(alphaMaskBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            backAlphaShader = BitmapShader(backAlphaMaskBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            sideShader = BitmapShader(sideBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            innerFoilShader = BitmapShader(innerBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            opaqueWhiteShader = BitmapShader(whiteBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            rowLeftEdgeU = leftU,
            rowRightEdgeU = rightU
        )
    }

    private fun trimTransparentBounds(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val px = IntArray(w * h)
        src.getPixels(px, 0, w, 0, 0, w, h)
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            val rowOff = y * w
            for (x in 0 until w) {
                val a = (px[rowOff + x] ushr 24) and 0xFF
                if (a >= 20) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX <= minX || maxY <= minY) return src
        if (minX <= 2 && minY <= 2 && maxX >= w - 3 && maxY >= h - 3) return src
        return Bitmap.createBitmap(src, minX, minY, maxX - minX + 1, maxY - minY + 1)
    }

    private fun renderSideFoilBitmap(theme: PackSatinTheme): Bitmap {
        val w = 64
        val h = 256
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = NativeCanvas(bmp)
        val base = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    theme.baseColor.toArgb(),
                    theme.logoAccent.toArgb(),
                    theme.crestTint.toArgb(),
                    theme.sheenHighlight.toArgb(),
                    theme.logoAccent.toArgb(),
                    theme.baseColor.toArgb()
                ),
                floatArrayOf(0f, 0.14f, 0.34f, 0.50f, 0.86f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), base)

        val seamPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = theme.crestTint.copy(alpha = 0.85f).toArgb()
            strokeWidth = 3f
        }
        c.drawLine(w * 0.5f, 18f, w * 0.5f, h - 18f, seamPaint)
        return bmp
    }

    private fun renderInnerFoilBitmap(): Bitmap {
        val w = 64
        val h = 128
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = NativeCanvas(bmp)
        val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    android.graphics.Color.rgb(218, 226, 236),
                    android.graphics.Color.rgb(122, 134, 152),
                    android.graphics.Color.rgb(32, 39, 54)
                ),
                floatArrayOf(0f, 0.38f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        return bmp
    }

    /**
     * Full-bleed 100% opaque back foil texture so back edges join cleanly into the V-pleat side gussets
     * without any translucent border seams or hairline gaps.
     */
    private fun renderBackSealBitmap(
        theme: PackSatinTheme,
        packCount: Int
    ): Bitmap {
        val w = TARGET_W.toFloat()
        val h = TARGET_H.toFloat()
        val bmp = Bitmap.createBitmap(TARGET_W, TARGET_H, Bitmap.Config.ARGB_8888)
        val c = NativeCanvas(bmp)

        val bgPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w, h,
                intArrayOf(
                    theme.baseColor.toArgb(),
                    Color(0xFF161026).toArgb(),
                    theme.logoAccent.copy(alpha = 0.56f).toArgb(),
                    theme.baseColor.toArgb()
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w, h, bgPaint)

        // Matching metallic foil side seam trim along left & right edges for watertight gusset blend
        val edgeTrimLeft = NativePaint().apply {
            shader = LinearGradient(
                0f, 0f, 14f, 0f,
                intArrayOf(theme.baseColor.toArgb(), theme.crestTint.copy(alpha = 0.55f).toArgb(), android.graphics.Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, 14f, h, edgeTrimLeft)
        val edgeTrimRight = NativePaint().apply {
            shader = LinearGradient(
                w - 14f, 0f, w, 0f,
                intArrayOf(android.graphics.Color.TRANSPARENT, theme.crestTint.copy(alpha = 0.55f).toArgb(), theme.baseColor.toArgb()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(w - 14f, 0f, w, h, edgeTrimRight)

        val beamPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.18f, w, h * 0.82f,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    theme.crestTint.copy(alpha = 0.24f).toArgb(),
                    theme.sheenHighlight.copy(alpha = 0.18f).toArgb(),
                    android.graphics.Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w, h, beamPaint)

        // Center folded heat-seal back fin spine
        val finW = 28f
        val finPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                w * 0.5f - finW, 0f, w * 0.5f + finW, 0f,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(135, 0, 0, 0),
                    theme.sheenHighlight.copy(alpha = 0.55f).toArgb(),
                    theme.crestTint.copy(alpha = 0.85f).toArgb(),
                    android.graphics.Color.argb(145, 0, 0, 0),
                    android.graphics.Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(w * 0.5f - finW, h * 0.09f, w * 0.5f + finW, h * 0.91f, finPaint)

        // Top & bottom crimp heat-seal ribs
        val crimpBandPaint = NativePaint().apply { color = android.graphics.Color.argb(110, 0, 0, 0) }
        c.drawRect(0f, 0f, w, h * BoosterPouchPhysics.TOP_CRIMP_FRAC, crimpBandPaint)
        c.drawRect(0f, h * BoosterPouchPhysics.BOTTOM_CRIMP_FRAC, w, h, crimpBandPaint)

        val ribLight = NativePaint().apply {
            color = theme.crestTint.copy(alpha = 0.48f).toArgb()
            strokeWidth = 3f
        }
        val ribDark = NativePaint().apply {
            color = android.graphics.Color.argb(150, 0, 0, 0)
            strokeWidth = 2.5f
        }
        for (i in 1..26) {
            val rx = i * (w / 27f)
            c.drawLine(rx, 0f, rx, h * BoosterPouchPhysics.TOP_CRIMP_FRAC, ribLight)
            c.drawLine(rx + 4f, 0f, rx + 4f, h * BoosterPouchPhysics.TOP_CRIMP_FRAC, ribDark)
            c.drawLine(rx, h * BoosterPouchPhysics.BOTTOM_CRIMP_FRAC, rx, h, ribLight)
            c.drawLine(rx + 4f, h * BoosterPouchPhysics.BOTTOM_CRIMP_FRAC, rx + 4f, h, ribDark)
        }

        val cardLeft = 36f
        val cardRight = w - 36f
        val cardTop = h * 0.25f
        val cardBot = h * 0.69f
        val panelPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(226, 11, 8, 22)
        }
        val panelBorder = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            style = NativePaint.Style.STROKE
            strokeWidth = 2.6f
            color = theme.crestTint.copy(alpha = 0.85f).toArgb()
        }
        val panelRect = RectF(cardLeft, cardTop, cardRight, cardBot)
        c.drawRoundRect(panelRect, 20f, 20f, panelPaint)
        c.drawRoundRect(panelRect, 20f, 20f, panelBorder)

        val titlePaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = theme.crestTint.toArgb()
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = NativePaint.Align.CENTER
        }
        c.drawText("MEGA COLLECTOR • ${MegaPackConfig.CARDS_PER_PACK} CARDS", w * 0.5f, h * 0.19f, titlePaint)

        val headerPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFFFACC15).toArgb()
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        c.drawText("CERTIFIED PULL ODDS", cardLeft + 22f, cardTop + 38f, headerPaint)

        val rowLabelPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFFE2E8F0).toArgb()
            textSize = 15.5f
        }
        val rowPctPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = theme.crestTint.toArgb()
            textSize = 15.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = NativePaint.Align.RIGHT
        }
        val oddsRows = listOf(
            "1-of-1 Misprint" to "0.005%",
            "Legend Tier" to "0.01%",
            "Collector Tier" to "1.0%",
            "Luckybox Tier" to "3.0%",
            "Epic Tier" to "8.0%",
            "Special Tier" to "19.0%",
            "Uncommon Tier" to "36.0%",
            "Common Tier" to "32.99%"
        )
        oddsRows.forEachIndexed { idx, (label, pct) ->
            val yPos = cardTop + 72f + idx * 28f
            c.drawText(label, cardLeft + 22f, yPos, rowLabelPaint)
            c.drawText(pct, cardRight - 22f, yPos, rowPctPaint)
        }

        val footerPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFFCBD5E1).toArgb()
            textSize = 13f
            textAlign = NativePaint.Align.CENTER
        }
        val badgeText = if (packCount > 1) "${packCount}x BUNDLE • ${theme.label.uppercase()}" else "MLBBDEX • ${theme.label.uppercase()}"
        c.drawText(badgeText, w * 0.5f, h * 0.76f, footerPaint)

        val barcodeLeft = w * 0.24f
        val barPaint = NativePaint().apply { color = Color(0xFFF1F5F9).toArgb() }
        val widths = listOf(4f, 8f, 3f, 6f, 4f, 9f, 3f, 5f, 8f, 3f, 6f, 4f, 3f, 8f, 5f)
        var bx = barcodeLeft
        widths.forEach { bw ->
            c.drawRect(bx, h * 0.79f, bx + bw, h * 0.845f, barPaint)
            bx += bw + 5f
        }
        return bmp
    }
}

private class MeshRenderBuffers(maxVerts: Int, maxTris: Int) {
    val rotX = FloatArray(maxVerts)
    val rotY = FloatArray(maxVerts)
    val rotZ = FloatArray(maxVerts)
    val projX = FloatArray(maxVerts)
    val projY = FloatArray(maxVerts)
    val nrmX = FloatArray(maxVerts)
    val nrmY = FloatArray(maxVerts)
    val nrmZ = FloatArray(maxVerts)
    val texU = FloatArray(maxVerts)
    val texV = FloatArray(maxVerts)
    val diffColor = IntArray(maxVerts)
    val specColor = IntArray(maxVerts)
    val matType = IntArray(maxVerts) // 0 = front, 1 = back, 2 = side, 3 = inner foil

    val triI0 = IntArray(maxTris)
    val triI1 = IntArray(maxTris)
    val triI2 = IntArray(maxTris)
    val triMat = IntArray(maxTris)
    val triDepth = FloatArray(maxTris)
    val sortOrder = IntArray(maxTris)

    val drawVerts = FloatArray(maxVerts * 2)
    val drawTexs = FloatArray(maxVerts * 2)
    val drawIndices = ShortArray(maxTris * 3)
}

private fun sampleEdgeU(edges: FloatArray, vNorm: Float): Float {
    val maxIdx = edges.lastIndex
    val scaled = (vNorm.coerceIn(0f, 1f) * maxIdx.toFloat())
    val i0 = scaled.toInt().coerceIn(0, maxIdx)
    val i1 = (i0 + 1).coerceAtMost(maxIdx)
    val frac = scaled - i0
    return edges[i0] + (edges[i1] - edges[i0]) * frac
}

/**
 * Renders a true hardware-accelerated 3D tessellated triangle mesh booster pack with curved pillow
 * belly deformation, micro-corrugated heat-crimped seals, folded V-pleat side gussets, back seal
 * spine, per-vertex Blinn-Phong metallic foil specular lighting, thin side-view drop shadow,
 * and genuine disconnected 2-piece foil cut tearing.
 */
@Composable
fun BoosterPack3DMesh(
    coverRes: Int,
    satinTheme: PackSatinTheme,
    packCount: Int,
    yawDeg: Float,
    pitchDeg: Float,
    rollDeg: Float = 0f,
    cameraDistancePx: Float,
    lighting: BoosterPackLightingState,
    tearProgress: Float = 0f,
    tearLeftYRatio: Float = 0.24f,
    tearRightYRatio: Float = 0.24f,
    tearPeelDirX: Float = 1f,
    tearPeelDirY: Float = -0.65f,
    tearAngleDeg: Float = 0f,
    pickUpProgress: Float = 0f,
    lowerPouchDropProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var textures by remember(coverRes, satinTheme.id, packCount) {
        mutableStateOf(PackMeshTextureCache.getOrNull(coverRes, satinTheme, packCount))
    }

    LaunchedEffect(coverRes, satinTheme.id, packCount) {
        if (textures == null) {
            textures = PackMeshTextureCache.getOrBuild(context, coverRes, satinTheme, packCount)
        }
    }

    val cols = PackMeshTextureCache.MESH_COLS
    val rows = PackMeshTextureCache.MESH_ROWS
    val maxVerts = 1400
    val maxTris = 2200

    val buf = remember { MeshRenderBuffers(maxVerts, maxTris) }
    val diffusePaint = remember {
        NativePaint(NativePaint.ANTI_ALIAS_FLAG or NativePaint.FILTER_BITMAP_FLAG)
    }
    val specPaint = remember {
        NativePaint(NativePaint.ANTI_ALIAS_FLAG or NativePaint.FILTER_BITMAP_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }
    }
    val shadowPaint = remember {
        NativePaint(NativePaint.ANTI_ALIAS_FLAG)
    }

    Canvas(modifier = modifier) {
        val tex = textures ?: return@Canvas
        val w = size.width
        val h = size.height
        if (w <= 4f || h <= 4f) return@Canvas

        val camDist = cameraDistancePx.coerceAtLeast(max(w, h) * 3.5f)
        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())
        val rollRad = Math.toRadians(rollDeg.toDouble())
        val cosY = cos(yawRad).toFloat()
        val sinY = sin(yawRad).toFloat()
        val cosP = cos(pitchRad).toFloat()
        val sinP = sin(pitchRad).toFloat()
        val cosR = cos(rollRad).toFloat()
        val sinR = sin(rollRad).toFloat()
        val cosAbs = abs(cosY)

        // True projected ground drop shadow: shrinks smoothly into a thin sliver when turned to the side
        val shadowLift = pickUpProgress.coerceIn(0f, 1f)
        val dropFade = (1f - lowerPouchDropProgress * 0.9f).coerceIn(0f, 1f)
        val shadowHalfW = computePackShadowHalfWidth(w, yawDeg)
        val shadowOffsetX = sinY * 3f
        val shadowOffsetY = 3f + shadowLift * 4f + lowerPouchDropProgress * h * 0.24f
        val shadowAlpha = 0.20f * computePackShadowStrength(yawDeg) * dropFade
        val shadowCornerR = min(6f, shadowHalfW * 0.90f)
        if (shadowAlpha > 0.01f) {
            shadowPaint.shader = LinearGradient(
                0f, 0f, 0f, h,
                intArrayOf(
                    Color.Black.copy(alpha = shadowAlpha * 0.6f).toArgb(),
                    Color.Black.copy(alpha = shadowAlpha).toArgb()
                ),
                null,
                Shader.TileMode.CLAMP
            )
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    w * 0.5f - shadowHalfW + shadowOffsetX,
                    h * 0.04f + shadowOffsetY,
                    w * 0.5f + shadowHalfW + shadowOffsetX,
                    h * 0.96f + shadowOffsetY,
                    shadowCornerR,
                    shadowCornerR,
                    shadowPaint
                )
            }
        }

        // Dynamic light vector driven by phone tilt + pack rotation
        val lxRaw = sin(Math.toRadians((lighting.specularCenterU - 0.5f) * 140.0)).toFloat()
        val lyRaw = -0.32f
        val lzRaw = sqrt((1f - lxRaw * lxRaw - lyRaw * lyRaw).coerceIn(0.25f, 1f))
        val lLen = sqrt(lxRaw * lxRaw + lyRaw * lyRaw + lzRaw * lzRaw).coerceAtLeast(0.001f)
        val lx = lxRaw / lLen
        val ly = lyRaw / lLen
        val lz = lzRaw / lLen

        val hxRaw = lx
        val hyRaw = ly
        val hzRaw = lz + 1f
        val hLen = sqrt(hxRaw * hxRaw + hyRaw * hyRaw + hzRaw * hzRaw).coerceAtLeast(0.001f)
        val hx = hxRaw / hLen
        val hy = hyRaw / hLen
        val hz = hzRaw / hLen

        val frontW = tex.frontBitmap.width.toFloat()
        val frontH = tex.frontBitmap.height.toFloat()
        val backW = tex.backBitmap.width.toFloat()
        val backH = tex.backBitmap.height.toFloat()
        val sideW = tex.sideFoilBitmap.width.toFloat()
        val sideH = tex.sideFoilBitmap.height.toFloat()
        val innerW = tex.innerFoilBitmap.width.toFloat()
        val innerH = tex.innerFoilBitmap.height.toFloat()

        val crestR = (satinTheme.crestTint.red * 255f).roundToInt().coerceIn(0, 255)
        val crestG = (satinTheme.crestTint.green * 255f).roundToInt().coerceIn(0, 255)
        val crestB = (satinTheme.crestTint.blue * 255f).roundToInt().coerceIn(0, 255)
        val sheenR = (satinTheme.sheenHighlight.red * 255f).roundToInt().coerceIn(0, 255)
        val sheenG = (satinTheme.sheenHighlight.green * 255f).roundToInt().coerceIn(0, 255)
        val sheenB = (satinTheme.sheenHighlight.blue * 255f).roundToInt().coerceIn(0, 255)

        var vCount = 0
        var tCount = 0

        // Progressive tear: the cut travels across the pack with the finger. Columns the cut has not
        // reached yet stay attached to the pouch; columns it has passed lift away from it.
        val tearFrontPos = (tearProgress / 0.8f).coerceIn(0f, 1f)
        fun cutWeight(uAlong: Float): Float {
            val s = if (tearPeelDirX >= 0f) uAlong else 1f - uAlong
            val x = ((tearFrontPos - s) / 0.16f).coerceIn(0f, 1f)
            return x * x * (3f - 2f * x)
        }

        fun emitVertex(
            xLocal: Float,
            yLocal: Float,
            zLocal: Float,
            nxL: Float,
            nyL: Float,
            nzL: Float,
            uCoord: Float,
            vCoord: Float,
            mat: Int,
            isTornTopCap: Boolean = false,
            uAlong: Float = 0.5f
        ): Int {
            if (vCount >= maxVerts - 1) return vCount - 1
            val idx = vCount++
            var mx = xLocal
            var my = yLocal
            var mz = zLocal
            val mnX = nxL
            val mnY = nyL
            val mnZ = nzL

            if (isTornTopCap && tearProgress > 0.01f) {
                // Peel detached upper torn strip cleanly away along swipe direction
                val peel = tearProgress * cutWeight(uAlong)
                val capAngle = Math.toRadians((peel * (tearAngleDeg * 0.42f + tearPeelDirX * -18f)).toDouble())
                val cCos = cos(capAngle).toFloat()
                val cSin = sin(capAngle).toFloat()
                val rx = mx * cCos - my * cSin
                val ry = mx * cSin + my * cCos
                mx = rx + tearPeelDirX * peel * w * 0.56f
                my = ry + tearPeelDirY * peel * h * 0.25f
                mz += peel * w * 0.16f
            } else if (!isTornTopCap && lowerPouchDropProgress > 0.005f) {
                // Slide empty lower pouch downward cleanly as cards emerge from inside it
                my += lowerPouchDropProgress * h * 0.36f
                mz -= lowerPouchDropProgress * w * 0.04f
            }

            // 1. Yaw (Y)
            val x1 = mx * cosY + mz * sinY
            val z1 = -mx * sinY + mz * cosY
            val nx1 = mnX * cosY + mnZ * sinY
            val nz1 = -mnX * sinY + mnZ * cosY

            // 2. Pitch (X)
            val y2 = my * cosP - z1 * sinP
            val z2 = my * sinP + z1 * cosP
            val ny2 = mnY * cosP - nz1 * sinP
            val nz2 = mnY * sinP + nz1 * cosP

            // 3. Roll (Z)
            val x3 = x1 * cosR - y2 * sinR
            val y3 = x1 * sinR + y2 * cosR
            val nx3 = nx1 * cosR - ny2 * sinR
            val ny3 = nx1 * sinR + ny2 * cosR

            val nLen = sqrt(nx3 * nx3 + ny3 * ny3 + nz2 * nz2).coerceAtLeast(0.001f)
            val rnx = nx3 / nLen
            val rny = ny3 / nLen
            val rnz = nz2 / nLen

            val persp = (camDist / (camDist - z2).coerceAtLeast(40f)).coerceIn(0.65f, 1.48f)
            val sx = w * 0.5f + x3 * persp
            val sy = h * 0.5f + y3 * persp

            buf.rotX[idx] = x3
            buf.rotY[idx] = y3
            buf.rotZ[idx] = z2
            buf.projX[idx] = sx
            buf.projY[idx] = sy
            buf.nrmX[idx] = rnx
            buf.nrmY[idx] = rny
            buf.nrmZ[idx] = rnz
            buf.texU[idx] = uCoord
            buf.texV[idx] = vCoord
            buf.matType[idx] = mat

            val nDotL = (rnx * lx + rny * ly + rnz * lz)
            val wrapDiff = ((nDotL + 0.35f) / 1.35f).coerceIn(0f, 1f)
            val facing = abs(rnz).coerceIn(0f, 1f)
            val shade = (0.64f + 0.36f * wrapDiff) * (0.84f + 0.16f * facing)

            val alphaMul = if (isTornTopCap && tearProgress > 0.01f) {
                (1f - cutWeight(uAlong) * ((tearProgress - 0.12f).coerceAtLeast(0f) / 0.88f) * 0.88f).coerceIn(0f, 1f)
            } else if (!isTornTopCap && lowerPouchDropProgress > 0.005f) {
                (1f - lowerPouchDropProgress * 0.94f).coerceIn(0f, 1f)
            } else 1f
            val aInt = (255f * alphaMul).roundToInt().coerceIn(0, 255)
            val sInt = (255f * shade).roundToInt().coerceIn(135, 255)
            buf.diffColor[idx] = (aInt shl 24) or (sInt shl 16) or (sInt shl 8) or sInt

            val nDotH = (rnx * hx + rny * hy + rnz * hz).coerceAtLeast(0f)
            val specPow = nDotH.pow(14f) * (0.34f + 0.36f * lighting.specularIntensity)
            val fresnel = (1f - facing).pow(2.4f) * 0.24f
            val mixWeight = (specPow * 0.75f + fresnel * 0.55f).coerceIn(0f, 0.78f) * alphaMul

            val rSpec = (mixWeight * (sheenR * 0.65f + crestR * 0.35f)).roundToInt().coerceIn(0, 185)
            val gSpec = (mixWeight * (sheenG * 0.65f + crestG * 0.35f)).roundToInt().coerceIn(0, 185)
            val bSpec = (mixWeight * (sheenB * 0.65f + crestB * 0.35f)).roundToInt().coerceIn(0, 185)
            buf.specColor[idx] = (aInt shl 24) or (rSpec shl 16) or (gSpec shl 8) or bSpec

            buf.drawVerts[idx * 2] = sx
            buf.drawVerts[idx * 2 + 1] = sy
            buf.drawTexs[idx * 2] = uCoord
            buf.drawTexs[idx * 2 + 1] = vCoord
            return idx
        }

        fun emitTriIfVisible(i0: Int, i1: Int, i2: Int, mat: Int) {
            if (tCount >= maxTris - 1) return
            val x0 = buf.projX[i0]
            val y0 = buf.projY[i0]
            val x1 = buf.projX[i1]
            val y1 = buf.projY[i1]
            val x2 = buf.projX[i2]
            val y2 = buf.projY[i2]
            val area2 = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0)
            if (area2 <= 0.001f) return
            val t = tCount++
            buf.triI0[t] = i0
            buf.triI1[t] = i1
            buf.triI2[t] = i2
            buf.triMat[t] = mat
            buf.triDepth[t] = (buf.rotZ[i0] + buf.rotZ[i1] + buf.rotZ[i2]) / 3f
            buf.sortOrder[t] = t
        }

        val eps = 0.015f
        fun computeMeshNormal(u: Float, v: Float, isFront: Boolean): Triple<Float, Float, Float> {
            val sign = if (isFront) 1f else -1f
            fun surfZ(uu: Float, vv: Float): Float {
                val base = BoosterPouchPhysics.pouchMeshHalfDepthFraction(uu, vv) * w
                val fin = if (!isFront) BoosterPouchPhysics.pouchBackCenterFinFraction(uu, vv) * w else 0f
                return sign * (base + fin)
            }
            val dzdu = (surfZ((u + eps).coerceIn(0f, 1f), v) - surfZ((u - eps).coerceIn(0f, 1f), v)) / (2f * eps * w)
            val dzdv = (surfZ(u, (v + eps).coerceIn(0f, 1f)) - surfZ(u, (v - eps).coerceIn(0f, 1f))) / (2f * eps * h)
            val nx = -sign * dzdu
            val ny = -sign * dzdv
            val nz = sign
            val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)
            return Triple(nx / len, ny / len, nz / len)
        }

        fun splitVAt(uNorm: Float): Float =
            (tearLeftYRatio + (tearRightYRatio - tearLeftYRatio) * uNorm).coerceIn(0.10f, 0.44f)

        val isTearing = tearProgress > 0.01f
        val stride = cols + 1

        if (!isTearing) {
            // INTACT PACK: single continuous front, back, side gussets, end caps
            val frontStart = vCount
            for (j in 0..rows) {
                val v = j.toFloat() / rows.toFloat()
                val uL = tex.rowLeftEdgeU[j]
                val uR = tex.rowRightEdgeU[j]
                val yLocal = (v - 0.5f) * h
                for (i in 0..cols) {
                    val uGrid = i.toFloat() / cols.toFloat()
                    val uTexNorm = uL + (uR - uL) * uGrid
                    val xLocal = (uTexNorm - 0.5f) * w
                    val zLocal = BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGrid, v) * w
                    val (nxL, nyL, nzL) = computeMeshNormal(uGrid, v, isFront = true)
                    emitVertex(xLocal, yLocal, zLocal, nxL, nyL, nzL, uTexNorm * frontW, v * frontH, mat = 0)
                }
            }
            for (j in 0 until rows) {
                for (i in 0 until cols) {
                    val i00 = frontStart + j * stride + i
                    emitTriIfVisible(i00, i00 + 1, i00 + stride, mat = 0)
                    emitTriIfVisible(i00 + 1, i00 + stride + 1, i00 + stride, mat = 0)
                }
            }

            val backStart = vCount
            for (j in 0..rows) {
                val v = j.toFloat() / rows.toFloat()
                val uL = tex.rowLeftEdgeU[j]
                val uR = tex.rowRightEdgeU[j]
                val yLocal = (v - 0.5f) * h
                for (i in 0..cols) {
                    val uGrid = i.toFloat() / cols.toFloat()
                    val uTexNorm = uL + (uR - uL) * uGrid
                    val xLocal = (uTexNorm - 0.5f) * w
                    val zLocal = -(BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGrid, v) +
                        BoosterPouchPhysics.pouchBackCenterFinFraction(uGrid, v)) * w
                    val (nxL, nyL, nzL) = computeMeshNormal(uGrid, v, isFront = false)
                    val backTexU = (1f - uTexNorm) * backW
                    emitVertex(xLocal, yLocal, zLocal, nxL, nyL, nzL, backTexU, v * backH, mat = 1)
                }
            }
            for (j in 0 until rows) {
                for (i in 0 until cols) {
                    val i00 = backStart + j * stride + i
                    emitTriIfVisible(i00, i00 + stride, i00 + 1, mat = 1)
                    emitTriIfVisible(i00 + 1, i00 + stride, i00 + stride + 1, mat = 1)
                }
            }

            fun emitIntactSideGusset(isRightEdge: Boolean) {
                val baseStart = vCount
                val signX = if (isRightEdge) 1f else -1f
                val edges = if (isRightEdge) tex.rowRightEdgeU else tex.rowLeftEdgeU
                for (j in 0..rows) {
                    val v = j.toFloat() / rows.toFloat()
                    val uEdge = edges[j]
                    val xEdge = (uEdge - 0.5f) * w
                    val yLocal = (v - 0.5f) * h
                    val zHalf = BoosterPouchPhysics.pouchMeshHalfDepthFraction(if (isRightEdge) 1f else 0f, v) * w
                    val bellyRatio = ((zHalf / (w * BoosterPouchPhysics.BELLY_HALF_DEPTH_RATIO)) - 0.1f).coerceIn(0f, 1f)
                    val pleatInset = w * 0.010f * bellyRatio
                    val texY = v * sideH

                    emitVertex(xEdge, yLocal, +zHalf, signX * 0.92f, 0f, 0.38f, 0.12f * sideW, texY, mat = 2)
                    emitVertex(xEdge - signX * pleatInset, yLocal, 0f, signX, 0f, 0f, 0.50f * sideW, texY, mat = 2)
                    emitVertex(xEdge, yLocal, -zHalf, signX * 0.92f, 0f, -0.38f, 0.88f * sideW, texY, mat = 2)
                }
                for (j in 0 until rows) {
                    val f0 = baseStart + j * 3
                    val m0 = f0 + 1
                    val b0 = f0 + 2
                    val f1 = f0 + 3
                    val m1 = f1 + 1
                    val b1 = f1 + 2
                    if (isRightEdge) {
                        emitTriIfVisible(f0, m0, f1, mat = 2)
                        emitTriIfVisible(m0, m1, f1, mat = 2)
                        emitTriIfVisible(m0, b0, m1, mat = 2)
                        emitTriIfVisible(b0, b1, m1, mat = 2)
                    } else {
                        emitTriIfVisible(b0, m0, b1, mat = 2)
                        emitTriIfVisible(m0, m1, b1, mat = 2)
                        emitTriIfVisible(m0, f0, m1, mat = 2)
                        emitTriIfVisible(f0, f1, m1, mat = 2)
                    }
                }
            }
            emitIntactSideGusset(isRightEdge = false)
            emitIntactSideGusset(isRightEdge = true)
        } else {
            // TORN PACK: Two completely disconnected meshes along splitV (open lower pouch + peeled upper cap)
            val lowerRows = 16
            val upperRows = 8

            fun emitSplitFrontGrid(rowSteps: Int, isUpperCap: Boolean) {
                val start = vCount
                for (j in 0..rowSteps) {
                    val t = j.toFloat() / rowSteps.toFloat()
                    for (i in 0..cols) {
                        val uGrid = i.toFloat() / cols.toFloat()
                        val splitHere = splitVAt(uGrid)
                        val vRaw = if (isUpperCap) splitHere * t else splitHere + (1f - splitHere) * t
                        val jagAmp = if (!isUpperCap && t < 0.08f) {
                            (1f - t / 0.08f) * sin(uGrid * 34f) * 0.0042f * tearProgress
                        } else if (isUpperCap && t > 0.92f) {
                            ((t - 0.92f) / 0.08f) * sin(uGrid * 34f) * 0.0042f * tearProgress
                        } else 0f
                        val v = (vRaw + jagAmp).coerceIn(0f, 1f)
                        val uL = sampleEdgeU(tex.rowLeftEdgeU, v)
                        val uR = sampleEdgeU(tex.rowRightEdgeU, v)
                        val uTexNorm = uL + (uR - uL) * uGrid
                        val xLocal = (uTexNorm - 0.5f) * w
                        var yLocal = (v - 0.5f) * h
                        var zLocal = BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGrid, v) * w
                        if (!isUpperCap && t < 0.24f) {
                            val flare = (1f - t / 0.24f).pow(1.4f)
                            val cw = cutWeight(uGrid)
                            zLocal += tearProgress * cw * w * 0.115f * flare
                            yLocal += tearProgress * cw * h * 0.008f * flare
                        }
                        val (nxL, nyL, nzL) = computeMeshNormal(uGrid, v, isFront = true)
                        emitVertex(
                            xLocal, yLocal, zLocal, nxL, nyL, nzL,
                            uTexNorm * frontW, v * frontH,
                            mat = 0, isTornTopCap = isUpperCap, uAlong = uGrid
                        )
                    }
                }
                for (j in 0 until rowSteps) {
                    for (i in 0 until cols) {
                        val i00 = start + j * stride + i
                        emitTriIfVisible(i00, i00 + 1, i00 + stride, mat = 0)
                        emitTriIfVisible(i00 + 1, i00 + stride + 1, i00 + stride, mat = 0)
                    }
                }
            }

            fun emitSplitBackGrid(rowSteps: Int, isUpperCap: Boolean) {
                val start = vCount
                for (j in 0..rowSteps) {
                    val t = j.toFloat() / rowSteps.toFloat()
                    for (i in 0..cols) {
                        val uGrid = i.toFloat() / cols.toFloat()
                        val splitHere = splitVAt(uGrid)
                        val vRaw = if (isUpperCap) splitHere * t else splitHere + (1f - splitHere) * t
                        val jagAmp = if (!isUpperCap && t < 0.08f) {
                            (1f - t / 0.08f) * sin(uGrid * 34f) * 0.0042f * tearProgress
                        } else if (isUpperCap && t > 0.92f) {
                            ((t - 0.92f) / 0.08f) * sin(uGrid * 34f) * 0.0042f * tearProgress
                        } else 0f
                        val v = (vRaw + jagAmp).coerceIn(0f, 1f)
                        val uL = sampleEdgeU(tex.rowLeftEdgeU, v)
                        val uR = sampleEdgeU(tex.rowRightEdgeU, v)
                        val uTexNorm = uL + (uR - uL) * uGrid
                        val xLocal = (uTexNorm - 0.5f) * w
                        val yLocal = (v - 0.5f) * h
                        var zLocal = -(BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGrid, v) +
                            BoosterPouchPhysics.pouchBackCenterFinFraction(uGrid, v)) * w
                        if (!isUpperCap && t < 0.24f) {
                            val flare = (1f - t / 0.24f).pow(1.4f)
                            zLocal -= tearProgress * cutWeight(uGrid) * w * 0.08f * flare
                        }
                        val (nxL, nyL, nzL) = computeMeshNormal(uGrid, v, isFront = false)
                        val backTexU = (1f - uTexNorm) * backW
                        emitVertex(
                            xLocal, yLocal, zLocal, nxL, nyL, nzL,
                            backTexU, v * backH,
                            mat = 1, isTornTopCap = isUpperCap, uAlong = uGrid
                        )
                    }
                }
                for (j in 0 until rowSteps) {
                    for (i in 0 until cols) {
                        val i00 = start + j * stride + i
                        emitTriIfVisible(i00, i00 + stride, i00 + 1, mat = 1)
                        emitTriIfVisible(i00 + 1, i00 + stride, i00 + stride + 1, mat = 1)
                    }
                }
            }

            fun emitInnerCavityThroat() {
                // Dark silver interior foil visible inside the flared lower pouch mouth
                val innerRows = 3
                val start = vCount
                for (j in 0..innerRows) {
                    val t = j.toFloat() / innerRows.toFloat()
                    for (i in 0..cols) {
                        val uGrid = i.toFloat() / cols.toFloat()
                        val splitHere = splitVAt(uGrid)
                        val v = (splitHere + t * 0.08f).coerceIn(0f, 1f)
                        val uL = sampleEdgeU(tex.rowLeftEdgeU, v)
                        val uR = sampleEdgeU(tex.rowRightEdgeU, v)
                        val insetU = 0.025f
                        val uTexNorm = (uL + insetU) + ((uR - insetU) - (uL + insetU)) * uGrid
                        val xLocal = (uTexNorm - 0.5f) * w
                        val yLocal = (v - 0.5f) * h
                        val zBackLip = -(BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGrid, v) * w +
                            tearProgress * cutWeight(uGrid) * w * 0.065f * (1f - t))
                        emitVertex(
                            xLocal, yLocal, zBackLip + 1.5f,
                            0f, 0.12f, 1f,
                            uGrid * innerW, t * innerH,
                            mat = 3, isTornTopCap = false
                        )
                    }
                }
                for (j in 0 until innerRows) {
                    for (i in 0 until cols) {
                        val i00 = start + j * stride + i
                        emitTriIfVisible(i00, i00 + 1, i00 + stride, mat = 3)
                        emitTriIfVisible(i00 + 1, i00 + stride + 1, i00 + stride, mat = 3)
                    }
                }
            }

            fun emitSplitSideGusset(isRightEdge: Boolean, rowSteps: Int, isUpperCap: Boolean) {
                val baseStart = vCount
                val signX = if (isRightEdge) 1f else -1f
                val edges = if (isRightEdge) tex.rowRightEdgeU else tex.rowLeftEdgeU
                val uGridEdge = if (isRightEdge) 1f else 0f
                val splitHere = splitVAt(uGridEdge)
                for (j in 0..rowSteps) {
                    val t = j.toFloat() / rowSteps.toFloat()
                    val v = (if (isUpperCap) splitHere * t else splitHere + (1f - splitHere) * t).coerceIn(0f, 1f)
                    val uEdge = sampleEdgeU(edges, v)
                    val xEdge = (uEdge - 0.5f) * w
                    val yLocal = (v - 0.5f) * h
                    val zBase = BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGridEdge, v) * w
                    val mouthFlareFront = if (!isUpperCap && t < 0.24f) {
                        tearProgress * cutWeight(uGridEdge) * w * 0.115f * (1f - t / 0.24f).pow(1.4f)
                    } else 0f
                    val mouthFlareBack = if (!isUpperCap && t < 0.24f) {
                        tearProgress * cutWeight(uGridEdge) * w * 0.08f * (1f - t / 0.24f).pow(1.4f)
                    } else 0f
                    val zFront = zBase + mouthFlareFront
                    val zBack = -(zBase + mouthFlareBack)
                    val bellyRatio = ((zBase / (w * BoosterPouchPhysics.BELLY_HALF_DEPTH_RATIO)) - 0.1f).coerceIn(0f, 1f)
                    val pleatInset = w * 0.010f * bellyRatio
                    val texY = v * sideH

                    emitVertex(xEdge, yLocal, zFront, signX * 0.92f, 0f, 0.38f, 0.12f * sideW, texY, mat = 2, isTornTopCap = isUpperCap, uAlong = uGridEdge)
                    emitVertex(xEdge - signX * pleatInset, yLocal, (zFront + zBack) * 0.5f, signX, 0f, 0f, 0.50f * sideW, texY, mat = 2, isTornTopCap = isUpperCap, uAlong = uGridEdge)
                    emitVertex(xEdge, yLocal, zBack, signX * 0.92f, 0f, -0.38f, 0.88f * sideW, texY, mat = 2, isTornTopCap = isUpperCap, uAlong = uGridEdge)
                }
                for (j in 0 until rowSteps) {
                    val f0 = baseStart + j * 3
                    val m0 = f0 + 1
                    val b0 = f0 + 2
                    val f1 = f0 + 3
                    val m1 = f1 + 1
                    val b1 = f1 + 2
                    if (isRightEdge) {
                        emitTriIfVisible(f0, m0, f1, mat = 2)
                        emitTriIfVisible(m0, m1, f1, mat = 2)
                        emitTriIfVisible(m0, b0, m1, mat = 2)
                        emitTriIfVisible(b0, b1, m1, mat = 2)
                    } else {
                        emitTriIfVisible(b0, m0, b1, mat = 2)
                        emitTriIfVisible(m0, m1, b1, mat = 2)
                        emitTriIfVisible(m0, f0, m1, mat = 2)
                        emitTriIfVisible(f0, f1, m1, mat = 2)
                    }
                }
            }

            emitSplitFrontGrid(lowerRows, isUpperCap = false)
            emitSplitFrontGrid(upperRows, isUpperCap = true)
            emitSplitBackGrid(lowerRows, isUpperCap = false)
            emitSplitBackGrid(upperRows, isUpperCap = true)
            emitInnerCavityThroat()
            emitSplitSideGusset(isRightEdge = false, rowSteps = lowerRows, isUpperCap = false)
            emitSplitSideGusset(isRightEdge = true, rowSteps = lowerRows, isUpperCap = false)
            emitSplitSideGusset(isRightEdge = false, rowSteps = upperRows, isUpperCap = true)
            emitSplitSideGusset(isRightEdge = true, rowSteps = upperRows, isUpperCap = true)
        }

        // Top & Bottom Watertight Crimp Edge Caps
        fun emitEndCap(isTop: Boolean) {
            val row = if (isTop) 0 else rows
            val v = if (isTop) 0f else 1f
            val uL = tex.rowLeftEdgeU[row]
            val uR = tex.rowRightEdgeU[row]
            val yLocal = (v - 0.5f) * h
            val nyCap = if (isTop) -1f else 1f
            val base = vCount
            for (i in 0..cols) {
                val uGrid = i.toFloat() / cols.toFloat()
                val uTexNorm = uL + (uR - uL) * uGrid
                val xLocal = (uTexNorm - 0.5f) * w
                val zHalf = BoosterPouchPhysics.pouchMeshHalfDepthFraction(uGrid, v) * w
                val isUpperCap = isTearing && isTop
                emitVertex(xLocal, yLocal, +zHalf, 0f, nyCap, 0.25f, uGrid * sideW, if (isTop) 0.04f * sideH else 0.96f * sideH, mat = 2, isTornTopCap = isUpperCap, uAlong = uGrid)
                emitVertex(xLocal, yLocal, -zHalf, 0f, nyCap, -0.25f, uGrid * sideW, if (isTop) 0.08f * sideH else 0.92f * sideH, mat = 2, isTornTopCap = isUpperCap, uAlong = uGrid)
            }
            for (i in 0 until cols) {
                val f0 = base + i * 2
                val b0 = f0 + 1
                val f1 = f0 + 2
                val b1 = f0 + 3
                if (isTop) {
                    emitTriIfVisible(f0, b0, f1, mat = 2)
                    emitTriIfVisible(b0, b1, f1, mat = 2)
                } else {
                    emitTriIfVisible(b0, f0, f1, mat = 2)
                    emitTriIfVisible(b0, f1, b1, mat = 2)
                }
            }
        }
        emitEndCap(isTop = true)
        emitEndCap(isTop = false)

        // Sort visible triangles back-to-front by Z depth
        for (k in 1 until tCount) {
            val keyIdx = buf.sortOrder[k]
            val keyDepth = buf.triDepth[keyIdx]
            var p = k - 1
            while (p >= 0 && buf.triDepth[buf.sortOrder[p]] > keyDepth) {
                buf.sortOrder[p + 1] = buf.sortOrder[p]
                p--
            }
            buf.sortOrder[p + 1] = keyIdx
        }

        drawIntoCanvas { composeCanvas ->
            val nc = composeCanvas.nativeCanvas
            val totalVertFloats = vCount * 2

            var batchStart = 0
            while (batchStart < tCount) {
                val firstTri = buf.sortOrder[batchStart]
                val mat = buf.triMat[firstTri]
                var batchEnd = batchStart
                var idxWrite = 0
                while (batchEnd < tCount && buf.triMat[buf.sortOrder[batchEnd]] == mat) {
                    val t = buf.sortOrder[batchEnd]
                    buf.drawIndices[idxWrite++] = buf.triI0[t].toShort()
                    buf.drawIndices[idxWrite++] = buf.triI1[t].toShort()
                    buf.drawIndices[idxWrite++] = buf.triI2[t].toShort()
                    batchEnd++
                }

                diffusePaint.shader = when (mat) {
                    0 -> tex.frontShader
                    1 -> tex.backShader
                    3 -> tex.innerFoilShader
                    else -> tex.sideShader
                }
                nc.drawVertices(
                    NativeCanvas.VertexMode.TRIANGLES,
                    totalVertFloats,
                    buf.drawVerts,
                    0,
                    buf.drawTexs,
                    0,
                    buf.diffColor,
                    0,
                    buf.drawIndices,
                    0,
                    idxWrite,
                    diffusePaint
                )

                // Front uses front alphaMask; full-bleed back/side/inner use solid opaque specular mask
                specPaint.shader = when (mat) {
                    0 -> tex.alphaShader
                    1 -> tex.backAlphaShader
                    else -> tex.opaqueWhiteShader
                }
                nc.drawVertices(
                    NativeCanvas.VertexMode.TRIANGLES,
                    totalVertFloats,
                    buf.drawVerts,
                    0,
                    buf.drawTexs,
                    0,
                    buf.specColor,
                    0,
                    buf.drawIndices,
                    0,
                    idxWrite,
                    specPaint
                )

                batchStart = batchEnd
            }
        }
    }
}
