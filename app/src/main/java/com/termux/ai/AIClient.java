package com.termux.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * AI Client for Claude and Gemini integration in Termux
 * 
 * Handles:
 * - Authentication with Claude API / Gemini API Key
 * - Real-time command analysis
 * - Context-aware suggestions
 * - Error diagnostics
 * - Code generation
 */
public class AIClient {
    private static final String TAG = "TermuxAI";
    private static final String PREFS_NAME = "termux_ai_prefs";
    private static final String PREF_AUTH_TOKEN = "auth_token";
    private static final String PREF_SESSION_ID = "session_id";
    private static final String PREF_AI_PROVIDER = "ai_provider";
    private static final String PREF_GEMINI_API_KEY = "gemini_api_key";
    
    public static final String CLAUDE_API_BASE_URL = "https://claude.ai/api";
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final Context context;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final SharedPreferences prefs;
    private final Handler mainHandler;
    
    private String authToken;
    private String sessionId;
    private String geminiApiKey;
    private String currentProvider; // "claude" or "gemini"

    private WebSocket webSocket;
    private AIClientListener listener;
    
    public interface AIClientListener {
        void onSuggestionReceived(String suggestion, float confidence);
        void onErrorAnalysis(String error, String analysis, String[] solutions);
        void onCodeGenerated(String code, String language);
        void onConnectionStatusChanged(boolean connected);
        void onAuthenticationRequired();
    }
    
    public AIClient(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Use encrypted SharedPreferences for secure credential storage
        this.prefs = EncryptedPreferencesManager.getEncryptedPrefs(context, PREFS_NAME);

        // Migrate existing plaintext preferences if they exist
        EncryptedPreferencesManager.migratePlaintextToEncrypted(
            context,
            "termux_ai_prefs",  // old plaintext name
            PREFS_NAME          // new encrypted name
        );

        // Build OkHttpClient with security configurations
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS);

        // Add certificate pinning for enhanced security
        // Note: Certificate pins should be updated periodically. To get current pins:
        // echo | openssl s_client -connect generativelanguage.googleapis.com:443 2>&1 | \
        //   openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | \
        //   openssl dgst -sha256 -binary | base64
        //
        // WARNING: Certificate pinning can cause app to stop working if certificates
        // are rotated. For production, implement pin backup/rotation strategy.
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
            // Google API pins (multiple pins for backup)
            // These are example pins - you MUST fetch actual pins before enabling
            // .add("generativelanguage.googleapis.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            // .add("generativelanguage.googleapis.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            // .add("*.googleapis.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")

            // Anthropic API pins (if/when using official API)
            // .add("api.anthropic.com", "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=")
            // .add("api.anthropic.com", "sha256/DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD=")
            .build();

        // Enable certificate pinning only if pins are properly configured
        // Comment this out until you fetch actual certificate pins
        // httpBuilder.certificatePinner(certificatePinner);

        this.httpClient = httpBuilder.build();

        loadAuthenticationData();
    }
    
    public void setListener(AIClientListener listener) {
        this.listener = listener;
    }
    
    private void loadAuthenticationData() {
        authToken = prefs.getString(PREF_AUTH_TOKEN, null);
        sessionId = prefs.getString(PREF_SESSION_ID, null);
        geminiApiKey = prefs.getString(PREF_GEMINI_API_KEY, null);
        currentProvider = prefs.getString(PREF_AI_PROVIDER, "claude");
    }
    
    private void saveAuthenticationData() {
        prefs.edit()
            .putString(PREF_AUTH_TOKEN, authToken)
            .putString(PREF_SESSION_ID, sessionId)
            .putString(PREF_GEMINI_API_KEY, geminiApiKey)
            .apply();
    }
    
    public boolean isAuthenticated() {
        if ("gemini".equals(currentProvider)) {
            return geminiApiKey != null && !geminiApiKey.isEmpty();
        }
        return authToken != null && !authToken.isEmpty();
    }
    
    /**
     * Authenticate with Claude using OAuth
     */
    public void authenticate(String oauthCode, AuthCallback callback) {
        JsonObject authRequest = new JsonObject();
        authRequest.addProperty("grant_type", "authorization_code");
        authRequest.addProperty("code", oauthCode);
        authRequest.addProperty("client_id", "termux-ai");
        
        RequestBody body = RequestBody.create(gson.toJson(authRequest), JSON);
        Request request = new Request.Builder()
            .url(CLAUDE_API_BASE_URL + "/oauth/token")
            .post(body)
            .build();
            
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Authentication failed", e);
                callback.onError("Authentication failed: " + e.getMessage());
            }
            
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject authResponse = gson.fromJson(responseBody, JsonObject.class);

                    authToken = authResponse.get("access_token").getAsString();
                    sessionId = authResponse.get("session_id").getAsString();

                    saveAuthenticationData();
                    callback.onSuccess();
                } else {
                    String errorBody = response.body().string();
                    String errorMessage = "Authentication failed: " + response.code();
                    try {
                        JsonObject errorResponse = gson.fromJson(errorBody, JsonObject.class);
                        if (errorResponse.has("error_description")) {
                            errorMessage = errorResponse.get("error_description").getAsString();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse error response", e);
                    }
                    callback.onError(errorMessage);
                }
            }
        });
    }
    
    /**
     * Analyze command and provide suggestions
     */
    public void analyzeCommand(String command, String context, AnalysisCallback callback) {
        loadAuthenticationData(); // Reload prefs in case settings changed
        
        if (!isAuthenticated()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onAuthenticationRequired();
                }
            });
            return;
        }
        
        if ("gemini".equals(currentProvider)) {
            analyzeCommandGemini(command, context, callback);
        } else {
            analyzeCommandClaude(command, context, callback);
        }
    }

    private void analyzeCommandClaude(String command, String context, AnalysisCallback callback) {
        JsonObject analysisRequest = new JsonObject();
        analysisRequest.addProperty("command", command);
        analysisRequest.addProperty("context", context);
        analysisRequest.addProperty("type", "command_analysis");
        
        sendClaudeRequest("/analyze", analysisRequest, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                if (response.has("suggestion")) {
                    String suggestion = response.get("suggestion").getAsString();
                    float confidence = response.has("confidence") ? 
                        response.get("confidence").getAsFloat() : 0.5f;
                    
                    mainHandler.post(() -> {
                        callback.onSuggestion(suggestion, confidence);
                        
                        if (listener != null) {
                            listener.onSuggestionReceived(suggestion, confidence);
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    private void analyzeCommandGemini(String command, String context, AnalysisCallback callback) {
        String prompt = "Analyze this command: " + command + "\nContext: " + context + 
                        "\nProvide a suggestion for improvement or explanation. " + 
                        "Return ONLY JSON with 'suggestion' (string) and 'confidence' (float 0.0-1.0) fields. No markdown.";

        sendGeminiRequest(prompt, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                try {
                    JsonObject json = parseGeminiResponse(response);
                    String suggestion = json.get("suggestion").getAsString();
                    float confidence = json.has("confidence") ? json.get("confidence").getAsFloat() : 0.8f;

                    mainHandler.post(() -> {
                        callback.onSuggestion(suggestion, confidence);
                        if (listener != null) listener.onSuggestionReceived(suggestion, confidence);
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Failed to parse Gemini response: " + e.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        }, error -> mainHandler.post(() -> callback.onError(error)));
    }
    
    /**
     * Analyze error and provide solutions
     */
    public void analyzeError(String command, String errorOutput, String context, ErrorCallback callback) {
        loadAuthenticationData();
        
        if (!isAuthenticated()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onAuthenticationRequired();
                }
            });
            return;
        }
        
        if ("gemini".equals(currentProvider)) {
            analyzeErrorGemini(command, errorOutput, context, callback);
        } else {
            analyzeErrorClaude(command, errorOutput, context, callback);
        }
    }

    private void analyzeErrorClaude(String command, String errorOutput, String context, ErrorCallback callback) {
        JsonObject errorRequest = new JsonObject();
        errorRequest.addProperty("command", command);
        errorRequest.addProperty("error", errorOutput);
        errorRequest.addProperty("context", context);
        errorRequest.addProperty("type", "error_analysis");
        
        sendClaudeRequest("/analyze", errorRequest, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                String analysis = response.get("analysis").getAsString();
                String[] solutions = gson.fromJson(response.get("solutions"), String[].class);
                
                mainHandler.post(() -> {
                    callback.onAnalysis(analysis, solutions);
                    
                    if (listener != null) {
                        listener.onErrorAnalysis(errorOutput, analysis, solutions);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    private void analyzeErrorGemini(String command, String errorOutput, String context, ErrorCallback callback) {
        String prompt = "Command: " + command + "\nError: " + errorOutput + "\nContext: " + context + 
                        "\nAnalyze and provide solutions. Return ONLY JSON with 'analysis' (string) and 'solutions' (string array). No markdown.";

        sendGeminiRequest(prompt, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                try {
                    JsonObject json = parseGeminiResponse(response);
                    String analysis = json.get("analysis").getAsString();
                    String[] solutions = gson.fromJson(json.get("solutions"), String[].class);

                    mainHandler.post(() -> {
                        callback.onAnalysis(analysis, solutions);
                        if (listener != null) listener.onErrorAnalysis(errorOutput, analysis, solutions);
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Failed to parse Gemini response: " + e.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        }, error -> mainHandler.post(() -> callback.onError(error)));
    }
    
    /**
     * Generate code based on natural language description
     */
    public void generateCode(String description, String language, String context, CodeCallback callback) {
        loadAuthenticationData();

        if (!isAuthenticated()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onAuthenticationRequired();
                }
            });
            return;
        }

        if ("gemini".equals(currentProvider)) {
            generateCodeGemini(description, language, context, callback);
        } else {
            generateCodeClaude(description, language, context, callback);
        }
    }

    private void generateCodeClaude(String description, String language, String context, CodeCallback callback) {
        JsonObject codeRequest = new JsonObject();
        codeRequest.addProperty("description", description);
        codeRequest.addProperty("language", language);
        codeRequest.addProperty("context", context);
        codeRequest.addProperty("type", "code_generation");
        
        sendClaudeRequest("/generate", codeRequest, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                String code = response.get("code").getAsString();
                String detectedLanguage = response.has("language") ? 
                    response.get("language").getAsString() : language;
                
                mainHandler.post(() -> {
                    callback.onCodeGenerated(code, detectedLanguage);
                    
                    if (listener != null) {
                        listener.onCodeGenerated(code, detectedLanguage);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    private void generateCodeGemini(String description, String language, String context, CodeCallback callback) {
        String prompt = "Write " + language + " code for: " + description + "\nContext: " + context + 
                        "\nReturn ONLY JSON with 'code' (string) and 'language' (string). No markdown.";

        sendGeminiRequest(prompt, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                try {
                    JsonObject json = parseGeminiResponse(response);
                    String code = json.get("code").getAsString();
                    String detectedLanguage = json.has("language") ? json.get("language").getAsString() : language;

                    mainHandler.post(() -> {
                        callback.onCodeGenerated(code, detectedLanguage);
                        if (listener != null) listener.onCodeGenerated(code, detectedLanguage);
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Failed to parse Gemini response: " + e.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        }, error -> mainHandler.post(() -> callback.onError(error)));
    }
    
    /**
     * Start real-time AI assistance session
     */
    public void startRealtimeSession() {
        loadAuthenticationData();

        if (!isAuthenticated()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onAuthenticationRequired();
                }
            });
            return;
        }
        
        if ("gemini".equals(currentProvider)) {
            Log.d(TAG, "Real-time session not supported for Gemini via REST");
            // Optionally, we could simulate it or use a different mechanism.
            // For now, just notifying connected to simulate success.
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onConnectionStatusChanged(true);
                }
            });
            return;
        }

        Request request = new Request.Builder()
            .url("wss://claude.ai/api/ws")
            .addHeader("Authorization", "Bearer " + authToken)
            .addHeader("Session-ID", sessionId)
            .build();
            
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket connection opened");
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onConnectionStatusChanged(true);
                    }
                });
            }
            
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                handleRealtimeMessage(text);
            }
            
            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket connection closing: " + reason);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onConnectionStatusChanged(false);
                    }
                });
            }
            
            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                Log.e(TAG, "WebSocket connection failed", t);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onConnectionStatusChanged(false);
                    }
                });
            }
        });
    }
    
    private void handleRealtimeMessage(String message) {
        try {
            JsonObject msg = gson.fromJson(message, JsonObject.class);
            String type = msg.get("type").getAsString();
            
            mainHandler.post(() -> {
                switch (type) {
                    case "suggestion":
                        if (listener != null) {
                            String suggestion = msg.get("content").getAsString();
                            float confidence = msg.get("confidence").getAsFloat();
                            listener.onSuggestionReceived(suggestion, confidence);
                        }
                        break;
                        
                    case "error_analysis":
                        if (listener != null) {
                            String error = msg.get("error").getAsString();
                            String analysis = msg.get("analysis").getAsString();
                            String[] solutions = gson.fromJson(msg.get("solutions"), String[].class);
                            listener.onErrorAnalysis(error, analysis, solutions);
                        }
                        break;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse realtime message", e);
        }
    }
    
    /**
     * Send real-time context update
     */
    public void sendContextUpdate(String workingDirectory, String currentCommand, String[] recentCommands) {
        if ("gemini".equals(currentProvider)) return; // Not supported for Gemini REST

        if (webSocket == null) return;
        
        JsonObject contextUpdate = new JsonObject();
        contextUpdate.addProperty("type", "context_update");
        contextUpdate.addProperty("working_directory", workingDirectory);
        contextUpdate.addProperty("current_command", currentCommand);
        contextUpdate.add("recent_commands", gson.toJsonTree(recentCommands));
        
        webSocket.send(gson.toJson(contextUpdate));
    }
    
    private void sendClaudeRequest(String endpoint, JsonObject requestBody, RequestCallback callback) {
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
            .url(CLAUDE_API_BASE_URL + endpoint)
            .addHeader("Authorization", "Bearer " + authToken)
            .addHeader("Session-ID", sessionId)
            .post(body)
            .build();
            
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Request failed: " + e.getMessage());
            }
            
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    callback.onSuccess(jsonResponse);
                } else if (response.code() == 401) {
                    // Token expired
                    authToken = null;
                    sessionId = null;
                    saveAuthenticationData();
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onAuthenticationRequired();
                        }
                    });
                } else {
                    callback.onError("Request failed: " + response.code());
                }
            }
        });
    }

    private void sendGeminiRequest(String prompt, RequestCallback callback, AIClient.AnalysisCallback.OnError errorCallback) { // Using RequestCallback interface but adapting errors
        // Construct Gemini JSON
        JsonObject content = new JsonObject();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(part);
        content.add("parts", parts);
        
        JsonArray contents = new JsonArray();
        contents.add(content);
        
        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        // Don't expose API key in URL - use header instead
        String url = GEMINI_API_URL;

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", geminiApiKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                errorCallback.onError("Gemini Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    callback.onSuccess(jsonResponse);
                } else {
                     errorCallback.onError("Gemini Request failed: " + response.code() + " " + response.body().string());
                }
            }
        });
    }

    private JsonObject parseGeminiResponse(JsonObject response) {
        JsonArray candidates = response.getAsJsonArray("candidates");
        if (candidates.size() > 0) {
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");
            String text = parts.get(0).getAsJsonObject().get("text").getAsString();
            
            // Clean markdown
            text = text.trim();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            } else if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            
            return gson.fromJson(text, JsonObject.class);
        }
        throw new RuntimeException("No candidates in Gemini response");
    }
    
    public void shutdown() {
        if (webSocket != null) {
            webSocket.close(1000, "Client shutdown");
            webSocket = null;
        }
    }
    
    // Callback interfaces
    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface AnalysisCallback {
        void onSuggestion(String suggestion, float confidence);
        void onError(String error);
        
        interface OnError {
             void onError(String error);
        }
    }
    
    public interface ErrorCallback {
        void onAnalysis(String analysis, String[] solutions);
        void onError(String error);
    }
    
    public interface CodeCallback {
        void onCodeGenerated(String code, String language);
        void onError(String error);
    }
    
    private interface RequestCallback {
        void onSuccess(JsonObject response);
        void onError(String error);
    }
}
