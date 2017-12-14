package com.android.camera.snap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.recyclerview.C0049R;
import android.view.ViewConfiguration;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Log;
import com.android.camera.snap.SnapCamera.SnapStatusListener;
import com.android.camera.storage.Storage;

public class SnapTrigger implements SnapStatusListener {
    private static final String TAG = SnapTrigger.class.getSimpleName();
    private static SnapTrigger sInstance;
    private int mBurstCount = 0;
    private SnapCamera mCamera = null;
    private Context mContext;
    private Handler mHandler;
    private final Runnable mLongPressRunnable = new C01262();
    private final Runnable mSnapRunnable = new C01251();

    class C01251 implements Runnable {
        C01251() {
        }

        public void run() {
            if (SnapTrigger.this.mCamera != null && Storage.getAvailableSpace() >= 52428800) {
                if (SnapTrigger.this.mCamera.isCamcorder()) {
                    SnapTrigger.this.shutdownWatchDog();
                    SnapTrigger.this.vibratorShort();
                    SnapTrigger.this.mCamera.startCamcorder();
                    Log.m0d(SnapTrigger.TAG, "take movie");
                    CameraDataAnalytics.instance().trackEvent("capture_times_quick_snap_movie");
                } else {
                    SnapTrigger.this.triggerWatchdog(false);
                    SnapTrigger.this.mCamera.takeSnap();
                    SnapTrigger snapTrigger = SnapTrigger.this;
                    snapTrigger.mBurstCount = snapTrigger.mBurstCount + 1;
                    Log.m0d(SnapTrigger.TAG, "take snap");
                    CameraDataAnalytics.instance().trackEvent("capture_times_quick_snap");
                }
            }
        }
    }

    class C01262 implements Runnable {
        C01262() {
        }

        public void run() {
            SnapTrigger.this.initCamera();
            if (SnapTrigger.this.mHandler != null) {
                SnapTrigger.this.mHandler.postDelayed(SnapTrigger.this.mSnapRunnable, (long) (SnapTrigger.this.mCamera.isCamcorder() ? 100 : 200));
            }
        }
    }

    public static synchronized void destroy() {
        synchronized (SnapTrigger.class) {
            if (sInstance != null) {
                sInstance.mBurstCount = 0;
                if (sInstance.mCamera != null) {
                    sInstance.mCamera.release();
                    sInstance.mCamera = null;
                }
                sInstance.mHandler = null;
                sInstance.mContext = null;
                sInstance = null;
            }
        }
    }

    public static SnapTrigger getInstance() {
        if (sInstance == null) {
            sInstance = new SnapTrigger();
        }
        return sInstance;
    }

    private void initCamera() {
        if (this.mCamera == null) {
            this.mCamera = new SnapCamera(this.mContext, this);
        }
    }

    public static void notifyForDetail(Context context, Uri uri, String str, String str2, boolean z) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setDataAndType(uri, z ? "video/*" : "image/*");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            Notification notification = new Notification(17301569, str, System.currentTimeMillis());
            notification.setLatestEventInfo(context, str, str2, PendingIntent.getActivity(context, 0, intent, 0));
            notification.flags |= 16;
            notificationManager.notify(0, notification);
        } catch (NullPointerException e) {
        }
    }

    private void shutdownWatchDog() {
        if (this.mHandler != null) {
            Log.m0d(TAG, "watch dog Off");
            this.mHandler.removeMessages(101);
        }
    }

    private void triggerWatchdog(boolean z) {
        if (this.mHandler != null) {
            Log.m0d(TAG, "watch dog On -" + z);
            this.mHandler.removeMessages(101);
            this.mHandler.sendEmptyMessageDelayed(101, (long) (z ? 0 : 5000));
        }
    }

    private void vibrator(long[] jArr) {
        try {
            ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(jArr, -1);
        } catch (Exception e) {
        }
    }

    private void vibratorShort() {
        vibrator(new long[]{10, 20});
    }

    public void handleKeyEvent(int i, int i2, long j) {
        if (isRunning()) {
            boolean z = false;
            if (i == 25) {
                if (i2 == 0) {
                    this.mHandler.postDelayed(this.mLongPressRunnable, ViewConfiguration.getGlobalActionKeyTimeout());
                } else if (i2 == 1) {
                    this.mHandler.removeCallbacks(this.mLongPressRunnable);
                    this.mHandler.removeCallbacks(this.mSnapRunnable);
                    z = true;
                }
            } else if (i == 26) {
                this.mHandler.removeCallbacks(this.mLongPressRunnable);
                this.mHandler.removeCallbacks(this.mSnapRunnable);
                z = true;
            }
            triggerWatchdog(z);
        }
    }

    public synchronized boolean init(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        return isRunning();
    }

    public synchronized boolean isRunning() {
        boolean z = false;
        synchronized (this) {
            if (!(this.mContext == null || this.mHandler == null)) {
                z = true;
            }
        }
        return z;
    }

    public void onDone(Uri uri) {
        if (isRunning()) {
            triggerWatchdog(false);
            vibratorShort();
            if (!this.mCamera.isCamcorder() && this.mBurstCount < 100) {
                this.mHandler.postDelayed(this.mSnapRunnable, 200);
            }
            notifyForDetail(this.mContext, uri, this.mContext.getString(C0049R.string.camera_snap_mode_title), this.mContext.getString(C0049R.string.camera_snap_mode_title_detail), this.mCamera.isCamcorder());
        }
    }
}
