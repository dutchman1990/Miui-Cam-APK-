package com.android.camera.ui;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.recyclerview.C0049R;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.camera.Util;

public class RotateTextToast {
    private static RotateTextToast sRotateTextToast;
    Handler mHandler = new Handler();
    ViewGroup mLayoutRoot;
    private final Runnable mRunnable = new C01451();
    RotateLayout mToast;

    class C01451 implements Runnable {
        C01451() {
        }

        public void run() {
            Util.fadeOut(RotateTextToast.this.mToast);
            RotateTextToast.this.mLayoutRoot.removeView(RotateTextToast.this.mToast);
            RotateTextToast.this.mToast = null;
            RotateTextToast.sRotateTextToast = null;
        }
    }

    private RotateTextToast(Activity activity) {
        this.mLayoutRoot = (ViewGroup) activity.getWindow().getDecorView();
        this.mToast = (RotateLayout) activity.getLayoutInflater().inflate(C0049R.layout.rotate_text_toast, this.mLayoutRoot).findViewById(C0049R.id.rotate_toast);
    }

    public static RotateTextToast getInstance() {
        return sRotateTextToast;
    }

    public static RotateTextToast getInstance(Activity activity) {
        if (sRotateTextToast == null) {
            sRotateTextToast = new RotateTextToast(activity);
        }
        return sRotateTextToast;
    }

    public void show(int i, int i2) {
        if (i == 0) {
            this.mHandler.removeCallbacks(this.mRunnable);
            this.mHandler.post(this.mRunnable);
            return;
        }
        ((TextView) this.mToast.findViewById(C0049R.id.message)).setText(i);
        this.mToast.setOrientation(i2, false);
        this.mToast.setVisibility(0);
        this.mHandler.removeCallbacks(this.mRunnable);
        this.mHandler.postDelayed(this.mRunnable, 5000);
    }
}
