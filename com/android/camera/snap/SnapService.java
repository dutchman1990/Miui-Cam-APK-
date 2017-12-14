package com.android.camera.snap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Log;
import com.android.camera.storage.Storage;
import java.lang.ref.WeakReference;

public class SnapService extends Service {
    private static final String TAG = SnapService.class.getSimpleName();
    private final InnerHandler mHandler = new InnerHandler(this);
    private BroadcastReceiver mReceiver = new C01241();
    private boolean mRegistered;

    class C01241 extends BroadcastReceiver {
        C01241() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.KEYCODE_POWER_UP".equals(intent.getAction()) || "android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                SnapTrigger.getInstance().handleKeyEvent(26, 0, System.currentTimeMillis());
            }
        }
    }

    private static class InnerHandler extends Handler {
        private final WeakReference<SnapService> mService;

        public InnerHandler(SnapService snapService) {
            this.mService = new WeakReference(snapService);
        }

        public void handleMessage(Message message) {
            SnapService snapService = (SnapService) this.mService.get();
            if (message != null && snapService != null) {
                switch (message.what) {
                    case 101:
                        Log.m0d(SnapService.TAG, "stop service");
                        snapService.destroy();
                        snapService.stopSelf();
                        break;
                }
            }
        }
    }

    private void destroy() {
        unregistPowerkeyReceiver();
        this.mHandler.removeMessages(101);
        SnapTrigger.destroy();
    }

    private void registePowerkeyReceiver() {
        if (!this.mRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.KEYCODE_POWER_UP");
            registerReceiver(this.mReceiver, intentFilter);
            this.mRegistered = true;
        }
    }

    private void triggerWatchdog() {
        this.mHandler.removeMessages(101);
        this.mHandler.sendEmptyMessageDelayed(101, 5000);
    }

    private void unregistPowerkeyReceiver() {
        if (this.mRegistered) {
            unregisterReceiver(this.mReceiver);
            this.mRegistered = false;
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    public void onStart(Intent intent, int i) {
        super.onStart(intent, i);
        Log.m0d(TAG, "start service");
        CameraDataAnalytics.instance().trackEvent("launch_snap_times_key");
        Storage.initStorage(this);
        triggerWatchdog();
        if (intent != null && SnapTrigger.getInstance().init(this, this.mHandler)) {
            SnapTrigger.getInstance().handleKeyEvent(intent.getIntExtra("key_code", 0), intent.getIntExtra("key_action", 0), intent.getLongExtra("key_event_time", 0));
            registePowerkeyReceiver();
        }
    }
}
