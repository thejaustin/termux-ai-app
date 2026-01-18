package com.termux.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.termux.ai.R;

/**
 * Context menu dialog for terminal tab operations.
 * Provides options to rename, duplicate, close, and close other tabs.
 */
public class TabContextMenuDialog extends DialogFragment {

    /**
     * Callback interface for tab context menu actions
     */
    public interface TabContextMenuCallback {
        void onRenameTab(int position, String newName);
        void onDuplicateTab(int position);
        void onCloseTab(int position);
        void onCloseOtherTabs(int position);
        void onCloseTabsToRight(int position);
    }

    private int tabPosition;
    private String currentTabName;
    private TabContextMenuCallback callback;
    private int totalTabs;

    public static TabContextMenuDialog newInstance(int position, String tabName, int totalTabs) {
        TabContextMenuDialog dialog = new TabContextMenuDialog();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("tabName", tabName);
        args.putInt("totalTabs", totalTabs);
        dialog.setArguments(args);
        return dialog;
    }

    public void setCallback(TabContextMenuCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt("position", 0);
            currentTabName = getArguments().getString("tabName", "Terminal");
            totalTabs = getArguments().getInt("totalTabs", 1);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] options;

        // Build menu options based on tab count and position
        if (totalTabs == 1) {
            // Only one tab - limited options
            options = new String[]{
                "Rename Tab",
                "Duplicate Tab"
            };
        } else if (tabPosition == totalTabs - 1) {
            // Last tab - no "close tabs to right"
            options = new String[]{
                "Rename Tab",
                "Duplicate Tab",
                "Close Tab",
                "Close Other Tabs"
            };
        } else {
            // Full options available
            options = new String[]{
                "Rename Tab",
                "Duplicate Tab",
                "Close Tab",
                "Close Other Tabs",
                "Close Tabs to Right"
            };
        }

        return new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tab: " + currentTabName)
            .setItems(options, (dialog, which) -> {
                if (callback == null) return;

                switch (which) {
                    case 0: // Rename
                        showRenameDialog();
                        break;
                    case 1: // Duplicate
                        callback.onDuplicateTab(tabPosition);
                        break;
                    case 2: // Close Tab (only if totalTabs > 1)
                        if (totalTabs > 1) {
                            callback.onCloseTab(tabPosition);
                        }
                        break;
                    case 3: // Close Other Tabs (only if totalTabs > 1)
                        if (totalTabs > 1) {
                            showCloseOthersConfirmation();
                        }
                        break;
                    case 4: // Close Tabs to Right
                        showCloseToRightConfirmation();
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .create();
    }

    private void showRenameDialog() {
        Context context = requireContext();

        // Create input layout with Material Design
        TextInputLayout inputLayout = new TextInputLayout(context);
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        inputLayout.setHint("Tab name");

        TextInputEditText input = new TextInputEditText(context);
        input.setText(currentTabName);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSelectAllOnFocus(true);
        inputLayout.addView(input);

        // Add padding to the layout
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, 0);
        container.addView(inputLayout);

        new MaterialAlertDialogBuilder(context)
            .setTitle("Rename Tab")
            .setView(container)
            .setPositiveButton("Rename", (dialog, which) -> {
                String newName = input.getText() != null ? input.getText().toString().trim() : "";
                if (!newName.isEmpty() && callback != null) {
                    callback.onRenameTab(tabPosition, newName);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCloseOthersConfirmation() {
        int otherTabsCount = totalTabs - 1;
        String message = "Close " + otherTabsCount + " other tab" + (otherTabsCount > 1 ? "s" : "") + "?";

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Close Other Tabs")
            .setMessage(message)
            .setPositiveButton("Close Others", (dialog, which) -> {
                if (callback != null) {
                    callback.onCloseOtherTabs(tabPosition);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCloseToRightConfirmation() {
        int tabsToRight = totalTabs - tabPosition - 1;
        String message = "Close " + tabsToRight + " tab" + (tabsToRight > 1 ? "s" : "") + " to the right?";

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Close Tabs to Right")
            .setMessage(message)
            .setPositiveButton("Close", (dialog, which) -> {
                if (callback != null) {
                    callback.onCloseTabsToRight(tabPosition);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
