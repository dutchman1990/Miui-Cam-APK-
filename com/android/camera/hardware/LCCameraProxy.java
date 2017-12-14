package com.android.camera.hardware;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import java.util.List;

public class LCCameraProxy extends CameraHardwareProxy {
    public String getPictureFlip(Parameters parameters) {
        return parameters.get("snapshot-picture-flip");
    }

    public List<String> getSupportedAutoexposure(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("metering-values"));
    }

    public List<String> getSupportedIsoValues(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("iso-mode-values"));
    }

    public boolean getZslSupported(Parameters parameters) {
        return "true".equals(parameters.get("zsl-supported"));
    }

    public boolean isFrontMirror(Parameters parameters) {
        return "1".equals(getPictureFlip(parameters));
    }

    public boolean isNeedFlashOn(Camera camera) {
        return false;
    }

    public boolean isPreviewEnabled(Camera camera) {
        return false;
    }

    public boolean isZSLMode(Parameters parameters) {
        return "true".equals(parameters.get("zsl"));
    }

    public void setAutoExposure(Parameters parameters, String str) {
        parameters.set("metering", str);
    }

    public void setBurstShotNum(Parameters parameters, int i) {
        parameters.set("zsl-num", i);
    }

    public void setContrast(Parameters parameters, String str) {
        parameters.set("contrast", str);
    }

    public void setISOValue(Parameters parameters, String str) {
        parameters.set("iso", str);
    }

    public void setPictureFlip(Parameters parameters, String str) {
        parameters.set("snapshot-picture-flip", str);
    }

    public void setSaturation(Parameters parameters, String str) {
        parameters.set("saturation", str);
    }

    public void setSharpness(Parameters parameters, String str) {
        parameters.set("sharpness", str);
    }

    public void setZSLMode(Parameters parameters, String str) {
        parameters.set("zsl", str);
    }
}
