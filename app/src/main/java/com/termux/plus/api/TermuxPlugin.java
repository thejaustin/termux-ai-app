package com.termux.plus.api;

import android.content.Context;

/**
 * Base interface for all Termux+ plugins.
 */
public interface TermuxPlugin {
    /**
     * Unique identifier for the plugin (e.g., "com.example.myplugin").
     */
    String getId();

    /**
     * Display name of the plugin.
     */
    String getName();

    /**
     * Description of what the plugin does.
     */
    String getDescription();

    /**
     * Version of the plugin.
     */
    String getVersion();

    /**
     * Author or vendor of the plugin.
     */
    String getAuthor();

    /**
     * Called when the plugin is loaded and initialized.
     * @param context Application context.
     */
    void onInit(Context context);

    /**
     * Called when the plugin is disabled or unloaded.
     */
    void onUnload();

    /**
     * Whether the plugin is currently enabled.
     */
    boolean isEnabled();

    /**
     * Set the enabled state of the plugin.
     */
    void setEnabled(boolean enabled);
}
