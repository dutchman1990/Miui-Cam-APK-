package com.android.camera.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.CameraManager;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.aosp_porting.animation.BackEaseOutInterpolator;
import com.android.camera.aosp_porting.animation.CubicEaseInOutInterpolator;
import com.android.camera.aosp_porting.animation.CubicEaseOutInterpolator;
import com.android.camera.aosp_porting.animation.QuadraticEaseInOutInterpolator;
import com.android.camera.aosp_porting.animation.QuadraticEaseOutInterpolator;
import com.android.camera.aosp_porting.animation.SineEaseOutInterpolator;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public class ZoomButton extends TextView implements V6FunctionUI, MessageDispacher, OnOtherPopupShowedListener, OnClickListener, OnLongClickListener, MutexView {
    private Interpolator mBackEaseOutInterpolator = new BackEaseOutInterpolator();
    private int mBottomMargin;
    private AnimatorSet mButtonSlideDownAnimator;
    private ObjectAnimator mButtonSlideUpAnimator;
    private Interpolator mCubicEaseInOutInterpolator = new CubicEaseInOutInterpolator();
    private Interpolator mCubicEaseOutInterpolator = new CubicEaseOutInterpolator();
    private TextAppearanceSpan mDigitsTextStyle;
    private Handler mHandler = new C01762();
    private int mLayoutLocationStatus;
    private MessageDispacher mMessageDispacher;
    private ZoomPopup mPopup;
    private int mPopupHeight;
    private ObjectAnimator mPopupSlideDownAnimator;
    private ObjectAnimator mPopupSlideUpAnimator;
    private IconListPreference mPreference;
    private Interpolator mQuadraticEaseInOutInterpolator = new QuadraticEaseInOutInterpolator();
    private Interpolator mQuadraticEaseOutInterpolator = new QuadraticEaseOutInterpolator();
    private Animation mShowAnimation;
    private Interpolator mSineEaseOutInterpolator = new SineEaseOutInterpolator();
    private AnimatorListener mSlideDownAnimatorListener = new C01773();
    private float mTouchDownEventOriginX;
    private boolean mTouchDownEventPassed;
    private TextAppearanceSpan mXTextStyle;
    private AnimatorSet mZoomInAnimator;
    private AnimatorSet mZoomInOutAnimator;
    private AnimatorSet mZoomOutAnimator;
    private boolean mZoomPopupAdjusting;
    private OnTouchListener mZoomPopupTouchListener = new C01751();
    private int mZoomRatio;
    private int mZoomRatioTele;
    private int mZoomRatioWide;
    private ObjectAnimator mZoomRequestAnimator;

    class C01751 implements OnTouchListener {
        private boolean mAnimated = false;

        C01751() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == 2) {
                if (!this.mAnimated) {
                    ZoomButton.this.mZoomInAnimator.start();
                    this.mAnimated = true;
                }
            } else if ((motionEvent.getAction() == 1 || motionEvent.getAction() == 3) && this.mAnimated) {
                ZoomButton.this.mZoomOutAnimator.start();
                this.mAnimated = false;
            }
            ZoomButton.this.sendHideMessage();
            return false;
        }
    }

    class C01762 extends Handler {
        C01762() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    ZoomButton.this.dismissPopup();
                    return;
                default:
                    return;
            }
        }
    }

    class C01773 extends AnimatorListenerAdapter {
        private float mButtonOriginalTranslationY;
        private boolean mButtonSlideDownAnimatorRunning;
        private float mPopupOriginalTranslationY;
        private boolean mPopupSlideDownAnimatorRunning;

        C01773() {
        }

        public void onAnimationEnd(Animator animator) {
            if (animator == ZoomButton.this.mPopupSlideDownAnimator) {
                this.mPopupSlideDownAnimatorRunning = false;
            } else if (animator == ZoomButton.this.mButtonSlideDownAnimator) {
                this.mButtonSlideDownAnimatorRunning = false;
            }
            if (!this.mPopupSlideDownAnimatorRunning && !this.mButtonSlideDownAnimatorRunning) {
                ZoomButton.this.mPopup.setVisibility(8);
                ZoomButton.this.mPopup.setTranslationY(this.mPopupOriginalTranslationY);
                ZoomButton.this.setTranslationY(this.mButtonOriginalTranslationY);
            }
        }

        public void onAnimationStart(Animator animator) {
            if (animator == ZoomButton.this.mPopupSlideDownAnimator) {
                this.mPopupSlideDownAnimatorRunning = true;
                this.mPopupOriginalTranslationY = ZoomButton.this.mPopup.getTranslationY();
            } else if (animator == ZoomButton.this.mButtonSlideDownAnimator) {
                this.mButtonSlideDownAnimatorRunning = true;
                this.mButtonOriginalTranslationY = ZoomButton.this.getTranslationY();
            }
        }
    }

    public ZoomButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);
        GradientDrawable gradientDrawable = (GradientDrawable) ((LayerDrawable) ((InsetDrawable) getBackground()).getDrawable()).findDrawableByLayerId(C0049R.id.ic_zoom_button_background);
        this.mDigitsTextStyle = new TextAppearanceSpan(context, C0049R.style.ZoomButtonDigitsTextStyle);
        this.mXTextStyle = new TextAppearanceSpan(context, C0049R.style.ZoomButtonXTextStyle);
        this.mZoomRatioWide = Integer.valueOf(context.getResources().getString(C0049R.string.pref_camera_zoom_ratio_wide)).intValue();
        this.mZoomRatioTele = Integer.valueOf(context.getResources().getString(C0049R.string.pref_camera_zoom_ratio_tele)).intValue();
        this.mPopupHeight = context.getResources().getDimensionPixelSize(C0049R.dimen.zoom_popup_layout_height);
        this.mBottomMargin = context.getResources().getDimensionPixelSize(C0049R.dimen.zoom_button_margin_bottom);
        this.mShowAnimation = AnimationUtils.loadAnimation(context, C0049R.anim.show);
        this.mShowAnimation.setDuration(100);
        this.mZoomRequestAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_request);
        this.mZoomRequestAnimator.setTarget(this);
        this.mZoomInOutAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_button_zoom_in_out);
        this.mZoomInOutAnimator.setTarget(this);
        this.mZoomInOutAnimator.setInterpolator(this.mQuadraticEaseOutInterpolator);
        this.mZoomInAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_button_zoom_in);
        this.mZoomInAnimator.setTarget(this);
        this.mZoomInAnimator.setInterpolator(this.mQuadraticEaseInOutInterpolator);
        this.mZoomOutAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_button_zoom_out);
        this.mZoomOutAnimator.setTarget(this);
        this.mZoomOutAnimator.setInterpolator(this.mQuadraticEaseInOutInterpolator);
        this.mButtonSlideUpAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_button_slide_up);
        this.mButtonSlideUpAnimator.setTarget(this);
        this.mButtonSlideUpAnimator.setFloatValues(new float[]{(float) this.mPopupHeight, 0.0f});
        this.mButtonSlideUpAnimator.setInterpolator(this.mBackEaseOutInterpolator);
        this.mPopupSlideUpAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_popup_slide_up);
        this.mPopupSlideUpAnimator.setFloatValues(new float[]{(float) this.mPopupHeight, 0.0f});
        this.mPopupSlideUpAnimator.setInterpolator(this.mCubicEaseOutInterpolator);
        this.mButtonSlideDownAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_button_slide_down);
        this.mButtonSlideDownAnimator.setTarget(this);
        ObjectAnimator objectAnimator = (ObjectAnimator) this.mButtonSlideDownAnimator.getChildAnimations().get(0);
        objectAnimator.setFloatValues(new float[]{0.0f, ((float) this.mPopupHeight) + (((float) this.mPopupHeight) * 0.2f)});
        objectAnimator.setInterpolator(this.mCubicEaseInOutInterpolator);
        ObjectAnimator objectAnimator2 = (ObjectAnimator) this.mButtonSlideDownAnimator.getChildAnimations().get(1);
        objectAnimator2.setFloatValues(new float[]{((float) this.mPopupHeight) + (((float) this.mPopupHeight) * 0.2f), (float) this.mPopupHeight});
        objectAnimator2.setInterpolator(this.mSineEaseOutInterpolator);
        this.mButtonSlideDownAnimator.addListener(this.mSlideDownAnimatorListener);
        this.mPopupSlideDownAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, C0049R.anim.zoom_popup_slide_down);
        this.mPopupSlideDownAnimator.setFloatValues(new float[]{0.0f, (float) this.mPopupHeight});
        this.mPopupSlideDownAnimator.setInterpolator(this.mCubicEaseInOutInterpolator);
        this.mPopupSlideDownAnimator.addListener(this.mSlideDownAnimatorListener);
    }

    private String getKey() {
        return this.mPreference != null ? this.mPreference.getKey() : null;
    }

    private int getPreferenceSize() {
        CharSequence[] entryValues = this.mPreference.getEntryValues();
        return entryValues != null ? entryValues.length : 0;
    }

    private void initializePopup() {
        if (this.mPreference == null) {
            Log.i("ZoomButton", "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup popupParent = ((ActivityBase) this.mContext).getUIController().getPopupParent();
            this.mPopup = (ZoomPopup) SettingPopupFactory.createSettingPopup(getKey(), popupParent, getContext());
            this.mPopupSlideUpAnimator.setTarget(this.mPopup);
            this.mPopupSlideDownAnimator.setTarget(this.mPopup);
            this.mPopup.setOnTouchListener(this.mZoomPopupTouchListener);
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            popupParent.addView(this.mPopup);
        }
    }

    private boolean isPopupShown() {
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    private boolean isVisible() {
        return (!CameraSettings.isSupportedOpticalZoom() || V6ModulePicker.isVideoModule() || !((ActivityBase) this.mContext).getUIController().getReviewDoneView().isInVisibleForUser() || CameraSettingPreferences.instance().isFrontCamera() || CameraSettings.isSwitchOn("pref_camera_manual_mode_key")) ? false : !CameraSettings.isSwitchOn("pref_camera_portrait_mode_key");
    }

    private void requestZoomRatio(int i) {
        requestZoomRatio(i, false);
    }

    private void requestZoomRatio(int i, boolean z) {
        if (i != this.mZoomRatio && this.mMessageDispacher != null && isVisible()) {
            this.mMessageDispacher.dispacherMessage(7, C0049R.id.zoom_popup, 2, Boolean.valueOf(z), Integer.valueOf(i));
        }
    }

    private void sendHideMessage() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    private void toggle() {
        if (this.mPreference != null) {
            int findIndexOfValue = this.mPreference.findIndexOfValue(this.mPreference.getValue()) + 1;
            if (findIndexOfValue >= getPreferenceSize()) {
                findIndexOfValue = 0;
            }
            this.mPreference.setValueIndex(findIndexOfValue);
            reloadPreference();
        }
        requestSwitchCamera();
    }

    private void triggerPopup() {
        if (this.mPreference == null) {
            return;
        }
        if (isPopupShown()) {
            dismissPopup();
            return;
        }
        setPressed(true);
        showPopup();
        ((ActivityBase) this.mContext).getUIController().getPreviewPage().simplifyPopup(false, false);
        PopupManager.getInstance(getContext()).notifyShowPopup(this, 1);
        sendHideMessage();
    }

    public boolean dismissPopup() {
        return dismissPopup(true);
    }

    public boolean dismissPopup(boolean z) {
        setPressed(false);
        if (this.mPopup == null || this.mPopup.getVisibility() != 0 || this.mPopupSlideDownAnimator.isRunning() || this.mButtonSlideDownAnimator.isRunning()) {
            return false;
        }
        if (z && this.mLayoutLocationStatus == 0) {
            this.mPopupSlideDownAnimator.start();
            this.mButtonSlideDownAnimator.start();
        } else {
            this.mPopup.setVisibility(8);
        }
        PopupManager.getInstance(getContext()).notifyDismissPopup();
        return true;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        if (this.mMessageDispacher == null) {
            return false;
        }
        requestZoomRatio(((Integer) obj2).intValue());
        return true;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public void hide() {
        setVisibility(8);
    }

    public void initialize() {
        if (this.mPreference == null) {
            this.mPreference = (IconListPreference) ((ActivityBase) this.mContext).getUIController().getPreferenceGroup().findPreference("pref_camera_zoom_mode_key");
        }
    }

    public void onCameraOpen() {
        initialize();
        reloadPreference();
        updateVisible();
        if (this.mPopup != null) {
            this.mPopup.updateBackground();
        }
    }

    public void onClick(View view) {
        if (CameraSettings.isSwitchCameraZoomMode()) {
            toggle();
        } else if (this.mZoomRatio == this.mZoomRatioWide) {
            this.mZoomRequestAnimator.setIntValues(new int[]{this.mZoomRatio, this.mZoomRatioTele});
            this.mZoomRequestAnimator.start();
        } else if (this.mZoomRatio <= this.mZoomRatioTele) {
            this.mZoomRequestAnimator.setIntValues(new int[]{this.mZoomRatio, this.mZoomRatioWide});
            this.mZoomRequestAnimator.start();
        } else {
            requestZoomRatio(this.mZoomRatioTele, true);
            requestZoomRatio(this.mZoomRatioWide, false);
        }
        this.mZoomInOutAnimator.start();
        dismissPopup();
    }

    public void onCreate() {
        initialize();
        updateVisible();
        if (!CameraSettings.isNoCameraModeSelected(this.mContext)) {
            setVisibility(8);
        }
    }

    public boolean onLongClick(View view) {
        if (isPopupShown() || CameraSettings.isSwitchCameraZoomMode()) {
            return false;
        }
        triggerPopup();
        this.mZoomPopupAdjusting = true;
        this.mTouchDownEventPassed = false;
        return true;
    }

    public boolean onOtherPopupShowed(int i) {
        return dismissPopup(false);
    }

    public void onPause() {
        dismissPopup();
        this.mZoomRequestAnimator.cancel();
    }

    public void onResume() {
        reloadPreference();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        if (motionEvent.getAction() == 0) {
            this.mTouchDownEventOriginX = motionEvent.getX();
            this.mZoomPopupAdjusting = false;
        } else if (this.mZoomPopupAdjusting) {
            float x = motionEvent.getX();
            motionEvent.setLocation(((((float) Util.sWindowWidth) / 2.0f) + x) - this.mTouchDownEventOriginX, motionEvent.getY());
            if (motionEvent.getAction() == 3 || motionEvent.getAction() == 1) {
                this.mZoomPopupAdjusting = false;
                if (this.mPopup != null) {
                    this.mPopup.passTouchEvent(motionEvent);
                }
            } else if (motionEvent.getAction() == 2) {
                if (!this.mTouchDownEventPassed) {
                    this.mTouchDownEventOriginX = x;
                    motionEvent.setLocation(((float) Util.sWindowWidth) / 2.0f, motionEvent.getY());
                    motionEvent.setAction(0);
                    this.mPopup.passTouchEvent(motionEvent);
                    this.mTouchDownEventPassed = true;
                    motionEvent.setAction(2);
                }
                if (this.mPopup != null) {
                    this.mPopup.passTouchEvent(motionEvent);
                }
            }
            motionEvent.setLocation(x, motionEvent.getY());
        }
        return super.onTouchEvent(motionEvent);
    }

    public void recoverIfNeeded() {
    }

    public void reloadPreference() {
        if (this.mPreference != null && isVisible()) {
            if (!CameraSettings.isSwitchCameraZoomMode()) {
                int readZoom = CameraSettings.readZoom(CameraSettingPreferences.instance());
                Parameters stashParameters = CameraManager.instance().getStashParameters();
                if (stashParameters == null) {
                    this.mZoomRatio = this.mZoomRatioWide;
                } else {
                    this.mZoomRatio = ((Integer) stashParameters.getZoomRatios().get(readZoom)).intValue();
                }
            } else if (CameraSettings.getString(C0049R.string.pref_camera_zoom_mode_entryvalue_wide).equals(this.mPreference.getValue())) {
                this.mZoomRatio = this.mZoomRatioWide;
            } else {
                this.mZoomRatio = this.mZoomRatioTele;
            }
            if (this.mZoomRequestAnimator.isRunning() && this.mZoomRatio != this.mZoomRatioWide) {
                if (this.mZoomRatio == this.mZoomRatioTele) {
                }
            }
            Object spannableStringBuilder = new SpannableStringBuilder();
            int i = this.mZoomRatio / 10;
            int i2 = i / 10;
            int i3 = i % 10;
            if (i3 == 0) {
                spannableStringBuilder.append(String.valueOf(i2), this.mDigitsTextStyle, 33);
            } else {
                spannableStringBuilder.append(i2 + "." + i3, this.mDigitsTextStyle, 33);
            }
            spannableStringBuilder.append("X", this.mXTextStyle, 33);
            setText(spannableStringBuilder);
        }
    }

    public void requestSwitchCamera() {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(7, C0049R.id.zoom_button, 2, getKey(), null);
        }
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

    public void setZoomRatio(int i) {
        requestZoomRatio(i, false);
    }

    public void show() {
        if (isVisible()) {
            setVisibility(0);
        }
    }

    public void showPopup() {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.setOrientation(0, false);
            this.mPopup.setVisibility(0);
            if (this.mLayoutLocationStatus == 0) {
                this.mPopupSlideUpAnimator.start();
                this.mButtonSlideUpAnimator.start();
            }
        }
    }

    public void updateLayoutLocation() {
        View exitButton = ((ActivityBase) this.mContext).getUIController().getModeExitView().getExitButton();
        View popupIndicatorLayout = ((ActivityBase) this.mContext).getUIController().getPopupIndicatorLayout();
        int i = exitButton.getVisibility() == 0 ? ((ActivityBase) this.mContext).getUIController().getPopupParent().getVisibility() == 0 ? 1 : popupIndicatorLayout.getVisibility() == 0 ? 2 : 0 : 0;
        if (i != this.mLayoutLocationStatus) {
            LayoutParams layoutParams = (LayoutParams) getLayoutParams();
            layoutParams.removeRule(2);
            layoutParams.removeRule(14);
            layoutParams.removeRule(11);
            layoutParams.removeRule(8);
            if (i == 0) {
                layoutParams.addRule(14);
                layoutParams.addRule(2, C0049R.id.qrcode_viewfinder_layout);
                layoutParams.bottomMargin = this.mBottomMargin;
            } else if (i == 1) {
                layoutParams.addRule(11);
                layoutParams.addRule(8, C0049R.id.camera_mode_exit_view);
                layoutParams.bottomMargin = ((LinearLayout.LayoutParams) exitButton.getLayoutParams()).bottomMargin + ((exitButton.getHeight() - getHeight()) / 2);
            } else if (i == 2) {
                layoutParams.addRule(11);
                layoutParams.addRule(8, C0049R.id.popup_indicator_layout);
                layoutParams.bottomMargin = (popupIndicatorLayout.getHeight() - getHeight()) / 2;
            }
            if (isVisible() && (i == 0 || this.mLayoutLocationStatus == 0)) {
                startAnimation(this.mShowAnimation);
            }
            this.mLayoutLocationStatus = i;
            requestLayout();
        }
    }

    public void updateVisible() {
        if (isVisible()) {
            setVisibility(0);
            return;
        }
        setVisibility(8);
        this.mZoomRequestAnimator.cancel();
        dismissPopup();
    }
}
