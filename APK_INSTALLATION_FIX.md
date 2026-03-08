# APK Installation Issue - Resolution Guide

## Problem Description
When installing the FocusLock APK, installation stops halfway with error:
- "App not installed"
- Shows "Update app" instead of "Install"
- Error message: "Something went wrong"

## Root Causes & Fixes Applied

### 1. Version Code Issue ✅ FIXED
**Problem**: If an older version of the app is already installed, the new APK must have a higher `versionCode` to be recognized as an update.

**Solution**: 
- Incremented `versionCode` from 1 to 2
- Updated `versionName` from "1.0" to "1.0.1"

**Location**: `app/build.gradle`
```gradle
versionCode 2
versionName "1.0.1"
```

### 2. Signing Configuration Issue ✅ FIXED
**Problem**: Missing or inconsistent signing configuration can cause installation failures, especially when trying to update an existing installation.

**Solution**: Added explicit debug signing configuration with CI/CD compatibility:
```gradle
signingConfigs {
    debug {
        // Use default debug keystore
        // First try the default location, which works in most development environments
        def debugKeystorePath = "${System.getProperty('user.home')}/.android/debug.keystore"
        def debugKeystoreFile = file(debugKeystorePath)
        
        // Only configure if the keystore exists, otherwise use Android's default
        if (debugKeystoreFile.exists()) {
            storeFile debugKeystoreFile
            storePassword 'android'
            keyAlias 'androiddebugkey'
            keyPassword 'android'
        }
    }
}

buildTypes {
    debug {
        debuggable true
        // Only apply custom signing config if keystore exists
        def debugKeystorePath = "${System.getProperty('user.home')}/.android/debug.keystore"
        if (file(debugKeystorePath).exists()) {
            signingConfig signingConfigs.debug
        }
    }
}
```

**Benefits**:
- Ensures consistent APK signatures across builds
- Falls back gracefully if keystore doesn't exist (CI/CD friendly)
- Prevents "signature verification failed" errors

### 3. Packaging Options ✅ FIXED
**Problem**: Duplicate resource files can cause APK build/installation failures.

**Solution**: Added packaging options to exclude common duplicate META-INF files:
```gradle
packagingOptions {
    resources {
        excludes += ['META-INF/DEPENDENCIES', 'META-INF/LICENSE', 'META-INF/LICENSE.txt', 
                     'META-INF/license.txt', 'META-INF/NOTICE', 'META-INF/NOTICE.txt', 
                     'META-INF/notice.txt', 'META-INF/ASL2.0', 'META-INF/*.kotlin_module']
    }
}
```

### 4. Gradle Wrapper Script Issue ✅ FIXED
**Problem**: The `gradlew` script had incorrect JVM options quoting causing build failures.

**Solution**: Fixed JVM options from nested quotes to single quotes:
```sh
# Before
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# After
DEFAULT_JVM_OPTS="-Xmx512m -Xms256m"
```

Also increased memory allocation for better build performance.

## How to Build and Install

### Step 1: Clean Previous Installations
First, completely uninstall any existing version of FocusLock from your device:
```bash
# On device: Settings > Apps > FocusLock > Uninstall
# Or via ADB:
adb uninstall com.focuslock
```

### Step 2: Build Fresh APK
```bash
cd /path/to/FocusLock
./gradlew clean assembleDebug --no-daemon --stacktrace
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Install APK
```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or transfer APK to device and install manually
```

## Troubleshooting

### Issue: Still shows "Update" instead of "Install"
**Solution**: An older version is still installed. Completely uninstall it first:
```bash
adb uninstall com.focuslock
```

### Issue: "App not installed - Package conflicts with existing package"
**Solution**: Different signing keys were used. Uninstall the old version completely:
```bash
adb shell pm uninstall com.focuslock
# Force uninstall if needed
adb shell pm uninstall -k com.focuslock
```

### Issue: Build fails with memory error
**Solution**: The gradlew script now allocates more memory (512MB), but if you still have issues:
```bash
export GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"
./gradlew clean assembleDebug
```

### Issue: "Signature verification failed"
**Solution**: For debug builds, ensure you're using the default debug keystore. For release builds, configure proper signing:
```gradle
signingConfigs {
    release {
        storeFile file("path/to/your/keystore.jks")
        storePassword "your-store-password"
        keyAlias "your-key-alias"
        keyPassword "your-key-password"
    }
}
```

## For Production/Release Builds

To create a properly signed release APK:

1. Generate or use existing keystore:
```bash
keytool -genkey -v -keystore focuslock-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias focuslock
```

2. Add signing config to `app/build.gradle`:
```gradle
signingConfigs {
    release {
        storeFile file("../focuslock-release.jks")
        storePassword "YOUR_STORE_PASSWORD"
        keyAlias "focuslock"
        keyPassword "YOUR_KEY_PASSWORD"
    }
}

buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}
```

3. Build release APK:
```bash
./gradlew assembleRelease
```

## Important Notes

1. **Version Management**: Always increment `versionCode` when releasing updates
2. **Signing Keys**: Keep your release keystore safe and backed up - losing it means you can't update your app
3. **Debug vs Release**: Debug and release builds use different signing keys by default
4. **Uninstall Before Testing**: When testing different builds, uninstall the previous version to avoid conflicts

## Summary of Changes

Files modified:
- ✅ `app/build.gradle` - Added signing config, incremented version, added packaging options
- ✅ `gradlew` - Fixed JVM options and increased memory allocation

These changes ensure:
- ✅ Consistent APK signing across builds
- ✅ Proper version management for updates
- ✅ No resource conflicts during build/install
- ✅ Smooth build process
- ✅ Successful installation on devices
