package com.android.camera.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.android.camera.ActivityBase;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public class StereoButton extends ImageView implements V6FunctionUI, OnClickListener, MessageDispacher, OnOtherPopupShowedListener, AnimationListener {
    private ObjectAnimator mAnimator;
    private CustomAnimatorListener mAnimatorListener;
    private ExitButton mExitButton;
    private V6ModeExitView mExitView;
    private boolean mIsShowing;
    private MessageDispacher mMessageDispacher;
    private int mModeExitButtonCenterX;
    private int mModeExitButtonHalfWidth;
    private int mModeExitButtonPadding;
    private StereoPopup mPopup;
    private IconListPreference mPreference;
    private Animation mRotateImageAnim;
    private Animation mSlideDownAnim;
    private Animation mSlideUpAnim;
    private TransitionDrawable mTransitionDrawable = ((TransitionDrawable) getDrawable());

    class C01551 implements Runnable {
        C01551() {
        }

        public void run() {
            StereoButton.this.updateVisible();
            StereoButton.this.updateActivated();
            if (StereoButton.this.isActivated()) {
                StereoButton.this.mTransitionDrawable.startTransition(200);
            }
            StereoButton.this.animate().rotationBy(60.0f).start();
        }
    }

    class C01562 implements Runnable {
        C01562() {
        }

        public void run() {
            StereoButton.this.mExitView.setTranslationY(0.0f);
        }
    }

    private enum AnimationType {
        COLLAPSE,
        EXPAND
    }

    private class CustomAnimatorListener extends AnimatorListenerAdapter implements AnimatorUpdateListener {
        private AnimationType mAnimationType;

        public CustomAnimatorListener(StereoButton stereoButton) {
            this(AnimationType.COLLAPSE);
        }

        public CustomAnimatorListener(AnimationType animationType) {
            this.mAnimationType = animationType;
            updateParameters();
        }

        public void onAnimationEnd(Animator animator) {
            StereoButton.this.mExitButton.setExpandedAnimation(false);
            if (this.mAnimationType == AnimationType.COLLAPSE) {
                StereoButton.this.mExitView.setExitButtonVisible(8);
            } else {
                StereoButton.this.mIsShowing = false;
            }
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
        }

        public void setAnimationType(AnimationType animationType) {
            this.mAnimationType = animationType;
        }

        public void updateParameters() {
            int measureText = (int) StereoButton.this.mExitButton.getPaint().measureText(StereoButton.this.mExitButton.getText(), 0, StereoButton.this.mExitButton.getText().length());
            StereoButton.this.mModeExitButtonCenterX = Util.sWindowWidth / 2;
            StereoButton.this.mModeExitButtonHalfWidth = measureText / 2;
            StereoButton.this.mModeExitButtonPadding = StereoButton.this.mExitButton.getPaddingLeft();
        }
    }

    public StereoButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
        this.mRotateImageAnim = AnimationUtils.loadAnimation(context, C0049R.anim.rotate_image);
        this.mRotateImageAnim.setAnimationListener(this);
        this.mSlideUpAnim = AnimationUtils.loadAnimation(context, C0049R.anim.slide_up);
        this.mSlideUpAnim.setAnimationListener(this);
        this.mSlideDownAnim = AnimationUtils.loadAnimation(context, C0049R.anim.slide_down);
        this.mSlideDownAnim.setAnimationListener(this);
    }

    private void animateShow() {
        this.mIsShowing = true;
        if (isActivated()) {
            showPopup(true);
        } else {
            startAnimation(this.mRotateImageAnim);
        }
        PopupManager.getInstance(getContext()).notifyShowPopup(this, 1);
    }

    private void createExpandAnimator(boolean z) {
        if (this.mAnimatorListener == null) {
            this.mAnimatorListener = new CustomAnimatorListener(this);
        } else {
            this.mAnimatorListener.updateParameters();
        }
        if (this.mAnimator == null) {
            this.mAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(this.mContext, C0049R.anim.exit_button_expand);
            this.mAnimator.setTarget(this);
            this.mAnimator.addListener(this.mAnimatorListener);
        }
        this.mAnimator.setIntValues(new int[]{0, this.mModeExitButtonHalfWidth});
        if (z) {
            this.mAnimatorListener.setAnimationType(AnimationType.EXPAND);
            this.mAnimator.start();
        } else {
            this.mAnimatorListener.setAnimationType(AnimationType.COLLAPSE);
            this.mAnimator.reverse();
        }
        this.mExitButton.setExpandedAnimation(true);
    }

    private void doWithPopup(boolean z, boolean z2) {
        if (!z2) {
            startAnimation(this.mSlideDownAnim);
        } else if (z) {
            startAnimation(this.mSlideUpAnim);
        } else {
            setVisibility(8);
            showScale(false);
        }
    }

    private String getKey() {
        return this.mPreference != null ? this.mPreference.getKey() : null;
    }

    private void initializePopup() {
        if (this.mPreference == null || !this.mPreference.hasPopup()) {
            Log.i("StereoButton", "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup popupParent = ((ActivityBase) this.mContext).getUIController().getPopupParent();
            this.mPopup = (StereoPopup) SettingPopupFactory.createSettingPopup(this.mPreference.getKey(), popupParent, getContext());
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            popupParent.addView(this.mPopup);
        }
    }

    private void reloadPreference() {
        ((ActivityBase) this.mContext).getUIController().getSettingsStatusBar().updateAperture();
        if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        }
    }

    private void updateActivated() {
        if (!CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") || isPopupVisible()) {
            setActivated(false);
            return;
        }
        setActivated(true);
        setImageDrawable(this.mTransitionDrawable);
    }

    private void updateExitButton(boolean z) {
        int exitText = CameraSettings.getExitText(this.mPreference.getKey());
        if (exitText == -1) {
            return;
        }
        if (CameraSettings.isSwitchOn(this.mPreference.getKey())) {
            if (z) {
                this.mExitView.setExitContent(exitText);
            } else {
                this.mExitView.updateExitButton(exitText, true);
            }
            this.mExitView.setExitButtonClickListener(this, this.mPreference.getKey());
        } else if (this.mExitView.isCurrentExitView(this.mPreference.getKey())) {
            if (!z) {
                this.mExitView.updateExitButton(exitText, false);
            }
            this.mExitView.setExitButtonClickListener(null, null);
        }
    }

    public boolean dismissPopup(boolean z) {
        this.mIsShowing = false;
        if (!isPopupVisible()) {
            return false;
        }
        this.mPopup.dismiss(z);
        dismissScale(z);
        if (!z) {
            updateVisible();
        }
        return true;
    }

    public void dismissScale(boolean z) {
        if (this.mExitView.isExitButtonShown()) {
            if (z) {
                this.mExitView.animate().setDuration(200).translationYBy((float) getContext().getResources().getDimensionPixelSize(C0049R.dimen.manual_popup_layout_height)).withEndAction(new C01562()).start();
                createExpandAnimator(false);
            } else {
                this.mExitView.setExitButtonVisible(8);
            }
        }
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        switch (i) {
            case 11:
                updateActivated();
                break;
            case 12:
                if (!CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                    updateActivated();
                }
                post(new C01551());
                break;
            default:
                if (this.mMessageDispacher != null) {
                    this.mMessageDispacher.dispacherMessage(i, C0049R.id.stereo_switch_image, 2, obj, obj2);
                }
                reloadPreference();
                break;
        }
        return false;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public View getPopup() {
        return this.mPopup;
    }

    public boolean isPopupVisible() {
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    public void onAnimationEnd(Animation animation) {
        if (this.mRotateImageAnim == animation && this.mIsShowing) {
            showPopup(true);
        } else if (this.mSlideUpAnim == animation && this.mIsShowing) {
            setVisibility(8);
            showScale(true);
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onCameraOpen() {
        if (!Device.isSupportedStereo() || CameraSettingPreferences.instance().isFrontCamera()) {
            setVisibility(8);
            return;
        }
        if (this.mPreference == null) {
            this.mPreference = (IconListPreference) ((ActivityBase) this.mContext).getUIController().getPreferenceGroup().findPreference("pref_camera_stereo_mode_key");
        }
        if (this.mPreference == null) {
            setVisibility(8);
            return;
        }
        PopupManager.getInstance(this.mContext).setOnOtherPopupShowedListener(this);
        if (!this.mIsShowing) {
            if (!CameraSettings.isSwitchOn(this.mPreference.getKey())) {
                updateVisible();
                dismissPopup(false);
            } else if (!isPopupVisible()) {
                setVisibility(8);
                showPopup(false);
            }
            updateExitButton(false);
        }
        if (this.mPopup != null) {
            this.mPopup.updateBackground();
        }
    }

    public void onClick(View view) {
        if (view == this && CameraSettings.isSwitchOn(this.mPreference.getKey())) {
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().simplifyPopup(false, false);
            animateShow();
            return;
        }
        setStereoValue(!CameraSettings.isSwitchOn(this.mPreference.getKey()), true, true);
    }

    public void onCreate() {
        this.mExitView = ((ActivityBase) this.mContext).getUIController().getModeExitView();
        if (this.mExitView != null) {
            this.mExitButton = this.mExitView.getExitButton();
        }
    }

    public boolean onOtherPopupShowed(int i) {
        boolean dismissPopup = dismissPopup(false);
        updateActivated();
        return dismissPopup;
    }

    public void onPause() {
        this.mIsShowing = false;
    }

    public void onResume() {
    }

    public void recoverIfNeeded() {
    }

    public void setActivated(boolean z) {
        super.setActivated(z);
        if (!z) {
            this.mTransitionDrawable.resetTransition();
        }
    }

    public void setDeltaX(int i) {
        this.mExitButton.setExpandingSize((this.mModeExitButtonCenterX - this.mModeExitButtonPadding) - i, (this.mModeExitButtonCenterX + this.mModeExitButtonPadding) + i);
        this.mExitButton.postInvalidateOnAnimation();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mPopup != null) {
            this.mPopup.setEnabled(z);
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setStereoValue(boolean z, boolean z2, boolean z3) {
        if (CameraSettings.isSwitchOn(this.mPreference.getKey()) != z) {
            if (z) {
                CameraDataAnalytics.instance().trackEvent(this.mPreference.getKey());
                this.mPreference.setValue(this.mContext.getString(C0049R.string.pref_camera_setting_switch_entryvalue_on));
            } else {
                this.mPreference.setValue(this.mPreference.findSupportedDefaultValue());
            }
            if (!z) {
                this.mRotateImageAnim.cancel();
                PopupManager.getInstance(getContext()).clearRecoveredPopupListenerIfNeeded(this);
                dismissPopup(z3);
            } else if (z3) {
                animateShow();
            } else {
                showPopup(z3);
            }
            updateExitButton(z3);
            if (z2 && this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(0, C0049R.id.stereo_switch_image, 2, null, null);
            }
        }
    }

    public void showPopup(boolean z) {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.show(z);
            doWithPopup(z, true);
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().onPopupChange();
        }
    }

    public void showScale(boolean z) {
        if (!this.mExitView.isExitButtonShown()) {
            if (z) {
                createExpandAnimator(true);
            }
            this.mExitView.setExitButtonVisible(0);
        }
    }

    public void switchOffStereo(boolean z) {
        setStereoValue(false, z, false);
    }

    public void updateVisible() {
        if (Device.isSupportedStereo() && CameraSettingPreferences.instance().isBackCamera() && CameraSettings.isNoCameraModeSelected(this.mContext) && !((ActivityBase) this.mContext).getCurrentModule().isCaptureIntent() && !CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            setActivated(false);
            setVisibility(0);
        } else if (!CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") || isPopupVisible()) {
            setActivated(false);
            setVisibility(8);
        } else {
            setVisibility(0);
        }
    }
}
