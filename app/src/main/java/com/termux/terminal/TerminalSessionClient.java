package com.termux.terminal;

import androidx.annotation.NonNull;

/**
 * STUB IMPLEMENTATION - TODO: Replace with real Termux terminal sources
 * 
 * Interface for receiving callbacks from TerminalSession
 * This is a minimal stub implementation that only includes methods
 * actually used in the current codebase.
 */
public interface TerminalSessionClient {
    
    /**
     * Called when the terminal session text content changes
     * @param changedSession The session that changed
     */
    void onTextChanged(@NonNull TerminalSession changedSession);
    
    /**
     * Called when the terminal session title changes
     * @param changedSession The session whose title changed
     */
    void onTitleChanged(@NonNull TerminalSession changedSession);
    
    /**
     * Called when a terminal session finishes
     * @param finishedSession The session that finished
     */
    void onSessionFinished(@NonNull TerminalSession finishedSession);
    
    /**
     * Called when text should be copied to clipboard
     * @param session The session requesting the copy
     * @param text The text to copy
     */
    void onCopyTextToClipboard(@NonNull TerminalSession session, String text);
    
    /**
     * Called when text should be pasted from clipboard
     * @param session The session requesting the paste
     */
    void onPasteTextFromClipboard(@NonNull TerminalSession session);
    
    /**
     * Called when the terminal bell is triggered
     * @param session The session that triggered the bell
     */
    void onBell(@NonNull TerminalSession session);
    
    /**
     * Called when terminal colors change
     * @param session The session whose colors changed
     */
    void onColorsChanged(@NonNull TerminalSession session);
}