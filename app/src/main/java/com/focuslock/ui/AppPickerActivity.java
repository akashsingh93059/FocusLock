package com.focuslock.ui;

import android.content.Intent;
import android.content.pm.*;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.focuslock.R;
import com.focuslock.model.AppInfo;
import java.util.*;

public class AppPickerActivity extends AppCompatActivity {
    private List<AppInfo> all = new ArrayList<>(), filtered = new ArrayList<>();
    private Set<String> selected = new HashSet<>();
    private Adapter adapter;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);

        ArrayList<String> prev = getIntent().getStringArrayListExtra("selected");
        if (prev != null) selected.addAll(prev);

        tvCount = findViewById(R.id.tvSelectedCount);
        RecyclerView rv = findViewById(R.id.rvApps);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        rv.setAdapter(adapter);

        loadApps();

        ((EditText)findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void onTextChanged(CharSequence s,int a,int b,int c){ filter(s.toString()); }
            public void afterTextChanged(Editable s){}
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            Intent r = new Intent();
            r.putStringArrayListExtra("selected", new ArrayList<>(selected));
            setResult(RESULT_OK, r);
            finish();
        });

        updateCount();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Set<String> distractions = new HashSet<>(Arrays.asList(AppInfo.DISTRACTION_PACKAGES));

        // Get all installed applications instead of just launcher apps
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo appInfo : apps) {
            String pkg = appInfo.packageName;
            // Skip system package and our own app
            if ("com.focuslock".equals(pkg)) continue;
            
            // Skip system apps (optional - only show user-installed apps)
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                // Check if it's an updated system app (like Chrome, Gmail)
                if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    continue;
                }
            }
            
            // Get app name
            CharSequence label = appInfo.loadLabel(pm);
            String appName = label != null ? label.toString() : pkg;
            
            AppInfo ai = new AppInfo(appName, pkg);
            ai.setSelected(selected.contains(pkg));
            all.add(ai);
        }
        
        // Sort by app name
        all.sort(Comparator.comparing(AppInfo::getAppName, String.CASE_INSENSITIVE_ORDER));

        // Sort: known distractions first
        all.sort((a, b) -> {
            boolean ad = distractions.contains(a.getPackageName());
            boolean bd = distractions.contains(b.getPackageName());
            if (ad && !bd) return -1;
            if (!ad && bd) return 1;
            return a.getAppName().compareTo(b.getAppName());
        });

        filtered = new ArrayList<>(all);
        adapter.notifyDataSetChanged();
    }

    private void filter(String q) {
        filtered.clear();
        if (q.isEmpty()) { filtered.addAll(all); }
        else {
            String lq = q.toLowerCase();
            for (AppInfo a : all)
                if (a.getAppName().toLowerCase().contains(lq)) filtered.add(a);
        }
        adapter.notifyDataSetChanged();
    }

    private void updateCount() {
        int n = selected.size();
        tvCount.setText(n + " app" + (n==1?"":"s") + " selected");
        ((Button)findViewById(R.id.btnDone)).setText("Done (" + n + ")");
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        class VH extends RecyclerView.ViewHolder {
            ImageView icon; TextView name, pkg, badge; CheckBox cb;
            VH(View v) {
                super(v);
                icon  = v.findViewById(R.id.ivAppIcon);
                name  = v.findViewById(R.id.tvAppName);
                pkg   = v.findViewById(R.id.tvAppPkg);
                badge = v.findViewById(R.id.tvAppBadge);
                cb    = v.findViewById(R.id.cbAppSelect);
            }
        }
        public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_app, p, false));
        }
        public void onBindViewHolder(VH h, int pos) {
            AppInfo ai = filtered.get(pos);
            h.name.setText(ai.getAppName());
            h.pkg.setText(ai.getPackageName());
            h.cb.setChecked(selected.contains(ai.getPackageName()));
            try { h.icon.setImageDrawable(getPackageManager().getApplicationIcon(ai.getPackageName())); }
            catch (Exception e) { h.icon.setImageResource(android.R.drawable.sym_def_app_icon); }
            if (ai.isKnownDistraction()) {
                h.badge.setText("⚠️ Known distraction");
                h.badge.setVisibility(View.VISIBLE);
            } else { h.badge.setVisibility(View.GONE); }
            h.itemView.setOnClickListener(v -> {
                boolean on = !selected.contains(ai.getPackageName());
                if (on) selected.add(ai.getPackageName()); else selected.remove(ai.getPackageName());
                h.cb.setChecked(on);
                updateCount();
            });
        }
        public int getItemCount() { return filtered.size(); }
    }
}
