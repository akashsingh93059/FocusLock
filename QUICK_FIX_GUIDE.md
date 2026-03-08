# Quick Start - Fixing APK Installation Issues

## The Problem
You experienced:
- APK installation stopping at ~50%
- "App not installed" error
- "Update app" showing instead of "Install"
- Message: "Something went wrong"

## The Solution (Applied)

### 🔧 Changes Made

**1. Version Bump** (`app/build.gradle`)
```gradle
versionCode 2        // Was: 1
versionName "1.0.1"  // Was: 1.0
```
- If you had an older version installed, Android needs a higher versionCode to recognize it as an update
- This ensures proper update flow

**2. Debug Signing Configuration** (`app/build.gradle`)
```gradle
signingConfigs {
    debug {
        // Use default debug keystore with existence check for CI/CD compatibility
        def debugKeystorePath = "${System.getProperty('user.home')}/.android/debug.keystore"
        def debugKeystoreFile = file(debugKeystorePath)
        
        if (debugKeystoreFile.exists()) {
            storeFile debugKeystoreFile
            storePassword 'android'
            keyAlias 'androiddebugkey'
            keyPassword 'android'
        }
        // Android will use its default if keystore not configured
    }
}

buildTypes {
    debug {
        debuggable true
        signingConfig signingConfigs.debug
    }
}
```
- Uses Android's default debug keystore
- Ensures consistent signatures across builds
- Prevents "signature mismatch" errors

**3. Packaging Options** (`app/build.gradle`)
```gradle
packagingOptions {
    resources {
        excludes += ['META-INF/DEPENDENCIES', 'META-INF/LICENSE', 'META-INF/LICENSE.txt', 
                     'META-INF/license.txt', 'META-INF/NOTICE', 'META-INF/NOTICE.txt', 
                     'META-INF/notice.txt', 'META-INF/ASL2.0', 'META-INF/*.kotlin_module']
    }
}
```
- Prevents APK corruption from duplicate resources
- Ensures clean APK generation

**4. Gradle Build Fix** (`gradlew`)
```sh
DEFAULT_JVM_OPTS="-Xmx512m -Xms256m"
```
- Fixed broken JVM options syntax
- Increased memory for better build performance

## 📱 How to Fix Your Installation

### Option 1: Complete Uninstall (Recommended)
```bash
# 1. Uninstall existing app completely
Settings > Apps > FocusLock > Uninstall

# OR via ADB:
adb uninstall com.focuslock

# 2. Build fresh APK
./gradlew clean assembleDebug

# 3. Install new APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Direct Update
```bash
# Build with the new versionCode
./gradlew clean assembleDebug

# Install (should work as update now)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## ⚠️ Common Issues & Solutions

### "App not installed - Package conflicts"
**Cause**: Different signing keys  
**Fix**: Completely uninstall the old version:
```bash
adb shell pm uninstall com.focuslock
```

### Still shows "Update" instead of "Install"
**Cause**: Old version still present  
**Fix**: Ensure complete uninstall, clear app data too

### Build fails
**Cause**: Gradle issues or network problems  
**Fix**: 
```bash
./gradlew clean
./gradlew assembleDebug --no-daemon --stacktrace
```

## 📋 What Each Fix Does

| Fix | Why It Matters |
|-----|----------------|
| **versionCode 2** | Android requires increasing version numbers for updates |
| **Debug signing** | Consistent signatures prevent "app not installed" errors |
| **Packaging options** | Removes duplicate files that cause APK corruption |
| **Gradle fixes** | Enables successful APK builds |

## 🚀 For Production Releases

When releasing to users:
1. Create a release keystore:
```bash
keytool -genkey -v -keystore focuslock-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias focuslock
```

2. Add to `app/build.gradle`:
```gradle
signingConfigs {
    release {
        storeFile file("../focuslock-release.jks")
        storePassword "YOUR_PASSWORD"
        keyAlias "focuslock"
        keyPassword "YOUR_KEY_PASSWORD"
    }
}
```

3. **IMPORTANT**: Back up your keystore file! If you lose it, you can't update your app!

## ✅ Verification

After applying these fixes, you should be able to:
- ✅ Build APK without errors
- ✅ Install fresh (after uninstall)
- ✅ Update existing installations (with higher versionCode)
- ✅ No "app not installed" errors
- ✅ No signature conflicts

## 📖 More Details

See `APK_INSTALLATION_FIX.md` for:
- Detailed explanations of each fix
- Advanced troubleshooting
- Production deployment guide
- Best practices for version management

---

**Summary**: The APK installation issues were caused by:
1. Missing version increment for updates
2. Inconsistent/missing signing configuration
3. Broken build script

All issues have been fixed. Just rebuild and reinstall! 🎉
