package com.termux.terminal;

import androidx.annotation.NonNull;

/**
 * Interface for terminal session client callbacks.
 * 
 * TODO: Replace with actual Termux TerminalSessionClient interface.
 * This is a minimal stub interface to allow compilation of TerminalFragment.
 */
public interface TerminalSessionClient {

    /**
     * Called when text changes in the terminal session.
     * 
     * @param changedSession the terminal session that changed
     */
    void onTextChanged(@NonNull TerminalSession changedSession);

    /**
     * Called when the title of the terminal session changes.
     * 
     * @param changedSession the terminal session with changed title
     */
    void onTitleChanged(@NonNull TerminalSession changedSession);

    /**
     * Called when a terminal session finishes.
     * 
     * @param finishedSession the terminal session that finished
     */
    void onSessionFinished(@NonNull TerminalSession finishedSession);

    /**
     * Called when text should be copied to clipboard.
     * 
     * @param session the terminal session
     * @param text the text to copy
     */
    void onCopyTextToClipboard(@NonNull TerminalSession session, String text);

    /**
     * Called when text should be pasted from clipboard.
     * 
     * @param session the terminal session
     */
    void onPasteTextFromClipboard(@NonNull TerminalSession session);

    /**
     * Called when the terminal bell is activated.
     * 
     * @param session the terminal session
     */
    void onBell(@NonNull TerminalSession session);

    /**
     * Called when terminal colors change.
     * 
     * @param session the terminal session
     */
    void onColorsChanged(@NonNull TerminalSession session);
}