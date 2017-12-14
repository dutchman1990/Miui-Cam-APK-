package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;

public class V6ModulePicker extends V6BottomAnimationImageView implements OnClickListener {
    private static final String TAG = V6ModulePicker.class.getSimpleName();
    private static int sCurrentModule = 0;
    private boolean mEnabled;
    private boolean mVisible;

    public V6ModulePicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    public static int getCurrentModule() {
        return sCurrentModule;
    }

    private void initModulePickView() {
        if (isVideoModule()) {
            setImageResource(C0049R.drawable.video_module_picker_bg);
            setContentDescription(getResources().getString(C0049R.string.accessibility_camera_module_picker));
            return;
        }
        setImageResource(C0049R.drawable.camera_module_picker_bg);
        setContentDescription(getResources().getString(C0049R.string.accessibility_video_module_picker));
    }

    public static boolean isCameraModule() {
        return sCurrentModule == 0;
    }

    public static boolean isPanoramaModule() {
        return sCurrentModule == 2;
    }

    public static boolean isVideoModule() {
        return sCurrentModule == 1;
    }

    public static void setCurrentModule(int i) {
        sCurrentModule = i;
    }

    public void enableControls(boolean z) {
        this.mEnabled = z;
        setEnabled(z);
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        setVisibility(0);
    }

    public void onClick(View view) {
        Log.v(TAG, "ModulePicker onclick");
        int i = sCurrentModule == 1 ? 0 : 1;
        if (this.mMessageDispacher != null && this.mEnabled) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_module_picker, 2, Integer.valueOf(i), null);
        }
        AutoLockManager.getInstance(getContext()).onUserInteraction();
    }

    public void onCreate() {
        boolean z = false;
        super.onCreate();
        ActivityBase activityBase = (ActivityBase) this.mContext;
        if (!(activityBase.isImageCaptureIntent() || activityBase.isVideoCaptureIntent())) {
            z = true;
        }
        this.mVisible = z;
        if (this.mVisible) {
            initModulePickView();
        }
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        ActivityBase activityBase = (ActivityBase) this.mContext;
        if (!(activityBase.isImageCaptureIntent() || activityBase.isVideoCaptureIntent())) {
            z = true;
        }
        this.mVisible = z;
    }

    public void setVisibility(int i) {
        if (!this.mVisible) {
            i = 8;
        }
        super.setVisibility(i);
    }
}
