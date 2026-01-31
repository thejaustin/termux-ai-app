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
import com.google.gson.reflect.TypeToken;
import com.termux.ai.R;
import com.termux.ai.databinding.ActivityTabbedTerminalBinding;
import com.termux.plus.api.AIProvider;
import com.termux.plus.plugin.PluginManager;
import com.termux.plus.plugin.impl.ClaudePlugin;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

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
    private com.termux.plus.plugin.impl.ClaudePlugin claudePlugin;
    private android.view.GestureDetector gestureDetector;
    
    private ExecutorService ioExecutor;

    private final ActivityResultLauncher<Intent> speechRecognizerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    TerminalFragment fragment = getCurrentTerminalFragment();
                    if (fragment != null) {
                        fragment.sendCommand(spokenText);
                        Toast.makeText(this, "Voice command: " + spokenText, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                startVoiceInput();
            } else {
                Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show();
            }
        }
    );

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
            // Enable Material You Dynamic Colors
            com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
            
            // It would be better to save the state of the tabs, including the working directory and the command history, so that the user can resume their session.
            super.onCreate(savedInstanceState);
            
            ioExecutor = Executors.newSingleThreadExecutor();
            
            sessionId = getIntent().getStringExtra("SESSION_ID");
            if (sessionId == null) {
                sessionId = "default";
            }

            // Validate incoming intents for security
            if (!validateIntent(getIntent())) {
                Log.w(TAG, "Invalid or malicious intent detected, finishing activity");
                finish();
                return;
            }

            binding = ActivityTabbedTerminalBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
    
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
            }
    
            binding.btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(TabbedTerminalActivity.this, TermuxPlusSettingsActivity.class));
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
            setupPlugins();
            setupFloatingActionButtons();
    
            // Initially hide bottom panel with animation
            binding.bottomPanel.setVisibility(View.GONE); // Ensure it's GONE before animation if not already
            // No animation on initial hide, as it's already GONE by default in XML,
            // and we want it to be initially hidden without a "slide out" effect on app start.
    
            loadTabs();

            // Show onboarding on first launch
            if (OnboardingOverlay.shouldShowOnboarding(this)) {
                showOnboarding();
            }
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
            // Reduced offscreen limit from 3 to 1 to save memory, especially when multiple windows are open.
            viewPager.setOffscreenPageLimit(1);

            // Add smooth transition animation
            viewPager.setPageTransformer((page, position) -> {
                int pageWidth = page.getWidth();
                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    page.setAlpha(0f);
                } else if (position <= 0) { // [-1,0]
                    // Use the default slide transition when moving to the left page
                    page.setAlpha(1f);
                    page.setTranslationX(0f);
                    page.setScaleX(1f);
                    page.setScaleY(1f);
                } else if (position <= 1) { // (0,1]
                    // Fade the page out.
                    page.setAlpha(1 - position);
                    // Counteract the default slide transition
                    page.setTranslationX(pageWidth * -position);
                    // Scale the page down (between MIN_SCALE and 1)
                    float scaleFactor = 0.75f + (1 - 0.75f) * (1 - Math.abs(position));
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    page.setAlpha(0f);
                }
            });
            
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

                // Show context menu on long press
                customTabView.setOnLongClickListener(v -> {
                    showTabContextMenu(position);
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
    
    private void setupPlugins() {
        com.termux.plus.plugin.PluginManager manager = com.termux.plus.plugin.PluginManager.getInstance(this);
        com.termux.plus.api.TermuxPlugin plugin = manager.getPlugin("com.termux.plus.claude");
        if (plugin instanceof com.termux.plus.plugin.impl.ClaudePlugin) {
            claudePlugin = (com.termux.plus.plugin.impl.ClaudePlugin) plugin;
        } else {
            claudePlugin = new com.termux.plus.plugin.impl.ClaudePlugin();
            claudePlugin.onInit(this);
        }

        binding.claudeStatusOverlay.setVisibility(View.GONE);

        claudePlugin.setAIListener(new com.termux.plus.api.AIProvider.AIListener() {
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
            public void onFileGenerated(String filePath, String action) {}

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
                runOnUiThread(() -> binding.claudeStatusText.setText("🤖 Claude: Tokens " + used + "/" + total));
            }
        });
    }

    private void updateCurrentTabIcon(boolean active) {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < terminalTabs.size()) {
            TerminalTab tab = terminalTabs.get(currentItem);
            tab.setClaudeActive(active);
            TabLayout.Tab layoutTab = tabLayout.getTabAt(currentItem);
            if (layoutTab != null && layoutTab.getCustomView() != null) {
                ImageView tabIcon = layoutTab.getCustomView().findViewById(R.id.tab_icon);
                if (tabIcon != null) {
                    tabIcon.setImageResource(tab.getIcon());
                }
            }
        }
        
        if (active && fabClaudeCode.getVisibility() != View.VISIBLE) {
            fabClaudeCode.startAnimation(fabFadeIn);
            fabClaudeCode.setVisibility(View.VISIBLE);
        }
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
            showFilePicker();
        });

        binding.btnVoiceInput.setOnClickListener(v -> {
            // Show voice input for Claude
            showVoiceInputDialog();
        });

        binding.btnQuickCommands.setOnClickListener(v -> {
            // Show quick command templates dialog
            showQuickCommandsMenu();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a command...");
        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show file picker dialog for selecting files to add as Claude context
     */
    private void showFilePicker() {
        TerminalTab currentTab = getCurrentTab();
        String startDir = currentTab != null ? currentTab.getWorkingDirectory() : getDefaultDirectory();

        FilePickerDialog dialog = FilePickerDialog.newInstance(startDir);
        dialog.setCallback(files -> {
            if (files.isEmpty()) return;

            // Build @-tag command for selected files
            StringBuilder command = new StringBuilder();
            command.append("# Selected files for Claude context:\n");

            for (java.io.File file : files) {
                command.append("# @").append(file.getName()).append("\n");
            }

            // Send to terminal as a comment showing selected files
            TerminalFragment currentFragment = getCurrentTerminalFragment();
            if (currentFragment != null) {
                // Build file list string
                StringBuilder fileListBuilder = new StringBuilder();
                StringBuilder echoCmdBuilder = new StringBuilder();
                echoCmdBuilder.append("echo 'Selected ").append(files.size()).append(" file(s) for context:'");

                for (int i = 0; i < files.size(); i++) {
                    java.io.File f = files.get(i);
                    if (i > 0) fileListBuilder.append(", ");
                    fileListBuilder.append(f.getName());
                    echoCmdBuilder.append(" && echo '  - ").append(f.getName()).append("'");
                }

                Toast.makeText(this, "Selected: " + fileListBuilder.toString(), Toast.LENGTH_LONG).show();
                currentFragment.sendCommand(echoCmdBuilder.toString());
            }
        });

        dialog.show(getSupportFragmentManager(), "file_picker");
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
    
    private void createNewTab(String name, String workingDirectory, String projectType) {
        if (terminalTabs.size() >= MAX_TABS) {
            Toast.makeText(this, "Maximum " + MAX_TABS + " tabs allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TerminalTab newTab = new TerminalTab(name, workingDirectory, projectType);
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
                createNewTab(name, directory, projectType);
            }
        });
        dialog.show(getSupportFragmentManager(), "new_tab");
    }
    
    private void showClaudeQuickActions(TerminalTab tab) {
        ClaudeQuickActionsDialog dialog = new ClaudeQuickActionsDialog();
        dialog.setTab(tab);

        // Set up callback to handle quick actions
        dialog.setCallback(new ClaudeQuickActionsDialog.QuickActionCallback() {
            @Override
            public void onSendCommand(String command) {
                TerminalFragment currentFragment = getCurrentTerminalFragment();
                if (currentFragment != null) {
                    currentFragment.sendCommand(command);
                }
            }

            @Override
            public void onStopClaude() {
                TerminalFragment currentFragment = getCurrentTerminalFragment();
                if (currentFragment != null) {
                    currentFragment.sendInterrupt();
                }
            }

            @Override
            public void onVoiceInputRequested() {
                showVoiceInputDialog();
            }

            @Override
            public void onModelSelectionRequested() {
                Intent intent = new Intent(TabbedTerminalActivity.this, TermuxPlusSettingsActivity.class);
                startActivity(intent);
            }
        });

        dialog.show(getSupportFragmentManager(), "claude_actions");
    }

    /**
     * Get the currently active TerminalFragment
     */
    public TerminalFragment getCurrentTerminalFragment() {
        return (TerminalFragment) getSupportFragmentManager()
            .findFragmentByTag("f" + viewPager.getCurrentItem());
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
        } else if (id == R.id.action_new_window) {
            Intent intent = new Intent(this, TabbedTerminalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("SESSION_ID", UUID.randomUUID().toString());
            startActivity(intent);
            return true;
        } else if (id == R.id.action_close_tab) {
            closeTab(viewPager.getCurrentItem());
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, TermuxPlusSettingsActivity.class));
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

    /**
     * Show onboarding overlay for new users or when requested from settings
     */
    private void showOnboarding() {
        OnboardingOverlay onboarding = new OnboardingOverlay();
        onboarding.setCallback(() -> {
            // Onboarding complete callback
            Toast.makeText(this, "Welcome to Termux+!", Toast.LENGTH_SHORT).show();
        });
        onboarding.show(getSupportFragmentManager(), "onboarding");
    }

    /**
     * Public method to trigger onboarding from settings
     */
    public void showOnboardingTutorial() {
        OnboardingOverlay.resetOnboarding(this);
        showOnboarding();
    }

    /**
     * Show context menu for a tab
     */
    private void showTabContextMenu(int position) {
        if (position < 0 || position >= terminalTabs.size()) return;

        TerminalTab tab = terminalTabs.get(position);
        TabContextMenuDialog dialog = TabContextMenuDialog.newInstance(
            position, tab.getName(), terminalTabs.size());

        dialog.setCallback(new TabContextMenuDialog.TabContextMenuCallback() {
            @Override
            public void onRenameTab(int pos, String newName) {
                renameTab(pos, newName);
            }

            @Override
            public void onDuplicateTab(int pos) {
                duplicateTab(pos);
            }

            @Override
            public void onCloseTab(int pos) {
                closeTab(pos);
            }

            @Override
            public void onCloseOtherTabs(int pos) {
                closeOtherTabs(pos);
            }

            @Override
            public void onCloseTabsToRight(int pos) {
                closeTabsToRight(pos);
            }
        });

        dialog.show(getSupportFragmentManager(), "tab_context_menu");
    }

    /**
     * Rename a tab
     */
    private void renameTab(int position, String newName) {
        if (position >= 0 && position < terminalTabs.size()) {
            TerminalTab tab = terminalTabs.get(position);
            tab.setName(newName);

            // Update the tab view
            TabLayout.Tab layoutTab = tabLayout.getTabAt(position);
            if (layoutTab != null && layoutTab.getCustomView() != null) {
                TextView tabTitle = layoutTab.getCustomView().findViewById(R.id.tab_title);
                if (tabTitle != null) {
                    tabTitle.setText(tab.getDisplayName());
                }
            }

            Toast.makeText(this, "Tab renamed to: " + newName, Toast.LENGTH_SHORT).show();
            saveTabs();
        }
    }

    /**
     * Duplicate a tab
     */
    private void duplicateTab(int position) {
        if (terminalTabs.size() >= MAX_TABS) {
            Toast.makeText(this, "Maximum " + MAX_TABS + " tabs allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position >= 0 && position < terminalTabs.size()) {
            TerminalTab originalTab = terminalTabs.get(position);
            String newName = originalTab.getName() + " (copy)";
            createNewTab(newName, originalTab.getWorkingDirectory(), originalTab.getProjectType());
            Toast.makeText(this, "Tab duplicated", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Close all tabs except the one at the given position
     */
    private void closeOtherTabs(int keepPosition) {
        if (keepPosition < 0 || keepPosition >= terminalTabs.size()) return;

        // Store the tab we want to keep
        TerminalTab tabToKeep = terminalTabs.get(keepPosition);

        // Remove all tabs except the one to keep
        terminalTabs.clear();
        terminalTabs.add(tabToKeep);

        // Refresh adapter
        tabAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);

        Toast.makeText(this, "Closed other tabs", Toast.LENGTH_SHORT).show();
        saveTabs();
    }

    /**
     * Close all tabs to the right of the given position
     */
    private void closeTabsToRight(int position) {
        if (position < 0 || position >= terminalTabs.size() - 1) return;

        int tabsToRemove = terminalTabs.size() - position - 1;

        // Remove tabs from the end to the right of position
        for (int i = 0; i < tabsToRemove; i++) {
            terminalTabs.remove(terminalTabs.size() - 1);
        }

        // Refresh adapter
        tabAdapter.notifyDataSetChanged();

        // Ensure current position is valid
        if (viewPager.getCurrentItem() >= terminalTabs.size()) {
            viewPager.setCurrentItem(terminalTabs.size() - 1);
        }

        Toast.makeText(this, "Closed " + tabsToRemove + " tab(s)", Toast.LENGTH_SHORT).show();
        saveTabs();
    }

    /**
     * Show a popup menu with quick command templates
     */
    private void showQuickCommandsMenu() {
        String[] commands = {
            "git status",
            "git diff",
            "git log --oneline -10",
            "ls -la",
            "pwd",
            "clear",
            "history",
            "npm run build",
            "python -m pytest",
            "./gradlew build"
        };

        String[] labels = {
            "📊 Git Status",
            "📝 Git Diff",
            "📜 Git Log (last 10)",
            "📁 List Files",
            "📍 Current Directory",
            "🧹 Clear Screen",
            "📋 Command History",
            "🔨 NPM Build",
            "🧪 Python Tests",
            "🏗️ Gradle Build"
        };

        new AlertDialog.Builder(this)
            .setTitle("Quick Commands")
            .setItems(labels, (dialog, which) -> {
                TerminalFragment fragment = getCurrentTerminalFragment();
                if (fragment != null) {
                    fragment.sendCommand(commands[which]);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private String getPrefsName() {
        if (sessionId == null || "default".equals(sessionId)) {
            return PREFS_NAME;
        }
        return PREFS_NAME + "_" + sessionId;
    }

    private void saveTabs() {
        if (ioExecutor == null || ioExecutor.isShutdown()) return;

        // Create a copy of the list to avoid ConcurrentModificationException if tabs change while saving
        final List<TerminalTab> tabsToSave = new ArrayList<>(terminalTabs);
        final String prefsName = getPrefsName();

        ioExecutor.execute(() -> {
            SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(tabsToSave);
            editor.putString(KEY_TABS, json);
            editor.apply(); // apply() is already async, but the JSON serialization above was the heavy part
        });
    }

    private void loadTabs() {
        try {
            SharedPreferences prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(KEY_TABS, null);
            
            // If this is a non-default session and no tabs exist yet, don't load default tabs
            if (json == null && !"default".equals(sessionId)) {
                createNewTab("home", getDefaultDirectory(), "Auto-detect");
                return;
            }

            Type type = new TypeToken<ArrayList<TerminalTab>>() {}.getType();
            List<TerminalTab> loadedTabs = gson.fromJson(json, type);

            if (loadedTabs != null && !loadedTabs.isEmpty()) {
                terminalTabs.clear();
                terminalTabs.addAll(loadedTabs);
                tabAdapter.notifyDataSetChanged();
            } else {
                // Create initial tab if no tabs were loaded
                createNewTab("home", getDefaultDirectory(), "Auto-detect");
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Failed to load tabs from SharedPreferences", e);
            // Create initial tab if loading fails
            createNewTab("home", getDefaultDirectory(), "Auto-detect");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (claudePlugin != null) {
            // Note: Plugin lifecycle is usually managed by PluginManager,
            // but we ensure listener is cleared here.
            claudePlugin.setAIListener(null);
        }

        if (shakeDetector != null) {
            shakeDetector.stop();
        }
        
        if (ioExecutor != null) {
            ioExecutor.shutdown();
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
     * Validates incoming intents to prevent malicious intent injection.
     *
     * @param intent The intent to validate
     * @return true if intent is valid or null, false if intent is malicious
     */
    private boolean validateIntent(Intent intent) {
        if (intent == null) {
            return true; // Null intent is fine (normal app launch)
        }

        // Get intent data
        android.net.Uri data = intent.getData();
        if (data == null) {
            return true; // No data URI, allow (normal launch or component intent)
        }

        // Validate scheme
        String scheme = data.getScheme();
        if (scheme == null) {
            Log.w(TAG, "Intent has null scheme");
            return false;
        }

        // Only allow our custom scheme
        if (!"termux-ai".equals(scheme)) {
            Log.w(TAG, "Invalid intent scheme: " + scheme);
            return false;
        }

        // Validate host if present
        String host = data.getHost();
        if (host != null) {
            // Define allowed hosts for termux-ai:// URLs
            // Example: termux-ai://open, termux-ai://new-tab
            String[] allowedHosts = {"open", "new-tab", "run", "settings"};
            boolean validHost = false;

            for (String allowedHost : allowedHosts) {
                if (allowedHost.equals(host)) {
                    validHost = true;
                    break;
                }
            }

            if (!validHost) {
                Log.w(TAG, "Invalid intent host: " + host);
                return false;
            }
        }

        // Additional validation: check for suspicious parameters
        String path = data.getPath();
        if (path != null) {
            // Block path traversal attempts
            if (path.contains("..") || path.contains("//")) {
                Log.w(TAG, "Suspicious path in intent: " + path);
                return false;
            }
        }

        // Validate action
        String action = intent.getAction();
        if (action != null) {
            // Only allow specific actions
            String[] allowedActions = {
                Intent.ACTION_MAIN,
                Intent.ACTION_VIEW,
                "android.intent.action.VIEW"
            };

            boolean validAction = false;
            for (String allowedAction : allowedActions) {
                if (allowedAction.equals(action)) {
                    validAction = true;
                    break;
                }
            }

            if (!validAction) {
                Log.w(TAG, "Suspicious intent action: " + action);
                return false;
            }
        }

        Log.d(TAG, "Intent validated successfully: " + data.toString());
        return true;
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
        
        public TerminalTab(String name, String workingDirectory, String projectType) {
            this.id = nextId++;
            this.name = name;
            this.workingDirectory = workingDirectory;
            if (projectType == null || projectType.equalsIgnoreCase("Auto-detect") || projectType.equalsIgnoreCase("General Terminal")) {
                this.projectType = detectProjectType(workingDirectory);
            } else {
                this.projectType = mapProjectTypeNameToId(projectType);
            }
            this.claudeActive = false;
            this.lastActivityTime = System.currentTimeMillis();
        }

        private String mapProjectTypeNameToId(String name) {
            if (name.toLowerCase().contains("node")) return "nodejs";
            if (name.toLowerCase().contains("python")) return "python";
            if (name.toLowerCase().contains("rust")) return "rust";
            if (name.toLowerCase().contains("go")) return "go";
            if (name.toLowerCase().contains("java")) return "java";
            return "general";
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