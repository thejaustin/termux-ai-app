package com.termux.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.termux.ai.R;

public class ClaudeHelpDialog extends DialogFragment {
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        
        View view = inflater.inflate(R.layout.dialog_claude_help, null);
        TextView helpText = view.findViewById(R.id.claude_help_text);
        
        String helpContent = getString(R.string.claude_help_content);
        helpText.setText(Html.fromHtml(helpContent, Html.FROM_HTML_MODE_COMPACT));
        
        builder.setView(view)
                .setTitle(R.string.claude_help_title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
                
        return builder.create();
    }
}