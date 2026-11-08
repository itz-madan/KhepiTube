<div align="center">

# 🎬 KhepiTube

### *Videos. Music. Local Media. Unlimited & Ad-Free.*

<p align="center">
  <a href="https://github.com/itz-madan/KhepiTube/releases/latest">
    <img src="https://img.shields.io/github/v/release/itz-madan/KhepiTube?style=for-the-badge&color=e53935&label=Release" alt="Latest Release">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge" alt="License: GPL v3">
  </a>
  <a href="https://kotlinlang.org">
    <img src="https://img.shields.io/badge/Kotlin-2.1.10-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  </a>
  <a href="https://developer.android.com">
    <img src="https://img.shields.io/badge/Android-API_26+-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white" alt="Android API">
  </a>
</p>

**KhepiTube** is a high-performance, lightweight, ad-free video, music, and local media client for Android built with modern **Jetpack Compose** and **Material 3 Expressive UI**.

Designed and maintained with ❤️ by **[KhepiLabs](https://khepilabs.com)**.

---

[Features](#-key-features) • [Architecture](#-architecture--tech-stack) • [Build from Source](#-building-from-source) • [License](#-license--acknowledgments)

</div>

---

## ✨ Key Features

### 🍿 Video Streaming & Background Play
* **100% Ad-Free**: Enjoy uninterrupted video streaming without advertisement interruptions.
* **1-Tap Audio Switch**: Instant `Headphones` overlay button to convert video playback into seamless background audio mode.
* **Picture-in-Picture (PiP)**: Smooth PiP transitions for effortless multitasking.
* **Variable Resolution & Speed**: Fine-grained playback speed slider and quality selector (144p up to 4K 60fps).
* **Smart Integrations**: Integrated **SponsorBlock** auto-skip, **DeArrow** clickbait replacement, and **Return YouTube Dislike**.

### 🎵 YouTube Music & Nearby Song Scanner
* **YouTube Music Integration**: Discover trending charts, top tracks, artist albums, curated playlists, and synced lyrics.
* **Nearby Shazam Scanner**: Identify songs playing in your environment in real time with 1 tap.
* **Custom Equalizer**: Multi-band equalizer with custom presets and audio enhancement.
* **Vertical Shorts Feed**: Watch YouTube Shorts with auto-scroll, speed boost, and full gesture controls.

### 🗑️ Local Media & Complete Trash Bin
* **Full Local Media Management**: Organize local videos and music with folder and grid views.
* **Smart Trash Bin**:
  * Real-time TopBar badge indicator.
  * Inline **Trash Bin Banner** with 30-day auto-delete notifications.
  * **Multi-Select Management**: Batch restore, filter by type (Videos/Music), storage calculation, and permanent deletion confirmation dialogs.

### 🔐 Local Storage & Privacy
* **Zero Tracking**: No account login, no telemetry, no background analytics.
* **100% On-Device Data**: Watch history, subscriptions, and playlists stay encrypted on your phone.
* **In-App Auto Updater**: Direct 1-tap update installation from GitHub Releases.

---

## 🛠️ Architecture & Tech Stack

| Layer | Technology |
| --- | --- |
| **UI Framework** | Jetpack Compose, Material 3 Expressive UI |
| **Architecture** | MVVM / MVI, Unidirectional Data Flow, Clean Architecture |
| **Media Playback** | AndroidX Media3 / ExoPlayer, Custom Audio Processing |
| **Extraction Engine** | Native InnerTube Client + NewPipe Fallback |
| **Database & State** | Room Database, DataStore Preferences, Kotlin StateFlow |
| **Dependency Injection** | Dagger Hilt |

---

## 🚀 Building from Source

### Prerequisites
* **Android Studio** (Ladybug / 2024.2.1+)
* **JDK 17**
* **Android SDK** (API 26 to 36)

### Command Line Build

```bash
# 1. Clone repository
git clone https://github.com/itz-madan/KhepiTube.git
cd KhepiTube

# 2. Check code quality
./gradlew ktlintCheck

# 3. Build Debug APK
./gradlew :app:assembleGithubDebug

# 4. Build Universal Release APK
./gradlew :app:assembleGithubRelease
```

Output APK will be generated at:
`app/build/outputs/apk/github/release/`



## 📥 Download KhepiTube

Choose the APK that matches your device's CPU architecture.

| APK File | Compatible Devices |
|---|---|
| `app-arm64-v8a-release.apk` | Modern 64-bit ARM Android phones and tablets |
| `app-armeabi-v7a-release.apk` | Older 32-bit ARM Android devices |
| `app-x86-release.apk` | 32-bit Intel x86 devices and compatible emulators |
| `app-x86_64-release.apk` | 64-bit Intel/AMD x86-64 devices and compatible emulators |
| `app-universal-release.apk` | General-purpose APK for multiple supported architectures |

**Not sure which one to download?**

- Most modern Android smartphones use **ARM64-v8a**.
- Older or 32-bit ARM phones should use **armeabi-v7a**.
- Android emulators may use **x86** or **x86_64**, depending on their configured system image.
- If you cannot identify your architecture, try the **Universal APK**.

*Note: Universal APK is larger because it includes support for multiple architectures. It is a convenient fallback, but the architecture-specific APK is generally preferable when you know your device architecture.*

## 📱 Installation Guide

### Step 1 — Download the APK
1. Open the Assets section below this release.
2. Find the APK compatible with your device.
3. Tap the APK filename to download it.

### Step 2 — Allow App Installation
If Android blocks the installation:

1. Open **Settings** on your device.
2. Go to **Apps → Special app access → Install unknown apps**.
3. Select the browser or file manager you used to download the APK.
4. Enable **Allow from this source**.

*The exact menu names may vary depending on your Android version and device manufacturer.*

### Step 3 — Install KhepiTube
1. Open your Downloads folder or file manager.
2. Locate the downloaded `KhepiTube APK`.
3. Tap the APK file.
4. Press **Install**.
5. Wait for the installation to finish.
6. Open KhepiTube and enjoy! 🎉

### 🔄 Updating from an Older Version
If you already have KhepiTube installed:

1. Download the newer APK matching your device architecture.
2. Open the downloaded APK.
3. Tap **Update** or **Install** when prompted.
4. Wait for the update to complete.

**Important:** For a normal update, install over your existing app rather than uninstalling it first. Uninstalling may erase locally stored app data. If Android reports a signature conflict, verify that you downloaded the correct official build.

### ❓ Installation Troubleshooting

| Problem | Suggested Solution |
|---|---|
| App not installed | Check architecture compatibility and available storage |
| Parse error | Download the APK again; the file may be incomplete |
| App not compatible | Try the correct architecture-specific APK |
| Installation blocked | Enable installation permission for your browser/file manager |
| Signature conflict | Check whether the existing app was signed with a different key |

---


## 🤝 Contributing

We welcome contributions from the community! Check out [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md) for guidelines on code style, issue reporting, and pull requests.

---

## 📜 License & Acknowledgments

### License
KhepiTube is free software licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See [LICENSE](LICENSE) for details.

### Attribution & Credits
* **Original Project**: Flow by [A-EDev](https://github.com/A-EDev)
* **Extraction Engine**: [TeamNewPipe](https://github.com/TeamNewPipe/NewPipeExtractor)
* **Libraries**: AndroidX, Material 3, ExoPlayer, OkHttp, Ktor, Coil, Hilt.

---

<div align="center">

Made with ❤️ by **[KhepiLabs](https://khepilabs.com)**

</div>
