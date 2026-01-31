package com.termux.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.ai.R;

/**
 * Helper class for displaying user-friendly error dialogs with retry options
 * and actionable suggestions for common error scenarios.
 */
public class ErrorDialogHelper {

    /**
     * Error types with specific handling
     */
    public enum ErrorType {
        NETWORK_ERROR,
        AUTH_ERROR,
        PERMISSION_ERROR,
        CLAUDE_NOT_INSTALLED,
        CLAUDE_API_ERROR,
        GEMINI_API_ERROR,
        TOKEN_LIMIT_EXCEEDED,
        FILE_NOT_FOUND,
        UNKNOWN_ERROR
    }

    /**
     * Callback for retry actions
     */
    public interface RetryCallback {
        void onRetry();
    }

    /**
     * Show an error dialog with retry option
     */
    public static void showErrorWithRetry(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            @Nullable RetryCallback retryCallback) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Dismiss", null);

        if (retryCallback != null) {
            builder.setPositiveButton("Retry", (dialog, which) -> retryCallback.onRetry());
        }

        builder.show();
    }

    /**
     * Show a contextual error dialog based on error type
     */
    public static void showError(
            @NonNull Context context,
            @NonNull ErrorType errorType,
            @Nullable String additionalInfo,
            @Nullable RetryCallback retryCallback) {

        switch (errorType) {
            case NETWORK_ERROR:
                showNetworkError(context, retryCallback);
                break;
            case AUTH_ERROR:
                showAuthError(context, additionalInfo);
                break;
            case PERMISSION_ERROR:
                showPermissionError(context, additionalInfo);
                break;
            case CLAUDE_NOT_INSTALLED:
                showClaudeNotInstalledError(context);
                break;
            case CLAUDE_API_ERROR:
                showClaudeApiError(context, additionalInfo, retryCallback);
                break;
            case GEMINI_API_ERROR:
                showGeminiApiError(context, additionalInfo, retryCallback);
                break;
            case TOKEN_LIMIT_EXCEEDED:
                showTokenLimitError(context, additionalInfo);
                break;
            case FILE_NOT_FOUND:
                showFileNotFoundError(context, additionalInfo);
                break;
            default:
                showGenericError(context, additionalInfo, retryCallback);
        }
    }

    /**
     * Network error with retry and settings options
     */
    private static void showNetworkError(Context context, @Nullable RetryCallback retryCallback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle("Network Error")
            .setMessage("Unable to connect to the network.\n\n" +
                "Please check:\n" +
                "• WiFi or mobile data is enabled\n" +
                "• Airplane mode is off\n" +
                "• VPN is working correctly")
            .setIcon(R.drawable.ic_network_error)
            .setNegativeButton("Dismiss", null)
            .setNeutralButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                context.startActivity(intent);
            });

        if (retryCallback != null) {
            builder.setPositiveButton("Retry", (dialog, which) -> retryCallback.onRetry());
        }

        builder.show();
    }

    /**
     * Authentication error with guidance
     */
    private static void showAuthError(Context context, @Nullable String details) {
        String message = "Authentication failed.\n\n";
        if (details != null && !details.isEmpty()) {
            message += "Details: " + details + "\n\n";
        }
        message += "Please check:\n" +
            "• Your API key is correct\n" +
            "• Your account has sufficient credits\n" +
            "• The API key has not expired";

        new MaterialAlertDialogBuilder(context)
            .setTitle("Authentication Error")
            .setMessage(message)
            .setIcon(R.drawable.ic_auth_error)
            .setNegativeButton("Dismiss", null)
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(context, TermuxPlusSettingsActivity.class);
                context.startActivity(intent);
            })
            .show();
    }

    /**
     * Permission error with settings shortcut
     */
    private static void showPermissionError(Context context, @Nullable String permissionName) {
        String message = "Permission denied.\n\n";
        if (permissionName != null && !permissionName.isEmpty()) {
            message += "Required permission: " + permissionName + "\n\n";
        }
        message += "Please grant the required permissions in app settings.";

        new MaterialAlertDialogBuilder(context)
            .setTitle("Permission Required")
            .setMessage(message)
            .setIcon(R.drawable.ic_permission_error)
            .setNegativeButton("Dismiss", null)
            .setPositiveButton("App Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            })
            .show();
    }

    /**
     * Claude not installed error with installation guidance
     */
    private static void showClaudeNotInstalledError(Context context) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Claude Code Not Found")
            .setMessage("Claude Code CLI is not installed.\n\n" +
                "To install, run:\n" +
                "npm install -g @anthropic-ai/claude-code\n\n" +
                "Make sure you have Node.js installed first.")
            .setIcon(R.drawable.ic_claude)
            .setNegativeButton("Dismiss", null)
            .setPositiveButton("Copy Command", (dialog, which) -> {
                android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText(
                    "Install Command", "npm install -g @anthropic-ai/claude-code");
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(context, "Command copied to clipboard",
                    android.widget.Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    /**
     * Claude API error with retry option
     */
    private static void showClaudeApiError(Context context, @Nullable String details,
                                           @Nullable RetryCallback retryCallback) {
        String message = "Claude API returned an error.\n\n";
        if (details != null && !details.isEmpty()) {
            message += "Details: " + details + "\n\n";
        }
        message += "This may be a temporary issue. Try again in a moment.";

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle("Claude API Error")
            .setMessage(message)
            .setIcon(R.drawable.ic_claude)
            .setNegativeButton("Dismiss", null);

        if (retryCallback != null) {
            builder.setPositiveButton("Retry", (dialog, which) -> retryCallback.onRetry());
        }

        builder.show();
    }

    /**
     * Gemini API error with retry option
     */
    private static void showGeminiApiError(Context context, @Nullable String details,
                                           @Nullable RetryCallback retryCallback) {
        String message = "Google Gemini API returned an error.\n\n";
        if (details != null && !details.isEmpty()) {
            message += "Details: " + details + "\n\n";
        }
        message += "This may be a temporary issue or safety filters triggered. Try again or check your prompt.";

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle("Gemini API Error")
            .setMessage(message)
            .setIcon(R.drawable.ic_launcher_foreground) // Use default AI icon if ic_gemini not available
            .setNegativeButton("Dismiss", null);

        if (retryCallback != null) {
            builder.setPositiveButton("Retry", (dialog, which) -> retryCallback.onRetry());
        }

        builder.show();
    }

    /**
     * Token limit exceeded with guidance
     */
    private static void showTokenLimitError(Context context, @Nullable String usage) {
        String message = "You've reached the token limit for this conversation.\n\n";
        if (usage != null && !usage.isEmpty()) {
            message += "Usage: " + usage + "\n\n";
        }
        message += "You can:\n" +
            "• Start a new conversation\n" +
            "• Clear the current context\n" +
            "• Increase the token limit in settings";

        new MaterialAlertDialogBuilder(context)
            .setTitle("Token Limit Exceeded")
            .setMessage(message)
            .setIcon(R.drawable.ic_token_limit)
            .setNegativeButton("Dismiss", null)
            .setNeutralButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(context, TermuxPlusSettingsActivity.class);
                context.startActivity(intent);
            })
            .setPositiveButton("New Conversation", (dialog, which) -> {
                // This would trigger starting a new conversation
                // The activity can listen for this
            })
            .show();
    }

    /**
     * File not found error
     */
    private static void showFileNotFoundError(Context context, @Nullable String filePath) {
        String message = "The requested file could not be found.\n\n";
        if (filePath != null && !filePath.isEmpty()) {
            message += "Path: " + filePath + "\n\n";
        }
        message += "The file may have been moved, renamed, or deleted.";

        new MaterialAlertDialogBuilder(context)
            .setTitle("File Not Found")
            .setMessage(message)
            .setIcon(R.drawable.ic_file_not_found)
            .setNegativeButton("OK", null)
            .show();
    }

    /**
     * Generic error with optional retry
     */
    private static void showGenericError(Context context, @Nullable String details,
                                         @Nullable RetryCallback retryCallback) {
        String message = "An unexpected error occurred.\n\n";
        if (details != null && !details.isEmpty()) {
            message += "Details: " + details;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle("Error")
            .setMessage(message)
            .setNegativeButton("Dismiss", null);

        if (retryCallback != null) {
            builder.setPositiveButton("Retry", (dialog, which) -> retryCallback.onRetry());
        }

        builder.show();
    }

    /**
     * Show a simple info dialog (not an error, but informational)
     */
    public static void showInfo(@NonNull Context context, @NonNull String title, @NonNull String message) {
        new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Show a confirmation dialog with yes/no options
     */
    public static void showConfirmation(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            @NonNull Runnable onConfirm) {

        new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm", (dialog, which) -> onConfirm.run())
            .show();
    }
}
