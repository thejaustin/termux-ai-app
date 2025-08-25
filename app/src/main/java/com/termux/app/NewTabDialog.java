package com.termux.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.termux.ai.R;

/**
 * Dialog for creating new terminal tabs
 */
public class NewTabDialog extends DialogFragment {
    
    public interface NewTabListener {
        void onCreateTab(String name, String directory, String projectType);
    }
    
    private NewTabListener listener;
    private EditText nameEdit;
    private EditText directoryEdit;
    private Spinner projectTypeSpinner;
    
    public void setListener(NewTabListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        
        // Inflate the dialog layout
        View view = inflater.inflate(R.layout.dialog_new_tab, null);
        
        // Initialize views
        nameEdit = view.findViewById(R.id.edit_tab_name);
        directoryEdit = view.findViewById(R.id.edit_working_directory);
        projectTypeSpinner = view.findViewById(R.id.spinner_project_type);
        
        // Setup project type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.project_types,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        projectTypeSpinner.setAdapter(adapter);
        
        // Set default values
        String defaultDir = requireActivity().getFilesDir().getParent() + "/files/home";
        directoryEdit.setText(defaultDir);
        nameEdit.setText("Terminal " + (System.currentTimeMillis() % 1000));
        
        builder.setView(view)
                .setTitle("New Terminal Tab")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = nameEdit.getText().toString().trim();
                        String directory = directoryEdit.getText().toString().trim();
                        String projectType = projectTypeSpinner.getSelectedItem().toString();
                        
                        if (name.isEmpty()) {
                            name = "Terminal";
                        }
                        
                        if (directory.isEmpty()) {
                            directory = requireActivity().getFilesDir().getParent() + "/files/home";
                        }
                        
                        if (listener != null) {
                            listener.onCreateTab(name, directory, projectType);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewTabDialog.this.getDialog().cancel();
                    }
                });
                
        return builder.create();
    }
}