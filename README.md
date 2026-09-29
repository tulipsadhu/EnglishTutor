# English Voice Coach

This repository contains a Java Android app for daily English speaking practice.

## Features
- Bengali-language onboarding and optional Bengali lesson help
- A 12-lesson path from Basic through Advanced English
- Spoken prompts, speech recognition, and spoken practice feedback
- Locally saved lesson progress, practice days, and streak
- Lesson advancement based on recognized word-order match; this is not pronunciation or accent scoring

## Run from VS Code

Install JDK 17 and Android SDK Platform 35, then connect an Android device with USB debugging enabled or start an emulator. The project includes a Gradle wrapper, so a separate Gradle installation is not needed.

In VS Code, run **Tasks: Run Task** and choose **Android: Launch on Device**. To build without installing, choose **Android: Build Debug APK**. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The SDK should be installed at the standard macOS location (`~/Library/Android/sdk`). Alternatively, set `ANDROID_HOME` to your SDK location before running the tasks. Gradle uses `local.properties` if it exists; that machine-specific file is ignored by Git.

## Notes
Progress is stored on the device. Gemini is not connected in this phase; a Gemini API integration will need separate API access and billing, rather than a Google One/Gemini phone subscription.
