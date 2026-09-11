# TabFold Risk and Liability Disclosure

Version: 2 · 2026-09-11

[简体中文](DISCLAIMER.md) · English

## 1. Nature of the project

TabFold is experimental open-source software offered free of charge for the maintainer's personal interest, technical research and community exchange. There are no paid services, advertisements, paid guarantees or promises of continued maintenance. A non-profit purpose does not mean the software is risk-free or automatically exempt anyone from legal liability.

TabFold is not an official product of Samsung, Google, Apple or their affiliates, and is not sponsored, certified or endorsed by them. Names, device models and trademarks identify devices, technical sources and compatibility; they belong to their respective owners.

Use, modification and redistribution are governed by LICENSE and applicable third-party licenses. The MIT License permits commercial use. The maintainer's non-profit purpose is not an additional prohibition on commercial use. This disclosure does not withdraw license rights.

## 2. Limitations to understand before use

- Android 13 or newer and a Samsung tablet with suitable motion data are required. Recognizing a model, installing the app, enabling a permission or passing capability checks does not establish real-device validation.
- Keep the keyboard base horizontal and stationary. Tablet orientation is not a direct measurement of the hinge angle between keyboard and screen. Sloping surfaces, lap use, lifting the whole assembly, moving only the keyboard, magnetic accessories and fast movements can produce incorrect estimates.
- Devices without gyroscope fusion may use gravity or accelerometer data, with greater error or delay in motion. Displayed decimal places and sensor frequencies do not guarantee accuracy or animation frame rate.
- System versions, regional firmware, One UI, desktop modes, covers, keyboards, power management and security policies can affect behavior. Compatibility with every Samsung tablet, keyboard, DeX, portrait mode, application or future system is not guaranteed.
- The app does not promise to reproduce every behavior of another brand's animation and does not replace a system compositor or a physical hinge sensor.

## 3. Permissions, screen images and information security

You must deliberately enable the accessibility service so TabFold can identify window ownership, request system-permitted images within your selected scope and display an animation layer. The declared accessibility capability includes window-content access to obtain target-window identifiers, but this implementation does not read view trees or view text. Grant this sensitive permission only to a version whose source you trust and have checked.

Official builds do not read view trees, view text, keystrokes, accounts, contacts, photo libraries, serial numbers or unique device identifiers, and do not perform touch gestures for you. Animation images are processed for the current animation, not written to image files, saved to a gallery or uploaded. Images can nevertheless contain visible notifications, clocks, widgets, messages, account details, work content or other sensitive information. Choose home and lock screen only, or include other permitted apps.

Not saving screenshots does not mean an image never enters memory or is immediately physically erased. Images may temporarily exist in system, GPU or app memory, whose release is platform-managed. Compromised systems, root access, debugging tools, malicious software, modified builds, supply-chain problems and unknown vulnerabilities can expose information. The project cannot guarantee absolute security of the system, memory, dependencies or other software.

Other-app animations require Android 14 or newer. The system first checks whether the target window permits screenshots; refusal skips the animation. System settings, permission pages and TabFold itself are excluded. When home or lock screen capture is unavailable, transparent shading may be used; it does not provide full image deformation. The app does not bypass screenshot protection or unlock the device.

Copying a device report puts it on the system clipboard, subject to system and other-app access rules. Optional diagnostic logging can include models, angles, event times and window-owner applications. Before sharing issues, screenshots, logs or recordings, remove accounts, notifications, identifiers, locations, keys and other private information. Do not submit full system logs to the public repository.

## 4. Device, data and experience risks

Use may increase battery consumption, heat or resource use, and can cause lag, flicker, unintended triggering, rendering errors, temporarily frozen images, color changes, crashes or system incompatibility. Prolonged static images can contribute to image retention or display aging; effects vary with device, brightness, system and use.

The software does not deliberately change firmware, keyboard touch-rejection parameters, the default launcher, lock-screen authentication rules or other apps' data. This is not a guarantee against system faults, data loss, interruption, device damage or other consequences. Back up important data and test gradually in non-critical situations.

Do not overbend keyboards, covers, hinges or cables to view the effect, or force accessories beyond their normal movement range. Support the device and avoid pinching and drops. Stop using the app, disable its service and investigate if you notice unusual heat, battery drain, flicker, dizziness, abnormal images or instability. Do not use it for safety-critical, medical, transport, industrial-control or timing-critical purposes.

## 5. As-is provision, warranty exclusion and liability limits

To the maximum extent permitted by applicable law, the software and documentation are provided “as is” and “as available”, without express or implied warranties, including merchantability, fitness for a particular purpose, non-infringement, accuracy, compatibility, security, uninterrupted operation or freedom from errors. The maintainer does not promise to fix every problem, keep updating, meet response deadlines, support future systems or provide compensation.

To the maximum extent permitted by applicable law, authors, maintainers, contributors and copyright holders are not liable for losses arising from using, being unable to use, installing, uninstalling, modifying, redistributing or relying on the software. These include device damage, data loss or leakage, privacy exposure, lost revenue or profits, business interruption, replacement service costs, and direct, indirect, incidental, special or consequential losses, whether claimed in contract, tort or otherwise.

These exclusions apply only where legally permitted. Nothing excludes non-excludable liability or removes non-waivable statutory rights. For example, where Chinese law applies, liability for personal injury to the other party, or property loss caused intentionally or through gross negligence, cannot automatically be excluded by this disclosure. Rules governing notice, explanation and validity of standard terms still apply.

This document is not a certificate of absolute immunity, does not prevent disputes, does not bind third parties who are not lawfully bound, and cannot replace professional legal assessment of particular facts and jurisdictions. Users must assess their device, data sensitivity and local law; maintainers remain responsible for non-excludable legal duties.

## 6. Voluntary activation, pause and exit

Read this document and PRIVACY.en.md before activating the feature. If you disagree or cannot accept the risks, do not activate it or uninstall it. Acknowledging the disclosure records your choice about the described functions and local processing; it does not waive rights that cannot lawfully be waived.

Android binds the enabled accessibility service in the background. A foreground service with an ongoing notification maintains the process state required for motion detection; its notification provides a pause action. Motion detection runs in eligible selected scenes while the display is on, stops when the display is off and resumes after waking. Power management, force-stopping or permission withdrawal may interrupt the service; permanent residency is not guaranteed.

Pause the animation, narrow its scope, disable closing animations or select “Withdraw consent and disable” at any time. Disabling closing animations leaves opening detection active. You can also disable the TabFold service in Android accessibility settings. Uninstallation removes the app and private settings according to Android rules; it does not revoke USB debugging, root or other-app permissions you granted separately.

## 7. Revisions and references

Material changes to processing, permissions or risks should update the disclosure version and require a renewed prompt before activation. Check the actual behavior of third-party modifications separately.

Legal effect depends on applicable law and facts. References include articles 496–498 and 506 of the Civil Code of the People's Republic of China. Open-source rights are governed by LICENSE.

[Official Civil Code text](https://www.weihai.gov.cn/attach/0/03210cf33aac4aa0a218fb4236187d68.pdf)
