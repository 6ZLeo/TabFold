package io.github.sixzleo.tabfold.auto

/** Detects reopening while the display stays on, including after an animation times out. */
class OpeningDetector(private val threshold: Float = .55f) {
    private var minimum = Float.NaN
    private var previous = Float.NaN
    private var rising = 0
    fun reset() { minimum = Float.NaN; previous = Float.NaN; rising = 0 }
    fun observe(angle: Float): Boolean {
        if (!angle.isFinite()) { reset(); return false }
        if (minimum.isNaN() || angle < minimum) minimum = angle
        rising = if (previous.isFinite() && angle - previous > .025f) rising + 1 else 0
        previous = angle
        return angle - minimum >= threshold && rising >= 2
    }
}
