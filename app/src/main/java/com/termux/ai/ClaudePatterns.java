package com.termux.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central repository for Claude Code CLI detection patterns.
 *
 * This class consolidates all regex patterns used to detect and parse
 * Claude Code CLI output, eliminating code duplication and providing
 * a single source of truth for pattern matching.
 *
 * Note: These patterns are fragile and depend on Claude CLI output format.
 * A more robust solution would use structured output (JSON) from the CLI.
 */
public class ClaudePatterns {
    private static final String TAG = "ClaudePatterns";

    // Claude CLI activation detection
    public static final Pattern CLAUDE_START_PATTERN =
        Pattern.compile("claude(?:\\s+code)?|Claude Code CLI", Pattern.CASE_INSENSITIVE);

    // Progress bar parsing: [▓▓▓░░░] 45%
    public static final Pattern CLAUDE_PROGRESS_PATTERN =
        Pattern.compile("\\[([▓=]+)([░\\-]*)\\]\\s*(\\d+)%");

    // File operations: ├─ index.js ✨ NEW
    public static final Pattern CLAUDE_FILE_PATTERN =
        Pattern.compile("(?:├─|\\s*)(\\S+\\.\\w+)\\s*(?:✨|🆕|📝)\\s*(NEW|MODIFIED|CREATED|UPDATED)");

    // Error detection: ❌ Error: File not found
    public static final Pattern CLAUDE_ERROR_PATTERN =
        Pattern.compile("(?:❌|ERROR|Error)\\s*:?\\s*(.+)", Pattern.CASE_INSENSITIVE);

    // Token usage: 150 tokens used
    public static final Pattern CLAUDE_TOKEN_PATTERN =
        Pattern.compile("(\\d+)\\s*tokens?\\s*(?:used|consumed)", Pattern.CASE_INSENSITIVE);

    /**
     * Extract progress percentage from output containing progress bar.
     *
     * @param output Terminal output line
     * @return Progress percentage (0-100), or -1 if not found
     */
    public static int extractProgress(String output) {
        if (output == null) {
            return -1;
        }

        Matcher matcher = CLAUDE_PROGRESS_PATTERN.matcher(output);
        if (matcher.find()) {
            String percentStr = matcher.group(3);
            try {
                int percent = Integer.parseInt(percentStr);
                return Math.max(0, Math.min(100, percent)); // Clamp to 0-100
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Extract file operation details from output.
     *
     * @param output Terminal output line
     * @return FileOperation object if found, null otherwise
     */
    public static FileOperation extractFileOperation(String output) {
        if (output == null) {
            return null;
        }

        Matcher matcher = CLAUDE_FILE_PATTERN.matcher(output);
        if (matcher.find()) {
            String fileName = matcher.group(1);
            String operation = matcher.group(2);
            return new FileOperation(fileName, operation);
        }
        return null;
    }

    /**
     * Extract error message from output.
     *
     * @param output Terminal output line
     * @return Error message if found, null otherwise
     */
    public static String extractError(String output) {
        if (output == null) {
            return null;
        }

        Matcher matcher = CLAUDE_ERROR_PATTERN.matcher(output);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Extract token count from output.
     *
     * @param output Terminal output line
     * @return Token count if found, -1 otherwise
     */
    public static int extractTokens(String output) {
        if (output == null) {
            return -1;
        }

        Matcher matcher = CLAUDE_TOKEN_PATTERN.matcher(output);
        if (matcher.find()) {
            String tokensStr = matcher.group(1);
            try {
                return Integer.parseInt(tokensStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Check if output indicates Claude Code CLI is starting.
     *
     * @param output Terminal output line
     * @return true if Claude CLI startup detected
     */
    public static boolean isClaudeStart(String output) {
        if (output == null) {
            return false;
        }
        return CLAUDE_START_PATTERN.matcher(output).find();
    }

    /**
     * Represents a file operation detected in Claude CLI output.
     */
    public static class FileOperation {
        private final String fileName;
        private final String operation; // NEW, MODIFIED, CREATED, UPDATED

        public FileOperation(String fileName, String operation) {
            this.fileName = fileName;
            this.operation = operation;
        }

        public String getFileName() {
            return fileName;
        }

        public String getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            return operation + ": " + fileName;
        }
    }
}
