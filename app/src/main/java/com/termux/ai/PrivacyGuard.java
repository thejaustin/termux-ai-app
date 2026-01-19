package com.termux.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivacyGuard {
    private static final List<Pattern> SENSITIVE_PATTERNS = new ArrayList<>();

    static {
        // Redact patterns that look like key=value or key:value
        SENSITIVE_PATTERNS.add(Pattern.compile("(?i)(api[_-]?key|auth[_-]?token|secret|password|passwd)[:=][ \t]*[a-zA-Z0-9]{16,}"));
        
        // AWS Keys
        SENSITIVE_PATTERNS.add(Pattern.compile("AKIA[0-9A-Z]{16}"));
        
        // SSH Keys
        SENSITIVE_PATTERNS.add(Pattern.compile("-----BEGIN [A-Z ]+ PRIVATE KEY-----"));
    }

    public static String filter(String input) {
        if (input == null || input.isEmpty()) return input;
        String filtered = input;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            filtered = pattern.matcher(filtered).replaceAll("[REDACTED]");
        }
        return filtered;
    }

    public static String filterCommand(String command) {
        if (command == null) return null;
        // Redact user:password in URLs
        String filtered = command.replaceAll("(http|https)://[^:]+:[^@]+@", "$1://[REDACTED]@");
        return filter(filtered);
    }
}
