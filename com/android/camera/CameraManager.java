package com.android.camera;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.CameraHardwareProxy.CameraMetaDataCallback;
import com.android.camera.hardware.CameraHardwareProxy.ContinuousShotCallback;
import java.io.IOException;
import java.util.ConcurrentModificationException;

public class CameraManager {
    private static CameraManager sCameraManager = new CameraManager();
    private Camera mCamera;
    private volatile boolean mCameraError;
    private Handler mCameraHandler;
    private CameraProxy mCameraProxy;
    private boolean mFlashOn;
    private boolean mFocusSuccessful;
    private Parameters mParameters;
    private boolean mPreviewEnable;
    private CameraHardwareProxy mProxy;
    private IOException mReconnectException;
    private ConditionVariable mSig = new ConditionVariable();
    private int mWBCT;

    private class CameraHandler extends Handler {
        CameraHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            CharSequence charSequence;
            int i;
            int i2;
            try {
                switch (message.what) {
                    case 1:
                        CameraManager.this.mCamera.release();
                        CameraManager.this.mCamera = null;
                        CameraManager.this.mCameraProxy = null;
                        break;
                    case 2:
                        CameraManager.this.mReconnectException = null;
                        try {
                            CameraManager.this.mCamera.reconnect();
                            break;
                        } catch (IOException e) {
                            CameraManager.this.mReconnectException = e;
                            break;
                        }
                    case 3:
                        CameraManager.this.mCamera.unlock();
                        break;
                    case 4:
                        CameraManager.this.mCamera.lock();
                        break;
                    case 5:
                        CameraManager.this.mCamera.setPreviewTexture((SurfaceTexture) message.obj);
                        break;
                    case 6:
                        CameraManager.this.mCamera.startPreview();
                        return;
                    case 7:
                        CameraManager.this.mCamera.stopPreview();
                        break;
                    case 8:
                        CameraManager.this.mCamera.setPreviewCallbackWithBuffer((PreviewCallback) message.obj);
                        break;
                    case 9:
                        CameraManager.this.mCamera.addCallbackBuffer((byte[]) message.obj);
                        break;
                    case 10:
                        CameraManager.this.mCamera.autoFocus((AutoFocusCallback) message.obj);
                        break;
                    case 11:
                        charSequence = "cancelAutoFocus failed";
                        CameraManager.this.mCamera.cancelAutoFocus();
                        break;
                    case 12:
                        CameraManager.this.mCamera.setAutoFocusMoveCallback((AutoFocusMoveCallback) message.obj);
                        break;
                    case 13:
                        CameraManager.this.mCamera.setDisplayOrientation(message.arg1);
                        break;
                    case 14:
                        CameraManager.this.mCamera.setZoomChangeListener((OnZoomChangeListener) message.obj);
                        break;
                    case 15:
                        CameraManager.this.mCamera.setFaceDetectionListener((FaceDetectionListener) message.obj);
                        break;
                    case 16:
                        CameraManager.this.mCamera.startFaceDetection();
                        break;
                    case 17:
                        CameraManager.this.mCamera.stopFaceDetection();
                        break;
                    case 18:
                        CameraManager.this.mCamera.setErrorCallback((ErrorCallback) message.obj);
                        break;
                    case 19:
                        CameraManager.this.mProxy.setParameters(CameraManager.this.mCamera, (Parameters) message.obj);
                        break;
                    case 20:
                        charSequence = "getParameters failed (empty parameters)";
                        i2 = 3;
                        while (true) {
                            i = i2 - 1;
                            if (i2 <= 0) {
                                break;
                            }
                            CameraManager.this.mParameters = CameraManager.this.mCamera.getParameters();
                            break;
                        }
                        break;
                    case 21:
                        CameraManager.this.mProxy.setParameters(CameraManager.this.mCamera, (Parameters) message.obj);
                        return;
                    case 23:
                        CameraManager.this.mCamera.setOneShotPreviewCallback((PreviewCallback) message.obj);
                        break;
                    case 24:
                        CameraManager.this.mCamera.addRawImageCallbackBuffer((byte[]) message.obj);
                        break;
                    case 25:
                        CameraManager.this.mCamera.startPreview();
                        break;
                    case 100:
                        CameraManager.this.mFlashOn = CameraManager.this.mProxy.isNeedFlashOn(CameraManager.this.mCamera);
                        break;
                    case 103:
                        CameraManager.this.mWBCT = CameraManager.this.mProxy.getWBCurrentCCT(CameraManager.this.mCamera);
                        break;
                    case 104:
                        CameraManager.this.mProxy.cancelContinuousMode(CameraManager.this.mCamera);
                        break;
                    case 105:
                        CameraManager.this.mProxy.setLongshotMode(CameraManager.this.mCamera, ((Boolean) message.obj).booleanValue());
                        break;
                    case 106:
                        RectF rectF = (RectF) message.obj;
                        CameraManager.this.mProxy.startObjectTrack(CameraManager.this.mCamera, (int) rectF.left, (int) rectF.top, (int) rectF.width(), (int) rectF.height());
                        break;
                    case 107:
                        CameraManager.this.mProxy.stopObjectTrack(CameraManager.this.mCamera);
                        break;
                    case 108:
                        CameraManager.this.mProxy.setMetadataCb(CameraManager.this.mCamera, (CameraMetaDataCallback) message.obj);
                        break;
                    case 109:
                        CameraManager.this.mFocusSuccessful = CameraManager.this.mProxy.isFocusSuccessful(CameraManager.this.mCamera);
                        break;
                    case 110:
                        CameraManager.this.mPreviewEnable = CameraManager.this.mProxy.isPreviewEnabled(CameraManager.this.mCamera);
                        break;
                    case 111:
                        CameraManager.this.mProxy.setBurstShotSpeed(CameraManager.this.mCamera, message.arg1);
                        break;
                    case 112:
                        CameraManager.this.mCamera.setPreviewDisplay((SurfaceHolder) message.obj);
                        break;
                    case 113:
                        CameraManager.this.mProxy.setContinuousShotCallback(CameraManager.this.mCamera, (ContinuousShotCallback) message.obj);
                        break;
                    case 114:
                        CameraManager.this.mCamera.setPreviewCallback((PreviewCallback) message.obj);
                        break;
                    case 115:
                        CameraManager.this.mProxy.setStereoDataCallback(CameraManager.this.mCamera, message.obj);
                        break;
                    case 116:
                        CameraManager.this.mProxy.setStereoWarningCallback(CameraManager.this.mCamera, message.obj);
                        break;
                    case 117:
                        CameraManager.this.mProxy.enableRaw(CameraManager.this.mCamera, message.obj);
                        break;
                }
            } catch (Throwable e2) {
                throw new RuntimeException(e2);
            } catch (Exception e3) {
                if (!e3.getMessage().contains(charSequence) || i == 0) {
                    throw new RuntimeException(e3.getMessage());
                }
                i2 = i;
            } catch (Exception e32) {
                if (!e32.getMessage().contains(charSequence)) {
                    throw new RuntimeException(e32.getMessage());
                }
            } catch (Throwable e22) {
                throw new RuntimeException(e22);
            } catch (ConcurrentModificationException e4) {
                Log.e("CameraManager", "ConcurrentModificationException: " + e4.toString());
            } catch (RuntimeException e5) {
                boolean z = false;
                if (!(message.what == 1 || CameraManager.this.mCamera == null)) {
                    if (!CameraManager.this.mCameraError) {
                        try {
                            Log.e("CameraManager", "camera hardware state test, use getParameters, msg=" + e5.getMessage());
                            CameraManager.this.mCamera.getParameters();
                            Log.e("CameraManager", "camera hardware state is normal");
                        } catch (Throwable e6) {
                            Log.e("CameraManager", "camera hardware crashed ", e6);
                            z = true;
                        }
                    }
                    try {
                        CameraManager.this.mCamera.release();
                    } catch (Throwable e62) {
                        Log.e("CameraManager", "Fail to release the camera.", e62);
                        z = true;
                    }
                    if (z) {
                        CameraManager.this.mCameraProxy.notifyHardwareError();
                    }
                    CameraManager.this.mCamera = null;
                    CameraManager.this.mCameraProxy = null;
                }
                Log.v("CameraManager", "exception in camerahandler, mCameraError=" + CameraManager.this.mCameraError + " " + z);
                if (!(CameraManager.this.mCameraError || z)) {
                    throw e5;
                }
            }
            CameraManager.this.mSig.open();
        }
    }

    public class CameraProxy {
        private HardwareErrorListener mHardwareErrorListener;

        private CameraProxy() {
            Util.Assert(CameraManager.this.mCamera != null);
        }

        public void addCallbackBuffer(byte[] bArr) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(9, bArr).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void addRawImageCallbackBuffer(byte[] bArr) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(24, bArr).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void autoFocus(AutoFocusCallback autoFocusCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(10, autoFocusCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void cancelAutoFocus() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(11);
            CameraManager.this.mSig.block();
        }

        public void cancelPicture() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(104);
            CameraManager.this.mSig.block();
        }

        public Camera getCamera() {
            return CameraManager.this.mCamera;
        }

        public Parameters getParameters() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(20);
            CameraManager.this.mSig.block();
            return CameraManager.this.mParameters;
        }

        public int getWBCT() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(103);
            CameraManager.this.mSig.block();
            return CameraManager.this.mWBCT;
        }

        public boolean isFocusSuccessful() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(109);
            CameraManager.this.mSig.block();
            return CameraManager.this.mFocusSuccessful;
        }

        public boolean isNeedFlashOn() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(100);
            CameraManager.this.mSig.block();
            return CameraManager.this.mFlashOn;
        }

        public boolean isPreviewEnable() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(110);
            CameraManager.this.mSig.block();
            return CameraManager.this.mPreviewEnable;
        }

        public void lock() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(4);
            CameraManager.this.mSig.block();
        }

        public void notifyHardwareError() {
            Log.e("CameraManager", "mark camera error from manager notify");
            CameraManager.this.mCameraError = true;
            if (this.mHardwareErrorListener != null) {
                this.mHardwareErrorListener.notifyError();
            }
        }

        public void reconnect() throws IOException {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(2);
            CameraManager.this.mSig.block();
            if (CameraManager.this.mReconnectException != null) {
                throw CameraManager.this.mReconnectException;
            }
        }

        public void release() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(1);
            CameraManager.this.mSig.block();
            setHardwareListener(null);
        }

        public void removeAllAsyncMessage() {
            CameraManager.this.mCameraHandler.removeMessages(21);
            CameraManager.this.mCameraHandler.removeMessages(6);
        }

        public void setAutoFocusMoveCallback(AutoFocusMoveCallback autoFocusMoveCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(12, autoFocusMoveCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setBurstShotSpeed(int i) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(111, i, 0).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setCameraError() {
            Log.e("CameraManager", "mark camera error from callback");
            CameraManager.this.mCameraError = true;
        }

        public void setContinuousShotCallback(ContinuousShotCallback continuousShotCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(113, continuousShotCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setDisplayOrientation(int i) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(13, i, 0).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setErrorCallback(ErrorCallback errorCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(18, errorCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setFaceDetectionListener(FaceDetectionListener faceDetectionListener) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(15, faceDetectionListener).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setHardwareListener(HardwareErrorListener hardwareErrorListener) {
            this.mHardwareErrorListener = hardwareErrorListener;
        }

        public final void setLongshotMode(boolean z) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(105, Boolean.valueOf(z)).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setMetaDataCallback(CameraMetaDataCallback cameraMetaDataCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(108, cameraMetaDataCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setOneShotPreviewCallback(PreviewCallback previewCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(23, previewCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setParameters(Parameters parameters) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(19, parameters).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setParametersAsync(Parameters parameters) {
            CameraManager.this.mCameraHandler.removeMessages(21);
            CameraManager.this.mCameraHandler.obtainMessage(21, parameters).sendToTarget();
        }

        public void setPreviewCallback(PreviewCallback previewCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(114, previewCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setPreviewCallbackWithBuffer(PreviewCallback previewCallback) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(8, previewCallback).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(112, surfaceHolder).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setPreviewTexture(SurfaceTexture surfaceTexture) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(5, surfaceTexture).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setStereoDataCallback(Object obj) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(115, obj).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setStereoWarningCallback(Object obj) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(116, obj).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void setZoomChangeListener(OnZoomChangeListener onZoomChangeListener) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(14, onZoomChangeListener).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void startFaceDetection() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(16);
            CameraManager.this.mSig.block();
        }

        public void startObjectTrack(RectF rectF) {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.obtainMessage(106, rectF).sendToTarget();
            CameraManager.this.mSig.block();
        }

        public void startPreview() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(25);
            CameraManager.this.mSig.block();
        }

        public void startPreviewAsync() {
            CameraManager.this.mCameraHandler.sendEmptyMessage(6);
        }

        public void stopFaceDetection() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(17);
            CameraManager.this.mSig.block();
        }

        public void stopObjectTrack() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(107);
            CameraManager.this.mSig.block();
        }

        public void stopPreview() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(7);
            CameraManager.this.mSig.block();
        }

        public void takePicture(ShutterCallback shutterCallback, PictureCallback pictureCallback, PictureCallback pictureCallback2, PictureCallback pictureCallback3) {
            CameraManager.this.mSig.close();
            final ShutterCallback shutterCallback2 = shutterCallback;
            final PictureCallback pictureCallback4 = pictureCallback;
            final PictureCallback pictureCallback5 = pictureCallback2;
            final PictureCallback pictureCallback6 = pictureCallback3;
            CameraManager.this.mCameraHandler.post(new Runnable() {
                public void run() {
                    if (CameraManager.this.mCamera != null) {
                        CameraManager.this.mCamera.takePicture(shutterCallback2, pictureCallback4, pictureCallback5, pictureCallback6);
                    }
                    CameraManager.this.mSig.open();
                }
            });
            CameraManager.this.mSig.block();
        }

        public void unlock() {
            CameraManager.this.mSig.close();
            CameraManager.this.mCameraHandler.sendEmptyMessage(3);
            CameraManager.this.mSig.block();
        }
    }

    public interface HardwareErrorListener {
        void notifyError();
    }

    private CameraManager() {
        HandlerThread handlerThread = new HandlerThread("Camera Handler Thread");
        handlerThread.start();
        this.mCameraHandler = new CameraHandler(handlerThread.getLooper());
        this.mProxy = CameraHardwareProxy.getDeviceProxy();
    }

    public static CameraManager instance() {
        return sCameraManager;
    }

    CameraProxy cameraOpen(int i) {
        this.mCameraError = false;
        this.mCamera = this.mProxy.openCamera(i);
        if (this.mCamera == null) {
            return null;
        }
        this.mCameraProxy = new CameraProxy();
        return this.mCameraProxy;
    }

    public CameraProxy getCameraProxy() {
        return this.mCameraProxy;
    }

    public Parameters getStashParameters() {
        return this.mParameters;
    }
}
