# FocusLock — Beginner Setup Guide
## Get your APK in 10 minutes, FREE, no Android Studio needed

---

## What this app does (tailored to your needs)

✅ **Block specific apps** — YouTube, Instagram, TikTok, etc.
✅ **Schedule blocking** — automatically blocks apps from 9AM-5PM (or any time you set)
✅ **Study timer** — Pomodoro countdown with live notification
✅ **Uninstall protection** — can't delete the app during focus
✅ **PIN lock** — need PIN to stop a session early
✅ **Stats & charts** — sessions, total hours, streaks, week bar chart

---

## STEP 1 — Upload to GitHub (free, takes 5 min)

1. Go to **https://github.com** and create a free account
2. Click the **+** button → **New repository**
3. Name it: `FocusLock`
4. Set to **Private** (important for your personal app)
5. Click **Create repository**
6. On the next page, click **"uploading an existing file"**
7. Drag and drop the entire **FocusLock** folder contents
   (all the files inside it — build.gradle, app/, .github/, etc.)
8. Click **Commit changes**

---

## STEP 2 — GitHub Builds the APK Automatically

1. In your repository, click the **"Actions"** tab at the top
2. You'll see a workflow called **"Build FocusLock APK"** running
3. Wait 3-5 minutes for it to complete (green checkmark ✅)
4. Click on the completed run
5. Scroll down to **"Artifacts"** section
6. Click **"FocusLock-APK"** to download a zip file
7. Extract the zip — inside is **app-debug.apk**

That's your app file! 🎉

---

## STEP 3 — Install on Your Android Phone

### Allow installation from unknown sources:
1. Send the APK to yourself (email, Google Drive, WhatsApp to yourself)
2. Open the file on your phone
3. Android will say "blocked by Play Protect" or "unknown source"
4. Tap **"Install anyway"** or go to Settings to allow it
5. On most phones: Settings → Apps → Special app access → Install unknown apps
   → enable it for your file manager or browser

### Install:
- Tap the APK file → Install → Done

---

## STEP 4 — First Launch Setup (do this once)

When you open FocusLock for the first time:

### Permission 1: Usage Access (REQUIRED — app can't work without this)
- App will show a dialog → tap "OK, let's do it"
- Android opens Settings → find **FocusLock** → tap it → toggle ON
- Go back to the app

### Permission 2: Display Over Other Apps (REQUIRED for block screen)
- Go to Settings → Apps → FocusLock → Display over other apps → Enable
- OR the app will ask when you start your first session

### Permission 3: Battery Optimization (IMPORTANT for reliability)
Different phones have different names:

| Phone Brand | Where to find it |
|------------|-----------------|
| Samsung | Settings → Battery → Background usage limits → Never sleeping apps → Add FocusLock |
| Xiaomi/MIUI | Settings → Apps → FocusLock → Battery saver → No restrictions |
| OnePlus | Settings → Battery → Battery optimization → All apps → FocusLock → Don't optimize |
| Stock Android | Settings → Apps → FocusLock → Battery → Unrestricted |

**This step is essential!** Without it, some phones kill the background service.

---

## STEP 5 — Using the App

### Tab 1: ⏱ Timer
- Drag slider to set study duration (5–120 min)
- Toggle **PIN Lock** to require PIN before stopping
- Toggle **Uninstall Protection** (Device Admin) → tap "Activate" when Android asks
- Tap **▶ START FOCUS**

### Tab 2: 📅 Schedule
- Toggle **Schedule Blocking** ON
- Set start time (e.g. 9:00 AM) and end time (e.g. 5:00 PM)
- Check which days to block (Mon-Fri by default)
- The status shows whether blocking is currently active
- **Schedule works even without manually starting a session!**
  Just enable schedule mode + select apps → it auto-blocks during set hours

### Tab 3: 📵 Apps
- Tap **"Select apps to block"**
- Known distractions (YouTube, Instagram, TikTok etc.) appear at the top with ⚠️ badge
- Tap any app to select/deselect
- Tap "Done"

### Tab 4: 📊 Stats
- Shows your sessions, total focus time, streaks
- Tap "View Full Stats" for week chart + session history

---

## What happens when you open a blocked app

- FocusLock instantly shows a full-screen block screen
- It shows the app name, how much time is left, and a motivational quote
- You can tap "← Go to Home Screen"
- "End Session" button requires your PIN if PIN lock is on
- The back button takes you home — you cannot bypass the screen

---

## Troubleshooting

**Block screen doesn't appear:**
→ Make sure "Display over other apps" is ON for FocusLock

**App doesn't detect blocked apps:**
→ Usage Access permission must be ON
→ Check battery optimization (see Step 4)

**Service stops when phone sleeps:**
→ Battery optimization is the issue — follow Step 4 for your phone brand
→ Also try: Settings → Developer options → Don't keep activities → OFF

**"Can't install, unsafe app" warning:**
→ This is because it's not from the Play Store — it's safe, it's your own app
→ Tap "Install anyway" or go to security settings

**Schedule not blocking:**
→ Make sure you also selected apps in the Apps tab
→ Schedule mode needs apps selected to know what to block

---

## How to Update the App

1. Make changes to the code files
2. Upload/push to GitHub
3. GitHub Actions builds automatically
4. Download new APK → install it (it'll update over the old one)

---

## Files in this project

```
FocusLock/
├── .github/workflows/build.yml     ← Cloud build automation (GitHub Actions)
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/focuslock/
│   │   ├── model/FocusSession.java  ← schedule logic lives here
│   │   ├── model/AppInfo.java       ← add more apps to block list here
│   │   ├── service/AppMonitorService.java  ← checks foreground app every 500ms
│   │   ├── receiver/BootReceiver.java      ← restart after reboot
│   │   ├── receiver/FocusDeviceAdminReceiver.java  ← uninstall protection
│   │   └── ui/
│   │       ├── MainActivity.java    ← 4-tab main screen
│   │       ├── BlockedActivity.java ← the block screen overlay
│   │       ├── AppPickerActivity.java ← choose apps to block
│   │       ├── PinActivity.java     ← PIN setup & verify
│   │       ├── StatsActivity.java   ← full stats + charts
│   │       └── WeekChartView.java   ← custom bar chart
│   └── res/layout/                  ← all screen designs
└── app/build.gradle                 ← dependencies
```

---

**Built for you — free forever. No subscriptions, no ads, no limits.**
