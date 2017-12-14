package com.android.camera.ui;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.Parameters;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraManager;
import com.android.camera.Device;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceInflater;
import java.util.List;

public class FlashButton extends AnimationImageView implements MessageDispacher, OnClickListener {
    private static String TAG = "FlashButton";
    private boolean mCameraOpened;
    private boolean mDispatching = false;
    private boolean mIsVideo;
    private String mOverrideValue;
    private V6AbstractSettingPopup mPopup;
    private IconListPreference mPreference;
    private boolean mVisible = true;

    public FlashButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    private void doTapButton() {
        if (!isOverridden()) {
            if (!this.mPreference.hasPopup() || this.mPreference.getEntryValues().length < 3) {
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

    public static String getRestoredFlashMode() {
        return CameraSettingPreferences.instance().getString("pref_camera_restored_flashmode_key", null);
    }

    private boolean isPopupShown() {
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    private void notifyClickToDispatcher() {
        if (this.mMessageDispacher != null && this.mPreference != null) {
            this.mDispatching = true;
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_flash_mode_button, 2, null, null);
            this.mDispatching = false;
            reloadPreference();
        }
    }

    private void notifyPopupVisibleChange(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(4, C0049R.id.v6_flash_mode_button, 3, Boolean.valueOf(z), null);
        }
    }

    private void setRestoredFlashMode(String str) {
        Editor edit = CameraSettingPreferences.instance().edit();
        if (str == null) {
            edit.remove("pref_camera_restored_flashmode_key");
        } else {
            String str2 = "pref_camera_restored_flashmode_key";
            if ("torch".equals(str)) {
                str = this.mIsVideo ? "off" : "auto";
            }
            edit.putString(str2, str);
        }
        edit.apply();
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
            if (isPopupShown()) {
                dismissPopup();
            } else {
                showPopup();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean avoidTorchOpen() {
        /*
        r4 = this;
        r0 = "torch";
        r1 = r4.mPreference;
        r1 = r1.getValue();
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x0069;
    L_0x000f:
        r0 = r4.mIsVideo;
        if (r0 != 0) goto L_0x0038;
    L_0x0013:
        r0 = r4.mContext;
        r0 = com.android.camera.CameraSettings.isNoCameraModeSelected(r0);
        if (r0 == 0) goto L_0x0060;
    L_0x001b:
        r1 = "live";
        r0 = r4.mCameraOpened;
        if (r0 == 0) goto L_0x0045;
    L_0x0022:
        r0 = r4.mContext;
        r0 = (com.android.camera.ActivityBase) r0;
        r0 = r0.getUIController();
        r0 = r0.getHdrButton();
        r0 = r0.getValue();
    L_0x0032:
        r0 = r1.equals(r0);
        if (r0 == 0) goto L_0x0060;
    L_0x0038:
        r0 = r4.mPreference;
        r1 = "off";
        r0.setValue(r1);
    L_0x0040:
        r4.refreshValue();
        r0 = 1;
        return r0;
    L_0x0045:
        r0 = r4.mPreference;
        r2 = r0.getSharedPreferences();
        r3 = "pref_camera_hdr_key";
        r0 = com.android.camera.Device.isSupportedAsdHdr();
        if (r0 == 0) goto L_0x005c;
    L_0x0054:
        r0 = "auto";
    L_0x0057:
        r0 = r2.getString(r3, r0);
        goto L_0x0032;
    L_0x005c:
        r0 = "off";
        goto L_0x0057;
    L_0x0060:
        r0 = r4.mPreference;
        r1 = "auto";
        r0.setValue(r1);
        goto L_0x0040;
    L_0x0069:
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.ui.FlashButton.avoidTorchOpen():boolean");
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
        setEnabled(z);
    }

    public String getValue() {
        return this.mOverrideValue != null ? this.mOverrideValue : this.mPreference.getValue();
    }

    protected void initializePopup() {
        if (this.mPreference == null || !this.mPreference.hasPopup()) {
            Log.i(TAG, "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup topPopupParent = ((ActivityBase) this.mContext).getUIController().getTopPopupParent();
            this.mPopup = SettingPopupFactory.createSettingPopup(getKey(), topPopupParent, getContext());
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            topPopupParent.addView(this.mPopup);
        }
    }

    public void initializeXml(boolean z) {
        this.mIsVideo = z;
        this.mPreference = (IconListPreference) new PreferenceInflater(this.mContext).inflate(z ? C0049R.xml.v6_video_flashmode_preferences : C0049R.xml.v6_camera_flashmode_preferences);
        if (!CameraSettingPreferences.instance().isFrontCamera() || Device.isSupportFrontFlash()) {
            this.mVisible = true;
            setVisibility(0);
            if (!avoidTorchOpen()) {
                refreshValue();
            }
            return;
        }
        this.mVisible = false;
        setVisibility(8);
    }

    public boolean isFlashPressed() {
        return this.mDispatching;
    }

    public boolean isOverridden() {
        return this.mOverrideValue != null;
    }

    public void keepSetValue(String str) {
        if (!getValue().equals(str)) {
            String restoredFlashMode = getRestoredFlashMode();
            if (restoredFlashMode == null) {
                setRestoredFlashMode(this.mPreference.getValue());
                setValue(str);
            } else if (restoredFlashMode.equals(str)) {
                restoreKeptValue();
            } else {
                setValue(str);
            }
        }
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        this.mCameraOpened = true;
        boolean isFrontCamera = CameraSettingPreferences.instance().isFrontCamera();
        if (!isFrontCamera || Device.isSupportFrontFlash()) {
            Parameters stashParameters = CameraManager.instance().getStashParameters();
            List normalFlashModes = stashParameters == null ? null : CameraHardwareProxy.getDeviceProxy().getNormalFlashModes(stashParameters);
            if (!(this.mIsVideo || normalFlashModes == null)) {
                if (Device.isSupportFrontFlash()) {
                    this.mPreference.setEntries(this.mContext.getResources().getTextArray(C0049R.array.pref_camera_flashmode_entries));
                    this.mPreference.setEntryValues((int) C0049R.array.pref_camera_flashmode_entryvalues);
                    this.mPreference.setIconRes(C0049R.array.camera_flashmode_icons);
                    if (isFrontCamera) {
                        normalFlashModes.remove("on");
                    } else if (!Device.isSupportedTorchCapture()) {
                        normalFlashModes.remove("torch");
                    }
                } else if (!Device.isSupportedTorchCapture()) {
                    normalFlashModes.remove("torch");
                }
            }
            if (normalFlashModes == null || normalFlashModes.size() <= 1) {
                this.mVisible = false;
                setVisibility(8);
                return;
            }
            this.mPreference.filterUnsupported(normalFlashModes);
            if (this.mPreference.getEntries().length <= 1) {
                this.mVisible = false;
                setVisibility(8);
                return;
            }
            this.mVisible = true;
            setVisibility(0);
            if (this.mPreference.findIndexOfValue(this.mPreference.getValue()) < 0) {
                this.mPreference.setValueIndex(0);
            }
            refreshValue();
            if (this.mPopup != null) {
                this.mPopup.updateBackground();
                if (Device.isSupportFrontFlash()) {
                    this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
                }
                if (this.mPopup.getVisibility() == 0) {
                    this.mPopup.dismiss(false);
                }
            }
            return;
        }
        this.mVisible = false;
        setVisibility(8);
    }

    public void onClick(View view) {
        if (!isPopupShown()) {
            CameraDataAnalytics.instance().trackEvent(getKey());
        }
        doTapButton();
        AutoLockManager.getInstance(this.mContext).onUserInteraction();
    }

    public void onCreate() {
        this.mOverrideValue = null;
        initializeXml(V6ModulePicker.isVideoModule());
    }

    public void onResume() {
        super.onResume();
        if (Device.isPad()) {
            setVisibility(8);
        }
        avoidTorchOpen();
    }

    public void overrideSettings(String str) {
        this.mOverrideValue = str;
        dismissPopup();
        refreshValue();
        setEnabled(str == null);
    }

    public void overrideValue(String str) {
        this.mOverrideValue = str;
    }

    public void refreshValue() {
        if (this.mPreference != null) {
            setImageResource(this.mPreference.getIconIds()[findCurrentIndex()]);
            setContentDescription(getResources().getString(C0049R.string.accessibility_flash_mode_button) + this.mPreference.getEntry());
            if (isPopupShown()) {
                this.mPopup.reloadPreference();
            }
        }
    }

    public void reloadPreference() {
        if (this.mPreference != null) {
            refreshValue();
        }
    }

    public void restoreKeptValue() {
        if (isFlashPressed()) {
            setRestoredFlashMode(null);
            return;
        }
        String restoredFlashMode = getRestoredFlashMode();
        if (restoredFlashMode != null) {
            setValue(restoredFlashMode);
            setRestoredFlashMode(null);
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

    public void setOrientation(int i, boolean z) {
    }

    public void setValue(String str) {
        this.mPreference.setValue(str);
        refreshValue();
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
            ((ActivityBase) this.mContext).getUIController().getTopPopupParent().showPopup(this.mPopup, true);
        }
    }

    public void updateFlashModeAccordingHdr(String str) {
        if (isFlashPressed()) {
            setRestoredFlashMode(null);
            return;
        }
        String restoredFlashMode = getRestoredFlashMode();
        Object value = restoredFlashMode != null ? restoredFlashMode : getValue();
        if ("auto".equals(str)) {
            if (!"off".equals(value)) {
                keepSetValue("auto");
            }
        } else if ("normal".equals(str)) {
            if (!"off".equals(value)) {
                keepSetValue("auto");
            }
        } else if (!"live".equals(str)) {
            restoreKeptValue();
        } else if (!"off".equals(value) && !"torch".equals(value)) {
            keepSetValue("auto");
        }
    }

    public void updatePopup(boolean z) {
        if (z != isPopupShown()) {
            if (z) {
                setVisibility(0);
            }
            triggerPopup();
        }
    }
}
