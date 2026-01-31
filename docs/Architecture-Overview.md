# Architecture Overview

This document provides an overview of the Termux AI app architecture, focusing on the AI integration and UX/UI enhancements.

## Project Structure

```
app/src/main/java/com/termux/
├── ai/                    # AI integration components
│   ├── AIClient.java     # Unified AI provider client (Claude, Gemini)
│   ├── ClaudeCodeIntegration.java # Claude-specific integration
│   ├── ClaudeCodeService.java # Background service for Claude operations
│   ├── ClaudeSetupActivity.java # Claude setup UI
│   ├── ContextEngine.java # Environment and project awareness
│   └── TermuxAIApplication.java # Global app initialization
├── app/                   # Main app activities
│   ├── TabbedTerminalActivity.java # Main activity with tab management
│   ├── TerminalFragment.java # Individual terminal instances
│   ├── TermuxPlusSettingsActivity.java # AI provider configuration
│   ├── ClaudeHelpDialog.java # Help documentation dialog
│   ├── ClaudeQuickActionsDialog.java # Quick actions for Claude
│   ├── ProjectInsightsDialog.java # Project analysis and insights
│   ├── QuickSettingsPanel.java # Sliding settings panel
│   └── MobileGesturesHelper.java # Mobile gesture detection
├── terminal/              # Enhanced terminal components
│   ├── EnhancedTerminalView.java # Terminal with AI overlays
│   ├── TerminalEmulator.java # Core terminal emulation
│   ├── TerminalSession.java # Terminal session management
│   └── TerminalSessionClient.java # Terminal session client
├── receivers/             # Broadcast receivers
├── ui/                    # UI components and helpers
└── view/                  # View components
```

## Core Components

### MobileGesturesHelper
Handles mobile-specific gestures for the terminal:
- Swipe detection for navigation
- Double tap for history
- Triple tap for voice input
- Long press for context menus
- Touch target optimization

### ContextEngine
Maintains awareness of the terminal environment:
- Current working directory and project structure
- Recent commands and their outcomes
- File system changes
- Git repository status
- Running processes
- Environment variables

### ClaudeCodeIntegration
Manages Claude Code CLI integration:
- Detection of Claude Code activation
- Parsing of Claude output for progress and file operations
- Visual enhancements during Claude operations
- Mobile-optimized Claude workflows

### QuickSettingsPanel
Provides quick access to common settings:
- AI model selection
- Token limit configuration
- Auto suggestion toggles
- Privacy controls
- Mobile-specific preferences

### ShakeDetector
Implements shake detection using device sensors:
- Accelerometer-based shake detection
- Proper lifecycle management
- Resource optimization
- Gesture-based terminal clearing

## AI Integration Architecture

### AIClient
Unified client for multiple AI providers:
- Claude API integration
- Gemini API integration
- Authentication management
- Real-time command analysis
- Error diagnostics
- Code generation

### ClaudeCodeService
Background service for Claude operations:
- Persistent Claude sessions
- Context management
- Notification handling
- Resource optimization

## Terminal Enhancement Architecture

### EnhancedTerminalView
Extended from TerminalView with AI capabilities:
- Claude Code detection and integration
- Gboard autocomplete support
- Visual indicators for AI operations
- Enhanced text selection for mobile
- Progress tracking for Claude operations
- File operation highlighting

### TabbedTerminalActivity
Main activity with tab management:
- Multiple terminal tabs with project-specific contexts
- Custom tab layouts with close buttons
- Gesture-based tab switching
- Claude status management per tab
- Mobile-optimized UI interactions

## Mobile-Specific Architecture

### Touch Target Optimization
- Minimum 48dp touch targets for accessibility
- Enhanced touch feedback
- Gesture-based navigation
- Mobile-first UI design

### Gesture Detection System
- Swipe gestures for navigation
- Double tap for quick actions
- Triple tap for voice input
- Long press for extended options
- Context-aware gesture handling

## Data Flow

1. **Terminal Input**: User enters command in terminal
2. **Gesture Detection**: MobileGesturesHelper detects swipe/tap gestures
3. **Context Analysis**: ContextEngine analyzes project and environment
4. **AI Processing**: AIClient sends command to appropriate AI provider
5. **Response Handling**: ClaudeCodeIntegration processes AI responses
6. **UI Updates**: EnhancedTerminalView updates visual indicators
7. **Feedback**: User receives visual and text feedback

## Security Considerations

- API keys stored securely with encryption
- Sensitive commands filtered before AI processing
- Local processing options for privacy
- Secure communication channels
- Minimal data collection