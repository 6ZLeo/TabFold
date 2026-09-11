package io.github.sixzleo.tabfold.auto

import org.junit.Assert.*
import org.junit.Test

class AutoPolicyTest {
    @Test fun allAppsRequiresScopeChoiceAndExcludesPermissionPages() {
        assertEquals(Scene.APP, AutoPolicy.scene(false, "calculator", "home", true, "tabfold"))
        assertNull(AutoPolicy.scene(false, "calculator", "home", false, "tabfold"))
        for (owner in listOf("tabfold", "android", "com.android.settings", "com.android.systemui",
            "com.samsung.android.packageinstaller", "com.google.android.permissioncontroller"))
            assertNull(AutoPolicy.scene(false, owner, "home", true, "tabfold"))
        assertNull(AutoPolicy.scene(true, "calculator", "home", true, "tabfold"))
        assertNull(AutoPolicy.scene(false, null, "home", true, "tabfold"))
    }
    @Test fun appCaptureCannotCrossPackageOrWindowBoundaries() {
        assertTrue(AutoPolicy.sameAppWindow("a", "a", 5, 5))
        assertFalse(AutoPolicy.sameAppWindow("a", "b", 5, 5))
        assertFalse(AutoPolicy.sameAppWindow("a", "a", 5, 6))
        assertFalse(AutoPolicy.sameAppWindow(null, null, 5, 5))
        assertFalse(AutoPolicy.sameAppWindow("a", "a", -1, -1))
    }
    @Test fun launcherTaskbarOverAnotherAppDoesNotQualifyAsHome() {
        assertEquals("bank", AutoPolicy.windowOwner("bank", "home", false, homeWidget = true))
        assertNull(AutoPolicy.windowOwner(null, "home", false, homeWidget = true))
        assertEquals("home", AutoPolicy.windowOwner("bank", "home", false, homeWidget = false))
    }
    @Test fun statusBarWakeEventDoesNotReplaceTheUnderlyingApp() {
        assertEquals("home", AutoPolicy.windowOwner("home", "com.android.systemui", false))
        assertEquals("bank", AutoPolicy.windowOwner("bank", "com.android.systemui", false))
        assertNull(AutoPolicy.windowOwner(null, "com.android.systemui", false))
        assertEquals("bank", AutoPolicy.windowOwner("home", "bank", false))
        assertEquals("com.android.systemui", AutoPolicy.windowOwner("home", "com.android.systemui", true))
    }
    @Test fun unlockedOtherAppsNeverQualify() {
        assertNull(AutoPolicy.scene(false, "bank", "home"))
        assertNull(AutoPolicy.scene(false, null, null))
        assertEquals(Scene.HOME, AutoPolicy.scene(false, "home", "home"))
        assertEquals(Scene.LOCK, AutoPolicy.scene(true, "home", "home"))
        assertNull(AutoPolicy.scene(true, "camera", "home"))
    }
    @Test fun captureCannotSurviveUnlockOrNewOpening() {
        assertFalse(AutoPolicy.validCapture(Scene.LOCK, Scene.HOME, 4, 4))
        assertFalse(AutoPolicy.validCapture(Scene.HOME, Scene.LOCK, 4, 4))
        assertFalse(AutoPolicy.validCapture(Scene.HOME, Scene.HOME, 3, 4))
        assertFalse(AutoPolicy.validCapture(Scene.HOME, null, 4, 4))
        assertTrue(AutoPolicy.validCapture(Scene.LOCK, Scene.LOCK, 4, 4))
    }
    @Test fun ordinaryUprightWakeAndInvalidPosesDoNotAnimate() {
        assertTrue(AutoPolicy.canBegin(30f, 120f, 0f, true, true))
        assertFalse(AutoPolicy.canBegin(119f, 120f, 0f, true, true))
        assertFalse(AutoPolicy.canBegin(30f, 120f, 16f, true, true))
        assertFalse(AutoPolicy.canBegin(30f, 120f, 0f, false, true))
        assertFalse(AutoPolicy.canBegin(30f, 120f, 0f, true, false))
        assertFalse(AutoPolicy.canBegin(Float.NaN, 120f, 0f, true, true))
    }
}
