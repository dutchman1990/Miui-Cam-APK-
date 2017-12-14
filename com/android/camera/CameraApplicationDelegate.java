package com.android.camera;

import android.app.Activity;
import android.content.Context;
import java.util.Stack;

public class CameraApplicationDelegate {
    private static CameraAppImpl sContext;
    private Stack<Activity> mActivities;
    private boolean mRestoreSetting = false;

    public CameraApplicationDelegate(CameraAppImpl cameraAppImpl) {
        sContext = cameraAppImpl;
    }

    public static Context getAndroidContext() {
        return sContext;
    }

    public synchronized void addActivity(Activity activity) {
        if (activity != null) {
            this.mActivities.push(activity);
        }
    }

    public synchronized void closeAllActivitiesBut(Activity activity) {
        int i = 0;
        for (int i2 = 0; i2 < getActivityCount(); i2++) {
            Activity activity2 = getActivity(i);
            if (activity2 != activity) {
                activity2.finish();
                this.mActivities.remove(activity2);
            } else {
                i++;
            }
        }
    }

    public synchronized Activity getActivity(int i) {
        if (i >= 0) {
            if (i < getActivityCount()) {
                return (Activity) this.mActivities.get(i);
            }
        }
        return null;
    }

    public synchronized int getActivityCount() {
        return this.mActivities.size();
    }

    public boolean getSettingsFlag() {
        return this.mRestoreSetting;
    }

    public void onCreate() {
        Util.initialize(sContext);
        this.mActivities = new Stack();
        this.mRestoreSetting = true;
    }

    public synchronized void removeActivity(Activity activity) {
        if (activity != null) {
            this.mActivities.remove(activity);
        }
    }

    public void resetRestoreFlag() {
        this.mRestoreSetting = false;
    }
}
