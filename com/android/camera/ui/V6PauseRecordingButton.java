package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.Device;

public class V6PauseRecordingButton extends V6BottomAnimationImageView implements OnClickListener, V6FunctionUI {
    private boolean mVisible;

    public V6PauseRecordingButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    public void onClick(View view) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_video_pause_button, 2, null, null);
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mVisible = Device.isSupportedVideoPause() ? V6ModulePicker.isVideoModule() : false;
    }

    public void onResume() {
        super.onResume();
        setVisibility(0);
    }

    public void setVisibility(int i) {
        if (!this.mVisible) {
            i = 8;
        }
        super.setVisibility(i);
    }
}
