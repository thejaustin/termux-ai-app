# Termux AI - Enhanced Terminal with Claude & Gemini Integration

A fork of the official Termux app that integrates AI capabilities directly into the terminal experience, inspired by Warp AI but built specifically for Android with support for multiple AI providers.

## 🚀 Features

### 🧠 Native AI Integration
- **Multi-Provider Support** - Choose between **Claude (Anthropic)** and **Gemini (Google)**
- **Real-time Command Assistance** - AI suggestions as you type
- **Intelligent Error Handling** - Automatic error analysis and solutions
- **Context-Aware Help** - Smart assistance based on current project and environment
- **Natural Language Commands** - Convert English to shell commands

### 💻 Enhanced Terminal Experience
- **Smart Autocomplete** - AI-powered command completion
- **Visual Command History** - Enhanced history with AI insights
- **Project Understanding** - Automatic detection of project types and frameworks
- **Code Generation** - Generate scripts and code directly in terminal

### 🎨 Improved UI/UX
- **Floating AI Panel** - Non-intrusive AI assistance overlay
- **ANSI Color Support** - Full 512-color terminal with text attributes
- **Command Suggestions** - Contextual command recommendations
- **Progress Indicators** - Visual feedback for AI operations

## 📱 Architecture

This fork modifies the official Termux app with the following enhancements:

### Core Modifications
- **Terminal Emulator** - Complete PTY implementation with ANSI/VT100 support
- **Native Layer** - C++ PTY management via JNI for real subprocess control
- **Shell Integration** - Deep hooks for command analysis and AI assistance
- **UI Overlays** - AI assistance panels with real-time suggestions
- **Security Layer** - AES-256-GCM encrypted credential storage

### New Components
- **AI Client** - Unified client for Claude & Gemini APIs with secure credential storage
- **Terminal Emulator** - Native PTY with ANSI escape sequence parser
- **Context Engine** - Environment and project awareness
- **Command Analyzer** - Real-time command understanding
- **Suggestion Engine** - Smart command and code suggestions
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
│   │   │   ├── ClaudePatterns.java     # Pattern consolidation
│   │   │   ├── ContextEngine.java      # Environment awareness
│   │   │   ├── CommandAnalyzer.java    # Command processing
│   │   │   └── SuggestionEngine.java   # AI suggestions
│   │   ├── terminal/
│   │   │   ├── TerminalView.java       # ANSI color rendering
│   │   │   └── TerminalSession.java    # Real PTY session
│   │   └── app/
│   │       ├── TabbedTerminalActivity.java     # Intent validation
│   │       └── TermuxAISettingsActivity.java   # AI configuration
│   └── cpp/                            # Native layer
│       └── (native code in terminal-emulator module)
├── terminal-emulator/                  # Terminal module
│   ├── src/main/
│   │   ├── cpp/                        # Native PTY implementation
│   │   │   ├── termux_pty.cpp         # JNI PTY functions
│   │   │   ├── pty_helper.cpp         # PTY utilities
│   │   │   └── CMakeLists.txt         # Native build config
│   │   └── java/com/termux/terminal/
│   │       ├── JNI.java               # JNI bindings
│   │       ├── TerminalEmulator.java  # Emulator coordinator
│   │       ├── TerminalBuffer.java    # Screen buffer
│   │       ├── TerminalRow.java       # Row management
│   │       ├── TerminalOutput.java    # ANSI parser
│   │       └── TextStyle.java         # Color encoding
└── docs/
    ├── DEEP_REVIEW_REPORT.md          # Architecture analysis
    └── TERMINAL_IMPLEMENTATION.md      # Implementation details
```

### Key Integration Points

1. **Terminal Input Processing**
   - Intercept keystrokes for real-time analysis
   - Inject AI suggestions into input stream
   - Handle special AI commands and shortcuts

2. **Command Execution Hooks**
   - Pre-execution: Command validation and suggestions
   - Post-execution: Error analysis and learning
   - Background: Context updates and pattern detection

3. **UI Enhancements**
   - Floating suggestion panels
   - Command history with AI insights
   - Settings for AI configuration
   - Progress indicators for AI operations

## 🏗 Build Instructions

### Prerequisites
- Android Studio 4.0+
- Android SDK 21+
- NDK for native components
- Claude API Key or Gemini API Key

### Building from Source

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/termux-ai.git
   cd termux-ai
   ```

2. **Setup Dependencies**
   ```bash
   # Initialize submodules
   git submodule update --init --recursive
   
   # Install NDK dependencies
   ./gradlew setupNDK
   ```

3. **Build the APK**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (requires signing)
   ./gradlew assembleRelease
   ```

## 📦 Installation

### From GitHub Releases (Recommended)
1. Go to the [Releases Page](https://github.com/your-username/termux-ai/releases) of this repository.
2. Download the latest `termux-ai-release-apk` or `termux-ai-debug-apk`.
3. Enable "Unknown Sources" in Android settings.
4. Install the APK.
5. Grant necessary permissions.
6. **Important:** Go to **Settings > AI Integration** to configure your API Key.

### F-Droid (Planned)
- F-Droid repository submission planned for stable release

## ⚙️ Configuration

### AI Settings
Access AI settings through: **Settings > AI Integration**

- **AI Provider** - Select **Claude** or **Gemini**.
- **API Key** - Enter your API key for the selected provider.
- **Model Selection** - Choose specific model variants (e.g., Gemini Flash, Claude Sonnet).
- **Suggestion Frequency** - Control how often AI suggests commands.

### Privacy Controls
- **Data Retention** - Control how long AI interactions are stored
- **Analytics** - Opt-in/out of usage analytics
- **Command Filtering** - Exclude sensitive commands from AI
- **Local Processing** - Use on-device AI when available

## 🔒 Privacy & Security

### Security Features
- **AES-256-GCM Encryption** - API keys encrypted at rest using AndroidX Security Crypto
- **Secure API Headers** - API keys transmitted via secure HTTP headers (not URLs)
- **Intent Validation** - Protection against intent injection attacks
- **Certificate Pinning** - Infrastructure ready for TLS certificate pinning
- **Minimal Permissions** - Only essential permissions requested

### Data Handling
- **Local First** - AI context processed locally when possible
- **Encrypted Storage** - API keys and credentials never stored in plaintext
- **Secure Communication** - TLS 1.3 encryption for all API calls
- **No Sensitive Data** - Passwords and keys never sent to AI providers
- **Backup Protection** - Encrypted data excluded from device backups

### Permissions
- **Network** - For AI API communication (required)
- **Storage** - For caching and configuration (limited to app scope)

## 🎯 Roadmap

### Phase 1 - Core Foundation ✅ COMPLETE
- [x] Basic AI integration (Claude)
- [x] Gemini Provider Support
- [x] Command suggestions
- [x] Error analysis
- [x] Security hardening (AES-256-GCM encryption)
- [x] Real terminal emulation (PTY + ANSI parser)
- [x] ANSI color support (512 colors)
- [x] Intent validation and permission cleanup
- [ ] UI polish and optimization (in progress)

### Phase 2 (Planned)
- [ ] Voice command support
- [ ] Multi-language support
- [ ] Plugin system for AI extensions
- [ ] Team collaboration features

### Phase 3 (Future)
- [ ] On-device AI models
- [ ] Custom AI training
- [ ] Advanced code generation
- [ ] Integration with development tools

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup
1. Fork the repository
2. Set up development environment
3. Create feature branch
4. Make changes with tests
5. Submit pull request

### Areas for Contribution
- AI model optimization
- UI/UX improvements
- Documentation
- Testing and bug fixes
- Translation

## 📄 License

This project is licensed under the GPLv3 License - see [LICENSE](LICENSE) file for details.

Original Termux app is also licensed under GPLv3.

## 🙏 Acknowledgments

- **Termux Team** - For the amazing terminal app foundation
- **Anthropic & Google** - For their powerful AI models
- **Warp** - For inspiration on AI-enhanced terminals
- **Community** - For feedback and contributions

## 📞 Support

- **Issues** - [GitHub Issues](https://github.com/your-username/termux-ai/issues)
- **Discussions** - [GitHub Discussions](https://github.com/your-username/termux-ai/discussions)
- **Matrix** - [#termux-ai:matrix.org](https://matrix.to/#/#termux-ai:matrix.org)
- **Email** - support@termux-ai.org

---

*Transforming the mobile terminal experience with AI assistance*