package io.github.sixzleo.tabfold.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.exp
import io.github.sixzleo.tabfold.SettingsStore
import io.github.sixzleo.tabfold.R
import io.github.sixzleo.tabfold.device.DeviceProfile

data class MotionReading(val angle: Float = 0f, val lateral: Float = 0f,
    val hz: Float = 0f, val ageMs: Long = 0, val ready: Boolean = false)

class FoldMotionModel(context: Context, private val rotation: () -> Int) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs = SettingsStore.prefs(context)
    private val sensor = DeviceProfile.motionSensor(context)
    val hasSensor = sensor != null
    val sourceLabel = when (sensor?.type) {
        Sensor.TYPE_GAME_ROTATION_VECTOR -> R.string.source_game
        Sensor.TYPE_ROTATION_VECTOR -> R.string.source_rotation
        Sensor.TYPE_GRAVITY -> R.string.source_gravity
        Sensor.TYPE_ACCELEROMETER -> R.string.source_accel
        else -> R.string.unavailable
    }
    private val matrix = FloatArray(9)
    private val screen = FloatArray(9)
    private var lastNs = 0L
    private var publishedNs = 0L
    private var windowNs = 0L
    private var count = 0
    private var hz = 0f
    private var angle = 0f
    private var started = false
    private var traceUntil = 0L
    private val _reading = MutableStateFlow(MotionReading())
    val reading = _reading.asStateFlow()
    private val _opening = MutableStateFlow(0f)
    val opening = _opening.asStateFlow()
    private val _fullyOpen = MutableStateFlow(prefs.getFloat("fully_open", 110f).coerceIn(45f, 160f))
    val fullyOpen = _fullyOpen.asStateFlow()
    private val _message = MutableStateFlow(R.string.calibration_default)
    val message = _message.asStateFlow()
    fun hasFreshReading(): Boolean = started && lastNs != 0L &&
        SystemClock.elapsedRealtimeNanos() - lastNs < 500_000_000L

    fun start(samplingPeriodUs: Int = 10_000) {
        if (started) return
        lastNs = 0L; publishedNs = 0L; windowNs = 0L; count = 0; hz = 0f
        _reading.value = MotionReading()
        started = sensor?.let { manager.registerListener(this, it, samplingPeriodUs, 0) } ?: false
        if (!started) _message.value = R.string.sensor_unavailable
    }
    fun changeSamplingPeriod(samplingPeriodUs: Int) {
        val current = sensor ?: return
        manager.unregisterListener(this)
        started = manager.registerListener(this, current, samplingPeriodUs, 0)
        if (!started) _reading.value = _reading.value.copy(ready = false)
    }
    fun stop() {
        manager.unregisterListener(this)
        started = false
        lastNs = 0L
        traceUntil = 0L
        _reading.value = _reading.value.copy(ready = false)
    }
    fun calibrateOpen(): Boolean {
        val sample = _reading.value
        if (!sample.ready || SystemClock.elapsedRealtimeNanos() - lastNs > 500_000_000L) {
            _message.value = R.string.sensor_wait; return false
        }
        if (sample.angle !in 45f..160f || abs(sample.lateral) > 12f) {
            _message.value = R.string.calibration_invalid; return false
        }
        _fullyOpen.value = sample.angle
        prefs.edit().putFloat("fully_open", sample.angle).apply()
        _message.value = R.string.calibration_saved
        return true
    }
    fun trace30Seconds() {
        traceUntil = SystemClock.elapsedRealtime() + 30_000L
        Log.i("TabFold", "trace_start,elapsed_ms,opening_deg,lateral_deg,hz,delivery_age_ms")
        _message.value = R.string.calibration_trace
    }
    fun keyboardStatus(): String {
        val keyboards = InputDevice.getDeviceIds().map { InputDevice.getDevice(it) }.filterNotNull()
            .filter { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC }
        return if (keyboards.isEmpty()) "当前未识别到实体字母键盘"
            else keyboards.joinToString { it.name }
    }
    override fun onSensorChanged(event: SensorEvent) {
        val normalUp: Float
        val lateral: Float
        if (event.sensor.type == Sensor.TYPE_GRAVITY || event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val length = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
            if (!length.isFinite() || length !in 6f..13f) return
            normalUp = event.values[2] / length
            val right = when (rotation()) {
                Surface.ROTATION_90 -> event.values[1]
                Surface.ROTATION_180 -> -event.values[0]
                Surface.ROTATION_270 -> -event.values[1]
                else -> event.values[0]
            }
            lateral = OpeningMath.lateral(right / length)
        } else {
        SensorManager.getRotationMatrixFromVector(matrix, event.values)
        val axes = when (rotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(matrix, axes.first, axes.second, screen)
            normalUp = screen[8]
            lateral = OpeningMath.lateral(screen[6])
        }
        if (!normalUp.isFinite()) return
        val target = OpeningMath.angle(normalUp)
        val dt = (event.timestamp - lastNs) / 1_000_000_000f
        // Resume at the observed pose, never replay an unobserved closed-screen opening.
        angle = if (lastNs == 0L || dt > 0.25f) target
            else if (sensor?.type == Sensor.TYPE_ACCELEROMETER) angle + (target - angle) * (1f - exp(-dt / .09f))
            else OpeningMath.smooth(angle, target, dt)
        lastNs = event.timestamp
        if (windowNs == 0L) windowNs = event.timestamp
        count++
        val seconds = (event.timestamp - windowNs) / 1_000_000_000f
        if (seconds >= 1f) { hz = (count - 1) / seconds; count = 1; windowNs = event.timestamp }
        _opening.value = angle
        if (event.timestamp - publishedNs >= 100_000_000L) {
            publishedNs = event.timestamp
            val age = ((SystemClock.elapsedRealtimeNanos() - event.timestamp) / 1_000_000).coerceAtLeast(0)
            _reading.value = MotionReading(angle, lateral, hz, age, true)
            if (traceUntil > 0L) {
                val now = SystemClock.elapsedRealtime()
                if (now <= traceUntil) Log.i("TabFold", String.format(Locale.US,
                    "sample,%d,%.3f,%.3f,%.1f,%d", now, angle, lateral, hz, age))
                else { traceUntil = 0L; Log.i("TabFold", "trace_end"); _message.value = R.string.calibration_trace_done }
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
