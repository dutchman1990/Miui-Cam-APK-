package com.android.camera.storage;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.recyclerview.C0049R;
import com.android.camera.CameraAppImpl;

public class PriorityStorageBroadcastReceiver extends BroadcastReceiver {
    public static boolean isPriorityStorage() {
        boolean z = true;
        Context androidContext = CameraAppImpl.getAndroidContext();
        int componentEnabledSetting = androidContext.getPackageManager().getComponentEnabledSetting(new ComponentName(androidContext, PriorityStorageBroadcastReceiver.class));
        if (componentEnabledSetting == 0) {
            return androidContext.getResources().getBoolean(C0049R.bool.priority_storage);
        }
        if (componentEnabledSetting != 1) {
            z = false;
        }
        return z;
    }

    public static void setPriorityStorage(boolean z) {
        Context androidContext = CameraAppImpl.getAndroidContext();
        androidContext.getPackageManager().setComponentEnabledSetting(new ComponentName(androidContext, PriorityStorageBroadcastReceiver.class), z ? 1 : 2, 1);
    }

    public void onReceive(Context context, Intent intent) {
    }
}
