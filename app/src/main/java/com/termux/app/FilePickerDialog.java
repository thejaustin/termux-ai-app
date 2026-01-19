package com.termux.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.ai.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * File picker dialog for selecting files to add as context for Claude.
 * Supports multi-selection and directory navigation.
 */
public class FilePickerDialog extends DialogFragment {

    /**
     * Callback for file selection
     */
    public interface FilePickerCallback {
        void onFilesSelected(List<File> files);
    }

    private static final String ARG_START_DIR = "start_directory";
    private static final String ARG_DIRECTORY_ONLY = "directory_only";
    private static final String ARG_SINGLE_SELECTION = "single_selection";
    private static final String[] HIDDEN_DIRS = {".git", "node_modules", "__pycache__", ".gradle", "build", ".idea"};
    private static final String[] SOURCE_EXTENSIONS = {".java", ".kt", ".py", ".js", ".ts", ".tsx", ".jsx", ".go", ".rs", ".c", ".cpp", ".h", ".hpp", ".swift", ".rb", ".php", ".html", ".css", ".scss", ".json", ".xml", ".yaml", ".yml", ".md", ".txt", ".sh", ".bash"};

    private FilePickerCallback callback;
    private File currentDirectory;
    private Set<File> selectedFiles = new HashSet<>();
    private FileAdapter adapter;
    private TextView pathView;
    private TextView selectionCountView;
    private boolean directoryOnly = false;
    private boolean singleSelection = false;

    public static FilePickerDialog newInstance(String startDirectory) {
        return newInstance(startDirectory, false, false);
    }

    public static FilePickerDialog newInstance(String startDirectory, boolean directoryOnly, boolean singleSelection) {
        FilePickerDialog dialog = new FilePickerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_START_DIR, startDirectory);
        args.putBoolean(ARG_DIRECTORY_ONLY, directoryOnly);
        args.putBoolean(ARG_SINGLE_SELECTION, singleSelection);
        dialog.setArguments(args);
        return dialog;
    }

    public void setCallback(FilePickerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String startDir = getArguments().getString(ARG_START_DIR);
            currentDirectory = new File(startDir != null ? startDir : "/");
            directoryOnly = getArguments().getBoolean(ARG_DIRECTORY_ONLY, false);
            singleSelection = getArguments().getBoolean(ARG_SINGLE_SELECTION, false);
        } else {
            currentDirectory = new File("/");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_file_picker, null);

        pathView = view.findViewById(R.id.current_path);
        selectionCountView = view.findViewById(R.id.selection_count);
        RecyclerView recyclerView = view.findViewById(R.id.file_list);
        View btnParent = view.findViewById(R.id.btn_parent_dir);

        adapter = new FileAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnParent.setOnClickListener(v -> navigateToParent());
        updateUI();

        return new MaterialAlertDialogBuilder(requireContext())
            .setTitle(directoryOnly ? "Select Directory" : "Select Files")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(directoryOnly ? "Select Current" : "Add Selected", (dialog, which) -> {
                if (callback != null) {
                    if (directoryOnly && selectedFiles.isEmpty()) {
                        callback.onFilesSelected(Collections.singletonList(currentDirectory));
                    } else if (!selectedFiles.isEmpty()) {
                        callback.onFilesSelected(new ArrayList<>(selectedFiles));
                    }
                }
            })
            .create();
    }

    private void navigateToParent() {
        File parent = currentDirectory.getParentFile();
        if (parent != null && parent.canRead()) {
            currentDirectory = parent;
            updateUI();
        }
    }

    private void navigateTo(File directory) {
        if (directory.isDirectory() && directory.canRead()) {
            currentDirectory = directory;
            if (directoryOnly && singleSelection) {
                selectedFiles.clear();
                selectedFiles.add(currentDirectory);
            }
            updateUI();
        }
    }

    private void toggleFileSelection(File file) {
        if (singleSelection) {
            selectedFiles.clear();
            selectedFiles.add(file);
        } else {
            if (selectedFiles.contains(file)) {
                selectedFiles.remove(file);
            } else {
                selectedFiles.add(file);
            }
        }
        updateSelectionCount();
        adapter.notifyDataSetChanged();
    }

    private void updateUI() {
        pathView.setText(currentDirectory.getAbsolutePath());
        adapter.setFiles(getFilteredFiles());
        updateSelectionCount();
    }

    private void updateSelectionCount() {
        int count = selectedFiles.size();
        if (count == 0) {
            selectionCountView.setText(directoryOnly ? "Current directory selected" : "No files selected");
        } else {
            selectionCountView.setText(count + " item" + (count > 1 ? "s" : "") + " selected");
        }
    }

    private List<FileItem> getFilteredFiles() {
        List<FileItem> items = new ArrayList<>();
        File[] files = currentDirectory.listFiles();

        if (files == null) return items;

        // Sort: directories first, then files alphabetically
        List<File> dirs = new ArrayList<>();
        List<File> filesList = new ArrayList<>();

        for (File file : files) {
            // Skip hidden directories
            if (file.isDirectory() && isHiddenDir(file.getName())) {
                continue;
            }
            // Skip hidden files (starting with .)
            if (file.getName().startsWith(".")) {
                continue;
            }

            if (file.isDirectory()) {
                dirs.add(file);
            } else if (!directoryOnly && isSourceFile(file.getName())) {
                filesList.add(file);
            }
        }

        Collections.sort(dirs, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        Collections.sort(filesList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (File dir : dirs) {
            items.add(new FileItem(dir, true, selectedFiles.contains(dir)));
        }
        for (File file : filesList) {
            items.add(new FileItem(file, false, selectedFiles.contains(file)));
        }

        return items;
    }

    private boolean isHiddenDir(String name) {
        for (String hidden : HIDDEN_DIRS) {
            if (hidden.equals(name)) return true;
        }
        return false;
    }

    private boolean isSourceFile(String name) {
        String lowerName = name.toLowerCase();
        for (String ext : SOURCE_EXTENSIONS) {
            if (lowerName.endsWith(ext)) return true;
        }
        return false;
    }

    /**
     * Data class for file list items
     */
    private static class FileItem {
        final File file;
        final boolean isDirectory;
        boolean isSelected;

        FileItem(File file, boolean isDirectory, boolean isSelected) {
            this.file = file;
            this.isDirectory = isDirectory;
            this.isSelected = isSelected;
        }
    }

    /**
     * RecyclerView adapter for file list
     */
    private class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
        private List<FileItem> files = new ArrayList<>();

        void setFiles(List<FileItem> files) {
            this.files = files;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_picker, parent, false);
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            FileItem item = files.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return files.size();
        }
    }

    /**
     * ViewHolder for file list items
     */
    private class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        private final TextView info;
        private final View checkMark;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.file_icon);
            name = itemView.findViewById(R.id.file_name);
            info = itemView.findViewById(R.id.file_info);
            checkMark = itemView.findViewById(R.id.check_mark);
        }

        void bind(FileItem item) {
            name.setText(item.file.getName());

            if (item.isDirectory) {
                icon.setImageResource(R.drawable.ic_folder);
                info.setText(countItems(item.file) + " items");
                checkMark.setVisibility(View.GONE);

                itemView.setOnClickListener(v -> navigateTo(item.file));
            } else {
                icon.setImageResource(getFileIcon(item.file.getName()));
                info.setText(formatFileSize(item.file.length()));
                checkMark.setVisibility(item.isSelected ? View.VISIBLE : View.INVISIBLE);

                itemView.setOnClickListener(v -> {
                    toggleFileSelection(item.file);
                    item.isSelected = selectedFiles.contains(item.file);
                    checkMark.setVisibility(item.isSelected ? View.VISIBLE : View.INVISIBLE);
                });
            }

            // Accessibility
            itemView.setContentDescription(
                (item.isDirectory ? "Folder: " : "File: ") + item.file.getName() +
                (item.isSelected ? ", selected" : "")
            );
        }

        private int countItems(File dir) {
            File[] files = dir.listFiles();
            return files != null ? files.length : 0;
        }

        private String formatFileSize(long size) {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return (size / 1024) + " KB";
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }

        private int getFileIcon(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".java") || lower.endsWith(".kt")) return R.drawable.ic_java;
            if (lower.endsWith(".py")) return R.drawable.ic_python;
            if (lower.endsWith(".js") || lower.endsWith(".ts") || lower.endsWith(".tsx") || lower.endsWith(".jsx"))
                return R.drawable.ic_nodejs;
            if (lower.endsWith(".go")) return R.drawable.ic_go;
            if (lower.endsWith(".rs")) return R.drawable.ic_rust;
            if (lower.endsWith(".json") || lower.endsWith(".xml") || lower.endsWith(".yaml") || lower.endsWith(".yml"))
                return R.drawable.ic_code;
            if (lower.endsWith(".md") || lower.endsWith(".txt")) return R.drawable.ic_info;
            return R.drawable.ic_code;
        }
    }
}
