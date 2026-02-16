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

    private final TerminalView terminalView;
    private String mComposingText = "";

    public TerminalViewInputConnection(TerminalView targetView, boolean fullEditor) {
        super(targetView, fullEditor);
        this.terminalView = targetView;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        if (text == null) {
            return true;
        }
        
        TerminalSession session = terminalView.getCurrentSession();
        if (session != null) {
            // If we have composing text, we need to remove it first because
            // commitText replaces the composing region.
            if (mComposingText.length() > 0) {
                // Backspace over the entire composing text
                for (int i = 0; i < mComposingText.length(); i++) {
                    session.write("\b");
                }
                mComposingText = "";
            }
            
            session.write(text.toString());
            return true;
        }

        return false;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        if (text == null) {
            return true;
        }

        String newText = text.toString();
        TerminalSession session = terminalView.getCurrentSession();
        
        if (session != null) {
            // Calculate the common prefix to avoid deleting and re-typing unchanged parts
            int commonPrefixLength = 0;
            int minLength = Math.min(mComposingText.length(), newText.length());
            
            while (commonPrefixLength < minLength && 
                   mComposingText.charAt(commonPrefixLength) == newText.charAt(commonPrefixLength)) {
                commonPrefixLength++;
            }
            
            // Backspace over the part of the old composing text that doesn't match
            for (int i = commonPrefixLength; i < mComposingText.length(); i++) {
                session.write("\b");
            }
            
            // Append the new part of the text
            if (commonPrefixLength < newText.length()) {
                session.write(newText.substring(commonPrefixLength));
            }
            
            mComposingText = newText;
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        // Clear composing text state on manual delete to avoid desync
        mComposingText = "";
        
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
            
            // Clear composing state
            mComposingText = "";
            
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
        // Clear internal tracking when composition is finished
        // The text is already written to the terminal
        mComposingText = "";
        return super.finishComposingText();
    }
}