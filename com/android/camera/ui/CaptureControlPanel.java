package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import com.android.camera.ActivityBase;

public class CaptureControlPanel extends RelativeLayout implements Rotatable, OnClickListener, V6FunctionUI {
    private V6BottomAnimationImageView mCancle;
    private V6BottomAnimationImageView mDone;
    private MessageDispacher mMessageDispacher;
    private boolean mVisible = true;

    public CaptureControlPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void enableControls(boolean z) {
    }

    public V6BottomAnimationImageView getReviewCanceledView() {
        return this.mCancle;
    }

    public V6BottomAnimationImageView getReviewDoneView() {
        return this.mDone;
    }

    public void onCameraOpen() {
    }

    public void onClick(View view) {
        if (this.mDone == view) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.capture_control_panel, 2, null, null);
        } else {
            this.mMessageDispacher.dispacherMessage(1, C0049R.id.capture_control_panel, 2, null, null);
        }
    }

    public void onCreate() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDone = (V6BottomAnimationImageView) findViewById(C0049R.id.v6_btn_done);
        this.mCancle = (V6BottomAnimationImageView) findViewById(C0049R.id.v6_btn_cancel);
        this.mDone.setOnClickListener(this);
        this.mCancle.setOnClickListener(this);
    }

    public void onPause() {
    }

    public void onResume() {
        ActivityBase activityBase = (ActivityBase) getContext();
        this.mVisible = !activityBase.isImageCaptureIntent() ? activityBase.isVideoCaptureIntent() : true;
        if (this.mVisible) {
            setVisibility(0);
            this.mCancle.setVisibility(0);
        } else {
            setVisibility(8);
            this.mCancle.setVisibility(8);
        }
        this.mDone.setVisibility(8);
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setOrientation(int i, boolean z) {
        if (this.mVisible) {
            this.mDone.setOrientation(i, z);
            this.mCancle.setOrientation(i, z);
        }
    }
}
