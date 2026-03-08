package com.focuslock.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.focuslock.R;
import com.focuslock.utils.SessionManager;
import java.util.Locale;

public class BlockedActivity extends Activity {
    private CountDownTimer refresher;
    private SessionManager sm;
    private TextView tvCoinBalance;
    private TextView tvTreeBalance;
    private Button btnPlantTree;
    private Button btnCutTree;

    private static final String[] QUOTES = {
        "📚 Stay focused — your goals need you now!",
        "💪 Discipline is choosing between what you want NOW and what you want MOST.",
        "🔥 Every minute you resist builds your future.",
        "🎯 Champions don't stop when it's hard. They stop when it's DONE.",
        "🌟 Close this. Go back to studying. You've got this.",
        "⚡ Your future self is watching. Don't let them down.",
        "🏆 One day or day one. You decide.",
        "🌳 Plant a tree today. Your future self will thank you.",
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

        // Setup coin and tree UI
        tvCoinBalance = findViewById(R.id.tvCoinBalance);
        tvTreeBalance = findViewById(R.id.tvTreeBalance);
        btnPlantTree = findViewById(R.id.btnPlantTree);
        btnCutTree = findViewById(R.id.btnCutTree);
        updateCoinAndTreeUI();

        // Countdown
        startRefresher(remainMs);

        // Go home
        findViewById(R.id.btnGoHome).setOnClickListener(v -> goHome());

        // Plant tree
        btnPlantTree.setOnClickListener(v -> plantTree());

        // Cut tree
        btnCutTree.setOnClickListener(v -> cutTree());

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

    private void updateCoinAndTreeUI() {
        int coins = sm.getCoins();
        int trees = sm.getTreesPlanted();
        
        tvCoinBalance.setText("🪙 " + coins + " coin" + (coins == 1 ? "" : "s"));
        tvTreeBalance.setText("🌳 " + trees + " tree" + (trees == 1 ? "" : "s"));
        
        // Enable/disable plant tree button based on coin balance
        if (coins >= 500) {
            btnPlantTree.setEnabled(true);
            btnPlantTree.setText("🌱 Plant Tree (500 Coins)");
        } else {
            btnPlantTree.setEnabled(false);
            btnPlantTree.setText("🌱 Need " + (500 - coins) + " more coins");
        }
        
        // Enable/disable cut tree button based on tree count
        if (trees > 0) {
            btnCutTree.setEnabled(true);
            btnCutTree.setText("🪓 Cut Tree (Get 500 Coins)");
        } else {
            btnCutTree.setEnabled(false);
            btnCutTree.setText("🪓 No trees to cut");
        }
    }

    private void plantTree() {
        int coins = sm.getCoins();
        if (coins < 500) {
            Toast.makeText(this, "Not enough coins! You need 500 coins to plant a tree.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("🌱 Plant a Virtual Tree?")
            .setMessage("Spend 500 coins to plant a tree in your virtual forest.\n\nThis is an achievement of your focus dedication!\n\nPlant this tree?")
            .setPositiveButton("Yes, Plant Tree! 🌳", (d, w) -> {
                if (sm.plantTree()) {
                    Toast.makeText(this, "🎉 Tree planted! You're growing your focus forest!", Toast.LENGTH_LONG).show();
                    updateCoinAndTreeUI();
                } else {
                    Toast.makeText(this, "Failed to plant tree. Try again.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void cutTree() {
        int trees = sm.getTreesPlanted();
        if (trees <= 0) {
            Toast.makeText(this, "No trees to cut!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("🪓 Cut a Tree?")
            .setMessage("⚠️ Warning: Cutting a tree will give you back 500 coins.\n\nBut remember - each tree represents your hard work and dedication. Cutting it down removes that achievement.\n\nAre you sure you want to cut a tree?")
            .setPositiveButton("Yes, Cut Tree", (d, w) -> {
                int coinsGained = sm.cutTree();
                if (coinsGained > 0) {
                    new AlertDialog.Builder(this)
                        .setTitle("😔 Tree Cut Down")
                        .setMessage("You gained " + coinsGained + " coins, but lost a tree from your forest.\n\nYour achievements represent real effort. Try to let your forest grow instead!")
                        .setPositiveButton("I understand", null)
                        .show();
                    updateCoinAndTreeUI();
                } else {
                    Toast.makeText(this, "Failed to cut tree. Try again.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("No, Keep My Forest", null)
            .show();
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
