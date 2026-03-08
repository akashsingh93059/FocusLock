package com.focuslock.model;

public class AppInfo {
    private String appName;
    private String packageName;
    private boolean selected;

    // Pre-flagged distraction apps shown at the top of the picker
    public static final String[] DISTRACTION_PACKAGES = {
        "com.google.android.youtube",
        "com.instagram.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.snapchat.android",
        "com.zhiliaoapp.musically",   // TikTok
        "com.reddit.frontpage",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient",
        "com.spotify.music",
        "com.whatsapp",
        "com.facebook.orca",          // Messenger
        "org.telegram.messenger",
        "com.discord",
        "com.mojang.minecraftpe",
        "com.pubg.imobile",
        "com.activision.callofduty.shooter",
        "com.pinterest",
        "com.linkedin.android",
        "com.tumblr",
        "com.twitch.android.app",
    };

    public AppInfo(String appName, String packageName) {
        this.appName = appName;
        this.packageName = packageName;
    }

    public String getAppName() { return appName; }
    public String getPackageName() { return packageName; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean isKnownDistraction() {
        for (String d : DISTRACTION_PACKAGES) {
            if (d.equals(packageName)) return true;
        }
        return false;
    }
}
