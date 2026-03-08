# Play Store Publishing - Quick Start Guide

## ✅ What's Been Done

Your FocusLock app has been fully updated for Google Play Store compliance. All configuration changes are complete and ready for production deployment.

### Version Update
- **Current Version**: 1.0.2 (versionCode 3)
- **Previous Version**: 1.0.1 (versionCode 2)

### Build Tools Updated
- Android Gradle Plugin: 8.7.3 (latest stable)
- Gradle: 8.11.1 (latest stable)
- All dependencies updated to latest stable versions

### Release Configuration
✅ ProGuard enabled (code optimization and obfuscation)
✅ Resource shrinking enabled (smaller APK size)
✅ App bundle splits configured (density and ABI)
✅ Backup and privacy rules added
✅ Release signing configured with environment variables

## 🚀 Steps to Publish to Play Store

### Step 1: Generate Production Keystore (ONE TIME ONLY)

```bash
keytool -genkey -v -keystore focuslock-release.jks \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -alias focuslock
```

**IMPORTANT**: 
- Store this keystore file securely (you'll need it for ALL future updates)
- If you lose this keystore, you CANNOT update your app on Play Store
- Back it up in multiple secure locations
- NEVER commit it to git

### Step 2: Set Environment Variables

Before building, set these environment variables:

**Linux/Mac:**
```bash
export RELEASE_KEYSTORE_PATH="/path/to/focuslock-release.jks"
export RELEASE_KEYSTORE_PASSWORD="your_keystore_password"
export RELEASE_KEY_ALIAS="focuslock"
export RELEASE_KEY_PASSWORD="your_key_password"
```

**Windows (PowerShell):**
```powershell
$env:RELEASE_KEYSTORE_PATH="C:\path\to\focuslock-release.jks"
$env:RELEASE_KEYSTORE_PASSWORD="your_keystore_password"
$env:RELEASE_KEY_ALIAS="focuslock"
$env:RELEASE_KEY_PASSWORD="your_key_password"
```

**Windows (CMD):**
```cmd
set RELEASE_KEYSTORE_PATH=C:\path\to\focuslock-release.jks
set RELEASE_KEYSTORE_PASSWORD=your_keystore_password
set RELEASE_KEY_ALIAS=focuslock
set RELEASE_KEY_PASSWORD=your_key_password
```

### Step 3: Build Release Bundle

```bash
./gradlew clean bundleRelease --no-daemon --stacktrace
```

The output will be at:
```
app/build/outputs/bundle/release/app-release.aab
```

**Note**: If environment variables are not set, the build will use debug keystore and print a warning. This is OK for testing but NOT for Play Store submission.

### Step 4: Test Release Build (Recommended)

Build and test the release APK locally before uploading:

```bash
./gradlew assembleRelease --no-daemon --stacktrace
```

Install on a test device:
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

Test all features thoroughly!

### Step 5: Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app (or create new app)
3. Navigate to **Release** → **Production** (or Testing)
4. Click **Create new release**
5. Upload `app-release.aab` (NOT the APK)
6. Fill in release notes
7. Review and roll out

### Step 6: Complete Play Console Requirements

Before you can publish, complete these in Play Console:

#### App Content
- [ ] Privacy Policy (if collecting data)
- [ ] Data Safety form (declare permissions and data usage)
- [ ] App category
- [ ] Target audience
- [ ] News apps declaration (if applicable)

#### Store Listing
- [ ] App title
- [ ] Short description (80 characters max)
- [ ] Full description (4000 characters max)
- [ ] App icon (512x512 PNG - already in app)
- [ ] Feature graphic (1024x500)
- [ ] Screenshots (minimum 2, recommend 4-8)
- [ ] Optional: Video

#### Content Rating
- [ ] Complete questionnaire
- [ ] Get IARC rating

#### Pricing & Distribution
- [ ] Free or Paid
- [ ] Countries/regions
- [ ] Consent for ads (if applicable)

## 📋 Pre-Launch Checklist

Before submitting to Play Store, verify:

- [ ] Production keystore generated and secured
- [ ] Environment variables set correctly
- [ ] Release bundle built successfully with production keystore
- [ ] Release build tested on physical device
- [ ] All app features work in release mode
- [ ] ProGuard hasn't broken any functionality
- [ ] Privacy policy uploaded (if needed)
- [ ] All Play Console sections completed
- [ ] Screenshots and graphics prepared
- [ ] Release notes written
- [ ] Target audience and content rating selected

## 🔍 Verification Commands

### Check Current Version
```bash
grep -E "versionCode|versionName" app/build.gradle
```

### Verify ProGuard is Enabled
```bash
grep -A 5 "buildTypes" app/build.gradle | grep minifyEnabled
```

### Check Bundle Size
```bash
ls -lh app/build/outputs/bundle/release/app-release.aab
```

### List APK Contents
```bash
unzip -l app/build/outputs/apk/release/app-release.apk | less
```

## ⚠️ Important Notes

### Keystore Security
- Your keystore is the ONLY way to update your app on Play Store
- If lost, you MUST create a new app listing (losing all reviews, ratings, downloads)
- Store multiple encrypted backups in different locations
- Never commit to version control
- Use strong passwords (12+ characters)

### ProGuard
- Enabled to reduce APK size by ~30-40%
- Obfuscates code for security
- May require testing to ensure no runtime issues
- If app crashes in release but not debug, check ProGuard rules

### Testing
- ALWAYS test release builds before publishing
- ProGuard can break reflection-based code
- Test on different Android versions (API 26+)
- Test on different screen sizes

### Version Codes
- Must increment for every release
- Current: versionCode 3
- Next release: versionCode 4, then 5, etc.
- Can NEVER reuse a version code

## 📚 Additional Documentation

For detailed information, see:
- **PLAY_STORE_DEPLOYMENT.md** - Comprehensive deployment guide
- **PLAY_STORE_COMPLIANCE_SUMMARY.md** - Detailed changes summary

## 🆘 Troubleshooting

### "You need to use a different version code"
- Increment versionCode in `app/build.gradle`

### "App bundle must be signed"
- Verify environment variables are set correctly
- Check keystore file path is correct

### "Upload failed" or "Invalid signature"
- Ensure using the same keystore as previous versions
- Check keystore password and alias are correct

### Build fails with ProGuard errors
- Check `app/build/outputs/mapping/release/` for details
- May need to add specific `-keep` rules

### App crashes in release but not debug
- ProGuard issue - check crash logs
- Add `-keep` rules for affected classes
- Test with `./gradlew assembleRelease`

## 🎉 Success!

Once uploaded and approved:
1. Your app will appear on Google Play Store
2. Users can download and install
3. You'll get analytics in Play Console
4. Future updates: increment versionCode and repeat steps 2-5

---

**Need Help?**
- Play Console Help: https://support.google.com/googleplay/android-developer
- Android Developers: https://developer.android.com/distribute
- Stack Overflow: Tag [google-play-console]
