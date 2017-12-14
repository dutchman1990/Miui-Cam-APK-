package com.android.camera;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.camera.CameraScreenNail.Listener;
import com.android.camera.google.ProcessingMediaManager;
import com.android.camera.module.BaseModule;
import com.android.camera.module.Module;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.ui.PopupManager;
import com.android.camera.ui.ScreenHint;
import com.android.camera.ui.UIController;
import com.google.android.apps.photos.api.PhotosOemApi;
import java.util.ArrayList;

public abstract class ActivityBase extends Activity {
    protected boolean mActivityPaused;
    private CameraAppImpl mApplication;
    protected CameraBrightness mCameraBrightness;
    protected CameraScreenNail mCameraScreenNail;
    private MiuiCameraSound mCameraSound;
    private Thread mCloseActivityThread;
    protected Module mCurrentModule;
    protected final Handler mHandler = new C00741();
    protected boolean mHibernated;
    private boolean mIsFinishInKeygard = false;
    private int mJumpFlag = 0;
    private KeyguardManager mKeyguardManager;
    private boolean mKeyguardSecureLocked = false;
    private LocationManager mLocationManager;
    protected int mOrientation = -1;
    protected boolean mPaused;
    private Runnable mResetGotoGallery = new C00752();
    protected ScreenHint mScreenHint;
    private ArrayList<Uri> mSecureUriList;
    private boolean mStartFromKeyguard = false;
    private ThumbnailUpdater mThumbnailUpdater;
    protected UIController mUIController;

    class C00741 extends Handler {
        C00741() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (!ActivityBase.this.mPaused) {
                        ActivityBase.this.mUIController.showDebugInfo((String) message.obj);
                        return;
                    }
                    return;
                case 1:
                    ActivityBase.this.mIsFinishInKeygard = true;
                    return;
                case 2:
                    ActivityBase.this.getUIController().getHibernateHintView().setVisibility(0);
                    ActivityBase.this.getCameraScreenNail().requestHibernate();
                    ActivityBase.this.mCurrentModule.onPauseBeforeSuper();
                    ActivityBase.this.mCurrentModule.onPauseAfterSuper();
                    ActivityBase.this.mHibernated = true;
                    return;
                default:
                    return;
            }
        }
    }

    class C00752 implements Runnable {
        C00752() {
        }

        public void run() {
            Log.e("ActivityBase", "Time of starting gallery is too long, maybe it was killed at background.");
            ActivityBase.this.mJumpFlag = 0;
            ((BaseModule) ActivityBase.this.mCurrentModule).enableCameraControls(true);
        }
    }

    class C00763 implements Runnable {
        C00763() {
        }

        public void run() {
            ActivityBase.this.mApplication.closeAllActivitiesBut(ActivityBase.this);
        }
    }

    class C00774 implements Listener {
        C00774() {
        }

        public boolean isKeptBitmapTexture() {
            return ActivityBase.this.mCurrentModule.isKeptBitmapTexture();
        }

        public void onPreviewPixelsRead(byte[] bArr, int i, int i2) {
            ActivityBase.this.onPreviewPixelsRead(bArr, i, i2);
        }

        public void onPreviewTextureCopied() {
            ActivityBase.this.onPreviewTextureCopied();
        }

        public void onSwitchAnimationDone() {
            ActivityBase.this.mCurrentModule.onSwitchAnimationDone();
        }

        public void requestRender() {
            ActivityBase.this.mUIController.getGLView().requestRender();
            ActivityBase.this.mUIController.getEffectButton().requestEffectRender();
            ActivityBase.this.mCurrentModule.requestRender();
        }
    }

    private void checkKeyguardFlag() {
        this.mKeyguardSecureLocked = false;
        this.mStartFromKeyguard = getKeyguardFlag();
        boolean isAppLocked = Util.isAppLocked(this, "com.google.android.apps.photos");
        if (this.mStartFromKeyguard) {
            getWindow().addFlags(524288);
            if (this.mKeyguardManager.isKeyguardSecure()) {
                isAppLocked = true;
            }
            this.mKeyguardSecureLocked = isAppLocked;
            this.mIsFinishInKeygard = false;
            this.mHandler.sendEmptyMessage(1);
        } else {
            this.mKeyguardSecureLocked = isAppLocked;
        }
        if (this.mKeyguardSecureLocked) {
            if (this.mSecureUriList == null) {
                this.mSecureUriList = new ArrayList();
            }
            if (this.mThumbnailUpdater != null) {
                this.mThumbnailUpdater.setThumbnail(null);
            }
            AutoLockManager.getInstance(this).lockScreenDelayed();
        } else {
            this.mSecureUriList = null;
        }
        Log.v("ActivityBase", "checkKeyguard: fromKeyguard=" + this.mStartFromKeyguard + " keyguardSecureLocked=" + this.mKeyguardSecureLocked + " secureUriList is " + (this.mSecureUriList == null ? "null" : "not null"));
    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private long[] getSecureStoreIds() {
        if (this.mSecureUriList == null || this.mSecureUriList.isEmpty()) {
            return new long[0];
        }
        long[] jArr = new long[this.mSecureUriList.size()];
        int i = 0;
        for (Uri parseId : this.mSecureUriList) {
            jArr[i] = ContentUris.parseId(parseId);
            i++;
        }
        return jArr;
    }

    private void initCameraScreenNail() {
        if (this.mCameraScreenNail != null && this.mCameraScreenNail.getSurfaceTexture() == null) {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            this.mCameraScreenNail.setSize(point.x, point.y);
            this.mCameraScreenNail.acquireSurfaceTexture();
            this.mCameraScreenNail.setRenderArea(new Rect(0, 0, point.x, point.y));
        }
    }

    private void releaseCameraScreenNail() {
        if (this.mCameraScreenNail != null) {
            this.mCameraScreenNail.releaseSurfaceTexture();
        }
    }

    public void addSecureUri(Uri uri) {
        if (this.mSecureUriList != null) {
            if (this.mSecureUriList.size() == 100) {
                this.mSecureUriList.remove(0);
            }
            this.mSecureUriList.add(uri);
        }
    }

    public void createCameraScreenNail(boolean z, boolean z2) {
        if (this.mCameraScreenNail == null) {
            this.mCameraScreenNail = new CameraScreenNail(new C00774());
        }
        initCameraScreenNail();
    }

    public void dismissKeyguard() {
        if (this.mStartFromKeyguard) {
            sendBroadcast(new Intent("xiaomi.intent.action.SHOW_SECURE_KEYGUARD"));
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            if (this.mHibernated) {
                onAwaken();
                return false;
            }
            AutoLockManager.getInstance(this).hibernateDelayed();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public CameraAppImpl getCameraAppImpl() {
        return this.mApplication;
    }

    public CameraScreenNail getCameraScreenNail() {
        return this.mCameraScreenNail;
    }

    public Module getCurrentModule() {
        return this.mCurrentModule;
    }

    protected boolean getKeyguardFlag() {
        boolean z = false;
        Intent intent = getIntent();
        CharSequence action = intent == null ? "" : intent.getAction();
        if (TextUtils.equals("android.media.action.STILL_IMAGE_CAMERA_SECURE", action) || TextUtils.equals("android.media.action.IMAGE_CAPTURE_SECURE", action)) {
            return true;
        }
        if (this.mKeyguardManager != null && getIntent().getBooleanExtra("secure_camera", false)) {
            z = this.mKeyguardManager.isKeyguardLocked();
        }
        return z;
    }

    public ScreenHint getScreenHint() {
        return this.mScreenHint;
    }

    public ArrayList<Uri> getSecureUriList() {
        return this.mSecureUriList;
    }

    public long getSoundPlayTime() {
        return this.mCameraSound != null ? this.mCameraSound.getLastSoundPlayTime() : 0;
    }

    public ThumbnailUpdater getThumbnailUpdater() {
        return this.mThumbnailUpdater;
    }

    public UIController getUIController() {
        return this.mUIController;
    }

    public void gotoGallery() {
        if (!this.mPaused) {
            Thumbnail thumbnail = this.mThumbnailUpdater.getThumbnail();
            if (thumbnail != null) {
                Uri uri = thumbnail.getUri();
                if (Util.isUriValid(uri, getContentResolver())) {
                    try {
                        Intent intent = new Intent("com.android.camera.action.REVIEW");
                        if (ProcessingMediaManager.instance().isProcessingMedia(uri)) {
                            intent.setDataAndType(uri, "image/jpeg");
                        } else {
                            intent.setData(uri);
                        }
                        intent.setPackage("com.google.android.apps.photos");
                        intent.putExtra("from_MiuiCamera", true);
                        if (ProcessingMediaManager.instance().isProcessingMedia(uri)) {
                            intent.putExtra("processing_uri_intent_extra", PhotosOemApi.getQueryProcessingUri(this, ContentUris.parseId(uri)));
                        }
                        if (Device.adjustScreenLight() && this.mCameraBrightness.getCurrentBrightness() != -1) {
                            intent.putExtra("camera-brightness", this.mCameraBrightness.getCurrentBrightness());
                        }
                        if (startFromKeyguard()) {
                            intent.putExtra("com.google.android.apps.photos.api.secure_mode", true);
                        }
                        if (Util.isAppLocked(this, "com.google.android.apps.photos")) {
                            intent.putExtra("skip_interception", true);
                        }
                        if (this.mSecureUriList != null && startFromKeyguard()) {
                            intent.putExtra("com.google.android.apps.photos.api.secure_mode_ids", getSecureStoreIds());
                        }
                        startActivity(intent);
                        this.mJumpFlag = 1;
                        this.mHandler.postDelayed(this.mResetGotoGallery, 300);
                        ((BaseModule) this.mCurrentModule).enableCameraControls(false);
                    } catch (ActivityNotFoundException e) {
                        try {
                            startActivity(new Intent("android.intent.action.VIEW", uri));
                        } catch (Throwable e2) {
                            Log.e("ActivityBase", "review image fail. uri=" + uri, e2);
                        }
                    }
                } else {
                    Log.e("ActivityBase", "Uri invalid. uri=" + uri);
                    getThumbnailUpdater().getLastThumbnailUncached();
                }
            }
        }
    }

    public boolean handleMessage(int i, int i2, Object obj, Object obj2) {
        return false;
    }

    public boolean isActivityPaused() {
        return this.mActivityPaused;
    }

    public boolean isGotoGallery() {
        return this.mJumpFlag == 1;
    }

    public boolean isImageCaptureIntent() {
        return "android.media.action.IMAGE_CAPTURE".equals(getIntent().getAction());
    }

    public boolean isNeedResetGotoGallery() {
        if (this.mJumpFlag != 1) {
            return false;
        }
        this.mJumpFlag = 0;
        return true;
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public boolean isScanQRCodeIntent() {
        String action = getIntent().getAction();
        return !"com.android.camera.action.QR_CODE_CAPTURE".equals(action) ? "com.google.zxing.client.android.SCAN".equals(action) : true;
    }

    public boolean isVideoCaptureIntent() {
        return "android.media.action.VIDEO_CAPTURE".equals(getIntent().getAction());
    }

    public boolean isVoiceAssistantCaptureIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        if (!TextUtils.equals("android.media.action.STILL_IMAGE_CAMERA", intent.getAction())) {
            return false;
        }
        boolean booleanExtra = intent.getBooleanExtra("KEY_HANDOVER_THROUGH_VELVET", false);
        intent.removeExtra("KEY_HANDOVER_THROUGH_VELVET");
        boolean equals = TextUtils.equals(intent.getStringExtra("android.intent.extra.REFERRER_NAME"), "android-app://com.google.android.googlequicksearchbox/https/www.google.com");
        intent.removeExtra("android.intent.extra.REFERRER_NAME");
        if (booleanExtra) {
            equals = true;
        }
        return equals;
    }

    public void loadCameraSound(int i) {
        if (this.mCameraSound != null) {
            this.mCameraSound.load(i);
        }
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        this.mCurrentModule.onActivityResult(i, i2, intent);
    }

    public void onAwaken() {
        this.mCurrentModule.onResumeBeforeSuper();
        this.mCurrentModule.onResumeAfterSuper();
        this.mHibernated = false;
        getUIController().getHibernateHintView().setVisibility(8);
        getCameraScreenNail().requestAwaken();
    }

    public void onBackPressed() {
        if (this.mHibernated) {
            onAwaken();
        } else {
            super.onBackPressed();
        }
    }

    public void onCreate(Bundle bundle) {
        getWindow().addFlags(1024);
        getWindow().addFlags(2097152);
        super.onCreate(bundle);
        setVolumeControlStream(1);
        this.mScreenHint = new ScreenHint(this);
        this.mThumbnailUpdater = new ThumbnailUpdater(this);
        this.mKeyguardManager = (KeyguardManager) getSystemService("keyguard");
        this.mApplication = (CameraAppImpl) getApplication();
        this.mApplication.addActivity(this);
        this.mCameraBrightness = new CameraBrightness(this);
        this.mLocationManager = LocationManager.instance();
        this.mCloseActivityThread = new Thread(new C00763());
        try {
            this.mCloseActivityThread.start();
        } catch (IllegalThreadStateException e) {
        }
    }

    protected void onDestroy() {
        AutoLockManager.removeInstance(this);
        PopupManager.removeInstance(this);
        this.mApplication.removeActivity(this);
        if (this.mCameraSound != null) {
            this.mCameraSound.release();
            this.mCameraSound = null;
        }
        super.onDestroy();
    }

    public void onHibernate() {
        this.mHandler.sendEmptyMessage(2);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 84 && keyEvent.isLongPress()) {
            return true;
        }
        if (!this.mHibernated) {
            AutoLockManager.getInstance(this).hibernateDelayed();
        }
        return super.onKeyDown(i, keyEvent);
    }

    public void onLayoutChange(int i, int i2) {
        if (this.mCameraScreenNail != null) {
            if (Util.getDisplayRotation(this) % 180 == 0) {
                this.mCameraScreenNail.setPreviewFrameLayoutSize(i, i2);
            } else {
                this.mCameraScreenNail.setPreviewFrameLayoutSize(i2, i);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        pause();
        this.mHandler.removeCallbacks(this.mResetGotoGallery);
        if (this.mJumpFlag == 0 && startFromSecureKeyguard()) {
            this.mSecureUriList = null;
            this.mThumbnailUpdater.setThumbnail(null, true);
        } else if (this.mJumpFlag == 1) {
            clearNotification();
        }
        this.mHandler.removeMessages(1);
    }

    protected void onPreviewPixelsRead(byte[] bArr, int i, int i2) {
        this.mCurrentModule.onPreviewPixelsRead(bArr, i, i2);
    }

    protected void onPreviewTextureCopied() {
        this.mCurrentModule.onPreviewTextureCopied();
    }

    protected void onResume() {
        super.onResume();
        this.mJumpFlag = 0;
        this.mHibernated = false;
        checkKeyguardFlag();
        resume();
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onSaveInstanceState(bundle);
        }
    }

    public boolean onSearchRequested() {
        return false;
    }

    public void pause() {
        this.mUIController.getGLView().onPause();
        releaseCameraScreenNail();
        this.mCameraBrightness.onPause();
        if (this.mCloseActivityThread != null) {
            try {
                this.mCloseActivityThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mCloseActivityThread = null;
        }
        if (this.mLocationManager != null) {
            this.mLocationManager.recordLocation(false);
        }
        if (startFromKeyguard() && this.mIsFinishInKeygard) {
            getWindow().clearFlags(2097152);
            if (this.mJumpFlag == 0) {
                finish();
            }
        }
        if (this.mThumbnailUpdater != null) {
            this.mThumbnailUpdater.saveThumbnailToFile();
            this.mThumbnailUpdater.cancelTask();
        }
    }

    public void playCameraSound(int i) {
        this.mCameraSound.playSound(i);
    }

    public void resume() {
        if (this.mCameraSound == null) {
            this.mCameraSound = new MiuiCameraSound(this);
        }
        initCameraScreenNail();
        this.mUIController.getGLView().onResume();
        AutoLockManager.getInstance(this).onResume();
        this.mLocationManager.recordLocation(CameraSettings.isRecordLocation(CameraSettingPreferences.instance()));
        this.mCameraBrightness.onResume();
    }

    public void setJumpFlag(int i) {
        this.mJumpFlag = i;
    }

    public boolean startFromKeyguard() {
        return this.mStartFromKeyguard;
    }

    public boolean startFromSecureKeyguard() {
        return this.mKeyguardSecureLocked;
    }
}
