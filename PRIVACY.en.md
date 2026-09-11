# TabFold Privacy and Local Data Processing

Version: 2 · 2026-09-11

[简体中文](PRIVACY.md) · English

Official TabFold builds have no INTERNET, storage, photo, contact or account-reading permissions. There are no advertising or analytics SDKs, automatic telemetry or background upload interfaces. Open source does not mean absolute security; also read DISCLAIMER.en.md.

## Information processed

| Information | Purpose | Storage and transmission |
| --- | --- | --- |
| Manufacturer, model, Android version, display specifications and sensor capabilities | Device and compatibility information | Read locally; no automatic upload |
| Physical keyboard names and connection state | Check attachment | Local use; no keystroke reading |
| Motion events and estimated opening angle | Drive animations and detect opening/closing trends | In-memory processing; logging off by default |
| Window-owner app, window identifier and class, display and lock state | Apply the chosen scope, exclude permission pages and discard stale images | Necessary metadata only; no view-tree or text reading |
| Permitted home, lock or app images within the chosen scope | Render an opening/closing animation | Temporary system/GPU/app memory; no image files, gallery storage or uploads |
| Calibration angle, strength, scope, switches and consent version | Remember choices | App-private preferences; system backup disabled |

With consent, sensors support calibration in the settings page. Once enabled, the service detects opening and closing continuously in eligible scenes while the display is on; the settings page need not remain open. Turning the display off, pausing or entering an excluded page stops automatic animations and related sensing. Choose home and lock screen only, or include other permitted apps (Android 14 onward). App images may contain messages, account details, work content or other sensitive information. Images are requested on animation triggers, not recorded continuously.

For other apps, a system screenshot check of the target window precedes capture for animation; a failed check skips the animation. Lightweight mode uses transparent shading. It requests no images on home/lock screens, but still requests one target-window image in other apps to respect system protection, then closes its buffer without using it for image deformation. App/window changes and unlocking discard images that no longer apply.

Android stores the language choice locally as an app language setting. It changes TabFold only, without changing the system or other apps' languages.

Protected-image failures do not bypass system protection or reuse a previous scene's image on the current lock screen. When animation ends, app-held image references are released; immediate physical memory erasure is not guaranteed.

## Optional actions

- “Copy device report” writes to the system clipboard only when selected; it does not send the report. Reports contain no serial number, account, unique device identifier or screen image.
- “Local diagnostic log” is off by default. Enabling it may write angles, events and window-owner applications to Android logs. The system manages those logs; switching it off stops future records without clearing other apps' or the whole system's logs.
- GitHub and official-help links open the system browser. The browser and destination website apply their own network and privacy rules; this does not give TabFold network permission.
- Sharing issues, logs, recordings or screenshots on GitHub is a separate user action. Check for private information first. Do not share full system logs or credentials.

## Withdrawal and deletion

Pause animation to stop background motion detection. Disabling closing animations alone leaves opening detection active. “Withdraw consent and disable” stops automatic features and requests that Android unbind TabFold's accessibility service, retaining calibration. Reactivation requires reviewing the disclosure again.

Clear this app's data or uninstall it through Android to remove its settings. Other software, system logs, clipboard histories and information voluntarily shared on GitHub follow their respective deletion mechanisms.

Background operation uses an Android foreground service and an ongoing notification with settings and pause actions. Notification permission only displays this running-status notification; if denied, Android may still show the service in its active-apps list. This service keeps the process eligible for background motion sensing. It samples no sensors with the display off and holds no wake lock.

Obtaining a target window identifier for protected-image checks requires declaring the accessibility window-content capability. The implementation does not call view-tree, view-text or keystroke-reading interfaces. Grant access only to trusted builds.

TabFold does not turn off other accessibility services or modify other apps' data or user files. Verify the download source, signing certificate and SHA-256; modified builds may have different permissions and behavior.
