package com.termux.plus.plugin.impl;

import android.content.Context;
import android.util.Log;
import com.termux.plus.api.TermuxPlugin;

public class AutoSavePlugin implements TermuxPlugin {

    private boolean enabled = true;

    @Override
    public String getId() {
        return "com.termux.plus.autosave";
    }

    @Override
    public String getName() {
        return "Auto Save";
    }

    @Override
    public String getDescription() {
        return "Automatically saves terminal session logs periodically.";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public String getAuthor() {
        return "Termux+ Team";
    }

    @Override
    public void onInit(Context context) {
        Log.d("AutoSavePlugin", "Auto Save plugin initialized.");
    }

    @Override
    public void onUnload() {
        Log.d("AutoSavePlugin", "Auto Save plugin unloaded.");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
