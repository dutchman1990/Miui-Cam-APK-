package com.android.camera.camera_adapter;

import android.hardware.Camera.Parameters;
import android.os.SystemProperties;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.PictureSize;
import com.android.camera.PictureSizeManager;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.QcomCameraProxy;
import com.android.camera.module.BaseModule;
import com.android.camera.module.CameraModule;
import java.util.List;

public class CameraQcom extends CameraModule {
    private static QcomCameraProxy sProxy = ((QcomCameraProxy) CameraHardwareProxy.getDeviceProxy());
    private boolean mIsLongShotMode = false;

    private boolean couldEnableChromaFlash() {
        return (this.mMultiSnapStatus || "af-bracket-on".equals(sProxy.getUbiFocus(this.mParameters)) || !isDefaultPreference("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default))) ? false : isDefaultPreference("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default));
    }

    private String getZSL() {
        return this.mMultiSnapStatus ? "on" : this.mMutexModePicker.isRAW() ? "off" : (!Device.IS_XIAOMI || Device.IS_MI2A || isDefaultManualExposure()) ? ((Device.isUsedNightMode() || this.mMutexModePicker.isNormal() || (this.mMutexModePicker.isSceneHdr() && sProxy.isZSLHDRSupported(this.mParameters))) && !(Device.IS_HM3LTE && isFrontCamera())) ? "on" : "off" : "off";
    }

    private void qcomUpdateCameraParametersPreference() {
        if (Device.isSupportedManualFunction()) {
            int min = Math.min(Integer.parseInt(getManualValue("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default))), sProxy.getMaxExposureTimeValue(this.mParameters));
            if (min >= 0) {
                sProxy.setExposureTime(this.mParameters, min);
                Log.v("Camera", "ExposureTime value=" + sProxy.getExposureTime(this.mParameters));
                if (Device.isFloatExposureTime()) {
                    min /= 1000;
                }
                if (min >= 1000) {
                    configOisParameters(this.mParameters, false);
                }
            }
        }
        if ((EffectController.getInstance().hasEffect() ? Device.isEffectWatermarkFilted() : false) || this.mMutexModePicker.isUbiFocus() || !CameraSettings.isTimeWaterMarkOpen(this.mPreferences)) {
            sProxy.setTimeWatermark(this.mParameters, "off");
        } else {
            sProxy.setTimeWatermark(this.mParameters, "on");
        }
        if ((EffectController.getInstance().hasEffect() ? Device.isEffectWatermarkFilted() : false) || this.mMutexModePicker.isUbiFocus() || !CameraSettings.isDualCameraWaterMarkOpen(this.mPreferences)) {
            sProxy.setDualCameraWatermark(this.mParameters, "off");
        } else {
            sProxy.setDualCameraWatermark(this.mParameters, "on");
        }
        if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
            sProxy.setPortraitMode(this.mParameters, "on");
        } else {
            sProxy.setPortraitMode(this.mParameters, "off");
        }
        if (Device.isSupportedFaceInfoWaterMark()) {
            sProxy.setFaceWatermark(this.mParameters, !this.mMutexModePicker.isUbiFocus() ? CameraSettings.isFaceWaterMarkOpen(this.mPreferences) : false);
        }
        setBeautyParams();
        if (Device.isSupportedIntelligentBeautify()) {
            String string = this.mPreferences.getString("pref_camera_show_gender_age_key", getString(C0049R.string.pref_camera_show_gender_age_default));
            getUIController().getFaceView().setShowGenderAndAge(string);
            Log.i("Camera", "SetShowGenderAndAge =" + string);
            sProxy.setAnalyzeAgeGender(this.mParameters, CameraSettings.isFaceBeautyOn(CameraSettings.getFaceBeautifyValue()) ? CameraSettings.showGenderAge(this.mPreferences) : false);
        }
        if (Device.isSupportedObjectTrack() || Device.isSupportedIntelligentBeautify()) {
            this.mParameters.set("xiaomi-preview-rotation", this.mOrientation == -1 ? 0 : this.mOrientation);
        }
        if (sProxy.getSupportedDenoiseModes(this.mParameters) != null) {
            String str = (Device.isSupportBurstDenoise() || !this.mMultiSnapStatus) ? "denoise-on" : "denoise-off";
            Log.v("Camera", "Denoise value = " + str);
            sProxy.setDenoise(this.mParameters, str);
        }
        if (this.mCameraState != 0) {
            String manualValue = getManualValue("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
            if (BaseModule.isSupported(manualValue, sProxy.getSupportedIsoValues(this.mParameters))) {
                Log.v("Camera", "ISO value = " + manualValue);
                sProxy.setISOValue(this.mParameters, manualValue);
            }
        }
        int min2 = Math.min(Integer.parseInt(this.mPreferences.getString("pref_qc_camera_saturation_key", getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_camera_saturation_default)))), sProxy.getMaxSaturation(this.mParameters));
        if (min2 >= 0) {
            Log.v("Camera", "Saturation value = " + min2);
            sProxy.setSaturation(this.mParameters, min2);
        }
        int min3 = Math.min(Integer.parseInt(this.mPreferences.getString("pref_qc_camera_contrast_key", getString(C0049R.string.pref_camera_contrast_default))), sProxy.getMaxContrast(this.mParameters));
        if (min3 >= 0) {
            Log.v("Camera", "Contrast value = " + min3);
            sProxy.setContrast(this.mParameters, min3);
        }
        int min4 = Math.min(Integer.parseInt(this.mPreferences.getString("pref_qc_camera_sharpness_key", getString(C0049R.string.pref_camera_sharpness_default))), sProxy.getMaxSharpness(this.mParameters));
        if (min4 >= 0) {
            Log.v("Camera", "Sharpness value = " + min4);
            sProxy.setSharpness(this.mParameters, min4);
        }
        String string2 = this.mPreferences.getString("pref_camera_touchafaec_key", getString(C0049R.string.pref_camera_touchafaec_default));
        if (BaseModule.isSupported(string2, sProxy.getSupportedTouchAfAec(this.mParameters))) {
            Log.v("Camera", "TouchAfAec value = " + string2);
            sProxy.setTouchAfAec(this.mParameters, string2);
        }
        if (Device.isSupportedMagicMirror()) {
            sProxy.setBeautyRank(this.mParameters, CameraSettings.isSwitchOn("pref_camera_magic_mirror_key"));
        }
        setPictureFlipIfNeed();
        if (this.mFaceDetectionEnabled) {
            sProxy.setFaceDetectionMode(this.mParameters, "on");
        } else {
            sProxy.setFaceDetectionMode(this.mParameters, "off");
        }
        if (Device.isUsedNightMode()) {
            this.mParameters.set("ae-bracket-hdr", "Off");
        }
        sProxy.setHandNight(this.mParameters, false);
        sProxy.setMorphoHDR(this.mParameters, false);
        sProxy.setUbiFocus(this.mParameters, "af-bracket-off");
        sProxy.setAoHDR(this.mParameters, "off");
        sProxy.setHDR(this.mParameters, "false");
        sProxy.setNightShot(this.mParameters, "false");
        sProxy.setNightAntiMotion(this.mParameters, "false");
        if (!this.mMutexModePicker.isNormal()) {
            if (this.mMutexModePicker.isHandNight()) {
                if (isSceneMotion()) {
                    sProxy.setNightAntiMotion(this.mParameters, "true");
                } else {
                    sProxy.setNightShot(this.mParameters, "true");
                }
                Log.v("Camera", "Hand Nigh = true");
            } else if (this.mMutexModePicker.isRAW()) {
                Log.v("Camera", "Raw Data = true");
            } else if (this.mMutexModePicker.isAoHdr()) {
                sProxy.setAoHDR(this.mParameters, "on");
                Log.v("Camera", "AoHDR = true");
            } else if (this.mMutexModePicker.isMorphoHdr()) {
                sProxy.setHDR(this.mParameters, "true");
                Log.v("Camera", "Morpho HDR = true");
            } else if (this.mMutexModePicker.isUbiFocus()) {
                sProxy.setUbiFocus(this.mParameters, "af-bracket-on");
                Log.v("Camera", "Ubi Focus = true");
            }
        }
        String zsl = getZSL();
        Log.v("Camera", "ZSL value = " + zsl);
        boolean z;
        if (zsl.equals("on")) {
            z = (!Device.shouldRestartPreviewAfterZslSwitch() || this.mIsZSLMode) ? false : this.mCameraState != 0;
            this.mRestartPreview = z;
            this.mIsZSLMode = true;
            sProxy.setZSLMode(this.mParameters, "on");
            sProxy.setCameraMode(this.mParameters, 1);
        } else if (zsl.equals("off")) {
            z = (Device.shouldRestartPreviewAfterZslSwitch() && this.mIsZSLMode) ? this.mCameraState != 0 : false;
            this.mRestartPreview = z;
            this.mIsZSLMode = false;
            sProxy.setZSLMode(this.mParameters, "off");
            sProxy.setCameraMode(this.mParameters, 0);
        }
        if (this.mIsZSLMode && this.mMultiSnapStatus && !this.mIsLongShotMode) {
            this.mIsLongShotMode = true;
            if (Device.IS_MI2 || Device.IS_MI2A) {
                this.mParameters.set("num-snaps-per-shutter", BURST_SHOOTING_COUNT);
            } else {
                this.mCameraDevice.setLongshotMode(true);
            }
            setTimeWatermarkIfNeed();
        } else if (this.mIsLongShotMode) {
            this.mIsLongShotMode = false;
            if (Device.IS_MI2 || Device.IS_MI2A) {
                this.mParameters.set("num-snaps-per-shutter", 1);
            } else {
                this.mCameraDevice.setLongshotMode(false);
            }
        }
        Log.v("Camera", "Long Shot mode value = " + isLongShotMode());
        this.mParameters.setAutoWhiteBalanceLock(this.mIsLongShotMode ? "torch".equals(this.mParameters.getFlashMode()) : false);
        if (Device.isSupportedChromaFlash()) {
            String str2 = (couldEnableChromaFlash() && this.mPreferences.getBoolean("pref_auto_chroma_flash_key", getResources().getBoolean(CameraSettings.getDefaultPreferenceId(C0049R.bool.pref_camera_auto_chroma_flash_default)))) ? "chroma-flash-on" : "chroma-flash-off";
            sProxy.setChromaFlash(this.mParameters, str2);
        }
        Log.v("Camera", "Chroma Flash = " + sProxy.getChromaFlash(this.mParameters));
        if (isBackCamera() && CameraSettings.isSupportedMetadata()) {
            int i = 0;
            if (CameraSettings.isSupportedPortrait() && CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                i = 5;
            }
            if (Device.isSupportedASD()) {
                boolean z2 = (getUIController().getSettingPage().isItemSelected() || this.mIsLongShotMode) ? false : i == 0;
                Log.v("Camera", "ASD Enable = " + z2);
                this.mParameters.set("scene-detect", z2 ? "on" : "off");
                if (z2) {
                    i = 3;
                }
            }
            setMetaCallback(i);
        }
    }

    private void setPictureFlipIfNeed() {
        if (!isFrontMirror()) {
            sProxy.setPictureFlip(this.mParameters, "off");
        } else if (this.mOrientation == -1 || this.mOrientation % 180 == 0) {
            sProxy.setPictureFlip(this.mParameters, "flip-v");
        } else {
            sProxy.setPictureFlip(this.mParameters, "flip-h");
        }
        Log.d("Camera", "Picture flip value = " + sProxy.getPictureFlip(this.mParameters));
    }

    protected void cancelContinuousShot() {
        if (!this.mMultiSnapStatus && this.mIsLongShotMode) {
            this.mIsLongShotMode = false;
            this.mCameraDevice.setLongshotMode(false);
            Log.v("Camera", "Long Shot mode value = " + isLongShotMode());
        }
    }

    protected PictureSize getBestPictureSize() {
        List supportedPortraitPictureSizes = (CameraSettings.isSupportedPortrait() && CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) ? sProxy.getSupportedPortraitPictureSizes(this.mCameraDevice, this.mParameters) : this.mParameters.getSupportedPictureSizes();
        PictureSizeManager.initialize(getActivity(), supportedPortraitPictureSizes, getMaxPictureSize());
        return PictureSizeManager.getBestPictureSize();
    }

    protected int getBurstDelayTime() {
        return Device.IS_HONGMI ? 300 : 200;
    }

    protected boolean isLongShotMode() {
        return this.mIsLongShotMode;
    }

    protected boolean isSupportSceneMode() {
        return Device.IS_HONGMI;
    }

    protected boolean isZeroShotMode() {
        return this.mIsZSLMode;
    }

    protected boolean needAutoFocusBeforeCapture() {
        String flashMode = this.mParameters.getFlashMode();
        return !"on".equals(flashMode) ? "auto".equals(flashMode) ? this.mCameraDevice.isNeedFlashOn() : false : true;
    }

    protected boolean needSetupPreview(boolean z) {
        return !z ? (SystemProperties.getBoolean("persist.camera.feature.restart", false) && sProxy.getInternalPreviewSupported(this.mParameters) && "jpeg".equalsIgnoreCase(this.mParameters.get("picture-format"))) ? false : true : false;
    }

    protected void onCameraStartPreview() {
        if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key") && CameraSettings.isDualCameraHintShown(this.mPreferences)) {
            this.mHandler.sendEmptyMessage(40);
        }
    }

    public void onResumeAfterSuper() {
        super.onResumeAfterSuper();
        this.mActivity.getSensorStateManager().setEdgeTouchEnabled(CameraSettings.isEdgePhotoEnable());
    }

    public void onSettingValueChanged(String str) {
        if (this.mCameraDevice != null) {
            if ("pref_focus_position_key".equals(str)) {
                sProxy.setFocusPosition(this.mParameters, CameraSettings.getFocusPosition());
                this.mCameraDevice.setParametersAsync(this.mParameters);
            } else if ("pref_qc_manual_whitebalance_k_value_key".equals(str)) {
                sProxy.setWBManualCCT(this.mParameters, CameraSettings.getKValue());
                this.mCameraDevice.setParametersAsync(this.mParameters);
            } else {
                super.onSettingValueChanged(str);
            }
        }
    }

    protected void prepareCapture() {
        setPictureFlipIfNeed();
        if (Device.IS_H2XLTE && this.mMutexModePicker.isHdr()) {
            this.mParameters.setAutoExposureLock(true);
            this.mParameters.setAutoWhiteBalanceLock(true);
        }
        if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
            setBeautyParams();
        }
        setTimeWatermarkIfNeed();
    }

    protected void setAutoExposure(Parameters parameters, String str) {
        List supportedAutoexposure = sProxy.getSupportedAutoexposure(parameters);
        if (supportedAutoexposure != null && supportedAutoexposure.contains(str)) {
            sProxy.setAutoExposure(parameters, str);
        }
    }

    protected void setBeautyParams() {
        if (!Device.IS_MI2 || Device.IS_MI2A) {
            super.setBeautyParams();
            return;
        }
        String faceBeautifyValue = CameraSettings.getFaceBeautifyValue();
        if (CameraSettings.isFaceBeautyOn(faceBeautifyValue)) {
            try {
                faceBeautifyValue = String.valueOf((((Integer.parseInt(faceBeautifyValue) | (Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_skin_color_key")) << 28)) | (Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_slim_face_key")) << 24)) | (Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_skin_smooth_key")) << 16)) | (Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_enlarge_eye_key")) << 20));
            } catch (NumberFormatException e) {
                Log.e("Camera", "check beautify detail values in strings.xml of aries");
            }
        }
        sProxy.setStillBeautify(this.mParameters, faceBeautifyValue);
        Log.i("Camera", "SetStillBeautify =" + sProxy.getStillBeautify(this.mParameters));
    }

    protected void setManualParameters() {
        sProxy.setFocusMode(this.mParameters, this.mFocusManager.getFocusMode());
        int min = Math.min(Integer.parseInt(getManualValue("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default))), sProxy.getMaxExposureTimeValue(this.mParameters));
        if (min >= 0) {
            sProxy.setExposureTime(this.mParameters, min);
            Log.v("Camera", "ExposureTime value=" + sProxy.getExposureTime(this.mParameters));
        }
        String manualValue = getManualValue("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
        if (BaseModule.isSupported(manualValue, sProxy.getSupportedIsoValues(this.mParameters))) {
            Log.v("Camera", "ISO value = " + manualValue);
            sProxy.setISOValue(this.mParameters, manualValue);
        }
    }

    protected void updateCameraParametersInitialize() {
        super.updateCameraParametersInitialize();
        int[] photoPreviewFpsRange = CameraSettings.getPhotoPreviewFpsRange(this.mParameters);
        if ((Device.IS_MI4 || Device.IS_X5) && photoPreviewFpsRange != null && photoPreviewFpsRange.length > 0) {
            this.mParameters.setPreviewFpsRange(photoPreviewFpsRange[0], photoPreviewFpsRange[1]);
        }
    }

    protected void updateCameraParametersPreference() {
        super.updateCameraParametersPreference();
        qcomUpdateCameraParametersPreference();
    }
}
