package io.github.sixzleo.tabfold.auto

/** Opening may settle below calibration; closing starts from the actual resting pose. */
class OpenPosition(savedOpen: Float, tolerance: Float, private val closingReference: Float? = null) {
    val threshold = (savedOpen - tolerance.coerceIn(1f, 8f)).coerceIn(15f, 180f)
    private var lowest = Float.POSITIVE_INFINITY
    private var returned = false
    fun reached(angle: Float): Boolean {
        if (!angle.isFinite()) return false
        val reference = closingReference ?: return angle >= threshold
        lowest = minOf(lowest, angle)
        if (angle - lowest >= .4f) returned = true
        // The tolerance must not cancel the beginning of a genuine closing movement.
        return angle >= reference - .12f || (returned && angle >= threshold)
    }
}
