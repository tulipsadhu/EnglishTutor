# English Voice Coach

This repository contains a Java Android app for daily English speaking practice.

## Features
- Bengali (West Bengal/Kolkata) onboarding, translations, and optional Bengali text-to-speech
- A 90-day path: Basic (days 1–30), Everyday (31–60), and Confident Conversation (61–90)
- Thirty daily speaking turns: ten new sentence variants plus twenty spaced-review prompts
- English text-to-speech, speech recognition, spoken feedback, and an animated speaking/listening coach
- Conversation-skills practice for follow-up questions, turn-taking, clear speech, polite disagreement, and confidence reflection
- A searchable Bengali course glossary, plus optional online English dictionary lookups
- On-device profile with completion, streak, weekly practice scores, and study-activity charts
- Lesson advancement uses speech-recognition word-order match; it does not measure pronunciation or accent

## Run from VS Code

Install JDK 17 and Android SDK Platform 35, then connect an Android device with USB debugging enabled or start an emulator. The project includes a Gradle wrapper, so a separate Gradle installation is not needed.

In VS Code, run **Tasks: Run Task** and choose **Android: Launch on Device**. To build without installing, choose **Android: Build Debug APK**. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The SDK should be installed at the standard macOS location (`~/Library/Android/sdk`). Alternatively, set `ANDROID_HOME` to your SDK location before running the tasks. Gradle uses `local.properties` if it exists; that machine-specific file is ignored by Git.

## Notes
Progress and profile data are stored on the device. Dictionary searches require internet; only the searched English word is sent to Free Dictionary API. Dictionary entries display their source and license when provided. Bengali text-to-speech depends on an installed `bn-IN` voice.

Gemini is not connected in this phase. A future Gemini integration needs separate API access and billing; a Google One/Gemini phone subscription is not an app API credential. Current speech scoring compares recognized words and their order, not pronunciation or accent.

## Learning References
The beginner lesson structure was informed by Off2Class's [ESL Speaking Lesson for Beginners: Daily Routine](https://oercommons.org/courseware/lesson/122280), listed on OER Commons under CC BY 4.0. The lesson text in this app is original and localized for West Bengal. The online dictionary uses [Free Dictionary API](https://dictionaryapi.dev/); its entries identify their Wiktionary source and license.
