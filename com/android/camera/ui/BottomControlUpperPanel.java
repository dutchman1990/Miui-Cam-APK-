package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import com.android.camera.ActivityBase;
import com.android.camera.Log;

public class BottomControlUpperPanel extends V6RelativeLayout implements AnimationListener {
    private Animation mAnimationIn;
    private Runnable mAnimationInCallback;
    private Animation mAnimationOut;
    private Runnable mAnimationOutCallback;
    public V6CameraPicker mCameraPicker;
    private boolean mControlVisible;
    public EffectButton mEffectButton;
    public ModeButton mModeButton;
    public SkinBeautyButton mSkinBeautyButton;
    public V6VideoCaptureButton mVideoCaptureButton;

    public BottomControlUpperPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void animateIn(Runnable runnable) {
        animateIn(runnable, 200);
    }

    public void animateIn(Runnable runnable, int i) {
        animateIn(runnable, i, true);
    }

    public void animateIn(Runnable runnable, int i, boolean z) {
        Log.m5v("Camera", "V6TopControl animateIn getVisibility()=" + getVisibility());
        if (this.mAnimationInCallback != null) {
            this.mAnimationInCallback.run();
            this.mAnimationInCallback = null;
        }
        if (getVisibility() != 0 || !this.mControlVisible) {
            this.mAnimationInCallback = runnable;
            if (z) {
                setVisibility(0);
            }
            if (getVisibility() == 0) {
                clearAnimation();
                this.mAnimationIn.setDuration((long) i);
                startAnimation(this.mAnimationIn);
            } else if (this.mAnimationInCallback != null) {
                this.mAnimationInCallback.run();
                this.mAnimationInCallback = null;
            }
            this.mControlVisible = true;
        }
    }

    public void animateOut(Runnable runnable) {
        animateOut(runnable, 200);
    }

    public void animateOut(Runnable runnable, int i) {
        animateOut(runnable, i, true);
    }

    public void animateOut(final Runnable runnable, int i, boolean z) {
        Log.m5v("Camera", "V6TopControl animateOut getVisibility()=" + getVisibility());
        this.mControlVisible = false;
        if (this.mAnimationOutCallback != null) {
            this.mAnimationOutCallback.run();
            this.mAnimationOutCallback = null;
        }
        if (getVisibility() == 0) {
            if (z) {
                this.mAnimationOutCallback = new Runnable() {
                    public void run() {
                        if (runnable != null) {
                            runnable.run();
                        }
                        if (!BottomControlUpperPanel.this.mControlVisible) {
                            BottomControlUpperPanel.this.setVisibility(8);
                        }
                    }
                };
            } else {
                this.mAnimationOutCallback = runnable;
            }
            if (getVisibility() == 0) {
                clearAnimation();
                this.mAnimationOut.setDuration((long) i);
                startAnimation(this.mAnimationOut);
            } else if (this.mAnimationOutCallback != null) {
                this.mAnimationOutCallback.run();
                this.mAnimationOutCallback = null;
            }
        }
    }

    public EffectButton getEffectButton() {
        return this.mEffectButton;
    }

    public void onAnimationEnd(Animation animation) {
        if (this.mAnimationIn == animation) {
            if (this.mAnimationInCallback != null) {
                this.mAnimationInCallback.run();
                this.mAnimationInCallback = null;
            }
        } else if (this.mAnimationOut == animation && this.mAnimationOutCallback != null) {
            this.mAnimationOutCallback.run();
            this.mAnimationOutCallback = null;
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        if (((ActivityBase) this.mContext).isScanQRCodeIntent()) {
            setVisibility(4);
        } else if (((ActivityBase) this.mContext).getUIController().getReviewDoneView().getVisibility() == 0 || V6ModulePicker.isPanoramaModule()) {
            setVisibility(8);
        } else {
            setVisibility(0);
        }
        this.mControlVisible = getVisibility() == 0;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCameraPicker = (V6CameraPicker) findChildrenById(C0049R.id.v6_camera_picker);
        this.mSkinBeautyButton = (SkinBeautyButton) findChildrenById(C0049R.id.skin_beatify_button);
        this.mModeButton = (ModeButton) findChildrenById(C0049R.id.mode_button);
        this.mEffectButton = (EffectButton) findChildrenById(C0049R.id.effect_mode_button);
        this.mVideoCaptureButton = (V6VideoCaptureButton) findChildrenById(C0049R.id.v6_video_capture_button);
        this.mAnimationIn = new AlphaAnimation(0.0f, 1.0f);
        this.mAnimationIn.setDuration(200);
        this.mAnimationIn.setInterpolator(new DecelerateInterpolator());
        this.mAnimationIn.setAnimationListener(this);
        this.mAnimationOut = new AlphaAnimation(1.0f, 0.0f);
        this.mAnimationOut.setDuration(200);
        this.mAnimationOut.setInterpolator(new DecelerateInterpolator());
        this.mAnimationOut.setAnimationListener(this);
    }
}
