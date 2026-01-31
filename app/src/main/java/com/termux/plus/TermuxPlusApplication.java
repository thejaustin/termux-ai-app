package com.termux.plus;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.termux.ai.BuildConfig;
import com.termux.ai.EncryptedPreferencesManager;
import com.termux.plus.plugin.PluginManager;
import com.termux.plus.plugin.impl.AutoSavePlugin;
import com.termux.plus.plugin.impl.ClaudePlugin;

/**
 * Main Application class for Termux+
 * 
 * Handles global initialization, configuration, and plugin management.
 */
public class TermuxPlusApplication extends Application {
    private static final String TAG = "TermuxPlusApplication";
    private static final String PREFS_NAME = "termux_plus_prefs";
    private static final String PREF_DYNAMIC_COLORS = "dynamic_colors_enabled";
    
    private static TermuxPlusApplication instance;
    private SharedPreferences preferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }

        Log.d(TAG, "Initializing Termux+ v" + BuildConfig.VERSION_NAME);

        // Initialize secure preferences
        preferences = EncryptedPreferencesManager.getEncryptedPrefs(this, PREFS_NAME);

        // Apply Material You 3 Dynamic Colors
        boolean dynamicColorsEnabled = preferences.getBoolean(PREF_DYNAMIC_COLORS, true);
        if (dynamicColorsEnabled) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }

        int nightMode = preferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        // Initialize Plugin Manager and Core Plugins
        initializePlugins();

        setupCrashHandler();

        // Background init
        new Thread(this::initializeTerminalEnvironment).start();

        Log.d(TAG, "Termux+ initialized successfully");
    }

    private void initializePlugins() {
        PluginManager manager = PluginManager.getInstance(this);
        
        // Register Official Core Plugins
        manager.registerPlugin(new ClaudePlugin());
        manager.registerPlugin(new AutoSavePlugin());
        
        Log.i(TAG, "Core plugins registered.");
    }

    private void enableStrictMode() {
        android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        android.os.StrictMode.setVmPolicy(new android.os.StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
    }
    
    private void initializeTerminalEnvironment() {
        createDirectories();
        // Setup environment variables (mock)
    }
    
    private void createDirectories() {
        try {
            java.io.File homeDir = new java.io.File(getFilesDir(), "home");
            if (!homeDir.exists()) homeDir.mkdirs();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create directories", e);
        }
    }
    
    private void setupCrashHandler() {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Log.e(TAG, "Uncaught exception", ex);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }
    
    public static TermuxPlusApplication getInstance() {
        return instance;
    }
    
    public SharedPreferences getAppPreferences() {
        return preferences;
    }
    
    // Config getters/setters delegating to preferences...
    public boolean isClaudeEnabled() {
        return preferences.getBoolean("claude_enabled", true);
    }

    public boolean areDynamicColorsAvailable() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S;
    }

    public boolean isDynamicColorsEnabled() {
        return preferences.getBoolean(PREF_DYNAMIC_COLORS, true);
    }

    public void setDynamicColorsEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_DYNAMIC_COLORS, enabled).apply();
    }

    public int getNightMode() {
        return preferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setNightMode(int nightMode) {
        preferences.edit().putInt("night_mode", nightMode).apply();
    }

    public String getThemeStyle() {
        return preferences.getString("theme_style", "expressive");
    }

    public void setThemeStyle(String style) {
        preferences.edit().putString("theme_style", style).apply();
    }
}
