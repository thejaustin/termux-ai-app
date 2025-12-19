package com.termux.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.termux.ai.ClaudeCodeIntegration;
import com.termux.ai.R;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity for Termux AI with tabbed terminal interface
 * 
 * Features:
 * - Multiple terminal tabs for different projects
 * - Claude Code integration across tabs
 * - Project workspace management
 * - Enhanced mobile UI
 */
import java.io.Serializable;
import java.lang.reflect.Type;

public class TabbedTerminalActivity extends AppCompatActivity {
    private static final String TAG = "TabbedTerminalActivity";
    private static final int MAX_TABS = 8;
    private static final String PREFS_NAME = "terminal_tabs";
    private static final String KEY_TABS = "tabs";
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabNewTab;
    private FloatingActionButton fabClaudeCode;
    
    private TerminalTabAdapter tabAdapter;
    private List<TerminalTab> terminalTabs;
    private ClaudeCodeIntegration claudeIntegration;
    
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            // It would be better to save the state of the tabs, including the working directory and the command history, so that the user can resume their session.
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tabbed_terminal);
    
            initializeViews();
            setupTerminalTabs();
            setupClaudeIntegration();
            setupFloatingActionButtons();
    
            loadTabs();
        }
    
        @Override
        protected void onPause() {
            super.onPause();
            saveTabs();
        }    
    private void initializeViews() {
        viewPager = findViewById(R.id.terminal_viewpager);
        tabLayout = findViewById(R.id.terminal_tablayout);
        fabNewTab = findViewById(R.id.fab_new_tab);
        fabClaudeCode = findViewById(R.id.fab_claude_code);
        
        // Customize tab layout for mobile
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabGravity(TabLayout.GRAVITY_START);
    }
    
    private void setupTerminalTabs() {
        terminalTabs = new ArrayList<>();
        tabAdapter = new TerminalTabAdapter(this);
        viewPager.setAdapter(tabAdapter);
        
        // Connect tabs with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position < terminalTabs.size()) {
                TerminalTab terminalTab = terminalTabs.get(position);
                tab.setText(terminalTab.getDisplayName());
                tab.setIcon(terminalTab.getIcon());
            }
        }).attach();
        
        // Handle tab selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onTabSelected(position);
            }
        });
    }
    
    private void setupClaudeIntegration() {
        claudeIntegration = new ClaudeCodeIntegration();
        claudeIntegration.setGlobalListener(new ClaudeCodeIntegration.GlobalListener() {
            @Override
            public void onClaudeDetected(int tabIndex) {
                runOnUiThread(() -> {
                    // Update tab icon to show Claude is active
                    TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
                    if (tab != null) {
                        tab.setIcon(R.drawable.ic_claude_active);
                    }
                    
                    // Show Claude FAB
                    fabClaudeCode.show();
                });
            }
            
            @Override
            public void onClaudeCompleted(int tabIndex) {
                runOnUiThread(() -> {
                    // Restore normal tab icon
                    TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
                    if (tab != null) {
                        TerminalTab terminalTab = terminalTabs.get(tabIndex);
                        tab.setIcon(terminalTab.getIcon());
                    }
                    
                    // Check if any other tabs have Claude active
                    boolean anyClaudeActive = false;
                    for (TerminalTab termTab : terminalTabs) {
                        if (termTab.isClaudeActive()) {
                            anyClaudeActive = true;
                            break;
                        }
                    }
                    
                    if (!anyClaudeActive) {
                        fabClaudeCode.hide();
                    }
                });
            }
        });
    }
    
    private void setupFloatingActionButtons() {
        fabNewTab.setOnClickListener(v -> showNewTabDialog());
        
        fabClaudeCode.setOnClickListener(v -> {
            TerminalTab currentTab = getCurrentTab();
            if (currentTab != null) {
                showClaudeQuickActions(currentTab);
            }
        });
        
        // Initially hide Claude FAB
        fabClaudeCode.hide();
    }
    
    private void createNewTab(String name, String workingDirectory) {
        if (terminalTabs.size() >= MAX_TABS) {
            Toast.makeText(this, "Maximum " + MAX_TABS + " tabs allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TerminalTab newTab = new TerminalTab(name, workingDirectory);
        terminalTabs.add(newTab);
        tabAdapter.notifyItemInserted(terminalTabs.size() - 1);
        
        // Switch to new tab
        viewPager.setCurrentItem(terminalTabs.size() - 1);
    }
    
    private void closeTab(int position) {
        if (terminalTabs.size() <= 1) {
            Toast.makeText(this, "Cannot close the last tab", Toast.LENGTH_SHORT).show();
            return;
        }
        
        terminalTabs.remove(position);
        tabAdapter.notifyItemRemoved(position);
        
        // Adjust current position if needed
        if (position <= viewPager.getCurrentItem() && viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
    
    private void onTabSelected(int position) {
        if (position < terminalTabs.size()) {
            TerminalTab tab = terminalTabs.get(position);
            
            // Update action bar title
            setTitle("Termux AI - " + tab.getDisplayName());
            
            // Update Claude FAB visibility
            if (tab.isClaudeActive()) {
                fabClaudeCode.show();
            } else if (!anyTabHasClaude()) {
                fabClaudeCode.hide();
            }
        }
    }
    
    private boolean anyTabHasClaude() {
        for (TerminalTab tab : terminalTabs) {
            if (tab.isClaudeActive()) {
                return true;
            }
        }
        return false;
    }
    
    private TerminalTab getCurrentTab() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < terminalTabs.size()) {
            return terminalTabs.get(currentItem);
        }
        return null;
    }
    
    private void showNewTabDialog() {
        NewTabDialog dialog = new NewTabDialog();
        dialog.setListener(new NewTabDialog.NewTabListener() {
            @Override
            public void onCreateTab(String name, String directory, String projectType) {
                createNewTab(name, directory);
            }
        });
        dialog.show(getSupportFragmentManager(), "new_tab");
    }
    
    private void showClaudeQuickActions(TerminalTab tab) {
        ClaudeQuickActionsDialog dialog = new ClaudeQuickActionsDialog();
        dialog.setTab(tab);
        dialog.show(getSupportFragmentManager(), "claude_actions");
    }
    
    private String getDefaultDirectory() {
        return getFilesDir().getParent() + "/files/home";
    }
    
    /**
     * Get a tab by index
     * @param index The tab index
     * @return The terminal tab or null if index is invalid
     */
    public TerminalTab getTab(int index) {
        if (index >= 0 && index < terminalTabs.size()) {
            return terminalTabs.get(index);
        }
        return null;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.terminal_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_new_tab) {
            showNewTabDialog();
            return true;
        } else if (id == R.id.action_close_tab) {
            closeTab(viewPager.getCurrentItem());
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, TermuxAISettingsActivity.class));
            return true;
        } else if (id == R.id.action_claude_help) {
            showClaudeHelp();
            return true;
        } else if (id == R.id.action_share) {
            TerminalFragment currentFragment = (TerminalFragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
            if (currentFragment != null) {
                currentFragment.shareTranscript();
            }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showClaudeHelp() {
        ClaudeHelpDialog dialog = new ClaudeHelpDialog();
        dialog.show(getSupportFragmentManager(), "claude_help");
    }

    private void saveTabs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(terminalTabs);
        editor.putString(KEY_TABS, json);
        editor.apply();
    }

    private void loadTabs() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(KEY_TABS, null);
            Type type = new TypeToken<ArrayList<TerminalTab>>() {}.getType();
            List<TerminalTab> loadedTabs = gson.fromJson(json, type);

            if (loadedTabs != null && !loadedTabs.isEmpty()) {
                terminalTabs.clear();
                terminalTabs.addAll(loadedTabs);
                tabAdapter.notifyDataSetChanged();
            } else {
                // Create initial tab if no tabs were loaded
                createNewTab("home", getDefaultDirectory());
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Failed to load tabs from SharedPreferences", e);
            // Create initial tab if loading fails
            createNewTab("home", getDefaultDirectory());
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (claudeIntegration != null) {
            claudeIntegration.cleanup();
        }
    }
    
    /**
     * Adapter for terminal tabs
     */
    private class TerminalTabAdapter extends FragmentStateAdapter {
        public TerminalTabAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            TerminalTab tab = terminalTabs.get(position);
            return TerminalFragment.newInstance(tab.getName(), tab.getWorkingDirectory(), position);
        }
        
        @Override
        public int getItemCount() {
            return terminalTabs.size();
        }
        
        @Override
        public long getItemId(int position) {
            return terminalTabs.get(position).getId();
        }
        
        @Override
        public boolean containsItem(long itemId) {
            for (TerminalTab tab : terminalTabs) {
                if (tab.getId() == itemId) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Represents a terminal tab
     */
    public static class TerminalTab implements Serializable {
        private static long nextId = 1;
        
        private final long id;
        private String name;
        private String workingDirectory;
        private String projectType;
        private boolean claudeActive;
        private long lastActivityTime;
        
        public TerminalTab(String name, String workingDirectory) {
            this.id = nextId++;
            this.name = name;
            this.workingDirectory = workingDirectory;
            this.projectType = detectProjectType(workingDirectory);
            this.claudeActive = false;
            this.lastActivityTime = System.currentTimeMillis();
        }
        
        private String detectProjectType(String directory) {
            File dir = new File(directory);
            if (!dir.exists()) return "general";
            
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if ("package.json".equals(fileName)) return "nodejs";
                    if ("requirements.txt".equals(fileName) || "pyproject.toml".equals(fileName)) return "python";
                    if ("Cargo.toml".equals(fileName)) return "rust";
                    if ("go.mod".equals(fileName)) return "go";
                    if ("pom.xml".equals(fileName) || "build.gradle".equals(fileName)) return "java";
                }
            }
            return "general";
        }
        
        public String getDisplayName() {
            return name + (claudeActive ? " 🤖" : "");
        }
        
        public int getIcon() {
            if (claudeActive) {
                return R.drawable.ic_claude_active;
            }
            
            switch (projectType) {
                case "nodejs": return R.drawable.ic_nodejs;
                case "python": return R.drawable.ic_python;
                case "rust": return R.drawable.ic_rust;
                case "go": return R.drawable.ic_go;
                case "java": return R.drawable.ic_java;
                default: return R.drawable.ic_terminal;
            }
        }
        
        // Getters and setters
        public long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getWorkingDirectory() { return workingDirectory; }
        public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }
        public String getProjectType() { return projectType; }
        public boolean isClaudeActive() { return claudeActive; }
        public void setClaudeActive(boolean claudeActive) { this.claudeActive = claudeActive; }
        public long getLastActivityTime() { return lastActivityTime; }
        public void updateActivityTime() { this.lastActivityTime = System.currentTimeMillis(); }
    }
}