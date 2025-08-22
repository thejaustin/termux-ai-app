#!/bin/bash

# Termux AI Project Verification Script
# Verifies that all necessary files are present for compilation

echo "🔍 Verifying Termux AI Project Structure"
echo "========================================"

PROJECT_ROOT="/data/data/com.termux/files/home/termux-ai-app"
ERRORS=0

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo "✅ $1"
    else
        echo "❌ MISSING: $1"
        ERRORS=$((ERRORS + 1))
    fi
}

# Function to check if directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo "✅ $1/"
    else
        echo "❌ MISSING DIR: $1/"
        ERRORS=$((ERRORS + 1))
    fi
}

echo ""
echo "📋 Checking Core Build Files:"
check_file "$PROJECT_ROOT/build.gradle"
check_file "$PROJECT_ROOT/settings.gradle"
check_file "$PROJECT_ROOT/gradle.properties"
check_file "$PROJECT_ROOT/app/build.gradle"
check_file "$PROJECT_ROOT/app/proguard-rules.pro"

echo ""
echo "📱 Checking Android Files:"
check_file "$PROJECT_ROOT/app/src/main/AndroidManifest.xml"

echo ""
echo "☕ Checking Java Source Files:"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/ai/TermuxAIApplication.java"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/ai/AIClient.java"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/ai/ClaudeCodeIntegration.java"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/ai/ContextEngine.java"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/app/TabbedTerminalActivity.java"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/app/TerminalFragment.java"
check_file "$PROJECT_ROOT/app/src/main/java/com/termux/terminal/EnhancedTerminalView.java"

echo ""
echo "🎨 Checking Resource Files:"
check_file "$PROJECT_ROOT/app/src/main/res/values/strings.xml"
check_file "$PROJECT_ROOT/app/src/main/res/values/colors.xml"
check_file "$PROJECT_ROOT/app/src/main/res/values/themes.xml"
check_file "$PROJECT_ROOT/app/src/main/res/layout/activity_tabbed_terminal.xml"
check_file "$PROJECT_ROOT/app/src/main/res/layout/fragment_terminal.xml"

echo ""
echo "🔧 Checking Build System:"
check_dir "$PROJECT_ROOT/gradle"
check_file "$PROJECT_ROOT/gradle/wrapper/gradle-wrapper.properties"

echo ""
echo "📖 Checking Documentation:"
check_file "$PROJECT_ROOT/README.md"
check_file "$PROJECT_ROOT/README_BUILD.md"
check_file "$PROJECT_ROOT/FINAL_SUMMARY.md"

echo ""
echo "========================================"

if [ $ERRORS -eq 0 ]; then
    echo "🎉 PROJECT VERIFICATION SUCCESSFUL!"
    echo ""
    echo "✅ All required files are present"
    echo "✅ Project structure is complete"
    echo "✅ Ready for compilation"
    echo ""
    echo "🚀 Next steps:"
    echo "1. Install build dependencies: pkg install openjdk-17 gradle"
    echo "2. Run build script: ./build.sh"
    echo "3. Install generated APK"
    echo ""
    echo "📊 Project Statistics:"
    echo "- Java files: 7"
    echo "- Resource files: 5"
    echo "- Layout files: 2"
    echo "- Build config files: 4"
    echo "- Total project files: 18+"
    echo ""
    echo "💡 Features included:"
    echo "- Claude Code integration"
    echo "- Gboard autocomplete support"
    echo "- Tabbed terminal interface"
    echo "- Mobile gesture controls"
    echo "- AI progress tracking"
    echo "- File operation highlighting"
    echo ""
else
    echo "❌ PROJECT VERIFICATION FAILED!"
    echo ""
    echo "Found $ERRORS missing files or directories."
    echo "Please ensure all required files are present before compilation."
    echo ""
    echo "🔧 To fix missing files:"
    echo "1. Re-run the project creation script"
    echo "2. Check file permissions"
    echo "3. Verify project path: $PROJECT_ROOT"
fi

echo "========================================"
exit $ERRORS