package com.termux.plus.api;

/**
 * Interface for AI capabilities within Termux+.
 */
public interface AIProvider extends TermuxPlugin {
    
    interface AIListener {
        void onOperationDetected(String operation);
        void onProgressUpdated(float progress);
        void onFileGenerated(String filePath, String action);
        void onError(String error);
        void onCompleted();
        void onTokenUsage(int used, int total);
    }

    /**
     * Process terminal output to detect AI specific patterns or triggers.
     * @param output The text line from the terminal.
     * @param tabIndex The index of the active tab.
     */
    void processTerminalOutput(String output, int tabIndex);

    /**
     * Check if an AI operation is currently active.
     */
    boolean isActive();

    /**
     * Set a listener for AI events.
     */
    void setAIListener(AIListener listener);

    /**
     * Stop any current AI operation.
     */
    void stopOperation();
}
