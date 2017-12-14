package com.android.camera.camera_adapter;

import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.NvidiaCameraProxy;
import com.android.camera.module.CameraModule;

public class CameraNv extends CameraModule {
    private static int RAW_META_DATA = 1000000;
    private static NvidiaCameraProxy sProxy = ((NvidiaCameraProxy) CameraHardwareProxy.getDeviceProxy());
    private final String TAG = "CameraNv";
    private int mNSLBurstCount;
    private boolean mPreviewPausedDisabled;
    private byte[] mRawBuffer;
    private int mRawBufferSize = 0;
    private boolean mSetAohdrLater;
    private boolean mSkipSetNSLAfterMultiShot;

    private void allocRawBufferIfNeeded() {
        int i = RAW_META_DATA + 26257920;
        if (this.mRawBuffer == null || this.mRawBufferSize < i) {
            try {
                this.mRawBuffer = new byte[i];
                this.mRawBufferSize = i;
            } catch (OutOfMemoryError e) {
                this.mRawBuffer = null;
                this.mRawBufferSize = 0;
                Log.v("CameraNv", "Raw OutOfMemoryError: " + e.getMessage());
            }
        }
    }

    private int getNSLBuffersNeededCount() {
        String flashMode = this.mParameters.getFlashMode();
        return this.mMultiSnapStatus ? 4 : (!getZSL().equals(getString(C0049R.string.pref_camera_zsl_value_off)) && this.mMutexModePicker.isNormal() && !"on".equals(flashMode) && sProxy.getNvExposureTime(this.mParameters) == 0 && sProxy.getISOValue(this.mParameters).equals(getString(C0049R.string.pref_camera_iso_value_auto)) && !("auto".equals(flashMode) && this.mCameraDevice.isNeedFlashOn())) ? 4 : 0;
    }

    private boolean getPreviewPausedDisabled() {
        this.mPreviewPausedDisabled = true;
        if (this.mMutexModePicker.isNormal() && sProxy.getNvExposureTime(this.mParameters) == 0 && sProxy.getISOValue(this.mParameters).equals(getString(C0049R.string.pref_camera_iso_value_auto))) {
            if (this.mIsImageCaptureIntent) {
            }
            return this.mPreviewPausedDisabled;
        }
        this.mPreviewPausedDisabled = false;
        Log.v("CameraNv", "getPreviewPausedDisabled " + this.mPreviewPausedDisabled + " " + this.mMutexModePicker.isNormal() + " " + sProxy.getNvExposureTime(this.mParameters) + " " + sProxy.getISOValue(this.mParameters) + " " + this.mIsImageCaptureIntent);
        return this.mPreviewPausedDisabled;
    }

    private String getZSL() {
        return "on";
    }

    private void updateNvCameraParametersPreference() {
        int parseInt = Integer.parseInt(this.mPreferences.getString("pref_qc_camera_saturation_key", getString(C0049R.string.pref_camera_saturation_default)));
        if (parseInt >= -100 && parseInt <= 100) {
            sProxy.setSaturation(this.mParameters, parseInt);
        }
        Log.i("CameraNv", "Saturation = " + parseInt);
        String string = this.mPreferences.getString("pref_qc_camera_contrast_key", getString(C0049R.string.pref_camera_contrast_default));
        sProxy.setContrast(this.mParameters, string);
        Log.i("CameraNv", "Contrast = " + string);
        int parseInt2 = Integer.parseInt(this.mPreferences.getString("pref_qc_camera_sharpness_key", getString(C0049R.string.pref_camera_sharpness_default)));
        if (parseInt2 >= -100 && parseInt2 <= 100) {
            sProxy.setEdgeEnhancement(this.mParameters, parseInt2);
        }
        Log.i("CameraNv", "Sharpness = " + parseInt2);
        if (!sProxy.getAutoRotation(this.mParameters)) {
            sProxy.setAutoRotation(this.mParameters, true);
        }
        String manualValue = getManualValue("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default));
        sProxy.setISOValue(this.mParameters, manualValue);
        Log.i("CameraNv", "PictureISO = " + manualValue);
        String manualValue2 = getManualValue("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default));
        sProxy.setExposureTime(this.mParameters, Integer.parseInt(manualValue2));
        Log.i("CameraNv", "ExposureTime = " + manualValue2);
        this.mSkipSetNSLAfterMultiShot = false;
        this.mSetAohdrLater = false;
        if (this.mMutexModePicker.isNormal()) {
            if (this.mRawBuffer != null) {
                this.mRawBuffer = null;
                this.mRawBufferSize = 0;
                this.mCameraDevice.addRawImageCallbackBuffer(null);
            }
            sProxy.setHandNight(this.mParameters, false);
            sProxy.setRawDumpFlag(this.mParameters, 0);
            if (sProxy.getAohdrEnable(this.mParameters)) {
                sProxy.setAohdrEnable(this.mParameters, false);
                this.mCameraDevice.setParameters(this.mParameters);
                this.mParameters = this.mCameraDevice.getParameters();
            }
            sProxy.setMorphoHDR(this.mParameters, false);
        } else if (this.mMutexModePicker.isHandNight()) {
            sProxy.setHandNight(this.mParameters, true);
            Log.i("CameraNv", "Hand Nigh = true");
        } else if (this.mMutexModePicker.isRAW()) {
            sProxy.setRawDumpFlag(this.mParameters, 13);
            Log.i("CameraNv", "Raw Data = true");
            allocRawBufferIfNeeded();
        } else if (this.mMutexModePicker.isAoHdr()) {
            if (!sProxy.getAohdrEnable(this.mParameters)) {
                this.mSetAohdrLater = true;
                Log.i("CameraNv", "AO HDR = true");
            }
        } else if (this.mMutexModePicker.isMorphoHdr()) {
            sProxy.setMorphoHDR(this.mParameters, true);
            Log.i("CameraNv", "Morpho HDR = true");
        }
        if (this.mMultiSnapStopRequest) {
            this.mSkipSetNSLAfterMultiShot = true;
        }
        this.mNSLBurstCount = sProxy.getNSLNumBuffers(this.mParameters);
        int nSLBuffersNeededCount = getNSLBuffersNeededCount();
        if (!(this.mSkipSetNSLAfterMultiShot || this.mNSLBurstCount == nSLBuffersNeededCount)) {
            sProxy.setNSLNumBuffers(this.mParameters, nSLBuffersNeededCount);
            if (nSLBuffersNeededCount == 0) {
                sProxy.setNSLBurstCount(this.mParameters, 0);
                sProxy.setBurstCount(this.mParameters, 1);
                sProxy.setNVShotMode(this.mParameters, "normal");
            }
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
            this.mNSLBurstCount = sProxy.getNSLNumBuffers(this.mParameters);
            Log.i("CameraNv", "Allocate NSLNumBuffers = " + this.mNSLBurstCount);
        }
        if (this.mMultiSnapStatus) {
            if (this.mNSLBurstCount <= 0 || nSLBuffersNeededCount <= 0) {
                sProxy.setNVShotMode(this.mParameters, "normal");
            } else {
                sProxy.setNVShotMode(this.mParameters, "shot2shot");
            }
            sProxy.setNSLBurstCount(this.mParameters, 0);
            sProxy.setBurstCount(this.mParameters, BURST_SHOOTING_COUNT);
        } else {
            if (this.mSkipSetNSLAfterMultiShot || this.mNSLBurstCount <= 0 || nSLBuffersNeededCount <= 0 || !this.mMutexModePicker.isNormal()) {
                sProxy.setNSLBurstCount(this.mParameters, 0);
                sProxy.setBurstCount(this.mParameters, 1);
            } else {
                sProxy.setNSLBurstCount(this.mParameters, 1);
                sProxy.setBurstCount(this.mParameters, 0);
            }
            sProxy.setNVShotMode(this.mParameters, "normal");
        }
        if (this.mSetAohdrLater) {
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
            if (!"off".equals(this.mParameters.getFlashMode())) {
                this.mParameters.setFlashMode("off");
                this.mCameraDevice.setParameters(this.mParameters);
                this.mParameters = this.mCameraDevice.getParameters();
            } else if (sProxy.getNSLNumBuffers(this.mParameters) != 0) {
                sProxy.setNSLNumBuffers(this.mParameters, 0);
                sProxy.setNSLBurstCount(this.mParameters, 0);
                this.mCameraDevice.setParameters(this.mParameters);
                this.mParameters = this.mCameraDevice.getParameters();
            }
            sProxy.setAohdrEnable(this.mParameters, true);
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
        }
        sProxy.setPreviewPauseDisabled(this.mParameters, getPreviewPausedDisabled());
        Log.d("CameraNv", "preview disabled = " + sProxy.getPreviewPauseDisabled(this.mParameters));
        if ((EffectController.getInstance().hasEffect() ? Device.isEffectWatermarkFilted() : false) || !CameraSettings.isTimeWaterMarkOpen(this.mPreferences)) {
            sProxy.setTimeWatermark(this.mParameters, "off");
        } else {
            sProxy.setTimeWatermark(this.mParameters, "on");
        }
        Log.i("CameraNv", "SetTimeWatermark =" + sProxy.getTimeWatermark(this.mParameters));
        setBeautyParams();
        String string2 = this.mPreferences.getString("pref_camera_show_gender_age_key", getString(C0049R.string.pref_camera_show_gender_age_default));
        getUIController().getFaceView().setShowGenderAndAge(string2);
        Log.i("CameraNv", "SetShowGenderAndAge =" + string2);
        sProxy.setMultiFaceBeautify(this.mParameters, "on");
        Log.i("CameraNv", "SetMultiFaceBeautify =on");
    }

    protected boolean isLongShotMode() {
        return this.mMultiSnapStatus;
    }

    protected boolean isZeroShotMode() {
        return this.mNSLBurstCount != 0;
    }

    protected boolean needAutoFocusBeforeCapture() {
        String flashMode = this.mParameters.getFlashMode();
        return ("auto".equals(flashMode) && this.mCameraDevice.isNeedFlashOn()) ? true : "on".equals(flashMode);
    }

    protected boolean needSetupPreview(boolean z) {
        return this.mPreviewPausedDisabled ? this.mMultiSnapStopRequest : true;
    }

    protected boolean needSwitchZeroShotMode() {
        String requestFlashMode = getRequestFlashMode();
        return !this.mSkipSetNSLAfterMultiShot ? this.mNSLBurstCount > 0 ? ("auto".equals(requestFlashMode) && this.mCameraDevice.isNeedFlashOn()) ? true : "on".equals(requestFlashMode) : false : true;
    }

    public void onPauseBeforeSuper() {
        if (this.mMutexModePicker.isAoHdr()) {
            this.mMutexModePicker.resetMutexMode();
        }
        super.onPauseBeforeSuper();
    }

    public void onSettingValueChanged(String str) {
        if (this.mCameraDevice != null) {
            if ("pref_focus_position_key".equals(str)) {
                sProxy.setFocusPosition(this.mParameters, CameraSettings.getFocusPosition());
                this.mCameraDevice.setParametersAsync(this.mParameters);
            } else if ("pref_qc_manual_whitebalance_k_value_key".equals(str)) {
                sProxy.setColorTemperature(this.mParameters, CameraSettings.getKValue());
                this.mCameraDevice.setParametersAsync(this.mParameters);
            } else {
                super.onSettingValueChanged(str);
            }
        }
    }

    protected void prepareCapture() {
        if (isFrontMirror()) {
            sProxy.setFlipStill(this.mParameters, "horizontal");
        } else {
            sProxy.setFlipStill(this.mParameters, "off");
        }
        Log.i("CameraNv", "Set JPEG horizontal flip = " + sProxy.isFrontMirror(this.mParameters));
    }

    protected void updateCameraParametersPreference() {
        super.updateCameraParametersPreference();
        updateNvCameraParametersPreference();
    }
}
