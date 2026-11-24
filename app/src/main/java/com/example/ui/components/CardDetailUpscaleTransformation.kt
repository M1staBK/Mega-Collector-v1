package com.example.ui.components

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Fast resolution-aware Coil Transformation for full-bleed trading card artwork.
 * Avoids expensive CPU pixel-loops on normal/HD assets and only applies progressive scaling
 * when an image arrives genuinely undersized.
 */
class CardDetailUpscaleTransformation(
    imageKey: String,
    private val targetMinPx: Int = 680,
    private val runSharpen: Boolean = false
) : Transformation {

    override val cacheKey: String = "detail_sr_v2_${targetMinPx}_${runSharpen}_$imageKey"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        return upscaleAndSharpen(input, targetMinPx, runSharpen)
    }

    companion object {
        fun upscaleAndSharpen(
            source: Bitmap,
            targetMinPx: Int = 680,
            runSharpen: Boolean = false
        ): Bitmap {
            val srcW = source.width
            val srcH = source.height
            if (srcW <= 2 || srcH <= 2) return source

            val currentMin = min(srcW, srcH)
            // Fast exit: already large enough and no CPU sharpen needed
            if (currentMin >= targetMinPx && !runSharpen) {
                return source
            }

            val safeBitmap = if (source.config == Bitmap.Config.HARDWARE) {
                source.copy(Bitmap.Config.ARGB_8888, false) ?: source
            } else {
                source
            }

            val scaled: Bitmap = if (currentMin < targetMinPx) {
                val scale = (targetMinPx.toFloat() / currentMin.toFloat()).coerceIn(1f, 2.0f)
                val maxTargetEdge = 960f
                val maxSrcEdge = max(srcW, srcH).toFloat()
                val clampedScale = if (maxSrcEdge * scale > maxTargetEdge) {
                    (maxTargetEdge / maxSrcEdge).coerceAtLeast(1f)
                } else {
                    scale
                }
                val dstW = (srcW * clampedScale).roundToInt().coerceAtLeast(srcW)
                val dstH = (srcH * clampedScale).roundToInt().coerceAtLeast(srcH)
                Bitmap.createScaledBitmap(safeBitmap, dstW, dstH, true)
            } else {
                safeBitmap
            }

            if (!runSharpen || currentMin >= 480) {
                if (safeBitmap !== source && safeBitmap !== scaled && !safeBitmap.isRecycled) {
                    safeBitmap.recycle()
                }
                return scaled
            }

            val outW = scaled.width
            val outH = scaled.height
            if (outW * outH > 640_000) {
                if (safeBitmap !== source && safeBitmap !== scaled && !safeBitmap.isRecycled) {
                    safeBitmap.recycle()
                }
                return scaled
            }

            val pixels = IntArray(outW * outH)
            scaled.getPixels(pixels, 0, outW, 0, 0, outW, outH)

            val sharpened = enhanceLuminanceDetail(
                pixels = pixels,
                width = outW,
                height = outH,
                amount = 0.52f,
                threshold = 4.0f
            )

            val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
            result.setPixels(sharpened, 0, outW, 0, 0, outW, outH)

            if (scaled !== safeBitmap && scaled !== source && !scaled.isRecycled) scaled.recycle()
            if (safeBitmap !== source && !safeBitmap.isRecycled) safeBitmap.recycle()
            return result
        }

        /**
         * Pure-JVM adaptive luminance Unsharp Mask + micro-clarity kernel.
         * Enhances fine detail strictly on luma channel (preserving skin tone hue/color fidelity).
         */
        fun enhanceLuminanceDetail(
            pixels: IntArray,
            width: Int,
            height: Int,
            amount: Float = 0.56f,
            threshold: Float = 4.0f
        ): IntArray {
            if (width < 3 || height < 3 || pixels.size != width * height) return pixels

            val total = width * height
            val luma = FloatArray(total)
            for (i in 0 until total) {
                val c = pixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                luma[i] = 0.299f * r + 0.587f * g + 0.114f * b
            }

            val out = pixels.copyOf()
            for (y in 1 until (height - 1)) {
                val rowOffset = y * width
                for (x in 1 until (width - 1)) {
                    val idx = rowOffset + x
                    val centerL = luma[idx]

                    // 3x3 Gaussian blur approximation [1,2,1; 2,4,2; 1,2,1] / 16
                    val blurL = (
                        luma[idx - width - 1] + 2f * luma[idx - width] + luma[idx - width + 1] +
                        2f * luma[idx - 1] + 4f * centerL + 2f * luma[idx + 1] +
                        luma[idx + width - 1] + 2f * luma[idx + width] + luma[idx + width + 1]
                    ) * 0.0625f

                    val diff = centerL - blurL
                    if (abs(diff) < threshold) continue

                    // Dampen extreme contrast edges to prevent white ringing halos
                    val delta = (diff * amount).coerceIn(-28f, 28f)
                    val c = pixels[idx]
                    val a = (c ushr 24) and 0xFF
                    val r = (((c shr 16) and 0xFF) + delta).roundToInt().coerceIn(0, 255)
                    val g = (((c shr 8) and 0xFF) + delta).roundToInt().coerceIn(0, 255)
                    val b = ((c and 0xFF) + delta).roundToInt().coerceIn(0, 255)
                    out[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
            return out
        }
    }
}
