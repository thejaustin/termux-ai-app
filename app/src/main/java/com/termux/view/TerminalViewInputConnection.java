package com.termux.view;

import android.view.inputmethod.BaseInputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stub implementation of TerminalViewInputConnection for compilation.
 * 
 * TODO: Replace with actual Termux TerminalViewInputConnection implementation.
 * This is a minimal no-op stub to allow compilation of EnhancedTerminalView.
 * 
 * Note: Kept as final and public to match expected API.
 */
public final class TerminalViewInputConnection extends BaseInputConnection {

    private final TerminalView terminalView;
    private final boolean fullEditor;

    public TerminalViewInputConnection(@NonNull TerminalView terminalView, boolean fullEditor) {
        super(terminalView, fullEditor);
        this.terminalView = terminalView;
        this.fullEditor = fullEditor;
        // TODO: Initialize actual terminal input connection
    }

    @Override
    public boolean commitText(@Nullable CharSequence text, int newCursorPosition) {
        // TODO: Implement actual text commit handling
        // No-op stub implementation
        return super.commitText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingText(@Nullable CharSequence text, int newCursorPosition) {
        // TODO: Implement actual composing text handling
        // No-op stub implementation
        return super.setComposingText(text, newCursorPosition);
    }
}