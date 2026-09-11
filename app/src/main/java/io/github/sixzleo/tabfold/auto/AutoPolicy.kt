package io.github.sixzleo.tabfold.auto

enum class Scene { HOME, LOCK }

object AutoPolicy {
    // System UI also emits a FrameLayout event for the status bar after waking.
    // Such an event does not establish that the underlying activity has changed.
    fun windowOwner(previous: String?, owner: String, locked: Boolean, homeWidget: Boolean = false): String? =
        if (homeWidget || (owner == "com.android.systemui" && !locked)) previous else owner

    fun scene(locked: Boolean, topPackage: String?, homePackage: String?): Scene? =
        if (locked) {
            if (topPackage == null || topPackage == "com.android.systemui" || topPackage == homePackage) Scene.LOCK else null
        } else if (homePackage != null && topPackage == homePackage) Scene.HOME else null

    fun canBegin(angle: Float, fullyOpen: Float, lateral: Float, keyboard: Boolean, landscape: Boolean): Boolean =
        keyboard && landscape && angle.isFinite() && lateral.isFinite() &&
            angle >= 0f && angle < fullyOpen - 6f && kotlin.math.abs(lateral) <= 15f

    fun validCapture(requestScene: Scene, currentScene: Scene?, requestId: Long, currentId: Long): Boolean =
        requestId == currentId && requestScene == currentScene
    fun canBeginClosing(angle: Float, lateral: Float, keyboard: Boolean, landscape: Boolean): Boolean =
        keyboard && landscape && angle.isFinite() && angle in 1f..180f && lateral.isFinite() && kotlin.math.abs(lateral) <= 15f
}
