package com.android.camera.hardware;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.aosp_porting.ReflectUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class CameraHardwareProxy {
    private static CameraHardwareProxy sProxy;
    protected Rect mHalCoordinate = new Rect(-1000, -1000, 1000, 1000);

    public interface CameraMetaDataCallback {
        void onCameraMetaData(byte[] bArr, Camera camera);
    }

    public interface ContinuousShotCallback {
        void onContinuousShotDone(int i);
    }

    public static class CameraHardwareFace {
        public float ageFemale;
        public float ageMale;
        public float beautyscore;
        public int blinkDetected = 0;
        public int faceRecognised = 0;
        public int faceType = 0;
        public float gender;
        public int id = -1;
        public Point leftEye = null;
        public Point mouth = null;
        public float prob;
        public Rect rect;
        public Point rightEye = null;
        public int score;
        public int smileDegree = 0;
        public int smileScore = 0;
        public int t2tStop = 0;

        public static CameraHardwareFace[] convertCameraHardwareFace(Face[] faceArr) {
            CameraHardwareFace[] cameraHardwareFaceArr = new CameraHardwareFace[faceArr.length];
            for (int i = 0; i < faceArr.length; i++) {
                cameraHardwareFaceArr[i] = new CameraHardwareFace();
                copyFace(cameraHardwareFaceArr[i], faceArr[i]);
            }
            return cameraHardwareFaceArr;
        }

        private static void copyFace(CameraHardwareFace cameraHardwareFace, Face face) {
            for (Field field : face.getClass().getFields()) {
                try {
                    cameraHardwareFace.getClass().getField(field.getName()).set(cameraHardwareFace, field.get(face));
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e2) {
                } catch (NoSuchFieldException e3) {
                }
            }
        }
    }

    private static class CameraMetaDataCallbackProxy implements InvocationHandler {
        private CameraMetaDataCallback mMetaDataCallback;

        public CameraMetaDataCallbackProxy(CameraMetaDataCallback cameraMetaDataCallback) {
            this.mMetaDataCallback = cameraMetaDataCallback;
        }

        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            if (this.mMetaDataCallback != null && method.getName().equals("onCameraMetaData")) {
                this.mMetaDataCallback.onCameraMetaData((byte[]) objArr[0], (Camera) objArr[1]);
            }
            return null;
        }
    }

    public static synchronized CameraHardwareProxy getDeviceProxy() {
        CameraHardwareProxy cameraHardwareProxy;
        synchronized (CameraHardwareProxy.class) {
            if (sProxy == null) {
                if (Device.isQcomPlatform()) {
                    sProxy = new QcomCameraProxy();
                } else if (Device.isLCPlatform()) {
                    sProxy = new LCCameraProxy();
                } else if (Device.isNvPlatform()) {
                    sProxy = new NvidiaCameraProxy();
                } else if (Device.isMTKPlatform()) {
                    sProxy = new MTKCameraProxy();
                } else {
                    sProxy = new CameraHardwareProxy();
                }
            }
            cameraHardwareProxy = sProxy;
        }
        return cameraHardwareProxy;
    }

    protected static ArrayList<String> split(String str) {
        if (str == null) {
            return null;
        }
        Iterable<String> simpleStringSplitter = new SimpleStringSplitter(',');
        simpleStringSplitter.setString(str);
        ArrayList<String> arrayList = new ArrayList();
        for (String add : simpleStringSplitter) {
            arrayList.add(add);
        }
        return arrayList;
    }

    private Size strToSize(CameraProxy cameraProxy, String str) {
        if (str == null) {
            return null;
        }
        int indexOf = str.indexOf(120);
        if (indexOf != -1) {
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1);
            Camera camera = cameraProxy.getCamera();
            camera.getClass();
            return new Size(camera, Integer.parseInt(substring), Integer.parseInt(substring2));
        }
        Log.e("CameraHardwareProxy", "Invalid size parameter string=" + str);
        return null;
    }

    public void cancelContinuousMode(Camera camera) {
    }

    public void clearExposureTime(Parameters parameters) {
    }

    public void enableRaw(Camera camera, Object obj) {
    }

    public List<String> getNormalFlashModes(Parameters parameters) {
        return parameters.getSupportedFlashModes();
    }

    public int getRotation(Parameters parameters) {
        String str = parameters.get("rotation");
        return str == null ? -1 : Integer.parseInt(str);
    }

    public String getStillBeautify(Parameters parameters) {
        return parameters.get("xiaomi-still-beautify-values");
    }

    public List<String> getSupportedFocusModes(Parameters parameters) {
        return parameters.getSupportedFocusModes();
    }

    public List<String> getSupportedIsoValues(Parameters parameters) {
        return new ArrayList();
    }

    public List<Size> getSupportedPreviewSizes(Parameters parameters) {
        Iterable<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Size> arrayList = new ArrayList();
        if (supportedPreviewSizes != null) {
            for (Size size : supportedPreviewSizes) {
                if (size.width <= Util.sWindowHeight && size.height <= Util.sWindowWidth) {
                    arrayList.add(size);
                }
            }
        }
        return arrayList;
    }

    public List<String> getSupportedWhiteBalance(Parameters parameters) {
        return parameters.getSupportedWhiteBalance();
    }

    public String getTimeWatermark(Parameters parameters) {
        return parameters.get("xiaomi-time-watermark");
    }

    public String getVideoHighFrameRate(Parameters parameters) {
        return "off";
    }

    public int getWBCurrentCCT(Camera camera) {
        return 0;
    }

    public boolean isFaceWatermarkOn(Parameters parameters) {
        return !"on".equals(parameters.get("xiaomi-face-watermark")) ? "on".equals(parameters.get("watermark_age")) : true;
    }

    public boolean isFocusSuccessful(Camera camera) {
        return true;
    }

    public boolean isFrontMirror(Parameters parameters) {
        return false;
    }

    public boolean isNeedFlashOn(Camera camera) {
        Object callMethod = ReflectUtil.callMethod(camera.getClass(), camera, "getFlashOn", "()I", new Object[0]);
        return callMethod != null && ((Integer) callMethod).intValue() == 1;
    }

    public boolean isPreviewEnabled(Camera camera) {
        return false;
    }

    public boolean isZSLMode(Parameters parameters) {
        return false;
    }

    public Camera openCamera(int i) {
        return Camera.open(i);
    }

    public void setAnalyzeAgeGender(Parameters parameters, boolean z) {
        parameters.set("xiaomi-face-age-gender-analyze", z ? "on" : "off");
    }

    public void setBeautifyEnlargeEye(Parameters parameters, String str) {
        parameters.set("xiaomi-beauty-enlarge-eye", str);
    }

    public void setBeautifySkinColor(Parameters parameters, String str) {
        parameters.set("xiaomi-beauty-skin-color", str);
    }

    public void setBeautifySkinSmooth(Parameters parameters, String str) {
        parameters.set("xiaomi-beauty-skin-smooth", str);
    }

    public void setBeautifySlimFace(Parameters parameters, String str) {
        parameters.set("xiaomi-beauty-slim-face", str);
    }

    public void setBeautyRank(Parameters parameters, boolean z) {
        parameters.set("xiaomi-face-beauty-rank", z ? "on" : "off");
    }

    public void setBurstShotSpeed(Camera camera, int i) {
    }

    public void setContinuousShotCallback(Camera camera, ContinuousShotCallback continuousShotCallback) {
    }

    public void setDualCameraWatermark(Parameters parameters, String str) {
        parameters.set("xiaomi-dualcam-watermark", str);
    }

    public void setFaceWatermark(Parameters parameters, boolean z) {
        parameters.set("xiaomi-face-watermark", z ? "on" : "off");
        parameters.set("watermark_age", z ? "on" : "off");
    }

    public void setFocusAreas(Parameters parameters, List<Area> list) {
        if (list != null && list.size() > 0) {
            for (Area area : list) {
                if (!this.mHalCoordinate.contains(area.rect)) {
                    Log.e("Camera", "setFocusAreas fail :" + area.rect);
                    parameters.setFocusAreas(null);
                    return;
                }
            }
        }
        parameters.setFocusAreas(list);
    }

    public void setFocusMode(Parameters parameters, String str) {
        parameters.setFocusMode(str);
    }

    public void setHDR(Parameters parameters, String str) {
        parameters.set("mi-hdr", str);
    }

    public void setLongshotMode(Camera camera, boolean z) {
    }

    public void setMetadataCb(Camera camera, CameraMetaDataCallback cameraMetaDataCallback) {
        if (CameraSettings.isSupportedMetadata()) {
            Object obj = null;
            if (cameraMetaDataCallback != null) {
                try {
                    obj = Proxy.newProxyInstance(Class.forName("android.hardware.Camera$CameraMetaDataCallback").getClassLoader(), new Class[]{r0}, new CameraMetaDataCallbackProxy(cameraMetaDataCallback));
                } catch (Throwable e) {
                    Log.e("CameraHardwareProxy", "IllegalArgumentException", e);
                    return;
                } catch (Throwable e2) {
                    Log.e("CameraHardwareProxy", "ClassNotFoundException", e2);
                    return;
                }
            }
            ReflectUtil.callMethod(camera.getClass(), camera, "setMetadataCb", "(Landroid/hardware/Camera$CameraMetaDataCallback;)V", obj);
        }
    }

    public void setMeteringAreas(Parameters parameters, List<Area> list) {
        if (list != null && list.size() > 0) {
            for (Area area : list) {
                if (!this.mHalCoordinate.contains(area.rect)) {
                    Log.e("Camera", "setMeteringAreas fail :" + area.rect);
                    parameters.setMeteringAreas(null);
                    return;
                }
            }
        }
        parameters.setMeteringAreas(list);
    }

    public void setMultiFaceBeautify(Parameters parameters, String str) {
        parameters.set("xiaomi-multi-face-beautify", str);
    }

    public void setNightAntiMotion(Parameters parameters, String str) {
        parameters.set("night-anti-motion", str);
    }

    public void setNightShot(Parameters parameters, String str) {
        parameters.set("night-shot", str);
    }

    public void setOIS(Parameters parameters, boolean z) {
    }

    public void setParameters(Camera camera, Parameters parameters) {
        camera.setParameters(parameters);
        if (Util.sIsDumpLog) {
            parameters.dump();
        }
    }

    public void setStereoDataCallback(Camera camera, Object obj) {
    }

    public void setStereoWarningCallback(Camera camera, Object obj) {
    }

    public void setStillBeautify(Parameters parameters, String str) {
        parameters.set("xiaomi-still-beautify-values", str);
    }

    public void setTimeWatermark(Parameters parameters, String str) {
        parameters.set("xiaomi-time-watermark", str);
        parameters.set("watermark", str);
    }

    public void setTimeWatermarkValue(Parameters parameters, String str) {
        parameters.set("xiaomi-time-watermark-value", str);
        parameters.set("watermark_value", str);
    }

    public void setWhiteBalance(Parameters parameters, String str) {
        parameters.setWhiteBalance(str);
    }

    public void setZSLMode(Parameters parameters, String str) {
    }

    protected ArrayList<Size> splitSize(CameraProxy cameraProxy, String str) {
        if (str == null) {
            return null;
        }
        Iterable<String> simpleStringSplitter = new SimpleStringSplitter(',');
        simpleStringSplitter.setString(str);
        ArrayList<Size> arrayList = new ArrayList();
        for (String strToSize : simpleStringSplitter) {
            Size strToSize2 = strToSize(cameraProxy, strToSize);
            if (strToSize2 != null) {
                arrayList.add(strToSize2);
            }
        }
        return arrayList.size() == 0 ? null : arrayList;
    }

    public void startObjectTrack(Camera camera, int i, int i2, int i3, int i4) {
    }

    public void stopObjectTrack(Camera camera) {
    }
}
