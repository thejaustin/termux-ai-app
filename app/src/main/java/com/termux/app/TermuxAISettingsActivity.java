package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.termux.ai.R;

public class TermuxAISettingsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "termux_ai_prefs";
    private static final String PREF_AI_PROVIDER = "ai_provider";
    private static final String PREF_GEMINI_API_KEY = "gemini_api_key";
    private static final String PREF_CLAUDE_API_KEY = "auth_token"; // Reusing existing

    private RadioGroup providerGroup;
    private RadioButton claudeButton;
    private RadioButton geminiButton;
    private EditText apiKeyInput;
    private Button saveButton;
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
        saveButton = findViewById(R.id.save_button);

        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());
        
        providerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateApiKeyHint(checkedId);
        });
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

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_AI_PROVIDER, provider);
        
        if ("gemini".equals(provider)) {
            editor.putString(PREF_GEMINI_API_KEY, apiKey);
        } else {
            editor.putString(PREF_CLAUDE_API_KEY, apiKey);
        }
        
        editor.apply();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}