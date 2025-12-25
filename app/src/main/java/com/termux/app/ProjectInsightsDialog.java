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
        // This would need more sophisticated detection based on actual files
        switch (projectType) {
            case "nodejs":
                // Would check for React, Vue, Angular, etc. in package.json
                return null; // Placeholder - would be determined dynamically
            case "python":
                // Would check for Django, Flask, FastAPI, etc. in requirements.txt
                return null; // Placeholder - would be determined dynamically
            case "java":
                // Would check for Spring, Maven, Gradle, etc. in build files
                return null; // Placeholder - would be determined dynamically
            default:
                return null;
        }
    }

    private void loadDependenciesForProject(String workingDirectory, TextView dependenciesView) {
        // This would analyze the project files to extract dependencies
        // For now, just show a placeholder
        dependenciesView.setText("Dependencies analysis not implemented yet in this view.\nWould scan package.json, requirements.txt, Cargo.toml, etc.");
    }

    private void generateDetailedReport() {
        // Implementation would generate a detailed project report
        // This could include file structure, technology stack, security vulnerabilities, etc.
    }

    private void askClaudeForAnalysis() {
        // Implementation would send a request to Claude to analyze the project
        // This could include code quality, security issues, optimization suggestions, etc.
    }
}