#!/bin/bash
set -e

echo "🔧 Building APK (no manual wrapper downloads)..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /project

# Make gradlew executable if it exists
[ -f "gradlew" ] && chmod +x gradlew

# Verify Android SDK is accessible
echo "📱 Verifying Android SDK..."
ls -la $ANDROID_HOME/platforms/
ls -la $ANDROID_HOME/build-tools/

# Build APK using wrapper or system gradle
echo "🔨 Building APK..."
if [ -f "gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Using Gradle wrapper..."
    ./gradlew assembleDebug --no-daemon --stacktrace
elif command -v gradle >/dev/null 2>&1; then
    echo "Using system Gradle..."
    gradle assembleDebug --no-daemon --stacktrace
else
    echo "❌ No Gradle installation found"
    exit 1
fi

echo "✅ Build completed! APK should be in app/build/outputs/apk/debug/"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Checking build output..."
find app/build/outputs -name "*.apk" -type f 2>/dev/null || echo "No APK found yet"