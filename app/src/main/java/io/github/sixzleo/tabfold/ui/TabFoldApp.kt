package io.github.sixzleo.tabfold.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sixzleo.tabfold.BuildConfig
import io.github.sixzleo.tabfold.MainActivity
import io.github.sixzleo.tabfold.R
import io.github.sixzleo.tabfold.SettingsStore
import io.github.sixzleo.tabfold.auto.AutoFoldService
import io.github.sixzleo.tabfold.device.DeviceProfile
import io.github.sixzleo.tabfold.sensors.FoldMotionModel
import kotlinx.coroutines.delay

private val Accent = Color(0xFF94DFCE)
private val Muted = Color(0xFFA6B4BE)
private val Panel = Color(0xFF1B232D)

@Composable
fun TabFoldApp(motion: FoldMotionModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val prefs = remember { SettingsStore.prefs(context) }
    var profile by remember { mutableStateOf(DeviceProfile.read(context)) }
    var keyboards by remember { mutableStateOf(DeviceProfile.keyboardNames()) }
    var keyboardReady by remember { mutableStateOf(DeviceProfile.keyboardReady(context)) }
    var consent by remember { mutableStateOf(SettingsStore.consented(context)) }
    var enabled by remember { mutableStateOf(SettingsStore.enabled(context)) }
    var closing by remember { mutableStateOf(prefs.getBoolean("close_enabled", true)) }
    var otherKeyboard by remember { mutableStateOf(prefs.getBoolean("allow_other_keyboard", false)) }
    var lightweight by remember { mutableStateOf(prefs.getBoolean("lightweight", profile.lowMemory)) }
    var diagnostics by remember { mutableStateOf(prefs.getBoolean("diagnostics", false)) }
    var strength by remember { mutableFloatStateOf(prefs.getFloat("strength", 65f)) }
    var legal by remember { mutableStateOf<String?>(null) }
    var consentDialog by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }
    val connected by AutoFoldService.connected.collectAsState()
    val status by AutoFoldService.status.collectAsState()
    val reading by motion.reading.collectAsState()
    val fullyOpen by motion.fullyOpen.collectAsState()
    val message by motion.message.collectAsState()
    val palette = darkColorScheme(primary = Accent, onPrimary = Color(0xFF12382F),
        background = Color(0xFF10151B), surface = Panel, onSurface = Color(0xFFECF1F5),
        onSurfaceVariant = Muted, secondary = Accent)

    LaunchedEffect(Unit) {
        while (true) {
            if (MainActivity.isForeground) {
            keyboards = DeviceProfile.keyboardNames()
            keyboardReady = DeviceProfile.keyboardReady(context)
            }
            delay(1000)
        }
    }

    MaterialTheme(colorScheme = palette) {
        Surface(Modifier.fillMaxSize(), color = palette.background) {
            BoxWithConstraints(Modifier.safeDrawingPadding().fillMaxSize()) {
                val wide = maxWidth >= 840.dp
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (wide) 36.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.widthIn(max = 1200.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_tabfold), contentDescription = null, Modifier.size(46.dp))
                        Column(Modifier.padding(start = 14.dp).weight(1f)) {
                            Text("TabFold", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("让开合之间，多一点动感", color = Muted, fontSize = 13.sp)
                        }
                        Text("BETA  ${BuildConfig.VERSION_NAME.substringBefore('-')}", color = Accent,
                            style = MaterialTheme.typography.labelMedium)
                    }
                    Row(Modifier.widthIn(max = 1200.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Block("你的平板", "在本机识别 · 不上传设备信息") {
                                Text(profile.name, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                                Text(profile.model, color = Muted)
                                Spacer(Modifier.height(8.dp))
                                Text(profile.verdict, color = if (profile.canRun) Accent else Color(0xFFFFCA8C))
                                InfoRow("系统", "Android ${profile.androidVersion} · API ${profile.sdk}")
                                InfoRow("屏幕", profile.displayInfo)
                                InfoRow("姿态来源", motion.sourceName)
                                Text("型号名称识别不等于实测兼容。新型号会显示系统报告的原始名称。", fontSize = 12.sp, color = Muted)
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                Text("键盘与保护套", fontWeight = FontWeight.Medium)
                                Text(keyboards, fontSize = 13.sp, color = Muted)
                                Text(if (keyboardReady) "键盘已识别" else "连接键盘后才能自动触发", color = if (keyboardReady) Accent else Muted)
                                Text("键盘底座需要平放且保持不动。角度来自平板姿态估算，不是键盘铰链的直接测量。", fontSize = 12.sp, color = Muted)
                                OutlinedButton(onClick = {
                                    profile = DeviceProfile.read(context)
                                    clipboard.setText(AnnotatedString(profile.report(DeviceProfile.keyboardNames())))
                                    feedback = "兼容性报告已复制；不含序列号、账号或屏幕画面。"
                                }) { Text("复制兼容性报告") }
                            }
                            if (wide) TrustBlock(consent, onRisk = { legal = "DISCLAIMER.md" }, onPrivacy = { legal = "PRIVACY.md" },
                                onLicense = { legal = "LICENSES" }, onRevoke = {
                                    AutoFoldService.revoke(context); consent = false; enabled = false; motion.stop()
                                    feedback = "已撤回同意并停用。本应用的校准设置仍保留。"
                                })
                            if (!wide) ControlBlock(motion, profile, consent, connected, enabled, closing, otherKeyboard,
                                lightweight, diagnostics, strength, reading.ready, reading.angle, fullyOpen, message, status, keyboardReady,
                                onConsent = { consentDialog = true }, onEnabled = { enabled = it; AutoFoldService.enable(context, it) },
                                onClosing = { closing = it; AutoFoldService.enableClosing(context, it) },
                                onOther = { otherKeyboard = it; prefs.edit().putBoolean("allow_other_keyboard", it).apply() },
                                onLight = { lightweight = it; prefs.edit().putBoolean("lightweight", it).apply() },
                                onDiagnostics = { diagnostics = it; prefs.edit().putBoolean("diagnostics", it).apply() },
                                onStrength = { strength = it }, onStrengthSaved = { prefs.edit().putFloat("strength", strength).apply() })
                        }
                        if (wide) Column(Modifier.weight(1.25f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            ControlBlock(motion, profile, consent, connected, enabled, closing, otherKeyboard,
                                lightweight, diagnostics, strength, reading.ready, reading.angle, fullyOpen, message, status, keyboardReady,
                                onConsent = { consentDialog = true }, onEnabled = { enabled = it; AutoFoldService.enable(context, it) },
                                onClosing = { closing = it; AutoFoldService.enableClosing(context, it) },
                                onOther = { otherKeyboard = it; prefs.edit().putBoolean("allow_other_keyboard", it).apply() },
                                onLight = { lightweight = it; prefs.edit().putBoolean("lightweight", it).apply() },
                                onDiagnostics = { diagnostics = it; prefs.edit().putBoolean("diagnostics", it).apply() },
                                onStrength = { strength = it }, onStrengthSaved = { prefs.edit().putFloat("strength", strength).apply() })
                        }
                    }
                    if (!wide) TrustBlock(consent, onRisk = { legal = "DISCLAIMER.md" }, onPrivacy = { legal = "PRIVACY.md" },
                        onLicense = { legal = "LICENSES" }, onRevoke = {
                            AutoFoldService.revoke(context); consent = false; enabled = false; motion.stop()
                        })
                    if (feedback.isNotBlank()) Text(feedback, color = Accent, fontSize = 13.sp)
                    Row(Modifier.widthIn(max = 1200.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("个人兴趣 · 免费开源 · 非官方项目", Modifier.weight(1f), color = Muted, fontSize = 12.sp)
                        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/6ZLeo/TabFold"))) }) { Text("GitHub ↗") }
                    }
                }
            }
        }
        if (consentDialog) {
            var understood by remember { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { consentDialog = false }, title = { Text("启用前，请先了解") },
                text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("这是免费开源的实验性动画工具，并非三星、Google 或 Apple 官方产品。")
                    Text("• 无障碍权限用于判断桌面/锁屏及截取当前画面。截图仅在内存中使用，本应用不联网、不保存或上传截图。")
                    Text("• 合盖检测会在符合条件的亮屏桌面/锁屏上读取姿态，可能增加耗电和发热；开合动画可能出现延迟、闪烁或短暂画面冻结。")
                    Text("• 不保证适配每个型号。请备份重要数据、保持键盘水平，避免过度弯折附件；出现异常应停用。")
                    Text("• 软件按现状提供。在法律允许范围内排除担保并限制责任；法律禁止排除的责任不受影响，不能保证零风险或绝对免责。")
                    TextButton(onClick = { legal = "DISCLAIMER.md" }) { Text("阅读完整风险与责任说明") }
                    TextButton(onClick = { legal = "PRIVACY.md" }) { Text("阅读隐私说明") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = understood, onCheckedChange = { understood = it })
                        Text("我已阅读并理解上述说明，自愿启用所述本地处理。", fontSize = 13.sp)
                    }
                } },
                confirmButton = { Button(enabled = understood && profile.canRun, onClick = {
                    prefs.edit().putInt("consent_version", SettingsStore.CONSENT_VERSION).putBoolean("auto_enabled", true).apply()
                    consent = true; enabled = true; consentDialog = false; motion.start()
                    AutoFoldService.enable(context, true)
                }) { Text("确认并启用") } },
                dismissButton = { TextButton(onClick = { consentDialog = false }) { Text("暂不启用") } })
        }
        legal?.let { file ->
            val content = remember(file) {
                val assetPath = if (file == "LICENSES") "licenses/THIRD_PARTY_NOTICES.txt" else "legal/$file"
                context.assets.open(assetPath).bufferedReader().use { it.readText() }
                    .replace(Regex("(?m)^#{1,6}\\s*"), "").replace("**", "")
            }
            AlertDialog(onDismissRequest = { legal = null }, title = { Text(when (file) {
                "DISCLAIMER.md" -> "风险与责任说明"; "PRIVACY.md" -> "隐私说明"; else -> "开源许可证与致谢"
            }) }, text = { Text(content, Modifier.heightIn(max = 580.dp).verticalScroll(rememberScrollState()), fontSize = 13.sp, lineHeight = 21.sp) },
                confirmButton = { TextButton(onClick = { legal = null }) { Text("返回") } })
        }
    }
}

@Composable
private fun Block(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = Panel, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(2.dp)); content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.width(70.dp), color = Muted, fontSize = 13.sp)
        Text(value, Modifier.weight(1f), fontSize = 13.sp)
    }
}

@Composable
private fun Toggle(title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, changed: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = Muted, lineHeight = 18.sp)
        }
        Switch(checked = checked, onCheckedChange = changed, enabled = enabled)
    }
}

@Composable
private fun ControlBlock(motion: FoldMotionModel, profile: DeviceProfile, consent: Boolean, connected: Boolean,
    enabled: Boolean, closing: Boolean, other: Boolean, light: Boolean, diagnostics: Boolean, strength: Float,
    ready: Boolean, angle: Float, fullyOpen: Float, message: String, status: String, keyboardReady: Boolean,
    onConsent: () -> Unit, onEnabled: (Boolean) -> Unit, onClosing: (Boolean) -> Unit,
    onOther: (Boolean) -> Unit, onLight: (Boolean) -> Unit, onDiagnostics: (Boolean) -> Unit,
    onStrength: (Float) -> Unit, onStrengthSaved: () -> Unit) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Block("开合盖动画", "开盖展开，合盖收起；中途反向也会跟随") {
            Text(when {
                !profile.canRun -> "当前设备不满足启用条件"
                !consent -> "未启用 · 先阅读风险说明"
                !enabled -> "已暂停"
                !connected -> "等待无障碍权限"
                !keyboardReady -> "等待键盘连接"
                else -> status
            }, color = Accent, fontWeight = FontWeight.Medium)
            if (!consent) Button(onClick = onConsent, enabled = profile.canRun) { Text("阅读说明并启用") }
            if (consent) {
                Toggle("自动动画", "桌面或锁屏亮屏后，按实际开合程度显示", enabled, profile.canRun, onEnabled)
                if (!connected) Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("开启无障碍服务") }
                if (!connected) Text("在系统「已安装的应用」中开启「TabFold 开合盖动画」。", color = Muted, fontSize = 12.sp)
            }
            Toggle("合盖时也播放", "亮屏桌面/锁屏下低频检测；可能增加耗电", closing, consent && profile.canRun, onClosing)
            Toggle("轻量明暗模式", "减少渲染负担，使用透明明暗变化", light, consent, onLight)
            OutlinedButton(enabled = connected && enabled && keyboardReady && profile.canRun, onClick = {
                if (AutoFoldService.previewHome()) context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
            }) { Text("到桌面试播") }
            Text("动画层不接收触摸。展开到位、熄屏或切换应用后退出，单次最多 12 秒。受保护的锁屏可能只能显示明暗效果。", fontSize = 12.sp, color = Muted)
        }
        Block("调整到你的习惯", "先将键盘平放，再把屏幕翻到舒适的位置") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("当前估算", color = Muted, fontSize = 12.sp); Text(if (ready) "${"%.1f".format(angle)}°" else "—", fontSize = 30.sp) }
                Column(horizontalAlignment = Alignment.End) { Text("完整展开位置", color = Muted, fontSize = 12.sp); Text("${"%.1f".format(fullyOpen)}°", fontSize = 30.sp) }
            }
            OutlinedButton(enabled = consent && ready, onClick = { motion.calibrateOpen() }) { Text("将当前位置设为完全展开") }
            Text(message, fontSize = 12.sp, color = Muted)
            Text("动画强度  ${"%.0f".format(strength / 65f * 100)}%", fontSize = 14.sp)
            Slider(value = strength, valueRange = 30f..70f, enabled = consent, onValueChange = onStrength, onValueChangeFinished = onStrengthSaved)
            Toggle("使用其他实体键盘", "为第三方键盘放宽识别；底座仍须保持水平固定", other, consent, onOther)
            Toggle("本机诊断日志", "默认关闭；仅记录事件、角度和窗口所属应用", diagnostics, consent, onDiagnostics)
        }
    }
}

@Composable
private fun TrustBlock(consent: Boolean, onRisk: () -> Unit, onPrivacy: () -> Unit, onLicense: () -> Unit, onRevoke: () -> Unit) {
    Block("透明、可控", "本应用无联网权限，无广告或数据上传") {
        Text("只处理实现动画所需的本地信息。屏幕截图短暂存在于内存中；不承诺系统、内存或第三方软件绝对安全。", color = Muted, fontSize = 13.sp)
        TextButton(onClick = onRisk) { Text("风险与责任说明") }
        TextButton(onClick = onPrivacy) { Text("隐私与数据处理") }
        TextButton(onClick = onLicense) { Text("开源许可证与致谢") }
        if (consent) TextButton(onClick = onRevoke) { Text("撤回同意并停用", color = Color(0xFFFFC0B7)) }
    }
}
