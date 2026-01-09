#!/bin/bash

echo "========================================"
echo "🔨 Building Android App with FCM Token Fix"
echo "========================================"
echo ""

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "📊 Current Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" -gt "17" ]; then
    echo "⚠️  WARNING: Java version is too new ($JAVA_VERSION)"
    echo "   Android requires Java 17 or lower"
    echo ""
    echo "Options:"
    echo "1. Install JDK 17: brew install openjdk@17"
    echo "2. Set JAVA_HOME: export JAVA_HOME=\$(/usr/libexec/java_home -v 17)"
    echo "3. Or build via Android Studio (recommended)"
    echo ""
    read -p "Do you want to continue anyway? (y/N) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Build cancelled"
        exit 1
    fi
fi

echo ""
echo "🧹 Cleaning previous build..."
./gradlew clean --no-daemon

echo ""
echo "🔨 Building debug APK..."
./gradlew assembleDebug --no-daemon

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "✅ Build successful!"
    echo "========================================"
    echo ""
    echo "📦 APK location:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📱 To install on device:"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "🧪 Testing steps:"
    echo "   1. Install APK on male user's device (6203224780)"
    echo "   2. Male user opens the app"
    echo "   3. Check logs for: '📧 Sending FCM token to backend on app start'"
    echo "   4. Verify FCM token in database"
    echo "   5. Female user calls male user"
    echo "   6. Male should receive incoming call notification"
    echo ""
else
    echo ""
    echo "========================================"
    echo "❌ Build failed!"
    echo "========================================"
    echo ""
    echo "💡 Recommended: Build via Android Studio instead"
    echo "   1. Open android_app folder in Android Studio"
    echo "   2. Wait for Gradle sync"
    echo "   3. Build > Build Bundle(s) / APK(s) > Build APK(s)"
    echo ""
    exit 1
fi
