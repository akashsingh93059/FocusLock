package com.focuslock.ui;

import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.focuslock.R;
import com.focuslock.utils.SessionManager;
import com.focuslock.utils.SessionManager.CompletedSession;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatsActivity extends AppCompatActivity {

    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        sm = SessionManager.get(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Your Stats");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindStats();
    }

    private void bindStats() {
        int totalMin = sm.getTotalMinutes();
        String focusedStr = totalMin >= 60
            ? (totalMin/60) + "h " + (totalMin%60) + "m"
            : totalMin + "m";

        ((TextView) findViewById(R.id.tvStatSessions)).setText(String.valueOf(sm.getTotalSessions()));
        ((TextView) findViewById(R.id.tvStatFocused)).setText(focusedStr);
        ((TextView) findViewById(R.id.tvStatStreak)).setText(sm.getStreak() + " 🔥");
        ((TextView) findViewById(R.id.tvStatBest)).setText(sm.getBestStreak() + " days");
        ((TextView) findViewById(R.id.tvStatDays)).setText(sm.getDaysActive() + " days");

        int[] week = sm.getWeekData();
        Calendar cal = Calendar.getInstance();
        int dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        ((TextView) findViewById(R.id.tvStatToday)).setText(week[dow] + " min");

        drawWeekChart(week);
        buildHistory();

        findViewById(R.id.btnResetStats).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Reset all stats?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Reset", (d,w) -> {
                    sm.resetStats();
                    bindStats();
                    Toast.makeText(this, "Stats cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null).show());
    }

    private void drawWeekChart(int[] weekData) {
        WeekChartView chart = findViewById(R.id.weekChart);
        chart.setData(weekData);
    }

    private void buildHistory() {
        LinearLayout container = findViewById(R.id.llHistory);
        container.removeAllViews();
        CompletedSession[] history = sm.getHistory();

        if (history == null || history.length == 0) {
            TextView empty = new TextView(this);
            empty.setText("No sessions yet — start your first focus session!");
            empty.setTextColor(0xFF5A5A78);
            empty.setTextSize(14);
            empty.setPadding(0, 16, 0, 0);
            container.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault());
        for (CompletedSession s : history) {
            if (s == null) continue;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, 12, 0, 12);

            View dot = new View(this);
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(20, 20);
            dp.setMarginEnd(16);
            dot.setLayoutParams(dp);
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(0xFF7C6BFF);
            dot.setBackground(gd);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView name = new TextView(this);
            name.setText(s.label != null ? s.label : "Focus Session");
            name.setTextColor(0xFFEEEEFF);
            name.setTextSize(14);

            TextView time = new TextView(this);
            time.setText(sdf.format(new Date(s.timestamp)));
            time.setTextColor(0xFF5A5A78);
            time.setTextSize(12);

            info.addView(name);
            info.addView(time);

            TextView dur = new TextView(this);
            dur.setText(s.minutes + " min");
            dur.setTextColor(0xFF6BFFC8);
            dur.setTextSize(15);
            dur.setTypeface(Typeface.MONOSPACE);

            row.addView(dot);
            row.addView(info);
            row.addView(dur);

            View div = new View(this);
            div.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
            div.setBackgroundColor(0xFF252535);

            container.addView(row);
            container.addView(div);
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
