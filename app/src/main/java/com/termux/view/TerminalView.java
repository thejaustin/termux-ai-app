package com.termux.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalBuffer;
import com.termux.terminal.TextStyle;

/**
 * Functional TerminalView implementation for Termux AI
 * Renders terminal emulator screen with proper text buffer rendering
 */
@SuppressWarnings("unused")
public class TerminalView extends View {

    private TerminalSession currentSession;
    private Paint textPaint;
    private Paint cursorPaint;
    private Paint backgroundPaint;
    private float textSize = 28f;
    private float charWidth;
    private float charHeight;
    private int topRow = 0;

    public TerminalView(Context context) {
        super(context);
        initialize();
    }

    public TerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TerminalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        initializePaints();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initializePaints() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.MONOSPACE);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);

        cursorPaint = new Paint();
        cursorPaint.setColor(0xFF00FF00); // Green cursor

        // Calculate character dimensions using a single character
        float[] widths = new float[1];
        textPaint.getTextWidths("X", widths);
        charWidth = widths[0];
        
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        charHeight = (float) Math.ceil(metrics.descent - metrics.ascent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw terminal background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (currentSession == null) {
            // Show welcome message if no session attached
            drawWelcomeMessage(canvas);
            return;
        }

        TerminalEmulator emulator = currentSession.getEmulator();
        if (emulator == null) {
            drawWelcomeMessage(canvas);
            return;
        }

        // Render terminal screen
        renderScreen(canvas, emulator);
    }

    private void drawWelcomeMessage(Canvas canvas) {
        String[] lines = new String[]{
            "Termux AI Terminal",
            "",
            "Welcome to the world's first",
            "AI-enhanced terminal!",
            "",
            "Setting up terminal session...",
            "$"
        };

        float y = charHeight + 20;
        for (String line : lines) {
            canvas.drawText(line, 20, y, textPaint);
            y += charHeight;
        }
    }

    private void renderScreen(Canvas canvas, TerminalEmulator emulator) {
        try {
            TerminalBuffer buffer = emulator.getScreen();
            int rows = buffer.getRows();
            int cols = buffer.getColumns();
            int cursorRow = emulator.getCursorRow();
            int cursorCol = emulator.getCursorCol();

            float y = charHeight;

            // Render each row
            for (int row = 0; row < rows; row++) {
                float x = 10;

                // Render each character with its style
                for (int col = 0; col < cols; col++) {
                    char ch = buffer.getChar(col, row);
                    int style = buffer.getStyle(col, row);

                    // Decode style information
                    int foreColor = TextStyle.decodeForeColor(style);
                    int backColor = TextStyle.decodeBackColor(style);
                    int effect = TextStyle.decodeEffect(style);

                    // Get actual RGB colors from palette
                    int fgRgb = TextStyle.getColor(foreColor, TextStyle.DEFAULT_COLORSCHEME);
                    int bgRgb = TextStyle.getColor(backColor, TextStyle.DEFAULT_COLORSCHEME);

                    // If style is default (0) or default foreground (256), use white on black
                    if (foreColor == 0 || foreColor == TextStyle.COLOR_INDEX_FOREGROUND) {
                        fgRgb = Color.WHITE;
                    }
                    if (backColor == 0 || backColor == TextStyle.COLOR_INDEX_BACKGROUND) {
                        bgRgb = Color.BLACK;
                    }

                    // Handle inverse video
                    if (TextStyle.isInverse(style)) {
                        int temp = fgRgb;
                        fgRgb = bgRgb;
                        bgRgb = temp;
                    }

                    // Draw background if not black
                    if (bgRgb != Color.BLACK) {
                        backgroundPaint.setColor(bgRgb);
                        canvas.drawRect(x, y - charHeight + 5, x + charWidth, y + 2, backgroundPaint);
                    }

                    // Set text attributes
                    textPaint.setColor(fgRgb);
                    textPaint.setFakeBoldText(TextStyle.isBold(style));
                    textPaint.setUnderlineText(TextStyle.isUnderline(style));

                    // Draw character
                    if (ch != ' ' && ch != 0) {
                        canvas.drawText(String.valueOf(ch), x, y, textPaint);
                    }

                    x += charWidth;
                }

                // Draw cursor if on this row
                if (row == cursorRow) {
                    float cursorX = 10 + (cursorCol * charWidth);
                    canvas.drawRect(cursorX, y - charHeight + 5,
                                  cursorX + charWidth, y + 2, cursorPaint);
                }

                y += charHeight;

                // Reset paint attributes for next row
                textPaint.setFakeBoldText(false);
                textPaint.setUnderlineText(false);
                backgroundPaint.setColor(Color.BLACK);
            }
        } catch (Exception e) {
            // If rendering fails, show error
            canvas.drawText("Error rendering terminal: " + e.getMessage(), 20, charHeight + 20, textPaint);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return currentSession != null;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = android.text.InputType.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
        return new BaseInputConnection(this, true);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentSession != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            // Handle special keys
            int keyCode = event.getKeyCode();

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                currentSession.write("\r");
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                currentSession.write("\u007f");
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_TAB) {
                currentSession.write("\t");
                return true;
            }

            // Handle character keys
            int unicodeChar = event.getUnicodeChar();
            if (unicodeChar > 0) {
                currentSession.write(new String(Character.toChars(unicodeChar)));
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate new rows and columns
        int cols = (int) Math.floor((w - 20) / charWidth);
        int rows = (int) Math.floor(h / charHeight);
        
        if (cols > 0 && rows > 0 && currentSession != null) {
            currentSession.updateSize(cols, rows);
        }
    }

    /**
     * Attach a terminal session to this view.
     */
    public void attachSession(TerminalSession session) {
        this.currentSession = session;
        
        // Update session size to match current view size
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            int cols = (int) Math.floor((w - 20) / charWidth);
            int rows = (int) Math.floor(h / charHeight);
            if (cols > 0 && rows > 0) {
                session.updateSize(cols, rows);
            }
        }
        
        invalidate();
    }

    /**
     * Get the currently attached terminal session.
     */
    public TerminalSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Called when the screen is updated.
     */
    public void onScreenUpdated() {
        invalidate();
    }

    /**
     * Called when text changes in the terminal.
     * Hook for subclasses to handle text change events.
     */
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        // Hook for subclasses to override
    }

    /**
     * Request a redraw of the terminal view.
     */
    public void requestRedraw() {
        invalidate();
    }

    /**
     * Get the entire transcript text currently displayed in the terminal.
     */
    public String getTranscriptText() {
        if (currentSession == null) {
            return "";
        }

        try {
            TerminalEmulator emulator = currentSession.getEmulator();
            if (emulator == null) {
                return "";
            }

            StringBuilder transcript = new StringBuilder();
            int rows = emulator.getScreen().getRows();
            int cols = emulator.getScreen().getColumns();

            for (int row = 0; row < rows; row++) {
                String line = emulator.getScreen().getSelectedText(0, row, cols, row);
                if (line != null) {
                    transcript.append(line);
                    if (row < rows - 1) {
                        transcript.append("\n");
                    }
                }
            }

            return transcript.toString();
        } catch (Exception e) {
            return "Error retrieving transcript: " + e.getMessage();
        }
    }
}