package com.focuslock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.focuslock.model.FocusSession;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREFS       = "focuslock_prefs";
    private static final String KEY_SESSION = "current_session";
    private static final String KEY_PIN     = "pin";
    private static final String KEY_PIN_ON  = "pin_enabled";
    private static final String KEY_PROTECT = "device_admin_on";
    // Stats keys
    private static final String KEY_TOTAL_SESSIONS  = "stat_sessions";
    private static final String KEY_TOTAL_MINUTES   = "stat_minutes";
    private static final String KEY_STREAK          = "stat_streak";
    private static final String KEY_BEST_STREAK     = "stat_best_streak";
    private static final String KEY_LAST_DAY        = "stat_last_day";
    private static final String KEY_DAYS_ACTIVE     = "stat_days_active";
    private static final String KEY_WEEK_DATA       = "stat_week";
    private static final String KEY_HISTORY         = "stat_history";   // JSON array of CompletedSession
    // Coin system keys
    private static final String KEY_COINS           = "coins";
    private static final String KEY_FOCUS_MINUTES_ACCUMULATED = "focus_minutes_accumulated";
    private static final String KEY_TEMP_BREAK_END_TIME = "temp_break_end_time";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private static SessionManager instance;

    public static SessionManager get(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx.getApplicationContext());
        return instance;
    }

    private SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ── Session ──────────────────────────────────────────
    public void saveSession(FocusSession s) {
        prefs.edit().putString(KEY_SESSION, gson.toJson(s)).apply();
    }
    public FocusSession loadSession() {
        String j = prefs.getString(KEY_SESSION, null);
        if (j == null) return new FocusSession();
        try { return gson.fromJson(j, FocusSession.class); }
        catch (Exception e) { return new FocusSession(); }
    }
    public boolean isSessionActive() { return loadSession().isActive(); }

    // ── PIN ──────────────────────────────────────────────
    public void setPin(String pin) { prefs.edit().putString(KEY_PIN, pin).apply(); }
    public String getPin() { return prefs.getString(KEY_PIN, ""); }
    public void setPinEnabled(boolean on) { prefs.edit().putBoolean(KEY_PIN_ON, on).apply(); }
    public boolean isPinEnabled() { return prefs.getBoolean(KEY_PIN_ON, false); }
    public boolean verifyPin(String pin) { return pin.equals(getPin()); }
    public void setProtectEnabled(boolean on) { prefs.edit().putBoolean(KEY_PROTECT, on).apply(); }
    public boolean isProtectEnabled() { return prefs.getBoolean(KEY_PROTECT, false); }

    // ── Stats ─────────────────────────────────────────────
    public static class CompletedSession {
        public long timestamp;
        public int minutes;
        public String label;
        public CompletedSession(long ts, int min, String label) {
            this.timestamp = ts; this.minutes = min; this.label = label;
        }
    }

    public void recordCompleted(int minutes, String label) {
        int total    = prefs.getInt(KEY_TOTAL_SESSIONS, 0) + 1;
        int totalMin = prefs.getInt(KEY_TOTAL_MINUTES, 0) + minutes;

        long todayDay = System.currentTimeMillis() / 86_400_000L;
        long lastDay  = prefs.getLong(KEY_LAST_DAY, 0);
        int streak    = prefs.getInt(KEY_STREAK, 0);
        int days      = prefs.getInt(KEY_DAYS_ACTIVE, 0);

        if (lastDay != todayDay) {
            streak = (lastDay == todayDay - 1) ? streak + 1 : 1;
            days++;
        }
        int best = Math.max(prefs.getInt(KEY_BEST_STREAK, 0), streak);

        // Week data (index = day of week 0=Mon)
        int[] week = getWeekData();
        int dow = (int)((todayDay + 3) % 7); // 0=Mon
        week[dow] += minutes;

        // History (keep last 30)
        CompletedSession[] history = getHistory();
        CompletedSession[] newHistory;
        int len = Math.min(history.length, 29);
        newHistory = new CompletedSession[len + 1];
        newHistory[0] = new CompletedSession(System.currentTimeMillis(), minutes, label);
        System.arraycopy(history, 0, newHistory, 1, len);

        prefs.edit()
            .putInt(KEY_TOTAL_SESSIONS, total)
            .putInt(KEY_TOTAL_MINUTES, totalMin)
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_BEST_STREAK, best)
            .putLong(KEY_LAST_DAY, todayDay)
            .putInt(KEY_DAYS_ACTIVE, days)
            .putString(KEY_WEEK_DATA, gson.toJson(week))
            .putString(KEY_HISTORY, gson.toJson(newHistory))
            .apply();
    }

    public int getTotalSessions() { return prefs.getInt(KEY_TOTAL_SESSIONS, 0); }
    public int getTotalMinutes()  { return prefs.getInt(KEY_TOTAL_MINUTES, 0); }
    public int getStreak()        { return prefs.getInt(KEY_STREAK, 0); }
    public int getBestStreak()    { return prefs.getInt(KEY_BEST_STREAK, 0); }
    public int getDaysActive()    { return prefs.getInt(KEY_DAYS_ACTIVE, 0); }

    public int[] getWeekData() {
        String j = prefs.getString(KEY_WEEK_DATA, null);
        if (j == null) return new int[7];
        try { return gson.fromJson(j, int[].class); }
        catch (Exception e) { return new int[7]; }
    }

    public CompletedSession[] getHistory() {
        String j = prefs.getString(KEY_HISTORY, null);
        if (j == null) return new CompletedSession[0];
        try { return gson.fromJson(j, CompletedSession[].class); }
        catch (Exception e) { return new CompletedSession[0]; }
    }

    public void resetStats() {
        prefs.edit()
            .remove(KEY_TOTAL_SESSIONS).remove(KEY_TOTAL_MINUTES)
            .remove(KEY_STREAK).remove(KEY_BEST_STREAK)
            .remove(KEY_LAST_DAY).remove(KEY_DAYS_ACTIVE)
            .remove(KEY_WEEK_DATA).remove(KEY_HISTORY)
            .apply();
    }

    // ── Coin System ───────────────────────────────────────
    /**
     * Get current coin balance
     */
    public int getCoins() {
        return prefs.getInt(KEY_COINS, 0);
    }

    /**
     * Add coins to the balance
     */
    public void addCoins(int amount) {
        int current = getCoins();
        prefs.edit().putInt(KEY_COINS, current + amount).apply();
    }

    /**
     * Spend coins if enough balance exists
     * @return true if coins were spent, false if insufficient balance
     */
    public boolean spendCoins(int amount) {
        int current = getCoins();
        if (current >= amount) {
            prefs.edit().putInt(KEY_COINS, current - amount).apply();
            return true;
        }
        return false;
    }

    /**
     * Track accumulated focus minutes and award coins
     * Every 1 minute = 1 coin
     * @param minutes Minutes to add
     * @return Number of coins earned from this addition
     */
    public int addFocusMinutesAndAwardCoins(int minutes) {
        // Award 1 coin per minute directly, no need to accumulate
        if (minutes > 0) {
            addCoins(minutes);
        }
        
        return minutes;
    }

    /**
     * Get accumulated minutes that haven't been converted to coins yet
     */
    public int getAccumulatedMinutes() {
        return prefs.getInt(KEY_FOCUS_MINUTES_ACCUMULATED, 0);
    }

    /**
     * Start a temporary break for the specified duration
     * @param minutes Duration of the break in minutes
     */
    public void startTemporaryBreak(int minutes) {
        long endTime = System.currentTimeMillis() + (minutes * 60_000L);
        prefs.edit().putLong(KEY_TEMP_BREAK_END_TIME, endTime).apply();
    }

    /**
     * Check if currently in a temporary break
     * @return true if in a break, false otherwise
     */
    public boolean isInTemporaryBreak() {
        long endTime = prefs.getLong(KEY_TEMP_BREAK_END_TIME, 0);
        if (endTime == 0) return false;
        if (System.currentTimeMillis() >= endTime) {
            // Break has expired, clear it
            prefs.edit().remove(KEY_TEMP_BREAK_END_TIME).apply();
            return false;
        }
        return true;
    }

    /**
     * Get remaining time in current temporary break
     * @return remaining milliseconds, or 0 if not in a break
     */
    public long getTemporaryBreakRemainingMs() {
        long endTime = prefs.getLong(KEY_TEMP_BREAK_END_TIME, 0);
        if (endTime == 0) return 0;
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Cancel the current temporary break
     */
    public void cancelTemporaryBreak() {
        prefs.edit().remove(KEY_TEMP_BREAK_END_TIME).apply();
    }
    }
}
