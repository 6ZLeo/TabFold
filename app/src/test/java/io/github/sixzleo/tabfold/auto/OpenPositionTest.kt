package io.github.sixzleo.tabfold.auto

import org.junit.Assert.*
import org.junit.Test

class OpenPositionTest {
    @Test fun openingFinishesInsideToleranceAndRemainsOpenBeyondIt() {
        val position = OpenPosition(120f, 3f)
        assertFalse(position.reached(116.9f))
        assertTrue(position.reached(117f))
        assertTrue(position.reached(123f))
        assertTrue(position.reached(130f))
        assertFalse(position.reached(Float.NaN))
    }
    @Test fun toleranceDoesNotSuppressSensitiveClosing() {
        val position = OpenPosition(120f, 3f, 121f)
        assertFalse(position.reached(120.4f))
        assertFalse(position.reached(119f))
        assertFalse(position.reached(100f))
        assertFalse(position.reached(101f))
        assertTrue(position.reached(118f))
    }
    @Test fun returningToAnActualLowerRestPositionStillFinishes() {
        val position = OpenPosition(125f, 3f, 110f)
        assertFalse(position.reached(109f))
        assertFalse(position.reached(90f))
        assertTrue(position.reached(110f))
    }
    @Test fun toleranceIsBounded() {
        assertEquals(119f, OpenPosition(120f, -1f).threshold, .001f)
        assertEquals(112f, OpenPosition(120f, 100f).threshold, .001f)
    }
}
