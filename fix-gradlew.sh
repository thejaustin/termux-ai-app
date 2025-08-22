#!/bin/bash
set -e

echo "🔧 Fixing Gradle wrapper..."

# Set environment
export ANDROID_HOME=/home/android-sdk  
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

cd /data/data/com.termux/files/home/termux-ai-app

# Remove broken wrapper jar
rm -f gradle/wrapper/gradle-wrapper.jar

# Download the correct wrapper jar for the version specified in properties
echo "📦 Downloading Gradle 8.10.2 wrapper jar..."
curl -L -o gradle/wrapper/gradle-wrapper.jar "https://services.gradle.org/distributions/gradle-8.10.2-wrapper.jar"

# Make gradlew executable
chmod +x gradlew

# Verify the jar is valid
echo "🔍 Verifying wrapper jar..."
file gradle/wrapper/gradle-wrapper.jar

# Try a simple gradle command to test
echo "🧪 Testing Gradle wrapper..."
./gradlew --version

echo "✅ Gradle wrapper fixed!"