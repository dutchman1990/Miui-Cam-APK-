package com.android.camera.camera_adapter;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.MTKCameraProxy;
import com.android.camera.module.VideoModule;
import com.android.camera.stereo.WarningCallback;
import java.util.HashMap;

public class VideoMTK extends VideoModule {
    private static MTKCameraProxy sProxy = ((MTKCameraProxy) CameraHardwareProxy.getDeviceProxy());
    private final WarningCallback mStereoCameraWarningCallback = new WarningCallback();

    static {
        if (Device.isSupportedHFR()) {
            int intField = Util.getIntField("com.mediatek.camcorder.CamcorderProfileEx", null, "SLOW_MOTION_480P_120FPS", "I");
            HashMap hashMap = VIDEO_QUALITY_TO_HIGHSPEED;
            Integer valueOf = Integer.valueOf(4);
            if (intField == Integer.MIN_VALUE) {
                intField = 4;
            }
            hashMap.put(valueOf, Integer.valueOf(intField));
            int intField2 = Util.getIntField("com.mediatek.camcorder.CamcorderProfileEx", null, "SLOW_MOTION_HD_120FPS", "I");
            hashMap = VIDEO_QUALITY_TO_HIGHSPEED;
            Integer valueOf2 = Integer.valueOf(5);
            if (intField2 == Integer.MIN_VALUE) {
                intField2 = 5;
            }
            hashMap.put(valueOf2, Integer.valueOf(intField2));
        }
    }

    private void setParameterExtra(MediaRecorder mediaRecorder, String str) {
    }

    protected void closeCamera() {
        if (Device.isSupportedStereo() && this.mCameraDevice != null) {
            this.mCameraDevice.setStereoWarningCallback(null);
        }
        super.closeCamera();
    }

    protected CamcorderProfile fetchProfile(int i, int i2) {
        return null;
    }

    protected int getNormalVideoFrameRate() {
        return 30;
    }

    protected boolean isProfileExist(int i, Integer num) {
        return true;
    }

    protected void onCameraOpen() {
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            this.mStereoCameraWarningCallback.setActivity(this.mActivity);
            this.mCameraDevice.setStereoWarningCallback(this.mStereoCameraWarningCallback);
            if (CameraSettings.isDualCameraHintShown(this.mPreferences)) {
                this.mHandler.sendEmptyMessage(21);
            }
        }
    }

    public void onSettingValueChanged(String str) {
        if (this.mCameraDevice != null) {
            if ("pref_camera_stereo_key".equals(str)) {
                String string = this.mPreferences.getString("pref_camera_stereo_key", getString(C0049R.string.pref_camera_stereo_default));
                Log.v("VideoMTK", "vfLevel value = " + string);
                sProxy.setVsDofLevel(this.mParameters, string);
                this.mCameraDevice.setParametersAsync(this.mParameters);
                updateStatusBar("pref_camera_stereo_key");
            } else {
                super.onSettingValueChanged(str);
            }
        }
    }

    protected void pauseMediaRecorder(MediaRecorder mediaRecorder) {
    }

    protected void prepareOpenCamera() {
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") && !this.mIsVideoCaptureIntent) {
            sProxy.enableStereoMode();
        }
    }

    protected void setHFRSpeed(MediaRecorder mediaRecorder, int i) {
        if (Device.isSupportedHFR()) {
            setParameterExtra(mediaRecorder, "media-param-slowmotion=" + i);
        }
    }

    protected boolean startRecordVideo() {
        boolean startRecordVideo = super.startRecordVideo();
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            setParameterExtra(this.mMediaRecorder, "media-param-audio-stop-first=1");
        }
        return startRecordVideo;
    }

    protected void updateVideoParametersPreference() {
        super.updateVideoParametersPreference();
        sProxy.setCameraMode(this.mParameters, 2);
        if (Device.isSupportedHFR()) {
            if ("slow".equals(this.mHfr)) {
                sProxy.setSlowMotion(this.mParameters, "on");
                sProxy.set3dnrMode(this.mParameters, "off");
                sProxy.setVideoHighFrameRate(this.mParameters, String.valueOf(this.mProfile.videoFrameRate));
            } else {
                sProxy.setSlowMotion(this.mParameters, "off");
                sProxy.set3dnrMode(this.mParameters, "on");
            }
            this.mParameters.setPreviewFrameRate(this.mProfile.videoFrameRate);
        }
        if (Device.isSupportedStereo()) {
            if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                this.mParameters.setPreviewFpsRange(5000, 24000);
                sProxy.setVsDofMode(this.mParameters, true);
                String string = this.mPreferences.getString("pref_camera_stereo_key", getString(C0049R.string.pref_camera_stereo_default));
                Log.v("VideoMTK", "vfLevel value = " + string);
                sProxy.setVsDofLevel(this.mParameters, string);
            } else {
                sProxy.setVsDofMode(this.mParameters, false);
                this.mParameters.setPreviewFpsRange(5000, 30000);
            }
        }
        this.mActivity.getCameraScreenNail().setVideoStabilizationCropped(false);
    }
}
