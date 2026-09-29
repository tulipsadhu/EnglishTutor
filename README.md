# English Voice Coach

This repository contains a first-version Android starter app for an English learning voice assistant.

## Features
- Speak a practice sentence using a voice assistant
- Listen to spoken English through Text-to-Speech
- Capture the user's speech with Android speech recognition
- Build a base for later AI-based pronunciation feedback

## Run from VS Code

Install JDK 17 and Android SDK Platform 35, then connect an Android device with USB debugging enabled or start an emulator. The project includes a Gradle wrapper, so a separate Gradle installation is not needed.

In VS Code, run **Tasks: Run Task** and choose **Android: Launch on Device**. To build without installing, choose **Android: Build Debug APK**. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The SDK should be installed at the standard macOS location (`~/Library/Android/sdk`). Alternatively, set `ANDROID_HOME` to your SDK location before running the tasks. Gradle uses `local.properties` if it exists; that machine-specific file is ignored by Git.

## Notes
This is a beginner-friendly prototype. The next step would be adding API-based AI evaluation, pronunciation scoring, or a lesson flow.
