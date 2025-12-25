package com.termux.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.termux.ai.R;

/**
 * Dialog for Claude Code quick actions
 */
public class ClaudeQuickActionsDialog extends DialogFragment {
    
    private TabbedTerminalActivity.TerminalTab tab;
    
    public void setTab(TabbedTerminalActivity.TerminalTab tab) {
        this.tab = tab;
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
        
        // Set up click listeners
        btnFileOperations.setOnClickListener(v -> {
            showQuickCommand("List and analyze project files");
            dismiss();
        });
        
        btnCodeGeneration.setOnClickListener(v -> {
            showQuickCommand("Generate code for common patterns");
            dismiss();
        });
        
        btnProjectAnalysis.setOnClickListener(v -> {
            showQuickCommand("Analyze project structure and suggest improvements");
            dismiss();
        });
        
        btnDebugHelp.setOnClickListener(v -> {
            showQuickCommand("Help debug the current issue");
            dismiss();
        });
        
        btnVoiceInput.setOnClickListener(v -> {
            // TODO: Implement voice input functionality
            Toast.makeText(getContext(), "Voice input - Coming soon!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        
        btnStop.setOnClickListener(v -> {
            // Send escape to stop Claude operation
            Toast.makeText(getContext(), "Stopping Claude operation...", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        
        builder.setView(view)
                .setTitle("Claude Quick Actions")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ClaudeQuickActionsDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private void showQuickCommand(String command) {
        // This would typically send the command to the active terminal
        Toast.makeText(getContext(), "Quick command: " + command, Toast.LENGTH_LONG).show();
    }
}