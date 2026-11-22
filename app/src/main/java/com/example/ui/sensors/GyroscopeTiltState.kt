package com.example.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Pure-JVM helper to remap device pitch/roll deltas according to screen surface rotation
 * so tilting left/right/forward/back works identically in portrait, landscape, or reverse orientation.
 */
internal fun mapTiltForDisplayRotation(
    rawPitchDeg: Float,
    rawRollDeg: Float,
    displayRotation: Int
): Pair<Float, Float> {
    return when (displayRotation) {
        Surface.ROTATION_90 -> -rawRollDeg to rawPitchDeg
        Surface.ROTATION_180 -> -rawPitchDeg to -rawRollDeg
        Surface.ROTATION_270 -> rawRollDeg to -rawPitchDeg
        else -> rawPitchDeg to rawRollDeg
    }
}

@Stable
class CardTiltState {
    var pitchDegrees by mutableFloatStateOf(0f)
        private set
    var rollDegrees by mutableFloatStateOf(0f)
        private set
    var isGyroAvailable by mutableStateOf(false)
        internal set
    var gyroEnabled by mutableStateOf(true)

    private var dragPitch = 0f
    private var dragRoll = 0f
    private var sensorPitch = 0f
    private var sensorRoll = 0f

    internal var baselineMatrix: FloatArray? = null
    internal var baselineGravity: FloatArray? = null

    fun updateRelativeTilt(relPitchDeg: Float, relRollDeg: Float) {
        if (!relPitchDeg.isFinite() || !relRollDeg.isFinite()) return
        val clampedPitch = relPitchDeg.coerceIn(-24f, 24f)
        val clampedRoll = relRollDeg.coerceIn(-24f, 24f)
        // Smooth exponential filter for responsive yet silky holographic shimmer
        sensorPitch = sensorPitch * 0.72f + clampedPitch * 0.28f
        sensorRoll = sensorRoll * 0.72f + clampedRoll * 0.28f
        recompute()
    }

    fun updateFromDrag(deltaX: Float, deltaY: Float) {
        dragRoll = (dragRoll + deltaX * 0.14f).coerceIn(-22f, 22f)
        dragPitch = (dragPitch - deltaY * 0.14f).coerceIn(-22f, 22f)
        recompute()
    }

    fun releaseDrag() {
        dragPitch *= 0.25f
        dragRoll *= 0.25f
        if (abs(dragPitch) < 0.5f) dragPitch = 0f
        if (abs(dragRoll) < 0.5f) dragRoll = 0f
        recompute()
    }

    fun calibrateCenter() {
        baselineMatrix = null
        baselineGravity = null
        sensorPitch = 0f
        sensorRoll = 0f
        dragPitch = 0f
        dragRoll = 0f
        recompute()
    }

    private fun recompute() {
        val activeSensorPitch = if (gyroEnabled) sensorPitch else 0f
        val activeSensorRoll = if (gyroEnabled) sensorRoll else 0f
        pitchDegrees = (activeSensorPitch + dragPitch).coerceIn(-26f, 26f)
        rollDegrees = (activeSensorRoll + dragRoll).coerceIn(-26f, 26f)
    }
}

private fun currentDisplayRotation(context: Context): Int {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.rotation ?: Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            @Suppress("DEPRECATION")
            wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        }
    } catch (_: Throwable) {
        Surface.ROTATION_0
    }
}

@Composable
fun rememberCardTiltState(): CardTiltState {
    val context = LocalContext.current
    val tiltState = remember { CardTiltState() }

    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    // Track whether preferred rotation-vector sensor produced real readings; fallback seamlessly if silent/missing
    var useFallbackMode by remember { mutableStateOf(false) }
    var receivedPrimaryEvent by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(sensorManager, useFallbackMode) {
        if (sensorManager != null && !useFallbackMode) {
            delay(500L)
            if (!receivedPrimaryEvent) {
                useFallbackMode = true
            }
        }
    }

    DisposableEffect(context, sensorManager, useFallbackMode) {
        if (sensorManager == null) {
            tiltState.isGyroAvailable = false
            return@DisposableEffect onDispose {}
        }

        val gameRotSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        val rotSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val primarySensor = if (!useFallbackMode) (gameRotSensor ?: rotSensor) else null

        val gravityOrAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val registeredSensors = mutableListOf<Sensor>()

        var integratedPitchDeg = 0f
        var integratedRollDeg = 0f
        var lastGyroNs = 0L

        val smoothedGravity = FloatArray(3)
        var hasGravityInit = false

        val listener = object : SensorEventListener {
            private val currentR = FloatArray(9)
            private val angleChange = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                val rot = currentDisplayRotation(context)
                when (event.sensor.type) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR,
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        if (event.values.size < 3) return
                        receivedPrimaryEvent = true

                        // Normalize to 3 or 4 values safely for vendor quirks (Samsung/Xiaomi/Pixel)
                        val q = if (event.values.size >= 4) event.values.copyOf(4) else event.values.copyOf(3)
                        SensorManager.getRotationMatrixFromVector(currentR, q)
                        if (currentR.any { !it.isFinite() }) return

                        val base = tiltState.baselineMatrix
                        if (base == null) {
                            tiltState.baselineMatrix = currentR.copyOf()
                            return
                        }

                        // Shortest-arc 3D relative rotation between baseline pose and current pose -> ZERO gimbal lock!
                        SensorManager.getAngleChange(angleChange, currentR, base)
                        // angleChange[1] = pitch around device X axis, angleChange[2] = roll around device Y axis
                        val dPitchDeg = Math.toDegrees(angleChange[1].toDouble()).toFloat() * 1.35f
                        val dRollDeg = Math.toDegrees(angleChange[2].toDouble()).toFloat() * 1.35f

                        val (mappedPitch, mappedRoll) = mapTiltForDisplayRotation(dPitchDeg, dRollDeg, rot)
                        tiltState.updateRelativeTilt(mappedPitch, mappedRoll)
                    }

                    Sensor.TYPE_GRAVITY,
                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]
                        val mag = sqrt(ax * ax + ay * ay + az * az)
                        if (mag < 1.0f || !mag.isFinite()) return

                        val nx = ax / mag
                        val ny = ay / mag
                        val nz = az / mag
                        if (!hasGravityInit) {
                            smoothedGravity[0] = nx
                            smoothedGravity[1] = ny
                            smoothedGravity[2] = nz
                            hasGravityInit = true
                        } else {
                            smoothedGravity[0] = smoothedGravity[0] * 0.80f + nx * 0.20f
                            smoothedGravity[1] = smoothedGravity[1] * 0.80f + ny * 0.20f
                            smoothedGravity[2] = smoothedGravity[2] * 0.80f + nz * 0.20f
                        }

                        val baseG = tiltState.baselineGravity
                        if (baseG == null) {
                            tiltState.baselineGravity = smoothedGravity.copyOf()
                            return
                        }

                        // Project delta gravity vector onto device X/Y axes (works upright or flat without Euler wrap!)
                        // Cross-product components around X and Y between baseG and smoothedGravity:
                        val crossX = baseG[1] * smoothedGravity[2] - baseG[2] * smoothedGravity[1]
                        val crossY = baseG[2] * smoothedGravity[0] - baseG[0] * smoothedGravity[2]
                        val dPitchDeg = Math.toDegrees(asin(crossX.coerceIn(-1f, 1f).toDouble())).toFloat() * 1.35f
                        val dRollDeg = Math.toDegrees(asin(crossY.coerceIn(-1f, 1f).toDouble())).toFloat() * 1.35f

                        val (mappedPitch, mappedRoll) = mapTiltForDisplayRotation(dPitchDeg, dRollDeg, rot)
                        tiltState.updateRelativeTilt(mappedPitch, mappedRoll)
                    }

                    Sensor.TYPE_GYROSCOPE -> {
                        // Only integrate gyroscope directly if gravity/accel fallback is absent
                        if (gravityOrAccelSensor != null) return
                        if (lastGyroNs != 0L) {
                            val dt = ((event.timestamp - lastGyroNs) / 1_000_000_000f).coerceIn(0f, 0.1f)
                            val wxDeg = Math.toDegrees((event.values[0] * dt).toDouble()).toFloat()
                            val wyDeg = Math.toDegrees((event.values[1] * dt).toDouble()).toFloat()
                            integratedPitchDeg = ((integratedPitchDeg + wxDeg) * 0.992f).coerceIn(-24f, 24f)
                            integratedRollDeg = ((integratedRollDeg + wyDeg) * 0.992f).coerceIn(-24f, 24f)
                            val (mappedPitch, mappedRoll) = mapTiltForDisplayRotation(
                                integratedPitchDeg,
                                integratedRollDeg,
                                rot
                            )
                            tiltState.updateRelativeTilt(mappedPitch, mappedRoll)
                        }
                        lastGyroNs = event.timestamp
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (primarySensor != null) {
            sensorManager.registerListener(listener, primarySensor, SensorManager.SENSOR_DELAY_GAME)
            registeredSensors += primarySensor
        } else {
            gravityOrAccelSensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
                registeredSensors += it
            }
            if (gravityOrAccelSensor == null && gyroSensor != null) {
                sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
                registeredSensors += gyroSensor
            }
        }

        tiltState.isGyroAvailable = registeredSensors.isNotEmpty()

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return tiltState
}
