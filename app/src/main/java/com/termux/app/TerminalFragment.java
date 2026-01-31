package com.termux.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.termux.ai.R;
import com.termux.terminal.EnhancedTerminalView;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.termux.plus.api.AIProvider;
import com.termux.plus.plugin.PluginManager;
import com.termux.plus.plugin.impl.ClaudePlugin;

import java.lang.ref.WeakReference;

/**
 * Fragment containing an enhanced terminal with Claude Code integration
 */
public class TerminalFragment extends Fragment implements TerminalSessionClient {
    private static final String ARG_TAB_NAME = "tab_name";
    private static final String ARG_WORKING_DIR = "working_dir";
    private static final String ARG_TAB_INDEX = "tab_index";
    
    private String tabName;
    private String workingDirectory;
    private int tabIndex;
    
    private EnhancedTerminalView terminalView;
    private TerminalSession terminalSession;
    private WeakReference<TabbedTerminalActivity> parentActivityRef;
    
    public static TerminalFragment newInstance(String tabName, String workingDirectory, int tabIndex) {
        TerminalFragment fragment = new TerminalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAB_NAME, tabName);
        args.putString(ARG_WORKING_DIR, workingDirectory);
        args.putInt(ARG_TAB_INDEX, tabIndex);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabName = getArguments().getString(ARG_TAB_NAME);
            workingDirectory = getArguments().getString(ARG_WORKING_DIR);
            tabIndex = getArguments().getInt(ARG_TAB_INDEX);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabbedTerminalActivity) {
            parentActivityRef = new WeakReference<>((TabbedTerminalActivity) context);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (parentActivityRef != null) {
            parentActivityRef.clear();
            parentActivityRef = null;
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        
        terminalView = view.findViewById(R.id.terminal_view);
        setupTerminalView();
        createTerminalSession();
        
        return view;
    }
    
    private void setupTerminalView() {
        // Disable autofill for the terminal view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            terminalView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        // Enable Gboard autocomplete
        terminalView.setGboardAutoCompleteEnabled(true);

        // Request focus and show keyboard when terminal is ready
        terminalView.post(() -> {
            terminalView.requestFocus();
            terminalView.showKeyboard();
        });
        
        // Setup AI Provider
        if (getContext() != null) {
            PluginManager manager = PluginManager.getInstance(getContext());
            // Use the first enabled AI provider
            List<AIProvider> providers = manager.getEnabledPluginsByType(AIProvider.class);
            if (!providers.isEmpty()) {
                terminalView.setTabIndex(tabIndex);
                terminalView.setAIProvider(providers.get(0));
            }
        }
        
        // Set Claude Code listener
        terminalView.setClaudeCodeListener(new EnhancedTerminalView.ClaudeCodeListener() {
            @Override
            public void onClaudeCodeDetected() {
                TabbedTerminalActivity activity = parentActivityRef != null ? parentActivityRef.get() : null;
                if (activity != null) {
                    // Update tab to show Claude is active
                    TabbedTerminalActivity.TerminalTab tab = activity.getTab(tabIndex);
                    if (tab != null) {
                        tab.setClaudeActive(true);
                    }
                }
            }
            
            @Override
            public void onClaudeOperationStarted(String operation) {
                Toast.makeText(getContext(), "Claude: " + operation, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onClaudeProgressUpdated(float progress) {
                // Progress is handled by the terminal view overlay
            }
            
            @Override
            public void onClaudeFileGenerated(String filePath, String action) {
                Toast.makeText(getContext(), "📁 " + action + ": " + filePath, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onClaudeOperationCompleted() {
                TabbedTerminalActivity activity = parentActivityRef != null ? parentActivityRef.get() : null;
                if (activity != null) {
                    TabbedTerminalActivity.TerminalTab tab = activity.getTab(tabIndex);
                    if (tab != null) {
                        tab.setClaudeActive(false);
                    }
                }
                Toast.makeText(getContext(), "✅ Claude operation completed", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onClaudeErrorDetected(String error) {
                Toast.makeText(getContext(), "❌ Claude Error: " + error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onClaudeTokenUsageUpdated(int used, int total) {
                // Update token usage display - could be shown in status bar
                String tokenInfo = used + "/" + total + " tokens";
                // For now, just log it - could be displayed in UI later
                if (getContext() != null && used > total * 0.8) {
                    Toast.makeText(getContext(), "⚠️ Token usage: " + tokenInfo, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void createTerminalSession() {
        String[] env = buildEnvironmentFromJson();
        String[] args = {"/system/bin/sh", "-"};
        
        terminalSession = new TerminalSession(
            "/system/bin/sh",
            workingDirectory,
            args,
            env,
            this
        );
        
        terminalView.attachSession(terminalSession);
        
        // Send initial setup commands
        sendInitialCommands();
    }
    
    private String[] buildEnvironmentFromJson() {
        try {
            InputStream is = getContext().getAssets().open("env.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            List<String> env = new ArrayList<>();
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                String value = jsonObject.getString(key);
                if (key.equals("HOME")) {
                    value = workingDirectory;
                }
                env.add(key + "=" + value);
            }
            env.add("TERMUX_AI_TAB=" + tabName);
            return env.toArray(new String[0]);
        } catch (Exception e) {
            Log.e("TermuxAI", "Failed to build environment from JSON", e);
            return buildEnvironment(); // fallback to default
        }
    }

    private String[] buildEnvironment() {
        // It would be better to make these environment variables configurable, so that the user can customize their terminal environment.
        // Build environment variables for the session
        return new String[]{
            "TERM=xterm-256color",
            "HOME=" + workingDirectory,
            "PATH=/system/bin:/system/xbin",
            "TMPDIR=/data/data/com.termux.ai/files/tmp",
            "SHELL=/system/bin/sh",
            "TERMUX_AI=1",
            "TERMUX_AI_TAB=" + tabName,
            "TERMUX_AI_VERSION=2.0.0"
        };
    }
    
    private void sendInitialCommands() {
        // Send commands to set up the terminal environment
        if (terminalSession != null) {
            try {
                // Change to working directory
                terminalSession.write("cd \"" + workingDirectory + "\"\r");

                // Clear the screen
                terminalSession.write("clear\r");

                // Show welcome message
                terminalSession.write("echo 'Welcome to Termux AI - " + tabName + "'\r");
                terminalSession.write("echo 'Type \"claude code\" to start AI-enhanced coding'\r");
                terminalSession.write("echo 'Gestures: Swipe down=stop, Double-tap=history'\r");
            } catch (Exception e) {
                Log.e("TermuxAI", "Failed to send initial commands", e);
            }
        }
    }
    
    public void shareTranscript() {
        if (terminalView != null) {
            String transcriptText = terminalView.getTranscriptText();
            if (transcriptText != null && !transcriptText.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, transcriptText);
                startActivity(Intent.createChooser(intent, "Share Transcript"));
            } else {
                Toast.makeText(getContext(), "No transcript to share", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isClaudeActive() {
        return terminalView != null && terminalView.isClaudeCodeActive();
    }
    
    public void forceClaudeMode(boolean active) {
        if (terminalView != null) {
            terminalView.forceClaudeCodeMode(active);
        }
    }
    
    public String getTabName() {
        return tabName;
    }
    
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    public int getTabIndex() {
        return tabIndex;
    }

    public void clearTerminal() {
        if (terminalSession != null) {
            terminalSession.write("clear\r");
        }
    }

    /**
     * Send a command to the terminal
     * @param command The command to execute (without trailing newline)
     */
    public void sendCommand(String command) {
        if (terminalSession != null && command != null) {
            terminalSession.write(command + "\r");
        }
    }

    /**
     * Send raw bytes to the terminal (for control characters like Ctrl+C)
     * @param bytes The bytes to send
     */
    public void sendBytes(byte[] bytes) {
        if (terminalSession != null && bytes != null) {
            terminalSession.write(new String(bytes));
        }
    }

    /**
     * Send interrupt signal (Ctrl+C) to the terminal
     */
    public void sendInterrupt() {
        if (terminalSession != null) {
            // Ctrl+C is ASCII 3 (ETX - End of Text)
            terminalSession.write("\u0003");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (terminalSession != null) {
            terminalSession.finishIfRunning();
        }
    }
    
    // TerminalSessionClient implementation
    @Override
    public void onTextChanged(@NonNull TerminalSession changedSession) {
        if (terminalView != null) {
            terminalView.onScreenUpdated();
        }
    }
    
    @Override
    public void onTitleChanged(@NonNull TerminalSession changedSession) {
        // Update tab title if needed
    }
    
    @Override
    public void onSessionFinished(@NonNull TerminalSession finishedSession) {
        // Handle session finish
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Terminal session ended", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    @Override
    public void onCopyTextToClipboard(@NonNull TerminalSession session, String text) {
        // Handle clipboard copy
        if (getContext() == null) return;
        android.content.ClipboardManager clipboard =
            (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            android.content.ClipData clip = android.content.ClipData.newPlainText("Terminal", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onPasteTextFromClipboard(@NonNull TerminalSession session) {
        // Handle clipboard paste
        if (getContext() == null) return;
        android.content.ClipboardManager clipboard =
            (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                String text = item.getText().toString();
                if (terminalSession != null) {
                    terminalSession.write(text);
                }
            }
        }
    }
    
    @Override
    public void onBell(@NonNull TerminalSession session) {
        // Handle terminal bell
    }
    
    @Override
    public void onColorsChanged(@NonNull TerminalSession session) {
        // Handle color changes
        if (terminalView != null) {
            terminalView.onScreenUpdated();
        }
    }
}