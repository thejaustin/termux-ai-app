# Termux+ - Modular AI-Enhanced Terminal for Android (v2.0.0)

A powerful, modular fork of the official Termux app that transforms the terminal into an AI-native development environment. Built for extensibility, Termux+ features a robust plugin system and deep integrations with leading AI models like Claude and Gemini.

## 🚀 Key Features

### 🧩 Modular Plugin Architecture (NEW in v2.0.0)
- **Plugin Manager** - Enable, disable, and configure extensions at runtime.
- **AI Provider API** - Swap between different AI backends (Claude, Gemini, etc.) via plugins.
- **Terminal Extensions** - Expand terminal functionality with official and third-party plugins.
- **Customizable Experience** - Only load the features you need.

### 🧠 Advanced AI Integrations
- **Claude Code Integration** - Full support for Anthropic's Claude Code CLI with native progress tracking.
- **Multi-Provider Support** - Choose between **Claude**, **Gemini**, and more.
- **Real-time Assistance** - AI-powered command suggestions and error analysis.
- **Voice Commands** - Spoken command input directly into the terminal.
- **Context-Aware** - Automatically detects project types (Node.js, Python, Rust, etc.) for smarter assistance.

### 💻 Professional Terminal Experience
- **Multi-Window Support** - Open multiple independent terminal windows with isolated sessions.
- **Tabbed Interface** - Manage multiple projects simultaneously with intuitive tabs.
- **Performance Optimized** - Background processing for regex parsing and persistence to ensure zero lag.
- **Material You (M3)** - Dynamic theming that adapts to your wallpaper.
- **Real PTY Emulation** - Native C++ backend for authentic shell behavior.

## 🔒 Security & Privacy

We take security seriously. This app handles shell access and AI credentials, so we've implemented production-grade security:

- **Encrypted Storage**: API keys are encrypted at rest using AndroidX Security (AES-256-GCM). Keys are *never* stored in plaintext.
- **Secure Transmission**: API keys are sent via secure HTTP headers, never in URLs.
- **Intent Validation**: Strict validation prevents intent injection attacks.
- **Minimized Permissions**: We've removed unused dangerous permissions.
- **Backup Protection**: Sensitive credentials are explicitly excluded from Android backups.

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

2. **Build the APK**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   ```

*Note: Building directly on an Android device (in Termux) is currently not supported due to NDK requirements.*

## 🏗 Roadmap

### Phase 1 - Core Foundation ✅ COMPLETE
- [x] Basic AI integration (Claude & Gemini)
- [x] Security hardening (AES-256-GCM)
- [x] Real terminal emulation (PTY + ANSI parser)
- [x] Native C++ implementation

### Phase 2 - Modularity & Polish ✅ COMPLETE
- [x] **Modular Plugin System**
- [x] **Multi-Window Support**
- [x] **Performance Optimizations**
- [x] **Real Voice Input**
- [x] **Termux+ Rebranding**

### Phase 3 - Ecosystem Expansion (Next)
- [ ] Third-party Plugin SDK (DEX/APK loading)
- [ ] On-device AI model support
- [ ] Enhanced Project Insights tool
- [ ] Cloud Context Sync

## 📄 License

This project is licensed under the GPLv3 License.

## 📞 Support

- **Issues** - [GitHub Issues](https://github.com/thejaustin/termux-ai-app/issues)