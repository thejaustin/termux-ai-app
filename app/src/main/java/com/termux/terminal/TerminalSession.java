package com.termux.terminal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * STUB IMPLEMENTATION - TODO: Replace with real Termux terminal sources
 * 
 * Terminal session stub that provides minimal functionality for process management
 * This implementation only includes methods actually used in the current codebase.
 */
public class TerminalSession {
    
    private final String shell;
    private final String workingDirectory;
    private final String[] args;
    private final String[] environment;
    private final TerminalSessionClient client;
    private final TerminalEmulator emulator;
    
    /**
     * Create a new terminal session
     * @param shell The shell program to run
     * @param workingDirectory Working directory for the session
     * @param args Command line arguments
     * @param environment Environment variables
     * @param client Client to receive callbacks
     */
    public TerminalSession(@NonNull String shell, 
                          @NonNull String workingDirectory,
                          @NonNull String[] args, 
                          @NonNull String[] environment,
                          @NonNull TerminalSessionClient client) {
        this.shell = shell;
        this.workingDirectory = workingDirectory;
        this.args = args;
        this.environment = environment;
        this.client = client;
        this.emulator = new TerminalEmulator();
        
        // TODO: Replace with real implementation that starts the shell process
        // For now, just log the session creation
        System.out.println("STUB: Created TerminalSession with shell=" + shell + 
                         ", workingDir=" + workingDirectory);
    }
    
    /**
     * Get the terminal emulator for this session
     * @return The terminal emulator instance
     */
    @NonNull
    public TerminalEmulator getEmulator() {
        return emulator;
    }
    
    /**
     * Get the shell command
     * @return Shell command string
     */
    @NonNull
    public String getShell() {
        return shell;
    }
    
    /**
     * Get the working directory
     * @return Working directory path
     */
    @NonNull
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    /**
     * Get the session client
     * @return The session client
     */
    @NonNull
    public TerminalSessionClient getClient() {
        return client;
    }
    
    /**
     * Check if the session is running
     * @return true if running, false if stopped
     */
    public boolean isRunning() {
        // TODO: Replace with real process status check
        return true; // Stub return
    }
    
    /**
     * Send input to the terminal session
     * @param text Text to send
     */
    public void write(@NonNull String text) {
        // TODO: Replace with real implementation that writes to shell process
        System.out.println("STUB: Write to terminal: " + text);
    }
    
    /**
     * Terminate the terminal session
     */
    public void finishIfRunning() {
        // TODO: Replace with real implementation that terminates the shell process
        System.out.println("STUB: Terminating terminal session");
        if (client != null) {
            client.onSessionFinished(this);
        }
    }
}