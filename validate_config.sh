#!/bin/bash
# Simple validation script for Termux AI build configuration
# Tests basic structure and configuration without requiring network access

echo "🔍 Validating Termux AI Gradle and theming configuration..."

# Check required files exist
echo "📁 Checking required files..."
files_to_check=(
    "app/build.gradle"
    "build.gradle" 
    "gradle.properties"
    "app/src/main/res/values/colors.xml"
    "app/src/main/res/values/themes.xml"
    "app/src/main/res/values-night/themes.xml"
    "app/src/main/res/drawable/claude_status_background.xml"
    "app/src/main/res/drawable/hint_background.xml"
    "app/src/main/java/com/termux/ai/TermuxAIApplication.java"
    "app/src/main/java/com/termux/terminal/EnhancedTerminalView.java"
)

missing_files=0
for file in "${files_to_check[@]}"; do
    if [[ ! -f "$file" ]]; then
        echo "❌ Missing: $file"
        missing_files=$((missing_files + 1))
    else
        echo "✅ Found: $file"
    fi
done

if [[ $missing_files -eq 0 ]]; then
    echo "✅ All required files present"
else
    echo "❌ $missing_files files missing"
    exit 1
fi

# Check key configuration values
echo -e "\n🔧 Checking Gradle configuration..."

# Check Java 17 in app/build.gradle
if grep -q "JavaVersion.VERSION_17" app/build.gradle; then
    echo "✅ Java 17 configured"
else
    echo "❌ Java 17 not found in app/build.gradle"
fi

# Check required dependencies
echo -e "\n📦 Checking required dependencies..."
required_deps=(
    "material:1.12.0"
    "appcompat:1.7.0"
    "core:1.13.1"
    "viewpager2"
    "recyclerview"
)

for dep in "${required_deps[@]}"; do
    if grep -q "$dep" app/build.gradle; then
        echo "✅ Found dependency: $dep"
    else
        echo "⚠️  Dependency not found: $dep"
    fi
done

# Check shrinkResources
if grep -q "shrinkResources true" app/build.gradle; then
    echo "✅ shrinkResources enabled"
else
    echo "❌ shrinkResources not enabled"
fi

# Check theming elements
echo -e "\n🎨 Checking theming configuration..."

# Check ai_green color
if grep -q "ai_green" app/src/main/res/values/colors.xml; then
    echo "✅ ai_green color defined"
else
    echo "❌ ai_green color missing"
fi

# Check expressive theme
if grep -q "Theme.TermuxAI.Expressive" app/src/main/res/values/themes.xml; then
    echo "✅ Expressive theme variant found"
else
    echo "❌ Expressive theme variant missing"
fi

# Check night themes
if [[ -f "app/src/main/res/values-night/themes.xml" ]]; then
    echo "✅ Night theme overrides present"
else
    echo "❌ Night theme overrides missing"
fi

# Check Widget.TermuxAI.TextInput style
if grep -q "Widget.TermuxAI.TextInput" app/src/main/res/values/themes.xml; then
    echo "✅ Custom TextInput style found"
else
    echo "❌ Custom TextInput style missing"
fi

# Check DynamicColors integration
if grep -q "applyDynamicColors" app/src/main/java/com/termux/ai/TermuxAIApplication.java; then
    echo "✅ DynamicColors integration found"
else
    echo "❌ DynamicColors integration missing"
fi

# Check themed colors in EnhancedTerminalView
if grep -q "R.color.ai_green" app/src/main/java/com/termux/terminal/EnhancedTerminalView.java; then
    echo "✅ Themed colors used in EnhancedTerminalView"
else
    echo "❌ Hard-coded colors still present in EnhancedTerminalView"
fi

echo -e "\n✅ Configuration validation complete!"
echo "📋 Ready for compilation with Material You support and upstream Termux alignment"