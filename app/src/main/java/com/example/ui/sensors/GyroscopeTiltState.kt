package com.example.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.atan2
import kotlin.math.sqrt

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
    private var baselinePitch: Float? = null
    private var baselineRoll: Float? = null

    fun updateFromSensor(rawPitchDeg: Float, rawRollDeg: Float) {
        if (baselinePitch == null || baselineRoll == null) {
            baselinePitch = rawPitchDeg
            baselineRoll = rawRollDeg
        }
        val relPitch = ((rawPitchDeg - (baselinePitch ?: 0f))).coerceIn(-24f, 24f)
        val relRoll = ((rawRollDeg - (baselineRoll ?: 0f))).coerceIn(-24f, 24f)
        // Smooth low-pass filter for silky foil shimmer
        sensorPitch = sensorPitch * 0.78f + relPitch * 0.22f
        sensorRoll = sensorRoll * 0.78f + relRoll * 0.22f
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
        if (kotlin.math.abs(dragPitch) < 0.5f) dragPitch = 0f
        if (kotlin.math.abs(dragRoll) < 0.5f) dragRoll = 0f
        recompute()
    }

    fun calibrateCenter() {
        baselinePitch = null
        baselineRoll = null
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

@Composable
fun rememberCardTiltState(): CardTiltState {
    val context = LocalContext.current
    val tiltState = remember { CardTiltState() }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val gyroSensor = if (rotationSensor == null) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        } else null
        val accelSensor = if (rotationSensor == null && gyroSensor == null) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else null

        val activeSensor = rotationSensor ?: gyroSensor ?: accelSensor
        tiltState.isGyroAvailable = activeSensor != null

        var integratedPitch = 0f
        var integratedRoll = 0f
        var lastTimestampNs = 0L

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR,
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat() * 1.4f
                        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat() * 1.4f
                        tiltState.updateFromSensor(pitchDeg, rollDeg)
                    }

                    Sensor.TYPE_GYROSCOPE -> {
                        if (lastTimestampNs != 0L) {
                            val dt = (event.timestamp - lastTimestampNs) / 1_000_000_000f
                            integratedPitch = (integratedPitch + Math.toDegrees((event.values[0] * dt).toDouble()).toFloat())
                                .coerceIn(-24f, 24f) * 0.985f
                            integratedRoll = (integratedRoll + Math.toDegrees((event.values[1] * dt).toDouble()).toFloat())
                                .coerceIn(-24f, 24f) * 0.985f
                            tiltState.updateFromSensor(integratedPitch, integratedRoll)
                        }
                        lastTimestampNs = event.timestamp
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]
                        val pitch = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
                        val roll = Math.toDegrees(atan2(-ax.toDouble(), az.toDouble())).toFloat()
                        tiltState.updateFromSensor(pitch, roll)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (activeSensor != null && sensorManager != null) {
            sensorManager.registerListener(listener, activeSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            if (activeSensor != null && sensorManager != null) {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    return tiltState
}
