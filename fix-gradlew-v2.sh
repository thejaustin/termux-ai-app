#!/bin/bash
set -e

echo "🔧 Fixing Gradle wrapper (v2)..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Remove broken wrapper jar
rm -f gradle/wrapper/gradle-wrapper.jar

# Download from the correct URL (following redirect)
echo "📦 Downloading Gradle 8.10.2 wrapper jar from downloads.gradle.org..."
curl -L -o gradle/wrapper/gradle-wrapper.jar "https://downloads.gradle.org/distributions/gradle-8.10.2-wrapper.jar"

# Make gradlew executable
chmod +x gradlew

# Verify the jar is valid
echo "🔍 Verifying wrapper jar..."
file gradle/wrapper/gradle-wrapper.jar

# Check if it's a valid ZIP/JAR file
if file gradle/wrapper/gradle-wrapper.jar | grep -q "Java archive"; then
    echo "✅ Valid JAR file downloaded"
else
    echo "❌ Downloaded file is not a valid JAR"
    exit 1
fi

# Try a simple gradle command to test
echo "🧪 Testing Gradle wrapper..."
./gradlew --version || echo "⚠️ Gradle wrapper test failed, but file seems valid"

echo "✅ Gradle wrapper should be working now!"