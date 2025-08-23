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

# Choose build method: wrapper first, then system gradle
if [ -f "gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "📦 Using Gradle wrapper..."
    ./gradlew --version
    echo "🔨 Building APK with wrapper..."
    ./gradlew assembleDebug --no-daemon --stacktrace
elif command -v gradle >/dev/null 2>&1; then
    echo "📦 Using system Gradle..."
    gradle --version
    echo "🔨 Building APK with system Gradle..."
    gradle assembleDebug --no-daemon --stacktrace
else
    echo "❌ No Gradle installation found"
    echo "Please install gradle: apt update && apt install -y gradle"
    exit 1
fi

echo "✅ Build completed!"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Checking for APK files..."
find app/build -name "*.apk" -type f 2>/dev/null || echo "No APK found"