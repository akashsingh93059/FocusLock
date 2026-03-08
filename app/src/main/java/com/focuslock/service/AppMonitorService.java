package com.focuslock.service;

import android.app.*;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import com.focuslock.R;
import com.focuslock.model.FocusSession;
import com.focuslock.ui.BlockedActivity;
import com.focuslock.ui.MainActivity;
import com.focuslock.utils.SessionManager;

import java.util.Locale;

public class AppMonitorService extends Service {

    public static final String ACTION_START = "com.focuslock.START";
    public static final String ACTION_STOP  = "com.focuslock.STOP";

    private static final String CHANNEL_MONITOR  = "fl_monitor";
    private static final String CHANNEL_DONE     = "fl_done";
    private static final int    NOTIF_MONITOR_ID = 1;
    private static final long   POLL_MS          = 500;

    private Handler handler;
    private Runnable poller;
    private UsageStatsManager usm;
    private SessionManager sm;
    private String lastBlockedPkg = "";

    @Override
    public void onCreate() {
        super.onCreate();
        usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        sm  = SessionManager.get(this);
        handler = new Handler(Looper.getMainLooper());
        createChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopPoller();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(NOTIF_MONITOR_ID, buildNotif("Focus session active 🔒"));
        startPoller();
        return START_STICKY;
    }

    private void startPoller() {
        if (poller != null) return;
        poller = new Runnable() {
            @Override public void run() {
                tick();
                handler.postDelayed(this, POLL_MS);
            }
        };
        handler.post(poller);
    }

    private void stopPoller() {
        if (poller != null) { handler.removeCallbacks(poller); poller = null; }
    }

    private void tick() {
        FocusSession s = sm.loadSession();
        if (!s.isActive()) { stopSelf(); return; }

        if (s.isExpired()) { onExpired(s); return; }

        // Update notification with countdown
        long ms  = s.getRemainingMs();
        long min = ms / 60_000;
        long sec = (ms % 60_000) / 1_000;
        String modeTag = FocusSession.MODE_SCHEDULE.equals(s.getMode()) ? "📅 " : "🔒 ";

        // In schedule mode, show whether we're in the blocking window
        if (FocusSession.MODE_SCHEDULE.equals(s.getMode())) {
            if (s.isWithinSchedule()) {
                updateNotif(modeTag + "BLOCKING NOW — " +
                    String.format(Locale.getDefault(), "%02d:%02d left", min, sec));
            } else {
                updateNotif("📅 Schedule active — waiting for block window");
            }
        } else {
            updateNotif(modeTag + String.format(Locale.getDefault(), "Focus: %02d:%02d remaining", min, sec));
        }

        // Check foreground app
        String fg = getForeground();
        if (fg == null || fg.isEmpty()) return;

        if (s.shouldBlockApp(fg)) {
            if (!fg.equals(lastBlockedPkg)) {
                lastBlockedPkg = fg;
                launchBlockScreen(fg, s);
            }
        } else {
            lastBlockedPkg = "";
        }
    }

    private String getForeground() {
        long now = System.currentTimeMillis();
        UsageEvents events = usm.queryEvents(now - 3000, now);
        if (events == null) return null;
        UsageEvents.Event ev = new UsageEvents.Event();
        String last = null;
        while (events.hasNextEvent()) {
            events.getNextEvent(ev);
            if (ev.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                last = ev.getPackageName();
            }
        }
        return last;
    }

    private void launchBlockScreen(String pkg, FocusSession session) {
        Intent i = new Intent(this, BlockedActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                   Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("blocked_pkg", pkg);
        i.putExtra("remaining_ms", session.getRemainingMs());
        i.putExtra("mode", session.getMode());
        startActivity(i);
    }

    private void onExpired(FocusSession s) {
        int minutes = s.getDurationMinutes();
        sm.recordCompleted(minutes, "Focus session");
        
        // Award coins based on focus time (45 min = 1 coin)
        int coinsEarned = sm.addFocusMinutesAndAwardCoins(minutes);
        
        s.setActive(false);
        sm.saveSession(s);
        stopPoller();

        // Vibrate
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null) v.vibrate(VibrationEffect.createWaveform(new long[]{0,300,200,300}, -1));

        // Done notification with coin info
        NotificationManager nm = getSystemService(NotificationManager.class);
        String contentText = "You focused for " + minutes + " minutes. Amazing work!";
        if (coinsEarned > 0) {
            contentText += " 🪙 +" + coinsEarned + " coin" + (coinsEarned == 1 ? "" : "s") + "!";
        }
        Notification n = new NotificationCompat.Builder(this, CHANNEL_DONE)
            .setContentTitle("🎉 Focus session complete!")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_lock)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build();
        nm.notify(2, n);

        stopForeground(true);
        stopSelf();
    }

    private void createChannels() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(
            CHANNEL_MONITOR, "Focus Monitor", NotificationManager.IMPORTANCE_LOW));
        nm.createNotificationChannel(new NotificationChannel(
            CHANNEL_DONE, "Session Complete", NotificationManager.IMPORTANCE_HIGH));
    }

    private Notification buildNotif(String text) {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CHANNEL_MONITOR)
            .setContentTitle("FocusLock")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_lock)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build();
    }

    private void updateNotif(String text) {
        getSystemService(NotificationManager.class)
            .notify(NOTIF_MONITOR_ID, buildNotif(text));
    }

    @Override public IBinder onBind(Intent i) { return null; }
    @Override public void onDestroy() { stopPoller(); super.onDestroy(); }
}
