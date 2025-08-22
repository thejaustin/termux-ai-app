#!/bin/bash
set -e

echo "🚀 Building APK with direct Gradle approach..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Install Gradle if not present
if ! command -v gradle &> /dev/null; then
    echo "📦 Installing Gradle..."
    apt update && apt install -y gradle
fi

# Check Gradle version
echo "📋 Gradle version:"
gradle --version

# Clean any previous builds
rm -rf app/build
rm -rf build

# Use system gradle with explicit gradle version compatibility
echo "🔨 Building APK with system Gradle..."

# Try to work around dependency download issues by setting offline first, then online
echo "🌐 Trying offline build first..."
gradle assembleDebug --offline --no-daemon --stacktrace --info || {
    echo "⚠️ Offline build failed, trying online build..."
    echo "📡 Building with online dependencies..."
    gradle assembleDebug --no-daemon --stacktrace --refresh-dependencies --info
}

echo "✅ Build completed!"
echo "🔍 Searching for APK files..."
find . -name "*.apk" -type f 2>/dev/null || echo "No APK found yet"

echo "📁 Checking app/build/outputs directory..."
ls -la app/build/outputs/ 2>/dev/null || echo "app/build/outputs not found"

echo "🎯 Checking for debug APK specifically..."
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Debug APK directory not found"