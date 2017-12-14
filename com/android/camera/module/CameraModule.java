package com.android.camera.module;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.camera.AudioCaptureManager;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraDisabledException;
import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraPreferenceActivity;
import com.android.camera.CameraSettings;
import com.android.camera.ChangeManager;
import com.android.camera.Device;
import com.android.camera.Exif;
import com.android.camera.ExifHelper;
import com.android.camera.FocusManager;
import com.android.camera.FocusManager.Listener;
import com.android.camera.JpegEncodingQualityMappings;
import com.android.camera.LocationManager;
import com.android.camera.OnClickAttr;
import com.android.camera.PictureSize;
import com.android.camera.PictureSizeManager;
import com.android.camera.SensorStateManager.SensorStateListener;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.EffectController.EffectRectAttribute;
import com.android.camera.effect.draw_mode.DrawJPEGAttribute;
import com.android.camera.effect.renders.SnapshotEffectRender;
import com.android.camera.groupshot.GroupShot;
import com.android.camera.hardware.CameraHardwareProxy.CameraHardwareFace;
import com.android.camera.hardware.CameraHardwareProxy.CameraMetaDataCallback;
import com.android.camera.permission.PermissionManager;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.Storage;
import com.android.camera.ui.FocusView;
import com.android.camera.ui.FrameView;
import com.android.camera.ui.ObjectView.ObjectViewListener;
import com.android.camera.ui.PopupManager;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.V6GestureRecognizer;
import com.android.camera.ui.V6ModulePicker;
import com.android.zxing.QRCodeManager;
import com.android.zxing.QRCodeManager.QRCodeManagerListener;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CameraModule extends BaseModule implements Listener, FaceDetectionListener, QRCodeManagerListener, ObjectViewListener, CameraMetaDataCallback {
    protected static final int BURST_SHOOTING_COUNT = Device.getBurstShootCount();
    protected boolean m3ALocked;
    private int mAFEndLogTimes;
    private boolean mAeLockSupported;
    protected AudioCaptureManager mAudioCaptureManager;
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private final AutoFocusMoveCallback mAutoFocusMoveCallback = new AutoFocusMoveCallback();
    public long mAutoFocusTime;
    protected boolean mAwbLockSupported;
    private String mBurstShotTitle;
    private BurstSpeedController mBurstSpeedController = new BurstSpeedController();
    protected CameraCategory mCameraCategory = new CameraCategory();
    private CameraStartUpThread mCameraStartUpThread;
    protected volatile int mCameraState = 0;
    private byte[] mCameraUUIDWatermarkImageData;
    public long mCaptureStartTime;
    private boolean mContinuousFocusSupported;
    private String mCropValue;
    private boolean mDidRegister = false;
    private int mDoCaptureRetry = 0;
    private Runnable mDoSnapRunnable = new C01001();
    private SnapshotEffectRender mEffectProcessor;
    protected boolean mFaceDetectionEnabled = true;
    private boolean mFaceDetectionStarted;
    private boolean mFirstTimeInitialized;
    private boolean mFocusAreaSupported;
    protected FocusManager mFocusManager;
    private long mFocusStartTime;
    protected boolean mFoundFace;
    private int mGroupFaceNum = 10;
    private GroupShot mGroupShot;
    private int mGroupShotTimes;
    protected final Handler mHandler = new MainHandler();
    private Parameters mInitialParams;
    private boolean mIsCaptureAfterLaunch;
    private boolean mIsCountDown;
    protected boolean mIsImageCaptureIntent;
    boolean mIsRecreateCameraScreenNail;
    private boolean mIsSaveCaptureImage;
    protected boolean mIsZSLMode;
    public long mJpegCallbackFinishTime;
    private byte[] mJpegImageData;
    private long mJpegPictureCallbackTime;
    protected int mJpegRotation;
    private boolean mKeepBitmapTexture;
    private long mLastFreezeHDRTime;
    private boolean mLastIsEffect;
    private long mLastShutterButtonClickTime;
    private boolean mLongPressedAutoFocus;
    private boolean mManualModeSwitched;
    private ContentProviderClient mMediaProviderClient;
    protected MetaDataManager mMetaDataManager = new MetaDataManager();
    private boolean mMeteringAreaSupported;
    protected Size mMultiSnapPictureSize;
    protected boolean mMultiSnapStatus = false;
    protected boolean mMultiSnapStopRequest = false;
    private boolean mNeedAutoFocus;
    private boolean mNeedSealCameraUUID;
    private long mOnResumeTime;
    private boolean mPendingCapture;
    private boolean mPendingMultiCapture;
    public long mPictureDisplayedToJpegCallbackTime;
    protected final PostViewPictureCallback mPostViewPictureCallback = new PostViewPictureCallback();
    private long mPostViewPictureCallbackTime;
    private int mPreviewHeight;
    private PreviewTextureCopiedCallback mPreviewTextureCopiedActionByPass = new C01023();
    private PreviewTextureCopiedCallback mPreviewTextureCopiedActionSwitchCamera = new C01034();
    private PreviewTextureCopiedCallback mPreviewTextureCopiedActionSwitchCameraLater = new C01045();
    private PreviewTextureCopiedCallback mPreviewTextureCopiedCallback;
    private int mPreviewWidth;
    protected boolean mQuickCapture;
    protected final RawPictureCallback mRawPictureCallback = new RawPictureCallback();
    private long mRawPictureCallbackTime;
    protected int mReceivedJpegCallbackNum = 0;
    private final BroadcastReceiver mReceiver = new C01012();
    protected boolean mRestartPreview = false;
    private Uri mSaveUri;
    protected String mSceneMode;
    private SensorStateListener mSensorStateListener = new C01056();
    private int mSetCameraParameter = 0;
    private boolean mSetMetaCallback;
    private int mShootOrientation;
    private float mShootRotation;
    protected final ShutterCallback mShutterCallback = new ShutterCallback();
    private long mShutterCallbackTime;
    public long mShutterLag;
    private boolean mSnapshotOnIdle = false;
    private ConditionVariable mStartPreviewPrerequisiteReady = new ConditionVariable();
    private boolean mSwitchCameraAnimationRunning;
    private Boolean mSwitchCameraLater;
    protected int mTotalJpegCallbackNum = 1;
    private boolean mUpdateImageTitle = false;
    private int mUpdateSet;
    protected boolean mVolumeLongPress = false;

    protected class CameraCategory {
        protected CameraCategory() {
        }

        public void takePicture(Location location) {
            CameraProxy cameraProxy = CameraModule.this.mCameraDevice;
            android.hardware.Camera.ShutterCallback shutterCallback = CameraModule.this.mShutterCallback;
            PictureCallback pictureCallback = CameraModule.this.mRawPictureCallback;
            PictureCallback pictureCallback2 = CameraModule.this.mPostViewPictureCallback;
            PictureCallback jpegPictureCallback = (CameraModule.this.mTotalJpegCallbackNum <= 2 || CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key")) ? new JpegPictureCallback(location) : new JpegQuickPictureCallback(location);
            cameraProxy.takePicture(shutterCallback, pictureCallback, pictureCallback2, jpegPictureCallback);
        }
    }

    protected class JpegPictureCallback implements PictureCallback {
        protected Location mLocation;
        private final boolean mPortraitMode = CameraSettings.isSwitchOn("pref_camera_portrait_mode_key");
        private final boolean mZSLEnabled;

        public JpegPictureCallback(Location location) {
            this.mLocation = location;
            this.mZSLEnabled = CameraModule.this.mIsZSLMode;
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            if (!CameraModule.this.mPaused) {
                CameraModule cameraModule = CameraModule.this;
                cameraModule.mReceivedJpegCallbackNum++;
                CameraModule.this.mJpegPictureCallbackTime = System.currentTimeMillis();
                if (CameraModule.this.mPostViewPictureCallbackTime != 0) {
                    CameraModule.this.mPictureDisplayedToJpegCallbackTime = CameraModule.this.mJpegPictureCallbackTime - CameraModule.this.mPostViewPictureCallbackTime;
                } else if (CameraModule.this.mRawPictureCallbackTime != 0) {
                    CameraModule.this.mPictureDisplayedToJpegCallbackTime = CameraModule.this.mJpegPictureCallbackTime - CameraModule.this.mRawPictureCallbackTime;
                } else {
                    CameraModule.this.mPictureDisplayedToJpegCallbackTime = CameraModule.this.mJpegPictureCallbackTime - CameraModule.this.mShutterCallbackTime;
                }
                Log.v("Camera", "mPictureDisplayedToJpegCallbackTime = " + CameraModule.this.mPictureDisplayedToJpegCallbackTime + "ms");
                CameraModule.this.mFocusManager.onShutter();
                if ((CameraModule.this.mReceivedJpegCallbackNum >= CameraModule.this.mTotalJpegCallbackNum || CameraModule.this.isGroupShotCapture()) && bArr != null) {
                    int i;
                    int i2;
                    Object obj = (Device.isHDRFreeze() && CameraModule.this.mMutexModePicker.isMorphoHdr() && !CameraModule.this.mIsImageCaptureIntent) ? 1 : null;
                    if (obj == null && CameraModule.this.mReceivedJpegCallbackNum == CameraModule.this.mTotalJpegCallbackNum) {
                        CameraModule.this.updateMutexModeUI(true);
                        if (!CameraModule.this.playAnimationBeforeCapture()) {
                            CameraModule.this.playSound(0);
                            CameraModule.this.animateSlide();
                        }
                    }
                    Size pictureSize = CameraModule.this.mParameters.getPictureSize();
                    int orientation = Exif.getOrientation(bArr);
                    if ((CameraModule.this.mJpegRotation + orientation) % 180 == 0) {
                        i = pictureSize.width;
                        i2 = pictureSize.height;
                    } else {
                        i = pictureSize.height;
                        i2 = pictureSize.width;
                    }
                    CameraModule.this.mBurstShotTitle = Util.createJpegName(System.currentTimeMillis()) + CameraModule.this.getSuffix();
                    String -get1 = CameraModule.this.mBurstShotTitle;
                    DrawJPEGAttribute drawJPEGAttribute = null;
                    if (EffectController.getInstance().hasEffect()) {
                        int max = i > i2 ? Math.max(CameraModule.this.mPreviewWidth, CameraModule.this.mPreviewHeight) : Math.min(CameraModule.this.mPreviewWidth, CameraModule.this.mPreviewHeight);
                        int max2 = i2 > i ? Math.max(CameraModule.this.mPreviewWidth, CameraModule.this.mPreviewHeight) : Math.min(CameraModule.this.mPreviewWidth, CameraModule.this.mPreviewHeight);
                        int effect = EffectController.getInstance().getEffect(false);
                        EffectRectAttribute copyEffectRectAttribute = EffectController.getInstance().copyEffectRectAttribute();
                        Location location = this.mLocation == null ? null : new Location(this.mLocation);
                        long currentTimeMillis = System.currentTimeMillis();
                        int shootOrientation = Util.getShootOrientation(CameraModule.this.mActivity, CameraModule.this.mShootOrientation);
                        float shootRotation = (EffectController.sGradienterIndex == EffectController.getInstance().getEffect(false) && CameraModule.this.mShootRotation == -1.0f) ? 0.0f : Util.getShootRotation(CameraModule.this.mActivity, CameraModule.this.mShootRotation);
                        boolean z = CameraModule.this.isFrontCamera() && !CameraModule.sProxy.isFrontMirror(CameraModule.this.mParameters);
                        drawJPEGAttribute = new DrawJPEGAttribute(bArr, max, max2, i, i2, effect, copyEffectRectAttribute, location, -get1, currentTimeMillis, shootOrientation, orientation, shootRotation, z, this.mPortraitMode);
                    }
                    CameraModule.this.trackPictureTaken(1, false, i, i2, this.mLocation != null);
                    if (CameraModule.this.mIsImageCaptureIntent) {
                        if (drawJPEGAttribute != null) {
                            CameraModule.this.mEffectProcessor.processorJpegSync(drawJPEGAttribute);
                            CameraModule.this.mJpegImageData = drawJPEGAttribute.mData;
                        } else {
                            CameraModule.this.mJpegImageData = bArr;
                        }
                        if (CameraModule.this.needReturnInvisibleWatermark()) {
                            String -wrap5 = CameraModule.this.getCameraUUID();
                            if (TextUtils.isEmpty(-wrap5)) {
                                -wrap5 = "no-fusion-id!";
                            }
                            CameraModule.this.mCameraUUIDWatermarkImageData = Util.sealInvisibleWatermark(CameraModule.this.mJpegImageData, (int) Math.floor((double) (((float) i) / 1000.0f)), CameraModule.buildWaterMarkForCameraUUID(-wrap5));
                        }
                        if (CameraModule.this.mQuickCapture) {
                            CameraModule.this.doAttach();
                        } else {
                            Bitmap createBitmap = Thumbnail.createBitmap(CameraModule.this.mJpegImageData, ((360 - CameraModule.this.mShootOrientation) + orientation) + CameraModule.this.mDisplayRotation, false, Integer.highestOneBit((int) Math.floor(((double) i) / ((double) CameraModule.this.mPreviewHeight))));
                            if (createBitmap != null) {
                                CameraModule.this.mActivity.getCameraScreenNail().renderBitmapToCanvas(createBitmap);
                                CameraModule.this.showPostCaptureAlert();
                                CameraModule.this.mKeepBitmapTexture = true;
                            }
                        }
                    } else {
                        if (drawJPEGAttribute != null) {
                            if (CameraModule.this.mEffectProcessor.processorJpegAsync(drawJPEGAttribute)) {
                                CameraModule.this.mLastIsEffect = true;
                            } else {
                                CameraModule.this.mBurstShotTitle = null;
                            }
                        } else if (CameraModule.this.isGroupShotCapture()) {
                            int attach = CameraModule.this.mGroupShot.attach(bArr);
                            com.android.camera.Log.m5v("Camera", String.format("mGroupShot attach() = 0x%08x index=%d", new Object[]{Integer.valueOf(attach), Integer.valueOf(CameraModule.this.mReceivedJpegCallbackNum)}));
                            if (CameraModule.this.mReceivedJpegCallbackNum < CameraModule.this.mTotalJpegCallbackNum) {
                                if (CameraModule.this.mReceivedJpegCallbackNum == 1 && CameraModule.this.mPreferences.getBoolean("pref_groupshot_with_primitive_picture_key", true)) {
                                    CameraModule.this.mActivity.getImageSaver().addImage(bArr, -get1, System.currentTimeMillis(), null, this.mLocation, i, i2, null, orientation, false, false, true);
                                }
                                if (CameraModule.this.needSetupPreview(this.mZSLEnabled)) {
                                    CameraModule.this.mCameraDevice.startPreview();
                                }
                                int i3 = (!CameraSettings.isFrontCamera() || CameraModule.this.getString(C0049R.string.pref_face_beauty_close).equals(CameraSettings.getFaceBeautifyValue())) ? 100 : 0;
                                CameraModule.this.mHandler.sendEmptyMessageDelayed(30, (long) i3);
                                return;
                            }
                            new SaveOutputImageTask(System.currentTimeMillis(), this.mLocation, i, i2, orientation, -get1, CameraModule.this.mGroupShot).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                            cameraModule = CameraModule.this;
                            cameraModule.mGroupShotTimes = cameraModule.mGroupShotTimes + 1;
                        } else {
                            CameraModule.this.mActivity.getImageSaver().addImage(bArr, -get1, null, System.currentTimeMillis(), null, this.mLocation, i, i2, null, orientation, false, false, true, this.mPortraitMode);
                        }
                        if (!(Device.isSupportedStereo() && CameraSettings.isSwitchOn("pref_camera_stereo_mode_key"))) {
                            CameraModule.this.setupPreview(this.mZSLEnabled);
                        }
                    }
                    long currentTimeMillis2 = System.currentTimeMillis();
                    CameraModule.this.mJpegCallbackFinishTime = currentTimeMillis2 - CameraModule.this.mJpegPictureCallbackTime;
                    Log.v("Camera", "mJpegCallbackFinishTime = " + CameraModule.this.mJpegCallbackFinishTime + "ms");
                    CameraModule.this.mCaptureStartTime = currentTimeMillis2 - CameraModule.this.mCaptureStartTime;
                    Log.d("Camera", "mCaptureStartTime(from onShutterButtonClick start to jpegCallback finished) = " + CameraModule.this.mCaptureStartTime + "ms");
                    if (!CameraModule.this.mHandler.hasMessages(24)) {
                        CameraModule.this.mHandler.sendEmptyMessage(27);
                    }
                }
            }
        }

        public void setLocation(Location location) {
            this.mLocation = location;
        }
    }

    class C01001 implements Runnable {
        C01001() {
        }

        public void run() {
            CameraModule.this.onShutterButtonClick();
        }
    }

    class C01012 extends BroadcastReceiver {
        C01012() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("Camera", "Received intent action=" + action);
            if (action.equals("android.intent.action.MEDIA_MOUNTED") || action.equals("android.intent.action.MEDIA_UNMOUNTED") || action.equals("android.intent.action.MEDIA_CHECKING")) {
                CameraModule.this.mActivity.getScreenHint().updateHint();
            } else if (action.equals("android.intent.action.MEDIA_SCANNER_FINISHED")) {
                CameraModule.this.mActivity.getScreenHint().updateHint();
                if (!CameraModule.this.mIsImageCaptureIntent) {
                    CameraModule.this.mActivity.getThumbnailUpdater().getLastThumbnail();
                }
            } else if (action.equals("android.intent.action.MEDIA_EJECT") && Storage.isCurrentStorageIsSecondary()) {
                Storage.switchToPhoneStorage();
            }
        }
    }

    private interface PreviewTextureCopiedCallback {
        void onPreviewTextureCopied();
    }

    class C01023 implements PreviewTextureCopiedCallback {
        C01023() {
        }

        public void onPreviewTextureCopied() {
            CameraModule.this.mActivity.getCameraScreenNail().animateSwitchCameraBefore();
            CameraModule.this.mHandler.sendEmptyMessage(19);
        }
    }

    class C01034 implements PreviewTextureCopiedCallback {
        C01034() {
        }

        public void onPreviewTextureCopied() {
            CameraModule.this.animateSwitchCamera();
            CameraModule.this.mHandler.sendEmptyMessage(6);
        }
    }

    class C01045 implements PreviewTextureCopiedCallback {
        C01045() {
        }

        public void onPreviewTextureCopied() {
            CameraModule.this.animateSwitchCamera();
            synchronized (CameraModule.this.mSwitchCameraLater) {
                if (!CameraModule.this.mSwitchCameraLater.booleanValue()) {
                    CameraModule.this.mHandler.sendEmptyMessage(6);
                }
                CameraModule.this.mSwitchCameraLater = Boolean.valueOf(false);
            }
        }
    }

    class C01056 implements SensorStateListener {
        C01056() {
        }

        public boolean isWorking() {
            return CameraModule.this.mCameraState != 0;
        }

        public void notifyDevicePostureChanged() {
            CameraModule.this.getUIController().getEdgeShutterView().onDevicePostureChanged();
        }

        public void onDeviceBecomeStable() {
        }

        public void onDeviceBeginMoving() {
            if (!CameraModule.this.mPaused && CameraSettings.isEdgePhotoEnable()) {
                CameraModule.this.getUIController().getEdgeShutterView().onDeviceMoving();
            }
        }

        public void onDeviceKeepMoving(double d) {
            if (!CameraModule.this.mPaused && CameraModule.this.mFocusManager != null && !CameraModule.this.mMultiSnapStatus && !CameraModule.this.getUIController().getShutterButton().isPressed() && !CameraModule.this.m3ALocked && !CameraModule.this.getUIController().getFocusView().isEvAdjustedTime()) {
                CameraModule.this.mFocusManager.onDeviceKeepMoving(d);
            }
        }

        public void onDeviceOrientationChanged(float f, boolean z) {
            if (CameraSettings.isSwitchOn("pref_camera_gradienter_key")) {
                CameraModule.this.mDeviceRotation = f;
                if (CameraModule.this.mCameraState != 3) {
                    EffectController.getInstance().setDeviceRotation(CameraModule.this.mActivity.getSensorStateManager().isDeviceLying(), Util.getShootRotation(CameraModule.this.mActivity, CameraModule.this.mDeviceRotation));
                }
                CameraModule.this.mHandler.removeMessages(33);
                if (!CameraModule.this.mPaused) {
                    CameraModule.this.mHandler.obtainMessage(33, Math.round(f), 0).sendToTarget();
                }
            }
            if (!CameraModule.this.mPaused && CameraModule.this.mActivity.getSensorStateManager().canDetectOrientation()) {
                CameraModule.this.mHandler.removeMessages(39);
                CameraModule.this.mHandler.obtainMessage(39, Boolean.valueOf(z)).sendToTarget();
            }
        }
    }

    private final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {
        private AutoFocusCallback() {
        }

        public void onAutoFocus(boolean z, Camera camera) {
            if (!CameraModule.this.mPaused && !CameraModule.this.mActivity.getCameraScreenNail().isModuleSwitching()) {
                CameraModule.this.mAutoFocusTime = System.currentTimeMillis() - CameraModule.this.mFocusStartTime;
                Log.v("Camera", "mAutoFocusTime = " + CameraModule.this.mAutoFocusTime + "ms" + " focused=" + z);
                if (!(CameraModule.this.mFocusManager.isFocusingSnapOnFinish() || CameraModule.this.mCameraState == 3)) {
                    CameraModule.this.setCameraState(1);
                }
                CameraModule.this.mFocusManager.onAutoFocus(z);
                CameraModule.this.mActivity.getSensorStateManager().reset();
            }
        }
    }

    private final class AutoFocusMoveCallback implements android.hardware.Camera.AutoFocusMoveCallback {
        private AutoFocusMoveCallback() {
        }

        public void onAutoFocusMoving(boolean z, Camera camera) {
            if (!CameraModule.this.mPaused) {
                Log.v("Camera", "onAutoFocusMoving moving=" + z + " " + CameraModule.this.mCameraState);
                CameraModule.this.getUIController().getFocusView().setFocusType(false);
                boolean isFocusSuccessful = z ? false : CameraModule.this.mCameraDevice.isFocusSuccessful();
                Object obj = null;
                String str = null;
                if (z) {
                    str = "onAutoFocusMoving start";
                    CameraModule.this.mAFEndLogTimes = 0;
                } else if (CameraModule.this.mAFEndLogTimes == 0) {
                    str = "onAutoFocusMoving end. result=" + isFocusSuccessful;
                    obj = 1;
                    CameraModule cameraModule = CameraModule.this;
                    cameraModule.mAFEndLogTimes = cameraModule.mAFEndLogTimes + 1;
                }
                if (Util.sIsDumpLog && str != null) {
                    Log.v("Camera", str);
                }
                if ((CameraModule.this.mCameraState != 3 || CameraModule.this.mHandler.hasMessages(36)) && !CameraModule.this.mActivity.getCameraScreenNail().isModuleSwitching()) {
                    CameraModule.this.mFocusManager.onAutoFocusMoving(z, isFocusSuccessful);
                }
                if (obj != null) {
                    QRCodeManager.instance(CameraModule.this.mActivity).requestDecode();
                }
            }
        }
    }

    private class BurstSpeedController {
        private long mBurstStartTime;

        private BurstSpeedController() {
        }

        private int getBurstSpeed() {
            long currentTimeMillis = System.currentTimeMillis() - this.mBurstStartTime;
            int i = 0;
            if (CameraModule.this.mReceivedJpegCallbackNum > 0 && currentTimeMillis != 0) {
                i = (int) (((long) (CameraModule.this.mReceivedJpegCallbackNum * 1000)) / currentTimeMillis);
            }
            if (Util.sIsDumpLog) {
                Log.v("Camera", "current burst Speed is " + i + " fps");
            }
            return i;
        }

        private void onPictureTaken() {
            if (!Device.isMTKPlatform()) {
                return;
            }
            if (CameraModule.this.mActivity.getImageSaver().isNeedStopCapture() && CameraModule.this.mReceivedJpegCallbackNum >= 1) {
                CameraModule.this.onShutterButtonFocus(false, 2);
            } else if (CameraModule.this.mActivity.getImageSaver().isNeedSlowDown()) {
                int burstSpeed = (int) (((float) getBurstSpeed()) * CameraModule.this.mActivity.getImageSaver().getSuitableBurstShotSpeed());
                if (burstSpeed == 0) {
                    burstSpeed = 1;
                    Log.d("Camera", "current performance is very poor, will set the speed = 1 to native ");
                }
                if (Util.sIsDumpLog) {
                    Log.v("Camera", "set BurstShotSpeed to " + burstSpeed + " fps");
                }
                CameraModule.this.mCameraDevice.setBurstShotSpeed(burstSpeed);
            }
        }

        private void onShutter() {
            if (this.mBurstStartTime == 0 && CameraModule.this.isLongShotMode()) {
                this.mBurstStartTime = System.currentTimeMillis();
            }
            if (Device.isQcomPlatform() && !Device.IS_MI2 && !Device.IS_MI2A && CameraModule.this.isLongShotMode()) {
                if (!CameraModule.this.mActivity.getImageSaver().isNeedStopCapture() || CameraModule.this.mReceivedJpegCallbackNum < 1) {
                    int burstDelay = CameraModule.this.mActivity.getImageSaver().getBurstDelay();
                    if (burstDelay == 0) {
                        CameraModule.this.sendBurstCommand();
                        return;
                    } else {
                        CameraModule.this.mHandler.sendEmptyMessageDelayed(30, (long) burstDelay);
                        return;
                    }
                }
                CameraModule.this.onShutterButtonFocus(false, 2);
            }
        }

        public void capture() {
            this.mBurstStartTime = 0;
        }
    }

    private class CameraStartUpThread extends Thread {
        private volatile boolean mCancelled;

        private CameraStartUpThread() {
        }

        public void cancel() {
            this.mCancelled = true;
        }

        public void run() {
            try {
                if (!this.mCancelled) {
                    CameraDataAnalytics.instance().trackEventTime("open_camera_times_key");
                    CameraModule.this.prepareOpenCamera();
                    CameraModule.this.mCameraDevice = Util.openCamera(CameraModule.this.mActivity, CameraModule.this.mCameraId);
                    CameraModule.this.mCameraDevice.setHardwareListener(CameraModule.this);
                    if (!this.mCancelled) {
                        Log.v("Camera", "CameraStartUpThread mCameraDevice=" + CameraModule.this.mCameraDevice + " " + CameraModule.this);
                        CameraModule.this.mParameters = CameraModule.this.mCameraDevice.getParameters();
                        CameraModule.this.initializeCapabilities();
                        if (CameraModule.this.mInitialParams == null || CameraModule.this.mParameters == null) {
                            throw new CameraHardwareException(new Exception("Failed to get parameters"));
                        }
                        CameraModule.this.mStartPreviewPrerequisiteReady.block();
                        if (CameraModule.this.mFocusManager == null) {
                            CameraModule.this.initializeFocusManager();
                        }
                        if (!this.mCancelled) {
                            CameraModule.this.setDisplayOrientation();
                            CameraModule.this.setCameraParameters(-1);
                            CameraModule.this.mHandler.sendEmptyMessage(8);
                            if (!this.mCancelled) {
                                if (CameraModule.this.mIsImageCaptureIntent && CameraModule.this.getUIController().getReviewDoneView().getVisibility() == 0) {
                                    CameraModule.this.mHandler.sendEmptyMessageDelayed(25, 30);
                                } else {
                                    CameraModule.this.startPreview();
                                }
                                CameraModule.this.onCameraStartPreview();
                                CameraModule.this.mHandler.sendEmptyMessage(9);
                                CameraModule.this.mOnResumeTime = SystemClock.uptimeMillis();
                                CameraModule.this.mHandler.sendEmptyMessage(4);
                                CameraModule.this.mHandler.sendEmptyMessage(31);
                                Log.v("Camera", "CameraStartUpThread done");
                            }
                        }
                    }
                }
            } catch (CameraHardwareException e) {
                CameraModule.this.mCameraStartUpThread = null;
                CameraModule.this.mOpenCameraFail = true;
                CameraModule.this.mHandler.sendEmptyMessage(10);
            } catch (CameraDisabledException e2) {
                CameraModule.this.mCameraStartUpThread = null;
                CameraModule.this.mCameraDisabled = true;
                CameraModule.this.mHandler.sendEmptyMessage(10);
            }
        }
    }

    private final class JpegQuickPictureCallback implements PictureCallback {
        Location mLocation;
        String mPressDownTitle;
        private final boolean mZSLEnabled;

        public JpegQuickPictureCallback(Location location) {
            this.mLocation = location;
            this.mZSLEnabled = CameraModule.this.mIsZSLMode;
        }

        private String getBurstShotTitle() {
            if (CameraModule.this.mUpdateImageTitle && CameraModule.this.mBurstShotTitle != null && CameraModule.this.mReceivedJpegCallbackNum == 1) {
                this.mPressDownTitle = CameraModule.this.mBurstShotTitle;
                CameraModule.this.mBurstShotTitle = null;
            }
            if (CameraModule.this.mBurstShotTitle == null) {
                long currentTimeMillis = System.currentTimeMillis();
                CameraModule.this.mBurstShotTitle = Util.createJpegName(currentTimeMillis);
                if (CameraModule.this.mBurstShotTitle.length() != 19) {
                    CameraModule.this.mBurstShotTitle = Util.createJpegName(1000 + currentTimeMillis);
                }
            }
            return CameraModule.this.mBurstShotTitle + (CameraModule.this.mMutexModePicker.isUbiFocus() ? "_UBIFOCUS_" + (CameraModule.this.mReceivedJpegCallbackNum - 1) : "_BURST" + CameraModule.this.mReceivedJpegCallbackNum);
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            if (!CameraModule.this.mPaused && bArr != null && CameraModule.this.mReceivedJpegCallbackNum < CameraModule.this.mTotalJpegCallbackNum && (CameraModule.this.mMutexModePicker.isUbiFocus() || CameraModule.this.isLongShotMode())) {
                if (CameraModule.this.mReceivedJpegCallbackNum == 1 && !CameraModule.this.mMultiSnapStopRequest) {
                    CameraModule.this.mFocusManager.onShutter();
                    if (!CameraModule.this.mMutexModePicker.isUbiFocus() && CameraModule.this.mUpdateImageTitle) {
                        if (CameraModule.this.mLastIsEffect) {
                            CameraModule.this.mEffectProcessor.changeJpegTitle(getBurstShotTitle(), this.mPressDownTitle);
                        } else {
                            CameraModule.this.mActivity.getImageSaver().updateImage(getBurstShotTitle(), this.mPressDownTitle);
                        }
                    }
                }
                CameraModule cameraModule;
                if (Storage.isLowStorageAtLastPoint()) {
                    if (!CameraModule.this.mMutexModePicker.isUbiFocus() && CameraModule.this.mMultiSnapStatus) {
                        cameraModule = CameraModule.this;
                        int i = CameraModule.this.mReceivedJpegCallbackNum;
                        int i2 = CameraModule.this.mMultiSnapPictureSize.width;
                        int i3 = CameraModule.this.mMultiSnapPictureSize.height;
                        boolean z = this.mLocation != null ? this.mLocation.getLatitude() == 0.0d ? this.mLocation.getLongitude() != 0.0d : true : false;
                        cameraModule.trackPictureTaken(i, true, i2, i3, z);
                        CameraModule.this.stopMultiSnap();
                    }
                    return;
                }
                int i4;
                int i5;
                if (!CameraModule.this.mMutexModePicker.isUbiFocus()) {
                    CameraModule.this.playSound(4);
                }
                cameraModule = CameraModule.this;
                cameraModule.mReceivedJpegCallbackNum++;
                CameraModule.this.getUIController().getMultiSnapNum().setText("" + CameraModule.this.mReceivedJpegCallbackNum);
                boolean z2 = CameraModule.this.mMutexModePicker.isUbiFocus() && CameraModule.this.mReceivedJpegCallbackNum <= CameraModule.this.mTotalJpegCallbackNum;
                int orientation = z2 ? 0 : Exif.getOrientation(bArr);
                if ((CameraModule.this.mJpegRotation + orientation) % 180 == 0) {
                    i4 = CameraModule.this.mMultiSnapPictureSize.width;
                    i5 = CameraModule.this.mMultiSnapPictureSize.height;
                } else {
                    i4 = CameraModule.this.mMultiSnapPictureSize.height;
                    i5 = CameraModule.this.mMultiSnapPictureSize.width;
                }
                String burstShotTitle = getBurstShotTitle();
                boolean z3 = CameraModule.this.mMutexModePicker.isUbiFocus() && CameraModule.this.mReceivedJpegCallbackNum == CameraModule.this.mTotalJpegCallbackNum - 1;
                Object obj = (CameraModule.this.mMutexModePicker.isUbiFocus() && CameraModule.this.mReceivedJpegCallbackNum == CameraModule.this.mTotalJpegCallbackNum) ? 1 : null;
                if (obj == null) {
                    CameraModule.this.mActivity.getImageSaver().addImage(bArr, burstShotTitle, System.currentTimeMillis(), (Uri) null, this.mLocation, i4, i5, null, orientation, z2, z3, true);
                }
                if (CameraModule.this.mReceivedJpegCallbackNum >= CameraModule.this.mTotalJpegCallbackNum || CameraModule.this.mMultiSnapStopRequest) {
                    CameraModule.this.mCaptureStartTime = System.currentTimeMillis() - CameraModule.this.mCaptureStartTime;
                    if (CameraModule.this.mMutexModePicker.isUbiFocus()) {
                        CameraModule.this.updateMutexModeUI(true);
                        CameraModule.this.setupPreview(this.mZSLEnabled);
                    } else {
                        CameraModule.this.stopMultiSnap();
                    }
                    CameraModule.this.trackPictureTaken(!CameraModule.this.mMutexModePicker.isUbiFocus() ? CameraModule.this.mReceivedJpegCallbackNum : 1, !CameraModule.this.mMutexModePicker.isUbiFocus(), i4, i5, this.mLocation != null);
                    Log.d("Camera", "Burst shooting finished. Total:" + CameraModule.this.mReceivedJpegCallbackNum + "pictures, " + "cost consuming:" + CameraModule.this.mCaptureStartTime + "ms");
                } else if (CameraModule.this.mMutexModePicker.isUbiFocus() && z3 && !Util.isProduceFocusInfoSuccess(bArr)) {
                    CameraModule.this.updateWarningMessage(C0049R.string.ubi_focus_capture_fail, false);
                } else {
                    CameraModule.this.mBurstSpeedController.onPictureTaken();
                }
            }
        }
    }

    private final class JpegQuickShutterCallback implements android.hardware.Camera.ShutterCallback {
        private JpegQuickShutterCallback() {
        }

        public void onShutter() {
            CameraModule.this.mShutterCallbackTime = System.currentTimeMillis();
            CameraModule.this.mShutterLag = CameraModule.this.mShutterCallbackTime - CameraModule.this.mCaptureStartTime;
            Log.v("Camera", "mShutterLag = " + CameraModule.this.mShutterLag + "ms");
            CameraModule.this.mBurstSpeedController.onShutter();
        }
    }

    private class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1:
                    CameraModule.this.initializeFirstTime();
                    return;
                case 2:
                    CameraModule.this.getWindow().clearFlags(128);
                    return;
                case 3:
                    CameraModule.this.setCameraParametersWhenIdle(0);
                    return;
                case 4:
                    CameraModule.this.checkActivityOrientation();
                    if (SystemClock.uptimeMillis() - CameraModule.this.mOnResumeTime < 5000) {
                        CameraModule.this.mHandler.sendEmptyMessageDelayed(4, 100);
                        return;
                    }
                    return;
                case 5:
                    CameraModule.this.mIgnoreFocusChanged = true;
                    CameraModule.this.mActivity.getScreenHint().showFirstUseHint();
                    return;
                case 6:
                    CameraModule.this.switchCamera();
                    return;
                case 7:
                    CameraModule.this.mActivity.getCameraScreenNail().animateSwitchCameraBefore();
                    return;
                case 8:
                    CameraModule.this.initializeAfterCameraOpen();
                    return;
                case 9:
                    CameraModule.this.mCameraStartUpThread = null;
                    CameraModule.this.mActivity.getCameraScreenNail().animateModuleChangeAfter();
                    CameraModule.this.getUIController().onCameraOpen();
                    CameraModule.this.mHandler.sendEmptyMessageDelayed(22, 100);
                    CameraModule.this.getUIController().getFocusView().initialize(CameraModule.this);
                    CameraModule.this.getUIController().getObjectView().setObjectViewListener(CameraModule.this);
                    CameraModule.this.updateModePreference();
                    if ((CameraModule.this.mCameraState == 0 || 1 == CameraModule.this.mCameraState) && !(CameraModule.this.mIsImageCaptureIntent && CameraModule.this.getUIController().getReviewDoneView().getVisibility() == 0)) {
                        CameraModule.this.setCameraState(1);
                    }
                    CameraModule.this.onSettingsBack();
                    CameraModule.this.startFaceDetection();
                    CameraModule.this.takeAPhotoIfNeeded();
                    return;
                case 10:
                    CameraModule.this.onCameraException();
                    return;
                case 12:
                    if (CameraModule.this.mCameraState == 3 || CameraModule.this.mFocusManager.isFocusingSnapOnFinish()) {
                        CameraModule.this.mPendingMultiCapture = true;
                        return;
                    } else if (CameraModule.this.mCameraState == 3) {
                        return;
                    } else {
                        if (!Device.isHDRFreeze() || Util.isTimeout(System.currentTimeMillis(), CameraModule.this.mLastFreezeHDRTime, 800)) {
                            if (!CameraModule.this.mMutexModePicker.isNormal()) {
                                CameraModule.this.mMutexModePicker.resetMutexMode();
                            }
                            CameraModule.this.mFocusManager.doMultiSnap(true);
                            return;
                        }
                        return;
                    }
                case 15:
                    if (CameraModule.this.isShutterButtonClickable()) {
                        CameraModule.this.onShutterButtonFocus(true, 3);
                        CameraModule.this.onShutterButtonClick();
                        CameraModule.this.onShutterButtonFocus(false, 0);
                        return;
                    } else if (CameraModule.this.mDoCaptureRetry < 20) {
                        CameraModule cameraModule = CameraModule.this;
                        cameraModule.mDoCaptureRetry = cameraModule.mDoCaptureRetry + 1;
                        Log.d("Camera", "retry do-capture: " + CameraModule.this.mDoCaptureRetry);
                        CameraModule.this.mHandler.sendEmptyMessageDelayed(15, 200);
                        return;
                    } else {
                        return;
                    }
                case 17:
                    CameraModule.this.mHandler.removeMessages(17);
                    CameraModule.this.mHandler.removeMessages(2);
                    CameraModule.this.getWindow().addFlags(128);
                    CameraModule.this.mHandler.sendEmptyMessageDelayed(2, (long) CameraModule.this.getScreenDelay());
                    return;
                case 18:
                    CameraSettings.changeUIByPreviewSize(CameraModule.this.mActivity, CameraModule.this.mUIStyle);
                    CameraModule.this.changePreviewSurfaceSize();
                    return;
                case 19:
                    if (CameraModule.this.mHasPendingSwitching) {
                        CameraModule.this.updateCameraScreenNailSize(CameraModule.this.mPreviewWidth, CameraModule.this.mPreviewHeight, CameraModule.this.mFocusManager);
                        CameraSettings.changeUIByPreviewSize(CameraModule.this.mActivity, CameraModule.this.mUIStyle);
                        CameraModule.this.changePreviewSurfaceSize();
                        CameraModule.this.mHasPendingSwitching = false;
                    } else if (CameraModule.this.isSquareModeChange()) {
                        CameraModule.this.updateCameraScreenNailSize(CameraModule.this.mPreviewWidth, CameraModule.this.mPreviewHeight, CameraModule.this.mFocusManager);
                    }
                    CameraModule.this.mActivity.getCameraScreenNail().switchCameraDone();
                    CameraModule.this.mSwitchingCamera = false;
                    CameraModule.this.mSwitchCameraAnimationRunning = false;
                    return;
                case 20:
                    if (message.arg1 > 0) {
                        Message obtainMessage = obtainMessage(20);
                        int i = message.arg1 - 1;
                        message.arg1 = i;
                        obtainMessage.arg1 = i;
                        obtainMessage.arg2 = message.arg2;
                        CameraModule.this.mAudioCaptureManager.setDelayStep(obtainMessage.arg1);
                        sendMessageDelayed(obtainMessage, (long) obtainMessage.arg2);
                        if (obtainMessage.arg1 < 3) {
                            CameraModule.this.playSound(5);
                            return;
                        }
                        return;
                    }
                    CameraModule.this.mAudioCaptureManager.hideDelayNumber();
                    CameraModule.this.sendDoCaptureMessage(1);
                    CameraModule.this.traceDelayCaptureEvents();
                    return;
                case 21:
                    CameraModule.this.updateWarningMessage(0, true);
                    return;
                case 22:
                    if (Device.isMDPRender()) {
                        CameraModule.this.getUIController().getSurfaceViewFrame().setSurfaceViewVisible(false);
                    }
                    if (CameraModule.this.getUIController().getSettingPage().getVisibility() != 0) {
                        CameraModule.this.mActivity.setBlurFlag(false);
                        return;
                    }
                    return;
                case 23:
                    CameraModule.this.mActivity.getScreenHint().showObjectTrackHint(CameraModule.this.mPreferences);
                    return;
                case 24:
                    if (Device.isHDRFreeze() && CameraModule.this.mMutexModePicker.isMorphoHdr()) {
                        CameraModule.this.updateMutexModeUI(true);
                        if (!CameraModule.this.playAnimationBeforeCapture()) {
                            CameraModule.this.playSound(0);
                            CameraModule.this.animateSlide();
                        }
                        CameraModule.this.setCameraState(message.arg1);
                        CameraModule.this.startFaceDetection();
                        CameraModule.this.mLastFreezeHDRTime = System.currentTimeMillis();
                        return;
                    }
                    return;
                case 25:
                    if (CameraModule.this.getUIController().getGLView().isBusy()) {
                        CameraModule.this.mHandler.sendEmptyMessageDelayed(25, 30);
                        return;
                    } else {
                        CameraModule.this.getUIController().getGLView().requestRender();
                        return;
                    }
                case 27:
                    if (CameraModule.this.mPendingMultiCapture) {
                        CameraModule.this.mPendingMultiCapture = false;
                        if (!CameraModule.this.mMutexModePicker.isNormal()) {
                            CameraModule.this.mMutexModePicker.resetMutexMode();
                        }
                        CameraModule.this.mFocusManager.doMultiSnap(true);
                        return;
                    }
                    return;
                case 28:
                    CameraModule.this.enableCameraControls(true);
                    return;
                case 29:
                    if (!CameraModule.this.mPaused) {
                        CameraModule.this.playSound(7);
                        CameraModule.this.mAudioCaptureManager.onClick();
                        return;
                    }
                    return;
                case 30:
                    if (CameraModule.this.isGroupShotCapture()) {
                        CameraModule.this.mCameraDevice.takePicture(null, null, null, new JpegPictureCallback(LocationManager.instance().getCurrentLocation()));
                        return;
                    } else {
                        CameraModule.this.sendBurstCommand();
                        return;
                    }
                case 31:
                    CameraModule.this.setOrientationParameter();
                    return;
                case 32:
                    CameraModule.this.applyPreferenceChange();
                    return;
                case 33:
                    CameraModule.this.setOrientation(message.arg1);
                    return;
                case 34:
                    if (message.arg1 > 0) {
                        if (message.obj != null && (message.obj instanceof FaceDetectionListener)) {
                            CameraModule.this.mCameraDevice.setFaceDetectionListener((FaceDetectionListener) message.obj);
                        }
                        CameraModule.this.updateFaceView(true, true);
                        return;
                    } else if (CameraModule.this.mFaceDetectionEnabled) {
                        CameraModule.this.startFaceDetection();
                        return;
                    } else {
                        CameraModule.this.updateFaceView(false, true);
                        return;
                    }
                case 35:
                    CameraModule cameraModule2 = CameraModule.this;
                    boolean z2 = message.arg1 > 0;
                    if (message.arg2 <= 0) {
                        z = false;
                    }
                    cameraModule2.handleUpdateFaceView(z2, z);
                    return;
                case 36:
                    CameraModule.this.setCameraState(1);
                    CameraModule.this.startFaceDetection();
                    return;
                case 37:
                    Log.e("Camera", "No continuous shot callback!", new RuntimeException());
                    CameraModule.this.handleMultiSnapDone();
                    return;
                case 39:
                    if (CameraModule.this.getUIController().getReviewDoneView().getVisibility() != 0) {
                        CameraModule.this.getUIController().getOrientationIndicator().updateVisible(((Boolean) message.obj).booleanValue());
                        return;
                    }
                    return;
                case 40:
                    if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") || CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                        CameraModule.this.updateWarningMessage(C0049R.string.dual_camera_use_hint, false);
                        return;
                    }
                    return;
                case 41:
                    if (CameraSettings.isSupportedOpticalZoom()) {
                        CameraModule.this.onCameraPickerClicked(CameraHolder.instance().getBackCameraId());
                        return;
                    }
                    return;
                case 42:
                    CameraModule.this.getUIController().updateThumbnailView((Thumbnail) message.obj);
                    return;
                case 43:
                    CameraModule.this.enableCameraControls(true);
                    return;
                case 44:
                    int countDownTimes = CameraSettings.getCountDownTimes();
                    CameraModule.this.sendDelayedCaptureMessage(1000, countDownTimes);
                    if (countDownTimes > 3) {
                        CameraModule.this.playSound(7);
                    }
                    CameraModule.this.mIsCountDown = true;
                    return;
                default:
                    return;
            }
        }
    }

    protected class MetaDataManager {
        private int mCurrentScene = -1;
        private int mLastScene = -1;
        private int mLastestState = -1;
        private int mLastestTimes = 0;
        private int mMetaType = 0;
        private int mSceneShieldMask = 255;

        private void applyScene(int i) {
            Log.v("CameraMetaDataManager", "applyScene " + i);
            switch (i) {
                case 0:
                    CameraModule.this.getUIController().getAsdIndicator().setImageResource(C0049R.drawable.v6_ic_indicator_asd_flash);
                    CameraModule.this.getUIController().getAsdIndicator().setVisibility(0);
                    if (CameraSettings.isAsdPopupEnable()) {
                        CameraModule.this.getUIController().getFlashButton().updatePopup(true);
                        return;
                    }
                    return;
                case 1:
                    CameraModule.this.mMutexModePicker.setMutexMode(1);
                    CameraModule.this.getUIController().getAsdIndicator().setImageResource(C0049R.drawable.v6_ic_indicator_asd_hdr);
                    CameraModule.this.getUIController().getAsdIndicator().setVisibility(0);
                    return;
                case 2:
                    CameraModule.this.mMutexModePicker.setMutexMode(3);
                    return;
                case 3:
                    CameraModule.this.mMutexModePicker.setMutexMode(3);
                    return;
                case 4:
                    CameraModule.this.updateWarningMessage(C0049R.string.portrait_mode_too_close_hint, false, false);
                    return;
                case 5:
                    CameraModule.this.updateWarningMessage(C0049R.string.portrait_mode_too_far_hint, false, false);
                    return;
                case 6:
                    CameraModule.this.updateWarningMessage(C0049R.string.portrait_mode_lowlight_hint, false, false);
                    return;
                case 7:
                    CameraModule.this.getUIController().getPortraitButton().showHintText();
                    return;
                default:
                    return;
            }
        }

        private int detectASDScene(int i) {
            i &= this.mSceneShieldMask;
            if (Device.isSupportedAsdFlash() && "auto".equals(CameraModule.this.mParameters.getFlashMode()) && (i & 1) != 0) {
                return 0;
            }
            if (!CameraModule.this.getUIController().getSettingPage().isItemSelected()) {
                if (Device.isSupportedAsdHdr() && CameraModule.this.mParameters.getZoom() == 0 && !"torch".equals(CameraModule.this.mParameters.getFlashMode()) && (i & 16) != 0) {
                    return 1;
                }
                if (Device.isSupportedAsdNight() && ((CameraModule.this.mMutexModePicker.isNormal() || CameraModule.this.mMutexModePicker.isHandNight()) && "off".equals(CameraModule.this.mParameters.getFlashMode()) && (i & 64) != 0)) {
                    return 2;
                }
                if (Device.isSupportedAsdMotion() && ((CameraModule.this.mMutexModePicker.isNormal() || CameraModule.this.mMutexModePicker.isHandNight()) && "off".equals(CameraModule.this.mParameters.getFlashMode()) && (i & 32) != 0)) {
                    return 3;
                }
            }
            return -1;
        }

        private int detectRTBScene(int i) {
            int i2 = -1;
            if (CameraModule.this.isPortraitModeUseHintShowing() || !CameraSettings.isSupportedPortrait() || !CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                return -1;
            }
            boolean isHintTextShown = CameraModule.this.getUIController().getPortraitButton().isHintTextShown();
            if (i == 2) {
                if (!isHintTextShown) {
                    i2 = 4;
                }
                return i2;
            } else if (i == 3) {
                if (!isHintTextShown) {
                    i2 = 5;
                }
                return i2;
            } else if (i != 4) {
                return i == 6 ? this.mCurrentScene : 7;
            } else {
                if (!isHintTextShown) {
                    i2 = 6;
                }
                return i2;
            }
        }

        private void filterScene(int i) {
            if (setScene(i)) {
                restoreScene(this.mLastScene);
                this.mLastScene = -1;
                applyScene(this.mCurrentScene);
            }
        }

        private void printMetaData(byte[] bArr) {
            String format;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < bArr.length; i++) {
                stringBuilder.append(String.format(Locale.ENGLISH, "%02x", new Object[]{Byte.valueOf(bArr[i])}));
            }
            stringBuilder.append("  data[8]=");
            if (bArr.length > 8) {
                format = String.format(Locale.ENGLISH, "%02x", new Object[]{Byte.valueOf(bArr[8])});
            } else {
                format = "not exist";
            }
            stringBuilder.append(format);
            stringBuilder.append("  mSceneShieldMask=").append(String.format(Locale.ENGLISH, "%02x", new Object[]{Integer.valueOf(this.mSceneShieldMask)}));
            stringBuilder.append("  result=").append(String.format(Locale.ENGLISH, "%02x", new Object[]{Integer.valueOf(bArr[8] & this.mSceneShieldMask)}));
            Log.v("CameraMetaDataManager", "onCameraMetaData buffer=" + stringBuilder.toString());
        }

        private void resetSceneMode() {
            if (CameraModule.this.currentIsMainThread()) {
                restoreScene(this.mCurrentScene);
            }
            this.mLastScene = -1;
            this.mCurrentScene = -1;
            this.mLastestState = -1;
            this.mLastestTimes = 0;
        }

        private void restoreScene(int i) {
            Log.v("CameraMetaDataManager", "restoreScene " + i);
            switch (i) {
                case 0:
                    CameraModule.this.getUIController().getAsdIndicator().setVisibility(8);
                    if (CameraSettings.isAsdPopupEnable()) {
                        CameraModule.this.getUIController().getFlashButton().updatePopup(false);
                        return;
                    }
                    return;
                case 1:
                    if (CameraModule.this.mMutexModePicker.isMorphoHdr() || CameraModule.this.mMutexModePicker.isSceneHdr()) {
                        CameraModule.this.mMutexModePicker.resetMutexMode();
                    }
                    CameraModule.this.getUIController().getAsdIndicator().setVisibility(8);
                    return;
                case 2:
                    if (CameraModule.this.mMutexModePicker.isHandNight()) {
                        CameraModule.this.mMutexModePicker.resetMutexMode();
                        return;
                    }
                    return;
                case 3:
                    if (CameraModule.this.mMutexModePicker.isHandNight()) {
                        CameraModule.this.mMutexModePicker.resetMutexMode();
                        return;
                    }
                    return;
                case 4:
                case 5:
                case 6:
                    CameraModule.this.updateWarningMessage(0, true);
                    return;
                case 7:
                    CameraModule.this.getUIController().getPortraitButton().hideHintText();
                    return;
                default:
                    return;
            }
        }

        private boolean setScene(int i) {
            if (Util.sIsDumpLog) {
                Log.v("CameraMetaDataManager", "setScene " + i + " mLastestState=" + this.mLastestState + " mLastestTimes=" + this.mLastestTimes + " mCurrentScene=" + this.mCurrentScene);
            }
            if (this.mLastestState != i) {
                this.mLastestState = i;
                this.mLastestTimes = 1;
            } else if (this.mLastestTimes < 3) {
                this.mLastestTimes++;
                if (3 == this.mLastestTimes && this.mCurrentScene != this.mLastestState) {
                    this.mLastScene = this.mCurrentScene;
                    this.mCurrentScene = this.mLastestState;
                    return true;
                }
            }
            return false;
        }

        public void reset() {
            resetFilter();
            resetSceneMode();
        }

        public void resetFilter() {
            setAsdDetectMask("pref_camera_flashmode_key");
            setAsdDetectMask("pref_camera_hdr_key");
            setAsdDetectMask("pref_camera_asd_night_key");
            setAsdDetectMask("pref_camera_asd_motion_key");
        }

        public void setAsdDetectMask(java.lang.String r1) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.camera.module.CameraModule.MetaDataManager.setAsdDetectMask(java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.CameraModule.MetaDataManager.setAsdDetectMask(java.lang.String):void");
        }

        public void setData(byte[] bArr) {
            if (Util.sIsDumpLog) {
                printMetaData(bArr);
            }
            if (bArr.length >= 9) {
                if (this.mMetaType == 5 && bArr[0] == (byte) 5) {
                    filterScene(detectRTBScene(bArr[8]));
                } else if (this.mMetaType == 3) {
                    filterScene(detectASDScene(bArr[8]));
                }
            }
        }

        public void setType(int i) {
            if (this.mMetaType != i) {
                reset();
            }
            this.mMetaType = i;
        }
    }

    private final class PostViewPictureCallback implements PictureCallback {
        private PostViewPictureCallback() {
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            CameraModule.this.mPostViewPictureCallbackTime = System.currentTimeMillis();
            Log.v("Camera", "mShutterToPostViewCallbackTime = " + (CameraModule.this.mPostViewPictureCallbackTime - CameraModule.this.mShutterCallbackTime) + "ms");
        }
    }

    private final class RawPictureCallback implements PictureCallback {
        private RawPictureCallback() {
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            CameraModule.this.mRawPictureCallbackTime = System.currentTimeMillis();
            Log.v("Camera", "mShutterToRawCallbackTime = " + (CameraModule.this.mRawPictureCallbackTime - CameraModule.this.mShutterCallbackTime) + "ms");
            if (bArr != null) {
                Log.v("Camera", "rawData size = " + bArr.length);
                CameraModule.this.writeImage(bArr, parseDataSizeDNG(bArr));
            }
        }

        protected int parseDataSizeDNG(byte[] bArr) {
            if (8 > bArr.length) {
                return 0;
            }
            int i = ((((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24)) + 318;
            if (i > bArr.length) {
                return 0;
            }
            Log.e("Camera", "DNG size:" + i);
            return i;
        }
    }

    private class SaveOutputImageTask extends AsyncTask<Void, Integer, Integer> {
        private GroupShot mGroupShotInternal;
        private int mHeight;
        private Location mLocation;
        private int mOrientation;
        private long mTimeTaken;
        private String mTitle;
        private int mWidth;
        private long start_time;

        SaveOutputImageTask(long j, Location location, int i, int i2, int i3, String str, GroupShot groupShot) {
            this.mTimeTaken = j;
            this.mLocation = location;
            this.mWidth = i;
            this.mHeight = i2;
            this.mOrientation = i3;
            this.mTitle = str;
            this.mGroupShotInternal = groupShot;
        }

        private void finishGroupshot() {
            this.mGroupShotInternal.clearImages();
            this.mGroupShotInternal.finish();
            if (CameraModule.this.mPaused) {
                this.mGroupShotInternal = null;
            }
            this.mGroupShotInternal = null;
            CameraModule cameraModule = CameraModule.this;
            cameraModule.mGroupShotTimes = cameraModule.mGroupShotTimes - 1;
        }

        protected Integer doInBackground(Void... voidArr) {
            Log.v("Camera", "doInBackground start");
            try {
                int attach_end = this.mGroupShotInternal.attach_end();
                com.android.camera.Log.m5v("Camera", String.format("attach_end() = 0x%08x", new Object[]{Integer.valueOf(attach_end)}));
                if (isCancelled()) {
                    return null;
                }
                attach_end = this.mGroupShotInternal.setBaseImage(0);
                com.android.camera.Log.m5v("Camera", String.format("setBaseImage() = 0x%08x", new Object[]{Integer.valueOf(attach_end)}));
                attach_end = this.mGroupShotInternal.setBestFace();
                com.android.camera.Log.m5v("Camera", "groupshot attach end & setbestface cost " + (System.currentTimeMillis() - this.start_time));
                String generateFilepath = Storage.generateFilepath(this.mTitle);
                if (Util.sIsDumpLog) {
                    String substring = generateFilepath.substring(0, generateFilepath.lastIndexOf(".jpg"));
                    new File(substring).mkdirs();
                    CameraModule.this.mGroupShot.saveInputImages(substring + File.separator);
                }
                if (isCancelled()) {
                    return null;
                }
                this.mGroupShotInternal.getImageAndSaveJpeg(generateFilepath);
                Log.v("Camera", "groupshot finish group cost " + (System.currentTimeMillis() - this.start_time));
                if (isCancelled()) {
                    return null;
                }
                ExifHelper.writeExif(generateFilepath, this.mOrientation, LocationManager.instance().getCurrentLocation(), this.mTimeTaken);
                Uri addImage = Storage.addImage(CameraModule.this.mActivity, generateFilepath, this.mOrientation, this.mTimeTaken, this.mLocation, this.mWidth, this.mHeight);
                Log.v("Camera", "groupshot insert db cost " + (System.currentTimeMillis() - this.start_time));
                CameraDataAnalytics.instance().trackEvent("capture_times_group_shot");
                CameraModule.this.mActivity.getScreenHint().updateHint();
                if (addImage != null) {
                    CameraModule.this.mActivity.addSecureUri(addImage);
                    Thumbnail createThumbnailFromUri = Thumbnail.createThumbnailFromUri(CameraModule.this.mActivity.getContentResolver(), addImage, false);
                    Util.broadcastNewPicture(CameraModule.this.mActivity, addImage);
                    CameraModule.this.mActivity.getThumbnailUpdater().setThumbnail(createThumbnailFromUri, false);
                }
                Log.v("Camera", "groupshot asynctask cost " + (System.currentTimeMillis() - this.start_time));
                return null;
            } catch (Exception e) {
                Log.e("Camera", "SaveOutputImageTask exception occurs, " + e.getMessage());
                if (null != null) {
                    new File(null).delete();
                }
                return null;
            }
        }

        protected void onCancelled() {
            Log.v("Camera", "SaveOutputImageTask onCancelled");
            finishGroupshot();
        }

        protected void onPostExecute(Integer num) {
            Log.v("Camera", "SaveOutputImageTask onPostExecute");
            if (!CameraModule.this.mPaused) {
                CameraModule.this.mActivity.getThumbnailUpdater().updateThumbnailView();
            }
            Log.v("Camera", "groupshot image process cost " + (System.currentTimeMillis() - this.start_time));
            finishGroupshot();
        }

        protected void onPreExecute() {
            this.start_time = System.currentTimeMillis();
        }
    }

    private final class ShutterCallback implements android.hardware.Camera.ShutterCallback {
        private ShutterCallback() {
        }

        public void onShutter() {
            CameraModule.this.mShutterCallbackTime = System.currentTimeMillis();
            CameraModule.this.mShutterLag = CameraModule.this.mShutterCallbackTime - CameraModule.this.mCaptureStartTime;
            Log.v("Camera", "mShutterLag = " + CameraModule.this.mShutterLag + "ms");
            if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                CameraModule.this.mActivity.getCameraScreenNail().requestReadPixels();
            } else {
                CameraModule.this.animateShutter();
            }
            CameraModule.this.updateMutexModeUI(false);
        }
    }

    private void animateCapture() {
        if (!this.mIsImageCaptureIntent && !this.mPaused) {
            if (Device.isMDPRender()) {
                getUIController().getPreviewPanel().onCapture();
            } else {
                this.mActivity.getCameraScreenNail().animateCapture(getCameraRotation());
            }
        }
    }

    private void animateHold() {
        if (!this.mIsImageCaptureIntent && !this.mPaused) {
            this.mActivity.getCameraScreenNail().animateHold(getCameraRotation());
        }
    }

    private void animateShutter() {
        if (playAnimationBeforeCapture()) {
            animateCapture();
            playSound(0);
            return;
        }
        animateHold();
    }

    private void animateSlide() {
        if (!this.mIsImageCaptureIntent && !this.mPaused) {
            this.mActivity.getCameraScreenNail().animateSlide();
        }
    }

    private void applyPreferenceChange() {
        if ((this.mSetCameraParameter & 1) != 0) {
            setCameraParameters(2);
        }
        if ((this.mSetCameraParameter & 2) != 0) {
            getUIController().getEffectCropView().updateVisible();
            getUIController().getSettingsStatusBar().updateStatus();
        }
        this.mSetCameraParameter = 0;
    }

    private void applyPreferenceSettingsLater() {
        this.mSetCameraParameter = -1;
        if (getUIController().getPreviewPage().isPreviewPageVisible()) {
            this.mHandler.removeMessages(32);
            this.mHandler.sendEmptyMessage(32);
        }
    }

    private static String buildWaterMarkForCameraUUID(String str) {
        return Build.DEVICE + "_" + str + "_" + String.valueOf(System.currentTimeMillis());
    }

    private boolean canTakePicture() {
        return isCameraIdle() && !Storage.isLowStorageAtLastPoint();
    }

    private void checkRestartPreview() {
        if (this.mRestartPreview && this.mCameraState != 0) {
            Log.v("Camera", "Restarting Preview... Camera Mode Changed");
            stopPreview();
            startPreview();
            startFaceDetection();
            setCameraState(1);
            this.mRestartPreview = false;
        }
    }

    private boolean couldEnableObjectTrack() {
        return (!Device.isSupportedObjectTrack() || !isBackCamera() || getUIController().getSettingPage().isItemSelected() || this.mMultiSnapStatus || this.mCameraState == 3 || this.mIsImageCaptureIntent) ? false : true;
    }

    private void doAttach() {
        if (!this.mPaused) {
            byte[] bArr = this.mJpegImageData;
            if (this.mIsSaveCaptureImage) {
                saveJpegData(bArr);
            }
            if (this.mCropValue != null) {
                Uri uri = null;
                Closeable closeable = null;
                try {
                    File fileStreamPath = this.mActivity.getFileStreamPath("crop-temp");
                    fileStreamPath.delete();
                    FileOutputStream openFileOutput = this.mActivity.openFileOutput("crop-temp", 0);
                    openFileOutput.write(bArr);
                    openFileOutput.close();
                    closeable = null;
                    uri = Uri.fromFile(fileStreamPath);
                    Bundle bundle = new Bundle();
                    if ("circle".equals(this.mCropValue)) {
                        bundle.putString("circleCrop", "true");
                    }
                    if (this.mSaveUri != null) {
                        bundle.putParcelable("output", this.mSaveUri);
                    } else {
                        bundle.putBoolean("return-data", true);
                    }
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setData(uri);
                    intent.putExtras(bundle);
                    this.mActivity.startActivityForResult(intent, 1000);
                } catch (FileNotFoundException e) {
                    this.mActivity.setResult(0);
                    this.mActivity.finish();
                } catch (IOException e2) {
                    this.mActivity.setResult(0);
                    this.mActivity.finish();
                } finally {
                    Util.closeSilently(closeable);
                }
            } else if (this.mSaveUri != null) {
                if (needReturnInvisibleWatermark()) {
                    if (this.mCameraUUIDWatermarkImageData == null) {
                        this.mActivity.setResult(0);
                        this.mActivity.finish();
                        return;
                    }
                    bArr = this.mCameraUUIDWatermarkImageData;
                }
                Closeable closeable2 = null;
                try {
                    closeable2 = this.mContentResolver.openOutputStream(this.mSaveUri);
                    closeable2.write(bArr);
                    closeable2.close();
                    this.mActivity.setResult(-1);
                } catch (IOException e3) {
                    Log.e("Camera", "IOException when doAttach");
                    e3.printStackTrace();
                } finally {
                    this.mActivity.finish();
                    Util.closeSilently(closeable2);
                }
            } else {
                this.mActivity.setResult(-1, new Intent("inline-data").putExtra("data", Util.rotate(Util.makeBitmap(bArr, 51200), Exif.getOrientation(this.mJpegImageData))));
                this.mActivity.finish();
            }
        }
    }

    private String getCameraUUID() {
        this.mParameters.set("camera-get-fusion-id", "true");
        this.mCameraDevice.setParameters(this.mParameters);
        this.mParameters = this.mCameraDevice.getParameters();
        return this.mParameters.get("camera-fusion-id");
    }

    private int getGroupshotNum() {
        CameraHardwareFace[] faces = getUIController().getFaceView().getFaces();
        return Util.clamp((faces != null ? faces.length : 0) + 1, 2, 4);
    }

    public static List<String> getLayoutModeKeys(com.android.camera.Camera camera, boolean z, boolean z2) {
        List<String> arrayList = new ArrayList();
        if (z) {
            if (!z2) {
                arrayList.add("pref_camera_panoramamode_key");
            }
            arrayList.add("pref_delay_capture_mode");
            arrayList.add("pref_audio_capture");
            if (!z2 && Device.isSupportedUbiFocus()) {
                arrayList.add("pref_camera_ubifocus_key");
            }
            arrayList.add("pref_camera_manual_mode_key");
            if (Device.isSupportGradienter()) {
                arrayList.add("pref_camera_gradienter_key");
            }
            if (Device.isSupportedSkinBeautify() && !Device.IS_H2X_LC) {
                arrayList.add("pref_camera_face_beauty_mode_key");
            }
            if (!z2 && Device.isSupportGroupShot()) {
                arrayList.add("pref_camera_groupshot_mode_key");
            } else if (Device.isUsedNightMode()) {
                arrayList.add("pref_camera_hand_night_key");
            }
            if (Device.isSupportSceneMode()) {
                arrayList.add("pref_camera_scenemode_setting_key");
            }
            if (Device.isSupportedTiltShift()) {
                arrayList.add("pref_camera_tilt_shift_mode");
            }
            if (Device.isSupportSquare()) {
                arrayList.add("pref_camera_square_mode_key");
            }
            if (!z2 && Device.isSupportGroupShot() && Device.isUsedNightMode()) {
                arrayList.add("pref_camera_hand_night_key");
            }
        } else {
            arrayList.add("pref_delay_capture_mode");
            arrayList.add("pref_audio_capture");
            if (Device.isSupportedMagicMirror()) {
                arrayList.add("pref_camera_magic_mirror_key");
            }
            if (Device.isSupportGroupShot()) {
                arrayList.add("pref_camera_groupshot_mode_key");
            }
        }
        return arrayList;
    }

    private String getSuffix() {
        return !this.mMutexModePicker.isNormal() ? this.mMutexModePicker.getSuffix() : "";
    }

    private void handleDelayShutter() {
        tryRemoveCountDownMessage();
        this.mHandler.removeMessages(29);
        if (CameraSettings.isAudioCaptureOpen()) {
            if (!this.mAudioCaptureManager.isRunning()) {
                this.mHandler.sendEmptyMessageDelayed(29, 350);
            }
        } else if (this.mAudioCaptureManager.isRunning()) {
            this.mAudioCaptureManager.close();
        }
    }

    private void handleUpdateFaceView(boolean z, boolean z2) {
        boolean z3 = false;
        FrameView faceView = getUIController().getFaceView();
        if (!z) {
            if (z2) {
                faceView.clear();
            }
            faceView.setVisibility(8);
        } else if ((this.mFaceDetectionStarted || isFaceBeautyMode()) && !"auto".equals(this.mParameters.getFocusMode())) {
            faceView.clear();
            faceView.setVisibility(0);
            faceView.setDisplayOrientation(this.mCameraDisplayOrientation);
            if (this.mCameraId == CameraHolder.instance().getFrontCameraId()) {
                z3 = true;
            }
            faceView.setMirror(z3);
            faceView.resume();
            this.mFocusManager.setFrameView(faceView);
        }
    }

    private void hideLoadUI(boolean z) {
        getUIController().getCaptureProgressBar().setVisibility(z ? 8 : 0);
    }

    private void hidePostCaptureAlert() {
        if (this.mIsImageCaptureIntent) {
            ignoreTouchEvent(false);
            getUIController().getSettingsStatusBar().show();
            getUIController().getEffectCropView().show();
            getUIController().getPreviewPage().setPopupVisible(true);
            getUIController().getShutterButton().animateIn(null, 100, true);
            getUIController().getBottomControlUpperPanel().animateIn(null);
            getUIController().getTopControlPanel().animateIn(null);
            getUIController().getReviewDoneView().animateOut(null, 100, true);
            getUIController().getZoomButton().updateVisible();
        }
    }

    private void initGroupShot(int i) {
        if (this.mParameters != null) {
            if (this.mGroupShot == null || this.mGroupShot.isUsed()) {
                this.mGroupShot = new GroupShot();
            }
            if (this.mOrientation % 180 == 0 && Device.isISPRotated()) {
                this.mGroupShot.initialize(i, this.mGroupFaceNum, this.mParameters.getPictureSize().height, this.mParameters.getPictureSize().width, this.mParameters.getPreviewSize().height, this.mParameters.getPreviewSize().width);
            } else {
                this.mGroupShot.initialize(i, this.mGroupFaceNum, this.mParameters.getPictureSize().width, this.mParameters.getPictureSize().height, this.mParameters.getPreviewSize().width, this.mParameters.getPreviewSize().height);
            }
        }
    }

    private void initializeCapabilities() {
        boolean z = false;
        this.mInitialParams = this.mCameraDevice.getParameters();
        if (this.mInitialParams != null) {
            this.mFocusAreaSupported = this.mInitialParams.getMaxNumFocusAreas() > 0 ? BaseModule.isSupported("auto", sProxy.getSupportedFocusModes(this.mInitialParams)) : false;
            if (this.mInitialParams.getMaxNumMeteringAreas() > 0) {
                z = true;
            }
            this.mMeteringAreaSupported = z;
            this.mAeLockSupported = this.mInitialParams.isAutoExposureLockSupported();
            this.mAwbLockSupported = this.mInitialParams.isAutoWhiteBalanceLockSupported();
            this.mContinuousFocusSupported = BaseModule.isSupported("continuous-picture", sProxy.getSupportedFocusModes(this.mInitialParams));
        }
    }

    private void initializeFirstTime() {
        if (!this.mFirstTimeInitialized) {
            keepMediaProviderInstance();
            installIntentFilter();
            updateLyingSensorState(true);
            this.mFirstTimeInitialized = true;
        }
    }

    private void initializeFocusManager() {
        boolean z = false;
        String[] stringArray = getResources().getStringArray(C0049R.array.pref_camera_focusmode_default_array);
        Context context = this.mActivity;
        CameraSettingPreferences cameraSettingPreferences = this.mPreferences;
        FocusView focusView = getUIController().getFocusView();
        Parameters parameters = this.mInitialParams;
        if (this.mCameraId == CameraHolder.instance().getFrontCameraId()) {
            z = true;
        }
        this.mFocusManager = new FocusManager(context, cameraSettingPreferences, stringArray, focusView, parameters, this, z, this.mActivity.getMainLooper());
        Rect rect = null;
        if (this.mActivity.getCameraScreenNail() != null) {
            rect = this.mActivity.getCameraScreenNail().getRenderRect();
        }
        if (rect == null || rect.width() <= 0) {
            this.mFocusManager.setRenderSize(Util.sWindowWidth, Util.sWindowHeight);
            this.mFocusManager.setPreviewSize(Util.sWindowWidth, Util.sWindowHeight);
            return;
        }
        this.mFocusManager.setRenderSize(this.mActivity.getCameraScreenNail().getRenderWidth(), this.mActivity.getCameraScreenNail().getRenderHeight());
        this.mFocusManager.setPreviewSize(rect.width(), rect.height());
    }

    private boolean initializeObjectTrack(RectF rectF, boolean z) {
        mapTapCoordinate(rectF);
        stopObjectTracking(false);
        getUIController().getObjectView().clear();
        getUIController().getFocusView().clear();
        getUIController().getObjectView().setVisibility(0);
        if (!getUIController().getObjectView().initializeTrackView(rectF, z)) {
            return false;
        }
        this.mFocusManager.setFrameView(getUIController().getObjectView());
        return true;
    }

    private void initializeSecondTime() {
        installIntentFilter();
        keepMediaProviderInstance();
        updateLyingSensorState(true);
        if (getUIController().getReviewDoneView().getVisibility() != 0) {
            hidePostCaptureAlert();
        }
    }

    private void installIntentFilter() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_SCANNER_FINISHED");
        intentFilter.addAction("android.intent.action.MEDIA_CHECKING");
        intentFilter.addDataScheme("file");
        this.mActivity.registerReceiver(this.mReceiver, intentFilter);
        this.mDidRegister = true;
    }

    private boolean isCameraIdle() {
        return this.mCameraState != 1 ? (this.mFocusManager == null || !this.mFocusManager.isFocusCompleted()) ? false : (this.mCameraState == 4 || this.mCameraState == 3) ? false : true : true;
    }

    private boolean isCaptureAfterLaunch() {
        if ("android.media.action.STILL_IMAGE_CAMERA".equals(this.mActivity.getIntent().getAction())) {
            Bundle extras = this.mActivity.getIntent().getExtras();
            if (extras != null && extras.containsKey("captureAfterLaunch")) {
                boolean z = extras.getBoolean("captureAfterLaunch", false);
                extras.putBoolean("captureAfterLaunch", false);
                this.mActivity.getIntent().putExtras(extras);
                return z;
            }
        }
        return false;
    }

    private boolean isCountDownMode() {
        return CameraSettings.isSwitchOn("pref_delay_capture_mode") && CameraSettings.getCountDownTimes() > 0;
    }

    private boolean isGroupShotCapture() {
        return CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key") ? this.mTotalJpegCallbackNum > 1 : false;
    }

    private boolean isPortraitModeUseHintShowing() {
        return TextUtils.equals(getUIController().getWarningMessageView().getText(), getString(C0049R.string.dual_camera_use_hint)) && getUIController().getWarningMessageParent().getVisibility() == 0;
    }

    private boolean isPreviewVisible() {
        return this.mFirstTimeInitialized && getUIController().getPreviewPage().isPreviewPageVisible() && getUIController().getReviewDoneView().getVisibility() != 0;
    }

    private boolean isSelectingCapturedImage() {
        return this.mIsImageCaptureIntent ? getUIController().getReviewDoneView().isVisibleWithAnimationDone() : false;
    }

    private boolean isShutterButtonClickable() {
        return (this.mPaused || this.mSwitchingCamera || this.mCameraState == 0) ? false : true;
    }

    private void keepMediaProviderInstance() {
        if (this.mMediaProviderClient == null) {
            this.mMediaProviderClient = this.mContentResolver.acquireContentProviderClient("media");
        }
    }

    private void keepScreenOnAwhile() {
        this.mHandler.sendEmptyMessageDelayed(17, 1000);
    }

    private boolean needReturnInvisibleWatermark() {
        return this.mNeedSealCameraUUID;
    }

    private void onFrameLayoutChange(View view, Rect rect) {
        this.mActivity.onLayoutChange(rect.width(), rect.height());
        if (this.mActivity.getCameraScreenNail() != null) {
            this.mActivity.getCameraScreenNail().setRenderArea(rect);
        }
        if (!(this.mFocusManager == null || this.mActivity.getCameraScreenNail() == null)) {
            this.mFocusManager.setRenderSize(this.mActivity.getCameraScreenNail().getRenderWidth(), this.mActivity.getCameraScreenNail().getRenderHeight());
            this.mFocusManager.setPreviewSize(rect.width(), rect.height());
        }
        if (getUIController().getObjectView() != null) {
            getUIController().getObjectView().setDisplaySize(rect.right - rect.left, rect.bottom - rect.top);
        }
        QRCodeManager.instance(this.mActivity).setPreviewLayoutSize(rect.width(), rect.height());
    }

    private void onModeSelected(Object obj) {
        handleDelayShutter();
        if ("pref_camera_panoramamode_key".equals(obj)) {
            boolean z = this.mSwitchCameraAnimationRunning;
            if (z) {
                this.mActivity.getCameraScreenNail().animateSwitchCameraBefore();
            }
            switchToOtherMode(2);
            if (z) {
                this.mActivity.getCameraScreenNail().switchCameraDone();
            }
            return;
        }
        if ("pref_camera_ubifocus_key".equals(obj)) {
            if (CameraSettings.isSwitchOn("pref_camera_ubifocus_key")) {
                this.mMutexModePicker.setMutexMode(6);
            } else if (this.mMutexModePicker.isUbiFocus()) {
                this.mMutexModePicker.resetMutexMode();
            }
        } else if ("pref_camera_hand_night_key".equals(obj)) {
            if (CameraSettings.isSwitchOn("pref_camera_hand_night_key")) {
                this.mMutexModePicker.setMutexMode(3);
            } else if (this.mMutexModePicker.isHandNight()) {
                this.mMutexModePicker.resetMutexMode();
            }
        } else if ("pref_camera_square_mode_key".equals(obj)) {
            getUIController().getPreviewFrame().updateRefenceLineAccordSquare();
            if ("auto".equals(this.mParameters.getFocusMode())) {
                this.mFocusManager.resetTouchFocus();
                cancelAutoFocus();
            }
            if (CameraSettings.isSupportedOpticalZoom()) {
                CameraSettings.resetZoom(this.mPreferences);
                getUIController().getZoomButton().reloadPreference();
            }
        } else if ("pref_camera_shader_coloreffect_key".equals(obj)) {
            if (this.mMutexModePicker.isUbiFocus() || this.mMutexModePicker.isBurstShoot()) {
                this.mSettingsOverrider.removeSavedSetting("pref_camera_shader_coloreffect_key");
                getUIController().getModeExitView().clearExitButtonClickListener(true);
            } else if (CameraSettings.isSwitchOn("pref_camera_gradienter_key") || CameraSettings.isSwitchOn("pref_camera_tilt_shift_mode") || CameraSettings.isSwitchOn("pref_camera_magic_mirror_key") || CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key")) {
                getUIController().getModeExitView().clearExitButtonClickListener(true);
            }
        } else if ("pref_camera_manual_mode_key".equals(obj)) {
            if (CameraSettings.isSupportedOpticalZoom()) {
                CameraSettings.resetZoom(this.mPreferences);
                CameraSettings.resetCameraZoomMode();
                getUIController().getZoomButton().reloadPreference();
                this.mSwitchCameraLater = Boolean.valueOf(true);
                prepareSwitchCameraAnimation(this.mPreviewTextureCopiedActionSwitchCameraLater);
                if (getUIController().getPreviewPage().isPreviewPageVisible()) {
                    this.mHandler.sendEmptyMessage(41);
                } else {
                    this.mManualModeSwitched = true;
                }
            }
            if (CameraSettings.isSwitchOn("pref_camera_manual_mode_key")) {
                getUIController().getFlashButton().keepSetValue("off");
                CameraSettings.updateFocusMode();
            } else {
                getUIController().getFlashButton().restoreKeptValue();
                getUIController().getHdrButton().overrideSettings(null);
            }
        } else if ("pref_camera_zoom_mode_key".equals(obj)) {
            getUIController().getZoomButton().requestSwitchCamera();
        }
        if ("pref_camera_groupshot_mode_key".equals(obj)) {
            if (CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key")) {
                initGroupShot(getGroupshotNum());
                if (!"torch".equals(getUIController().getFlashButton().getValue())) {
                    getUIController().getFlashButton().keepSetValue("off");
                }
                updateWarningMessage(C0049R.string.groupshot_mode_use_hint, false);
            } else {
                updateWarningMessage(0, true);
            }
            if (CameraSettings.isSupportedOpticalZoom()) {
                CameraSettings.resetZoom(this.mPreferences);
                getUIController().getZoomButton().reloadPreference();
            }
        }
        this.mActivity.getSensorStateManager().setGradienterEnabled(CameraSettings.isSwitchOn("pref_camera_gradienter_key"));
        if (CameraSettings.isSwitchOn("pref_camera_gradienter_key") || CameraSettings.isSwitchOn("pref_camera_tilt_shift_mode") || CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key")) {
            getUIController().getEffectButton().resetSettings();
        } else {
            getUIController().getEffectButton().restoreSettings();
        }
        onSharedPreferenceChanged();
        getUIController().getEffectCropView().updateVisible();
        getUIController().getSettingsStatusBar().updateStatus();
    }

    private void onSettingsBack() {
        ChangeManager changeManager = CameraSettings.sCameraChangeManager;
        if (changeManager.check(3)) {
            changeManager.clear(3);
            restorePreferences();
        } else if (changeManager.check(1)) {
            changeManager.clear(1);
            onSharedPreferenceChanged();
        }
    }

    private void onStereoModeChanged() {
        QRCodeManager.instance(this.mActivity).onPause();
        setCameraState(0);
        resetMetaDataManager();
        this.mActivity.getSensorStateManager().setFocusSensorEnabled(false);
        if (this.mFocusManager != null) {
            this.mFocusManager.removeMessages();
            this.mFocusManager.resetTouchFocus();
        }
        this.mCameraStartUpThread = new CameraStartUpThread();
        this.mCameraStartUpThread.start();
        CameraSettings.resetZoom(this.mPreferences);
        CameraSettings.resetExposure();
        if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            this.mMutexModePicker.resetMutexModeDummy();
            updateStereoSettings(true);
            return;
        }
        this.mSettingsOverrider.restoreSettings();
        if (TextUtils.equals(getUIController().getWarningMessageView().getText(), getString(C0049R.string.dual_camera_use_hint))) {
            updateWarningMessage(0, true);
        }
    }

    private void overrideCameraSettings(String str, String str2, String str3, String str4, String str5, String str6) {
        if (Device.isQcomPlatform()) {
            getUIController().getSettingPage().overrideSettings("pref_camera_whitebalance_key", str2, "pref_camera_exposure_key", str3, "pref_camera_focus_mode_key", str4, "pref_qc_camera_iso_key", str5, "pref_camera_coloreffect_key", str6);
            return;
        }
        getUIController().getSettingPage().overrideSettings("pref_camera_whitebalance_key", str2, "pref_camera_exposure_key", str3, "pref_camera_focus_mode_key", str4, "pref_qc_camera_iso_key", str5);
    }

    private void prepareGroupShot() {
        if (isGroupShotCapture()) {
            initGroupShot(this.mTotalJpegCallbackNum);
            if (this.mGroupShot != null) {
                this.mGroupShot.attach_start(1);
            } else {
                this.mTotalJpegCallbackNum = 1;
            }
            this.mReceivedJpegCallbackNum = 0;
        }
    }

    private void prepareSwitchCameraAnimation(PreviewTextureCopiedCallback previewTextureCopiedCallback) {
        this.mPreviewTextureCopiedCallback = previewTextureCopiedCallback;
        this.mActivity.getCameraScreenNail().animateSwitchCopyTexture();
        this.mSwitchCameraAnimationRunning = true;
    }

    private void prepareUIByPreviewSize() {
        if (Device.isPad()) {
            this.mUIStyle = 0;
        } else if (CameraSettings.sCroppedIfNeeded) {
            this.mUIStyle = 1;
        } else {
            PictureSize pictureSize = new PictureSize(this.mPreferences.getString("pref_camera_picturesize_key", PictureSizeManager.getDefaultValue()));
            if (!pictureSize.isEmpty()) {
                int uIStyleByPreview = CameraSettings.getUIStyleByPreview(pictureSize.width, pictureSize.height);
                if (uIStyleByPreview != this.mUIStyle) {
                    this.mUIStyle = uIStyleByPreview;
                    CameraSettings.changeUIByPreviewSize(this.mActivity, this.mUIStyle);
                    changePreviewSurfaceSize();
                }
                getUIController().getPreviewFrame().setAspectRatio(CameraSettings.getPreviewAspectRatio(pictureSize.width, pictureSize.height));
            }
        }
    }

    private void previewBecomeInvisible() {
        stopFaceDetection(true);
        stopPreview();
    }

    private void previewBecomeVisible() {
        this.mActivity.getCameraScreenNail().releaseBitmapIfNeeded();
        startPreview();
        startFaceDetection();
    }

    private void releaseResources() {
        stopPreview();
        closeCamera();
        CameraDataAnalytics.instance().uploadToServer();
        this.mWaitForRelease = false;
    }

    private void resetGradienter() {
        if (CameraSettings.isSwitchOn("pref_camera_gradienter_key")) {
            this.mActivity.getSensorStateManager().setGradienterEnabled(false);
            this.mSettingsOverrider.restoreSettings();
        }
    }

    private void resetScreenOn() {
        this.mHandler.removeMessages(17);
        this.mHandler.removeMessages(2);
        getWindow().clearFlags(128);
    }

    private void restorePreferences() {
        if (this.mParameters.isZoomSupported()) {
            setZoomValue(0);
        }
        getUIController().getFlashButton().reloadPreference();
        getUIController().getSettingPage().reloadPreferences();
        onSharedPreferenceChanged();
    }

    private void restoreStatusAfterBurst() {
        this.mMultiSnapStatus = false;
        this.mSnapshotOnIdle = false;
        setupPreview();
        if (!this.mMutexModePicker.isBurstShoot() && this.mSettingsOverrider.restoreSettings()) {
            setCameraParameters(2);
            getUIController().getFlashButton().refreshValue();
            getUIController().getSettingsStatusBar().updateStatus();
            getUIController().getEffectCropView().updateVisible();
        }
    }

    private void resumePreview() {
        startPreview();
        this.mHandler.sendEmptyMessage(9);
    }

    private void saveJpegData(byte[] bArr) {
        int i;
        int i2;
        Size pictureSize = this.mParameters.getPictureSize();
        Location currentLocation = LocationManager.instance().getCurrentLocation();
        int orientation = Exif.getOrientation(bArr);
        if ((this.mJpegRotation + orientation) % 180 == 0) {
            i = pictureSize.width;
            i2 = pictureSize.height;
        } else {
            i = pictureSize.height;
            i2 = pictureSize.width;
        }
        byte[] bArr2 = bArr;
        this.mActivity.getImageSaver().addImage(bArr2, Util.createJpegName(System.currentTimeMillis()), System.currentTimeMillis(), null, currentLocation, i, i2, null, orientation, false, false, true);
    }

    private void saveStatusBeforeBurst() {
        this.mMultiSnapStatus = true;
        if (!this.mMutexModePicker.isBurstShoot()) {
            List arrayList = new ArrayList();
            arrayList.addAll(Arrays.asList(new String[]{"pref_qc_camera_iso_key", null, "pref_qc_camera_exposuretime_key", null, "pref_camera_face_beauty_key", null, "pref_camera_shader_coloreffect_key", null}));
            String value = getUIController().getFlashButton().getValue();
            if (!("off".equals(value) || "torch".equals(value))) {
                arrayList.add("pref_camera_flashmode_key");
                arrayList.add("off");
            }
            this.mSettingsOverrider.overrideSettings((String[]) arrayList.toArray(new String[arrayList.size()]));
        }
        stopObjectTracking(false);
        setCameraState(3);
        setCameraParameters(-1);
        getUIController().getEffectCropView().updateVisible();
        getUIController().getFlashButton().refreshValue();
        getUIController().getSettingsStatusBar().updateStatus();
    }

    private void sendBurstCommand() {
        if (Device.isQcomPlatform() && isLongShotMode()) {
            synchronized (this.mCameraDevice) {
                this.mCameraDevice.takePicture(new JpegQuickShutterCallback(), this.mRawPictureCallback, null, new JpegQuickPictureCallback(LocationManager.instance().getCurrentLocation()));
            }
        }
    }

    private void sendDoCaptureMessage(long j) {
        this.mDoCaptureRetry = 0;
        if (!this.mHandler.hasMessages(15)) {
            this.mHandler.sendEmptyMessageDelayed(15, j);
        }
    }

    private void setOrientation(int i) {
        if (i != -1) {
            this.mOrientation = Util.roundOrientation(i, this.mOrientation);
            EffectController.getInstance().setOrientation(Util.getShootOrientation(this.mActivity, this.mOrientation));
            checkActivityOrientation();
            int i2 = (this.mOrientation + this.mDisplayRotation) % 360;
            if (this.mOrientationCompensation != i2) {
                this.mOrientationCompensation = i2;
                setOrientationIndicator(this.mOrientationCompensation, true);
                setOrientationParameter();
            }
        }
    }

    private void setOrientationParameter() {
        if (this.mParameters != null && this.mCameraDevice != null && this.mCameraState != 3 && this.mCameraStartUpThread == null) {
            Object obj = null;
            if (Device.isFaceDetectNeedRotation()) {
                int jpegRotation = Util.getJpegRotation(this.mCameraId, this.mOrientation);
                if (sProxy.getRotation(this.mParameters) != jpegRotation) {
                    obj = 1;
                    this.mParameters.setRotation(jpegRotation);
                }
            }
            if (!((!Device.isSupportedIntelligentBeautify() && !Device.isSupportedObjectTrack()) || this.mOrientation == -1 || String.valueOf(this.mOrientation).equals(this.mParameters.get("xiaomi-preview-rotation")))) {
                obj = 1;
                this.mParameters.set("xiaomi-preview-rotation", this.mOrientation);
            }
            if (obj == null) {
                return;
            }
            if (Device.isLCPlatform() || Device.isMTKPlatform() || Device.isSupportedIntelligentBeautify()) {
                this.mCameraDevice.setParametersAsync(this.mParameters);
            }
        }
    }

    private void setPictureOrientation() {
        this.mShootRotation = this.mActivity.getSensorStateManager().isDeviceLying() ? -1.0f : this.mDeviceRotation;
        this.mShootOrientation = this.mOrientation == -1 ? 0 : this.mOrientation;
    }

    private void setPreviewFrameLayoutAspectRatio() {
        Size pictureSize = this.mParameters.getPictureSize();
        getUIController().getPreviewFrame().setAspectRatio(CameraSettings.getPreviewAspectRatio(pictureSize.width, pictureSize.height));
    }

    private void setupCaptureParams() {
        Bundle extras = this.mActivity.getIntent().getExtras();
        if (extras != null) {
            this.mSaveUri = (Uri) extras.getParcelable("output");
            this.mCropValue = extras.getString("crop");
            this.mIsSaveCaptureImage = extras.getBoolean("save-image", false);
            this.mNeedSealCameraUUID = Util.getNeedSealCameraUUIDIntentExtras(this.mActivity);
        }
        if (Util.isPortraitIntent(this.mActivity) && !CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
            this.mPreferences.edit().putString("pref_camera_portrait_mode_key", "on").apply();
        }
    }

    private void showObjectTrackToastIfNeeded() {
        if (this.mPreferences.getBoolean("pref_camera_first_tap_screen_hint_shown_key", true) && couldEnableObjectTrack()) {
            this.mHandler.sendEmptyMessageDelayed(23, 1000);
        }
    }

    private void showPostCaptureAlert() {
        if (this.mIsImageCaptureIntent) {
            ignoreTouchEvent(true);
            this.mFocusManager.removeMessages();
            previewBecomeInvisible();
            getUIController().getSettingsStatusBar().hide();
            getUIController().getEffectCropView().hide();
            getUIController().getPreviewPage().setPopupVisible(false);
            getUIController().getZoomButton().setVisibility(8);
            getUIController().getShutterButton().animateOut(null, 100, true);
            getUIController().getBottomControlUpperPanel().animateOut(null);
            getUIController().getTopControlPanel().animateOut(null);
            getUIController().getReviewDoneView().animateIn(null, 100, true);
            getUIController().getOrientationIndicator().updateVisible(false);
            resetMetaDataManager();
        }
    }

    private void showTapToFocusToastIfNeeded() {
        if (this.mPreferences.getBoolean("pref_camera_first_use_hint_shown_key", true) || this.mPreferences.getBoolean("pref_camera_first_portrait_use_hint_shown_key", true)) {
            this.mHandler.sendEmptyMessageDelayed(5, 1000);
        }
    }

    private void stopMultiSnap() {
        animateCapture();
        cancelContinuousShot();
        this.mHandler.removeMessages(30);
        this.mMultiSnapStopRequest = false;
        if (Device.isMTKPlatform()) {
            this.mHandler.sendEmptyMessageDelayed(37, 5000);
        } else {
            handleMultiSnapDone();
        }
    }

    private void stopPreview() {
        if (currentIsMainThread()) {
            stopObjectTracking(false);
        }
        if (!(this.mCameraDevice == null || this.mCameraState == 0)) {
            Log.v("Camera", "stopPreview");
            this.mCameraDevice.stopPreview();
            this.mFaceDetectionStarted = false;
        }
        if (currentIsMainThread()) {
            setCameraState(0);
        } else {
            this.mCameraState = 0;
        }
        if (currentIsMainThread() && this.mFocusManager != null) {
            this.mFocusManager.onPreviewStopped();
        }
    }

    private void switchCamera() {
        boolean z = true;
        if (!this.mPaused) {
            updateWarningMessage(0, true);
            updateStereoSettings(false);
            resetMetaDataManager();
            if (!this.mMutexModePicker.isNormal()) {
                this.mMutexModePicker.resetMutexMode();
            }
            this.mAudioCaptureManager.onPause();
            tryRemoveCountDownMessage();
            Log.v("Camera", "Start to switch camera. id=" + this.mPendingSwitchCameraId);
            this.mCameraId = this.mPendingSwitchCameraId;
            this.mPendingSwitchCameraId = -1;
            CameraSettings.writePreferredCameraId(this.mPreferences, this.mCameraId);
            this.mActivity.changeRequestOrientation();
            CameraSettings.resetZoom(this.mPreferences);
            if (!isBackCamera()) {
                CameraSettings.resetCameraZoomMode();
            }
            CameraSettings.resetExposure();
            resetGradienter();
            resetFaceBeautyMode();
            CameraSettingPreferences.instance().setLocalId(getPreferencesLocalId());
            updateExitButton(false);
            PopupManager.getInstance(this.mActivity).notifyShowPopup(null, 1);
            stopObjectTracking(false);
            closeCamera();
            getUIController().getFaceView().clear();
            getUIController().getEdgeShutterView().cancelAnimation();
            if (this.mFocusManager != null) {
                this.mFocusManager.removeMessages();
            }
            getUIController().updatePreferenceGroup();
            openCamera();
            if (hasCameraException()) {
                onCameraException();
                return;
            }
            initializeCapabilities();
            updateStereoSettings(true);
            FocusManager focusManager = this.mFocusManager;
            if (this.mCameraId != CameraHolder.instance().getFrontCameraId()) {
                z = false;
            }
            focusManager.setMirror(z);
            this.mFocusManager.setParameters(this.mParameters);
            setOrientationIndicator(this.mOrientationCompensation, false);
            getUIController().getFlashButton().avoidTorchOpen();
            startPreview();
            startFaceDetection();
            initializeAfterCameraOpen();
            enableCameraControls(false);
            getUIController().onCameraOpen();
            getUIController().getFocusView().initialize(this);
            getUIController().getObjectView().setObjectViewListener(this);
            onCameraStartPreview();
            updateModePreference();
            this.mAudioCaptureManager.onResume();
            this.mHandler.sendEmptyMessage(19);
        }
    }

    private void switchToOtherMode(int i) {
        if (!this.mActivity.isFinishing()) {
            this.mHandler.removeMessages(1);
            this.mActivity.switchToOtherModule(i);
        }
    }

    private void takeAPhotoIfNeeded() {
        if (this.mIsCaptureAfterLaunch) {
            AutoLockManager.getInstance(this.mActivity).removeMessage();
        }
        boolean isVoiceAssistantCaptureIntent = this.mActivity.isVoiceAssistantCaptureIntent();
        this.mIsCaptureAfterLaunch = !isCaptureAfterLaunch() ? isVoiceAssistantCaptureIntent : true;
        if (this.mIsCaptureAfterLaunch) {
            if (BaseModule.isSupported("off", this.mParameters.getSupportedFlashModes())) {
                this.mParameters.setFlashMode("off");
                getUIController().getFlashButton().setValue("off");
                this.mCameraDevice.setParameters(this.mParameters);
            }
            if (isVoiceAssistantCaptureIntent) {
                this.mHandler.removeMessages(44);
                this.mHandler.sendEmptyMessageDelayed(44, 1000);
            } else {
                sendDoCaptureMessage(1000);
            }
            AutoLockManager.getInstance(this.mActivity).lockScreenDelayed();
        }
    }

    private void traceDelayCaptureEvents() {
        if (this.mAudioCaptureManager.isRunning()) {
            CameraDataAnalytics.instance().trackEvent("capture_times_audio");
            return;
        }
        CameraDataAnalytics.instance().trackEvent("capture_times_count_down");
        int countDownTimes = CameraSettings.getCountDownTimes();
        if (countDownTimes == 3) {
            CameraDataAnalytics.instance().trackEvent("capture_times_count_down_3s");
        } else if (countDownTimes == 5) {
            CameraDataAnalytics.instance().trackEvent("capture_times_count_down_5s");
        } else if (countDownTimes == 10) {
            CameraDataAnalytics.instance().trackEvent("capture_times_count_down_10s");
        }
    }

    private void updateHDRPreference() {
        int mutexHdrMode = getMutexHdrMode(getUIController().getHdrButton().getValue());
        updateASD("pref_camera_hdr_key");
        if (mutexHdrMode != -1) {
            this.mMutexModePicker.setMutexMode(mutexHdrMode);
        } else if (this.mMutexModePicker.isHdr()) {
            this.mMutexModePicker.resetMutexMode();
        } else {
            onSharedPreferenceChanged();
        }
    }

    private void updateLyingSensorState(boolean z) {
        if (this.mActivity.getSensorStateManager().canDetectOrientation()) {
            this.mActivity.getSensorStateManager().setRotationIndicatorEnabled(z);
        }
    }

    private void updateModePreference() {
        if (!isFrontCamera()) {
            int mutexHdrMode = getMutexHdrMode(getUIController().getHdrButton().getValue());
            if (mutexHdrMode != 0) {
                getUIController().getFlashButton().updateFlashModeAccordingHdr(getUIController().getHdrButton().getValue());
                this.mMutexModePicker.setMutexMode(mutexHdrMode);
            } else if (CameraSettings.isSwitchOn("pref_camera_hand_night_key")) {
                this.mMutexModePicker.setMutexMode(3);
            } else if (CameraSettings.isSwitchOn("pref_camera_ubifocus_key")) {
                this.mMutexModePicker.setMutexMode(6);
            } else {
                this.mMutexModePicker.resetMutexMode();
            }
            if (CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key")) {
                if (!"torch".equals(getUIController().getFlashButton().getValue())) {
                    getUIController().getFlashButton().keepSetValue("off");
                }
                updateWarningMessage(C0049R.string.groupshot_mode_use_hint, false);
            } else if (CameraSettings.isSwitchOn("pref_camera_manual_mode_key")) {
                setManualParameters();
                this.mCameraDevice.setParameters(this.mParameters);
            }
            int shaderEffect = CameraSettings.getShaderEffect();
            if (EffectController.sGradienterIndex == shaderEffect) {
                this.mActivity.getSensorStateManager().setGradienterEnabled(true);
            } else if (shaderEffect != EffectController.getInstance().getEffect(false)) {
                applyPreferenceSettingsLater();
            }
        }
    }

    private void updateMutexModeUI(boolean z) {
        if (!this.mMutexModePicker.isNormal()) {
            if (!this.mMutexModePicker.isAoHdr()) {
                hideLoadUI(z);
            }
            if (this.mMutexModePicker.isUbiFocus() && !(z && getUIController().getWarningMessageView().getText().equals(getString(C0049R.string.ubi_focus_capture_fail)))) {
                updateWarningMessage(C0049R.string.cannot_move_warning_message, z);
            }
        }
        if (z || Integer.parseInt(getManualValue("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default))) > 250000 || !getString(C0049R.string.pref_face_beauty_close).equals(CameraSettings.getFaceBeautifyValue())) {
            hideLoadUI(z);
        }
    }

    private void updateSceneModeUI() {
        if ("auto".equals(this.mSceneMode)) {
            overrideCameraSettings(null, null, null, null, null, null);
        } else {
            overrideCameraSettings(this.mParameters.getFlashMode(), getString(C0049R.string.pref_camera_whitebalance_default), getString(C0049R.string.pref_exposure_default), getString(C0049R.string.pref_camera_focusmode_value_default), getString(C0049R.string.pref_camera_iso_default), getString(C0049R.string.pref_camera_coloreffect_default));
        }
        getUIController().getFlashButton().overrideSettings(CameraSettings.getFlashModeByScene(this.mSceneMode));
    }

    private void updateStereoSettings(boolean z) {
        if (!CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            return;
        }
        if (z) {
            this.mSettingsOverrider.overrideSettings("pref_camera_shader_coloreffect_key", null, "pref_camera_flashmode_key", "off", "pref_camera_hdr_key", "off");
            return;
        }
        this.mSettingsOverrider.restoreSettings();
    }

    private void updateWarningMessage(int i, boolean z) {
        updateWarningMessage(i, z, true);
    }

    private void updateWarningMessage(int i, boolean z, boolean z2) {
        int i2 = 0;
        if (i != 0) {
            getUIController().getWarningMessageView().setText(i);
        }
        this.mHandler.removeMessages(21);
        if (!z && z2) {
            if (C0049R.string.ubifocus_capture_warning_message == i) {
                this.mHandler.sendEmptyMessageDelayed(21, 15000);
            } else {
                this.mHandler.sendEmptyMessageDelayed(21, 5000);
            }
        }
        LinearLayout warningMessageParent = getUIController().getWarningMessageParent();
        if (z) {
            i2 = 8;
        }
        warningMessageParent.setVisibility(i2);
    }

    private void waitCameraStartUpThread() {
        try {
            if (this.mCameraStartUpThread != null) {
                this.mCameraStartUpThread.cancel();
                this.mCameraStartUpThread.join();
                this.mCameraStartUpThread = null;
                setCameraState(1);
            }
        } catch (InterruptedException e) {
        }
    }

    private void writeImage(byte[] bArr, int i) {
        Exception e;
        Object obj;
        Throwable th;
        Closeable closeable = null;
        try {
            String createJpegName = Util.createJpegName(System.currentTimeMillis());
            String generateFilepath = Storage.generateFilepath(createJpegName, ".dng");
            FileOutputStream fileOutputStream = new FileOutputStream(generateFilepath);
            try {
                Log.e("Camera", "write image to: " + generateFilepath + " with length: " + i);
                fileOutputStream.write(bArr, 0, i);
                fileOutputStream.close();
                closeable = null;
                Storage.addDNGToDataBase(this.mActivity, createJpegName);
                Util.closeSilently(null);
            } catch (Exception e2) {
                e = e2;
                obj = fileOutputStream;
                try {
                    Log.d("Camera", "exception: " + e.getMessage());
                    Util.closeSilently(closeable);
                } catch (Throwable th2) {
                    th = th2;
                    Util.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                obj = fileOutputStream;
                Util.closeSilently(closeable);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.d("Camera", "exception: " + e.getMessage());
            Util.closeSilently(closeable);
        }
    }

    protected void animateSwitchCamera() {
        if (Device.isMDPRender()) {
            this.mHandler.sendEmptyMessageDelayed(22, 100);
            enableCameraControls(true);
            this.mSwitchingCamera = false;
            return;
        }
        this.mHandler.sendEmptyMessage(7);
    }

    protected void applyMultiShutParameters(boolean z) {
    }

    public void autoFocus() {
        this.mFocusStartTime = System.currentTimeMillis();
        if (this.mFirstTimeInitialized && this.mFocusAreaSupported && !this.mSwitchingCamera) {
            this.mCameraDevice.autoFocus(this.mAutoFocusCallback);
            setCameraState(2);
        }
    }

    public void cancelAutoFocus() {
        this.mCameraDevice.cancelAutoFocus();
        if (this.mParameters.getExposureCompensation() != 0) {
            this.mKeepAdjustedEv = true;
        }
        if (this.mCameraState != 3) {
            setCameraState(1);
            setCameraParameters(-1);
        }
    }

    protected void cancelContinuousShot() {
        this.mCameraDevice.cancelPicture();
    }

    public boolean capture() {
        if (this.mCameraDevice == null || this.mCameraState == 3 || this.mSwitchingCamera) {
            return false;
        }
        tryRemoveCountDownMessage();
        this.mCaptureStartTime = System.currentTimeMillis();
        this.mBurstSpeedController.capture();
        this.mPostViewPictureCallbackTime = 0;
        this.mJpegImageData = null;
        Location location = null;
        if (!(Device.IS_MI2 || Device.IS_C1 || Device.IS_C8)) {
            this.mParameters = this.mCameraDevice.getParameters();
        }
        this.mJpegRotation = Util.getJpegRotation(this.mCameraId, this.mOrientation);
        setPictureOrientation();
        this.mParameters.setRotation(this.mJpegRotation);
        if (256 == this.mParameters.getPictureFormat()) {
            location = LocationManager.instance().getCurrentLocation();
        }
        Util.setGpsParameters(this.mParameters, location);
        prepareCapture();
        this.mCameraDevice.setParameters(this.mParameters);
        this.mTotalJpegCallbackNum = getBurstCount();
        if (!this.mIsZSLMode) {
            stopObjectTracking(false);
        }
        this.mLastIsEffect = false;
        this.mCameraCategory.takePicture(location);
        if (Device.isCaptureStopFaceDetection()) {
            this.mFaceDetectionStarted = false;
        }
        setCameraState(3);
        this.mBurstShotTitle = null;
        this.mMultiSnapPictureSize = this.mParameters.getPictureSize();
        this.mReceivedJpegCallbackNum = 0;
        prepareGroupShot();
        if (Integer.parseInt(getManualValue("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default))) > 250000 || !getString(C0049R.string.pref_face_beauty_close).equals(CameraSettings.getFaceBeautifyValue())) {
            hideLoadUI(false);
        }
        return true;
    }

    protected void closeCamera() {
        Log.v("Camera", "closeCamera");
        if (this.mCameraDevice != null) {
            if (this.mSetMetaCallback) {
                this.mSetMetaCallback = false;
                this.mCameraDevice.setMetaDataCallback(null);
            }
            this.mCameraDevice.setFaceDetectionListener(null);
            this.mCameraDevice.setErrorCallback(null);
            this.mCameraDevice.setOneShotPreviewCallback(null);
            this.mCameraDevice.setAutoFocusMoveCallback(null);
            this.mCameraDevice.addRawImageCallbackBuffer(null);
            this.mCameraDevice.removeAllAsyncMessage();
            CameraHolder.instance().release();
            this.mFaceDetectionStarted = false;
            this.m3ALocked = false;
            this.mCameraDevice = null;
            setCameraState(0);
            if (this.mFocusManager != null && currentIsMainThread()) {
                this.mFocusManager.onCameraReleased();
            }
        }
    }

    protected void enterMutexMode() {
        if (!(this.mMutexModePicker.isHdr() || isDetectedHHT() || this.mMutexModePicker.isSupportedFlashOn())) {
            getUIController().getFlashButton().keepSetValue("off");
        }
        setOrientationIndicator(this.mOrientationCompensation, false);
        if (this.mMutexModePicker.isUbiFocus()) {
            setZoomValue(0);
        }
        if (this.mMutexModePicker.isBurstShoot()) {
            Util.clearMemoryLimit();
        }
        if (!CameraSettings.getFocusMode().equals(getString(C0049R.string.pref_camera_focusmode_value_default))) {
            CameraSettings.setFocusModeSwitching(true);
        }
        List arrayList = new ArrayList();
        arrayList.addAll(Arrays.asList(new String[]{"pref_qc_camera_iso_key", null, "pref_qc_camera_exposuretime_key", null, "pref_camera_face_beauty_key", null, "pref_camera_focus_mode_key", null, "pref_camera_whitebalance_key", null, "pref_camera_coloreffect_key", null}));
        if (this.mMutexModePicker.isUbiFocus()) {
            getUIController().getFocusView().clear();
            updateWarningMessage(C0049R.string.ubifocus_capture_warning_message, false);
        }
        if (this.mMutexModePicker.isUbiFocus() || this.mMutexModePicker.isBurstShoot()) {
            arrayList.add("pref_camera_shader_coloreffect_key");
            arrayList.add(null);
        }
        if (!(this.mMutexModePicker.isHdr() || this.mMutexModePicker.isHandNight())) {
            arrayList.add("pref_camera_exposure_key");
            arrayList.add(null);
        }
        this.mSettingsOverrider.overrideSettings((String[]) arrayList.toArray(new String[arrayList.size()]));
        setCameraParameters(2);
        checkRestartPreview();
        getUIController().getEffectCropView().updateVisible();
        getUIController().getSettingsStatusBar().updateStatus();
    }

    protected void exitMutexMode() {
        boolean isDetectedHHT = (this.mMutexModePicker.getLastMutexMode() == 0 || this.mMutexModePicker.getLastMutexMode() == 4 || this.mMutexModePicker.getLastMutexMode() == 2 || this.mMutexModePicker.getLastMutexMode() == 5 || this.mMutexModePicker.getLastMutexMode() == 1) ? true : this.mMutexModePicker.getLastMutexMode() == 3 ? isDetectedHHT() : false;
        if (!isDetectedHHT) {
            getUIController().getFlashButton().restoreKeptValue();
        }
        this.mSettingsOverrider.restoreSettings();
        updateWarningMessage(0, true);
        if (!(this.m3ALocked || CameraSettings.getFocusMode().equals(getString(C0049R.string.pref_camera_focusmode_value_default)))) {
            CameraSettings.setFocusModeSwitching(true);
        }
        if (this.mCameraState == 3) {
            startPreview();
        } else {
            setCameraParameters(2);
        }
        checkRestartPreview();
        getUIController().getEffectCropView().updateVisible();
        getUIController().getSettingsStatusBar().updateStatus();
    }

    protected boolean exitWhiteBalanceLockMode() {
        return false;
    }

    public void findQRCode() {
    }

    protected PictureSize getBestPictureSize() {
        PictureSizeManager.initialize(getActivity(), this.mParameters.getSupportedPictureSizes(), getMaxPictureSize());
        return PictureSizeManager.getBestPictureSize();
    }

    protected int getBurstCount() {
        return this.mMultiSnapStatus ? BURST_SHOOTING_COUNT : this.mMutexModePicker.isUbiFocus() ? 7 : (this.mMutexModePicker.isSceneHdr() && Device.IS_HM2A) ? this.mParameters.getInt("num-snaps-per-shutter") : (CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key") && this.mGroupShotTimes <= 5 && (Util.isMemoryRich(this.mActivity) || this.mGroupShotTimes == 0)) ? getGroupshotNum() : 1;
    }

    protected int getBurstDelayTime() {
        return 0;
    }

    protected int getCameraRotation() {
        return ((this.mOrientationCompensation - this.mDisplayRotation) + 360) % 360;
    }

    protected String getManualValue(String str, String str2) {
        return CameraSettings.isSwitchOn("pref_camera_manual_mode_key") ? this.mPreferences.getString(str, str2) : str2;
    }

    protected int getMaxPictureSize() {
        if (CameraSettings.getShaderEffect() == 0) {
            return CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key") ? 7680000 : 0;
        } else {
            if (Device.isSupportFullSizeEffect()) {
                return 0;
            }
            return Device.isLowerEffectSize() ? 3145728 : 9000000;
        }
    }

    protected int getMutexHdrMode(String str) {
        if (!getString(C0049R.string.pref_camera_hdr_entryvalue_normal).equals(str)) {
            return (Device.isSupportedAoHDR() && getString(C0049R.string.pref_camera_hdr_entryvalue_live).equals(str)) ? 2 : 0;
        } else {
            int i = (!Device.isUsedNightMode() || (Device.isMTKPlatform() && !Device.isSupportedAsdHdr())) ? 5 : 1;
            return i;
        }
    }

    protected String getRequestFlashMode() {
        if (isSupportSceneMode()) {
            getUIController().getFlashButton().overrideValue(CameraSettings.getFlashModeByScene(this.mSceneMode));
        }
        return (this.mMutexModePicker.isSupportedFlashOn() || this.mMutexModePicker.isSupportedTorch()) ? getUIController().getFlashButton().getValue() : "off";
    }

    public List<String> getSupportedSettingKeys() {
        return getLayoutModeKeys(this.mActivity, isBackCamera(), this.mIsImageCaptureIntent);
    }

    public boolean handleMessage(int i, int i2, final Object obj, Object obj2) {
        if (super.handleMessage(i, i2, obj, obj2)) {
            return true;
        }
        switch (i2) {
            case C0049R.id.hide_mode_animation_done:
                if (this.mSetCameraParameter != 0) {
                    this.mHandler.removeMessages(32);
                    this.mHandler.sendEmptyMessageDelayed(32, 200);
                }
                if (this.mManualModeSwitched) {
                    this.mManualModeSwitched = false;
                    this.mHandler.removeMessages(41);
                    this.mHandler.sendEmptyMessageDelayed(41, 200);
                }
                return true;
            case C0049R.id.v6_thumbnail_button:
                onThumbnailClicked(null);
                return true;
            case C0049R.id.v6_shutter_button:
                if (i == 0) {
                    onShutterButtonClick();
                    if (!isCountDownMode()) {
                        CameraDataAnalytics.instance().trackEvent("capture_times_shutter");
                    }
                    if (this.mFocusManager.getMeteringAreas() != null) {
                        CameraDataAnalytics.instance().trackEvent("touch_focus_shutter_capture_times_key");
                    }
                } else if (i == 1) {
                    onShutterButtonLongClick();
                } else if (i == 2) {
                    if (isBackCamera()) {
                        Point point = (Point) obj;
                        Point point2 = (Point) obj2;
                        getUIController().getSmartShutterButton().flyin(point.x, point.y, point2.x, point2.y);
                    }
                } else if (i == 3) {
                    onShutterButtonFocus(((Boolean) obj).booleanValue(), 2);
                    if (this.mMutexModePicker.isBurstShoot() && ((Boolean) obj).booleanValue()) {
                        CameraDataAnalytics.instance().trackEvent("capture_times_shutter");
                    }
                }
                return true;
            case C0049R.id.v6_module_picker:
                Runnable c01067 = new Runnable() {
                    public void run() {
                        CameraModule.this.mActivity.getCameraScreenNail().animateModuleChangeBefore();
                        CameraModule.this.switchToOtherMode(((Integer) obj).intValue());
                    }
                };
                enableCameraControls(false);
                getUIController().getShutterButton().onPause();
                getUIController().getFocusView().clear();
                getUIController().getBottomControlLowerPanel().animationSwitchToVideo(c01067);
                this.mActivity.getCameraScreenNail().switchModule();
                return true;
            case C0049R.id.mode_button:
                tryRemoveCountDownMessage();
                resetMetaDataManager();
                QRCodeManager.instance(this.mActivity).hideViewFinderFrame();
                return true;
            case C0049R.id.v6_camera_picker:
                return onCameraPickerClicked(((Integer) obj).intValue());
            case C0049R.id.capture_control_panel:
                if (i == 0) {
                    onReviewDoneClicked(null);
                } else {
                    onReviewCancelClicked(null);
                }
                return true;
            case C0049R.id.v6_flash_mode_button:
                if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") || CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                    this.mSettingsOverrider.removeSavedSetting("pref_camera_flashmode_key");
                    if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                        getUIController().getStereoButton().switchOffStereo(true);
                    } else {
                        this.mSettingsOverrider.removeSavedSetting("pref_camera_hdr_key");
                        getUIController().getPortraitButton().switchOff();
                    }
                    getUIController().getHdrButton().updateHdrAccordingFlash(getUIController().getFlashButton().getValue());
                    return true;
                }
                if (!(!CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key") || "off".equals(getUIController().getFlashButton().getValue()) || "torch".equals(getUIController().getFlashButton().getValue()))) {
                    getUIController().getModeExitView().clearExitButtonClickListener(true);
                }
                if (!(this.mMutexModePicker.isNormal() || this.mMutexModePicker.isHdr() || this.mMutexModePicker.isSupportedFlashOn() || this.mMutexModePicker.isSupportedTorch())) {
                    if (getUIController().getModeExitView().isExitButtonShown()) {
                        getUIController().getModeExitView().clearExitButtonClickListener(true);
                    } else if (this.mMutexModePicker.isHandNight() && isDetectedHHT()) {
                        this.mMutexModePicker.resetMutexMode();
                    }
                }
                tryRemoveCountDownMessage();
                getUIController().getHdrButton().updateHdrAccordingFlash(getUIController().getFlashButton().getValue());
                stopObjectTracking(true);
                updateASD("pref_camera_flashmode_key");
                onSharedPreferenceChanged();
                return true;
            case C0049R.id.v6_hdr:
                if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                    this.mSettingsOverrider.removeSavedSetting("pref_camera_hdr_key");
                    getUIController().getStereoButton().switchOffStereo(true);
                    return true;
                } else if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                    this.mSettingsOverrider.removeSavedSetting("pref_camera_flashmode_key");
                    this.mSettingsOverrider.removeSavedSetting("pref_camera_hdr_key");
                    getUIController().getPortraitButton().switchOff();
                    return true;
                } else {
                    stopObjectTracking(true);
                    getUIController().getFlashButton().updateFlashModeAccordingHdr(getUIController().getHdrButton().getValue());
                    updateHDRPreference();
                    return true;
                }
            case C0049R.id.skin_beatify_button:
                onSharedPreferenceChanged();
                return true;
            case C0049R.id.portrait_switch_image:
                onSettingValueChanged((String) obj);
                return true;
            case C0049R.id.stereo_switch_image:
                if (i == 7) {
                    onSettingValueChanged((String) obj);
                } else {
                    onStereoModeChanged();
                }
                return true;
            case C0049R.id.v6_frame_layout:
                if (i == 0) {
                    if (this.mFocusManager != null) {
                        Point point3 = (Point) obj;
                        this.mFocusManager.setPreviewSize(point3.x, point3.y);
                    }
                } else if (i == 1) {
                    onFrameLayoutChange((View) obj, (Rect) obj2);
                }
                return true;
            case C0049R.id.v6_focus_view:
                if (i == 2) {
                    onShutterButtonClick();
                }
                return true;
            case C0049R.id.v6_setting_page:
                if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                    if ("pref_camera_shader_coloreffect_key".equals(obj)) {
                        this.mSettingsOverrider.removeSavedSetting("pref_camera_shader_coloreffect_key");
                    }
                    if ("pref_camera_panoramamode_key".equals(obj) && CameraSettings.isSwitchOn("pref_camera_panoramamode_key")) {
                        getUIController().getStereoButton().switchOffStereo(false);
                        closeCamera();
                        switchToOtherMode(2);
                    } else {
                        getUIController().getStereoButton().switchOffStereo(true);
                    }
                    return true;
                }
                if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key") && !"pref_camera_shader_coloreffect_key".equals(obj)) {
                    getUIController().getPortraitButton().switchOff(false);
                }
                if (getUIController().getHdrButton().getVisibility() == 0) {
                    getUIController().getPeakButton().updateVisible();
                    getUIController().getHdrButton().updateVisible();
                }
                if (getUIController().getSettingPage().isItemSelected()) {
                    stopObjectTracking(true);
                }
                if (i == 7) {
                    onSettingValueChanged((String) obj);
                } else if (i == 6) {
                    onModeSelected(obj);
                }
                if (getUIController().getHdrButton().getVisibility() == 8) {
                    getUIController().getHdrButton().updateVisible();
                    getUIController().getPeakButton().updateVisible();
                }
                getUIController().getStereoButton().updateVisible();
                getUIController().getPortraitButton().updateVisible();
                return true;
            case C0049R.id.setting_page_watermark_option:
                onSettingValueChanged((String) obj);
                return true;
            case C0049R.id.setting_button:
                openSettingActivity();
                return true;
            case C0049R.id.edge_shutter_view:
                onShutterButtonClick();
                return true;
            default:
                return false;
        }
    }

    protected void handleMultiSnapDone() {
        if (!this.mPaused) {
            restoreStatusAfterBurst();
            final int i = this.mReceivedJpegCallbackNum;
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (i > 1) {
                        String string = CameraModule.this.getResources().getString(C0049R.string.toast_burst_snap_finished_start);
                        Toast.makeText(CameraModule.this.mActivity, string + " " + i + " " + CameraModule.this.getResources().getString(C0049R.string.toast_burst_snap_finished_end), 0).show();
                    }
                    if (!CameraModule.this.mMultiSnapStatus) {
                        CameraModule.this.getUIController().getMultiSnapNum().setVisibility(8);
                    }
                    CameraModule.this.mBurstShotTitle = null;
                    CameraModule.this.mUpdateImageTitle = false;
                }
            });
            updateHDRPreference();
        }
    }

    protected void initializeAfterCameraOpen() {
        setPreviewFrameLayoutAspectRatio();
        initializeZoom();
        initializeExposureCompensation();
        showTapToFocusToastIfNeeded();
        QRCodeManager.instance(this.mActivity).setCameraDevice(this.mCameraDevice);
    }

    public boolean isCameraEnabled() {
        return (this.mPaused || this.mSwitchingCamera || this.mCameraState == 0) ? false : true;
    }

    public boolean isCaptureIntent() {
        return this.mIsImageCaptureIntent;
    }

    protected boolean isDefaultManualExposure() {
        return isDefaultPreference("pref_qc_camera_iso_key", getString(C0049R.string.pref_camera_iso_default)) ? isDefaultPreference("pref_qc_camera_exposuretime_key", getString(C0049R.string.pref_camera_exposuretime_default)) : false;
    }

    protected boolean isDefaultPreference(String str, String str2) {
        return str2.equals(getManualValue(str, str2));
    }

    protected boolean isDetectedHHT() {
        boolean z = true;
        if (!Device.isSupportedAsdNight() && !Device.isSupportedAsdMotion()) {
            return false;
        }
        if (!(2 == this.mMetaDataManager.mCurrentScene || 2 == this.mMetaDataManager.mLastScene || 3 == this.mMetaDataManager.mCurrentScene || 3 == this.mMetaDataManager.mLastScene)) {
            z = false;
        }
        return z;
    }

    protected boolean isFaceBeautyMode() {
        return false;
    }

    protected boolean isFrontMirror() {
        if (!isFrontCamera()) {
            return false;
        }
        String frontMirror = CameraSettings.getFrontMirror(this.mPreferences);
        return getString(C0049R.string.pref_front_mirror_entryvalue_auto).equals(frontMirror) ? getUIController().getFaceView().faceExists() : getString(C0049R.string.pref_front_mirror_entryvalue_on).equals(frontMirror);
    }

    public boolean isKeptBitmapTexture() {
        return this.mKeepBitmapTexture;
    }

    protected boolean isLongShotMode() {
        return false;
    }

    public boolean isMeteringAreaOnly() {
        String focusMode = this.mParameters.getFocusMode();
        return ((!this.mFocusAreaSupported && this.mMeteringAreaSupported) || "edof".equals(focusMode) || "fixed".equals(focusMode) || "infinity".equals(focusMode) || "manual".equals(focusMode)) ? true : "lock".equals(focusMode);
    }

    public boolean isNeedMute() {
        return !super.isNeedMute() ? CameraSettings.isAudioCaptureOpen() : true;
    }

    protected boolean isSceneMotion() {
        return this.mMetaDataManager.mCurrentScene == 3;
    }

    public boolean isShowCaptureButton() {
        return !this.mMutexModePicker.isBurstShoot();
    }

    protected boolean isSupportSceneMode() {
        return false;
    }

    protected boolean isZeroShotMode() {
        return false;
    }

    protected boolean isZoomEnabled() {
        return (this.mMutexModePicker.isUbiFocus() || CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") || CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) ? false : true;
    }

    public boolean multiCapture() {
        if (this.mIsImageCaptureIntent || this.mCameraState == 3 || this.mCameraDevice == null || this.mSwitchingCamera || isFrontCamera()) {
            return false;
        }
        if (this.mAudioCaptureManager.isRunning()) {
            Toast.makeText(this.mActivity, C0049R.string.toast_burst_snap_forbidden_when_audio_capture_open, 0).show();
            return false;
        }
        this.mActivity.getScreenHint().updateHint();
        if (Storage.isLowStorageAtLastPoint()) {
            Log.i("Camera", "Not enough space or storage not ready. remaining=" + Storage.getLeftSpace());
            return false;
        } else if (this.mActivity.getImageSaver().shouldStopShot()) {
            Log.i("Camera", "ImageSaver is full, wait for a moment!");
            RotateTextToast.getInstance(this.mActivity).show(C0049R.string.toast_saving, 0);
            return false;
        } else {
            Location location = null;
            if (256 == this.mParameters.getPictureFormat()) {
                location = LocationManager.instance().getCurrentLocation();
            }
            this.mCaptureStartTime = System.currentTimeMillis();
            this.mBurstSpeedController.capture();
            if (!Device.IS_MI2) {
                this.mParameters = this.mCameraDevice.getParameters();
            }
            Util.setGpsParameters(this.mParameters, location);
            this.mJpegRotation = Util.getJpegRotation(this.mCameraId, this.mOrientation);
            this.mParameters.setRotation(this.mJpegRotation);
            prepareMultiCapture();
            this.mCameraDevice.setParameters(this.mParameters);
            saveStatusBeforeBurst();
            this.mTotalJpegCallbackNum = getBurstCount();
            if (!this.mUpdateImageTitle || this.mBurstShotTitle == null) {
                this.mReceivedJpegCallbackNum = 0;
                this.mBurstShotTitle = null;
            } else {
                this.mReceivedJpegCallbackNum = 1;
            }
            this.mMultiSnapStopRequest = false;
            this.mMultiSnapPictureSize = this.mParameters.getPictureSize();
            this.mCameraDevice.takePicture(new JpegQuickShutterCallback(), this.mRawPictureCallback, null, new JpegQuickPictureCallback(LocationManager.instance().getCurrentLocation()));
            CameraDataAnalytics.instance().trackEvent("burst_times");
            getUIController().getMultiSnapNum().setText("");
            getUIController().getMultiSnapNum().setVisibility(0);
            return true;
        }
    }

    protected boolean needAutoFocusBeforeCapture() {
        return false;
    }

    protected boolean needSetupPreview(boolean z) {
        return true;
    }

    protected boolean needSwitchZeroShotMode() {
        return false;
    }

    public void notifyError() {
        super.notifyError();
        setCameraState(0);
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        switch (i) {
            case 1000:
                Intent intent2 = new Intent();
                if (intent != null) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        intent2.putExtras(extras);
                    }
                }
                this.mActivity.setResult(i2, intent2);
                this.mActivity.finish();
                this.mActivity.getFileStreamPath("crop-temp").delete();
                return;
            default:
                return;
        }
    }

    public boolean onBackPressed() {
        boolean z = false;
        AutoLockManager.getInstance(this.mActivity).onUserInteraction();
        if (this.mCameraStartUpThread != null) {
            return false;
        }
        if (isSelectingCapturedImage()) {
            onReviewCancelClicked(getUIController().getReviewCanceledView());
            return true;
        } else if ((!this.mMutexModePicker.isNormal() && this.mCameraState == 3) || QRCodeManager.instance(this.mActivity).onBackPressed() || exitWhiteBalanceLockMode()) {
            return true;
        } else {
            tryRemoveCountDownMessage();
            if (getUIController().onBack() || this.mCameraState == 3) {
                return true;
            }
            if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                getUIController().getStereoButton().setStereoValue(false, true, true);
                return true;
            } else if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                getUIController().getPortraitButton().switchOff();
                if (!Util.isPortraitIntent(this.mActivity)) {
                    z = true;
                }
                return z;
            } else {
                if (getUIController().getSettingPage().isItemSelected()) {
                    boolean resetSettings = getUIController().getSettingPage().resetSettings();
                    if (this.mAudioCaptureManager.isRunning()) {
                        this.mAudioCaptureManager.close();
                    }
                    if (resetSettings) {
                        return true;
                    }
                }
                if (!getUIController().getPreviewPage().isPopupShown()) {
                    return this.mAudioCaptureManager.onBackPressed() ? true : super.onBackPressed();
                } else {
                    PopupManager.getInstance(this.mActivity).notifyShowPopup(null, 1);
                    return true;
                }
            }
        }
    }

    public void onCameraMetaData(byte[] bArr, Camera camera) {
        if (!this.mPaused && getUIController().getPreviewPage().isPreviewPageVisible() && !this.mActivity.getCameraScreenNail().isModuleSwitching() && this.mCameraState == 1 && !this.mMultiSnapStatus && !this.mHandler.hasMessages(26)) {
            this.mMetaDataManager.setData(bArr);
        }
    }

    public boolean onCameraPickerClicked(int i) {
        if (this.mPaused || this.mPendingSwitchCameraId != -1 || this.mSwitchingCamera) {
            return false;
        }
        Log.v("Camera", "Start to copy texture. cameraId=" + i + " " + CameraSettings.isBackCamera());
        if (Device.isMDPRender()) {
            this.mActivity.setBlurFlag(true);
            this.mHandler.sendEmptyMessage(6);
        } else if (this.mSwitchCameraAnimationRunning) {
            synchronized (this.mSwitchCameraLater) {
                if (this.mSwitchCameraLater.booleanValue()) {
                    this.mSwitchCameraLater = Boolean.valueOf(false);
                } else {
                    this.mHandler.sendEmptyMessage(6);
                }
            }
        } else {
            prepareSwitchCameraAnimation(this.mPreviewTextureCopiedActionSwitchCamera);
        }
        this.mPendingSwitchCameraId = i;
        setCameraState(4);
        this.mSwitchingCamera = true;
        exitWhiteBalanceLockMode();
        QRCodeManager.instance(this.mActivity).hideViewFinderFrame();
        return true;
    }

    protected void onCameraStartPreview() {
    }

    public void onCreate(com.android.camera.Camera camera) {
        super.onCreate(camera);
        this.mPreferences = CameraSettingPreferences.instance();
        CameraSettings.upgradeGlobalPreferences(this.mPreferences);
        if (isRestoring()) {
            this.mActivity.getCameraAppImpl().resetRestoreFlag();
        } else {
            resetCameraSettingsIfNeed();
        }
        this.mCameraId = getPreferredCameraId();
        changeConflictPreference();
        this.mActivity.changeRequestOrientation();
        if (PermissionManager.checkCameraLaunchPermissions()) {
            this.mCameraStartUpThread = new CameraStartUpThread();
            this.mCameraStartUpThread.start();
        }
        this.mIsImageCaptureIntent = this.mActivity.isImageCaptureIntent();
        CameraSettingPreferences.instance().setLocalId(getPreferencesLocalId());
        this.mActivity.createContentView();
        this.mActivity.createCameraScreenNail(!this.mIsImageCaptureIntent, false);
        V6ModulePicker.setCurrentModule(0);
        getUIController().onCreate();
        getUIController().useProperView();
        prepareUIByPreviewSize();
        this.mActivity.getSensorStateManager().setSensorStateListener(this.mSensorStateListener);
        QRCodeManager.instance(this.mActivity).onCreate(this.mActivity, this.mHandler.getLooper(), this);
        this.mStartPreviewPrerequisiteReady.open();
        if (this.mIsImageCaptureIntent) {
            setupCaptureParams();
        }
        this.mQuickCapture = this.mActivity.getIntent().getBooleanExtra("android.intent.extra.quickCapture", false);
        initializeMutexMode();
        this.mAudioCaptureManager = new AudioCaptureManager(this, this.mActivity);
        enableCameraControls(false);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mActivity.getScreenHint().hideToast();
        this.mJpegImageData = null;
        this.mNeedSealCameraUUID = false;
        this.mCameraUUIDWatermarkImageData = null;
    }

    public void onFaceDetection(Face[] faceArr, Camera camera) {
        if (this.mSwitchingCamera || this.mActivity.getCameraScreenNail().isModuleSwitching()) {
            getUIController().getFaceView().clear();
            return;
        }
        CameraHardwareFace[] convertCameraHardwareFace = CameraHardwareFace.convertCameraHardwareFace(faceArr);
        if (Device.isSupportedObjectTrack() && convertCameraHardwareFace.length > 0 && convertCameraHardwareFace[0].faceType == 64206) {
            if (this.mObjectTrackingStarted) {
                getUIController().getObjectView().setObject(convertCameraHardwareFace[0]);
            }
        } else if (getUIController().getFaceView().setFaces(convertCameraHardwareFace) && this.mCameraState != 2 && getUIController().getFaceView().faceExists() && "continuous-picture".equals(this.mParameters.getFocusMode())) {
            this.mFocusManager.resetFocusIndicator();
        }
    }

    public boolean onGestureTrack(RectF rectF, boolean z) {
        return couldEnableObjectTrack() ? initializeObjectTrack(rectF, z) : false;
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        boolean z = false;
        switch (i) {
            case 24:
            case 25:
                if (isPreviewVisible()) {
                    if (i == 24) {
                        z = true;
                    }
                    if (handleVolumeKeyEvent(z, true, keyEvent.getRepeatCount())) {
                        return true;
                    }
                }
                break;
            case 27:
            case 66:
                if (keyEvent.getRepeatCount() == 0 && isPreviewVisible()) {
                    onShutterButtonClick();
                    if (Util.isFingerPrintKeyEvent(keyEvent)) {
                        CameraDataAnalytics.instance().trackEvent("capture_times_finger");
                    }
                }
                return true;
            case 80:
                if (keyEvent.getRepeatCount() == 0 && isPreviewVisible()) {
                    onShutterButtonFocus(true, 1);
                }
                return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        switch (i) {
            case 24:
            case 25:
                if (isPreviewVisible()) {
                    if (handleVolumeKeyEvent(i == 24, false, keyEvent.getRepeatCount())) {
                        return true;
                    }
                }
                break;
        }
        return super.onKeyUp(i, keyEvent);
    }

    public void onLongPress(int i, int i2) {
        if (isInTapableRect(i, i2)) {
            onSingleTapUp(i, i2);
            this.mHandler.post(this.mDoSnapRunnable);
            CameraDataAnalytics.instance().trackEvent("capture_times_long_press");
            getUIController().getPreviewFrame().performHapticFeedback(0);
        }
    }

    public void onNewIntent() {
        this.mCameraId = getPreferredCameraId();
        changeConflictPreference();
        this.mIsImageCaptureIntent = this.mActivity.isImageCaptureIntent();
        CameraSettingPreferences.instance().setLocalId(getPreferencesLocalId());
        if (Util.isPortraitIntent(this.mActivity) && !CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
            this.mPreferences.edit().putString("pref_camera_portrait_mode_key", "on").apply();
        }
    }

    public void onObjectStable() {
        if (this.mCameraState != 3 && !this.mFocusManager.isFocusingSnapOnFinish() && getUIController().getPreviewPage().isPreviewPageVisible()) {
            this.mFocusManager.requestAutoFocus();
            if (this.mPreferences.getBoolean("pref_capture_when_stable_key", false)) {
                Log.v("Camera", "Object is Stable, call onShutterButtonClick to capture");
                onShutterButtonClick();
                CameraDataAnalytics.instance().trackEvent("capture_times_t2t");
            }
        }
    }

    public void onOrientationChanged(int i) {
        this.mDeviceRotation = (float) i;
        if (!CameraSettings.isSwitchOn("pref_camera_gradienter_key")) {
            setOrientation(i);
        }
    }

    public void onPauseBeforeSuper() {
        resetMetaDataManager();
        super.onPauseBeforeSuper();
        if (!this.mMutexModePicker.isNormal()) {
            updateExitButton(true);
        }
        hideLoadUI(true);
        if (this.mMultiSnapStatus) {
            this.mMultiSnapStatus = false;
            setCameraState(1);
            if (Device.isQcomPlatform() || Device.isLCPlatform() || Device.isMTKPlatform()) {
                cancelContinuousShot();
            }
            getUIController().getMultiSnapNum().setVisibility(8);
        }
        exitWhiteBalanceLockMode();
        this.mAudioCaptureManager.onPause();
        resetGradienter();
        resetFaceBeautyMode();
        updateLyingSensorState(false);
        updateStereoSettings(false);
        waitCameraStartUpThread();
        if (!(this.mCameraDevice == null || this.mCameraState == 0)) {
            this.mCameraDevice.cancelAutoFocus();
        }
        stopFaceDetection(true);
        if (this.mActivity.isGotoGallery() ? Device.isReleaseLaterForGallery() : false) {
            this.mWaitForRelease = true;
            enableCameraControls(false);
        } else {
            releaseResources();
        }
        getUIController().onPause();
        if (!(this.mActivity.isActivityPaused() || CameraSettings.isSwitchOn("pref_camera_stereo_mode_key"))) {
            PopupManager.getInstance(this.mActivity).notifyShowPopup(null, 1);
        }
        if (this.mEffectProcessor != null) {
            this.mEffectProcessor.setImageSaver(null);
            this.mEffectProcessor.release();
            this.mEffectProcessor = null;
        }
        if (this.mDidRegister) {
            this.mActivity.unregisterReceiver(this.mReceiver);
            this.mDidRegister = false;
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.removeMessages();
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(6);
        this.mHandler.removeMessages(7);
        this.mHandler.removeMessages(8);
        this.mHandler.removeMessages(9);
        this.mHandler.removeMessages(22);
        this.mHandler.removeMessages(10);
        this.mHandler.removeMessages(5);
        this.mHandler.removeMessages(15);
        this.mHandler.removeMessages(24);
        this.mHandler.removeMessages(25);
        this.mHandler.removeMessages(29);
        this.mHandler.removeMessages(30);
        this.mHandler.removeMessages(31);
        this.mHandler.removeMessages(32);
        this.mHandler.removeMessages(34);
        this.mHandler.removeMessages(37);
        this.mHandler.removeMessages(36);
        this.mHandler.removeMessages(40);
        this.mHandler.removeMessages(43);
        this.mSetCameraParameter = 0;
        this.mIsRecreateCameraScreenNail = false;
        tryRemoveCountDownMessage();
        this.mActivity.getSensorStateManager().reset();
        resetScreenOn();
        updateWarningMessage(0, true);
        QRCodeManager.instance(this.mActivity).onPause();
        this.mPendingSwitchCameraId = -1;
        this.mSwitchingCamera = false;
        this.mSwitchCameraAnimationRunning = false;
        if (this.mHasPendingSwitching) {
            int height = getUIController().getPreviewFrame().getHeight();
            int width = getUIController().getPreviewFrame().getWidth();
            if (!(height == 0 || width == 0)) {
                this.mUIStyle = CameraSettings.getUIStyleByPreview(height, width);
            }
            this.mHasPendingSwitching = false;
        }
        if (this.mMutexModePicker.isUbiFocus() && this.mTotalJpegCallbackNum == 7 && this.mReceivedJpegCallbackNum > 0 && this.mReceivedJpegCallbackNum < this.mTotalJpegCallbackNum) {
            Storage.deleteImage(this.mBurstShotTitle);
        }
    }

    public void onPreviewPixelsRead(byte[] bArr, int i, int i2) {
        animateShutter();
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Config.ARGB_8888);
        createBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bArr));
        this.mHandler.obtainMessage(42, Thumbnail.createThumbnail(null, createBitmap, this.mShootOrientation, false)).sendToTarget();
    }

    public void onPreviewTextureCopied() {
        this.mPreviewTextureCopiedCallback.onPreviewTextureCopied();
    }

    public void onResumeAfterSuper() {
        super.onResumeAfterSuper();
        if (!this.mOpenCameraFail && !this.mCameraDisabled && PermissionManager.checkCameraLaunchPermissions()) {
            if (!(this.mIsImageCaptureIntent && getUIController().getReviewDoneView().getVisibility() == 0)) {
                this.mKeepBitmapTexture = false;
                this.mActivity.getCameraScreenNail().releaseBitmapIfNeeded();
                getUIController().onResume();
            }
            this.mJpegPictureCallbackTime = 0;
            updateStereoSettings(true);
            if (this.mCameraStartUpThread == null && (this.mCameraState == 0 || this.mCameraDevice == null)) {
                this.mCameraStartUpThread = new CameraStartUpThread();
                this.mCameraStartUpThread.start();
                if (Util.checkDeviceHasNavigationBar(this.mActivity)) {
                    CameraSettings.changeUIByPreviewSize(this.mActivity, this.mUIStyle);
                }
            } else if (this.mWaitForRelease) {
                resumePreview();
            }
            this.mWaitForRelease = false;
            if (!this.mIsImageCaptureIntent) {
                this.mActivity.getThumbnailUpdater().getLastThumbnail();
            }
            if (this.mFirstTimeInitialized) {
                initializeSecondTime();
            } else {
                this.mHandler.sendEmptyMessage(1);
            }
            keepScreenOnAwhile();
            this.mActivity.loadCameraSound(1);
            this.mActivity.loadCameraSound(0);
            this.mActivity.loadCameraSound(4);
            this.mActivity.loadCameraSound(5);
            this.mActivity.loadCameraSound(7);
            this.mAudioCaptureManager.onResume();
        }
    }

    public void onResumeBeforeSuper() {
        super.onResumeBeforeSuper();
    }

    @OnClickAttr
    public void onReviewCancelClicked(View view) {
        this.mKeepBitmapTexture = false;
        if (isSelectingCapturedImage()) {
            previewBecomeVisible();
            setCameraState(1);
            hidePostCaptureAlert();
            return;
        }
        this.mActivity.setResult(0, new Intent());
        this.mActivity.finish();
    }

    @OnClickAttr
    public void onReviewDoneClicked(View view) {
        doAttach();
    }

    public void onSettingValueChanged(String str) {
        super.onSettingValueChanged(str);
        if (this.mCameraDevice != null) {
            if ("pref_delay_capture_key".equals(str)) {
                handleDelayShutter();
            } else {
                if ("pref_camera_focus_mode_key".equals(str)) {
                    getUIController().getPeakButton().updateVisible();
                }
                if ("pref_camera_portrait_mode_key".equals(str)) {
                    CameraSettings.resetZoom(this.mPreferences);
                    CameraSettings.resetExposure();
                    if (!(CameraSettings.isSwitchOn("pref_camera_manual_mode_key") || CameraSettings.isSwitchOn("pref_camera_panoramamode_key"))) {
                        prepareSwitchCameraAnimation(this.mPreviewTextureCopiedActionByPass);
                        stopPreview();
                        if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
                            this.mSettingsOverrider.overrideSettings("pref_camera_flashmode_key", "off", "pref_camera_hdr_key", "off");
                            if (CameraSettings.isDualCameraHintShown(this.mPreferences)) {
                                this.mHandler.sendEmptyMessage(40);
                            }
                            this.mMutexModePicker.resetMutexModeDummy();
                        } else {
                            this.mSettingsOverrider.restoreSettings();
                            this.mHandler.removeMessages(40);
                            if (TextUtils.equals(getUIController().getWarningMessageView().getText(), getString(C0049R.string.dual_camera_use_hint))) {
                                updateWarningMessage(0, true);
                            }
                            updateHDRPreference();
                        }
                        getUIController().getFlashButton().reloadPreference();
                        getUIController().getHdrButton().reloadPreference();
                        getUIController().getZoomButton().reloadPreference();
                        getUIController().getZoomButton().updateVisible();
                        startPreview();
                        enableCameraControls(false);
                        this.mHandler.sendEmptyMessageDelayed(43, 500);
                    }
                } else {
                    onSharedPreferenceChanged();
                }
            }
        }
    }

    public void onSharedPreferenceChanged() {
        if (!this.mPaused) {
            LocationManager.instance().recordLocation(CameraSettings.isRecordLocation(this.mPreferences));
            setCameraParametersWhenIdle(2);
        }
    }

    public void onShutterButtonClick() {
        Log.v("Camera", "onShutterButtonClick " + this.mCameraState);
        this.m3ALocked = false;
        if (!(!isShutterButtonClickable() || this.mMultiSnapStatus || this.mMutexModePicker.isBurstShoot())) {
            AutoLockManager.getInstance(this.mActivity).onUserInteraction();
            QRCodeManager.instance(this.mActivity).hideViewFinderFrame();
            if (this.mIsCountDown || !isCountDownMode()) {
                tryRemoveCountDownMessage();
                exitWhiteBalanceLockMode();
                this.mActivity.getScreenHint().updateHint();
                if (Storage.isLowStorageAtLastPoint()) {
                    Log.i("Camera", "Not enough space or storage not ready. remaining=" + Storage.getLeftSpace());
                } else if (this.mActivity.getImageSaver().shouldStopShot()) {
                    Log.i("Camera", "ImageSaver is full, wait for a moment!");
                    RotateTextToast.getInstance(this.mActivity).show(C0049R.string.toast_saving, 0);
                } else {
                    if (getUIController().getObjectView().isTrackFailed()) {
                        stopObjectTracking(false);
                    }
                    if (this.mCameraState != 3) {
                        this.mNeedAutoFocus = needAutoFocusBeforeCapture();
                    }
                    if (this.mCameraState == 3) {
                        if (!(this.mIsImageCaptureIntent || this.mNeedAutoFocus || !this.mMutexModePicker.isNormal())) {
                            this.mHandler.removeCallbacks(this.mDoSnapRunnable);
                            this.mSnapshotOnIdle = true;
                        }
                        return;
                    }
                    this.mLastShutterButtonClickTime = System.currentTimeMillis();
                    this.mSnapshotOnIdle = false;
                    this.mUpdateImageTitle = false;
                    this.mFocusManager.prepareCapture(this.mNeedAutoFocus, 2);
                    this.mFocusManager.doSnap();
                    if (this.mFocusManager.isFocusingSnapOnFinish()) {
                        enableCameraControls(false);
                    }
                    if (this.mKeepAdjustedEv) {
                        CameraDataAnalytics.instance().trackEvent("ev_adjust_keep_time_key");
                    }
                }
            } else {
                int countDownTimes = CameraSettings.getCountDownTimes();
                sendDelayedCaptureMessage(1000, countDownTimes);
                if (countDownTimes > 3) {
                    playSound(7);
                }
                this.mIsCountDown = true;
            }
        }
    }

    public void onShutterButtonFocus(boolean z, int i) {
        if (z) {
            if (this.mMutexModePicker.isBurstShoot()) {
                this.mFocusManager.doMultiSnap(false);
            }
        } else if (this.mPendingMultiCapture) {
            this.mPendingMultiCapture = false;
            return;
        } else if (this.mHandler.hasMessages(12)) {
            this.mHandler.removeMessages(12);
            return;
        } else if (!this.mFocusManager.cancelMultiSnapPending()) {
            if (this.mMultiSnapStatus) {
                this.mMultiSnapStopRequest = true;
                return;
            } else if (this.mPendingCapture) {
                this.mPendingCapture = false;
                if (getUIController().getShutterButton().isCanceled() || CameraSettings.isPressDownCapture()) {
                    this.mFocusManager.resetFocusStateIfNeeded();
                    cancelAutoFocus();
                } else {
                    onShutterButtonClick();
                }
                return;
            }
        } else {
            return;
        }
        if (!this.mPaused && this.mCameraState != 3 && this.mCameraState != 0 && !this.mSwitchingCamera && !isFrontCamera()) {
            if (!z || canTakePicture()) {
                if (z) {
                    if (needSwitchZeroShotMode()) {
                        setCameraParameters(2);
                    }
                    this.mFocusManager.onShutterDown();
                } else {
                    this.mFocusManager.onShutterUp();
                }
            }
        }
    }

    public boolean onShutterButtonLongClick() {
        if (this.mMutexModePicker.isBurstShoot() || this.mIsImageCaptureIntent) {
            return true;
        }
        if (CameraSettings.isBurstShootingEnable(this.mPreferences) && !getUIController().getSettingPage().isItemSelected() && !this.mIsImageCaptureIntent && !CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") && !CameraSettings.isSwitchOn("pref_camera_portrait_mode_key") && isBackCamera() && !this.mMultiSnapStatus && !this.mHandler.hasMessages(12) && !this.mHandler.hasMessages(24) && !this.mPendingMultiCapture) {
            this.mHandler.sendEmptyMessageDelayed(12, 0);
            if (Device.isSupportedFastCapture()) {
                this.mUpdateImageTitle = true;
            }
        } else if (this.mCameraState == 3) {
            return false;
        } else {
            this.mPendingCapture = true;
            this.mLongPressedAutoFocus = true;
            getUIController().getFocusView().setFocusType(false);
            this.mFocusManager.requestAutoFocus();
            this.mActivity.getScreenHint().updateHint();
            AutoLockManager.getInstance(this.mActivity).onUserInteraction();
            QRCodeManager.instance(this.mActivity).hideViewFinderFrame();
            exitWhiteBalanceLockMode();
        }
        return true;
    }

    public void onSingleTapUp(int i, int i2) {
        Log.v("Camera", "onSingleTapUp " + this.mPaused + " " + this.mCameraDevice + " " + this.mFirstTimeInitialized + " " + this.mCameraState + " " + this.mMultiSnapStatus + " " + this);
        getUIController().getEffectButton().dismissPopup();
        getUIController().getZoomButton().dismissPopup();
        getUIController().getPreviewPage().simplifyPopup(true, true);
        this.m3ALocked = false;
        this.mFocusManager.setAeAwbLock(false);
        if (!this.mPaused && this.mCameraDevice != null && this.mFirstTimeInitialized && !this.mActivity.getCameraScreenNail().isModuleSwitching() && isInTapableRect(i, i2) && this.mCameraState != 3 && this.mCameraState != 4 && this.mCameraState != 0 && !this.mMultiSnapStatus) {
            tryRemoveCountDownMessage();
            if ((this.mFocusAreaSupported || this.mMeteringAreaSupported) && !isSelectingCapturedImage()) {
                QRCodeManager.instance(this.mActivity).hideViewFinderFrame();
                if (!this.mMutexModePicker.isUbiFocus()) {
                    if (this.mObjectTrackingStarted) {
                        stopObjectTracking(true);
                    }
                    getUIController().getFocusView().setFocusType(true);
                    showObjectTrackToastIfNeeded();
                    Point point = new Point(i, i2);
                    mapTapCoordinate(point);
                    this.mFocusManager.onSingleTapUp(point.x, point.y);
                    if (!this.mFocusAreaSupported && this.mMeteringAreaSupported) {
                        this.mActivity.getSensorStateManager().reset();
                    }
                }
            }
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mActivity.isNeedResetGotoGallery() && Device.isReleaseLaterForGallery()) {
            releaseResources();
        }
        if (this.mMediaProviderClient != null) {
            this.mMediaProviderClient.release();
            this.mMediaProviderClient = null;
        }
    }

    public void onSwitchAnimationDone() {
        this.mHandler.sendEmptyMessage(28);
    }

    @OnClickAttr
    public void onThumbnailClicked(View view) {
        tryRemoveCountDownMessage();
        if (this.mActivity.getThumbnailUpdater().getThumbnail() != null) {
            this.mActivity.gotoGallery();
        }
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        keepScreenOnAwhile();
    }

    protected void openSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(this.mActivity, CameraPreferenceActivity.class);
        intent.putExtra("from_where", 1);
        intent.putExtra("IsCaptureIntent", this.mIsImageCaptureIntent);
        intent.putExtra(":miui:starting_window_label", getResources().getString(C0049R.string.pref_camera_settings_category));
        if (this.mActivity.startFromKeyguard()) {
            intent.putExtra("StartActivityWhenLocked", true);
        }
        this.mActivity.startActivity(intent);
        this.mActivity.setJumpFlag(2);
        CameraDataAnalytics.instance().trackEvent("pref_settings");
    }

    protected void performVolumeKeyClicked(int i, boolean z) {
        if (isShutterButtonClickable()) {
            if (i == 0) {
                if (z) {
                    onShutterButtonFocus(true, 2);
                    onShutterButtonClick();
                    if (!isCountDownMode()) {
                        CameraDataAnalytics.instance().trackEvent("capture_times_volume");
                    }
                    if (this.mParameters.getMeteringAreas() != null) {
                        CameraDataAnalytics.instance().trackEvent("touch_focus_volume_capture_times_key");
                    }
                } else {
                    onShutterButtonFocus(false, 0);
                    this.mVolumeLongPress = false;
                }
            } else if (z && !this.mVolumeLongPress) {
                onShutterButtonLongClick();
                this.mVolumeLongPress = true;
                this.mUpdateImageTitle = true;
            }
        }
    }

    protected boolean playAnimationBeforeCapture() {
        return (isZeroShotMode() || this.mMutexModePicker.isNeedComposed()) ? (Device.isHDRFreeze() && this.mMutexModePicker.isHdr()) ? false : true : false;
    }

    public void playSound(int i) {
        if (!this.mAudioCaptureManager.isRunning() || i != 1) {
            playCameraSound(i);
        }
    }

    protected void prepareCapture() {
    }

    protected void prepareMultiCapture() {
        applyMultiShutParameters(true);
    }

    public boolean readyToAudioCapture() {
        boolean z = false;
        if (this.mHandler.hasMessages(20)) {
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (((this.mCameraState == 2 && !this.mFocusManager.isFocusingSnapOnFinish()) || this.mCameraState == 1) && Util.isTimeout(currentTimeMillis, AutoLockManager.getInstance(this.mActivity).getLastActionTime(), 500)) {
            if (Util.isTimeout(currentTimeMillis, this.mActivity.getSoundPlayTime(), 1000)) {
                if (Util.isTimeout(currentTimeMillis, this.mLastShutterButtonClickTime, 1000)) {
                    z = Util.isTimeout(currentTimeMillis, this.mJpegPictureCallbackTime, 500);
                }
            }
        }
        return z;
    }

    public void requestRender() {
    }

    protected void resetFaceBeautyMode() {
    }

    protected void resetMetaDataManager() {
        if (CameraSettings.isSupportedMetadata()) {
            this.mMetaDataManager.reset();
        }
    }

    public boolean scanQRCodeEnabled() {
        return !Device.IS_D2A ? ((this.mCameraState != 1 && this.mCameraState != 2) || this.mIsImageCaptureIntent || !isBackCamera() || this.mMultiSnapStatus || getUIController().getSettingPage().isItemSelected() || !getUIController().getPreviewPage().isPreviewPageVisible() || getUIController().getModeExitView().isExitButtonShown() || CameraSettings.isSwitchOn("pref_camera_stereo_mode_key") || CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) ? false : true : false;
    }

    public void sendDelayedCaptureMessage(int i, int i2) {
        if (!this.mHandler.hasMessages(20) && getUIController().getPreviewPage().isPreviewPageVisible()) {
            this.mHandler.obtainMessage(20, i2, i).sendToTarget();
        }
    }

    protected void sendOpenFailMessage() {
        this.mHandler.sendEmptyMessage(10);
    }

    protected void setAutoExposure(Parameters parameters, String str) {
        if (this.mFocusManager.getMeteringAreas() == null) {
            CameraSettings.setAutoExposure(sProxy, this.mParameters, str);
        }
    }

    protected void setBeautyParams() {
        if (Device.isSupportedSkinBeautify()) {
            String faceBeautifyValue = CameraSettings.getFaceBeautifyValue();
            if (CameraSettings.isSwitchOn("pref_camera_portrait_mode_key") && getUIController().getFaceView().faceExists() && CameraSettings.isCameraPortraitWithFaceBeauty()) {
                faceBeautifyValue = getString(C0049R.string.pref_face_beauty_default);
            }
            sProxy.setStillBeautify(this.mParameters, faceBeautifyValue);
            Log.i("Camera", "SetStillBeautify =" + sProxy.getStillBeautify(this.mParameters));
            if (CameraSettings.isFaceBeautyOn(faceBeautifyValue)) {
                sProxy.setBeautifySkinColor(this.mParameters, CameraSettings.getBeautifyDetailValue("pref_skin_beautify_skin_color_key"));
                sProxy.setBeautifySlimFace(this.mParameters, CameraSettings.getBeautifyDetailValue("pref_skin_beautify_slim_face_key"));
                sProxy.setBeautifySkinSmooth(this.mParameters, CameraSettings.getBeautifyDetailValue("pref_skin_beautify_skin_smooth_key"));
                sProxy.setBeautifyEnlargeEye(this.mParameters, CameraSettings.getBeautifyDetailValue("pref_skin_beautify_enlarge_eye_key"));
            }
        }
    }

    protected void setCameraParameters(int i) {
        this.mParameters = this.mCameraDevice.getParameters();
        if ((i & 1) != 0) {
            updateCameraParametersInitialize();
        }
        if ((i & 2) != 0) {
            updateCameraParametersPreference();
            this.mSetCameraParameter &= -2;
        }
        this.mCameraDevice.setParameters(this.mParameters);
    }

    protected void setCameraParametersWhenIdle(int i) {
        this.mUpdateSet |= i;
        if (this.mCameraDevice == null) {
            this.mUpdateSet = 0;
            return;
        }
        if (isCameraIdle()) {
            setCameraParameters(this.mUpdateSet);
            checkRestartPreview();
            this.mRestartPreview = false;
            setPreviewFrameLayoutAspectRatio();
            updateSceneModeUI();
            exitWhiteBalanceLockMode();
            getUIController().getSettingsStatusBar().updateStatus();
            this.mUpdateSet = 0;
        } else if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessageDelayed(3, 1000);
        }
    }

    protected void setCameraState(int i) {
        this.mCameraState = i;
        if (currentIsMainThread()) {
            switch (i) {
                case 0:
                case 3:
                case 4:
                    enableCameraControls(false);
                    break;
                case 1:
                    enableCameraControls(true);
                    break;
            }
        }
    }

    protected void setDisplayOrientation() {
        super.setDisplayOrientation();
        getUIController().getFaceView().setDisplayOrientation(this.mCameraDisplayOrientation);
        if (this.mFocusManager != null) {
            this.mFocusManager.setDisplayOrientation(this.mCameraDisplayOrientation);
        }
    }

    public void setFocusParameters() {
        if (this.mCameraState != 3) {
            setCameraParameters(2);
        }
    }

    protected void setManualParameters() {
    }

    protected void setMetaCallback(int i) {
        boolean z = false;
        boolean z2 = i != 0;
        if (this.mSetMetaCallback != z2) {
            if (!this.mSetMetaCallback) {
                z = true;
            }
            this.mSetMetaCallback = z;
            this.mCameraDevice.setMetaDataCallback(this.mSetMetaCallback ? this : null);
        }
        if (this.mSetMetaCallback) {
            this.mMetaDataManager.resetFilter();
            this.mMetaDataManager.setType(i);
        }
        if (!z2 && -1 != this.mMetaDataManager.mCurrentScene) {
            this.mMetaDataManager.resetSceneMode();
        }
    }

    protected void setMetaCallback(boolean z) {
        setMetaCallback(z ? 3 : 0);
    }

    protected void setTimeWatermarkIfNeed() {
        if (CameraSettings.isTimeWaterMarkOpen(this.mPreferences)) {
            sProxy.setTimeWatermarkValue(this.mParameters, Util.getTimeWatermark());
        }
    }

    protected void setupPreview() {
        setupPreview(this.mIsZSLMode);
    }

    protected void setupPreview(boolean z) {
        boolean isLongShotMode = isLongShotMode();
        if (Device.isResetToCCAFAfterCapture()) {
            this.mCameraDevice.cancelAutoFocus();
        }
        if (needSetupPreview(z)) {
            startPreview();
        } else if (this.mSnapshotOnIdle) {
            this.mHandler.post(this.mDoSnapRunnable);
        } else {
            this.mFocusManager.resetAfterCapture(!this.mNeedAutoFocus ? this.mLongPressedAutoFocus : true);
            this.mLongPressedAutoFocus = false;
            this.mFocusManager.setAeAwbLock(false);
            setCameraParameters(-1);
        }
        if (this.mNeedAutoFocus) {
            this.mHandler.sendEmptyMessageDelayed(26, 600);
            this.mNeedAutoFocus = false;
        }
        if (Device.isHDRFreeze() && this.mMutexModePicker.isMorphoHdr()) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(24, 1, 0), 500);
        } else if (isLongShotMode && Device.isQcomPlatform() && getBurstDelayTime() > 0) {
            this.mHandler.sendEmptyMessageDelayed(36, (long) getBurstDelayTime());
        } else {
            setCameraState(1);
            startFaceDetection();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startFaceDetection() {
        /*
        r2 = this;
        r1 = 1;
        r0 = r2.mFaceDetectionEnabled;
        if (r0 == 0) goto L_0x000d;
    L_0x0005:
        r0 = r2.mFaceDetectionStarted;
        if (r0 != 0) goto L_0x000d;
    L_0x0009:
        r0 = r2.mCameraState;
        if (r0 == r1) goto L_0x000e;
    L_0x000d:
        return;
    L_0x000e:
        r0 = r2.mObjectTrackingStarted;
        if (r0 != 0) goto L_0x000d;
    L_0x0012:
        r0 = r2.isFaceBeautyMode();
        if (r0 != 0) goto L_0x000d;
    L_0x0018:
        r0 = r2.getUIController();
        r0 = r0.getObjectView();
        if (r0 == 0) goto L_0x0030;
    L_0x0022:
        r0 = r2.getUIController();
        r0 = r0.getObjectView();
        r0 = r0.isAdjusting();
        if (r0 != 0) goto L_0x000d;
    L_0x0030:
        r0 = r2.mParameters;
        r0 = r0.getMaxNumDetectedFaces();
        if (r0 <= 0) goto L_0x0047;
    L_0x0038:
        r2.mFaceDetectionStarted = r1;
        r0 = r2.mCameraDevice;
        r0.setFaceDetectionListener(r2);
        r0 = r2.mCameraDevice;
        r0.startFaceDetection();
        r2.updateFaceView(r1, r1);
    L_0x0047:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.CameraModule.startFaceDetection():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startObjectTracking() {
        /*
        r5 = this;
        r4 = 1;
        r1 = com.android.camera.Device.isSupportedObjectTrack();
        if (r1 != 0) goto L_0x0008;
    L_0x0007:
        return;
    L_0x0008:
        r1 = "Camera";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "startObjectTracking mObjectTrackingStarted=";
        r2 = r2.append(r3);
        r3 = r5.mObjectTrackingStarted;
        r2 = r2.append(r3);
        r3 = " mCameraState=";
        r2 = r2.append(r3);
        r3 = r5.mCameraState;
        r2 = r2.append(r3);
        r2 = r2.toString();
        android.util.Log.i(r1, r2);
        r1 = r5.mObjectTrackingStarted;
        if (r1 != 0) goto L_0x003a;
    L_0x0035:
        r1 = r5.mCameraState;
        r2 = 3;
        if (r1 != r2) goto L_0x003b;
    L_0x003a:
        return;
    L_0x003b:
        r1 = r5.mPaused;
        if (r1 != 0) goto L_0x003a;
    L_0x003f:
        r1 = r5.mCameraDevice;
        if (r1 == 0) goto L_0x00c6;
    L_0x0043:
        r1 = com.android.camera.Device.isSupportedObjectTrack();
        if (r1 == 0) goto L_0x00c6;
    L_0x0049:
        r5.stopFaceDetection(r4);
        r5.mObjectTrackingStarted = r4;
        r1 = r5.mFocusManager;
        r2 = r5.getUIController();
        r2 = r2.getObjectView();
        r1.setFrameView(r2);
        r1 = sProxy;
        r2 = r5.mParameters;
        r3 = "auto";
        r1.setFocusMode(r2, r3);
        r1 = r5.getUIController();
        r1 = r1.getFlashButton();
        r0 = r1.getValue();
        r1 = "torch";
        r1 = r1.equals(r0);
        if (r1 != 0) goto L_0x0083;
    L_0x007a:
        r1 = "off";
        r1 = r1.equals(r0);
        if (r1 == 0) goto L_0x00c7;
    L_0x0083:
        r1 = r5.mCameraDevice;
        r2 = r5.mParameters;
        r1.setParameters(r2);
        r1 = r5.mCameraDevice;
        r1.setFaceDetectionListener(r5);
        r1 = "Camera";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "startObjectTracking rect=";
        r2 = r2.append(r3);
        r3 = r5.getUIController();
        r3 = r3.getObjectView();
        r3 = r3.getFocusRectInPreviewFrame();
        r2 = r2.append(r3);
        r2 = r2.toString();
        android.util.Log.i(r1, r2);
        r1 = r5.mCameraDevice;
        r2 = r5.getUIController();
        r2 = r2.getObjectView();
        r2 = r2.getFocusRectInPreviewFrame();
        r1.startObjectTrack(r2);
    L_0x00c6:
        return;
    L_0x00c7:
        r1 = r5.getUIController();
        r1 = r1.getFlashButton();
        r2 = "off";
        r1.keepSetValue(r2);
        r1 = "pref_camera_flashmode_key";
        r5.updateASD(r1);
        goto L_0x0083;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.CameraModule.startObjectTracking():void");
    }

    protected void startPreview() {
        if (this.mCameraDevice != null && this.mFocusManager != null) {
            if (currentIsMainThread()) {
                this.mFocusManager.resetTouchFocus();
            }
            this.mCameraDevice.setErrorCallback(this.mErrorCallback);
            if (!(this.mCameraState == 0 || (Device.isMTKPlatform() && isZeroShotMode()))) {
                stopPreview();
            }
            setDisplayOrientation();
            this.mCameraDevice.setDisplayOrientation(this.mCameraDisplayOrientation);
            if (!this.mSnapshotOnIdle) {
                if ("continuous-picture".equals(this.mFocusManager.getFocusMode())) {
                    this.mCameraDevice.cancelAutoFocus();
                }
                this.mFocusManager.setAeAwbLock(false);
            }
            this.mFoundFace = false;
            this.mKeepAdjustedEv = false;
            if (currentIsMainThread()) {
                setCameraParameters(-1);
            }
            this.mCameraDevice.setPreviewTexture(this.mActivity.getCameraScreenNail().getSurfaceTexture());
            Log.v("Camera", "startPreview");
            this.mCameraDevice.startPreview();
            this.mFocusManager.onPreviewStarted();
            if (currentIsMainThread()) {
                setCameraState(1);
            }
            if (this.mSnapshotOnIdle) {
                this.mHandler.post(this.mDoSnapRunnable);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopFaceDetection(boolean r4) {
        /*
        r3 = this;
        r2 = 0;
        r0 = r3.mFaceDetectionEnabled;
        if (r0 == 0) goto L_0x0031;
    L_0x0005:
        r0 = r3.mFaceDetectionStarted;
        if (r0 == 0) goto L_0x0031;
    L_0x0009:
        r0 = r3.mParameters;
        r0 = r0.getMaxNumDetectedFaces();
        if (r0 <= 0) goto L_0x0030;
    L_0x0011:
        r0 = com.android.camera.Device.isMTKPlatform();
        if (r0 == 0) goto L_0x0020;
    L_0x0017:
        r0 = r3.mCameraState;
        r1 = 3;
        if (r0 == r1) goto L_0x0025;
    L_0x001c:
        r0 = r3.mCameraState;
        if (r0 == 0) goto L_0x0025;
    L_0x0020:
        r0 = r3.mCameraDevice;
        r0.stopFaceDetection();
    L_0x0025:
        r3.mFaceDetectionStarted = r2;
        r0 = r3.mCameraDevice;
        r1 = 0;
        r0.setFaceDetectionListener(r1);
        r3.updateFaceView(r2, r4);
    L_0x0030:
        return;
    L_0x0031:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.CameraModule.stopFaceDetection(boolean):void");
    }

    public void stopObjectTracking(boolean z) {
        if (Device.isSupportedObjectTrack()) {
            Log.i("Camera", "stopObjectTracking mObjectTrackingStarted=" + this.mObjectTrackingStarted + " restartFD=" + z);
            if (this.mObjectTrackingStarted) {
                if (this.mCameraDevice != null) {
                    this.mObjectTrackingStarted = false;
                    this.mCameraDevice.setFaceDetectionListener(null);
                    this.mCameraDevice.stopObjectTrack();
                    if (!(getUIController().getObjectView().isAdjusting() || V6GestureRecognizer.getInstance(this.mActivity).getCurrentGesture() == 10)) {
                        getUIController().getFlashButton().updateFlashModeAccordingHdr(getUIController().getHdrButton().getValue());
                        if (!(this.mPaused || this.mMultiSnapStatus || this.mCameraState == 3 || this.mFocusManager.isFocusingSnapOnFinish())) {
                            CameraSettings.setFocusModeSwitching(true);
                            setCameraParameters(2);
                        }
                    }
                    getUIController().getObjectView().clear();
                    getUIController().getObjectView().setVisibility(8);
                }
                if (z) {
                    startFaceDetection();
                }
                return;
            }
            if (!(getUIController().getObjectView() == null || getUIController().getObjectView().getVisibility() == 8)) {
                getUIController().getObjectView().clear();
                getUIController().getObjectView().setVisibility(8);
            }
        }
    }

    protected void trackPictureTaken(int i, boolean z, int i2, int i3, boolean z2) {
        if (this.mMutexModePicker.isMorphoHdr() || this.mMutexModePicker.isSceneHdr()) {
            CameraDataAnalytics.instance().trackEvent("capture_nums_normal_hdr");
        } else if (this.mMutexModePicker.isAoHdr()) {
            CameraDataAnalytics.instance().trackEvent("capture_nums_live_hdr");
        } else if (this.mMutexModePicker.isHandNight()) {
            CameraDataAnalytics.instance().trackEvent("capture_nums_hht");
        } else if (this.mMutexModePicker.isUbiFocus()) {
            CameraDataAnalytics.instance().trackEvent("capture_nums_ubfocus");
        } else {
            Object stillBeautify = sProxy.getStillBeautify(this.mParameters);
            String string = getString(C0049R.string.pref_face_beauty_close);
            if (!TextUtils.isEmpty(stillBeautify) && !stillBeautify.equals(string)) {
                CameraDataAnalytics.instance().trackEvent("capture_nums_beauty");
            } else if (CameraSettings.isSwitchOn("pref_camera_manual_mode_key")) {
                CameraDataAnalytics.instance().trackEvent("capture_nums_manual");
            } else if (CameraSettings.isSwitchOn("pref_camera_gradienter_key")) {
                CameraDataAnalytics.instance().trackEvent("capture_nums_gradienter");
            } else if (CameraSettings.isSwitchOn("pref_camera_tilt_shift_mode")) {
                int shaderEffect = CameraSettings.getShaderEffect();
                if (shaderEffect == EffectController.sGaussianIndex) {
                    CameraDataAnalytics.instance().trackEvent("capture_nums_tilt_shift_circle");
                } else if (shaderEffect == EffectController.sTiltShiftIndex) {
                    CameraDataAnalytics.instance().trackEvent("capture_nums_tilt_shift_parallel");
                }
            }
        }
        if (EffectController.getInstance().isEffectPageSelected()) {
            CameraDataAnalytics.instance().trackEvent(EffectController.getInstance().getAnalyticsKey());
        }
        if (z && CameraSettings.isPressDownCapture() && i > 1) {
            i--;
        }
        if (CameraSettings.isTimeWaterMarkOpen(this.mPreferences)) {
            CameraDataAnalytics.instance().trackEvent("time_watermark_taken_key");
        } else if (CameraSettings.isDualCameraWaterMarkOpen(this.mPreferences)) {
            CameraDataAnalytics.instance().trackEvent("dual_watermark_taken_key");
        }
        if (sProxy.isFaceWatermarkOn(this.mParameters) && getUIController().getFaceView().faceExists()) {
            CameraDataAnalytics.instance().trackEvent("faceinfo_watermark_taken_key");
        }
        super.trackPictureTaken(i, z, i2, i3, z2);
    }

    public void tryRemoveCountDownMessage() {
        this.mHandler.removeMessages(20);
        this.mAudioCaptureManager.hideDelayNumber();
        this.mIsCountDown = false;
    }

    protected void updateASD(String str) {
        if (Device.isSupportedASD()) {
            this.mMetaDataManager.setAsdDetectMask(str);
        }
    }

    protected void updateCameraParametersInitialize() {
        Collection supportedPreviewFrameRates = this.mParameters.getSupportedPreviewFrameRates();
        if (supportedPreviewFrameRates != null) {
            this.mParameters.setPreviewFrameRate(((Integer) Collections.max(supportedPreviewFrameRates)).intValue());
        }
        this.mParameters.setRecordingHint(false);
        if ("true".equals(this.mParameters.get("video-stabilization-supported"))) {
            this.mParameters.set("video-stabilization", "false");
        }
    }

    protected void updateCameraParametersPreference() {
        if (this.mAeLockSupported) {
            this.mParameters.setAutoExposureLock(this.mFocusManager.getAeAwbLock());
        }
        if (this.mAwbLockSupported) {
            this.mParameters.setAutoWhiteBalanceLock(this.mFocusManager.getAeAwbLock());
        }
        PictureSize bestPictureSize = getBestPictureSize();
        if (bestPictureSize != null) {
            Log.d("Camera", "pictureSize = " + bestPictureSize);
            Size pictureSize = this.mParameters.getPictureSize();
            if (!(pictureSize.width == bestPictureSize.width && pictureSize.height == bestPictureSize.height)) {
                stopObjectTracking(true);
            }
            this.mParameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
        } else {
            Log.e("Camera", "get null pictureSize");
            bestPictureSize = PictureSizeManager.getPictureSize(false);
        }
        Size optimalPreviewSize = Util.getOptimalPreviewSize(this.mActivity, sProxy.getSupportedPreviewSizes(this.mParameters), (double) CameraSettings.getPreviewAspectRatio(bestPictureSize.width, bestPictureSize.height));
        Size previewSize = this.mParameters.getPreviewSize();
        int uIStyleByPreview = CameraSettings.getUIStyleByPreview(optimalPreviewSize.width, optimalPreviewSize.height);
        if (!previewSize.equals(optimalPreviewSize)) {
            this.mParameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
        }
        if (this.mUIStyle != uIStyleByPreview) {
            this.mUIStyle = uIStyleByPreview;
            if (!this.mSwitchingCamera || Device.isMDPRender()) {
                this.mHandler.sendEmptyMessage(18);
            } else {
                this.mHasPendingSwitching = true;
            }
        }
        this.mPreviewWidth = optimalPreviewSize.width;
        this.mPreviewHeight = optimalPreviewSize.height;
        if (21 <= VERSION.SDK_INT) {
            optimalPreviewSize = Util.getOptimalJpegThumbnailSize(this.mParameters.getSupportedJpegThumbnailSizes(), ((double) bestPictureSize.width) / ((double) bestPictureSize.height));
            if (!this.mParameters.getJpegThumbnailSize().equals(optimalPreviewSize)) {
                this.mParameters.setJpegThumbnailSize(optimalPreviewSize.width, optimalPreviewSize.height);
            }
            Log.v("Camera", "Thumbnail size is " + optimalPreviewSize.width + "x" + optimalPreviewSize.height);
        }
        if (this.mMutexModePicker.isSceneHdr()) {
            this.mSceneMode = "hdr";
            if (!("auto".equals(this.mParameters.getSceneMode()) || "hdr".equals(this.mParameters.getSceneMode()))) {
                this.mParameters.setSceneMode("auto");
                this.mCameraDevice.setParameters(this.mParameters);
                this.mParameters = this.mCameraDevice.getParameters();
            }
        } else if (CameraSettings.isSwitchOn("pref_camera_scenemode_setting_key")) {
            this.mSceneMode = this.mPreferences.getString("pref_camera_scenemode_key", getString(C0049R.string.pref_camera_scenemode_default));
        } else {
            this.mSceneMode = "auto";
        }
        Log.v("Camera", "mSceneMode " + this.mSceneMode + " getMutexMode=" + this.mMutexModePicker.getMutexMode());
        if (!BaseModule.isSupported(this.mSceneMode, this.mParameters.getSupportedSceneModes())) {
            this.mSceneMode = this.mParameters.getSceneMode();
            if (this.mSceneMode == null) {
                this.mSceneMode = "auto";
            }
        } else if (!this.mParameters.getSceneMode().equals(this.mSceneMode)) {
            Log.v("Camera", "mSceneMode " + this.mSceneMode + " pas=" + this.mParameters.getSceneMode());
            this.mParameters.setSceneMode(this.mSceneMode);
            this.mCameraDevice.setParameters(this.mParameters);
            this.mParameters = this.mCameraDevice.getParameters();
        }
        String jpegQuality = CameraSettings.getJpegQuality(this.mPreferences, this.mMultiSnapStatus);
        Log.i("Camera", "jpegQuality : " + jpegQuality);
        this.mParameters.setJpegQuality(JpegEncodingQualityMappings.getQualityNumber(jpegQuality));
        int readExposure = CameraSettings.readExposure(this.mPreferences);
        Log.i("Camera", "EV : " + readExposure);
        int maxExposureCompensation = this.mParameters.getMaxExposureCompensation();
        if (readExposure < this.mParameters.getMinExposureCompensation() || readExposure > maxExposureCompensation) {
            Log.w("Camera", "invalid exposure range: " + readExposure);
        } else {
            this.mParameters.setExposureCompensation(readExposure);
        }
        if (Device.isSupportedShaderEffect()) {
            int shaderEffect = CameraSettings.getShaderEffect();
            Log.v("Camera", "Shader color effect value =" + shaderEffect);
            EffectController.getInstance().setEffect(shaderEffect);
            if (EffectController.getInstance().hasEffect() && this.mEffectProcessor == null) {
                this.mEffectProcessor = new SnapshotEffectRender(this.mActivity, this.mIsImageCaptureIntent);
                this.mEffectProcessor.setImageSaver(this.mActivity.getImageSaver());
            }
            if (this.mEffectProcessor != null) {
                this.mEffectProcessor.prepareEffectRender(shaderEffect);
                this.mEffectProcessor.setQuality(this.mParameters.getJpegQuality());
            }
        } else {
            String string = this.mPreferences.getString("pref_camera_coloreffect_key", getString(C0049R.string.pref_camera_coloreffect_default));
            Log.v("Camera", "Color effect value =" + string);
            if (BaseModule.isSupported(string, this.mParameters.getSupportedColorEffects())) {
                this.mParameters.setColorEffect(string);
            }
        }
        String string2 = this.mPreferences.getString("pref_camera_autoexposure_key", getString(C0049R.string.pref_camera_autoexposure_default));
        Log.v("Camera", "autoExposure value =" + string2);
        setAutoExposure(this.mParameters, string2);
        String string3 = this.mPreferences.getString("pref_camera_antibanding_key", getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_camera_antibanding_default)));
        Log.v("Camera", "antiBanding value =" + string3);
        if (BaseModule.isSupported(string3, this.mParameters.getSupportedAntibanding())) {
            this.mParameters.setAntibanding(string3);
        }
        String requestFlashMode;
        if ("auto".equals(this.mSceneMode) || "hdr".equals(this.mSceneMode)) {
            if (!this.m3ALocked) {
                this.mFocusManager.overrideFocusMode(null);
            }
            List supportedFlashModes = this.mParameters.getSupportedFlashModes();
            if (supportedFlashModes != null && supportedFlashModes.size() > 0) {
                String flashMode = this.mParameters.getFlashMode();
                requestFlashMode = getRequestFlashMode();
                if (BaseModule.isSupported(requestFlashMode, supportedFlashModes)) {
                    this.mParameters.setFlashMode(requestFlashMode);
                }
                if (this.mMutexModePicker.isHdr() && !"off".equals(flashMode) && !"torch".equals(flashMode) && BaseModule.isSupported("off", supportedFlashModes)) {
                    this.mParameters.setFlashMode("off");
                    if (this.mMutexModePicker.isAoHdr()) {
                        this.mCameraDevice.setParameters(this.mParameters);
                        this.mParameters = this.mCameraDevice.getParameters();
                    }
                }
            }
            if (CameraSettings.isFocusModeSwitching()) {
                CameraSettings.setFocusModeSwitching(false);
                if (this.mCameraStartUpThread == null) {
                    this.mFocusManager.resetFocusStateIfNeeded();
                }
            }
            String focusMode = ((this.mMultiSnapStatus || this.mObjectTrackingStarted) && this.mFocusAreaSupported) ? "auto" : this.mFocusManager.getFocusMode();
            if (focusMode != null) {
                if (!Device.isQcomPlatform() || this.mCameraState != 0 || !"manual".equals(focusMode)) {
                    sProxy.setFocusMode(this.mParameters, focusMode);
                }
                if ("macro".equals(focusMode) || "manual".equals(focusMode)) {
                    stopObjectTracking(true);
                }
            }
            Log.i("Camera", "Focus mode value = " + focusMode);
            String manualValue = getManualValue("pref_camera_whitebalance_key", getString(C0049R.string.pref_camera_whitebalance_default));
            if (BaseModule.isSupported(manualValue, this.mParameters.getSupportedWhiteBalance())) {
                sProxy.setWhiteBalance(this.mParameters, manualValue);
            } else if (this.mParameters.getWhiteBalance() == null) {
                manualValue = "auto";
            }
        } else {
            requestFlashMode = getRequestFlashMode();
            if (BaseModule.isSupported(requestFlashMode, this.mParameters.getSupportedFlashModes())) {
                this.mParameters.setFlashMode(requestFlashMode);
            }
            if (CameraSettings.isFocusModeSwitching() && isBackCamera()) {
                CameraSettings.setFocusModeSwitching(false);
                if (this.mCameraStartUpThread == null) {
                    this.mFocusManager.resetFocusStateIfNeeded();
                }
            }
            sProxy.setFocusMode(this.mParameters, "continuous-picture");
            this.mFocusManager.overrideFocusMode("continuous-picture");
        }
        if (this.mFocusAreaSupported) {
            sProxy.setFocusAreas(this.mParameters, this.mFocusManager.getFocusAreas());
        }
        if (this.mMeteringAreaSupported) {
            sProxy.setMeteringAreas(this.mParameters, this.mFocusManager.getMeteringAreas());
        }
        if (this.mContinuousFocusSupported) {
            if (this.mParameters.getFocusMode().equals("continuous-picture")) {
                this.mCameraDevice.setAutoFocusMoveCallback(this.mAutoFocusMoveCallback);
            } else {
                this.mCameraDevice.setAutoFocusMoveCallback(null);
            }
        }
        boolean z = true;
        if (this.mMultiSnapStatus || this.mMutexModePicker.isUbiFocus() || CameraSettings.isSwitchOn("pref_camera_gradienter_key") || CameraSettings.isSwitchOn("pref_camera_tilt_shift_mode")) {
            z = false;
        } else if (!(CameraSettings.isSwitchOn("pref_camera_magic_mirror_key") || CameraSettings.isSwitchOn("pref_camera_portrait_mode_key") || CameraSettings.isSwitchOn("pref_camera_groupshot_mode_key"))) {
            z = this.mPreferences.getBoolean("pref_camera_facedetection_key", getResources().getBoolean(CameraSettings.getDefaultPreferenceId(C0049R.bool.pref_camera_facedetection_default)));
        }
        getUIController().getFaceView().setSkipDraw(!z);
        if (z) {
            if (!this.mFaceDetectionEnabled) {
                this.mFaceDetectionEnabled = true;
                startFaceDetection();
            }
        } else if (this.mFaceDetectionEnabled) {
            stopFaceDetection(true);
            this.mFaceDetectionEnabled = false;
        }
        if (this.mParameters.isZoomSupported()) {
            this.mParameters.setZoom(getZoomValue());
        }
        this.mActivity.getSensorStateManager().setFocusSensorEnabled(this.mFocusManager.getMeteringAreas() != null);
        QRCodeManager.instance(this.mActivity).needScanQRCode(CameraSettings.isScanQRCode(this.mPreferences));
        QRCodeManager.instance(this.mActivity).setTransposePreviewSize(this.mPreviewWidth, this.mPreviewHeight);
        QRCodeManager.instance(this.mActivity).setPreviewFormat(this.mParameters.getPreviewFormat());
        addMuteToParameters(this.mParameters);
        if ((!CameraSettings.isSupportedOpticalZoom() || this.mParameters.getZoom() <= 0 || CameraSettings.isSwitchOn("pref_camera_manual_mode_key")) && !CameraSettings.isSwitchOn("pref_camera_portrait_mode_key")) {
            configOisParameters(this.mParameters, true);
        } else {
            configOisParameters(this.mParameters, false);
        }
        addT2TParameters(this.mParameters);
        if (!this.mSwitchingCamera || CameraSettings.sCroppedIfNeeded) {
            updateCameraScreenNailSize(this.mPreviewWidth, this.mPreviewHeight, this.mFocusManager);
        }
        if (Device.isFaceDetectNeedRotation()) {
            this.mParameters.setRotation(Util.getJpegRotation(this.mCameraId, this.mOrientation));
        }
    }

    protected void updateExitButton(boolean z) {
        boolean isDetectedHHT = !this.mMutexModePicker.isHdr() ? this.mMutexModePicker.isHandNight() ? isDetectedHHT() : false : true;
        if (!isDetectedHHT) {
            getUIController().getModeExitView().updateExitButton(-1, z);
        }
    }

    protected void updateFaceView(boolean z, boolean z2) {
        int i = 1;
        if (this.mHandler.hasMessages(35)) {
            this.mHandler.removeMessages(35);
        }
        Handler handler = this.mHandler;
        int i2 = z ? 1 : 0;
        if (!z2) {
            i = 0;
        }
        handler.obtainMessage(35, i2, i).sendToTarget();
    }
}
