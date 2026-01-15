package com.termux.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Manages encrypted SharedPreferences for secure storage of sensitive data.
 *
 * Uses AndroidX Security Crypto library to encrypt both keys and values
 * stored in SharedPreferences, protecting against:
 * - Device backups exposing credentials
 * - Rooted device access
 * - Memory dumps
 * - Logcat examination
 */
public class EncryptedPreferencesManager {
    private static final String TAG = "EncryptedPrefs";

    /**
     * Get or create encrypted SharedPreferences instance.
     *
     * @param context Application context
     * @param prefName Name of the preferences file
     * @return Encrypted SharedPreferences instance
     * @throws RuntimeException if encryption setup fails
     */
    public static SharedPreferences getEncryptedPrefs(Context context, String prefName) {
        try {
            // Create or retrieve the master key for encryption
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

            // Create encrypted SharedPreferences
            return EncryptedSharedPreferences.create(
                context,
                prefName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create encrypted preferences", e);
            throw new RuntimeException("Failed to initialize secure storage", e);
        }
    }

    /**
     * Migrate existing plaintext preferences to encrypted storage.
     *
     * @param context Application context
     * @param plaintextPrefName Name of plaintext preferences file
     * @param encryptedPrefName Name of encrypted preferences file
     * @return true if migration successful, false otherwise
     */
    public static boolean migratePlaintextToEncrypted(Context context,
                                                      String plaintextPrefName,
                                                      String encryptedPrefName) {
        try {
            SharedPreferences plaintextPrefs = context.getSharedPreferences(
                plaintextPrefName,
                Context.MODE_PRIVATE
            );

            // Check if plaintext prefs are empty (already migrated or first run)
            if (plaintextPrefs.getAll().isEmpty()) {
                return true;
            }

            SharedPreferences encryptedPrefs = getEncryptedPrefs(context, encryptedPrefName);
            SharedPreferences.Editor editor = encryptedPrefs.edit();

            // Copy all values from plaintext to encrypted
            for (String key : plaintextPrefs.getAll().keySet()) {
                Object value = plaintextPrefs.getAll().get(key);

                if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Float) {
                    editor.putFloat(key, (Float) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                }
            }

            // Commit encrypted values
            boolean success = editor.commit();

            if (success) {
                // Clear plaintext preferences after successful migration
                plaintextPrefs.edit().clear().commit();
                Log.i(TAG, "Successfully migrated preferences to encrypted storage");
            }

            return success;

        } catch (Exception e) {
            Log.e(TAG, "Failed to migrate preferences", e);
            return false;
        }
    }

    /**
     * Check if encrypted preferences exist and are accessible.
     *
     * @param context Application context
     * @param prefName Name of the preferences file
     * @return true if preferences are accessible, false otherwise
     */
    public static boolean isEncryptedPrefsAccessible(Context context, String prefName) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context, prefName);
            // Try to read from preferences to verify accessibility
            prefs.getAll();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Encrypted preferences not accessible", e);
            return false;
        }
    }
}
