package com.focuslock.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.*;
import android.widget.Toast;
import com.focuslock.utils.SessionManager;

public class FocusDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context ctx, Intent i) {
        SessionManager.get(ctx).setProtectEnabled(true);
        Toast.makeText(ctx, "🔒 Uninstall protection ON", Toast.LENGTH_SHORT).show();
    }
    @Override
    public CharSequence onDisableRequested(Context ctx, Intent i) {
        if (SessionManager.get(ctx).isSessionActive())
            return "⚠️ A focus session is active! Disabling will remove protection.";
        return "Disable uninstall protection?";
    }
    @Override
    public void onDisabled(Context ctx, Intent i) {
        SessionManager.get(ctx).setProtectEnabled(false);
        Toast.makeText(ctx, "Uninstall protection removed", Toast.LENGTH_SHORT).show();
    }
}
