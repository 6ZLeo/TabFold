# TabFold 0.1.0-beta.2

## 简体中文

- 可在普通应用中响应开合动作，并可切换为仅桌面与锁屏。启用后通过常驻通知支持后台姿态检测，通知可直接暂停；无需保持设置页打开；亮屏时检测动作，熄屏停止，醒来后恢复。
- 增加亮屏状态下重新展开的检测，延续轻微合拢趋势与平滑衔接。
- 展开到位容差默认 ±3°，可调 ±1°–±8°；进入到位范围结束动画，保持合盖灵敏度。
- 首页提供四步引导：阅读说明、开启服务、连接键盘并校准、启动动画；同时显示真实权限和服务连接状态。
- 应用内可选择简体中文、English 或跟随系统，帮助、风险及隐私说明均提供中英文。
- 受限设置帮助可直接打开 TabFold 应用信息，明确三星菜单“允许限制性设置”的位置。
- 扩大动画范围前需重新确认第 2 版风险及本地画面处理说明。

下载签名 APK 并核对随附的 SHA-256。可直接覆盖官方 beta.1，保留设置；更新后按首页提示完成启用。Android 对浏览器下载 APK 的额外确认由系统控制，本应用不会绕过它。

普通应用动画需要 Android 14 起的目标窗口截图检查；系统拒绝时跳过，轻量模式同样遵守。Android 13 支持桌面和锁屏。系统设置、授权页面及 TabFold 本身不触发自动动画。应用或窗口切换时丢弃过时画面。

### 验证范围与限制

- 30 项 JUnit 测试通过，覆盖启动条件、同意版本、开合趋势、到位容差、角度映射、设备能力以及跨应用和窗口的画面失效规则。
- Release 构建与 Android Lint 通过；四份中英文风险/隐私说明与应用内资源一致。
- Galaxy Tab S10 Ultra（SM-X920）、Android 16：已完成签名覆盖安装、系统受限设置入口核对、服务授权状态及应用语言切换检查。
- 同一设备的普通应用动画、静置后开合与展开容差已获使用者确认。设备检查记录显示动画正常退出后，姿态监听仍约为 50 Hz，窗口截图无错误；这是本次测试结果，不代表长期稳定性或所有设备均已验证。
- 其他型号、DeX、多窗口、不同固件安全策略及长期后台稳定性仍需更多实测，设备名称识别不等于兼容性认证。

键盘底座须水平固定、屏幕横向。角度来自平板姿态，不能测量独立移动的键盘铰链。单次动画最多 12 秒；系统功耗管理、强行停止或撤回权限可能中断后台服务。检测和渲染可能增加耗电、发热。实时画面在动画期间使用快照，退出后恢复。请先阅读 [风险说明](../DISCLAIMER.md) 和 [隐私说明](../PRIVACY.md)。

## English

- Animate in ordinary permitted apps, with an option to limit the scope to home and lock screens. An ongoing foreground-service notification supports motion sensing and includes a pause action. Settings need not stay open; sensing stops with the display off and resumes after waking.
- Detect reopening while the display stays on, with gentle closing detection and smooth entry.
- Open-position tolerance defaults to ±3° and is adjustable from ±1° to ±8°, ending the effect when the support settles within range without delaying closing detection.
- Four-step setup shows the next action, actual permission state and service connection.
- Choose English, Simplified Chinese or follow system. Help, risk disclosures and privacy information are bilingual.
- Restricted-settings help opens this app's info page and explains Samsung's menu.
- The broader screen-processing scope requires renewed acknowledgement of disclosure version 2.

Install the signed APK over official beta.1 to retain settings, then follow the setup page. Verify the supplied SHA-256. Android may still require its installation-source confirmation for browser-downloaded APKs; TabFold does not bypass it.

Other-app animations require Android 14's target-window capture check. A refusal skips the animation, including in lightweight mode. Android 13 supports home and lock screens. System settings, permission pages and TabFold itself are excluded; app/window changes discard stale images.

Validation includes 30 passing unit tests, Release build, Android Lint, bundled disclosure consistency, and installation, permission-state and language checks on an Android 16 Galaxy Tab S10 Ultra. The tester confirmed ordinary-app animation, folding after idle and the open-position tolerance. Device checks showed approximately 50 Hz sensing after animation completion without capture errors. This is a limited test result, not a long-term guarantee. Other models, DeX, multi-window behavior and firmware policies need further testing.

Keep the keyboard horizontal and stationary, with the screen in landscape. Orientation is not a direct keyboard hinge measurement. Animations last at most 12 seconds; system power management, force-stopping or permission withdrawal may interrupt the background service. Sensing and rendering may increase heat and battery use. Images temporarily freeze during deformation and resume afterward. Read the [risk disclosure](../DISCLAIMER.en.md) and [privacy information](../PRIVACY.en.md).

## Signing certificate · 签名证书

SHA-256 of the signing certificate (not the APK file):

```text
4cc99665a14654d950dc643fcdf29daf28e36429e36250435804efb4db179ab2
```
