package com.focuslock.model;

import java.util.ArrayList;
import java.util.List;

public class FocusSession {
    // Modes
    public static final String MODE_BLOCK = "block";
    public static final String MODE_SCHEDULE = "schedule";

    private String mode = MODE_BLOCK;
    private int durationMinutes = 25;
    private long startTime = 0;
    private boolean active = false;

    // Apps to block (package names)
    private List<String> blockedApps = new ArrayList<>();

    // Schedule settings
    private boolean scheduleEnabled = false;
    private int scheduleStartHour = 9;   // 9 AM
    private int scheduleStartMin = 0;
    private int scheduleEndHour = 17;    // 5 PM
    private int scheduleEndMin = 0;
    private boolean[] scheduleDays = {false, true, true, true, true, true, false}; // Mon-Fri default

    // Security
    private boolean pinLocked = false;

    // Break settings
    private boolean autoBreak = true;
    private int breakMinutes = 5;

    // ── Logic ─────────────────────────────────────────────

    public long getRemainingMs() {
        if (!active || startTime == 0) return (long) durationMinutes * 60_000;
        long elapsed = System.currentTimeMillis() - startTime;
        long total = (long) durationMinutes * 60_000;
        return Math.max(0, total - elapsed);
    }

    public boolean isExpired() {
        return active && getRemainingMs() == 0;
    }

    public boolean shouldBlockApp(String packageName) {
        if (!active) return false;
        if ("com.focuslock".equals(packageName)) return false;
        // System packages never block
        if (packageName.startsWith("com.android.") || packageName.startsWith("android")) return false;

        if (MODE_SCHEDULE.equals(mode)) {
            // In schedule mode, only block if we're inside the scheduled window
            if (!isWithinSchedule()) return false;
            return blockedApps.contains(packageName);
        } else {
            return blockedApps.contains(packageName);
        }
    }

    public boolean isWithinSchedule() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK); // 1=Sun, 2=Mon...7=Sat
        int arrayIndex = (dayOfWeek + 5) % 7; // convert to 0=Mon...6=Sun
        if (!scheduleDays[arrayIndex]) return false;

        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int min  = cal.get(java.util.Calendar.MINUTE);
        int nowMins = hour * 60 + min;
        int startMins = scheduleStartHour * 60 + scheduleStartMin;
        int endMins   = scheduleEndHour * 60 + scheduleEndMin;
        return nowMins >= startMins && nowMins < endMins;
    }

    // ── Getters / Setters ─────────────────────────────────

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getBlockedApps() { return blockedApps; }
    public void setBlockedApps(List<String> blockedApps) { this.blockedApps = blockedApps; }

    public boolean isScheduleEnabled() { return scheduleEnabled; }
    public void setScheduleEnabled(boolean scheduleEnabled) { this.scheduleEnabled = scheduleEnabled; }

    public int getScheduleStartHour() { return scheduleStartHour; }
    public void setScheduleStartHour(int h) { this.scheduleStartHour = h; }

    public int getScheduleStartMin() { return scheduleStartMin; }
    public void setScheduleStartMin(int m) { this.scheduleStartMin = m; }

    public int getScheduleEndHour() { return scheduleEndHour; }
    public void setScheduleEndHour(int h) { this.scheduleEndHour = h; }

    public int getScheduleEndMin() { return scheduleEndMin; }
    public void setScheduleEndMin(int m) { this.scheduleEndMin = m; }

    public boolean[] getScheduleDays() { return scheduleDays; }
    public void setScheduleDays(boolean[] days) { this.scheduleDays = days; }

    public boolean isPinLocked() { return pinLocked; }
    public void setPinLocked(boolean pinLocked) { this.pinLocked = pinLocked; }

    public boolean isAutoBreak() { return autoBreak; }
    public void setAutoBreak(boolean autoBreak) { this.autoBreak = autoBreak; }

    public int getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(int breakMinutes) { this.breakMinutes = breakMinutes; }
}
