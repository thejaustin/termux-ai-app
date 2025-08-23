#!/bin/bash
set -e

echo "🔧 Using existing Gradle wrapper (no manual downloads)..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Make gradlew executable
chmod +x gradlew

# Check if wrapper exists and is valid
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "🔍 Verifying existing wrapper jar..."
    file gradle/wrapper/gradle-wrapper.jar
    
    if file gradle/wrapper/gradle-wrapper.jar | grep -q "Zip archive\|Java archive"; then
        echo "✅ Valid wrapper jar found"
    else
        echo "❌ Wrapper jar is not valid"
        echo "Please run: gradle wrapper --gradle-version 8.9"
        exit 1
    fi
else
    echo "❌ No wrapper jar found"
    echo "Please run: gradle wrapper --gradle-version 8.9"
    exit 1
fi

# Try using the wrapper or fallback to system gradle
echo "🧪 Testing Gradle wrapper..."
if ./gradlew --version; then
    echo "✅ Gradle wrapper is working!"
elif command -v gradle >/dev/null 2>&1; then
    echo "⚠️ Wrapper failed, using system gradle"
    gradle --version
else
    echo "❌ No working Gradle installation found"
    exit 1
fi