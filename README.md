# Termux+

A fork of [Termux](https://github.com/termux/termux-app) with AI integration, tabbed sessions, and Material You theming.

[![GitHub release](https://img.shields.io/github/v/release/thejaustin/termux-ai-app)](https://github.com/thejaustin/termux-ai-app/releases)
[![License](https://img.shields.io/badge/license-GPLv3-blue)](LICENSE)

---

## What is Termux+?

Termux+ is an Android terminal emulator and Linux environment built on top of the official Termux terminal core. It adds an AI-assist layer, tabbed terminal sessions, and a plugin system while keeping the same reliable PTY backend that Termux uses.

It is aimed at developers who want a mobile terminal that can talk to AI models (Claude, Gemini) without leaving the terminal.

---

## Features

- **Full terminal emulation** — VT100/xterm compatible, true 24-bit color, Unicode and wide-character support
- **Tabbed sessions** — open multiple independent shell sessions side by side
- **AI integration** — Claude and Gemini plugins; get command suggestions and error analysis inline
- **Material You theming** — dynamic colors that follow your wallpaper (Android 12+)
- **Plugin system** — enable or disable features at runtime; swap AI backends without rebuilding
- **Gesture controls** — pinch to zoom, fling to scroll, long-press for text selection
- **Encrypted key storage** — API keys are stored with AES-256-GCM, never in plaintext

---

## Installation

> **Note:** Termux+ is in active development. It requires Termux bootstrap packages to be installed at the standard paths.

### From releases

Download the latest APK from the [Releases](https://github.com/thejaustin/termux-ai-app/releases) page and sideload it.

### Build from source

**Requirements:**
- Android Studio Ladybug or newer
- Android SDK 34+
- Android NDK (side-by-side) — required for C++ PTY compilation
- CMake 3.22.1+

```bash
git clone https://github.com/thejaustin/termux-ai-app.git
cd termux-ai-app
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## AI Setup

1. Open **Settings → Plus Features**
2. Enable the AI provider you want (Claude or Gemini)
3. Enter your API key — it is stored encrypted on-device
4. Start a terminal session; the AI overlay activates automatically

API keys are sent only to the provider you configure. They are never stored in plaintext or included in backups.

---

## Project structure

```
app/                    Android app module (activities, fragments, AI/plugin layer)
terminal-emulator/      Terminal core library (VT100 emulator, PTY JNI, session)
  src/main/cpp/         Native C++ PTY code (CMake)
  src/main/java/        Java terminal emulator classes
```

The terminal core (`terminal-emulator/`) tracks upstream [termux/termux-app](https://github.com/termux/termux-app). Termux+ additions live in `app/`.

---

## Contributing

Pull requests are welcome. Please open an issue first for significant changes.

- Bug reports: [Issues](https://github.com/thejaustin/termux-ai-app/issues)
- The terminal core should stay compatible with upstream Termux where possible

---

## License

GPLv3 — see [LICENSE](LICENSE). Termux+ is a fork of Termux, which is also GPLv3.
