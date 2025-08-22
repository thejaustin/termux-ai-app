#!/bin/bash
set -e

echo "🚀 Starting Android build in Ubuntu environment..."

# Update system
echo "📦 Updating Ubuntu packages..."
apt update
apt install -y wget unzip openjdk-17-jdk curl

# Set up Android SDK
echo "⬇️ Downloading Android SDK..."
cd /home
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
mkdir -p android-sdk/cmdline-tools
unzip -q commandlinetools-linux-11076708_latest.zip -d android-sdk/cmdline-tools
mv android-sdk/cmdline-tools/cmdline-tools android-sdk/cmdline-tools/latest

# Set environment
export ANDROID_HOME=/home/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

echo "📱 Installing Android SDK components..."
# Accept licenses
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