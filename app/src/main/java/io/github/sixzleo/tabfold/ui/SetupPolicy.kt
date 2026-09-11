package io.github.sixzleo.tabfold.ui

enum class SetupStep { UNSUPPORTED, CONSENT, ACCESSIBILITY, CALIBRATION, START, READY }

object SetupPolicy {
    fun step(capable: Boolean, consent: Boolean, permission: Boolean, connected: Boolean,
             keyboard: Boolean, landscape: Boolean, calibrated: Boolean, enabled: Boolean): SetupStep = when {
        !capable -> SetupStep.UNSUPPORTED
        !consent -> SetupStep.CONSENT
        !permission || !connected -> SetupStep.ACCESSIBILITY
        !keyboard || !landscape || !calibrated -> SetupStep.CALIBRATION
        !enabled -> SetupStep.START
        else -> SetupStep.READY
    }
}
