package com.android.camera.hardware;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.aosp_porting.FeatureParser;
import com.android.camera.aosp_porting.ReflectUtil;
import java.util.ArrayList;
import java.util.List;

public class QcomCameraProxy extends CameraHardwareProxy {
    private String getWBCurrentCCT(Parameters parameters) {
        return parameters.get("wb-manual-cct");
    }

    private void setFocusPosition(Parameters parameters, int i, int i2) {
        parameters.set("manual-focus-pos-type", Integer.toString(i));
        parameters.set("manual-focus-position", Integer.toString(i2));
    }

    public void clearExposureTime(Parameters parameters) {
        setExposureTime(parameters, 0);
    }

    public String getChromaFlash(Parameters parameters) {
        return parameters.get("chroma-flash");
    }

    public String getExposureTime(Parameters parameters) {
        return parameters.get("exposure-time");
    }

    public boolean getInternalPreviewSupported(Parameters parameters) {
        return "true".equals(parameters.get("internal-restart"));
    }

    public int getMaxContrast(Parameters parameters) {
        return parameters.getInt("max-contrast");
    }

    public int getMaxExposureTimeValue(Parameters parameters) {
        String str = parameters.get("max-exposure-time");
        return (str == null || str.length() == 0) ? 0 : Device.isFloatExposureTime() ? (int) (Double.parseDouble(str) * 1000.0d) : Integer.parseInt(str);
    }

    public int getMaxSaturation(Parameters parameters) {
        return parameters.getInt("max-saturation");
    }

    public int getMaxSharpness(Parameters parameters) {
        return parameters.getInt("max-sharpness");
    }

    public int getMinExposureTimeValue(Parameters parameters) {
        String str = parameters.get("min-exposure-time");
        return (str == null || str.length() == 0) ? 0 : Device.isFloatExposureTime() ? (int) (Double.parseDouble(str) * 1000.0d) : Integer.parseInt(str);
    }

    public String getPictureFlip(Parameters parameters) {
        return parameters.get("snapshot-picture-flip");
    }

    public List<String> getSupportedAutoexposure(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("auto-exposure-values"));
    }

    public List<String> getSupportedDenoiseModes(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("denoise-values"));
    }

    public List<String> getSupportedIsoValues(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("iso-values"));
    }

    public List<Size> getSupportedPortraitPictureSizes(CameraProxy cameraProxy, Parameters parameters) {
        return splitSize(cameraProxy, parameters.get("bokeh-picture-size"));
    }

    public List<String> getSupportedTouchAfAec(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("touch-af-aec-values"));
    }

    public List<String> getSupportedVideoHighFrameRateModes(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("video-hfr-values"));
    }

    public List<String> getSupportedWhiteBalance(Parameters parameters) {
        List<String> supportedWhiteBalance = parameters.getSupportedWhiteBalance();
        if (supportedWhiteBalance != null && (CameraSettings.isFrontCamera() || !Device.isSupportedManualFunction())) {
            supportedWhiteBalance.remove("manual");
            supportedWhiteBalance.remove("manual-cct");
        }
        return supportedWhiteBalance;
    }

    public String getUbiFocus(Parameters parameters) {
        return parameters.get("af-bracket");
    }

    public String getVideoHighFrameRate(Parameters parameters) {
        return parameters.get("video-hfr");
    }

    public int getWBCurrentCCT(Camera camera) {
        String wBCurrentCCT = getWBCurrentCCT(camera.getParameters());
        return wBCurrentCCT != null ? Integer.parseInt(wBCurrentCCT) : 0;
    }

    public boolean isFocusSuccessful(Camera camera) {
        boolean z = true;
        if (Device.IS_B3 || Device.IS_B3_PRO) {
            return "true".equals(camera.getParameters().get("focus-done"));
        }
        int integer = FeatureParser.getInteger("camera_focus_success_flag", 0);
        if (integer != 0) {
            Object callMethod = ReflectUtil.callMethod(camera.getClass(), camera, "getFocusState", "()I", new Object[0]);
            if (callMethod != null) {
                if (integer != ((Integer) callMethod).intValue()) {
                    z = false;
                }
                return z;
            }
        }
        return true;
    }

    public boolean isFrontMirror(Parameters parameters) {
        String str = parameters.get("snapshot-picture-flip");
        return !"flip-h".equals(str) ? "flip-v".equals(str) : true;
    }

    public boolean isNeedFlashOn(Camera camera) {
        return (!Device.IS_XIAOMI || Device.IS_B3 || Device.IS_B3_PRO) ? "true".equals(camera.getParameters().get("flash-on")) : super.isNeedFlashOn(camera);
    }

    public boolean isZSLHDRSupported(Parameters parameters) {
        String str = parameters.get("zsl-hdr-supported");
        return str != null && "true".equals(str);
    }

    public boolean isZSLMode(Parameters parameters) {
        return "on".equals(parameters.get("zsl"));
    }

    public Camera openCamera(int i) {
        Camera camera = null;
        try {
            Object callMethod = ReflectUtil.callMethod(Class.forName("android.hardware.Camera"), null, "openLegacy", "(II)Landroid/hardware/Camera;", Integer.valueOf(i), Integer.valueOf(256));
            Log.e("QcomCameraProxy", "openLegacy is " + callMethod);
            camera = callMethod == null ? null : (Camera) callMethod;
        } catch (Exception e) {
            Log.v("QcomCameraProxy", "openLegacy failed due to " + e.getMessage() + ", using open instead");
        }
        return camera == null ? super.openCamera(i) : camera;
    }

    public void setAoHDR(Parameters parameters, String str) {
        parameters.set("sensor-hdr", str);
    }

    public void setAutoExposure(Parameters parameters, String str) {
        parameters.set("auto-exposure", str);
    }

    public void setCameraMode(Parameters parameters, int i) {
        parameters.set("camera-mode", i);
    }

    public void setChromaFlash(Parameters parameters, String str) {
        parameters.set("chroma-flash", str);
    }

    public void setContrast(Parameters parameters, int i) {
        if (i >= 0 && i <= parameters.getInt("max-contrast")) {
            parameters.set("contrast", String.valueOf(i));
        }
    }

    public void setDenoise(Parameters parameters, String str) {
        parameters.set("denoise", str);
    }

    public void setExposureTime(Parameters parameters, int i) {
        if (Device.isFloatExposureTime()) {
            parameters.set("exposure-time", Double.toString(((double) i) / 1000.0d));
        } else {
            parameters.set("exposure-time", Integer.toString(i));
        }
    }

    public void setFaceDetectionMode(Parameters parameters, String str) {
        parameters.set("face-detection", str);
    }

    public void setFocusMode(Parameters parameters, String str) {
        if ("manual".equals(str)) {
            setFocusPosition(parameters, CameraSettings.getFocusPosition());
        } else if ("lock".equals(str)) {
            str = "auto";
        }
        parameters.setFocusMode(str);
    }

    public void setFocusPosition(Parameters parameters, int i) {
        setFocusPosition(parameters, 2, (1000 - i) / 10);
    }

    public void setHDR(Parameters parameters, String str) {
        super.setHDR(parameters, str);
        if ("true".equals(str)) {
            setMorphoHDR(parameters, true);
            if (!Device.isNewHdrParamKeyUsed()) {
                parameters.set("ae-bracket-hdr", "AE-Bracket");
                parameters.set("capture-burst-exposures", "-6,8,0");
            }
        }
    }

    public void setHandNight(Parameters parameters, boolean z) {
        parameters.set("morpho-hht", Boolean.toString(z));
    }

    public void setISOValue(Parameters parameters, String str) {
        parameters.set("iso", str);
    }

    public void setLongshotMode(Camera camera, boolean z) {
        ReflectUtil.callMethod(camera.getClass(), camera, "setLongshot", "(Z)V", Boolean.valueOf(z));
    }

    public void setMorphoHDR(Parameters parameters, boolean z) {
        parameters.set("morpho-hdr", Boolean.toString(z));
    }

    public void setMultiFaceBeautify(Parameters parameters, String str) {
        parameters.set("xiaomi-multi-face-beautify", str);
    }

    public void setNightAntiMotion(Parameters parameters, String str) {
        super.setNightAntiMotion(parameters, str);
        if ("true".equals(str)) {
            setHandNight(parameters, true);
            if ((Device.IS_XIAOMI || Device.IS_HM3LTE || Device.IS_H2XLTE) && !Device.isNewHdrParamKeyUsed()) {
                parameters.set("ae-bracket-hdr", "AE-Bracket");
                parameters.set("capture-burst-exposures", "0");
            }
        }
    }

    public void setNightShot(Parameters parameters, String str) {
        super.setNightShot(parameters, str);
        if ("true".equals(str)) {
            setHandNight(parameters, true);
            if ((Device.IS_XIAOMI || Device.IS_HM3LTE || Device.IS_H2XLTE) && !Device.isNewHdrParamKeyUsed()) {
                parameters.set("ae-bracket-hdr", "AE-Bracket");
                parameters.set("capture-burst-exposures", "0,0,0");
            }
        }
    }

    public void setOIS(Parameters parameters, boolean z) {
        String str = z ? "enable" : "disable";
        ArrayList split = CameraHardwareProxy.split(parameters.get("ois-values"));
        if (split != null && split.contains(str)) {
            parameters.set("ois", str);
        }
    }

    public void setPictureFlip(Parameters parameters, String str) {
        parameters.set("snapshot-picture-flip", str);
    }

    public void setPortraitMode(Parameters parameters, String str) {
        parameters.set("xiaomi-portrait-mode", str);
    }

    public void setSaturation(Parameters parameters, int i) {
        if (i >= 0 && i <= parameters.getInt("max-saturation")) {
            parameters.set("saturation", String.valueOf(i));
        }
    }

    public void setSharpness(Parameters parameters, int i) {
        if (i >= 0 && i <= parameters.getInt("max-sharpness")) {
            parameters.set("sharpness", String.valueOf(i));
        }
    }

    public void setTouchAfAec(Parameters parameters, String str) {
        parameters.set("touch-af-aec", str);
    }

    public void setUbiFocus(Parameters parameters, String str) {
        parameters.set("af-bracket", str);
    }

    public void setVideoHDR(Parameters parameters, String str) {
        parameters.set("video-hdr", str);
    }

    public void setVideoHighFrameRate(Parameters parameters, String str) {
        parameters.set("video-hfr", str);
    }

    public void setWBManualCCT(Parameters parameters, int i) {
        parameters.set("manual-wb-type", 0);
        parameters.set("manual-wb-value", i);
    }

    public void setWhiteBalance(Parameters parameters, String str) {
        if ("manual".equals(str)) {
            setWBManualCCT(parameters, CameraSettings.getKValue());
        } else if ("measure".equals(str)) {
            str = "auto";
        }
        super.setWhiteBalance(parameters, str);
    }

    public void setZSLMode(Parameters parameters, String str) {
        parameters.set("zsl", str);
    }

    public void startObjectTrack(Camera camera, int i, int i2, int i3, int i4) {
        Log.v("QcomCameraProxy", "startObjectTrack left=" + i + " top=" + i2 + " width=" + i3 + " height=" + i4);
        ReflectUtil.callMethod(camera.getClass(), camera, "startTrack", "(IIII)V", Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Integer.valueOf(i4));
    }

    public void stopObjectTrack(Camera camera) {
        Log.v("QcomCameraProxy", "stopObjectTrack");
        ReflectUtil.callMethod(camera.getClass(), camera, "stopTrack", "()V", new Object[0]);
    }
}
