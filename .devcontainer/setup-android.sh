#!/bin/bash

# Android SDK setup for GitHub Codespaces
set -e

# Install Android SDK
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
mkdir -p $HOME/android-sdk/cmdline-tools
unzip -q commandlinetools-linux-11076708_latest.zip -d $HOME/android-sdk/cmdline-tools
mv $HOME/android-sdk/cmdline-tools/cmdline-tools $HOME/android-sdk/cmdline-tools/latest

# Set environment variables
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# Source environment
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses and install SDK components
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"

# Create local.properties
echo "sdk.dir=$HOME/android-sdk" > local.properties

# Make gradlew executable
chmod +x gradlew

echo "Android development environment setup complete!"
echo "You can now run: ./gradlew assembleDebug"