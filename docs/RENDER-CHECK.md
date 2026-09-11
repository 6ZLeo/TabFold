# Synthetic renderer check

The Debug build has a synthetic checkerboard activity to validate the AGSL renderer without accessibility permission, screen capture, personal content or device-compatibility overrides. It is excluded from Release builds and does not appear in the launcher.

On a dedicated Android 13+ test device or emulator, install the Debug build and run:

```sh
adb shell am start -S -n io.github.sixzleo.tabfold/.debug.RenderProbeActivity --ef opening 110
adb shell am start -S -n io.github.sixzleo.tabfold/.debug.RenderProbeActivity --ef opening 55
```

At 110 degrees the whole 8 × 5 checkerboard should fill the view, without clamped strips at the bottom or right. At 55 degrees it should show the fold perspective, scattering and darkening. A shader compilation failure is a crash and appears under `AndroidRuntime` in the test device log. `TabFoldProbe` records successful shader creation.

This validates basic rendering only. It does not validate screenshot capture, Samsung GPU performance, motion sensors, keyboard detection, lock-screen policies or the complete automatic-animation flow.
