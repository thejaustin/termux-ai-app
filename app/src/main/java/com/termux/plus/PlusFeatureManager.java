package com.termux.plus;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages Plus feature toggles for Termux+.
 *
 * Features toggled OFF here are hidden from the UI and disabled at runtime.
 * Backend improvements (codebase quality, security, performance) remain active regardless.
 *
 * Modeled after Obtainium+'s Plus Toggles approach.
 */
public class PlusFeatureManager {
    private static final String PREFS_NAME = "plus_feature_toggles";

    // Feature keys
    public static final String FEATURE_AI_INTEGRATION = "plus_ai_integration";
    public static final String FEATURE_VOICE_INPUT = "plus_voice_input";
    public static final String FEATURE_GESTURE_CONTROLS = "plus_gesture_controls";
    public static final String FEATURE_PROJECT_INSIGHTS = "plus_project_insights";
    public static final String FEATURE_QUICK_SETTINGS_PANEL = "plus_quick_settings_panel";
    public static final String FEATURE_DYNAMIC_COLORS = "plus_dynamic_colors";
    public static final String FEATURE_ONBOARDING = "plus_onboarding";
    public static final String FEATURE_MULTI_TAB = "plus_multi_tab";
    public static final String FEATURE_BIOMETRIC_PROTECTION = "plus_biometric_protection";
    public static final String FEATURE_PLUGIN_SYSTEM = "plus_plugin_system";

    private static PlusFeatureManager instance;
    private final SharedPreferences prefs;

    private PlusFeatureManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PlusFeatureManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlusFeatureManager(context);
        }
        return instance;
    }

    /**
     * Check if a Plus feature is enabled.
     * All features default to enabled (true).
     */
    public boolean isFeatureEnabled(String featureKey) {
        return prefs.getBoolean(featureKey, true);
    }

    /**
     * Enable or disable a Plus feature.
     */
    public void setFeatureEnabled(String featureKey, boolean enabled) {
        prefs.edit().putBoolean(featureKey, enabled).apply();
    }

    /**
     * Reset all toggles to defaults (all enabled).
     */
    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }

    // Convenience getters
    public boolean isAIEnabled() {
        return isFeatureEnabled(FEATURE_AI_INTEGRATION);
    }

    public boolean isVoiceInputEnabled() {
        return isFeatureEnabled(FEATURE_VOICE_INPUT);
    }

    public boolean isGestureControlsEnabled() {
        return isFeatureEnabled(FEATURE_GESTURE_CONTROLS);
    }

    public boolean isProjectInsightsEnabled() {
        return isFeatureEnabled(FEATURE_PROJECT_INSIGHTS);
    }

    public boolean isQuickSettingsPanelEnabled() {
        return isFeatureEnabled(FEATURE_QUICK_SETTINGS_PANEL);
    }

    public boolean isDynamicColorsEnabled() {
        return isFeatureEnabled(FEATURE_DYNAMIC_COLORS);
    }

    public boolean isOnboardingEnabled() {
        return isFeatureEnabled(FEATURE_ONBOARDING);
    }

    public boolean isMultiTabEnabled() {
        return isFeatureEnabled(FEATURE_MULTI_TAB);
    }

    public boolean isBiometricProtectionEnabled() {
        return isFeatureEnabled(FEATURE_BIOMETRIC_PROTECTION);
    }

    public boolean isPluginSystemEnabled() {
        return isFeatureEnabled(FEATURE_PLUGIN_SYSTEM);
    }
}
