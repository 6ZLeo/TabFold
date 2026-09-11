package io.github.sixzleo.tabfold.auto

/** Observe a real decrease from the latest open pose; small resting jitter is ignored. */
class ClosingDetector(private val threshold: Float = 0.55f) {
    private var peak = Float.NaN
    private var previous = Float.NaN
    private var fallingSamples = 0
    val openReference: Float get() = peak
    fun reset() { peak = Float.NaN; previous = Float.NaN; fallingSamples = 0 }
    fun observe(angle: Float): Boolean {
        if (!angle.isFinite() || angle !in 0f..180f) { reset(); return false }
        if (!peak.isFinite() || angle > peak) peak = angle
        val decrease = previous - angle
        if (decrease > 0.025f) fallingSamples++ else if (decrease < -0.025f) fallingSamples = 0
        previous = angle
        return peak >= 15f && peak - angle >= threshold &&
            (fallingSamples >= 2 || decrease >= 1.5f)
    }
}
