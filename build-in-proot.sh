#!/data/data/com.termux/files/usr/bin/bash

# Build Android app using proot-distro Ubuntu environment
# This provides better compatibility with Android build tools

echo "Setting up proot-distro Ubuntu environment for Android building..."

# Install proot-distro if not installed
if ! command -v proot-distro &> /dev/null; then
    echo "Installing proot-distro..."
    pkg update && pkg install proot-distro -y
fi

# Install Ubuntu if not installed
if ! proot-distro list --installed | grep -q ubuntu; then
    echo "Installing Ubuntu distribution..."
    proot-distro install ubuntu
fi

# Create build script for Ubuntu environment
cat > /tmp/android-build.sh << 'EOF'
#!/bin/bash
set -e

# Update system
apt update
apt install -y wget unzip openjdk-17-jdk

# Set up Android SDK
cd /home
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
mkdir -p android-sdk/cmdline-tools
unzip -q commandlinetools-linux-11076708_latest.zip -d android-sdk/cmdline-tools
mv android-sdk/cmdline-tools/cmdline-tools android-sdk/cmdline-tools/latest

# Set environment
export ANDROID_HOME=/home/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Accept licenses
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses || true
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"

# Build project
cd /project
echo "sdk.dir=/home/android-sdk" > local.properties
chmod +x gradlew
./gradlew assembleDebug --no-daemon --stacktrace

echo "Build completed! APK should be in app/build/outputs/apk/debug/"
EOF

chmod +x /tmp/android-build.sh

echo "Starting build in Ubuntu environment..."
proot-distro login ubuntu --bind /data/data/com.termux/files/home/termux-ai-app:/project -- /tmp/android-build.sh

echo "Build completed! Check app/build/outputs/apk/debug/ for your APK"