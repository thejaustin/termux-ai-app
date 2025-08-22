package com.termux.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.termux.ai.R;
import com.termux.terminal.EnhancedTerminalView;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

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
    private TabbedTerminalActivity parentActivity;
    
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabbedTerminalActivity) {
            parentActivity = (TabbedTerminalActivity) context;
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
        // Enable Gboard autocomplete
        terminalView.setGboardAutoCompleteEnabled(true);
        
        // Set Claude Code listener
        terminalView.setClaudeCodeListener(new EnhancedTerminalView.ClaudeCodeListener() {
            @Override
            public void onClaudeCodeDetected() {
                if (parentActivity != null) {
                    // Update tab to show Claude is active
                    TabbedTerminalActivity.TerminalTab tab = parentActivity.getTab(tabIndex);
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
                if (parentActivity != null) {
                    TabbedTerminalActivity.TerminalTab tab = parentActivity.getTab(tabIndex);
                    if (tab != null) {
                        tab.setClaudeActive(false);
                    }
                }
                Toast.makeText(getContext(), "✅ Claude operation completed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createTerminalSession() {
        String[] env = buildEnvironment();
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
    
    private String[] buildEnvironment() {
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
            // Change to working directory
            terminalSession.write("cd \"" + workingDirectory + "\"\r");
            
            // Clear the screen
            terminalSession.write("clear\r");
            
            // Show welcome message
            terminalSession.write("echo 'Welcome to Termux AI - " + tabName + "'\r");
            terminalSession.write("echo 'Type \"claude code\" to start AI-enhanced coding'\r");
            terminalSession.write("echo 'Gestures: Swipe down=stop, Double-tap=history'\r");
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
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Terminal", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPasteTextFromClipboard(@NonNull TerminalSession session) {
        // Handle clipboard paste
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String text = item.getText().toString();
            terminalSession.write(text);
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