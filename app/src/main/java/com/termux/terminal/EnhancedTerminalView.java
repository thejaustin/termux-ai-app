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
import androidx.core.content.ContextCompat;

import com.termux.ai.ClaudeCodeIntegration;
import com.termux.ai.R;
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
    }
    
    private ClaudeCodeListener claudeListener;
    
    public EnhancedTerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    
    private void initialize() {
        claudeIntegration = new ClaudeCodeIntegration();
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        fileHighlights = new ArrayList<>();
        mainHandler = new Handler(Looper.getMainLooper());
        
        initializePaints();
        setupGestureDetector();
        setupClaudeIntegration();
    }
    
    private void initializePaints() {
        overlayPaint = new Paint();
        overlayPaint.setAntiAlias(true);
        overlayPaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_overlay));
        
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_green));
        progressPaint.setStrokeWidth(8);
        
        highlightPaint = new Paint();
        highlightPaint.setAntiAlias(true);
        highlightPaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_highlight));
    }
    
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isClaudeCodeActive) {
                    showClaudeHistory();
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Swipe down to stop Claude operation (replaces Escape key)
                if (e2.getY() - e1.getY() > 100 && Math.abs(velocityY) > 1000) {
                    if (isClaudeCodeActive && !currentClaudeOperation.isEmpty()) {
                        stopClaudeOperation();
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public void onLongPress(MotionEvent e) {
                // Enhanced text selection for mobile
                startEnhancedTextSelection(e);
            }
        });
    }
    
    private void setupClaudeIntegration() {
        claudeIntegration.setListener(new ClaudeCodeIntegration.ClaudeIntegrationListener() {
            @Override
            public void onOperationDetected(String operation) {
                mainHandler.post(() -> {
                    currentClaudeOperation = operation;
                    if (claudeListener != null) {
                        claudeListener.onClaudeOperationStarted(operation);
                    }
                });
            }
            
            @Override
            public void onProgressUpdated(float progress) {
                mainHandler.post(() -> {
                    claudeProgress = progress;
                    invalidate(); // Trigger redraw for progress bar
                    if (claudeListener != null) {
                        claudeListener.onClaudeProgressUpdated(progress);
                    }
                });
            }
            
            @Override
            public void onFileGenerated(String filePath, String action) {
                mainHandler.post(() -> {
                    addFileHighlight(filePath, action);
                    if (claudeListener != null) {
                        claudeListener.onClaudeFileGenerated(filePath, action);
                    }
                });
            }
            
            @Override
            public void onOperationCompleted() {
                mainHandler.post(() -> {
                    currentClaudeOperation = "";
                    claudeProgress = 0.0f;
                    invalidate();
                    if (claudeListener != null) {
                        claudeListener.onClaudeOperationCompleted();
                    }
                });
            }
        });
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // Enable Gboard autocomplete and suggestions
        if (gboardAutoCompleteEnabled) {
            outAttrs.inputType = InputType.TYPE_CLASS_TEXT 
                | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                | InputType.TYPE_TEXT_VARIATION_NORMAL;
            
            outAttrs.imeOptions = EditorInfo.IME_ACTION_UNSPECIFIED
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_FLAG_NO_FULLSCREEN;
                
            // Enable rich input features
            outAttrs.inputType |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            
            return new EnhancedInputConnection(this, true);
        }
        
        return super.onCreateInputConnection(outAttrs);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle gestures first
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw Claude Code enhancements
        drawClaudeOverlays(canvas);
        drawFileHighlights(canvas);
        drawProgressIndicator(canvas);
    }
    
    private void drawClaudeOverlays(Canvas canvas) {
        if (!isClaudeCodeActive) return;
        
        // Draw subtle overlay to indicate Claude Code mode
        int width = getWidth();
        int height = getHeight();
        
        // Top indicator bar
        canvas.drawRect(0, 0, width, 4, progressPaint);
        
        // Claude status text (top-right)
        if (!currentClaudeOperation.isEmpty()) {
            String statusText = "🤖 " + currentClaudeOperation;
            Paint textPaint = new Paint();
            textPaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_green));
            textPaint.setTextSize(24);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            
            Rect textBounds = new Rect();
            textPaint.getTextBounds(statusText, 0, statusText.length(), textBounds);
            
            canvas.drawText(statusText, width - textBounds.width() - 16, 32, textPaint);
        }
    }
    
    private void drawFileHighlights(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        
        // Remove expired highlights
        fileHighlights.removeIf(highlight -> 
            currentTime - highlight.timestamp > 5000); // 5 second fade
        
        // Draw remaining highlights
        for (FileHighlight highlight : fileHighlights) {
            float alpha = 1.0f - ((currentTime - highlight.timestamp) / 5000.0f);
            if (alpha > 0) {
                highlightPaint.setAlpha((int)(alpha * 68)); // Fade out
                canvas.drawRect(highlight.rect, highlightPaint);
            }
        }
        
        if (!fileHighlights.isEmpty()) {
            invalidate(); // Continue animation
        }
    }
    
    private void drawProgressIndicator(Canvas canvas) {
        if (claudeProgress > 0 && claudeProgress < 1.0f) {
            int width = getWidth();
            int progressWidth = (int)(width * claudeProgress);
            
            // Progress bar at bottom
            canvas.drawRect(0, getHeight() - 8, progressWidth, getHeight(), progressPaint);
            
            // Progress text
            String progressText = (int)(claudeProgress * 100) + "%";
            Paint textPaint = new Paint();
            textPaint.setColor(ContextCompat.getColor(getContext(), R.color.ai_green));
            textPaint.setTextSize(32);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            
            Rect textBounds = new Rect();
            textPaint.getTextBounds(progressText, 0, progressText.length(), textBounds);
            
            canvas.drawText(progressText, 
                (width - textBounds.width()) / 2, 
                getHeight() - 16, 
                textPaint);
        }
    }
    
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        
        // Detect Claude Code activation
        String currentLine = getCurrentLine();
        if (currentLine != null) {
            detectClaudeCode(currentLine);
            if (isClaudeCodeActive) {
                parseClaudeOutput(currentLine);
            }
        }
    }
    
    private String getCurrentLine() {
        try {
            TerminalSession session = getCurrentSession();
            if (session != null) {
                TerminalEmulator emulator = session.getEmulator();
                if (emulator != null) {
                    return emulator.getScreen().getSelectedText(0, emulator.getCursorRow(), 
                        emulator.getScreen().getColumns(), emulator.getCursorRow());
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }
    
    private void detectClaudeCode(String text) {
        Matcher matcher = CLAUDE_START_PATTERN.matcher(text.toLowerCase());
        if (matcher.find() && !isClaudeCodeActive) {
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
    }
    
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
    
    /**
     * Enhanced InputConnection for better Gboard integration
     */
    private static class EnhancedInputConnection extends TerminalViewInputConnection {
        public EnhancedInputConnection(TerminalView terminalView, boolean fullEditor) {
            super(terminalView, fullEditor);
        }
        
        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            // Handle Gboard autocomplete suggestions
            return super.commitText(text, newCursorPosition);
        }
        
        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            // Handle Gboard composing text (predictive text)
            return super.setComposingText(text, newCursorPosition);
        }
    }
}