package com.android.camera.module;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import com.android.camera.AutoLockManager;
import com.android.camera.Camera;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraDisabledException;
import com.android.camera.CameraErrorCallback;
import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraManager.HardwareErrorListener;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.FocusManagerAbstract;
import com.android.camera.MutexModeManager;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.SettingsOverrider;
import com.android.camera.ui.FocusView.ExposureViewListener;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.UIController;
import com.android.camera.ui.V6GestureRecognizer;
import com.android.camera.ui.V6ModulePicker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class BaseModule implements Module, ExposureViewListener, HardwareErrorListener {
    protected static CameraHardwareProxy sProxy = CameraHardwareProxy.getDeviceProxy();
    protected Camera mActivity;
    protected CameraProxy mCameraDevice;
    protected boolean mCameraDisabled;
    protected int mCameraDisplayOrientation;
    protected boolean mCameraHardwareError;
    protected int mCameraId;
    protected ContentResolver mContentResolver;
    protected float mDeviceRotation = -1.0f;
    protected int mDisplayRotation;
    protected CameraErrorCallback mErrorCallback;
    protected float mExposureCompensationStep;
    private Handler mHandler = new C00951();
    protected boolean mHasPendingSwitching;
    protected boolean mIgnoreFocusChanged;
    private boolean mIgnoreTouchEvent;
    protected boolean mKeepAdjustedEv;
    protected long mMainThreadId;
    protected int mMaxExposureCompensation;
    protected int mMinExposureCompensation;
    protected MutexModeManager mMutexModePicker;
    protected int mNumberOfCameras;
    protected boolean mObjectTrackingStarted;
    protected boolean mOpenCameraFail;
    protected int mOrientation = -1;
    protected int mOrientationCompensation = 0;
    protected Parameters mParameters;
    protected boolean mPaused;
    protected int mPendingSwitchCameraId = -1;
    protected CameraSettingPreferences mPreferences;
    private boolean mRestoring;
    protected SettingsOverrider mSettingsOverrider;
    protected boolean mSwitchingCamera;
    protected int mUIStyle = -1;
    protected boolean mWaitForRelease;
    protected int mZoomMax;
    protected int mZoomMaxRatio;
    private float mZoomScaled;

    class C00951 extends Handler {
        C00951() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (BaseModule.this.mDisplayRotation != 180) {
                        BaseModule.this.mActivity.getScreenHint().showFrontCameraFirstUseHintPopup();
                        CameraSettings.cancelFrontCameraFirstUseHint(BaseModule.this.mPreferences);
                        return;
                    }
                    return;
                case 1:
                    BaseModule.this.mActivity.getScreenHint().dismissFrontCameraFirstUseHintPopup();
                    return;
                default:
                    return;
            }
        }
    }

    class C00962 implements Runnable {
        C00962() {
        }

        public void run() {
            BaseModule.this.enterMutexMode();
        }
    }

    class C00973 implements Runnable {
        C00973() {
        }

        public void run() {
            BaseModule.this.exitMutexMode();
        }
    }

    class C00984 implements Runnable {
        C00984() {
        }

        public void run() {
            BaseModule.this.enterMutexMode();
        }
    }

    class C00995 implements Runnable {
        C00995() {
        }

        public void run() {
            BaseModule.this.exitMutexMode();
        }
    }

    public enum CameraMode {
        Normal(0),
        ImageCapture(2),
        VideoCapture(4),
        ScanQRCode(6);
        
        public int value;

        private CameraMode(int i) {
            this.value = i;
        }
    }

    protected class CameraOpenThread extends Thread {
        public void run() {
            BaseModule.this.openCamera();
        }
    }

    private ArrayList<CameraMode> getCameraModeList() {
        ArrayList<CameraMode> arrayList = new ArrayList();
        arrayList.add(CameraMode.Normal);
        arrayList.add(CameraMode.ImageCapture);
        arrayList.add(CameraMode.VideoCapture);
        arrayList.add(CameraMode.ScanQRCode);
        return arrayList;
    }

    public static int getPreferencesLocalId(int i, CameraMode cameraMode) {
        return cameraMode.value + i;
    }

    protected static boolean isSupported(String str, List<String> list) {
        return list != null && list.indexOf(str) >= 0;
    }

    public boolean IsIgnoreTouchEvent() {
        return this.mIgnoreTouchEvent;
    }

    protected void addMuteToParameters(Parameters parameters) {
        parameters.set("camera-service-mute", "true");
    }

    protected void addT2TParameters(Parameters parameters) {
        if (Device.isSupportedObjectTrack()) {
            parameters.set("t2t", "on");
        }
    }

    protected void addZoom(int i) {
        int zoomValue = getZoomValue() + i;
        if (zoomValue < 0) {
            zoomValue = 0;
        } else if (zoomValue > this.mZoomMax) {
            zoomValue = this.mZoomMax;
        }
        onZoomValueChanged(zoomValue);
    }

    public boolean canIgnoreFocusChanged() {
        return this.mIgnoreFocusChanged;
    }

    protected void changeConflictPreference() {
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            Iterable<String> supportedSettingKeys = getSupportedSettingKeys();
            if (supportedSettingKeys != null) {
                Editor edit = CameraSettingPreferences.instance().edit();
                for (String str : supportedSettingKeys) {
                    if (CameraSettings.isSwitchOn(str)) {
                        edit.remove(str);
                    }
                }
                edit.apply();
            }
        }
    }

    protected void changePreviewSurfaceSize() {
        int i = 0;
        int i2 = 0;
        switch (this.mUIStyle) {
            case 0:
                i = Util.sWindowWidth;
                i2 = (Util.sWindowWidth * 4) / 3;
                break;
            case 1:
                i = Util.sWindowWidth;
                i2 = Util.sWindowHeight;
                break;
        }
        this.mActivity.onLayoutChange(i, i2);
    }

    public void checkActivityOrientation() {
        if (this.mDisplayRotation != Util.getDisplayRotation(this.mActivity)) {
            setDisplayOrientation();
        }
    }

    protected void configOisParameters(Parameters parameters, boolean z) {
        sProxy.setOIS(parameters, z);
    }

    protected boolean currentIsMainThread() {
        return this.mMainThreadId == Thread.currentThread().getId();
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return V6GestureRecognizer.getInstance(this.mActivity).onTouchEvent(motionEvent);
    }

    public void enableCameraControls(boolean z) {
        getUIController().enableControls(z);
        ignoreTouchEvent(!z);
    }

    protected void enterMutexMode() {
    }

    protected void exitMutexMode() {
    }

    public Camera getActivity() {
        return this.mActivity;
    }

    protected int getPreferencesLocalId() {
        int i = this.mCameraId;
        return this.mActivity.isImageCaptureIntent() ? getPreferencesLocalId(this.mCameraId, CameraMode.ImageCapture) : this.mActivity.isVideoCaptureIntent() ? getPreferencesLocalId(this.mCameraId, CameraMode.VideoCapture) : this.mActivity.isScanQRCodeIntent() ? getPreferencesLocalId(this.mCameraId, CameraMode.ScanQRCode) : getPreferencesLocalId(this.mCameraId, CameraMode.Normal);
    }

    protected int getPreferredCameraId() {
        int cameraFacingIntentExtras = Util.getCameraFacingIntentExtras(this.mActivity);
        if (cameraFacingIntentExtras == -1) {
            cameraFacingIntentExtras = Util.getStartCameraId(this.mActivity);
        }
        if (cameraFacingIntentExtras == -1) {
            cameraFacingIntentExtras = CameraSettings.readPreferredCameraId(this.mPreferences);
        }
        CameraSettings.writePreferredCameraId(this.mPreferences, cameraFacingIntentExtras);
        return cameraFacingIntentExtras;
    }

    public Resources getResources() {
        return this.mActivity.getResources();
    }

    protected int getScreenDelay() {
        return (this.mActivity == null || this.mActivity.startFromKeyguard()) ? 30000 : 60000;
    }

    public String getString(int i) {
        return this.mActivity.getString(i);
    }

    public List<String> getSupportedSettingKeys() {
        return null;
    }

    public UIController getUIController() {
        return this.mActivity.getUIController();
    }

    public Window getWindow() {
        return this.mActivity.getWindow();
    }

    public int getZoomMax() {
        return this.mZoomMax;
    }

    public int getZoomMaxRatio() {
        return this.mZoomMaxRatio;
    }

    protected int getZoomValue() {
        return CameraSettings.readZoom(this.mPreferences);
    }

    public boolean handleMessage(int i, int i2, Object obj, Object obj2) {
        switch (i2) {
            case C0049R.id.zoom_button:
                if (i == 7 && !this.mPaused && isCameraEnabled() && CameraSettings.isSupportedOpticalZoom() && CameraSettings.isSwitchCameraZoomMode()) {
                    onCameraPickerClicked(CameraHolder.instance().getBackCameraId());
                    return true;
                }
            case C0049R.id.v6_focus_view:
                if (i == 1 && !this.mPaused && isCameraEnabled()) {
                    int intValue = ((Integer) obj).intValue();
                    int intValue2 = ((Integer) obj2).intValue();
                    if (intValue2 == 2) {
                        this.mParameters.setAutoWhiteBalanceLock(false);
                    } else if (intValue2 == 1) {
                        this.mParameters.setExposureCompensation(intValue);
                        this.mParameters.setAutoWhiteBalanceLock(true);
                    }
                    this.mCameraDevice.setParametersAsync(this.mParameters);
                    if (intValue2 == 1) {
                        CameraSettings.writeExposure(this.mPreferences, intValue);
                        updateStatusBar("pref_camera_exposure_key");
                    }
                    if (this.mKeepAdjustedEv) {
                        CameraDataAnalytics.instance().trackEvent("ev_adjust_recom_times_key");
                        this.mKeepAdjustedEv = false;
                    }
                    Log.d("Camera", "EV = : " + intValue);
                    return true;
                }
            case C0049R.id.zoom_popup:
                if (i == 7 && !this.mPaused && isCameraEnabled()) {
                    onZoomValueChanged(Util.binarySearchRightMost(this.mParameters.getZoomRatios(), Integer.valueOf(((Integer) obj2).intValue())), ((Boolean) obj).booleanValue());
                    return true;
                }
        }
        return false;
    }

    protected boolean handleVolumeKeyEvent(boolean z, boolean z2, int i) {
        String string = this.mPreferences.getString("pref_camera_volumekey_function_key", this.mActivity.getString(C0049R.string.pref_camera_volumekey_function_default));
        if (this.mCameraDevice == null || this.mParameters == null || !isCameraEnabled()) {
            return true;
        }
        if (string.equals(this.mActivity.getString(C0049R.string.pref_camera_volumekey_function_entryvalue_shutter))) {
            performVolumeKeyClicked(i, z2);
            return true;
        } else if (V6ModulePicker.isPanoramaModule() || !string.equals(this.mActivity.getString(C0049R.string.pref_camera_volumekey_function_entryvalue_zoom)) || !this.mParameters.isZoomSupported() || !isZoomEnabled() || !z2) {
            return false;
        } else {
            if (i == 0) {
                CameraDataAnalytics.instance().trackEvent("zoom_volume_times");
            }
            if (z) {
                addZoom(1);
            } else {
                addZoom(-1);
            }
            return true;
        }
    }

    protected boolean hasCameraException() {
        return (this.mCameraDisabled || this.mOpenCameraFail) ? true : this.mCameraHardwareError;
    }

    public void ignoreTouchEvent(boolean z) {
        this.mIgnoreTouchEvent = z;
    }

    protected void initializeExposureCompensation() {
        this.mMaxExposureCompensation = this.mParameters.getMaxExposureCompensation();
        this.mMinExposureCompensation = this.mParameters.getMinExposureCompensation();
        this.mExposureCompensationStep = this.mParameters.getExposureCompensationStep();
    }

    protected void initializeMutexMode() {
        if (this.mMutexModePicker == null) {
            HashMap hashMap = new HashMap();
            C00962 c00962 = new C00962();
            C00973 c00973 = new C00973();
            HashMap hashMap2 = new HashMap();
            hashMap2.put("enter", c00962);
            hashMap2.put("exit", c00973);
            hashMap.put(MutexModeManager.getMutexModeName(1), hashMap2);
            hashMap.put(MutexModeManager.getMutexModeName(2), hashMap2);
            hashMap.put(MutexModeManager.getMutexModeName(5), hashMap2);
            hashMap.put(MutexModeManager.getMutexModeName(3), hashMap2);
            hashMap.put(MutexModeManager.getMutexModeName(7), hashMap2);
            C00984 c00984 = new C00984();
            C00995 c00995 = new C00995();
            HashMap hashMap3 = new HashMap();
            hashMap3.put("enter", c00984);
            hashMap3.put("exit", c00995);
            hashMap.put(MutexModeManager.getMutexModeName(4), hashMap3);
            this.mMutexModePicker = new MutexModeManager(hashMap);
        }
    }

    protected void initializeZoom() {
        if (this.mParameters.isZoomSupported()) {
            this.mZoomMax = this.mParameters.getMaxZoom();
            this.mZoomMaxRatio = ((Integer) this.mParameters.getZoomRatios().get(this.mZoomMax)).intValue();
            this.mActivity.getCameraScreenNail().setOrientation(this.mOrientationCompensation, false);
            setZoomValue(this.mParameters.getZoom());
        }
    }

    protected boolean isBackCamera() {
        return this.mCameraId == CameraHolder.instance().getBackCameraId();
    }

    public boolean isCameraEnabled() {
        return true;
    }

    public boolean isCaptureIntent() {
        return false;
    }

    protected boolean isFrontCamera() {
        return this.mCameraId == CameraHolder.instance().getFrontCameraId();
    }

    public boolean isInTapableRect(int i, int i2) {
        if (getUIController().getPreviewFrame() == null) {
            return false;
        }
        Point point = new Point(i, i2);
        mapTapCoordinate(point);
        return this.mActivity.getCameraScreenNail().getRenderRect().contains(point.x, point.y);
    }

    public boolean isKeptBitmapTexture() {
        return false;
    }

    public boolean isMeteringAreaOnly() {
        return false;
    }

    public boolean isNeedMute() {
        return !CameraSettings.isCameraSoundOpen(this.mPreferences);
    }

    protected boolean isRestoring() {
        return this.mRestoring;
    }

    public boolean isShowCaptureButton() {
        return false;
    }

    protected boolean isSquareModeChange() {
        return CameraSettings.isSwitchOn("pref_camera_square_mode_key") != (this.mActivity.getCameraScreenNail().getRenderTargeRatio() == 2);
    }

    public boolean isVideoRecording() {
        return false;
    }

    protected boolean isZoomEnabled() {
        return true;
    }

    protected void mapTapCoordinate(Object obj) {
        int[] relativeLocation = Util.getRelativeLocation(getUIController().getGLView(), getUIController().getPreviewFrame());
        if (obj instanceof Point) {
            Point point = (Point) obj;
            point.x -= relativeLocation[0];
            Point point2 = (Point) obj;
            point2.y -= relativeLocation[1];
        } else if (obj instanceof RectF) {
            RectF rectF = (RectF) obj;
            rectF.left -= (float) relativeLocation[0];
            rectF = (RectF) obj;
            rectF.right -= (float) relativeLocation[0];
            rectF = (RectF) obj;
            rectF.top -= (float) relativeLocation[1];
            RectF rectF2 = (RectF) obj;
            rectF2.bottom -= (float) relativeLocation[1];
        }
    }

    public void notifyError() {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setCameraError();
        }
        this.mCameraHardwareError = true;
        if (this.mActivity.isPaused()) {
            this.mActivity.finish();
        } else {
            onCameraException();
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
    }

    public boolean onBackPressed() {
        return false;
    }

    protected void onCameraException() {
        if (currentIsMainThread()) {
            if (this.mOpenCameraFail || this.mCameraHardwareError) {
                if (this.mOpenCameraFail) {
                    CameraDataAnalytics.instance().trackEvent("open_camera_fail_key");
                }
                if ((!this.mActivity.isPaused() || this.mOpenCameraFail) && this.mActivity.couldShowErrorDialog()) {
                    Activity activity = this.mActivity;
                    int i = Util.isInVideoCall(this.mActivity) ? C0049R.string.cannot_connect_camera_volte_call : CameraSettings.updateOpenCameraFailTimes() > 1 ? C0049R.string.cannot_connect_camera_twice : C0049R.string.cannot_connect_camera_once;
                    Util.showErrorAndFinish(activity, i);
                    this.mActivity.showErrorDialog();
                }
            }
            if (this.mCameraDisabled && this.mActivity.couldShowErrorDialog()) {
                Util.showErrorAndFinish(this.mActivity, C0049R.string.camera_disabled);
                this.mActivity.showErrorDialog();
                return;
            }
            return;
        }
        sendOpenFailMessage();
    }

    public boolean onCameraPickerClicked(int i) {
        return false;
    }

    public void onCreate(Camera camera) {
        this.mActivity = camera;
        this.mMainThreadId = Thread.currentThread().getId();
        this.mContentResolver = camera.getContentResolver();
        this.mNumberOfCameras = CameraHolder.instance().getNumberOfCameras();
        this.mErrorCallback = new CameraErrorCallback(this.mActivity);
        this.mPreferences = CameraSettingPreferences.instance();
        this.mSettingsOverrider = new SettingsOverrider();
        initializeMutexMode();
    }

    public void onDestroy() {
        getUIController().getEffectCropView().onDestory();
    }

    public boolean onGestureTrack(RectF rectF, boolean z) {
        return true;
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return false;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 82 && !this.mActivity.startFromSecureKeyguard() && getUIController().getSettingButton().isEnabled()) {
            openSettingActivity();
        }
        return false;
    }

    public void onLongPress(int i, int i2) {
    }

    public void onNewIntent() {
    }

    public void onOrientationChanged(int i) {
        if (i != -1) {
            this.mOrientation = Util.roundOrientation(i, this.mOrientation);
            EffectController.getInstance().setOrientation(Util.getShootOrientation(this.mActivity, this.mOrientation));
            checkActivityOrientation();
            int i2 = (this.mOrientation + this.mDisplayRotation) % 360;
            if (this.mOrientationCompensation != i2) {
                this.mOrientationCompensation = i2;
                setOrientationIndicator(this.mOrientationCompensation, true);
            }
            if (this.mDisplayRotation == 180 && isFrontCamera() && this.mActivity.getScreenHint().isShowingFrontCameraFirstUseHintPopup()) {
                this.mHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    public void onPauseAfterSuper() {
        AutoLockManager.getInstance(this.mActivity).onPause();
        if (this.mActivity.getScreenHint() != null) {
            this.mActivity.getScreenHint().cancelHint();
        }
        CameraSettings.resetZoom(this.mPreferences);
        CameraSettings.resetCameraZoomMode();
        setZoomValue(0);
    }

    public void onPauseBeforeSuper() {
        this.mPaused = true;
    }

    public void onPreviewPixelsRead(byte[] bArr, int i, int i2) {
    }

    public void onPreviewTextureCopied() {
    }

    public void onResumeAfterSuper() {
        if (this.mActivity.getScreenHint() != null) {
            this.mActivity.getScreenHint().updateHint();
        }
        AutoLockManager.getInstance(this.mActivity).onResume();
    }

    public void onResumeBeforeSuper() {
        this.mPaused = false;
    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("killed-moduleIndex", V6ModulePicker.getCurrentModule());
    }

    public boolean onScale(float f, float f2, float f3) {
        if (isZoomEnabled()) {
            this.mZoomScaled += f3 - 1.0f;
            if (scaleZoomValue(this.mZoomScaled)) {
                this.mZoomScaled = Float.MIN_VALUE;
            }
            getUIController().getZoomButton().dismissPopup();
        }
        return true;
    }

    public boolean onScaleBegin(float f, float f2) {
        this.mZoomScaled = Float.MIN_VALUE;
        return true;
    }

    public void onScaleEnd() {
    }

    public void onSettingValueChanged(String str) {
    }

    public void onSingleTapUp(int i, int i2) {
    }

    public void onStop() {
    }

    public void onSwitchAnimationDone() {
    }

    public void onUserInteraction() {
    }

    public void onWindowFocusChanged(boolean z) {
        if (z) {
            this.mIgnoreFocusChanged = false;
        }
    }

    public void onZoomValueChanged(int i) {
        onZoomValueChanged(i, false);
    }

    public void onZoomValueChanged(int i, boolean z) {
        if (!this.mPaused && this.mParameters != null && this.mCameraDevice != null && isCameraEnabled()) {
            setZoomValue(i);
            this.mParameters.setZoom(i);
            if (CameraSettings.isSupportedOpticalZoom() && V6ModulePicker.isCameraModule() && !CameraSettings.isSwitchOn("pref_camera_manual_mode_key")) {
                if (i > 0) {
                    configOisParameters(this.mParameters, false);
                } else {
                    configOisParameters(this.mParameters, true);
                }
            }
            if (z) {
                this.mCameraDevice.setParameters(this.mParameters);
            } else {
                this.mCameraDevice.setParametersAsync(this.mParameters);
            }
            updateStatusBar("pref_camera_zoom_key");
            this.mActivity.getUIController().getZoomButton().reloadPreference();
            Log.d("Camera", "Zoom : " + i);
        }
    }

    protected void openCamera() {
        try {
            prepareOpenCamera();
            this.mCameraDevice = Util.openCamera(this.mActivity, this.mCameraId);
            this.mCameraDevice.setHardwareListener(this);
            if (this.mCameraDevice != null) {
                this.mParameters = this.mCameraDevice.getParameters();
            }
        } catch (CameraHardwareException e) {
            this.mOpenCameraFail = true;
        } catch (CameraDisabledException e2) {
            this.mCameraDisabled = true;
        }
    }

    protected void openSettingActivity() {
    }

    protected void performVolumeKeyClicked(int i, boolean z) {
    }

    protected void playCameraSound(int i) {
        if (CameraSettings.isCameraSoundOpen(this.mPreferences)) {
            this.mActivity.playCameraSound(i);
        }
    }

    protected void prepareOpenCamera() {
        if (this.mDisplayRotation != 180 && isFrontCamera() && CameraSettings.isNeedFrontCameraFirstUseHint(this.mPreferences)) {
            this.mHandler.sendEmptyMessageDelayed(0, 200);
        }
    }

    public void requestRender() {
    }

    protected void resetCameraSettingsIfNeed() {
        if (this.mActivity.getCameraAppImpl().isNeedRestore()) {
            this.mActivity.getCameraAppImpl().resetRestoreFlag();
            Iterator it = getCameraModeList().iterator();
            while (it.hasNext()) {
                CameraMode cameraMode = (CameraMode) it.next();
                this.mCameraId = 0;
                this.mPreferences.setLocalId(getPreferencesLocalId(this.mCameraId, cameraMode));
                CameraSettings.resetSettingsNoNeedToSave(this.mPreferences, this.mCameraId);
                this.mCameraId = 1;
                this.mPreferences.setLocalId(getPreferencesLocalId(this.mCameraId, cameraMode));
                CameraSettings.resetSettingsNoNeedToSave(this.mPreferences, this.mCameraId);
            }
            return;
        }
        CameraSettings.resetPreference("pref_camera_panoramamode_key");
        CameraSettings.resetPreference("pref_camera_portrait_mode_key");
    }

    protected void resetFaceBeautyParams(Parameters parameters) {
        sProxy.setStillBeautify(parameters, getString(C0049R.string.pref_face_beauty_close));
    }

    public boolean scaleZoomValue(float f) {
        int zoomValue = getZoomValue() + ((int) (((float) this.mZoomMax) * f));
        if (getZoomValue() == zoomValue) {
            return false;
        }
        if (zoomValue < 0) {
            zoomValue = 0;
        } else if (zoomValue > this.mZoomMax) {
            zoomValue = this.mZoomMax;
        }
        onZoomValueChanged(zoomValue);
        return true;
    }

    protected void sendOpenFailMessage() {
    }

    protected void setDisplayOrientation() {
        this.mDisplayRotation = Util.getDisplayRotation(this.mActivity);
        this.mCameraDisplayOrientation = Util.getDisplayOrientation(this.mDisplayRotation, this.mCameraId);
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setDisplayOrientation(this.mCameraDisplayOrientation);
        }
    }

    protected void setOrientationIndicator(int i, boolean z) {
        int i2 = 0;
        Rotatable[] rotatableArr = new Rotatable[]{getUIController(), this.mActivity.getCameraScreenNail()};
        int length = rotatableArr.length;
        while (i2 < length) {
            Rotatable rotatable = rotatableArr[i2];
            if (rotatable != null) {
                rotatable.setOrientation(i, z);
            }
            i2++;
        }
    }

    public void setRestoring(boolean z) {
        this.mRestoring = z;
    }

    protected void setZoomValue(int i) {
        CameraSettings.writeZoom(this.mPreferences, i);
    }

    protected void trackPictureTaken(int i, boolean z, int i2, int i3, boolean z2) {
        CameraDataAnalytics.instance().trackEvent("camera_picture_taken_key", (long) i);
        if (z) {
            CameraDataAnalytics.instance().trackEvent("capture_nums_burst", (long) i);
        }
        if (CameraSettings.isAspectRatio16_9(i2, i3)) {
            CameraDataAnalytics.instance().trackEvent("capture_times_size_16_9", (long) i);
        } else {
            CameraDataAnalytics.instance().trackEvent("capture_times_size_4_3", (long) i);
        }
        if (z2) {
            CameraDataAnalytics.instance().trackEvent("picture_with_location_key", (long) i);
        } else if (CameraSettings.isRecordLocation(this.mPreferences)) {
            CameraDataAnalytics.instance().trackEvent("picture_without_location_key", (long) i);
        }
        CameraDataAnalytics.instance().trackEvent(isFrontCamera() ? "front_camera_picture_taken_key" : "back_camera_picture_taken_key", (long) i);
    }

    public void transferOrientationCompensation(Module module) {
        this.mOrientation = ((BaseModule) module).mOrientation;
        this.mOrientationCompensation = ((BaseModule) module).mOrientationCompensation;
    }

    protected void updateCameraScreenNailSize(int i, int i2, FocusManagerAbstract focusManagerAbstract) {
        if (this.mCameraDisplayOrientation % 180 != 0) {
            int i3 = i;
            i = i2;
            i2 = i3;
        }
        if (this.mActivity.getCameraScreenNail().getWidth() == i && this.mActivity.getCameraScreenNail().getHeight() == i2 && !this.mSwitchingCamera) {
            if (isSquareModeChange()) {
            }
            if (getUIController().getObjectView() != null) {
                getUIController().getObjectView().setPreviewSize(i, i2);
            }
        }
        this.mActivity.getCameraScreenNail().setSize(i, i2);
        focusManagerAbstract.setRenderSize(this.mActivity.getCameraScreenNail().getRenderWidth(), this.mActivity.getCameraScreenNail().getRenderHeight());
        if (getUIController().getObjectView() != null) {
            getUIController().getObjectView().setPreviewSize(i, i2);
        }
    }

    protected void updateStatusBar(String str) {
        this.mActivity.getUIController().getSettingsStatusBar().updateStatus(str);
    }
}
