# Termux AI - Enhanced Terminal with Claude & Gemini Integration

A fork of the official Termux app that integrates AI capabilities directly into the terminal experience, inspired by Warp AI but built specifically for Android with support for multiple AI providers.

## 🚀 Features

### 🧠 Native AI Integration
- **Multi-Provider Support** - Choose between **Claude (Anthropic)** and **Gemini (Google)**
- **Real-time Command Assistance** - AI suggestions as you type
- **Intelligent Error Handling** - Automatic error analysis and solutions
- **Context-Aware Help** - Smart assistance based on current project and environment
- **Natural Language Commands** - Convert English to shell commands
- **Privacy First** - API keys stored in **EncryptedSharedPreferences (AES256-GCM)**

### 💻 Professional Terminal Experience
- **Real PTY Emulation** - Native C++ backend via JNI for authentic terminal behavior
- **ANSI/VT100 Support** - Full escape sequence parsing
- **512-Color Support** - True color support with text attributes (bold, italic, underline, etc.)
- **Smart Autocomplete** - AI-powered command completion
- **Visual Command History** - Enhanced history with AI insights
- **Project Understanding** - Automatic detection of project types and frameworks

### 🎨 Improved UI/UX
- **Floating AI Panel** - Non-intrusive AI assistance overlay
- **Material Design 3** - Modern UI components and theming
- **Command Suggestions** - Contextual command recommendations
- **Progress Indicators** - Visual feedback for AI operations

## 🔒 Security & Privacy

We take security seriously. This app handles shell access and AI credentials, so we've implemented production-grade security:

- **Encrypted Storage**: API keys are encrypted at rest using AndroidX Security (AES-256-GCM). Keys are *never* stored in plaintext.
- **Secure Transmission**: API keys are sent via secure HTTP headers, never in URLs.
- **Intent Validation**: Strict validation prevents intent injection attacks.
- **Minimized Permissions**: We've removed dangerous permissions like `RECORD_AUDIO`, `CAMERA`, and `SYSTEM_ALERT_WINDOW`.
- **Backup Protection**: Sensitive credentials are explicitly excluded from Android backups.
- **Certificate Pinning Ready**: Infrastructure is in place for SSL pinning.

## 📱 Architecture

This fork modifies the official Termux app with the following enhancements:

### Core Modifications
- **Terminal Emulator** - Complete PTY implementation with ANSI/VT100 support
- **Native Layer** - C++ PTY management via JNI for real subprocess control
- **Shell Integration** - Deep hooks for command analysis and AI assistance
- **UI Overlays** - AI assistance panels with real-time suggestions

### New Components
- **AI Client** - Unified client for Claude & Gemini APIs
- **Terminal Emulator** - Native PTY with ANSI escape sequence parser (in `:terminal-emulator` module)
- **Context Engine** - Environment and project awareness
- **Command Analyzer** - Real-time command understanding
- **Security Manager** - Encrypted preferences and certificate pinning

## 🔧 Technical Details

### Project Structure
```
termux-ai/
├── app/src/main/
│   ├── java/com/termux/
│   │   ├── ai/                          # AI integration package
│   │   │   ├── AIClient.java           # Unified AI client with encryption
│   │   │   ├── EncryptedPreferencesManager.java  # Credential encryption
│   │   │   └── ...
│   │   └── terminal/
│   │       ├── TerminalSession.java    # Real PTY session wrapper
│   │       └── ...
├── terminal-emulator/                  # Complete Terminal Module
│   ├── src/main/
│   │   ├── cpp/                        # Native PTY implementation (C++)
│   │   └── java/com/termux/terminal/   # Terminal emulation logic
└── docs/
    ├── DEEP_REVIEW_REPORT.md          # Architecture analysis
    └── TERMINAL_IMPLEMENTATION.md      # Implementation details
```

## 🏗 Build Instructions

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 34+
- **Android NDK** (Side-by-side) - Required for C++ PTY compilation
- CMake 3.22.1+

### Building from Source

1. **Clone the Repository**
   ```bash
   git clone https://github.com/thejaustin/termux-ai-app.git
   cd termux-ai-app
   ```

2. **Setup Dependencies**
   Ensure you have the NDK installed via Android Studio SDK Manager.

3. **Build the APK**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   ```

   *Note: Building directly on an Android device (in Termux) is currently not supported due to NDK requirements.*

## 📦 Installation

### From GitHub Releases
1. Go to the [Releases Page](https://github.com/thejaustin/termux-ai-app/releases).
2. Download the latest `app-debug.apk`.
3. Enable "Unknown Sources" in Android settings.
4. Install the APK.
5. Grant necessary permissions.
6. **Important:** Go to **Settings > AI Integration** to configure your API Key.

## ⚙️ Configuration

### AI Settings
Access AI settings through: **Settings > AI Integration**

- **AI Provider** - Select **Claude** or **Gemini**.
- **API Key** - Enter your API key. It will be encrypted immediately.
- **Model Selection** - Choose specific model variants.

## 🎯 Roadmap

### Phase 1 - Core Foundation ✅ COMPLETE
- [x] Basic AI integration (Claude & Gemini)
- [x] Security hardening (AES-256-GCM)
- [x] Real terminal emulation (PTY + ANSI parser)
- [x] Native C++ implementation
- [x] Intent validation and permission cleanup

### Phase 2 (In Progress)
- [ ] UI polish and optimization
- [ ] Voice command support
- [ ] Multi-language support
- [ ] Plugin system

## 📄 License

This project is licensed under the GPLv3 License.

## 📞 Support

- **Issues** - [GitHub Issues](https://github.com/thejaustin/termux-ai-app/issues)
