package io.github.sixzleo.tabfold.sensors

import kotlin.math.*

/** Estimated opening of a face-down cover, with a horizontal, stationary base. */
object OpeningMath {
    fun angle(normalUp: Float): Float = Math.toDegrees(acos((-normalUp).coerceIn(-1f, 1f)).toDouble()).toFloat()
    fun progress(angle: Float, fullyOpen: Float): Float = (angle / fullyOpen.coerceIn(15f, 180f)).coerceIn(0f, 1f)
    fun tilt(angle: Float, fullyOpen: Float, strength: Float = 65f): Float {
        val p = progress(angle, fullyOpen)
        // Increase visible change near the open position without losing endpoints.
        return (1f - p * p) * strength.coerceIn(30f, 70f)
    }
    fun smooth(previous: Float, target: Float, dt: Float): Float =
        previous + (target - previous) * (1f - exp(-dt.coerceIn(0f, 0.25f) / 0.012f))
    fun lateral(rightUp: Float): Float = Math.toDegrees(asin(rightUp.coerceIn(-1f, 1f)).toDouble()).toFloat()
}
