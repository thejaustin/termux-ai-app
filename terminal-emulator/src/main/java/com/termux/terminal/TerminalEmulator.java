package com.termux.terminal;

/**
 * Real terminal emulator implementation.
 *
 * Coordinates the terminal buffer, ANSI parser, and cursor management.
 * Processes terminal output and maintains terminal state.
 *
 * This replaces the placeholder implementation.
 */
public class TerminalEmulator {
    private static final String TAG = "TerminalEmulator";

    private TerminalBuffer mBuffer;
    private TerminalOutput mParser;
    private int mColumns;
    private int mRows;

    /**
     * Create a terminal emulator.
     *
     * @param columns Number of columns (width)
     * @param rows Number of rows (height)
     * @param session Optional session for callbacks (can be null)
     */
    public TerminalEmulator(int columns, int rows, Object session) {
        this.mColumns = columns;
        this.mRows = rows;
        this.mBuffer = new TerminalBuffer(columns, rows, 1000); // 1000 lines scrollback
        this.mParser = new TerminalOutput(mBuffer);
    }

    /**
     * Process terminal output bytes.
     *
     * @param bytes Byte array containing terminal output
     * @param length Number of bytes to process
     */
    public void append(byte[] bytes, int length) {
        mParser.process(bytes, length);
    }

    /**
     * Resize the terminal.
     *
     * @param columns New number of columns
     * @param rows New number of rows
     */
    public void resize(int columns, int rows) {
        if (columns == mColumns && rows == mRows) {
            return;
        }

        this.mColumns = columns;
        this.mRows = rows;

        // Use TerminalBuffer's resize method to preserve content
        this.mBuffer = mBuffer.resize(columns, rows);
        this.mParser.setBuffer(mBuffer);
    }

    /**
     * Get the terminal screen buffer.
     *
     * @return TerminalBuffer containing screen content
     */
    public TerminalBuffer getScreen() {
        return mBuffer;
    }

    /**
     * Get current cursor row.
     *
     * @return Cursor row (0-based)
     */
    public int getCursorRow() {
        return mParser.getCursorRow();
    }

    /**
     * Get current cursor column.
     *
     * @return Cursor column (0-based)
     */
    public int getCursorCol() {
        return mParser.getCursorCol();
    }

    /**
     * Get number of columns.
     */
    public int getColumns() {
        return mColumns;
    }

    /**
     * Get number of rows.
     */
    public int getRows() {
        return mRows;
    }

    /**
     * Reset the terminal to initial state.
     */
    public void reset() {
        mBuffer = new TerminalBuffer(mColumns, mRows, 1000);
        mParser = new TerminalOutput(mBuffer);
    }

    /**
     * Get the screen buffer (alias for getScreen).
     *
     * Inner class for compatibility with existing code.
     */
    public static class Screen {
        private final TerminalBuffer mBuffer;

        public Screen(TerminalBuffer buffer) {
            this.mBuffer = buffer;
        }

        public char getChar(int col, int row) {
            return mBuffer.getChar(col, row);
        }

        public int getStyle(int col, int row) {
            return mBuffer.getStyle(col, row);
        }

        public String getSelectedText(int startCol, int startRow, int endCol, int endRow) {
            return mBuffer.getSelectedText(startCol, startRow, endCol, endRow);
        }

        public int getColumns() {
            return mBuffer.getColumns();
        }

        public int getRows() {
            return mBuffer.getScreenRows();
        }
    }
}
