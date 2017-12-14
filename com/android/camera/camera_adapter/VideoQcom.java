package com.android.camera.camera_adapter;

import android.media.MediaRecorder;
import android.os.Build.VERSION;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.QcomCameraProxy;
import com.android.camera.module.BaseModule;
import com.android.camera.module.VideoModule;
import java.util.HashMap;

public class VideoQcom extends VideoModule {
    private static final String VIDEO_HIGH_FRAME_RATE = (Device.IS_MI2 ? "90" : "120");
    private static QcomCameraProxy sProxy = ((QcomCameraProxy) CameraHardwareProxy.getDeviceProxy());

    static {
        if (Device.isSupportedHFR()) {
            int intField = Util.getIntField("android.media.CamcorderProfile", null, "QUALITY_HIGH_SPEED_480P", "I");
            HashMap hashMap = VIDEO_QUALITY_TO_HIGHSPEED;
            Integer valueOf = Integer.valueOf(4);
            if (intField == Integer.MIN_VALUE) {
                intField = 4;
            }
            hashMap.put(valueOf, Integer.valueOf(intField));
            int intField2 = Util.getIntField("android.media.CamcorderProfile", null, "QUALITY_HIGH_SPEED_720P", "I");
            hashMap = VIDEO_QUALITY_TO_HIGHSPEED;
            Integer valueOf2 = Integer.valueOf(5);
            if (intField2 == Integer.MIN_VALUE) {
                intField2 = 5;
            }
            hashMap.put(valueOf2, Integer.valueOf(intField2));
        }
    }

    protected void configMediaRecorder(MediaRecorder mediaRecorder) {
        if (VERSION.SDK_INT >= 23 && "slow".equals(this.mHfr)) {
            int i = 0;
            String str = null;
            try {
                str = sProxy.getVideoHighFrameRate(this.mParameters);
                i = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                Log.e("VideoQcom", "Invalid hfr(" + str + ")");
            }
            if (i > 0) {
                Log.i("VideoQcom", "Setting capture-rate = " + i);
                mediaRecorder.setCaptureRate((double) i);
            }
            Log.i("VideoQcom", "Setting target fps = " + 30);
            mediaRecorder.setVideoFrameRate(30);
            int i2 = this.mProfile.videoBitRate;
            if (!(!Device.IS_MI4 ? Device.IsVideoFrameRate() : true)) {
                i2 = (this.mProfile.videoBitRate * 30) / this.mProfile.videoFrameRate;
            }
            Log.i("VideoQcom", "Scaled Video bitrate : " + i2);
            mediaRecorder.setVideoEncodingBitRate(i2);
        }
    }

    protected boolean isShowHFRDuration() {
        return VERSION.SDK_INT < 23;
    }

    protected void updateVideoParametersPreference() {
        super.updateVideoParametersPreference();
        int[] maxPreviewFpsRange = CameraSettings.getMaxPreviewFpsRange(this.mParameters);
        if ((Device.IS_MI4 || Device.IsVideoFrameRate()) && maxPreviewFpsRange.length > 0) {
            this.mParameters.setPreviewFpsRange(maxPreviewFpsRange[0], maxPreviewFpsRange[1]);
        } else {
            this.mParameters.setPreviewFrameRate(this.mProfile.videoFrameRate);
        }
        if (Device.isSupportedAoHDR()) {
            sProxy.setVideoHDR(this.mParameters, this.mMutexModePicker.isAoHdr() ? "on" : "off");
        }
        if (Device.isSupportedVideoQuality4kUHD()) {
            this.mParameters.set("preview-format", CameraSettings.is4KHigherVideoQuality(this.mQuality) ? "nv12-venus" : "yuv420sp");
        }
        if (Device.isSupportedHFR()) {
            String str = !"slow".equals(this.mHfr) ? "off" : VIDEO_HIGH_FRAME_RATE;
            if (BaseModule.isSupported(str, sProxy.getSupportedVideoHighFrameRateModes(this.mParameters))) {
                Log.v("VideoQcom", "HighFrameRate value =" + str);
                sProxy.setVideoHighFrameRate(this.mParameters, str);
            }
        }
        if (sProxy.getSupportedDenoiseModes(this.mParameters) != null) {
            sProxy.setDenoise(this.mParameters, "denoise-on");
        }
        sProxy.setFaceWatermark(this.mParameters, false);
    }
}
