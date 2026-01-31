# AutoCat Context

## Current Project Status
- **Project Name:** Termux+ (formerly Termux AI)
- **Version:** 2.0.0
- **Architecture:** Modular Plugin System (Host-Plugin Model)
- **Key Modules:**
    - `app`: Main application, plugin host, UI.
    - `terminal-emulator`: Core terminal logic, native PTY layer.
- **Plugins:**
    - `ClaudePlugin` (com.termux.plus.claude): Official Claude Code integration.
    - `AutoSavePlugin` (com.termux.plus.autosave): Example terminal extension.

## Development Goals
- [DONE] Define modular plugin architecture.
- [DONE] Implement Plugin Manager and Settings UI.
- [DONE] Rebrand to Termux+.
- [TODO] Support dynamic loading of 3rd party plugins via `DexClassLoader`.
- [TODO] Implement on-device AI model support.

## Recent Changes
- Fixed syntax errors in `TabbedTerminalActivity` and `PluginSettingsActivity`.
- Renamed `TermuxAISettingsActivity` to `TermuxPlusSettingsActivity`.
- Moved `TermuxPlusApplication` to `com.termux.plus`.
- Updated `README.md` and `strings.xml` for full Termux+ v2.0.0 branding.