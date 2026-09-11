# Third-party notices and provenance

## Renderer: DuoLikeAnimation — MIT

Copyright (c) 2026 Elijah Semyonov.

Source: https://github.com/elijah-semyonov/DuoLikeAnimation

Pinned source commit: be927684c8585ce3d90761095284329dfdeff901

Source path: DuoLikeAnimation/Shaders/DuoFold.metal

TabFold's `app/src/main/res/raw/fold_surface.agsl` directly adapts this MIT-licensed source to Android AGSL, an opaque bitmap input, a bottom-edge hinge and bounded 16-sample scattering. Original copyright and permission text are preserved at `app/src/main/assets/licenses/DuoLikeAnimation-MIT.txt` and bundled in the APK. Local modifications are also MIT licensed.

## Android application libraries — Apache-2.0

AndroidX Core, Activity, Compose UI / Material3 and Lifecycle are provided by The Android Open Source Project. Kotlin and its standard library are provided by JetBrains and contributors. Their respective notices and licenses apply. The Apache-2.0 text is bundled at `app/src/main/assets/licenses/Apache-2.0.txt`.

Sources: https://android.googlesource.com/platform/frameworks/support/ and https://github.com/JetBrains/kotlin

## Build tools — Apache-2.0

The Gradle wrapper library is from Gradle 8.7; its SHA-256 was checked against the official distribution metadata. Its copyright and license remain with Gradle and contributors. Android Gradle Plugin and Kotlin Gradle plugins are build dependencies, not app features. See https://github.com/gradle/gradle and https://www.apache.org/licenses/LICENSE-2.0 .

## Device names

`device_names.json` contains factual model-to-marketing-name pairs for Samsung tablets, selected from Google's public Android supported-device catalog on 2026-09-11: https://storage.googleapis.com/play_public/supported_devices.csv . The map does not imply device certification or app compatibility. Trademarks belong to their respective owners.

## Earlier experiment

The private proof of concept began with Atomicx7/Duo-animation v1.0. At the publication audit, that Android repository had no explicit license. This standalone release therefore excludes its demo dashboard, Compose fold modifier, original AGSL file and other demo assets. Android entry points, project configuration and UI are separately authored; the renderer is ported directly from the licensed Metal source above. This note describes provenance and does not imply endorsement.

## App icon and project-owned code

The TabFold icon is a project-authored vector drawing of a tablet and keyboard, with no Apple or Samsung logo assets. The device adaptation, automatic trigger service, calibration, settings and disclosures are covered by the project MIT license.

This release includes no original device screenshots, user diagnostic logs, credentials, serial numbers or signing keys.
