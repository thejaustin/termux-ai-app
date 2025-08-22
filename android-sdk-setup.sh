#!/bin/bash
set -e

echo "🚀 Continuing Android SDK setup and build..."

# Set environment with correct paths
export ANDROID_HOME=/home/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

# Verify Java is working
echo "☕ Java version:"
java -version

# Install Android SDK components  
echo "📱 Installing Android SDK components..."
printf 'y\ny\ny\ny\ny\ny\ny\n' | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses || true
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"

# Build project
echo "🔨 Building Android APK..."
cd /project
echo "sdk.dir=/home/android-sdk" > local.properties
chmod +x gradlew
./gradlew assembleDebug --no-daemon --stacktrace

echo "✅ Build completed! APK should be in app/build/outputs/apk/debug/"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "❌ APK directory not found"