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
