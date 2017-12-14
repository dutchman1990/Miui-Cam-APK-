package com.android.camera;

import android.content.Context;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;

public class CrashHandler implements UncaughtExceptionHandler {
    private static CrashHandler sInstance = new CrashHandler();
    private WeakReference<Context> mContextRef;
    private UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        this.mContextRef = new WeakReference(context);
        if (this.mDefaultHandler == null) {
            this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    public void uncaughtException(Thread thread, Throwable th) {
        Log.m2e("CameraFCHandler", "Camera FC, msg=" + th.getMessage());
        if (this.mContextRef != null) {
            CameraSettings.setEdgeMode((Context) this.mContextRef.get(), false);
            this.mContextRef = null;
        }
        if (this.mDefaultHandler != null) {
            Log.m2e("CameraFCHandler", "mDefaultHandler=" + this.mDefaultHandler);
            this.mDefaultHandler.uncaughtException(thread, th);
            this.mDefaultHandler = null;
        }
    }
}
