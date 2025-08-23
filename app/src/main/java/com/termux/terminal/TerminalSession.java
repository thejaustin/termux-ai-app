package com.termux.terminal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stub implementation of TerminalSession for compilation.
 * 
 * TODO: Replace with actual Termux TerminalSession implementation.
 * This is a minimal no-op stub to allow compilation of EnhancedTerminalView and TerminalFragment.
 */
public class TerminalSession {

    /**
     * Get the terminal emulator for this session.
     * 
     * TODO: Return actual terminal emulator instead of null
     * @return null in stub implementation
     */
    @Nullable
    public TerminalEmulator getEmulator() {
        // TODO: Return actual terminal emulator
        return null;
    }

    /**
     * Finish the terminal session if it's still running.
     * 
     * TODO: Implement actual session termination
     */
    public void finishIfRunning() {
        // TODO: Terminate actual terminal session
        // No-op stub implementation
    }

    /**
     * Write text to the terminal session.
     * 
     * TODO: Implement actual text writing to terminal
     * @param text the text to write
     */
    public void write(@NonNull String text) {
        // TODO: Write text to actual terminal
        // No-op stub implementation
    }
}