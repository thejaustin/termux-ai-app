package com.termux.ai;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.termux.ai.BuildConfig;

/**
 * Main Application class for Termux AI
 * 
 * Handles global initialization and configuration
 */
public class TermuxAIApplication extends Application {
    private static final String TAG = "TermuxAIApplication";
    private static final String PREFS_NAME = "termux_ai_prefs";
    private static final String PREF_DYNAMIC_COLORS = "dynamic_colors_enabled";
    
    private static TermuxAIApplication instance;
    private SharedPreferences preferences;
    private ClaudeCodeIntegration globalClaudeIntegration;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Enable StrictMode in debug builds to catch performance issues
        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }

        Log.d(TAG, "Initializing Termux AI Application v" + BuildConfig.VERSION_NAME);

        // Initialize preferences
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Apply Material You 3 Dynamic Colors
        boolean dynamicColorsEnabled = preferences.getBoolean(PREF_DYNAMIC_COLORS, true); // Enable by default
        if (dynamicColorsEnabled) {
            // Apply dynamic colors with custom options
            DynamicColors.applyToActivitiesIfAvailable(
                this,
                new com.google.android.material.color.DynamicColorsOptions.Builder()
                    .setThemeOverlay(getThemeOverlay())
                    .setPrecondition((activity, theme) -> {
                        // Only apply if user has enabled it
                        return preferences.getBoolean(PREF_DYNAMIC_COLORS, true);
                    })
                    .setOnAppliedCallback(activity -> {
                        Log.d(TAG, "Material You 3 Dynamic Colors applied successfully");
                    })
                    .build()
            );
            Log.d(TAG, "Material You 3 Dynamic Colors enabled with expressive theming");
        } else {
            Log.d(TAG, "Using static Material 3 theme");
        }

        // Set up theme to follow system (light/dark)
        int nightMode = preferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        // Initialize global components
        initializeClaudeIntegration();

        // Setup crash handling
        setupCrashHandler();

        // Initialize terminal environment in background to avoid blocking main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                initializeTerminalEnvironment();
            }
        }).start();

        Log.d(TAG, "Termux AI Application initialized successfully");
    }

    private void enableStrictMode() {
        android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        android.os.StrictMode.setVmPolicy(new android.os.StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());

        Log.d(TAG, "StrictMode enabled for performance monitoring");
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
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                Log.e(TAG, "Uncaught exception in thread " + thread.getName(), ex);

                // Save crash info for debugging
                saveCrashInfo(ex);

                // Try to gracefully shut down
                cleanup();

                // Re-throw to allow system handling
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, ex);
                }
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

    // Material You 3 Theming Methods

    /**
     * Get the theme overlay resource ID based on user preference
     */
    private int getThemeOverlay() {
        String themeStyle = preferences.getString("theme_style", "expressive");
        switch (themeStyle) {
            case "vibrant":
                return com.google.android.material.R.style.ThemeOverlay_Material3_DynamicColors_DayNight;
            case "tonal":
                return 0; // Use default
            case "expressive":
            default:
                return 0; // Use default expressive theme
        }
    }

    /**
     * Apply theme mode (light/dark/auto)
     */
    public void setNightMode(int mode) {
        preferences.edit().putInt("night_mode", mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public int getNightMode() {
        return preferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Enable or disable dynamic colors
     */
    public void setDynamicColorsEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_DYNAMIC_COLORS, enabled).apply();
    }

    public boolean isDynamicColorsEnabled() {
        return preferences.getBoolean(PREF_DYNAMIC_COLORS, true);
    }

    /**
     * Set theme style (expressive, vibrant, tonal)
     */
    public void setThemeStyle(String style) {
        preferences.edit().putString("theme_style", style).apply();
    }

    public String getThemeStyle() {
        return preferences.getString("theme_style", "expressive");
    }

    /**
     * Check if dynamic colors are available on this device
     */
    public boolean areDynamicColorsAvailable() {
        return com.google.android.material.color.DynamicColors.isDynamicColorAvailable();
    }
}