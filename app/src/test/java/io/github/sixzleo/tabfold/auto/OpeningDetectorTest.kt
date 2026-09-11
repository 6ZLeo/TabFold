package io.github.sixzleo.tabfold.auto

import org.junit.Assert.*
import org.junit.Test

class OpeningDetectorTest {
    @Test fun noiseAndClosingDoNotTrigger() {
        val detector = OpeningDetector()
        listOf(90f, 89.9f, 90.1f, 90f, 89f, 87f, 85f).forEach { assertFalse(detector.observe(it)) }
    }
    @Test fun gentleReopeningTriggersWithoutScreenWake() {
        val detector = OpeningDetector()
        assertFalse(detector.observe(30f))
        assertFalse(detector.observe(30.2f))
        assertTrue(detector.observe(30.6f))
    }
    @Test fun windowChangeAndInvalidSampleDiscardPreviousMotion() {
        val detector = OpeningDetector()
        detector.observe(10f)
        detector.reset()
        assertFalse(detector.observe(80f))
        assertFalse(detector.observe(80.1f))
        assertFalse(detector.observe(Float.NaN))
        assertFalse(detector.observe(90f))
        assertFalse(detector.observe(90.2f))
    }
}
