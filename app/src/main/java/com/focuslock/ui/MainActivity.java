package com.focuslock.ui;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.app.AppOpsManager;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.focuslock.R;
import com.focuslock.model.FocusSession;
import com.focuslock.receiver.FocusDeviceAdminReceiver;
import com.focuslock.service.AppMonitorService;
import com.focuslock.utils.SessionManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SessionManager sm;
    private FocusSession session;
    private CountDownTimer countdown;

    // Tab views
    private LinearLayout tabTimer, tabSchedule, tabApps, tabStats;
    private View pageTimer, pageSchedule, pageApps, pageStats;

    // Timer page
    private TextView tvTimerClock, tvTimerStatus, tvStreakBadge;
    private SeekBar seekDuration;
    private TextView tvDurationVal;
    private Button btnStartStop;
    private Switch swAutoBreak, swPin, swProtect;

    // Schedule page
    private Switch swScheduleEnable;
    private TimePicker tpStart, tpEnd;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private TextView tvScheduleStatus;

    // Apps page
    private Button btnPickApps;
    private TextView tvBlockedCount;

    // Stats page — navigates to StatsActivity
    private Button btnViewStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = SessionManager.get(this);
        session = sm.loadSession();

        bindViews();
        setupTabs();
        setupTimerPage();
        setupSchedulePage();
        setupAppsPage();
        setupStatsPage();
        updateTimerUI();
        checkPermissions();
    }

    // ── Bind ──────────────────────────────────────────────
    private void bindViews() {
        tabTimer    = findViewById(R.id.tabTimer);
        tabSchedule = findViewById(R.id.tabSchedule);
        tabApps     = findViewById(R.id.tabApps);
        tabStats    = findViewById(R.id.tabStats);
        pageTimer   = findViewById(R.id.pageTimer);
        pageSchedule= findViewById(R.id.pageSchedule);
        pageApps    = findViewById(R.id.pageApps);
        pageStats   = findViewById(R.id.pageStats);

        tvTimerClock  = findViewById(R.id.tvTimerClock);
        tvTimerStatus = findViewById(R.id.tvTimerStatus);
        tvStreakBadge = findViewById(R.id.tvStreakBadge);
        seekDuration  = findViewById(R.id.seekDuration);
        tvDurationVal = findViewById(R.id.tvDurationVal);
        btnStartStop  = findViewById(R.id.btnStartStop);
        swAutoBreak   = findViewById(R.id.swAutoBreak);
        swPin         = findViewById(R.id.swPin);
        swProtect     = findViewById(R.id.swProtect);

        swScheduleEnable = findViewById(R.id.swScheduleEnable);
        tpStart       = findViewById(R.id.tpStart);
        tpEnd         = findViewById(R.id.tpEnd);
        cbMon=findViewById(R.id.cbMon); cbTue=findViewById(R.id.cbTue);
        cbWed=findViewById(R.id.cbWed); cbThu=findViewById(R.id.cbThu);
        cbFri=findViewById(R.id.cbFri); cbSat=findViewById(R.id.cbSat);
        cbSun=findViewById(R.id.cbSun);
        tvScheduleStatus = findViewById(R.id.tvScheduleStatus);

        btnPickApps    = findViewById(R.id.btnPickApps);
        tvBlockedCount = findViewById(R.id.tvBlockedCount);

        btnViewStats   = findViewById(R.id.btnViewStats);
    }

    // ── Tabs ──────────────────────────────────────────────
    private void setupTabs() {
        View[] pages = {pageTimer, pageSchedule, pageApps, pageStats};
        LinearLayout[] tabs = {tabTimer, tabSchedule, tabApps, tabStats};

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnClickListener(v -> {
                for (View p : pages) p.setVisibility(View.GONE);
                for (LinearLayout t : tabs) t.setAlpha(0.5f);
                pages[idx].setVisibility(View.VISIBLE);
                tabs[idx].setAlpha(1f);
            });
        }
        // Default: timer tab
        pageTimer.setVisibility(View.VISIBLE);
        tabTimer.setAlpha(1f);
    }

    // ── Timer Page ────────────────────────────────────────
    private void setupTimerPage() {
        seekDuration.setMax(115); // 5-120 min
        seekDuration.setProgress(session.getDurationMinutes() - 5);
        tvDurationVal.setText(session.getDurationMinutes() + " min");
        seekDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean user) {
                int m = p + 5;
                session.setDurationMinutes(m);
                tvDurationVal.setText(m + " min");
                if (!session.isActive())
                    tvTimerClock.setText(String.format(Locale.getDefault(), "%02d:00", m));
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) { sm.saveSession(session); }
        });

        swAutoBreak.setChecked(session.isAutoBreak());
        swAutoBreak.setOnCheckedChangeListener((b, c) -> { session.setAutoBreak(c); sm.saveSession(session); });

        swPin.setChecked(sm.isPinEnabled());
        swPin.setOnCheckedChangeListener((b, c) -> {
            if (c && sm.getPin().isEmpty()) {
                swPin.setChecked(false);
                startActivityForResult(new Intent(this, PinActivity.class)
                    .putExtra("mode","setup"), 300);
            } else {
                sm.setPinEnabled(c);
                session.setPinLocked(c);
                sm.saveSession(session);
            }
        });

        swProtect.setChecked(sm.isProtectEnabled());
        swProtect.setOnCheckedChangeListener((b, c) -> {
            if (c) enableDeviceAdmin();
            else disableDeviceAdmin();
        });

        btnStartStop.setOnClickListener(v -> {
            if (session.isActive()) stopSession();
            else startSession();
        });
    }

    private void startSession() {
        if (!hasUsageStats()) { requestUsageStats(); return; }
        if (!Settings.canDrawOverlays(this)) { requestOverlay(); return; }

        session.setActive(true);
        session.setStartTime(System.currentTimeMillis());
        sm.saveSession(session);

        Intent si = new Intent(this, AppMonitorService.class);
        si.setAction(AppMonitorService.ACTION_START);
        startForegroundService(si);

        beginCountdown();
        updateTimerUI();
        Toast.makeText(this, "🔒 Focus locked! Stay strong.", Toast.LENGTH_SHORT).show();
    }

    private void stopSession() {
        if (sm.isPinEnabled() && !sm.getPin().isEmpty()) {
            startActivityForResult(
                new Intent(this, PinActivity.class).putExtra("mode","verify"), 100);
        } else {
            confirmStop();
        }
    }

    private void confirmStop() {
        new AlertDialog.Builder(this)
            .setTitle("End session early?")
            .setMessage("Progress will not be recorded for incomplete sessions.")
            .setPositiveButton("End", (d,w) -> forceStop())
            .setNegativeButton("Keep going 💪", null).show();
    }

    private void forceStop() {
        if (countdown != null) { countdown.cancel(); countdown = null; }
        session.setActive(false);
        sm.saveSession(session);
        startService(new Intent(this, AppMonitorService.class)
            .setAction(AppMonitorService.ACTION_STOP));
        updateTimerUI();
        Toast.makeText(this, "Session ended", Toast.LENGTH_SHORT).show();
    }

    private void beginCountdown() {
        if (countdown != null) countdown.cancel();
        long ms = session.getRemainingMs();
        countdown = new CountDownTimer(ms, 1000) {
            public void onTick(long left) {
                long m = left/60_000, s = (left%60_000)/1_000;
                tvTimerClock.setText(String.format(Locale.getDefault(),"%02d:%02d",m,s));
            }
            public void onFinish() {
                tvTimerClock.setText("00:00");
                tvTimerStatus.setText("🎉 Session complete!");
                session.setActive(false);
                sm.saveSession(session);
                updateTimerUI();
            }
        }.start();
    }

    private void updateTimerUI() {
        boolean active = session.isActive();
        seekDuration.setEnabled(!active);
        swAutoBreak.setEnabled(!active);

        if (active) {
            btnStartStop.setText("⏹  END SESSION");
            btnStartStop.setBackgroundTintList(
                getColorStateList(android.R.color.holo_red_dark));
            tvTimerStatus.setText("Focus session running…");
            if (countdown == null && session.getRemainingMs() > 0) beginCountdown();
        } else {
            btnStartStop.setText("▶  START FOCUS");
            btnStartStop.setBackgroundTintList(
                getColorStateList(R.color.purple_500));
            tvTimerStatus.setText("Ready to focus");
            tvTimerClock.setText(String.format(Locale.getDefault(),
                "%02d:00", session.getDurationMinutes()));
        }

        tvStreakBadge.setText("🔥 " + sm.getStreak() + " day streak");
    }

    // ── Schedule Page ─────────────────────────────────────
    private void setupSchedulePage() {
        tpStart.setIs24HourView(true);
        tpEnd.setIs24HourView(true);
        tpStart.setHour(session.getScheduleStartHour());
        tpStart.setMinute(session.getScheduleStartMin());
        tpEnd.setHour(session.getScheduleEndHour());
        tpEnd.setMinute(session.getScheduleEndMin());

        boolean[] days = session.getScheduleDays();
        CheckBox[] cbs = {cbMon,cbTue,cbWed,cbThu,cbFri,cbSat,cbSun};
        for (int i=0;i<7;i++) cbs[i].setChecked(days[i]);

        swScheduleEnable.setChecked(session.isScheduleEnabled());
        updateScheduleStatus();

        View.OnClickListener save = v -> saveSchedule();
        tpStart.setOnTimeChangedListener((tp,h,m) -> saveSchedule());
        tpEnd.setOnTimeChangedListener((tp,h,m) -> saveSchedule());
        for (CheckBox cb : cbs) cb.setOnClickListener(save);

        swScheduleEnable.setOnCheckedChangeListener((b, c) -> {
            session.setScheduleEnabled(c);
            if (c) session.setMode(FocusSession.MODE_SCHEDULE);
            else session.setMode(FocusSession.MODE_BLOCK);
            sm.saveSession(session);
            updateScheduleStatus();
            Toast.makeText(this,
                c ? "📅 Schedule blocking enabled" : "Schedule blocking off",
                Toast.LENGTH_SHORT).show();
        });
    }

    private void saveSchedule() {
        session.setScheduleStartHour(tpStart.getHour());
        session.setScheduleStartMin(tpStart.getMinute());
        session.setScheduleEndHour(tpEnd.getHour());
        session.setScheduleEndMin(tpEnd.getMinute());
        boolean[] days = new boolean[7];
        CheckBox[] cbs = {cbMon,cbTue,cbWed,cbThu,cbFri,cbSat,cbSun};
        for (int i=0;i<7;i++) days[i] = cbs[i].isChecked();
        session.setScheduleDays(days);
        sm.saveSession(session);
        updateScheduleStatus();
    }

    private void updateScheduleStatus() {
        if (!session.isScheduleEnabled()) {
            tvScheduleStatus.setText("Schedule blocking is OFF");
            return;
        }
        boolean inWindow = session.isWithinSchedule();
        String time = String.format(Locale.getDefault(), "%02d:%02d – %02d:%02d",
            session.getScheduleStartHour(), session.getScheduleStartMin(),
            session.getScheduleEndHour(), session.getScheduleEndMin());
        tvScheduleStatus.setText(inWindow
            ? "✅ ACTIVE now — blocking from " + time
            : "⏳ Waiting… will block from " + time);
    }

    // ── Apps Page ─────────────────────────────────────────
    private void setupAppsPage() {
        updateAppsUI();
        btnPickApps.setOnClickListener(v -> {
            Intent i = new Intent(this, AppPickerActivity.class);
            i.putStringArrayListExtra("selected",
                new java.util.ArrayList<>(session.getBlockedApps()));
            startActivityForResult(i, 200);
        });
    }

    private void updateAppsUI() {
        int n = session.getBlockedApps().size();
        tvBlockedCount.setText(n > 0
            ? n + " app" + (n==1?"":"s") + " will be blocked"
            : "No apps selected yet");
        btnPickApps.setText(n > 0
            ? "📵 Change blocked apps (" + n + ")"
            : "📵 Select apps to block");
    }

    // ── Stats Page ────────────────────────────────────────
    private void setupStatsPage() {
        btnViewStats.setOnClickListener(v ->
            startActivity(new Intent(this, StatsActivity.class)));
        refreshQuickStats();
    }

    private void refreshQuickStats() {
        TextView tvQS = findViewById(R.id.tvQuickStats);
        if (tvQS == null) return;
        int min = sm.getTotalMinutes();
        tvQS.setText(
            "Sessions: " + sm.getTotalSessions() + "\n" +
            "Focused: " + (min >= 60 ? (min/60) + "h " + (min%60) + "m" : min + "m") + "\n" +
            "Streak: " + sm.getStreak() + " days\n" +
            "Best: " + sm.getBestStreak() + " days"
        );
    }

    // ── Permissions ───────────────────────────────────────
    private void checkPermissions() {
        if (!hasUsageStats()) {
            new AlertDialog.Builder(this)
                .setTitle("One-time Setup Required")
                .setMessage("FocusLock needs 'Usage Access' permission to detect which app you're using and block it.\n\nTap OK → find FocusLock → enable it.")
                .setPositiveButton("OK, let's do it", (d,w) -> requestUsageStats())
                .setCancelable(false).show();
        }
    }

    private boolean hasUsageStats() {
        AppOpsManager aom = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        int mode = aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStats() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        Toast.makeText(this, "Find FocusLock → enable Usage Access", Toast.LENGTH_LONG).show();
    }

    private void requestOverlay() {
        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getPackageName())));
        Toast.makeText(this, "Enable 'Display over other apps' for FocusLock", Toast.LENGTH_LONG).show();
    }

    private void enableDeviceAdmin() {
        Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
            new ComponentName(this, FocusDeviceAdminReceiver.class));
        i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Prevents FocusLock from being uninstalled during focus sessions.");
        startActivityForResult(i, 400);
    }

    private void disableDeviceAdmin() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        dpm.removeActiveAdmin(new ComponentName(this, FocusDeviceAdminReceiver.class));
        sm.setProtectEnabled(false);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK) {
            if (req == 200 && data != null) {
                java.util.ArrayList<String> sel = data.getStringArrayListExtra("selected");
                if (sel != null) { session.setBlockedApps(sel); sm.saveSession(session); }
                updateAppsUI();
            } else if (req == 300) {
                sm.setPinEnabled(true); swPin.setChecked(true);
                session.setPinLocked(true); sm.saveSession(session);
            } else if (req == 100 && data != null && data.getBooleanExtra("verified", false)) {
                confirmStop();
            } else if (req == 400) {
                swProtect.setChecked(sm.isProtectEnabled());
            }
        } else {
            if (req == 400) swProtect.setChecked(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        session = sm.loadSession();
        updateTimerUI();
        updateScheduleStatus();
        updateAppsUI();
        refreshQuickStats();
    }

    @Override
    protected void onDestroy() {
        if (countdown != null) countdown.cancel();
        super.onDestroy();
    }
}
