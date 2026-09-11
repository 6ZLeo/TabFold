package io.github.sixzleo.tabfold.ui

import org.junit.Assert.*
import org.junit.Test

class SetupPolicyTest {
    private fun step(capable: Boolean = true, consent: Boolean = true, permission: Boolean = true,
        connected: Boolean = true, keyboard: Boolean = true, landscape: Boolean = true,
        calibrated: Boolean = true, enabled: Boolean = true) =
        SetupPolicy.step(capable, consent, permission, connected, keyboard, landscape, calibrated, enabled)

    @Test fun consentAndCapabilityBlockStartEvenWithSavedEnabledFlag() {
        assertEquals(SetupStep.UNSUPPORTED, step(capable = false))
        assertEquals(SetupStep.CONSENT, step(consent = false))
    }
    @Test fun permissionMustBeGrantedAndServiceActuallyConnected() {
        assertEquals(SetupStep.ACCESSIBILITY, step(permission = false))
        assertEquals(SetupStep.ACCESSIBILITY, step(connected = false))
    }
    @Test fun missingPhysicalSetupCannotDisplayReady() {
        assertEquals(SetupStep.CALIBRATION, step(keyboard = false))
        assertEquals(SetupStep.CALIBRATION, step(landscape = false))
        assertEquals(SetupStep.CALIBRATION, step(calibrated = false))
        assertEquals(SetupStep.START, step(enabled = false))
        assertEquals(SetupStep.READY, step())
    }
}
