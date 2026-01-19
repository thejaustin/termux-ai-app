package com.termux.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for filtering sensitive information from commands and terminal output
 * before it is sent to external AI providers.
 */
public class PrivacyGuard {

    private static final List<Pattern> SENSITIVE_PATTERNS = new ArrayList<>();

    static {
        // API Keys and Tokens - Using simple ranges to avoid backslash escaping issues
        // Pattern matches key=value or key:value with optional quotes
        SENSITIVE_PATTERNS.add(Pattern.compile("(?i)(api[_-]?key|auth[_-]?token|secret|password|passwd)[:=][ \t]*['"]?([a-zA-Z0-9]{16,})['"]?"));
        
        // AWS Keys
        SENSITIVE_PATTERNS.add(Pattern.compile("AKIA[0-9A-Z]{16}"));
        
        // SSH Keys (partial match for detection)
        SENSITIVE_PATTERNS.add(Pattern.compile("-----BEGIN [A-Z ]+ PRIVATE KEY-----"));
    }

    /**
     * Filter sensitive information from a string.
     * Replaces detected sensitive data with [REDACTED].
     */
    public static String filter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String filtered = input;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(filtered);
            if (matcher.find()) {
                // If it's a key-value pair, we only want to redact the value part
                if (matcher.groupCount() >= 2) {
                    String fullMatch = matcher.group(0);
                    String value = matcher.group(2);
                    String redacted = fullMatch.replace(value, "[REDACTED]");
                    filtered = filtered.replace(fullMatch, redacted);
                } else {
                    filtered = matcher.replaceAll("[REDACTED]");
                }
            }
        }
        return filtered;
    }

    /**
     * Specifically filter commands before sending to AI.
     */
    public static String filterCommand(String command) {
        if (command == null) return null;
        
        // Redact common sensitive command patterns
        // e.g., git push https://user:password@github.com...
        String filtered = command.replaceAll("(http|https)://[^:]+:[^@]+@", "$1://[REDACTED]@");
        
        // Pass through general filters
        return filter(filtered);
    }
}
