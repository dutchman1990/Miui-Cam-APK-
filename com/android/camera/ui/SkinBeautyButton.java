package com.android.camera.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.Device;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.preferences.PreferenceInflater;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public class SkinBeautyButton extends AnimationImageView implements MessageDispacher, OnOtherPopupShowedListener, OnClickListener {
    private Handler mHandler = new C01531();
    private V6AbstractSettingPopup mLastSubPopup;
    private V6AbstractSettingPopup mPopup;
    private IconListPreference mPreference;
    private MessageDispacher mSubDispacher = new C01542();
    private V6AbstractSettingPopup[] mSubPopups;
    private boolean mVisible = true;

    class C01531 extends Handler {
        C01531() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    SkinBeautyButton.this.dismissPopup();
                    return;
                default:
                    return;
            }
        }
    }

    class C01542 implements MessageDispacher {
        C01542() {
        }

        public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
            if (!((obj2 instanceof Boolean) && ((Boolean) obj2).booleanValue())) {
                if (i == 10) {
                    SkinBeautyButton.this.sendHideMessage();
                    return true;
                }
                SkinBeautyButton.this.notifyClickToDispatcher();
                SkinBeautyButton.this.sendHideMessage();
            }
            return true;
        }
    }

    public SkinBeautyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    private int findCurrentIndex() {
        return this.mPreference.findIndexOfValue(this.mPreference.getValue());
    }

    private V6AbstractSettingPopup findCurrentSubPopup() {
        return this.mSubPopups[findCurrentIndex()];
    }

    private String getKey() {
        return this.mPreference == null ? "" : this.mPreference.getKey();
    }

    private boolean isPopupShown() {
        return this.mPopup != null && this.mPopup.getVisibility() == 0;
    }

    private void notifyClickToDispatcher() {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.skin_beatify_button, 2, null, null);
        }
    }

    private void sendHideMessage() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    public boolean couldBeVisible() {
        return (CameraSettingPreferences.instance().isFrontCamera() && Device.isSupportedSkinBeautify()) ? V6ModulePicker.isCameraModule() : false;
    }

    public boolean dismissPopup() {
        this.mHandler.removeMessages(1);
        dismissSubPopup();
        if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
            return false;
        }
        ((ActivityBase) this.mContext).getUIController().getTopPopupParent().dismissPopup(this.mPopup, true);
        setActivated(false);
        PopupManager.getInstance(getContext()).notifyDismissPopup();
        return true;
    }

    protected boolean dismissSubPopup() {
        boolean z = false;
        if (this.mLastSubPopup != null && this.mLastSubPopup.getVisibility() == 0) {
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().dismissPopup(this.mLastSubPopup);
            z = true;
            if (findCurrentSubPopup() == null) {
                PopupManager.getInstance(getContext()).notifyDismissPopup();
            }
        }
        return z;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        switch (i) {
            case 3:
                if (obj instanceof Boolean) {
                    boolean booleanValue = ((Boolean) obj).booleanValue();
                    if (this.mMessageDispacher != null) {
                        this.mMessageDispacher.dispacherMessage(4, C0049R.id.skin_beatify_button, 3, Boolean.valueOf(booleanValue), null);
                    }
                    if (booleanValue) {
                        if (this.mPreference.getValue().equals("pref_camera_face_beauty_key") || this.mPreference.getValue().equals("pref_camera_face_beauty_advanced_key")) {
                            showSubPopup();
                            break;
                        }
                    }
                    dismissSubPopup();
                    break;
                }
                break;
            case 6:
                if (!((obj2 instanceof Boolean) && ((Boolean) obj2).booleanValue())) {
                    notifyClickToDispatcher();
                    sendHideMessage();
                    setImageResource(this.mPreference.getIconIds()[findCurrentIndex()]);
                    if (!this.mPreference.getValue().equals("pref_camera_face_beauty_key") && !this.mPreference.getValue().equals("pref_camera_face_beauty_advanced_key")) {
                        dismissSubPopup();
                        break;
                    }
                    showSubPopup();
                    break;
                }
                break;
        }
        return true;
    }

    protected void initializePopup() {
        if (this.mPreference != null && !this.mPreference.hasPopup()) {
            Log.d("SkinBeautyButton", "no need to initialize popup, key=" + getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            ViewGroup topPopupParent = ((ActivityBase) this.mContext).getUIController().getTopPopupParent();
            this.mPopup = SettingPopupFactory.createSettingPopup(getKey(), topPopupParent, getContext());
            this.mPopup.initialize(((ActivityBase) this.mContext).getUIController().getPreferenceGroup(), this.mPreference, this);
            topPopupParent.addView(this.mPopup);
        }
    }

    protected void initializeSubPopup() {
        V6AbstractSettingPopup findCurrentSubPopup = findCurrentSubPopup();
        if (findCurrentSubPopup != null) {
            findCurrentSubPopup.reloadPreference();
            return;
        }
        ViewGroup popupParent = ((ActivityBase) this.mContext).getUIController().getPopupParent();
        View createSettingPopup = SettingPopupFactory.createSettingPopup(this.mPreference.getValue(), popupParent, getContext());
        PreferenceGroup preferenceGroup = ((ActivityBase) this.mContext).getUIController().getPreferenceGroup();
        createSettingPopup.initialize(preferenceGroup, (IconListPreference) preferenceGroup.findPreference(this.mPreference.getValue()), this.mSubDispacher);
        popupParent.addView(createSettingPopup);
        this.mSubPopups[findCurrentIndex()] = createSettingPopup;
    }

    public void onCameraOpen() {
        int i = 0;
        super.onCameraOpen();
        if (CameraSettingPreferences.instance().isFrontCamera() && Device.isSupportedSkinBeautify() && !V6ModulePicker.isVideoModule()) {
            this.mVisible = true;
            setImageResource(this.mPreference.getIconIds()[findCurrentIndex()]);
            setVisibility(0);
            PopupManager.getInstance(this.mContext).setOnOtherPopupShowedListener(this);
            if (this.mPopup != null) {
                this.mPopup.updateBackground();
            }
            if (this.mSubPopups != null) {
                V6AbstractSettingPopup[] v6AbstractSettingPopupArr = this.mSubPopups;
                int length = v6AbstractSettingPopupArr.length;
                while (i < length) {
                    V6AbstractSettingPopup v6AbstractSettingPopup = v6AbstractSettingPopupArr[i];
                    if (v6AbstractSettingPopup != null) {
                        v6AbstractSettingPopup.updateBackground();
                    }
                    i++;
                }
            }
            return;
        }
        this.mVisible = false;
        setVisibility(8);
    }

    public void onClick(View view) {
        if (!isPopupShown()) {
            CameraDataAnalytics.instance().trackEvent("pref_camera_face_beauty_mode_key");
        }
        if (isPopupShown()) {
            dismissPopup();
        } else {
            showPopup();
            sendHideMessage();
        }
        AutoLockManager.getInstance(this.mContext).onUserInteraction();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPreference = (IconListPreference) new PreferenceInflater(this.mContext).inflate((int) C0049R.xml.camera_skin_beautify_preferences);
        this.mSubPopups = new V6AbstractSettingPopup[this.mPreference.getEntryValues().length];
    }

    public boolean onOtherPopupShowed(int i) {
        return i == 1 ? dismissPopup() : false;
    }

    public void onPause() {
        this.mHandler.removeMessages(1);
    }

    public void recoverIfNeeded() {
    }

    public void setOrientation(int i, boolean z) {
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
            setActivated(true);
        }
    }

    protected void showSubPopup() {
        initializeSubPopup();
        V6AbstractSettingPopup findCurrentSubPopup = findCurrentSubPopup();
        if (findCurrentSubPopup != null) {
            findCurrentSubPopup.setOrientation(0, false);
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().showPopup(findCurrentSubPopup);
            ((ActivityBase) this.mContext).getUIController().getPreviewPage().simplifyPopup(false, false);
            PopupManager.getInstance(getContext()).notifyShowPopup(this, 1);
        }
        if (!(this.mLastSubPopup == null || this.mLastSubPopup == findCurrentSubPopup)) {
            dismissSubPopup();
        }
        this.mLastSubPopup = findCurrentSubPopup;
    }
}
