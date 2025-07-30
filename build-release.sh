#!/bin/bash

# Music Player - Release Build Script
# This script builds a release APK ready for distribution

echo "🎵 Building Music Player Release APK..."
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo -e "${RED}❌ Error: build.gradle not found. Please run this script from the project root directory.${NC}"
    exit 1
fi

# Check if gradlew exists
if [ ! -f "gradlew" ]; then
    echo -e "${RED}❌ Error: gradlew not found. Please ensure you have a complete Android project.${NC}"
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

echo -e "${BLUE}📋 Build Information:${NC}"
echo "   App Name: Music Player"
echo "   Version: 1.0.0"
echo "   Package: com.musicplayer.app"
echo "   Build Type: Release"
echo ""

echo -e "${YELLOW}🧹 Cleaning project...${NC}"
./gradlew clean

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Clean failed. Please check your setup.${NC}"
    exit 1
fi

echo -e "${YELLOW}🔨 Building release APK...${NC}"
./gradlew assembleRelease

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed. Please check the errors above.${NC}"
    exit 1
fi

# Create release directory if it doesn't exist
mkdir -p app/release

# Find the generated APK
APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ -f "$APK_PATH" ]; then
    # Copy APK to release directory with versioned name
    RELEASE_APK="app/release/MusicPlayer-v1.0.0-release.apk"
    cp "$APK_PATH" "$RELEASE_APK"
    
    echo -e "${GREEN}✅ Build successful!${NC}"
    echo ""
    echo -e "${BLUE}📱 APK Details:${NC}"
    echo "   Location: $RELEASE_APK"
    
    # Get APK size
    APK_SIZE=$(du -h "$RELEASE_APK" | cut -f1)
    echo "   Size: $APK_SIZE"
    
    # Get APK info using aapt if available
    if command -v aapt &> /dev/null; then
        echo "   Package: $(aapt dump badging "$RELEASE_APK" | grep package | awk -F"'" '{print $2}')"
        echo "   Version: $(aapt dump badging "$RELEASE_APK" | grep versionName | awk -F"'" '{print $6}')"
    fi
    
    # Generate SHA256 hash
    if command -v sha256sum &> /dev/null; then
        SHA256=$(sha256sum "$RELEASE_APK" | cut -d' ' -f1)
        echo "   SHA256: $SHA256"
        echo "$SHA256  $(basename "$RELEASE_APK")" > app/release/SHA256SUMS
    elif command -v shasum &> /dev/null; then
        SHA256=$(shasum -a 256 "$RELEASE_APK" | cut -d' ' -f1)
        echo "   SHA256: $SHA256"
        echo "$SHA256  $(basename "$RELEASE_APK")" > app/release/SHA256SUMS
    fi
    
    echo ""
    echo -e "${GREEN}🎉 Release APK ready for distribution!${NC}"
    echo ""
    echo -e "${BLUE}📦 Installation Instructions:${NC}"
    echo "1. Enable 'Install from Unknown Sources' in Android Settings"
    echo "2. Transfer the APK file to your Android device"
    echo "3. Tap the APK file to install"
    echo "4. Grant storage permissions when prompted"
    echo "5. Enjoy your new music player!"
    echo ""
    echo -e "${YELLOW}⚠️  Note: This APK is signed with a debug key for demo purposes.${NC}"
    echo -e "${YELLOW}   For production release, use a proper release keystore.${NC}"
    
else
    echo -e "${RED}❌ APK not found at expected location: $APK_PATH${NC}"
    echo "   Please check the build output for errors."
    exit 1
fi