package com.termux.terminal;

import java.util.Arrays;

/**
 * Represents a single row/line in the terminal screen buffer.
 *
 * Each row stores characters and their associated styles (colors, attributes).
 * Rows are designed to be efficient and reusable.
 */
public class TerminalRow {
    private char[] mText;
    private int[] mStyle;
    private final int mColumns;
    private boolean mLineWrap;

    /**
     * Create a new terminal row.
     *
     * @param columns Number of columns in the row
     */
    public TerminalRow(int columns) {
        this.mColumns = columns;
        this.mText = new char[columns];
        this.mStyle = new int[columns];
        clear();
    }

    /**
     * Get character at column.
     *
     * @param column Column index (0-based)
     * @return Character at position, or space if invalid column
     */
    public char getChar(int column) {
        if (column < 0 || column >= mColumns) {
            return ' ';
        }
        return mText[column];
    }

    /**
     * Get style at column.
     *
     * @param column Column index (0-based)
     * @return Style encoding at position
     */
    public int getStyle(int column) {
        if (column < 0 || column >= mColumns) {
            return 0;
        }
        return mStyle[column];
    }

    /**
     * Set character and style at column.
     *
     * @param column Column index (0-based)
     * @param ch Character to set
     * @param style Style encoding
     */
    public void setChar(int column, char ch, int style) {
        if (column >= 0 && column < mColumns) {
            mText[column] = ch;
            mStyle[column] = style;
        }
    }

    /**
     * Clear the entire row to spaces with default style.
     */
    public void clear() {
        Arrays.fill(mText, ' ');
        Arrays.fill(mStyle, 0);
        mLineWrap = false;
    }

    /**
     * Clear from column to end of row.
     *
     * @param column Starting column (0-based)
     */
    public void clearFrom(int column) {
        if (column >= 0 && column < mColumns) {
            Arrays.fill(mText, column, mColumns, ' ');
            Arrays.fill(mStyle, column, mColumns, 0);
        }
    }

    /**
     * Clear from start of row to column (inclusive).
     *
     * @param column Ending column (0-based)
     */
    public void clearTo(int column) {
        if (column >= 0 && column < mColumns) {
            Arrays.fill(mText, 0, column + 1, ' ');
            Arrays.fill(mStyle, 0, column + 1, 0);
        }
    }

    /**
     * Get the number of columns in this row.
     */
    public int getColumns() {
        return mColumns;
    }

    /**
     * Check if this line has a line wrap flag.
     *
     * A wrapped line indicates the text continued from the previous line
     * because it exceeded the screen width.
     */
    public boolean isLineWrap() {
        return mLineWrap;
    }

    /**
     * Set the line wrap flag.
     */
    public void setLineWrap(boolean lineWrap) {
        this.mLineWrap = lineWrap;
    }

    /**
     * Create a copy of this row.
     *
     * @return New TerminalRow with copied data
     */
    public TerminalRow copy() {
        TerminalRow copy = new TerminalRow(mColumns);
        System.arraycopy(mText, 0, copy.mText, 0, mColumns);
        System.arraycopy(mStyle, 0, copy.mStyle, 0, mColumns);
        copy.mLineWrap = this.mLineWrap;
        return copy;
    }

    /**
     * Get text content as String.
     *
     * @return String representation of row, trimming trailing spaces
     */
    public String getText() {
        int end = mColumns;
        // Trim trailing spaces
        while (end > 0 && mText[end - 1] == ' ') {
            end--;
        }
        return new String(mText, 0, end);
    }

    /**
     * Get text content without trimming.
     *
     * @return Full string with all columns
     */
    public String getFullText() {
        return new String(mText);
    }

    /**
     * Find the last non-space character position.
     *
     * @return Column index of last non-space, or -1 if row is empty
     */
    public int findLastNonSpace() {
        for (int i = mColumns - 1; i >= 0; i--) {
            if (mText[i] != ' ') {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return getText();
    }
}
