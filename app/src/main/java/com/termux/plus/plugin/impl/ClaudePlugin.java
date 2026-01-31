package com.termux.plus.plugin.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.termux.ai.ClaudeCodeIntegration; // Keep using the original logic class for now as backend
import com.termux.plus.api.AIProvider;
import com.termux.plus.api.TermuxPlugin;

/**
 * Official Termux+ Plugin for Anthropic's Claude Code.
 * Adapts the legacy ClaudeCodeIntegration to the new Plugin API.
 */
public class ClaudePlugin implements AIProvider {

    private final ClaudeCodeIntegration legacyIntegration;
    private AIListener aiListener;
    private boolean enabled = true;
    private Context context;

    public ClaudePlugin() {
        this.legacyIntegration = new ClaudeCodeIntegration();
    }

    @Override
    public String getId() {
        return "com.termux.plus.claude";
    }

    @Override
    public String getName() {
        return "Claude Code Integration";
    }

    @Override
    public String getDescription() {
        return "Deep integration with Anthropic's Claude Code CLI tool.";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getAuthor() {
        return "Termux+ Team";
    }

    @Override
    public void onInit(Context context) {
        this.context = context;
        // Wire up legacy listeners to the new interface
        legacyIntegration.setListener(new ClaudeCodeIntegration.ClaudeIntegrationListener() {
            @Override
            public void onOperationDetected(String operation) {
                if (aiListener != null) aiListener.onOperationDetected(operation);
            }

            @Override
            public void onProgressUpdated(float progress) {
                if (aiListener != null) aiListener.onProgressUpdated(progress);
            }

            @Override
            public void onFileGenerated(String filePath, String action) {
                if (aiListener != null) aiListener.onFileGenerated(filePath, action);
            }

            @Override
            public void onErrorDetected(String error) {
                if (aiListener != null) aiListener.onError(error);
            }

            @Override
            public void onOperationCompleted() {
                if (aiListener != null) aiListener.onCompleted();
            }

            @Override
            public void onTokenUsageUpdated(int used, int total) {
                if (aiListener != null) aiListener.onTokenUsage(used, total);
            }
        });
    }

    @Override
    public void onUnload() {
        legacyIntegration.cleanup();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void processTerminalOutput(String output, int tabIndex) {
        if (isEnabled()) {
            legacyIntegration.processTerminalOutput(output, tabIndex);
        }
    }

    @Override
    public boolean isActive() {
        return legacyIntegration.isClaudeActive();
    }

    @Override
    public void setAIListener(AIListener listener) {
        this.aiListener = listener;
    }

    @Override
    public void stopOperation() {
        // Legacy integration doesn't have a direct stop without knowing the tab index strictly
        // For now, we assume active tab or just force complete locally
        // In a real scenario, we might need to send a signal to the terminal session
    }
    
    // Accessor for legacy code if needed during migration
    public ClaudeCodeIntegration getLegacyIntegration() {
        return legacyIntegration;
    }
}
