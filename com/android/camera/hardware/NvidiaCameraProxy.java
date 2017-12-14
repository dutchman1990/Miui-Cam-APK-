package com.android.camera.hardware;

import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.text.TextUtils;
import android.util.Log;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.hardware.CameraHardwareProxy.CameraMetaDataCallback;
import java.util.List;

public class NvidiaCameraProxy extends CameraHardwareProxy {
    private static String areaListToString(List<Area> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        int size = list.size();
        StringBuilder stringBuilder = new StringBuilder(256);
        for (int i = 0; i < size; i++) {
            Area area = (Area) list.get(i);
            stringBuilder.append("(");
            stringBuilder.append(area.rect.left);
            stringBuilder.append(",");
            stringBuilder.append(area.rect.top);
            stringBuilder.append(",");
            stringBuilder.append(area.rect.right);
            stringBuilder.append(",");
            stringBuilder.append(area.rect.bottom);
            stringBuilder.append(",");
            stringBuilder.append(area.weight);
            stringBuilder.append(")");
            if (i != size - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public void cancelContinuousMode(Camera camera) {
    }

    public void clearExposureTime(Parameters parameters) {
        setExposureTime(parameters, 0);
    }

    public boolean getAohdrEnable(Parameters parameters) {
        return Boolean.valueOf(parameters.get("nv-aohdr-enable")).booleanValue();
    }

    public boolean getAutoRotation(Parameters parameters) {
        return Boolean.valueOf(parameters.get("nv-auto-rotation")).booleanValue();
    }

    public String getISOValue(Parameters parameters) {
        return parameters.get("nv-picture-iso");
    }

    public int getNSLNumBuffers(Parameters parameters) {
        return parameters.getInt("nv-nsl-num-buffers");
    }

    public List<String> getNormalFlashModes(Parameters parameters) {
        return (getAohdrEnable(parameters) && TextUtils.isEmpty(parameters.get("flash-mode-values"))) ? CameraHardwareProxy.split("off,on,auto,red-eye,torch") : parameters.getSupportedFlashModes();
    }

    public int getNvExposureTime(Parameters parameters) {
        Object obj = parameters.get("nv-exposure-time");
        return TextUtils.isEmpty(obj) ? 0 : Integer.parseInt(obj);
    }

    public boolean getPreviewPauseDisabled(Parameters parameters) {
        return Boolean.valueOf(parameters.get("nv-disable-preview-pause")).booleanValue();
    }

    public List<String> getSupportedFocusModes(Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (!(supportedFocusModes == null || CameraSettings.isFrontCamera())) {
            if (!Util.isSupported("manual", supportedFocusModes)) {
                supportedFocusModes.add("manual");
            }
            if (!Util.isSupported("lock", supportedFocusModes)) {
                supportedFocusModes.add("lock");
            }
        }
        return supportedFocusModes;
    }

    public List<String> getSupportedIsoValues(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("nv-picture-iso-values"));
    }

    public int getWBCurrentCCT(Camera camera) {
        return 0;
    }

    public boolean isFrontMirror(Parameters parameters) {
        return "horizontal".equals(parameters.get("nv-flip-still"));
    }

    public Camera openCamera(int i) {
        return null;
    }

    public void setAohdrEnable(Parameters parameters, boolean z) {
        parameters.set("nv-aohdr-enable", Boolean.toString(z));
    }

    public void setAutoRotation(Parameters parameters, boolean z) {
        parameters.set("nv-auto-rotation", Boolean.toString(z));
    }

    public void setBurstCount(Parameters parameters, int i) {
        parameters.set("nv-burst-picture-count", Integer.toString(i));
    }

    public void setColorTemperature(Parameters parameters, int i) {
        parameters.set("nv-awb-cct-range", i + "," + i);
    }

    public void setContrast(Parameters parameters, String str) {
        parameters.set("nv-contrast", str);
    }

    public void setEdgeEnhancement(Parameters parameters, int i) {
        parameters.set("nv-edge-enhancement", Integer.toString(i));
    }

    public void setExposureTime(Parameters parameters, int i) {
        parameters.set("nv-exposure-time", Integer.toString(i));
    }

    public void setFlipStill(Parameters parameters, String str) {
        parameters.set("nv-flip-still", str);
    }

    public void setFocusAreas(Parameters parameters, List<Area> list) {
        if (list != null && list.size() > 0) {
            for (Area area : list) {
                if (!this.mHalCoordinate.contains(area.rect)) {
                    Log.e("Camera", "setFocusAreas fail :" + area.rect);
                    return;
                }
            }
        }
        String areaListToString = areaListToString(list);
        if (areaListToString != null) {
            parameters.set("focus-areas", areaListToString);
        }
    }

    public void setFocusMode(Parameters parameters, String str) {
        if ("manual".equals(str)) {
            str = "auto";
            setFocusPosition(parameters, CameraSettings.getFocusPosition());
        } else if ("lock".equals(str)) {
            str = "auto";
        }
        parameters.setFocusMode(str);
    }

    public void setFocusPosition(Parameters parameters, int i) {
        parameters.set("nv-focus-position", Integer.toString(i));
    }

    public void setHandNight(Parameters parameters, boolean z) {
        parameters.set("hand-night", Boolean.toString(z));
    }

    public void setISOValue(Parameters parameters, String str) {
        parameters.set("nv-picture-iso", str);
    }

    public void setMetadataCb(Camera camera, CameraMetaDataCallback cameraMetaDataCallback) {
    }

    public void setMeteringAreas(Parameters parameters, List<Area> list) {
        if (list != null && list.size() > 0) {
            for (Area area : list) {
                if (!this.mHalCoordinate.contains(area.rect)) {
                    Log.e("Camera", "setMeteringAreas fail :" + area.rect);
                    return;
                }
            }
        }
        String areaListToString = areaListToString(list);
        if (areaListToString != null) {
            parameters.set("metering-areas", areaListToString);
        }
    }

    public void setMorphoHDR(Parameters parameters, boolean z) {
        parameters.set("nv-still-hdr-morpho", Boolean.toString(z));
    }

    public void setNSLBurstCount(Parameters parameters, int i) {
        parameters.set("nv-nsl-burst-picture-count", Integer.toString(i));
    }

    public void setNSLNumBuffers(Parameters parameters, int i) {
        parameters.set("nv-nsl-num-buffers", Integer.toString(i));
    }

    public boolean setNVShotMode(Parameters parameters, String str) {
        if (str == null) {
            return false;
        }
        if (str.equals("shot2shot")) {
            parameters.set("nv-capture-mode", "shot2shot");
            return true;
        } else if (!str.equals("normal")) {
            return false;
        } else {
            parameters.set("nv-capture-mode", "normal");
            return true;
        }
    }

    public void setParameters(Camera camera, Parameters parameters) {
    }

    public void setPreviewPauseDisabled(Parameters parameters, boolean z) {
        parameters.set("nv-disable-preview-pause", Boolean.toString(z));
    }

    public void setRawDumpFlag(Parameters parameters, int i) {
        parameters.set("nv-raw-dump-flag", Integer.toString(i));
    }

    public void setSaturation(Parameters parameters, int i) {
        parameters.set("nv-saturation", Integer.toString(i));
    }

    public void setWhiteBalance(Parameters parameters, String str) {
        if ("manual".equals(str)) {
            setColorTemperature(parameters, CameraSettings.getKValue());
        }
        parameters.setWhiteBalance(str);
    }
}
