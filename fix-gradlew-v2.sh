#!/bin/bash
set -e

echo "🔧 Using existing Gradle wrapper (no manual downloads v2)..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Make gradlew executable
chmod +x gradlew

# Check if wrapper exists and is valid
echo "🔍 Checking existing wrapper jar..."
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    file gradle/wrapper/gradle-wrapper.jar
    
    # Check if it's a valid ZIP/JAR file
    if file gradle/wrapper/gradle-wrapper.jar | grep -q "Java archive\|Zip archive"; then
        echo "✅ Valid JAR file found"
    else
        echo "❌ Existing file is not a valid JAR"
        echo "Please regenerate wrapper with: gradle wrapper --gradle-version 8.9"
        exit 1
    fi
else
    echo "❌ No wrapper jar found"
    echo "Please generate wrapper with: gradle wrapper --gradle-version 8.9"
    exit 1
fi

# Try a simple gradle command to test
echo "🧪 Testing Gradle wrapper..."
if ./gradlew --version; then
    echo "✅ Gradle wrapper is working!"
elif command -v gradle >/dev/null 2>&1; then
    echo "⚠️ Wrapper test failed, but system gradle is available"
    gradle --version
else
    echo "❌ No working Gradle installation found"
    exit 1
fi

echo "✅ Gradle setup validated!"