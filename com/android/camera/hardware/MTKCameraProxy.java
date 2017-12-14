package com.android.camera.hardware;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.Device;
import com.android.camera.hardware.CameraHardwareProxy.ContinuousShotCallback;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class MTKCameraProxy extends CameraHardwareProxy {

    public interface StereoDataCallback {
    }

    private class ContinuousShotCallbackProxy implements InvocationHandler {
        private ContinuousShotCallback mContinuousCallback;

        public ContinuousShotCallbackProxy(ContinuousShotCallback continuousShotCallback) {
            this.mContinuousCallback = continuousShotCallback;
        }

        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            if (this.mContinuousCallback != null && method.getName().equals("onConinuousShotDone")) {
                this.mContinuousCallback.onContinuousShotDone(((Integer) objArr[0]).intValue());
            }
            return null;
        }
    }

    private static class SameNameCallbackProxy implements InvocationHandler {
        private Class<?> mClazz;
        private Object mRealCallbackImpl;

        public SameNameCallbackProxy(Object obj, Class<?> cls) {
            this.mRealCallbackImpl = obj;
            this.mClazz = obj.getClass();
        }

        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            Log.v("MTKCameraProxy", "invokeinvokeinvokeinvoke " + this.mClazz);
            return (this.mRealCallbackImpl == null || method != null) ? null : null;
        }
    }

    public interface StereoWarningCallback {
    }

    public void cancelContinuousMode(Camera camera) {
    }

    public void enableRaw(Camera camera, Object obj) {
        try {
            new Class[1][0] = camera.getClass();
        } catch (IllegalArgumentException e) {
            Log.e("MTKCameraProxy", "disableRawCallback IllegalArgumentException");
        }
    }

    public void enableStereoMode() {
    }

    public List<String> getNormalFlashModes(Parameters parameters) {
        String str = parameters.get("flash-mode-values");
        return (str == null || str.length() == 0) ? null : CameraHardwareProxy.split("off,on,auto,red-eye,torch");
    }

    public String getPictureFlip(Parameters parameters) {
        return parameters.get("snapshot-picture-flip");
    }

    public List<String> getSupportedAutoexposure(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("exposure-meter-values"));
    }

    public List<String> getSupportedCaptureMode(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("cap-mode-values"));
    }

    public List<String> getSupportedFocusModes(Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        supportedFocusModes.remove("manual");
        return supportedFocusModes;
    }

    public List<String> getSupportedIsoValues(Parameters parameters) {
        return CameraHardwareProxy.split(parameters.get("iso-speed-values"));
    }

    public List<Size> getSupportedStereoPictureSizes(CameraProxy cameraProxy, Parameters parameters) {
        return splitSize(cameraProxy, parameters.get("refocus-picture-size-values"));
    }

    public String getVideoHighFrameRate(Parameters parameters) {
        return parameters.get("video-hfr");
    }

    public boolean isFrontMirror(Parameters parameters) {
        return "1".equals(getPictureFlip(parameters));
    }

    public boolean isZSLMode(Parameters parameters) {
        return "on".equals(parameters.get("zsd-mode"));
    }

    public void set3dnrMode(Parameters parameters, String str) {
        parameters.set("3dnr-mode", str);
    }

    public void setAutoExposure(Parameters parameters, String str) {
        parameters.set("exposure-meter", str);
    }

    public void setBurstShotNum(Parameters parameters, int i) {
        parameters.set("burst-num", i);
    }

    public void setBurstShotSpeed(Camera camera, int i) {
    }

    public void setCameraMode(Parameters parameters, int i) {
        parameters.set("mtk-cam-mode", i);
    }

    public void setCaptureMode(Parameters parameters, String str) {
        parameters.set("cap-mode", str);
    }

    public void setContinuousShotCallback(Camera camera, ContinuousShotCallback continuousShotCallback) {
        String continuousShotCallbackClass = Device.getContinuousShotCallbackClass();
        String continuousShotCallbackSetter = Device.getContinuousShotCallbackSetter();
        if (continuousShotCallbackClass == null || continuousShotCallbackSetter == null) {
            Log.w("MTKCameraProxy", "Insufficient continuous shot callback info[class:" + continuousShotCallbackClass + " setter:" + continuousShotCallbackSetter + "]");
            continuousShotCallbackClass = "ContinuousShotCallback";
            continuousShotCallbackSetter = "setContinuousShotCallback";
        }
        if (continuousShotCallback != null) {
            try {
                Object newProxyInstance = Proxy.newProxyInstance(Class.forName("android.hardware.Camera$" + continuousShotCallbackClass).getClassLoader(), new Class[]{r0}, new ContinuousShotCallbackProxy(continuousShotCallback));
            } catch (Throwable e) {
                Log.e("MTKCameraProxy", "IllegalArgumentException", e);
                return;
            } catch (Throwable e2) {
                Log.e("MTKCameraProxy", "ClassNotFoundException", e2);
                return;
            }
        }
        new Class[1][0] = camera.getClass();
        StringBuilder append = new StringBuilder().append("(Landroid/hardware/Camera$").append(continuousShotCallbackClass).append(";)V");
    }

    public void setContrast(Parameters parameters, String str) {
        parameters.set("contrast", str);
    }

    public void setEnlargeEye(Parameters parameters, String str) {
        if ("off".equals(str)) {
            parameters.remove("fb-enlarge-eye");
        } else {
            parameters.set("fb-enlarge-eye", str);
        }
    }

    public void setExtremeBeauty(Parameters parameters, String str) {
        parameters.set("fb-extreme-beauty", str);
    }

    public void setFaceBeauty(Parameters parameters, String str) {
        parameters.set("face-beauty", str);
    }

    public void setFacePosition(Parameters parameters, String str) {
        parameters.set("fb-face-pos", str);
    }

    public void setISOValue(Parameters parameters, String str) {
        parameters.set("iso-speed", str);
    }

    public void setPictureFlip(Parameters parameters, String str) {
        parameters.set("snapshot-picture-flip", str);
    }

    public void setSaturation(Parameters parameters, String str) {
        parameters.set("saturation", str);
    }

    public void setSharpness(Parameters parameters, String str) {
        parameters.set("edge", str);
    }

    public void setSkinColor(Parameters parameters, String str) {
        if ("off".equals(str)) {
            parameters.remove("fb-skin-color");
        } else {
            parameters.set("fb-skin-color", str);
        }
    }

    public void setSlimFace(Parameters parameters, String str) {
        if ("off".equals(str)) {
            parameters.remove("fb-slim-face");
        } else {
            parameters.set("fb-slim-face", str);
        }
    }

    public void setSlowMotion(Parameters parameters, String str) {
        parameters.set("slow-motion", str);
    }

    public void setSmoothLevel(Parameters parameters, String str) {
        parameters.set("fb-smooth-level", str);
    }

    public void setStereoDataCallback(Camera camera, Object obj) {
        Log.v("MTKCameraProxy", "setStereoDataCallback");
        if (Device.isSupportedStereo()) {
            try {
                Log.v("MTKCameraProxy", "setStereoDataCallback 366");
                if (obj != null) {
                    Object newProxyInstance = Proxy.newProxyInstance(Class.forName("android.hardware.Camera$StereoCameraDataCallback").getClassLoader(), new Class[]{r0}, new SameNameCallbackProxy(obj, StereoDataCallback.class));
                }
                new Class[1][0] = camera.getClass();
                Log.v("MTKCameraProxy", "setStereoDataCallback 375");
                Log.v("MTKCameraProxy", "setStereoDataCallback 378");
            } catch (Throwable e) {
                Log.e("MTKCameraProxy", "IllegalArgumentException", e);
            } catch (Throwable e2) {
                Log.e("MTKCameraProxy", "ClassNotFoundException", e2);
            }
        }
    }

    public void setStereoWarningCallback(Camera camera, Object obj) {
        Log.v("MTKCameraProxy", "setStereoWarningCallback");
        if (Device.isSupportedStereo() && obj != null) {
            try {
                Object newProxyInstance = Proxy.newProxyInstance(Class.forName("android.hardware.Camera$StereoCameraWarningCallback").getClassLoader(), new Class[]{r0}, new SameNameCallbackProxy(obj, StereoWarningCallback.class));
            } catch (Throwable e) {
                Log.e("MTKCameraProxy", "IllegalArgumentException", e);
            } catch (Throwable e2) {
                Log.e("MTKCameraProxy", "ClassNotFoundException", e2);
            }
        }
    }

    public void setVideoHighFrameRate(Parameters parameters, String str) {
        parameters.set("video-hfr", str);
    }

    public void setVsDofLevel(Parameters parameters, String str) {
        parameters.set("stereo-dof-level", str);
    }

    public void setVsDofMode(Parameters parameters, boolean z) {
        parameters.set("stereo-vsdof-mode", z ? "on" : "off");
        parameters.set("stereo-image-refocus", z ? "on" : "off");
        parameters.set("stereo-denoise-mode", "off");
    }

    public void setZSLMode(Parameters parameters, String str) {
        parameters.set("zsd-mode", str);
    }
}
