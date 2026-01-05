package com.termux.terminal;

import com.termux.view.TerminalView;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;

/**
 * InputConnection for handling keyboard input in the terminal.
 * Based on the original Termux implementation.
 */
@SuppressWarnings("unused")
public class TerminalViewInputConnection extends BaseInputConnection {

    private TerminalView terminalView;

    public TerminalViewInputConnection(TerminalView targetView, boolean fullEditor) {
        super(targetView, fullEditor);
        this.terminalView = targetView;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        if (text == null || text.length() == 0) {
            return true;
        }

        // Send each character to the terminal
        TerminalSession session = terminalView.getCurrentSession();
        if (session != null) {
            session.write(text.toString());
            return true;
        }

        return false;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        // For composing text (predictive input), treat it same as commit
        // This allows Gboard autocomplete to work properly
        if (text == null || text.length() == 0) {
            return true;
        }

        TerminalSession session = terminalView.getCurrentSession();
        if (session != null) {
            session.write(text.toString());
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        // Handle backspace/delete
        if (beforeLength > 0) {
            TerminalSession session = terminalView.getCurrentSession();
            if (session != null) {
                // Send backspace characters
                for (int i = 0; i < beforeLength; i++) {
                    session.write("\b");
                }
                return true;
            }
        }
        return super.deleteSurroundingText(beforeLength, afterLength);
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        // Forward key events to the terminal view
        if (terminalView != null) {
            return terminalView.dispatchKeyEvent(event);
        }
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean performEditorAction(int actionCode) {
        // Handle special actions like Enter key
        if (actionCode == EditorInfo.IME_ACTION_UNSPECIFIED ||
            actionCode == EditorInfo.IME_ACTION_DONE ||
            actionCode == EditorInfo.IME_ACTION_GO) {
            TerminalSession session = terminalView.getCurrentSession();
            if (session != null) {
                session.write("\r");  // Send carriage return (Enter)
                return true;
            }
        }
        return super.performEditorAction(actionCode);
    }

    @Override
    public boolean finishComposingText() {
        // Clear any composing text
        return super.finishComposingText();
    }
}