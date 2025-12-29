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
import com.google.gson.reflect.TypeToken;
import com.termux.ai.ClaudeCodeIntegration;
import com.termux.ai.R;
import com.termux.ai.databinding.ActivityTabbedTerminalBinding;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TabbedTerminalActivity extends AppCompatActivity {
    private static final String TAG = "TabbedTerminalActivity";
    private static final int MAX_TABS = 8;
    private static final String PREFS_NAME = "terminal_tabs";
    private static final String KEY_TABS = "tabs";
    private long lastDoubleTapTime = 0;

    private ActivityTabbedTerminalBinding binding;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabNewTab;
    private FloatingActionButton fabClaudeCode;
    private QuickSettingsPanel quickSettingsPanel;
    private ShakeDetector shakeDetector;
    
    private TerminalTabAdapter tabAdapter;
    private List<TerminalTab> terminalTabs;
    private ClaudeCodeIntegration claudeIntegration;
    private android.view.GestureDetector gestureDetector;

    // Cached animations to prevent lag from repeated resource loading
    private android.view.animation.Animation slideInBottom;
    private android.view.animation.Animation slideOutBottom;
    private android.view.animation.Animation slideInTop;
    private android.view.animation.Animation slideOutTop;
    private android.view.animation.Animation fabFadeIn;
    private android.view.animation.Animation fabFadeOut;
    
        private void toggleBottomPanelVisibility() {
            if (binding.bottomPanel.getVisibility() == View.VISIBLE) {
                binding.bottomPanel.startAnimation(slideOutBottom);
                binding.bottomPanel.setVisibility(View.GONE);
            } else {
                binding.bottomPanel.startAnimation(slideInBottom);
                binding.bottomPanel.setVisibility(View.VISIBLE);
            }
        }
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // It would be better to save the state of the tabs, including the working directory and the command history, so that the user can resume their session.
            super.onCreate(savedInstanceState);
            binding = ActivityTabbedTerminalBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
    
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
            }
    
            binding.btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(TabbedTerminalActivity.this, TermuxAISettingsActivity.class));
            });

            // Initialize quick settings panel
            quickSettingsPanel = binding.quickSettingsPanel;
            quickSettingsPanel.setCallback(new QuickSettingsPanel.QuickSettingsPanelCallback() {
                @Override
                public void onSettingsApplied() {
                    // Handle settings applied
                    Toast.makeText(TabbedTerminalActivity.this, "Settings updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPanelClosed() {
                    toggleQuickSettingsPanel(); // Hide the panel
                }
            });
    
            initializeViews();
            setupTerminalTabs();
            setupClaudeIntegration();
            setupFloatingActionButtons();
    
            // Initially hide bottom panel with animation
            binding.bottomPanel.setVisibility(View.GONE); // Ensure it's GONE before animation if not already
            // No animation on initial hide, as it's already GONE by default in XML,
            // and we want it to be initially hidden without a "slide out" effect on app start.
    
            loadTabs();
        }    
        @Override
        protected void onPause() {
            super.onPause();
            saveTabs();
        }    
    private void initializeViews() {
        viewPager = binding.terminalViewpager;
        tabLayout = binding.terminalTablayout;
        fabNewTab = binding.fabNewTab;
        fabClaudeCode = binding.fabClaudeCode;

        // Customize tab layout for mobile
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabGravity(TabLayout.GRAVITY_START);

        // Load and cache animations to prevent lag
        slideInBottom = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideOutBottom = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        slideInTop = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        slideOutTop = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        fabFadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fab_fade_in);
        fabFadeOut = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fab_fade_out);
    }
    
    private void setupTerminalTabs() {
        terminalTabs = new ArrayList<>();
        tabAdapter = new TerminalTabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        // Connect tabs with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position < terminalTabs.size()) {
                TerminalTab terminalTab = terminalTabs.get(position);

                // Create custom tab view with close button
                View customTabView = getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
                ImageView tabIcon = customTabView.findViewById(R.id.tab_icon);
                TextView tabTitle = customTabView.findViewById(R.id.tab_title);
                ImageButton closeBtn = customTabView.findViewById(R.id.tab_close_button);

                tabTitle.setText(terminalTab.getDisplayName());
                tabIcon.setImageResource(terminalTab.getIcon());

                // Ensure minimum touch target size for mobile
                MobileGesturesHelper.setupTouchTarget(customTabView, 48);
                MobileGesturesHelper.setupTouchTarget(closeBtn, 48);

                // Show close button on long press
                customTabView.setOnLongClickListener(v -> {
                    closeBtn.setVisibility(View.VISIBLE);
                    // Auto-hide after 3 seconds
                    customTabView.postDelayed(() -> {
                        if (closeBtn.getVisibility() == View.VISIBLE) {
                            closeBtn.setVisibility(View.GONE);
                        }
                    }, 3000);
                    return true;
                });

                // Handle tab selection
                customTabView.setOnClickListener(v -> {
                    if (closeBtn.getVisibility() == View.VISIBLE) {
                        // If close button is visible and user taps again, close the tab
                        closeTab(position);
                        closeBtn.setVisibility(View.GONE);
                    } else {
                        // Select the tab normally
                        viewPager.setCurrentItem(position);
                    }
                });

                // Set up close button click
                closeBtn.setOnClickListener(v -> {
                    closeTab(position);
                    closeBtn.setVisibility(View.GONE);
                });

                tab.setCustomView(customTabView);
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
        binding.claudeStatusOverlay.setVisibility(View.GONE); // Initially hide the overlay

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
                    if (fabClaudeCode.getVisibility() != View.VISIBLE) {
                        fabClaudeCode.startAnimation(fabFadeIn);
                        fabClaudeCode.setVisibility(View.VISIBLE);
                    }

                    // Update tab to show Claude is active
                    if (tabIndex < terminalTabs.size()) {
                        terminalTabs.get(tabIndex).setClaudeActive(true);
                    }
                });
            }

            public void onOperationDetected(String operation) {
                runOnUiThread(() -> {
                    // Show overlay with slide-down animation
                    if (binding.claudeStatusOverlay.getVisibility() != View.VISIBLE) {
                        binding.claudeStatusOverlay.setVisibility(View.VISIBLE);
                        binding.claudeStatusOverlay.startAnimation(slideInTop);
                    }
                    binding.claudeStatusText.setText("🤖 Claude: " + operation + "...");
                    binding.claudeProgressBar.setProgress(0);
                    binding.claudeProgressText.setText("0%");

                    // Update the custom tab to show Claude is active
                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem < terminalTabs.size()) {
                        terminalTabs.get(currentItem).setClaudeActive(true);
                        // Update the tab view
                        TabLayout.Tab tab = tabLayout.getTabAt(currentItem);
                        if (tab != null && tab.getCustomView() != null) {
                            ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                            if (tabIcon != null) {
                                tabIcon.setImageResource(R.drawable.ic_claude_active);
                            }
                        }
                    }
                });
            }

            public void onProgressUpdated(float progress) {
                runOnUiThread(() -> {
                    int percent = (int) (progress * 100);
                    binding.claudeProgressBar.setProgress(percent);
                    binding.claudeProgressText.setText(percent + "%");

                    // Update the progress bar to be more visually appealing
                    binding.claudeProgressBar.setIndeterminate(false);
                    binding.claudeProgressBar.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onClaudeCompleted(int tabIndex) {
                runOnUiThread(() -> {
                    // Restore normal tab icon
                    TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
                    if (tab != null) {
                        if (tabIndex < terminalTabs.size()) {
                            TerminalTab terminalTab = terminalTabs.get(tabIndex);
                            terminalTab.setClaudeActive(false);

                            // Update the tab view
                            if (tab.getCustomView() != null) {
                                ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                                if (tabIcon != null) {
                                    tabIcon.setImageResource(terminalTab.getIcon());
                                }
                            }
                        }
                    }

                    // Hide overlay with slide-up animation
                    if (binding.claudeStatusOverlay.getVisibility() == View.VISIBLE) {
                        binding.claudeStatusOverlay.startAnimation(slideOutTop);
                        binding.claudeStatusOverlay.setVisibility(View.GONE);
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
                        fabClaudeCode.startAnimation(fabFadeOut);
                        fabClaudeCode.setVisibility(View.GONE);
                    }
                });
            }

            public void onClaudeErrorDetected(String error) {
                runOnUiThread(() -> {
                    // Hide overlay with slide-up animation on error
                    if (binding.claudeStatusOverlay.getVisibility() == View.VISIBLE) {
                        binding.claudeStatusOverlay.startAnimation(slideOutTop);
                        binding.claudeStatusOverlay.setVisibility(View.GONE);
                    }
                    Toast.makeText(TabbedTerminalActivity.this, "❌ Claude Error: " + error, Toast.LENGTH_LONG).show();
                });
            }

            public void onClaudeTokenUsageUpdated(int used, int total) {
                runOnUiThread(() -> {
                    // Update token usage in the status overlay
                    binding.claudeStatusText.setText("🤖 Claude: Tokens " + used + "/" + total);
                });
            }
        });
    }
    
    private void setupFloatingActionButtons() {
        fabNewTab.setOnClickListener(v -> showNewTabDialog());
        fabNewTab.setOnLongClickListener(v -> {
            toggleBottomPanelVisibility();
            return true;
        });

        // Add swipe gestures to the main coordinator layout for mobile navigation
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) binding.getRoot();
        MobileGesturesHelper gesturesHelper = new MobileGesturesHelper(this, new MobileGesturesHelper.GestureCallback() {
            @Override
            public void onSwipeUp() {
                // Could be used to show notifications or quick access panel
            }

            @Override
            public void onSwipeDown() {
                // If Claude is active, this might stop the operation
                if (binding.claudeStatusOverlay.getVisibility() == View.VISIBLE) {
                    // Hide Claude status overlay
                    binding.claudeStatusOverlay.startAnimation(slideOutTop);
                    binding.claudeStatusOverlay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSwipeLeft() {
                // Navigate to next tab
                int nextTab = (viewPager.getCurrentItem() + 1) % terminalTabs.size();
                viewPager.setCurrentItem(nextTab);
            }

            @Override
            public void onSwipeRight() {
                // Navigate to previous tab
                int prevTab = (viewPager.getCurrentItem() - 1 + terminalTabs.size()) % terminalTabs.size();
                viewPager.setCurrentItem(prevTab);
            }

            @Override
            public void onDoubleTap() {
                // Could show quick settings or expand current tab info
            }

            @Override
            public void onLongPress() {
                // Could show context menu
            }

            @Override
            public void onTripleTap() {
                // Triple tap activates voice input
                showVoiceInputDialog();
            }

            @Override
            public void onShake() {
                // Handle device shake - could clear terminal or show quick actions
                runOnUiThread(() -> {
                    // Option 1: Clear the terminal
                    // Get current terminal fragment and clear it
                    TerminalFragment currentFragment = (TerminalFragment) getSupportFragmentManager()
                        .findFragmentByTag("f" + viewPager.getCurrentItem());
                    if (currentFragment != null) {
                        currentFragment.clearTerminal();
                    }

                    // Option 2: Show a quick action toast
                    Toast.makeText(TabbedTerminalActivity.this, "Device shaken - cleared terminal", Toast.LENGTH_SHORT).show();
                });
            }
        });

        coordinatorLayout.setOnTouchListener(gesturesHelper);

        // Initialize gesture detector for triple tap gesture
        gestureDetector = new android.view.GestureDetector(this, new android.view.GestureDetector.SimpleOnGestureListener());

        // Also handle triple tap gesture for voice input
        gestureDetector.setOnDoubleTapListener(new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    // Track if we're in a triple tap sequence
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastDoubleTapTime < 300) { // Within triple tap window
                        performTripleTap();
                        return true;
                    }
                    lastDoubleTapTime = currentTime;
                }
                return false;
            }
        });

        fabClaudeCode.setOnClickListener(v -> {
            TerminalTab currentTab = getCurrentTab();
            if (currentTab != null) {
                showClaudeQuickActions(currentTab);
            }
        });

        fabClaudeCode.startAnimation(fabFadeOut);
        fabClaudeCode.setVisibility(View.GONE);

        // Initialize and start shake detector
        shakeDetector = new ShakeDetector(this, new ShakeDetector.ShakeCallback() {
            @Override
            public void onShake() {
                // Handle device shake - could clear terminal or show quick actions
                runOnUiThread(() -> {
                    // Option 1: Clear the terminal
                    // Get current terminal fragment and clear it
                    TerminalFragment currentFragment = (TerminalFragment) getSupportFragmentManager()
                        .findFragmentByTag("f" + viewPager.getCurrentItem());
                    if (currentFragment != null) {
                        currentFragment.clearTerminal();
                    }

                    // Option 2: Show a quick action toast
                    Toast.makeText(TabbedTerminalActivity.this, "Device shaken - cleared terminal", Toast.LENGTH_SHORT).show();
                });
            }
        });
        shakeDetector.start();

        // Set up bottom panel button listeners
        binding.btnFilePicker.setOnClickListener(v -> {
            // Show file picker for Claude context
            Toast.makeText(this, "File picker - coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.btnVoiceInput.setOnClickListener(v -> {
            // Show voice input for Claude
            showVoiceInputDialog();
        });

        binding.btnQuickCommands.setOnClickListener(v -> {
            // Show quick command templates
            Toast.makeText(this, "Quick commands - coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.btnProjectInfo.setOnClickListener(v -> {
            // Show project insights dialog
            TerminalTab currentTab = getCurrentTab();
            if (currentTab != null) {
                showProjectInsights(currentTab);
            }
        });

        // Set up long press for settings panel
        binding.btnSettings.setOnLongClickListener(v -> {
            toggleQuickSettingsPanel();
            return true;
        });
    }

    private void performTripleTap() {
        // Triple tap action - show voice input dialog
        showVoiceInputDialog();
    }

    public void showVoiceInputDialog() {
        // Show a dialog or interface for voice input to Claude
        Toast.makeText(this, "Voice input activated - would open microphone for Claude commands", Toast.LENGTH_SHORT).show();

        // In a real implementation, this would use Android's SpeechRecognizer
        // to capture voice input and send it to Claude
    }

    private void toggleQuickSettingsPanel() {
        if (quickSettingsPanel.getVisibility() == View.VISIBLE) {
            // Slide out animation
            quickSettingsPanel.animate()
                .translationX(300)
                .setDuration(300)
                .withEndAction(() -> quickSettingsPanel.setVisibility(View.GONE))
                .start();
        } else {
            // Show and slide in animation
            quickSettingsPanel.setVisibility(View.VISIBLE);
            quickSettingsPanel.animate()
                .translationX(0)
                .setDuration(300)
                .start();
        }
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
                fabClaudeCode.startAnimation(fabFadeIn);
                fabClaudeCode.setVisibility(View.VISIBLE);
            } else if (!anyTabHasClaude()) {
                fabClaudeCode.startAnimation(fabFadeOut);
                fabClaudeCode.setVisibility(View.GONE);
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

    private void showProjectInsights(TerminalTab tab) {
        ProjectInsightsDialog dialog = new ProjectInsightsDialog();
        dialog.setTab(tab);
        dialog.show(getSupportFragmentManager(), "project_insights");
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

        if (shakeDetector != null) {
            shakeDetector.stop();
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
            if (position < 0 || position >= terminalTabs.size()) {
                // Fallback to prevent crash - create default fragment
                return TerminalFragment.newInstance("home", getDefaultDirectory(), 0);
            }
            TerminalTab tab = terminalTabs.get(position);
            return TerminalFragment.newInstance(tab.getName(), tab.getWorkingDirectory(), position);
        }
        
        @Override
        public int getItemCount() {
            return terminalTabs.size();
        }
        
        @Override
        public long getItemId(int position) {
            if (position < 0 || position >= terminalTabs.size()) {
                return -1; // Invalid ID
            }
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