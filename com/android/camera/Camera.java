package com.android.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Window;
import android.widget.FrameLayout;
import com.android.camera.camera_adapter.CameraLC;
import com.android.camera.camera_adapter.CameraMTK;
import com.android.camera.camera_adapter.CameraNv;
import com.android.camera.camera_adapter.CameraPadOne;
import com.android.camera.camera_adapter.CameraQcom;
import com.android.camera.camera_adapter.VideoLC;
import com.android.camera.camera_adapter.VideoMTK;
import com.android.camera.camera_adapter.VideoNv;
import com.android.camera.camera_adapter.VideoPadOne;
import com.android.camera.camera_adapter.VideoQcom;
import com.android.camera.effect.EffectController;
import com.android.camera.module.CameraModule;
import com.android.camera.module.Module;
import com.android.camera.module.MorphoPanoramaModule;
import com.android.camera.module.VideoModule;
import com.android.camera.permission.PermissionManager;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.ImageSaver;
import com.android.camera.storage.Storage;
import com.android.camera.ui.UIController;
import com.android.camera.ui.V6GestureRecognizer;
import com.android.camera.ui.V6ModulePicker;
import com.android.zxing.QRCodeManager;

public class Camera extends ActivityBase implements OnRequestPermissionsResultCallback {
    private boolean mCameraErrorShown;
    private FrameLayout mContentFrame;
    private boolean mContentInflated;
    private int mCurrentModuleIndex = 0;
    private LogThread mDebugThread;
    private ImageSaver mImageSaver;
    private boolean mIntentChanged;
    private boolean mIsFromLauncher;
    private int mLastIgnoreKey = -1;
    private long mLastKeyEventTime = 0;
    private MyOrientationEventListener mOrientationListener;
    private SensorStateManager mSensorStateManager;
    private int mTick;
    private Thread mWatchDog;
    private final Runnable tickerRunnable = new C00801();

    class C00801 implements Runnable {
        C00801() {
        }

        public void run() {
            Camera.this.mTick = (Camera.this.mTick + 1) % 10;
        }
    }

    class LogThread extends Thread {
        private boolean mRunFlag = true;

        LogThread() {
        }

        public void run() {
            while (this.mRunFlag) {
                try {
                    Thread.sleep(10);
                    if (!Camera.this.mPaused) {
                        Camera.this.mHandler.obtainMessage(0, Util.getDebugInfo()).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        public void setRunFlag(boolean z) {
            this.mRunFlag = z;
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int i) {
            if (i != -1) {
                Camera.this.mOrientation = Util.roundOrientation(i, Camera.this.mOrientation);
                Camera.this.mCurrentModule.onOrientationChanged(i);
            }
        }
    }

    private class WatchDogThread extends Thread {
        private WatchDogThread() {
        }

        public void run() {
            setName("ANR-WatchDog");
            while (!isInterrupted()) {
                Log.v("Camera", "watch dog run " + Thread.currentThread().getId());
                int -get0 = Camera.this.mTick;
                Camera.this.mHandler.post(Camera.this.tickerRunnable);
                try {
                    Thread.sleep(5000);
                    if (Camera.this.mTick == -get0) {
                        CameraSettings.setEdgeMode(Camera.this, false);
                        return;
                    }
                } catch (InterruptedException e) {
                    Log.v("Camera", "watch dog InterruptedException " + Thread.currentThread().getId());
                    return;
                }
            }
        }
    }

    private void closeModule(Module module) {
        this.mPaused = true;
        module.onPauseBeforeSuper();
        module.onPauseAfterSuper();
        module.onStop();
        module.onDestroy();
    }

    private Module getCameraByDevice() {
        return Device.isPad() ? new CameraPadOne() : Device.isQcomPlatform() ? new CameraQcom() : Device.isLCPlatform() ? new CameraLC() : Device.isNvPlatform() ? new CameraNv() : Device.isMTKPlatform() ? new CameraMTK() : new CameraModule();
    }

    private Module getModuleByIndex(int i) {
        if (i == 2) {
            this.mCurrentModuleIndex = i;
            V6ModulePicker.setCurrentModule(this.mCurrentModuleIndex);
            return new MorphoPanoramaModule();
        } else if (i == 1) {
            this.mCurrentModuleIndex = i;
            V6ModulePicker.setCurrentModule(this.mCurrentModuleIndex);
            return getVideoByDevice();
        } else {
            this.mCurrentModuleIndex = 0;
            V6ModulePicker.setCurrentModule(this.mCurrentModuleIndex);
            return getCameraByDevice();
        }
    }

    private Module getVideoByDevice() {
        return Device.isQcomPlatform() ? new VideoQcom() : Device.isLCPlatform() ? new VideoLC() : Device.isNvPlatform() ? new VideoNv() : Device.isMTKPlatform() ? new VideoMTK() : Device.isPad() ? new VideoPadOne() : new VideoModule();
    }

    private void openModule(Module module) {
        module.transferOrientationCompensation(this.mCurrentModule);
        this.mCurrentModule = module;
        this.mPaused = false;
        module.onCreate(this);
        module.onResumeBeforeSuper();
        module.onResumeAfterSuper();
    }

    private void setTranslucentNavigation(boolean z) {
        if (Util.checkDeviceHasNavigationBar(this)) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(768);
            window.addFlags(Integer.MIN_VALUE);
        }
    }

    private void showDebug() {
        if (Util.isShowDebugInfo()) {
            Log.m2e("CameraDebug", "ready to start show debug info ");
            this.mUIController.showDebugView();
            this.mDebugThread = new LogThread();
            this.mDebugThread.start();
        }
    }

    private void switchEdgeFingerMode(boolean z) {
        if (Device.isSupportedEdgeTouch()) {
            CameraSettings.setEdgeMode(this, z);
            if (z) {
                this.mWatchDog = new WatchDogThread();
                this.mWatchDog.start();
            } else if (this.mWatchDog != null) {
                this.mWatchDog.interrupt();
                this.mWatchDog = null;
            }
        }
    }

    private void trackLaunchEvent() {
        Object obj = null;
        Intent intent = getIntent();
        if (intent == null) {
            CameraDataAnalytics.instance().trackEvent("launch_normal_times_key");
            return;
        }
        String str;
        if (TextUtils.equals(intent.getAction(), "android.media.action.STILL_IMAGE_CAMERA") && getKeyguardFlag()) {
            if ((8388608 & intent.getFlags()) != 0) {
                obj = 1;
            }
            str = obj != null ? "launch_keyguard_times_key" : "launch_volume_key_times_key";
        } else {
            str = isImageCaptureIntent() ? "launch_capture_intent_times_key" : isVideoCaptureIntent() ? "launch_video_intent_times_key" : "launch_normal_times_key";
        }
        CameraDataAnalytics.instance().trackEvent(str);
    }

    public void changeRequestOrientation() {
        if (Device.IS_A8 || Device.IS_D5) {
            if (CameraSettings.isFrontCamera()) {
                setRequestedOrientation(7);
            } else {
                setRequestedOrientation(1);
            }
        }
    }

    public boolean couldShowErrorDialog() {
        return !this.mCameraErrorShown;
    }

    public void createContentView() {
        if (!this.mContentInflated) {
            getLayoutInflater().inflate(C0049R.layout.v6_camera, this.mContentFrame);
            this.mContentInflated = true;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return !super.dispatchTouchEvent(motionEvent) ? this.mCurrentModule.dispatchTouchEvent(motionEvent) : true;
    }

    public int getCapturePosture() {
        return this.mSensorStateManager.getCapturePosture();
    }

    public ImageSaver getImageSaver() {
        return this.mImageSaver;
    }

    public SensorStateManager getSensorStateManager() {
        return this.mSensorStateManager;
    }

    public void onBackPressed() {
        if (!this.mCurrentModule.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        EffectController.releaseInstance();
        setContentView(C0049R.layout.v6_main);
        getWindow().setBackgroundDrawable(null);
        if (!getKeyguardFlag()) {
            PermissionManager.requestCameraRuntimePermissions(this);
        }
        this.mContentInflated = false;
        this.mContentFrame = (FrameLayout) findViewById(C0049R.id.main_content);
        this.mUIController = new UIController(this);
        if (bundle != null) {
            int i = bundle.getInt("killed-moduleIndex", -1);
            if (i != -1) {
                this.mCurrentModule = getModuleByIndex(i);
                this.mCurrentModule.setRestoring(true);
                Log.m0d("Camera", "restoreModuleIndex=" + i);
                this.mIsFromLauncher = true;
                Util.updateCountryIso(this);
                this.mSensorStateManager = new SensorStateManager(this, getMainLooper());
                this.mCurrentModule.onCreate(this);
                this.mOrientationListener = new MyOrientationEventListener(this);
                this.mImageSaver = new ImageSaver(this, this.mHandler, isImageCaptureIntent() ? isVideoCaptureIntent() : true);
                showDebug();
                setTranslucentNavigation(true);
                trackLaunchEvent();
            }
        }
        if ("android.media.action.VIDEO_CAMERA".equals(getIntent().getAction()) || "android.media.action.VIDEO_CAPTURE".equals(getIntent().getAction())) {
            this.mCurrentModule = getModuleByIndex(1);
        } else {
            this.mCurrentModule = getModuleByIndex(0);
        }
        this.mIsFromLauncher = true;
        Util.updateCountryIso(this);
        this.mSensorStateManager = new SensorStateManager(this, getMainLooper());
        this.mCurrentModule.onCreate(this);
        this.mOrientationListener = new MyOrientationEventListener(this);
        if (isImageCaptureIntent()) {
        }
        this.mImageSaver = new ImageSaver(this, this.mHandler, isImageCaptureIntent() ? isVideoCaptureIntent() : true);
        showDebug();
        setTranslucentNavigation(true);
        trackLaunchEvent();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mCurrentModule.onDestroy();
        this.mImageSaver.onHostDestroy();
        this.mSensorStateManager.onDestory();
        QRCodeManager.instance(this).onDestroy();
        V6GestureRecognizer.onDestory(this);
        if (this.mDebugThread != null) {
            this.mDebugThread.setRunFlag(false);
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (keyEvent.getRepeatCount() == 0 && (i == 66 || i == 27 || i == 24 || i == 25)) {
            if (Util.isTimeout(keyEvent.getEventTime(), this.mLastKeyEventTime, 150)) {
                this.mLastIgnoreKey = -1;
            } else {
                this.mLastIgnoreKey = i;
                return true;
            }
        } else if (keyEvent.getRepeatCount() > 0 && i == this.mLastIgnoreKey) {
            this.mLastIgnoreKey = -1;
        }
        return !this.mCurrentModule.onKeyDown(i, keyEvent) ? super.onKeyDown(i, keyEvent) : true;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        boolean z = true;
        if (i == this.mLastIgnoreKey) {
            this.mLastKeyEventTime = 0;
            this.mLastIgnoreKey = -1;
            return true;
        }
        this.mLastKeyEventTime = keyEvent.getEventTime();
        if (!this.mCurrentModule.onKeyUp(i, keyEvent)) {
            z = super.onKeyUp(i, keyEvent);
        }
        return z;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.mIntentChanged = true;
        setIntent(intent);
        this.mCurrentModule.onNewIntent();
        trackLaunchEvent();
    }

    public void onPause() {
        this.mPaused = true;
        this.mActivityPaused = true;
        switchEdgeFingerMode(false);
        this.mOrientationListener.disable();
        this.mCurrentModule.onPauseBeforeSuper();
        super.onPause();
        this.mCurrentModule.onPauseAfterSuper();
        this.mImageSaver.onHostPause();
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == PermissionManager.getCameraRuntimePermissionRequestCode()) {
            if (!PermissionManager.isCameraLaunchPermissionsResultReady(strArr, iArr)) {
                finish();
            }
            if (!this.mPaused && PermissionManager.isCameraLocationPermissionsResultReady(strArr, iArr)) {
                LocationManager.instance().recordLocation(CameraSettings.isRecordLocation(CameraSettingPreferences.instance()));
            }
        }
    }

    public void onResume() {
        if (getKeyguardFlag() && !PermissionManager.checkCameraLaunchPermissions()) {
            finish();
        }
        if (Util.checkDeviceHasNavigationBar(this)) {
            Util.getWindowAttribute(this);
        }
        Util.checkLockedOrientation(this);
        this.mPaused = false;
        this.mActivityPaused = false;
        switchEdgeFingerMode(true);
        Storage.initStorage(this);
        this.mOrientationListener.enable();
        int startModuleIndex = Util.getStartModuleIndex(this);
        if (!this.mIntentChanged || startModuleIndex < 0 || startModuleIndex == this.mCurrentModuleIndex) {
            this.mCurrentModule.onResumeBeforeSuper();
            super.onResume();
            this.mCurrentModule.onResumeAfterSuper();
        } else {
            super.onResume();
            switchToOtherModule(startModuleIndex);
            this.mIntentChanged = false;
        }
        if (this.mCurrentModuleIndex == 0) {
            Util.replaceStartEffectRender(this);
        }
        setBlurFlag(false);
        this.mImageSaver.onHostResume(!isImageCaptureIntent() ? isVideoCaptureIntent() : true);
        QRCodeManager.instance(this).resetQRScanExit(true);
    }

    public void onStop() {
        super.onStop();
        this.mCurrentModule.onStop();
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        this.mCurrentModule.onUserInteraction();
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        this.mCurrentModule.onWindowFocusChanged(z);
        if (!(this.mCameraBrightness == null || this.mCurrentModule.canIgnoreFocusChanged())) {
            this.mCameraBrightness.onWindowFocusChanged(z);
        }
        if (z) {
            Util.checkLockedOrientation(this);
            this.mCurrentModule.checkActivityOrientation();
            if (this.mSensorStateManager != null) {
                this.mSensorStateManager.register();
            }
        } else if (this.mSensorStateManager != null) {
            this.mSensorStateManager.unregister(15);
        }
    }

    public void pause() {
        if (!this.mCurrentModule.isVideoRecording()) {
            super.pause();
        }
    }

    public void resume() {
        if (!this.mCurrentModule.isVideoRecording()) {
            super.resume();
        }
    }

    public void setBlurFlag(boolean z) {
        if (z) {
            getWindow().addFlags(4);
            getUIController().getGLView().setBackgroundColor(getResources().getColor(C0049R.color.realtimeblur_bg));
            return;
        }
        getWindow().clearFlags(4);
        getUIController().getGLView().setBackground(null);
    }

    public void showErrorDialog() {
        this.mCameraErrorShown = true;
    }

    public void switchToOtherModule(int i) {
        if (!this.mPaused) {
            this.mIsFromLauncher = false;
            CameraHolder.instance().keep();
            closeModule(this.mCurrentModule);
            openModule(getModuleByIndex(i));
        }
    }
}
