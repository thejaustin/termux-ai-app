package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.ai.R;
import com.termux.plus.TermuxPlusApplication;

public class TermuxPlusSettingsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "termux_plus_prefs";
    private static final String PREF_AI_PROVIDER = "ai_provider";
    private static final String PREF_GEMINI_API_KEY = "gemini_api_key";
    private static final String PREF_CLAUDE_API_KEY = "auth_token";
    private static final String PREF_DYNAMIC_COLORS = "dynamic_colors_enabled";
    private static final String PREF_CLAUDE_MODEL = "claude_model";
    private static final String PREF_TOKEN_LIMIT = "token_limit";
    private static final String PREF_AUTO_SUGGESTIONS = "auto_suggestions_enabled";
    private static final String PREF_COMMAND_FILTERING = "command_filtering_enabled";
    private static final String PREF_LOCAL_PROCESSING = "local_processing_enabled";

    // Theme settings
    private SwitchMaterial dynamicColorsSwitch;
    private TextView dynamicColorsStatus;
    private RadioGroup themeModeGroup;
    private RadioButton themeAuto, themeLight, themeDark;
    private RadioGroup themeStyleGroup;
    private RadioButton styleExpressive, styleVibrant, styleTonal;
    private TextView themeStyleLabel;

    // AI Provider settings
    private RadioGroup providerGroup;
    private RadioButton claudeButton;
    private RadioButton geminiButton;
    private EditText apiKeyInput;
    private View btnBiometricAuth;
    private Spinner modelSpinner;
    private EditText tokenLimitInput;

    // Feature toggles
    private SwitchMaterial autoSuggestionsSwitch;
    private SwitchMaterial commandFilteringSwitch;
    private SwitchMaterial localProcessingSwitch;

    private Button saveButton;
    private SharedPreferences prefs;
    private TermuxPlusApplication app;
    private boolean isAuthorized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Material You Dynamic Colors
        com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Use encrypted preferences for all settings to ensure security
        prefs = com.termux.ai.EncryptedPreferencesManager.getEncryptedPrefs(this, PREFS_NAME);
        app = (TermuxPlusApplication) getApplication();

        // Theme settings
        dynamicColorsSwitch = findViewById(R.id.dynamic_colors_switch);
        dynamicColorsStatus = findViewById(R.id.dynamic_colors_status);
        themeModeGroup = findViewById(R.id.theme_mode_group);
        themeAuto = findViewById(R.id.theme_auto);
        themeLight = findViewById(R.id.theme_light);
        themeDark = findViewById(R.id.theme_dark);
        themeStyleGroup = findViewById(R.id.theme_style_group);
        styleExpressive = findViewById(R.id.style_expressive);
        styleVibrant = findViewById(R.id.style_vibrant);
        styleTonal = findViewById(R.id.style_tonal);
        themeStyleLabel = findViewById(R.id.theme_style_label);

        // AI Provider settings
        providerGroup = findViewById(R.id.provider_group);
        claudeButton = findViewById(R.id.provider_claude);
        geminiButton = findViewById(R.id.provider_gemini);
        apiKeyInput = findViewById(R.id.api_key_input);
        btnBiometricAuth = findViewById(R.id.btn_biometric_auth);
        modelSpinner = findViewById(R.id.model_spinner);
        tokenLimitInput = findViewById(R.id.token_limit_input);

        // Initial security state: mask API key and disable editing until authorized
        apiKeyInput.setEnabled(false);
        setupBiometricAuth();

        // Feature toggles
        autoSuggestionsSwitch = findViewById(R.id.auto_suggestions_switch);
        commandFilteringSwitch = findViewById(R.id.command_filtering_switch);
        localProcessingSwitch = findViewById(R.id.local_processing_switch);
        Button btnClearData = findViewById(R.id.btn_clear_ai_data);

        saveButton = findViewById(R.id.save_button);
        Button btnManagePlugins = findViewById(R.id.btn_manage_plugins);
        btnManagePlugins.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, PluginSettingsActivity.class);
            startActivity(intent);
        });

        setupModelSpinner();
        setupThemeListeners();
        loadSettings();
        updateDynamicColorsStatus();

        saveButton.setOnClickListener(v -> saveSettings());

        btnClearData.setOnClickListener(v -> {
            ErrorDialogHelper.showConfirmation(this, 
                "Clear All AI Data?", 
                "This will permanently delete all stored API keys, session tokens, and locally cached AI interactions.",
                () -> {
                    clearAllAIData();
                });
        });

        providerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateApiKeyHint(checkedId);
            // Load the appropriate API key when switching providers
            loadApiKeyForProvider(checkedId);
        });
    }

    private void setupBiometricAuth() {
        if (BiometricHelper.isBiometricAvailable(this)) {
            btnBiometricAuth.setVisibility(View.VISIBLE);
            btnBiometricAuth.setOnClickListener(v -> {
                BiometricHelper.authenticate(this, 
                    "Authorize AI Settings", 
                    "Authenticate to view or edit API keys",
                    new BiometricHelper.BiometricCallback() {
                        @Override
                        public void onAuthenticationSuccess() {
                            isAuthorized = true;
                            apiKeyInput.setEnabled(true);
                            btnBiometricAuth.setVisibility(View.GONE);
                            loadSettings(); // Populate keys now that we are authorized
                            Toast.makeText(TermuxPlusSettingsActivity.this, "Authorized", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(String error) {
                            Toast.makeText(TermuxPlusSettingsActivity.this, "Auth error: " + error, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onAuthenticationCancelled() {
                            // Do nothing
                        }
                    });
            });
        } else {
            // Biometrics not available, allow access by default but log a warning
            btnBiometricAuth.setVisibility(View.GONE);
            apiKeyInput.setEnabled(true);
            isAuthorized = true;
        }
    }

    private void clearAllAIData() {
        // Wipe all preferences
        prefs.edit().clear().apply();
        
        // Clear input fields
        apiKeyInput.setText("");
        tokenLimitInput.setText("100000");
        
        // Reset toggles
        autoSuggestionsSwitch.setChecked(true);
        commandFilteringSwitch.setChecked(true);
        localProcessingSwitch.setChecked(false);
        
        Toast.makeText(this, "All AI data cleared successfully", Toast.LENGTH_SHORT).show();
        
        // Optionally restart to ensure clean state in AIClient
        finish();
    }

    private void setupThemeListeners() {
        // Dynamic colors toggle listener
        dynamicColorsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeStyleGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            themeStyleLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateDynamicColorsStatus();
        });
    }

    private void updateDynamicColorsStatus() {
        if (app.areDynamicColorsAvailable()) {
            if (dynamicColorsSwitch.isChecked()) {
                dynamicColorsStatus.setText("✓ Dynamic colors are active and adapting to your wallpaper");
            } else {
                dynamicColorsStatus.setText("Dynamic colors are available. Enable to personalize your app!");
            }
        } else {
            dynamicColorsStatus.setText("⚠ Dynamic colors require Android 12+ (API 31)");
            dynamicColorsSwitch.setEnabled(false);
        }
    }

    private void setupModelSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.claude_models, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }

    private void loadSettings() {
        // Load theme settings
        boolean dynamicColors = app.isDynamicColorsEnabled();
        dynamicColorsSwitch.setChecked(dynamicColors);
        themeStyleGroup.setVisibility(dynamicColors ? View.VISIBLE : View.GONE);
        themeStyleLabel.setVisibility(dynamicColors ? View.VISIBLE : View.GONE);

        int nightMode = app.getNightMode();
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeDark.setChecked(true);
        } else if (nightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeLight.setChecked(true);
        } else {
            themeAuto.setChecked(true);
        }

        String themeStyle = app.getThemeStyle();
        if ("vibrant".equals(themeStyle)) {
            styleVibrant.setChecked(true);
        } else if ("tonal".equals(themeStyle)) {
            styleTonal.setChecked(true);
        } else {
            styleExpressive.setChecked(true);
        }

        // Load AI provider settings
        String provider = prefs.getString(PREF_AI_PROVIDER, "claude");
        if ("gemini".equals(provider)) {
            geminiButton.setChecked(true);
            if (isAuthorized || !BiometricHelper.isBiometricAvailable(this)) {
                apiKeyInput.setText(prefs.getString(PREF_GEMINI_API_KEY, ""));
            } else {
                apiKeyInput.setText("");
            }
        } else {
            claudeButton.setChecked(true);
            if (isAuthorized || !BiometricHelper.isBiometricAvailable(this)) {
                apiKeyInput.setText(prefs.getString(PREF_CLAUDE_API_KEY, ""));
            } else {
                apiKeyInput.setText("");
            }
        }

        String model = prefs.getString(PREF_CLAUDE_MODEL, "claude-3-5-sonnet-20241022");
        int spinnerPosition = 0;
        for (int i = 0; i < modelSpinner.getCount(); i++) {
            if (modelSpinner.getItemAtPosition(i).toString().equals(model)) {
                spinnerPosition = i;
                break;
            }
        }
        modelSpinner.setSelection(spinnerPosition);

        String tokenLimit = prefs.getString(PREF_TOKEN_LIMIT, "100000");
        tokenLimitInput.setText(tokenLimit);

        // Load feature toggles
        boolean autoSuggestions = prefs.getBoolean(PREF_AUTO_SUGGESTIONS, true);
        autoSuggestionsSwitch.setChecked(autoSuggestions);

        boolean commandFiltering = prefs.getBoolean(PREF_COMMAND_FILTERING, true);
        commandFilteringSwitch.setChecked(commandFiltering);

        boolean localProcessing = prefs.getBoolean(PREF_LOCAL_PROCESSING, false);
        localProcessingSwitch.setChecked(localProcessing);

        updateApiKeyHint(providerGroup.getCheckedRadioButtonId());
    }

    private void updateApiKeyHint(int checkedId) {
        if (checkedId == R.id.provider_gemini) {
            apiKeyInput.setHint("Enter Gemini API Key");
        } else {
            apiKeyInput.setHint("Enter Claude API Key (or Token)");
        }
    }

    private void loadApiKeyForProvider(int checkedId) {
        // Only load/save if authorized or biometrics not available
        if (!isAuthorized && BiometricHelper.isBiometricAvailable(this)) {
            apiKeyInput.setText("");
            return;
        }

        // Save current API key before switching
        String currentKey = apiKeyInput.getText().toString().trim();
        if (!currentKey.isEmpty()) {
            if (checkedId == R.id.provider_gemini) {
                // Switching to Gemini, save Claude key first
                prefs.edit().putString(PREF_CLAUDE_API_KEY, currentKey).apply();
            } else {
                // Switching to Claude, save Gemini key first
                prefs.edit().putString(PREF_GEMINI_API_KEY, currentKey).apply();
            }
        }

        // Load the API key for the newly selected provider
        if (checkedId == R.id.provider_gemini) {
            apiKeyInput.setText(prefs.getString(PREF_GEMINI_API_KEY, ""));
        } else {
            apiKeyInput.setText(prefs.getString(PREF_CLAUDE_API_KEY, ""));
        }
    }

    private void saveSettings() {
        // Save theme settings
        boolean dynamicColors = dynamicColorsSwitch.isChecked();
        boolean dynamicColorsChanged = dynamicColors != app.isDynamicColorsEnabled();

        int nightMode;
        if (themeDark.isChecked()) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (themeLight.isChecked()) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        boolean nightModeChanged = nightMode != app.getNightMode();

        String themeStyle;
        if (styleVibrant.isChecked()) {
            themeStyle = "vibrant";
        } else if (styleTonal.isChecked()) {
            themeStyle = "tonal";
        } else {
            themeStyle = "expressive";
        }
        boolean themeStyleChanged = !themeStyle.equals(app.getThemeStyle());

        // Save AI provider settings
        String provider = geminiButton.isChecked() ? "gemini" : "claude";
        String apiKey = apiKeyInput.getText().toString().trim();
        String model = modelSpinner.getSelectedItem().toString();
        String tokenLimitStr = tokenLimitInput.getText().toString().trim();
        int tokenLimit = 100000; // Default value
        try {
            tokenLimit = Integer.parseInt(tokenLimitStr);
        } catch (NumberFormatException e) {
            // Use default if parsing fails
        }

        // Save feature toggles
        boolean autoSuggestions = autoSuggestionsSwitch.isChecked();
        boolean commandFiltering = commandFilteringSwitch.isChecked();
        boolean localProcessing = localProcessingSwitch.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_AI_PROVIDER, provider);

        // Save both API keys to preserve them when switching providers
        if ("gemini".equals(provider)) {
            editor.putString(PREF_GEMINI_API_KEY, apiKey);
            // Keep existing Claude key if present
            if (!prefs.contains(PREF_CLAUDE_API_KEY) || prefs.getString(PREF_CLAUDE_API_KEY, "").isEmpty()) {
                // Don't overwrite existing Claude key
            }
        } else {
            editor.putString(PREF_CLAUDE_API_KEY, apiKey);
            // Keep existing Gemini key if present
            if (!prefs.contains(PREF_GEMINI_API_KEY) || prefs.getString(PREF_GEMINI_API_KEY, "").isEmpty()) {
                // Don't overwrite existing Gemini key
            }
        }

        editor.putString(PREF_CLAUDE_MODEL, model);
        editor.putString(PREF_TOKEN_LIMIT, String.valueOf(tokenLimit));
        editor.putBoolean(PREF_DYNAMIC_COLORS, dynamicColors);
        editor.putBoolean(PREF_AUTO_SUGGESTIONS, autoSuggestions);
        editor.putBoolean(PREF_COMMAND_FILTERING, commandFiltering);
        editor.putBoolean(PREF_LOCAL_PROCESSING, localProcessing);

        // Save theme settings to app
        app.setDynamicColorsEnabled(dynamicColors);
        app.setNightMode(nightMode);
        app.setThemeStyle(themeStyle);

        editor.apply();

        // Check if we need to restart the app for theme changes
        boolean needsRestart = dynamicColorsChanged || nightModeChanged || themeStyleChanged;

        if (needsRestart) {
            // Show appropriate message
            if (dynamicColorsChanged && dynamicColors) {
                Toast.makeText(this, "✨ Material You 3 enabled! App restarting...", Toast.LENGTH_SHORT).show();
            } else if (nightModeChanged) {
                String modeName = nightMode == AppCompatDelegate.MODE_NIGHT_YES ? "Dark" :
                                  nightMode == AppCompatDelegate.MODE_NIGHT_NO ? "Light" : "Auto";
                Toast.makeText(this, modeName + " mode activated! App restarting...", Toast.LENGTH_SHORT).show();
            } else if (themeStyleChanged) {
                Toast.makeText(this, themeStyle.substring(0, 1).toUpperCase() + themeStyle.substring(1) +
                              " style applied! App restarting...", Toast.LENGTH_SHORT).show();
            }

            // Apply night mode immediately
            if (nightModeChanged) {
                AppCompatDelegate.setDefaultNightMode(nightMode);
            }

            // Recreate the app with new theme
            finish();
            android.content.Intent intent = new android.content.Intent(this, com.termux.app.TabbedTerminalActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}