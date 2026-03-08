package com.focuslock.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.*;
import android.view.WindowManager;
import android.widget.*;
import com.focuslock.R;
import com.focuslock.utils.SessionManager;
import java.util.Locale;

public class BlockedActivity extends Activity {
    private CountDownTimer refresher;
    private SessionManager sm;

    private static final String[] QUOTES = {
        "📚 Stay focused — your goals need you now!",
        "💪 Discipline is choosing between what you want NOW and what you want MOST.",
        "🔥 Every minute you resist builds your future.",
        "🎯 Champions don't stop when it's hard. They stop when it's DONE.",
        "🌟 Close this. Go back to studying. You've got this.",
        "⚡ Your future self is watching. Don't let them down.",
        "🏆 One day or day one. You decide.",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                             WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_blocked);

        sm = SessionManager.get(this);
        String pkg        = getIntent().getStringExtra("blocked_pkg");
        long remainMs     = getIntent().getLongExtra("remaining_ms", 0);
        String mode       = getIntent().getStringExtra("mode");

        // App name
        String appName = pkg;
        try {
            appName = getPackageManager()
                .getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) {}

        ((TextView) findViewById(R.id.tvBlockedName)).setText("🚫 " + appName + " is blocked");

        String quote = QUOTES[(int)(System.currentTimeMillis() % QUOTES.length)];
        ((TextView) findViewById(R.id.tvQuote)).setText(quote);

        if ("schedule".equals(mode)) {
            ((TextView) findViewById(R.id.tvModeInfo)).setText(
                "📅 Schedule blocking is active.\nOnly allowed apps may be used right now.");
        } else {
            ((TextView) findViewById(R.id.tvModeInfo)).setText(
                "This app is blocked during your focus session.");
        }

        // Countdown
        startRefresher(remainMs);

        // Go home
        findViewById(R.id.btnGoHome).setOnClickListener(v -> goHome());

        // End session
        findViewById(R.id.btnEndSession).setOnClickListener(v -> {
            if (sm.isPinEnabled() && !sm.getPin().isEmpty()) {
                startActivityForResult(
                    new Intent(this, PinActivity.class).putExtra("mode","verify"), 100);
            } else {
                confirmEnd();
            }
        });
    }

    private void startRefresher(long initMs) {
        updateCountdown(initMs);
        refresher = new CountDownTimer(initMs, 1000) {
            public void onTick(long ms) { updateCountdown(ms); }
            public void onFinish() { finish(); }
        }.start();
    }

    private void updateCountdown(long ms) {
        long m = ms/60_000, s=(ms%60_000)/1_000;
        ((TextView)findViewById(R.id.tvCountdown)).setText(
            String.format(Locale.getDefault(), "%02d:%02d remaining", m, s));
    }

    private void confirmEnd() {
        new AlertDialog.Builder(this)
            .setTitle("End focus session?")
            .setNegativeButton("Keep focusing 💪", null)
            .setPositiveButton("End", (d,w) -> {
                startService(new Intent(this,
                    com.focuslock.service.AppMonitorService.class)
                    .setAction(com.focuslock.service.AppMonitorService.ACTION_STOP));
                com.focuslock.model.FocusSession fs = sm.loadSession();
                fs.setActive(false);
                sm.saveSession(fs);
                goHome();
            }).show();
    }

    private void goHome() {
        Intent h = new Intent(Intent.ACTION_MAIN);
        h.addCategory(Intent.CATEGORY_HOME);
        h.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(h);
        finish();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req==100 && res==RESULT_OK && data!=null && data.getBooleanExtra("verified",false))
            confirmEnd();
    }

    @Override public void onBackPressed() { goHome(); }

    @Override protected void onDestroy() {
        if (refresher != null) refresher.cancel();
        super.onDestroy();
    }
}
