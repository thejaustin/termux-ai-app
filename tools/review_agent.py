#!/usr/bin/env python3
import os
import re
import sys

# Configuration
SOURCE_DIRS = ["app/src/main/java"]
EXCLUDE_DIRS = ["build", "generated"]
PATTERNS = [
    {
        "name": "Regex Compilation in Loop/Method",
        "pattern": r"Pattern\.compile\(",
        "exclude_pattern": r"static final|private static",
        "message": "Avoid compiling regex patterns inside methods. Define them as static constants."
    },
    {
        "name": "Hard Reference to Activity/Context",
        "pattern": r"(private|public|protected)\s+(static\s+)?(Activity|Context|TabbedTerminalActivity)\s+\w+;",
        "exclude_pattern": r"WeakReference",
        "message": "Avoid holding hard references to Activity or Context. Use WeakReference to prevent memory leaks."
    },
    {
        "name": "Main Thread Sleep",
        "pattern": r"Thread\.sleep\(",
        "message": "Avoid Thread.sleep() on the main thread. It causes UI freezes."
    },
    {
        "name": "Print Stack Trace",
        "pattern": r"\.printStackTrace\(",
        "message": "Avoid e.printStackTrace(). Use a logger instead."
    },
    {
        "name": "Generic Exception Catching",
        "pattern": r"catch\s*\(\s*Exception\s+\w+\s*\)",
        "message": "Avoid catching generic Exception. Catch specific exceptions."
    }
]

def scan_file(file_path):
    issues = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            for i, line in enumerate(lines):
                for rule in PATTERNS:
                    if re.search(rule["pattern"], line):
                        # Check exclusions
                        if "exclude_pattern" in rule and re.search(rule["exclude_pattern"], line):
                            continue
                        
                        issues.append({
                            "file": file_path,
                            "line": i + 1,
                            "rule": rule["name"],
                            "message": rule["message"],
                            "content": line.strip()
                        })
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
    return issues

def main():
    print("🤖 AI Code Review Agent Initialized...")
    all_issues = []
    
    for source_dir in SOURCE_DIRS:
        for root, dirs, files in os.walk(source_dir):
            # Filter excluded dirs
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
            
            for file in files:
                if file.endswith(".java") or file.endswith(".kt"):
                    file_path = os.path.join(root, file)
                    issues = scan_file(file_path)
                    all_issues.extend(issues)

    if all_issues:
        print(f"\n⚠️ Found {len(all_issues)} potential issues (Non-blocking):")
        for issue in all_issues:
            print(f"  • {issue['file']}:{issue['line']} - {issue['rule']}")
            print(f"    {issue['message']}")
            print(f"    Code: {issue['content']}\n")
        print("Build proceeding (review mode only).")
        sys.exit(0)
    else:
        print("\n✅ Codebase looks clean! No critical issues found.")
        sys.exit(0)

if __name__ == "__main__":
    main()
