package com.termux.terminal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.ai.ClaudeCodeIntegration;
import com.termux.app.TabbedTerminalActivity;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced TerminalView for Termux AI
 * 
 * Key enhancements:
 * - Claude Code integration and detection
 * - Gboard autocomplete support
 * - Visual indicators for AI operations
 * - Enhanced text selection for mobile
 * - Progress tracking for Claude operations
 * - File operation highlighting
 */
public class EnhancedTerminalView extends TerminalView {
    private static final String TAG = "EnhancedTerminalView";
    
    // Claude Code integration
    private ClaudeCodeIntegration claudeIntegration;
    private boolean isClaudeCodeActive = false;
    private String currentClaudeOperation = "";
    private float claudeProgress = 0.0f;
    
    // Gboard integration
    private boolean gboardAutoCompleteEnabled = true;
    private InputMethodManager inputMethodManager;
    
    // Visual enhancements
    private Paint overlayPaint;
    private Paint progressPaint;
    private Paint highlightPaint;
    private List<FileHighlight> fileHighlights;
    
    // Gesture handling
    private GestureDetector gestureDetector;
    private Handler mainHandler;
    
    // Claude Code patterns
    private static final Pattern CLAUDE_START_PATTERN = Pattern.compile("claude code|claude-code");
    private static final Pattern CLAUDE_PROGRESS_PATTERN = Pattern.compile("\\[▓*░*\\]\\s*(\\d+)%");
    private static final Pattern CLAUDE_FILE_PATTERN = Pattern.compile("├─\\s*([^\\s]+)\\s*✨\\s*(NEW|MODIFIED)");
    
    public interface ClaudeCodeListener {
        void onClaudeCodeDetected();
        void onClaudeOperationStarted(String operation);
        void onClaudeProgressUpdated(float progress);
        void onClaudeFileGenerated(String filePath, String action);
        void onClaudeOperationCompleted();
        void onClaudeErrorDetected(String error);
        void onClaudeTokenUsageUpdated(int used, int total);
    }
    
    private ClaudeCodeListener claudeListener;
    
    public EnhancedTerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ... (imports)

public class EnhancedTerminalView extends TerminalView {
    // ... (fields)
    private ExecutorService backgroundExecutor;

    // ... (constructors)

    private void initialize() {
        claudeIntegration = new ClaudeCodeIntegration();
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        fileHighlights = new ArrayList<>();
        mainHandler = new Handler(Looper.getMainLooper());
        backgroundExecutor = Executors.newSingleThreadExecutor();

        // Make view focusable for keyboard input
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);

        initializePaints();
        setupGestureDetector();
        setupClaudeIntegration();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
        }
    }

    // ... (rest of the file until onTextChanged)

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        
        // Detect Claude Code activation
        // Fetch string on UI thread to ensure thread safety with emulator
        final String currentLine = getCurrentLine();
        
        if (currentLine != null && backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.execute(() -> {
                detectClaudeCode(currentLine);
                if (isClaudeCodeActive) {
                    parseClaudeOutput(currentLine);
                }
            });
        }
    }
    
    // ... (getCurrentLine)

    private void detectClaudeCode(String text) {
        Matcher matcher = CLAUDE_START_PATTERN.matcher(text.toLowerCase());
        if (matcher.find() && !isClaudeCodeActive) {
            mainHandler.post(() -> {
                if (!isClaudeCodeActive) { // Check again in case of race
                    isClaudeCodeActive = true;
                    invalidate();
                    if (claudeListener != null) {
                        claudeListener.onClaudeCodeDetected();
                    }
                    
                    // Show helpful toast
                    Toast.makeText(getContext(), 
                        "Claude Code detected! 🤖\nSwipe down to stop • Double-tap for history", 
                        Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // ... (rest of file)
    
    private void parseClaudeOutput(String text) {
        // Parse progress indicators
        Matcher progressMatcher = CLAUDE_PROGRESS_PATTERN.matcher(text);
        if (progressMatcher.find()) {
            try {
                int progress = Integer.parseInt(progressMatcher.group(1));
                claudeIntegration.updateProgress(progress / 100.0f);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        
        // Parse file operations
        Matcher fileMatcher = CLAUDE_FILE_PATTERN.matcher(text);
        if (fileMatcher.find()) {
            String filePath = fileMatcher.group(1);
            String action = fileMatcher.group(2);
            claudeIntegration.reportFileGenerated(filePath, action);
        }
    }
    
    private void addFileHighlight(String filePath, String action) {
        // Find the line containing this file path and highlight it
        FileHighlight highlight = new FileHighlight();
        highlight.filePath = filePath;
        highlight.action = action;
        highlight.timestamp = System.currentTimeMillis();
        
        // Calculate highlight rectangle (simplified - would need proper text measurement)
        highlight.rect = new Rect(0, 0, getWidth(), 40); // Placeholder
        
        fileHighlights.add(highlight);
        invalidate();
    }
    
    private void showClaudeHistory() {
        // Implementation would show a dialog with Claude message history
        Toast.makeText(getContext(), "Claude History (Double-tap feature)", Toast.LENGTH_SHORT).show();
    }
    
    private void stopClaudeOperation() {
        // Send Escape key to stop Claude operation
        sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
        sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE));
        
        Toast.makeText(getContext(), "Stopping Claude operation...", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Send a key event to the terminal
     * @param keyEvent The key event to send
     */
    private void sendKeyEvent(KeyEvent keyEvent) {
        // Dispatch the key event to be processed by the terminal
        dispatchKeyEvent(keyEvent);
    }
    
    private void startEnhancedTextSelection(MotionEvent e) {
        // Enhanced text selection for mobile - would implement better selection handles
        performLongClick();
    }
    
    // Setters and getters
    public void setClaudeCodeListener(ClaudeCodeListener listener) {
        this.claudeListener = listener;
    }
    
    public void setGboardAutoCompleteEnabled(boolean enabled) {
        this.gboardAutoCompleteEnabled = enabled;
        if (inputMethodManager != null) {
            inputMethodManager.restartInput(this);
        }
    }
    
    public boolean isClaudeCodeActive() {
        return isClaudeCodeActive;
    }
    
    public void forceClaudeCodeMode(boolean active) {
        this.isClaudeCodeActive = active;
        invalidate();
    }
    
    // Helper classes
    private static class FileHighlight {
        String filePath;
        String action;
        long timestamp;
        Rect rect;
    }
}