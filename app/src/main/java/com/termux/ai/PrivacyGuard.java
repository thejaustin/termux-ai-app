package com.termux.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivacyGuard {
    private static final List<Pattern> SENSITIVE_PATTERNS = new ArrayList<>();

    static {
        // Simple patterns to avoid escaping issues for now
        SENSITIVE_PATTERNS.add(Pattern.compile("password[:=]\s*\w+", Pattern.CASE_INSENSITIVE));
        SENSITIVE_PATTERNS.add(Pattern.compile("key[:=]\s*\w+", Pattern.CASE_INSENSITIVE));
    }

    public static String filter(String input) {
        if (input == null) return null;
        String filtered = input;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            filtered = pattern.matcher(filtered).replaceAll("[REDACTED]");
        }
        return filtered;
    }

    public static String filterCommand(String command) {
        return filter(command);
    }
}