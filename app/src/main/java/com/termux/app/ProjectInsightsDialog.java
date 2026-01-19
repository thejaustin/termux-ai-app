package com.termux.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.termux.ai.R;

/**
 * Dialog for displaying project insights and AI recommendations
 */
public class ProjectInsightsDialog extends DialogFragment {

    private TabbedTerminalActivity.TerminalTab tab;

    public void setTab(TabbedTerminalActivity.TerminalTab tab) {
        this.tab = tab;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate the project insights dialog layout
        View view = inflater.inflate(R.layout.dialog_project_insights, null);

        // Initialize UI elements
        TextView projectNameValue = view.findViewById(R.id.project_name_value);
        TextView projectTypeValue = view.findViewById(R.id.project_type_value);
        TextView projectLanguageValue = view.findViewById(R.id.project_language_value);
        TextView projectFrameworkValue = view.findViewById(R.id.project_framework_value);
        TextView projectDependenciesList = view.findViewById(R.id.project_dependencies_list);
        TextView aiRecommendations = view.findViewById(R.id.ai_recommendations);
        Button generateReportBtn = view.findViewById(R.id.btn_generate_report);
        Button askClaudeBtn = view.findViewById(R.id.btn_ask_claude);

        // Populate with project information if available
        if (tab != null) {
            projectNameValue.setText(tab.getName());
            projectTypeValue.setText(tab.getProjectType());
            
            // Determine language based on project type
            String language = determineLanguageFromProjectType(tab.getProjectType());
            projectLanguageValue.setText(language);
            
            // Determine framework if possible
            String framework = determineFrameworkFromProjectType(tab.getProjectType());
            projectFrameworkValue.setText(framework != null ? framework : "None detected");
            
            // Load dependencies based on project type
            loadDependenciesForProject(tab.getWorkingDirectory(), projectDependenciesList);
        }

        // Set up button click listeners
        generateReportBtn.setOnClickListener(v -> {
            // Generate detailed project report
            generateDetailedReport();
        });

        askClaudeBtn.setOnClickListener(v -> {
            // Ask Claude for project analysis
            askClaudeForAnalysis();
        });

        builder.setView(view)
                .setTitle("Project Insights")
                .setNegativeButton("Close", (dialog, id) -> {
                    ProjectInsightsDialog.this.getDialog().cancel();
                });

        return builder.create();
    }

    private String determineLanguageFromProjectType(String projectType) {
        switch (projectType) {
            case "nodejs":
                return "JavaScript/TypeScript";
            case "python":
                return "Python";
            case "rust":
                return "Rust";
            case "go":
                return "Go";
            case "java":
                return "Java/Kotlin";
            default:
                return "Unknown";
        }
    }

    private String determineFrameworkFromProjectType(String projectType) {
        String workingDir = tab.getWorkingDirectory();
        java.io.File dir = new java.io.File(workingDir);
        if (!dir.exists()) return null;

        switch (projectType) {
            case "nodejs":
                if (new java.io.File(dir, "react.json").exists() || 
                    new java.io.File(dir, "src/App.js").exists()) return "React";
                if (new java.io.File(dir, "next.config.js").exists()) return "Next.js";
                if (new java.io.File(dir, "vue.config.js").exists()) return "Vue.js";
                return "Node.js (Vanilla)";
            case "python":
                if (new java.io.File(dir, "manage.py").exists()) return "Django";
                if (new java.io.File(dir, "app.py").exists()) return "Flask/FastAPI";
                return "Python (Vanilla)";
            case "java":
                if (new java.io.File(dir, "src/main/resources/application.properties").exists()) return "Spring Boot";
                if (new java.io.File(dir, "build.gradle").exists()) return "Gradle Project";
                return "Java Project";
            default:
                return null;
        }
    }

    private void loadDependenciesForProject(String workingDirectory, TextView dependenciesView) {
        java.io.File dir = new java.io.File(workingDirectory);
        if (!dir.exists()) return;

        java.io.File packageJson = new java.io.File(dir, "package.json");
        java.io.File requirementsTxt = new java.io.File(dir, "requirements.txt");
        java.io.File cargoToml = new java.io.File(dir, "Cargo.toml");

        if (packageJson.exists()) {
            dependenciesView.setText("NPM Dependencies found in package.json.\n(Scan required for details)");
        } else if (requirementsTxt.exists()) {
            dependenciesView.setText("Python requirements found in requirements.txt.");
        } else if (cargoToml.exists()) {
            dependenciesView.setText("Rust crates found in Cargo.toml.");
        } else {
            dependenciesView.setText("No standard dependency files found.");
        }
    }

    private void generateDetailedReport() {
        android.widget.Toast.makeText(getContext(), "Generating report for " + tab.getName() + "...", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void askClaudeForAnalysis() {
        if (getActivity() instanceof TabbedTerminalActivity) {
            TabbedTerminalActivity activity = (TabbedTerminalActivity) getActivity();
            TerminalFragment fragment = activity.getCurrentTerminalFragment();
            if (fragment != null) {
                fragment.sendCommand("claude analyze project");
                dismiss();
            }
        }
    }
}