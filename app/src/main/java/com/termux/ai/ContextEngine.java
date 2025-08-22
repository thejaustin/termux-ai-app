package com.termux.ai;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Context Engine for Termux AI
 * 
 * Maintains awareness of:
 * - Current working directory and project structure
 * - Recent commands and their outcomes
 * - File system changes
 * - Git repository status
 * - Running processes
 * - Environment variables
 */
public class ContextEngine {
    private static final String TAG = "TermuxContextEngine";
    private static final int MAX_RECENT_COMMANDS = 20;
    private static final int MAX_FILE_CHANGES = 50;
    
    private final Context context;
    private final Gson gson;
    private final ExecutorService executor;
    
    private String currentWorkingDirectory;
    private ProjectInfo currentProject;
    private final List<CommandRecord> recentCommands;
    private final List<FileChange> recentFileChanges;
    private GitStatus gitStatus;
    private SystemInfo systemInfo;
    
    public interface ContextUpdateListener {
        void onContextChanged(ContextSnapshot snapshot);
        void onProjectDetected(ProjectInfo project);
        void onGitStatusChanged(GitStatus status);
    }
    
    private ContextUpdateListener listener;
    
    public ContextEngine(@NonNull Context context) {
        this.context = context;
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        this.recentCommands = new ArrayList<>();
        this.recentFileChanges = new ArrayList<>();
        
        this.currentWorkingDirectory = getTermuxHomeDirectory();
        updateContext();
    }
    
    public void setListener(ContextUpdateListener listener) {
        this.listener = listener;
    }
    
    private String getTermuxHomeDirectory() {
        return context.getFilesDir().getParent() + "/files/home";
    }
    
    /**
     * Update all context information
     */
    public void updateContext() {
        executor.execute(() -> {
            try {
                updateProjectInfo();
                updateGitStatus();
                updateSystemInfo();
                
                if (listener != null) {
                    listener.onContextChanged(getCurrentSnapshot());
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to update context", e);
            }
        });
    }
    
    /**
     * Record a command execution
     */
    public void recordCommand(String command, int exitCode, String output, String error) {
        CommandRecord record = new CommandRecord();
        record.command = command;
        record.exitCode = exitCode;
        record.output = truncateString(output, 1000);
        record.error = truncateString(error, 1000);
        record.timestamp = System.currentTimeMillis();
        record.workingDirectory = currentWorkingDirectory;
        
        synchronized (recentCommands) {
            recentCommands.add(0, record);
            if (recentCommands.size() > MAX_RECENT_COMMANDS) {
                recentCommands.remove(recentCommands.size() - 1);
            }
        }
        
        // Update context after command execution
        updateContext();
    }
    
    /**
     * Record a file system change
     */
    public void recordFileChange(String filePath, String changeType) {
        FileChange change = new FileChange();
        change.filePath = filePath;
        change.changeType = changeType; // created, modified, deleted
        change.timestamp = System.currentTimeMillis();
        
        synchronized (recentFileChanges) {
            recentFileChanges.add(0, change);
            if (recentFileChanges.size() > MAX_FILE_CHANGES) {
                recentFileChanges.remove(recentFileChanges.size() - 1);
            }
        }
    }
    
    /**
     * Change working directory
     */
    public void setWorkingDirectory(String directory) {
        if (directory != null && !directory.equals(currentWorkingDirectory)) {
            currentWorkingDirectory = directory;
            updateContext();
        }
    }
    
    /**
     * Get current context snapshot for AI
     */
    public ContextSnapshot getCurrentSnapshot() {
        ContextSnapshot snapshot = new ContextSnapshot();
        snapshot.workingDirectory = currentWorkingDirectory;
        snapshot.project = currentProject;
        snapshot.gitStatus = gitStatus;
        snapshot.systemInfo = systemInfo;
        snapshot.recentCommands = new ArrayList<>(recentCommands);
        snapshot.recentFileChanges = new ArrayList<>(recentFileChanges);
        snapshot.timestamp = System.currentTimeMillis();
        
        return snapshot;
    }
    
    /**
     * Get context as JSON for AI consumption
     */
    public String getContextAsJson() {
        return gson.toJson(getCurrentSnapshot());
    }
    
    private void updateProjectInfo() {
        File workingDir = new File(currentWorkingDirectory);
        if (!workingDir.exists() || !workingDir.isDirectory()) {
            currentProject = null;
            return;
        }
        
        ProjectInfo project = new ProjectInfo();
        project.path = currentWorkingDirectory;
        project.name = workingDir.getName();
        
        // Detect project type and language
        File[] files = workingDir.listFiles();
        if (files != null) {
            List<String> fileNames = new ArrayList<>();
            for (File file : files) {
                fileNames.add(file.getName());
            }
            
            project.type = detectProjectType(fileNames);
            project.language = detectPrimaryLanguage(files);
            project.framework = detectFramework(fileNames, project.language);
        }
        
        // Check if project changed
        if (currentProject == null || !project.equals(currentProject)) {
            currentProject = project;
            if (listener != null) {
                listener.onProjectDetected(project);
            }
        }
    }
    
    private String detectProjectType(List<String> fileNames) {
        if (fileNames.contains("package.json")) return "nodejs";
        if (fileNames.contains("requirements.txt") || fileNames.contains("pyproject.toml")) return "python";
        if (fileNames.contains("Cargo.toml")) return "rust";
        if (fileNames.contains("go.mod")) return "go";
        if (fileNames.contains("pom.xml") || fileNames.contains("build.gradle")) return "java";
        if (fileNames.contains("Makefile")) return "c/c++";
        if (fileNames.contains(".git")) return "git_repository";
        return "unknown";
    }
    
    private String detectPrimaryLanguage(File[] files) {
        int[] counts = new int[10]; // js, py, java, cpp, etc.
        
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".js") || name.endsWith(".ts")) counts[0]++;
                else if (name.endsWith(".py")) counts[1]++;
                else if (name.endsWith(".java")) counts[2]++;
                else if (name.endsWith(".cpp") || name.endsWith(".c")) counts[3]++;
                else if (name.endsWith(".rs")) counts[4]++;
                else if (name.endsWith(".go")) counts[5]++;
                else if (name.endsWith(".sh")) counts[6]++;
                else if (name.endsWith(".kt")) counts[7]++;
                else if (name.endsWith(".swift")) counts[8]++;
                else if (name.endsWith(".rb")) counts[9]++;
            }
        }
        
        int maxIndex = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[maxIndex]) {
                maxIndex = i;
            }
        }
        
        String[] languages = {"javascript", "python", "java", "cpp", "rust", "go", "bash", "kotlin", "swift", "ruby"};
        return counts[maxIndex] > 0 ? languages[maxIndex] : "unknown";
    }
    
    private String detectFramework(List<String> fileNames, String language) {
        if ("javascript".equals(language)) {
            if (fileNames.contains("next.config.js")) return "nextjs";
            if (fileNames.contains("vue.config.js")) return "vue";
            if (fileNames.contains("angular.json")) return "angular";
            if (fileNames.contains("svelte.config.js")) return "svelte";
        } else if ("python".equals(language)) {
            if (fileNames.contains("manage.py")) return "django";
            if (fileNames.contains("app.py")) return "flask";
            if (fileNames.contains("fastapi")) return "fastapi";
        } else if ("java".equals(language)) {
            if (fileNames.contains("pom.xml")) return "maven";
            if (fileNames.contains("build.gradle")) return "gradle";
        }
        return null;
    }
    
    private void updateGitStatus() {
        File gitDir = new File(currentWorkingDirectory, ".git");
        if (!gitDir.exists()) {
            gitStatus = null;
            return;
        }
        
        GitStatus status = new GitStatus();
        status.isRepository = true;
        status.branch = getCurrentGitBranch();
        status.hasUnstagedChanges = hasUnstagedChanges();
        status.hasStagedChanges = hasStagedChanges();
        status.hasUncommittedChanges = status.hasUnstagedChanges || status.hasStagedChanges;
        
        // Check if git status changed
        if (gitStatus == null || !status.equals(gitStatus)) {
            gitStatus = status;
            if (listener != null) {
                listener.onGitStatusChanged(status);
            }
        }
    }
    
    private String getCurrentGitBranch() {
        try {
            File headFile = new File(currentWorkingDirectory, ".git/HEAD");
            if (headFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(headFile));
                String line = reader.readLine();
                reader.close();
                
                if (line != null && line.startsWith("ref: refs/heads/")) {
                    return line.substring("ref: refs/heads/".length());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read git branch", e);
        }
        return "unknown";
    }
    
    private boolean hasUnstagedChanges() {
        // This is a simplified check - in a real implementation,
        // you might want to execute git commands or use JGit library
        return false;
    }
    
    private boolean hasStagedChanges() {
        // This is a simplified check - in a real implementation,
        // you might want to execute git commands or use JGit library
        return false;
    }
    
    private void updateSystemInfo() {
        SystemInfo info = new SystemInfo();
        info.timestamp = System.currentTimeMillis();
        info.availableMemory = Runtime.getRuntime().freeMemory();
        info.totalMemory = Runtime.getRuntime().totalMemory();
        info.processorCount = Runtime.getRuntime().availableProcessors();
        
        // Get disk space
        File homeDir = new File(currentWorkingDirectory);
        info.freeSpace = homeDir.getFreeSpace();
        info.totalSpace = homeDir.getTotalSpace();
        
        systemInfo = info;
    }
    
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    // Data classes
    public static class ContextSnapshot {
        public String workingDirectory;
        public ProjectInfo project;
        public GitStatus gitStatus;
        public SystemInfo systemInfo;
        public List<CommandRecord> recentCommands;
        public List<FileChange> recentFileChanges;
        public long timestamp;
    }
    
    public static class ProjectInfo {
        public String path;
        public String name;
        public String type;
        public String language;
        public String framework;
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ProjectInfo that = (ProjectInfo) obj;
            return path.equals(that.path) && 
                   type.equals(that.type) && 
                   language.equals(that.language);
        }
    }
    
    public static class GitStatus {
        public boolean isRepository;
        public String branch;
        public boolean hasUnstagedChanges;
        public boolean hasStagedChanges;
        public boolean hasUncommittedChanges;
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GitStatus that = (GitStatus) obj;
            return isRepository == that.isRepository &&
                   hasUnstagedChanges == that.hasUnstagedChanges &&
                   hasStagedChanges == that.hasStagedChanges &&
                   branch.equals(that.branch);
        }
    }
    
    public static class SystemInfo {
        public long timestamp;
        public long availableMemory;
        public long totalMemory;
        public int processorCount;
        public long freeSpace;
        public long totalSpace;
    }
    
    public static class CommandRecord {
        public String command;
        public int exitCode;
        public String output;
        public String error;
        public long timestamp;
        public String workingDirectory;
    }
    
    public static class FileChange {
        public String filePath;
        public String changeType;
        public long timestamp;
    }
}