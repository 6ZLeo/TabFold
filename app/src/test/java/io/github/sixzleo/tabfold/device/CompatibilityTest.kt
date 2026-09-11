package io.github.sixzleo.tabfold.device

import org.junit.Assert.*
import org.junit.Test

class CompatibilityTest {
    @Test fun tabletsRemainTabletsInSmallWindowsButFoldPhonesDoNot() {
        assertTrue(Compatibility.samsungTablet("samsung", "SM-X920", 400))
        assertTrue(Compatibility.samsungTablet("Samsung", "SM-T970", 600))
        assertFalse(Compatibility.samsungTablet("samsung", "SM-F946B", 800))
        assertFalse(Compatibility.samsungTablet("other", "SM-X920", 900))
    }
    @Test fun modelLabelIsNotACompatibilityGuarantee() {
        assertFalse(Compatibility.canRun(32, true, true))
        assertFalse(Compatibility.canRun(35, true, false))
        assertTrue(Compatibility.canRun(33, true, true))
    }
    @Test fun multipleOfficialKeyboardsAndOptInAlternativesAreRecognized() {
        assertTrue(Compatibility.keyboardAccepted("Book Cover Keyboard (EF-DX715)", false, true, false))
        assertTrue(Compatibility.keyboardAccepted("Book Cover Keyboard (EF-DX925)", false, true, false))
        assertFalse(Compatibility.keyboardAccepted("Samsung Keyboard", true, true, true))
        assertFalse(Compatibility.keyboardAccepted("USB Keyboard", false, true, false))
        assertTrue(Compatibility.keyboardAccepted("USB Keyboard", false, true, true))
        assertFalse(Compatibility.keyboardAccepted("Touchpad", false, false, true))
    }
}
