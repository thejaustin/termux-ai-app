package com.termux.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Claude Code Integration for Termux AI
 * 
 * Handles:
 * - Detection of Claude Code CLI activation
 * - Parsing of Claude output for progress and file operations
 * - Visual enhancements during Claude operations
 * - Mobile-optimized Claude workflows
 */
public class ClaudeCodeIntegration {
    private static final String TAG = "ClaudeCodeIntegration";
    
    // Claude Code detection patterns
    private static final Pattern CLAUDE_START_PATTERN = 
        Pattern.compile("(?i)(claude\\s+code|claude-code|@anthropic/claude-code)");
    
    private static final Pattern CLAUDE_THINKING_PATTERN = 
        Pattern.compile("🤔\\s*(Claude is thinking|Thinking|Processing)");
        
    private static final Pattern CLAUDE_PROGRESS_PATTERN = 
        Pattern.compile("\\[([▓=]+)([░\\-]*)\\]\\s*(\\d+)%");
        
    private static final Pattern CLAUDE_FILE_PATTERN = 
        Pattern.compile("(?:├─|\\s*)(\\S+\\.\\w+)\\s*(✨|🆕|📝)\\s*(NEW|MODIFIED|CREATED|UPDATED)");
        
    private static final Pattern CLAUDE_ERROR_PATTERN = 
        Pattern.compile("❌\\s*(Error|Failed|Exception):\\s*(.+)");
        
    private static final Pattern CLAUDE_SUCCESS_PATTERN = 
        Pattern.compile("✅\\s*(Success|Complete|Done|Finished)");
        
    private static final Pattern CLAUDE_TOKEN_PATTERN = 
        Pattern.compile("Token[s]?:\\s*(\\d+)/(\\d+)\\s*(?:K|k)?");
    
    // Listeners
    public interface ClaudeIntegrationListener {
        void onOperationDetected(String operation);
        void onProgressUpdated(float progress);
        void onFileGenerated(String filePath, String action);
        void onErrorDetected(String error);
        void onOperationCompleted();
        void onTokenUsageUpdated(int used, int total);
    }
    
    public interface GlobalListener {
        void onClaudeDetected(int tabIndex);
        void onClaudeCompleted(int tabIndex);
    }
    
    private ClaudeIntegrationListener listener;
    private GlobalListener globalListener;
    private List<ClaudeSession> activeSessions;
    
    // Session tracking
    private static class ClaudeSession {
        int tabIndex;
        String currentOperation;
        float progress;
        boolean isActive;
        long startTime;
        List<String> generatedFiles;
        int tokensUsed;
        int tokenLimit;
        
        ClaudeSession(int tabIndex) {
            this.tabIndex = tabIndex;
            this.generatedFiles = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
            this.progress = 0.0f;
            this.isActive = true;
        }
    }
    
    public ClaudeCodeIntegration() {
        this.activeSessions = new ArrayList<>();
    }
    
    public void setListener(ClaudeIntegrationListener listener) {
        this.listener = listener;
    }
    
    public void setGlobalListener(GlobalListener globalListener) {
        this.globalListener = globalListener;
    }
    
    /**
     * Process terminal output to detect and enhance Claude Code operations
     */
    public void processTerminalOutput(String output, int tabIndex) {
        if (output == null || output.trim().isEmpty()) return;
        
        String[] lines = output.split("\n");
        for (String line : lines) {
            processLine(line.trim(), tabIndex);
        }
    }
    
    private void processLine(String line, int tabIndex) {
        // Detect Claude Code start
        if (detectClaudeStart(line, tabIndex)) {
            return;
        }
        
        ClaudeSession session = getSession(tabIndex);
        if (session == null || !session.isActive) {
            return;
        }
        
        // Process Claude operations
        if (detectProgress(line, session)) return;
        if (detectFileOperation(line, session)) return;
        if (detectError(line, session)) return;
        if (detectSuccess(line, session)) return;
        if (detectTokenUsage(line, session)) return;
        if (detectOperation(line, session)) return;
    }
    
    private boolean detectClaudeStart(String line, int tabIndex) {
        Matcher matcher = CLAUDE_START_PATTERN.matcher(line);
        if (matcher.find()) {
            Log.d(TAG, "Claude Code detected in tab " + tabIndex);
            
            ClaudeSession session = new ClaudeSession(tabIndex);
            activeSessions.removeIf(s -> s.tabIndex == tabIndex); // Remove existing
            activeSessions.add(session);
            
            if (globalListener != null) {
                globalListener.onClaudeDetected(tabIndex);
            }
            
            return true;
        }
        return false;
    }
    
    private boolean detectProgress(String line, ClaudeSession session) {
        Matcher matcher = CLAUDE_PROGRESS_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                String progressStr = matcher.group(3);
                float progress = Float.parseFloat(progressStr) / 100.0f;
                
                session.progress = progress;
                
                if (listener != null) {
                    listener.onProgressUpdated(progress);
                }
                
                Log.d(TAG, "Progress updated: " + (int)(progress * 100) + "%");
                return true;
            } catch (NumberFormatException e) {
                Log.w(TAG, "Failed to parse progress: " + line);
            }
        }
        
        // Also detect thinking indicators
        if (CLAUDE_THINKING_PATTERN.matcher(line).find()) {
            if (listener != null) {
                listener.onProgressUpdated(0.1f); // Show minimal progress for thinking
            }
            return true;
        }
        
        return false;
    }
    
    private boolean detectFileOperation(String line, ClaudeSession session) {
        Matcher matcher = CLAUDE_FILE_PATTERN.matcher(line);
        if (matcher.find()) {
            String filePath = matcher.group(1);
            String action = matcher.group(3);
            
            session.generatedFiles.add(filePath);
            
            if (listener != null) {
                listener.onFileGenerated(filePath, action);
            }
            
            Log.d(TAG, "File operation detected: " + filePath + " " + action);
            return true;
        }
        return false;
    }
    
    private boolean detectError(String line, ClaudeSession session) {
        Matcher matcher = CLAUDE_ERROR_PATTERN.matcher(line);
        if (matcher.find()) {
            String error = matcher.group(2);
            
            if (listener != null) {
                listener.onErrorDetected(error);
            }
            
            Log.w(TAG, "Claude error detected: " + error);
            return true;
        }
        return false;
    }
    
    private boolean detectSuccess(String line, ClaudeSession session) {
        if (CLAUDE_SUCCESS_PATTERN.matcher(line).find()) {
            completeSession(session);
            return true;
        }
        return false;
    }
    
    private boolean detectTokenUsage(String line, ClaudeSession session) {
        Matcher matcher = CLAUDE_TOKEN_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                int used = Integer.parseInt(matcher.group(1));
                int total = Integer.parseInt(matcher.group(2));
                
                session.tokensUsed = used;
                session.tokenLimit = total;
                
                if (listener != null) {
                    listener.onTokenUsageUpdated(used, total);
                }
                
                return true;
            } catch (NumberFormatException e) {
                Log.w(TAG, "Failed to parse token usage: " + line);
            }
        }
        return false;
    }
    
    private boolean detectOperation(String line, ClaudeSession session) {
        // Detect various Claude operations
        String[] operations = {
            "Creating", "Generating", "Writing", "Updating", "Modifying",
            "Analyzing", "Refactoring", "Testing", "Building", "Installing"
        };
        
        for (String op : operations) {
            if (line.toLowerCase().contains(op.toLowerCase())) {
                session.currentOperation = op;
                
                if (listener != null) {
                    listener.onOperationDetected(op);
                }
                
                return true;
            }
        }
        return false;
    }
    
    private void completeSession(ClaudeSession session) {
        session.isActive = false;
        session.progress = 1.0f;
        
        if (listener != null) {
            listener.onProgressUpdated(1.0f);
            listener.onOperationCompleted();
        }
        
        if (globalListener != null) {
            globalListener.onClaudeCompleted(session.tabIndex);
        }
        
        Log.d(TAG, "Claude session completed for tab " + session.tabIndex);
        
        // Remove completed session after delay
        new android.os.Handler().postDelayed(() -> {
            activeSessions.removeIf(s -> s == session);
        }, 5000); // 5 second delay
    }
    
    private ClaudeSession getSession(int tabIndex) {
        for (ClaudeSession session : activeSessions) {
            if (session.tabIndex == tabIndex && session.isActive) {
                return session;
            }
        }
        return null;
    }
    
    /**
     * Public methods for manual updates
     */
    public void updateProgress(float progress) {
        if (listener != null) {
            listener.onProgressUpdated(progress);
        }
    }
    
    public void reportFileGenerated(String filePath, String action) {
        if (listener != null) {
            listener.onFileGenerated(filePath, action);
        }
    }
    
    /**
     * Check if Claude is active in any session
     */
    public boolean isClaudeActive() {
        return activeSessions.stream().anyMatch(s -> s.isActive);
    }
    
    /**
     * Check if Claude is active in specific tab
     */
    public boolean isClaudeActive(int tabIndex) {
        return getSession(tabIndex) != null;
    }
    
    /**
     * Get current operation for tab
     */
    public String getCurrentOperation(int tabIndex) {
        ClaudeSession session = getSession(tabIndex);
        return session != null ? session.currentOperation : null;
    }
    
    /**
     * Get progress for tab
     */
    public float getProgress(int tabIndex) {
        ClaudeSession session = getSession(tabIndex);
        return session != null ? session.progress : 0.0f;
    }
    
    /**
     * Get generated files for tab
     */
    public List<String> getGeneratedFiles(int tabIndex) {
        ClaudeSession session = getSession(tabIndex);
        return session != null ? new ArrayList<>(session.generatedFiles) : new ArrayList<>();
    }
    
    /**
     * Force complete session (e.g., when user interrupts)
     */
    public void forceCompleteSession(int tabIndex) {
        ClaudeSession session = getSession(tabIndex);
        if (session != null) {
            completeSession(session);
        }
    }
    
    /**
     * Cleanup all sessions
     */
    public void cleanup() {
        for (ClaudeSession session : activeSessions) {
            if (globalListener != null) {
                globalListener.onClaudeCompleted(session.tabIndex);
            }
        }
        activeSessions.clear();
    }
    
    /**
     * Get session statistics
     */
    public SessionStats getSessionStats(int tabIndex) {
        ClaudeSession session = getSession(tabIndex);
        if (session == null) return null;
        
        SessionStats stats = new SessionStats();
        stats.duration = System.currentTimeMillis() - session.startTime;
        stats.filesGenerated = session.generatedFiles.size();
        stats.tokensUsed = session.tokensUsed;
        stats.tokenLimit = session.tokenLimit;
        stats.progress = session.progress;
        stats.currentOperation = session.currentOperation;
        
        return stats;
    }
    
    public static class SessionStats {
        public long duration;
        public int filesGenerated;
        public int tokensUsed;
        public int tokenLimit;
        public float progress;
        public String currentOperation;
    }
}