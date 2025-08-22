# Termux AI - Enhanced Terminal with Claude Integration

A fork of the official Termux app that integrates AI capabilities directly into the terminal experience, inspired by Warp AI but built specifically for Android and Claude integration.

## 🚀 Features

### 🧠 Native AI Integration
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
- **Syntax Highlighting** - Enhanced terminal output formatting
- **Command Suggestions** - Contextual command recommendations
- **Progress Indicators** - Visual feedback for AI operations

## 📱 Architecture

This fork modifies the official Termux app with the following enhancements:

### Core Modifications
- **Terminal Emulator** - Enhanced with AI input processing
- **Shell Integration** - Deep hooks for command analysis
- **UI Overlays** - AI assistance panels and suggestions
- **Background Services** - AI processing and context management

### New Components
- **AI Client** - Claude API integration with OAuth
- **Context Engine** - Environment and project awareness
- **Command Analyzer** - Real-time command understanding
- **Suggestion Engine** - Smart command and code suggestions

## 🔧 Technical Details

### Modified Files
```
app/src/main/java/com/termux/
├── ai/                          # New AI integration package
│   ├── AIClient.java           # Claude API client
│   ├── ContextEngine.java      # Environment awareness
│   ├── CommandAnalyzer.java    # Command processing
│   └── SuggestionEngine.java   # AI suggestions
├── terminal/
│   ├── TerminalView.java       # Enhanced with AI overlays
│   └── TerminalSession.java    # AI command hooks
└── app/
    ├── TermuxActivity.java     # AI UI integration
    └── AISettingsActivity.java # AI configuration
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
- Claude API access (OAuth)

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

3. **Configure AI Integration**
   ```bash
   # Copy AI configuration template
   cp app/src/main/assets/ai-config.template.json app/src/main/assets/ai-config.json
   
   # Add your Claude OAuth configuration
   # Edit ai-config.json with your settings
   ```

4. **Build the APK**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (requires signing)
   ./gradlew assembleRelease
   ```

## 📦 Installation

### From APK
1. Download the latest APK from [Releases](https://github.com/your-username/termux-ai/releases)
2. Enable "Unknown Sources" in Android settings
3. Install the APK
4. Grant necessary permissions
5. Authenticate with Claude during first launch

### F-Droid (Planned)
- F-Droid repository submission planned for stable release

## ⚙️ Configuration

### AI Settings
Access AI settings through: **Settings > AI Integration**

- **Model Selection** - Choose Claude model (Haiku, Sonnet, Opus)
- **Suggestion Frequency** - Control how often AI suggests commands
- **Context Sharing** - Configure what context is shared with AI
- **Learning Mode** - Enable/disable pattern learning
- **Offline Mode** - Use cached suggestions when offline

### Privacy Controls
- **Data Retention** - Control how long AI interactions are stored
- **Analytics** - Opt-in/out of usage analytics
- **Command Filtering** - Exclude sensitive commands from AI
- **Local Processing** - Use on-device AI when available

## 🔒 Privacy & Security

### Data Handling
- **Local First** - AI context processed locally when possible
- **Encrypted Storage** - All AI data encrypted at rest
- **Secure Communication** - TLS encryption for API calls
- **No Sensitive Data** - Passwords and keys never sent to AI

### Permissions
- **Network** - For AI API communication
- **Storage** - For caching and configuration
- **Microphone** - Optional voice commands (can be disabled)
- **Camera** - Optional QR code scanning (can be disabled)

## 🎯 Roadmap

### Phase 1 (Current)
- [x] Basic AI integration
- [x] Command suggestions
- [x] Error analysis
- [ ] UI polish and optimization

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
- **Anthropic** - For Claude AI capabilities
- **Warp** - For inspiration on AI-enhanced terminals
- **Community** - For feedback and contributions

## 📞 Support

- **Issues** - [GitHub Issues](https://github.com/your-username/termux-ai/issues)
- **Discussions** - [GitHub Discussions](https://github.com/your-username/termux-ai/discussions)
- **Matrix** - [#termux-ai:matrix.org](https://matrix.to/#/#termux-ai:matrix.org)
- **Email** - support@termux-ai.org

---

*Transforming the mobile terminal experience with AI assistance*