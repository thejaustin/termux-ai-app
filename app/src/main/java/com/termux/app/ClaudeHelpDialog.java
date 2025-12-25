package com.termux.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.termux.ai.R;

/**
 * Dialog for Claude Code help and instructions
 */
public class ClaudeHelpDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate the help dialog layout
        View view = inflater.inflate(R.layout.dialog_claude_help, null);

        builder.setView(view)
                .setTitle("Claude Code Help")
                .setPositiveButton("OK", (dialog, id) -> {
                    // Close the dialog
                });

        return builder.create();
    }
}