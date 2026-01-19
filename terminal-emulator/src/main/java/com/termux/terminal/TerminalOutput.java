package com.termux.terminal;

import android.util.Log;

/**
 * ANSI/VT100 escape sequence parser and processor.
 *
 * Processes terminal output byte stream and applies changes to the terminal buffer.
 * Handles escape sequences for cursor movement, colors, text attributes, and more.
 *
 * This is a simplified implementation supporting common sequences.
 * Full VT100/xterm support would require significantly more code.
 */
public class TerminalOutput {
    private static final String TAG = "TerminalOutput";

    // Parser states
    private static final int STATE_NORMAL = 0;
    private static final int STATE_ESCAPE = 1;
    private static final int STATE_CSI = 2;
    private static final int STATE_OSC = 3;

    // Special characters
    // Special characters
    private static final byte ESC = 27;
    private static final byte BEL = 7;
    private static final byte BS = 8;
    private static final byte HT = 9;
    private static final byte LF = 10;
    private static final byte CR = 13;

    private TerminalBuffer mBuffer;
    private int mCursorRow = 0;
    private int mCursorCol = 0;

    // Current text style
    private int mForeColor = 7; // White
    private int mBackColor = 0; // Black
    private int mEffect = 0;

    // Parser state
    private int mParseState = STATE_NORMAL;
    private final int[] mCsiParams = new int[16];
    private int mCsiParamCount = 0;
    private final StringBuilder mOscBuffer = new StringBuilder();

    public TerminalOutput(TerminalBuffer buffer) {
        this.mBuffer = buffer;
    }

    public void setBuffer(TerminalBuffer buffer) {
        this.mBuffer = buffer;
    }

    /**
     * Process output bytes and update terminal buffer.
     *
     * @param bytes Byte array to process
     * @param length Number of bytes to process
     */
    public void process(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            processByte(bytes[i] & 0xFF);
        }
    }

    private void processByte(int b) {
        switch (mParseState) {
            case STATE_NORMAL:
                processNormalByte(b);
                break;

            case STATE_ESCAPE:
                processEscapeByte(b);
                break;

            case STATE_CSI:
                processCsiByte(b);
                break;

            case STATE_OSC:
                processOscByte(b);
                break;
        }
    }

    private void processNormalByte(int b) {
        if (b == ESC) {
            mParseState = STATE_ESCAPE;
        } else if (b == CR) {
            mCursorCol = 0;
        } else if (b == LF) {
            linefeed();
        } else if (b == BS) {
            if (mCursorCol > 0) {
                mCursorCol--;
            }
        } else if (b == HT) {
            // Tab - move to next multiple of 8
            mCursorCol = (mCursorCol + 8) & ~7;
            if (mCursorCol >= mBuffer.getColumns()) {
                mCursorCol = mBuffer.getColumns() - 1;
            }
        } else if (b == BEL) {
            // Bell - ignore for now
        } else if (b >= 32) {
            // Printable character
            emitCharacter((char) b);
        }
        // Ignore other control characters
    }

    private void processEscapeByte(int b) {
        if (b == '[') {
            // CSI sequence
            mParseState = STATE_CSI;
            mCsiParamCount = 0;
            for (int i = 0; i < mCsiParams.length; i++) {
                mCsiParams[i] = 0;
            }
        } else if (b == ']') {
            // OSC sequence
            mParseState = STATE_OSC;
            mOscBuffer.setLength(0);
        } else {
            // Other escape sequences (simplified handling)
            mParseState = STATE_NORMAL;
        }
    }

    private void processCsiByte(int b) {
        if (b >= '0' && b <= '9') {
            // Digit - accumulate parameter
            if (mCsiParamCount < mCsiParams.length) {
                mCsiParams[mCsiParamCount] = mCsiParams[mCsiParamCount] * 10 + (b - '0');
            }
        } else if (b == ';') {
            // Parameter separator
            mCsiParamCount++;
        } else if (b >= '@' && b <= '~') {
            // Final byte - execute command
            if (mCsiParamCount == 0 || mCsiParams[0] != 0) {
                mCsiParamCount++;
            }
            handleCsi(b);
            mParseState = STATE_NORMAL;
        } else {
            // Invalid - return to normal
            mParseState = STATE_NORMAL;
        }
    }

    private void processOscByte(int b) {
        if (b == BEL || b == ESC) {
            // End of OSC sequence
            handleOsc(mOscBuffer.toString());
            mParseState = STATE_NORMAL;
        } else {
            mOscBuffer.append((char) b);
        }
    }

    private void handleCsi(int finalByte) {
        switch (finalByte) {
            case 'A': // CUU - Cursor Up
                moveCursorRelative(0, -getCsiParam(0, 1));
                break;

            case 'B': // CUD - Cursor Down
                moveCursorRelative(0, getCsiParam(0, 1));
                break;

            case 'C': // CUF - Cursor Forward
                moveCursorRelative(getCsiParam(0, 1), 0);
                break;

            case 'D': // CUB - Cursor Backward
                moveCursorRelative(-getCsiParam(0, 1), 0);
                break;

            case 'H': // CUP - Cursor Position
            case 'f': // HVP - Horizontal and Vertical Position
                setCursorPosition(getCsiParam(1, 1) - 1, getCsiParam(0, 1) - 1);
                break;

            case 'J': // ED - Erase in Display
                eraseDisplay(getCsiParam(0, 0));
                break;

            case 'K': // EL - Erase in Line
                eraseLine(getCsiParam(0, 0));
                break;

            case 'm': // SGR - Select Graphic Rendition
                handleSgr();
                break;

            case 'r': // DECSTBM - Set Scrolling Region (ignore for now)
                break;

            case 'h': // SM - Set Mode (simplified)
                break;

            case 'l': // RM - Reset Mode (simplified)
                break;

            case 'G': // CHA - Cursor Horizontal Absolute
                mCursorCol = Math.max(0, Math.min(getCsiParam(0, 1) - 1, mBuffer.getColumns() - 1));
                break;

            case 'd': // VPA - Vertical Position Absolute
                mCursorRow = Math.max(0, Math.min(getCsiParam(0, 1) - 1, mBuffer.getScreenRows() - 1));
                break;

            default:
                Log.d(TAG, "Unhandled CSI sequence: " + (char) finalByte);
                break;
        }
    }

    private void handleSgr() {
        // SGR - Select Graphic Rendition (colors and text attributes)
        if (mCsiParamCount == 0) {
            // Reset to default
            mForeColor = 7;
            mBackColor = 0;
            mEffect = 0;
            return;
        }

        for (int i = 0; i < mCsiParamCount; i++) {
            int param = mCsiParams[i];

            if (param == 0) {
                // Reset
                mForeColor = 7;
                mBackColor = 0;
                mEffect = 0;
            } else if (param == 1) {
                // Bold
                mEffect = TextStyle.setBold(getCurrentStyle(), true);
                mEffect = TextStyle.decodeEffect(mEffect);
            } else if (param == 4) {
                // Underline
                mEffect = TextStyle.setUnderline(getCurrentStyle(), true);
                mEffect = TextStyle.decodeEffect(mEffect);
            } else if (param == 7) {
                // Inverse
                mEffect = TextStyle.setInverse(getCurrentStyle(), true);
                mEffect = TextStyle.decodeEffect(mEffect);
            } else if (param == 22) {
                // Normal intensity (not bold)
                mEffect = TextStyle.setBold(getCurrentStyle(), false);
                mEffect = TextStyle.decodeEffect(mEffect);
            } else if (param == 24) {
                // Not underlined
                mEffect = TextStyle.setUnderline(getCurrentStyle(), false);
                mEffect = TextStyle.decodeEffect(mEffect);
            } else if (param == 27) {
                // Not inverse
                mEffect = TextStyle.setInverse(getCurrentStyle(), false);
                mEffect = TextStyle.decodeEffect(mEffect);
            } else if (param >= 30 && param <= 37) {
                // Foreground color (standard)
                mForeColor = param - 30;
            } else if (param == 38) {
                // Extended foreground color (256-color or RGB)
                i = handleExtendedColor(i, true);
            } else if (param == 39) {
                // Default foreground color
                mForeColor = 7;
            } else if (param >= 40 && param <= 47) {
                // Background color (standard)
                mBackColor = param - 40;
            } else if (param == 48) {
                // Extended background color
                i = handleExtendedColor(i, false);
            } else if (param == 49) {
                // Default background color
                mBackColor = 0;
            } else if (param >= 90 && param <= 97) {
                // Bright foreground color
                mForeColor = param - 90 + 8;
            } else if (param >= 100 && param <= 107) {
                // Bright background color
                mBackColor = param - 100 + 8;
            }
        }
    }

    private int handleExtendedColor(int paramIndex, boolean foreground) {
        // Handle 256-color or RGB color codes
        // Format: 38;5;N (256-color) or 38;2;R;G;B (RGB)
        if (paramIndex + 1 < mCsiParamCount) {
            int colorType = mCsiParams[paramIndex + 1];
            if (colorType == 5 && paramIndex + 2 < mCsiParamCount) {
                // 256-color
                int colorIndex = mCsiParams[paramIndex + 2];
                if (foreground) {
                    mForeColor = colorIndex;
                } else {
                    mBackColor = colorIndex;
                }
                return paramIndex + 2;
            } else if (colorType == 2 && paramIndex + 4 < mCsiParamCount) {
                // RGB color (convert to 256-color approximation)
                int r = mCsiParams[paramIndex + 2];
                int g = mCsiParams[paramIndex + 3];
                int b = mCsiParams[paramIndex + 4];
                int colorIndex = rgbTo256Color(r, g, b);
                if (foreground) {
                    mForeColor = colorIndex;
                } else {
                    mBackColor = colorIndex;
                }
                return paramIndex + 4;
            }
        }
        return paramIndex;
    }

    private int rgbTo256Color(int r, int g, int b) {
        // Convert RGB to 256-color palette
        // Use 6x6x6 color cube (colors 16-231)
        r = r * 5 / 255;
        g = g * 5 / 255;
        b = b * 5 / 255;
        return 16 + (r * 36) + (g * 6) + b;
    }

    private void handleOsc(String command) {
        // OSC sequences (simplified handling)
        // Format: number;text
        // Common uses: window title, colors, etc.
        // For now, we'll ignore these
        Log.d(TAG, "OSC command: " + command);
    }

    private void emitCharacter(char ch) {
        // Ensure cursor is within bounds
        if (mCursorRow >= mBuffer.getScreenRows()) {
            mCursorRow = mBuffer.getScreenRows() - 1;
        }

        // Write character
        int style = getCurrentStyle();
        mBuffer.setChar(mCursorCol, mCursorRow, ch, style);

        // Advance cursor
        mCursorCol++;

        // Wrap to next line if needed
        if (mCursorCol >= mBuffer.getColumns()) {
            mCursorCol = 0;
            linefeed();
        }
    }

    private void linefeed() {
        mCursorRow++;
        if (mCursorRow >= mBuffer.getScreenRows()) {
            // Scroll up
            mBuffer.scrollDownOneLine();
            mCursorRow = mBuffer.getScreenRows() - 1;
        }
    }

    private void moveCursorRelative(int dx, int dy) {
        mCursorCol = Math.max(0, Math.min(mCursorCol + dx, mBuffer.getColumns() - 1));
        mCursorRow = Math.max(0, Math.min(mCursorRow + dy, mBuffer.getScreenRows() - 1));
    }

    private void setCursorPosition(int col, int row) {
        mCursorCol = Math.max(0, Math.min(col, mBuffer.getColumns() - 1));
        mCursorRow = Math.max(0, Math.min(row, mBuffer.getScreenRows() - 1));
    }

    private void eraseDisplay(int mode) {
        switch (mode) {
            case 0: // Erase from cursor to end of screen
                mBuffer.clearFromCursorToEndOfScreen(mCursorCol, mCursorRow);
                break;
            case 1: // Erase from start of screen to cursor
                mBuffer.clearFromStartOfScreenToCursor(mCursorCol, mCursorRow);
                break;
            case 2: // Erase entire screen
            case 3: // Erase saved lines (scrollback) - we'll treat same as 2
                mBuffer.clearScreen();
                break;
        }
    }

    private void eraseLine(int mode) {
        TerminalRow row = mBuffer.getRow(mCursorRow);
        switch (mode) {
            case 0: // Erase from cursor to end of line
                row.clearFrom(mCursorCol);
                break;
            case 1: // Erase from start of line to cursor
                row.clearTo(mCursorCol);
                break;
            case 2: // Erase entire line
                row.clear();
                break;
        }
    }

    private int getCsiParam(int index, int defaultValue) {
        if (index >= 0 && index < mCsiParamCount) {
            int value = mCsiParams[index];
            return value == 0 ? defaultValue : value;
        }
        return defaultValue;
    }

    private int getCurrentStyle() {
        return TextStyle.encode(mForeColor, mBackColor, mEffect);
    }

    // Getters for cursor position
    public int getCursorRow() {
        return mCursorRow;
    }

    public int getCursorCol() {
        return mCursorCol;
    }
}
