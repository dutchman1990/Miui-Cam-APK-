package com.android.camera;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.ui.FaceView;
import com.android.camera.ui.FocusIndicator;
import com.android.camera.ui.FocusView;
import com.android.camera.ui.FrameView;
import java.util.ArrayList;
import java.util.List;

public class FocusManager extends FocusManagerAbstract {
    private boolean mAeAwbLock;
    private long mCafStartTime;
    private Context mContext;
    private String[] mDefaultFocusModes;
    private boolean mFocusAreaSupported;
    private String mFocusMode;
    private FocusView mFocusView;
    private FrameView mFrameView;
    private Handler mHandler;
    private boolean mKeepFocusUIState;
    private int mLastFocusFrom = -1;
    private int mLastState = 0;
    private RectF mLatestFocusFace;
    private long mLatestFocusTime;
    private Listener mListener;
    private boolean mLockAeAwbNeeded;
    private boolean mMeteringAreaSupported;
    private String mOverrideFocusMode;
    private Parameters mParameters;
    private boolean mPendingMultiCapture;

    public interface Listener {
        void autoFocus();

        void cancelAutoFocus();

        boolean capture();

        boolean multiCapture();

        void playSound(int i);

        void setFocusParameters();

        void startFaceDetection();

        void stopObjectTracking(boolean z);
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                case 1:
                    FocusManager.this.cancelAutoFocus();
                    FocusManager.this.mListener.startFaceDetection();
                    return;
                default:
                    return;
            }
        }
    }

    public FocusManager(Context context, CameraSettingPreferences cameraSettingPreferences, String[] strArr, FocusView focusView, Parameters parameters, Listener listener, boolean z, Looper looper) {
        this.mHandler = new MainHandler(looper);
        this.mContext = context;
        this.mDefaultFocusModes = strArr;
        this.mFocusView = focusView;
        setParameters(parameters);
        this.mListener = listener;
        setMirror(z);
    }

    private void autoFocus(int i) {
        Log.v("FocusManager", "start autoFocus from " + i);
        this.mLastFocusFrom = i;
        if (i != 1 || (this.mFrameView instanceof FaceView)) {
            this.mListener.stopObjectTracking(false);
        }
        this.mListener.autoFocus();
        if (!(this.mFrameView == null || i == 1)) {
            this.mFrameView.pause();
        }
        setFocusState(1);
        updateFocusUI();
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    private void cancelAutoFocus() {
        resetTouchFocus();
        if (needAutoFocusCall()) {
            this.mListener.cancelAutoFocus();
        } else {
            this.mListener.setFocusParameters();
        }
        setFocusState(0);
        updateFocusUI();
        this.mCancelAutoFocusIfMove = false;
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        Log.v("FocusManager", "cancelAutoFocus");
    }

    private void capture() {
        if (this.mListener.capture()) {
            if (Device.isResetToCCAFAfterCapture()) {
                setFocusState(0);
                this.mCancelAutoFocusIfMove = false;
            }
            this.mPendingMultiCapture = false;
            this.mHandler.removeMessages(0);
        }
    }

    private void focusPoint(int i, int i2, int i3, boolean z) {
        if (this.mInitialized && this.mState != 2 && (this.mOverrideFocusMode == null || isAutoFocusMode(this.mOverrideFocusMode))) {
            if (isNeedCancelAutoFocus()) {
                cancelAutoFocus();
            }
            initializeParameters(i, i2, i3, z);
            this.mListener.setFocusParameters();
            if (!this.mFocusAreaSupported || z) {
                if (this.mMeteringAreaSupported) {
                    if (3 == i3 && isFocusValid(i3)) {
                        this.mCancelAutoFocusIfMove = true;
                    }
                    this.mLastFocusFrom = i3;
                    setFocusState(1);
                    updateFocusUI();
                    this.mHandler.removeMessages(0);
                }
            } else if (isFocusValid(i3)) {
                autoFocus(i3);
            }
        }
    }

    private int getTapAction() {
        String focusMode = getFocusMode();
        return (focusMode.equals("infinity") || focusMode.equals("edof") || focusMode.equals("fixed") || focusMode.equals("lock") || focusMode.equals("manual")) ? 1 : 2;
    }

    private void initializeFocusAreas(int i, int i2, int i3, int i4, int i5, int i6) {
        if (this.mFocusArea == null) {
            this.mFocusArea = new ArrayList();
            this.mFocusArea.add(new Area(new Rect(), 1));
        }
        calculateTapArea(i, i2, 1.0f, i3, i4, i5, i6, ((Area) this.mFocusArea.get(0)).rect);
    }

    private void initializeFocusIndicator(int i, int i2) {
        this.mFocusView.setPosition(i, i2);
    }

    private void initializeMeteringAreas(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (this.mMeteringArea == null) {
            this.mMeteringArea = new ArrayList();
            this.mMeteringArea.add(new Area(new Rect(), 1));
        }
        if (i7 != 1 || this.mFrameView.isNeedExposure()) {
            calculateTapArea(i, i2, 1.8f, i3, i4, i5, i6, ((Area) this.mMeteringArea.get(0)).rect);
            return;
        }
        this.mMeteringArea = null;
    }

    private void initializeParameters(int i, int i2, int i3, boolean z) {
        int i4 = i;
        int i5 = i2;
        if (EffectController.getInstance().isFishEye()) {
            float[] fArr = new float[]{(float) i, (float) i2};
            this.mPreviewChangeMatrix.mapPoints(fArr);
            i4 = (int) fArr[0];
            i5 = (int) fArr[1];
        }
        if (this.mFocusAreaSupported && !z) {
            initializeFocusAreas(this.FOCUS_AREA_WIDTH, this.FOCUS_AREA_HEIGHT, i4, i5, this.mPreviewWidth, this.mPreviewHeight);
        }
        if (this.mMeteringAreaSupported) {
            initializeMeteringAreas(this.FOCUS_AREA_WIDTH, this.FOCUS_AREA_HEIGHT, i4, i5, this.mPreviewWidth, this.mPreviewHeight, i3);
        }
        initializeFocusIndicator(i, i2);
    }

    private boolean isAutoFocusMode(String str) {
        return !"auto".equals(str) ? "macro".equals(str) : true;
    }

    private boolean isFocusEnabled() {
        return (!this.mInitialized || this.mState == 2 || this.mState == 1) ? false : needAutoFocusCall();
    }

    private boolean isFocusValid(int i) {
        long currentTimeMillis = System.currentTimeMillis();
        int i2 = (this.mLastFocusFrom == 3 || this.mLastFocusFrom == 4) ? 5000 : 4000;
        long j = (long) i2;
        if (i >= 3 || i >= this.mLastFocusFrom || Util.isTimeout(currentTimeMillis, this.mLatestFocusTime, j)) {
            this.mLatestFocusTime = System.currentTimeMillis();
            return true;
        }
        if (this.mLastFocusFrom == 1) {
            resetTouchFocus();
        }
        return false;
    }

    private boolean isNeedCancelAutoFocus() {
        return (this.mHandler.hasMessages(0) || this.mHandler.hasMessages(1)) ? true : this.mCancelAutoFocusIfMove;
    }

    private static boolean isSupported(String str, List<String> list) {
        return list != null && list.indexOf(str) >= 0;
    }

    private void multiCapture() {
        if (this.mListener.multiCapture()) {
            setFocusState(0);
            this.mPendingMultiCapture = false;
            this.mHandler.removeMessages(0);
        }
    }

    private boolean needAutoFocusCall() {
        return 2 == getTapAction() ? this.mFocusAreaSupported : false;
    }

    private void resetFocusAreaToCenter() {
        initializeFocusAreas(this.FOCUS_AREA_WIDTH, this.FOCUS_AREA_HEIGHT, this.mPreviewWidth / 2, this.mPreviewHeight / 2, this.mPreviewWidth, this.mPreviewHeight);
        initializeFocusIndicator(this.mPreviewWidth / 2, this.mPreviewHeight / 2);
    }

    private boolean resetFocusAreaToFaceArea() {
        if (this.mFrameView != null && this.mFrameView.faceExists()) {
            RectF focusRect = this.mFrameView.getFocusRect();
            if (focusRect != null) {
                this.mLatestFocusFace = focusRect;
                initializeFocusAreas(this.FOCUS_AREA_WIDTH, this.FOCUS_AREA_HEIGHT, (int) ((focusRect.left + focusRect.right) / 2.0f), (int) ((focusRect.top + focusRect.bottom) / 2.0f), this.mPreviewWidth, this.mPreviewHeight);
                return true;
            }
        }
        return false;
    }

    private void setFocusState(int i) {
        this.mState = i;
    }

    private void setLastFocusState(int i) {
        this.mLastState = i;
    }

    public boolean cancelMultiSnapPending() {
        if (this.mState != 2 || !this.mPendingMultiCapture) {
            return false;
        }
        this.mPendingMultiCapture = false;
        return true;
    }

    public void doMultiSnap(boolean z) {
        if (this.mInitialized) {
            if (!z) {
                multiCapture();
            }
            if (this.mState == 3 || this.mState == 4 || !needAutoFocusCall()) {
                multiCapture();
            } else if (this.mState == 1) {
                setFocusState(2);
                this.mPendingMultiCapture = true;
            } else if (this.mState == 0) {
                multiCapture();
            }
        }
    }

    public void doSnap() {
        if (this.mInitialized) {
            if (this.mState == 3 || this.mState == 4 || !needAutoFocusCall()) {
                capture();
            } else if (this.mState == 1) {
                setFocusState(2);
            } else if (this.mState == 0) {
                capture();
            }
        }
    }

    public boolean focusFaceArea() {
        if (this.mFrameView == null || !isAutoFocusMode(getFocusMode())) {
            return false;
        }
        RectF focusRect = this.mFrameView.getFocusRect();
        if (focusRect == null) {
            return false;
        }
        if (this.mLatestFocusFace != null && this.mLastFocusFrom == 1 && Math.abs(focusRect.left - this.mLatestFocusFace.left) < 80.0f && Math.abs((focusRect.right - focusRect.left) - (this.mLatestFocusFace.right - this.mLatestFocusFace.left)) < 80.0f) {
            return false;
        }
        this.mLatestFocusFace = focusRect;
        focusPoint((int) ((focusRect.left + focusRect.right) / 2.0f), (int) ((focusRect.top + focusRect.bottom) / 2.0f), 1, false);
        return true;
    }

    public boolean getAeAwbLock() {
        return this.mAeAwbLock;
    }

    public List<Area> getFocusAreas() {
        return this.mFocusArea;
    }

    public String getFocusMode() {
        if (this.mOverrideFocusMode != null) {
            return this.mOverrideFocusMode;
        }
        List supportedFocusModes = CameraHardwareProxy.getDeviceProxy().getSupportedFocusModes(this.mParameters);
        this.mFocusMode = CameraSettings.getFocusMode();
        if (this.mFocusAreaSupported && this.mFocusArea != null) {
            if ("manual".equals(this.mFocusMode)) {
                this.mFocusMode = "manual";
            } else if ("continuous-picture".equals(this.mFocusMode) || "continuous-video".equals(this.mFocusMode) || "macro".equals(this.mFocusMode)) {
                this.mFocusMode = "auto";
            }
        }
        if (!isSupported(this.mFocusMode, supportedFocusModes)) {
            Object obj = null;
            for (String str : this.mDefaultFocusModes) {
                if (Util.isSupported(str, supportedFocusModes)) {
                    this.mFocusMode = str;
                    obj = 1;
                    break;
                }
            }
            if (obj == null) {
                if (isSupported("auto", supportedFocusModes)) {
                    this.mFocusMode = "auto";
                } else {
                    this.mFocusMode = this.mParameters.getFocusMode();
                }
            }
            if (this.mFocusMode != null) {
                Editor edit = CameraSettingPreferences.instance().edit();
                edit.putString("pref_camera_focus_mode_key", this.mFocusMode);
                edit.apply();
            }
        }
        if ("continuous-picture".equals(this.mFocusMode)) {
            this.mLastFocusFrom = -1;
        }
        Log.v("FocusManager", "FocusMode = " + this.mFocusMode);
        return this.mFocusMode;
    }

    public List<Area> getMeteringAreas() {
        return this.mMeteringArea;
    }

    public boolean isFocusCompleted() {
        return this.mState == 3 || this.mState == 4;
    }

    public boolean isFocusingSnapOnFinish() {
        return this.mState == 2;
    }

    public void onAutoFocus(boolean z) {
        if (this.mState == 2) {
            if (z) {
                setFocusState(3);
                setLastFocusState(3);
            } else {
                setFocusState(4);
                setLastFocusState(4);
            }
            updateFocusUI();
            if (this.mPendingMultiCapture) {
                multiCapture();
            } else {
                capture();
            }
        } else if (this.mState == 1) {
            if (z) {
                setFocusState(3);
                setLastFocusState(3);
                if (!("continuous-picture".equals(this.mFocusMode) || this.mLastFocusFrom == 1)) {
                    this.mListener.playSound(1);
                }
            } else {
                setFocusState(this.mMirror ? 1 : 4);
                setLastFocusState(4);
            }
            updateFocusUI();
            this.mHandler.removeMessages(1);
            this.mCancelAutoFocusIfMove = true;
        } else if (this.mState != 0) {
        }
    }

    public void onAutoFocusMoving(boolean z, boolean z2) {
        if (this.mInitialized) {
            Object obj = 1;
            if (this.mFrameView != null && this.mFrameView.faceExists()) {
                this.mFocusView.clear();
                obj = null;
            }
            if (this.mFocusArea == null && "continuous-picture".equals(getFocusMode())) {
                if (z) {
                    if (this.mState != 2) {
                        setFocusState(1);
                    }
                    Log.v("FocusManager", "Camera KPI: CAF start");
                    this.mCafStartTime = System.currentTimeMillis();
                    if (obj != null) {
                        this.mFocusView.showStart();
                    }
                } else {
                    int i = this.mState;
                    Log.v("FocusManager", "Camera KPI: CAF stop: Focus time: " + (System.currentTimeMillis() - this.mCafStartTime));
                    if (z2) {
                        setFocusState(3);
                        setLastFocusState(3);
                    } else {
                        setFocusState(4);
                        setLastFocusState(4);
                    }
                    if (obj != null) {
                        if (z2) {
                            this.mFocusView.showSuccess();
                        } else {
                            this.mFocusView.showFail();
                        }
                    }
                    if (i == 2) {
                        setFocusState(3);
                        this.mFocusView.showSuccess();
                        if (this.mPendingMultiCapture) {
                            multiCapture();
                        } else {
                            capture();
                        }
                    }
                }
            }
        }
    }

    public void onCameraReleased() {
        onPreviewStopped();
    }

    public void onDeviceKeepMoving(double d) {
        if (Util.isTimeout(System.currentTimeMillis(), this.mLatestFocusTime, 3000)) {
            setLastFocusState(0);
            if (this.mCancelAutoFocusIfMove) {
                this.mHandler.sendEmptyMessage(0);
            }
        }
    }

    public void onPreviewStarted() {
        setFocusState(0);
    }

    public void onPreviewStopped() {
        setFocusState(0);
        resetTouchFocus();
        updateFocusUI();
    }

    public void onShutter() {
        updateFocusUI();
        this.mAeAwbLock = false;
    }

    public void onShutterDown() {
    }

    public void onShutterUp() {
    }

    public void onSingleTapUp(int i, int i2) {
        boolean z = true;
        if (1 != getTapAction()) {
            z = false;
        }
        focusPoint(i, i2, 3, z);
    }

    public void overrideFocusMode(String str) {
        this.mOverrideFocusMode = str;
    }

    public void prepareCapture(boolean z, int i) {
        if (this.mInitialized) {
            Object obj = 1;
            Object obj2 = null;
            String focusMode = getFocusMode();
            if (i == 2 && (("auto".equals(focusMode) || "macro".equals(focusMode)) && this.mLastState == 3)) {
                obj = null;
            }
            boolean equals = "continuous-picture".equals(focusMode);
            if (!(!isFocusEnabled() || equals || r3 == null)) {
                if (this.mState == 3 || this.mState == 4) {
                    if (!(!z || this.mFocusArea == null || Device.isResetToCCAFAfterCapture())) {
                        this.mKeepFocusUIState = true;
                        autoFocus(this.mLastFocusFrom);
                        this.mKeepFocusUIState = false;
                        obj2 = 1;
                    }
                } else if (this.mFrameView == null || !this.mFrameView.faceExists()) {
                    resetFocusAreaToCenter();
                    autoFocus(0);
                    obj2 = 1;
                } else {
                    focusFaceArea();
                    obj2 = 1;
                }
            }
            if (obj2 == null && z && equals) {
                if (!Device.isHalDoesCafWhenFlashOn()) {
                    requestAutoFocus();
                } else if (this.mState == 1) {
                    cancelAutoFocus();
                }
            }
        }
    }

    public void removeMessages() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
    }

    public void requestAutoFocus() {
        if (needAutoFocusCall() && this.mInitialized && this.mState != 2) {
            int i = 4;
            if (isNeedCancelAutoFocus()) {
                this.mListener.cancelAutoFocus();
                this.mFocusView.clear();
                setFocusState(0);
                this.mCancelAutoFocusIfMove = false;
                this.mHandler.removeMessages(0);
                this.mHandler.removeMessages(1);
            }
            if (resetFocusAreaToFaceArea()) {
                this.mFocusView.clear();
                i = 1;
            } else {
                resetFocusAreaToCenter();
            }
            this.mAeAwbLock = false;
            this.mListener.setFocusParameters();
            autoFocus(i);
        }
    }

    public void resetAfterCapture(boolean z) {
        if (Device.isResetToCCAFAfterCapture()) {
            resetTouchFocus();
        } else if (!z) {
        } else {
            if (this.mLastFocusFrom == 4) {
                this.mListener.cancelAutoFocus();
                resetTouchFocus();
                removeMessages();
                return;
            }
            setLastFocusState(0);
        }
    }

    public void resetFocusIndicator() {
        this.mFocusView.clear();
    }

    public void resetFocusStateIfNeeded() {
        this.mFocusArea = null;
        this.mMeteringArea = null;
        setFocusState(0);
        setLastFocusState(0);
        this.mCancelAutoFocusIfMove = false;
        if (!this.mHandler.hasMessages(0)) {
            this.mHandler.sendEmptyMessage(0);
        }
    }

    public void resetTouchFocus() {
        if (this.mInitialized) {
            this.mFocusArea = null;
            this.mMeteringArea = null;
            this.mCancelAutoFocusIfMove = false;
            resetFocusIndicator();
        }
        if (this.mFrameView != null) {
            this.mFrameView.resume();
        }
    }

    public void setAeAwbLock(boolean z) {
        this.mAeAwbLock = z;
    }

    public void setFrameView(FrameView frameView) {
        this.mFrameView = frameView;
    }

    public void setParameters(Parameters parameters) {
        boolean z = true;
        boolean z2 = false;
        this.mParameters = parameters;
        this.mFocusAreaSupported = this.mParameters.getMaxNumFocusAreas() > 0 ? isSupported("auto", CameraHardwareProxy.getDeviceProxy().getSupportedFocusModes(this.mParameters)) : false;
        if (this.mParameters.getMaxNumMeteringAreas() > 0) {
            z2 = true;
        }
        this.mMeteringAreaSupported = z2;
        if (!this.mParameters.isAutoExposureLockSupported()) {
            z = this.mParameters.isAutoWhiteBalanceLockSupported();
        }
        this.mLockAeAwbNeeded = z;
    }

    public void setPreviewSize(int i, int i2) {
        if (this.mPreviewWidth != i || this.mPreviewHeight != i2) {
            this.mPreviewWidth = i;
            this.mPreviewHeight = i2;
            setMatrix();
        }
    }

    public void updateFocusUI() {
        if (this.mInitialized && !this.mKeepFocusUIState) {
            FocusIndicator focusIndicator = this.mLastFocusFrom == 1 ? this.mFrameView : this.mFocusView;
            if (this.mState == 0) {
                focusIndicator.clear();
            } else if (this.mState == 1 || this.mState == 2) {
                focusIndicator.showStart();
            } else if ("continuous-picture".equals(this.mFocusMode)) {
                focusIndicator.showSuccess();
            } else if (this.mState == 3) {
                focusIndicator.showSuccess();
            } else if (this.mState == 4) {
                focusIndicator.showFail();
            }
        }
    }
}
