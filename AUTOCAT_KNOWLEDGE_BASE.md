# AutoCat Knowledge Base

## Architecture: Termux+ Plugin System

### Overview
Termux+ v2.0.0 introduces a modular architecture where features are implemented as plugins. The core app acts as a host.

### Core Components
1.  **TermuxPlugin Interface:** Base contract for all plugins.
2.  **AIProvider Interface:** Specialization for AI capabilities (process terminal output, UI overlays).
3.  **PluginManager:** Singleton managing registration, lifecycle, and persistence of plugins.
4.  **EnhancedTerminalView:** A smart view that delegates AI logic to the active `AIProvider`.

### Plugin Lifecycle
- **Registration:** Plugins are registered via `PluginManager.registerPlugin()`.
- **Initialization:** `onInit(Context)` is called when enabled.
- **Execution:** Plugins react to events or terminal output.
- **Unload:** `onUnload()` is called when disabled or app terminates.

### AI Integration
- **Detection:** `AIProvider.processTerminalOutput()` scans text for triggers.
- **Feedback:** `AIProvider.AIListener` callbacks update the UI (overlays, progress bars).
- **Isolation:** Each tab can have its own AI state, though currently providers are often singleton-backed (legacy).

### Best Practices
- **Threading:** Use `Dispatchers.IO` for heavy work.
- **State:** Persist plugin configuration in SharedPreferences via `PluginManager` or internal logic.
- **Security:** Never expose API keys. Use `EncryptedPreferencesManager`.
