package com.termux.plus.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.termux.plus.api.TermuxPlugin;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the lifecycle and discovery of Termux+ plugins.
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static final String PREFS_NAME = "plugin_prefs";
    private static final String PLUGIN_DIR = "plugins";
    private static PluginManager instance;
    private final Map<String, TermuxPlugin> plugins = new HashMap<>();
    private final Context context;
    private final SharedPreferences prefs;

    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPluginsFromDirectory();
    }

    public static synchronized PluginManager getInstance(Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;
    }

    /**
     * Scans the external files 'plugins' directory for .jar or .apk files and attempts to load them.
     * Expects a manifest or a specific class name convention (e.g. 'PluginEntry') in the future.
     * For this v2.0.0 implementation, we provide the mechanism but rely on manual registration for now.
     */
    private void loadPluginsFromDirectory() {
        File pluginDir = new File(context.getExternalFilesDir(null), PLUGIN_DIR);
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
            return;
        }

        File[] files = pluginDir.listFiles((dir, name) -> name.endsWith(".jar") || name.endsWith(".apk"));
        if (files == null) return;

        for (File file : files) {
            try {
                // This is a simplified loader. In production, we'd read a manifest from the JAR.
                // For now, we assume the JAR contains a class named 'com.termux.plugin.Entry'
                // or similar, or we scan classes.
                // DexClassLoader loader = new DexClassLoader(
                //     file.getAbsolutePath(),
                //     context.getCodeCacheDir().getAbsolutePath(),
                //     null,
                //     context.getClassLoader()
                // );
                // Class<?> pluginClass = loader.loadClass("com.termux.plugin.Entry");
                // TermuxPlugin plugin = (TermuxPlugin) pluginClass.newInstance();
                // registerPlugin(plugin);
                Log.d(TAG, "Found potential plugin file: " + file.getName());
            } catch (Exception e) {
                Log.e(TAG, "Failed to load plugin from " + file.getName(), e);
            }
        }
    }

    /**
     * Registers a plugin instance.
     */
    public void registerPlugin(TermuxPlugin plugin) {
        if (!plugins.containsKey(plugin.getId())) {
            try {
                // Restore enabled state
                boolean isEnabled = prefs.getBoolean(plugin.getId(), true); // Default to enabled
                plugin.setEnabled(isEnabled);
                
                if (isEnabled) {
                    plugin.onInit(context);
                }
                
                plugins.put(plugin.getId(), plugin);
                Log.i(TAG, "Registered plugin: " + plugin.getName() + " (Enabled: " + isEnabled + ")");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize plugin: " + plugin.getName(), e);
            }
        }
    }

    /**
     * Unregisters a plugin.
     */
    public void unregisterPlugin(String pluginId) {
        TermuxPlugin plugin = plugins.remove(pluginId);
        if (plugin != null && plugin.isEnabled()) {
            plugin.onUnload();
        }
    }
    
    /**
     * Toggle plugin state.
     */
    public void setPluginEnabled(String pluginId, boolean enabled) {
        TermuxPlugin plugin = plugins.get(pluginId);
        if (plugin != null) {
            if (plugin.isEnabled() != enabled) {
                if (enabled) {
                    plugin.onInit(context);
                } else {
                    plugin.onUnload();
                }
                plugin.setEnabled(enabled);
                prefs.edit().putBoolean(pluginId, enabled).apply();
            }
        }
    }

    /**
     * Returns a list of all registered plugins.
     */
    public List<TermuxPlugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    /**
     * Get a specific plugin by ID.
     */
    public TermuxPlugin getPlugin(String id) {
        return plugins.get(id);
    }
    
    /**
     * Get enabled plugins of a specific type (e.g., AIProvider.class).
     */
    public <T> List<T> getEnabledPluginsByType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (TermuxPlugin plugin : plugins.values()) {
            if (plugin.isEnabled() && type.isInstance(plugin)) {
                result.add(type.cast(plugin));
            }
        }
        return result;
    }
}
