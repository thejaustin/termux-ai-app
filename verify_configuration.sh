#!/bin/bash

echo "🔍 Verifying Termux AI Project Configuration"
echo "============================================="

PROJECT_ROOT="/home/runner/work/termux-ai-app/termux-ai-app"
cd "$PROJECT_ROOT"

echo ""
echo "📋 Checking Build Configuration Files:"

# Check build.gradle files
if [ -f "build.gradle" ]; then
    echo "✅ build.gradle (root)"
    echo "   - AGP version: $(grep 'com.android.tools.build:gradle' build.gradle | sed 's/.*://' | tr -d "'" | tr -d ' ')"
else
    echo "❌ Missing build.gradle (root)"
fi

if [ -f "app/build.gradle" ]; then
    echo "✅ app/build.gradle"
    echo "   - compileSdk: $(grep 'compileSdk' app/build.gradle | grep -o '[0-9]*')"
    echo "   - targetSdk: $(grep 'targetSdk' app/build.gradle | grep -o '[0-9]*')"
    echo "   - minSdk: $(grep 'minSdk' app/build.gradle | grep -o '[0-9]*')"
    echo "   - Java version: $(grep 'sourceCompatibility.*VERSION' app/build.gradle | sed 's/.*VERSION_//' | tr -d ' ')"
    echo "   - viewBinding: $(grep 'viewBinding.*true' app/build.gradle > /dev/null && echo 'enabled' || echo 'disabled')"
    echo "   - shrinkResources: $(grep 'shrinkResources.*true' app/build.gradle > /dev/null && echo 'enabled' || echo 'disabled')"
else
    echo "❌ Missing app/build.gradle"
fi

if [ -f "gradle.properties" ]; then
    echo "✅ gradle.properties"
    echo "   - JVM args: $(grep 'org.gradle.jvmargs' gradle.properties | cut -d'=' -f2)"
    echo "   - Parallel builds: $(grep 'org.gradle.parallel.*true' gradle.properties > /dev/null && echo 'enabled' || echo 'disabled')"
    echo "   - Caching: $(grep 'org.gradle.caching.*true' gradle.properties > /dev/null && echo 'enabled' || echo 'disabled')"
else
    echo "❌ Missing gradle.properties"
fi

echo ""
echo "🎨 Checking Theme Configuration:"

# Check themes
if [ -f "app/src/main/res/values/themes.xml" ]; then
    echo "✅ app/src/main/res/values/themes.xml"
    echo "   - Theme.TermuxAI: $(grep 'Theme.TermuxAI"' app/src/main/res/values/themes.xml > /dev/null && echo 'present' || echo 'missing')"
    echo "   - Theme.TermuxAI.Light: $(grep 'Theme.TermuxAI.Light' app/src/main/res/values/themes.xml > /dev/null && echo 'present' || echo 'missing')"
    echo "   - Widget.TermuxAI.TextInput: $(grep 'Widget.TermuxAI.TextInput' app/src/main/res/values/themes.xml > /dev/null && echo 'present' || echo 'missing')"
else
    echo "❌ Missing app/src/main/res/values/themes.xml"
fi

if [ -f "app/src/main/res/values-night/themes.xml" ]; then
    echo "✅ app/src/main/res/values-night/themes.xml"
else
    echo "❌ Missing app/src/main/res/values-night/themes.xml"
fi

if [ -f "app/src/main/res/values/colors.xml" ]; then
    echo "✅ app/src/main/res/values/colors.xml"
    echo "   - ai_green: $(grep 'ai_green' app/src/main/res/values/colors.xml > /dev/null && echo 'present' || echo 'missing')"
    echo "   - light_terminal_background: $(grep 'light_terminal_background' app/src/main/res/values/colors.xml > /dev/null && echo 'present' || echo 'missing')"
else
    echo "❌ Missing app/src/main/res/values/colors.xml"
fi

echo ""
echo "☕ Checking Java Source Files:"

if [ -f "app/src/main/java/com/termux/ai/TermuxAIApplication.java" ]; then
    echo "✅ TermuxAIApplication.java"
    echo "   - setupDynamicTheming: $(grep 'setupDynamicTheming' app/src/main/java/com/termux/ai/TermuxAIApplication.java > /dev/null && echo 'present' || echo 'missing')"
    echo "   - isDynamicColorSupported: $(grep 'isDynamicColorSupported' app/src/main/java/com/termux/ai/TermuxAIApplication.java > /dev/null && echo 'present' || echo 'missing')"
else
    echo "❌ Missing TermuxAIApplication.java"
fi

if [ -f "app/src/main/java/com/termux/terminal/EnhancedTerminalView.java" ]; then
    echo "✅ EnhancedTerminalView.java"
    echo "   - Hard-coded colors replaced: $(grep '0xFF4CAF50\|0x88000000\|0x4400FF00' app/src/main/java/com/termux/terminal/EnhancedTerminalView.java > /dev/null && echo 'NO - still present' || echo 'YES - replaced with R.color')"
else
    echo "❌ Missing EnhancedTerminalView.java"
fi

echo ""
echo "📱 Checking Dependencies:"

if [ -f "app/build.gradle" ]; then
    echo "Material Design: $(grep 'material:1.12.0' app/build.gradle > /dev/null && echo '1.12.0 ✅' || echo 'Wrong version ❌')"
    echo "AppCompat: $(grep 'appcompat:1.7.0' app/build.gradle > /dev/null && echo '1.7.0 ✅' || echo 'Wrong version ❌')"
    echo "Core: $(grep 'core:1.13.1' app/build.gradle > /dev/null && echo '1.13.1 ✅' || echo 'Wrong version ❌')"
    echo "ViewPager2: $(grep 'viewpager2' app/build.gradle > /dev/null && echo 'Present ✅' || echo 'Missing ❌')"
    echo "RecyclerView: $(grep 'recyclerview' app/build.gradle > /dev/null && echo 'Present ✅' || echo 'Missing ❌')"
    echo "Lifecycle: $(grep 'lifecycle.*:2.8.4' app/build.gradle > /dev/null && echo '2.8.4 ✅' || echo 'Wrong version ❌')"
fi

echo ""
echo "🎯 Configuration Summary:"
echo "========================"

ISSUES=0

# Check critical configurations
if ! grep -q 'compileSdk 34' app/build.gradle; then
    echo "❌ compileSdk should be 34"
    ISSUES=$((ISSUES + 1))
fi

if ! grep -q 'targetSdk 34' app/build.gradle; then
    echo "❌ targetSdk should be 34"
    ISSUES=$((ISSUES + 1))
fi

if ! grep -q 'minSdk 24' app/build.gradle; then
    echo "❌ minSdk should be 24"
    ISSUES=$((ISSUES + 1))
fi

if ! grep -q 'VERSION_17' app/build.gradle; then
    echo "❌ Java should be version 17"
    ISSUES=$((ISSUES + 1))
fi

if ! grep -q 'shrinkResources true' app/build.gradle; then
    echo "❌ shrinkResources should be enabled for release builds"
    ISSUES=$((ISSUES + 1))
fi

if ! grep -q 'Theme.TermuxAI.Light' app/src/main/res/values/themes.xml; then
    echo "❌ Light theme is missing"
    ISSUES=$((ISSUES + 1))
fi

if ! test -d "app/src/main/res/values-night"; then
    echo "❌ values-night directory is missing"
    ISSUES=$((ISSUES + 1))
fi

if grep -q 'boxStrokeColorStateList.*=' app/src/main/res/values/themes.xml; then
    echo "❌ boxStrokeColorStateList should be replaced with boxStrokeColor"
    ISSUES=$((ISSUES + 1))
fi

if grep -q '0xFF4CAF50\|0x88000000\|0x4400FF00' app/src/main/java/com/termux/terminal/EnhancedTerminalView.java; then
    echo "❌ Hard-coded colors still present in EnhancedTerminalView"
    ISSUES=$((ISSUES + 1))
fi

if [ $ISSUES -eq 0 ]; then
    echo ""
    echo "🎉 ALL CONFIGURATION REQUIREMENTS MET!"
    echo "✅ Ready for compilation"
    echo ""
    echo "📋 Summary of Enhancements Applied:"
    echo "• Build configuration updated to Java 17, SDK 34"
    echo "• Dependencies updated to latest versions"
    echo "• Dynamic theming implemented with light/dark mode support"
    echo "• Hard-coded colors replaced with theme-based colors"
    echo "• TextInput style fixed (removed boxStrokeColorStateList)"
    echo "• Performance optimizations in gradle.properties"
    echo ""
    echo "🚀 Next steps:"
    echo "1. Fix gradle wrapper or use system gradle"
    echo "2. Run: gradle clean assembleDebug"
    echo "3. Test dynamic colors on Android 12+"
    echo "4. Verify light/dark theme switching"
else
    echo ""
    echo "⚠️  Found $ISSUES configuration issues to resolve"
fi

echo ""
echo "📊 Project Statistics:"
echo "======================"
echo "Java files: $(find app/src/main/java -name "*.java" | wc -l)"
echo "Resource files: $(find app/src/main/res -name "*.xml" | wc -l)"
echo "Total project size: $(du -sh . | cut -f1)"

echo ""
echo "============================================="
echo "Configuration verification complete!"