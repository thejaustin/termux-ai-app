package com.termux.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * AI Client for Claude integration in Termux
 * 
 * Handles:
 * - Authentication with Claude API
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
    
    public static final String API_BASE_URL = "https://claude.ai/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final Context context;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final SharedPreferences prefs;
    
    private String authToken;
    private String sessionId;
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
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
            
        loadAuthenticationData();
    }
    
    public void setListener(AIClientListener listener) {
        this.listener = listener;
    }
    
    private void loadAuthenticationData() {
        authToken = prefs.getString(PREF_AUTH_TOKEN, null);
        sessionId = prefs.getString(PREF_SESSION_ID, null);
    }
    
    private void saveAuthenticationData() {
        prefs.edit()
            .putString(PREF_AUTH_TOKEN, authToken)
            .putString(PREF_SESSION_ID, sessionId)
            .apply();
    }
    
    public boolean isAuthenticated() {
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
            .url(API_BASE_URL + "/oauth/token")
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
                    callback.onError("Authentication failed: " + response.code());
                }
            }
        });
    }
    
    /**
     * Analyze command and provide suggestions
     */
    public void analyzeCommand(String command, String context, AnalysisCallback callback) {
        if (!isAuthenticated()) {
            if (listener != null) {
                listener.onAuthenticationRequired();
            }
            return;
        }
        
        JsonObject analysisRequest = new JsonObject();
        analysisRequest.addProperty("command", command);
        analysisRequest.addProperty("context", context);
        analysisRequest.addProperty("type", "command_analysis");
        
        sendAIRequest("/analyze", analysisRequest, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                if (response.has("suggestion")) {
                    String suggestion = response.get("suggestion").getAsString();
                    float confidence = response.has("confidence") ? 
                        response.get("confidence").getAsFloat() : 0.5f;
                    
                    callback.onSuggestion(suggestion, confidence);
                    
                    if (listener != null) {
                        listener.onSuggestionReceived(suggestion, confidence);
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Analyze error and provide solutions
     */
    public void analyzeError(String command, String errorOutput, String context, ErrorCallback callback) {
        if (!isAuthenticated()) {
            if (listener != null) {
                listener.onAuthenticationRequired();
            }
            return;
        }
        
        JsonObject errorRequest = new JsonObject();
        errorRequest.addProperty("command", command);
        errorRequest.addProperty("error", errorOutput);
        errorRequest.addProperty("context", context);
        errorRequest.addProperty("type", "error_analysis");
        
        sendAIRequest("/analyze", errorRequest, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                String analysis = response.get("analysis").getAsString();
                String[] solutions = gson.fromJson(response.get("solutions"), String[].class);
                
                callback.onAnalysis(analysis, solutions);
                
                if (listener != null) {
                    listener.onErrorAnalysis(errorOutput, analysis, solutions);
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Generate code based on natural language description
     */
    public void generateCode(String description, String language, String context, CodeCallback callback) {
        if (!isAuthenticated()) {
            if (listener != null) {
                listener.onAuthenticationRequired();
            }
            return;
        }
        
        JsonObject codeRequest = new JsonObject();
        codeRequest.addProperty("description", description);
        codeRequest.addProperty("language", language);
        codeRequest.addProperty("context", context);
        codeRequest.addProperty("type", "code_generation");
        
        sendAIRequest("/generate", codeRequest, new RequestCallback() {
            @Override
            public void onSuccess(JsonObject response) {
                String code = response.get("code").getAsString();
                String detectedLanguage = response.has("language") ? 
                    response.get("language").getAsString() : language;
                
                callback.onCodeGenerated(code, detectedLanguage);
                
                if (listener != null) {
                    listener.onCodeGenerated(code, detectedLanguage);
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Start real-time AI assistance session
     */
    public void startRealtimeSession() {
        if (!isAuthenticated()) {
            if (listener != null) {
                listener.onAuthenticationRequired();
            }
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
                if (listener != null) {
                    listener.onConnectionStatusChanged(true);
                }
            }
            
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                handleRealtimeMessage(text);
            }
            
            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket connection closing: " + reason);
                if (listener != null) {
                    listener.onConnectionStatusChanged(false);
                }
            }
            
            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                Log.e(TAG, "WebSocket connection failed", t);
                if (listener != null) {
                    listener.onConnectionStatusChanged(false);
                }
            }
        });
    }
    
    private void handleRealtimeMessage(String message) {
        try {
            JsonObject msg = gson.fromJson(message, JsonObject.class);
            String type = msg.get("type").getAsString();
            
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
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse realtime message", e);
        }
    }
    
    /**
     * Send real-time context update
     */
    public void sendContextUpdate(String workingDirectory, String currentCommand, String[] recentCommands) {
        if (webSocket == null) return;
        
        JsonObject contextUpdate = new JsonObject();
        contextUpdate.addProperty("type", "context_update");
        contextUpdate.addProperty("working_directory", workingDirectory);
        contextUpdate.addProperty("current_command", currentCommand);
        contextUpdate.add("recent_commands", gson.toJsonTree(recentCommands));
        
        webSocket.send(gson.toJson(contextUpdate));
    }
    
    private void sendAIRequest(String endpoint, JsonObject requestBody, RequestCallback callback) {
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
            .url(API_BASE_URL + endpoint)
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
                    if (listener != null) {
                        listener.onAuthenticationRequired();
                    }
                } else {
                    callback.onError("Request failed: " + response.code());
                }
            }
        });
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