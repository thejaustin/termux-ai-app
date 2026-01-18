package com.termux.terminal;

/**
 * Terminal screen buffer with scrollback support.
 *
 * The buffer is a circular array of TerminalRows. It maintains:
 * - Screen rows (visible on screen)
 * - Scrollback rows (history above visible area)
 *
 * As new lines are added at the bottom, old lines scroll up into
 * the scrollback buffer.
 */
public class TerminalBuffer {
    private final TerminalRow[] mLines;
    private final int mColumns;
    private final int mScreenRows;
    private final int mTotalRows;

    // Points to the first row of the visible screen within mLines array
    private int mScreenFirstRow;

    // Number of rows currently in use (screen + scrollback)
    private int mActiveRows;

    /**
     * Create a new terminal buffer.
     *
     * @param columns Number of columns (width)
     * @param screenRows Number of visible rows (height)
     * @param scrollbackRows Number of scrollback rows (history)
     */
    public TerminalBuffer(int columns, int screenRows, int scrollbackRows) {
        this.mColumns = columns;
        this.mScreenRows = screenRows;
        this.mTotalRows = screenRows + scrollbackRows;
        this.mLines = new TerminalRow[mTotalRows];
        this.mScreenFirstRow = 0;
        this.mActiveRows = screenRows;

        // Initialize all rows
        for (int i = 0; i < mTotalRows; i++) {
            mLines[i] = new TerminalRow(columns);
        }
    }

    /**
     * Get character at screen position.
     *
     * @param column Column (0-based)
     * @param row Screen row (0-based, 0 = top of screen)
     * @return Character at position
     */
    public char getChar(int column, int row) {
        return getRow(row).getChar(column);
    }

    /**
     * Get style at screen position.
     *
     * @param column Column (0-based)
     * @param row Screen row (0-based)
     * @return Style encoding at position
     */
    public int getStyle(int column, int row) {
        return getRow(row).getStyle(column);
    }

    /**
     * Set character and style at screen position.
     *
     * @param column Column (0-based)
     * @param row Screen row (0-based)
     * @param ch Character to set
     * @param style Style encoding
     */
    public void setChar(int column, int row, char ch, int style) {
        getRow(row).setChar(column, ch, style);
    }

    /**
     * Get a row from the screen area.
     *
     * @param row Screen row (0-based, 0 = top of screen)
     * @return TerminalRow object
     */
    public TerminalRow getRow(int row) {
        if (row < 0 || row >= mScreenRows) {
            // Return empty row for invalid indices
            return new TerminalRow(mColumns);
        }
        int index = externalToInternalRow(row);
        return mLines[index];
    }

    /**
     * Get a row from scrollback (negative indices) or screen.
     *
     * @param row Row index (negative = scrollback, 0+ = screen)
     * @return TerminalRow object
     */
    public TerminalRow getRowShifted(int row) {
        int scrollbackRows = getScrollbackRows();
        if (row < -scrollbackRows) {
            return new TerminalRow(mColumns);
        }
        int screenRow = row + scrollbackRows;
        int index = externalToInternalRow(screenRow);
        return mLines[index];
    }

    /**
     * Scroll the screen down by one line.
     *
     * This adds a new blank line at the bottom and moves everything up,
     * pushing the top line into scrollback.
     */
    public void scrollDownOneLine() {
        // If we're at maximum capacity, we'll reuse the oldest row
        if (mActiveRows < mTotalRows) {
            mActiveRows++;
        }

        // Move screen start down (wrapping around circular buffer)
        mScreenFirstRow = (mScreenFirstRow + 1) % mTotalRows;

        // Clear the new bottom line
        int bottomIndex = externalToInternalRow(mScreenRows - 1);
        mLines[bottomIndex].clear();
    }

    /**
     * Clear the entire screen.
     */
    public void clearScreen() {
        for (int row = 0; row < mScreenRows; row++) {
            getRow(row).clear();
        }
    }

    /**
     * Clear from current position to end of screen.
     *
     * @param column Starting column
     * @param row Starting row
     */
    public void clearFromCursorToEndOfScreen(int column, int row) {
        // Clear from cursor to end of current row
        getRow(row).clearFrom(column);

        // Clear all rows below
        for (int r = row + 1; r < mScreenRows; r++) {
            getRow(r).clear();
        }
    }

    /**
     * Clear from start of screen to current position.
     *
     * @param column Ending column
     * @param row Ending row
     */
    public void clearFromStartOfScreenToCursor(int column, int row) {
        // Clear all rows above
        for (int r = 0; r < row; r++) {
            getRow(r).clear();
        }

        // Clear from start of current row to cursor
        getRow(row).clearTo(column);
    }

    /**
     * Get the number of columns.
     */
    public int getColumns() {
        return mColumns;
    }

    /**
     * Get the number of visible screen rows.
     */
    public int getScreenRows() {
        return mScreenRows;
    }

    /**
     * Get the number of rows (alias for getScreenRows for API consistency).
     */
    public int getRows() {
        return mScreenRows;
    }

    /**
     * Get the number of scrollback rows currently in use.
     */
    public int getScrollbackRows() {
        return Math.max(0, mActiveRows - mScreenRows);
    }

    /**
     * Get selected text from screen.
     *
     * @param startColumn Start column
     * @param startRow Start row
     * @param endColumn End column
     * @param endRow End row
     * @return Selected text as string
     */
    public String getSelectedText(int startColumn, int startRow, int endColumn, int endRow) {
        StringBuilder result = new StringBuilder();

        if (startRow == endRow) {
            // Single line selection
            TerminalRow row = getRow(startRow);
            for (int col = startColumn; col <= endColumn && col < mColumns; col++) {
                result.append(row.getChar(col));
            }
        } else {
            // Multi-line selection
            // First line
            TerminalRow firstRow = getRow(startRow);
            for (int col = startColumn; col < mColumns; col++) {
                result.append(firstRow.getChar(col));
            }
            result.append('\n');

            // Middle lines
            for (int row = startRow + 1; row < endRow; row++) {
                TerminalRow midRow = getRow(row);
                result.append(midRow.getText());
                result.append('\n');
            }

            // Last line
            TerminalRow lastRow = getRow(endRow);
            for (int col = 0; col <= endColumn && col < mColumns; col++) {
                result.append(lastRow.getChar(col));
            }
        }

        return result.toString();
    }

    /**
     * Resize the terminal buffer, preserving content where possible.
     *
     * This creates a new buffer with the new dimensions and copies content
     * from the old buffer. When columns change, content is either truncated
     * (if shrinking) or padded with spaces (if growing). When rows change,
     * content is preserved and scrollback is adjusted accordingly.
     *
     * @param newColumns New number of columns
     * @param newRows New number of screen rows
     * @return A new TerminalBuffer with the resized dimensions and copied content
     */
    public TerminalBuffer resize(int newColumns, int newRows) {
        if (newColumns == mColumns && newRows == mScreenRows) {
            return this; // No change needed
        }

        // Calculate new scrollback to maintain similar ratio
        int currentScrollback = mTotalRows - mScreenRows;
        int newScrollback = currentScrollback; // Keep same scrollback capacity

        // Create new buffer
        TerminalBuffer newBuffer = new TerminalBuffer(newColumns, newRows, newScrollback);

        // Copy content from old buffer to new buffer
        // Start from the oldest line in scrollback and work forward
        int oldScrollbackRows = getScrollbackRows();
        int copyRows = Math.min(mActiveRows, newBuffer.mTotalRows);

        for (int i = 0; i < copyRows; i++) {
            // Calculate source row (starting from scrollback if present)
            int srcRowOffset = i - oldScrollbackRows;
            TerminalRow srcRow = getRowShifted(srcRowOffset);

            // Calculate destination row in new buffer
            int dstRow = i;
            if (dstRow < newBuffer.mTotalRows) {
                TerminalRow dstRowObj = newBuffer.mLines[dstRow];

                // Copy character by character up to the minimum of old and new columns
                int colsToCopy = Math.min(mColumns, newColumns);
                for (int col = 0; col < colsToCopy; col++) {
                    dstRowObj.setChar(col, srcRow.getChar(col), srcRow.getStyle(col));
                }

                // Preserve line wrap flag
                dstRowObj.setLineWrap(srcRow.isLineWrap());
            }
        }

        // Update active rows in new buffer
        newBuffer.mActiveRows = Math.min(copyRows, newRows);
        if (oldScrollbackRows > 0) {
            // If we had scrollback, include it in active rows
            newBuffer.mActiveRows = Math.min(copyRows, newBuffer.mTotalRows);
        }

        return newBuffer;
    }

    /**
     * Copy content from another row into this buffer at specified position.
     * Used internally during resize operations.
     *
     * @param targetRow The row index in this buffer to copy to
     * @param sourceRow The source TerminalRow to copy from
     * @param columns Number of columns to copy
     */
    private void copyRowContent(int targetRow, TerminalRow sourceRow, int columns) {
        if (targetRow < 0 || targetRow >= mTotalRows) return;

        TerminalRow destRow = mLines[targetRow];
        int colsToCopy = Math.min(columns, Math.min(sourceRow.getColumns(), destRow.getColumns()));

        for (int col = 0; col < colsToCopy; col++) {
            destRow.setChar(col, sourceRow.getChar(col), sourceRow.getStyle(col));
        }
        destRow.setLineWrap(sourceRow.isLineWrap());
    }

    /**
     * Convert external row number to internal array index.
     *
     * @param externalRow Row number (0 = top of screen)
     * @return Index in mLines array
     */
    private int externalToInternalRow(int externalRow) {
        return (mScreenFirstRow + externalRow) % mTotalRows;
    }

    /**
     * Get total buffer capacity.
     */
    public int getTotalRows() {
        return mTotalRows;
    }

    /**
     * Get number of active rows (screen + scrollback in use).
     */
    public int getActiveRows() {
        return mActiveRows;
    }

    /**
     * Allocate a new blank row at the bottom of the screen.
     *
     * This is used when text needs to be added below the current bottom line.
     */
    public TerminalRow allocateRow() {
        scrollDownOneLine();
        return getRow(mScreenRows - 1);
    }
}
