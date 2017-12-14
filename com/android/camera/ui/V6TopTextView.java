package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Log;
import com.android.camera.Util;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public class V6TopTextView extends TextView implements MessageDispacher, OnOtherPopupShowedListener, V6FunctionUI, AnimateView {
    protected MessageDispacher mMessageDispacher;
    protected String mOverrideValue;
    protected V6AbstractSettingPopup mPopup;
    protected IconListPreference mPreference;

    public V6TopTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void doTapButton() {
        if (!isOverridden()) {
            if (this.mPreference == null || !this.mPreference.hasPopup() || this.mPreference.getEntryValues().length < 3) {
                toggle();
            } else if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
                setPressed(true);
                showPopup();
                ((ActivityBase) this.mContext).getUIController().getPreviewPage().simplifyPopup(false, false);
                PopupManager.getInstance(getContext()).notifyShowPopup(this, 1);
            } else {
                dismissPopup();
            }
        }
    }

    private String getKey() {
        return this.mPreference == null ? "" : this.mPreference.getKey();
    }

    private int getPreferenceSize() {
        CharSequence[] entryValues = this.mPreference.getEntryValues();
        return entryValues != null ? entryValues.length : 0;
    }

    private Animation initAnimation(boolean z) {
        if (z) {
            return AnimationUtils.loadAnimation(this.mContext, C0049R.anim.show);
        }
        Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.dismiss);
        loadAnimation.setAnimationListener(new SimpleAnimationListener(this, false));
        return loadAnimation;
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
        notifyClickToDispatcher();
    }

    public boolean dismissPopup() {
        boolean z = false;
        if (this.mPopup != null && this.mPopup.getVisibility() == 0) {
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().dismissPopup(this.mPopup);
            notifyPopupVisibleChange(false);
            z = true;
            PopupManager.getInstance(getContext()).notifyDismissPopup();
        }
        setPressed(false);
        return z;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        dismissPopup();
        if (!((obj2 instanceof Boolean) && ((Boolean) obj2).booleanValue())) {
            notifyClickToDispatcher();
        }
        return true;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
    }

    public void hide(boolean z) {
        if (z) {
            clearAnimation();
            startAnimation(initAnimation(false));
            return;
        }
        setVisibility(8);
    }

    protected void initializePopup() {
        if (this.mPreference == null || !this.mPreference.hasPopup()) {
            Log.m4i("V6TopTextView", "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup topPopupParent = ((ActivityBase) this.mContext).getUIController().getTopPopupParent();
            this.mPopup = SettingPopupFactory.createSettingPopup(getKey(), topPopupParent, getContext());
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            topPopupParent.addView(this.mPopup);
        }
    }

    public boolean isOverridden() {
        return this.mOverrideValue != null;
    }

    protected void notifyClickToDispatcher() {
    }

    protected void notifyPopupVisibleChange(boolean z) {
    }

    public void onCameraOpen() {
    }

    public void onCreate() {
    }

    public boolean onOtherPopupShowed(int i) {
        dismissPopup();
        return false;
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0 && !isOverridden()) {
            setPressed(true);
            return true;
        } else if (action == 3) {
            dismissPopup();
            return true;
        } else if (action != 1) {
            return true;
        } else {
            if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
                CameraDataAnalytics.instance().trackEvent(getKey());
            }
            if (Util.pointInView(motionEvent.getRawX(), motionEvent.getRawY(), this)) {
                doTapButton();
                if (this.mPopup == null) {
                    setPressed(false);
                }
                playSoundEffect(0);
                AutoLockManager.getInstance(this.mContext).onUserInteraction();
            }
            return true;
        }
    }

    public void recoverIfNeeded() {
        showPopup();
    }

    public void reloadPreference() {
        updateTitle();
        if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void show(boolean z) {
        setVisibility(0);
        if (z) {
            clearAnimation();
            startAnimation(initAnimation(true));
        }
    }

    public void showPopup() {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.setOrientation(0, false);
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().showPopup(this.mPopup);
            notifyPopupVisibleChange(true);
        }
    }

    protected void updateTitle() {
        StringBuilder stringBuilder = new StringBuilder(this.mPreference.getTitle());
        stringBuilder.append("  ");
        stringBuilder.append(this.mPreference.getEntry());
        setText(stringBuilder.toString());
    }
}
