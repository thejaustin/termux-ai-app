#!/bin/bash
set -e

echo "🚀 Building APK offline with wrapper..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Check if gradlew exists
if [ -f "gradlew" ]; then
    echo "📦 Using Gradle wrapper..."
    # Try offline build first
    echo "🔨 Trying offline build with Gradle wrapper..."
    ./gradlew assembleDebug --offline --no-daemon --stacktrace || {
        echo "⚠️ Offline build failed, trying online..."
        ./gradlew assembleDebug --no-daemon --stacktrace
    }
else
    echo "❌ No gradlew found"
    exit 1
fi

echo "✅ Build completed!"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Checking for APK files..."
find app/build -name "*.apk" -type f 2>/dev/null || echo "No APK found"