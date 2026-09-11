package io.github.sixzleo.tabfold.auto

import org.junit.Assert.*
import org.junit.Test

class ClosingDetectorTest {
    @Test fun restingNoiseAndOpeningDoNotTrigger() {
        val detector = ClosingDetector()
        for (angle in listOf(90f, 90.3f, 89.5f, 91f, 100f, 125f, 124.1f))
            assertFalse(detector.observe(angle))
    }
    @Test fun ClosingFromFullOrHalfOpenTriggers() {
        val detector = ClosingDetector()
        assertFalse(detector.observe(125f))
        assertFalse(detector.observe(124.8f))
        assertTrue(detector.observe(120f))
        detector.reset()
        assertFalse(detector.observe(55f))
        assertTrue(detector.observe(50f))
    }
    @Test fun screenOrAppTransitionDoesNotReuseOldPose() {
        val detector = ClosingDetector()
        detector.observe(125f)
        detector.reset()
        assertFalse(detector.observe(30f))
        assertFalse(detector.observe(Float.NaN))
        assertFalse(detector.observe(20f))
        assertTrue(detector.observe(15f))
    }
    @Test fun gentleContinuousClosingTriggersBeforeOneDegree() {
        val detector = ClosingDetector()
        assertFalse(detector.observe(125f))
        assertFalse(detector.observe(124.8f))
        assertTrue(detector.observe(124.4f))
        assertEquals(125f, detector.openReference, 0.01f)
        assertTrue(AutoPolicy.canBeginClosing(140f, 0f, true, true))
        assertFalse(AutoPolicy.canBeginClosing(140f, 0f, false, true))
    }
}
