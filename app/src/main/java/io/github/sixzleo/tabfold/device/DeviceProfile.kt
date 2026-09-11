package io.github.sixzleo.tabfold.device

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.InputDevice
import io.github.sixzleo.tabfold.SettingsStore
import org.json.JSONObject

data class DeviceProfile(val name: String, val model: String, val androidVersion: String,
    val sdk: Int, val displayInfo: String, val sensorInfo: String, val samsungTablet: Boolean,
    val hasMotion: Boolean, val basicMotion: Boolean, val lowMemory: Boolean) {
    val canRun get() = Compatibility.canRun(sdk, samsungTablet, hasMotion)
    val verdict get() = when {
        !samsungTablet -> "当前设备不是已识别的三星平板"
        !hasMotion -> "缺少可用姿态传感器，无法启用"
        basicMotion -> "具备基础能力 · 动态角度精度有限"
        else -> "具备所需能力 · 实际效果需校准验证"
    }
    fun report(keyboardNames: String) = """
        TabFold compatibility report
        Device: $name ($model)
        Android: $androidVersion / API $sdk
        Display: $displayInfo
        Motion: $sensorInfo
        Basic motion: $basicMotion
        Low-memory device: $lowMemory
        Keyboards: $keyboardNames
        Capability check: $canRun
        This is a capability check, not a guarantee of compatibility.
        No serial number, account, device identifier or screen image included.
    """.trimIndent()

    companion object {
        fun motionSensor(context: Context): Sensor? {
            val manager = context.getSystemService(SensorManager::class.java)
            return listOf(Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER).firstNotNullOfOrNull { manager.getDefaultSensor(it) }
        }
        fun read(context: Context): DeviceProfile {
            val names = runCatching { JSONObject(context.assets.open("device_names.json").bufferedReader().use { it.readText() }) }.getOrNull()
            val model = Build.MODEL.orEmpty()
            val marketing = names?.optString(model)?.takeIf { it.isNotBlank() } ?: "${Build.MANUFACTURER} $model"
            val display = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
            val mode = display?.mode
            val refresh = display?.supportedModes?.maxOfOrNull { it.refreshRate }?.toInt() ?: 0
            val sensor = motionSensor(context)
            return DeviceProfile(marketing, model, Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
                "${mode?.physicalWidth ?: 0} × ${mode?.physicalHeight ?: 0} · 最高 $refresh Hz",
                sensor?.name ?: "不可用", Compatibility.samsungTablet(Build.MANUFACTURER, model,
                    context.resources.configuration.smallestScreenWidthDp), sensor != null,
                sensor?.type in listOf(Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER),
                context.getSystemService(ActivityManager::class.java).isLowRamDevice)
        }
        fun keyboards(): List<InputDevice> = InputDevice.getDeviceIds().map { InputDevice.getDevice(it) }.filterNotNull()
            .filter { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC }
        fun keyboardNames() = keyboards().joinToString { it.name }.ifBlank { "未连接实体键盘" }
        fun keyboardReady(context: Context): Boolean {
            val allowOther = SettingsStore.prefs(context).getBoolean("allow_other_keyboard", false)
            return keyboards().any { Compatibility.keyboardAccepted(it.name, it.isVirtual, true, allowOther) }
        }
    }
}
