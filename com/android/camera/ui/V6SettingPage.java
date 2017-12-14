package com.android.camera.ui;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import com.android.camera.ActivityBase;
import com.android.camera.CameraManager;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Log;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.preferences.CameraPreference;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.ListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.List;

public class V6SettingPage extends RelativeLayout implements MessageDispacher, V6FunctionUI, Rotatable, AnimationListener, OnCheckedChangeListener {
    private int mDefaultColumnCount;
    private SettingDismissButton mDismissButton;
    private boolean mEnabled;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private int mIndicatorWidth;
    private MessageDispacher mMessageDispacher;
    private ModeView mModeView;
    public int mOrientation;
    private PreferenceGroup mPreferenceGroup;
    public V6SettingButton mSettingButton;
    private View mTitleView;
    private View mWaterMarkLayout;
    private Switch mWaterMarkOptionView;

    public V6SettingPage(Context context) {
        super(context);
    }

    public V6SettingPage(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void filterUnsupportedOptions(PreferenceGroup preferenceGroup, ListPreference listPreference, List<String> list) {
        if (list == null || list.size() <= 1) {
            removePreference(preferenceGroup, listPreference.getKey());
            return;
        }
        listPreference.filterUnsupported(list);
        if (listPreference.getEntries().length <= 1) {
            removePreference(preferenceGroup, listPreference.getKey());
        } else {
            resetIfInvalid(listPreference);
        }
    }

    private void initAnimation() {
        this.mFadeIn = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.screen_setting_fade_in);
        this.mFadeOut = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.screen_setting_fade_out);
        this.mFadeIn.setAnimationListener(this);
        this.mFadeOut.setAnimationListener(this);
    }

    private void initPreference() {
        Parameters stashParameters = CameraManager.instance().getStashParameters();
        this.mPreferenceGroup = ((ActivityBase) this.mContext).getUIController().getPreferenceGroup();
        ListPreference findPreference = this.mPreferenceGroup.findPreference("pref_video_time_lapse_frame_interval_key");
        ListPreference findPreference2 = this.mPreferenceGroup.findPreference("pref_camera_whitebalance_key");
        ListPreference findPreference3 = this.mPreferenceGroup.findPreference("pref_camera_scenemode_key");
        ListPreference findPreference4 = this.mPreferenceGroup.findPreference("pref_camera_coloreffect_key");
        ListPreference findPreference5 = this.mPreferenceGroup.findPreference("pref_camera_focus_mode_key");
        if (findPreference2 != null) {
            filterUnsupportedOptions(this.mPreferenceGroup, findPreference2, CameraHardwareProxy.getDeviceProxy().getSupportedWhiteBalance(stashParameters));
        }
        if (findPreference3 != null) {
            filterUnsupportedOptions(this.mPreferenceGroup, findPreference3, stashParameters.getSupportedSceneModes());
        }
        if (findPreference4 != null) {
            filterUnsupportedOptions(this.mPreferenceGroup, findPreference4, stashParameters.getSupportedColorEffects());
        }
        if (findPreference5 != null) {
            filterUnsupportedOptions(this.mPreferenceGroup, findPreference5, CameraHardwareProxy.getDeviceProxy().getSupportedFocusModes(stashParameters));
        }
        if (findPreference != null) {
            resetIfInvalid(findPreference);
        }
    }

    private boolean removePreference(PreferenceGroup preferenceGroup, String str) {
        int size = preferenceGroup.size();
        for (int i = 0; i < size; i++) {
            CameraPreference cameraPreference = preferenceGroup.get(i);
            if ((cameraPreference instanceof PreferenceGroup) && removePreference((PreferenceGroup) cameraPreference, str)) {
                return true;
            }
            if ((cameraPreference instanceof ListPreference) && ((ListPreference) cameraPreference).getKey().equals(str)) {
                preferenceGroup.removePreference(i);
                return true;
            }
        }
        return false;
    }

    private void resetIfInvalid(ListPreference listPreference) {
        if (listPreference.findIndexOfValue(listPreference.getValue()) == -1) {
            listPreference.setValueIndex(0);
        }
    }

    public void dismiss() {
        clearAnimation();
        startAnimation(this.mFadeOut);
        setVisibility(8);
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        if (i == 9) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.dismiss_setting, 3, null, null);
        } else {
            this.mMessageDispacher.dispacherMessage(i, C0049R.id.v6_setting_page, 2, obj, obj2);
        }
        return true;
    }

    public void enableControls(boolean z) {
        setEnabled(z);
        this.mEnabled = z;
    }

    public View getCurrentPopup() {
        return this.mModeView.getCurrentPopup();
    }

    protected void initIndicators() {
        List supportedSettingKeys = ((ActivityBase) this.mContext).getCurrentModule().getSupportedSettingKeys();
        if (supportedSettingKeys != null && supportedSettingKeys.size() != 0) {
            this.mModeView.initializeSettingScreen(this.mPreferenceGroup, supportedSettingKeys, this, 3);
        }
    }

    public boolean isItemSelected() {
        return this.mModeView.isItemSelected();
    }

    public void onAnimationEnd(Animation animation) {
        int i = 0;
        if (animation == this.mFadeOut) {
            i = C0049R.id.hide_mode_animation_done;
        } else if (animation == this.mFadeIn) {
            i = C0049R.id.show_mode_animation_done;
        }
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, i, 3, null, null);
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onCameraOpen() {
        reload();
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        CameraSettings.setDualCameraWaterMarkOpen(CameraSettingPreferences.instance(), z);
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.setting_page_watermark_option, 2, "pref_dualcamera_watermark", null);
        }
    }

    public void onCreate() {
        this.mModeView.resetSelectedFlag();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDefaultColumnCount = Device.isPad() ? 6 : 3;
        this.mModeView = (ModeView) findViewById(C0049R.id.setting_mode_view);
        this.mSettingButton = (V6SettingButton) findViewById(C0049R.id.setting_button);
        this.mDismissButton = (SettingDismissButton) findViewById(C0049R.id.dismiss_setting);
        this.mIndicatorWidth = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.v6_setting_item_width);
        this.mTitleView = findViewById(C0049R.id.setting_page_title_view);
        this.mWaterMarkLayout = findViewById(C0049R.id.setting_page_watermark_option_layout);
        this.mWaterMarkOptionView = (Switch) findViewById(C0049R.id.setting_page_watermark_option);
        this.mWaterMarkOptionView.setOnCheckedChangeListener(this);
        initAnimation();
    }

    public void onPause() {
    }

    public void onResume() {
        setVisibility(8);
    }

    public void overrideSettings(String... strArr) {
        this.mModeView.overrideSettings(strArr);
    }

    public void reload() {
        Log.m5v("Camera5", "reload getid=" + getId());
        removePopup();
        setVisibility(8);
        initPreference();
        initIndicators();
    }

    public void reloadPreferences() {
        this.mModeView.reloadPreferences();
    }

    public void removePopup() {
        this.mModeView.removePopup();
    }

    public boolean resetSettings() {
        return this.mModeView.resetSettings();
    }

    public void setEnabled(boolean z) {
        this.mSettingButton.setEnabled(z);
        this.mModeView.setEnabled(z);
        super.setEnabled(z);
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
        this.mSettingButton.setMessageDispatcher(messageDispacher);
        this.mDismissButton.setMessageDispatcher(messageDispacher);
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        this.mModeView.setOrientation(i, z);
        this.mSettingButton.setOrientation(i, z);
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        Log.m5v("Camera", "V6SettingPage setVisibility =" + i);
    }

    public void show() {
        clearAnimation();
        if (CameraSettings.isSupportedOpticalZoom() && CameraSettings.isBackCamera() && V6ModulePicker.isCameraModule()) {
            this.mTitleView.setVisibility(8);
            this.mWaterMarkOptionView.setChecked(CameraSettings.isDualCameraWaterMarkOpen(CameraSettingPreferences.instance()));
            this.mWaterMarkLayout.setVisibility(0);
        } else {
            this.mTitleView.setVisibility(0);
            this.mWaterMarkLayout.setVisibility(8);
        }
        setVisibility(0);
        reloadPreferences();
        enableControls(false);
        startAnimation(this.mFadeIn);
    }
}
