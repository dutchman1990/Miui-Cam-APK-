package com.android.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.camera.permission.PermissionManager;
import com.android.camera.preferences.CameraSettingPreferences;

public class CameraButtonIntentReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        CameraHolder instance = CameraHolder.instance();
        int readPreferredCameraId = CameraSettings.readPreferredCameraId(CameraSettingPreferences.instance());
        if (PermissionManager.checkCameraLaunchPermissions() && instance.tryOpen(readPreferredCameraId) != null) {
            instance.keep();
            instance.release();
            Intent intent2 = new Intent("android.intent.action.MAIN");
            intent2.setClass(context, Camera.class);
            intent2.addCategory("android.intent.category.LAUNCHER");
            intent2.setFlags(268435456);
            context.startActivity(intent2);
        }
    }
}
