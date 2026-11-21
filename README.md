<div align="center">

# 🤖 LocalGPT-AndroidAiAgent

**Modern Android AI Agent & Chat Hub built with Kotlin and Jetpack Compose**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Language](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

</div>

---

## ✨ Key Features

* **Animated Sidebar & Chat Sessions:** Slide-out drawer with smooth spring animations, `+ New chat`, real-time chat search, and session management.
* **Tokens Used Widget & Usage Controls:**
  * Displays total tokens, `↑ in` / `↓ out` progress ratio, and estimated `Cost` (`USD`).
  * **Toggleable in Settings → Usage:** Turn the `Tokens used` widget **ON** or **OFF** anytime to hide or show the token card in the sidebar and token badge in the top bar.
  * Configurable session & weekly token limits, counter reset options, and provider billing shortcuts.
* **48 Languages + AutoSync Lang:** Defaults to English (`en`), supports **48 languages**, and can automatically detect and sync language across prompts, UI alerts, and settings.
* **Multi-Provider AI & Custom Endpoints:** Configure **Provider**, **API Key**, **Endpoint URL**, **Model ID**, and **System Prompt** directly in the **AI** settings tab.
* **Themes & Motion Levels:** Switch between visual themes and choose `Full`, `Lite`, or `Off` animation modes plus startup splash toggle.
* **Local Storage & Backup (`SharedPreferences`):** Stores AI settings, client preferences, and chat data locally on device, with JSON export/import and reset support.

---

## 📁 Project Structure

```text
LocalGPT-AndroidAiAgent/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── ai/
│   │   │   │           └── agent/
│   │   │   │               ├── ui/
│   │   │   │               │   └── theme/              # Jetpack Compose theme, color & typography
│   │   │   │               ├── AgentEngine.kt          # Core AI agent execution & API request engine
│   │   │   │               ├── AgentModels.kt          # Agent configuration, mode & error models
│   │   │   │               ├── HubSettingsModels.kt    # Theme palettes, storage, usage stats & presets
│   │   │   │               └── MainActivity.kt         # Main UI, animated sidebar, 48-lang AutoSync & settings
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   └── ic_launcher_foreground.xml
│   │   │   │   ├── mipmap/
│   │   │   │   │   ├── ic_launcher/
│   │   │   │   │   ├── ic_launcher_foreground/
│   │   │   │   │   └── ic_launcher_round/
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   │   └── java/com/ai/agent/                      # Instrumented Android tests
│   │   └── test/
│   │       └── java/com/ai/agent/                      # Local unit tests
│   └── build.gradle.kts                                # Module (:app) Gradle configuration
├── gradle/
│   └── wrapper/                                        # Gradle wrapper files
├── build.gradle.kts                                    # Root project Gradle build script
├── settings.gradle.kts                                 # Gradle module & repository settings
├── gradle.properties                                   # Project-wide Gradle properties
├── gradlew                                             # Gradle wrapper script (Linux/macOS)
├── gradlew.bat                                         # Gradle wrapper script (Windows)
├── .gitignore                                          # Android & Gradle ignored files
├── LICENSE                                             # MIT License
└── README.md                                           # Project documentation
```

*(Note: Local build/IDE folders such as `.gradle/`, `.idea/`, `.kotlin/`, `build/`, and `local.properties` are automatically excluded by `.gitignore`.)*

---

## 🛠️ Build & Export APK

Run the following command in PowerShell from the project root directory to build a debug APK and copy it to your Downloads folder:

```powershell
$outDir = "$env:USERPROFILE\Downloads\LocalGPT Android"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
.\gradlew.bat assembleDebug
Copy-Item -Force "app\build\outputs\apk\debug\app-debug.apk" "$outDir\LocalGPTHub.apk"
```

---

## 🔒 Privacy Note

* API keys and chat configurations are stored locally on your device.
* Avoid sharing exported backup JSON files if you choose to include API keys (`Include API keys in export`).

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

**Author:** [@theleduylamofficialvngpro](https://github.com/theleduylamofficialvngpro)
