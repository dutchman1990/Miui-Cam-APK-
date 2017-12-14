package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public class EffectButton extends AnimationImageView implements MessageDispacher, OnOtherPopupShowedListener {
    private static String TAG = "EffectButton";
    private boolean mDispatching = false;
    private String mOverrideValue;
    private EffectPopup mPopup;
    private IconListPreference mPreference;
    private String mSavedValue;
    private boolean mVisible = true;

    public EffectButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
    }

    private void doTapButton() {
        if (!isOverridden()) {
            if (this.mPreference == null || !this.mPreference.hasPopup() || this.mPreference.getEntryValues().length < 3) {
                toggle();
            } else {
                triggerPopup();
            }
        }
    }

    private int findCurrentIndex() {
        return this.mPreference.findIndexOfValue(getValue());
    }

    private String getKey() {
        return this.mPreference == null ? "" : this.mPreference.getKey();
    }

    private int getPreferenceSize() {
        CharSequence[] entryValues = this.mPreference.getEntryValues();
        return entryValues != null ? entryValues.length : 0;
    }

    private String getValue() {
        return this.mOverrideValue != null ? this.mOverrideValue : this.mPreference.getValue();
    }

    private boolean isPopupShown() {
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    private void notifyClickToDispatcher() {
        if (this.mMessageDispacher != null && this.mPreference != null) {
            this.mDispatching = true;
            this.mSavedValue = null;
            this.mMessageDispacher.dispacherMessage(6, C0049R.id.v6_setting_page, 2, getKey(), this);
            this.mDispatching = false;
            refreshIcon();
        }
    }

    private void refreshIcon() {
        if (this.mPreference != null) {
            setImageResource(findCurrentIndex() == 0 ? C0049R.drawable.ic_effect_button_normal : C0049R.drawable.ic_effect_button_highlight);
        }
    }

    private void toggle() {
        if (this.mPreference != null) {
            int findIndexOfValue = this.mPreference.findIndexOfValue(this.mPreference.getValue()) + 1;
            if (findIndexOfValue >= getPreferenceSize()) {
                findIndexOfValue = 0;
            }
            this.mPreference.setValueIndex(findIndexOfValue);
            reloadPreference();
            notifyClickToDispatcher();
        }
    }

    private void triggerPopup() {
        if (!isOverridden() && this.mPreference.hasPopup() && this.mPreference.getEntryValues().length >= 3) {
            if (isPopupShown() || this.mOverrideValue != null) {
                dismissPopup();
                return;
            }
            setPressed(true);
            showPopup();
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().simplifyPopup(false, false);
            PopupManager.getInstance(getContext()).notifyShowPopup(this, 1);
        }
    }

    public boolean dismissPopup() {
        setPressed(false);
        if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
            return false;
        }
        ((ActivityBase) this.mContext).getUIController().getPreviewPage().dismissPopup(this.mPopup);
        this.mPopup.stopEffectRender();
        PopupManager.getInstance(getContext()).notifyDismissPopup();
        return true;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        if (!((obj2 instanceof Boolean) && ((Boolean) obj2).booleanValue())) {
            notifyClickToDispatcher();
        }
        return true;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
        if (!z && isPressed()) {
            setPressed(false);
        }
        refreshIcon();
    }

    protected void initializePopup() {
        if (this.mPreference == null || !this.mPreference.hasPopup()) {
            Log.i(TAG, "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup popupParent = ((ActivityBase) this.mContext).getUIController().getPopupParent();
            this.mPopup = (EffectPopup) SettingPopupFactory.createSettingPopup(getKey(), popupParent, getContext());
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            popupParent.addView(this.mPopup);
        }
    }

    public void initializeXml() {
        if (this.mPreference == null && V6ModulePicker.isCameraModule() && Device.isSupportedShaderEffect()) {
            this.mPreference = (IconListPreference) ((ActivityBase) this.mContext).getUIController().getPreferenceGroup().findPreference("pref_camera_shader_coloreffect_key");
            this.mPreference.setEntries(EffectController.getInstance().getEntries());
            this.mPreference.setEntryValues(EffectController.getInstance().getEntryValues());
            this.mPreference.setIconIds(EffectController.getInstance().getImageIds());
        }
    }

    public boolean isOverridden() {
        return this.mOverrideValue != null;
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        if (this.mPopup != null) {
            this.mPopup.updateBackground();
        }
    }

    public void onCreate() {
        super.onCreate();
        if (V6ModulePicker.isCameraModule() && Device.isSupportedShaderEffect()) {
            this.mVisible = true;
            initializeXml();
            setVisibility(0);
            return;
        }
        this.mVisible = false;
        setVisibility(8);
    }

    public boolean onOtherPopupShowed(int i) {
        dismissPopup();
        return false;
    }

    public void onPause() {
        dismissPopup();
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
        } else if (!isPressed() || action != 1) {
            return true;
        } else {
            if (Util.pointInView(motionEvent.getRawX(), motionEvent.getRawY(), this)) {
                if (!isPopupShown()) {
                    CameraDataAnalytics.instance().trackEvent(getKey());
                }
                doTapButton();
                playSoundEffect(0);
                AutoLockManager.getInstance(this.mContext).onUserInteraction();
            }
            if (!isPopupShown()) {
                setPressed(false);
            }
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
            return true;
        }
    }

    public void recoverIfNeeded() {
    }

    public void refreshValue() {
        if (this.mPreference != null) {
            if (isPopupShown()) {
                this.mPopup.reloadPreference();
            }
            refreshIcon();
        }
    }

    public void reloadPreference() {
        if (this.mPreference != null) {
            refreshValue();
        }
    }

    public void requestEffectRender() {
        if (isPopupShown()) {
            this.mPopup.requestEffectRender();
        }
    }

    public void resetSettings() {
        this.mSavedValue = getValue();
        this.mPreference.setValueIndex(0);
        dismissPopup();
        refreshValue();
    }

    public void restoreSettings() {
        if (this.mSavedValue != null) {
            this.mPreference.setValue(this.mSavedValue);
            dismissPopup();
            refreshValue();
        }
    }

    public void setEnabled(boolean z) {
        if (isOverridden()) {
            z = false;
        }
        if ((isEnabled() ^ z) != 0) {
            super.setEnabled(z);
        }
        if (this.mPopup != null) {
            this.mPopup.setEnabled(z);
        }
    }

    public void setVisibility(int i) {
        if (!this.mVisible) {
            i = 8;
        }
        super.setVisibility(i);
    }

    public void showPopup() {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.setOrientation(0, false);
            this.mPopup.startEffectRender();
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().showPopup(this.mPopup);
        }
    }
}
