package io.github.sixzleo.tabfold

import org.junit.Assert.*
import org.junit.Test

class ConsentPolicyTest {
    @Test fun installationOrPermissionAloneDoesNotEnableProcessing() {
        assertFalse(ConsentPolicy.canActivate(0, 1, true))
        assertFalse(ConsentPolicy.canActivate(0, 1, false))
        assertTrue(ConsentPolicy.canActivate(1, 1, true))
    }
    @Test fun revocationPauseAndMaterialNoticeUpdateBlockActivation() {
        assertFalse(ConsentPolicy.canActivate(1, 1, false))
        assertFalse(ConsentPolicy.canActivate(1, 2, true))
        assertFalse(ConsentPolicy.canActivate(0, 0, true))
    }
}
