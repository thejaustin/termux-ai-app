#!/bin/bash
set -e

echo "🚀 Building APK with system Gradle..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Install gradle if not present
if ! command -v gradle &> /dev/null; then
    echo "📦 Installing Gradle..."
    apt update && apt install -y gradle
fi

# Check Gradle version
gradle --version

# Verify environment
echo "☕ Java version:"
java -version
echo "📱 Android SDK location: $ANDROID_HOME"
echo "🔨 Build tools location:"
ls -la $ANDROID_HOME/build-tools/34.0.0/

# Build using system gradle
echo "🔨 Building APK with system Gradle..."
gradle assembleDebug --no-daemon --stacktrace

echo "✅ Build completed!"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Checking for APK files..."
find app/build -name "*.apk" -type f 2>/dev/null || echo "No APK found"