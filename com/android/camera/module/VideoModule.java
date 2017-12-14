package com.android.camera.module;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore.Video.Media;
import android.support.v7.recyclerview.C0049R;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraHolder;
import com.android.camera.CameraPreferenceActivity;
import com.android.camera.CameraSettings;
import com.android.camera.ChangeManager;
import com.android.camera.Device;
import com.android.camera.Exif;
import com.android.camera.FocusManagerSimple;
import com.android.camera.LocationManager;
import com.android.camera.OnClickAttr;
import com.android.camera.SensorStateManager.SensorStateListener;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.aosp_porting.ReflectUtil;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy.CameraHardwareFace;
import com.android.camera.permission.PermissionManager;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.Storage;
import com.android.camera.ui.GridSettingTextPopup;
import com.android.camera.ui.ObjectView.ObjectViewListener;
import com.android.camera.ui.PopupManager;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.V6AbstractSettingPopup;
import com.android.camera.ui.V6GestureRecognizer;
import com.android.camera.ui.V6ModulePicker;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class VideoModule extends BaseModule implements OnErrorListener, OnInfoListener, AutoFocusCallback, AutoFocusMoveCallback, ObjectViewListener, FaceDetectionListener {
    private static boolean HOLD_WHEN_SAVING_VIDEO = false;
    public static final long VIDEO_MIN_SINGLE_FILE_SIZE = Math.min(8388608, 52428800);
    protected static final HashMap<Integer, Integer> VIDEO_QUALITY_TO_HIGHSPEED = new HashMap();
    private AudioManager mAudioManager;
    private String mBaseFileName;
    private boolean mCaptureTimeLapse;
    private boolean mContinuousFocusSupported;
    private volatile int mCurrentFileNumber;
    private int mCurrentShowIndicator = -1;
    private String mCurrentVideoFilename;
    private Uri mCurrentVideoUri;
    private ContentValues mCurrentVideoValues;
    private int mDesiredPreviewHeight;
    private int mDesiredPreviewWidth;
    private boolean mFocusAreaSupported;
    private FocusManagerSimple mFocusManager;
    private long mFocusStartTime;
    protected final Handler mHandler = new MainHandler();
    protected String mHfr = "normal";
    private boolean mInStartingFocusRecording = false;
    private boolean mIsFromStop;
    private boolean mIsTouchFocused;
    protected boolean mIsVideoCaptureIntent;
    private long mLastBackPressedTime = 0;
    private AsyncTask<Void, Void, Void> mLoadThumbnailTask;
    protected int mMaxVideoDurationInMs;
    protected MediaRecorder mMediaRecorder;
    private volatile boolean mMediaRecorderRecording = false;
    private boolean mMediaRecorderRecordingPaused = false;
    private boolean mMeteringAreaSupported;
    private long mOnResumeTime;
    private int mOrientationCompensationAtRecordStart;
    private int mOriginalMusicVolume;
    private long mPauseClickTime = 0;
    private final PhoneStateListener mPhoneStateListener = new C01121();
    private boolean mPreviewing;
    protected CamcorderProfile mProfile;
    protected int mQuality = 5;
    private boolean mQuickCapture;
    private BroadcastReceiver mReceiver = null;
    public volatile boolean mRecorderBusy = false;
    private long mRecordingStartTime;
    private String mRecordingTime;
    private boolean mRecordingTimeCountsDown = false;
    private boolean mRecordingUIShown;
    protected boolean mRestartPreview;
    private Runnable mRestoreRunnable = new C01143();
    private boolean mSavingResult = false;
    private SensorStateListener mSensorStateListener = new C01132();
    private boolean mSnapshotInProgress = false;
    private StereoSwitchThread mStereoSwitchThread;
    private boolean mSwitchingCamera;
    private final Object mTaskLock = new Object();
    TelephonyManager mTelephonyManager;
    private int mTimeBetweenTimeLapseFrameCaptureMs = 0;
    private long mTouchFocusStartingTime = 0;
    private ParcelFileDescriptor mVideoFileDescriptor;
    private String mVideoFilename;
    private String mVideoFocusMode;
    protected int mVideoHeight;
    private long mVideoRecordedDuration;
    private SavingTask mVideoSavingTask;
    protected int mVideoWidth;

    class C01121 extends PhoneStateListener {
        C01121() {
        }

        public void onCallStateChanged(int i, String str) {
            if (i == 2 && VideoModule.this.mMediaRecorderRecording) {
                Log.i("videocamera", "CALL_STATE_OFFHOOK, so we call onstop here to stop recording");
                VideoModule.this.onStop();
            }
            super.onCallStateChanged(i, str);
        }
    }

    class C01132 implements SensorStateListener {
        C01132() {
        }

        public boolean isWorking() {
            return VideoModule.this.mPreviewing;
        }

        public void notifyDevicePostureChanged() {
            VideoModule.this.getUIController().getEdgeShutterView().onDevicePostureChanged();
        }

        public void onDeviceBecomeStable() {
        }

        public void onDeviceBeginMoving() {
        }

        public void onDeviceKeepMoving(double d) {
            if (!VideoModule.this.getUIController().getFocusView().isEvAdjustedTime() && !VideoModule.this.mPaused && Util.isTimeout(System.currentTimeMillis(), VideoModule.this.mTouchFocusStartingTime, 2000)) {
                VideoModule.this.mIsTouchFocused = false;
                if (VideoModule.this.mFocusManager != null) {
                    VideoModule.this.mFocusManager.onDeviceKeepMoving();
                    if (VideoModule.this.mFocusManager.isNeedCancelAutoFocus()) {
                        VideoModule.this.cancelAutoFocus();
                        VideoModule.this.getUIController().getFocusView().clear();
                    }
                }
            }
        }

        public void onDeviceOrientationChanged(float f, boolean z) {
        }
    }

    class C01143 implements Runnable {
        C01143() {
        }

        public void run() {
            Log.i("videocamera", "mRestoreRunnable start");
            VideoModule.this.mAudioManager.abandonAudioFocus(null);
            VideoModule.this.restoreMusicSound();
            if (!VideoModule.this.mIsVideoCaptureIntent) {
                VideoModule.this.enableCameraControls(true);
            }
            VideoModule.this.keepScreenOnAwhile();
            if (VideoModule.this.mIsVideoCaptureIntent && !VideoModule.this.mPaused) {
                if (VideoModule.this.mQuickCapture) {
                    VideoModule.this.doReturnToCaller(VideoModule.this.mSavingResult);
                } else if (VideoModule.this.mSavingResult) {
                    VideoModule.this.showAlert();
                }
            }
            VideoModule.this.mActivity.getScreenHint().updateHint();
            VideoModule.this.animateSlide();
            if (VideoModule.this.mRecordingUIShown) {
                VideoModule.this.showRecordingUI(false);
            }
            VideoModule.this.updateLoadUI(false);
            VideoModule.this.onStopRecording();
            VideoModule.this.mRecorderBusy = false;
        }
    }

    class C01154 implements Runnable {
        C01154() {
        }

        public void run() {
            VideoModule.this.startPreview();
        }
    }

    class C01176 implements Runnable {
        C01176() {
        }

        public void run() {
            VideoModule.this.getUIController().getVideoRecordingTimeView().animateIn(null, 150, true);
        }
    }

    class C01198 implements Runnable {
        C01198() {
        }

        public void run() {
            if (!VideoModule.this.mIsVideoCaptureIntent || VideoModule.this.mPaused) {
                VideoModule.this.getUIController().getVideoRecordingTimeView().setText("");
            }
            if (VideoModule.this.getUIController().getSettingsStatusBar().isSubViewShown()) {
                VideoModule.this.getUIController().getSettingsStatusBar().animateIn(null);
            }
        }
    }

    class C01209 implements Runnable {
        C01209() {
        }

        public void run() {
            VideoModule.this.getUIController().getThumbnailButton().animateIn(null, 100, true);
            if (!VideoModule.this.mIsVideoCaptureIntent || VideoModule.this.mPaused) {
                VideoModule.this.getUIController().getModulePicker().animateIn(null, 100, true);
            } else {
                VideoModule.this.getUIController().getReviewCanceledView().animateIn(null, 100, true);
            }
            VideoModule.this.getUIController().getStereoButton().updateVisible();
        }
    }

    private final class JpegPictureCallback implements PictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location location) {
            this.mLocation = location;
        }

        public void onPictureTaken(byte[] bArr, Camera camera) {
            Log.v("videocamera", "onPictureTaken");
            VideoModule.this.mSnapshotInProgress = false;
            if (!VideoModule.this.mPaused) {
                VideoModule.this.storeImage(bArr, this.mLocation);
                VideoModule.this.getUIController().getShutterButton().enableControls(true);
            }
        }
    }

    private class LoadThumbnailTask extends AsyncTask<Void, Void, Void> {
        Thumbnail mThumbnail;
        Uri mUri;
        String mVideoPath;

        public LoadThumbnailTask() {
            this.mUri = VideoModule.this.mCurrentVideoUri;
            this.mVideoPath = VideoModule.this.mCurrentVideoFilename;
        }

        private void updateThumbnail() {
            Log.e("videocamera", "LoadThumbnailTask updateThumbnail mThumbnail=" + this.mThumbnail + " mPaused=" + VideoModule.this.mPaused);
            if (this.mThumbnail != null || VideoModule.this.mPaused) {
                VideoModule.this.mActivity.getThumbnailUpdater().setThumbnail(this.mThumbnail, !VideoModule.this.mPaused);
            }
        }

        protected Void doInBackground(Void... voidArr) {
            if (this.mUri != null) {
                Bitmap createVideoThumbnailBitmap = Thumbnail.createVideoThumbnailBitmap(this.mVideoPath, 512);
                if (!(isCancelled() || createVideoThumbnailBitmap == null)) {
                    this.mThumbnail = Thumbnail.createThumbnail(this.mUri, createVideoThumbnailBitmap, 0, false);
                }
            }
            return null;
        }

        protected void onCancelled() {
            Log.e("videocamera", "LoadThumbnailTask onCancelled");
            updateThumbnail();
        }

        protected void onPostExecute(Void voidR) {
            if (VideoModule.this.mPaused || !isCancelled()) {
                updateThumbnail();
            }
        }
    }

    private class MainHandler extends Handler {

        class C01211 implements Runnable {
            C01211() {
            }

            public void run() {
                if (VideoModule.this.getUIController().getSettingsStatusBar().getVisibility() == 8) {
                    if (VideoModule.this.getUIController().getVideoRecordingTimeView().getVisibility() != 0) {
                        VideoModule.this.getUIController().getVideoRecordingTimeView().animateIn(null, 150, true);
                    }
                    if (VideoModule.this.mMediaRecorderRecordingPaused) {
                        VideoModule.this.mCurrentShowIndicator = 0;
                    }
                }
            }
        }

        private MainHandler() {
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    if (!(Util.getDisplayRotation(VideoModule.this.mActivity) == VideoModule.this.mDisplayRotation || VideoModule.this.mMediaRecorderRecording || VideoModule.this.mSwitchingCamera)) {
                        VideoModule.this.startPreview();
                    }
                    if (SystemClock.uptimeMillis() - VideoModule.this.mOnResumeTime < 5000) {
                        VideoModule.this.mHandler.sendEmptyMessageDelayed(1, 100);
                        return;
                    }
                    return;
                case 2:
                    VideoModule.this.getWindow().clearFlags(128);
                    return;
                case 3:
                    VideoModule.this.updateRecordingTime();
                    return;
                case 4:
                    VideoModule.this.getUIController().getShutterButton().enableControls(true);
                    return;
                case 5:
                    VideoModule.this.switchCamera();
                    return;
                case 6:
                    VideoModule.this.mActivity.getCameraScreenNail().animateSwitchCameraBefore();
                    return;
                case 10:
                    VideoModule.this.mHandler.removeMessages(10);
                    VideoModule.this.mHandler.removeMessages(2);
                    VideoModule.this.getWindow().addFlags(128);
                    VideoModule.this.mHandler.sendEmptyMessageDelayed(2, (long) VideoModule.this.getScreenDelay());
                    return;
                case 11:
                    CameraSettings.changeUIByPreviewSize(VideoModule.this.mActivity, VideoModule.this.mUIStyle, VideoModule.this.mDesiredPreviewWidth, VideoModule.this.mDesiredPreviewHeight);
                    VideoModule.this.changePreviewSurfaceSize();
                    return;
                case 12:
                    if (VideoModule.this.mHasPendingSwitching) {
                        CameraSettings.changeUIByPreviewSize(VideoModule.this.mActivity, VideoModule.this.mUIStyle, VideoModule.this.mDesiredPreviewWidth, VideoModule.this.mDesiredPreviewHeight);
                        VideoModule.this.changePreviewSurfaceSize();
                        VideoModule.this.mHasPendingSwitching = false;
                    }
                    VideoModule.this.updateCameraScreenNailSize(VideoModule.this.mDesiredPreviewWidth, VideoModule.this.mDesiredPreviewHeight, VideoModule.this.mFocusManager);
                    VideoModule.this.mActivity.getCameraScreenNail().switchCameraDone();
                    VideoModule.this.mSwitchingCamera = false;
                    return;
                case 13:
                    VideoModule.this.getUIController().getCaptureProgressBar().setVisibility(0);
                    return;
                case 14:
                    if (Device.isMDPRender() && VideoModule.this.getUIController().getSurfaceViewFrame().isSurfaceViewVisible()) {
                        VideoModule.this.getUIController().getGLView().setVisibility(8);
                    }
                    if (VideoModule.this.getUIController().getSettingPage().getVisibility() != 0) {
                        VideoModule.this.mActivity.setBlurFlag(false);
                    }
                    if (!VideoModule.this.mIsVideoCaptureIntent || VideoModule.this.getUIController().getReviewDoneView().getVisibility() != 0) {
                        VideoModule.this.ignoreTouchEvent(false);
                        return;
                    }
                    return;
                case 15:
                    VideoModule.this.showStoppingUI();
                    return;
                case 16:
                    VideoModule.this.enableCameraControls(true);
                    return;
                case 17:
                    if (VideoModule.this.mPaused || !VideoModule.this.mMediaRecorderRecording || !hasMessages(3)) {
                        return;
                    }
                    if (V6GestureRecognizer.getInstance(VideoModule.this.mActivity).isGestureDetecting()) {
                        Runnable c01211 = new C01211();
                        if (VideoModule.this.getUIController().getSettingsStatusBar().getVisibility() == 0) {
                            VideoModule.this.getUIController().getSettingsStatusBar().animateOut(c01211);
                            return;
                        } else {
                            c01211.run();
                            return;
                        }
                    }
                    sendEmptyMessageDelayed(17, 1000);
                    return;
                case 18:
                    VideoModule.this.onCameraException();
                    VideoModule.this.onStopVideoRecording(VideoModule.this.mPaused);
                    if (VideoModule.this.mPaused) {
                        VideoModule.this.closeCamera();
                        return;
                    }
                    return;
                case 19:
                    VideoModule.this.mIgnoreFocusChanged = true;
                    VideoModule.this.mActivity.getScreenHint().showFirstUseHint();
                    return;
                case 20:
                    VideoModule.this.onPreviewStart();
                    VideoModule.this.mStereoSwitchThread = null;
                    return;
                case 21:
                    if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                        VideoModule.this.getUIController().getWarningMessageView().setText(C0049R.string.dual_camera_use_hint);
                        VideoModule.this.getUIController().getWarningMessageParent().setVisibility(0);
                        VideoModule.this.mHandler.sendEmptyMessageDelayed(22, 5000);
                        return;
                    }
                    return;
                case 22:
                    VideoModule.this.getUIController().getWarningMessageParent().setVisibility(8);
                    return;
                case 23:
                    VideoModule.this.restoreMusicSound();
                    return;
                case 24:
                    VideoModule.this.autoFocus(VideoModule.this.getUIController().getPreviewFrame().getWidth() / 2, VideoModule.this.getUIController().getPreviewFrame().getHeight() / 2, VideoModule.this.mFocusManager.getDefaultFocusAreaWidth(), VideoModule.this.mFocusManager.getDefaultFocusAreaHeight(), 0);
                    return;
                default:
                    Log.v("videocamera", "Unhandled message: " + message.what);
                    return;
            }
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (Storage.isRelatedStorage(intent.getData())) {
                String action = intent.getAction();
                Log.v("videocamera", "onReceive: action=" + action);
                if (action.equals("android.intent.action.MEDIA_EJECT")) {
                    if (Storage.isCurrentStorageIsSecondary()) {
                        Storage.switchToPhoneStorage();
                        VideoModule.this.stopVideoRecordingAsync();
                    }
                } else if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                    VideoModule.this.mActivity.getScreenHint().updateHint();
                    VideoModule.this.mActivity.getThumbnailUpdater().getLastThumbnail();
                } else if (action.equals("android.intent.action.MEDIA_UNMOUNTED")) {
                    VideoModule.this.mActivity.getScreenHint().updateHint();
                } else if (!action.equals("android.intent.action.MEDIA_SCANNER_STARTED") && action.equals("android.intent.action.MEDIA_SCANNER_FINISHED")) {
                    VideoModule.this.mActivity.getScreenHint().updateHint();
                }
            }
        }
    }

    class SavingTask extends Thread {
        private boolean mRestart;

        private SavingTask(boolean z) {
            this.mRestart = false;
            this.mRestart = z;
        }

        public void run() {
            Log.v("videocamera", "SavingTask run mMediaRecorderRecording = " + VideoModule.this.mMediaRecorderRecording);
            VideoModule.this.mSavingResult = false;
            if (VideoModule.this.mMediaRecorderRecording) {
                ContentValues contentValues = null;
                if (VideoModule.this.subStopRecording()) {
                    VideoModule videoModule;
                    if (this.mRestart) {
                        contentValues = new ContentValues(VideoModule.this.mCurrentVideoValues);
                        videoModule = VideoModule.this;
                        videoModule.mCurrentFileNumber = videoModule.mCurrentFileNumber + 1;
                        Storage.switchStoragePathIfNeeded();
                        this.mRestart = !Storage.isLowStorageSpace(Storage.DIRECTORY) ? VideoModule.this.startRecordVideo() : false;
                    }
                    if (!this.mRestart) {
                        if (!VideoModule.this.mPaused) {
                            VideoModule.this.playCameraSound(3);
                        }
                        contentValues = VideoModule.this.mCurrentVideoValues;
                        if (VideoModule.this.mCurrentFileNumber > 0) {
                            videoModule = VideoModule.this;
                            videoModule.mCurrentFileNumber = videoModule.mCurrentFileNumber + 1;
                        }
                    }
                    if (!VideoModule.this.addVideoToMediaStore(contentValues)) {
                        VideoModule.this.mSavingResult = true;
                    }
                }
                Object -get19;
                if (this.mRestart) {
                    VideoModule.this.mRecorderBusy = false;
                    -get19 = VideoModule.this.mTaskLock;
                    synchronized (-get19) {
                        VideoModule.this.mTaskLock.notifyAll();
                        VideoModule.this.mVideoSavingTask = null;
                    }
                } else {
                    VideoModule.this.mCurrentVideoValues = null;
                    VideoModule.this.mActivity.sendBroadcast(new Intent("com.android.camera.action.stop_video_recording"));
                    if (!VideoModule.this.mIsVideoCaptureIntent && VideoModule.this.mSavingResult) {
                        if (VideoModule.this.mPaused) {
                            VideoModule.this.mActivity.getThumbnailUpdater().setThumbnail(null, !VideoModule.this.mPaused);
                        } else {
                            if (VideoModule.this.mLoadThumbnailTask != null) {
                                VideoModule.this.mLoadThumbnailTask.cancel(true);
                            }
                            VideoModule.this.mLoadThumbnailTask = new LoadThumbnailTask().execute(new Void[0]);
                        }
                    }
                    VideoModule.this.mTelephonyManager.listen(VideoModule.this.mPhoneStateListener, 0);
                    Log.v("videocamera", "listen none");
                    -get19 = VideoModule.this.mTaskLock;
                    synchronized (-get19) {
                        VideoModule.this.mTaskLock.notifyAll();
                        VideoModule.this.mHandler.removeCallbacks(VideoModule.this.mRestoreRunnable);
                        VideoModule.this.mHandler.postAtFrontOfQueue(VideoModule.this.mRestoreRunnable);
                        VideoModule.this.mMediaRecorderRecording = false;
                        Log.w("videocamera", "stop recording at SavingTask, space = " + Storage.getLeftSpace());
                        VideoModule.this.mVideoSavingTask = null;
                    }
                }
                return;
            }
            VideoModule.this.mVideoSavingTask = null;
        }
    }

    protected class StereoSwitchThread extends Thread {
        private volatile boolean mCancelled;

        protected StereoSwitchThread() {
        }

        public void cancel() {
            this.mCancelled = true;
        }

        public void run() {
            VideoModule.this.closeCamera();
            if (!this.mCancelled) {
                VideoModule.this.openCamera();
                if (VideoModule.this.hasCameraException()) {
                    VideoModule.this.onCameraException();
                } else if (!this.mCancelled) {
                    CameraSettings.resetZoom(VideoModule.this.mPreferences);
                    CameraSettings.resetExposure();
                    VideoModule.this.onCameraOpen();
                    VideoModule.this.readVideoPreferences();
                    VideoModule.this.resizeForPreviewAspectRatio();
                    if (!this.mCancelled) {
                        VideoModule.this.startPreview();
                        VideoModule.this.mHandler.sendEmptyMessage(20);
                    }
                }
            }
        }
    }

    private boolean addVideoToMediaStore(ContentValues contentValues) {
        boolean z = false;
        if (this.mVideoFileDescriptor != null) {
            return false;
        }
        contentValues.put("_size", Long.valueOf(new File(this.mCurrentVideoFilename).length()));
        contentValues.put("duration", Long.valueOf(getDuration(this.mCurrentVideoFilename)));
        try {
            String asString = contentValues.getAsString("_data");
            if (this.mCurrentFileNumber > 0) {
                String format = String.format(Locale.ENGLISH, "_%d", new Object[]{Integer.valueOf(this.mCurrentFileNumber)});
                asString = insertPostfix(asString, format);
                contentValues.put("_data", asString);
                contentValues.put("title", this.mBaseFileName + format);
                contentValues.put("_display_name", insertPostfix(contentValues.getAsString("_display_name"), format));
            }
            if (new File(this.mCurrentVideoFilename).renameTo(new File(asString))) {
                this.mCurrentVideoFilename = asString;
            } else {
                contentValues.put("_data", this.mCurrentVideoFilename);
            }
            this.mCurrentVideoUri = this.mContentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues);
            this.mActivity.addSecureUri(this.mCurrentVideoUri);
            if (VERSION.SDK_INT < 24) {
                this.mActivity.sendBroadcast(new Intent("android.hardware.action.NEW_VIDEO", this.mCurrentVideoUri));
            }
            Storage.saveToCloudAlbum(this.mActivity, this.mCurrentVideoFilename);
        } catch (Throwable e) {
            Log.e("videocamera", "failed to add video to media store", e);
            this.mCurrentVideoUri = null;
            this.mCurrentVideoFilename = null;
            z = true;
        } finally {
            Log.v("videocamera", "Current video URI: " + this.mCurrentVideoUri);
        }
        return z;
    }

    private void animateHold() {
        if (HOLD_WHEN_SAVING_VIDEO && !this.mIsVideoCaptureIntent && !this.mPaused) {
            this.mActivity.getCameraScreenNail().animateHold(getCameraRotation());
        }
    }

    private void animateSlide() {
        if (HOLD_WHEN_SAVING_VIDEO && !this.mIsVideoCaptureIntent && !this.mPaused) {
            this.mActivity.getCameraScreenNail().clearAnimation();
        }
    }

    private void autoFocus(int i, int i2, int i3, int i4, int i5) {
        Log.v("videocamera", "autoFocus mVideoFocusMode=" + this.mVideoFocusMode);
        if (!"auto".equals(this.mVideoFocusMode) && !this.mObjectTrackingStarted) {
            return;
        }
        if (this.mFocusAreaSupported || this.mMeteringAreaSupported) {
            if (this.mFocusManager.isNeedCancelAutoFocus()) {
                cancelAutoFocus();
            }
            this.mParameters = this.mCameraDevice.getParameters();
            this.mFocusManager.focusPoint();
            if (this.mFocusAreaSupported) {
                sProxy.setFocusAreas(this.mParameters, this.mFocusManager.getFocusArea(i, i2, i3, i4));
            }
            if (this.mMeteringAreaSupported && i5 != 4) {
                sProxy.setMeteringAreas(this.mParameters, this.mFocusManager.getMeteringsArea(i, i2, i3, i4));
            }
            this.mCameraDevice.setParameters(this.mParameters);
            this.mFocusStartTime = System.currentTimeMillis();
            if (!this.mObjectTrackingStarted) {
                getUIController().getFocusView().setPosition(i, i2);
            }
            if (i5 == 3) {
                getUIController().getFocusView().showStart();
            }
            this.mCameraDevice.autoFocus(this);
        }
    }

    private void cancelAutoFocus() {
        this.mCameraDevice.cancelAutoFocus();
        this.mFocusManager.cancelAutoFocus();
        this.mParameters = this.mCameraDevice.getParameters();
        List supportedFocusModes = sProxy.getSupportedFocusModes(this.mParameters);
        String str = Device.isMTKPlatform() ? "auto" : "macro";
        if (BaseModule.isSupported(str, supportedFocusModes)) {
            sProxy.setFocusMode(this.mParameters, str);
            updateAutoFocusMoveCallback();
        }
        if (this.mFocusAreaSupported) {
            sProxy.setFocusAreas(this.mParameters, null);
        }
        if (this.mMeteringAreaSupported) {
            sProxy.setMeteringAreas(this.mParameters, null);
        }
        this.mCameraDevice.setParameters(this.mParameters);
    }

    private boolean capture() {
        if (this.mPaused || this.mSnapshotInProgress || !this.mMediaRecorderRecording) {
            return false;
        }
        if (Storage.isLowStorageAtLastPoint()) {
            onStopVideoRecording(false);
            return false;
        } else if (this.mActivity.getImageSaver().shouldStopShot()) {
            Log.i("videocamera", "ImageSaver is full, wait for a moment!");
            RotateTextToast.getInstance(this.mActivity).show(C0049R.string.toast_saving, 0);
            return false;
        } else {
            Util.setRotationParameter(this.mParameters, this.mCameraId, this.mOrientation);
            Location currentLocation = LocationManager.instance().getCurrentLocation();
            Util.setGpsParameters(this.mParameters, currentLocation);
            this.mCameraDevice.setParameters(this.mParameters);
            if (Device.isMDPRender()) {
                getUIController().getPreviewPanel().onCapture();
            } else {
                this.mActivity.getCameraScreenNail().animateCapture(getCameraRotation());
            }
            Log.v("videocamera", "Video snapshot start");
            this.mCameraDevice.takePicture(null, null, null, new JpegPictureCallback(currentLocation));
            getUIController().getShutterButton().enableControls(false);
            this.mSnapshotInProgress = true;
            return true;
        }
    }

    private boolean checkCallingState() {
        if (2 != this.mTelephonyManager.getCallState()) {
            return true;
        }
        this.mActivity.getScreenHint().showConfirmMessage(C0049R.string.confirm_recording_fail_title, C0049R.string.confirm_recording_fail_calling_alert);
        return false;
    }

    private void cleanupEmptyFile() {
        if (this.mVideoFilename != null) {
            File file = new File(this.mVideoFilename);
            if (file.length() == 0 && file.delete()) {
                Log.v("videocamera", "Empty video file deleted: " + this.mVideoFilename);
                this.mVideoFilename = null;
            }
        }
    }

    private void closeVideoFileDescriptor() {
        if (this.mVideoFileDescriptor != null) {
            try {
                this.mVideoFileDescriptor.close();
            } catch (Throwable e) {
                Log.e("videocamera", "Fail to close fd", e);
            }
            this.mVideoFileDescriptor = null;
        }
    }

    private int computePopupTransY() {
        View bottomControlUpperPanel = getUIController().getBottomControlUpperPanel();
        if (bottomControlUpperPanel == null) {
            return 0;
        }
        return bottomControlUpperPanel.getHeight() - this.mActivity.getResources().getDimensionPixelSize(C0049R.dimen.bottom_control_margin_bottom);
    }

    public static String convertOutputFormatToFileExt(int i) {
        return i == 2 ? ".mp4" : ".3gp";
    }

    public static String convertOutputFormatToMimeType(int i) {
        return i == 2 ? "video/mp4" : "video/3gpp";
    }

    private String createName(long j) {
        if (this.mCurrentFileNumber > 0) {
            return this.mBaseFileName;
        }
        this.mBaseFileName = new SimpleDateFormat(getString(C0049R.string.video_file_name_format)).format(new Date(j));
        return this.mBaseFileName;
    }

    private void deleteCurrentVideo() {
        if (this.mCurrentVideoFilename != null) {
            deleteVideoFile(this.mCurrentVideoFilename);
            this.mCurrentVideoFilename = null;
            if (this.mCurrentVideoUri != null) {
                Util.safeDelete(this.mCurrentVideoUri, null, null);
                this.mCurrentVideoUri = null;
            }
        }
        this.mActivity.getScreenHint().updateHint();
    }

    private void deleteVideoFile(String str) {
        Log.v("videocamera", "Deleting video " + str);
        if (!new File(str).delete()) {
            Log.v("videocamera", "Could not delete " + str);
        }
    }

    private void doReturnToCaller(boolean z) {
        int i;
        Intent intent = new Intent();
        if (z) {
            i = -1;
            intent.setData(this.mCurrentVideoUri);
            intent.setFlags(1);
        } else {
            i = 0;
        }
        this.mActivity.setResult(i, intent);
        this.mActivity.finish();
    }

    private void generateVideoFilename(int i) {
        long currentTimeMillis = System.currentTimeMillis();
        String createName = createName(currentTimeMillis);
        String str = createName + convertOutputFormatToFileExt(i);
        String convertOutputFormatToMimeType = convertOutputFormatToMimeType(i);
        String str2 = Storage.DIRECTORY + '/' + str;
        String str3 = str2 + ".tmp" + this.mCurrentFileNumber;
        this.mCurrentVideoValues = new ContentValues(7);
        this.mCurrentVideoValues.put("title", createName);
        this.mCurrentVideoValues.put("_display_name", str);
        this.mCurrentVideoValues.put("datetaken", Long.valueOf(currentTimeMillis));
        this.mCurrentVideoValues.put("mime_type", convertOutputFormatToMimeType);
        this.mCurrentVideoValues.put("_data", str2);
        this.mCurrentVideoValues.put("resolution", Integer.toString(this.mProfile.videoFrameWidth) + "x" + Integer.toString(this.mProfile.videoFrameHeight));
        Location currentLocation = LocationManager.instance().getCurrentLocation();
        if (!(currentLocation == null || (currentLocation.getLatitude() == 0.0d && currentLocation.getLongitude() == 0.0d))) {
            this.mCurrentVideoValues.put("latitude", Double.valueOf(currentLocation.getLatitude()));
            this.mCurrentVideoValues.put("longitude", Double.valueOf(currentLocation.getLongitude()));
        }
        this.mVideoFilename = str3;
        Log.v("videocamera", "New video filename: " + this.mVideoFilename);
    }

    private void getDesiredPreviewSize() {
        this.mParameters = this.mCameraDevice.getParameters();
        if (Device.isMTKPlatform() && "slow".equals(this.mHfr)) {
            this.mDesiredPreviewWidth = this.mProfile.videoFrameWidth;
            this.mDesiredPreviewHeight = this.mProfile.videoFrameHeight;
        } else if (this.mParameters.getSupportedVideoSizes() == null) {
            this.mDesiredPreviewWidth = this.mProfile.videoFrameWidth;
            this.mDesiredPreviewHeight = this.mProfile.videoFrameHeight;
        } else if ((Device.IS_MI4 || Device.IS_X5) && CameraSettings.is4KHigherVideoQuality(this.mQuality)) {
            this.mDesiredPreviewWidth = this.mProfile.videoFrameWidth;
            this.mDesiredPreviewHeight = this.mProfile.videoFrameHeight;
        } else {
            List supportedPreviewSizes = sProxy.getSupportedPreviewSizes(this.mParameters);
            Size preferredPreviewSizeForVideo = this.mParameters.getPreferredPreviewSizeForVideo();
            int i = preferredPreviewSizeForVideo.width * preferredPreviewSizeForVideo.height;
            Iterator it = supportedPreviewSizes.iterator();
            while (it.hasNext()) {
                Size size = (Size) it.next();
                if (size.width * size.height > i) {
                    it.remove();
                }
            }
            Activity activity = this.mActivity;
            double d = (Device.IS_MI3TD && this.mQuality == 0) ? 1.3333333333333333d : ((double) this.mProfile.videoFrameWidth) / ((double) this.mProfile.videoFrameHeight);
            Size optimalPreviewSize = Util.getOptimalPreviewSize(activity, supportedPreviewSizes, d);
            this.mDesiredPreviewWidth = optimalPreviewSize.width;
            this.mDesiredPreviewHeight = optimalPreviewSize.height;
        }
        Log.v("videocamera", "mDesiredPreviewWidth=" + this.mDesiredPreviewWidth + ". mDesiredPreviewHeight=" + this.mDesiredPreviewHeight);
    }

    private long getDuration(String str) {
        long parseLong;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(str);
            parseLong = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
            return parseLong;
        } catch (Throwable e) {
            parseLong = "IllegalArgumentException when getDuration()";
            Log.e("videocamera", parseLong, e);
            return 0;
        } catch (Throwable e2) {
            parseLong = "RuntimeException when getDuration()";
            Log.e("videocamera", parseLong, e2);
            return 0;
        } finally {
            mediaMetadataRetriever.release();
        }
    }

    private int getHFRQuality(int i, int i2) {
        Integer num = (Integer) VIDEO_QUALITY_TO_HIGHSPEED.get(Integer.valueOf(i2));
        if (num != null && isProfileExist(i, num)) {
            return num.intValue();
        }
        Log.w("videocamera", "cannot find hfrquality in VIDEO_QUALITY_TO_HIGHSPEED, quality " + i2 + " hfrQuality=" + num);
        return i2;
    }

    private long getSpeedRecordVideoLength(long j, double d) {
        return d == 0.0d ? 0 : (long) (((((double) j) / d) / ((double) getNormalVideoFrameRate())) * 1000.0d);
    }

    private void hideAlert() {
        Util.fadeOut(getUIController().getReviewImageView());
        Util.fadeOut(getUIController().getReviewPlayView());
        getUIController().getSettingsStatusBar().show();
        getUIController().getPreviewPage().setPopupVisible(true);
        getUIController().getReviewDoneView().animateOut(null, 100, true);
        getUIController().getShutterButton().animateIn(null, 100, true);
        getUIController().getBottomControlUpperPanel().animateIn(null);
        getUIController().getTopControlPanel().animateIn(null);
        getUIController().getBottomControlUpperPanel().setEnabled(true);
        enableCameraControls(true);
    }

    private void initializeCapabilities() {
        boolean z = false;
        this.mFocusAreaSupported = this.mParameters.getMaxNumFocusAreas() > 0 ? BaseModule.isSupported("auto", sProxy.getSupportedFocusModes(this.mParameters)) : false;
        if (this.mParameters.getMaxNumMeteringAreas() > 0) {
            z = true;
        }
        this.mMeteringAreaSupported = z;
        this.mContinuousFocusSupported = BaseModule.isSupported("continuous-video", sProxy.getSupportedFocusModes(this.mParameters));
    }

    private void initializeFocusManager() {
        this.mFocusManager = new FocusManagerSimple(getUIController().getPreviewFrame().getWidth(), getUIController().getPreviewFrame().getHeight(), this.mCameraId == CameraHolder.instance().getFrontCameraId(), Util.getDisplayOrientation(this.mCameraDisplayOrientation, this.mCameraId));
        Display defaultDisplay = this.mActivity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        this.mFocusManager.setPreviewSize(point.x, point.y);
        this.mFocusManager.setRenderSize(point.x, point.y);
    }

    private void initializeMiscControls() {
    }

    private boolean initializeObjectTrack(RectF rectF, boolean z) {
        mapTapCoordinate(rectF);
        stopObjectTracking(false);
        getUIController().getObjectView().clear();
        getUIController().getFocusView().clear();
        getUIController().getObjectView().setVisibility(0);
        return getUIController().getObjectView().initializeTrackView(rectF, z);
    }

    private void initializeRecorder() {
        Log.v("videocamera", "initializeRecorder");
        if (this.mCameraDevice != null) {
            Bundle extras = this.mActivity.getIntent().getExtras();
            this.mVideoWidth = this.mProfile.videoFrameWidth;
            this.mVideoHeight = this.mProfile.videoFrameHeight;
            long j = 0;
            closeVideoFileDescriptor();
            if (this.mIsVideoCaptureIntent && extras != null) {
                Uri uri = (Uri) extras.getParcelable("output");
                if (uri != null) {
                    try {
                        this.mVideoFileDescriptor = this.mContentResolver.openFileDescriptor(uri, "rw");
                        this.mCurrentVideoUri = uri;
                    } catch (FileNotFoundException e) {
                        Log.e("videocamera", e.toString());
                    }
                }
                j = extras.getLong("android.intent.extra.sizeLimit");
            }
            this.mMediaRecorder = new MediaRecorder();
            this.mCameraDevice.unlock();
            this.mMediaRecorder.setCamera(this.mCameraDevice.getCamera());
            if ("normal".equals(this.mHfr)) {
                this.mMediaRecorder.setAudioSource(5);
            } else {
                this.mProfile.audioCodec = -1;
            }
            this.mMediaRecorder.setVideoSource(1);
            this.mProfile.duration = this.mMaxVideoDurationInMs;
            setProfileToRecorder();
            this.mMediaRecorder.setMaxDuration(this.mMaxVideoDurationInMs);
            if (Device.isSupportedAudioFocus()) {
                String string = isBackCamera() ? this.mPreferences.getString("pref_audio_focus_key", getString(C0049R.string.pref_audio_focus_default)) : getString(C0049R.string.pref_audio_focus_entryvalue_front);
                Log.v("videocamera", "set AudioParam camcorder_mode=" + string);
                AudioSystem.setParameters("camcorder_mode=" + string);
            }
            int videoQuality = CameraSettings.getVideoQuality();
            if (Device.IS_MI2 && "fast".equals(this.mHfr) && videoQuality == 5) {
                this.mMediaRecorder.setVideoEncodingBitRate(4000000);
            }
            if (this.mCaptureTimeLapse) {
                this.mMediaRecorder.setCaptureRate(1000.0d / ((double) this.mTimeBetweenTimeLapseFrameCaptureMs));
            }
            configMediaRecorder(this.mMediaRecorder);
            Location currentLocation = LocationManager.instance().getCurrentLocation();
            if (currentLocation != null) {
                this.mMediaRecorder.setLocation((float) currentLocation.getLatitude(), (float) currentLocation.getLongitude());
            }
            if (this.mVideoFileDescriptor != null) {
                this.mMediaRecorder.setOutputFile(this.mVideoFileDescriptor.getFileDescriptor());
            } else {
                generateVideoFilename(this.mProfile.fileFormat);
                this.mMediaRecorder.setOutputFile(this.mVideoFilename);
            }
            long storageSpace = this.mActivity.getScreenHint().getStorageSpace() - 52428800;
            if (3670016000L < storageSpace) {
                Log.v("videocamera", "need reduce , now maxFileSize = " + storageSpace);
                storageSpace = 3670016000L;
            }
            if (storageSpace < VIDEO_MIN_SINGLE_FILE_SIZE) {
                storageSpace = VIDEO_MIN_SINGLE_FILE_SIZE;
            }
            if (j > 0 && j < r12) {
                storageSpace = j;
            }
            try {
                Log.v("videocamera", "maxFileSize = " + storageSpace);
                this.mMediaRecorder.setMaxFileSize(storageSpace);
            } catch (RuntimeException e2) {
            }
            if ("slow".equals(this.mHfr)) {
                setHFRSpeed(this.mMediaRecorder, this.mProfile.videoFrameRate / 30);
            } else {
                setHFRSpeed(this.mMediaRecorder, 1);
            }
            CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[this.mCameraId];
            int i = this.mOrientation != -1 ? cameraInfo.facing == 1 ? ((cameraInfo.orientation - this.mOrientation) + 360) % 360 : (cameraInfo.orientation + this.mOrientation) % 360 : cameraInfo.orientation;
            this.mMediaRecorder.setOrientationHint(i);
            this.mOrientationCompensationAtRecordStart = this.mOrientationCompensation;
            try {
                this.mMediaRecorder.prepare();
                this.mMediaRecorder.setOnErrorListener(this);
                this.mMediaRecorder.setOnInfoListener(this);
            } catch (Throwable e3) {
                Log.e("videocamera", "prepare failed for " + this.mVideoFilename, e3);
                releaseMediaRecorder();
                throw new RuntimeException(e3);
            }
        }
    }

    private String insertPostfix(String str, String str2) {
        StringBuffer stringBuffer = new StringBuffer(str);
        stringBuffer.insert(stringBuffer.lastIndexOf("."), str2);
        return stringBuffer.toString();
    }

    private boolean isAudioFocusPopupVisible(View view) {
        if (view == null || !(view instanceof V6AbstractSettingPopup)) {
            return false;
        }
        return "pref_audio_focus_key".equals(((V6AbstractSettingPopup) view).getKey());
    }

    private boolean isSelectingCapturedVideo() {
        return this.mIsVideoCaptureIntent && getUIController().getReviewDoneView().getVisibility() == 0;
    }

    private boolean isVideoProcessing() {
        return this.mVideoSavingTask != null ? this.mVideoSavingTask.isAlive() : false;
    }

    private void keepScreenOn() {
        this.mHandler.removeMessages(10);
        this.mHandler.removeMessages(2);
        getWindow().addFlags(128);
    }

    private void keepScreenOnAwhile() {
        this.mHandler.sendEmptyMessageDelayed(10, 1000);
    }

    private void manuallyTriggerAutoFocus() {
        if (!TextUtils.equals("continuous-video", this.mPreferences.getString("pref_video_focusmode_key", getString(C0049R.string.pref_video_focusmode_entryvalue_default)))) {
            this.mHandler.sendEmptyMessageDelayed(24, 1000);
        }
    }

    private static String millisecondToTimeString(long j, boolean z) {
        long j2 = j / 1000;
        long j3 = j2 / 60;
        long j4 = j3 / 60;
        long j5 = j3 - (60 * j4);
        long j6 = j2 - (60 * j3);
        StringBuilder stringBuilder = new StringBuilder();
        if (j4 > 0) {
            if (j4 < 10) {
                stringBuilder.append(String.format("%d", new Object[]{Integer.valueOf(0)}));
            }
            stringBuilder.append(String.format("%d", new Object[]{Long.valueOf(j4)}));
            stringBuilder.append(':');
        }
        if (j5 < 10) {
            stringBuilder.append(String.format("%d", new Object[]{Integer.valueOf(0)}));
        }
        stringBuilder.append(String.format("%d", new Object[]{Long.valueOf(j5)}));
        stringBuilder.append(':');
        if (j6 < 10) {
            stringBuilder.append(String.format("%d", new Object[]{Integer.valueOf(0)}));
        }
        stringBuilder.append(String.format("%d", new Object[]{Long.valueOf(j6)}));
        if (z) {
            stringBuilder.append('.');
            long j7 = (j - (1000 * j2)) / 10;
            if (j7 < 10) {
                stringBuilder.append('0');
            }
            stringBuilder.append(j7);
        }
        return stringBuilder.toString();
    }

    private void onFrameLayoutChange(View view, Rect rect) {
        this.mActivity.onLayoutChange(rect.width(), rect.height());
        if (!(this.mFocusManager == null || this.mActivity.getCameraScreenNail() == null)) {
            this.mActivity.getCameraScreenNail().setRenderArea(rect);
            this.mFocusManager.setRenderSize(this.mActivity.getCameraScreenNail().getRenderWidth(), this.mActivity.getCameraScreenNail().getRenderHeight());
            this.mFocusManager.setPreviewSize(rect.width(), rect.height());
        }
        if (getUIController().getObjectView() != null) {
            getUIController().getObjectView().setDisplaySize(rect.right - rect.left, rect.bottom - rect.top);
        }
    }

    private void onPreviewStart() {
        if (this.mPreviewing) {
            this.mActivity.getCameraScreenNail().animateModuleChangeAfter();
            getUIController().getFocusView().initialize(this);
            getUIController().onCameraOpen();
            updateMutexModePreference();
            this.mHandler.removeMessages(14);
            this.mHandler.sendEmptyMessageDelayed(14, 100);
            enableCameraControls(true);
        }
    }

    private void onRestartVideoRecording() {
        if (this.mMediaRecorderRecording && this.mVideoSavingTask == null) {
            this.mRecorderBusy = true;
            this.mVideoSavingTask = new SavingTask(true);
            this.mVideoSavingTask.start();
        }
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
        enableCameraControls(false);
        this.mActivity.getSensorStateManager().setFocusSensorEnabled(false);
        if (this.mFocusManager != null && this.mFocusManager.isNeedCancelAutoFocus()) {
            this.mFocusManager.cancelAutoFocus();
        }
        this.mStereoSwitchThread = new StereoSwitchThread();
        this.mStereoSwitchThread.start();
    }

    private void onStopVideoRecording(boolean z) {
        if (z) {
            stopVideoOnPause();
        } else {
            stopVideoRecordingAsync();
        }
    }

    private void pauseVideoRecording() {
        Log.d("videocamera", "pauseVideoRecording() mRecorderBusy=" + this.mRecorderBusy);
        getUIController().getPauseRecordingButton().setImageResource(C0049R.drawable.ic_recording_resume);
        if (this.mMediaRecorderRecording && !this.mMediaRecorderRecordingPaused) {
            try {
                pauseMediaRecorder(this.mMediaRecorder);
            } catch (IllegalStateException e) {
                Log.e("videocamera", "Could not pause media recorder. ");
            }
            this.mVideoRecordedDuration = SystemClock.uptimeMillis() - this.mRecordingStartTime;
            this.mMediaRecorderRecordingPaused = true;
            this.mHandler.removeMessages(3);
            this.mCurrentShowIndicator = 0;
            updateRecordingTime();
        }
    }

    private void prepareUIByPreviewSize() {
        if (!CameraSettings.sCroppedIfNeeded || Device.isMDPRender()) {
            if (1 != this.mUIStyle) {
                this.mUIStyle = 1;
                CameraSettings.changeUIByPreviewSize(this.mActivity, this.mUIStyle, this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
                changePreviewSurfaceSize();
            }
            getUIController().getPreviewFrame().setAspectRatio(CameraSettings.getPreviewAspectRatio(16, 9));
            return;
        }
        this.mUIStyle = 1;
    }

    private void releaseMediaRecorder() {
        Log.v("videocamera", "Releasing media recorder.");
        if (this.mMediaRecorder != null) {
            cleanupEmptyFile();
            this.mMediaRecorder.reset();
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
        if (Device.isSupportedAudioFocus()) {
            Log.v("videocamera", "restore AudioParam camcorder_mode=" + getString(C0049R.string.pref_audio_focus_default));
            AudioSystem.setParameters("camcorder_mode=" + getString(C0049R.string.pref_audio_focus_default));
        }
        this.mVideoFilename = null;
    }

    private void releaseResources() {
        closeCamera();
        releaseMediaRecorder();
        CameraDataAnalytics.instance().uploadToServer();
        this.mWaitForRelease = false;
    }

    private void resetScreenOn() {
        this.mHandler.removeMessages(10);
        this.mHandler.removeMessages(2);
        getWindow().clearFlags(128);
    }

    private void resizeForPreviewAspectRatio() {
        CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[this.mCameraId];
        if (((cameraInfo.orientation - Util.getDisplayRotation(this.mActivity)) + 360) % 180 == 0) {
            getUIController().getPreviewFrame().setAspectRatio(((float) this.mProfile.videoFrameHeight) / ((float) this.mProfile.videoFrameWidth));
        } else {
            getUIController().getPreviewFrame().setAspectRatio(((float) this.mProfile.videoFrameWidth) / ((float) this.mProfile.videoFrameHeight));
        }
    }

    private void restoreMusicSound() {
        if (this.mOriginalMusicVolume != 0 && this.mAudioManager.getStreamVolume(3) == 0) {
            this.mAudioManager.setStreamMute(3, false);
        }
        this.mOriginalMusicVolume = 0;
        this.mHandler.removeMessages(23);
    }

    private void restorePreferences() {
        if (this.mParameters.isZoomSupported()) {
            setZoomValue(0);
        }
        getUIController().getFlashButton().reloadPreference();
        getUIController().getSettingPage().reloadPreferences();
        onSharedPreferenceChanged();
    }

    private void setProfileToRecorder() {
        this.mMediaRecorder.setOutputFormat(this.mProfile.fileFormat);
        this.mMediaRecorder.setVideoFrameRate(this.mProfile.videoFrameRate);
        this.mMediaRecorder.setVideoSize(this.mProfile.videoFrameWidth, this.mProfile.videoFrameHeight);
        this.mMediaRecorder.setVideoEncodingBitRate(this.mProfile.videoBitRate);
        this.mMediaRecorder.setVideoEncoder(this.mProfile.videoCodec);
        if (this.mProfile.audioCodec >= 0) {
            this.mMediaRecorder.setAudioEncodingBitRate(this.mProfile.audioBitRate);
            this.mMediaRecorder.setAudioChannels(this.mProfile.audioChannels);
            this.mMediaRecorder.setAudioSamplingRate(this.mProfile.audioSampleRate);
            this.mMediaRecorder.setAudioEncoder(this.mProfile.audioCodec);
        }
    }

    private void showAlert() {
        Bitmap bitmap = null;
        if (this.mVideoFileDescriptor != null) {
            bitmap = Thumbnail.createVideoThumbnailBitmap(this.mVideoFileDescriptor.getFileDescriptor(), getUIController().getPreviewFrame().getWidth());
        } else if (this.mCurrentVideoFilename != null) {
            bitmap = Thumbnail.createVideoThumbnailBitmap(this.mCurrentVideoFilename, getUIController().getPreviewFrame().getWidth());
        }
        if (bitmap != null) {
            getUIController().getReviewImageView().setImageBitmap(Util.rotateAndMirror(bitmap, -this.mOrientationCompensationAtRecordStart, this.mCameraId == CameraHolder.instance().getFrontCameraId()));
            getUIController().getReviewImageView().setVisibility(0);
        }
        Util.fadeIn(getUIController().getReviewPlayView());
        ignoreTouchEvent(true);
        getUIController().getSettingsStatusBar().hide();
        getUIController().getPreviewPage().setPopupVisible(false);
        getUIController().getShutterButton().animateOut(null, 100, true);
        getUIController().getReviewDoneView().animateIn(null, 100, true);
        getUIController().getBottomControlUpperPanel().animateOut(null);
        getUIController().getTopControlPanel().animateOut(null);
    }

    private void showFirstUseHintIfNeeded() {
        if (this.mPreferences.getBoolean("pref_camera_first_use_hint_shown_key", true) || this.mPreferences.getBoolean("pref_camera_first_portrait_use_hint_shown_key", true)) {
            this.mHandler.sendEmptyMessageDelayed(19, 1000);
        }
    }

    private void showRecordingUI(boolean z) {
        boolean z2 = false;
        this.mRecordingUIShown = z;
        View currentPopup = getUIController().getSettingPage().getCurrentPopup();
        boolean isAudioFocusPopupVisible = isAudioFocusPopupVisible(currentPopup);
        boolean isFullScreen = getUIController().getPreviewFrame().isFullScreen();
        View popupParentLayout;
        if (z) {
            getUIController().getShutterButton().changeImageWithAnimation(C0049R.drawable.video_shutter_button_stop_bg, 200);
            getUIController().getVideoRecordingTimeView().setText("");
            if (isAudioFocusPopupVisible) {
                getUIController().getPreviewPage().showPopupWithoutExitView();
                currentPopup.setEnabled(true);
                popupParentLayout = getUIController().getPopupParentLayout();
                if (popupParentLayout != null) {
                    popupParentLayout.setTranslationY((float) computePopupTransY());
                }
                if (isFullScreen) {
                    ((GridSettingTextPopup) currentPopup).shrink(this.mActivity.getResources().getDimensionPixelSize(C0049R.dimen.audio_focus_popup_width));
                }
            } else if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                getUIController().getStereoButton().showPopup(false);
                getUIController().getPreviewPage().showPopupWithoutExitView();
                getUIController().getStereoButton().getPopup().setEnabled(true);
            } else {
                getUIController().getPreviewPage().setPopupVisible(false);
            }
            getUIController().getStereoButton().setVisibility(8);
            getUIController().getBottomControlPanel().setBackgroundVisible(false);
            getUIController().getBottomControlUpperPanel().animateOut(null);
            getUIController().getTopControlPanel().animateOut(null);
            if (getUIController().getSettingsStatusBar().getVisibility() == 0) {
                z2 = true;
            }
            if (z2) {
                getUIController().getSettingsStatusBar().animateOut(new C01176());
            }
            getUIController().getThumbnailButton().animateOut(new Runnable() {
                public void run() {
                    if (!z2) {
                        VideoModule.this.getUIController().getVideoRecordingTimeView().animateIn(null, 150, true);
                    }
                    VideoModule.this.getUIController().getPauseRecordingButton().setImageResource(C0049R.drawable.ic_recording_pause);
                    VideoModule.this.getUIController().getPauseRecordingButton().animateIn(null, 100, true);
                    VideoModule.this.getUIController().getPauseRecordingButton().enableControls(true);
                    VideoModule.this.getUIController().getVideoCaptureButton().animateIn(null, 100, true);
                    VideoModule.this.getUIController().getVideoCaptureButton().enableControls(true);
                }
            }, 100, true);
            if (this.mIsVideoCaptureIntent) {
                getUIController().getReviewCanceledView().animateOut(null, 100, true);
                return;
            } else {
                getUIController().getModulePicker().animateOut(null, 100, true);
                return;
            }
        }
        getUIController().getShutterButton().changeImageWithAnimation(C0049R.drawable.video_shutter_button_start_bg, 200);
        getUIController().getVideoCaptureButton().setVisibility(8);
        getUIController().getBottomControlPanel().setBackgroundVisible(true);
        if (isAudioFocusPopupVisible) {
            popupParentLayout = getUIController().getPopupParentLayout();
            if (popupParentLayout != null) {
                popupParentLayout.setTranslationY(0.0f);
            }
            if (isFullScreen) {
                ((GridSettingTextPopup) currentPopup).restoreFromShrink();
            }
        }
        if (isAudioFocusPopupVisible || CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            getUIController().getModeExitView().show();
        }
        if (!this.mIsVideoCaptureIntent) {
            getUIController().getPreviewPage().setPopupVisible(true);
            getUIController().getBottomControlUpperPanel().animateIn(null);
            getUIController().getTopControlPanel().animateIn(null);
        }
        getUIController().getVideoRecordingTimeView().animateOut(new C01198(), 150, true);
        getUIController().getPauseRecordingButton().animateOut(new C01209(), 100, true);
    }

    private void showStoppingUI() {
        if (this.mRecordingUIShown) {
            showRecordingUI(false);
        }
        updateLoadUI(true);
    }

    private void silenceSounds() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mActivity.getSystemService("audio");
        }
        this.mAudioManager.requestAudioFocus(null, 3, 2);
        this.mOriginalMusicVolume = this.mAudioManager.getStreamVolume(3);
        if (this.mOriginalMusicVolume != 0) {
            this.mAudioManager.setStreamMute(3, true);
            this.mHandler.sendEmptyMessageDelayed(23, 3000);
        }
    }

    private void startPlayVideoActivity() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(this.mCurrentVideoUri, convertOutputFormatToMimeType(this.mProfile.fileFormat));
        intent.setFlags(1);
        try {
            this.mActivity.startActivity(intent);
        } catch (Throwable e) {
            Log.e("videocamera", "Couldn't view video " + this.mCurrentVideoUri, e);
        }
    }

    private void startVideoRecording() {
        Log.v("videocamera", "startVideoRecording");
        this.mCurrentVideoUri = null;
        this.mCurrentFileNumber = this.mIsVideoCaptureIntent ? -1 : 0;
        silenceSounds();
        prepareRecording();
        if (startRecordVideo()) {
            ignoreTouchEvent(false);
            Log.v("videocamera", "startVideoRecording process done");
            this.mParameters = this.mCameraDevice.getParameters();
            this.mActivity.sendBroadcast(new Intent("com.android.camera.action.start_video_recording"));
            this.mMediaRecorderRecording = true;
            this.mSavingResult = false;
            this.mMediaRecorderRecordingPaused = false;
            this.mRecordingStartTime = SystemClock.uptimeMillis();
            this.mPauseClickTime = SystemClock.uptimeMillis();
            this.mCurrentShowIndicator = -1;
            this.mRecordingTime = "";
            showRecordingUI(true);
            this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
            Log.v("videocamera", "listen call state");
            updateRecordingTime();
            keepScreenOn();
            trackRecordingInfo();
            AutoLockManager.getInstance(this.mActivity).hibernateDelayed();
            return;
        }
        enableCameraControls(true);
        this.mAudioManager.abandonAudioFocus(null);
        restoreMusicSound();
    }

    private void stopVideoOnPause() {
        Log.i("videocamera", "stopVideoOnPause() mMediaRecorderRecording =  " + this.mMediaRecorderRecording + " mRecorderBusy=" + this.mRecorderBusy);
        boolean z = false;
        if (this.mMediaRecorderRecording) {
            stopVideoRecordingAsync();
            z = isVideoProcessing();
        } else {
            releaseMediaRecorder();
        }
        if (z) {
            waitForRecorder();
        } else {
            closeVideoFileDescriptor();
        }
        Log.i("videocamera", "stopVideoOnPause()  videoSaving=" + z + ", mVideoSavingTask=" + this.mVideoSavingTask + ", mMediaRecorderRecording=" + this.mMediaRecorderRecording);
    }

    private void stopVideoRecordingAsync() {
        if (!this.mRecorderBusy && this.mMediaRecorderRecording) {
            animateHold();
            this.mRecorderBusy = true;
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessage(15);
            this.mVideoSavingTask = new SavingTask(false);
            this.mVideoSavingTask.start();
        }
    }

    private void storeImage(byte[] bArr, Location location) {
        long currentTimeMillis = System.currentTimeMillis();
        int orientation = Exif.getOrientation(bArr);
        Size pictureSize = this.mParameters.getPictureSize();
        this.mActivity.getImageSaver().addImage(bArr, Util.createJpegName(currentTimeMillis), System.currentTimeMillis(), null, location, pictureSize.width, pictureSize.height, null, orientation, false, false, true);
        trackPictureTaken(1, false, pictureSize.width, pictureSize.height, location != null);
    }

    private void switchCamera() {
        if (!this.mPaused) {
            if (!this.mMutexModePicker.isNormal()) {
                this.mMutexModePicker.resetMutexMode();
            }
            updateStereoSettings(false);
            Log.d("videocamera", "Start to switch camera.");
            this.mCameraId = this.mPendingSwitchCameraId;
            this.mPendingSwitchCameraId = -1;
            CameraSettings.writePreferredCameraId(this.mPreferences, this.mCameraId);
            this.mActivity.changeRequestOrientation();
            CameraSettings.resetZoom(this.mPreferences);
            CameraSettings.resetExposure();
            CameraSettingPreferences.instance().setLocalId(getPreferencesLocalId());
            PopupManager.getInstance(this.mActivity).notifyShowPopup(null, 1);
            getUIController().getModeExitView().updateExitButton(-1, false);
            closeCamera();
            getUIController().updatePreferenceGroup();
            CameraOpenThread cameraOpenThread = new CameraOpenThread();
            cameraOpenThread.start();
            try {
                cameraOpenThread.join();
            } catch (InterruptedException e) {
            }
            if (hasCameraException()) {
                onCameraException();
                return;
            }
            onCameraOpen();
            initializeCapabilities();
            updateStereoSettings(true);
            readVideoPreferences();
            getUIController().getFlashButton().avoidTorchOpen();
            startPreview();
            getUIController().onCameraOpen();
            getUIController().getFocusView().initialize(this);
            initializeZoom();
            initializeExposureCompensation();
            setOrientationIndicator(this.mOrientationCompensation, false);
            updateMutexModePreference();
            showFirstUseHintIfNeeded();
            this.mHandler.sendEmptyMessage(12);
        }
    }

    private boolean switchToOtherMode(int i) {
        if (this.mActivity.isFinishing()) {
            return false;
        }
        this.mActivity.switchToOtherModule(i);
        return true;
    }

    private void trackRecordingInfo() {
        CameraDataAnalytics.instance().trackEvent("video_recorded_key");
        if ("fast".equals(this.mHfr)) {
            CameraDataAnalytics.instance().trackEvent("video_fast_recording_times_key");
        } else if ("slow".equals(this.mHfr)) {
            CameraDataAnalytics.instance().trackEvent("video_slow_recording_times_key");
        } else if (CameraSettings.isSwitchOn("pref_video_hdr_key")) {
            CameraDataAnalytics.instance().trackEvent("video_hdr_recording_times_key");
        }
        if (this.mQuality == 5) {
            CameraDataAnalytics.instance().trackEvent("video_quality_720_recording_times_key");
        } else if (this.mQuality == 4) {
            CameraDataAnalytics.instance().trackEvent("video_quality_480_recording_times_key");
        } else if (this.mQuality == 6) {
            CameraDataAnalytics.instance().trackEvent("video_quality_1080_recording_times_key");
        } else {
            CameraDataAnalytics.instance().trackEvent("video_quality_4k_recording_times_key");
        }
        if ("torch".equals(this.mParameters.getFlashMode())) {
            CameraDataAnalytics.instance().trackEvent("video_torch_recording_times_key");
        }
        if (isFrontCamera()) {
            CameraDataAnalytics.instance().trackEvent("video_front_camera_recording_times_key");
        }
        if (this.mCurrentVideoValues == null) {
            return;
        }
        if (this.mCurrentVideoValues.get("latitude") != null || this.mCurrentVideoValues.get("longitude") != null) {
            CameraDataAnalytics.instance().trackEvent("video_with_location_key");
        } else if (CameraSettings.isRecordLocation(this.mPreferences)) {
            CameraDataAnalytics.instance().trackEvent("video_without_location_key");
        }
    }

    private void updateAutoFocusMoveCallback() {
        if (!this.mContinuousFocusSupported) {
            return;
        }
        if (this.mParameters.getFocusMode().equals("continuous-video")) {
            this.mCameraDevice.setAutoFocusMoveCallback(this);
        } else {
            this.mCameraDevice.setAutoFocusMoveCallback(null);
        }
    }

    private void updateMotionFocusManager() {
        this.mActivity.getSensorStateManager().setFocusSensorEnabled("auto".equals(this.mVideoFocusMode));
    }

    private void updateMutexModePreference() {
        if ("on".equals(getUIController().getHdrButton().getValue())) {
            this.mMutexModePicker.setMutexMode(2);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateParametersAfterRecording() {
        /*
        r2 = this;
        r0 = r2.mParameters;
        if (r0 == 0) goto L_0x0008;
    L_0x0004:
        r0 = r2.mCameraDevice;
        if (r0 != 0) goto L_0x0009;
    L_0x0008:
        return;
    L_0x0009:
        r0 = com.android.camera.Device.isPad();
        if (r0 == 0) goto L_0x003f;
    L_0x000f:
        r0 = r2.mParameters;
        r0 = r0.isVideoStabilizationSupported();
        if (r0 == 0) goto L_0x003f;
    L_0x0017:
        r0 = r2.mPreferences;
        r0 = com.android.camera.CameraSettings.isMovieSolidOn(r0);
        if (r0 == 0) goto L_0x003f;
    L_0x001f:
        r0 = "videocamera";
        r1 = "set video stabilization to false";
        android.util.Log.v(r0, r1);
        r0 = r2.mParameters;
        r1 = 0;
        r0.setVideoStabilization(r1);
        r0 = r2.mCameraDevice;
        r1 = r2.mParameters;
        r0.setParameters(r1);
        r0 = r2.mActivity;
        r0 = r0.getCameraScreenNail();
        r1 = 1;
        r0.setVideoStabilizationCropped(r1);
    L_0x003f:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.VideoModule.updateParametersAfterRecording():void");
    }

    private void updateRecordingTime() {
        if (this.mMediaRecorderRecording) {
            String millisecondToTimeString;
            long uptimeMillis = SystemClock.uptimeMillis() - this.mRecordingStartTime;
            if (this.mMediaRecorderRecordingPaused) {
                uptimeMillis = this.mVideoRecordedDuration;
            }
            boolean z = this.mMaxVideoDurationInMs != 0 ? uptimeMillis >= ((long) (this.mMaxVideoDurationInMs - 60000)) : false;
            long j = uptimeMillis;
            if (z) {
                j = Math.max(0, ((long) this.mMaxVideoDurationInMs) - j) + 999;
            }
            long j2 = 1000;
            if ("normal".equals(this.mHfr)) {
                millisecondToTimeString = millisecondToTimeString(j, false);
            } else {
                double d = 0.0d;
                if ("fast".equals(this.mHfr)) {
                    d = (double) this.mTimeBetweenTimeLapseFrameCaptureMs;
                    j2 = (long) d;
                } else {
                    if ("slow".equals(this.mHfr) && isShowHFRDuration()) {
                        String videoHighFrameRate = sProxy.getVideoHighFrameRate(this.mParameters);
                        if (!(videoHighFrameRate == null || videoHighFrameRate.equals("off"))) {
                            d = 1000.0d / Double.parseDouble(videoHighFrameRate);
                            j2 = (long) ((getNormalVideoFrameRate() * 1000) / Integer.parseInt(videoHighFrameRate));
                        }
                    }
                }
                if (d != 0.0d) {
                    millisecondToTimeString = millisecondToTimeString(getSpeedRecordVideoLength(uptimeMillis, d), "fast".equals(this.mHfr));
                    if (millisecondToTimeString.equals(this.mRecordingTime)) {
                        j2 = (long) d;
                    }
                } else {
                    millisecondToTimeString = millisecondToTimeString(j, false);
                }
            }
            getUIController().getVideoRecordingTimeView().setText(millisecondToTimeString);
            this.mRecordingTime = millisecondToTimeString;
            if (this.mRecordingTimeCountsDown != z) {
                this.mRecordingTimeCountsDown = z;
            }
            if (this.mCurrentShowIndicator != -1) {
                this.mCurrentShowIndicator = 1 - this.mCurrentShowIndicator;
                if (this.mMediaRecorderRecordingPaused && 1 == this.mCurrentShowIndicator) {
                    getUIController().getVideoRecordingTimeView().setVisibility(4);
                } else {
                    getUIController().getVideoRecordingTimeView().setVisibility(0);
                    if (!this.mMediaRecorderRecordingPaused) {
                        this.mCurrentShowIndicator = -1;
                    }
                }
            }
            long j3 = 500;
            if (!this.mMediaRecorderRecordingPaused) {
                j3 = j2 - (uptimeMillis % j2);
            }
            this.mHandler.sendEmptyMessageDelayed(3, j3);
        }
    }

    private void updateStereoSettings(boolean z) {
        if (!CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
            return;
        }
        if (z) {
            this.mSettingsOverrider.overrideSettings("pref_camera_video_flashmode_key", "off");
            return;
        }
        this.mSettingsOverrider.restoreSettings();
    }

    private void waitStereoSwitchThread() {
        try {
            if (this.mStereoSwitchThread != null) {
                this.mStereoSwitchThread.cancel();
                this.mStereoSwitchThread.join();
                this.mStereoSwitchThread = null;
            }
        } catch (InterruptedException e) {
        }
    }

    protected void animateSwitchCamera() {
        if (Device.isMDPRender()) {
            this.mHandler.sendEmptyMessageDelayed(14, 100);
            enableCameraControls(true);
            CameraSettings.changeUIByPreviewSize(this.mActivity, this.mUIStyle, this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
            this.mSwitchingCamera = false;
            return;
        }
        this.mHandler.sendEmptyMessage(6);
    }

    protected void closeCamera() {
        Log.v("videocamera", "closeCamera");
        if (this.mCameraDevice == null) {
            Log.d("videocamera", "already stopped.");
            return;
        }
        stopPreview();
        this.mCameraDevice.setErrorCallback(null);
        this.mCameraDevice.removeAllAsyncMessage();
        CameraHolder.instance().release();
        this.mCameraDevice = null;
        this.mPreviewing = false;
        this.mSnapshotInProgress = false;
    }

    protected void configMediaRecorder(MediaRecorder mediaRecorder) {
    }

    protected void enterMutexMode() {
        setOrientationIndicator(this.mOrientationCompensation, false);
        setZoomValue(0);
        this.mSettingsOverrider.overrideSettings("pref_camera_whitebalance_key", null, "pref_camera_coloreffect_key", null);
        onSharedPreferenceChanged();
        getUIController().getSettingsStatusBar().updateStatus();
    }

    protected void exitMutexMode() {
        this.mSettingsOverrider.restoreSettings();
        onSharedPreferenceChanged();
        getUIController().getSettingsStatusBar().updateStatus();
    }

    protected CamcorderProfile fetchProfile(int i, int i2) {
        return CamcorderProfile.get(i, i2);
    }

    protected int getCameraRotation() {
        return ((this.mOrientationCompensation - this.mDisplayRotation) + 360) % 360;
    }

    protected int getNormalVideoFrameRate() {
        return ("slow".equals(this.mHfr) || this.mProfile == null) ? 30 : this.mProfile.videoFrameRate;
    }

    public List<String> getSupportedSettingKeys() {
        List<String> arrayList = new ArrayList();
        if (isBackCamera()) {
            arrayList.add("pref_video_speed_fast_key");
            if (Device.isSupportedHFR()) {
                arrayList.add("pref_video_speed_slow_key");
            }
            if (Device.isSupportedAudioFocus()) {
                arrayList.add("pref_audio_focus_mode_key");
            }
        }
        return arrayList;
    }

    public boolean handleMessage(int i, int i2, final Object obj, Object obj2) {
        if (super.handleMessage(i, i2, obj, obj2)) {
            return true;
        }
        switch (i2) {
            case C0049R.id.hide_mode_animation_done:
                getUIController().useProperView();
                return true;
            case C0049R.id.v6_thumbnail_button:
                onThumbnailClicked(null);
                return true;
            case C0049R.id.v6_video_pause_button:
                onPauseButtonClick();
                return true;
            case C0049R.id.v6_shutter_button:
                if (i == 0) {
                    onShutterButtonClick();
                } else if (i == 1) {
                    onShutterButtonLongClick();
                } else if (i == 2) {
                    if (isBackCamera()) {
                        Point point = (Point) obj;
                        Point point2 = (Point) obj2;
                        getUIController().getSmartShutterButton().flyin(point.x, point.y, point2.x, point2.y);
                    }
                } else if (i == 3) {
                    onShutterButtonFocus(((Boolean) obj).booleanValue());
                }
                return true;
            case C0049R.id.v6_module_picker:
                Runnable c01165 = new Runnable() {
                    public void run() {
                        VideoModule.this.mActivity.getCameraScreenNail().animateModuleChangeBefore();
                        VideoModule.this.switchToOtherMode(((Integer) obj).intValue());
                    }
                };
                getUIController().enableControls(false);
                ignoreTouchEvent(true);
                getUIController().getFocusView().clear();
                getUIController().getBottomControlLowerPanel().animationSwitchToCamera(c01165);
                this.mActivity.getCameraScreenNail().switchModule();
                return true;
            case C0049R.id.v6_video_capture_button:
                capture();
                CameraDataAnalytics.instance().trackEvent("capture_nums_video_capture");
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
                if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                    this.mSettingsOverrider.removeSavedSetting("pref_camera_flashmode_key");
                    getUIController().getStereoButton().switchOffStereo(true);
                    return true;
                } else if (this.mMutexModePicker.isNormal() || this.mMutexModePicker.isSupportedFlashOn() || this.mMutexModePicker.isSupportedTorch()) {
                    onSharedPreferenceChanged();
                    return true;
                } else {
                    if (getUIController().getModeExitView().isExitButtonShown()) {
                        getUIController().getModeExitView().clearExitButtonClickListener(true);
                    } else {
                        this.mMutexModePicker.resetMutexMode();
                    }
                    return true;
                }
            case C0049R.id.v6_hdr:
                switchMutexHDR();
                return true;
            case C0049R.id.stereo_switch_image:
                if (i == 7) {
                    onSettingValueChanged((String) obj);
                } else {
                    if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                        updateStereoSettings(true);
                    } else {
                        this.mSettingsOverrider.restoreSettings();
                    }
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
            case C0049R.id.v6_video_btn_play:
                startPlayVideoActivity();
                return true;
            case C0049R.id.v6_setting_page:
                if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                    getUIController().getStereoButton().switchOffStereo(true);
                    return true;
                }
                getUIController().getHdrButton().updateVisible();
                if (i == 7) {
                    onSharedPreferenceChanged();
                } else if (i == 6) {
                    if ("pref_video_speed_fast_key".equals(obj) || "pref_video_speed_slow_key".equals(obj)) {
                        this.mRestartPreview = true;
                        onSharedPreferenceChanged();
                    } else if ("pref_video_hdr_key".equals(obj)) {
                        switchMutexHDR();
                    }
                }
                getUIController().getStereoButton().updateVisible();
                return true;
            case C0049R.id.setting_button:
                openSettingActivity();
                return true;
            case C0049R.id.v6_surfaceview:
                if (Device.isMDPRender() && !this.mPreviewing) {
                    startPreview();
                    onPreviewStart();
                }
                return true;
            default:
                return false;
        }
    }

    public boolean isCameraEnabled() {
        return this.mPreviewing;
    }

    public boolean isCaptureIntent() {
        return this.mIsVideoCaptureIntent;
    }

    public boolean isMeteringAreaOnly() {
        return !this.mFocusAreaSupported ? this.mMeteringAreaSupported : false;
    }

    public boolean isNeedMute() {
        return (super.isNeedMute() || this.mObjectTrackingStarted) ? true : this.mMediaRecorderRecording && !this.mMediaRecorderRecordingPaused;
    }

    protected boolean isProfileExist(int i, Integer num) {
        return CamcorderProfile.hasProfile(i, num.intValue());
    }

    protected boolean isShowHFRDuration() {
        return true;
    }

    public boolean isVideoRecording() {
        return !this.mIsFromStop ? this.mMediaRecorderRecording : false;
    }

    protected boolean isZoomEnabled() {
        return this.mVideoSavingTask == null && !CameraSettings.isSwitchOn("pref_camera_stereo_mode_key");
    }

    public void notifyError() {
        super.notifyError();
        if (currentIsMainThread()) {
            onStopVideoRecording(this.mPaused);
            if (this.mPaused) {
                closeCamera();
            }
        }
    }

    public void onAutoFocus(boolean z, Camera camera) {
        if (!this.mPaused && !this.mActivity.getCameraScreenNail().isModuleSwitching()) {
            Log.v("videocamera", "mAutoFocusTime = " + (System.currentTimeMillis() - this.mFocusStartTime) + "ms focused=" + z + " waitforrecording=" + this.mFocusManager.isFocusingSnapOnFinish());
            if (this.mFocusManager.isFocusingSnapOnFinish()) {
                this.mInStartingFocusRecording = false;
                record();
            }
            if (!this.mObjectTrackingStarted) {
                if (z) {
                    getUIController().getFocusView().showSuccess();
                    if (!isNeedMute() && this.mIsTouchFocused) {
                        playCameraSound(1);
                    }
                } else {
                    getUIController().getFocusView().showFail();
                }
            }
            this.mFocusManager.onAutoFocus(z);
            this.mActivity.getSensorStateManager().reset();
        }
    }

    public void onAutoFocusMoving(boolean z, Camera camera) {
        Log.v("videocamera", "onAutoFocusMoving moving= " + z);
        if (!this.mPaused && !this.mMediaRecorderRecording && !this.mActivity.getCameraScreenNail().isModuleSwitching()) {
            getUIController().getFocusView().setFocusType(false);
            if (z) {
                getUIController().getFocusView().showStart();
            } else if (this.mCameraDevice.isFocusSuccessful()) {
                getUIController().getFocusView().showSuccess();
            } else {
                getUIController().getFocusView().showFail();
            }
        }
    }

    public boolean onBackPressed() {
        if (this.mPaused) {
            return true;
        }
        if (this.mStereoSwitchThread != null) {
            return false;
        }
        if (this.mMediaRecorderRecording) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - this.mLastBackPressedTime > 3000) {
                this.mLastBackPressedTime = currentTimeMillis;
                Toast.makeText(this.mActivity, getString(C0049R.string.record_back_pressed_hint), 0).show();
            } else {
                onStopVideoRecording(false);
            }
            return true;
        } else if (getUIController().onBack()) {
            return true;
        } else {
            if (CameraSettings.isSwitchOn("pref_camera_stereo_mode_key")) {
                getUIController().getStereoButton().setStereoValue(false, true, true);
                return true;
            } else if (getUIController().getSettingPage().isItemSelected() && getUIController().getSettingPage().resetSettings()) {
                return true;
            } else {
                if (!getUIController().getPreviewPage().isPopupShown()) {
                    return super.onBackPressed();
                }
                PopupManager.getInstance(this.mActivity).notifyShowPopup(null, 1);
                return true;
            }
        }
    }

    protected void onCameraOpen() {
    }

    public boolean onCameraPickerClicked(int i) {
        if (this.mPaused || this.mPendingSwitchCameraId != -1 || this.mSwitchingCamera) {
            return false;
        }
        Log.d("videocamera", "Start to copy texture.");
        if (Device.isMDPRender()) {
            this.mActivity.setBlurFlag(true);
            this.mHandler.sendEmptyMessage(5);
        } else {
            this.mActivity.getCameraScreenNail().animateSwitchCopyTexture();
        }
        this.mPendingSwitchCameraId = i;
        enableCameraControls(false);
        this.mSwitchingCamera = true;
        return true;
    }

    public void onCreate(com.android.camera.Camera camera) {
        super.onCreate(camera);
        this.mActivity.createContentView();
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
        this.mIsVideoCaptureIntent = this.mActivity.isVideoCaptureIntent();
        CameraSettingPreferences.instance().setLocalId(getPreferencesLocalId());
        EffectController.getInstance().setEffect(0);
        V6ModulePicker.setCurrentModule(1);
        getUIController().onCreate();
        getUIController().useProperView();
        this.mActivity.getSensorStateManager().setSensorStateListener(this.mSensorStateListener);
        CameraDataAnalytics.instance().trackEventTime("open_camera_times_key");
        CameraOpenThread cameraOpenThread = null;
        boolean checkCameraLaunchPermissions = PermissionManager.checkCameraLaunchPermissions();
        if (checkCameraLaunchPermissions) {
            cameraOpenThread = new CameraOpenThread();
            cameraOpenThread.start();
        }
        initializeMiscControls();
        this.mActivity.createCameraScreenNail(!this.mIsVideoCaptureIntent, false);
        if (cameraOpenThread != null) {
            try {
                cameraOpenThread.join();
            } catch (InterruptedException e) {
            }
        }
        if (hasCameraException()) {
            onCameraException();
            return;
        }
        initializeFocusManager();
        if (checkCameraLaunchPermissions) {
            onCameraOpen();
            initializeCapabilities();
            readVideoPreferences();
            prepareUIByPreviewSize();
            if (!Device.isMDPRender() || getUIController().getSurfaceViewFrame().isSurfaceViewAvailable()) {
                Thread thread = new Thread(new C01154());
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e2) {
                }
                onPreviewStart();
            }
            resizeForPreviewAspectRatio();
        }
        this.mQuickCapture = this.mActivity.getIntent().getBooleanExtra("android.intent.extra.quickCapture", false);
        getUIController().getObjectView().setObjectViewListener(this);
        showFirstUseHintIfNeeded();
        ignoreTouchEvent(true);
        this.mTelephonyManager = (TelephonyManager) this.mActivity.getSystemService("phone");
    }

    public void onError(MediaRecorder mediaRecorder, int i, int i2) {
        Log.e("videocamera", "MediaRecorder error. what=" + i + ". extra=" + i2);
        if (i == 1) {
            stopVideoRecordingAsync();
            this.mActivity.getScreenHint().updateHint();
        }
    }

    public void onFaceDetection(Face[] faceArr, Camera camera) {
        CameraHardwareFace[] convertCameraHardwareFace = CameraHardwareFace.convertCameraHardwareFace(faceArr);
        if (Device.isSupportedObjectTrack() && convertCameraHardwareFace.length > 0 && convertCameraHardwareFace[0].faceType == 64206 && this.mObjectTrackingStarted) {
            getUIController().getObjectView().setObject(convertCameraHardwareFace[0]);
        }
    }

    public boolean onGestureTrack(RectF rectF, boolean z) {
        return (this.mInStartingFocusRecording || isVideoProcessing() || this.mSwitchingCamera || !isBackCamera() || !Device.isSupportedObjectTrack() || CameraSettings.is4KHigherVideoQuality(this.mQuality) || this.mIsVideoCaptureIntent) ? false : initializeObjectTrack(rectF, z);
    }

    public void onInfo(MediaRecorder mediaRecorder, int i, int i2) {
        if (i == 800) {
            if (this.mMediaRecorderRecording) {
                onStopVideoRecording(false);
            }
        } else if (i == 801 && this.mMediaRecorderRecording) {
            Log.v("videocamera", "reached max size " + this.mCurrentFileNumber);
            if (-1 < this.mCurrentFileNumber) {
                onRestartVideoRecording();
                return;
            }
            onStopVideoRecording(false);
            if (!this.mActivity.getScreenHint().isScreenHintVisible()) {
                Toast.makeText(this.mActivity, C0049R.string.video_reach_size_limit, 1).show();
            }
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        boolean z = false;
        if (this.mPaused) {
            return true;
        }
        switch (i) {
            case 24:
            case 25:
                if (getUIController().getPreviewPage().isPreviewPageVisible()) {
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
                if (keyEvent.getRepeatCount() == 0 && getUIController().getPreviewPage().isPreviewPageVisible()) {
                    onShutterButtonClick();
                    if (Util.isFingerPrintKeyEvent(keyEvent)) {
                        CameraDataAnalytics.instance().trackEvent("record_times_finger");
                    }
                    return true;
                }
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        switch (i) {
            case 27:
            case 66:
                getUIController().getShutterButton().setPressed(false);
                return true;
            default:
                return super.onKeyUp(i, keyEvent);
        }
    }

    public void onNewIntent() {
        this.mCameraId = getPreferredCameraId();
        changeConflictPreference();
        this.mIsVideoCaptureIntent = this.mActivity.isVideoCaptureIntent();
        CameraSettingPreferences.instance().setLocalId(getPreferencesLocalId());
    }

    public void onObjectStable() {
        RectF focusRect = getUIController().getObjectView().getFocusRect();
        if (focusRect != null && this.mFocusManager.canAutoFocus()) {
            autoFocus((int) focusRect.centerX(), (int) focusRect.centerY(), (int) focusRect.width(), (int) focusRect.height(), 2);
        }
    }

    public void onPauseAfterSuper() {
        if (!isVideoRecording() || isVideoProcessing()) {
            super.onPauseAfterSuper();
        }
    }

    public void onPauseBeforeSuper() {
        super.onPauseBeforeSuper();
        if (!isVideoRecording() || isVideoProcessing()) {
            waitStereoSwitchThread();
            getUIController().onPause();
            if (this.mMediaRecorderRecording) {
                stopObjectTracking(false);
                onStopVideoRecording(true);
                closeCamera();
            } else {
                if (this.mActivity.isGotoGallery() ? Device.isReleaseLaterForGallery() : false) {
                    this.mWaitForRelease = true;
                } else {
                    releaseResources();
                }
            }
            updateStereoSettings(false);
            closeVideoFileDescriptor();
            if (this.mReceiver != null) {
                this.mActivity.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
            this.mActivity.getSensorStateManager().reset();
            resetScreenOn();
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(5);
            this.mHandler.removeMessages(6);
            this.mHandler.removeMessages(17);
            this.mHandler.removeMessages(18);
            this.mHandler.removeMessages(19);
            this.mHandler.removeMessages(14);
            if (this.mLoadThumbnailTask != null) {
                this.mLoadThumbnailTask.cancel(true);
            }
            if (this.mHandler.hasMessages(15)) {
                this.mHandler.removeMessages(15);
                showStoppingUI();
            }
            if (this.mHandler.hasCallbacks(this.mRestoreRunnable)) {
                this.mHandler.removeCallbacks(this.mRestoreRunnable);
                this.mRestoreRunnable.run();
            }
            if (!(this.mActivity.isActivityPaused() || CameraSettings.isSwitchOn("pref_camera_stereo_mode_key"))) {
                PopupManager.getInstance(this.mActivity).notifyShowPopup(null, 1);
            }
            this.mPendingSwitchCameraId = -1;
            this.mSwitchingCamera = false;
        }
    }

    public void onPauseButtonClick() {
        Log.i("videocamera", "mVideoPauseResumeListener.onClick() mMediaRecorderRecordingPaused=" + this.mMediaRecorderRecordingPaused + ",mRecorderBusy = " + this.mRecorderBusy + ",mMediaRecorderRecording = " + this.mMediaRecorderRecording);
        long currentTimeMillis = System.currentTimeMillis();
        if (!this.mRecorderBusy && this.mMediaRecorderRecording && currentTimeMillis - this.mPauseClickTime >= 500) {
            this.mPauseClickTime = currentTimeMillis;
            this.mRecorderBusy = true;
            if (this.mMediaRecorderRecordingPaused) {
                getUIController().getPauseRecordingButton().setImageResource(C0049R.drawable.ic_recording_pause);
                try {
                    resumeMediaRecorder(this.mMediaRecorder);
                    this.mRecordingStartTime = SystemClock.uptimeMillis() - this.mVideoRecordedDuration;
                    this.mVideoRecordedDuration = 0;
                    this.mMediaRecorderRecordingPaused = false;
                    this.mHandler.removeMessages(3);
                    this.mRecordingTime = "";
                    updateRecordingTime();
                } catch (Throwable e) {
                    Log.e("videocamera", "Could not start media recorder. ", e);
                    releaseMediaRecorder();
                }
            } else {
                pauseVideoRecording();
                CameraDataAnalytics.instance().trackEvent("video_pause_recording_times_key");
            }
            this.mRecorderBusy = false;
            Log.i("videocamera", "mVideoPauseResumeListener.onClick() end. mRecorderBusy=" + this.mRecorderBusy);
        }
    }

    public void onPreviewTextureCopied() {
        animateSwitchCamera();
        this.mHandler.sendEmptyMessage(5);
    }

    public void onResumeAfterSuper() {
        if (!isVideoRecording()) {
            super.onResumeAfterSuper();
            if (!this.mOpenCameraFail && !this.mCameraDisabled && PermissionManager.checkCameraLaunchPermissions()) {
                if (!(this.mIsVideoCaptureIntent && getUIController().getReviewDoneView().getVisibility() == 0)) {
                    getUIController().onResume();
                }
                if (this.mCameraDevice == null && (!this.mPreviewing || this.mWaitForRelease)) {
                    CameraDataAnalytics.instance().trackEventTime("open_camera_times_key");
                    openCamera();
                    if (hasCameraException()) {
                        onCameraException();
                        return;
                    }
                    onCameraOpen();
                    initializeCapabilities();
                    readVideoPreferences();
                    resizeForPreviewAspectRatio();
                    showFirstUseHintIfNeeded();
                }
                updateStereoSettings(true);
                if (!this.mPreviewing || this.mWaitForRelease) {
                    if (!Device.isMDPRender() || getUIController().getSurfaceViewFrame().isSurfaceViewAvailable()) {
                        startPreview();
                        onPreviewStart();
                    }
                    this.mWaitForRelease = false;
                }
                initializeZoom();
                initializeExposureCompensation();
                keepScreenOnAwhile();
                IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
                intentFilter.addAction("android.intent.action.MEDIA_EJECT");
                intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
                intentFilter.addAction("android.intent.action.MEDIA_SCANNER_STARTED");
                intentFilter.addAction("android.intent.action.MEDIA_SCANNER_FINISHED");
                intentFilter.addDataScheme("file");
                this.mReceiver = new MyBroadcastReceiver();
                this.mActivity.registerReceiver(this.mReceiver, intentFilter);
                if (!this.mIsVideoCaptureIntent) {
                    this.mActivity.getThumbnailUpdater().getLastThumbnail();
                }
                onSettingsBack();
                if (this.mPreviewing) {
                    this.mOnResumeTime = SystemClock.uptimeMillis();
                    this.mHandler.sendEmptyMessageDelayed(1, 100);
                }
                this.mActivity.loadCameraSound(2);
                this.mActivity.loadCameraSound(3);
                if (getUIController().getReviewDoneView().getVisibility() == 0) {
                    ignoreTouchEvent(true);
                }
            }
        }
    }

    public void onResumeBeforeSuper() {
        super.onResumeBeforeSuper();
    }

    @OnClickAttr
    public void onReviewCancelClicked(View view) {
        if (isSelectingCapturedVideo()) {
            deleteCurrentVideo();
            hideAlert();
            return;
        }
        stopVideoRecordingAsync();
        doReturnToCaller(false);
    }

    @OnClickAttr
    public void onReviewDoneClicked(View view) {
        doReturnToCaller(true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSharedPreferenceChanged() {
        /*
        r5 = this;
        r2 = r5.mPaused;
        if (r2 == 0) goto L_0x0005;
    L_0x0004:
        return;
    L_0x0005:
        r3 = r5.mPreferences;
        monitor-enter(r3);
        r2 = r5.mCameraDevice;	 Catch:{ all -> 0x004e }
        if (r2 != 0) goto L_0x000e;
    L_0x000c:
        monitor-exit(r3);
        return;
    L_0x000e:
        r2 = r5.mPreferences;	 Catch:{ all -> 0x004e }
        r0 = com.android.camera.CameraSettings.isRecordLocation(r2);	 Catch:{ all -> 0x004e }
        r2 = com.android.camera.LocationManager.instance();	 Catch:{ all -> 0x004e }
        r2.recordLocation(r0);	 Catch:{ all -> 0x004e }
        r5.readVideoPreferences();	 Catch:{ all -> 0x004e }
        r2 = r5.mParameters;	 Catch:{ all -> 0x004e }
        r1 = r2.getPreviewSize();	 Catch:{ all -> 0x004e }
        r2 = r1.width;	 Catch:{ all -> 0x004e }
        r4 = r5.mDesiredPreviewWidth;	 Catch:{ all -> 0x004e }
        if (r2 != r4) goto L_0x0030;
    L_0x002a:
        r2 = r1.height;	 Catch:{ all -> 0x004e }
        r4 = r5.mDesiredPreviewHeight;	 Catch:{ all -> 0x004e }
        if (r2 == r4) goto L_0x0046;
    L_0x0030:
        r5.stopPreview();	 Catch:{ all -> 0x004e }
        r5.resizeForPreviewAspectRatio();	 Catch:{ all -> 0x004e }
        r5.startPreview();	 Catch:{ all -> 0x004e }
    L_0x0039:
        monitor-exit(r3);
        r2 = r5.getUIController();
        r2 = r2.getSettingsStatusBar();
        r2.updateStatus();
        return;
    L_0x0046:
        r2 = r5.mRestartPreview;	 Catch:{ all -> 0x004e }
        if (r2 != 0) goto L_0x0030;
    L_0x004a:
        r5.setCameraParameters();	 Catch:{ all -> 0x004e }
        goto L_0x0039;
    L_0x004e:
        r2 = move-exception;
        monitor-exit(r3);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.VideoModule.onSharedPreferenceChanged():void");
    }

    public void onShutterButtonClick() {
        Log.v("videocamera", "onShutterButtonClick mSwitchingCamera=" + this.mSwitchingCamera + " mMediaRecorderRecording=" + this.mMediaRecorderRecording + " mInStartingFocusRecording=" + this.mInStartingFocusRecording);
        this.mInStartingFocusRecording = false;
        if (!this.mSwitchingCamera && getUIController().getShutterButton().isShown() && getUIController().getShutterButton().isEnabled()) {
            boolean z = this.mMediaRecorderRecording;
            this.mHandler.removeMessages(24);
            if (z) {
                onStopVideoRecording(false);
                updateParametersAfterRecording();
            } else if (checkCallingState()) {
                this.mActivity.getScreenHint().updateHint();
                if (Storage.isLowStorageAtLastPoint()) {
                    Log.v("videocamera", "Storage issue, ignore the start request");
                    return;
                }
                enableCameraControls(false);
                if (this.mFocusManager.canRecord()) {
                    record();
                } else {
                    Log.v("videocamera", "wait for autofocus");
                    this.mInStartingFocusRecording = true;
                }
            } else {
                return;
            }
            getUIController().getShutterButton().enableControls(false);
        }
    }

    public void onShutterButtonFocus(boolean z) {
        Log.v("videocamera", "onShutterButtonFocus " + this.mFocusManager.isInValidFocus());
        if (z && !this.mSwitchingCamera && getUIController().getShutterButton().isEnabled() && !isVideoRecording() && isBackCamera() && this.mFocusManager.isInValidFocus()) {
            getUIController().getFocusView().clear();
            getUIController().getFocusView().setFocusType(false);
            autoFocus(getUIController().getPreviewFrame().getWidth() / 2, getUIController().getPreviewFrame().getHeight() / 2, this.mFocusManager.getDefaultFocusAreaWidth(), this.mFocusManager.getDefaultFocusAreaHeight(), 4);
            this.mInStartingFocusRecording = true;
        } else if ("continuous-video".equals(this.mVideoFocusMode) && getUIController().getFocusView().isShown()) {
            getUIController().getFocusView().clear();
        }
    }

    public boolean onShutterButtonLongClick() {
        return false;
    }

    public void onSingleTapUp(int i, int i2) {
        if (!this.mPaused && !this.mSnapshotInProgress && !isFrontCamera() && isInTapableRect(i, i2) && !this.mActivity.getCameraScreenNail().isModuleSwitching()) {
            if (!isVideoRecording()) {
                getUIController().getPreviewPage().simplifyPopup(true, true);
            }
            if (this.mObjectTrackingStarted) {
                stopObjectTracking(false);
            }
            this.mHandler.removeMessages(24);
            getUIController().getFocusView().setFocusType(true);
            this.mIsTouchFocused = true;
            this.mTouchFocusStartingTime = System.currentTimeMillis();
            Point point = new Point(i, i2);
            mapTapCoordinate(point);
            autoFocus(point.x, point.y, this.mFocusManager.getDefaultFocusAreaWidth(), this.mFocusManager.getDefaultFocusAreaHeight(), 3);
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mMediaRecorderRecording) {
            this.mIsFromStop = true;
            onPauseBeforeSuper();
            this.mActivity.pause();
            onPauseAfterSuper();
            this.mIsFromStop = false;
        }
        if (this.mActivity.isNeedResetGotoGallery() && Device.isReleaseLaterForGallery()) {
            releaseResources();
        }
    }

    public void onStopRecording() {
        AutoLockManager.getInstance(this.mActivity).hibernateDelayed();
    }

    public void onSwitchAnimationDone() {
        this.mHandler.sendEmptyMessage(16);
    }

    @OnClickAttr
    public void onThumbnailClicked(View view) {
        if (!this.mMediaRecorderRecording && this.mActivity.getThumbnailUpdater().getThumbnail() != null && !this.mSwitchingCamera) {
            this.mActivity.gotoGallery();
        }
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        if (!this.mMediaRecorderRecording) {
            keepScreenOnAwhile();
        }
    }

    protected void openSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(this.mActivity, CameraPreferenceActivity.class);
        intent.putExtra("from_where", 2);
        intent.putExtra(":miui:starting_window_label", getResources().getString(C0049R.string.pref_camera_settings_category));
        if (this.mActivity.startFromKeyguard()) {
            intent.putExtra("StartActivityWhenLocked", true);
        }
        this.mActivity.startActivity(intent);
        this.mActivity.setJumpFlag(2);
        CameraDataAnalytics.instance().trackEvent("pref_settings");
    }

    protected void pauseMediaRecorder(MediaRecorder mediaRecorder) {
        ReflectUtil.callMethod(MediaRecorder.class, this.mMediaRecorder, "pause", "()V", new Object[0]);
    }

    protected void performVolumeKeyClicked(int i, boolean z) {
        if (i == 0 && z) {
            onShutterButtonClick();
        }
    }

    protected void prepareRecording() {
    }

    protected void readVideoPreferences() {
        int videoQuality = CameraSettings.getVideoQuality();
        Intent intent = this.mActivity.getIntent();
        if (intent.hasExtra("android.intent.extra.videoQuality")) {
            videoQuality = intent.getIntExtra("android.intent.extra.videoQuality", 0) > 0 ? 1 : 0;
        }
        this.mHfr = CameraSettings.getVideoSpeed(this.mPreferences);
        this.mTimeBetweenTimeLapseFrameCaptureMs = 0;
        this.mCaptureTimeLapse = false;
        if ("fast".equals(this.mHfr)) {
            this.mTimeBetweenTimeLapseFrameCaptureMs = Integer.parseInt(this.mPreferences.getString("pref_video_time_lapse_frame_interval_key", getString(C0049R.string.pref_video_time_lapse_frame_interval_default)));
            this.mCaptureTimeLapse = this.mTimeBetweenTimeLapseFrameCaptureMs != 0;
            if (this.mCaptureTimeLapse) {
                videoQuality += 1000;
                if (videoQuality < 1000 || videoQuality > 1018) {
                    videoQuality -= 1000;
                    Editor edit = this.mPreferences.edit();
                    edit.putString("pref_video_speed_key", "normal");
                    edit.apply();
                    this.mCaptureTimeLapse = false;
                    RotateTextToast.getInstance(this.mActivity).show(C0049R.string.time_lapse_error, this.mOrientation);
                    getUIController().getSettingPage().reload();
                }
            }
            this.mQuality = videoQuality % 1000;
        } else {
            this.mQuality = videoQuality;
            if ("slow".equals(this.mHfr)) {
                videoQuality = getHFRQuality(this.mCameraId, videoQuality);
            }
        }
        if (!(this.mProfile == null || this.mProfile.quality % 1000 == this.mQuality)) {
            stopObjectTracking(false);
        }
        this.mProfile = fetchProfile(this.mCameraId, videoQuality);
        Log.v("videocamera", "readVideoPreferences: frameRate=" + this.mProfile.videoFrameRate + ", w=" + this.mProfile.videoFrameWidth + ", h=" + this.mProfile.videoFrameHeight + ", codec=" + this.mProfile.videoCodec);
        getDesiredPreviewSize();
        if (intent.hasExtra("android.intent.extra.durationLimit")) {
            this.mMaxVideoDurationInMs = intent.getIntExtra("android.intent.extra.durationLimit", 0) * 1000;
        } else if (!CameraSettings.is4KHigherVideoQuality(this.mQuality) || this.mCaptureTimeLapse) {
            this.mMaxVideoDurationInMs = 0;
        } else {
            this.mMaxVideoDurationInMs = 480000;
        }
        if (this.mMaxVideoDurationInMs != 0 && this.mMaxVideoDurationInMs < 1000) {
            this.mMaxVideoDurationInMs = 1000;
        }
    }

    public void record() {
        Log.v("videocamera", "record");
        playCameraSound(2);
        startVideoRecording();
        this.mHandler.sendEmptyMessageDelayed(4, 500);
    }

    protected void resumeMediaRecorder(MediaRecorder mediaRecorder) {
        if (VERSION.SDK_INT < 24) {
            this.mMediaRecorder.start();
        } else {
            ReflectUtil.callMethod(MediaRecorder.class, this.mMediaRecorder, "resume", "()V", new Object[0]);
        }
    }

    protected void sendOpenFailMessage() {
        this.mHandler.sendEmptyMessage(18);
    }

    protected void setCameraParameters() {
        updateVideoParametersPreference();
        this.mCameraDevice.setParameters(this.mParameters);
        this.mParameters = this.mCameraDevice.getParameters();
        if (!this.mSwitchingCamera || CameraSettings.sCroppedIfNeeded) {
            updateCameraScreenNailSize(this.mDesiredPreviewWidth, this.mDesiredPreviewHeight, this.mFocusManager);
        }
    }

    protected void setDisplayOrientation() {
        super.setDisplayOrientation();
        if (this.mFocusManager != null) {
            this.mFocusManager.setDisplayOrientation(this.mCameraDisplayOrientation);
        }
    }

    protected void setHFRSpeed(MediaRecorder mediaRecorder, int i) {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startObjectTracking() {
        /*
        r3 = this;
        r0 = "videocamera";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "startObjectTracking mObjectTrackingStarted=";
        r1 = r1.append(r2);
        r2 = r3.mObjectTrackingStarted;
        r1 = r1.append(r2);
        r1 = r1.toString();
        android.util.Log.i(r0, r1);
        r0 = r3.mObjectTrackingStarted;
        if (r0 != 0) goto L_0x0024;
    L_0x0020:
        r0 = r3.mPaused;
        if (r0 == 0) goto L_0x0025;
    L_0x0024:
        return;
    L_0x0025:
        r0 = r3.mCameraDevice;
        if (r0 == 0) goto L_0x00a5;
    L_0x0029:
        r0 = com.android.camera.Device.isSupportedObjectTrack();
        if (r0 == 0) goto L_0x00a5;
    L_0x002f:
        r0 = 1;
        r3.mObjectTrackingStarted = r0;
        r0 = "continuous-video";
        r1 = r3.mParameters;
        r1 = r1.getFocusMode();
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x0066;
    L_0x0041:
        r0 = "auto";
        r1 = sProxy;
        r2 = r3.mParameters;
        r1 = r1.getSupportedFocusModes(r2);
        r0 = com.android.camera.module.BaseModule.isSupported(r0, r1);
        if (r0 == 0) goto L_0x0066;
    L_0x0052:
        r0 = sProxy;
        r1 = r3.mParameters;
        r2 = "auto";
        r0.setFocusMode(r1, r2);
        r3.updateMotionFocusManager();
        r0 = r3.mCameraDevice;
        r1 = r3.mParameters;
        r0.setParameters(r1);
    L_0x0066:
        r3.updateAutoFocusMoveCallback();
        r0 = r3.mCameraDevice;
        r0.setFaceDetectionListener(r3);
        r0 = "videocamera";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "startObjectTracking rect=";
        r1 = r1.append(r2);
        r2 = r3.getUIController();
        r2 = r2.getObjectView();
        r2 = r2.getFocusRectInPreviewFrame();
        r1 = r1.append(r2);
        r1 = r1.toString();
        android.util.Log.i(r0, r1);
        r0 = r3.mCameraDevice;
        r1 = r3.getUIController();
        r1 = r1.getObjectView();
        r1 = r1.getFocusRectInPreviewFrame();
        r0.startObjectTrack(r1);
    L_0x00a5:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.module.VideoModule.startObjectTracking():void");
    }

    protected void startPreview() {
        Log.v("videocamera", "startPreview " + this.mPreviewing);
        if (this.mCameraDevice != null && this.mFocusManager != null) {
            this.mCameraDevice.setErrorCallback(this.mErrorCallback);
            if (this.mPreviewing) {
                stopPreview();
            }
            setDisplayOrientation();
            this.mCameraDevice.setDisplayOrientation(this.mCameraDisplayOrientation);
            setCameraParameters();
            try {
                if (Device.isMDPRender()) {
                    SurfaceHolder surfaceHolder = getUIController().getSurfaceViewFrame().getSurfaceHolder();
                    if (surfaceHolder == null) {
                        Log.w("videocamera", "startPreview: holder for preview are not ready.");
                        return;
                    }
                    this.mCameraDevice.setPreviewDisplay(surfaceHolder);
                } else {
                    this.mCameraDevice.setPreviewTexture(this.mActivity.getCameraScreenNail().getSurfaceTexture());
                }
                this.mCameraDevice.startPreviewAsync();
                this.mFocusManager.resetFocused();
                this.mPreviewing = true;
                manuallyTriggerAutoFocus();
            } catch (Throwable e) {
                closeCamera();
                throw new RuntimeException("startPreview or setPreviewSurfaceTexture failed", e);
            }
        }
    }

    protected boolean startRecordVideo() {
        initializeRecorder();
        if (this.mMediaRecorder == null) {
            Log.e("videocamera", "Fail to initialize media recorder");
            return false;
        }
        try {
            this.mMediaRecorder.start();
            return true;
        } catch (Throwable e) {
            Log.e("videocamera", "Could not start media recorder. ", e);
            if (e instanceof IllegalStateException) {
                this.mActivity.getScreenHint().showConfirmMessage(C0049R.string.confirm_recording_fail_title, C0049R.string.confirm_recording_fail_recorder_busy_alert);
            }
            releaseMediaRecorder();
            this.mCameraDevice.lock();
            return false;
        }
    }

    public void stopObjectTracking(boolean z) {
        Log.i("videocamera", "stopObjectTracking mObjectTrackingStarted=" + this.mObjectTrackingStarted);
        if (this.mObjectTrackingStarted) {
            if (this.mCameraDevice != null) {
                this.mObjectTrackingStarted = false;
                this.mCameraDevice.setFaceDetectionListener(null);
                this.mCameraDevice.stopObjectTrack();
                if (!this.mInStartingFocusRecording && this.mFocusManager.isNeedCancelAutoFocus()) {
                    this.mCameraDevice.cancelAutoFocus();
                    this.mFocusManager.cancelAutoFocus();
                }
                if (!(this.mPaused || getUIController().getObjectView().isAdjusting())) {
                    setCameraParameters();
                }
                getUIController().getObjectView().clear();
                getUIController().getObjectView().setVisibility(8);
            }
            return;
        }
        if (!(!this.mPaused || getUIController().getObjectView() == null || getUIController().getObjectView().getVisibility() == 8)) {
            getUIController().getObjectView().clear();
            getUIController().getObjectView().setVisibility(8);
        }
    }

    protected void stopPreview() {
        Log.v("videocamera", "stopPreview");
        if (currentIsMainThread()) {
            stopObjectTracking(false);
        }
        this.mHandler.removeMessages(24);
        this.mCameraDevice.stopPreview();
        this.mPreviewing = false;
        if (this.mFocusManager != null) {
            this.mFocusManager.resetFocused();
        }
    }

    protected boolean subStopRecording() {
        boolean z;
        synchronized (this) {
            z = false;
            if (this.mMediaRecorderRecording) {
                try {
                    this.mMediaRecorder.setOnErrorListener(null);
                    this.mMediaRecorder.setOnInfoListener(null);
                    this.mMediaRecorder.stop();
                    z = true;
                    this.mCurrentVideoFilename = this.mVideoFilename;
                    Log.v("videocamera", "stopVideoRecording: Setting current video filename: " + this.mCurrentVideoFilename);
                } catch (Throwable e) {
                    Log.e("videocamera", "stop fail", e);
                    if (this.mVideoFilename != null) {
                        deleteVideoFile(this.mVideoFilename);
                    }
                    synchronized (this.mTaskLock) {
                        this.mMediaRecorderRecording = false;
                        if (!isVideoProcessing()) {
                            this.mHandler.postAtFrontOfQueue(this.mRestoreRunnable);
                        }
                    }
                }
                if (this.mPaused) {
                    closeCamera();
                }
            }
            releaseMediaRecorder();
        }
        return z;
    }

    protected void switchMutexHDR() {
        if ("off".equals(getUIController().getHdrButton().getValue())) {
            this.mMutexModePicker.resetMutexMode();
        } else {
            this.mMutexModePicker.setMutexMode(2);
        }
    }

    protected void updateLoadUI(boolean z) {
        if (z) {
            this.mHandler.sendEmptyMessageDelayed(13, 500);
        } else {
            this.mHandler.removeMessages(13);
            getUIController().getCaptureProgressBar().setVisibility(8);
        }
        getUIController().getShutterButton().enableControls(!z);
    }

    protected void updateStatusBar(String str) {
        super.updateStatusBar(str);
        this.mHandler.removeMessages(17);
        if (!this.mPaused && this.mMediaRecorderRecording && !this.mRecorderBusy) {
            if ((getUIController().getVideoRecordingTimeView().isShown() || this.mCurrentShowIndicator != -1) && getUIController().getSettingsStatusBar().getVisibility() == 0) {
                this.mCurrentShowIndicator = -1;
                getUIController().getVideoRecordingTimeView().clearAnimation();
                getUIController().getVideoRecordingTimeView().setVisibility(4);
            }
            this.mHandler.sendEmptyMessageDelayed(17, 1000);
        }
    }

    protected void updateVideoParametersPreference() {
        Log.e("videocamera", "Preview dimension in App->" + this.mDesiredPreviewWidth + "X" + this.mDesiredPreviewHeight);
        this.mParameters.setPreviewSize(this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
        this.mVideoWidth = this.mProfile.videoFrameWidth;
        this.mVideoHeight = this.mProfile.videoFrameHeight;
        String str = this.mVideoWidth + "x" + this.mVideoHeight;
        Log.e("videocamera", "Video dimension in App->" + str);
        this.mParameters.set("video-size", str);
        List supportedFlashModes = this.mParameters.getSupportedFlashModes();
        String string = this.mPreferences.getString("pref_camera_video_flashmode_key", getString(C0049R.string.pref_camera_video_flashmode_default));
        if (BaseModule.isSupported(string, supportedFlashModes)) {
            this.mParameters.setFlashMode(string);
        }
        if (isBackCamera()) {
            List supportedFocusModes = sProxy.getSupportedFocusModes(this.mParameters);
            String string2 = this.mPreferences.getString("pref_video_focusmode_key", getString(C0049R.string.pref_video_focusmode_entryvalue_default));
            if (BaseModule.isSupported(string2, supportedFocusModes)) {
                if ("continuous-video".equals(string2)) {
                    this.mVideoFocusMode = "continuous-video";
                    getUIController().getFocusView().setFocusType(false);
                    this.mFocusManager.resetFocused();
                } else {
                    this.mVideoFocusMode = "auto";
                    getUIController().getFocusView().setFocusType(true);
                }
                sProxy.setFocusMode(this.mParameters, this.mVideoFocusMode);
                updateMotionFocusManager();
                updateAutoFocusMoveCallback();
            }
        }
        String string3 = this.mPreferences.getString("pref_camera_coloreffect_key", getString(C0049R.string.pref_camera_coloreffect_default));
        Log.e("videocamera", "Color effect value =" + string3);
        if (BaseModule.isSupported(string3, this.mParameters.getSupportedColorEffects())) {
            this.mParameters.setColorEffect(string3);
        }
        String str2 = "auto";
        if (BaseModule.isSupported(str2, this.mParameters.getSupportedWhiteBalance())) {
            this.mParameters.setWhiteBalance(str2);
        } else if (this.mParameters.getWhiteBalance() == null) {
            str2 = "auto";
        }
        if (this.mParameters.isZoomSupported()) {
            this.mParameters.setZoom(getZoomValue());
        }
        this.mParameters.setRecordingHint(true);
        if (!this.mParameters.isVideoStabilizationSupported() || ((Device.IS_X9 && (!"normal".equals(this.mHfr) || this.mQuality >= 6)) || CameraSettings.is4KHigherVideoQuality(this.mQuality) || !isBackCamera() || !CameraSettings.isMovieSolidOn(this.mPreferences))) {
            Log.v("videocamera", "set video stabilization to false");
            this.mParameters.setVideoStabilization(false);
            this.mActivity.getCameraScreenNail().setVideoStabilizationCropped(false);
        } else {
            Log.v("videocamera", "set video stabilization to true");
            this.mParameters.setVideoStabilization(true);
            this.mActivity.getCameraScreenNail().setVideoStabilizationCropped(true);
        }
        int i = Integer.MAX_VALUE;
        int i2 = Integer.MAX_VALUE;
        if (Device.isVideoSnapshotSizeLimited()) {
            i = this.mProfile.videoFrameWidth;
            i2 = this.mProfile.videoFrameHeight;
        }
        Size optimalVideoSnapshotPictureSize = Util.getOptimalVideoSnapshotPictureSize(this.mParameters.getSupportedPictureSizes(), ((double) this.mDesiredPreviewWidth) / ((double) this.mDesiredPreviewHeight), i, i2);
        Size pictureSize = this.mParameters.getPictureSize();
        if (pictureSize == null) {
            Log.v("videocamera", "get null pictureSize");
        } else if (!pictureSize.equals(optimalVideoSnapshotPictureSize)) {
            this.mParameters.setPictureSize(optimalVideoSnapshotPictureSize.width, optimalVideoSnapshotPictureSize.height);
        }
        Log.v("videocamera", "Video snapshot size is " + optimalVideoSnapshotPictureSize.width + "x" + optimalVideoSnapshotPictureSize.height);
        if (Device.isQcomPlatform()) {
            if (21 <= VERSION.SDK_INT) {
                Size pictureSize2 = this.mParameters.getPictureSize();
                optimalVideoSnapshotPictureSize = Util.getOptimalJpegThumbnailSize(this.mParameters.getSupportedJpegThumbnailSizes(), ((double) pictureSize2.width) / ((double) pictureSize2.height));
                if (!this.mParameters.getJpegThumbnailSize().equals(optimalVideoSnapshotPictureSize)) {
                    this.mParameters.setJpegThumbnailSize(optimalVideoSnapshotPictureSize.width, optimalVideoSnapshotPictureSize.height);
                }
                Log.v("videocamera", "Thumbnail size is " + optimalVideoSnapshotPictureSize.width + "x" + optimalVideoSnapshotPictureSize.height);
            } else {
                this.mParameters.setJpegThumbnailSize(0, 0);
            }
        }
        this.mParameters.setJpegQuality(CameraProfile.getJpegEncodingQualityParameter(this.mCameraId, 2));
        addMuteToParameters(this.mParameters);
        configOisParameters(this.mParameters, true);
        addT2TParameters(this.mParameters);
        resetFaceBeautyParams(this.mParameters);
        sProxy.clearExposureTime(this.mParameters);
        String string4 = (Device.isSupportedHFR() && "slow".equals(this.mHfr)) ? "off" : this.mPreferences.getString("pref_camera_antibanding_key", getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_camera_antibanding_default)));
        Log.v("videocamera", "antiBanding value =" + string4);
        if (BaseModule.isSupported(string4, this.mParameters.getSupportedAntibanding())) {
            this.mParameters.setAntibanding(string4);
        }
        int uIStyleByPreview = CameraSettings.getUIStyleByPreview(this.mDesiredPreviewWidth, this.mDesiredPreviewHeight);
        if (this.mUIStyle != uIStyleByPreview) {
            this.mUIStyle = uIStyleByPreview;
            if (this.mSwitchingCamera) {
                this.mHasPendingSwitching = true;
            } else {
                this.mHandler.sendEmptyMessage(11);
            }
        }
        if (this.mParameters.get("xiaomi-time-watermark") != null) {
            this.mParameters.set("xiaomi-time-watermark", "off");
        }
        if (this.mParameters.get("xiaomi-dualcam-watermark") != null) {
            this.mParameters.set("xiaomi-dualcam-watermark", "off");
        }
        if (this.mParameters.get("watermark") != null) {
            this.mParameters.set("watermark", "off");
        }
    }

    protected void waitForRecorder() {
        synchronized (this.mTaskLock) {
            if (this.mVideoSavingTask != null && this.mMediaRecorderRecording) {
                try {
                    Log.i("videocamera", "Wait for releasing camera done in MediaRecorder");
                    this.mTaskLock.wait();
                } catch (Throwable e) {
                    Log.w("videocamera", "Got notify from Media recorder()", e);
                }
            }
        }
    }
}
