#!/bin/bash
set -e

echo "🚀 Building APK with Gradle (wrapper or system)..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Make gradlew executable if it exists
[ -f "gradlew" ] && chmod +x gradlew

# Clean any previous builds
rm -rf app/build
rm -rf build

# Use wrapper first, fallback to system gradle
echo "🔨 Building APK..."

if [ -f "gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "📦 Using Gradle wrapper..."
    ./gradlew --version
    ./gradlew assembleDebug --no-daemon --stacktrace
elif command -v gradle >/dev/null 2>&1; then
    echo "📦 Using system Gradle..."
    gradle --version
    # Try offline first, then online if that fails
    echo "🌐 Trying offline build first..."
    gradle assembleDebug --offline --no-daemon --stacktrace --info || {
        echo "⚠️ Offline build failed, trying online build..."
        echo "📡 Building with online dependencies..."
        gradle assembleDebug --no-daemon --stacktrace --refresh-dependencies --info
    }
else
    echo "❌ No Gradle installation found"
    echo "Please install gradle: apt update && apt install -y gradle"
    exit 1
fi

echo "✅ Build completed!"
echo "🔍 Searching for APK files..."
find . -name "*.apk" -type f 2>/dev/null || echo "No APK found yet"

echo "📁 Checking app/build/outputs directory..."
ls -la app/build/outputs/ 2>/dev/null || echo "app/build/outputs not found"

echo "🎯 Checking for debug APK specifically..."
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Debug APK directory not found"