# TabFold

[简体中文](README.md) · **English**

**Screen animations that follow your Samsung tablet as it opens and closes.** Unfold the screen to reveal its content; fold it back to reverse the effect. The animation follows pauses and changes in direction.

A free, open-source project maintained out of personal interest. Not affiliated with Samsung, Google or Apple.

[Download](https://github.com/6ZLeo/TabFold/releases) · [Compatibility](docs/COMPATIBILITY.md) · [Report an issue](https://github.com/6ZLeo/TabFold/issues)

## See it in action

| Opening the cover | Folding and opening again |
| :---: | :---: |
| ![The desktop gradually unfolds as the tablet opens](docs/media/tabfold-opening.gif) | ![The animation follows the tablet as it folds and opens again](docs/media/tabfold-fold-unfold.gif) |

[Full demo video (10.8 seconds · 720p / 60 fps · Silent · 3.1 MB)](docs/media/tabfold-demo.mp4)

Shown on a Galaxy Tab S10 Ultra with an official Book Cover Keyboard. Results vary by device, system version and setup.

## Features

- **Motion tracking:** estimates the opening angle from the tablet's orientation to animate home, lock and other permitted app screens.
- **Smooth closing:** detects small, continuous closing movements and follows changes in direction.
- **Background response:** an ongoing notification supports background sensing and provides a pause action. No need to keep settings open. Motion detection runs while the display is on, stops when off and resumes after waking.
- **Language choice:** Simplified Chinese, English or follow system, selectable in the app.
- **Device detection:** shows the device name, system version, display, motion sensors and keyboard connection status.
- **Personal adjustments:** calibrate the open position, change animation strength and enable or disable closing animations separately.
- **Open-position tolerance:** defaults to ±3°, adjustable from ±1° to ±8°. The image clears as opening enters the range, accommodating slight changes in the support position.
- **Lightweight mode:** uses transparent shading to reduce rendering load.
- **Local processing:** no network permission, ads or automatic uploads. Pause the feature or withdraw consent at any time.

## Download and setup

Download the signed APK from [Releases](https://github.com/6ZLeo/TabFold/releases) and check it against the supplied SHA-256 checksum. Android 13 (API 33) or newer is required. Root and a persistent computer connection are unnecessary.

Follow the four steps on the app's home page. It shows your progress and the next action.

1. **Read and agree:** select **Read and continue**, review local processing and risks, choose the animation scope and voluntarily confirm.
2. **Enable system service:** select **Open accessibility settings**, enable **TabFold cover animation** under **Installed apps**, then return.
3. **Connect and calibrate:** keep the keyboard flat and stationary, the screen in landscape and at your normal working angle. Select **Save this open position**.
4. **Start:** select **Start and preview on home screen** and allow notifications to see its running status and pause action. Then open an ordinary app and slowly fold or open the tablet. Preferences can limit the scope to home and lock screen.

Use **Language / 语言** at the top right to choose English, Simplified Chinese or follow system. The home preview runs by time; normal use follows motion.

### If Android shows “Restricted settings”

Installing an APK downloaded in a browser may require an additional system confirmation. Open **Blocked by “Restricted settings”?** in TabFold, then **Open TabFold app info → ⋮ → Allow restricted settings**. Confirm the system prompt, then enable the accessibility service. Samsung Chinese menus may call it “允许限制性设置”.

This is Android's installation-source protection. An app cannot grant itself the permission or override the policy. Menus vary with One UI and device-management rules. See [Android's official help](https://support.google.com/android/answer/12623953?hl=en).

To stop, disable automatic animations in the app, select **Withdraw consent and disable**, or turn off the TabFold service in Android's accessibility settings.

## Compatibility

| Item | Requirements |
| --- | --- |
| System | Android 13 / API 33 or newer |
| Device | A Samsung tablet with usable motion sensors |
| Keyboard | An official Book Cover keyboard; other physical keyboards can be allowed in settings |
| Position | Keyboard base horizontal and stationary, screen in landscape; move only the screen |
| Scene | Home and lock screen; Android 14 onward can include other permitted apps. Protected apps, system settings and permission pages are skipped |

**Model-name recognition is not a compatibility certification.** Check the [compatibility notes](docs/COMPATIBILITY.md) and [release notes](https://github.com/6ZLeo/TabFold/releases) for device-specific test coverage.

Tablet motion sensors cannot directly measure an independently moving keyboard hinge. Tilting the whole setup, using it on your lap or moving only the keyboard can cause incorrect estimates. Less capable devices, different One UI versions, DeX, multi-window use, portrait orientation and security policies may limit functionality.

When motion fusion is unavailable, gravity or accelerometer input can provide a basic fallback, with potentially greater error and latency during movement.

## Privacy and usage risks

Read the [risk disclosure](DISCLAIMER.en.md) and [privacy information](PRIVACY.en.md) before enabling the feature. The accessibility service identifies windows within your selected scope, captures permitted images and displays the animation layer. App images may contain messages, account details or other sensitive information. Images stay briefly in memory and are not saved or uploaded.

Image deformation uses a snapshot at the trigger, so live content resumes visibly after the animation exits. When protected lock-screen capture is denied, the app uses transparent shading. This does not transform the full lock-screen image or bypass authentication and screenshot protection. Other-app animations first require a successful system capture check of the target window; refusal skips the animation, including in lightweight mode.

Motion detection and rendering may increase battery use and heat. Each animation lasts at most 12 seconds. Window changes discard stale images; turning the display off ends the animation. Android binds the background service, but power management, force-stopping or permission withdrawal can interrupt it. The app shows its connection status and provides a pause button. Disable the feature if you experience flickering, stuttering or unusual heat.

The software is provided as is, without a guarantee that it will work on every device. Liability is limited only to the extent permitted by applicable law. Mandatory rights and non-excludable liabilities remain unaffected.

## Build locally

Use JDK 17, Android SDK Platform 35 and Build Tools 35.0.0. Put your SDK path in your own `local.properties` and do not commit that file.

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
./gradlew assembleRelease
```

On Windows, use `gradlew.bat`. Sign release builds with your own key.

## License and credits

The project uses the [MIT license](LICENSE). The maintainer's non-profit purpose describes this project's publication; MIT permits commercial use without an additional non-commercial restriction.

Animation rendering is based on Elijah Semyonov's MIT-licensed [DuoLikeAnimation](https://github.com/elijah-semyonov/DuoLikeAnimation). See [third-party notices](THIRD_PARTY_NOTICES.md) for attribution and licenses.

Compatibility reports and improvements are welcome. Read the [contribution guide](CONTRIBUTING.md) and [security policy](SECURITY.md) before contributing.
