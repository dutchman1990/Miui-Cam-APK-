package com.android.camera.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.aosp_porting.animation.CubicEaseOutInterpolator;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public class PortraitButton extends ImageView implements V6FunctionUI, OnClickListener, OnOtherPopupShowedListener, MutexView {
    private boolean mAnimatorInitialized;
    private ScaleDrawable mBackgroundDrawable;
    private ObjectAnimator mHintHideAnimator;
    private AnimatorListenerAdapter mHintHideAnimatorListener = new C01431();
    private ObjectAnimator mHintShowAnimator;
    private TextView mHintTextView;
    private MessageDispacher mMessageDispacher;
    private ScaleDrawable mPortraitDrawable;
    private IconListPreference mPreference;
    private AnimatorSet mSwitchOnAnimator;

    class C01431 extends AnimatorListenerAdapter {
        C01431() {
        }

        public void onAnimationEnd(Animator animator) {
            PortraitButton.this.mHintTextView.setVisibility(8);
        }
    }

    public PortraitButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
    }

    private void initialize() {
        if (this.mPreference == null) {
            this.mPreference = (IconListPreference) ((ActivityBase) this.mContext).getUIController().getPreferenceGroup().findPreference("pref_camera_portrait_mode_key");
        }
        if (!this.mAnimatorInitialized) {
            initializeAnimator();
            this.mAnimatorInitialized = true;
        }
    }

    private void initializeAnimator() {
        this.mHintTextView = ((ActivityBase) this.mContext).getUIController().getPortraitHintTextView();
        LayerDrawable layerDrawable = (LayerDrawable) getDrawable();
        this.mPortraitDrawable = (ScaleDrawable) layerDrawable.findDrawableByLayerId(C0049R.id.ic_portrait_button_foreground);
        this.mBackgroundDrawable = (ScaleDrawable) layerDrawable.findDrawableByLayerId(C0049R.id.ic_portrait_button_background);
        resetTransition();
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mPortraitDrawable, "level", new int[]{10000, 7800});
        ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this.mBackgroundDrawable, "alpha", new int[]{0, 255});
        ObjectAnimator ofInt3 = ObjectAnimator.ofInt(this.mBackgroundDrawable, "level", new int[]{8000, 10000});
        this.mSwitchOnAnimator = new AnimatorSet();
        this.mSwitchOnAnimator.playTogether(new Animator[]{ofInt, ofInt2, ofInt3});
        this.mSwitchOnAnimator.setInterpolator(new CubicEaseOutInterpolator());
        this.mSwitchOnAnimator.setDuration(350);
        this.mHintShowAnimator = ObjectAnimator.ofFloat(this.mHintTextView, "alpha", new float[]{0.0f, 1.0f});
        this.mHintShowAnimator.setInterpolator(new CubicEaseOutInterpolator());
        this.mHintShowAnimator.setDuration(100);
        this.mHintHideAnimator = ObjectAnimator.ofFloat(this.mHintTextView, "alpha", new float[]{1.0f, 0.0f});
        this.mHintHideAnimator.setInterpolator(new CubicEaseOutInterpolator());
        this.mHintHideAnimator.addListener(this.mHintHideAnimatorListener);
        this.mHintHideAnimator.setDuration(100);
    }

    private boolean isSettingsStatusBarShown() {
        return ((ActivityBase) this.mContext).getUIController().getSettingsStatusBar().isSubViewShown() ? ((ActivityBase) this.mContext).getUIController().getSettingsStatusBar().isShown() : false;
    }

    private boolean isVisible() {
        return (!CameraSettings.isSupportedPortrait() || !V6ModulePicker.isCameraModule() || CameraSettingPreferences.instance().isFrontCamera() || (((ActivityBase) this.mContext).getCurrentModule().isCaptureIntent() && !Util.isPortraitIntent((ActivityBase) this.mContext))) ? false : CameraSettings.isNoCameraModeSelected(this.mContext);
    }

    private void requestPortraitModeChange() {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.portrait_switch_image, 2, "pref_camera_portrait_mode_key", null);
        }
    }

    private void resetTransition() {
        this.mPortraitDrawable.setLevel(10000);
        this.mBackgroundDrawable.setAlpha(0);
        this.mBackgroundDrawable.setLevel(8000);
    }

    private void reverseTransition() {
        this.mSwitchOnAnimator.reverse();
    }

    private void startTransition() {
        this.mSwitchOnAnimator.start();
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public void hide() {
        setVisibility(8);
        this.mHintTextView.setVisibility(8);
    }

    public void hideHintText() {
        this.mHintHideAnimator.start();
    }

    public boolean isHintTextShown() {
        return this.mHintTextView.isShown();
    }

    public void onCameraOpen() {
        initialize();
        reloadPreference();
        updateVisible();
    }

    public void onClick(View view) {
        if (isActivated()) {
            setActivated(false);
            reverseTransition();
        } else {
            setActivated(true);
            startTransition();
        }
        updatePreference();
        requestPortraitModeChange();
    }

    public void onCreate() {
        initialize();
        updateVisible();
    }

    public boolean onOtherPopupShowed(int i) {
        return false;
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void recoverIfNeeded() {
    }

    public void reloadPreference() {
        if (this.mPreference != null) {
            if (this.mPreference.getValue().equals(this.mContext.getString(C0049R.string.pref_camera_setting_switch_entryvalue_on))) {
                if (!isActivated()) {
                    setActivated(true);
                    startTransition();
                }
            } else if (isActivated()) {
                setActivated(false);
                reverseTransition();
            }
        }
        updatePreference();
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void show() {
        if (isVisible() && !isSettingsStatusBarShown()) {
            setVisibility(0);
        }
    }

    public void showHintText() {
        this.mHintTextView.setVisibility(0);
        this.mHintShowAnimator.start();
    }

    public void switchOff() {
        switchOff(true);
    }

    public void switchOff(boolean z) {
        if (isActivated()) {
            setActivated(false);
            if (z) {
                reverseTransition();
            } else {
                resetTransition();
            }
            updatePreference();
            requestPortraitModeChange();
        }
    }

    public void updatePreference() {
        if (this.mPreference != null) {
            if (isActivated()) {
                this.mPreference.setValue(this.mContext.getString(C0049R.string.pref_camera_setting_switch_entryvalue_on));
            } else {
                this.mPreference.setValue(this.mContext.getString(C0049R.string.pref_camera_setting_switch_entryvalue_off));
            }
            setContentDescription(this.mPreference.getEntry());
        }
    }

    public void updateVisible() {
        if (!isVisible() || isSettingsStatusBarShown()) {
            setVisibility(8);
            this.mHintTextView.setVisibility(8);
            return;
        }
        setVisibility(0);
    }
}
