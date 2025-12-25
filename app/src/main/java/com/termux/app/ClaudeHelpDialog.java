package com.termux.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

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

        // Find the close button and set click listener
        Button closeButton = view.findViewById(R.id.btn_close_help);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }

        builder.setView(view)
                .setTitle(R.string.claude_help_title);

        return builder.create();
    }
}