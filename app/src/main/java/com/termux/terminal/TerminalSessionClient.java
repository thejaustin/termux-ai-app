package com.termux.terminal;

/**
 * Interface for terminal session client callbacks.
 * 
 * TODO: Replace with real Termux core implementation or modularized dependency.
 * This is a compatibility shim for initial compile and testing only.
 */
@SuppressWarnings("unused")
public interface TerminalSessionClient {
    
    /**
     * Called when text in the terminal session changes.
     * TODO: Implement real text change notifications.
     */
    void onTextChanged(TerminalSession session);
    
    /**
     * Called when the terminal session title changes.
     * TODO: Implement real title change notifications.
     */
    void onTitleChanged(TerminalSession session);
    
    /**
     * Called when a terminal session finishes.
     * TODO: Implement real session lifecycle management.
     */
    void onSessionFinished(TerminalSession session);
    
    /**
     * Called when text should be copied to clipboard.
     * TODO: Implement real clipboard integration.
     */
    void onCopyTextToClipboard(TerminalSession session, String text);
    
    /**
     * Called when text should be pasted from clipboard.
     * TODO: Implement real clipboard integration.
     */
    void onPasteTextFromClipboard(TerminalSession session);
    
    /**
     * Called when terminal bell is triggered.
     * TODO: Implement real bell notification.
     */
    void onBell(TerminalSession session);
    
    /**
     * Called when terminal colors change.
     * TODO: Implement real color scheme management.
     */
    void onColorsChanged(TerminalSession session);
}