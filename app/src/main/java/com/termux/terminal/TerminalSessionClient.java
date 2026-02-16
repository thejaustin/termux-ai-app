package com.termux.terminal;

/**
 * Interface for terminal session client callbacks.
 * Implemented by the hosting component (e.g., TerminalFragment) to handle
 * events from the terminal session such as screen updates, clipboard operations,
 * and lifecycle events.
 */
public interface TerminalSessionClient {
    
    /**
     * Called when text in the terminal session changes.
     */
    void onTextChanged(TerminalSession session);
    
    /**
     * Called when the terminal session title changes.
     */
    void onTitleChanged(TerminalSession session);
    
    /**
     * Called when a terminal session finishes.
     */
    void onSessionFinished(TerminalSession session);
    
    /**
     * Called when text should be copied to clipboard.
     */
    void onCopyTextToClipboard(TerminalSession session, String text);
    
    /**
     * Called when text should be pasted from clipboard.
     */
    void onPasteTextFromClipboard(TerminalSession session);
    
    /**
     * Called when terminal bell is triggered.
     */
    void onBell(TerminalSession session);
    
    /**
     * Called when terminal colors change.
     */
    void onColorsChanged(TerminalSession session);
}