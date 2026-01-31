package com.termux.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.termux.app.QuickSettingsPanel;
import com.termux.app.ShakeDetector;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.termux.plus.plugin.PluginManager;
import com.termux.plus.plugin.impl.ClaudePlugin;
import com.termux.plus.api.AIProvider;

public class TabbedTerminalActivity extends AppCompatActivity {
    private static final String TAG = "TabbedTerminalActivity";
    private static final int MAX_TABS = 8;
    private static final String PREFS_NAME = "terminal_tabs";
    private static final String KEY_TABS = "tabs";
    private long lastDoubleTapTime = 0;
    private String sessionId;

    private ActivityTabbedTerminalBinding binding;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabNewTab;
    private FloatingActionButton fabClaudeCode;
    private QuickSettingsPanel quickSettingsPanel;
    private ShakeDetector shakeDetector;
    
    private TerminalTabAdapter tabAdapter;
    private List<TerminalTab> terminalTabs;
    private ClaudePlugin claudePlugin;
    private android.view.GestureDetector gestureDetector;
    
    private ExecutorService ioExecutor;

    // ... (rest of fields) ...

            initializeViews();
            setupTerminalTabs();
            setupPlugins();
            setupFloatingActionButtons();
    
    // ... (rest of onCreate) ...

    private void setupPlugins() {
        PluginManager manager = PluginManager.getInstance(this);
        // In a real implementation, we would look up by ID or type.
        // For now, we cast to the known implementation for backward compatibility logic
        com.termux.plus.api.TermuxPlugin plugin = manager.getPlugin("com.termux.plus.claude");
        if (plugin instanceof ClaudePlugin) {
            claudePlugin = (ClaudePlugin) plugin;
        } else {
            // Fallback if not registered (should not happen if App initialized correctly)
            claudePlugin = new ClaudePlugin();
            claudePlugin.onInit(this);
        }

        binding.claudeStatusOverlay.setVisibility(View.GONE);

        claudePlugin.setAIListener(new AIProvider.AIListener() {
            @Override
            public void onOperationDetected(String operation) {
                runOnUiThread(() -> {
                    if (binding.claudeStatusOverlay.getVisibility() != View.VISIBLE) {
                        binding.claudeStatusOverlay.setVisibility(View.VISIBLE);
                        binding.claudeStatusOverlay.startAnimation(slideInTop);
                    }
                    binding.claudeStatusText.setText("🤖 Claude: " + operation + "...");
                    binding.claudeProgressBar.setProgress(0);
                    binding.claudeProgressText.setText("0%");

                    // Update tab icon
                    updateCurrentTabIcon(true);
                });
            }

            @Override
            public void onProgressUpdated(float progress) {
                runOnUiThread(() -> {
                    int percent = (int) (progress * 100);
                    binding.claudeProgressBar.setProgress(percent);
                    binding.claudeProgressText.setText(percent + "%");
                    binding.claudeProgressBar.setIndeterminate(false);
                    binding.claudeProgressBar.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFileGenerated(String filePath, String action) {
                // Toast handled by TerminalView usually, but we could log here
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (binding.claudeStatusOverlay.getVisibility() == View.VISIBLE) {
                        binding.claudeStatusOverlay.startAnimation(slideOutTop);
                        binding.claudeStatusOverlay.setVisibility(View.GONE);
                    }
                    Toast.makeText(TabbedTerminalActivity.this, "❌ Claude Error: " + error, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onCompleted() {
                runOnUiThread(() -> {
                    // Restore normal tab icon
                    updateCurrentTabIcon(false);

                    if (binding.claudeStatusOverlay.getVisibility() == View.VISIBLE) {
                        binding.claudeStatusOverlay.startAnimation(slideOutTop);
                        binding.claudeStatusOverlay.setVisibility(View.GONE);
                    }

                    if (!anyTabHasClaude()) {
                        fabClaudeCode.startAnimation(fabFadeOut);
                        fabClaudeCode.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onTokenUsage(int used, int total) {
                runOnUiThread(() -> {
                    binding.claudeStatusText.setText("🤖 Claude: Tokens " + used + "/" + total);
                });
            }
        });
        
        // We also need to listen for global detection events which are slightly different
        // This part implies we might need to enhance the AIProvider interface to support global events better
        // or just rely on the fact that the plugin wraps the legacy singleton logic for now.
        // The legacy global listener is attached inside ClaudePlugin.onInit if we modify it, but for now
        // let's manually attach the global listener to the underlying legacy instance for tab detection support.
        
        claudePlugin.getLegacyIntegration().setGlobalListener(new ClaudeCodeIntegration.GlobalListener() {
            @Override
            public void onClaudeDetected(int tabIndex) {
                runOnUiThread(() -> {
                    TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
                    if (tab != null) {
                        tab.setIcon(R.drawable.ic_claude_active);
                    }
                    if (fabClaudeCode.getVisibility() != View.VISIBLE) {
                        fabClaudeCode.startAnimation(fabFadeIn);
                        fabClaudeCode.setVisibility(View.VISIBLE);
                    }
                    if (tabIndex < terminalTabs.size()) {
                        terminalTabs.get(tabIndex).setClaudeActive(true);
                    }
                });
            }

            @Override
            public void onClaudeCompleted(int tabIndex) {
                 runOnUiThread(() -> {
                    TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
                    if (tab != null && tabIndex < terminalTabs.size()) {
                        TerminalTab terminalTab = terminalTabs.get(tabIndex);
                        terminalTab.setClaudeActive(false);
                        if (tab.getCustomView() != null) {
                            ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                            if (tabIcon != null) {
                                tabIcon.setImageResource(terminalTab.getIcon());
                            }
                        }
                    }
                 });
            }
        });
    }

    private void updateCurrentTabIcon(boolean active) {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < terminalTabs.size()) {
            terminalTabs.get(currentItem).setClaudeActive(active);
            TabLayout.Tab tab = tabLayout.getTabAt(currentItem);
            if (tab != null && tab.getCustomView() != null) {
                ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                if (tabIcon != null) {
                    tabIcon.setImageResource(active ? R.drawable.ic_claude_active : terminalTabs.get(currentItem).getIcon());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (claudePlugin != null) {
            claudePlugin.onUnload();
        }
        // ... (rest of onDestroy)