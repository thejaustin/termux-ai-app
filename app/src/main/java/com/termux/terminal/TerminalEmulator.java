package com.termux.terminal;

/**
 * STUB IMPLEMENTATION - TODO: Replace with real Termux terminal sources
 * 
 * Terminal emulator stub that provides minimal functionality
 * This implementation only includes methods actually used in the current codebase.
 */
public class TerminalEmulator {
    
    private Screen screen;
    private int cursorRow = 0;
    
    public TerminalEmulator() {
        this.screen = new Screen();
    }
    
    /**
     * Get the terminal screen
     * @return The terminal screen instance
     */
    public Screen getScreen() {
        return screen;
    }
    
    /**
     * Get the current cursor row position
     * @return The cursor row (0-based)
     */
    public int getCursorRow() {
        return cursorRow;
    }
    
    /**
     * STUB IMPLEMENTATION - TODO: Replace with real Termux terminal sources
     * 
     * Terminal screen stub that provides minimal text access functionality
     */
    public static class Screen {
        
        private int columns = 80; // Default terminal width
        
        /**
         * Get selected text from the terminal screen
         * @param startColumn Start column (0-based)
         * @param startRow Start row (0-based) 
         * @param endColumn End column (0-based)
         * @param endRow End row (0-based)
         * @return The selected text, or empty string as stub
         */
        public String getSelectedText(int startColumn, int startRow, int endColumn, int endRow) {
            // TODO: Replace with real implementation that extracts text from terminal buffer
            return ""; // Stub return
        }
        
        /**
         * Get the number of columns in the terminal
         * @return Number of columns
         */
        public int getColumns() {
            return columns;
        }
        
        /**
         * Set the number of columns (for testing/configuration)
         * @param columns Number of columns
         */
        public void setColumns(int columns) {
            this.columns = columns;
        }
    }
}