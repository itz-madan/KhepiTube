<div align="center">

# 🎬 KhepiTube

### *Videos. Background Playback. Local Media. Ad-Free.*

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

**KhepiTube** is a lightweight, ad-free video streaming and local media player for Android, built with modern **Kotlin, Jetpack Compose, and Material 3**.

Designed around a simple philosophy:

**Watch videos. Play in the background. Skip sponsors. Download media. Enjoy your local library.**

No unnecessary complexity. Just the features that matter.

Designed and maintained with ❤️ by **[KhepiLabs](https://khepilabs.com)**.

---

[Features](#-key-features) •
[Design](#-design-philosophy) •
[Architecture](#-architecture--tech-stack) •
[Build](#-building-from-source) •
[Download](#-download) •
[Installation](#-installation-guide) •
[Contributing](#-contributing) •
[License](#-license--acknowledgments)

</div>

---

## ✨ Key Features

### 🍿 Video Streaming & Playback

- **Ad-Free Experience:** Enjoy supported video content without traditional advertisement interruptions.
- **Background Playback:** Continue audio playback while using other apps or when the screen is off.
- **Picture-in-Picture:** Watch videos in a floating window while multitasking, where supported.
- **Playback Speed:** Adjust playback speed according to your preference.
- **Video Quality:** Choose from the resolutions and formats available for a video.
- **Smart Playback Controls:** Access essential play/pause, seeking, fullscreen and playback controls.
- **Remember Playback Position:** Continue watching from where you left off.
- **Mini Player:** Keep playback accessible while navigating the app.
- **Queue and Autoplay:** Manage playback order and continue to the next video where supported.

### ⏭️ SponsorBlock Integration

Skip unwanted portions of supported videos with SponsorBlock.

- Automatic sponsor-segment skipping.
- Intro and outro skipping.
- Interaction-reminder skipping.
- On-screen skip controls and notifications.
- Simple controls to enable or disable SponsorBlock.
- Skip behavior depends on available community-submitted segment data.

### 📥 Downloads

Save supported media for offline access.

- Video downloading.
- Audio downloading.
- Available quality and format selection.
- Download progress tracking.
- Download queue management.
- Pause, resume and cancel downloads.
- Retry failed downloads.
- Access downloaded media from the Downloads section.

### 📁 Local Media

Your device, your media library.

- Browse local videos and audio files.
- Organize and browse media through folders.
- Grid and list browsing.
- Local video playback.
- Local audio playback.
- Background audio playback.
- Resume previously played media.
- Continue watching local videos.
- Manage your local media library.
- Trash and restore support where available.

### 📱 Shorts & Video Discovery

- Dedicated Shorts viewing experience.
- Vertical video playback.
- Smooth scrolling and playback controls.
- Personalized recommendations for supported video feeds.

### 🔒 Privacy & Local-First Experience

- No mandatory account required for basic local media playback.
- Local media remains on your device.
- Playback history and library information can be stored locally.
- No unnecessary background services.
- A lightweight experience focused on essential features.

---

## 💎 Design Philosophy

### Liquid Glass UI

KhepiTube features a modern Liquid Glass-inspired interface.

The design focuses on clarity, smooth navigation and visual consistency.

- Translucent glass-style surfaces.
- Subtle background blur.
- Soft borders and layered surfaces.
- Clean and minimal layouts.
- Consistent glass-style components.
- Smooth and lightweight interactions.

### Minimal by Design

KhepiTube prioritizes essential functionality over excessive options.

The interface is designed to keep important features accessible without overwhelming users with unnecessary customization.

---

## ⚙️ Settings & Controls

Settings are organized into simple, focused sections.

| Section | Purpose |
|---|---|
| Appearance & Language | Theme and language preferences |
| Playback | Video playback and background playback |
| SponsorBlock | Automatic sponsor skipping |
| Downloads | Download preferences and management |
| Local Media | Local video and audio library |
| Audio | Essential audio playback settings |
| Data & Storage | Cache and storage management |
| Account | Account-related options, if available in the build |
| System | Notifications and app permissions |
| About | Version and application information |

---

## 🛠️ Architecture & Tech Stack

| Layer | Technology |
|---|---|
| UI Framework | Jetpack Compose |
| Design System | Material 3 |
| Architecture | MVVM / MVI |
| Media Playback | AndroidX Media3 / ExoPlayer |
| Extraction Engine | Native InnerTube Client + NewPipe Fallback |
| Database | Room Database |
| Preferences | DataStore |
| Reactive State | Kotlin StateFlow |
| Dependency Injection | Dagger Hilt |
| Networking | OkHttp / Ktor |
| Image Loading | Coil |

---

## 🚀 Building from Source

### Prerequisites

- Android Studio (Ladybug / 2024.2.1+)
- JDK version required by the project Gradle configuration
- Android SDK
- Git

### Clone Repository

```bash
git clone https://github.com/itz-madan/KhepiTube.git
cd KhepiTube
```

### Check Code Quality

```bash
./gradlew ktlintCheck
```

### Build Debug APK

Use the build variant configured by the project. For the standard debug variant:

```bash
./gradlew :app:assembleDebug --no-daemon
```

If your checkout uses the GitHub flavor, build it with:

```bash
./gradlew :app:assembleGithubDebug --no-daemon
```

### Build Release APK

For the standard release variant:

```bash
./gradlew :app:assembleRelease --no-daemon
```

If your checkout uses the GitHub flavor:

```bash
./gradlew :app:assembleGithubRelease --no-daemon
```

Build outputs are generated under:

```text
app/build/outputs/apk/
```

The exact output directory and available APK variants depend on the project's Gradle build configuration.

---

## 📥 Download

Get the latest published KhepiTube release:

**[Download Latest Release](https://github.com/itz-madan/KhepiTube/releases/latest)**

Development builds, when available, can be found through GitHub Actions:

**[GitHub Actions Builds](https://github.com/itz-madan/KhepiTube/actions)**

Choose the APK that matches your device architecture if architecture-specific builds are provided.

| Architecture | Compatible Devices |
|---|---|
| ARM64-v8a | Modern 64-bit ARM Android devices |
| armeabi-v7a | Older 32-bit ARM devices |
| x86 | 32-bit Intel-compatible devices and emulators |
| x86_64 | 64-bit Intel/AMD devices and emulators |
| Universal | Multiple supported architectures |

The available APK names and architectures depend on the release workflow. If only one APK is published, use that build.

---

## 📱 Installation Guide

### Step 1 — Download the APK

1. Open the [latest GitHub Release](https://github.com/itz-madan/KhepiTube/releases/latest).
2. Find the APK provided for that release.
3. Download the APK to your Android device.

### Step 2 — Allow Installation

If Android blocks installation:

1. Open Android **Settings**.
2. Navigate to **Apps → Special app access → Install unknown apps**.
3. Select the browser or file manager used to download the APK.
4. Enable **Allow from this source**.

Menu names may vary depending on your Android version and device manufacturer.

### Step 3 — Install KhepiTube

1. Open your Downloads folder or file manager.
2. Locate the downloaded KhepiTube APK.
3. Tap the APK file.
4. Press **Install**.
5. Wait for installation to finish.
6. Open KhepiTube.

### 🔄 Updating an Existing Installation

1. Download the newer APK that matches your existing installation.
2. Open the downloaded APK.
3. Select **Update** or **Install** when prompted.
4. Wait for the update to complete.

For a normal update, install over the existing app rather than uninstalling it first. Uninstalling may erase locally stored app data. If Android reports a signature conflict, make sure the new APK is signed with the same signing key as the installed version.

### ❓ Installation Troubleshooting

| Problem | Suggested Solution |
|---|---|
| App not installed | Check device compatibility and available storage |
| Parse error | Download the APK again; the file may be incomplete |
| App not compatible | Check the Android version and APK architecture |
| Installation blocked | Enable installation permission for your browser or file manager |
| Signature conflict | Verify that the APK comes from the same official signing source as the installed app |

---

## 🤝 Contributing

Contributions are welcome!

You can help improve KhepiTube by:

- Reporting bugs.
- Suggesting improvements.
- Improving performance.
- Contributing code.
- Improving the user interface.
- Helping with documentation and testing.

Please read the following files before contributing:

- [CONTRIBUTING.md](CONTRIBUTING.md)
- [AGENTS.md](AGENTS.md)

---

## 📜 License & Acknowledgments

### License

KhepiTube is free and open-source software licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

See [LICENSE](LICENSE) for details.

### Attribution & Credits

- **Original Project:** [Flow by A-EDev](https://github.com/A-EDev)
- **Extraction Engine:** [TeamNewPipe / NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor)
- **Media Playback:** AndroidX Media3 / ExoPlayer
- **Libraries:** AndroidX, Jetpack Compose, Material 3, Kotlin, OkHttp, Ktor, Coil and Hilt.

---

<div align="center">

### Made with ❤️ by KhepiLabs

**KhepiTube — Simple. Lightweight. Focused.**

[GitHub Repository](https://github.com/itz-madan/KhepiTube)

</div>
