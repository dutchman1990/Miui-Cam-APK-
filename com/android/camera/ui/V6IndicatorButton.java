package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraSettings;
import com.android.camera.Log;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;

public class V6IndicatorButton extends V6AbstractIndicator implements MessageDispacher, OnClickListener {
    private View mModeRemind;
    private String mOverrideValue;
    private boolean mSelected;

    public V6IndicatorButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
    }

    public V6IndicatorButton(Context context, IconListPreference iconListPreference) {
        super(context);
        this.mPreference = iconListPreference;
        reloadPreference();
    }

    private void notifyClickAction(boolean z) {
        if (z) {
            CameraSettings.cancelRemind(this.mPreference.getKey());
            if (!V6ModulePicker.isPanoramaModule() && !"pref_camera_panoramamode_key".equals(getKey())) {
                this.mMessageDispacher.dispacherMessage(9, 0, 3, getKey(), this);
            }
        }
    }

    private void notifyToModule() {
        Log.m5v("Camera5", "mMessageDispacher=" + this.mMessageDispacher);
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(6, 0, 3, getKey(), this);
        }
    }

    private void onIndicatorValueChange() {
        resetOtherSetting();
        refreshValue();
        updatePopup();
        notifyToModule();
    }

    private void refreshValue() {
        if (this.mSelected) {
            CameraDataAnalytics.instance().trackEvent(getKey());
            this.mPreference.setValue(this.mContext.getString(C0049R.string.pref_camera_setting_switch_entryvalue_on));
        } else {
            this.mPreference.setValue(this.mPreference.findSupportedDefaultValue());
        }
        if ("pref_camera_manual_mode_key".equals(getKey())) {
            CameraSettings.updateFocusMode();
        }
    }

    private void resetOtherSetting() {
        if (!V6ModulePicker.isPanoramaModule() && this.mSelected) {
            this.mMessageDispacher.dispacherMessage(8, 0, 3, getKey(), this);
        }
    }

    private void updateExitButton() {
        int exitText = CameraSettings.getExitText(this.mPreference.getKey());
        if (exitText == -1) {
            return;
        }
        if (this.mSelected) {
            this.mExitView.updateExitButton(exitText, true);
            this.mExitView.setExitButtonClickListener(this, this.mPreference.getKey());
        } else if (this.mExitView.isCurrentExitView(this.mPreference.getKey())) {
            this.mExitView.updateExitButton(exitText, false);
            this.mExitView.setExitButtonClickListener(null, null);
        }
    }

    private void updatePopup() {
        Log.m5v("Camera5", "updatePopup this=" + this.mPreference.getKey() + " value=" + this.mPreference.getValue() + " default=" + this.mPreference.findSupportedDefaultValue());
        if (!this.mPreference.hasPopup()) {
            return;
        }
        if (this.mSelected) {
            showPopup();
            PopupManager.getInstance(getContext()).notifyShowPopup(this, 1);
            return;
        }
        PopupManager.getInstance(getContext()).clearRecoveredPopupListenerIfNeeded(this);
        dismissPopup();
    }

    private void updateRemind() {
        if (CameraSettings.isNeedRemind(this.mPreference.getKey())) {
            this.mModeRemind.setVisibility(0);
        } else {
            this.mModeRemind.setVisibility(8);
        }
    }

    public boolean dismissPopup() {
        if (!isPopupVisible()) {
            return false;
        }
        this.mPopup.dismiss(false);
        return true;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        return (this.mMessageDispacher == null || i == 10) ? false : this.mMessageDispacher.dispacherMessage(i, i2, i3, obj, this);
    }

    public void initialize(IconListPreference iconListPreference, MessageDispacher messageDispacher, ViewGroup viewGroup, int i, int i2, PreferenceGroup preferenceGroup) {
        this.mSelected = !iconListPreference.isDefaultValue();
        super.initialize(iconListPreference, messageDispacher, viewGroup, i, i2, preferenceGroup);
        this.mImage.setOnClickListener(this);
        setClickable(false);
        updateExitButton();
        updatePopup();
        updateRemind();
    }

    protected void initializePopup() {
        if (this.mPopup == null && this.mPreference.hasPopup()) {
            this.mPopup = SettingPopupFactory.createSettingPopup(getKey(), this.mPopupRoot, getContext());
            this.mPopup.initialize(this.mPreferenceGroup, this.mPreference, this);
            this.mPopupRoot.addView(this.mPopup);
        }
    }

    protected boolean isIndicatorSelected() {
        return isPopupVisible();
    }

    public boolean isItemSelected() {
        return this.mSelected;
    }

    public boolean isPopupVisible() {
        String str = "Camera5";
        StringBuilder append = new StringBuilder().append("visible=");
        boolean z = this.mPopup != null && this.mPopup.getVisibility() == 0;
        Log.m5v(str, append.append(z).append(" this=").append(this.mPreference.getKey()).toString());
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    public void onClick(View view) {
        boolean z = false;
        if (this.mImage != view || isEnabled()) {
            this.mSelected = !this.mSelected;
            if (view != null) {
                z = view instanceof TwoStateImageView;
            }
            notifyClickAction(z);
            onIndicatorValueChange();
            if (this.mImage != null) {
                AutoLockManager.getInstance(this.mContext).onUserInteraction();
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mImage.setOnClickListener(null);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mModeRemind = findViewById(C0049R.id.mode_remind);
    }

    public boolean onOtherPopupShowed(int i) {
        return i == 1 ? dismissPopup() : false;
    }

    public void overrideSettings(String... strArr) {
        Object obj = null;
        this.mOverrideValue = null;
        for (int i = 0; i < strArr.length; i += 2) {
            String str = strArr[i];
            String str2 = strArr[i + 1];
            if (str.equals(getKey())) {
                obj = 1;
                this.mOverrideValue = str2;
                setEnabled(str2 == null);
                if (obj != null) {
                    reloadPreference();
                }
            }
        }
        if (obj != null) {
            reloadPreference();
        }
    }

    public void reloadPreference() {
        Log.m5v("Camera5", "indicatorbutton reloadPreference");
        updateImage();
        updateExitButton();
        updateRemind();
        if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        }
    }

    public void removePopup() {
        if (this.mPopup != null) {
            this.mPopupRoot.removeView(this.mPopup);
            this.mPopup.onDestroy();
        }
    }

    public void resetSettings() {
        this.mSelected = false;
        onIndicatorValueChange();
        if (this.mSelected) {
            this.mPreference.setValue(this.mPreference.findSupportedDefaultValue());
        }
        dismissPopup();
    }

    public void setOrientation(int i, boolean z) {
        super.setOrientation(i, z);
        if (this.mPopup != null) {
            this.mPopup.setOrientation(i, z);
        }
    }

    public void showPopup() {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.setOrientation(this.mOrientation, false);
            this.mPopup.show(false);
        }
    }
}
