#!/bin/bash

# Termux AI Build Script
# Compiles the enhanced Termux app with Claude Code integration

set -e

echo "🚀 Building Termux AI v2.0.0"
echo "==============================="

# Check if we're in Termux
if [ ! -d "/data/data/com.termux" ]; then
    echo "❌ This script must be run in Termux environment"
    exit 1
fi

# Install required packages for Android development in Termux
echo "📦 Installing build dependencies..."
pkg update -y
pkg install -y openjdk-17 gradle android-tools

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME=$PREFIX/opt/openjdk
    echo "☕ Set JAVA_HOME to $JAVA_HOME"
fi

# Set Android SDK path (if available)
if [ -d "$HOME/android-sdk" ]; then
    export ANDROID_SDK_ROOT=$HOME/android-sdk
    export ANDROID_HOME=$ANDROID_SDK_ROOT
    echo "📱 Using Android SDK at $ANDROID_SDK_ROOT"
else
    echo "⚠️  Android SDK not found. You may need to install it manually."
    echo "   Download from: https://developer.android.com/studio#command-tools"
fi

# Navigate to project directory
cd "$(dirname "$0")"
PROJECT_DIR=$(pwd)
echo "📁 Project directory: $PROJECT_DIR"

# Make gradlew executable
chmod +x gradlew 2>/dev/null || echo "ℹ️  gradlew not found, using system gradle"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
if [ -f "gradlew" ]; then
    ./gradlew clean
else
    gradle clean
fi

# Check for required resources
echo "🔍 Checking project structure..."
if [ ! -f "app/src/main/AndroidManifest.xml" ]; then
    echo "❌ AndroidManifest.xml not found"
    exit 1
fi

if [ ! -f "app/build.gradle" ]; then
    echo "❌ app/build.gradle not found"
    exit 1
fi

echo "✅ Project structure validated"

# Build debug APK
echo "🔨 Building debug APK..."
BUILD_CMD="assembleDebug"

# Use gradlew if available, otherwise fallback to system gradle
if [ -f "gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Using project Gradle wrapper..."
    ./gradlew $BUILD_CMD
elif command -v gradle >/dev/null 2>&1; then
    echo "Using system Gradle..."
    gradle $BUILD_CMD
else
    echo "❌ No Gradle installation found"
    echo "Please install gradle or ensure wrapper is properly configured"
    exit 1
fi

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Build completed successfully!"
    echo "==============================="
    
    # Find the generated APK
    APK_PATH=$(find app/build/outputs/apk -name "*.apk" | head -1)
    
    if [ -n "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📱 APK generated: $APK_PATH"
        echo "📏 APK size: $APK_SIZE"
        
        # Copy APK to easily accessible location
        cp "$APK_PATH" "$HOME/storage/downloads/termux-ai-debug.apk" 2>/dev/null || \
        cp "$APK_PATH" "$HOME/termux-ai-debug.apk"
        
        echo "📁 APK copied to: $HOME/termux-ai-debug.apk"
        echo ""
        echo "📋 Installation instructions:"
        echo "1. Enable 'Unknown Sources' in Android Settings"
        echo "2. Install the APK: $HOME/termux-ai-debug.apk"
        echo "3. Grant necessary permissions when prompted"
        echo "4. Launch Termux AI and enjoy enhanced terminal experience!"
        
    else
        echo "❌ APK not found in build outputs"
        exit 1
    fi
    
else
    echo "❌ Build failed!"
    echo ""
    echo "🔧 Troubleshooting tips:"
    echo "1. Ensure Android SDK is properly installed"
    echo "2. Check that JAVA_HOME is set correctly"
    echo "3. Verify all dependencies are installed"
    echo "4. Check build logs above for specific errors"
    exit 1
fi

echo ""
echo "✨ Termux AI build process completed!"