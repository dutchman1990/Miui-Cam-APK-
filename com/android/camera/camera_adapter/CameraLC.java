package com.android.camera.camera_adapter;

import android.hardware.Camera.Parameters;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.LCCameraProxy;
import com.android.camera.module.BaseModule;
import com.android.camera.module.CameraModule;
import java.util.List;

public class CameraLC extends CameraModule {
    private static LCCameraProxy sProxy = ((LCCameraProxy) CameraHardwareProxy.getDeviceProxy());
    private boolean mIsLongShotMode = false;

    private boolean getZSL() {
        return (getString(C0049R.string.pref_face_beauty_close).equals(CameraSettings.getFaceBeautifyValue()) && (this.mMultiSnapStatus || this.mMutexModePicker.isNormal() || this.mMutexModePicker.isHdr())) ? sProxy.getZslSupported(this.mParameters) : false;
    }

    private void lcUpdateCameraParametersPreference() {
        setBeautyParams();
        if (Device.isSupportedIntelligentBeautify()) {
            String string = this.mPreferences.getString("pref_camera_show_gender_age_key", getString(C0049R.string.pref_camera_show_gender_age_default));
            getUIController().getFaceView().setShowGenderAndAge(string);
            Log.i("Camera", "SetShowGenderAndAge =" + string);
            if ("on".equals(string)) {
                this.mParameters.set("xiaomi-preview-rotation", this.mOrientation == -1 ? 0 : this.mOrientation);
            }
        }
        if ((EffectController.getInstance().hasEffect() ? Device.isEffectWatermarkFilted() : false) || this.mMutexModePicker.isUbiFocus() || !CameraSettings.isTimeWaterMarkOpen(this.mPreferences)) {
            sProxy.setTimeWatermark(this.mParameters, "off");
        } else {
            sProxy.setTimeWatermark(this.mParameters, "on");
        }
        String manualValue = getManualValue("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
        if (BaseModule.isSupported(manualValue, sProxy.getSupportedIsoValues(this.mParameters))) {
            Log.v("Camera", "ISO value = " + manualValue);
            sProxy.setISOValue(this.mParameters, manualValue);
        }
        String string2 = this.mPreferences.getString("pref_qc_camera_saturation_key", getString(C0049R.string.pref_camera_saturation_default));
        Log.v("Camera", "Saturation value = " + string2);
        sProxy.setSaturation(this.mParameters, string2);
        String string3 = this.mPreferences.getString("pref_qc_camera_contrast_key", getString(C0049R.string.pref_camera_contrast_default));
        Log.v("Camera", "Contrast value = " + string3);
        sProxy.setContrast(this.mParameters, string3);
        String string4 = this.mPreferences.getString("pref_qc_camera_sharpness_key", getString(C0049R.string.pref_camera_sharpness_default));
        Log.v("Camera", "Sharpness value = " + string4);
        sProxy.setSharpness(this.mParameters, string4);
        setPictureFlipIfNeed(this.mParameters);
        this.mIsZSLMode = getZSL();
        sProxy.setZSLMode(this.mParameters, this.mIsZSLMode ? "true" : "false");
        if (this.mIsZSLMode && this.mMultiSnapStatus && !this.mIsLongShotMode) {
            this.mIsLongShotMode = true;
            sProxy.setBurstShotNum(this.mParameters, BURST_SHOOTING_COUNT);
        } else if (this.mIsLongShotMode) {
            this.mIsLongShotMode = false;
            sProxy.setBurstShotNum(this.mParameters, 1);
        } else {
            sProxy.setBurstShotNum(this.mParameters, 1);
        }
        Log.v("Camera", "Long Shot mode value = " + isLongShotMode());
    }

    private void setPictureFlipIfNeed(Parameters parameters) {
        if (isFrontMirror()) {
            sProxy.setPictureFlip(parameters, "1");
        } else {
            sProxy.setPictureFlip(parameters, "0");
        }
        Log.d("Camera", "Picture flip value = " + sProxy.getPictureFlip(parameters));
    }

    protected void applyMultiShutParameters(boolean z) {
        sProxy.setBurstShotNum(this.mParameters, z ? BURST_SHOOTING_COUNT : 0);
    }

    protected void cancelContinuousShot() {
        if (this.mIsLongShotMode) {
            this.mIsLongShotMode = false;
            applyMultiShutParameters(false);
            this.mCameraDevice.setParameters(this.mParameters);
        }
    }

    protected boolean isLongShotMode() {
        return this.mIsLongShotMode;
    }

    protected boolean isSupportSceneMode() {
        return true;
    }

    protected boolean isZeroShotMode() {
        return this.mIsZSLMode;
    }

    protected boolean needAutoFocusBeforeCapture() {
        String flashMode = this.mParameters.getFlashMode();
        return ("auto".equals(flashMode) && this.mCameraDevice.isNeedFlashOn()) ? true : "on".equals(flashMode);
    }

    protected boolean needSetupPreview(boolean z) {
        return !this.mCameraDevice.isPreviewEnable();
    }

    public void onSettingValueChanged(String str) {
        if (this.mCameraDevice != null) {
            if ("pref_qc_camera_iso_key".equals(str)) {
                String string = this.mPreferences.getString("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
                if (BaseModule.isSupported(string, sProxy.getSupportedIsoValues(this.mParameters))) {
                    Log.v("Camera", "ISO value = " + string);
                    sProxy.setISOValue(this.mParameters, string);
                }
                this.mCameraDevice.setParametersAsync(this.mParameters);
            } else {
                super.onSettingValueChanged(str);
            }
        }
    }

    protected void prepareCapture() {
        setPictureFlipIfNeed(this.mParameters);
        setTimeWatermarkIfNeed();
    }

    protected void setAutoExposure(Parameters parameters, String str) {
        List supportedAutoexposure = sProxy.getSupportedAutoexposure(parameters);
        if (supportedAutoexposure != null && supportedAutoexposure.contains(str)) {
            sProxy.setAutoExposure(parameters, str);
        }
    }

    protected void updateCameraParametersPreference() {
        super.updateCameraParametersPreference();
        lcUpdateCameraParametersPreference();
    }
}
