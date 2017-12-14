package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;

public class V6VideoCaptureButton extends V6BottomAnimationImageView implements OnClickListener, V6FunctionUI {
    public V6VideoCaptureButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public void onCameraOpen() {
        setVisibility(8);
    }

    public void onClick(View view) {
        if (this.mMessageDispacher != null && V6ModulePicker.isVideoModule()) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_video_capture_button, 2, null, null);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
    }

    public void setVisibility(int i) {
        if (!(V6ModulePicker.isVideoModule() && !((ActivityBase) this.mContext).isVideoCaptureIntent() && CameraSettings.isVideoCaptureVisible())) {
            i = 8;
        }
        super.setVisibility(i);
    }
}
