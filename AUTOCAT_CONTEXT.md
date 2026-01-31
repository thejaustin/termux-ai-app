# AutoCat Context

## Current Project Status
- **Project Name:** Termux+ (formerly Termux AI)
- **Version:** 2.0.0
- **Architecture:** Modular Plugin System
- **Key Modules:**
    - `app`: Main application, plugin host.
    - `terminal-emulator`: Core terminal logic.
- **Plugins:**
    - `ClaudePlugin` (com.termux.plus.claude): Official Claude Code integration.
    - `AutoSavePlugin` (com.termux.plus.autosave): Example utility.

## Development Goals
- Expand plugin ecosystem.
- Support dynamic loading of 3rd party plugins (DEX/APK).
- Maintain robust AI integration (Claude, Gemini, etc.).

## Recent Changes
- Rebranded to Termux+.
- Refactored `ClaudeCodeIntegration` into `ClaudePlugin` implementing `AIProvider`.
- Added `PluginManager` and Plugin Settings UI.
- Decoupled `EnhancedTerminalView` from specific AI implementations.
