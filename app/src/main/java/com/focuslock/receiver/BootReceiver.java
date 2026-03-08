package com.focuslock.receiver;

import android.content.*;
import com.focuslock.service.AppMonitorService;
import com.focuslock.utils.SessionManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String a = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(a) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            if (SessionManager.get(ctx).isSessionActive()) {
                Intent si = new Intent(ctx, AppMonitorService.class);
                si.setAction(AppMonitorService.ACTION_START);
                ctx.startForegroundService(si);
            }
        }
    }
}
