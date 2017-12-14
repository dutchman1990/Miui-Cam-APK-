package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Log;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceInflater;
import java.util.ArrayList;
import java.util.List;

public class HdrButton extends AnimationImageView implements MessageDispacher, OnClickListener {
    private boolean mIsVideo;
    private String mOverrideValue;
    private V6AbstractSettingPopup mPopup;
    private IconListPreference mPreference;

    public HdrButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    private void doTapButton() {
        if (!isOverridden()) {
            if (this.mPreference == null || !this.mPreference.hasPopup() || this.mPreference.getEntryValues().length < 3) {
                toggle();
            } else if (isPopupShown()) {
                dismissPopup();
            } else {
                showPopup();
            }
        }
    }

    private void filterPreference() {
        List arrayList = new ArrayList(4);
        for (CharSequence charSequence : this.mPreference.getEntryValues()) {
            arrayList.add(charSequence.toString());
        }
        if (!this.mIsVideo) {
            if (Device.IS_MI2 || !Device.isSupportedAoHDR()) {
                arrayList.remove("live");
                CharSequence[] entryValues = this.mPreference.getEntryValues();
                for (int i = 0; i < entryValues.length; i++) {
                    if ("normal".equals(entryValues[i])) {
                        this.mPreference.getEntries()[i] = getResources().getString(C0049R.string.pref_simple_hdr_entry_on);
                    }
                }
            }
            if (Device.IS_MI2A) {
                arrayList.remove("normal");
            }
            if (!Device.isSupportedAsdHdr()) {
                arrayList.remove("auto");
            }
        } else if (Device.IS_MI3TD || !Device.isSupportedAoHDR()) {
            arrayList.remove("on");
        }
        this.mPreference.filterUnsupported(arrayList);
        if (this.mPreference.findIndexOfValue(this.mPreference.getValue()) < 0) {
            this.mPreference.setValueIndex(0);
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

    private void initializePopup() {
        if (this.mPreference == null || !this.mPreference.hasPopup()) {
            Log.m4i("HdrButton", "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup topPopupParent = ((ActivityBase) this.mContext).getUIController().getTopPopupParent();
            this.mPopup = SettingPopupFactory.createSettingPopup(getKey(), topPopupParent, getContext());
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            topPopupParent.addView(this.mPopup);
        }
    }

    private boolean isPopupShown() {
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    private void notifyClickToDispatcher() {
        if (this.mMessageDispacher != null && this.mPreference != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_hdr, 2, null, null);
            reloadPreference();
        }
    }

    private void notifyPopupVisibleChange(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(4, C0049R.id.v6_hdr, 3, Boolean.valueOf(z), null);
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
        }
        notifyClickToDispatcher();
    }

    public boolean couldBeVisible() {
        return (CameraSettingPreferences.instance().isFrontCamera() || ((ActivityBase) this.mContext).getUIController().getSettingPage().isItemSelected() || this.mPreference == null) ? false : this.mPreference.getEntries().length > 1;
    }

    public boolean dismissPopup() {
        if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
            return false;
        }
        ((ActivityBase) this.mContext).getUIController().getTopPopupParent().dismissPopup(this.mPopup, true);
        return true;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        switch (i) {
            case 3:
                if (obj instanceof Boolean) {
                    notifyPopupVisibleChange(((Boolean) obj).booleanValue());
                    break;
                }
                break;
            case 6:
                dismissPopup();
                if (!((obj2 instanceof Boolean) && ((Boolean) obj2).booleanValue())) {
                    notifyClickToDispatcher();
                    break;
                }
        }
        return true;
    }

    public void enableControls(boolean z) {
        super.enableControls(z);
        if (this.mPopup != null) {
            this.mPopup.setEnabled(z);
        }
    }

    public String getValue() {
        return this.mOverrideValue != null ? this.mOverrideValue : this.mPreference.getValue();
    }

    public void initializeXml(boolean z) {
        this.mIsVideo = z;
        this.mPreference = (IconListPreference) new PreferenceInflater(this.mContext).inflate(z ? C0049R.xml.v6_video_hdr_preferences : C0049R.xml.v6_camera_hdr_preferences);
        filterPreference();
        if (CameraSettingPreferences.instance().isFrontCamera() || this.mPreference.getEntries().length <= 1) {
            setVisibility(8);
            return;
        }
        setVisibility(CameraSettings.isNoCameraModeSelected(this.mContext) ? 0 : 8);
        refreshValue();
    }

    public boolean isOverridden() {
        return this.mOverrideValue != null;
    }

    public void onCameraOpen() {
        int i = 8;
        clearAnimation();
        if (CameraSettingPreferences.instance().isFrontCamera() || this.mPreference.getEntries().length <= 1) {
            setVisibility(8);
            return;
        }
        if (V6ModulePicker.isVideoModule() == this.mIsVideo) {
            boolean isNoCameraModeSelected = CameraSettings.isNoCameraModeSelected(this.mContext);
            if (isNoCameraModeSelected) {
                i = 0;
            }
            setVisibility(i);
            overrideSettings(isNoCameraModeSelected ? null : "off");
        }
        if (this.mPreference.findIndexOfValue(this.mPreference.getValue()) < 0) {
            this.mPreference.setValueIndex(0);
        }
        refreshValue();
        if (this.mPopup != null) {
            this.mPopup.updateBackground();
        }
    }

    public void onClick(View view) {
        if (!isPopupShown()) {
            CameraDataAnalytics.instance().trackEvent(getKey());
        }
        doTapButton();
        AutoLockManager.getInstance(this.mContext).onUserInteraction();
    }

    public void onCreate() {
        initializeXml(V6ModulePicker.isVideoModule());
    }

    public void overrideSettings(String str) {
        this.mOverrideValue = str;
        reloadPreference();
    }

    public void refreshValue() {
        if (this.mPreference != null) {
            setImageResource(this.mPreference.getIconIds()[findCurrentIndex()]);
            setContentDescription(getResources().getString(C0049R.string.accessibility_hdr) + this.mPreference.getEntry());
            if (isPopupShown()) {
                this.mPopup.reloadPreference();
            }
        }
    }

    public void reloadPreference() {
        refreshValue();
        if (this.mPreference != null && isPopupShown()) {
            this.mPopup.reloadPreference();
        }
    }

    public void setOrientation(int i, boolean z) {
    }

    public void setValue(String str) {
        this.mPreference.setValue(str);
        reloadPreference();
    }

    public void showPopup() {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.setOrientation(0, false);
            ((ActivityBase) this.mContext).getUIController().getTopPopupParent().showPopup(this.mPopup, true);
        }
    }

    public void updateHdrAccordingFlash(String str) {
        String value = getValue();
        if ("auto".equals(str)) {
            if ("normal".equals(value) || "live".equals(value)) {
                setValue(Device.isSupportedAsdHdr() ? "auto" : "off");
                notifyClickToDispatcher();
            }
        } else if ("on".equals(str)) {
            if (!"off".equals(value)) {
                setValue("off");
                notifyClickToDispatcher();
            }
        } else if ("torch".equals(str) && !"live".equals(value) && !"off".equals(value)) {
            setValue("off");
            notifyClickToDispatcher();
        }
    }

    public void updateVisible() {
        boolean couldBeVisible = couldBeVisible();
        if (couldBeVisible != (getVisibility() == 0)) {
            overrideSettings(couldBeVisible ? null : "off");
            if (couldBeVisible && ((ActivityBase) this.mContext).getUIController().getFlashButton().isFlashPressed()) {
                updateHdrAccordingFlash(((ActivityBase) this.mContext).getUIController().getFlashButton().getValue());
            }
            notifyClickToDispatcher();
            setVisibility(couldBeVisible ? 0 : 8);
        }
    }
}
