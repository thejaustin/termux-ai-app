package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.termux.ai.R;

public class TermuxAISettingsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "termux_ai_prefs";
    private static final String PREF_AI_PROVIDER = "ai_provider";
    private static final String PREF_GEMINI_API_KEY = "gemini_api_key";
    private static final String PREF_CLAUDE_API_KEY = "auth_token";
    private static final String PREF_DYNAMIC_COLORS = "dynamic_colors_enabled";
    private static final String PREF_CLAUDE_MODEL = "claude_model";
    private static final String PREF_TOKEN_LIMIT = "token_limit";
    private static final String PREF_AUTO_SUGGESTIONS = "auto_suggestions_enabled";
    private static final String PREF_COMMAND_FILTERING = "command_filtering_enabled";
    private static final String PREF_LOCAL_PROCESSING = "local_processing_enabled";

    private RadioGroup providerGroup;
    private RadioButton claudeButton;
    private RadioButton geminiButton;
    private EditText apiKeyInput;
    private Spinner modelSpinner;
    private EditText tokenLimitInput;
    private Button saveButton;
    private Switch dynamicColorsSwitch;
    private Switch autoSuggestionsSwitch;
    private Switch commandFilteringSwitch;
    private Switch localProcessingSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        providerGroup = findViewById(R.id.provider_group);
        claudeButton = findViewById(R.id.provider_claude);
        geminiButton = findViewById(R.id.provider_gemini);
        apiKeyInput = findViewById(R.id.api_key_input);
        modelSpinner = findViewById(R.id.model_spinner);
        tokenLimitInput = findViewById(R.id.token_limit_input);
        saveButton = findViewById(R.id.save_button);
        dynamicColorsSwitch = findViewById(R.id.dynamic_colors_switch);
        autoSuggestionsSwitch = findViewById(R.id.auto_suggestions_switch);
        commandFilteringSwitch = findViewById(R.id.command_filtering_switch);
        localProcessingSwitch = findViewById(R.id.local_processing_switch);

        setupModelSpinner();
        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());

        providerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateApiKeyHint(checkedId);
        });
    }

    private void setupModelSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.claude_models, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }

    private void loadSettings() {
        String provider = prefs.getString(PREF_AI_PROVIDER, "claude");
        if ("gemini".equals(provider)) {
            geminiButton.setChecked(true);
            apiKeyInput.setText(prefs.getString(PREF_GEMINI_API_KEY, ""));
        } else {
            claudeButton.setChecked(true);
            apiKeyInput.setText(prefs.getString(PREF_CLAUDE_API_KEY, ""));
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

        boolean dynamicColors = prefs.getBoolean(PREF_DYNAMIC_COLORS, false);
        dynamicColorsSwitch.setChecked(dynamicColors);

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

    private void saveSettings() {
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

        boolean dynamicColors = dynamicColorsSwitch.isChecked();
        boolean autoSuggestions = autoSuggestionsSwitch.isChecked();
        boolean commandFiltering = commandFilteringSwitch.isChecked();
        boolean localProcessing = localProcessingSwitch.isChecked();

        boolean dynamicColorsChanged = dynamicColors != prefs.getBoolean(PREF_DYNAMIC_COLORS, false);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_AI_PROVIDER, provider);

        if ("gemini".equals(provider)) {
            editor.putString(PREF_GEMINI_API_KEY, apiKey);
        } else {
            editor.putString(PREF_CLAUDE_API_KEY, apiKey);
        }

        editor.putString(PREF_CLAUDE_MODEL, model);
        editor.putString(PREF_TOKEN_LIMIT, String.valueOf(tokenLimit));
        editor.putBoolean(PREF_DYNAMIC_COLORS, dynamicColors);
        editor.putBoolean(PREF_AUTO_SUGGESTIONS, autoSuggestions);
        editor.putBoolean(PREF_COMMAND_FILTERING, commandFiltering);
        editor.putBoolean(PREF_LOCAL_PROCESSING, localProcessing);

        editor.apply();

        if (dynamicColorsChanged) {
             Toast.makeText(this, "Settings saved. Restart app for theme changes.", Toast.LENGTH_LONG).show();
        } else {
             Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}