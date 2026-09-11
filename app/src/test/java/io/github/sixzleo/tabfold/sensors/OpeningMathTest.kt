package io.github.sixzleo.tabfold.sensors

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.cos

class OpeningMathTest {
    @Test fun tracksClosedVerticalAndPastVerticalWithoutFoldingBack() {
        for (expected in listOf(0f, 30f, 60f, 90f, 110f, 150f, 180f)) {
            assertEquals(expected, OpeningMath.angle(-cos(Math.toRadians(expected.toDouble())).toFloat()), 0.01f)
        }
    }
    @Test fun remainsAtHalfOpenAfterLongHold() {
        var angle = 20f
        repeat(10000) { angle = OpeningMath.smooth(angle, 55f, 0.01f) }
        assertEquals(55f, angle, 0.001f)
        assertEquals(48.75f, OpeningMath.tilt(angle, 110f), 0.001f)
    }
    @Test fun reversalAndEndStopsStayContinuous() {
        val values = (0..110).map { OpeningMath.tilt(it.toFloat(), 110f) }
        assertTrue(values.zipWithNext().all { (a, b) -> a >= b })
        assertEquals(65f, values.first(), 0f)
        assertEquals(0f, OpeningMath.tilt(140f, 110f), 0f)
        assertEquals(65f, OpeningMath.tilt(-1f, 110f), 0f)
    }
    @Test fun smoothingIndependentOfSampleRate() {
        var a = 0f; var b = 0f
        repeat(10) { a = OpeningMath.smooth(a, 90f, 0.01f) }
        repeat(5) { b = OpeningMath.smooth(b, 90f, 0.02f) }
        assertEquals(a, b, 0.001f)
    }
}
