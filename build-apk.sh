#!/bin/bash
set -e

echo "🔧 Fixing Gradle wrapper and building APK..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /project

# Download correct Gradle wrapper JAR
echo "📦 Downloading correct Gradle wrapper JAR..."
rm -f gradle/wrapper/gradle-wrapper.jar
curl -L https://services.gradle.org/distributions/gradle-8.10.2-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar

# Make gradlew executable
chmod +x gradlew

# Verify Android SDK is accessible
echo "📱 Verifying Android SDK..."
ls -la $ANDROID_HOME/platforms/
ls -la $ANDROID_HOME/build-tools/

# Build APK
echo "🔨 Building APK..."
./gradlew assembleDebug --no-daemon --stacktrace

echo "✅ Build completed! APK should be in app/build/outputs/apk/debug/"
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Checking build output..."
find app/build/outputs -name "*.apk" -type f 2>/dev/null || echo "No APK found yet"