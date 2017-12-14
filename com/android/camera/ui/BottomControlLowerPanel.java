package com.android.camera.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.camera.Device;
import com.android.camera.Thumbnail;
import com.android.camera.Util;

public class BottomControlLowerPanel extends V6RelativeLayout implements AnimationListener {
    private static final int DURATION = (Device.IS_CM_TEST ? 200 : 300);
    private boolean mControlVisible;
    private Runnable mModuleAnimationCallback;
    public V6ModulePicker mModulePicker;
    private AnimationSet mModulePickerSwitchIn;
    public V6PauseRecordingButton mPauseRecordingButton;
    public View mProgressBar;
    public V6ShutterButton mShutterButton;
    private AnimationSet mShutterButtonSwitchIn;
    public V6ThumbnailButton mThumbnailButton;
    public V6VideoCaptureButton mVideoCaptureButton;

    public BottomControlLowerPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void clearLastAnimation() {
        if (!this.mShutterButtonSwitchIn.hasEnded()) {
            this.mShutterButton.clearAnimation();
            this.mModulePicker.clearAnimation();
            if (this.mModuleAnimationCallback != null) {
                this.mModuleAnimationCallback.run();
                this.mModuleAnimationCallback = null;
            }
        }
    }

    private TransitionDrawable initModulePickTransView(boolean z) {
        Drawable[] drawableArr = new Drawable[2];
        if (z) {
            drawableArr[0] = this.mContext.getResources().getDrawable(C0049R.drawable.ic_camera_shutter_button_small);
            drawableArr[1] = this.mContext.getResources().getDrawable(C0049R.drawable.video_module_picker_bg);
            this.mModulePicker.setContentDescription(getResources().getString(C0049R.string.accessibility_camera_module_picker));
        } else {
            drawableArr[0] = this.mContext.getResources().getDrawable(C0049R.drawable.ic_video_shutter_button_small);
            drawableArr[1] = this.mContext.getResources().getDrawable(C0049R.drawable.camera_module_picker_bg);
            this.mModulePicker.setContentDescription(getResources().getString(C0049R.string.accessibility_video_module_picker));
        }
        TransitionDrawable transitionDrawable = new TransitionDrawable(drawableArr);
        transitionDrawable.setCrossFadeEnabled(true);
        return transitionDrawable;
    }

    private void initModulePickerSwitchAnimation(float f, float f2, float f3, float f4) {
        this.mModulePickerSwitchIn = new AnimationSet(true);
        this.mModulePickerSwitchIn.setDuration((long) DURATION);
        this.mModulePickerSwitchIn.setInterpolator(new DecelerateInterpolator());
        this.mModulePickerSwitchIn.addAnimation(new ScaleAnimation(f2 / f3, 1.0f, f2 / f3, 1.0f, 1, 0.5f, 1, 0.5f));
        this.mModulePickerSwitchIn.addAnimation(new TranslateAnimation(0, -((((float) (Util.sWindowWidth / 2)) - f4) - (f / 2.0f)), 1, 0.0f, 1, 0.0f, 1, 0.0f));
    }

    private void initShutterButtonSwitchAnimation(float f, float f2, float f3, float f4) {
        this.mShutterButtonSwitchIn = new AnimationSet(true);
        this.mShutterButtonSwitchIn.setDuration((long) DURATION);
        this.mShutterButtonSwitchIn.setInterpolator(new DecelerateInterpolator());
        this.mShutterButtonSwitchIn.setAnimationListener(this);
        this.mShutterButtonSwitchIn.addAnimation(new ScaleAnimation(f3 / f2, 1.0f, f3 / f2, 1.0f, 1, 0.5f, 1, 0.5f));
        this.mShutterButtonSwitchIn.addAnimation(new TranslateAnimation(0, (((float) (Util.sWindowWidth / 2)) - f4) - (f / 2.0f), 1, 0.0f, 1, 0.0f, 1, 0.0f));
    }

    private TransitionDrawable initShutterTransView(boolean z) {
        Drawable[] drawableArr = new Drawable[2];
        if (z) {
            drawableArr[0] = this.mContext.getResources().getDrawable(C0049R.drawable.camera_module_picker_bg);
            drawableArr[1] = this.mContext.getResources().getDrawable(C0049R.drawable.video_shutter_button_start_bg);
        } else {
            drawableArr[0] = this.mContext.getResources().getDrawable(C0049R.drawable.video_module_picker_bg);
            drawableArr[1] = this.mContext.getResources().getDrawable(C0049R.drawable.camera_shutter_button_bg);
        }
        TransitionDrawable transitionDrawable = new TransitionDrawable(drawableArr);
        transitionDrawable.setCrossFadeEnabled(true);
        return transitionDrawable;
    }

    public void animateSwitch(Runnable runnable, boolean z) {
        setVisibility(0);
        clearLastAnimation();
        Drawable initModulePickTransView = initModulePickTransView(z);
        Drawable initShutterTransView = initShutterTransView(z);
        this.mModuleAnimationCallback = runnable;
        this.mModulePicker.setImageDrawable(initModulePickTransView);
        initModulePickTransView.startTransition(DURATION - 50);
        this.mShutterButton.setImageDrawable(initShutterTransView);
        initShutterTransView.startTransition(DURATION - 50);
        this.mShutterButton.startAnimation(this.mShutterButtonSwitchIn);
        this.mModulePicker.startAnimation(this.mModulePickerSwitchIn);
    }

    public void animationSwitchToCamera(Runnable runnable) {
        animateSwitch(runnable, false);
    }

    public void animationSwitchToVideo(Runnable runnable) {
        animateSwitch(runnable, true);
    }

    public V6ModulePicker getModulePicker() {
        return this.mModulePicker;
    }

    public View getProgressBar() {
        return this.mProgressBar;
    }

    public V6ShutterButton getShutterButton() {
        return this.mShutterButton;
    }

    public V6ThumbnailButton getThumbnailButton() {
        return this.mThumbnailButton;
    }

    public V6VideoCaptureButton getVideoCaptureButton() {
        return this.mVideoCaptureButton;
    }

    public V6PauseRecordingButton getVideoPauseButton() {
        return this.mPauseRecordingButton;
    }

    public void onAnimationEnd(Animation animation) {
        if (this.mShutterButtonSwitchIn == animation && this.mModuleAnimationCallback != null) {
            post(this.mModuleAnimationCallback);
            this.mModuleAnimationCallback = null;
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onCameraOpen() {
        boolean z = false;
        super.onCameraOpen();
        if (getVisibility() == 0) {
            z = true;
        }
        this.mControlVisible = z;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mThumbnailButton = (V6ThumbnailButton) findChildrenById(C0049R.id.v6_thumbnail_button);
        this.mShutterButton = (V6ShutterButton) findChildrenById(C0049R.id.v6_shutter_button);
        this.mModulePicker = (V6ModulePicker) findChildrenById(C0049R.id.v6_module_picker);
        this.mProgressBar = findViewById(C0049R.id.v6_progress_capture);
        this.mPauseRecordingButton = (V6PauseRecordingButton) findChildrenById(C0049R.id.v6_video_pause_button);
        this.mVideoCaptureButton = (V6VideoCaptureButton) findChildrenById(C0049R.id.v6_video_capture_button);
        float intrinsicWidth = (float) this.mThumbnailButton.mImage.getDrawable().getIntrinsicWidth();
        float intrinsicWidth2 = (float) this.mShutterButton.getDrawable().getIntrinsicWidth();
        float intrinsicWidth3 = (float) this.mModulePicker.getDrawable().getIntrinsicWidth();
        float dimensionPixelSize = (float) (this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.bottom_control_lower_panel_padding_width) + this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.normal_view_expanded_space));
        initShutterButtonSwitchAnimation(intrinsicWidth, intrinsicWidth2, intrinsicWidth3, dimensionPixelSize);
        initModulePickerSwitchAnimation(intrinsicWidth, intrinsicWidth2, intrinsicWidth3, dimensionPixelSize);
    }

    public void onResume() {
        super.onResume();
        setVisibility(0);
        this.mThumbnailButton.setVisibility(0);
        this.mShutterButton.setVisibility(0);
        this.mModulePicker.setVisibility(0);
        this.mPauseRecordingButton.setVisibility(8);
        this.mVideoCaptureButton.setVisibility(8);
    }

    public void updateThumbnailView(Thumbnail thumbnail) {
        this.mThumbnailButton.updateThumbnail(thumbnail);
    }
}
