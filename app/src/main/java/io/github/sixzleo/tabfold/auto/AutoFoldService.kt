package io.github.sixzleo.tabfold.auto

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.*
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import io.github.sixzleo.tabfold.sensors.FoldMotionModel
import io.github.sixzleo.tabfold.MainActivity
import io.github.sixzleo.tabfold.SettingsStore
import io.github.sixzleo.tabfold.device.DeviceProfile
import android.hardware.input.InputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoFoldService : AccessibilityService() {
    companion object {
        private var active: AutoFoldService? = null
        private val state = MutableStateFlow(false)
        val connected = state.asStateFlow()
        private val detail = MutableStateFlow("等待开启系统权限")
        val status = detail.asStateFlow()
        fun enable(context: Context, value: Boolean) {
            SettingsStore.prefs(context).edit()
                .putBoolean("auto_enabled", value).apply()
            if (!value) { active?.finish("paused"); MotionService.stop(context) }
            else { MotionService.start(context); active?.reconcileWatch() }
            detail.value = if (value) "已就绪 · 回到桌面后开合平板" else "自动动画已暂停"
        }
        fun enableClosing(context: Context, value: Boolean) {
            SettingsStore.prefs(context).edit()
                .putBoolean("close_enabled", value).apply()
            active?.let {
                if (!value && it.closing) it.finish("paused")
                it.reconcileWatch()
            }
        }
        fun setAllApps(context: Context, value: Boolean) {
            SettingsStore.prefs(context).edit().putBoolean("all_apps", value).apply()
            active?.let { it.finish("scope_changed"); it.reconcileWatch() }
        }
        fun backgroundReady() { active?.reconcileWatch() }
        fun backgroundStopped() { active?.finish("background_stopped") }
        fun previewHome(): Boolean {
            val service = active ?: return false
            if (!SettingsStore.enabled(service) || !service.profile.canRun) return false
            service.handler.postDelayed({ service.begin(true) }, 650)
            return true
        }
        fun revoke(context: Context) {
            SettingsStore.prefs(context).edit().putInt("consent_version", 0).putBoolean("auto_enabled", false).apply()
            MotionService.stop(context)
            active?.let { it.finish("paused"); it.disableSelf() }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { SettingsStore.prefs(this) }
    private val profile by lazy { DeviceProfile.read(this) }
    private val keyguard by lazy { getSystemService(KeyguardManager::class.java) }
    private val power by lazy { getSystemService(PowerManager::class.java) }
    private val wm by lazy { getSystemService(WindowManager::class.java) }
    private val screen by lazy { getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY) }
    private var homePackage: String? = null
    private var homeActivity: String? = null
    private var topPackage: String? = null
    private var topWindow = -1
    private var sceneOwner: String? = null
    private var sceneWindow = -1
    private var protectedOwner: String? = null
    private var protectedWindow = -1
    private var captureRetryAt = 0L
    private var motion: FoldMotionModel? = null
    private var overlay: AutoFoldView? = null
    private var snapshot: Bitmap? = null
    private var generation = 0L
    private var running = false
    private var capturePending = false
    private var scene: Scene? = null
    private var startedAt = 0L
    private var shownAt = 0L
    private var demo = false
    private var startRotation = 0
    private var firstReadingLogged = false
    private var nextKeyboardCheck = 0L
    private var keyboardAttached = false
    private var receiverRegistered = false
    private var closing = false
    private var motionTriggered = false
    private var openingReference = 110f
    private var openPosition = OpenPosition(110f, 3f)
    private var watcher: FoldMotionModel? = null
    private var closingDetector = ClosingDetector()
    private var openingDetector = OpeningDetector()
    private var watchRotation = 0
    private var watchKeyboardCheck = 0L
    private var shownCount = 0L
    private var lastShownScene: Scene? = null
    private var lastShownUptime = 0L
    private var lastExit = "none"
    private var lastCaptureError = 0
    private val inputManager by lazy { getSystemService(InputManager::class.java) }
    private val inputListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(id: Int) { if (!running) reconcileWatch() }
        override fun onInputDeviceChanged(id: Int) { if (!running) reconcileWatch() }
        override fun onInputDeviceRemoved(id: Int) {
            if (running && !hasKeyboard()) finish("keyboard_detached") else if (!running) reconcileWatch()
        }
    }

    private fun trace(value: String) { if (prefs.getBoolean("diagnostics", false)) Log.i("TabFold", value) }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> finish("screen_off")
                Intent.ACTION_SCREEN_ON -> {
                    protectedOwner = null; protectedWindow = -1
                    begin(false)
                }
                Intent.ACTION_USER_PRESENT -> {
                    // Never carry a locked-screen snapshot over the unlock boundary.
                    finish("user_present")
                    begin(false)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        setCacheEnabled(false)
        active = this
        state.value = true
        detail.value = if (SettingsStore.enabled(this)) "服务已就绪" else "服务已连接，自动动画尚未开启"
        val resolvedHome = packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)?.activityInfo
        homePackage = resolvedHome?.packageName
        homeActivity = resolvedHome?.name
        registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON); addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_USER_PRESENT)
        }, Context.RECEIVER_NOT_EXPORTED)
        receiverRegistered = true
        inputManager.registerInputDeviceListener(inputListener, handler)
        if (SettingsStore.enabled(this)) MotionService.start(this)
        handler.post(healthCheck)
        trace("connected,home=$homePackage")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!SettingsStore.enabled(this)) { topPackage = null; finish("paused"); return }
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        // Only window ownership is inspected. No text, view tree, key events or gestures.
        if (event.className?.toString() == AutoFoldView::class.java.name) return
        val owner = event.packageName?.toString() ?: return
        trace("window_id=${event.windowId},owner=$owner")
        if (running) trace("window,owner=$owner,class=${event.className},locked=${keyguard.isKeyguardLocked}")
        // Window-level events may be attributed to a framework container rather than this View.
        if (owner == packageName && !MainActivity.isForeground) return
        if (event.className?.toString() == "android.widget.Toast") return
        // Samsung's taskbar/edge panel can belong to the launcher over a different app.
        // Only the actual launcher window can establish HOME eligibility.
        val homeWidget = owner == homePackage && event.className?.toString() !in
            setOf(homeActivity, "com.sec.android.app.launcher.Launcher")
        val previousOwner = topPackage
        val previousWindow = topWindow
        topPackage = AutoPolicy.windowOwner(topPackage, owner, keyguard.isKeyguardLocked, homeWidget)
        // Taskbar and status-bar events do not replace the underlying application window.
        if (!homeWidget && !(owner == "com.android.systemui" && !keyguard.isKeyguardLocked))
            topWindow = event.windowId
        if (previousOwner != topPackage || previousWindow != topWindow) {
            protectedOwner = null; protectedWindow = -1
            closingDetector.reset(); openingDetector.reset()
        }
        if (running && scene != null && (currentScene() != scene ||
            (scene == Scene.APP && !AutoPolicy.sameAppWindow(sceneOwner, topPackage, sceneWindow, topWindow))))
            transition("window_changed")
        if (!running) reconcileWatch()
    }

    private fun currentScene(): Scene? {
        val result = AutoPolicy.scene(keyguard.isKeyguardLocked, topPackage, homePackage,
            SettingsStore.allApps(this), packageName)
        if (result == Scene.APP && (Build.VERSION.SDK_INT < 34 || topWindow < 0 ||
            AutoPolicy.sameAppWindow(protectedOwner, topPackage, protectedWindow, topWindow))) return null
        return result
    }

    private fun captureIsCurrent(request: Scene, id: Long, owner: String?, window: Int): Boolean =
        AutoPolicy.validCapture(request, currentScene(), id, generation) &&
            (request != Scene.APP || AutoPolicy.sameAppWindow(owner, topPackage, window, topWindow))

    private fun transition(reason: String) {
        val next = currentScene()
        finish(reason)
        if (next != null) begin(false)
    }

    private fun hasKeyboard(): Boolean = DeviceProfile.keyboardReady(this)

    private fun begin(isDemo: Boolean, isClosing: Boolean = false, continuingMotion: FoldMotionModel? = null, reference: Float = 110f) {
        finish("new_opening")
        if (!SettingsStore.enabled(this) || !MotionService.running.value || !profile.canRun || !power.isInteractive || !hasKeyboard()) { continuingMotion?.stop(); return }
        if (isDemo && keyguard.isKeyguardLocked) return
        running = true
        demo = isDemo
        closing = isClosing
        motionTriggered = continuingMotion != null
        startedAt = SystemClock.uptimeMillis()
        startRotation = screen.rotation
        keyboardAttached = true
        nextKeyboardCheck = startedAt + 250
        firstReadingLogged = false
        motion = continuingMotion?.also { it.changeSamplingPeriod(10_000) }
            ?: FoldMotionModel(this) { screen.rotation }.also { it.start() }
        openPosition = OpenPosition(motion!!.fullyOpen.value, SettingsStore.openTolerance(this),
            if (isClosing) reference.coerceIn(15f, 180f) else null)
        openingReference = if (isClosing) reference.coerceIn(15f, 180f) else openPosition.threshold
        trace("armed,demo=$demo,direction=${if (closing) "close" else "open"}")
        handler.post(frame)
    }

    private fun stopWatch() {
        handler.removeCallbacks(watchFrame)
        if (watcher != null) trace("closing_watch,stopped")
        watcher?.stop(); watcher = null
        closingDetector.reset()
        openingDetector.reset()
    }

    private fun reconcileWatch() {
        val bounds = wm.currentWindowMetrics.bounds
        if (running || !SettingsStore.enabled(this) || !MotionService.running.value || !profile.canRun ||
            !power.isInteractive || currentScene() == null || !hasKeyboard() || bounds.width() <= bounds.height()) {
            stopWatch(); return
        }
        if (watcher != null) return
        watchRotation = screen.rotation
        watchKeyboardCheck = SystemClock.uptimeMillis() + 500
        closingDetector = ClosingDetector(if (profile.basicMotion) 1.4f else 0.55f)
        openingDetector = OpeningDetector(if (profile.basicMotion) 1.4f else .55f)
        // Monitor only while awake, enabled and in a permitted scene. Capture only on a trigger.
        watcher = FoldMotionModel(this) { screen.rotation }.also { it.start(20_000) }
        trace("closing_watch,started,requested_hz=50,scene=${currentScene()}")
        handler.post(watchFrame)
    }

    private val healthCheck = object : Runnable {
        override fun run() {
            if (!running) reconcileWatch()
            handler.postDelayed(this, 2_000)
        }
    }

    private val watchFrame = object : Runnable {
        override fun run() {
            val model = watcher ?: return
            val now = SystemClock.uptimeMillis()
            if (running || !power.isInteractive || currentScene() == null || screen.rotation != watchRotation) {
                stopWatch(); return
            }
            if (now >= watchKeyboardCheck) {
                watchKeyboardCheck = now + 500
                if (!hasKeyboard()) { stopWatch(); return }
            }
            val sample = model.reading.value
            if (now < captureRetryAt) { closingDetector.reset(); openingDetector.reset() }
            else if (sample.ready && model.hasFreshReading() && kotlin.math.abs(sample.lateral) <= 15f) {
                val angle = model.opening.value
                val isOpening = openingDetector.observe(angle)
                val isClosing = closingDetector.observe(angle)
                if (prefs.getBoolean("close_enabled", true) && isClosing && AutoPolicy.canBeginClosing(angle,
                        sample.lateral, true, true)) {
                    trace("closing_detected,angle=$angle,hz=${sample.hz}")
                    val reference = closingDetector.openReference
                    watcher = null
                    handler.removeCallbacks(this)
                    begin(false, true, model, reference)
                    return
                }
                if (isOpening && AutoPolicy.canBegin(angle, model.fullyOpen.value, sample.lateral, true, true)) {
                    trace("opening_detected,angle=$angle,hz=${sample.hz}")
                    watcher = null
                    handler.removeCallbacks(this)
                    begin(false, continuingMotion = model)
                    return
                }
            } else { closingDetector.reset(); openingDetector.reset() }
            handler.postDelayed(this, 20)
        }
    }

    private val frame = object : Runnable {
        override fun run() {
            if (!running) return
            val now = SystemClock.uptimeMillis()
            val elapsed = now - startedAt
            if (now >= nextKeyboardCheck) { keyboardAttached = hasKeyboard(); nextKeyboardCheck = now + 250 }
            if (!power.isInteractive || !keyboardAttached || screen.rotation != startRotation) {
                finish("environment_changed"); return
            }
            if (elapsed > 12_000) { finish("timeout"); return }
            val current = currentScene()
            if (scene != null && (current != scene || (scene == Scene.APP &&
                !AutoPolicy.sameAppWindow(sceneOwner, topPackage, sceneWindow, topWindow)))) {
                transition("scene_changed"); return
            }
            val model = motion ?: return
            val sample = model.reading.value
            if (sample.ready && !firstReadingLogged) {
                firstReadingLogged = true
                trace("pose,angle=${sample.angle},lateral=${sample.lateral},scene=$current")
            }
            if (scene == null && !capturePending && sample.ready && current != null) {
                val bounds = wm.currentWindowMetrics.bounds
                if (demo || (if (closing) AutoPolicy.canBeginClosing(sample.angle, sample.lateral, keyboardAttached, bounds.width() > bounds.height())
                    else AutoPolicy.canBegin(sample.angle, model.fullyOpen.value, sample.lateral,
                        keyboardAttached, bounds.width() > bounds.height()))) {
                    scene = current
                    sceneOwner = topPackage
                    sceneWindow = topWindow
                    capture(current, generation)
                } else if (elapsed > 550) { finish("pose_outside_opening"); return }
            }
            if (overlay == null && elapsed > 2500 && !capturePending) { finish("no_eligible_window_or_sensor"); return }
            overlay?.let { view ->
                val angle = if (demo) model.fullyOpen.value * ((now - shownAt) / 2400f).coerceIn(0f, 1f)
                    else model.opening.value
                if (!demo && (!sample.ready || !model.hasFreshReading())) { finish("stale_sensor"); return }
                view.opening = angle
                view.fullyOpen = openingReference
                val blend = ((now - shownAt) / 130f).coerceIn(0f, 1f)
                view.effectAmount = if (closing || motionTriggered) blend * blend * (3f - 2f * blend) else 1f
                view.invalidate()
                if (openPosition.reached(angle)) {
                    // Short blend reveals the still-live original window without an abrupt swap.
                    view.animate().alpha(0f).setDuration(120).withEndAction { finish("fully_open") }.start()
                    return
                }
            }
            handler.postDelayed(this, 16)
        }
    }

    private fun capture(requestScene: Scene, id: Long) {
        val light = prefs.getBoolean("lightweight", profile.lowMemory)
        // In an ordinary app, even shading must first respect the system's capture permission.
        if (light && requestScene != Scene.APP) { show(null, requestScene); return }
        val owner = sceneOwner
        val window = sceneWindow
        capturePending = true
        val callback = object : TakeScreenshotCallback {
            override fun onSuccess(result: ScreenshotResult) {
                val buffer = result.hardwareBuffer
                var copied: Bitmap? = null
                try {
                    if (!light && running && captureIsCurrent(requestScene, id, owner, window)) {
                        copied = Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                    }
                } catch (e: RuntimeException) { Log.w("TabFold", "snapshot_conversion_failed", e) }
                finally { buffer.close() }
                if (id != generation || !running) { copied?.recycle(); return }
                capturePending = false
                if (!captureIsCurrent(requestScene, id, owner, window)) {
                    copied?.recycle(); transition("capture_scene_changed"); return
                }
                show(copied, requestScene)
            }
            override fun onFailure(errorCode: Int) {
                if (id != generation || !running) return
                capturePending = false
                lastCaptureError = errorCode
                trace("screenshot_unavailable,scene=$requestScene,error=$errorCode")
                if (!captureIsCurrent(requestScene, id, owner, window)) {
                    transition("capture_scene_changed"); return
                }
                if (requestScene == Scene.APP) {
                    if (errorCode == ERROR_TAKE_SCREENSHOT_SECURE_WINDOW ||
                        errorCode == ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS) {
                        protectedOwner = owner; protectedWindow = window
                    } else captureRetryAt = SystemClock.uptimeMillis() + 1_000
                    finish("app_capture_denied")
                } else show(null, requestScene)
            }
        }
        if (requestScene == Scene.APP && Build.VERSION.SDK_INT >= 34) {
            // A display screenshot can redact secure windows instead of reporting an error.
            // Check the target window first; never fall back to a display capture on refusal.
            takeScreenshotOfWindow(window, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    result.hardwareBuffer.close()
                    if (!running || id != generation) return
                    if (!captureIsCurrent(requestScene, id, owner, window)) {
                        capturePending = false; transition("capture_scene_changed"); return
                    }
                    if (light) { capturePending = false; show(null, requestScene) }
                    else takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, callback)
                }
                override fun onFailure(errorCode: Int) = callback.onFailure(errorCode)
            })
        } else takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, callback)
    }

    private fun show(bitmap: Bitmap?, source: Scene) {
        if (!running || !power.isInteractive) { bitmap?.recycle(); return }
        snapshot = bitmap
        try {
            val view = AutoFoldView(this, bitmap).apply {
                opening = if (demo) 0f else motion!!.opening.value
                fullyOpen = openingReference
                effectAmount = if (closing || motionTriggered) 0f else 1f
                strength = prefs.getFloat("strength", 65f).coerceIn(30f, 70f)
            }
            val layout = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT
            ).apply {
                title = "TabFold transient animation"
                gravity = Gravity.TOP or Gravity.START
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                setFitInsetsTypes(0)
            }
            overlay = view

            wm.addView(view, layout)

            shownAt = SystemClock.uptimeMillis()
            shownCount++; lastShownScene = source; lastShownUptime = shownAt; lastCaptureError = 0
            detail.value = "最近一次：${if (source == Scene.LOCK) "锁屏" else "桌面"} · ${if (closing) "合盖" else "开盖"} · ${if (bitmap == null) "明暗效果" else "画面变形"}"
            trace("shown,scene=$source,snapshot=${bitmap != null},direction=${if (closing) "close" else "open"},delay_ms=${shownAt - startedAt}")
        } catch (e: RuntimeException) {
            Log.e("TabFold", "overlay_failed", e)
            detail.value = "系统未能显示动画，请查看调试记录"
            finish("overlay_failed")
        }
    }

    private fun finish(reason: String) {
        stopWatch()
        generation++
        val wasRunning = running
        running = false
        handler.removeCallbacks(frame)
        overlay?.animate()?.cancel()
        overlay?.let { try { wm.removeViewImmediate(it) } catch (_: IllegalArgumentException) {} }
        overlay = null
        // The detached View's render node may still reference its bitmap until the next render.
        // Release our references; Android frees this in-memory image with that node.
        snapshot = null
        motion?.stop(); motion = null
        scene = null; sceneOwner = null; sceneWindow = -1; capturePending = false
        if (wasRunning) trace("removed,reason=$reason")
        if (wasRunning) lastExit = reason
        if (reason !in setOf("screen_off", "new_opening", "paused", "service_destroyed", "interrupted", "background_stopped"))
            handler.post { if (!running && active === this) reconcileWatch() }
    }

    override fun onInterrupt() { finish("interrupted") }
    // Explicit system diagnostics only; no screen contents, app names or device identifiers.
    override fun dump(fd: java.io.FileDescriptor, writer: java.io.PrintWriter, args: Array<out String>?) {
        val model = motion ?: watcher
        writer.println("TabFold enabled=${SettingsStore.enabled(this)} foreground=${MotionService.running.value} awake=${power.isInteractive}")
        writer.println("scene=${currentScene()} running=$running watching=${watcher != null} capturing=$capturePending")
        writer.println("sensorFresh=${model?.hasFreshReading()} hz=${model?.reading?.value?.hz} angle=${model?.opening?.value}")
        writer.println("shownCount=$shownCount lastScene=$lastShownScene lastShownUptime=$lastShownUptime lastExit=$lastExit captureError=$lastCaptureError")
    }
    override fun onDestroy() {
        finish("service_destroyed")
        handler.removeCallbacksAndMessages(null)
        if (receiverRegistered) unregisterReceiver(receiver)
        inputManager.unregisterInputDeviceListener(inputListener)
        if (active === this) { active = null; state.value = false; detail.value = "自动开盖服务已关闭" }
        super.onDestroy()
    }
}
