# Google Play Store Deployment Guide

## Pre-Release Checklist

### 1. App Signing
Before publishing to Play Store, you MUST configure proper release signing:

1. Generate a release keystore:
```bash
keytool -genkey -v -keystore focuslock-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias focuslock
```

2. Update `app/build.gradle` release signing config with your keystore details:
```groovy
release {
    storeFile file("path/to/focuslock-release.jks")
    storePassword "YOUR_STORE_PASSWORD"
    keyAlias "focuslock"
    keyPassword "YOUR_KEY_PASSWORD"
}
```

OR use environment variables (recommended for CI/CD):
```groovy
release {
    storeFile file(System.getenv("RELEASE_KEYSTORE_PATH") ?: "focuslock-release.jks")
    storePassword System.getenv("RELEASE_KEYSTORE_PASSWORD")
    keyAlias System.getenv("RELEASE_KEY_ALIAS")
    keyPassword System.getenv("RELEASE_KEY_PASSWORD")
}
```

**IMPORTANT**: Keep your keystore file safe and NEVER commit it to version control!

### 2. Build Release Bundle
```bash
./gradlew clean bundleRelease --no-daemon --stacktrace
```

The release bundle will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

### 3. Build Release APK (for testing)
```bash
./gradlew clean assembleRelease --no-daemon --stacktrace
```

The release APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

## Play Store Requirements Compliance

### ✅ Completed Updates

1. **Version Management**
   - Updated versionCode to 3
   - Updated versionName to 1.0.2

2. **Build Configuration**
   - Updated Android Gradle Plugin to 8.7.3
   - Updated Gradle wrapper to 8.11.1
   - Enabled ProGuard with minifyEnabled and shrinkResources
   - Added comprehensive ProGuard rules

3. **App Bundle Configuration**
   - Configured density and ABI splits
   - Language split disabled for base APK inclusion

4. **Security & Privacy**
   - Added backup rules (backup_rules.xml)
   - Added data extraction rules (data_extraction_rules.xml)
   - Configured allowBackup with proper exclusions

5. **Dependencies**
   - Updated to latest stable versions:
     - androidx.constraintlayout: 2.2.0
     - com.google.code.gson: 2.11.0

6. **Target API**
   - Currently targeting API 34 (Android 14)
   - Minimum SDK: API 26 (Android 8.0)

### Required Actions Before Publishing

1. **Create Release Keystore** (see App Signing section above)

2. **Update Signing Configuration** in `app/build.gradle`:
   - Replace debug keystore with your production keystore
   - Use environment variables or secure credential storage

3. **Create Play Store Listing**:
   - App title and short description
   - Full description
   - Screenshots (at least 2)
   - Feature graphic (1024x500)
   - App icon (already configured)
   - Privacy policy URL (if collecting user data)

4. **Privacy & Security**:
   - Complete Data Safety section in Play Console
   - Declare all permissions and data usage
   - Provide privacy policy if needed

5. **Content Rating**:
   - Complete content rating questionnaire
   - Get IARC rating

6. **Testing**:
   - Test release build thoroughly
   - Use Internal Testing track first
   - Conduct closed/open beta testing

7. **App Bundle**:
   - Upload the AAB file (not APK) to Play Console
   - Play Console will generate optimized APKs

## Common Issues & Solutions

### Issue: "Upload failed: You need to use a different version code"
**Solution**: Increment versionCode in app/build.gradle

### Issue: "Your app is not compliant with Google Play Policies"
**Solution**: Review and fix any policy violations in Play Console

### Issue: "Keystore was tampered with, or password was incorrect"
**Solution**: Verify keystore password and alias are correct

### Issue: "App Bundle must be signed"
**Solution**: Ensure release signing config is properly configured

## Build Commands Reference

### Clean Build
```bash
./gradlew clean --no-daemon
```

### Debug Build
```bash
./gradlew assembleDebug --no-daemon --stacktrace
```

### Release Bundle (for Play Store)
```bash
./gradlew bundleRelease --no-daemon --stacktrace
```

### Release APK (for testing/direct distribution)
```bash
./gradlew assembleRelease --no-daemon --stacktrace
```

### Check Dependencies
```bash
./gradlew dependencies --configuration releaseRuntimeClasspath
```

## Version History

- **1.0.2 (versionCode 3)** - Play Store compliance updates
- **1.0.1 (versionCode 2)** - Previous version
- **1.0.0 (versionCode 1)** - Initial release

## Support & Resources

- [Google Play Console](https://play.google.com/console)
- [Android App Bundle Documentation](https://developer.android.com/guide/app-bundle)
- [Play Store Publishing Guide](https://developer.android.com/distribute/best-practices/launch)
- [App Signing Best Practices](https://developer.android.com/studio/publish/app-signing)
