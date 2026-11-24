package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Rect
import android.media.FaceDetector
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Crops any landscape or portrait skin illustration into a 1:1 square centered on the hero character.
 * Uses a fast 32x24 saliency + upper-torso compositional grid (and optional FaceDetector only in
 * full-resolution detail mode) and avoids costly CPU pixel sharpening on thumbnails.
 */
class CharacterFocusSquareTransformation(
    imageKey: String,
    private val targetPx: Int = 400,
    private val fastMode: Boolean = true
) : Transformation {

    override val cacheKey: String = "char_focus_sq_v3_${targetPx}_${fastMode}_$imageKey"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val w = input.width
        val h = input.height
        if (w <= 0 || h <= 0) return input

        val safeInput: Bitmap = if (input.config == Bitmap.Config.HARDWARE) {
            input.copy(Bitmap.Config.ARGB_8888, false) ?: input
        } else {
            input
        }

        val cropped = if (w == h) {
            safeInput
        } else {
            val faceCenter = if (!fastMode) detectPrimaryFaceNormalized(safeInput) else null
            val cropRect = computeFocusCropRect(
                width = safeInput.width,
                height = safeInput.height,
                faceNormX = faceCenter?.first,
                faceNormY = faceCenter?.second,
                energyProvider = { gridW, gridH ->
                    sampleVisualEnergyGrid(safeInput, gridW, gridH)
                }
            )
            Bitmap.createBitmap(
                safeInput,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
            )
        }

        val result = if (cropped.width > targetPx) {
            Bitmap.createScaledBitmap(cropped, targetPx, targetPx, true)
        } else if (!fastMode && cropped.width < targetPx) {
            Bitmap.createScaledBitmap(cropped, targetPx, targetPx, true)
        } else {
            cropped
        }

        if (cropped !== safeInput && cropped !== result && !cropped.isRecycled) {
            cropped.recycle()
        }
        if (safeInput !== input && safeInput !== result && !safeInput.isRecycled) {
            safeInput.recycle()
        }
        return result
    }

    private fun detectPrimaryFaceNormalized(bitmap: Bitmap): Pair<Float, Float>? {
        return try {
            val targetW = 128
            val targetH = ((bitmap.height.toFloat() / bitmap.width.toFloat()) * targetW)
                .roundToInt()
                .coerceIn(64, 200)
            val evenW = targetW and -2
            val small = Bitmap.createScaledBitmap(bitmap, evenW, targetH, true)
            val rgb565 = small.copy(Bitmap.Config.RGB_565, false)
            if (small !== bitmap && !small.isRecycled) small.recycle()
            if (rgb565 == null) return null

            val faces = arrayOfNulls<FaceDetector.Face>(2)
            val count = FaceDetector(evenW, targetH, 2).findFaces(rgb565, faces)
            rgb565.recycle()

            val best = faces.take(count).filterNotNull().maxByOrNull { it.confidence() }
            if (best != null && best.confidence() >= 0.42f) {
                val mid = android.graphics.PointF()
                best.getMidPoint(mid)
                val nx = (mid.x / evenW.toFloat()).coerceIn(0f, 1f)
                val ny = ((mid.y / targetH.toFloat()) + 0.08f).coerceIn(0f, 1f)
                nx to ny
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun sampleVisualEnergyGrid(bitmap: Bitmap, gridW: Int, gridH: Int): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, gridW, gridH, true)
        val pixels = IntArray(gridW * gridH)
        scaled.getPixels(pixels, 0, gridW, 0, 0, gridW, gridH)
        if (scaled !== bitmap) scaled.recycle()

        val luma = FloatArray(pixels.size)
        val sat = FloatArray(pixels.size)
        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            luma[i] = 0.299f * r + 0.587f * g + 0.114f * b
            val maxC = max(r, max(g, b))
            val minC = min(r, min(g, b))
            sat[i] = if (maxC > 0) (maxC - minC).toFloat() / maxC.toFloat() else 0f
        }

        val energy = FloatArray(pixels.size)
        for (y in 0 until gridH) {
            val ny = (y + 0.5f) / gridH.toFloat()
            val verticalPrior = when {
                ny < 0.08f -> 0.55f
                ny <= 0.62f -> 1.45f
                ny <= 0.80f -> 0.90f
                else -> 0.45f
            }
            for (x in 0 until gridW) {
                val nx = (x + 0.5f) / gridW.toFloat()
                val horizontalPrior = if (nx < 0.08f || nx > 0.92f) 0.45f else 1.15f

                val idx = y * gridW + x
                val left = luma[y * gridW + max(0, x - 1)]
                val right = luma[y * gridW + min(gridW - 1, x + 1)]
                val up = luma[max(0, y - 1) * gridW + x]
                val down = luma[min(gridH - 1, y + 1) * gridW + x]
                val grad = abs(right - left) + abs(down - up)

                val rawScore = (grad * 0.85f) + (sat[idx] * 48f)
                energy[idx] = rawScore * verticalPrior * horizontalPrior
            }
        }
        return energy
    }

    companion object {
        internal const val GRID_W = 32
        internal const val GRID_H = 24

        data class CropBounds(
            val left: Int,
            val top: Int,
            val right: Int,
            val bottom: Int
        ) {
            fun width(): Int = right - left
            fun height(): Int = bottom - top
        }

        /**
         * Pure-JVM testable 1:1 focus square calculator.
         */
        fun computeFocusCropBounds(
            width: Int,
            height: Int,
            faceNormX: Float? = null,
            faceNormY: Float? = null,
            energyProvider: ((Int, Int) -> FloatArray)? = null
        ): CropBounds {
            val squareSize = min(width, height).coerceAtLeast(1)
            if (width == height) return CropBounds(0, 0, width, height)

            if (faceNormX != null && faceNormY != null) {
                val centerX = (faceNormX * width).roundToInt()
                val centerY = (faceNormY * height).roundToInt()
                val left = (centerX - squareSize / 2).coerceIn(0, width - squareSize)
                val top = (centerY - squareSize / 2).coerceIn(0, height - squareSize)
                return CropBounds(left, top, left + squareSize, top + squareSize)
            }

            val grid = energyProvider?.invoke(GRID_W, GRID_H)
            if (grid != null && grid.size == GRID_W * GRID_H) {
                if (width > height) {
                    val colEnergy = FloatArray(GRID_W)
                    for (x in 0 until GRID_W) {
                        var sum = 0f
                        for (y in 0 until GRID_H) {
                            sum += grid[y * GRID_W + x]
                        }
                        colEnergy[x] = sum
                    }
                    val windowCols = ((squareSize.toFloat() / width.toFloat()) * GRID_W)
                        .roundToInt()
                        .coerceIn(1, GRID_W)
                    var bestStartCol = ((GRID_W - windowCols) / 2).coerceAtLeast(0)
                    var bestScore = -1f
                    for (start in 0..(GRID_W - windowCols)) {
                        var windowScore = 0f
                        for (c in start until (start + windowCols)) {
                            windowScore += colEnergy[c]
                        }
                        if (windowScore > bestScore) {
                            bestScore = windowScore
                            bestStartCol = start
                        }
                    }
                    val left = ((bestStartCol.toFloat() / GRID_W.toFloat()) * width)
                        .roundToInt()
                        .coerceIn(0, width - squareSize)
                    return CropBounds(left, 0, left + squareSize, squareSize)
                } else {
                    val rowEnergy = FloatArray(GRID_H)
                    for (y in 0 until GRID_H) {
                        var sum = 0f
                        for (x in 0 until GRID_W) {
                            sum += grid[y * GRID_W + x]
                        }
                        rowEnergy[y] = sum
                    }
                    val windowRows = ((squareSize.toFloat() / height.toFloat()) * GRID_H)
                        .roundToInt()
                        .coerceIn(1, GRID_H)
                    var bestStartRow = 0
                    var bestScore = -1f
                    for (start in 0..(GRID_H - windowRows)) {
                        var windowScore = 0f
                        for (r in start until (start + windowRows)) {
                            windowScore += rowEnergy[r]
                        }
                        if (windowScore > bestScore) {
                            bestScore = windowScore
                            bestStartRow = start
                        }
                    }
                    val top = ((bestStartRow.toFloat() / GRID_H.toFloat()) * height)
                        .roundToInt()
                        .coerceIn(0, height - squareSize)
                    return CropBounds(0, top, squareSize, top + squareSize)
                }
            }

            val left = ((width - squareSize) / 2).coerceIn(0, width - squareSize)
            val top = if (height > width) ((height - squareSize) * 0.25f).roundToInt() else 0
            return CropBounds(left, top, left + squareSize, top + squareSize)
        }

        fun computeFocusCropRect(
            width: Int,
            height: Int,
            faceNormX: Float? = null,
            faceNormY: Float? = null,
            energyProvider: ((Int, Int) -> FloatArray)? = null
        ): Rect {
            val b = computeFocusCropBounds(width, height, faceNormX, faceNormY, energyProvider)
            return Rect(b.left, b.top, b.right, b.bottom)
        }
    }
}
