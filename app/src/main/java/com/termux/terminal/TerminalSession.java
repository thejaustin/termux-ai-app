package com.termux.terminal;

/**
 * Placeholder implementation of TerminalSession for compilation compatibility.
 * 
 * TODO: Replace with real Termux core implementation or modularized dependency.
 * This is a compatibility shim for initial compile and testing only.
 */
@SuppressWarnings("unused")
public class TerminalSession {
    
    private String executable;
    private String workingDirectory;
    private String[] args;
    private String[] environment;
    private TerminalSessionClient client;
    private TerminalEmulator emulator;
    private boolean isRunning;
    private StringBuilder buffer;
    
    /**
     * Constructor matching usage in TerminalFragment.
     * TODO: Implement real PTY session management.
     */
    public TerminalSession(String executable, String workingDir, String[] args, String[] env, TerminalSessionClient client) {
        this.executable = executable;
        this.workingDirectory = workingDir;
        this.args = args != null ? args.clone() : new String[0];
        this.environment = env != null ? env.clone() : new String[0];
        this.client = client;
        this.emulator = new TerminalEmulator();
        this.isRunning = true;
        this.buffer = new StringBuilder();
    }
    
    /**
     * Write text to the terminal session.
     * TODO: Implement real PTY input handling.
     */
    public void write(String text) {
        if (text != null) {
            // Append to internal buffer for placeholder behavior
            buffer.append(text);
            
            // Notify client of text change
            if (client != null) {
                client.onTextChanged(this);
            }
        }
    }
    
    /**
     * Finish the terminal session if it's running.
     * TODO: Implement real process termination.
     */
    public void finishIfRunning() {
        if (isRunning) {
            isRunning = false;
            
            // Notify client that session finished
            if (client != null) {
                client.onSessionFinished(this);
            }
        }
    }
    
    /**
     * Get the terminal emulator instance.
     * TODO: Return real emulator with proper screen buffer.
     */
    public TerminalEmulator getEmulator() {
        return emulator;
    }
    
    /**
     * Check if the session is currently running.
     * TODO: Implement real process state tracking.
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    // Getters for session properties
    public String getExecutable() {
        return executable;
    }
    
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    public String[] getArgs() {
        return args != null ? args.clone() : new String[0];
    }
    
    public String[] getEnvironment() {
        return environment != null ? environment.clone() : new String[0];
    }
    
    public String getBufferContent() {
        return buffer.toString();
    }
}