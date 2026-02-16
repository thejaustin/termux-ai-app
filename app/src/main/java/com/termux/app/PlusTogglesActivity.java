package com.termux.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.ai.R;
import com.termux.plus.PlusFeatureManager;

/**
 * Plus Toggles screen - similar to Obtainium+'s Plus Features toggle list.
 *
 * Allows users to enable/disable Plus features that differentiate Termux+ from upstream Termux.
 * Backend improvements (security, performance, code quality) stay active regardless.
 */
public class PlusTogglesActivity extends AppCompatActivity {

    private PlusFeatureManager featureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_toggles);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Plus Features");
        }

        featureManager = PlusFeatureManager.getInstance(this);

        setupToggles();

        Button resetButton = findViewById(R.id.btn_reset_toggles);
        resetButton.setOnClickListener(v -> {
            featureManager.resetToDefaults();
            recreate();
            Toast.makeText(this, "All Plus features re-enabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToggles() {
        // AI Integration
        bindToggle(
            R.id.toggle_ai_integration,
            PlusFeatureManager.FEATURE_AI_INTEGRATION,
            "AI Integration",
            "Claude & Gemini AI providers, command analysis, code generation, and AI-powered suggestions."
        );

        // Voice Input
        bindToggle(
            R.id.toggle_voice_input,
            PlusFeatureManager.FEATURE_VOICE_INPUT,
            "Voice Input",
            "Speech-to-command via microphone. Triple-tap or use the mic button to speak commands."
        );

        // Gesture Controls
        bindToggle(
            R.id.toggle_gesture_controls,
            PlusFeatureManager.FEATURE_GESTURE_CONTROLS,
            "Gesture Controls",
            "Shake-to-clear, swipe navigation between tabs, and custom touch gestures."
        );

        // Project Insights
        bindToggle(
            R.id.toggle_project_insights,
            PlusFeatureManager.FEATURE_PROJECT_INSIGHTS,
            "Project Insights",
            "Auto-detect project type, show framework info, dependencies, and AI recommendations."
        );

        // Quick Settings Panel
        bindToggle(
            R.id.toggle_quick_settings_panel,
            PlusFeatureManager.FEATURE_QUICK_SETTINGS_PANEL,
            "Quick Settings Panel",
            "Floating panel with quick access to common settings like privacy mode and AI model."
        );

        // Dynamic Colors (Material You)
        bindToggle(
            R.id.toggle_dynamic_colors,
            PlusFeatureManager.FEATURE_DYNAMIC_COLORS,
            "Dynamic Colors (Material You)",
            "Adapt app colors to your wallpaper using Android 12+ Material You theming."
        );

        // Onboarding
        bindToggle(
            R.id.toggle_onboarding,
            PlusFeatureManager.FEATURE_ONBOARDING,
            "Onboarding Tutorial",
            "First-launch tutorial introducing Plus features and gestures to new users."
        );

        // Multi-Tab
        bindToggle(
            R.id.toggle_multi_tab,
            PlusFeatureManager.FEATURE_MULTI_TAB,
            "Multi-Tab Terminal",
            "Run up to 8 terminal tabs with independent sessions, swipe to switch."
        );

        // Biometric Protection
        bindToggle(
            R.id.toggle_biometric_protection,
            PlusFeatureManager.FEATURE_BIOMETRIC_PROTECTION,
            "Biometric Protection",
            "Require fingerprint or face unlock to view/edit API keys in settings."
        );

        // Plugin System
        bindToggle(
            R.id.toggle_plugin_system,
            PlusFeatureManager.FEATURE_PLUGIN_SYSTEM,
            "Plugin System",
            "Load and manage plugins for extended functionality (Claude integration, auto-save, etc.)."
        );
    }

    private void bindToggle(int viewId, String featureKey, String title, String description) {
        LinearLayout container = findViewById(viewId);
        if (container == null) return;

        SwitchMaterial toggle = container.findViewById(R.id.feature_toggle);
        TextView titleView = container.findViewById(R.id.feature_title);
        TextView descView = container.findViewById(R.id.feature_description);

        if (titleView != null) titleView.setText(title);
        if (descView != null) descView.setText(description);

        if (toggle != null) {
            toggle.setChecked(featureManager.isFeatureEnabled(featureKey));
            toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                featureManager.setFeatureEnabled(featureKey, isChecked);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
