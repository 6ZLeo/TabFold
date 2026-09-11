package io.github.sixzleo.tabfold.ui

import android.app.LocaleManager
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource as tr
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sixzleo.tabfold.BuildConfig
import io.github.sixzleo.tabfold.MainActivity
import io.github.sixzleo.tabfold.R
import io.github.sixzleo.tabfold.SettingsStore
import io.github.sixzleo.tabfold.auto.AutoFoldService
import io.github.sixzleo.tabfold.auto.MotionService
import io.github.sixzleo.tabfold.device.DeviceProfile
import io.github.sixzleo.tabfold.sensors.FoldMotionModel
import kotlinx.coroutines.delay
import kotlin.math.abs

private val Accent = Color(0xFF94DFCE)
private val Muted = Color(0xFFA6B4BE)
private val Panel = Color(0xFF1B232D)

@Composable
fun TabFoldApp(motion: FoldMotionModel) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val clipboard = LocalClipboardManager.current
    val prefs = remember { SettingsStore.prefs(context) }
    val profile = remember { DeviceProfile.read(context) }
    var keyboards by remember { mutableStateOf(DeviceProfile.keyboardNames()) }
    var keyboardReady by remember { mutableStateOf(DeviceProfile.keyboardReady(context)) }
    var permission by remember { mutableStateOf(SystemSettings.serviceEnabled(context)) }
    var consent by remember { mutableStateOf(SettingsStore.consented(context)) }
    var enabled by remember { mutableStateOf(SettingsStore.enabled(context)) }
    var calibrated by remember { mutableStateOf(prefs.contains("fully_open")) }
    var closing by remember { mutableStateOf(prefs.getBoolean("close_enabled", true)) }
    var other by remember { mutableStateOf(prefs.getBoolean("allow_other_keyboard", false)) }
    var light by remember { mutableStateOf(prefs.getBoolean("lightweight", profile.lowMemory)) }
    var diagnostics by remember { mutableStateOf(prefs.getBoolean("diagnostics", false)) }
    var allApps by remember { mutableStateOf(SettingsStore.allApps(context)) }
    var strength by remember { mutableFloatStateOf(prefs.getFloat("strength", 65f)) }
    var tolerance by remember { mutableFloatStateOf(SettingsStore.openTolerance(context)) }
    var legal by remember { mutableStateOf<String?>(null) }
    var consentDialog by remember { mutableStateOf(false) }
    var restrictedHelp by remember { mutableStateOf(false) }
    var advanced by remember { mutableStateOf(false) }
    var feedback by remember { mutableIntStateOf(0) }
    val connected by AutoFoldService.connected.collectAsState()
    val background by MotionService.running.collectAsState()
    val reading by motion.reading.collectAsState()
    val fullyOpen by motion.fullyOpen.collectAsState()
    val message by motion.message.collectAsState()
    val landscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val step = SetupPolicy.step(profile.canRun, consent, permission, connected, keyboardReady,
        landscape, calibrated, enabled && background)
    val validPose = reading.ready && motion.hasFreshReading() &&
        reading.angle in 45f..160f && abs(reading.lateral) <= 12f
    val palette = darkColorScheme(primary = Accent, onPrimary = Color(0xFF12382F),
        background = Color(0xFF10151B), surface = Panel, onSurface = Color(0xFFECF1F5),
        onSurfaceVariant = Muted, secondary = Accent)
    fun savePosition() {
        if (motion.calibrateOpen()) { calibrated = true; feedback = R.string.calibration_saved }
    }
    fun preview(start: Boolean) {
        if (start) { AutoFoldService.enable(context, true); enabled = true }
        if (AutoFoldService.previewHome()) {
            if (!SystemSettings.home(context)) feedback = R.string.settings_unavailable
        } else feedback = R.string.preview_unavailable
    }
    fun revoke() {
        AutoFoldService.revoke(context)
        consent = false; enabled = false; motion.stop(); feedback = R.string.revoked
    }
    val notificationRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        preview(true)
    }
    fun startAnimation() {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            notificationRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        else preview(true)
    }
    fun allowOther(value: Boolean) {
        other = value
        prefs.edit().putBoolean("allow_other_keyboard", value).apply()
        keyboardReady = DeviceProfile.keyboardReady(context)
    }
    fun openSettings(appInfo: Boolean = false) {
        val opened = if (appInfo) SystemSettings.appInfo(context) else SystemSettings.accessibility(context)
        if (!opened) feedback = R.string.settings_unavailable
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (MainActivity.isForeground) {
                keyboards = DeviceProfile.keyboardNames()
                keyboardReady = DeviceProfile.keyboardReady(context)
                permission = SystemSettings.serviceEnabled(context)
                enabled = SettingsStore.enabled(context)
            }
            delay(700)
        }
    }

    MaterialTheme(colorScheme = palette) {
        Surface(Modifier.fillMaxSize(), color = palette.background) {
            BoxWithConstraints(Modifier.safeDrawingPadding().fillMaxSize()) {
                val wide = maxWidth >= 840.dp
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(if (wide) 32.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.widthIn(max = 1200.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_tabfold), null, Modifier.size(42.dp))
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Text("TabFold", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                            Text(tr(R.string.tagline), color = Muted, fontSize = 12.sp)
                        }
                        LanguagePicker()
                    }
                    val setup: @Composable () -> Unit = {
                        SetupCard(step, permission, connected, keyboardReady, keyboards.isNotBlank(),
                            landscape, reading.ready, validPose, reading.angle, fullyOpen, message,
                            other, allApps,
                            onPrimary = {
                                when (step) {
                                    SetupStep.CONSENT -> consentDialog = true
                                    SetupStep.ACCESSIBILITY -> openSettings()
                                    SetupStep.CALIBRATION -> savePosition()
                                    SetupStep.START -> startAnimation()
                                    SetupStep.READY -> preview(false)
                                    SetupStep.UNSUPPORTED -> Unit
                                }
                            }, onHelp = { restrictedHelp = true }, onOther = { allowOther(it) },
                            onPause = { AutoFoldService.enable(context, false); enabled = false })
                    }
                    val preferences: @Composable () -> Unit = {
                        Block(R.string.settings_title, R.string.settings_hint) {
                            Toggle(R.string.scope_title, R.string.scope_hint, allApps, consent && android.os.Build.VERSION.SDK_INT >= 34) {
                                allApps = it; AutoFoldService.setAllApps(context, it)
                            }
                            Toggle(R.string.closing_title, R.string.closing_hint, closing, consent) {
                                closing = it; AutoFoldService.enableClosing(context, it)
                            }
                            Toggle(R.string.light_title, R.string.light_hint, light, consent) {
                                light = it; prefs.edit().putBoolean("lightweight", it).apply()
                            }
                            Text(tr(R.string.strength_title, (strength / 65f * 100).toInt()))
                            Slider(value = strength, valueRange = 30f..70f, enabled = consent,
                                onValueChange = { strength = it },
                                onValueChangeFinished = { prefs.edit().putFloat("strength", strength).apply() })
                            Text(tr(R.string.tolerance_title, tolerance.toInt()))
                            Text(tr(R.string.tolerance_hint), fontSize = 12.sp, color = Muted, lineHeight = 18.sp)
                            Slider(value = tolerance, valueRange = 1f..8f, steps = 6, enabled = consent,
                                onValueChange = { tolerance = kotlin.math.round(it) },
                                onValueChangeFinished = { prefs.edit().putFloat("open_tolerance", tolerance).apply() })
                            Text(tr(R.string.tolerance_range,
                                "%.1f".format(fullyOpen - tolerance), "%.1f".format(fullyOpen + tolerance)),
                                color = Accent, fontSize = 12.sp)
                            TextButton(onClick = { advanced = !advanced }) {
                                Text(tr(R.string.advanced_title) + if (advanced) " −" else " +")
                            }
                            if (advanced) {
                                Toggle(R.string.other_keyboard_title, R.string.other_keyboard_hint,
                                    other, consent) { allowOther(it) }
                                Toggle(R.string.diagnostics_title, R.string.diagnostics_hint,
                                    diagnostics, consent) {
                                    diagnostics = it; prefs.edit().putBoolean("diagnostics", it).apply()
                                }
                                AngleRow(reading.ready, reading.angle, fullyOpen)
                                OutlinedButton(enabled = consent && validPose && keyboardReady && landscape,
                                    onClick = { savePosition() }) { Text(tr(R.string.recalibrate)) }
                                Text(tr(message), fontSize = 12.sp, color = Muted)
                            }
                        }
                    }
                    if (wide) Row(Modifier.widthIn(max = 1200.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            setup(); preferences()
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            DeviceCard(profile, motion.sourceLabel, keyboards, onCopy = {
                                clipboard.setText(AnnotatedString(profile.report(keyboards.ifBlank { "None" })))
                                feedback = R.string.report_copied
                            })
                            TrustCard(consent, { legal = "DISCLAIMER" }, { legal = "PRIVACY" },
                                { legal = "LICENSES" }, { revoke() }, { restrictedHelp = true })
                        }
                    } else Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        setup()
                        preferences()
                        DeviceCard(profile, motion.sourceLabel, keyboards, onCopy = {
                            clipboard.setText(AnnotatedString(profile.report(keyboards.ifBlank { "None" })))
                            feedback = R.string.report_copied
                        })
                        TrustCard(consent, { legal = "DISCLAIMER" }, { legal = "PRIVACY" },
                            { legal = "LICENSES" }, { revoke() }, { restrictedHelp = true })
                    }
                    if (feedback != 0) Text(tr(feedback), color = Accent)
                    Row(Modifier.widthIn(max = 1200.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr(R.string.footer), color = Muted, fontSize = 12.sp)
                            Text(BuildConfig.VERSION_NAME, color = Muted, fontSize = 11.sp)
                        }
                        TextButton(onClick = {
                            if (!SystemSettings.web(context, "https://github.com/6ZLeo/TabFold"))
                                feedback = R.string.settings_unavailable
                        }) { Text("GitHub ↗") }
                    }
                }
            }
        }

        if (consentDialog) {
            var understood by remember { mutableStateOf(false) }
            var selectedScope by remember { mutableStateOf(allApps) }
            AlertDialog(onDismissRequest = { consentDialog = false },
                title = { Text(tr(R.string.consent_dialog_title)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(R.string.consent_nonprofit, R.string.consent_screen,
                            R.string.consent_motion, R.string.consent_device, R.string.consent_liability)
                            .forEach { Text(tr(it), fontSize = 14.sp) }
                        Toggle(R.string.scope_consent, R.string.scope_hint, selectedScope,
                            android.os.Build.VERSION.SDK_INT >= 34) { selectedScope = it }
                        TextButton(onClick = { legal = "DISCLAIMER" }) { Text(tr(R.string.risk_link)) }
                        TextButton(onClick = { legal = "PRIVACY" }) { Text(tr(R.string.privacy_link)) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(understood, { understood = it })
                            Text(tr(R.string.consent_check), fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(enabled = understood && profile.canRun, onClick = {
                        prefs.edit().putInt("consent_version", SettingsStore.CONSENT_VERSION)
                            .putBoolean("auto_enabled", false).putBoolean("all_apps", selectedScope).apply()
                        allApps = selectedScope; consent = true; enabled = false
                        consentDialog = false; motion.start()
                    }) { Text(tr(R.string.confirm_continue)) }
                },
                dismissButton = { TextButton(onClick = { consentDialog = false }) { Text(tr(R.string.not_now)) } })
        }
        if (restrictedHelp) {
            AlertDialog(onDismissRequest = { restrictedHelp = false },
                title = { Text(tr(R.string.restricted_title)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(R.string.restricted_intro, R.string.restricted_step_one,
                            R.string.restricted_step_two, R.string.restricted_step_three,
                            R.string.restricted_step_four, R.string.restricted_missing).forEach {
                            Text(tr(it), fontSize = 14.sp)
                        }
                        OutlinedButton(onClick = {
                            if (!SystemSettings.web(context, "https://support.google.com/android/answer/12623953"))
                                feedback = R.string.settings_unavailable
                        }) { Text(tr(R.string.official_help)) }
                    }
                },
                confirmButton = { Button(onClick = { openSettings(true) }) { Text(tr(R.string.open_app_info)) } },
                dismissButton = { TextButton(onClick = { restrictedHelp = false }) { Text(tr(R.string.back)) } })
        }
        legal?.let { document ->
            val chinese = config.locales[0].language == "zh"
            val content = remember(document, chinese) {
                val path = if (document == "LICENSES") "licenses/THIRD_PARTY_NOTICES.txt"
                    else "legal/" + document + (if (chinese) ".md" else ".en.md")
                context.assets.open(path).bufferedReader().use { it.readText() }
                    .replace(Regex("(?m)^#{1,6}\\s*"), "").replace("**", "")
            }
            AlertDialog(onDismissRequest = { legal = null },
                title = { Text(tr(when (document) {
                    "DISCLAIMER" -> R.string.risk_link
                    "PRIVACY" -> R.string.privacy_link
                    else -> R.string.license_link
                })) },
                text = { Text(content, Modifier.heightIn(max = 580.dp).verticalScroll(rememberScrollState()),
                    fontSize = 13.sp, lineHeight = 21.sp) },
                confirmButton = { TextButton(onClick = { legal = null }) { Text(tr(R.string.back)) } })
        }
    }
}

@Composable
private fun LanguagePicker() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val manager = context.getSystemService(LocaleManager::class.java)
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(tr(R.string.language_menu), fontSize = 12.sp) }
        DropdownMenu(expanded, { expanded = false }) {
            listOf("" to tr(R.string.follow_system), "zh-Hans" to "简体中文", "en" to "English").forEach { (tag, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    expanded = false
                    manager.applicationLocales = LocaleList.forLanguageTags(tag)
                })
            }
        }
    }
}

@Composable
private fun SetupCard(step: SetupStep, permission: Boolean, connected: Boolean, keyboard: Boolean,
    hasKeyboard: Boolean, landscape: Boolean, reading: Boolean, validPose: Boolean, angle: Float,
    fullyOpen: Float, message: Int, other: Boolean, allApps: Boolean, onPrimary: () -> Unit,
    onHelp: () -> Unit, onOther: (Boolean) -> Unit, onPause: () -> Unit) {
    val index = when (step) {
        SetupStep.UNSUPPORTED, SetupStep.CONSENT -> 0
        SetupStep.ACCESSIBILITY -> 1
        SetupStep.CALIBRATION -> 2
        SetupStep.START -> 3
        SetupStep.READY -> 4
    }
    val title = when (step) {
        SetupStep.UNSUPPORTED -> R.string.unsupported_title
        SetupStep.CONSENT -> R.string.consent_title
        SetupStep.ACCESSIBILITY -> R.string.access_title
        SetupStep.CALIBRATION -> R.string.calibration_title
        SetupStep.START -> R.string.start_title
        SetupStep.READY -> R.string.ready_title
    }
    Block(title) {
        Text(tr(R.string.setup_progress, index), color = Accent, fontSize = 13.sp)
        LinearProgressIndicator(progress = { index / 4f }, modifier = Modifier.fillMaxWidth())
        val labels = listOf(R.string.step_consent, R.string.step_accessibility,
            R.string.step_calibration, R.string.step_start)
        labels.forEachIndexed { position, label ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = if (position <= index) Color(0xFF21483F) else Color(0xFF303943),
                    shape = MaterialTheme.shapes.small) {
                    Text(if (position < index) "✓" else (position + 1).toString(),
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Accent)
                }
                Text(tr(label), Modifier.weight(1f), fontSize = 14.sp)
                if (position < index) Text(tr(R.string.done), color = Accent, fontSize = 12.sp)
                else if (position == index && step != SetupStep.UNSUPPORTED)
                    Text(tr(R.string.next_step), color = Accent, fontSize = 12.sp)
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        Text(when (step) {
            SetupStep.UNSUPPORTED -> tr(R.string.unsupported_hint)
            SetupStep.CONSENT -> tr(R.string.consent_hint)
            SetupStep.ACCESSIBILITY -> tr(R.string.access_hint, tr(R.string.service_name))
            SetupStep.CALIBRATION -> tr(R.string.calibration_hint)
            SetupStep.START -> tr(R.string.start_hint)
            SetupStep.READY -> tr(R.string.ready_hint)
        }, fontSize = 15.sp, lineHeight = 23.sp)
        if (step == SetupStep.ACCESSIBILITY) {
            Text(tr(R.string.permission_state, tr(if (permission) R.string.permission_on else R.string.permission_off)), color = Muted)
            Text(tr(R.string.connection_state, tr(if (connected) R.string.connection_on else R.string.connection_off)), color = Muted)
            if (permission && !connected) Text(tr(R.string.binding_wait), color = Accent)
        }
        if (step == SetupStep.CALIBRATION) {
            when {
                !keyboard -> {
                    Text(tr(if (hasKeyboard) R.string.keyboard_not_accepted else R.string.keyboard_needed), color = Color(0xFFFFCA8C))
                    if (hasKeyboard) Toggle(R.string.other_keyboard_title, R.string.other_keyboard_hint, other, changed = onOther)
                }
                !landscape -> Text(tr(R.string.landscape_needed), color = Color(0xFFFFCA8C))
                !reading -> Text(tr(R.string.sensor_wait), color = Muted)
                !validPose -> Text(tr(R.string.calibration_invalid), color = Color(0xFFFFCA8C))
            }
            AngleRow(reading, angle, fullyOpen)
            if (message != R.string.calibration_default) Text(tr(message), color = Muted, fontSize = 12.sp)
        }
        if (step != SetupStep.UNSUPPORTED) {
            Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                enabled = step != SetupStep.CALIBRATION || (keyboard && landscape && validPose)) {
                Text(tr(when (step) {
                    SetupStep.CONSENT -> R.string.read_continue
                    SetupStep.ACCESSIBILITY -> R.string.open_accessibility
                    SetupStep.CALIBRATION -> R.string.save_open_position
                    SetupStep.START -> R.string.start_animation
                    else -> R.string.preview_home
                }))
            }
        }
        if (step == SetupStep.ACCESSIBILITY) TextButton(onClick = onHelp) { Text(tr(R.string.restricted_link)) }
        if (step == SetupStep.START || step == SetupStep.READY) {
            Text(tr(R.string.scope_value, tr(if (allApps) R.string.scope_all else R.string.scope_home)), color = Accent, fontSize = 13.sp)
            Text(tr(R.string.background_note), color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
        }
        if (step == SetupStep.READY) OutlinedButton(onClick = onPause) { Text(tr(R.string.pause)) }
    }
}

@Composable
private fun DeviceCard(profile: DeviceProfile, sourceLabel: Int, keyboards: String, onCopy: () -> Unit) {
    Block(R.string.device_title, R.string.device_subtitle) {
        Text(profile.name, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text(profile.model, color = Muted)
        Text(tr(profile.verdictRes), color = if (profile.canRun) Accent else Color(0xFFFFCA8C))
        InfoRow(R.string.system_label, "Android ${profile.androidVersion} · API ${profile.sdk}")
        InfoRow(R.string.display_label, profile.displayInfo)
        InfoRow(R.string.motion_label, tr(sourceLabel))
        InfoRow(R.string.keyboard_label, keyboards.ifBlank { tr(R.string.no_keyboard) })
        Text(tr(R.string.model_note), fontSize = 12.sp, color = Muted)
        OutlinedButton(onClick = onCopy) { Text(tr(R.string.copy_report)) }
    }
}

@Composable
private fun AngleRow(ready: Boolean, angle: Float, fullyOpen: Float) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(tr(R.string.current_angle), fontSize = 12.sp, color = Muted)
            Text(if (ready) "%.1f°".format(angle) else "—", fontSize = 28.sp)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(tr(R.string.saved_angle), fontSize = 12.sp, color = Muted)
            Text("%.1f°".format(fullyOpen), fontSize = 28.sp)
        }
    }
}

@Composable
private fun TrustCard(consent: Boolean, risk: () -> Unit, privacy: () -> Unit, license: () -> Unit,
    revoke: () -> Unit, help: () -> Unit) {
    Block(R.string.privacy_title) {
        Text(tr(R.string.privacy_short), color = Muted, fontSize = 13.sp)
        TextButton(onClick = risk) { Text(tr(R.string.risk_link)) }
        TextButton(onClick = privacy) { Text(tr(R.string.privacy_link)) }
        TextButton(onClick = license) { Text(tr(R.string.license_link)) }
        TextButton(onClick = help) { Text(tr(R.string.restricted_link)) }
        if (consent) TextButton(onClick = revoke) { Text(tr(R.string.revoke), color = Color(0xFFFFC0B7)) }
    }
}

@Composable
private fun Block(title: Int, subtitle: Int? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = Panel, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(tr(title), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(tr(subtitle), color = Muted, fontSize = 12.sp)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: Int, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(tr(label), Modifier.width(88.dp), color = Muted, fontSize = 13.sp)
        Text(value, Modifier.weight(1f), fontSize = 13.sp)
    }
}

@Composable
private fun Toggle(title: Int, hint: Int, checked: Boolean, enabled: Boolean = true,
    changed: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(tr(title), fontSize = 15.sp)
            Text(tr(hint), fontSize = 12.sp, lineHeight = 18.sp, color = Muted)
        }
        Switch(checked, changed, enabled = enabled)
    }
}
