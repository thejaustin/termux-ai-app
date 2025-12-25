package com.termux.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.ai.R;

/**
 * Quick Settings Panel for Termux AI
 * A sliding panel that provides quick access to common settings
 */
public class QuickSettingsPanel extends LinearLayout {
    
    private SwitchMaterial switchAutoSuggestions;
    private SwitchMaterial switchGboardAutocomplete;
    private SwitchMaterial switchDynamicColors;
    private SwitchMaterial switchPrivacyMode;
    private SwitchMaterial switchLocalProcessing;
    private Spinner spinnerAiModel;
    private Button btnSaveSettings;
    private Button btnResetSettings;
    private Button btnClosePanel;
    
    private QuickSettingsPanelCallback callback;

    public interface QuickSettingsPanelCallback {
        void onSettingsApplied();
        void onPanelClosed();
    }

    public QuickSettingsPanel(Context context) {
        super(context);
        init(context);
    }

    public QuickSettingsPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuickSettingsPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        
        // Inflate the quick settings panel layout
        LayoutInflater.from(context).inflate(R.layout.quick_settings_panel, this, true);
        
        // Find views
        switchAutoSuggestions = findViewById(R.id.switch_auto_suggestions);
        switchGboardAutocomplete = findViewById(R.id.switch_gboard_autocomplete);
        switchDynamicColors = findViewById(R.id.switch_dynamic_colors);
        switchPrivacyMode = findViewById(R.id.switch_privacy_mode);
        switchLocalProcessing = findViewById(R.id.switch_local_processing);
        spinnerAiModel = findViewById(R.id.spinner_ai_model);
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        btnResetSettings = findViewById(R.id.btn_reset_settings);
        btnClosePanel = findViewById(R.id.btn_close_panel);
        
        setupListeners();
    }
    
    private void setupListeners() {
        btnSaveSettings.setOnClickListener(v -> {
            saveSettings();
            if (callback != null) callback.onSettingsApplied();
        });
        
        btnResetSettings.setOnClickListener(v -> resetSettings());
        
        btnClosePanel.setOnClickListener(v -> {
            if (callback != null) callback.onPanelClosed();
        });
    }
    
    private void saveSettings() {
        // Save the settings to SharedPreferences
        // This would typically interact with the main activity's settings
        Toast.makeText(getContext(), "Settings saved", Toast.LENGTH_SHORT).show();
    }
    
    private void resetSettings() {
        // Reset to default settings
        switchAutoSuggestions.setChecked(true);
        switchGboardAutocomplete.setChecked(true);
        switchDynamicColors.setChecked(false);
        switchPrivacyMode.setChecked(false);
        switchLocalProcessing.setChecked(false);
        
        // Reset spinner to default selection
        spinnerAiModel.setSelection(0);
        
        Toast.makeText(getContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show();
    }
    
    public void setCallback(QuickSettingsPanelCallback callback) {
        this.callback = callback;
    }
}