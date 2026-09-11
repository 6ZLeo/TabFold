# TabFold

An independent, experimental opening/closing animation utility for Samsung tablets. The app contains device detection, calibration, animation settings, permission status and risk disclosures. It does not include the old demo dashboard and does not require DuoFold to be installed.

Android 13+ and usable motion sensors are required. Keep the keyboard base horizontal and stationary. Capability detection and model-name recognition are not proof that every Samsung tablet has been tested. The original prototype was exercised on a Galaxy Tab S10 Ultra with its official Book Cover Keyboard.

The app uses an explicitly enabled accessibility service for window ownership, transient in-memory screen images and a touch-through animation layer. Protected captures fall back to transparent shading. It does not bypass the lock screen, inspect view trees or keystrokes, save screenshots, or include network/analytics permissions. System memory and third-party software are not guaranteed secure.

Download signed builds from [Releases](https://github.com/6ZLeo/TabFold/releases). Read [the risk disclosure](DISCLAIMER.md), [privacy information](PRIVACY.md) and [compatibility notes](docs/COMPATIBILITY.md) before enabling it. The current UI and full disclosures are in Chinese.

This is a free, personal-interest project with no affiliation to Samsung, Google or Apple. MIT licensed; the maintainer's non-profit purpose does not restrict the commercial rights granted by MIT. No warranty is provided, and liability is limited only to the extent permitted by applicable law. Mandatory rights and non-excludable liabilities remain unaffected.

The renderer is a direct Android adaptation of the MIT-licensed [DuoLikeAnimation](https://github.com/elijah-semyonov/DuoLikeAnimation). See [notices](THIRD_PARTY_NOTICES.md). Build with JDK 17 and Android SDK 35: `./gradlew testDebugUnitTest assembleDebug lintDebug`.
