package com.termux.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.termux.terminal.TerminalSession;

/**
 * Placeholder implementation of TerminalView for compilation compatibility.
 * 
 * TODO: Replace with real Termux core implementation or modularized dependency.
 * This is a compatibility shim for initial compile and testing only.
 */
@SuppressWarnings("unused")
public class TerminalView extends View {
    
    private TerminalSession currentSession;

    public TerminalView(Context context) {
        super(context);
    }

    public TerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TerminalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Attach a terminal session to this view.
     * TODO: Implement real session binding with PTY management.
     */
    public void attachSession(TerminalSession session) {
        this.currentSession = session;
    }

    /**
     * Get the currently attached terminal session.
     * TODO: Return actual active session.
     */
    public TerminalSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Called when the screen is updated.
     * TODO: Implement real screen rendering and invalidation.
     */
    public void onScreenUpdated() {
        // Placeholder - would trigger view redraw
        invalidate();
    }

    /**
     * Called when text changes in the terminal.
     * Hook for subclasses to handle text change events.
     * TODO: Implement real text change handling.
     */
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        // Placeholder hook for subclasses
    }

    /**
     * Request a redraw of the terminal view.
     * TODO: Implement efficient rendering.
     */
    public void requestRedraw() {
        invalidate();
    }
}