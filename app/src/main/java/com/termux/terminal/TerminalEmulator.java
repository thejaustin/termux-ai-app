package com.termux.terminal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stub implementation of TerminalEmulator for compilation.
 * 
 * TODO: Replace with actual Termux TerminalEmulator implementation.
 * This is a minimal no-op stub to allow compilation of EnhancedTerminalView.
 */
public class TerminalEmulator {

    private final Screen screen = new Screen();

    /**
     * Get the current cursor row position.
     * 
     * TODO: Return actual cursor row from terminal emulator
     * @return 0 in stub implementation
     */
    public int getCursorRow() {
        // TODO: Return actual cursor row
        return 0;
    }

    /**
     * Get the terminal screen.
     * 
     * TODO: Return actual terminal screen
     * @return stub screen implementation
     */
    @NonNull
    public Screen getScreen() {
        // TODO: Return actual terminal screen
        return screen;
    }

    /**
     * Stub implementation of terminal screen for compilation.
     * 
     * TODO: Replace with actual Termux Screen implementation.
     */
    public static class Screen {

        /**
         * Get selected text from the screen.
         * 
         * TODO: Implement actual text selection from terminal screen
         * @param startColumn start column
         * @param startRow start row  
         * @param endColumn end column
         * @param endRow end row
         * @return null in stub implementation
         */
        @Nullable
        public String getSelectedText(int startColumn, int startRow, int endColumn, int endRow) {
            // TODO: Return actual selected text from terminal screen
            return null;
        }

        /**
         * Get the number of columns in the terminal.
         * 
         * TODO: Return actual column count from terminal screen
         * @return 80 in stub implementation (common default)
         */
        public int getColumns() {
            // TODO: Return actual column count
            return 80;
        }
    }
}