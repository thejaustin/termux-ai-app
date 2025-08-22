package com.termux.ai;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Main Application class for Termux AI
 * 
 * Handles global initialization and configuration
 */
public class TermuxAIApplication extends Application {
    private static final String TAG = "TermuxAIApplication";
    private static final String PREFS_NAME = "termux_ai_prefs";
    
    private static TermuxAIApplication instance;
    private SharedPreferences preferences;
    private ClaudeCodeIntegration globalClaudeIntegration;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        Log.d(TAG, "Initializing Termux AI Application v" + BuildConfig.VERSION_NAME);
        
        // Initialize preferences
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Set up dark theme (always dark for terminal app)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        
        // Apply Material You dynamic colors with fallback
        applyDynamicColors();
        
        // Initialize global components
        initializeClaudeIntegration();
        initializeTerminalEnvironment();
        
        // Setup crash handling
        setupCrashHandler();
        
        Log.d(TAG, "Termux AI Application initialized successfully");
    }
    
    private void initializeClaudeIntegration() {
        globalClaudeIntegration = new ClaudeCodeIntegration();
        
        // Set up global Claude settings
        boolean claudeEnabled = preferences.getBoolean("claude_enabled", true);
        boolean gboardEnabled = preferences.getBoolean("gboard_enabled", true);
        
        Log.d(TAG, "Claude integration enabled: " + claudeEnabled);
        Log.d(TAG, "Gboard autocomplete enabled: " + gboardEnabled);
    }
    
    private void initializeTerminalEnvironment() {
        // Create necessary directories
        createDirectories();
        
        // Set up terminal environment variables
        setupEnvironmentVariables();
        
        // Initialize any native components if needed
        initializeNativeComponents();
    }
    
    private void createDirectories() {
        try {
            // Create app-specific directories
            java.io.File homeDir = new java.io.File(getFilesDir(), "home");
            if (!homeDir.exists()) {
                homeDir.mkdirs();
                Log.d(TAG, "Created home directory: " + homeDir.getAbsolutePath());
            }
            
            java.io.File tmpDir = new java.io.File(getFilesDir(), "tmp");
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
                Log.d(TAG, "Created tmp directory: " + tmpDir.getAbsolutePath());
            }
            
            java.io.File claudeDir = new java.io.File(homeDir, ".claude");
            if (!claudeDir.exists()) {
                claudeDir.mkdirs();
                Log.d(TAG, "Created Claude directory: " + claudeDir.getAbsolutePath());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create directories", e);
        }
    }
    
    private void setupEnvironmentVariables() {
        // This would set up environment variables for terminal sessions
        // In a real implementation, this might involve JNI calls or other native setup
        Log.d(TAG, "Environment variables configured");
    }
    
    private void initializeNativeComponents() {
        // Initialize any native libraries or components
        // This is where you'd load native libraries for terminal emulation
        try {
            // Example: System.loadLibrary("termux-ai-native");
            Log.d(TAG, "Native components initialized");
        } catch (Exception e) {
            Log.w(TAG, "Failed to initialize native components: " + e.getMessage());
        }
    }
    
    private void setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                Log.e(TAG, "Uncaught exception in thread " + thread.getName(), ex);
                
                // Save crash info for debugging
                saveCrashInfo(ex);
                
                // Try to gracefully shut down
                cleanup();
                
                // Re-throw to allow system handling
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, ex);
            }
        });
    }
    
    private void saveCrashInfo(Throwable ex) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("last_crash", ex.toString());
            editor.putLong("last_crash_time", System.currentTimeMillis());
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save crash info", e);
        }
    }
    
    private void cleanup() {
        try {
            if (globalClaudeIntegration != null) {
                globalClaudeIntegration.cleanup();
            }
            Log.d(TAG, "Application cleanup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }
    
    // Public accessors
    public static TermuxAIApplication getInstance() {
        return instance;
    }
    
    public SharedPreferences getAppPreferences() {
        return preferences;
    }
    
    public ClaudeCodeIntegration getGlobalClaudeIntegration() {
        return globalClaudeIntegration;
    }
    
    // Configuration methods
    public boolean isClaudeEnabled() {
        return preferences.getBoolean("claude_enabled", true);
    }
    
    public void setClaudeEnabled(boolean enabled) {
        preferences.edit().putBoolean("claude_enabled", enabled).apply();
    }
    
    public boolean isGboardEnabled() {
        return preferences.getBoolean("gboard_enabled", true);
    }
    
    public void setGboardEnabled(boolean enabled) {
        preferences.edit().putBoolean("gboard_enabled", enabled).apply();
    }
    
    public String getClaudeModel() {
        return preferences.getString("claude_model", "claude-3-sonnet");
    }
    
    public void setClaudeModel(String model) {
        preferences.edit().putString("claude_model", model).apply();
    }
    
    public int getTokenLimit() {
        return preferences.getInt("token_limit", 8000);
    }
    
    public void setTokenLimit(int limit) {
        preferences.edit().putInt("token_limit", limit).apply();
    }
    
    public boolean isAutoSuggestionsEnabled() {
        return preferences.getBoolean("auto_suggestions", true);
    }
    
    public void setAutoSuggestionsEnabled(boolean enabled) {
        preferences.edit().putBoolean("auto_suggestions", enabled).apply();
    }
    
    // Utility methods
    public String getHomeDirectory() {
        return getFilesDir().getAbsolutePath() + "/home";
    }
    
    public String getTempDirectory() {
        return getFilesDir().getAbsolutePath() + "/tmp";
    }
    
    public String getClaudeDirectory() {
        return getHomeDirectory() + "/.claude";
    }
    
    public boolean isFirstRun() {
        boolean isFirst = preferences.getBoolean("first_run", true);
        if (isFirst) {
            preferences.edit().putBoolean("first_run", false).apply();
        }
        return isFirst;
    }
    
    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }
    
    public int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }
    
    public boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }
    
    /**
     * Apply Material You dynamic colors with safe fallback
     * Attempts to use dynamic colors on Android 12+ devices,
     * falls back to expressive palette if dynamic colors are unsupported
     */
    private void applyDynamicColors() {
        try {
            // Try to apply dynamic colors (requires Android 12+ and Material 3)
            Class<?> dynamicColorsClass = Class.forName("com.google.android.material.color.DynamicColors");
            java.lang.reflect.Method applyMethod = dynamicColorsClass.getMethod("applyToActivitiesIfAvailable", Application.class);
            applyMethod.invoke(null, this);
            Log.d(TAG, "Dynamic colors applied successfully");
        } catch (Exception e) {
            Log.d(TAG, "Dynamic colors not available, using expressive theme fallback: " + e.getMessage());
            // Optional: Set flag to use Theme.TermuxAI.Expressive in activities
            // Activities can check this preference to choose the appropriate theme
            preferences.edit().putBoolean("use_expressive_theme", true).apply();
        }
    }
}