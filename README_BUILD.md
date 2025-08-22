# Termux AI - Build & Installation Guide

A complete fork of Termux with native Claude Code integration and mobile-optimized AI features.

## 🚀 Quick Start

### Pre-built APK Installation
1. Download the latest APK from releases
2. Enable "Unknown Sources" in Android Settings
3. Install and grant permissions
4. Launch and enjoy AI-enhanced terminal!

### Building from Source

#### Prerequisites
- Termux app installed
- Android device with 4GB+ RAM
- 2GB+ free storage space

#### Build Steps

```bash
# 1. Install build dependencies in Termux
pkg update && pkg upgrade
pkg install git openjdk-17 gradle android-tools

# 2. Clone the repository  
git clone https://github.com/your-username/termux-ai-app.git
cd termux-ai-app

# 3. Run the build script
chmod +x build.sh
./build.sh

# 4. Install the generated APK
# APK will be copied to: ~/termux-ai-debug.apk
```

## 📋 Detailed Build Instructions

### Setting up Android SDK in Termux

```bash
# Download Android command line tools
cd ~
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip

# Create SDK directory structure
mkdir -p android-sdk/cmdline-tools
mv cmdline-tools android-sdk/cmdline-tools/latest

# Set environment variables
echo 'export ANDROID_SDK_ROOT=$HOME/android-sdk' >> ~/.bashrc
echo 'export ANDROID_HOME=$ANDROID_SDK_ROOT' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin' >> ~/.bashrc
source ~/.bashrc

# Accept SDK licenses
yes | sdkmanager --licenses

# Install required SDK components
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### Manual Build Process

If the build script fails, you can build manually:

```bash
# 1. Set up environment
export JAVA_HOME=$PREFIX/opt/openjdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
export ANDROID_HOME=$ANDROID_SDK_ROOT

# 2. Navigate to project
cd /path/to/termux-ai-app

# 3. Build with Gradle
./gradlew assembleDebug

# 4. Find APK
find . -name "*.apk" -type f
```

## 🎯 Key Features Built

### ✅ Completed Features

#### Core Terminal Enhancements
- **Enhanced TerminalView** with Claude Code detection
- **Gboard autocomplete** integration for mobile typing
- **Tabbed interface** supporting up to 8 concurrent sessions
- **Project type detection** (Node.js, Python, Rust, Go, Java)
- **Smart tab icons** with Claude activity indicators

#### Claude Code Integration
- **Real-time detection** when Claude Code CLI starts
- **Progress tracking** with visual indicators
- **File operation highlighting** for newly created/modified files
- **Token usage monitoring** and display
- **Error detection** with intelligent parsing

#### Mobile-Optimized Controls
- **Gesture navigation** - Swipe down to stop, double-tap for history
- **Touch-friendly file picker** for @-tagging files
- **Enhanced text selection** for mobile devices
- **Voice input support** for Claude commands
- **Quick action overlays** for common operations

#### UI/UX Improvements
- **Dark theme optimized** for terminal use
- **Visual progress indicators** for AI operations
- **File change highlights** with fade animations
- **Status overlays** showing Claude operation state
- **Mobile-first design** with proper touch targets

### 🏗 Architecture Highlights

#### Android App Structure
```
termux-ai-app/
├── app/src/main/java/com/termux/
│   ├── ai/                     # AI integration components
│   │   ├── AIClient.java       # Claude API client
│   │   ├── ClaudeCodeIntegration.java
│   │   └── TermuxAIApplication.java
│   ├── app/                    # Main app activities
│   │   ├── TabbedTerminalActivity.java
│   │   └── TerminalFragment.java
│   └── terminal/               # Enhanced terminal
│       └── EnhancedTerminalView.java
├── app/src/main/res/           # Resources and layouts
└── app/build.gradle            # Build configuration
```

#### Key Classes
- **`TabbedTerminalActivity`** - Main activity with tab management
- **`EnhancedTerminalView`** - Terminal with AI integration
- **`ClaudeCodeIntegration`** - Core AI detection engine
- **`TerminalFragment`** - Individual terminal instances
- **`TermuxAIApplication`** - Global app initialization

## 🔧 Configuration

### App Settings
Access via Settings menu in app:
- **Claude Model Selection** - Choose between Haiku, Sonnet, Opus
- **Gboard Integration** - Enable/disable autocomplete
- **Token Limits** - Configure max tokens per conversation
- **Auto Suggestions** - Control AI suggestion frequency
- **Gesture Controls** - Customize touch gestures

### Claude Code Setup
1. Install Claude Code CLI in terminal:
   ```bash
   npm install -g @anthropic/claude-code
   ```

2. Authenticate with your Claude account:
   ```bash
   claude code
   # Follow OAuth authentication flow
   ```

3. Start using enhanced features immediately!

## 📱 Usage Guide

### Getting Started
1. **Launch Termux AI** - Open the app
2. **Create tabs** - Tap + to create project-specific terminals
3. **Start Claude Code** - Type `claude code` in any tab
4. **Use gestures** - Swipe down to stop, double-tap for history
5. **Pick files** - Tap 📁 button for easy @-tagging

### Advanced Features
- **Multi-project workflow** - Different tabs for different projects
- **Voice commands** - Tap 🎤 for voice input to Claude
- **Quick templates** - Tap ⚡ for common command templates
- **Project insights** - Tap ℹ️ for AI-powered project analysis

## 🐛 Troubleshooting

### Common Build Issues

#### Java/Gradle Issues
```bash
# Set correct Java version
export JAVA_HOME=$PREFIX/opt/openjdk

# Clear Gradle cache
rm -rf ~/.gradle/caches

# Retry build
./gradlew clean assembleDebug
```

#### SDK Issues
```bash
# Verify SDK installation
ls $ANDROID_SDK_ROOT/platforms

# Reinstall platform tools
sdkmanager --update
sdkmanager "platform-tools" "platforms;android-34"
```

#### Permission Issues
```bash
# Grant storage permissions
termux-setup-storage

# Fix file permissions
chmod -R 755 /path/to/termux-ai-app
```

### Runtime Issues

#### Claude Code Not Detected
1. Ensure Claude Code CLI is installed: `npm list -g @anthropic/claude-code`
2. Check authentication: `claude code --help`
3. Verify network connection for API calls

#### Gboard Not Working
1. Enable in app settings: Settings > Gboard Integration
2. Restart app after enabling
3. Check Android keyboard settings

#### Tab Issues
1. Restart app if tabs don't respond
2. Clear app data if persistent issues
3. Check available memory (app needs 2GB+ RAM)

## 📖 Development

### Contributing
1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request

### Building Variants
```bash
# Debug build (includes logging)
./gradlew assembleDebug

# Release build (optimized)
./gradlew assembleRelease

# Install directly to device
./gradlew installDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## 📄 License

GPLv3 - Same as original Termux project

## 🙏 Credits

- **Termux Team** - Original terminal app foundation
- **Anthropic** - Claude AI integration
- **Android Community** - Development tools and libraries

---

**Ready to transform your mobile development workflow with AI-enhanced terminal experience!** 🚀