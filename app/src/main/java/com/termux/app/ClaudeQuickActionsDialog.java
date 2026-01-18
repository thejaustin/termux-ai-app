package com.termux.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;

import com.termux.ai.R;

/**
 * Dialog for Claude Code quick actions.
 * Provides quick access to common Claude commands and terminal operations.
 * Enhanced with accessibility support and improved UX.
 */
public class ClaudeQuickActionsDialog extends DialogFragment {

    /**
     * Callback interface for handling quick action commands
     */
    public interface QuickActionCallback {
        /**
         * Called when a command should be sent to the terminal
         * @param command The command to send
         */
        void onSendCommand(String command);

        /**
         * Called when Claude operation should be interrupted (Ctrl+C)
         */
        void onStopClaude();

        /**
         * Called when voice input is requested
         */
        void onVoiceInputRequested();
    }

    private TabbedTerminalActivity.TerminalTab tab;
    private QuickActionCallback callback;

    public void setTab(TabbedTerminalActivity.TerminalTab tab) {
        this.tab = tab;
    }

    public void setCallback(QuickActionCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate the dialog layout
        View view = inflater.inflate(R.layout.dialog_claude_actions, null);

        // Initialize action buttons
        Button btnFileOperations = view.findViewById(R.id.btn_file_operations);
        Button btnCodeGeneration = view.findViewById(R.id.btn_code_generation);
        Button btnProjectAnalysis = view.findViewById(R.id.btn_project_analysis);
        Button btnDebugHelp = view.findViewById(R.id.btn_debug_help);
        Button btnVoiceInput = view.findViewById(R.id.btn_voice_input);
        Button btnStop = view.findViewById(R.id.btn_stop_claude);

        // Set accessibility content descriptions
        setupAccessibility(btnFileOperations, "File Operations",
            "Lists project files and source code in the current directory");
        setupAccessibility(btnCodeGeneration, "Code Generation",
            "Shows hints for using Claude to generate code");
        setupAccessibility(btnProjectAnalysis, "Project Analysis",
            "Analyzes project structure and file counts by language");
        setupAccessibility(btnDebugHelp, "Debug Help",
            "Shows recent commands and debugging tips");
        setupAccessibility(btnVoiceInput, "Voice Input",
            "Activate voice input to speak commands to Claude");
        setupAccessibility(btnStop, "Stop Claude",
            "Immediately stops the current Claude operation");

        // Ensure minimum touch target size (48dp)
        ensureMinTouchTarget(btnFileOperations);
        ensureMinTouchTarget(btnCodeGeneration);
        ensureMinTouchTarget(btnProjectAnalysis);
        ensureMinTouchTarget(btnDebugHelp);
        ensureMinTouchTarget(btnVoiceInput);
        ensureMinTouchTarget(btnStop);

        // Set up click listeners with practical development commands
        btnFileOperations.setOnClickListener(v -> {
            announceForAccessibility(v, "Running file operations command");
            // Show project files with useful information
            String cmd = "echo '📁 Project Files:' && " +
                "ls -la 2>/dev/null | head -20 && " +
                "echo '' && echo '📄 Source files:' && " +
                "find . -maxdepth 3 -type f \\( -name '*.java' -o -name '*.kt' -o -name '*.py' -o -name '*.js' -o -name '*.ts' -o -name '*.go' -o -name '*.rs' \\) 2>/dev/null | head -15";
            sendCommand(cmd);
            dismiss();
        });

        btnCodeGeneration.setOnClickListener(v -> {
            announceForAccessibility(v, "Showing code generation hints");
            // Prompt for Claude to help with code
            String cmd = "echo '💡 Claude Code Ready' && " +
                "echo 'Type your request, e.g.:' && " +
                "echo '  - Create a REST API endpoint' && " +
                "echo '  - Write unit tests for [file]' && " +
                "echo '  - Implement [feature]' && " +
                "echo '' && echo 'Start with: claude code'";
            sendCommand(cmd);
            dismiss();
        });

        btnProjectAnalysis.setOnClickListener(v -> {
            announceForAccessibility(v, "Analyzing project structure");
            // Comprehensive project analysis
            String cmd = "echo '🔍 Project Analysis:' && " +
                "echo '' && echo '📂 Structure:' && " +
                "(tree -L 2 -I 'node_modules|__pycache__|.git|build|target' 2>/dev/null || " +
                "find . -maxdepth 2 -type d ! -path '*/\\.*' ! -path '*/node_modules*' | head -25) && " +
                "echo '' && echo '📊 File counts:' && " +
                "echo \"  Java: $(find . -name '*.java' 2>/dev/null | wc -l)\" && " +
                "echo \"  Kotlin: $(find . -name '*.kt' 2>/dev/null | wc -l)\" && " +
                "echo \"  Python: $(find . -name '*.py' 2>/dev/null | wc -l)\" && " +
                "echo \"  JS/TS: $(find . \\( -name '*.js' -o -name '*.ts' \\) 2>/dev/null | wc -l)\"";
            sendCommand(cmd);
            dismiss();
        });

        btnDebugHelp.setOnClickListener(v -> {
            announceForAccessibility(v, "Showing debug information");
            // Show recent build/run errors and help
            String cmd = "echo '🐛 Debug Information:' && " +
                "echo '' && echo '📋 Recent commands:' && " +
                "(history 5 2>/dev/null || echo '  No history available') && " +
                "echo '' && echo '🔧 Common debug commands:' && " +
                "echo '  - Check logs: cat *.log | tail -50' && " +
                "echo '  - Find errors: grep -r \"error\\|Error\\|ERROR\" . --include=\"*.log\"' && " +
                "echo '  - Git status: git status' && " +
                "echo '  - Git diff: git diff'";
            sendCommand(cmd);
            dismiss();
        });

        btnVoiceInput.setOnClickListener(v -> {
            announceForAccessibility(v, "Opening voice input");
            if (callback != null) {
                callback.onVoiceInputRequested();
            } else {
                Toast.makeText(getContext(), "Voice input - Coming soon!", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        btnStop.setOnClickListener(v -> {
            announceForAccessibility(v, "Stopping Claude operation");
            stopClaude();
            dismiss();
        });

        builder.setView(view)
                .setTitle("Claude Quick Actions")
                .setNegativeButton("Close", (dialog, id) ->
                    ClaudeQuickActionsDialog.this.getDialog().cancel()
                );

        AlertDialog dialog = builder.create();

        // Set dialog to be announced for accessibility
        dialog.setOnShowListener(d -> {
            View dialogView = dialog.getWindow().getDecorView();
            dialogView.announceForAccessibility("Claude Quick Actions dialog opened. Select an action.");
        });

        return dialog;
    }

    /**
     * Set up accessibility attributes for a button
     */
    private void setupAccessibility(Button button, String label, String description) {
        button.setContentDescription(label + ". " + description);
        ViewCompat.setAccessibilityHeading(button, false);
    }

    /**
     * Ensure minimum touch target size for accessibility
     */
    private void ensureMinTouchTarget(View view) {
        view.post(() -> {
            int minSize = (int) (48 * getResources().getDisplayMetrics().density);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params.height < minSize) {
                params.height = minSize;
                view.setLayoutParams(params);
            }
        });
    }

    /**
     * Announce action for accessibility services
     */
    private void announceForAccessibility(View view, String message) {
        view.announceForAccessibility(message);
    }

    /**
     * Send a command to the active terminal
     */
    private void sendCommand(String command) {
        if (callback != null) {
            callback.onSendCommand(command);
        } else {
            Toast.makeText(getContext(), "Sending: " + command, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop Claude operation by sending Ctrl+C (interrupt signal)
     */
    private void stopClaude() {
        if (callback != null) {
            callback.onStopClaude();
            Toast.makeText(getContext(), "Stopping Claude operation...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Unable to stop - no terminal connection", Toast.LENGTH_SHORT).show();
        }
    }
}