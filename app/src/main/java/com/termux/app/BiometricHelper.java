package com.termux.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Helper class for biometric authentication (Fingerprint, Face, etc.)
 * Used to protect sensitive operations like viewing/editing API keys.
 */
public class BiometricHelper {

    public interface BiometricCallback {
        void onAuthenticationSuccess();
        void onAuthenticationError(String error);
        void onAuthenticationCancelled();
    }

    /**
     * Check if biometric authentication is available on the device.
     */
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG 
                | BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Show biometric prompt for authentication.
     */
    public static void authenticate(
            @NonNull FragmentActivity activity,
            @NonNull String title,
            @NonNull String subtitle,
            @NonNull BiometricCallback callback) {

        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || 
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    callback.onAuthenticationCancelled();
                } else {
                    callback.onAuthenticationError(errString.toString());
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Handled automatically by the system UI
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG 
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}